package ai.monastery_of_silence;

import java.util.List;

import jts.gameserver.ai.Fighter;
import jts.gameserver.instancemanager.SpawnManager;
import jts.gameserver.model.Spawner;
import jts.gameserver.model.instances.NpcInstance;

public class DivinityMonster extends Fighter
{
	private final boolean _isDefault;
	private final String _nextMakerName;

	public DivinityMonster(NpcInstance actor)
	{
		super(actor);

		_isDefault = actor.getParameter("is_default", false);
		_nextMakerName = actor.getParameter("next_maker_name", null);
	}

	@Override
	public void onEvtDeSpawn()
	{
		if(_isDefault)
		{
			List<Spawner> maker = SpawnManager.getInstance().getSpawners(_nextMakerName);
			if(maker.isEmpty())
				SpawnManager.getInstance().spawn(_nextMakerName);
		}
		super.onEvtDeSpawn();
	}
}