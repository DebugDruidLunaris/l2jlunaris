package jts.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import jts.commons.collections.MultiValueSet;
import jts.commons.threading.RunnableImpl;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.entity.SevenSigns;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.entity.events.objects.SiegeClanObject;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.entity.residence.Dominion;
import jts.gameserver.model.pledge.UnitMember;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class DominionSiegeRunnerEvent extends GlobalEvent
{
	public static final String BATTLEFIELD = "battlefield";
	public static final String REGISTRATION = "registration";
	private BattlefieldChatTask _battlefieldChatTask = new BattlefieldChatTask();
	private Calendar _startTime = Calendar.getInstance();
	private List<Dominion> _registeredDominions = new ArrayList<Dominion>(9);
	private Map<ClassId, Quest> _classQuests = new HashMap<ClassId, Quest>();
	private List<Quest> _breakQuests = new ArrayList<Quest>();
	private boolean _battlefieldChatActive;
	private Future<?> _battlefieldChatFuture;
	private boolean _isInProgress;
	private boolean _isRegistrationOver;

	public DominionSiegeRunnerEvent(MultiValueSet<String> set)
	{
		super(set);

		_startTime.setTimeInMillis(0);
	}

	@Override
	public void startEvent()
	{
		if(_startTime.getTimeInMillis() == 0)
		{
			clearActions();
			return;
		}

		super.startEvent();
		setInProgress(true);

		if(_battlefieldChatFuture != null)
		{
			_battlefieldChatFuture.cancel(false);

			_battlefieldChatFuture = null;
		}

		// проверка на 2х реги, и чиста ревардов от другой територии, если зареган был
		for(Dominion d : _registeredDominions)
		{
			List<SiegeClanObject> defenders = d.getSiegeEvent().getObjects(DominionSiegeEvent.DEFENDERS);

			for(SiegeClanObject siegeClan : defenders)
			{
				// листаем мемберов от клана
				for(UnitMember member : siegeClan.getClan())
				{
					for(Dominion d2 : _registeredDominions)
					{
						DominionSiegeEvent siegeEvent2 = d2.getSiegeEvent();
						List<Integer> defenderPlayers2 = siegeEvent2.getObjects(DominionSiegeEvent.DEFENDER_PLAYERS);

						defenderPlayers2.remove(Integer.valueOf(member.getObjectId()));

						// с базы удалять ненужно, удалит познее
						// SiegePlayerDAO.getInstance().delete(d, 0, a);
						// если у игрока есть реварды от другой з територии - обнуляем
						if(d != d2)
						{
							siegeEvent2.clearReward(member.getObjectId());
						}
					}
				}
			}

			List<Integer> defenderPlayers = d.getSiegeEvent().getObjects(DominionSiegeEvent.DEFENDER_PLAYERS);

			for(int i : defenderPlayers)
			{
				for(Dominion d2 : _registeredDominions)
				{
					DominionSiegeEvent siegeEvent2 = d2.getSiegeEvent();

					// если у игрока есть реварды от другой з територии - обнуляем
					if(d != d2)
					{
						siegeEvent2.clearReward(i);
					}
				}
			}
		}

		// ненужно поднимать выше
		for(Dominion d : _registeredDominions)
		{
			d.getSiegeEvent().clearActions();
			d.getSiegeEvent().registerActions();
		}
		broadcastToWorld(SystemMsg.TERRITORY_WAR_HAS_BEGUN);
	}

	@Override
	public void stopEvent()
	{
		setInProgress(false);
		reCalcNextTime(false);

		for(Dominion d : _registeredDominions)
		{
			d.getSiegeDate().setTimeInMillis(_startTime.getTimeInMillis());
		}
		broadcastToWorld(SystemMsg.TERRITORY_WAR_HAS_ENDED);

		_battlefieldChatFuture = ThreadPoolManager.getInstance().schedule(_battlefieldChatTask, 600000L);

		super.stopEvent();
	}

	@Override
	public void announce(int val)
	{
		switch(val)
		{
			case -20:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_WILL_BEGIN_IN_20_MINUTES);
				break;
			case -10:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_10_MINUTES);
				break;
			case -5:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_5_MINUTES);
				break;
			case -1:
				broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_1_MINUTE);
				break;
			case 3600:
				broadcastToWorld(new SystemMessage2(SystemMsg.THE_TERRITORY_WAR_WILL_END_IN_S1HOURS).addInteger(val / 3600));
				break;
			case 600:
			case 300:
			case 60:
				broadcastToWorld(new SystemMessage2(SystemMsg.THE_TERRITORY_WAR_WILL_END_IN_S1MINUTES).addInteger(val / 60));
				break;
			case 10:
			case 5:
			case 4:
			case 3:
			case 2:
			case 1:
				broadcastToWorld(new SystemMessage2(SystemMsg.S1_SECONDS_TO_THE_END_OF_TERRITORY_WAR).addInteger(val));
				break;
		}
	}

	public Calendar getSiegeDate()
	{
		return _startTime;
	}
    /**
     * Валидация времени ТВ и синхрон. с периодом Семи Печатей	//TODO Test
     */
    private void validateSiegeDate() {
        while (System.currentTimeMillis() > _startTime.getTimeInMillis()) {
            _startTime.add(Calendar.DAY_OF_MONTH, Config.TW_SIEGE_DAY);
        }

        // синхронизируем с периодом Семи Печатей
        if (!SevenSigns.getInstance().isDateInSealValidPeriod(_startTime) && Config.TW_SIEGE_DAY == 14) {
            _startTime.add(Calendar.DAY_OF_MONTH, 7);
        }
    }
    @Override
    public void reCalcNextTime(boolean onInit) {
        clearActions();

        if (onInit) {
            if (_startTime.getTimeInMillis() > 0) {
                registerActions();
            }
        } else {
            if (_startTime.getTimeInMillis() > 0) {
                validateSiegeDate();
                registerActions();
            }
        }
    }

	@Override
	protected long startTimeMillis()
	{
		return _startTime.getTimeInMillis();
	}

	@Override
	protected void printInfo() {}

	// ========================================================================================================================================================================
	// Broadcast
	// ========================================================================================================================================================================
	public void broadcastTo(L2GameServerPacket packet)
	{
		for(Dominion dominion : _registeredDominions)
		{
			dominion.getSiegeEvent().broadcastTo(packet);
		}
	}

	// ========================================================================================================================================================================
	// Getters/Setters
	// ========================================================================================================================================================================
	public boolean isBattlefieldChatActive()
	{
		return _battlefieldChatActive;
	}

	public void setBattlefieldChatActive(boolean battlefieldChatActive)
	{
		_battlefieldChatActive = battlefieldChatActive;
	}

	@Override
	public boolean isInProgress()
	{
		return _isInProgress;
	}

	public void setInProgress(boolean inProgress)
	{
		_isInProgress = inProgress;
	}

	public boolean isRegistrationOver()
	{
		return _isRegistrationOver;
	}

	public void setRegistrationOver(boolean registrationOver)
	{
		_isRegistrationOver = registrationOver;

		for(Dominion d : _registeredDominions)
		{
			d.getSiegeEvent().setRegistrationOver(registrationOver);
		}

		if(registrationOver)
		{
			broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_REQUEST_PERIOD_HAS_ENDED);
		}
	}

	public void addClassQuest(ClassId c, Quest quest)
	{
		_classQuests.put(c, quest);
	}

	public Quest getClassQuest(ClassId c)
	{
		return _classQuests.get(c);
	}

	public void addBreakQuest(Quest q)
	{
		_breakQuests.add(q);
	}

	public List<Quest> getBreakQuests()
	{
		return _breakQuests;
	}

	// ========================================================================================================================================================================
	// Overrides GlobalEvent
	// ========================================================================================================================================================================
	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(REGISTRATION))
		{
			setRegistrationOver(!start);
		}
		else if(name.equalsIgnoreCase(BATTLEFIELD))
		{
			setBattlefieldChatActive(start);
		}
		else
		{
			super.action(name, start);
		}
	}

    public synchronized void registerDominion(Dominion d) {
        if (_registeredDominions.contains(d)) {
            return;
        }

        if (_registeredDominions.isEmpty()) {
            Castle castle = d.getCastle();
            if (castle.getOwnDate().getTimeInMillis() == 0) {
                return;
            }

            validateSiegeDate();

            _startTime.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            _startTime.set(Calendar.HOUR_OF_DAY, 20);
            _startTime.set(Calendar.MINUTE, 0);
            _startTime.set(Calendar.SECOND, 0);
            _startTime.set(Calendar.MILLISECOND, 0);

            d.getSiegeDate().setTimeInMillis(_startTime.getTimeInMillis());
            reCalcNextTime(false);
        } else {
            d.getSiegeDate().setTimeInMillis(_startTime.getTimeInMillis());
        }

        d.getSiegeEvent().spawnAction(DominionSiegeEvent.TERRITORY_NPC, true);
        d.rewardSkills();

        _registeredDominions.add(d);
    }

	public synchronized void unRegisterDominion(Dominion d)
	{
		if(!_registeredDominions.contains(d))
			return;

		_registeredDominions.remove(d);
		d.getSiegeEvent().spawnAction(DominionSiegeEvent.TERRITORY_NPC, false);
		d.getSiegeDate().setTimeInMillis(0);

		if(_registeredDominions.isEmpty())
		{
			clearActions();
			_startTime.setTimeInMillis(0);
			reCalcNextTime(false);
		}
	}

	public List<Dominion> getRegisteredDominions()
	{
		return _registeredDominions;
	}

	private class BattlefieldChatTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			setBattlefieldChatActive(false);
			setRegistrationOver(false);

			for(Dominion d : _registeredDominions)
			{
				DominionSiegeEvent siegeEvent = d.getSiegeEvent();

				siegeEvent.updateParticles(false);
				siegeEvent.broadcastTo(SystemMsg.THE_BATTLEFIELD_CHANNEL_HAS_BEEN_DEACTIVATED);
				siegeEvent.removeObjects(DominionSiegeEvent.ATTACKERS);
				siegeEvent.removeObjects(DominionSiegeEvent.DEFENDERS);
				siegeEvent.removeObjects(DominionSiegeEvent.ATTACKER_PLAYERS);
				siegeEvent.removeObjects(DominionSiegeEvent.DEFENDER_PLAYERS);
			}

			_battlefieldChatFuture = null;
		}
	}
}