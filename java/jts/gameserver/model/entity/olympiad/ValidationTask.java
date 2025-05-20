package jts.gameserver.model.entity.olympiad;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.instancemanager.OlympiadHistoryManager;
import jts.gameserver.model.entity.Hero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationTask extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(ValidationTask.class);

	@Override
	public void runImpl() throws Exception
	{
		OlympiadHistoryManager.getInstance().switchData();

		OlympiadDatabase.sortHerosToBe();
		OlympiadDatabase.saveNobleData();
		if(Hero.getInstance().computeNewHeroes(Olympiad._heroesToBe))
			_log.warn("Olympiad: Error while computing new heroes!");
		DifferentMethods.sayToAll("jts.gameserver.model.entity.olympiad.ValidationTask", null); //удобно для тех кто меняет период проверки
		Olympiad._period = 0;
		Olympiad._currentCycle++;
		OlympiadDatabase.cleanupNobles();
		OlympiadDatabase.loadNoblesRank();
		OlympiadDatabase.setNewOlympiadEnd();
		Olympiad.init();
		OlympiadDatabase.save();
	}
}