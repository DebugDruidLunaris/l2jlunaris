package events.TheFallHarvest;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.reward.RewardData;
import jts.gameserver.model.reward.RewardItem;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.Log;
import npc.model.SquashInstance;

public class SquashAI extends Fighter
{
	public class PolimorphTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			SquashInstance actor = getActor();
			if(actor == null)
				return;
			SimpleSpawner spawn = null;

			try
			{
				spawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(_npcId));
				spawn.setLoc(actor.getLoc());
				NpcInstance npc = spawn.doSpawn(true);
				npc.setAI(new SquashAI(npc));
				((SquashInstance) npc).setSpawner(actor.getSpawner());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			_timeToUnspawn = Long.MAX_VALUE;
			actor.deleteMe();
		}
	}

	protected static final RewardData[] _dropList = new RewardData[] 
	{ 
		new RewardData(1539, 1, 5, 15000), // Greater Healing Potion
		new RewardData(1374, 1, 3, 15000), // Greater Haste Potion

		new RewardData(4411, 1, 1, 5000), // Echo Crystal - Theme of Journey
		new RewardData(4412, 1, 1, 5000), // Echo Crystal - Theme of Battle
		new RewardData(4413, 1, 1, 5000), // Echo Crystal - Theme of Love
		new RewardData(4414, 1, 1, 5000), // Echo Crystal - Theme of Solitude
		new RewardData(4415, 1, 1, 5000), // Echo Crystal - Theme of the Feast
		new RewardData(4416, 1, 1, 5000), // Echo Crystal - Theme of Celebration
		new RewardData(4417, 1, 1, 5000), // Echo Crystal - Theme of Comedy
		new RewardData(5010, 1, 1, 5000), // Echo Crystal - Theme of Victory

		new RewardData(1458, 10, 30, 13846), // Crystal: D-Grade 1.3%
		new RewardData(1459, 10, 30, 3000), // Crystal: C-Grade  0.3%
		new RewardData(1460, 10, 30, 1000), // Crystal: B-Grade  0.1%
		new RewardData(1461, 10, 30, 600), // Crystal: A-Grade   0.06%
		new RewardData(1462, 10, 30, 360), // Crystal: S-Grade   0.036%

		new RewardData(4161, 1, 1, 5000), // Recipe: Blue Wolf Tunic
		new RewardData(4182, 1, 1, 5000), // Recipe: Great Sword
		new RewardData(4174, 1, 1, 5000), // Recipe:  Zubei's Boots
		new RewardData(4166, 1, 1, 5000), // Recipe: Doom Helmet

		new RewardData(8660, 1, 1, 1000), // Demon Horns        0.1%
		new RewardData(8661, 1, 1, 1000), // Mask of Spirits    0.1%
		new RewardData(4393, 1, 1, 300), // Calculator          0.03%
		new RewardData(7836, 1, 1, 200), // Santa's Hat         0.02%
		new RewardData(5590, 1, 1, 200), // Squeaking Shoes     0.02%
		new RewardData(7058, 1, 1, 50), // Chrono Darbuka       0.005%
		new RewardData(8350, 1, 1, 50), // Chrono Maracas       0.005%
		new RewardData(5133, 1, 1, 50), // Chrono Unitus        0.005%
		new RewardData(5817, 1, 1, 50), // Chrono Campana       0.005%
		new RewardData(9140, 1, 1, 30), // Salvation Bow        0.003%

		// Призрачные аксессуары - шанс 0.01%
		new RewardData(9177, 1, 1, 100), // Teddy Bear Hat - Blessed Resurrection Effect
		new RewardData(9178, 1, 1, 100), // Piggy Hat - Blessed Resurrection Effect
		new RewardData(9179, 1, 1, 100), // Jester Hat - Blessed Resurrection Effect
		new RewardData(9180, 1, 1, 100), // Wizard's Hat - Blessed Resurrection Effect
		new RewardData(9181, 1, 1, 100), // Dapper Cap - Blessed Resurrection Effect
		new RewardData(9182, 1, 1, 100), // Romantic Chapeau - Blessed Resurrection Effect
		new RewardData(9183, 1, 1, 100), // Iron Circlet - Blessed Resurrection Effect
		new RewardData(9184, 1, 1, 100), // Teddy Bear Hat - Blessed Escape Effect
		new RewardData(9185, 1, 1, 100), // Piggy Hat - Blessed Escape Effect
		new RewardData(9186, 1, 1, 100), // Jester Hat - Blessed Escape Effect
		new RewardData(9187, 1, 1, 100), // Wizard's Hat - Blessed Escape Effect
		new RewardData(9188, 1, 1, 100), // Dapper Cap - Blessed Escape Effect
		new RewardData(9189, 1, 1, 100), // Romantic Chapeau - Blessed Escape Effect
		new RewardData(9190, 1, 1, 100), // Iron Circlet - Blessed Escape Effect
		new RewardData(9191, 1, 1, 100), // Teddy Bear Hat - Big Head
		new RewardData(9192, 1, 1, 100), // Piggy Hat - Big Head
		new RewardData(9193, 1, 1, 100), // Jester Hat - Big Head
		new RewardData(9194, 1, 1, 100), // Wizard Hat - Big Head
		new RewardData(9195, 1, 1, 100), // Dapper Hat - Big Head
		new RewardData(9196, 1, 1, 100), // Romantic Chapeau - Big Head
		new RewardData(9197, 1, 1, 100), // Iron Circlet - Big Head
		new RewardData(9198, 1, 1, 100), // Teddy Bear Hat - Firework
		new RewardData(9199, 1, 1, 100), // Piggy Hat - Firework
		new RewardData(9200, 1, 1, 100), // Jester Hat - Firework
		new RewardData(9201, 1, 1, 100), // Wizard's Hat - Firework
		new RewardData(9202, 1, 1, 100), // Dapper Hat - Firework
		new RewardData(9203, 1, 1, 100), // Romantic Chapeau - Firework
		new RewardData(9204, 1, 1, 100), // Iron Circlet - Firework

		new RewardData(9146, 1, 3, 5000), // Scroll of Guidance        0.5%
		new RewardData(9147, 1, 3, 5000), // Scroll of Death Whisper   0.5%
		new RewardData(9148, 1, 3, 5000), // Scroll of Focus           0.5%
		new RewardData(9149, 1, 3, 5000), // Scroll of Acumen          0.5%
		new RewardData(9150, 1, 3, 5000), // Scroll of Haste           0.5%
		new RewardData(9151, 1, 3, 5000), // Scroll of Agility         0.5%
		new RewardData(9152, 1, 3, 5000), // Scroll of Empower         0.5%
		new RewardData(9153, 1, 3, 5000), // Scroll of Might           0.5%
		new RewardData(9154, 1, 3, 5000), // Scroll of Wind Walk       0.5%
		new RewardData(9155, 1, 3, 5000), // Scroll of Shield          0.5%
		new RewardData(9156, 1, 3, 2000), // BSoE                      0.2%
		new RewardData(9157, 1, 3, 1000), // BRES                      0.1%

		new RewardData(955, 1, 1, 400), // EWD          0.04%
		new RewardData(956, 1, 1, 2000), // EAD         0.2%
		new RewardData(951, 1, 1, 300), // EWC          0.03%
		new RewardData(952, 1, 1, 1500), // EAC         0.15%
		new RewardData(947, 1, 1, 200), // EWB          0.02%
		new RewardData(948, 1, 1, 1000), // EAB         0.1%
		new RewardData(729, 1, 1, 100), // EWA          0.01%
		new RewardData(730, 1, 1, 500), // EAA          0.05%
		new RewardData(959, 1, 1, 50), // EWS           0.005%
		new RewardData(960, 1, 1, 300), // EAS          0.03%
	};

	public final static int Young_Squash = 12774;
	public final static int High_Quality_Squash = 12775;
	public final static int Low_Quality_Squash = 12776;
	public final static int Large_Young_Squash = 12777;
	public final static int High_Quality_Large_Squash = 12778;
	public final static int Low_Quality_Large_Squash = 12779;
	public final static int King_Squash = 13016;
	public final static int Emperor_Squash = 13017;

	public final static int Squash_Level_up = 4513;
	public final static int Squash_Poisoned = 4514;

	private static final String[] textOnSpawn = new String[] 
	{
		"scripts.events.TheFallHarvest.SquashAI.textOnSpawn.0",
		"scripts.events.TheFallHarvest.SquashAI.textOnSpawn.1",
		"scripts.events.TheFallHarvest.SquashAI.textOnSpawn.2" 
	};

	private static final String[] textOnAttack = new String[] 
	{
		"Кусает крыса-а-ТАТ... изменилось... тело...!",
		"Ха-ха, вырос! Абсолютно на все!",
		"Не для всех цель? Посмотрел все вытекать...",
		"Это что, тоже рассчитанный удар? Искать человека, который имеет силу!",
		"Не тратьте свое время!",
		"Ха, этот звук действительно приятно слышать?",
		"Я ем твою атаку, чтобы расти!",
		"Время, чтобы ударить снова! Приходите позже!",
		"Только полезная музыка может открыть большую тыкву... она не может быть открыта с оружием!" 
	};

	private static final String[] textTooFast = new String[] 
	{
		"хе-хе,видать хорошо ударил!",
		"йо йо? Ваше мастерство средненькое?",
		"Время, чтобы ударить снова! Приходите позже!",
		"Я ем твою атаку, чтобы расти!",
		"Приложим усилия... чтобы получить приз, как это, я шел...",
		"Что это за разновидность степенью хотите открыть мне? На самом деле потакает в фантазии!",
		"Хороший способ борьбы. Видимо муху тоже может преодолеть.",
		"Тратить свое время..." 
	};

	private static final String[] textSuccess0 = new String[] 
	{
		"Прекрасные молодые плоды тыквы начинают блестеть, когда приняты к молотя земле! Отныне будет иметь возможность расти здоровыми и сильными!",
		"Ой, не видела тебя долгое время",
		"Друг,я думала,ты только, как увидеть мою прекрасную внешность?",
		"Хорошо! Это что-то! Это нектар?",
		"Заправляй! Выпить 5 бутылок чтобы быть в состоянии вырасти в большую тыкву ох!" 
	};

	private static final String[] textFail0 = new String[] 
	{
		"Если я пью нектар, я могу расти быстрее!",
		"Приходите поливайте меня нектаром! Конечно, я могу превратить в большую тыкву!!!",
		"Принеси мне нектар, Тыквенный нектар!" 
	};

	private static final String[] textSuccess1 = new String[] 
	{
		"Желаю быть большой тыквой!",
		"Полностью стала зона отдыха! Действительно хороший!",
		"Догадайся я-зрелая личность или я гнилой?",
		"Нектар-это просто лучшее! Ха! Ха! Ха!" 
	};

	private static final String[] textFail1 = new String[] 
	{
		"ой! Случайно пропустила! Слишком быстро брызгает нектаром?",
		"Если я умру такой, вы только могли бы получить молодые тыквы...",
		"Культивировать немного быстрее! Хорошие речи становится большой тыквы, молодая тыква-это не есть хорошо!",
		"Приниси нектар, я могу быть больше!" 
	};

	private static final String[] textSuccess2 = new String[] 
	{
		"Молодой тыквы желающие! Имеет, как уже повзрослела?",
		"Уже вырос! Быстро поймал такси...",
		"Милосердие, это очень хорошо. Приходите снова, теперь я чувствую себя все более и более популярным" 
	};

	private static final String[] textFail2 = new String[] 
	{
		"Эй! Не было там! Вот! Вот! Не потому, что я не могу правильно ухаживать? Маленький!",
		"Ничего себе, остановки? Такой дерзкий",
		"Жаждущие нектар ох...",
		"Вы хотите большую тыкву? Но я как молодая тыква..." 
	};

	private static final String[] textSuccess3 = new String[] 
	{
		"Большая тыква желающие! Попросите, чтобы трезвый!",
		"это действительно вкусно! Не так ли?",
		"Культивирование меня просто покушать? Хорошо, это ваш повседневный... не давать манну на самоубийство!" 
	};

	private static final String[] textFail3 = new String[] 
	{
		"Правда воду вы добавляете? Какой аромат?",
		"Мастер, спаси мое... у меня нет нектара аромат, я должен умереть..." 
	};

	private static final String[] textSuccess4 = new String[] 
	{
		"очень хорошо, делает очень хорошо! Знал, что следующий шаг должны сделать?",
		"Если поймаешь меня, я даю вам 10 миллионов адены!!! Согласны?" 
	};

	private static final String[] textFail4 = new String[] { "Жаждущие нектар ох...", "Если я пью нектар, я могу расти быстрее!" };

	private int _npcId;
	private int _nectar;
	private int _tryCount;
	private long _lastNectarUse;
	private long _timeToUnspawn;

	private ScheduledFuture<?> _polimorphTask;

	private static int NECTAR_REUSE = 3000;

	public SquashAI(NpcInstance actor)
	{
		super(actor);
		_npcId = getActor().getNpcId();
		Functions.npcSayCustomMessage(getActor(), textOnSpawn[Rnd.get(textOnSpawn.length)]);
		_timeToUnspawn = System.currentTimeMillis() + 120000;
	}

	@Override
	protected boolean thinkActive()
	{
		if(System.currentTimeMillis() > _timeToUnspawn)
		{
			_timeToUnspawn = Long.MAX_VALUE;
			if(_polimorphTask != null)
			{
				_polimorphTask.cancel(false);
				_polimorphTask = null;
			}

			SquashInstance actor = getActor();
			if(actor != null)
				actor.deleteMe();
		}

		return false;
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		SquashInstance actor = getActor();
		if(actor == null || skill.getId() != 2005)
			return;

		switch(_tryCount)
		{
			case 0:
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess0[Rnd.get(textSuccess0.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail0[Rnd.get(textFail0.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 1:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess1[Rnd.get(textSuccess1.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail1[Rnd.get(textFail1.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 2:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess2[Rnd.get(textSuccess2.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail2[Rnd.get(textFail2.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 3:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess3[Rnd.get(textSuccess3.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail3[Rnd.get(textFail3.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 4:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess4[Rnd.get(textSuccess4.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail4[Rnd.get(textFail4.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				if(_npcId == Young_Squash)
				{
					if(_nectar < 3)
						_npcId = Low_Quality_Squash;
					else if(_nectar == 5)
						_npcId = King_Squash;
					else
						_npcId = High_Quality_Squash;
				}
				else if(_npcId == Large_Young_Squash)
					if(_nectar < 3)
						_npcId = Low_Quality_Large_Squash;
					else if(_nectar == 5)
						_npcId = Emperor_Squash;
					else
						_npcId = High_Quality_Large_Squash;

				_polimorphTask = ThreadPoolManager.getInstance().schedule(new PolimorphTask(), NECTAR_REUSE);
				break;
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		SquashInstance actor = getActor();
		if(actor != null && Rnd.chance(5))
			Functions.npcSay(actor, textOnAttack[Rnd.get(textOnAttack.length)]);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_tryCount = -1;
		SquashInstance actor = getActor();
		if(actor == null)
			return;

		double dropMod = 1.5;

		switch(_npcId)
		{
			case Low_Quality_Squash:
				dropMod *= 1;
				Functions.npcSay(actor, "Тыква открылась!!!");
				Functions.npcSay(actor, "йа йо! открылась! Хороших вещей, много...");
				break;
			case High_Quality_Squash:
				dropMod *= 2;
				Functions.npcSay(actor, "Тыква открылась!!!");
				Functions.npcSay(actor, "йа йо! открылась! Хороших вещей, много...");
				break;
			case King_Squash:
				dropMod *= 4;
				Functions.npcSay(actor, "Тыква открылась!!!");
				Functions.npcSay(actor, "йа йо! открылась! Хороших вещей, много...");
				break;
			case Low_Quality_Large_Squash:
				dropMod *= 12.5;
				Functions.npcSay(actor, "Тыква открылась!!!");
				Functions.npcSay(actor, "йа йо! открылась! Хороших вещей, много...");
				break;
			case High_Quality_Large_Squash:
				dropMod *= 25;
				Functions.npcSay(actor, "Тыква открылась!!!");
				Functions.npcSay(actor, "йа йо! открылась! Хороших вещей, много...");
				break;
			case Emperor_Squash:
				dropMod *= 50;
				Functions.npcSay(actor, "Тыква открылась!!!");
				Functions.npcSay(actor, "йа йо! открылась! Хороших вещей, много...");
				break;
			default:
				dropMod *= 0;
				Functions.npcSay(actor, "Ой, если бы я умер такой, вы могли бы не получили ничего!");
				Functions.npcSay(actor, "Известие о моей смерти не должны выкладывать, ох!");
				break;
		}

		super.onEvtDead(actor);

		if(dropMod > 0)
		{
			if(_polimorphTask != null)
			{
				_polimorphTask.cancel(false);
				_polimorphTask = null;
				Log.add("TheFallHarvest :: Player " + actor.getSpawner().getName() + " tried to use cheat (SquashAI clone): killed " + actor + " after polymorfing started", "illegal-actions");
				return; // при таких вариантах ничего не даем
			}

			for(RewardData d : _dropList)
			{
				List<RewardItem> itd = d.roll(null, dropMod);
				for(RewardItem i : itd)
					actor.dropItem(actor.getSpawner(), i.itemId, i.count);
			}
		}
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	public SquashInstance getActor()
	{
		return (SquashInstance) super.getActor();
	}
}