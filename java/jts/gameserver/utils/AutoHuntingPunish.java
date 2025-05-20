package jts.gameserver.utils;

/**
 * autor Norman
 *  02.04.2015
 */

public class AutoHuntingPunish
{
	private Punish _AutoHuntingPunishPunishment;
	private long _punishTime;
	private int _punishDuration;
	
	public enum Punish
	{
		CHATBAN,
		MOVEBAN,
		PARTYBAN,
		ACTIONBAN
	}
	
	public AutoHuntingPunish(Punish punish, int mins)
	{
		_AutoHuntingPunishPunishment = punish;
		_punishTime = System.currentTimeMillis() + ( mins * 60 * 1000);
		_punishDuration = mins * 60;
	}
	
	public Punish getBotPunishType()
	{
		return _AutoHuntingPunishPunishment;
	}
	
	public long getPunishStarterTime()
	{
		return _punishTime;
	}
	
	/**
	 * Returns the duration (in seconds) of the applied
	 * punish
	 * @return int 
	 */
	public int getDuration()
	{
		return _punishDuration;
	}
	
	/**
	 * Return the time left to end up this punish
	 * @return long
	 */
	public long getPunishTimeLeft()
	{
		long left = System.currentTimeMillis() - _punishTime;
		return left;
	}
	
	/**
	 * @return true if the player punishment has
	 * expired 
	 */
	public boolean canWalk()
	{
		if(_AutoHuntingPunishPunishment == Punish.MOVEBAN
				&& System.currentTimeMillis() - _punishTime <= 0)
			return false;
		return true;
	}
	
	/**
	 * @return true if the player punishment has
	 * expired 
	 */
	public boolean canTalk()
	{
		if(_AutoHuntingPunishPunishment == Punish.CHATBAN
				&& System.currentTimeMillis() - _punishTime <= 0)
			return false;
		return true;
	}
	
	/**
	 * @return true if the player punishment has
	 * expired 
	 */
	public boolean canJoinParty()
	{
		if(_AutoHuntingPunishPunishment == Punish.PARTYBAN
				&& System.currentTimeMillis() - _punishTime <= 0)
			return false;
		return true;
	}
	
	/**
	 * @return true if the player punishment has
	 * expired 
	 */
	public boolean canPerformAction()
	{
		if(_AutoHuntingPunishPunishment == Punish.ACTIONBAN
				&& System.currentTimeMillis() - _punishTime <= 0)
			return false;
		return true;
	}
}