package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class TriolsMirrorInstance extends NpcInstance
{
	public TriolsMirrorInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(getNpcId() == 32040)
			player.teleToLocation(-12766, -35840, -10856); //to pagan
		else if(getNpcId() == 32039)
			player.teleToLocation(35079, -49758, -760); //from pagan
	}
}