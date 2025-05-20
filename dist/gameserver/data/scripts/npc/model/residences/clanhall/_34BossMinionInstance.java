package npc.model.residences.clanhall;

import jts.gameserver.model.Creature;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.npc.NpcTemplate;
import npc.model.residences.SiegeGuardInstance;

@SuppressWarnings("serial")
public abstract class _34BossMinionInstance extends SiegeGuardInstance implements _34SiegeGuard
{
	public _34BossMinionInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onDeath(Creature killer)
	{
		setCurrentHp(1, true);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		Functions.npcShout(this, spawnChatSay());
	}

	public abstract NpcString spawnChatSay();

	@Override
	public abstract NpcString teleChatSay();
}