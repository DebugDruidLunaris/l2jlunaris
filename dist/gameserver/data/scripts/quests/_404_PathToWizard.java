package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _404_PathToWizard extends Quest implements ScriptFile
{
	//npc
	public final int PARINA = 30391;
	public final int EARTH_SNAKE = 30409;
	public final int WASTELAND_LIZARDMAN = 30410;
	public final int FLAME_SALAMANDER = 30411;
	public final int WIND_SYLPH = 30412;
	public final int WATER_UNDINE = 30413;
	//mobs
	public final int RED_BEAR = 20021;
	public final int RATMAN_WARRIOR = 20359;
	public final int WATER_SEER = 27030;
	//items
	public final int MAP_OF_LUSTER_ID = 1280;
	public final int KEY_OF_FLAME_ID = 1281;
	public final int FLAME_EARING_ID = 1282;
	public final int BROKEN_BRONZE_MIRROR_ID = 1283;
	public final int WIND_FEATHER_ID = 1284;
	public final int WIND_BANGEL_ID = 1285;
	public final int RAMAS_DIARY_ID = 1286;
	public final int SPARKLE_PEBBLE_ID = 1287;
	public final int WATER_NECKLACE_ID = 1288;
	public final int RUST_GOLD_COIN_ID = 1289;
	public final int RED_SOIL_ID = 1290;
	public final int EARTH_RING_ID = 1291;
	public final int BEAD_OF_SEASON_ID = 1292;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _404_PathToWizard()
	{
		super(false);

		addStartNpc(PARINA);

		addTalkId(EARTH_SNAKE);
		addTalkId(WASTELAND_LIZARDMAN);
		addTalkId(FLAME_SALAMANDER);
		addTalkId(WIND_SYLPH);
		addTalkId(WATER_UNDINE);

		addKillId(RED_BEAR);
		addKillId(RATMAN_WARRIOR);
		addKillId(WATER_SEER);

		addQuestItem(new int[] {
				KEY_OF_FLAME_ID,
				MAP_OF_LUSTER_ID,
				WIND_FEATHER_ID,
				BROKEN_BRONZE_MIRROR_ID,
				SPARKLE_PEBBLE_ID,
				RAMAS_DIARY_ID,
				RED_SOIL_ID,
				RUST_GOLD_COIN_ID,
				FLAME_EARING_ID,
				WIND_BANGEL_ID,
				WATER_NECKLACE_ID,
				EARTH_RING_ID });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("1"))
		{
			if(st.getPlayer().getClassId().getId() == 0x0a)
			{
				if(st.getPlayer().getLevel() >= 18)
				{
					if(st.ownItemCount(BEAD_OF_SEASON_ID) > 0)
						htmltext = "parina_q0404_03.htm";
					else
					{
						htmltext = "parina_q0404_08.htm";
						st.setCond(1);
						st.setState(STARTED);
						st.soundEffect(SOUND_ACCEPT);
					}
				}
				else
					htmltext = "parina_q0404_02.htm";
			}
			else if(st.getPlayer().getClassId().getId() == 0x0b)
				htmltext = "parina_q0404_02a.htm";
			else
				htmltext = "parina_q0404_01.htm";
		}
		else if(event.equalsIgnoreCase("30410_1"))
			if(st.ownItemCount(WIND_FEATHER_ID) < 1)
			{
				htmltext = "lizardman_of_wasteland_q0404_03.htm";
				st.giveItems(WIND_FEATHER_ID, 1);
				st.setCond(6);
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == PARINA)
		{
			if(cond == 0)
				htmltext = "parina_q0404_04.htm";
			else if(cond > 0 && st.ownItemCount(FLAME_EARING_ID) < 1 | st.ownItemCount(WIND_BANGEL_ID) < 1 | st.ownItemCount(WATER_NECKLACE_ID) < 1 | st.ownItemCount(EARTH_RING_ID) < 1)
				htmltext = "parina_q0404_05.htm";
			else if(cond > 0 && st.ownItemCount(FLAME_EARING_ID) > 0 && st.ownItemCount(WIND_BANGEL_ID) > 0 && st.ownItemCount(WATER_NECKLACE_ID) > 0 && st.ownItemCount(EARTH_RING_ID) > 0)
			{
				htmltext = "parina_q0404_06.htm";
				st.takeItems(FLAME_EARING_ID, st.ownItemCount(FLAME_EARING_ID));
				st.takeItems(WIND_BANGEL_ID, st.ownItemCount(WIND_BANGEL_ID));
				st.takeItems(WATER_NECKLACE_ID, st.ownItemCount(WATER_NECKLACE_ID));
				st.takeItems(EARTH_RING_ID, st.ownItemCount(EARTH_RING_ID));
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					if(st.ownItemCount(BEAD_OF_SEASON_ID) < 1)
						st.giveItems(BEAD_OF_SEASON_ID, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(295862, 18274);
						//FIXME [G1ta0] дать адены, только если первый чар на акке
						st.giveItems(ADENA_ID, 81900);
					}
				}
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
			}
		}
		else if(npcId == FLAME_SALAMANDER)
		{
			if(cond > 0 && st.ownItemCount(MAP_OF_LUSTER_ID) < 1 && st.ownItemCount(FLAME_EARING_ID) < 1)
			{
				st.giveItems(MAP_OF_LUSTER_ID, 1);
				htmltext = "flame_salamander_q0404_01.htm";
				st.setCond(2);
			}
			else if(cond > 0 && st.ownItemCount(MAP_OF_LUSTER_ID) > 0 && st.ownItemCount(KEY_OF_FLAME_ID) < 1)
				htmltext = "flame_salamander_q0404_02.htm";
			else if(cond == 3 && st.ownItemCount(MAP_OF_LUSTER_ID) > 0 && st.ownItemCount(KEY_OF_FLAME_ID) > 0)
			{
				st.takeItems(KEY_OF_FLAME_ID, -1);
				st.takeItems(MAP_OF_LUSTER_ID, -1);
				if(st.ownItemCount(FLAME_EARING_ID) < 1)
					st.giveItems(FLAME_EARING_ID, 1);
				htmltext = "flame_salamander_q0404_03.htm";
				st.setCond(4);
			}
			else if(cond > 0 && st.ownItemCount(FLAME_EARING_ID) > 0)
				htmltext = "flame_salamander_q0404_04.htm";
		}
		else if(npcId == WIND_SYLPH)
		{
			if(cond == 4 && st.ownItemCount(FLAME_EARING_ID) > 0 && st.ownItemCount(BROKEN_BRONZE_MIRROR_ID) < 1 && st.ownItemCount(WIND_BANGEL_ID) < 1)
			{
				st.giveItems(BROKEN_BRONZE_MIRROR_ID, 1);
				htmltext = "wind_sylph_q0404_01.htm";
				st.setCond(5);
			}
			else if(cond > 0 && st.ownItemCount(BROKEN_BRONZE_MIRROR_ID) > 0 && st.ownItemCount(WIND_FEATHER_ID) < 1)
				htmltext = "wind_sylph_q0404_02.htm";
			else if(cond > 0 && st.ownItemCount(BROKEN_BRONZE_MIRROR_ID) > 0 && st.ownItemCount(WIND_FEATHER_ID) > 0)
			{
				st.takeItems(WIND_FEATHER_ID, st.ownItemCount(WIND_FEATHER_ID));
				st.takeItems(BROKEN_BRONZE_MIRROR_ID, st.ownItemCount(BROKEN_BRONZE_MIRROR_ID));
				if(st.ownItemCount(WIND_BANGEL_ID) < 1)
					st.giveItems(WIND_BANGEL_ID, 1);
				htmltext = "wind_sylph_q0404_03.htm";
				st.setCond(7);
			}
			else if(cond > 0 && st.ownItemCount(WIND_BANGEL_ID) > 0)
				htmltext = "wind_sylph_q0404_04.htm";
		}
		else if(npcId == WASTELAND_LIZARDMAN)
		{
			if(cond > 0 && st.ownItemCount(BROKEN_BRONZE_MIRROR_ID) > 0 && st.ownItemCount(WIND_FEATHER_ID) < 1)
				htmltext = "lizardman_of_wasteland_q0404_01.htm";
			else if(cond > 0 && st.ownItemCount(BROKEN_BRONZE_MIRROR_ID) > 0 && st.ownItemCount(WIND_FEATHER_ID) > 0)
				htmltext = "lizardman_of_wasteland_q0404_04.htm";
		}
		else if(npcId == WATER_UNDINE)
		{
			if(cond == 7 && st.ownItemCount(WIND_BANGEL_ID) > 0 && st.ownItemCount(RAMAS_DIARY_ID) < 1 && st.ownItemCount(WATER_NECKLACE_ID) < 1)
			{
				st.giveItems(RAMAS_DIARY_ID, 1);
				htmltext = "water_undine_q0404_01.htm";
				st.setCond(8);
			}
			else if(cond > 0 && st.ownItemCount(RAMAS_DIARY_ID) > 0 && st.ownItemCount(SPARKLE_PEBBLE_ID) < 2)
				htmltext = "water_undine_q0404_02.htm";
			else if(cond == 9 && st.ownItemCount(RAMAS_DIARY_ID) > 0 && st.ownItemCount(SPARKLE_PEBBLE_ID) > 1)
			{
				st.takeItems(SPARKLE_PEBBLE_ID, -1);
				st.takeItems(RAMAS_DIARY_ID, -1);
				if(st.ownItemCount(WATER_NECKLACE_ID) < 1)
					st.giveItems(WATER_NECKLACE_ID, 1);
				htmltext = "water_undine_q0404_03.htm";
				st.setCond(10);
			}
			else if(cond > 0 && st.ownItemCount(WATER_NECKLACE_ID) > 0)
				htmltext = "water_undine_q0404_04.htm";
		}
		else if(npcId == EARTH_SNAKE)
			if(cond > 0 && st.ownItemCount(WATER_NECKLACE_ID) > 0 && st.ownItemCount(RUST_GOLD_COIN_ID) < 1 && st.ownItemCount(EARTH_RING_ID) < 1)
			{
				st.giveItems(RUST_GOLD_COIN_ID, 1);
				htmltext = "earth_snake_q0404_01.htm";
				st.setCond(11);
			}
			else if(cond > 0 && st.ownItemCount(RUST_GOLD_COIN_ID) > 0 && st.ownItemCount(RED_SOIL_ID) < 1)
				htmltext = "earth_snake_q0404_02.htm";
			else if(cond == 12 && st.ownItemCount(RUST_GOLD_COIN_ID) > 0 && st.ownItemCount(RED_SOIL_ID) > 0)
			{
				st.takeItems(RED_SOIL_ID, st.ownItemCount(RED_SOIL_ID));
				st.takeItems(RUST_GOLD_COIN_ID, st.ownItemCount(RUST_GOLD_COIN_ID));
				if(st.ownItemCount(EARTH_RING_ID) < 1)
					st.giveItems(EARTH_RING_ID, 1);
				htmltext = "earth_snake_q0404_04.htm";
				st.setCond(13);
			}
			else if(cond > 0 && st.ownItemCount(EARTH_RING_ID) > 0)
				htmltext = "earth_snake_q0404_04.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == RATMAN_WARRIOR)
		{
			if(cond == 2)
			{
				st.giveItems(KEY_OF_FLAME_ID, 1);
				st.soundEffect(SOUND_MIDDLE);
				st.setCond(3);
			}
		}
		else if(npcId == WATER_SEER)
		{
			if(cond == 8 && st.ownItemCount(SPARKLE_PEBBLE_ID) < 2)
			{
				st.giveItems(SPARKLE_PEBBLE_ID, 1);
				if(st.ownItemCount(SPARKLE_PEBBLE_ID) == 2)
				{
					st.soundEffect(SOUND_MIDDLE);
					st.setCond(9);
				}
				else
					st.soundEffect(SOUND_ITEMGET);
			}
		}
		else if(npcId == RED_BEAR)
			if(cond == 11)
			{
				st.giveItems(RED_SOIL_ID, 1);
				st.soundEffect(SOUND_MIDDLE);
				st.setCond(12);
			}
		return null;
	}
}
