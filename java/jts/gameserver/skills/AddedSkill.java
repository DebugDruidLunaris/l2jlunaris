package jts.gameserver.skills;

import jts.gameserver.model.Skill;
import jts.gameserver.tables.SkillTable;

public class AddedSkill
{
	public static final AddedSkill[] EMPTY_ARRAY = new AddedSkill[0];
	private Skill _skill;
	public int id;
	public int level;

	public AddedSkill(int id, int level)
	{
		this.id = id;
		this.level = level;
	}

	public Skill getSkill()
	{
		if(_skill == null)
		{
			_skill = SkillTable.getInstance().getInfo(id, level);
		}

		return _skill;
	}
}