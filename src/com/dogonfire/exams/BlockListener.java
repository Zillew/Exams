package com.dogonfire.exams;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener
{
	private Exams	plugin;

	BlockListener(Exams p)
	{
		this.plugin = p;
	}

	@EventHandler
	public void OnSignChange(SignChangeEvent event)
	{
		Player player = event.getPlayer();

		if (!plugin.getExamManager().isExamSign(event.getBlock(), event.getLines()))
		{
			return;
		}

		if (!player.isOp() && !plugin.getPermissionsManager().hasPermission(player, "exams.place"))
		{
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
			event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));

			plugin.sendInfo(player, ChatColor.RED + "You cannot place exams signs");

			return;
		}

		if (!plugin.getExamManager().handleNewExamSign(event))
		{
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
			event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
		}
	}

	@EventHandler
	public void OnPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!plugin.getExamManager().isExamSign(event.getClickedBlock()))
		{
			return;
		}

		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}

		String examName = plugin.getExamManager().getExamFromSign(event.getClickedBlock());

		if (examName == null)
		{
			return;
		}

		String currentExam = plugin.getStudentManager().getExamForStudent(player.getName());

		if(currentExam==null)
		{
			// Check for required RANK
			String requiredRank = plugin.getExamManager().getRequiredRankForExam(player.getName(), examName);
			if (requiredRank!=null && !plugin.getPermissionsManager().getGroup(player.getName()).equalsIgnoreCase(requiredRank))
			{
				plugin.sendInfo(event.getPlayer(), ChatColor.RED + "Only players with the " + ChatColor.YELLOW + requiredRank + ChatColor.RED + " rank can take this exam!");
				return;			
			}
	
			// Check for required EXAM
			String requiredExam = plugin.getExamManager().getUnpassedRequiredExamForExam(player.getName(), examName);
			
			if (requiredExam!=null)
			{
				plugin.sendInfo(event.getPlayer(), ChatColor.RED + "You must pass the " + ChatColor.YELLOW + requiredExam + ChatColor.RED + " exam before taking this exam!");
				return;			
			}

			if (plugin.getExamManager().signupForExam(player.getName(), examName))
			{
				plugin.sendMessage(player.getName(), ChatColor.AQUA + "Click the sign again to start this exam!");
			}
			else
			{
				return;
			}

			return;
		}

		if (!currentExam.equals(examName))
		{
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "You are already signed up for the " + ChatColor.YELLOW + currentExam + ChatColor.RED + " exam!");
			return;
		}
		
		if (plugin.getExamManager().isExamOpen(player.getWorld(), examName))
		{
			if (!plugin.getStudentManager().isDoingExam(player.getName()))
			{
				if (!plugin.getExamManager().generateExam(player.getName(), examName))
				{
					player.sendMessage(ChatColor.RED + "ERROR: Could not generate a " + ChatColor.YELLOW + examName + ChatColor.RED + "exam!");
					return;
				}

				plugin.sendMessage(player.getName(), "You started on the " + ChatColor.YELLOW + examName + ChatColor.AQUA + " exam.");
				plugin.sendMessage(player.getName(), "Click on the sign again to repeat the exam question.");
				plugin.sendMessage(player.getName(), "Good luck!");

				plugin.getExamManager().nextExamQuestion(player.getName());
			}

			plugin.getExamManager().doExamQuestion(player.getName());
		}
		else if (!plugin.getStudentManager().isDoingExam(player.getName()))
		{
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "The exam has not started yet!");
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "Please come back at " + ChatColor.YELLOW + plugin.getExamManager().getExamStartTime(examName) + ChatColor.RED + " minecraft time");
		}
		else
		{
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "The exam has ended!");
			plugin.getExamManager().calculateExamResult(event.getPlayer().getName());
			plugin.getStudentManager().removeStudent(player.getName());
		}
	}
}