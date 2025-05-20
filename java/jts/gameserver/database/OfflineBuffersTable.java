package jts.gameserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jts.gameserver.instancemanager.OfflineBufferManager;
import jts.gameserver.instancemanager.OfflineBufferManager.BufferData;

public class OfflineBuffersTable
{
	private static final Logger _log = LoggerFactory.getLogger(OfflineBuffersTable.class);

	public void restoreOfflineBuffers()
	{
		_log.info(getClass().getSimpleName() + ": Loading offline buffers...");

		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM character_offline_buffers WHERE charId > 0");
			ResultSet rs = st.executeQuery())
		{
			int nBuffers = 0;

			while (rs.next())
			{
				Player player = null;

				try
				{
					player = Player.restore(rs.getInt("charId"));
					if (player == null)
						continue;

					player.setOfflineMode(true);
					player.setIsOnline(true);
					player.updateOnlineStatus();

					player.spawnMe();

					if (player.getClan() != null && player.getClan().getAnyMember(player.getObjectId()) != null)
						player.getClan().getAnyMember(player.getObjectId()).setPlayerInstance(player, false);

					final BufferData buffer = new BufferData(player, rs.getString("title"), rs.getInt("price"), null);

					try (PreparedStatement stm_items = con.prepareStatement("SELECT * FROM character_offline_buffer_buffs WHERE charId = ?"))
					{
						stm_items.setInt(1, player.getObjectId());
						try (ResultSet skills = stm_items.executeQuery())
						{
							if (skills.next())
							{
								final String[] skillIds = skills.getString("skillIds").split(",");
								for (String skillId : skillIds)
								{
									final Skill skill = player.getKnownSkill(Integer.parseInt(skillId));
									if (skill == null)
										continue;

									buffer.getBuffs().put(skill.getId(), skill);
								}
							}
						}
					}

					OfflineBufferManager.getInstance().getBuffStores().put(player.getObjectId(), buffer);

					player.sitDown(null);
					player.setTitle(buffer.getSaleTitle());
					player.setPrivateStoreType(Player.STORE_PRIVATE_BUFF);

					player.broadcastUserInfo(true);
					nBuffers++;

				}
				catch (Exception e)
				{
					_log.warn(getClass().getSimpleName() + ": Error loading buffer: " + player, e);
					if (player != null)
					{
						player.deleteMe();
					}
				}
			}

			_log.info(getClass().getSimpleName() + ": Loaded: " + nBuffers + " offline buffer(s)");
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while loading offline buffer: ", e);
		}
	}

	/**
	 * Invocado cuando un pj loguea a su pj estando en un buff store
	 *
	 * @param trader
	 */
	public synchronized void onLogin(Player trader)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			OfflineBufferManager.getInstance().getBuffStores().remove(trader.getObjectId());

			try (PreparedStatement st = con.prepareStatement("DELETE FROM character_offline_buffers WHERE charId=?"))
			{
				st.setInt(1, trader.getObjectId());
				st.executeUpdate();
			}

			try (PreparedStatement st = con.prepareStatement("DELETE FROM character_offline_buffer_buffs WHERE charId=?"))
			{
				st.setInt(1, trader.getObjectId());
				st.executeUpdate();
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while removing offline buffer: " + e, e);
		}
	}

	public synchronized void onLogout(Player trader)
	{
		final BufferData buffer = OfflineBufferManager.getInstance().getBuffStores().get(trader.getObjectId());
		if (buffer == null)
			return;

		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement st = con.prepareStatement("REPLACE INTO character_offline_buffers VALUES (?,?,?)"))
			{
				st.setInt(1, trader.getObjectId());
				st.setInt(2, buffer.getBuffPrice());
				st.setString(3, buffer.getSaleTitle());
				st.executeUpdate();
			}

			try (PreparedStatement st = con.prepareStatement("REPLACE INTO character_offline_buffer_buffs VALUES (?,?)"))
			{
				st.setInt(1, trader.getObjectId());
				st.setString(2, joinAllSkillsToString(buffer.getBuffs().values()));
				st.executeUpdate();
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while saving offline buffer: " + e, e);
		}
	}

	private final String joinAllSkillsToString(Collection<Skill> skills)
	{
		if (skills.isEmpty())
			return "";

		String result = "";
		for (Skill val : skills)
		{
			result += val.getId() + ",";
		}

		return result.substring(0, result.length() - 1);
	}

	public static OfflineBuffersTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final OfflineBuffersTable _instance = new OfflineBuffersTable();
	}
}