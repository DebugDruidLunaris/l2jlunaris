package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _411_PathToAssassin extends Quest implements ScriptFile
{
	//npc
	public final int TRISKEL = 30416;
	public final int LEIKAN = 30382;
	public final int ARKENIA = 30419;
	//mobs
	public final int MOONSTONE_BEAST = 20369;
	public final int CALPICO = 27036;
	//items
	public final int SHILENS_CALL_ID = 1245;
	public final int ARKENIAS_LETTER_ID = 1246;
	public final int LEIKANS_NOTE_ID = 1247;
	public final int ONYX_BEASTS_MOLAR_ID = 1248;
	public final int LEIKANS_KNIFE_ID = 1249;
	public final int SHILENS_TEARS_ID = 1250;
	public final int ARKENIA_RECOMMEND_ID = 1251;
	public final int IRON_HEART_ID = 1252;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _411_PathToAssassin()
	{
		super(false);

		addStartNpc(TRISKEL);

		addTalkId(LEIKAN);
		addTalkId(ARKENIA);

		addKillId(MOONSTONE_BEAST);
		addKillId(CALPICO);

		addQuestItem(new int[] {
				SHILENS_CALL_ID,
				LEIKANS_NOTE_ID,
				LEIKANS_KNIFE_ID,
				ARKENIA_RECOMMEND_ID,
				ARKENIAS_LETTER_ID,
				ONYX_BEASTS_MOLAR_ID,
				SHILENS_TEARS_ID });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("1"))
		{
			if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 0x1f && st.ownItemCount(IRON_HEART_ID) < 1)
			{
				st.setCond(1);
				st.setState(STARTED);
				st.soundEffect(SOUND_ACCEPT);
				st.giveItems(SHILENS_CALL_ID, 1);
				htmltext = "triskel_q0411_05.htm";
			}
			else if(st.getPlayer().getClassId().getId() != 0x1f)
			{
				if(st.getPlayer().getClassId().getId() == 0x23)
					htmltext = "triskel_q0411_02a.htm";
				else
				{
					htmltext = "triskel_q0411_02.htm";
					st.exitQuest(true);
				}
			}
			else if(st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 0x1f)
			{
				htmltext = "triskel_q0411_03.htm";
				st.exitQuest(true);
			}
			else if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 0x1f && st.ownItemCount(IRON_HEART_ID) > 0)
				htmltext = "triskel_q0411_04.htm";
		}
		else if(event.equalsIgnoreCase("30419_1"))
		{
			htmltext = "arkenia_q0411_05.htm";
			st.takeItems(SHILENS_CALL_ID, -1);
			st.giveItems(ARKENIAS_LETTER_ID, 1);
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("30382_1"))
		{
			htmltext = "guard_leikan_q0411_03.htm";
			st.takeItems(ARKENIAS_LETTER_ID, -1);
			st.giveItems(LEIKANS_NOTE_ID, 1);
			st.setCond(3);
			st.soundEffect(SOUND_MIDDLE);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == TRISKEL)
		{
			if(cond < 1)
			{
				if(st.ownItemCount(IRON_HEART_ID) < 1)
					htmltext = "triskel_q0411_01.htm";
				else
					htmltext = "triskel_q0411_04.htm";
			}
			else if(cond == 7)
			{
				htmltext = "triskel_q0411_06.htm";
				st.takeItems(ARKENIA_RECOMMEND_ID, -1);
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(IRON_HEART_ID, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(228064, 16455);
						//FIXME [G1ta0] дать адены, только если первый чар на акке
						st.giveItems(ADENA_ID, 81900);
					}
				}
				st.exitQuest(true);
				st.soundEffect(SOUND_FINISH);
			}
			else if(cond == 2)
				htmltext = "triskel_q0411_07.htm";
			else if(cond == 1)
				htmltext = "triskel_q0411_11.htm";
			else if(cond > 2 && cond < 7)
				if(cond > 2 && cond < 5)
					htmltext = "triskel_q0411_08.htm";
				else if(cond > 4 && cond < 7)
					if(st.ownItemCount(SHILENS_TEARS_ID) < 1)
						htmltext = "triskel_q0411_09.htm";
					else
						htmltext = "triskel_q0411_10.htm";
		}
		else if(npcId == ARKENIA)
		{
			if(cond == 1 && st.ownItemCount(SHILENS_CALL_ID) > 0)
				htmltext = "arkenia_q0411_01.htm";
			else if(cond == 2 && st.ownItemCount(ARKENIAS_LETTER_ID) > 0)
				htmltext = "arkenia_q0411_07.htm";
			else if(cond > 2 && cond < 5 && st.ownItemCount(LEIKANS_NOTE_ID) > 0)
				htmltext = "arkenia_q0411_10.htm";
			else if(cond == 5 && st.ownItemCount(LEIKANS_KNIFE_ID) > 0)
				htmltext = "arkenia_q0411_11.htm";
			else if(cond == 6 && st.ownItemCount(SHILENS_TEARS_ID) > 0)
			{
				htmltext = "arkenia_q0411_08.htm";
				st.takeItems(SHILENS_TEARS_ID, -1);
				st.takeItems(LEIKANS_KNIFE_ID, -1);
				st.giveItems(ARKENIA_RECOMMEND_ID, 1);
				st.setCond(7);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(cond == 7)
				htmltext = "arkenia_q0411_09.htm";
		}
		else if(npcId == LEIKAN)
			if(cond == 2 && st.ownItemCount(ARKENIAS_LETTER_ID) > 0)
				htmltext = "guard_leikan_q0411_01.htm";
			else if(cond > 2 && cond < 4 && st.ownItemCount(ONYX_BEASTS_MOLAR_ID) < 1)
			{
				htmltext = "guard_leikan_q0411_05.htm";
				if(cond == 4)
					st.setCond(3);
			}
			else if(cond > 2 && cond < 4 && st.ownItemCount(ONYX_BEASTS_MOLAR_ID) < 10)
			{
				htmltext = "guard_leikan_q0411_06.htm";
				if(cond == 4)
					st.setCond(3);
			}
			else if(cond == 4 && st.ownItemCount(ONYX_BEASTS_MOLAR_ID) > 9)
			{
				htmltext = "guard_leikan_q0411_07.htm";
				st.takeItems(ONYX_BEASTS_MOLAR_ID, -1);
				st.takeItems(LEIKANS_NOTE_ID, -1);
				st.giveItems(LEIKANS_KNIFE_ID, 1);
				st.setCond(5);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(cond > 4 && cond < 7 && st.ownItemCount(SHILENS_TEARS_ID) < 1)
			{
				htmltext = "guard_leikan_q0411_09.htm";
				if(cond == 6)
					st.setCond(5);
			}
			else if(cond == 6 && st.ownItemCount(SHILENS_TEARS_ID) > 0)
				htmltext = "guard_leikan_q0411_08.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == CALPICO)
		{
			if(cond == 5 && st.ownItemCount(LEIKANS_KNIFE_ID) > 0 && st.ownItemCount(SHILENS_TEARS_ID) < 1)
			{
				st.giveItems(SHILENS_TEARS_ID, 1);
				st.soundEffect(SOUND_MIDDLE);
				st.setCond(6);
			}
		}
		else if(npcId == MOONSTONE_BEAST)
			if(cond == 3 && st.ownItemCount(LEIKANS_NOTE_ID) > 0 && st.ownItemCount(ONYX_BEASTS_MOLAR_ID) < 10)
			{
				st.giveItems(ONYX_BEASTS_MOLAR_ID, 1);
				if(st.ownItemCount(ONYX_BEASTS_MOLAR_ID) > 9)
				{
					st.soundEffect(SOUND_MIDDLE);
					st.setCond(4);
				}
				else
					st.soundEffect(SOUND_ITEMGET);
			}
		return null;
	}
}