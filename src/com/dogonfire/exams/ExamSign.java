package com.dogonfire.exams;

import java.util.Date;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class ExamSign
{
	private int			mode;
	private Location	location;
	private String		world;
	private String		region;
	private double		price;
	private String		account;
	private long		renttime;
	private String		rentby;
	private Date		expiredate;
	public static int	MODE_SELL_REGION			= 0;
	public static int	MODE_RENT_REGION			= 1;
	public static int	MODE_SELL_REGION_MEMBERSHIP	= 2;
	public static int	MODE_CLAIM_REGION			= 3;

	public ExamSign(int mode, Location location, String region, double price, String account, long renttime)
	{
		this.mode = mode;
		this.location = location;
		this.world = location.getWorld().getName();
		this.region = region;
		this.price = price;
		this.account = account;
		this.renttime = renttime;
		this.rentby = "";
		this.expiredate = null;
	}

	public int getMode()
	{
		return this.mode;
	}

	public String getModeName()
	{
		if (this.mode == MODE_SELL_REGION)
		{
			return "Region Sell";
		}
		if (this.mode == MODE_RENT_REGION)
		{
			return "Region Rent";
		}
		if (this.mode == MODE_SELL_REGION_MEMBERSHIP)
		{
			return "Region Join";
		}
		if (this.mode == MODE_CLAIM_REGION)
		{
			return "Region Claim";
		}

		return "UNKNOWN";
	}

	public Location getLocation()
	{
		return this.location;
	}

	public String getWorld()
	{
		return this.world;
	}

	public World getWorldWorld()
	{
		return getLocation().getWorld();
	}

	public String getRegion()
	{
		return this.region;
	}

	public double getPrice()
	{
		return this.price;
	}

	public String getAccount()
	{
		return this.account;
	}

	public long getRentTime()
	{
		return this.renttime;
	}

	public String getRent()
	{
		return this.rentby;
	}

	public boolean isRent()
	{
		return !getRent().isEmpty();
	}

	public void rentTo(String playername)
	{
		if (!playername.isEmpty())
		{
			this.expiredate = new Date(System.currentTimeMillis() + getRentTime());
		}
		else
		{
			this.expiredate = null;
		}
		this.rentby = playername;
	}

	public void rentTo(String playername, Date expiredate)
	{
		this.rentby = playername;
		this.expiredate = expiredate;
	}

	public Date getExpireDate()
	{
		return this.expiredate;
	}

	public void setExpireDate(Date expiredate)
	{
		this.expiredate = expiredate;
	}

	public boolean onWall()
	{
		return getLocation().getBlock().getType() == Material.WALL_SIGN;
	}

	public void destroyAgent(boolean drop)
	{
		getLocation().getBlock().setType(Material.AIR);

		if (drop)
		{
			getWorldWorld().dropItem(getLocation(), new ItemStack(Material.SIGN, 1));
		}
	}
}