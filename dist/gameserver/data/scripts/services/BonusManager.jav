package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BonusManager implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(BonusManager.class);

	private static int STATUS_OK = 0;
	//private static int STATUS_NO = 1;
	private static int STATUS_ERROR = 2;

	public static final String CLAN_NAME = "clan_name";
	public static final String PARTY_LEADER_NAME = "party_leader";

	private Map<Integer, Reward> toReward = new HashMap<Integer, Reward>();

	private int[][] clanReward;
	private int[][] partyReward;

	public static BonusManager _instance;

	public static BonusManager getInstance()
	{
		return _instance != null ? _instance : (_instance = new BonusManager());
	}

	@Override
	public void onLoad()
	{
		_instance = this;
	}

	private BonusManager()
	{
		parseReward();
	}

	private void parseReward()
	{
		this.clanReward = new int[clanReward.length][];
		this.partyReward = new int[partyReward.length][];

		for(int i = 0; i < clanReward.length; i++)
		{
			this.clanReward[i][0] = Config.BONUS_SERVICE_CLAN_REWARD[i];
			this.clanReward[i][1] = Config.BONUS_SERVICE_CLAN_REWARD[i + 1];
		}

		for(int i = 0; i < partyReward.length; i++)
		{
			this.partyReward[i][0] = Config.BONUS_SERVICE_PARY_REWARD[i];
			this.partyReward[i][1] = Config.BONUS_SERVICE_PARY_REWARD[i + 1];
		}
	}

	@Override
	public void onReload()
	{
		_instance = null;
	}

	@Override
	public void onShutdown()
	{
		onReload();
	}

	public boolean hasReward(String name, String type)
	{
		boolean result = true;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT hasReward FROM clan_bonus WHERE " + type + "=?");
			statement.setString(1, name);
			rset = statement.executeQuery();
			result = rset.getBoolean("hasReward");
		}
		catch(SQLException e)
		{
			_log.warn("Error while executing 'hasReward' for {}", name);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public void sortPlayers(List<Player> players)
	{
		for(Player player : players)
			if(checkPlayerReward(player))
				players.remove(player);
	}

	private boolean checkPlayerReward(Player player)
	{
		boolean result = false;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT FROM players_bonus WHERE obj_id = ? OR hwid = ?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, player.getNetConnection().getHWID());
			rset = statement.executeQuery();
			result = rset.next();
		}
		catch(SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	private int giveReward(Reward reward)
	{
		int status = STATUS_OK;
		Connection con = null;
		PreparedStatement statement = null;

		for(Player player : reward.getPlayers())
		{
			if(reward.getType().equals(CLAN_NAME))
				for(int i = 0; i < clanReward.length; i++)
					Functions.addItem(player, clanReward[i][0], clanReward[i][1]);

			else if(reward.getType().equals(PARTY_LEADER_NAME))
				for(int i = 0; i < partyReward.length; i++)
					Functions.addItem(player, partyReward[i][0], partyReward[i][1]);

			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO players_bonus VALUES (?,?)");
				statement.setInt(1, player.getObjectId());
				statement.setString(2, player.getNetConnection().getHWID());
				statement.execute();
			}
			catch(SQLException e)
			{
				_log.warn("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_bonus SET hasReward = 1 WHERE " + reward.getType() + " = ?");
			statement.setString(1, reward.getName());
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("", e);
			status = STATUS_ERROR;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return status;
	}

	public void makeRewardList(Player player, List<Player> players, String type)
	{
		if(toReward.containsKey(players.hashCode()))
			return;
		toReward.put(players.hashCode(), new Reward(player.getName(), players, type));
	}

	public int doReward(int hash)
	{
		if(toReward.get(hash) == null)
			return STATUS_ERROR;
		return giveReward(toReward.get(hash));
	}

	private class Reward
	{
		private String name;
		private List<Player> players;
		private String type;

		public Reward(String name, List<Player> players, String type)
		{
			this.name = name;
			this.players = players;
			this.type = type;
		}

		public String getName()
		{
			return name;
		}

		public String getType()
		{
			return type;
		}

		public List<Player> getPlayers()
		{
			return players;
		}
	}
}