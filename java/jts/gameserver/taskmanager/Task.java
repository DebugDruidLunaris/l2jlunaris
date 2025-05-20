package jts.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;

import jts.gameserver.taskmanager.TaskManager.ExecutedTask;

public abstract class Task
{
	public abstract void initializate();

	public ScheduledFuture<?> launchSpecial(ExecutedTask instance)
	{
		return null;
	}

	public abstract String getName();

	public abstract void onTimeElapsed(ExecutedTask task);

	public void onDestroy()
	{}
}