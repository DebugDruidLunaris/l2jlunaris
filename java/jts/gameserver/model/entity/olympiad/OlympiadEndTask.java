package jts.gameserver.model.entity.olympiad;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadEndTask extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadEndTask.class);

	@Override
	public void runImpl() throws Exception
	{
		if(Olympiad._inCompPeriod) // Если бои еще не закончились, откладываем окончание олимпиады на минуту
		{
			ThreadPoolManager.getInstance().schedule(new OlympiadEndTask(), 10);//TODO Нужно проверить на RPG так как из за сия идет 2 подсчет валидации
			return;
		}

		Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_ENDED).addNumber(Olympiad._currentCycle));
		DifferentMethods.sayToAll("jts.gameserver.model.entity.olympiad.OlympiadEndTask", null);

		Olympiad._isOlympiadEnd = true;
		if(Olympiad._scheduledManagerTask != null)
			Olympiad._scheduledManagerTask.cancel(false);
		if(Olympiad._scheduledWeeklyTask != null)
			Olympiad._scheduledWeeklyTask.cancel(false);

		Olympiad._validationEnd = Olympiad._olympiadEnd + Config.OLYMPIAD_VPERIOD;


		OlympiadDatabase.saveNobleData();
		Olympiad._period = 1;
		Hero.getInstance().clearHeroes();

		try
		{
			OlympiadDatabase.save();
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Failed to save Olympiad configuration!", e);
		}

		_log.info("Olympiad System: Starting Validation period. Time to end validation:" + Olympiad.getMillisToValidationEnd() / (60 * 1000));

		if(Olympiad._scheduledValdationTask != null)
			Olympiad._scheduledValdationTask.cancel(false);
		Olympiad._scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), Olympiad.getMillisToValidationEnd());
	}
}