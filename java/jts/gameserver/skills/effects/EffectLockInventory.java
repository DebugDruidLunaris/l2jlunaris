package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.LockType;
import jts.gameserver.stats.Env;

public class EffectLockInventory extends Effect
{
	private LockType _lockType;
	private int[] _lockItems;

	public EffectLockInventory(Env env, EffectTemplate template)
	{
		super(env, template);
		_lockType = template.getParam().getEnum("lockType", LockType.class);
		_lockItems = template.getParam().getIntegerArray("lockItems");
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Player player = _effector.getPlayer();

		player.getInventory().lockItems(_lockType, _lockItems);
	}

	@Override
	public void onExit()
	{
		super.onExit();

		Player player = _effector.getPlayer();

		player.getInventory().unlock();
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}