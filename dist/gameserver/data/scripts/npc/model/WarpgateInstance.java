package npc.model;

import jts.gameserver.Config;
import jts.gameserver.instancemanager.HellboundManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class WarpgateInstance extends NpcInstance
{
	public WarpgateInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("enter_hellbound"))
		{
			if(Config.HELLBOUND_ENTER_NOQUEST)
			{
				player.teleToLocation(-11272, 236464, -3248);
				return;
			}
			else
			if(HellboundManager.getHellboundLevel() != 0 && (player.isQuestCompleted("_130_PathToHellbound") || player.isQuestCompleted("_133_ThatsBloodyHot")))
			{
				player.teleToLocation(-11272, 236464, -3248);
				return;
			}
			else if(HellboundManager.getConfidence() < 1 && (player.isQuestCompleted("_130_PathToHellbound")))
			{
				HellboundManager.setConfidence(1);
				player.teleToLocation(-11272, 236464, -3248);
				
			}
			else
			{
				showChatWindow(player, "default/32318-1.htm");
				return;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}