package jts.gameserver.model.instances;

import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class NpcNotSayInstance extends NpcInstance
{
	public NpcNotSayInstance(final int objectID, final NpcTemplate template)
	{
		super(objectID, template);
		setHasChatWindow(false);
	}
}