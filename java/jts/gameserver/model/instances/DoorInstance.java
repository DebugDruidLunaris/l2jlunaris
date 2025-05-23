package jts.gameserver.model.instances;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jts.commons.geometry.Shape;
import jts.commons.listener.Listener;
import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.DoorAI;
import jts.gameserver.geodata.GeoCollision;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.listener.actor.door.OnOpenCloseListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.entity.SevenSigns;
import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.MyTargetSelected;
import jts.gameserver.network.serverpackets.StaticObject;
import jts.gameserver.network.serverpackets.ValidateLocation;
import jts.gameserver.scripts.Events;
import jts.gameserver.templates.DoorTemplate;
import jts.gameserver.templates.item.WeaponTemplate;

@SuppressWarnings("serial")
public final class DoorInstance extends Creature implements GeoCollision
{
	private class AutoOpenClose extends RunnableImpl
	{
		private boolean _open;

		public AutoOpenClose(boolean open)
		{
			_open = open;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(_open)
				openMe(null, true);
			else
				closeMe(null, true);
		}
	}

	private boolean _open = true;
	private boolean _geoOpen = true;

	private Lock _openLock = new ReentrantLock();

	private int _upgradeHp;

	private byte[][] _geoAround;

	protected ScheduledFuture<?> _autoActionTask;

	public DoorInstance(int objectId, DoorTemplate template)
	{
		super(objectId, template);
	}

	public boolean isUnlockable()
	{
		return getTemplate().isUnlockable();
	}

	@Override
	public String getName()
	{
		return getTemplate().getName();
	}

	@Override
	public int getLevel()
	{
		return 1;
	}

	public int getDoorId()
	{
		return getTemplate().getNpcId();
	}

	public boolean isOpen()
	{
		return _open;
	}

	protected boolean setOpen(boolean open)
	{
		if(_open == open)
			return false;
		_open = open;
		return true;
	}

	/**
	 * Запланировать открытие/закрытие двери
	 *
	 * @param open		- открытие/закрытие
	 * @param actionDelay - время до открытие/закрытие
	 */
	public void scheduleAutoAction(boolean open, long actionDelay)
	{
		if(_autoActionTask != null)
		{
			_autoActionTask.cancel(false);
			_autoActionTask = null;
		}

		_autoActionTask = ThreadPoolManager.getInstance().schedule(new AutoOpenClose(open), actionDelay);
	}

	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil(getCurrentHpRatio() * 6);
		return Math.max(0, Math.min(6, dmg));
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isAttackable(attacker);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isAttackable(Creature attacker)
	{
		if(attacker == null || isOpen())
			return false;

		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);

		switch(getDoorType())
		{
			case WALL:
				if(!attacker.isSummon() || siegeEvent == null || !siegeEvent.containsSiegeSummon((SummonInstance) attacker))
					return false;
				break;
			case DOOR:
				Player player = attacker.getPlayer();
				if(player == null)
					return false;
				if(siegeEvent != null)
				{
					if(siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, player.getClan()) != null)
						return false;
					if(siegeEvent.getObjects(DominionSiegeEvent.DEFENDER_PLAYERS).contains(player.getObjectId()))
						return false;
				}
				break;
		}

		return !isInvul();
	}

	@Override
	public void sendChanges()
	{}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;

		if(this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));

			if(isAutoAttackable(player))
				player.sendPacket(new StaticObject(this, player));

			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			if(isAutoAttackable(player))
			{
				player.getAI().Attack(this, false, shift);
				return;
			}

			if(!isInRange(player, INTERACTION_DISTANCE))
			{
				if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				return;
			}

			getAI().onEvtTwiceClick(player);
		}
	}

	@Override
	public DoorAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = getTemplate().getNewAI(this);
			}

		return (DoorAI) _ai;
	}

	@Override
	public void broadcastStatusUpdate()
	{
		for(Player player : World.getAroundPlayers(this))
			if(player != null)
				player.sendPacket(new StaticObject(this, player));
	}

	public boolean openMe()
	{
		return openMe(null, true);
	}

	public boolean openMe(Player opener, boolean autoClose)
	{
		_openLock.lock();
		try
		{
			if(!setOpen(true))
				return false;

			setGeoOpen(true);
		}
		finally
		{
			_openLock.unlock();
		}

		broadcastStatusUpdate();

		if(autoClose && getTemplate().getCloseTime() > 0)
			scheduleAutoAction(false, getTemplate().getCloseTime() * 1000L);

		getAI().onEvtOpen(opener);

		for(Listener<Creature> l : getListeners().getListeners())
			if(l instanceof OnOpenCloseListener)
				((OnOpenCloseListener) l).onOpen(this);

		return true;
	}

	public boolean closeMe()
	{
		return closeMe(null, true);
	}

	public boolean closeMe(Player closer, boolean autoOpen)
	{
		if(isDead())
			return false;

		_openLock.lock();
		try
		{
			if(!setOpen(false))
				return false;

			setGeoOpen(false);
		}
		finally
		{
			_openLock.unlock();
		}

		broadcastStatusUpdate();

		if(autoOpen && getTemplate().getOpenTime() > 0)
		{
			long openDelay = getTemplate().getOpenTime() * 1000L;
			if(getTemplate().getRandomTime() > 0)
				openDelay += Rnd.get(0, getTemplate().getRandomTime()) * 1000L;

			scheduleAutoAction(true, openDelay);
		}

		getAI().onEvtClose(closer);

		for(Listener<Creature> l : getListeners().getListeners())
			if(l instanceof OnOpenCloseListener)
				((OnOpenCloseListener) l).onClose(this);

		return true;
	}

	@Override
	public String toString()
	{
		return "[Door " + getDoorId() + "]";
	}

	@Override
	protected void onDeath(Creature killer)
	{
		_openLock.lock();
		try
		{
			setGeoOpen(true);
		}
		finally
		{
			_openLock.unlock();
		}

		super.onDeath(killer);
	}

	@Override
	protected void onRevive()
	{
		super.onRevive();

		_openLock.lock();
		try
		{
			if(!isOpen())
				setGeoOpen(false);
		}
		finally
		{
			_openLock.unlock();
		}
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		setCurrentHpMp(getMaxHp(), getMaxMp(), true);

		closeMe(null, true);
	}

	@Override
	protected void onDespawn()
	{
		if(_autoActionTask != null)
		{
			_autoActionTask.cancel(false);
			_autoActionTask = null;
		}

		super.onDespawn();
	}

	public boolean isHPVisible()
	{
		return getTemplate().isHPVisible();
	}

	@Override
	public int getMaxHp()
	{
		return super.getMaxHp() + _upgradeHp;
	}

	public void setUpgradeHp(int hp)
	{
		_upgradeHp = hp;
	}

	public int getUpgradeHp()
	{
		return _upgradeHp;
	}

	@Override
	public int getPDef(Creature target)
	{
		switch(SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_DAWN:
				return (int) (super.getPDef(target) * 1.2);
			case SevenSigns.CABAL_DUSK:
				return (int) (super.getPDef(target) * 0.3);
			default:
				return super.getPDef(target);
		}
	}

	@Override
	public int getMDef(Creature target, Skill skill)
	{
		switch(SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_DAWN:
				return (int) (super.getMDef(target, skill) * 1.2);
			case SevenSigns.CABAL_DUSK:
				return (int) (super.getMDef(target, skill) * 0.3);
			default:
				return super.getMDef(target, skill);
		}
	}

	/**
	 * Двери на осадах уязвимы во время осады.
	 * Остальные двери не уязвимы вообще.
	 *
	 * @return инвульная ли дверь.
	 */
	@Override
	public boolean isInvul()
	{
		if(!getTemplate().isHPVisible())
			return true;
		else
		{
			SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
			if(siegeEvent != null && siegeEvent.isInProgress())
				return false;

			return super.isInvul();
		}
	}

	/**
	 * Устанавливает значение закрытости\открытости в геодате<br>
	 *
	 * @param open новое значение
	 */
	protected boolean setGeoOpen(boolean open)
	{
		if(_geoOpen == open)
			return false;

		_geoOpen = open;

		if(Config.ALLOW_GEODATA)
			if(open)
				GeoEngine.removeGeoCollision(this, getGeoIndex());
			else
				GeoEngine.applyGeoCollision(this, getGeoIndex());

		return true;
	}

	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}

	@Override
	public boolean isActionsDisabled()
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

	@Override
	public boolean isConcrete()
	{
		return true;
	}

	@Override
	public boolean isHealBlocked()
	{
		return true;
	}

	@Override
	public boolean isEffectImmune()
	{
		return true;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		return Collections.<L2GameServerPacket> singletonList(new StaticObject(this, forPlayer));
	}

	@Override
	public boolean isDoor()
	{
		return true;
	}

	@Override
	public Shape getShape()
	{
		return getTemplate().getPolygon();
	}

	@Override
	public byte[][] getGeoAround()
	{
		return _geoAround;
	}

	@Override
	public void setGeoAround(byte[][] geo)
	{
		_geoAround = geo;
	}

	@Override
	public DoorTemplate getTemplate()
	{
		return (DoorTemplate) super.getTemplate();
	}

	public DoorTemplate.DoorType getDoorType()
	{
		return getTemplate().getDoorType();
	}

	public int getKey()
	{
		return getTemplate().getKey();
	}
}