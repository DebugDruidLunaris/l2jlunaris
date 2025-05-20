package jts.gameserver.model.instances;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class ChestInstance extends MonsterInstance
{
	public ChestInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	public void tryOpen(Player opener, Skill skill)
	{
		getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, opener, 100);
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}