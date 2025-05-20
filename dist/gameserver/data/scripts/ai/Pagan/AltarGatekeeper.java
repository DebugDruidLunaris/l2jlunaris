package ai.Pagan;

import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.instances.DoorInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.ReflectionUtils;

public class AltarGatekeeper extends DefaultAI
{
	private boolean _firstTime = true;

	public AltarGatekeeper(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		// Двери на балкон
		DoorInstance door1 = ReflectionUtils.getDoor(19160014);
		DoorInstance door2 = ReflectionUtils.getDoor(19160015);
		// Двери к алтарю
		DoorInstance door3 = ReflectionUtils.getDoor(19160016);
		DoorInstance door4 = ReflectionUtils.getDoor(19160017);

		// Кричим 4 раза (т.к. актор заспавнен в 4х местах) как на оффе о том что двери открылись
		if(!door1.isOpen() && !door2.isOpen() && door3.isOpen() && door4.isOpen() && _firstTime)
		{
			_firstTime = false;
			Functions.npcSay(actor, NpcString.PAGAN);
		}

		return true;
	}
}