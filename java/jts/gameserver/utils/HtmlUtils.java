package jts.gameserver.utils;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.network.serverpackets.components.SysString;

public class HtmlUtils
{
	public static final String PREV_BUTTON = "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
	public static final String NEXT_BUTTON = "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";

	public static String htmlResidenceName(int id)
	{
		return "&%" + id + ";";
	}

	public static String htmlNpcName(int npcId)
	{
		return "&@" + npcId + ";";
	}

	public static String htmlSysString(SysString sysString)
	{
		return htmlSysString(sysString.getId());
	}

	public static String htmlSysString(int id)
	{
		return "&$" + id + ";";
	}

	public static String htmlItemName(int itemId)
	{
		return "&#" + itemId + ";";
	}

	public static String htmlClassName(int classId)
	{
		return "<ClassId>" + classId + "</ClassId>";
	}

	// Список профессии для вывода на всех языках методам посылки пакета CustomMessage.
	public static CustomMessage htmlClassNameNonClient(Player player, int classId)
	{
		switch(classId)
		{
			case 0:
				return new CustomMessage("utils.classId.name.0", player);
			case 1:
				return new CustomMessage("utils.classId.name.1", player);
			case 2:
				return new CustomMessage("utils.classId.name.2", player);
			case 3:
				return new CustomMessage("utils.classId.name.3", player);
			case 4:
				return new CustomMessage("utils.classId.name.4", player);
			case 5:
				return new CustomMessage("utils.classId.name.5", player);
			case 6:
				return new CustomMessage("utils.classId.name.6", player);
			case 7:
				return new CustomMessage("utils.classId.name.7", player);
			case 8:
				return new CustomMessage("utils.classId.name.8", player);
			case 9:
				return new CustomMessage("utils.classId.name.9", player);
			case 10:
				return new CustomMessage("utils.classId.name.10", player);
			case 11:
				return new CustomMessage("utils.classId.name.11", player);
			case 12:
				return new CustomMessage("utils.classId.name.12", player);
			case 13:
				return new CustomMessage("utils.classId.name.13", player);
			case 14:
				return new CustomMessage("utils.classId.name.14", player);
			case 15:
				return new CustomMessage("utils.classId.name.15", player);
			case 16:
				return new CustomMessage("utils.classId.name.16", player);
			case 17:
				return new CustomMessage("utils.classId.name.17", player);
			case 18:
				return new CustomMessage("utils.classId.name.18", player);
			case 19:
				return new CustomMessage("utils.classId.name.19", player);
			case 20:
				return new CustomMessage("utils.classId.name.20", player);
			case 21:
				return new CustomMessage("utils.classId.name.21", player);
			case 22:
				return new CustomMessage("utils.classId.name.22", player);
			case 23:
				return new CustomMessage("utils.classId.name.23", player);
			case 24:
				return new CustomMessage("utils.classId.name.24", player);
			case 25:
				return new CustomMessage("utils.classId.name.25", player);
			case 26:
				return new CustomMessage("utils.classId.name.26", player);
			case 27:
				return new CustomMessage("utils.classId.name.27", player);
			case 28:
				return new CustomMessage("utils.classId.name.28", player);
			case 29:
				return new CustomMessage("utils.classId.name.29", player);
			case 30:
				return new CustomMessage("utils.classId.name.30", player);
			case 31:
				return new CustomMessage("utils.classId.name.31", player);
			case 32:
				return new CustomMessage("utils.classId.name.32", player);
			case 33:
				return new CustomMessage("utils.classId.name.33", player);
			case 34:
				return new CustomMessage("utils.classId.name.34", player);
			case 35:
				return new CustomMessage("utils.classId.name.35", player);
			case 36:
				return new CustomMessage("utils.classId.name.36", player);
			case 37:
				return new CustomMessage("utils.classId.name.37", player);
			case 38:
				return new CustomMessage("utils.classId.name.38", player);
			case 39:
				return new CustomMessage("utils.classId.name.39", player);
			case 40:
				return new CustomMessage("utils.classId.name.40", player);
			case 41:
				return new CustomMessage("utils.classId.name.41", player);
			case 42:
				return new CustomMessage("utils.classId.name.42", player);
			case 43:
				return new CustomMessage("utils.classId.name.43", player);
			case 44:
				return new CustomMessage("utils.classId.name.44", player);
			case 45:
				return new CustomMessage("utils.classId.name.45", player);
			case 46:
				return new CustomMessage("utils.classId.name.46", player);
			case 47:
				return new CustomMessage("utils.classId.name.47", player);
			case 48:
				return new CustomMessage("utils.classId.name.48", player);
			case 49:
				return new CustomMessage("utils.classId.name.49", player);
			case 50:
				return new CustomMessage("utils.classId.name.50", player);
			case 51:
				return new CustomMessage("utils.classId.name.51", player);
			case 52:
				return new CustomMessage("utils.classId.name.52", player);
			case 53:
				return new CustomMessage("utils.classId.name.53", player);
			case 54:
				return new CustomMessage("utils.classId.name.54", player);
			case 55:
				return new CustomMessage("utils.classId.name.55", player);
			case 56:
				return new CustomMessage("utils.classId.name.56", player);
			case 57:
				return new CustomMessage("utils.classId.name.57", player);
			case 88:
				return new CustomMessage("utils.classId.name.88", player);
			case 89:
				return new CustomMessage("utils.classId.name.89", player);
			case 90:
				return new CustomMessage("utils.classId.name.90", player);
			case 91:
				return new CustomMessage("utils.classId.name.91", player);
			case 92:
				return new CustomMessage("utils.classId.name.92", player);
			case 93:
				return new CustomMessage("utils.classId.name.93", player);
			case 94:
				return new CustomMessage("utils.classId.name.94", player);
			case 95:
				return new CustomMessage("utils.classId.name.95", player);
			case 96:
				return new CustomMessage("utils.classId.name.96", player);
			case 97:
				return new CustomMessage("utils.classId.name.97", player);
			case 98:
				return new CustomMessage("utils.classId.name.98", player);
			case 99:
				return new CustomMessage("utils.classId.name.99", player);
			case 100:
				return new CustomMessage("utils.classId.name.100", player);
			case 101:
				return new CustomMessage("utils.classId.name.101", player);
			case 102:
				return new CustomMessage("utils.classId.name.102", player);
			case 103:
				return new CustomMessage("utils.classId.name.103", player);
			case 104:
				return new CustomMessage("utils.classId.name.104", player);
			case 105:
				return new CustomMessage("utils.classId.name.105", player);
			case 106:
				return new CustomMessage("utils.classId.name.106", player);
			case 107:
				return new CustomMessage("utils.classId.name.107", player);
			case 108:
				return new CustomMessage("utils.classId.name.108", player);
			case 109:
				return new CustomMessage("utils.classId.name.109", player);
			case 110:
				return new CustomMessage("utils.classId.name.110", player);
			case 111:
				return new CustomMessage("utils.classId.name.111", player);
			case 112:
				return new CustomMessage("utils.classId.name.112", player);
			case 113:
				return new CustomMessage("utils.classId.name.113", player);
			case 114:
				return new CustomMessage("utils.classId.name.114", player);
			case 115:
				return new CustomMessage("utils.classId.name.115", player);
			case 116:
				return new CustomMessage("utils.classId.name.116", player);
			case 117:
				return new CustomMessage("utils.classId.name.117", player);
			case 118:
				return new CustomMessage("utils.classId.name.118", player);
			case 123:
				return new CustomMessage("utils.classId.name.123", player);
			case 124:
				return new CustomMessage("utils.classId.name.124", player);
			case 125:
				return new CustomMessage("utils.classId.name.125", player);
			case 126:
				return new CustomMessage("utils.classId.name.126", player);
			case 127:
				return new CustomMessage("utils.classId.name.127", player);
			case 128:
				return new CustomMessage("utils.classId.name.128", player);
			case 129:
				return new CustomMessage("utils.classId.name.129", player);
			case 130:
				return new CustomMessage("utils.classId.name.130", player);
			case 131:
				return new CustomMessage("utils.classId.name.131", player);
			case 132:
				return new CustomMessage("utils.classId.name.132", player);
			case 133:
				return new CustomMessage("utils.classId.name.133", player);
			case 134:
				return new CustomMessage("utils.classId.name.134", player);
			case 135:
				return new CustomMessage("utils.classId.name.135", player);
			case 136:
				return new CustomMessage("utils.classId.name.136", player);
			default:
				return new CustomMessage("utils.classId.name.default", player);
		}
	}

	public static String htmlNpcString(NpcString id, Object... params)
	{
		return htmlNpcString(id.getId(), params);
	}

	public static String htmlNpcString(int id, Object... params)
	{
		String replace = "<fstring";
		if(params.length > 0)
			for(int i = 0; i < params.length; i++)
				replace += " p" + (i + 1) + "=\"" + String.valueOf(params[i]) + "\"";
		replace += ">" + id + "</fstring>";
		return replace;
	}

	public static String htmlButton(String value, String action, int width)
	{
		return htmlButton(value, action, width, 22);
	}
	
	/**
	 * Статический метод, для вызова из любых мест
	 * @return 
	 */
	public static String show(String text, Player self, NpcInstance npc, Object... arg)
	{
		if(text == null || self == null)
			return text;

		NpcHtmlMessage msg = new NpcHtmlMessage(self, npc);

		// приводим нашу html-ку в нужный вид
		if(text.endsWith(".html") || text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(Strings.bbParse(text));

		if(arg != null && arg.length % 2 == 0)
			for(int i = 0; i < arg.length; i = +2)
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));

		self.sendPacket(msg);
		return text;
	}
	
	public static String show(CustomMessage message, Player self)
	{
		return show(message.toString(), self, null);
	}
	
	public static String htmlButton(String value, String action, int width, int height)
	{
		return String.format("<button value=\"%s\" action=\"%s\" back=\"L2UI_CT1.Button_DF_Small_Down\" width=%d height=%d fore=\"L2UI_CT1.Button_DF_Small\">", value, action, width, height);
	}
}