package jts.gameserver.model.instances;

import jts.commons.lang.reference.HardReference;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.reference.L2Reference;
import jts.gameserver.network.serverpackets.MyTargetSelected;

@SuppressWarnings("serial")
public class ControlKeyInstance extends GameObject
{
	protected HardReference<ControlKeyInstance> reference;

	public ControlKeyInstance()
	{
		super(IdFactory.getInstance().getNextId());
		reference = new L2Reference<ControlKeyInstance>(this);
	}

	@Override
	public HardReference<ControlKeyInstance> getRef()
	{
		return reference;
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			return;
		}

		player.sendActionFailed();
	}
}