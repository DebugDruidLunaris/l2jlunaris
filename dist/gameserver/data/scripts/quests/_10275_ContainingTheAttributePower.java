package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.Inventory;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.tables.SkillTable;

public class _10275_ContainingTheAttributePower extends Quest implements ScriptFile
{
	private final static int Holly = 30839;
	private final static int Weber = 31307;
	private final static int Yin = 32325;
	private final static int Yang = 32326;
	private final static int Water = 27380;
	private final static int Air = 27381;

	private final static int YinSword = 13845;
	private final static int YangSword = 13881;
	private final static int SoulPieceWater = 13861;
	private final static int SoulPieceAir = 13862;

	@Override
	public void onLoad()
	{
	}

	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
	}

	public _10275_ContainingTheAttributePower()
	{
		super(false);

		addStartNpc(Holly);
		addStartNpc(Weber);

		addTalkId(Yin);
		addTalkId(Yang);

		addKillId(Air);
		addKillId(Water);

		addQuestItem(YinSword, YangSword, SoulPieceWater, SoulPieceAir);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;

		Player player = st.getPlayer();

		if(event.equalsIgnoreCase("30839-02.htm") || event.equalsIgnoreCase("31307-02.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("30839-05.htm"))
		{
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("31307-05.htm"))
		{
			st.setCond(7);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("32325-03.htm"))
		{
			st.setCond(3);
			st.giveItems(YinSword, 1, Element.FIRE, 10);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("32326-03.htm"))
		{
			st.setCond(8);
			st.giveItems(YangSword, 1, Element.EARTH, 10);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("32325-06.htm"))
		{
			if(st.getQuestItemsCount(YinSword) > 0)
			{
				st.takeItems(YinSword, 1);
				htmltext = "32325-07.htm";
			}
			st.giveItems(YinSword, 1, Element.FIRE, 10);
		}
		else if(event.equalsIgnoreCase("32326-06.htm"))
		{
			if(st.getQuestItemsCount(YangSword) > 0)
			{
				st.takeItems(YangSword, 1);
				htmltext = "32326-07.htm";
			}
			st.giveItems(YangSword, 1, Element.EARTH, 10);
		}
		else if(event.equalsIgnoreCase("32325-09.htm"))
		{
			st.setCond(5);
			SkillTable.getInstance().getInfo(2635, 1).getEffects(player, player, false, false);
			st.giveItems(YinSword, 1, Element.FIRE, 10);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("32326-09.htm"))
		{
			st.setCond(10);
			SkillTable.getInstance().getInfo(2636, 1).getEffects(player, player, false, false);
			st.giveItems(YangSword, 1, Element.EARTH, 10);
			st.soundEffect(SOUND_MIDDLE);
		}
		else
		{
			int item = 0;

			if(event.equalsIgnoreCase("1"))
				item = 10521;
			else if(event.equalsIgnoreCase("2"))
				item = 10522;
			else if(event.equalsIgnoreCase("3"))
				item = 10523;
			else if(event.equalsIgnoreCase("4"))
				item = 10524;
			else if(event.equalsIgnoreCase("5"))
				item = 10525;
			else if(event.equalsIgnoreCase("6"))
				item = 10526;

			if(item > 0)
			{
				st.giveItems(item, 2);
				st.addExpAndSp(202160, 20375);
				st.exitQuest(false);
				st.soundEffect(SOUND_FINISH);
				if(npc != null)
					htmltext = str(npc.getNpcId()) + "-1" + event + ".htm";
				else
					htmltext = null;
			}
		}

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if(id == COMPLETED)
		{
			if(npcId == Holly)
				htmltext = "30839-0a.htm";
			else if(npcId == Weber)
				htmltext = "31307-0a.htm";
		}
		else if(id == CREATED)
			if(st.getPlayer().getLevel() >= 76)
				if(npcId == Holly)
					htmltext = "30839-01.htm";
				else
					htmltext = "31307-01.htm";
			else if(npcId == Holly)
				htmltext = "30839-00.htm";
			else
				htmltext = "31307-00.htm";
		else if(npcId == Holly)
		{
			if(cond == 1)
				htmltext = "30839-03.htm";
			else if(cond == 2)
				htmltext = "30839-05.htm";
		}
		else if(npcId == Weber)
		{
			if(cond == 1)
				htmltext = "31307-03.htm";
			else if(cond == 7)
				htmltext = "31307-05.htm";
		}
		else if(npcId == Yin)
		{
			if(cond == 2)
				htmltext = "32325-01.htm";
			else if(cond == 3 || cond == 5)
				htmltext = "32325-04.htm";
			else if(cond == 4)
			{
				htmltext = "32325-08.htm";
				st.takeItems(YinSword, 1);
				st.takeItems(SoulPieceWater, -1);
			}
			else if(cond == 6)
				htmltext = "32325-10.htm";
		}
		else if(npcId == Yang)
			if(cond == 7)
				htmltext = "32326-01.htm";
			else if(cond == 8 || cond == 10)
				htmltext = "32326-04.htm";
			else if(cond == 9)
			{
				htmltext = "32326-08.htm";
				st.takeItems(YangSword, 1);
				st.takeItems(SoulPieceAir, -1);
			}
			else if(cond == 11)
				htmltext = "32326-10.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != STARTED)
			return null;

		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if(npcId == Air)
		{
			if(st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == YangSword && (cond == 8 || cond == 10) && st.getQuestItemsCount(SoulPieceAir) < 6 && Rnd.chance(30))
			{
				st.giveItems(SoulPieceAir, 1);
				if(st.getQuestItemsCount(SoulPieceAir) >= 6)
				{
					st.setCond(cond + 1);
					st.soundEffect(SOUND_MIDDLE);
				}
			}
		}
		else if(npcId == Water)
			if(st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == YinSword && (cond == 3 || cond == 5) && st.getQuestItemsCount(SoulPieceWater) < 6 && Rnd.chance(30))
			{
				st.giveItems(SoulPieceWater, 1);
				if(st.getQuestItemsCount(SoulPieceWater) >= 6)
				{
					st.setCond(cond + 1);
					st.soundEffect(SOUND_MIDDLE);
				}
			}

		return null;
	}
}