package jts.gameserver.model.instances;

import jts.gameserver.model.Player;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class NoActionNpcInstance extends NpcInstance
{
	public NoActionNpcInstance(final int objectID, final NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onAction(final Player player, final boolean dontMove)
	{
		player.sendActionFailed();
	}
}