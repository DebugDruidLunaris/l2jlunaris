package npc.model.residences.fortress.siege;

import jts.gameserver.model.Player;
import jts.gameserver.model.Spawner;
import jts.gameserver.model.entity.events.impl.FortressSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.events.objects.SpawnExObject;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class MainMachineInstance extends NpcInstance
{
	private int _powerUnits = 3;

	public MainMachineInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		FortressSiegeEvent siegeEvent = getEvent(FortressSiegeEvent.class);

		if(siegeEvent == null)
			return;

		siegeEvent.barrackAction(3, false);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(_powerUnits != 0)
			return;

		Functions.npcShout(this, NpcString.FORTRESS_POWER_DISABLED);

		FortressSiegeEvent siegeEvent = getEvent(FortressSiegeEvent.class);

		if(siegeEvent == null)
			return;

		siegeEvent.spawnAction(FortressSiegeEvent.IN_POWER_UNITS, false);
		siegeEvent.barrackAction(3, true);
		siegeEvent.broadcastTo(SystemMsg.THE_BARRACKS_HAVE_BEEN_SEIZED, SiegeEvent.ATTACKERS, SiegeEvent.DEFENDERS);
		onDecay();
		siegeEvent.checkBarracks();
	}

	public void powerOff(PowerControlUnitInstance powerUnit)
	{
		FortressSiegeEvent event = getEvent(FortressSiegeEvent.class);
		SpawnExObject exObject = event.getFirstObject(FortressSiegeEvent.IN_POWER_UNITS);
		int machineNumber = -1;

		for(int i = 0; i < 3; i++)
		{
			Spawner spawn = exObject.getSpawns().get(i);

			if(spawn == powerUnit.getSpawn())
			{
				machineNumber = i;
			}
		}

		NpcString msg = null;

		switch(machineNumber)
		{
			case 0:
				msg = NpcString.MACHINE_NO_1_POWER_OFF;
				break;
			case 1:
				msg = NpcString.MACHINE_NO_2_POWER_OFF;
				break;
			case 2:
				msg = NpcString.MACHINE_NO_3_POWER_OFF;
				break;
			default:
				throw new IllegalArgumentException("Wrong spawn at fortress: " + event.getName());
		}

		_powerUnits--;

		Functions.npcShout(this, msg);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		NpcHtmlMessage message = new NpcHtmlMessage(player, this);

		if(_powerUnits != 0)
		{
			message.setFile("residence2/fortress/fortress_mainpower002.htm");
		}
		else
		{
			message.setFile("residence2/fortress/fortress_mainpower001.htm");
		}

		player.sendPacket(message);
	}
}