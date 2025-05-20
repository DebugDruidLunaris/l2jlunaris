package npc.model.residences.castle;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class CastleFakeTowerInstance extends NpcInstance
{
	public CastleFakeTowerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	/**
	 * Фэйковые вышки нельзя атаковать
	 */
	@Override
	public boolean isAutoAttackable(Creature player)
	{
		return false;
	}

	/**
	 * Вышки не умеют говорить
	 */
	@Override
	public void showChatWindow(Player player, int val, Object... arg) {}

	/**
	 * Вышки не умеют говорить
	 */
	@Override
	public void showChatWindow(Player player, String filename, Object... replace) {}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	/**
	 * Фэйковые вышки неуязвимы
	 * @return true
	 */
	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
}