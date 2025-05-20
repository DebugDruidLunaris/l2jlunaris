package npc.model;

import jts.commons.util.Rnd;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.ChestInstance;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.npc.NpcTemplate;

public class TreasureChestInstance extends ChestInstance
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Field TREASURE_BOMB_ID. (value is 4143)
	 */
	private static final int TREASURE_BOMB_ID = 4143;
	
	/**
	 * Constructor for TreasureChestInstance.
	 * @param objectId int
	 * @param template NpcTemplate
	 */
	public TreasureChestInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	/**
	 * Method tryOpen.
	 * @param opener Player
	 * @param skill Skill
	 */
	@Override
	public void tryOpen(Player opener, Skill skill)
	{
		double chance = calcChance(opener, skill);
		if (Rnd.chance(chance))
		{
			getAggroList().addDamageHate(opener, 10000, 0);
			doDie(opener);
		}
		else
		{
			fakeOpen(opener);
		}
	}
	
	/**
	 * Method calcChance.
	 * @param opener Player
	 * @param skill Skill
	 * @return double
	 */
	public double calcChance(Player opener, Skill skill)
	{
		double chance = skill.getActivateRate();
		int npcLvl = getLevel();
		if (!isCommonTreasureChest())
		{
			double levelmod = (double) skill.getMagicLevel() - npcLvl;
			chance += levelmod * skill.getLevelModifier();
		}
		else
		{
			chance = 65;
			int openerLvl = skill.getId() == 22271 ? opener.getLevel() : skill.getMagicLevel();
			int lvlDiff = Math.abs(openerLvl - npcLvl);
			if (((openerLvl <= 77) && (lvlDiff >= 6)) || ((openerLvl >= 78) && (lvlDiff >= 5)))
				chance = 25;

		}
		if (chance < 0)
			chance = 1;
		return chance;
	}
	
	/**
	 * Method fakeOpen.
	 * @param opener Creature
	 */
	private void fakeOpen(Creature opener)
	{
		Skill bomb = SkillTable.getInstance().getInfo(TREASURE_BOMB_ID, getBombLvl());
		if (bomb != null)
		{
			doCast(bomb, opener, false);
		}
		onDecay();
	}
	
	/**
	 * Method getBombLvl.
	 * @return int
	 */
	private int getBombLvl()
	{
		int npcLvl = getLevel();
		int lvl = 1;
		if (npcLvl >= 86)
		{
			lvl = 14;
		}
		else if (npcLvl >= 81)
		{
			lvl = 13;
		}
		else if (npcLvl >= 81)
		{
			lvl = 12;
		}
		else if (npcLvl >= 81)
		{
			lvl = 11;
		}
		else if (npcLvl >= 78)
		{
			lvl = 10;
		}
		else if (npcLvl >= 72)
		{
			lvl = 9;
		}
		else if (npcLvl >= 66)
		{
			lvl = 8;
		}
		else if (npcLvl >= 60)
		{
			lvl = 7;
		}
		else if (npcLvl >= 54)
		{
			lvl = 6;
		}
		else if (npcLvl >= 48)
		{
			lvl = 5;
		}
		else if (npcLvl >= 42)
		{
			lvl = 4;
		}
		else if (npcLvl >= 36)
		{
			lvl = 3;
		}
		else if (npcLvl >= 30)
		{
			lvl = 2;
		}
		return lvl;
	}
	
	/**
	 * Method isCommonTreasureChest.
	 * @return boolean
	 */
	private boolean isCommonTreasureChest()
	{
		int npcId = getNpcId();
		if ((npcId >= 18265) && (npcId <= 18286))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Method onReduceCurrentHp.
	 * @param damage double
	 * @param attacker Creature
	 * @param skill Skill
	 * @param awake boolean
	 * @param standUp boolean
	 * @param directHp boolean
	 */
	@Override
	public void onReduceCurrentHp(final double damage, final Creature attacker, Skill skill, final boolean awake, final boolean standUp, boolean directHp)
	{
		if (!isCommonTreasureChest())
		{
			fakeOpen(attacker);
		}
	}
}