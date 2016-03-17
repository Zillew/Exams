package com.dogonfire.exams;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.dogonfire.exams.Metrics;

public class Exams extends JavaPlugin
{
	private ExamManager			examManager			= null;
	private StudentManager		studentManager		= null;
	private PermissionsManager	permissionManager	= null;

	private FileConfiguration	config				= null;
	private Commands			commands			= null;

	public boolean				debug				= false;
	public boolean				examPricesEnabled	= true;

	public String				serverName			= "Your Server";
	public String				languageFilename	= "english.yml";

	public int					minExamTime			= 60;
	public int 					autoCleanTime		= 8*60;
	public int					requiredExamScore	= 80;

	public ExamManager getExamManager()
	{
		return this.examManager;
	}

	public StudentManager getStudentManager()
	{
		return this.studentManager;
	}

	public PermissionsManager getPermissionsManager()
	{
		return this.permissionManager;
	}

	public void log(String message)
	{
		Logger.getLogger("minecraft").info("[" + getDescription().getFullName() + "] " + message);
	}

	public void logDebug(String message)
	{
		if (this.debug)
		{
			Logger.getLogger("minecraft").info("[" + getDescription().getFullName() + "] " + message);
		}
	}

	public void sendInfo(Player player, String message)
	{
		player.sendMessage(ChatColor.AQUA + message);
	}

	public void sendToAll(String message)
	{
		getServer().broadcastMessage(message);
	}

	public void sendMessage(String playerName, String message)
	{
		getServer().getPlayer(playerName).sendMessage(ChatColor.AQUA + message);
	}

	public void reloadSettings()
	{
		reloadConfig();

		loadSettings();

		examManager.load();
	    studentManager.load();
	}

	public void loadSettings()
	{
		config = getConfig();
		
		serverName = config.getString("ServerName", "Your Server");
		minExamTime = config.getInt("MinExamTime", 60);
		requiredExamScore = config.getInt("RequiredExamScore", 80);
		debug = config.getBoolean("Debug", false);
	}

	public void saveSettings()
	{
		config.set("ServerName", serverName);
		config.set("MinExamTime", minExamTime);
		config.set("RequiredExamScore", requiredExamScore);
		config.set("Debug", debug);

		saveConfig();
	}

	public void onEnable()
	{
		this.examManager = new ExamManager(this);
		this.studentManager = new StudentManager(this);
		this.permissionManager = new PermissionsManager(this);

		this.commands = new Commands(this);

		loadSettings();
		saveSettings();

		this.permissionManager.load();
		this.examManager.load();
		this.studentManager.load();

		getServer().getPluginManager().registerEvents(new BlockListener(this), this);

		try
		{
			Metrics metrics = new Metrics(this);

			metrics.addCustomData(new Metrics.Plotter("Using PermissionsBukkit")
			{
				public int getValue()
				{
					if (getPermissionsManager().getPermissionPluginName().equals("PermissionsBukkit"))
					{
						return 1;
					}
					return 0;
				}
			});

			metrics.addCustomData(new Metrics.Plotter("Using PermissionsEx")
			{
				public int getValue()
				{
					if (getPermissionsManager().getPermissionPluginName().equals("PermissionsEx"))
					{
						return 1;
					}
					return 0;
				}
			});

			metrics.addCustomData(new Metrics.Plotter("Using GroupManager")
			{
				public int getValue()
				{
					if (Exams.this.getPermissionsManager().getPermissionPluginName().equals("GroupManager"))
					{
						return 1;
					}
					return 0;
				}
			});

			metrics.addCustomData(new Metrics.Plotter("Using bPermissions")
			{
				public int getValue()
				{
					if (Exams.this.getPermissionsManager().getPermissionPluginName().equals("bPermissions"))
					{
						return 1;
					}
					return 0;
				}
			});

			metrics.start();
		}
		catch (Exception ex)
		{
			log("Failed to submit metrics :-(");
		}
	}

	public void onDisable()
	{
		//reloadSettings();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		return commands.onCommand(sender, cmd, label, args);
	}
}