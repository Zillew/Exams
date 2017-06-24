package com.dogonfire.exams;

//import java.util.Comparator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands
{
	private Exams	plugin	= null;

	Commands(Exams p)
	{
		this.plugin = p;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player = null;

		if ((sender instanceof Player))
		{
			player = (Player) sender;
		}

		if (player == null)
		{
			if (cmd.getName().equalsIgnoreCase("exams"))
			{
				if (args.length == 1)
				{
					if(args[0].equalsIgnoreCase("reload"))
					{
						plugin.reloadSettings();
						plugin.loadSettings();
						plugin.getExamManager().load();
						plugin.getStudentManager().load();

						return true;
					}
					else if (args[0].equalsIgnoreCase("clean"))
					{
						CommandClean(sender);
						
						return true;
					}
				}

				CommandExamList(player);
			}

			return true;
		}

		if (cmd.getName().equalsIgnoreCase("exams"))
		{
			if (args.length == 0)
			{
				CommandHelp(sender);
				plugin.log(sender.getName() + " /exam");
				return true;
			}
			if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("reload"))
				{
					if ((!player.isOp()) && (!player.hasPermission("exams.reload")))
					{
						return false;
					}

					this.plugin.loadSettings();
					this.plugin.getExamManager().load();
					this.plugin.getStudentManager().load();
					sender.sendMessage(this.plugin.getDescription().getFullName() + ": Reloaded configuration.");
					this.plugin.log(sender.getName() + " /exams reload");
					return true;
				}
				if (args[0].equalsIgnoreCase("help"))
				{
					if ((!player.isOp()) && (!player.hasPermission("exams.list")))
					{
						return false;
					}

					CommandList(sender);
					this.plugin.log(sender.getName() + " /exams help");
					return true;
				}
				if (args[0].equalsIgnoreCase("clean"))
				{
					if ((!player.isOp()) && (!player.hasPermission("exams.clean")))
					{
						return false;
					}

					CommandClean(sender);

					this.plugin.log(sender.getName() + " /exams clean");
					return true;
				}
				if ((args[0].equalsIgnoreCase("a")) || (args[0].equalsIgnoreCase("b")) || (args[0].equalsIgnoreCase("c")) || (args[0].equalsIgnoreCase("d")))
				{
					CommandAnswer(player, args[0].toLowerCase());
				}
				else
				{
					if (args[0].equalsIgnoreCase("list"))
					{
						if ((!player.isOp()) && (!player.hasPermission("exams.list")))
						{
							return false;
						}

						this.plugin.log(sender.getName() + " /exams list");
						return true;
					}

					sender.sendMessage(ChatColor.RED + "Invalid Exams command");
					return true;
				}
			}
			else
			{
				if (args.length == 2)
				{
					if (args[0].equalsIgnoreCase("info"))
					{
						if ((!player.isOp()) && (!player.hasPermission("exams.info")))
						{
							return false;
						}

						CommandInfo(sender, args[1]);
						this.plugin.log(sender.getName() + " /exams info " + args[1]);
						return true;
					}

					sender.sendMessage(ChatColor.RED + "Invalid Exams command");
					return true;
				}

				if (args.length > 3)
				{
					sender.sendMessage(ChatColor.RED + "Too many arguments!");
					return true;
				}
			}
		}
		return true;
	}

	private boolean CommandInfo(CommandSender sender, String examName)
	{
		return true;
	}

	private void CommandAnswer(Player player, String answer)
	{
		if (!plugin.getStudentManager().isDoingExam(player.getName()))
		{
			player.sendMessage(ChatColor.RED + "You are not taking any exam!");
			return;
		}

		plugin.getStudentManager().answer(player.getName(), answer);

		if (plugin.getExamManager().nextExamQuestion(player.getName()))
		{
			plugin.getExamManager().doExamQuestion(player.getName());
		}
		else
		{
			plugin.getExamManager().calculateExamResult(player.getName());
		}
	}

	private boolean CommandHelp(CommandSender sender)
	{
		sender.sendMessage(ChatColor.YELLOW + "------------------ " + plugin.getDescription().getFullName() + " ------------------");
		sender.sendMessage(ChatColor.AQUA + "By DogOnFire");
		sender.sendMessage(ChatColor.AQUA + "");
		sender.sendMessage(ChatColor.AQUA + "There are currently " + ChatColor.WHITE + plugin.getExamManager().getExams().size() + ChatColor.AQUA + " exams in " + this.plugin.serverName);
		sender.sendMessage(ChatColor.AQUA + "");
		sender.sendMessage(ChatColor.AQUA + "Use " + ChatColor.WHITE + "/exams help" + ChatColor.AQUA + " for a list of commands");

		return true;
	}

	private boolean CommandList(CommandSender sender)
	{
		sender.sendMessage(ChatColor.YELLOW + "------------------ " + this.plugin.getDescription().getFullName() + " ------------------");
		sender.sendMessage(ChatColor.AQUA + "/exams" + ChatColor.WHITE + " - Basic info");
		//sender.sendMessage(ChatColor.AQUA + "/exams list" + ChatColor.WHITE + " - List of all exams");
		sender.sendMessage(ChatColor.AQUA + "/exams a" + ChatColor.WHITE + " - Answer A to an exam question");
		sender.sendMessage(ChatColor.AQUA + "/exams b" + ChatColor.WHITE + " - Answer A to an exam question");
		sender.sendMessage(ChatColor.AQUA + "/exams c" + ChatColor.WHITE + " - Answer A to an exam question");
		sender.sendMessage(ChatColor.AQUA + "/exams d" + ChatColor.WHITE + " - Answer A to an exam question");
		sender.sendMessage(ChatColor.AQUA + "/exams reload" + ChatColor.WHITE + " - Reload gods system");

		return true;
	}

	private boolean CommandClean(CommandSender sender)
	{
		int students = 0;

		students = plugin.getExamManager().cleanStudentData();

		if(sender!=null)
		{
			sender.sendMessage(ChatColor.AQUA + "Cleaned up data for " + ChatColor.YELLOW + students + ChatColor.AQUA + " students");
		}
		
		plugin.log("Cleaned up data for " + students + " students");

		return true;
	}

	private void CommandExamList(CommandSender sender)
	{
	}
}