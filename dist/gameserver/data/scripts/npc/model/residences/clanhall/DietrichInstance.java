package npc.model.residences.clanhall;

import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class DietrichInstance extends _34BossMinionInstance
{
	public DietrichInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public NpcString spawnChatSay()
	{
		return NpcString.SOLDIERS_OF_GUSTAV_GO_FORTH_AND_DESTROY_THE_INVADERS;
	}

	@Override
	public NpcString teleChatSay()
	{
		return NpcString.AH_THE_BITTER_TASTE_OF_DEFEAT_I_FEAR_MY_TORMENTS_ARE_NOT_OVER;
	}
}