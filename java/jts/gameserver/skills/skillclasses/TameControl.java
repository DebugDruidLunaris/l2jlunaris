package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.Config;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.TamedBeastInstance;
import jts.gameserver.templates.StatsSet;

public class TameControl extends Skill
{
	private final int _type;

	public TameControl(StatsSet set)
	{
		super(set);
		_type = set.getInteger("type", 0);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());

		if(!activeChar.isPlayer())
			return;

		Player player = activeChar.getPlayer();
		if(player.getTrainedBeasts() == null)
			return;

		if(_type == 0)
		{
			for(Creature target : targets)
				if(target != null && target instanceof TamedBeastInstance)
					if(player.getTrainedBeasts().get(target.getObjectId()) != null)
						((TamedBeastInstance) target).despawnWithDelay(1000);
		}
		else if(_type > 0)
			if(_type == 1) // Приказать бежать за хозяином.
				for(TamedBeastInstance tamedBeast : player.getTrainedBeasts().values())
					tamedBeast.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player, Config.ALT_FOLLOW_RANGE);
			else if(_type == 3) // Использовать особое умение
				for(TamedBeastInstance tamedBeast : player.getTrainedBeasts().values())
					tamedBeast.buffOwner();
			else if(_type == 4) // Отпустить всех зверей.
				for(TamedBeastInstance tamedBeast : player.getTrainedBeasts().values())
					tamedBeast.doDespawn();
	}
}