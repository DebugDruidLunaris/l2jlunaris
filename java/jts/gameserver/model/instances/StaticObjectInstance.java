package jts.gameserver.model.instances;

import java.util.Collections;
import java.util.List;

import jts.commons.lang.reference.HardReference;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.reference.L2Reference;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.MyTargetSelected;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.ShowTownMap;
import jts.gameserver.network.serverpackets.StaticObject;
import jts.gameserver.scripts.Events;
import jts.gameserver.templates.StaticObjectTemplate;
import jts.gameserver.utils.Location;

@SuppressWarnings("serial")
public class StaticObjectInstance extends GameObject
{
	private final HardReference<StaticObjectInstance> reference;
	private final StaticObjectTemplate _template;
	private int _meshIndex;

	public StaticObjectInstance(int objectId, StaticObjectTemplate template)
	{
		super(objectId);

		_template = template;
		reference = new L2Reference<StaticObjectInstance>(this);
	}

	@Override
	public HardReference<StaticObjectInstance> getRef()
	{
		return reference;
	}

	public int getUId()
	{
		return _template.getUId();
	}

	public int getType()
	{
		return _template.getType();
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;

		if(player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			return;
		}

		MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
		player.sendPacket(my);

		if(!isInRange(player, 150))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}

		if(_template.getType() == 0) // Arena Board
			player.sendPacket(new NpcHtmlMessage(player, getUId(), "newspaper/arena.htm", 0));
		else if(_template.getType() == 2) // Village map
		{
			player.sendPacket(new ShowTownMap(_template.getFilePath(), _template.getMapX(), _template.getMapY()));
			player.sendActionFailed();
		}
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		return Collections.<L2GameServerPacket> singletonList(new StaticObject(this));
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}

	public void broadcastInfo(boolean force)
	{
		StaticObject p = new StaticObject(this);
		for(Player player : World.getAroundPlayers(this))
			player.sendPacket(p);
	}

	@Override
	public int getGeoZ(Location loc) //FIXME [VISTALL] нужно ли?
	{
		return loc.z;
	}

	public int getMeshIndex()
	{
		return _meshIndex;
	}

	public void setMeshIndex(int meshIndex)
	{
		_meshIndex = meshIndex;
	}
}