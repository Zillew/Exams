package com.dogonfire.exams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class ExamManager
{
	private Exams				plugin;
	private FileConfiguration	examsConfig		= null;
	private File				examsConfigFile	= null;
	private Random				random			= new Random();
	private Economy				economy			= null;

	ExamManager(Exams p)
	{
		this.plugin = p;

		if (this.plugin.getServer().getPluginManager().getPlugin("Vault") != null)
		{
			RegisteredServiceProvider economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);

			if (economyProvider != null)
			{
				this.economy = ((Economy) economyProvider.getProvider());
				this.plugin.examPricesEnabled = true;
				this.plugin.log("Vault found, exam prices enabled.");
			}
			else
			{
				this.plugin.log("Vault not found, exam prices disabled.");
				this.plugin.examPricesEnabled = false;
			}
		}
		else
		{
			this.plugin.log("Vault not found, exam prices disabled.");
			this.plugin.examPricesEnabled = false;
		}
	}

	public void load()
	{
		if (examsConfigFile == null)
		{
			examsConfigFile = new File(plugin.getDataFolder(), "exams.yml");
		}
		
		examsConfig = YamlConfiguration.loadConfiguration(examsConfigFile);
		
		try
		{
			examsConfig.load(new InputStreamReader(new FileInputStream(examsConfigFile), Charset.forName("UTF-8")));
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(!examsConfigFile.exists())
		{
			String testExam = "Citizen";

			List<String> questions = new ArrayList<String>();
			
			questions.add("Is it ok to grief?");
			questions.add("Is it ok to spam?");
			questions.add("Can i become admin?");
			questions.add("Does admins give out free stuff?");
			questions.add("Is this a RPG server?");
			questions.add("Are you allowed to insult people?");

			this.examsConfig.set(testExam + ".RankName", "Citizen");
			this.examsConfig.set(testExam + ".StartTime", 600);
			this.examsConfig.set(testExam + ".EndTime", 13000);
			this.examsConfig.set(testExam + ".Price", 100);
			this.examsConfig.set(testExam + ".NumberOfQuestions", 3);
			this.examsConfig.set(testExam + ".Questions", questions);

			for (String question : questions)
			{
				List options = new ArrayList();
				options.add("Yes");
				options.add("No");
				options.add("Maybe");
				options.add("I dont know");

				this.examsConfig.set(testExam + ".Questions." + question + ".Options", options);
				this.examsConfig.set(testExam + ".Questions." + question + ".CorrectOption", "B");
			}
			
			
			testExam = "Wizard";

			questions = new ArrayList<String>();
			questions.add("What does a speed potion consist of?");
			questions.add("How do you spawn 4 pigs?");
			questions.add("Where do you become wizard?");
			questions.add("How do you cast a fireball spell?");
			questions.add("In which world are wizards enabled?");
			questions.add("How do you slay a dragon?");

			this.examsConfig.set(testExam + ".RankName", "Wizard");
			this.examsConfig.set(testExam + ".Command", "/give $PlayerName 38 1");
			this.examsConfig.set(testExam + ".StartTime", 600);
			this.examsConfig.set(testExam + ".EndTime", 13000);
			this.examsConfig.set(testExam + ".Price", 100);
			this.examsConfig.set(testExam + ".NumberOfQuestions", 3);
			this.examsConfig.set(testExam + ".Questions", questions);

			for (String question : questions)
			{
				List options = new ArrayList();
				options.add("Cobweb and spidereyes");
				options.add("Light and darkness");
				options.add("No idea");
				options.add("Blue monday");

				this.examsConfig.set(testExam + ".Questions." + question + ".Options", options);
				this.examsConfig.set(testExam + ".Questions." + question + ".CorrectOption", "A");
			}			
			
			save();			
		}

		//examsConfig = YamlConfiguration.loadConfiguration(examsConfigFile);

		if (examsConfig.getKeys(false).size() > 0)
		{
			this.plugin.log("Loaded " + examsConfig.getKeys(false).size() + " exams.");
		}
	}

	private void save()
	{
		if ((this.examsConfig == null) || (this.examsConfigFile == null))
		{
			return;
		}

		try
		{
			this.examsConfig.save(this.examsConfigFile);
		}
		catch (Exception ex)
		{
			this.plugin.log("Could not save config to " + this.examsConfigFile + ": " + ex.getMessage());
		}
	}

	public void update()
	{
	}

	public boolean isExamOpen(World world, String examName)
	{
		long time = world.getFullTime() % 24000L;

		long startTime = examsConfig.getLong(examName + ".StartTime");
		long endTime = examsConfig.getLong(examName + ".EndTime");

		if (startTime == endTime)
		{
			return true;
		}

		plugin.logDebug("Time is " + time);
		plugin.logDebug("Startime is " + startTime);
		plugin.logDebug("Endtime is " + endTime);

		return (time >= startTime) && (time <= endTime);
	}

	public String getExamFromSign(Block clickedBlock)
	{
		if (clickedBlock.getType() != Material.WALL_SIGN)
		{
			return null;
		}

		BlockState state = clickedBlock.getState();

		Sign sign = (Sign) state;

		String[] lines = sign.getLines();

		return lines[2];
	}
	
	public String getRequiredRankForExam(String playerName, String examName)
	{
		return examsConfig.getString(examName + ".RequiredRank");
	}
	
	public String getUnpassedRequiredExamForExam(String playerName, String examName)
	{
		String requiredExamName = "";
		
		requiredExamName = examsConfig.getString(examName + ".RequiredExam");

		for(String passedExam : plugin.getStudentManager().getPassedExams(playerName))
		{
			if(passedExam.equals(requiredExamName))
			{
				return null;
			}
		}

		return requiredExamName;
	}

	public boolean isExamSign(Block clickedBlock)
	{
		if ((clickedBlock == null) || (clickedBlock.getType() != Material.WALL_SIGN))
		{
			return false;
		}

		BlockState state = clickedBlock.getState();

		Sign sign = (Sign) state;

		String[] lines = sign.getLines();

		if (!lines[0].equalsIgnoreCase("Exam"))
		{
			return false;
		}

		return true;
	}

	public boolean isExamSign(Block clickedBlock, String[] lines)
	{
		if (clickedBlock.getType() != Material.WALL_SIGN)
		{
			this.plugin.logDebug("Not a exam sign");
			return false;
		}

		BlockState state = clickedBlock.getState();

		Sign sign = (Sign) state;

		if (!lines[0].equalsIgnoreCase("Exam"))
		{
			this.plugin.logDebug("Not written exam on first line: " + lines[0]);
			return false;
		}

		return true;
	}

	public void calculateExamResult(String playerName)
	{
		int correctAnswers = this.plugin.getStudentManager().getCorrectAnswersForStudent(playerName);
		String examName = this.plugin.getStudentManager().getExamForStudent(playerName);

		int score = 100 * correctAnswers / getExamNumberOfQuestions(examName);

		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "------------- Exam done -------------");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.AQUA + " Exam score:  " + ChatColor.YELLOW + score + ChatColor.AQUA + " points");
		plugin.sendMessage(playerName, ChatColor.AQUA + " Points needed: " + ChatColor.YELLOW + plugin.requiredExamScore + ChatColor.AQUA + " points");

		Player player = plugin.getServer().getPlayer(playerName);

		plugin.getStudentManager().setLastExamTime(playerName);
		
		if (score >= plugin.requiredExamScore)
		{
			String newGroup = getExamRank(examName);
			
			if(newGroup!=null)
			{
				plugin.getPermissionsManager().setGroup(playerName, newGroup);
			}
			else
			{
				String oldGroup = plugin.getStudentManager().getOriginalRank(playerName);
				plugin.getPermissionsManager().setGroup(playerName, oldGroup);				
			}

			String command = getExamCommand(examName);
			
			if(command!=null)
			{
				plugin.logDebug("Reading single command");
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("$PlayerName", playerName));
			}
			else
			{			
				plugin.logDebug("Reading multiple commands");

				List<String> commands = getExamCommands(examName);
			
				if(commands!=null)
				{
					for(String c : commands)
					{
						plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("$PlayerName", playerName)); 
					}
				}
			}			
			
			plugin.getStudentManager().setPassedExam(playerName, examName);

			plugin.sendMessage(playerName, ChatColor.GREEN + "Congratulations, you passed the exam!");
			plugin.sendToAll(ChatColor.GREEN + playerName + " just PASSED the " + ChatColor.YELLOW + plugin.getStudentManager().getExamForStudent(playerName) + ChatColor.GREEN + " exam!");
		}
		else
		{
			String oldGroup = plugin.getStudentManager().getOriginalRank(playerName);

			plugin.sendMessage(playerName, ChatColor.RED + "Sorry, you did not pass the exam...");
			plugin.sendToAll(ChatColor.RED + playerName + " just FAILED the " + ChatColor.YELLOW + plugin.getStudentManager().getExamForStudent(playerName) + ChatColor.RED + " exam...");
			plugin.log(playerName + " failed the " + examName + " exam with " + score + " points");

			plugin.getPermissionsManager().setGroup(playerName, oldGroup);
		}
	}

	public double getExamPrice(String examName)
	{
		return examsConfig.getDouble(examName + ".Price");
	}

	public int getExamNumberOfQuestions(String examName)
	{
		int number = examsConfig.getInt(examName + ".NumberOfQuestions");

		if (number == 0)
		{
			plugin.log("Found no NumberOfQuestions for exam '" + examName + "'. Setting NumberOfQuestions to 1.");
			number = 1;
		}

		return number;
	}

	public String getExamStartTime(String examName)
	{
		int time = examsConfig.getInt(examName + ".StartTime") % 24000;

		int hours = 6 + time / 1000;

		return hours + ":00";
	}

	public int cleanStudentData()
	{
		int n = 0;

		for (String studentName : plugin.getStudentManager().getStudents())
		{
			if (plugin.getStudentManager().hasOutdatedExamAttempt(studentName))
			{
				plugin.getStudentManager().deleteStudent(studentName);

				n++;
			}
		}
		return n;
	}

	public String getExamRank(String examName)
	{
		return examsConfig.getString(examName + ".RankName");
	}

	public String getExamCommand(String examName)
	{
		return examsConfig.getString(examName + ".Command");
	}

	public List<String> getExamCommands(String examName)
	{
		return examsConfig.getStringList(examName + ".Commands");
	}

	public boolean nextExamQuestion(String playerName)
	{
		String examName = plugin.getStudentManager().getExamForStudent(playerName);

		if (plugin.getStudentManager().nextExamQuestion(playerName) >= getExamNumberOfQuestions(examName))
		{
			plugin.logDebug("getExamNumberOfQuestions: No more questions");
			return false;
		}

		int examQuestionIndex = plugin.getStudentManager().getExamQuestionIndexForStudent(playerName);

		String question = getExamQuestionText(examName, examQuestionIndex);

		if (question == null)
		{
			plugin.logDebug("nextExamQuestion: No question found for exam " + examName);
			return false;
		}

		String correctOption = getExamQuestionCorrectOptionText(examName, examQuestionIndex);
		List<String> options = getExamQuestionOptionText(examName, examQuestionIndex);

		if (options==null || options.size() == 0)
		{
			plugin.logDebug("nextExamQuestion: No options found for question '" + question + "'");
			return false;
		}

		plugin.getStudentManager().setExamQuestionForStudent(playerName, question, options, correctOption);

		return true;
	}

	public String getExamQuestionText(String examName, int examQuestionIndex)
	{
		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
		Set<String> questions = configSection.getKeys(false);

		if(examQuestionIndex >= questions.size())
		{
			plugin.log("ERROR: Could not find question text with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}
		
		return (String) questions.toArray()[examQuestionIndex];
	}

	public String getExamQuestionCorrectOptionText(String examName, int examQuestionIndex)
	{
		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
		Set<String> questions = configSection.getKeys(false);

		if(examQuestionIndex >= questions.size())
		{
			plugin.log("ERROR: Could not find question correct option with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}

		String question = (String) questions.toArray()[examQuestionIndex];

		return examsConfig.getString(examName + ".Questions." + question + ".CorrectOption");
	}

	public List<String> getExamQuestionOptionText(String examName, int examQuestionIndex)
	{
		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
		Set questions = configSection.getKeys(false);

		if(examQuestionIndex >= questions.size())
		{
			plugin.log("ERROR: Could not find question option with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}
		
		String question = (String) questions.toArray()[examQuestionIndex];

		return examsConfig.getStringList(examName + ".Questions." + question + ".Options");
	}

	public boolean generateExam(String playerName, String examName)
	{
		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
		Set<String> questions = configSection.getKeys(false);

		if (questions.size() == 0)
		{
			plugin.log("No questions for exam called '" + examName + "'");
			return false;
		}

		if (questions.size() < getExamNumberOfQuestions(examName))
		{
			plugin.log("Not enough questions for exam '" + examName + "'");
			return false;
		}

		this.plugin.logDebug("Got " + questions.size() + " questions");

		List<String> selectedQuestions = new ArrayList<String>();

		for (int q = 0; q < getExamNumberOfQuestions(examName); q++)
		{
			selectedQuestions.add(String.valueOf(this.random.nextInt(questions.size())));
		}

		while (!isDifferentStrings(selectedQuestions))
		{
			selectedQuestions.set(random.nextInt(selectedQuestions.size()), String.valueOf(random.nextInt(questions.size())));
		}

		plugin.getStudentManager().setExamForStudent(playerName, examName, selectedQuestions);

		return true;
	}

	private boolean isDifferentStrings(List<String> strings)
	{
		for (int s1 = 0; s1 < strings.size(); s1++)
		{
			for (int s2 = 0; s2 < strings.size(); s2++)
			{
				if (s1 != s2)
				{
					String string1 = (String) strings.get(s1);
					String string2 = (String) strings.get(s2);

					if (string1.equals(string2))
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	public void doExamQuestion(String playerName)
	{
		String question = plugin.getStudentManager().getExamQuestionForStudent(playerName);
		String examName = plugin.getStudentManager().getExamForStudent(playerName);
		List<String> options = plugin.getStudentManager().getExamQuestionOptionsForStudent(playerName);

		plugin.sendMessage(playerName, "------------- Exam question " + ChatColor.YELLOW + (plugin.getStudentManager().getExamProgressIndexForStudent(playerName) + 1) + "/" + getExamNumberOfQuestions(examName) + ChatColor.AQUA + " -------------");
		plugin.sendMessage(playerName, question);

		int n = 0;

		for (String option : options)
		{
			switch(n)
			{
				case 0 : plugin.sendMessage(playerName, ChatColor.YELLOW + "A - " + ChatColor.AQUA + option); break;
				case 1 : plugin.sendMessage(playerName, ChatColor.YELLOW + "B - " + ChatColor.AQUA + option); break;
				case 2 : plugin.sendMessage(playerName, ChatColor.YELLOW + "C - " + ChatColor.AQUA + option); break;
				case 3 : plugin.sendMessage(playerName, ChatColor.YELLOW + "D - " + ChatColor.AQUA + option); break;

			}
						
			n++;
		}

		plugin.sendMessage(playerName, ChatColor.AQUA + "Type " + ChatColor.WHITE + "/exams a, /exams b, /exams c or /exams d" + ChatColor.AQUA + " to answer.");
	}

	public boolean handleNewExamSign(SignChangeEvent event)
	{
		String[] lines = event.getLines();

		if (!examExists(lines[2]))
		{
			event.getPlayer().sendMessage(ChatColor.RED + "There is no exam called '" + lines[2] + "'");
			this.plugin.logDebug(event.getPlayer().getName() + " placed an exam sign for an invalid exam");
			return false;
		}

		event.setLine(0, "Exam");
		event.setLine(1, "In");

		event.getPlayer().sendMessage(ChatColor.AQUA + "You placed a sign for the " + ChatColor.GOLD + lines[2] + ChatColor.AQUA + " exam!");

		return true;
	}

	public List<String> getExams()
	{
		List exams = new ArrayList();

		for (String examName : this.examsConfig.getKeys(false))
		{
			exams.add(examName);
		}

		return exams;
	}

	public boolean examExists(String examName)
	{
		for (String name : this.examsConfig.getKeys(false))
		{
			if (examName.equalsIgnoreCase(name))
			{
				return true;
			}
		}

		return false;
	}

	public boolean signupForExam(String playerName, String examName)
	{
		double price = getExamPrice(examName);

		if (this.plugin.examPricesEnabled)
		{
			if ((price > 0.0D) && (!economy.has(playerName, price)))
			{
				plugin.sendMessage(playerName, ChatColor.RED + "You need " + economy.format(getExamPrice(examName)) + " to take this exam");
				return false;
			}
		}

		if (plugin.getStudentManager().hasRecentExamAttempt(playerName))
		{
			plugin.sendMessage(playerName, ChatColor.RED + "You cannot take another exam so soon!");
			plugin.sendMessage(playerName, ChatColor.RED + "Try again in " + ChatColor.YELLOW + plugin.getStudentManager().getTimeUntilCanDoExam(plugin.getServer().getPlayer(playerName).getWorld(), playerName) + ChatColor.RED + " minutes");
			return false;
		}

		String oldRank = plugin.getPermissionsManager().getGroup(playerName);

		plugin.getStudentManager().setOriginalRank(playerName, oldRank);

		plugin.getPermissionsManager().setGroup(playerName, "student");

		plugin.getStudentManager().signupForExam(playerName, examName);

		if (plugin.examPricesEnabled && (price > 0.0D))
		{
			economy.withdrawPlayer(playerName, price);
			plugin.sendMessage(playerName, ChatColor.AQUA + "You paid " + ChatColor.YELLOW + economy.format(getExamPrice(examName)) + ChatColor.AQUA + " for signing up to this exam");
		}

		return true;
	}
}