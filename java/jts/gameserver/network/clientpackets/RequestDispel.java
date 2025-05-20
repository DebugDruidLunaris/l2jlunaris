package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Player;
import jts.gameserver.skills.SkillType;
import jts.gameserver.skills.EffectType;

public class RequestDispel extends L2GameClientPacket
{
	private int _objectId, _id, _level;

	@Override
	protected void readImpl() throws Exception
	{
		_objectId = readD();
		_id = readD();
		_level = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getObjectId() != _objectId && activeChar.getPet() == null)
			return;

		Creature target = activeChar;
		if(activeChar.getObjectId() != _objectId)
			target = activeChar.getPet();

		for(Effect e : target.getEffectList().getAllEffects())
			if(e.getDisplayId() == _id && e.getDisplayLevel() == _level)
				if (!e.isOffensive() && (!e.getSkill().isMusic() || Config.ALT_DISPEL_MUSIC) && e.getSkill().isSelfDispellable() && (e.getSkill().getSkillType() != SkillType.TRANSFORMATION) && (e.getTemplate().getEffectType() != EffectType.Hourglass))					e.exit();
				else
					return;
	}
}