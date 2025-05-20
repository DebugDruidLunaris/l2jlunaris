package jts.gameserver.captcha;

import java.io.File;
import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.commons.util.Rnd;
import de.jave.figlet.Figlet;


public class ASCIICaptcha implements ICaptcha
{

	private final File dir = new File(Config.DATAPACK_ROOT, "data/fonts");
	private final Figlet figlet;
	private final String[] fonts;

	public ASCIICaptcha() throws Exception
	{
		figlet = new Figlet(dir);
		fonts = figlet.getFileLibrary().getAllFontNames();

	}

	@Override
	public void sendCaptchaPage(final Player activeChar)
	{
		NpcHtmlMessage captchapage = new NpcHtmlMessage(0);
		captchapage.setFile("common/captcha_ascii.htm");
		captchapage.replace("%captchatime%", Config.CAPTCHA_TIME + "");
        captchapage.replace("%countCaptcha%", Integer.toString(Config.CAPTCHA_COUNT_ERROR - activeChar.getCaptchaCountError()));


		String cap1 = String.valueOf(Rnd.get(10));
		String captcha1 = stringToRandomASCII(cap1);

		String cap2 = String.valueOf(Rnd.get(10));
		String captcha2 = stringToRandomASCII(cap2);

		captchapage.replace("%captcha1%", captcha1);
		captchapage.replace("%captcha2%", captcha2);
		activeChar.sendPacket(captchapage);
		String captcha = cap1 + cap2;
		activeChar.setCaptcha(captcha);
	}

	public String stringToRandomASCII(String source)
	{
		// выбираем шрифт
		String fontName = fonts[Rnd.get(fonts.length)];
		// делаем ASCII рисунок
		String result = null;
		try
		{
			result = figlet.figletize(source, fontName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		result = result.replace("\n\n", "");
		StringTokenizer st = new StringTokenizer(result, "\n", false);
		String result2 = "<table border=0 cellspacing=0 cellpadding=0>\n";
		while (st.hasMoreTokens())
		{
			result2 += " <tr>";
			String row = st.nextToken();
			for(int i = 0; i < row.length(); i++)
			{
				result2 += "<td>" + row.substring(i, i + 1) + "</td>";
			}
			result2 += "</tr>\n";

		}
		result2 += "</table>";
		return result2;
	}
}
