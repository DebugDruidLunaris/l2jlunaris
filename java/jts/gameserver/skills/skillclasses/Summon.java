package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.ThreadPoolManager;
import jts.gameserver.cache.Msg;
import jts.gameserver.dao.EffectsDAO;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObjectTasks;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.instances.MerchantInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.SummonInstance;
import jts.gameserver.model.instances.TrapInstance;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.skills.SkillTargetType;
import jts.gameserver.stats.Stats;
import jts.gameserver.stats.funcs.FuncAdd;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.StatsSet;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.NpcUtils;

public class Summon extends Skill
{
	private final SummonType _summonType;

	private final double _expPenalty;
	private final int _itemConsumeIdInTime;
	private final int _itemConsumeCountInTime;
	private final int _itemConsumeDelay;
	private final int _lifeTime;
	private final int _minRadius;

	private static enum SummonType
	{
		PET,
		SIEGE_SUMMON,
		AGATHION,
		TRAP,
		MERCHANT,
		NPC
	}

	public Summon(StatsSet set)
	{
		super(set);

		_summonType = Enum.valueOf(SummonType.class, set.getString("summonType", "PET").toUpperCase());
		_expPenalty = set.getDouble("expPenalty", 0.f);
		_itemConsumeIdInTime = set.getInteger("itemConsumeIdInTime", 0);
		_itemConsumeCountInTime = set.getInteger("itemConsumeCountInTime", 0);
		_itemConsumeDelay = set.getInteger("itemConsumeDelay", 240) * 1000;
		_lifeTime = set.getInteger("lifeTime", 1200) * 1000;
		_minRadius = set.getInteger("minRadius", 0);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = activeChar.getPlayer();
		if(player == null)
			return false;

		if(player.isProcessingRequest())
		{
			player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}

		switch(_summonType)
		{
			case TRAP:
				if(player.isInZonePeace())
				{
					activeChar.sendPacket(Msg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
					return false;
				}
				break;
			case PET:
			case SIEGE_SUMMON:
				if(player.getPet() != null || player.isMounted())
				{
					player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
					return false;
				}
				break;
			case AGATHION:
				if(player.getAgathionId() > 0 && _npcId != 0)
				{
					player.sendPacket(SystemMsg.AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED);
					return false;
				}
			case NPC:
				if (_minRadius > 0)
					for (NpcInstance npc : World.getAroundNpc(player, _minRadius, 200))
						if (npc != null && npc.getNpcId() == getNpcId())
						{
							player.sendPacket(new SystemMessage(SystemMsg.SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN).addName(npc));
							return false;
						}
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		Player activeChar = caster.getPlayer();

		switch(_summonType)
		{
			case AGATHION:
				activeChar.setAgathion(getNpcId());
				break;
			case TRAP:
				Skill trapSkill = getFirstAddedSkill();

				if(activeChar.getTrapsCount() >= 5)
					activeChar.destroyFirstTrap();
				TrapInstance trap = new TrapInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(getNpcId()), activeChar, trapSkill);
				activeChar.addTrap(trap);
				trap.spawnMe();
				break;
			case PET:
			case SIEGE_SUMMON:
				Location loc = null;
				if(_targetType == SkillTargetType.TARGET_CORPSE)
					for(Creature target : targets)
						if(target != null && target.isDead())
						{
							activeChar.getAI().setAttackTarget(null);
							loc = target.getLoc();
							if(target.isNpc())
								((NpcInstance) target).endDecayTask();
							else if(target.isSummon())
								((SummonInstance) target).endDecayTask();
							else
								return;
						}

				if(activeChar.getPet() != null || activeChar.isMounted())
					return;

				NpcTemplate summonTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				SummonInstance summon = new SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay, this);
				activeChar.setPet(summon);

				summon.setTitle(activeChar.getName());
				summon.setExpPenalty(_expPenalty);
				summon.setExp(Experience.LEVEL[Math.min(summon.getLevel(), Experience.LEVEL.length - 1)]);
				summon.setHeading(activeChar.getHeading());
				summon.setReflection(activeChar.getReflection());
				summon.spawnMe(loc == null ? Location.findAroundPosition(activeChar, 50, 70) : loc);
				summon.setRunning();
				summon.setFollowMode(true);

				if(summon.getSkillLevel(4140) > 0)
					summon.altUseSkill(SkillTable.getInstance().getInfo(4140, summon.getSkillLevel(4140)), activeChar);

				if(summon.getName().equalsIgnoreCase("Shadow"))
					summon.addStatFunc(new FuncAdd(Stats.ABSORB_DAMAGE_PERCENT, 0x40, this, 15));

				EffectsDAO.getInstance().restoreEffects(summon);
				if(activeChar.isInOlympiadMode())
					summon.getEffectList().stopAllEffects();

				summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp(), false);

				if(_summonType == SummonType.SIEGE_SUMMON)
				{
					SiegeEvent siegeEvent = activeChar.getEvent(SiegeEvent.class);

					siegeEvent.addSiegeSummon(summon);
				}
				break;
			case MERCHANT:
				if(activeChar.getPet() != null || activeChar.isMounted())
					return;

				NpcTemplate merchantTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				MerchantInstance merchant = new MerchantInstance(IdFactory.getInstance().getNextId(), merchantTemplate);

				merchant.setCurrentHp(merchant.getMaxHp(), false);
				merchant.setCurrentMp(merchant.getMaxMp());
				merchant.setHeading(activeChar.getHeading());
				merchant.setReflection(activeChar.getReflection());
				merchant.spawnMe(activeChar.getLoc());

				ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(merchant), _lifeTime);
				break;
			case NPC:
				NpcUtils.spawnSingle(getNpcId(), activeChar.getLoc(), activeChar.getReflection(), _lifeTime, activeChar.getName());
				break;
		}

		if(isSSPossible())
			caster.unChargeShots(isMagic());
	}

	@Override
	public boolean isOffensive()
	{
		return _targetType == SkillTargetType.TARGET_CORPSE;
	}
}