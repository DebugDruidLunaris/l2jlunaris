package jts.gameserver.captcha;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;

public class CaptchaValidator
{

	private static CaptchaValidator instance = null;

	private ICaptcha _captchaEngine;

	public static CaptchaValidator getInstance()
	{
		if(instance == null)
			instance = new CaptchaValidator();
		return instance;
	}

	private CaptchaValidator()
	{
		if(Config.CAPTCHA_ENABLE || Config.CAPTCHA_COMMAND_ENABLE)
			try
			{
				if(Config.CAPTCHA_TYPE.equalsIgnoreCase("IMAGE"))
					_captchaEngine = new ImageCaptcha();
				else
					_captchaEngine = new ASCIICaptcha();
			}
			catch(final Exception e)
			{
			}
		else
			_captchaEngine = null;
	}

	private static void sendNextPage(final Player activeChar, final boolean actionPerformed)
	{
		if(actionPerformed)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("common/captcha_successful.htm");
			activeChar.resetCaptchaAtemptRequest();
			activeChar.sendPacket(html);
			activeChar.unblock();
			activeChar.unCaptchaChatBlock();
		}
	}

	public void sendCaptcha(final Player activeChar)
	{
		if(!Config.CAPTCHA_ENABLE || !Config.CAPTCHA_SHOW_PLAYERS_WITH_PA && activeChar.hasBonus())
		{
			sendNextPage(activeChar, false);
			return;
		}

		_captchaEngine.sendCaptchaPage(activeChar);

		if(activeChar.getSchedulePlayerCaptchaValidation() != null)
			activeChar.resetSchedulePlayerCaptchaValidation();
		activeChar.setSchedulePlayerCaptchaValidation();
	}
	

	public static void processCaptchaBypass(final String command, final Player player)
	{
		if(command.length() <= 9)
		{
			kickPlayer(player);
		}
		else
		{
			final String playercaptcha = command.substring(9);
			if(player.getCaptcha().compareTo(playercaptcha) == 0)
			{
				player.setCaptcha("");
				player.block();
                player.setCaptchaCountError(0);
				sendNextPage(player, true);
			}
			else
			{
                if(player.getCaptchaCountError() >= (Config.CAPTCHA_COUNT_ERROR - 1))
                {
                	kickPlayer(player);
                }
                else
                {
                    player.setCaptchaCountError(player.getCaptchaCountError() + 1);
                    CaptchaValidator.getInstance().sendCaptcha(player);
                }
			}
		}
	}


public static void kickPlayer(final Player player)
{
	player.logout();
}
}