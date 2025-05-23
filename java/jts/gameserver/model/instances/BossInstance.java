package jts.gameserver.model.instances;

import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class BossInstance extends RaidBossInstance
{
	private boolean _teleportedToNest;

	/**
	 * Constructor<?> for L2BossInstance. This represent all grandbosses:
	 * <ul>
	 * <li>29001	Queen Ant</li>
	 * <li>29014	Orfen</li>
	 * <li>29019	Antharas</li>
	 * <li>29020	Baium</li>
	 * <li>29022	Zaken</li>
	 * <li>29028	Valakas</li>
	 * <li>29006	Core</li>
	 * </ul>
	 * <br>
	 * <b>For now it's nothing more than a L2Monster but there'll be a scripting<br>
	 * engine for AI soon and we could add special behaviour for those boss</b><br>
	 * <br>
	 * @param objectId ID of the instance
	 * @param template L2NpcTemplate of the instance
	 */
	public BossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isBoss()
	{
		return true;
	}

	@Override
	public final boolean isMovementDisabled()
	{
		// Core should stay anyway
		return getNpcId() == 29006 || super.isMovementDisabled();
	}

	/**
	 * Used by Orfen to set 'teleported' flag, when hp goes to <50%
	 * @param flag
	 */
	public void setTeleported(boolean flag)
	{
		_teleportedToNest = flag;
	}

	public boolean isTeleported()
	{
		return _teleportedToNest;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}