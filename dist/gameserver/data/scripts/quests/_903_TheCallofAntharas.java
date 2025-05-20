package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * @author pchayka
 *         Daily quest
 */
public class _903_TheCallofAntharas extends Quest implements ScriptFile
{
	private static final int Theodric = 30755;
	private static final int BehemothDragonLeather = 21992;
	private static final int TaraskDragonsLeatherFragment = 21991;

	private static final int TaraskDragon = 29190;
	private static final int BehemothDragon = 29069;

	public _903_TheCallofAntharas()
	{
		super(PARTY_ALL);
		addStartNpc(Theodric);
		addKillId(TaraskDragon, BehemothDragon);
		addQuestItem(BehemothDragonLeather, TaraskDragonsLeatherFragment);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("theodric_q903_03.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("theodric_q903_06.htm"))
		{
			st.takeAllItems(BehemothDragonLeather);
			st.takeAllItems(TaraskDragonsLeatherFragment);
			st.giveItems(21897, 1); // Scroll: Antharas Call
			st.setState(COMPLETED);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(this);
		}

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npc.getNpcId() == Theodric)
			switch(st.getState())
			{
				case CREATED:
					if(st.isDailyQuest())
					{
						if(st.getPlayer().getLevel() >= 83)
						{
							if(st.ownItemCount(3865) > 0)
								htmltext = "theodric_q903_01.htm";
							else
								htmltext = "theodric_q903_00b.htm";
						}
						else
						{
							htmltext = "theodric_q903_00.htm";
							st.exitQuest(true);
						}
					}
					else
						htmltext = "theodric_q903_00a.htm";
					break;
				case STARTED:
					if(cond == 1)
						htmltext = "theodric_q903_04.htm";
					else if(cond == 2)
						htmltext = "theodric_q903_05.htm";
					break;
			}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			switch(npc.getNpcId())
			{
				case TaraskDragon:
					if(st.ownItemCount(TaraskDragonsLeatherFragment) < 1)
						st.giveItems(TaraskDragonsLeatherFragment, 1);
					break;
				case BehemothDragon:
					if(st.ownItemCount(BehemothDragonLeather) < 1)
						st.giveItems(BehemothDragonLeather, 1);
					break;
				default:
					break;
			}
			if(st.ownItemCount(BehemothDragonLeather) > 0 && st.ownItemCount(TaraskDragonsLeatherFragment) > 0)
				st.setCond(2);
		}
		return null;
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}