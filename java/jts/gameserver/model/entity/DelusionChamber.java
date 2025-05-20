package jts.gameserver.model.entity;

import java.util.concurrent.Future;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.data.xml.holder.InstantZoneHolder;
import jts.gameserver.instancemanager.DimensionalRiftManager;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.InstantZone;
import jts.gameserver.utils.Location;

public class DelusionChamber extends DimensionalRift
{
	private Future<?> killRiftTask;

	public DelusionChamber(Party party, int type, int room)
	{
		super(party, type, room);
	}

	@Override
	public synchronized void createNewKillRiftTimer()
	{
		if(killRiftTask != null)
		{
			killRiftTask.cancel(false);
			killRiftTask = null;
		}

		killRiftTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
			@Override
			public void runImpl() throws Exception
			{
				if(getParty() != null && !getParty().getPartyMembers().isEmpty())
					for(Player p : getParty().getPartyMembers())
						if(p.getReflection() == DelusionChamber.this)
						{
							String var = p.getVar("backCoords");
							if(var == null || var.equals(""))
								continue;
							p.teleToLocation(Location.parseLoc(var), ReflectionManager.DEFAULT);
							p.unsetVar("backCoords");
						}
				collapse();
			}
		}, 100L);
	}

	@Override
	public void partyMemberExited(Player player)
	{
		if(getPlayersInside(false) < 2 || getPlayersInside(true) == 0)
		{
			createNewKillRiftTimer();
			return;
		}
	}

	@Override
	public void manualExitRift(Player player, NpcInstance npc)
	{
		if(!player.isInParty() || player.getParty().getReflection() != this)
			return;

		if(!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;
		}

		createNewKillRiftTimer();
	}

	@Override
	public String getName()
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(_roomType + 120);
		return iz.getName();
	}

	@Override
	protected int getManagerId()
	{
		return 32664;
	}
}