package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _263_OrcSubjugation extends Quest implements ScriptFile
{
	// NPC
	public final int KAYLEEN = 30346;

	// MOBS
	public final int BALOR_ORC_ARCHER = 20385;
	public final int BALOR_ORC_FIGHTER = 20386;
	public final int BALOR_ORC_FIGHTER_LEADER = 20387;
	public final int BALOR_ORC_LIEUTENANT = 20388;

	public final int ORC_AMULET = 1116;
	public final int ORC_NECKLACE = 1117;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _263_OrcSubjugation()
	{
		super(false);
		addStartNpc(KAYLEEN);
		addKillId(new int[] { BALOR_ORC_ARCHER, BALOR_ORC_FIGHTER, BALOR_ORC_FIGHTER_LEADER, BALOR_ORC_LIEUTENANT });
		addQuestItem(new int[] { ORC_AMULET, ORC_NECKLACE });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("sentry_kayleen_q0263_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("sentry_kayleen_q0263_06.htm"))
		{
			st.setCond(0);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 8 && st.getPlayer().getRace() == Race.darkelf)
			{
				htmltext = "sentry_kayleen_q0263_02.htm";
				return htmltext;
			}
			else if(st.getPlayer().getRace() != Race.darkelf)
			{
				htmltext = "sentry_kayleen_q0263_00.htm";
				st.exitQuest(true);
			}
			else if(st.getPlayer().getLevel() < 8)
			{
				htmltext = "sentry_kayleen_q0263_01.htm";
				st.exitQuest(true);
			}
		}
		else if(cond == 1)
			if(st.ownItemCount(ORC_AMULET) == 0 && st.ownItemCount(ORC_NECKLACE) == 0)
				htmltext = "sentry_kayleen_q0263_04.htm";
			else if(st.ownItemCount(ORC_AMULET) + st.ownItemCount(ORC_NECKLACE) >= 10)
			{
				htmltext = "sentry_kayleen_q0263_05.htm";
				st.giveItems(ADENA_ID, st.ownItemCount(ORC_AMULET) * 20 + st.ownItemCount(ORC_NECKLACE) * 30 + 1100);
				st.takeItems(ORC_AMULET, -1);
				st.takeItems(ORC_NECKLACE, -1);
			}
			else
			{
				htmltext = "sentry_kayleen_q0263_05.htm";
				st.giveItems(ADENA_ID, st.ownItemCount(ORC_AMULET) * 20 + st.ownItemCount(ORC_NECKLACE) * 30);
				st.takeItems(ORC_AMULET, -1);
				st.takeItems(ORC_NECKLACE, -1);
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.getCond() == 1 && Rnd.chance(60))
		{
			if(npcId == BALOR_ORC_ARCHER)
				st.giveItems(ORC_AMULET, 1);
			else if(npcId == BALOR_ORC_FIGHTER || npcId == BALOR_ORC_FIGHTER_LEADER || npcId == BALOR_ORC_LIEUTENANT)
				st.giveItems(ORC_NECKLACE, 1);
			st.soundEffect(SOUND_ITEMGET);
		}
		return null;
	}
}