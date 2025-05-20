package jts.gameserver.taskmanager;

import java.util.concurrent.Future;

import jts.commons.threading.RunnableImpl;
import jts.commons.threading.SteppingRunnableQueueManager;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.dao.AccountBonusDAO;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.BonusRequest;
import jts.gameserver.model.Player;
import jts.gameserver.model.actor.instances.player.Bonus;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.ExBR_PremiumState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.CustomMessage;

public class LazyPrecisionTaskManager extends SteppingRunnableQueueManager
{
	private static final LazyPrecisionTaskManager _instance = new LazyPrecisionTaskManager();

	public static final LazyPrecisionTaskManager getInstance()
	{
		return _instance;
	}

	private LazyPrecisionTaskManager()
	{
		super(1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		//Очистка каждые 60 секунд
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl(){
			@Override
			public void runImpl() throws Exception
			{
				LazyPrecisionTaskManager.this.purge();
			}

		}, 60000L, 60000L);
	}

	public Future<?> addPCCafePointsTask(final Player player)
	{
		long delay = Config.ALT_PCBANG_POINTS_DELAY * 60000L;

		return scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				if(player.isInOfflineMode() || player.getLevel() < Config.ALT_PCBANG_POINTS_MIN_LVL)
					return;

				if(player.getPcBangPoints() < Config.ALT_MAX_PC_BANG_POINTS)
					player.addPcBangPoints(Config.ALT_PCBANG_POINTS_BONUS, Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE > 0 && Rnd.chance(Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE));
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessage.THE_MAXIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED_YOU_CAN_NO_LONGER_ACQUIRE);
					player.sendPacket(sm);
				}
			}

		}, delay, delay);
	}

	public Future<?> addVitalityRegenTask(final Player player)
	{
		long delay = 60000L;

		return scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				if(player.isInOfflineMode() || !player.isInPeaceZone())
					return;

				player.setVitality(player.getVitality() + 1); // одно очко раз в минуту
			}

		}, delay, delay);
	}

	public Future<?> startBonusExpirationTask(final Player player)
	{
		long delay = player.getBonus().getBonusExpire() * 1000L - System.currentTimeMillis();
		player.broadcastUserInfo(true);
		return schedule(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				player.getBonus().setRateXp(1.);
				player.getBonus().setRateSp(1.);
				player.getBonus().setDropAdena(1.);
				player.getBonus().setDropItems(1.);
				player.getBonus().setDropSpoil(1.);

				if(player.getParty() != null)
					player.getParty().recalculatePartyData();

				String msg = new CustomMessage("scripts.services.RateBonus.LuckEnded", player).toString();
				player.sendPacket(new ExShowScreenMessage(msg, 10000, ScreenMessageAlign.TOP_CENTER, true), new ExBR_PremiumState(player, false));
				player.sendMessage(msg);

				if(Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_GAMESERVER)
					AccountBonusDAO.getInstance().delete(player.getAccountName());
				else 
					LoginServerCommunication.getInstance().sendPacket(new BonusRequest(player.getAccountName(), 0, 0));
				
				player.broadcastUserInfo(true);
				
			//	Functions i = new Functions();
			//	i.show(HtmCache.getInstance().getNotNull("scripts/services/RateBonusGet.htm", player), player);
			}

		}, delay);
	}

	public Future<?> addNpcAnimationTask(final NpcInstance npc)
	{
		return scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				if(npc.isVisible() && !npc.isActionsDisabled() && !npc.isMoving && !npc.isInCombat())
					npc.onRandomAnimation();
			}

		}, 1000L, Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION) * 1000L);
	}

}