package npc.model.residences.fortress.siege;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class BallistaInstance extends NpcInstance
{
	public BallistaInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		if((killer == null) || !killer.isPlayer())
			return;

		Player player = killer.getPlayer();

		if(player.getClan() == null)
			return;

		player.getClan().incReputation(30, false, "Ballista " + getTitle());
		player.sendPacket(new SystemMessage2(SystemMsg.THE_BALLISTA_HAS_BEEN_SUCCESSFULLY_DESTROYED));
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg) {}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}
}