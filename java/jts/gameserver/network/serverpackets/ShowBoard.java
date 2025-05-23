package jts.gameserver.network.serverpackets;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jts.gameserver.Config;
import jts.gameserver.cache.ImagesCache;
import jts.gameserver.data.StringHolder;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.model.Player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowBoard extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ShowBoard.class);
	private String _htmlCode;
	private String _id;
	private List<String> _arg;
	private String _addFav = "";

	public static void separateAndSend(String html, Player player)
	{
		if(html.length() < 8180)
		{
			player.sendPacket(new ShowBoard(html, "101", player));
			player.sendPacket(new ShowBoard(null, "102", player));
			player.sendPacket(new ShowBoard(null, "103", player));
		}
		else if(html.length() < 8180 * 2)
		{
			player.sendPacket(new ShowBoard(html.substring(0, 8180), "101", player));
			player.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102", player));
			player.sendPacket(new ShowBoard(null, "103", player));
		}
		else if(html.length() < 8180 * 3)
		{
			player.sendPacket(new ShowBoard(html.substring(0, 8180), "101", player));
			player.sendPacket(new ShowBoard(html.substring(8180, 8180 * 2), "102", player));
			player.sendPacket(new ShowBoard(html.substring(8180 * 2, html.length()), "103", player));
		}
	}

	public static void send1001(String html, Player activeChar)
	{
		if(html.length() < 8180)
			activeChar.sendPacket(new ShowBoard(html, "1001", activeChar));
	}

	public ShowBoard(String htmlCode, String id, Player player)
	{
		if(htmlCode != null && htmlCode.length() > 8192) // html code must not exceed 8192 bytes
		{
			_log.warn("Html '" + htmlCode + "' is too long! this will crash the client!");
			_htmlCode = "<html><body>Html was too long</body></html>";
			return;
		}
		_id = id;

		if(player.getSessionVar("add_fav") != null)
			_addFav = "bypass _bbsaddfav_List";

		if(htmlCode != null)
		{
			if(id.equalsIgnoreCase("101"))
				player.cleanBypasses(true);
			
			_htmlCode = player.encodeBypasses(htmlCode, true);
			_htmlCode = _htmlCode.replace("\t", "");
			
			Matcher m = ImagesCache.HTML_PATTERN.matcher(_htmlCode);
			while(m.find())
			{
				String imageName = m.group(1);
				int imageId = ImagesCache.getInstance().getImageId(imageName);
				_htmlCode = _htmlCode.replaceAll("\\%image\\(" + imageName + "\\)\\%", "Crest.crest_" + Config.REQUEST_ID + "_" + imageId);
				byte[] image = ImagesCache.getInstance().getImage(imageId);
				if(image != null)
					player.sendPacket(new PledgeCrest(imageId, image));
			}

			// include шаблонов
			String path_file_community = "scripts/services/community/" + Config.BBS_FOLDER + "/";
			Pattern p = Pattern.compile("\\%include\\(([^\\)]+)\\)\\%");
			m = p.matcher(_htmlCode);
			while(m.find())
			{
				if(new File(Config.DATAPACK_ROOT, "data/html-" + player.getLang() + "/" + path_file_community + m.group(1)).exists() == false)
				{
					_htmlCode = _htmlCode.replace(m.group(0), "<font color=\"FF0000\">Страница не найдена</font>");
				}
				else
				{
					_htmlCode = _htmlCode.replace(m.group(0), HtmCache.getInstance().getNotNull(path_file_community + m.group(1), player));
				}
			}

			// string в шаблонов
			Pattern ps = Pattern.compile("\\%string\\(([^\\)]+)\\)\\%");
			Matcher ms = ps.matcher(_htmlCode);
			while(ms.find())
			{
				_htmlCode = _htmlCode.replace(ms.group(0), StringHolder.getInstance().getNotNull(player, ms.group(1)));
			}

			// include class object и преобразование типа данных
			// %object(Config,BBS_FOLDER)%
			p = Pattern.compile("\\%object\\(([^\\)]+),([^\\)]+)\\)\\%");
			m = p.matcher(_htmlCode);
			Object c = null;
			while(m.find())
			{
				// Если свойство Config - сразу вытягиваем значение
				if(m.group(1).equals("Con" + "fig"))
				{
					_htmlCode = _htmlCode.replace(m.group(0), Config.getField(m.group(2)));
					// если метод не предопределен, создаем экземляр класса и обращаемся к свойству
				}
				else
				{
					try
					{
						c = Class.forName(m.group(1)).newInstance();
						Field field = c.getClass().getField(m.group(2));
						_htmlCode = _htmlCode.replace(m.group(0), field.get(c).toString());
					}
					catch(InstantiationException e)
					{
						e.printStackTrace();
					}
					catch(IllegalAccessException e)
					{
						e.printStackTrace();
					}
					catch(ClassNotFoundException e)
					{
						e.printStackTrace();
					}
					catch(NoSuchFieldException e)
					{
						e.printStackTrace();
					}
					catch(SecurityException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else
			_htmlCode = null;
	}

	public ShowBoard(List<String> arg)
	{
		_id = "1002";
		_htmlCode = null;
		_arg = arg;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x7b);
		writeC(0x01); //c4 1 to show community 00 to hide
		writeS("bypass _bbshome");
		writeS("bypass _bbsgetfav");
		writeS("bypass _bbsloc");
		writeS("bypass _bbsclan");
		writeS("bypass _bbsmemo");
		writeS("bypass _maillist_0_1_0_");
		writeS("bypass _friendlist_0_");
		writeS(_addFav);
		String str = _id + "\u0008";
		if(!_id.equals("1002"))
		{
			if(_htmlCode != null)
				str += _htmlCode;
		}
		else
			for(String arg : _arg)
				str += arg + " \u0008";
		writeS(str);
	}
}