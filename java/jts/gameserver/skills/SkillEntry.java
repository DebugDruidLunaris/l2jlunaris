package jts.gameserver.skills;

import java.util.AbstractMap;

import jts.gameserver.model.Skill;

@SuppressWarnings("serial")
public class SkillEntry extends AbstractMap.SimpleImmutableEntry<SkillEntryType, Skill>
{
	private boolean _disabled;

	public SkillEntry(SkillEntryType key, Skill value)
	{
		super(key, value);
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean disabled)
	{
		_disabled = disabled;
	}
}