package jts.gameserver.skills.effects;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Player;
import jts.gameserver.stats.Env;
import jts.gameserver.stats.Stats;
import jts.gameserver.stats.funcs.Func;

public class EffectServitorShare extends Effect
{
	public EffectServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public Func[] getStatFuncs()
	{
		return new Func[] { new Func(Stats.POWER_ATTACK, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getPAtk(target != null ? target : null) * 0.5D;
			}
		}, new Func(Stats.POWER_DEFENCE, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getPDef(target != null ? target : null) * 0.5D;
			}
		}, new Func(Stats.MAGIC_ATTACK, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getMAtk(target != null ? target : null, env.skill) * 0.25D;
			}
		}, new Func(Stats.MAGIC_DEFENCE, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getMDef(target != null ? target : null, env.skill) * 0.25D;
			}
		}, new Func(Stats.MAX_HP, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				//Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getMaxHp() * 0.1D;
			}
		}, new Func(Stats.MAX_MP, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				//Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getMaxMp() * 0.1D;
			}
		}, new Func(Stats.CRITICAL_BASE, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getCriticalHit(target != null ? target : null, env.skill) * 0.2D;
			}
		}, new Func(Stats.POWER_ATTACK_SPEED, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				//Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getPAtkSpd() * 0.1D;
			}
		}, new Func(Stats.MAGIC_ATTACK_SPEED, 64, this){
			@Override
			public void calc(Env env)
			{
				Player caster = env.character.getPlayer();
				//Creature target = env.target;
				if(caster != null && caster.getPet() != null)
					env.value += caster.getMAtkSpd() * 0.03D;
			}
		} };
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}