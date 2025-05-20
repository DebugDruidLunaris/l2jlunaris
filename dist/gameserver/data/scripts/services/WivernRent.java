package services;

import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WivernRent extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(WivernRent.class);

	@Override
	public void onLoad()
	{
		_log.info("Loaded service: PC Cafe Wivern Rent");
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}

	public void ride()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		if(player.isTerritoryFlagEquipped())
		{
			player.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
			return;
		}

		if(player.getTransformation() != 0)
		{
			show(player.isLangRus() ? "Вы не можете взять пета в прокат, пока находитесь в режиме трансформации." : "You can`t take the pet in  while you are in transformation mode", player);
			return;
		}

		if(player.getPet() != null || player.isMounted())
		{
			player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
			return;
		}

		if(player.getPcBangPoints() < Config.ALT_PC_BANG_WIVERN_PRICE)
		{
			player.sendPacket(Msg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
			return;
		}

		player.reducePcBangPoints(Config.ALT_PC_BANG_WIVERN_PRICE);

		ride(player, 12621);
		new RentTask(player);
	}

	private static synchronized void sayToPlayer(Player player, String timeString, int timeToEnd, int time)
	{
		ExShowScreenMessage packet;
		if(player.isLangRus())
			packet = new ExShowScreenMessage("До конца аренды " + timeToEnd + timeString, time, ScreenMessageAlign.BOTTOM_CENTER, false);
		else
			packet = new ExShowScreenMessage("Rental ends in " + timeToEnd + timeString, time, ScreenMessageAlign.BOTTOM_CENTER, false);
		player.sendPacket(packet);
	}

	private class RentTask extends RunnableImpl
	{
		private Player player;
		private ScheduledFuture<?> rentTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(this, 0L, 1000L);
		private int seconds = Config.ALT_PC_BANG_WIVERN_TIME * 60;

		public RentTask(Player player)
		{
			this.player = player;
		}

		@Override
		public void runImpl() throws Exception
		{
			switch(seconds)
			{
				case 300:
					sayToPlayer(player, player.isLangRus() ? " минут" : " minutes", 5, 3000);
					break;
				case 240:
					sayToPlayer(player, player.isLangRus() ? " минуты" : " minutes", 4, 3000);
					break;
				case 180:
					sayToPlayer(player, player.isLangRus() ? " минуты" : " minutes", 3, 3000);
					break;
				case 120:
					sayToPlayer(player, player.isLangRus() ? " минуты" : " minutes", 2, 3000);
					break;
				case 60:
					sayToPlayer(player, player.isLangRus() ? " минута" : " minute", 1, 3000);
					break;
				case 30:
					sayToPlayer(player, player.isLangRus() ? " секунд" : " seconds", seconds, 3000);
					break;
				case 20:
					sayToPlayer(player, player.isLangRus() ? " секунд" : " seconds", seconds, 3000);
					break;
				case 10:
					sayToPlayer(player, player.isLangRus() ? " секунд" : " seconds", seconds, 3000);
					break;
				case 5:
					sayToPlayer(player, player.isLangRus() ? " секунд" : " seconds", seconds, 1000);
					player.sendPacket(new ExShowScreenMessage(player.isLangRus() ? "Хьюстон у нас проблемы!" : "Houston we have a problem!", 1000, ScreenMessageAlign.MIDDLE_CENTER, true));
					break;
				case 4:
					sayToPlayer(player, player.isLangRus() ? " секунды" : " seconds", seconds, 1000);
					player.sendPacket(new ExShowScreenMessage(player.isLangRus() ? "Хьюстон у нас проблемы!" : "Houston we have a problem!", 1000, ScreenMessageAlign.MIDDLE_CENTER, true));
					break;
				case 3:
					sayToPlayer(player, player.isLangRus() ? " секунды" : " seconds", seconds, 1000);
					player.sendPacket(new ExShowScreenMessage(player.isLangRus() ? "Хьюстон у нас проблемы!" : "Houston we have a problem!", 1000, ScreenMessageAlign.MIDDLE_CENTER, true));
					break;
				case 2:
					sayToPlayer(player, player.isLangRus() ? " секунды" : " seconds", seconds, 1000);
					player.sendPacket(new ExShowScreenMessage(player.isLangRus() ? "Хьюстон у нас проблемы!" : "Houston we have a problem!", 1000, ScreenMessageAlign.MIDDLE_CENTER, true));
					break;
				case 1:
					sayToPlayer(player, player.isLangRus() ? " секунда" : " second", seconds, 1000);
					player.sendPacket(new ExShowScreenMessage(player.isLangRus() ? "Хьюстон у нас проблемы!" : "Houston we have a problem!", 1000, ScreenMessageAlign.MIDDLE_CENTER, true));
					break;
				case 0:
					player.sendPacket(new ExShowScreenMessage(player.isLangRus() ? "Wyvern Airlines желает Вам приятного дня." : "Wyvern Airlines wishes you a pleasant day.", 3000, ScreenMessageAlign.MIDDLE_CENTER, true));
					unRide(player);
					rentTask.cancel(false);
					break;
			}
			seconds--;
		}
	}
}