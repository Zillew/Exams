package com.dogonfire.exams;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.api.util.Calculable;
import de.bananaco.bpermissions.api.util.CalculableType;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;


public class PermissionsManager
{
	private String				pluginName			= "null";
	private PluginManager		pluginManager		= null;
	private Exams				plugin;
	private PermissionsPlugin	permissionsBukkit	= null;

	private PermissionManager	pex					= null;

	private GroupManager		groupManager		= null;

	public PermissionsManager(Exams p)
	{
		this.plugin = p;
	}

	public void load()
	{
		pluginManager = plugin.getServer().getPluginManager();

		if (pluginManager.getPlugin("PermissionsBukkit") != null)
		{
			plugin.log("Using PermissionsBukkit.");
			pluginName = "PermissionsBukkit";
			permissionsBukkit = ((PermissionsPlugin) pluginManager.getPlugin("PermissionsBukkit"));
		}
		else if (pluginManager.getPlugin("PermissionsEx") != null)
		{
			plugin.log("Using PermissionsEx.");
			pluginName = "PermissionsEx";
			pex = PermissionsEx.getPermissionManager();
		}
		else if (pluginManager.getPlugin("GroupManager") != null)
		{
			plugin.log("Using GroupManager");
			pluginName = "GroupManager";
			groupManager = ((GroupManager) pluginManager.getPlugin("GroupManager"));
		}
		else if (pluginManager.getPlugin("bPermissions") != null)
		{
			plugin.log("Using bPermissions.");
			pluginName = "bPermissions";
		}
		else
		{
			plugin.log("No permissions plugin detected! Defaulting to superperm");
			pluginName = "SuperPerm";
		}
	}

	public Plugin getPlugin()
	{
		return plugin;
	}

	public String getPermissionPluginName()
	{
		return pluginName;
	}

	public boolean hasPermission(Player player, String node)
	{
		if (pluginName.equals("PermissionsBukkit"))
		{
			return player.hasPermission(node);
		}
		if (pluginName.equals("PermissionsEx"))
		{
			return pex.has(player, node);
		}
		if (pluginName.equals("GroupManager"))
		{
			AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(player.getName());

			if (handler == null)
			{
				return false;
			}

			return handler.permission(player, node);
		}
		if (pluginName.equals("bPermissions"))
		{
			return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), node);
		}

		return player.hasPermission(node);
	}

	public boolean isGroup(String groupName)
	{
		if (pluginName.equals("PermissionsBukkit"))
		{
			if (permissionsBukkit.getGroup(groupName) == null)
			{
				return false;
			}

			return true;
		}

		return false;
	}

	public String getGroup(String playerName)
	{
		if (pluginName.equals("PermissionsBukkit"))
		{
			if (permissionsBukkit.getGroups(playerName) == null)
			{
				return "";
			}

			if (this.permissionsBukkit.getGroups(playerName).size() == 0)
			{
				return "";
			}

			return ((Group) permissionsBukkit.getGroups(playerName).get(0)).getName();
		}
		if (pluginName.equals("PermissionsEx"))
		{
			if ((pex.getUser(playerName).getGroups() == null) || (pex.getUser(playerName).getGroups().length == 0))
			{
				return "";
			}

			return pex.getUser(playerName).getGroupsNames()[0];
		}
		if (pluginName.equals("GroupManager"))
		{
			AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);

			if (handler == null)
			{
				return "";
			}

			return handler.getGroup(playerName);
		}
		if (pluginName.equals("bPermissions"))
		{
			de.bananaco.bpermissions.api.World w = WorldManager.getInstance().getWorld(playerName);

			if (w == null)
			{
				return "";
			}
			
			if(w.getUser(playerName).getGroupsAsString().size()==0)
			{
				return "";
			}
			

			return (String)(w.getUser(playerName).getGroupsAsString().toArray()[0]);
		}

		return "";
	}

	public String getPrefix(String playerName)
	{
		if (this.pluginName.equals("PermissionsBukkit"))
		{
			return "";
		}
		if (this.pluginName.equals("PermissionsEx"))
		{
			return this.pex.getUser(this.pluginName).getOwnSuffix();
		}
		if (this.pluginName.equals("GroupManager"))
		{
			AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);

			if (handler == null)
			{
				return "";
			}

			return handler.getUserPrefix(playerName);
		}
		if (this.pluginName.equals("bPermissions"))
		{
			de.bananaco.bpermissions.api.World w = WorldManager.getInstance().getWorld(playerName);

			if (w == null)
			{
				return "";
			}

			//Calculable c = w.get(playerName, CalculableType.USER);

			return "";//c.getValue("prefix");
		}

		return "";
	}

	public void setGroup(String playerName, String groupName)
	{
		if (this.pluginName.equals("PermissionsBukkit"))
		{
			if (this.permissionsBukkit.getServer().getPlayer(playerName) != null)
			{
				if (this.permissionsBukkit.getServer().getPlayer(playerName).getGameMode() == GameMode.CREATIVE)
				{
					permissionsBukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "gm survival " + playerName);
				}
			}

			permissionsBukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "perm player setgroup " + playerName + " " + groupName);
		}
		else if (pluginName.equals("PermissionsEx"))
		{

			PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
			PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);

			if (group == null)
			{
				plugin.log("No group with the name '" + groupName + "'");
				return;
			}

			if (user == null)
			{
				plugin.log("No user with the name '" + playerName + "'");
				return;
			}

			PermissionGroup[] groups = new PermissionGroup[] { group };

			user.setGroups(groups);
		}
		else if (pluginName.equals("bPermissions"))
		{
			for (World world : plugin.getServer().getWorlds())
			{
				ApiLayer.setGroup(world.getName(), CalculableType.USER, playerName, groupName);
			}
		}
		else if (pluginName.equals("GroupManager"))
		{
			OverloadedWorldHolder owh;

			owh = this.groupManager.getWorldsHolder().getWorldDataByPlayerName(playerName);

			if (owh == null)
			{
				return;
			}

			User user = owh.getUser(playerName);

			if (user == null)
			{
				plugin.log("No player with the name '" + groupName + "'");
				return;
			}

			org.anjocaido.groupmanager.data.Group group = owh.getGroup(groupName);

			if (group == null)
			{
				plugin.log("No group with the name '" + groupName + "'");
				return;
			}

			user.setGroup(group);

			Player p = Bukkit.getPlayer(playerName);

			if (p != null)
			{
				GroupManager.BukkitPermissions.updatePermissions(p);
			}
		}
	}
}