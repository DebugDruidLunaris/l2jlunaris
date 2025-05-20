package ai.residences.castle;

import jts.commons.lang.reference.HardReference;
import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CharacterAI;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.events.objects.SiegeClanObject;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.skills.SkillTargetType;

public class ArtefactAI extends CharacterAI
{
	public ArtefactAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		NpcInstance actor;
		Player player;
		if(attacker == null || (player = attacker.getPlayer()) == null || (actor = (NpcInstance) getActor()) == null)
			return;

		SiegeEvent<?, ?> siegeEvent1 = actor.getEvent(SiegeEvent.class);
		SiegeEvent<?, ?> siegeEvent2 = player.getEvent(SiegeEvent.class);
		SiegeClanObject siegeClan = siegeEvent1.getSiegeClan(SiegeEvent.ATTACKERS, player.getClan());

		if(siegeEvent2 == null || siegeEvent1 == siegeEvent2 && siegeClan != null)
			ThreadPoolManager.getInstance().schedule(new notifyGuard(player), 1000);
	}

	class notifyGuard extends RunnableImpl
	{
		private HardReference<Player> _playerRef;

		public notifyGuard(Player attacker)
		{
			_playerRef = attacker.getRef();
		}

		@Override
		public void runImpl() throws Exception
		{
			NpcInstance actor;
			Player attacker = _playerRef.get();
			if(attacker == null || (actor = (NpcInstance) getActor()) == null)
				return;

			for(NpcInstance npc : actor.getAroundNpc(1500, 200))
				if(npc.isSiegeGuard() && Rnd.chance(20))
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);

			if(attacker.getCastingSkill() != null && attacker.getCastingSkill().getTargetType() == SkillTargetType.TARGET_HOLY)
				ThreadPoolManager.getInstance().schedule(this, 10000);
		}
	}
}