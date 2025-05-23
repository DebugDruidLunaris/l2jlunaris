package jts.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.SymbolInstance;
import jts.gameserver.network.serverpackets.MagicSkillLaunched;
import jts.gameserver.skills.SkillTargetType;
import jts.gameserver.stats.Env;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EffectSymbol extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(EffectSymbol.class);

	private NpcInstance _symbol = null;

	public EffectSymbol(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getSkill().getTargetType() != SkillTargetType.TARGET_SELF)
		{
			_log.error("Symbol skill with target != self, id = " + getSkill().getId());
			return false;
		}

		Skill skill = getSkill().getFirstAddedSkill();
		if(skill == null)
		{
			_log.error("Not implemented symbol skill, id = " + getSkill().getId());
			return false;
		}

		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Skill skill = getSkill().getFirstAddedSkill();

		// Затычка, в клиенте они почему-то не совпадают.
		skill.setMagicType(getSkill().getMagicType());

		Location loc = _effected.getLoc();
		if(_effected.isPlayer() && ((Player) _effected).getGroundSkillLoc() != null)
		{
			loc = ((Player) _effected).getGroundSkillLoc();
			((Player) _effected).setGroundSkillLoc(null);
		}

		NpcTemplate template = NpcHolder.getInstance().getTemplate(_skill.getSymbolId());
		if(getTemplate()._count <= 1)
			_symbol = new SymbolInstance(IdFactory.getInstance().getNextId(), template, _effected, skill);
		else
			_symbol = new NpcInstance(IdFactory.getInstance().getNextId(), template);

		_symbol.setLevel(_effected.getLevel());
		_symbol.setReflection(_effected.getReflection());
		_symbol.spawnMe(loc);
	}

	@Override
	public void onExit()
	{
		super.onExit();

		if(_symbol != null && _symbol.isVisible())
			_symbol.deleteMe();

		_symbol = null;
	}

	@Override
	public boolean onActionTime()
	{
		if(getTemplate()._count <= 1)
			return false;

		Creature effector = getEffector();
		Skill skill = getSkill().getFirstAddedSkill();
		NpcInstance symbol = _symbol;
		double mpConsume = getSkill().getMpConsume();

		if(effector == null || skill == null || symbol == null)
			return false;

		if(mpConsume > effector.getCurrentMp())
		{
			effector.sendPacket(Msg.NOT_ENOUGH_MP);
			return false;
		}

		effector.reduceCurrentMp(mpConsume, effector);

		// Использовать разрешено только скиллы типа TARGET_ONE
		for(Creature cha : World.getAroundCharacters(symbol, getSkill().getSkillRadius(), 200))
			if(!cha.isDoor() && cha.getEffectList().getEffectsBySkill(skill) == null && skill.checkTarget(effector, cha, cha, false, false) == null)
			{
				if(skill.isOffensive() && !GeoEngine.canSeeTarget(symbol, cha, false))
					continue;
				List<Creature> targets = new ArrayList<Creature>(1);
				targets.add(cha);
				effector.callSkill(skill, targets, true);
				effector.broadcastPacket(new MagicSkillLaunched(symbol.getObjectId(), getSkill().getDisplayId(), getSkill().getDisplayLevel(), cha));
			}

		return true;
	}
}