package jts.gameserver.model.instances;

import jts.gameserver.model.Player;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class BlockInstance extends NpcInstance
{
	private boolean _isRed;

	public BlockInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	public boolean isRed()
	{
		return _isRed;
	}

	public void setRed(boolean red)
	{
		_isRed = red;
		broadcastCharInfo();
	}

	public void changeColor()
	{
		setRed(!_isRed);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg) {}

	@Override
	public boolean isNameAbove()
	{
		return false;
	}

	@Override
	public int getFormId()
	{
		return _isRed ? 0x53 : 0;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}
}