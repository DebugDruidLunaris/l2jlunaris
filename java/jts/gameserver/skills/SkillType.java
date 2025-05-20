package jts.gameserver.skills;

import java.lang.reflect.Constructor;

import jts.gameserver.model.Skill;
import jts.gameserver.skills.skillclasses.AIeffects;
import jts.gameserver.skills.skillclasses.Aggression;
import jts.gameserver.skills.skillclasses.Balance;
import jts.gameserver.skills.skillclasses.BeastFeed;
import jts.gameserver.skills.skillclasses.BuffCharger;
import jts.gameserver.skills.skillclasses.CPDam;
import jts.gameserver.skills.skillclasses.Call;
import jts.gameserver.skills.skillclasses.ChainHeal;
import jts.gameserver.skills.skillclasses.Charge;
import jts.gameserver.skills.skillclasses.ChargeSoul;
import jts.gameserver.skills.skillclasses.ClanGate;
import jts.gameserver.skills.skillclasses.CombatPointHeal;
import jts.gameserver.skills.skillclasses.Continuous;
import jts.gameserver.skills.skillclasses.Craft;
import jts.gameserver.skills.skillclasses.CurseDivinity;
import jts.gameserver.skills.skillclasses.DeathPenalty;
import jts.gameserver.skills.skillclasses.Decoy;
import jts.gameserver.skills.skillclasses.Default;
import jts.gameserver.skills.skillclasses.DefuseTrap;
import jts.gameserver.skills.skillclasses.DeleteHate;
import jts.gameserver.skills.skillclasses.DeleteHateOfMe;
import jts.gameserver.skills.skillclasses.DestroySummon;
import jts.gameserver.skills.skillclasses.DetectTrap;
import jts.gameserver.skills.skillclasses.Disablers;
import jts.gameserver.skills.skillclasses.Drain;
import jts.gameserver.skills.skillclasses.DrainSoul;
import jts.gameserver.skills.skillclasses.Effect;
import jts.gameserver.skills.skillclasses.EffectsFromSkills;
import jts.gameserver.skills.skillclasses.EnergyReplenish;
import jts.gameserver.skills.skillclasses.ExtractStone;
import jts.gameserver.skills.skillclasses.FishingSkill;
import jts.gameserver.skills.skillclasses.Harvesting;
import jts.gameserver.skills.skillclasses.Heal;
import jts.gameserver.skills.skillclasses.HealPercent;
import jts.gameserver.skills.skillclasses.InstantJump;
import jts.gameserver.skills.skillclasses.KamaelWeaponExchange;
import jts.gameserver.skills.skillclasses.LearnSkill;
import jts.gameserver.skills.skillclasses.LethalShot;
import jts.gameserver.skills.skillclasses.MDam;
import jts.gameserver.skills.skillclasses.ManaDam;
import jts.gameserver.skills.skillclasses.ManaHeal;
import jts.gameserver.skills.skillclasses.ManaHealPercent;
import jts.gameserver.skills.skillclasses.NegateEffects;
import jts.gameserver.skills.skillclasses.NegateStats;
import jts.gameserver.skills.skillclasses.PDam;
import jts.gameserver.skills.skillclasses.PcBangPointsAdd;
import jts.gameserver.skills.skillclasses.PetSummon;
import jts.gameserver.skills.skillclasses.Recall;
import jts.gameserver.skills.skillclasses.ReelingPumping;
import jts.gameserver.skills.skillclasses.Refill;
import jts.gameserver.skills.skillclasses.Resurrect;
import jts.gameserver.skills.skillclasses.Ride;
import jts.gameserver.skills.skillclasses.SPHeal;
import jts.gameserver.skills.skillclasses.ShiftAggression;
import jts.gameserver.skills.skillclasses.Sowing;
import jts.gameserver.skills.skillclasses.Spoil;
import jts.gameserver.skills.skillclasses.StealBuff;
import jts.gameserver.skills.skillclasses.Summon;
import jts.gameserver.skills.skillclasses.SummonItem;
import jts.gameserver.skills.skillclasses.SummonSiegeFlag;
import jts.gameserver.skills.skillclasses.Sweep;
import jts.gameserver.skills.skillclasses.TakeCastle;
import jts.gameserver.skills.skillclasses.TakeFlag;
import jts.gameserver.skills.skillclasses.TakeFortress;
import jts.gameserver.skills.skillclasses.TameControl;
import jts.gameserver.skills.skillclasses.TeleportNpc;
import jts.gameserver.skills.skillclasses.Toggle;
import jts.gameserver.skills.skillclasses.Transformation;
import jts.gameserver.skills.skillclasses.Unlock;
import jts.gameserver.skills.skillclasses.VitalityHeal;
import jts.gameserver.templates.StatsSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum SkillType
{
	AGGRESSION(Aggression.class),
	AIEFFECTS(AIeffects.class),
	BALANCE(Balance.class),
	BEAST_FEED(BeastFeed.class),
	BLEED(Continuous.class),
	BUFF(Continuous.class),
	BUFF_CHARGER(BuffCharger.class),
	CALL(Call.class),
	CHAIN_HEAL(ChainHeal.class),
	CHARGE(Charge.class),
	CHARGE_SOUL(ChargeSoul.class),
	CLAN_GATE(ClanGate.class),
	COMBATPOINTHEAL(CombatPointHeal.class),
	CONT(Toggle.class),
	CPDAM(CPDam.class),
	CPHOT(Continuous.class),
	CRAFT(Craft.class),
	DEATH_PENALTY(DeathPenalty.class),
	DECOY(Decoy.class),
	DEBUFF(Continuous.class),
	DELETE_HATE(DeleteHate.class),
	DELETE_HATE_OF_ME(DeleteHateOfMe.class),
	DESTROY_SUMMON(DestroySummon.class),
	DEFUSE_TRAP(DefuseTrap.class),
	DETECT_TRAP(DetectTrap.class),
	DISCORD(Continuous.class),
	DOT(Continuous.class),
	DRAIN(Drain.class),
	DRAIN_SOUL(DrainSoul.class),
	EFFECT(Effect.class),
	EFFECTS_FROM_SKILLS(EffectsFromSkills.class),
	ENERGY_REPLENISH(EnergyReplenish.class),
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	EXTRACT_STONE(ExtractStone.class),
	FEED_PET,
	FISHING(FishingSkill.class),
	HARDCODED(Effect.class),
	HARVESTING(Harvesting.class),
	HEAL(Heal.class),
	HEAL_PERCENT(HealPercent.class),
	HOT(Continuous.class),
	INSTANT_JUMP(InstantJump.class),
	KAMAEL_WEAPON_EXCHANGE(KamaelWeaponExchange.class),
	LEARN_SKILL(LearnSkill.class),
	LETHAL_SHOT(LethalShot.class),
	LUCK,
	MANADAM(ManaDam.class),
	MANAHEAL(ManaHeal.class),
	MANAHEAL_PERCENT(ManaHealPercent.class),
	MDAM(MDam.class),
	MDOT(Continuous.class),
	MPHOT(Continuous.class),
	MUTE(Disablers.class),
	NEGATE_EFFECTS(NegateEffects.class),
	NEGATE_STATS(NegateStats.class),
	ADD_PC_BANG(PcBangPointsAdd.class),
	NOTDONE,
	NOTUSED,
	PARALYZE(Disablers.class),
	PASSIVE,
	PDAM(PDam.class),
	PET_SUMMON(PetSummon.class),
	POISON(Continuous.class),
	PUMPING(ReelingPumping.class),
	RECALL(Recall.class),
	REELING(ReelingPumping.class),
	REFILL(Refill.class),
	RESURRECT(Resurrect.class),
	RIDE(Ride.class),
	ROOT(Disablers.class),
	SHIFT_AGGRESSION(ShiftAggression.class),
	SLEEP(Disablers.class),
	SOULSHOT,
	SOWING(Sowing.class),
	SPHEAL(SPHeal.class),
	SPIRITSHOT,
	SPOIL(Spoil.class),
	STEAL_BUFF(StealBuff.class),
	CURSE_DIVINITY(CurseDivinity.class),
	STUN(Disablers.class),
	SUMMON(Summon.class),
	SUMMON_FLAG(SummonSiegeFlag.class),
	SUMMON_ITEM(SummonItem.class),
	SWEEP(Sweep.class),
	TAKECASTLE(TakeCastle.class),
	TAKEFORTRESS(TakeFortress.class),
	TAMECONTROL(TameControl.class),
	TAKEFLAG(TakeFlag.class),
	TELEPORT_NPC(TeleportNpc.class),
	TRANSFORMATION(Transformation.class),
	UNLOCK(Unlock.class),
	WATCHER_GAZE(Continuous.class),
	VITALITY_HEAL(VitalityHeal.class);

	private final Class<? extends Skill> clazz;

	private static final Logger _log = LoggerFactory.getLogger(SkillType.class);

	private SkillType()
	{
		clazz = Default.class;
	}

	private SkillType(Class<? extends Skill> clazz)
	{
		this.clazz = clazz;
	}

	public Skill makeSkill(StatsSet set)
	{
		try
		{
			Constructor<? extends Skill> c = clazz.getConstructor(StatsSet.class);
			return c.newInstance(set);
		}
		catch(Exception e)
		{
			_log.error("", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Работают только против npc
	 */
	public final boolean isPvM()
	{
		switch(this)
		{
			case DISCORD:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Такие скиллы не аггрят цель, и не флагают чара, но являются "плохими"
	 */
	public boolean isAI()
	{
		switch(this)
		{
			case AGGRESSION:
			case AIEFFECTS:
			case SOWING:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
				return true;
			default:
				return false;
		}
	}

	public final boolean isPvpSkill()
	{
		switch(this)
		{
			case BLEED:
			case AGGRESSION:
			case DEBUFF:
			case DOT:
			case MDOT:
			case MUTE:
			case PARALYZE:
			case POISON:
			case ROOT:
			case SLEEP:
			case MANADAM:
			case DESTROY_SUMMON:
			case NEGATE_STATS:
			case NEGATE_EFFECTS:
			case STEAL_BUFF:
			case CURSE_DIVINITY:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
				return true;
			default:
				return false;
		}
	}

	public boolean isOffensive()
	{
		switch(this)
		{
			case AGGRESSION:
			case AIEFFECTS:
			case BLEED:
			case DEBUFF:
			case DOT:
			case DRAIN:
			case DRAIN_SOUL:
			case LETHAL_SHOT:
			case MANADAM:
			case MDAM:
			case MDOT:
			case MUTE:
			case PARALYZE:
			case PDAM:
			case CPDAM:
			case POISON:
			case ROOT:
			case SLEEP:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case STUN:
			case SWEEP:
			case HARVESTING:
			case TELEPORT_NPC:
			case SOWING:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
			case DESTROY_SUMMON:
			case STEAL_BUFF:
			case CURSE_DIVINITY:
			case DISCORD:
				return true;
			default:
				return false;
		}
	}
}