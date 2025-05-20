package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class KegorNpcInstance extends NpcInstance
{
	public KegorNpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String htmlpath = null;
		if(getReflection().isDefault())
			htmlpath = "default/32761-default.htm";
		else
			htmlpath = "default/32761.htm";
		return htmlpath;
	}
}