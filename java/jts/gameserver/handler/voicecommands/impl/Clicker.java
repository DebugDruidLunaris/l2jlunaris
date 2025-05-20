package jts.gameserver.handler.voicecommands.impl;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.captcha.CaptchaValidator;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;

public class Clicker implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = { "clicker" };

	   
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.startsWith("clicker"))
			return clicker(activeChar);
		return false;
	}

	public boolean clicker(Player activeChar)
	{
		// check target
		if(activeChar.getTarget() == null)
		{
			activeChar.sendMessage("Нет цели.");
			return false;
		}
		// check if target is a L2Player
		if(!activeChar.getTarget().isPlayer())
		{
			activeChar.sendMessage("Цель должна быть игроком.");
			return false;
		}
		
		Player ptarget = (Player) activeChar.getTarget();

		// check if player target himself
		if(ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("Вы не можете использовать команду на себя.");
			return false;
		}
		
		if(activeChar.getPvpFlag() > 0 || activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("Вы не можете использовать команду пока вы флагнутый или ПК.");
			return false;
		}

		if(System.currentTimeMillis() - ptarget.getCharCaptchaTime() < Config.REUSE_COMMAND_TIME * 60000)
		{
			activeChar.sendMessage("Данный игрок недавно вводил каптчу.");
			return false;
		}
		
		if(!ptarget.isInCombat() || ptarget.getPvpFlag() > 0 || ptarget.isInOlympiadMode() || ptarget.isInDuel() || ptarget.isInPeaceZone()
			|| activeChar.isOnSiegeField()|| (activeChar.isInDeathMatch() | activeChar.isInCtF()| activeChar.isInTvT() | activeChar.isInLastHero() | activeChar.isInFightClub() | activeChar.isInTVTArena()))
		{
			activeChar.sendMessage("Вы не можете использовать команду на этого чара.");
			return false;
		}
		
		CaptchaValidator.getInstance().sendCaptcha(ptarget);
		ptarget.setCharCaptchaTime();		
		activeChar.sendMessage("Каптча успешно отправлена цели.");		
		return true;
	}

	   
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
