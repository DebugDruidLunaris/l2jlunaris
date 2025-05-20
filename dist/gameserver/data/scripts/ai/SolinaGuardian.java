package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class SolinaGuardian extends Fighter
{
	public SolinaGuardian(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		getActor().altOnMagicUseTimer(getActor(), SkillTable.getInstance().getInfo(6371, 1));
	}
}