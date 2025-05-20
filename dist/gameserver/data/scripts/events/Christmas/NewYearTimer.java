package events.Christmas;

import java.util.Calendar;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.Announcements;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.instancemanager.ServerVariables;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.tables.SkillTable;

public class NewYearTimer implements ScriptFile
{
	private static NewYearTimer instance;

	public static NewYearTimer getInstance()
	{
		if(instance == null)
			new NewYearTimer();
		return instance;
	}

	public NewYearTimer()
	{
		if(instance != null)
			return;

		instance = this;

		if(!isActive())
			return;

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		c.set(Calendar.MONTH, Calendar.JANUARY);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		while(getDelay(c) < 0)
			c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 1);

		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("Bem Vindo " + c.get(Calendar.YEAR) + ", Feliz Ano Novo!!!!"), getDelay(c));
		c.add(Calendar.SECOND, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("1"), getDelay(c));
		c.add(Calendar.SECOND, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("2"), getDelay(c));
		c.add(Calendar.SECOND, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("3"), getDelay(c));
		c.add(Calendar.SECOND, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("4"), getDelay(c));
		c.add(Calendar.SECOND, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("5"), getDelay(c));
	}

	private long getDelay(Calendar c)
	{
		return c.getTime().getTime() - System.currentTimeMillis();
	}

	/**
	 * Called class loading browser
	 */
	@Override
	public void onLoad() {}

	/**
	 * Called when you restart
	 * After rebooting the onLoad () is called automatically
	 */
	@Override
	public void onReload() {}

	/**
	 * Reads the status of the opening event of the base.
	 * @return
	 */
	private static boolean isActive()
	{
		return ServerVariables.getString("Christmas", "off").equalsIgnoreCase("on");
	}

	/**
	 * Called when shutdown of the server
	 */
	@Override
	public void onShutdown() {}

	private class NewYearAnnouncer extends RunnableImpl
	{
		private final String message;

		private NewYearAnnouncer(String message)
		{
			this.message = message;
		}

		@Override
		public void runImpl() throws Exception
		{
			Announcements.getInstance().announceToAll(message);

			// Through the ass done, but does not matter :)
			if(message.length() == 1)
				return;

			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				Skill skill = SkillTable.getInstance().getInfo(3266, 1);
				MagicSkillUse msu = new MagicSkillUse(player, player, 3266, 1, skill.getHitTime(), 0);
				player.broadcastPacket(msu);
			}

			instance = null;
			new NewYearTimer();
		}
	}
}