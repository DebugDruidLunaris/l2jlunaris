package jts.gameserver.model.instances;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class GuardInstance extends NpcInstance
{
	public GuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.isMonster() && ((MonsterInstance) attacker).isAggressive() || attacker.isPlayable() && attacker.getKarma() > 0;
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "guard/" + pom + ".htm";
	}

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

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		getAggroList().addDamageHate(attacker, (int) damage, 0);

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
}