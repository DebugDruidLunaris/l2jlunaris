package npc.model.residences.fortress.siege;

import java.util.List;

import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.entity.events.impl.FortressSiegeEvent;
import jts.gameserver.model.entity.events.objects.DoorObject;
import jts.gameserver.model.entity.events.objects.SiegeClanObject;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.npc.NpcTemplate;
import ai.residences.fortress.siege.MercenaryCaption;

@SuppressWarnings("serial")
public class MercenaryCaptionInstance extends MonsterInstance
{
	private class DoorDeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature door, Creature killer)
		{
			if(isDead())
				return;

			FortressSiegeEvent event = door.getEvent(FortressSiegeEvent.class);

			if(event == null)
				return;

			Functions.npcShout(MercenaryCaptionInstance.this, NpcString.WE_HAVE_BROKEN_THROUGH_THE_GATE_DESTROY_THE_ENCAMPMENT_AND_MOVE_TO_THE_COMMAND_POST);

			List<DoorObject> objects = event.getObjects(FortressSiegeEvent.ENTER_DOORS);

			for(DoorObject d : objects)
			{
				d.open(event);
			}

			((MercenaryCaption) getAI()).startMove(true);
		}
	}

	private DoorDeathListener _doorDeathListener = new DoorDeathListener();

	public MercenaryCaptionInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		setHasChatWindow(false);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		Fortress f = getFortress();
		FortressSiegeEvent event = f.getSiegeEvent();
		List<DoorObject> objects = event.getObjects(FortressSiegeEvent.ENTER_DOORS);

		for(DoorObject d : objects)
		{
			d.getDoor().addListener(_doorDeathListener);
		}
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		FortressSiegeEvent event = getEvent(FortressSiegeEvent.class);

		if(event == null)
			return false;

		SiegeClanObject object = event.getSiegeClan(FortressSiegeEvent.DEFENDERS, attacker.getClan());

		if(object == null)
			return false;

		return true;
	}

	@Override
	public void onDeath(Creature killer)
	{
		super.onDeath(killer);
		Functions.npcShout(this, NpcString.THE_GODS_HAVE_FORSAKEN_US__RETREAT);
	}

	@Override
	public void onDecay()
	{
		super.onDecay();

		Fortress f = getFortress();
		FortressSiegeEvent event = f.getSiegeEvent();
		List<DoorObject> objects = event.getObjects(FortressSiegeEvent.ENTER_DOORS);

		for(DoorObject d : objects)
		{
			d.getDoor().removeListener(_doorDeathListener);
		}
	}
}