package jts.gameserver.model;

import gnu.trove.iterator.TIntObjectIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.SummonAI;
import jts.gameserver.dao.EffectsDAO;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.model.actor.recorder.SummonStatsChangeRecorder;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.entity.events.impl.DuelEvent;
import jts.gameserver.model.instances.PetInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.PetInventory;
import jts.gameserver.network.serverpackets.ActionFail;
import jts.gameserver.network.serverpackets.AutoAttackStart;
import jts.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import jts.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import jts.gameserver.network.serverpackets.ExPartyPetWindowUpdate;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.MyTargetSelected;
import jts.gameserver.network.serverpackets.NpcInfo;
import jts.gameserver.network.serverpackets.PartySpelled;
import jts.gameserver.network.serverpackets.PetDelete;
import jts.gameserver.network.serverpackets.PetInfo;
import jts.gameserver.network.serverpackets.PetItemList;
import jts.gameserver.network.serverpackets.PetStatusShow;
import jts.gameserver.network.serverpackets.PetStatusUpdate;
import jts.gameserver.network.serverpackets.RelationChanged;
import jts.gameserver.network.serverpackets.StatusUpdate;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Events;
import jts.gameserver.stats.Stats;
import jts.gameserver.taskmanager.DecayTaskManager;
import jts.gameserver.templates.item.WeaponTemplate;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;

@SuppressWarnings("serial")
public abstract class Summon extends Playable
{
	private static final int SUMMON_DISAPPEAR_RANGE = 2500;

	private final Player _owner;

	private int _spawnAnimation = 2;
	protected long _exp = 0;
	protected int _sp = 0;
	private int _maxLoad, _spsCharged;
	private boolean _follow = true, _depressed = false, _ssCharged = false;

	private Future<?> _decayTask;

	public Summon(int objectId, NpcTemplate template, Player owner)
	{
		super(objectId, template);
		_owner = owner;

		if(template.getSkills().size() > 0)
			for(TIntObjectIterator<Skill> iterator = template.getSkills().iterator(); iterator.hasNext();)
			{
				iterator.advance();
				addSkill(iterator.value());
			}

		setXYZ(owner.getX() + Rnd.get(-100, 100), owner.getY() + Rnd.get(-100, 100), owner.getZ());
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_spawnAnimation = 0;

		Player owner = getPlayer();
		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowAdd(this));
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public SummonAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = new SummonAI(this);
			}

		return (SummonAI) _ai;
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) _template;
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getSummonType();

	public abstract int getEffectIdentifier();

	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}

	@Override
	public void onAction(final Player player, boolean shift)
	{
		if(isFrozen())
		{
			player.sendPacket(ActionFail.STATIC);
			return;
		}

		if(Events.onAction(player, this, shift))
		{
			player.sendPacket(ActionFail.STATIC);
			return;
		}

		Player owner = getPlayer();

		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
				player.sendPacket(new MyTargetSelected(getObjectId(), 0), makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP, StatusUpdate.CUR_MP, StatusUpdate.MAX_MP));
			else
				player.sendPacket(ActionFail.STATIC);
		}
		else if(player == owner)
		{
			player.sendPacket(new PetInfo(this).update());

			if(!player.isActionsDisabled())
				player.sendPacket(new PetStatusShow(this));

			player.sendPacket(ActionFail.STATIC);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
		{
			if(!shift)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.ALT_FOLLOW_RANGE);
			else
				player.sendActionFailed();
		}
		else
			player.sendActionFailed();
	}

	public long getExpForThisLevel()
	{
		return Experience.getExpForLevel(getLevel());
	}

	public long getExpForNextLevel()
	{
		return Experience.getExpForLevel(getLevel() + 1);
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	public final long getExp()
	{
		return _exp;
	}

	public final void setExp(final long exp)
	{
		_exp = exp;
	}

	public final int getSp()
	{
		return _sp;
	}

	public void setSp(final int sp)
	{
		_sp = sp;
	}

	@Override
	public int getMaxLoad()
	{
		return _maxLoad;
	}

	public void setMaxLoad(final int maxLoad)
	{
		_maxLoad = maxLoad;
	}

	@Override
	public int getBuffLimit()
	{
		Player owner = getPlayer();
		return (int) calcStat(Stats.BUFF_LIMIT, owner.getBuffLimit(), null, null);
	}

	@Override
	public int getSongLimit()
	{
		Player owner = getPlayer();
		return (int) calcStat(Stats.SONG_LIMIT, owner.getSongLimit(), null, null);
	}

	public abstract int getCurrentFed();

	public abstract int getMaxFed();

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		startDecay(8500L);

		Player owner = getPlayer();

		if(killer == null || killer == owner || killer == this || isInZoneBattle() || killer.isInZoneBattle())
			return;

		if(killer instanceof Summon)
			killer = killer.getPlayer();

		if(killer == null)
			return;

		if(killer.isPlayer())
		{
			Player pk = (Player) killer;

			if(isInZone(ZoneType.SIEGE))
				return;

			DuelEvent duelEvent = getEvent(DuelEvent.class);
			if(owner.getPvpFlag() > 0 || owner.atMutualWarWith(pk))
				pk.setPvpKills(pk.getPvpKills() + 1);
			else if((duelEvent == null || duelEvent != pk.getEvent(DuelEvent.class)) && getKarma() <= 0)
			{
				int pkCountMulti = Math.max(pk.getPkKills() / 2, 1);
				pk.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
			}

			// Send a Server->Client UserInfo packet to attacker with its PvP Kills Counter
			pk.sendChanges();
		}
	}

	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}

	protected void stopDecay()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}

	@Override
	protected void onDecay()
	{
		deleteMe();
	}

	public void endDecayTask()
	{
		stopDecay();
		doDecay();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;

		Player owner = getPlayer();

		sendStatusUpdate();

		StatusUpdate su = makeStatusUpdate(StatusUpdate.MAX_HP, StatusUpdate.CUR_HP);
		broadcastToStatusListeners(su);

		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowUpdate(this));
	}

	public void sendStatusUpdate()
	{
		Player owner = getPlayer();
		owner.sendPacket(new PetStatusUpdate(this));
	}

	@Override
	protected void onDelete()
	{
		Player owner = getPlayer();

		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
		owner.sendPacket(new PetDelete(getObjectId(), getSummonType()));
		owner.setPet(null);

		stopDecay();
		super.onDelete();
	}

	public void unSummon()
	{
		deleteMe();
	}

	public void saveEffects()
	{
		Player owner = getPlayer();
		if(owner == null)
			return;
		getEffectList().stopEffect(4515);//канцелим сумона при релоге на определенный скил
		if(owner.isInOlympiadMode())
			getEffectList().stopAllEffects();
		else//test
		EffectsDAO.getInstance().insert(this);
	}

	public void setFollowMode(boolean state)
	{
		Player owner = getPlayer();

		_follow = state;

		if(_follow)
		{
			if(getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.ALT_FOLLOW_RANGE);
		}
		else if(getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	public boolean isFollowMode()
	{
		return _follow;
	}

	private Future<?> _updateEffectIconsTask;

	private class UpdateEffectIcons extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

	@Override
	public void updateEffectIcons()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}

		if(_updateEffectIconsTask != null)
			return;

		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	public void updateEffectIconsImpl()
	{
		Player owner = getPlayer();
		PartySpelled ps = new PartySpelled(this, true);
		Party party = owner.getParty();
		if(party != null)
			party.broadCast(ps);
		else
			owner.sendPacket(ps);
	}

	public int getControlItemObjId()
	{
		return 0;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	@Override
	public void doPickupItem(final GameObject object)
	{}

	@Override
	public void doRevive()
	{
		super.doRevive();
		setRunning();
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		setFollowMode(true);
	}

	/**
	 * Return null.<BR><BR>
	 */
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
	public abstract void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic);

	@Override
	public abstract void displayReceiveDamageMessage(Creature attacker, int damage);

	@Override
	public boolean unChargeShots(final boolean spirit)
	{
		Player owner = getPlayer();

		if(spirit)
		{
			if(_spsCharged != 0)
			{
				_spsCharged = 0;
				owner.autoShot();
				return true;
			}
		}
		else if(_ssCharged)
		{
			_ssCharged = false;
			owner.autoShot();
			return true;
		}

		return false;
	}

	@Override
	public boolean getChargedSoulShot()
	{
		return _ssCharged;
	}

	@Override
	public int getChargedSpiritShot()
	{
		return _spsCharged;
	}

	public void chargeSoulShot()
	{
		_ssCharged = true;
	}

	public void chargeSpiritShot(final int state)
	{
		_spsCharged = state;
	}

	public int getSoulshotConsumeCount()
	{
		return getLevel() / 27 + 1;
	}

	public int getSpiritshotConsumeCount()
	{
		return getLevel() / 58 + 1;
	}

	public boolean isDepressed()
	{
		return _depressed;
	}

	public void setDepressed(final boolean depressed)
	{
		_depressed = depressed;
	}

	public boolean isInRange()
	{
		Player owner = getPlayer();
		return getDistance(owner) < SUMMON_DISAPPEAR_RANGE;
	}

	public void teleportToOwner()
	{
		Player owner = getPlayer();

		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		if(owner.isInOlympiadMode())
			teleToLocation(owner.getLoc(), owner.getReflection());
		else
			teleToLocation(Location.findPointToStay(owner, 50, 150), owner.getReflection());

		if(!isDead() && _follow)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.ALT_FOLLOW_RANGE);
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfo()
	{
		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	public void broadcastCharInfoImpl()
	{
		Player owner = getPlayer();

		for(Player player : World.getAroundPlayers(this))
			if(player == owner)
				player.sendPacket(new PetInfo(this).update());
			else
				player.sendPacket(new NpcInfo(this, player).update());
	}

	private Future<?> _petInfoTask;

	private class PetInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sendPetInfoImpl();
			_petInfoTask = null;
		}
	}

	private void sendPetInfoImpl()
	{
		Player owner = getPlayer();
		owner.sendPacket(new PetInfo(this).update());
	}

	public void sendPetInfo()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_petInfoTask != null)
			{
				_petInfoTask.cancel(false);
				_petInfoTask = null;
			}
			sendPetInfoImpl();
			return;
		}

		if(_petInfoTask != null)
			return;

		_petInfoTask = ThreadPoolManager.getInstance().schedule(new PetInfoTask(), Config.USER_INFO_INTERVAL);
	}

	/**
	 * Нужно для отображения анимации спауна, используется в пакете NpcInfo, PetInfo:
	 * 0=false, 1=true, 2=summoned (only works if model has a summon animation)
	 **/
	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	@Override
	public void startPvPFlag(Creature target)
	{
		Player owner = getPlayer();
		owner.startPvPFlag(target);
	}

	@Override
	public int getPvpFlag()
	{
		Player owner = getPlayer();
		return owner.getPvpFlag();
	}

	@Override
	public int getKarma()
	{
		Player owner = getPlayer();
		return owner.getKarma();
	}

	@Override
	public TeamType getTeam()
	{
		Player owner = getPlayer();
		return owner.getTeam();
	}

	@Override
	public Player getPlayer()
	{
		return _owner;
	}

	public abstract double getExpPenalty();

	@Override
	public SummonStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new SummonStatsChangeRecorder(this);
			}

		return (SummonStatsChangeRecorder) _statsRecorder;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		Player owner = getPlayer();

		if(owner == forPlayer)
		{
			list.add(new PetInfo(this));
			list.add(new PartySpelled(this, true));

			if(isPet())
				list.add(new PetItemList((PetInstance) this));
		}
		else
		{
			Party party = forPlayer.getParty();
			if(getReflection() == ReflectionManager.GIRAN_HARBOR && (owner == null || party == null || party != owner.getParty()))
				return list;
			list.add(new NpcInfo(this, forPlayer));
			if(owner != null && party != null && party == owner.getParty())
				list.add(new PartySpelled(this, true));
			list.add(RelationChanged.update(forPlayer, this, forPlayer));
		}

		if(isInCombat())
			list.add(new AutoAttackStart(getObjectId()));

		if(isMoving || isFollow)
			list.add(movePacket());
		return list;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		Player player = getPlayer();
		if(player != null)
			player.startAttackStanceTask0();
	}

	@Override
	public <E extends GlobalEvent> E getEvent(Class<E> eventClass)
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvent(eventClass);
		else
			return super.getEvent(eventClass);
	}

	@Override
	public Set<GlobalEvent> getEvents()
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvents();
		else
			return super.getEvents();
	}

	@Override
	public void sendReuseMessage(Skill skill)
	{
		Player player = getPlayer();
		if(player != null && isSkillDisabled(skill))
			player.sendPacket(SystemMsg.THAT_PET_SERVITOR_SKILL_CANNOT_BE_USED_BECAUSE_IT_IS_RECHARGING);
	}
}