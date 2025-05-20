package jts.gameserver.taskmanager.actionrunner.tasks;

import jts.gameserver.model.entity.olympiad.OlympiadDatabase;

public class OlympiadSaveTask extends AutomaticTask
{
	public OlympiadSaveTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		//_log.info("OlympiadSaveTask: data save started.");
		OlympiadDatabase.save();
		//_log.info("OlympiadSaveTask: data save ended in time: " + (System.currentTimeMillis() - t) + " ms.");
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + 600000L;
	}
}