package npc.model.residences.clanhall;

import java.util.List;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Spawner;
import jts.gameserver.model.World;
import jts.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import jts.gameserver.model.entity.events.objects.SpawnExObject;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.NpcUtils;

@SuppressWarnings("serial")
public class RainbowGourdInstance extends NpcInstance
{
	private CMGSiegeClanObject _winner;

	public RainbowGourdInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	public void doDecrease(Creature character)
	{
		if(isDead())
			return;

		reduceCurrentHp(getMaxHp() * 0.2, character, null, false, false, false, false, false, false, false);
	}

	public void doHeal()
	{
		if(isDead())
			return;

		setCurrentHp(getCurrentHp() + getMaxHp() * 0.2, false);
	}

	public void doSwitch(RainbowGourdInstance npc)
	{
		if(isDead() || npc.isDead())
			return;

		final double currentHp = getCurrentHp();
		setCurrentHp(npc.getCurrentHp(), false);
		npc.setCurrentHp(currentHp, false);
	}

	@Override
	public void onDeath(Creature killer)
	{
		super.onDeath(killer);

		ClanHallMiniGameEvent miniGameEvent = getEvent(ClanHallMiniGameEvent.class);
		if(miniGameEvent == null)
			return;

		Player player = killer.getPlayer();

		CMGSiegeClanObject siegeClanObject = miniGameEvent.getSiegeClan(SiegeEvent.ATTACKERS, player.getClan());
		if(siegeClanObject == null)
			return;

		_winner = siegeClanObject;

		List<CMGSiegeClanObject> attackers = miniGameEvent.getObjects(SiegeEvent.ATTACKERS);
		for(int i = 0; i < attackers.size(); i++)
		{
			if(attackers.get(i) == siegeClanObject)
				continue;

			String arenaName = "arena_" + i;
			SpawnExObject spawnEx = miniGameEvent.getFirstObject(arenaName);

			RainbowYetiInstance yetiInstance = (RainbowYetiInstance) spawnEx.getSpawns().get(0).getFirstSpawned();
			yetiInstance.teleportFromArena();

			miniGameEvent.spawnAction(arenaName, false);
		}
	}

	@Override
	public void onDecay()
	{
		super.onDecay();

		final ClanHallMiniGameEvent miniGameEvent = getEvent(ClanHallMiniGameEvent.class);
		if(miniGameEvent == null)
			return;

		if(_winner == null)
			return;

		List<CMGSiegeClanObject> attackers = miniGameEvent.getObjects(SiegeEvent.ATTACKERS);

		int index = attackers.indexOf(_winner);

		String arenaName = "arena_" + index;
		miniGameEvent.spawnAction(arenaName, false);

		SpawnExObject spawnEx = miniGameEvent.getFirstObject(arenaName);

		Spawner spawner = spawnEx.getSpawns().get(0);

		Location loc = (Location) spawner.getCurrentSpawnRange();

		miniGameEvent.removeBanishItems();

		final NpcInstance npc = NpcUtils.spawnSingle(35600, loc.x, loc.y, loc.z, 0);
		ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
			@Override
			public void runImpl() throws Exception
			{
				List<Player> around = World.getAroundPlayers(npc, 750, 100);

				npc.deleteMe();

				for(Player player : around)
					player.teleToLocation(miniGameEvent.getResidence().getOwnerRestartPoint());

				miniGameEvent.processStep(_winner.getClan());
			}
		}, 10000L);
	}

	@Override
	public boolean isAttackable(Creature c)
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(Creature c)
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}
}