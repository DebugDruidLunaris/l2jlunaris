package quests;

import java.util.HashMap;
import java.util.Map;

import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.listener.actor.OnCurrentHpDamageListener;
import jts.gameserver.listener.actor.player.OnPlayerEnterListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.TutorialCloseHtml;
import jts.gameserver.scripts.ScriptFile;

public class _255_Tutorial extends Quest implements ScriptFile, OnPlayerEnterListener
{
	// table for Quest Timer ( Ex == -2 ) [raceId, voice, html]
	public final String[][] QTEXMTWO = {
			{ "0", "tutorial_voice_001a", "tutorial_human_fighter001.htm" },
			{ "10", "tutorial_voice_001b", "tutorial_human_mage001.htm" },
			{ "18", "tutorial_voice_001c", "tutorial_elven_fighter001.htm" },
			{ "25", "tutorial_voice_001d", "tutorial_elven_mage001.htm" },
			{ "31", "tutorial_voice_001e", "tutorial_delf_fighter001.htm" },
			{ "38", "tutorial_voice_001f", "tutorial_delf_mage001.htm" },
			{ "44", "tutorial_voice_001g", "tutorial_orc_fighter001.htm" },
			{ "49", "tutorial_voice_001h", "tutorial_orc_mage001.htm" },
			{ "53", "tutorial_voice_001i", "tutorial_dwarven_fighter001.htm" },
			{ "123", "tutorial_voice_001k", "tutorial_kamael_male001.htm" },
			{ "124", "tutorial_voice_001j", "tutorial_kamael_female001.htm" } };

	// table for Client Event Enable (8) [raceId, html, x, y, z]
	public final String[][] CEEa = {
			{ "0", "tutorial_human_fighter007.htm", "-71424", "258336", "-3109" },
			{ "10", "tutorial_human_mage007.htm", "-91036", "248044", "-3568" },
			{ "18", "tutorial_elf007.htm", "46112", "41200", "-3504" },
			{ "25", "tutorial_elf007.htm", "46112", "41200", "-3504" },
			{ "31", "tutorial_delf007.htm", "28384", "11056", "-4233" },
			{ "38", "tutorial_delf007.htm", "28384", "11056", "-4233" },
			{ "44", "tutorial_orc007.htm", "-56736", "-113680", "-672" },
			{ "49", "tutorial_orc007.htm", "-56736", "-113680", "-672" },
			{ "53", "tutorial_dwarven_fighter007.htm", "108567", "-173994", "-406" },
			{ "123", "tutorial_kamael007.htm", "-125872", "38016", "1251" },
			{ "124", "tutorial_kamael007.htm", "-125872", "38016", "1251" } };

	// table for Question Mark Clicked (9 & 11) learning skills [raceId, html, x, y, z]
	public final String[][] QMCa = {
			{ "0", "tutorial_fighter017.htm", "-83165", "242711", "-3720" },
			{ "10", "tutorial_mage017.htm", "-85247", "244718", "-3720" },
			{ "18", "tutorial_fighter017.htm", "45610", "52206", "-2792" },
			{ "25", "tutorial_mage017.htm", "45610", "52206", "-2792" },
			{ "31", "tutorial_fighter017.htm", "10344", "14445", "-4242" },
			{ "38", "tutorial_mage017.htm", "10344", "14445", "-4242" },
			{ "44", "tutorial_fighter017.htm", "-46324", "-114384", "-200" },
			{ "49", "tutorial_fighter017.htm", "-46305", "-112763", "-200" },
			{ "53", "tutorial_fighter017.htm", "115447", "-182672", "-1440" },
			{ "123", "tutorial_fighter017.htm", "-118132", "42788", "723" },
			{ "124", "tutorial_fighter017.htm", "-118132", "42788", "723" } };

	// table for Question Mark Clicked (24) newbie lvl [raceId, html]
	public final Map<Integer, String> QMCb = new HashMap<Integer, String>();

	// table for Question Mark Clicked (35) 1st class transfer [raceId, html]
	public final Map<Integer, String> QMCc = new HashMap<Integer, String>();

	// table for Tutorial Close Link (26) 2nd class transfer [raceId, html]
	public final Map<Integer, String> TCLa = new HashMap<Integer, String>();

	// table for Tutorial Close Link (23) 2nd class transfer [raceId, html]
	public final Map<Integer, String> TCLb = new HashMap<Integer, String>();

	// table for Tutorial Close Link (24) 2nd class transfer [raceId, html]
	public final Map<Integer, String> TCLc = new HashMap<Integer, String>();

	private static TutorialShowListener _tutorialShowListener;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _255_Tutorial()
	{
		super(false);

		CharListenerList.addGlobal(this);

		_tutorialShowListener = new TutorialShowListener();

		QMCb.put(0, "tutorial_human009.htm");
		QMCb.put(10, "tutorial_human009.htm");
		QMCb.put(18, "tutorial_elf009.htm");
		QMCb.put(25, "tutorial_elf009.htm");
		QMCb.put(31, "tutorial_delf009.htm");
		QMCb.put(38, "tutorial_delf009.htm");
		QMCb.put(44, "tutorial_orc009.htm");
		QMCb.put(49, "tutorial_orc009.htm");
		QMCb.put(53, "tutorial_dwarven009.htm");
		QMCb.put(123, "tutorial_kamael009.htm");
		QMCb.put(124, "tutorial_kamael009.htm");

		QMCc.put(0, "tutorial_21.htm");
		QMCc.put(10, "tutorial_21a.htm");
		QMCc.put(18, "tutorial_21b.htm");
		QMCc.put(25, "tutorial_21c.htm");
		QMCc.put(31, "tutorial_21g.htm");
		QMCc.put(38, "tutorial_21h.htm");
		QMCc.put(44, "tutorial_21d.htm");
		QMCc.put(49, "tutorial_21e.htm");
		QMCc.put(53, "tutorial_21f.htm");

		TCLa.put(1, "tutorial_22w.htm");
		TCLa.put(4, "tutorial_22.htm");
		TCLa.put(7, "tutorial_22b.htm");
		TCLa.put(11, "tutorial_22c.htm");
		TCLa.put(15, "tutorial_22d.htm");
		TCLa.put(19, "tutorial_22e.htm");
		TCLa.put(22, "tutorial_22f.htm");
		TCLa.put(26, "tutorial_22g.htm");
		TCLa.put(29, "tutorial_22h.htm");
		TCLa.put(32, "tutorial_22n.htm");
		TCLa.put(35, "tutorial_22o.htm");
		TCLa.put(39, "tutorial_22p.htm");
		TCLa.put(42, "tutorial_22q.htm");
		TCLa.put(45, "tutorial_22i.htm");
		TCLa.put(47, "tutorial_22j.htm");
		TCLa.put(50, "tutorial_22k.htm");
		TCLa.put(54, "tutorial_22l.htm");
		TCLa.put(56, "tutorial_22m.htm");

		TCLb.put(4, "tutorial_22aa.htm");
		TCLb.put(7, "tutorial_22ba.htm");
		TCLb.put(11, "tutorial_22ca.htm");
		TCLb.put(15, "tutorial_22da.htm");
		TCLb.put(19, "tutorial_22ea.htm");
		TCLb.put(22, "tutorial_22fa.htm");
		TCLb.put(26, "tutorial_22ga.htm");
		TCLb.put(32, "tutorial_22na.htm");
		TCLb.put(35, "tutorial_22oa.htm");
		TCLb.put(39, "tutorial_22pa.htm");
		TCLb.put(50, "tutorial_22ka.htm");

		TCLc.put(4, "tutorial_22ab.htm");
		TCLc.put(7, "tutorial_22bb.htm");
		TCLc.put(11, "tutorial_22cb.htm");
		TCLc.put(15, "tutorial_22db.htm");
		TCLc.put(19, "tutorial_22eb.htm");
		TCLc.put(22, "tutorial_22fb.htm");
		TCLc.put(26, "tutorial_22gb.htm");
		TCLc.put(32, "tutorial_22nb.htm");
		TCLc.put(35, "tutorial_22ob.htm");
		TCLc.put(39, "tutorial_22pb.htm");
		TCLc.put(50, "tutorial_22kb.htm");
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		if(player == null)
			return null;

		String html = "";

		int classId = player.getClassId().getId();
		int Ex = st.getInt("Ex");

		// Вход в мир
		if(event.startsWith("UC"))
		{
			int level = player.getLevel();
			if(level < 6 && st.getInt("onlyone") == 0)
			{
				int uc = st.getInt("ucMemo");
				if(uc == 0)
				{
					st.setMemoState("ucMemo", "0");
					st.startQuestTimer("QT", 10000);
					st.setMemoState("Ex", "-2");
				}
				else if(uc == 1)
				{
					st.showQuestionMark(1);
					st.playTutorialVoice("tutorial_voice_006");
					st.soundEffect(SOUND_TUTORIAL);
				}
				else if(uc == 2)
				{
					if(Ex == 2)
					{
						st.showQuestionMark(3);
						st.soundEffect(SOUND_TUTORIAL);
					}
					else if(st.ownItemCount(6353) > 0)
					{
						st.showQuestionMark(5);
						st.soundEffect(SOUND_TUTORIAL);
					}
				}
				else if(uc == 3)
				{
					st.showQuestionMark(12);
					st.soundEffect(SOUND_TUTORIAL);
					st.onTutorialClientEvent(0);
				}
			}
			else if(level == 18 && player.getQuestState("_10276_MutatedKaneusGludio") == null)
			{
				st.showQuestionMark(36);
				st.soundEffect(SOUND_TUTORIAL);
			}
			else if(level == 28 && player.getQuestState("_10277_MutatedKaneusDion") == null)
			{
				st.showQuestionMark(36);
				st.soundEffect(SOUND_TUTORIAL);
			}
			else if(level == 28 && player.getQuestState("_10278_MutatedKaneusHeine") == null)
			{
				st.showQuestionMark(36);
				st.soundEffect(SOUND_TUTORIAL);
			}
			else if(level == 28 && player.getQuestState("_10279_MutatedKaneusOren") == null)
			{
				st.showQuestionMark(36);
				st.soundEffect(SOUND_TUTORIAL);
			}
			else if(level == 28 && player.getQuestState("_10280_MutatedKaneusSchuttgart") == null)
			{
				st.showQuestionMark(36);
				st.soundEffect(SOUND_TUTORIAL);
			}
			else if(level == 28 && player.getQuestState("_10281_MutatedKaneusRune") == null)
			{
				st.showQuestionMark(36);
				st.soundEffect(SOUND_TUTORIAL);
			}
			else if(level == 79 && player.getQuestState("_192_SevenSignSeriesOfDoubt") == null)
			{
				st.showQuestionMark(36);
				st.soundEffect(SOUND_TUTORIAL);
			}
		}

		// Обработка таймера QT
		else if(event.startsWith("QT"))
		{
			if(Ex == -2)
			{
				String voice = "";
				for(String[] element : QTEXMTWO)
					if(classId == Integer.valueOf(element[0]))
					{
						voice = element[1];
						html = element[2];
					}
				st.playTutorialVoice(voice);
				st.setMemoState("Ex", "-3");
				st.cancelQuestTimer("QT");
				st.startQuestTimer("QT", 30000);
			}
			else if(Ex == -3)
			{
				st.playTutorialVoice("tutorial_voice_002");
				st.setMemoState("Ex", "0");
			}
			else if(Ex == -4)
			{
				st.playTutorialVoice("tutorial_voice_008");
				st.setMemoState("Ex", "-5");
			}
		}

		// Tutorial close
		else if(event.startsWith("TE"))
		{
			st.cancelQuestTimer("TE");
			int event_id = 0;
			if(!event.equalsIgnoreCase("TE"))
				event_id = Integer.valueOf(event.substring(2));
			if(event_id == 0)
				player.sendPacket(TutorialCloseHtml.STATIC);
			else if(event_id == 1)
			{
				player.sendPacket(TutorialCloseHtml.STATIC);
				st.playTutorialVoice("tutorial_voice_006");
				st.showQuestionMark(1);
				st.soundEffect(SOUND_TUTORIAL);
				st.startQuestTimer("QT", 30000);
				st.setMemoState("Ex", "-4");
			}
			else if(event_id == 2)
			{
				st.playTutorialVoice("tutorial_voice_003");
				html = "tutorial_02.htm";
				st.onTutorialClientEvent(1);
				st.setMemoState("Ex", "-5");
			}
			else if(event_id == 3)
			{
				html = "tutorial_03.htm";
				st.onTutorialClientEvent(2);
			}
			else if(event_id == 5)
			{
				html = "tutorial_05.htm";
				st.onTutorialClientEvent(8);
			}
			else if(event_id == 7)
			{
				html = "tutorial_100.htm";
				st.onTutorialClientEvent(0);
			}
			else if(event_id == 8)
			{
				html = "tutorial_101.htm";
				st.onTutorialClientEvent(0);
			}
			else if(event_id == 10)
			{
				html = "tutorial_103.htm";
				st.onTutorialClientEvent(0);
			}
			else if(event_id == 12)
				player.sendPacket(TutorialCloseHtml.STATIC);
			else if(event_id == 23 && TCLb.containsKey(classId))
				html = TCLb.get(classId);
			else if(event_id == 24 && TCLc.containsKey(classId))
				html = TCLc.get(classId);
			else if(event_id == 25)
				html = "tutorial_22cc.htm";
			else if(event_id == 26 && TCLa.containsKey(classId))
				html = TCLa.get(classId);
			else if(event_id == 27)
				html = "tutorial_29.htm";
			else if(event_id == 28)
				html = "tutorial_28.htm";
		}

		// Client Event
		else if(event.startsWith("CE"))
		{
			int event_id = Integer.valueOf(event.substring(2));
			if(event_id == 1 && player.getLevel() < 6)
			{
				st.playTutorialVoice("tutorial_voice_004");
				html = "tutorial_03.htm";
				st.soundEffect(SOUND_TUTORIAL);
				st.onTutorialClientEvent(2);
			}
			else if(event_id == 2 && player.getLevel() < 6)
			{
				st.playTutorialVoice("tutorial_voice_005");
				html = "tutorial_05.htm";
				st.soundEffect(SOUND_TUTORIAL);
				st.onTutorialClientEvent(8);
			}
			else if(event_id == 8 && player.getLevel() < 6)
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for(String[] element : CEEa)
					if(classId == Integer.valueOf(element[0]))
					{
						html = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				if(x != 0)
				{
					st.soundEffect(SOUND_TUTORIAL);
					st.addRadar(x, y, z);
					st.playTutorialVoice("tutorial_voice_007");
					st.setMemoState("ucMemo", "1");
					st.setMemoState("Ex", "-5");
				}
			}
			else if(event_id == 30 && player.getLevel() < 10 && st.getInt("Die") == 0)
			{
				st.playTutorialVoice("tutorial_voice_016");
				st.soundEffect(SOUND_TUTORIAL);
				st.setMemoState("Die", "1");
				st.showQuestionMark(8);
				st.onTutorialClientEvent(0);
			}
			else if(event_id == 800000 && player.getLevel() < 6 && st.getInt("sit") == 0)
			{
				st.playTutorialVoice("tutorial_voice_018");
				st.soundEffect(SOUND_TUTORIAL);
				st.setMemoState("sit", "1");
				st.onTutorialClientEvent(0);
				html = "tutorial_21z.htm";
			}
			else if(event_id == 40)
			{
				if(player.getLevel() == 5)
					if(st.getInt("lvl") < 5 && !player.getClassId().isMage() || classId == 49)
					{
						st.playTutorialVoice("tutorial_voice_014");
						st.showQuestionMark(9);
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "5");
					}
				if(player.getLevel() == 6)
				{
					if(st.getInt("lvl") < 6 && player.getClassId().level() == 0)
					{
						st.playTutorialVoice("tutorial_voice_020");
						st.soundEffect(SOUND_TUTORIAL);
						st.showQuestionMark(24);
						st.setMemoState("lvl", "6");
					}
				}
				else if(player.getLevel() == 7)
				{
					if(st.getInt("lvl") < 7 && player.getClassId().isMage() && classId != 49 && player.getClassId().level() == 0)
					{
						st.playTutorialVoice("tutorial_voice_019");
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "7");
						st.showQuestionMark(11);
					}
				}
				else if(player.getLevel() == 15)
				{
					if(st.getInt("lvl") < 15)
					{
						// st.playTutorialVoice("tutorial_voice_???");
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "15");
						st.showQuestionMark(33);
					}
				}
				else if(player.getLevel() == 18)
				{
					if(st.getInt("lvl") < 18)
					{
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "18");
						st.showQuestionMark(36);
					}
				}
				else if(player.getLevel() == 19)
				{
					if(st.getInt("lvl") < 19 && player.getRace() != Race.kamael && player.getClassId().level() == 0)
						switch(classId)
						{
							case 0:
							case 10:
							case 18:
							case 25:
							case 31:
							case 38:
							case 44:
							case 49:
							case 52:
								// st.playTutorialVoice("tutorial_voice_???");
								st.soundEffect(SOUND_TUTORIAL);
								st.setMemoState("lvl", "19");
								st.showQuestionMark(35);
						}
				}
				else if(player.getLevel() == 28)
				{
					if(st.getInt("lvl") < 28)
					{
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "28");
						st.showQuestionMark(36);
					}
				}
				else if(player.getLevel() == 35)
				{
					if(st.getInt("lvl") < 35 && player.getRace() != Race.kamael && player.getClassId().level() == 1)
						switch(classId)
						{
							case 1:
							case 4:
							case 7:
							case 11:
							case 15:
							case 19:
							case 22:
							case 26:
							case 29:
							case 32:
							case 35:
							case 39:
							case 42:
							case 45:
							case 47:
							case 50:
							case 54:
							case 56:
								// st.playTutorialVoice("tutorial_voice_???");
								st.soundEffect(SOUND_TUTORIAL);
								st.setMemoState("lvl", "35");
								st.showQuestionMark(34);
						}
				}
				else if(player.getLevel() == 38)
				{
					if(st.getInt("lvl") < 38)
					{
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "38");
						st.showQuestionMark(36);
					}
				}
				else if(player.getLevel() == 48)
				{
					if(st.getInt("lvl") < 48)
					{
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "48");
						st.showQuestionMark(36);
					}
				}
				else if(player.getLevel() == 58)
				{
					if(st.getInt("lvl") < 58)
					{
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "58");
						st.showQuestionMark(36);
					}
				}
				else if(player.getLevel() == 68)
				{
					if(st.getInt("lvl") < 68)
					{
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "68");
						st.showQuestionMark(36);
					}
				}
				else if(player.getLevel() == 79)
					if(st.getInt("lvl") < 79)
					{
						st.soundEffect(SOUND_TUTORIAL);
						st.setMemoState("lvl", "79");
						st.showQuestionMark(79);
					}
			}
			else if(event_id == 45 && player.getLevel() < 10 && st.getInt("HP") == 0)
			{
				st.playTutorialVoice("tutorial_voice_017");
				st.soundEffect(SOUND_TUTORIAL);
				st.setMemoState("HP", "1");
				st.showQuestionMark(10);
				st.onTutorialClientEvent(800000);
			}
			else if(event_id == 57 && player.getLevel() < 6 && st.getInt("Adena") == 0)
			{
				st.playTutorialVoice("tutorial_voice_012");
				st.soundEffect(SOUND_TUTORIAL);
				st.setMemoState("Adena", "1");
				st.showQuestionMark(23);
			}
			else if(event_id == 6353 && player.getLevel() < 6 && st.getInt("Gemstone") == 0)
			{
				st.playTutorialVoice("tutorial_voice_013");
				st.soundEffect(SOUND_TUTORIAL);
				st.setMemoState("Gemstone", "1");
				st.showQuestionMark(5);
			}
			// TODO оно нужно?
			else if(event_id == 1048576 && player.getLevel() < 6)
			{
				st.showQuestionMark(5);
				st.playTutorialVoice("tutorial_voice_013");
				st.soundEffect(SOUND_TUTORIAL);
			}
		}

		// Question mark clicked
		else if(event.startsWith("QM"))
		{
			int MarkId = Integer.valueOf(event.substring(2));
			if(MarkId == 1)
			{
				st.playTutorialVoice("tutorial_voice_007");
				st.setMemoState("Ex", "-5");
				int x = 0;
				int y = 0;
				int z = 0;
				for(String[] element : CEEa)
					if(classId == Integer.valueOf(element[0]))
					{
						html = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				st.addRadar(x, y, z);
			}
			else if(MarkId == 3)
			{
				html = "tutorial_09.htm";
				// TODO оно нужно?
				st.onTutorialClientEvent(1048576);
			}
			else if(MarkId == 5)
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for(String[] element : CEEa)
					if(classId == Integer.valueOf(element[0]))
					{
						html = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				st.addRadar(x, y, z);
				html = "tutorial_11.htm";
			}
			else if(MarkId == 7)
			{
				html = "tutorial_15.htm";
				st.setMemoState("ucMemo", "3");
			}
			else if(MarkId == 8)
				html = "tutorial_18.htm";
			else if(MarkId == 9)
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for(String[] element : QMCa)
					if(classId == Integer.valueOf(element[0]))
					{
						html = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				if(x != 0)
					st.addRadar(x, y, z);
			}
			else if(MarkId == 10)
				html = "tutorial_19.htm";
			else if(MarkId == 11)
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for(String[] element : QMCa)
					if(classId == Integer.valueOf(element[0]))
					{
						html = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				if(x != 0)
					st.addRadar(x, y, z);
			}
			else if(MarkId == 12)
			{
				html = "tutorial_15.htm";
				st.setMemoState("ucMemo", "4");
			}
			else if(MarkId == 12)
				html = "tutorial_30.htm";
			else if(MarkId == 23)
				html = "tutorial_24.htm";
			else if(MarkId == 24 && QMCb.containsKey(classId))
				html = QMCb.get(classId);
			else if(MarkId == 26)
			{
				if(player.getClassId().isMage() && classId != 49)
					html = "tutorial_newbie004b.htm";
				else
					html = "tutorial_newbie004a.htm";
			}
			else if(MarkId == 33)
				html = "tutorial_27.htm";
			else if(MarkId == 34)
				html = "tutorial_28.htm";
			else if(MarkId == 35 && QMCc.containsKey(classId))
				html = QMCc.get(classId);
			else if(MarkId == 36)
			{
				int lvl = player.getLevel();
				if(lvl == 18)
					html = "tutorial_kama_18.htm";
				else if(lvl == 28)
					html = "tutorial_kama_28.htm";
				else if(lvl == 38)
					html = "tutorial_kama_38.htm";
				else if(lvl == 48)
					html = "tutorial_kama_48.htm";
				else if(lvl == 58)
					html = "tutorial_kama_58.htm";
				else if(lvl == 68)
					html = "tutorial_kama_68.htm";
				else if(lvl == 79)
					html = "tutorial_epic_quest.htm";
			}
		}

		if(html.isEmpty())
			return null;
		st.showTutorialHTML(html);
		return null;
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(player.getLevel() < 6)
			player.addListener(_tutorialShowListener);
	}

	public class TutorialShowListener implements OnCurrentHpDamageListener
	{
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			Player player = actor.getPlayer();
			if(player.getCurrentHpPercents() < 25)
			{
				player.removeListener(_tutorialShowListener);
				Quest q = QuestManager.getQuest(255);
				if(q != null)
					player.processQuestEvent(q.getName(), "CE45", null);
			}
			else if(player.getLevel() > 5)
				player.removeListener(_tutorialShowListener);
		}
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}
}