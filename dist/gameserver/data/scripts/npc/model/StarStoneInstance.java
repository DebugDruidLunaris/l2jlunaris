package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public final class StarStoneInstance extends NpcInstance
{
	public StarStoneInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg) {}

	@Override
	public void showChatWindow(Player player, String filename, Object... replace) {}

	@Override
	public void onBypassFeedback(Player player, String command) {}
}