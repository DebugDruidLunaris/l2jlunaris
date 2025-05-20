package jts.gameserver.model.instances;

import jts.gameserver.ai.CharacterAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.network.serverpackets.Die;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class DeadManInstance extends NpcInstance
{
	public DeadManInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setAI(new CharacterAI(this));
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		setCurrentHp(0, false);
		broadcastPacket(new Die(this));
		setWalking();
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage) {}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isBlocked()
	{
		return true;
	}
}