package jts.gameserver.model;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jts.commons.lang.reference.HardReference;
import jts.commons.util.Rnd;
import jts.commons.util.concurrent.atomic.AtomicState;
import jts.gameserver.Config;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.cache.Msg;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.model.AggroList.AggroInfo;
import jts.gameserver.skills.SkillType;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.entity.events.impl.DuelEvent;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.StaticObjectInstance;
import jts.gameserver.model.items.Inventory;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.Revive;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.skills.EffectType;
import jts.gameserver.skills.SkillTargetType;
import jts.gameserver.stats.Stats;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.CharTemplate;
import jts.gameserver.templates.item.EtcItemTemplate;
import jts.gameserver.templates.item.WeaponTemplate;
import jts.gameserver.templates.item.WeaponTemplate.WeaponType;

@SuppressWarnings("serial")
public abstract class Playable extends Creature
{
	private AtomicState _isSilentMoving = new AtomicState();

	private boolean _isPendingRevive;

	/** Блокировка для чтения/записи состояний квестов */
	protected final ReadWriteLock questLock = new ReentrantReadWriteLock();
	protected final Lock questRead = questLock.readLock();
	protected final Lock questWrite = questLock.writeLock();

	public Playable(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<? extends Playable> getRef()
	{
		return (HardReference<? extends Playable>) super.getRef();
	}

	public abstract Inventory getInventory();

	public abstract long getWearedMask();

	/**
	 * Проверяет, выставлять ли PvP флаг для игрока.<BR><BR>
	 */
	@Override
	public boolean checkPvP(final Creature target, Skill skill)
	{
		Player player = getPlayer();

		if(isDead() || target == null || player == null || target == this || target == player || target == player.getPet() || player.getKarma() > 0)
			return false;

		if(skill != null)
		{
			if(skill.altUse())
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_FEEDABLE_BEAST)
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_UNLOCKABLE)
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_CHEST)
				return false;
		}

		// Проверка на дуэли... Мэмбэры одной дуэли не флагаются
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && duelEvent == target.getEvent(DuelEvent.class))
			return false;

		if(isInZonePeace() && target.isInZonePeace())
			return false;
		if(isInZoneBattle() && target.isInZoneBattle())
			return false;
		if(isInZone(ZoneType.SIEGE) && target.isInZone(ZoneType.SIEGE))
			return false;

		if(skill == null || skill.isOffensive())
		{
			if(target.getKarma() > 0)
				return false;
			else if(target.isPlayable())
				return true;
		}
		else if(target.getPvpFlag() > 0 || target.getKarma() > 0 || target.isMonster())
			return true;

		return false;
	}

	/**
	 * Проверяет, можно ли атаковать цель (для физ атак)
	 */
	public boolean checkTarget(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
			return false;

		if(target == null || target.isDead())
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(!isInRange(target, 2000))
		{
			player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(target.isDoor() && !target.isAttackable(this))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(target.paralizeOnAttack(this))
		{
			if(Config.PARALIZE_ON_RAID_DIFF)
				paralizeMe(target);
			return false;
		}

		if(target.isInvisible() || getReflection() != target.getReflection() || !GeoEngine.canSeeTarget(this, target, false))
		{
			player.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}

		// Запрет на атаку мирных NPC в осадной зоне на TW. Иначе таким способом набивают очки.
		//if(player.getTerritorySiege() > -1 && target.isNpc() && !(target instanceof L2TerritoryFlagInstance) && !(target.getAI() instanceof DefaultAI) && player.isInZone(ZoneType.Siege))
		//{
		//	player.sendPacket(Msg.INVALID_TARGET);
		//	return false;
		//}

		if(player.isInZone(ZoneType.epic) != target.isInZone(ZoneType.epic))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(target.isPlayable())
		{
			// Нельзя атаковать того, кто находится на арене, если ты сам не на арене
			if(isInZoneBattle() != target.isInZoneBattle())
			{
				player.sendPacket(Msg.INVALID_TARGET);
				return false;
			}

			// Если цель либо атакующий находится в мирной зоне - атаковать нельзя
			if(isInZonePeace() || target.isInZonePeace())
			{
				player.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				return false;
			}
			if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
				return false;
		}

		return true;
	}

	private boolean isBetray()
	{
		if(this.isSummon())
		{
			for(Effect e : getEffectList().getAllEffects())
				if(e.getEffectType() == EffectType.Betray)
				{
					return true;
				}

			return false;
		}
		return false;
	}

	@Override
	public void doAttack(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
			return;

		if((this.isSummon() || this.isPet()) && target.isPlayer() && target.getPlayer() == this.getPlayer() && !isBetray())
		{
			player.sendMessage(player.isLangRus() ? "Не могу бить своего хозяина!" : "I can not beat its owner!");
			player.sendActionFailed();
			this.sendActionFailed();
			return;
		}

		if(isAMuted() || isAttackingNow())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isInObserverMode())
		{
			player.sendMessage(new CustomMessage("jts.gameserver.model.L2Playable.OutOfControl.ObserverNoAttack", player));
			return;
		}

		if(!checkTarget(target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			player.sendActionFailed();
			return;
		}

		// Прерывать дуэли если цель не дуэлянт
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
			duelEvent.abortDuel(getPlayer());

		WeaponTemplate weaponItem = getActiveWeaponItem();

		if(weaponItem != null && (weaponItem.getItemType() == WeaponType.BOW || weaponItem.getItemType() == WeaponType.CROSSBOW))
		{
			double bowMpConsume = weaponItem.getMpConsume();
			if(bowMpConsume > 0)
			{
				// cheap shot SA
				double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0., target, null);
				if(chance > 0 && Rnd.chance(chance))
					bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);

				if(_currentMp < bowMpConsume)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					player.sendPacket(Msg.NOT_ENOUGH_MP);
					player.sendActionFailed();
					return;
				}

				reduceCurrentMp(bowMpConsume, null);
			}

			if(!player.checkAndEquipArrows())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				player.sendPacket(player.getActiveWeaponInstance().getItemType() == WeaponType.BOW ? Msg.YOU_HAVE_RUN_OUT_OF_ARROWS : Msg.NOT_ENOUGH_BOLTS);
				player.sendActionFailed();
				return;
			}
		}

		super.doAttack(target);
	}

	@Override
	public void doCast(final Skill skill, final Creature target, boolean forceUse)
	{
		if(skill == null)
			return;

		if((this.isSummon() || this.isPet()) && skill.isOffensive() && target.isPlayer() && target.getPlayer() == this.getPlayer() && skill.getId() != 1380)
		{
			this.getPlayer().sendMessage(this.getPlayer().isLangRus() ? "Не могу бить своего хозяина!" : "I can not beat its owner!");
			this.getPlayer().sendActionFailed();
			this.sendActionFailed();
			return;
		}

		// Прерывать дуэли если цель не дуэлянт
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
			duelEvent.abortDuel(getPlayer());

		//нельзя использовать масс скиллы в мирной зоне
		if(skill.isAoE() && isInPeaceZone())
		{
			getPlayer().sendPacket(Msg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
			return;
		}

		if(skill.getSkillType() == SkillType.DEBUFF && target.isNpc() && target.isInvul() && !target.isMonster() && !target.isInCombat() && target.getPvpFlag() == 0)
		{
			getPlayer().sendPacket(Msg.INVALID_TARGET);
			return;
		}

		super.doCast(skill, target, forceUse);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;

		if(isDamageBlocked() && transferDamage)
			return;

		if(isDamageBlocked() && attacker != this)
		{
			if(sendMessage)
				attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		if(attacker != this && attacker.isPlayable())
		{
			Player player = getPlayer();
			Player pcAttacker = attacker.getPlayer();
			if(pcAttacker != player)
				if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
				{
					if(sendMessage)
						pcAttacker.sendPacket(Msg.INVALID_TARGET);
					return;
				}

			if(isInZoneBattle() != attacker.isInZoneBattle())
			{
				if(sendMessage)
					attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
				return;
			}

			DuelEvent duelEvent = getEvent(DuelEvent.class);
			if(duelEvent != null && attacker.getEvent(DuelEvent.class) != duelEvent)
				duelEvent.abortDuel(player);
		}

		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	public int getPAtkSpd()
	{
		return Math.max((int) calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _template.basePAtkSpd, null, null), null, null), 1);
	}

	@Override
	public int getPAtk(final Creature target)
	{
		double init = getActiveWeaponInstance() == null ? _template.basePAtk : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getMAtk(final Creature target, final Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		final double init = getActiveWeaponInstance() == null ? _template.baseMAtk : 0;
		return (int) calcStat(Stats.MAGIC_ATTACK, init, target, skill);
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, true, false);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, false, false);
	}

	public boolean isCtrlAttackable(Creature attacker, boolean force, boolean witchCtrl)
	{
		Player player = getPlayer();
		if(attacker == null || player == null || attacker == this || attacker == player && !force || isAlikeDead() || attacker.isAlikeDead())
			return false;

		if(isInvisible() || getReflection() != attacker.getReflection())
			return false;

		if(isInBoat())
			return false;

		for(GlobalEvent e : getEvents())
			if(e.checkForAttack(this, attacker, null, force) != null)
				return false;

		for(GlobalEvent e : player.getEvents())
			if(e.canAttack(this, attacker, null, force))
				return true;

		Player pcAttacker = attacker.getPlayer();

		if(pcAttacker != null && pcAttacker != player)
		{
			if(pcAttacker.isInBoat())
				return false;

			if(pcAttacker.getBlockCheckerArena() > -1 || player.getBlockCheckerArena() > -1)
				return false;

			// Player with lvl < 21 can't attack a cursed weapon holder, and a cursed weapon holder can't attack players with lvl < 21
			if(pcAttacker.isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && pcAttacker.getLevel() < 21)
				return false;

			if(player.isInZone(ZoneType.epic) != pcAttacker.isInZone(ZoneType.epic))
				return false;

			if((player.isInOlympiadMode() || pcAttacker.isInOlympiadMode()) && player.getOlympiadGame() != pcAttacker.getOlympiadGame()) // На всякий случай
				return false;
			if(player.isInOlympiadMode() && !player.isOlympiadCompStart()) // Бой еще не начался
				return false;
			if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcAttacker.getOlympiadSide() && !force) // Свою команду атаковать нельзя
				return false;

			//TODO [VISTALL] что за?
			/*if(pcAttacker.getTeam() > 0 && pcAttacker.isChecksForTeam() && player.getTeam() == 0) // Запрет на атаку/баф участником эвента незарегистрированного игрока
				return false;
			if(player.getTeam() > 0 && player.isChecksForTeam() && pcAttacker.getTeam() == 0) // Запрет на атаку/баф участника эвента незарегистрированным игроком
				return false;
			if(player.getTeam() > 0 && player.isChecksForTeam() && pcAttacker.getTeam() > 0 && pcAttacker.isChecksForTeam() && player.getTeam() == pcAttacker.getTeam()) // Свою команду атаковать нельзя
				return false;   */

			if(isInZonePeace())
				return false;
			if(isInZoneBattle())
				return true;
			if(!force && player.isInParty() && pcAttacker.isInParty() && player.getParty() == pcAttacker.getParty())
				return false;
			if(!force && player.isInParty() && player.getParty().getCommandChannel() != null && pcAttacker.isInParty() && pcAttacker.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == pcAttacker.getParty().getCommandChannel())
				return false;
			if(!force && player.getClan() != null && player.getClan() == pcAttacker.getClan())
				return false;
			if(!force && player.getClan() != null && player.getClan().getAlliance() != null && pcAttacker.getClan() != null && pcAttacker.getClan().getAlliance() != null && player.getClan().getAlliance() == pcAttacker.getClan().getAlliance())
				return false;
			if(isInZone(ZoneType.SIEGE))
				return true;

			if(pcAttacker.atMutualWarWith(player))
				return true;
			if(player.getKarma() > 0 || player.getPvpFlag() != 0)
				return true;
			if(witchCtrl && player.getPvpFlag() > 0)
				return true;

			return force;
		}

		return true;
	}

	@Override
	public int getKarma()
	{
		Player player = getPlayer();
		return player == null ? 0 : player.getKarma();
	}

	@Override
	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
	{
		Player player = getPlayer();
		if(player == null)
			return;

		if(useActionSkills && !skill.altUse() && !skill.getSkillType().equals(SkillType.BEAST_FEED))
			for(Creature target : targets)
			{
				if(target.isNpc())
				{
					if(skill.isOffensive())
					{
						// mobs will hate on debuff
						if(target.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
								paralizeMe(target);
							return;
						}
						if(!skill.isAI())
						{
							int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : 1;
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, damage);
						}
					}
					target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
				}
				else // исключать баффы питомца на владельца
				if(target.isPlayable() && target != getPet() && !((isSummon() || isPet()) && target == player))
				{
					int aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());

					List<NpcInstance> npcs = World.getAroundNpc(target);
					for(NpcInstance npc : npcs)
					{
						if(npc.isDead() || !npc.isInRangeZ(this, 2000)) //FIXME [G1ta0] параметр достойный конфига
							continue;

						npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);

						AggroInfo ai = npc.getAggroList().get(target);
						//Пропускаем, если цель отсутсвует в хейтлисте
						if(ai == null)
							continue;

						if(!skill.isHandler() && npc.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
								paralizeMe(npc);
							return;
						}

						//Если хейт меньше 100, пропускаем
						if(ai.hate < 100)
							continue;

						if(GeoEngine.canSeeTarget(npc, target, false)) // Моб агрится только если видит цель, которую лечишь/бафаешь.
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, ai.damage == 0 ? aggro / 2 : aggro);
					}
				}

				// Check for PvP Flagging / Drawing Aggro
				if(checkPvP(target, skill))
					startPvPFlag(target);
			}

		super.callSkill(skill, targets, useActionSkills);
	}

	/**
	 * Оповещает других игроков о поднятии вещи
	 * @param item предмет который был поднят
	 */
	public void broadcastPickUpMsg(ItemInstance item)
	{
		Player player = getPlayer();

		if(item == null || player == null || player.isInvisible())
			return;

		if(item.isEquipable() && !(item.getTemplate() instanceof EtcItemTemplate))
		{
			SystemMessage msg = null;
			String player_name = player.getName();
			if(item.getEnchantLevel() > 0)
			{
				int msg_id = isPlayer() ? SystemMessage.ATTENTION_S1_PICKED_UP__S2_S3 : SystemMessage.ATTENTION_S1_PET_PICKED_UP__S2_S3;
				msg = new SystemMessage(msg_id).addString(player_name).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			}
			else
			{
				int msg_id = isPlayer() ? SystemMessage.ATTENTION_S1_PICKED_UP_S2 : SystemMessage.ATTENTION_S1_PET_PICKED_UP__S2_S3;
				msg = new SystemMessage(msg_id).addString(player_name).addItemName(item.getItemId());
			}
			player.broadcastPacket(msg);
		}
	}

	public void paralizeMe(Creature effector)
	{
		Skill revengeSkill = SkillTable.getInstance().getInfo(Skill.SKILL_RAID_CURSE, 1);
		revengeSkill.getEffects(effector, this, false, false);
	}

	public final void setPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public boolean isPendingRevive()
	{
		return _isPendingRevive;
	}

	/** Sets HP, MP and CP and revives the L2Playable. */
	public void doRevive()
	{
		if(!isTeleporting())
		{
			setPendingRevive(false);
			setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);

			if(isSalvation())
			{
				for(Effect e : getEffectList().getAllEffects())
					if(e.getEffectType() == EffectType.Salvation)
					{
						e.exit();
						break;
					}
				setCurrentHp(getMaxHp(), true);
				setCurrentMp(getMaxMp());
				setCurrentCp(getMaxCp());
			}
			else
			{
				if(isPlayer() && Config.RESPAWN_RESTORE_CP >= 0)
					setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);

				setCurrentHp(Math.max(1, getMaxHp() * Config.RESPAWN_RESTORE_HP), true);

				if(Config.RESPAWN_RESTORE_MP >= 0)
					setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
			}

			broadcastPacket(new Revive(this));
		}
		else
			setPendingRevive(true);
	}

	public abstract void doPickupItem(GameObject object);

	public void sitDown(StaticObjectInstance throne)
	{}

	public void standUp()
	{}

	private long _nonAggroTime;

	public long getNonAggroTime()
	{
		return _nonAggroTime;
	}

	public void setNonAggroTime(long time)
	{
		_nonAggroTime = time;
	}

	/**
	 * 
	 * @return предыдущее состояние
	 */
	public boolean startSilentMoving()
	{
		return _isSilentMoving.getAndSet(true);
	}

	/**
	 * 
	 * @return текущее состояние
	 */
	public boolean stopSilentMoving()
	{
		return _isSilentMoving.setAndGet(false);
	}

	/**
	 * @return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving.get();
	}

	public boolean isInCombatZone()
	{
		return isInZoneBattle();
	}

	public boolean isInPeaceZone()
	{
		return isInZonePeace();
	}

	@Override
	public boolean isInZoneBattle()
	{
		return super.isInZoneBattle();
	}

	public boolean isOnSiegeField()
	{
		return isInZone(ZoneType.SIEGE);
	}

	public boolean isInSSQZone()
	{
		return isInZone(ZoneType.ssq_zone);
	}

	public boolean isInDangerArea()
	{
		return isInZone(ZoneType.damage) || isInZone(ZoneType.swamp) || isInZone(ZoneType.poison) || isInZone(ZoneType.instant_skill);
	}

	public int getMaxLoad()
	{
		return 0;
	}

	public int getInventoryLimit()
	{
		return 0;
	}

	@Override
	public boolean isPlayable()
	{
		return true;
	}
}