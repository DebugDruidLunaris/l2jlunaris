package services;

import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;

public class GrandIsleofPrayerRace extends Functions
{
	private static final int RACE_STAMP = 10013;
	private static final int SECRET_KEY = 9694;

	public void startRace()
	{
		Skill skill = SkillTable.getInstance().getInfo(Skill.SKILL_EVENT_TIMER, 1);
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(skill == null || player == null || npc == null)
			return;

		getNpc().altUseSkill(skill, player);
		removeItem(player, RACE_STAMP, getItemCount(player, RACE_STAMP));
		show("defautl/32349-2.htm", player, npc);
	}

	public String DialogAppend_32349(Integer val)
	{
		Player player = getSelf();
		if(player == null)
			return "";

		// Нет бафа с таймером
		if(player.getEffectList().getEffectsBySkillId(Skill.SKILL_EVENT_TIMER) == null)
			return "<br>[scripts_services.GrandIsleofPrayerRace:startRace|Start the Race.]";

		// Есть бафф с таймером
		long raceStampsCount = getItemCount(player, RACE_STAMP);
		if(raceStampsCount < 4)
			return "<br>*Race in progress, hurry!*";
		removeItem(player, RACE_STAMP, raceStampsCount);
		addItem(player, SECRET_KEY, 3);
		player.getEffectList().stopEffect(Skill.SKILL_EVENT_TIMER);
		return "<br>Good job, here is your keys.";
	}
}