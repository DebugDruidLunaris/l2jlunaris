package ai.residences.clanhall;

import java.util.List;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CharacterAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import jts.gameserver.model.entity.events.objects.SpawnExObject;
import jts.gameserver.model.entity.events.objects.ZoneObject;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.NpcUtils;
import npc.model.residences.clanhall.RainbowGourdInstance;
import npc.model.residences.clanhall.RainbowYetiInstance;

public class RainbowYeti extends CharacterAI
{
	private static class ZoneDeactive extends RunnableImpl
	{
		private final ZoneObject _zone;

		public ZoneDeactive(ZoneObject zone)
		{
			_zone = zone;
		}

		@Override
		public void runImpl() throws Exception
		{
			_zone.setActive(false);
		}
	}

	public RainbowYeti(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtSeeSpell(Skill skill, Creature character)
	{
		RainbowYetiInstance actor = (RainbowYetiInstance) getActor();
		ClanHallMiniGameEvent miniGameEvent = actor.getEvent(ClanHallMiniGameEvent.class);
		if(miniGameEvent == null)
			return;
		if(!character.isPlayer())
			return;

		Player player = character.getPlayer();

		CMGSiegeClanObject siegeClan = null;
		List<CMGSiegeClanObject> attackers = miniGameEvent.getObjects(SiegeEvent.ATTACKERS);
		for(CMGSiegeClanObject $ : attackers)
			if($.isParticle(player))
				siegeClan = $;

		if(siegeClan == null)
			return;

		int index = attackers.indexOf(siegeClan);
		int warIndex = Integer.MIN_VALUE;

		RainbowGourdInstance gourdInstance = null;
		RainbowGourdInstance gourdInstance2 = null;
		switch(skill.getId())
		{
			case 2240: //nectar
				// убить хп у своего Фрукта :D
				if(Rnd.chance(90))
				{
					gourdInstance = getGourd(index);
					if(gourdInstance == null)
						return;

					gourdInstance.doDecrease(player);
				}
				else
					actor.addMob(NpcUtils.spawnSingle(35592, actor.getX() + 10, actor.getY() + 10, actor.getZ(), 0));
				break;
			case 2241: //mineral water
				// увеличить ХП у чужого фрукта
				warIndex = rndEx(attackers.size(), index);
				if(warIndex == Integer.MIN_VALUE)
					return;

				gourdInstance2 = getGourd(warIndex);
				if(gourdInstance2 == null)
					return;
				gourdInstance2.doHeal();
				break;
			case 2242: //water
				// обменять ХП с чужим фруктом
				warIndex = rndEx(attackers.size(), index);
				if(warIndex == Integer.MIN_VALUE)
					return;

				gourdInstance = getGourd(index);
				gourdInstance2 = getGourd(warIndex);
				if(gourdInstance2 == null || gourdInstance == null)
					return;

				gourdInstance.doSwitch(gourdInstance2);
				break;
			case 2243: //sulfur
				// наложить дебафф в чужогой арене
				warIndex = rndEx(attackers.size(), index);
				if(warIndex == Integer.MIN_VALUE)
					return;

				ZoneObject zone = miniGameEvent.getFirstObject("zone_" + warIndex);
				if(zone == null)
					return;
				zone.setActive(true);
				ThreadPoolManager.getInstance().schedule(new ZoneDeactive(zone), 60000L);
				break;
		}
	}

	private RainbowGourdInstance getGourd(int index)
	{
		ClanHallMiniGameEvent miniGameEvent = getActor().getEvent(ClanHallMiniGameEvent.class);

		SpawnExObject spawnEx = miniGameEvent.getFirstObject("arena_" + index);

		return (RainbowGourdInstance) spawnEx.getSpawns().get(1).getFirstSpawned();
	}

	private int rndEx(int size, int ex)
	{
		int rnd = Integer.MIN_VALUE;
		for(int i = 0; i < Byte.MAX_VALUE; i++)
		{
			rnd = Rnd.get(size);
			if(rnd != ex)
				break;
		}

		return rnd;
	}
}