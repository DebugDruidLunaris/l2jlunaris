package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _290_ThreatRemoval extends Quest implements ScriptFile
{
	// npc
	private static final int pinaps = 30201;

	// questitem
	private static final int q_id_tag_of_xel = 15714;

	// etcitem
	private static final int scrl_of_ench_wp_s = 959;
	private static final int scrl_of_ench_am_s = 960;
	private static final int high_ore_of_fire = 9552;

	// mobs
	private static final int xel_trainer_mage = 22775;
	private static final int xel_trainer_high_mage = 22776;
	private static final int xel_trainer_warrior = 22777;
	private static final int xel_trainer_sniper = 22778;
	private static final int xel_recruit_mage = 22780;
	private static final int xel_recruit_high_mage = 22781;
	private static final int xel_recruit_warrior = 22782;
	private static final int xel_recruit_high_warrior = 22783;
	private static final int xel_recruit_sniper = 22784;
	private static final int xel_recruit_high_sniper = 22785;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _290_ThreatRemoval()
	{
		super(PARTY_ONE);
		addStartNpc(pinaps);
		addKillId(xel_trainer_mage, xel_trainer_high_mage, xel_trainer_warrior, xel_trainer_sniper, xel_recruit_mage, xel_recruit_high_mage, xel_recruit_warrior, xel_recruit_high_warrior, xel_recruit_sniper, xel_recruit_high_sniper);
		addQuestItem(q_id_tag_of_xel);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("remove_the_cause");

		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.setMemoState("remove_the_cause", String.valueOf(1), true);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			htmltext = "pinaps_q0290_03.htm";
		}
		else if(event.equalsIgnoreCase("reply_1"))
		{
			if(GetMemoState == 1 && st.ownItemCount(q_id_tag_of_xel) >= 400)
			{
				st.takeItems(q_id_tag_of_xel, 400);
				int i0 = Rnd.get(10);
				if(i0 == 0)
					st.giveItems(scrl_of_ench_wp_s, 1);
				else if(i0 >= 1 && i0 < 4)
					st.giveItems(scrl_of_ench_am_s, 1);
				else if(i0 >= 4 && i0 < 6)
					st.giveItems(scrl_of_ench_am_s, 2);
				else if(i0 >= 6 && i0 < 7)
					st.giveItems(scrl_of_ench_am_s, 3);
				else if(i0 >= 7 && i0 < 9)
					st.giveItems(high_ore_of_fire, 1);
				else
					st.giveItems(high_ore_of_fire, 2);
				st.soundEffect(SOUND_FINISH);
				htmltext = "pinaps_q0290_06.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			if(GetMemoState == 1 && st.ownItemCount(q_id_tag_of_xel) > 1)
				htmltext = "pinaps_q0290_08.htm";
			else if(GetMemoState == 1 && st.ownItemCount(q_id_tag_of_xel) == 0)
			{
				st.takeItems(q_id_tag_of_xel, -1);
				st.removeMemo("remove_the_cause");
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
				htmltext = "pinaps_q0290_09.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_4"))
			if(GetMemoState == 1)
			{
				st.takeItems(q_id_tag_of_xel, -1);
				st.removeMemo("remove_the_cause");
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
				htmltext = "pinaps_q0290_10.htm";
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		QuestState qs = st.getPlayer().getQuestState(_251_NoSecrets.class);
		int GetMemoState = st.getInt("remove_the_cause");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == pinaps)
					if(st.getPlayer().getLevel() >= 82 && qs != null && qs.isCompleted())
						htmltext = "pinaps_q0290_02.htm";
					else
					{
						htmltext = "pinaps_q0290_01.htm";
						st.exitQuest(true);
					}
				break;
			case STARTED:
				if(npcId == pinaps)
					if(GetMemoState == 1 && st.ownItemCount(q_id_tag_of_xel) < 400)
						htmltext = "pinaps_q0290_04.htm";
					else if(GetMemoState == 1 && st.ownItemCount(q_id_tag_of_xel) >= 400)
						htmltext = "pinaps_q0290_05.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("remove_the_cause");
		int npcId = npc.getNpcId();

		if(GetMemoState == 1)
			if(npcId == xel_trainer_mage)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 932)
				{
					st.giveItems(q_id_tag_of_xel, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
			}
			else if(npcId == xel_trainer_high_mage)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 397)
				{
					st.giveItems(q_id_tag_of_xel, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
			}
			else if(npcId == xel_trainer_warrior)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 932)
				{
					st.giveItems(q_id_tag_of_xel, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
			}
			else if(npcId == xel_trainer_sniper)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 932)
				{
					st.giveItems(q_id_tag_of_xel, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
			}
			else if(npcId == xel_recruit_mage || npcId == xel_recruit_warrior || npcId == xel_recruit_sniper)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 363)
				{
					st.giveItems(q_id_tag_of_xel, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
			}
			else if(npcId == xel_recruit_high_mage)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 483)
				{
					st.giveItems(q_id_tag_of_xel, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
			}
			else if(npcId == xel_recruit_high_warrior)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 352)
				{
					st.giveItems(q_id_tag_of_xel, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
			}
			else if(npcId == xel_recruit_high_sniper)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 169)
				{
					st.giveItems(q_id_tag_of_xel, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
			}
		return null;
	}
}