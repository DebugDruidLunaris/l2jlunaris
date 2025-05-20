package services;

import jts.gameserver.ThreadPoolManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Summon;
import jts.gameserver.network.serverpackets.MagicSkillLaunched;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.tables.SkillTable;

public class PcCafeBuffer extends Functions implements ScriptFile
{
	@Override
	public void onLoad() {}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}

	public void doBuff(String[] args)
	{
		Player player = getSelf();
		Summon pet = player.getPet();

		int skill_id = Integer.valueOf(args[0]);
		int skill_lvl = Integer.valueOf(args[1]);
		int select_id = Integer.valueOf(args[2]);
		int buff_price = Integer.valueOf(args[3]);

		try
		{
			Skill skill = SkillTable.getInstance().getInfo(skill_id, skill_lvl);
			if(select_id == 0)
				if(player.getPcBangPoints() < buff_price)
				{
					player.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
					return;
				}
				else
				{
					player.reducePcBangPoints(buff_price);
					ThreadPoolManager.getInstance().schedule(new BeginBuff(getNpc(), skill, player), skill.getHitTime());
				}
			if(select_id == 1)
			{
				if(pet == null)
					return;
				if(player.getPcBangPoints() < buff_price)
				{
					player.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
					return;
				}
				else
				{
					player.reducePcBangPoints(buff_price);
					ThreadPoolManager.getInstance().schedule(new BeginPetBuff(getNpc(), skill, pet), skill.getHitTime());
				}
			}
		}
		catch(Exception e)
		{
			player.sendMessage(player.isLangRus() ? "Не верный скил!" : "Invalid skill!");
		}
	}

	public class BeginBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Player _target;

		public BeginBuff(Creature buffer, Skill skill, Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		@Override
		public void run()
		{
			if(_target.isInOlympiadMode())
				return;
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), _skill.getHitTime(), 0));
			ThreadPoolManager.getInstance().schedule(new EndBuff(_buffer, _skill, _target), _skill.getHitTime());
		}
	}

	public class EndBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Player _target;

		public EndBuff(Creature buffer, Skill skill, Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		@Override
		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target));
		}
	}

	public class BeginPetBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Summon _target;

		public BeginPetBuff(Creature buffer, Skill skill, Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		@Override
		public void run()
		{
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), _skill.getHitTime(), 0));
			ThreadPoolManager.getInstance().schedule(new EndPetBuff(_buffer, _skill, _target), _skill.getHitTime());
		}
	}

	public class EndPetBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Summon _target;

		public EndPetBuff(Creature buffer, Skill skill, Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		@Override
		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target));
		}
	}
}