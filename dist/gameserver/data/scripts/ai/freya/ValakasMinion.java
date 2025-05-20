package ai.freya;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import bosses.ValakasManager;

public class ValakasMinion extends Mystic
{
	public ValakasMinion(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		for(Player p : ValakasManager.getZone().getInsidePlayers())
			notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 5000);
	}
}