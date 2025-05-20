package jts.gameserver.model.items.attachment;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;

public interface FlagItemAttachment extends PickableAttachment
{
	//FIXME [VISTALL] возможно переделать на слушатели игрока
	void onLogout(Player player);

	//FIXME [VISTALL] возможно переделать на слушатели игрока
	void onDeath(Player owner, Creature killer);
	boolean canAttack(Player player);
	boolean canCast(Player player, Skill skill);
}