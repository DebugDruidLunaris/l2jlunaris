package jts.gameserver.model.instances;

import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class SpecialMonsterInstance extends MonsterInstance
{
	public SpecialMonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}