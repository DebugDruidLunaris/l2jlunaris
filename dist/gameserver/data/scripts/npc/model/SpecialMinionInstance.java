package npc.model;

import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public final class SpecialMinionInstance extends MonsterInstance
{
	public SpecialMinionInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}

	@Override
	public void onRandomAnimation()
	{
		return;
	}
}