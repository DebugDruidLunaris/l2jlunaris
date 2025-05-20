package npc.model.events;

import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.HtmlUtils;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Util;

import org.apache.commons.lang3.StringUtils;

public class FightClubManagerInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private static final String HTML_INDEX = "scripts/events/fightclub/index.htm";
	private static final String HTML_ACCEPT = "scripts/events/fightclub/accept.htm";
	private static final String HTML_MAKEBATTLE = "scripts/events/fightclub/makebattle.htm";
	private static final String HTML_INFO = "scripts/events/fightclub/info.htm";
	private static final String HTML_DISABLED = "scripts/events/fightclub/disabled.htm";
	private static final String HTML_LIST = "scripts/events/fightclub/fightslist.htm";
	private static final String HTML_RESULT = "scripts/events/fightclub/result.htm";

	public FightClubManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(!Config.FIGHT_CLUB_ENABLED)
		{
			showChatWindow(player, HTML_DISABLED);
			return;
		}

		if(command.equalsIgnoreCase("index"))
		{
			showChatWindow(player, HTML_INDEX);
		}

		else if(command.equalsIgnoreCase("makebattle"))
		{
			player.sendPacket(makeBattleHtml(player));
		}

		else if(command.equalsIgnoreCase("info"))
		{
			showChatWindow(player, HTML_INFO);
		}

		else
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			String pageName = st.nextToken();
			if(pageName.equalsIgnoreCase("addbattle"))
			{
				int count = 0;
				try
				{
					count = Integer.parseInt(st.nextToken());
					if(count == 0)
					{
						sendResult(player, HtmlUtils.show(new CustomMessage("common.Error", player), player), HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.1", player), player));
						return;
					}
				}
				catch(NumberFormatException e)
				{
					sendResult(player, HtmlUtils.show(new CustomMessage("common.Error", player), player), HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.1", player), player));
					return;
				}
				String itemName = StringUtils.EMPTY;
				if(st.hasMoreTokens())
				{
					itemName = st.nextToken();
					while(st.hasMoreTokens())
						itemName += " " + st.nextToken();
				}
				Object[] objects = { player, itemName, count };
				final String respone = (String) callScripts("addApplication", objects);
				if("OK".equalsIgnoreCase(respone))
				{
					sendResult(player, HtmlUtils.show(new CustomMessage("common.Completed", player), player), (HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.2", player), player)) + String.valueOf(objects[2]) + " " + String.valueOf(objects[1]) + (HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.3", player), player)));
				}
				else if("NoItems".equalsIgnoreCase(respone))
				{
					sendResult(player, HtmlUtils.show(new CustomMessage("common.Error", player), player), HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.4", player), player));
					return;
				}
				else if("reg".equalsIgnoreCase(respone))
				{
					sendResult(player, HtmlUtils.show(new CustomMessage("common.Error", player), player), HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.5", player), player));
					return;
				}
			}
			else if(pageName.equalsIgnoreCase("delete"))
			{
				Object[] playerObject = { player };
				if((Boolean) callScripts("isRegistered", playerObject))
				{
					callScripts("deleteRegistration", playerObject);
					sendResult(player, HtmlUtils.show(new CustomMessage("common.Completed", player), player), HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.6", player), player));
				}
				else
					sendResult(player, HtmlUtils.show(new CustomMessage("common.Error", player), player), HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.7", player), player));
			}
			else if(pageName.equalsIgnoreCase("openpage"))
			{
				player.sendPacket(makeOpenPage(player, Integer.parseInt(st.nextToken())));
			}
			else if(pageName.equalsIgnoreCase("tryaccept"))
			{
				player.sendPacket(makeAcceptHtml(player, Long.parseLong(st.nextToken())));
			}
			else if(pageName.equalsIgnoreCase("accept"))
			{
				accept(player, Long.parseLong(st.nextToken()));
			}
		}
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showChatWindow(player, HTML_INDEX);
	}

	private NpcHtmlMessage makeOpenPage(Player player, int pageId)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(HTML_LIST);

		StringBuilder sb = new StringBuilder();

		final int count = (Integer) callScripts("getRatesCount", new Object[0]);

		int num = pageId * Config.FIGHT_CLUB_PLAYERS_PER_PAGE;
		if(num > count)
			num = count;
		if(count > 0)
		{

			for(int i = pageId * Config.FIGHT_CLUB_PLAYERS_PER_PAGE - Config.FIGHT_CLUB_PLAYERS_PER_PAGE; i < num; i++)
			{
				Object[] index = { i };
				Rate rate = (Rate) callScripts("getRateByIndex", index);

				sb.append("<table width=280 border=0 cellspacing=4 cellpadding=3>");
				sb.append("<tr>");
				sb.append("<td width=50 align=right valign=top>");
				sb.append("<img src=\"icon.etc_quest_account_reward_i00\" width=32 height=32>");
				sb.append("</td>");
				sb.append("<td width=230 align=left valign=top>");
				sb.append("<font color=\"LEVEL\"><a action=\"bypass -h npc_%objectId%_tryaccept ").append(rate.getStoredId()).append("\">").append(rate.getPlayerName()).append("</a><font>&nbsp;(<font color=\"CCFF33\">").append(new CustomMessage("utils.classId.name." + rate.getPlayerClass() + "", player)).append("<font>)<br1>");
				sb.append("<font color=\"AAAAAA\">Ставка:&nbsp;").append(Util.formatAdena(rate.getItemCount())).append("&nbsp;").append(rate.getItemName()).append("<font>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}

			int pg = getPagesCount(count);
			if(pg > 1)
			{
				sb.append("<table width=280>");
				sb.append("<tr>");
				sb.append("<td width=280 height=20 align=center valign=top>");
				sb.append("Текущая страница:&nbsp;").append(pageId).append("");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=280>");
				sb.append("<tr>");
				sb.append("<td width=140 height=20 align=right valign=top>");
				sb.append("<combobox var=\"page\" width=\"100\" List=\"");
				for(int i = 1; i <= pg; i++)
				{
					sb.append("").append(i).append(";");
				}
				sb.append("\">");
				sb.append("</td>");
				sb.append("<td width=140 height=20 align=left valign=top>");
				sb.append("<button value=\"Перейти\" action=\"bypass -h npc_%objectId%_openpage $page\" width=80 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}
		}
		else
		{
			sb.append(HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.8", player), player));
		}
		html.replace("%data%", sb.toString());

		return html;
	}

	private NpcHtmlMessage makeBattleHtml(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(HTML_MAKEBATTLE);
		html.replace("%items%", (String) callScripts("getItemsList", new Object[0]));

		return html;
	}

	private NpcHtmlMessage makeAcceptHtml(Player player, long storedId)
	{
		Object[] id = { storedId };
		Rate rate = (Rate) callScripts("getRateByStoredId", id);
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(HTML_ACCEPT);
		html.replace("%name%", rate.getPlayerName());
		html.replace("%class%", HtmlUtils.show(new CustomMessage("utils.classId.name." + rate.getPlayerClass() + "", player), player));
		html.replace("%level%", String.valueOf(rate.getPlayerLevel()));
		html.replace("%rate%", rate.getItemCount() + " " + rate.getItemName());
		html.replace("%storedId%", String.valueOf(rate.getStoredId()));
		return html;
	}

	private void accept(Player player, long storedId)
	{
		final Object[] data = { GameObjectsStorage.getAsPlayer(storedId), player };
		if(player.getStoredId() == storedId)
		{
			sendResult(player, HtmlUtils.show(new CustomMessage("common.Error", player), player), HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.9", player), player));
			return;
		}
		//TODO: Проверка на айтемы... Берем пример с doStart
		//if(Functions.getItemCount(player, _))
		if((Boolean) callScripts("requestConfirmation", data))
		{
			sendResult(player, HtmlUtils.show(new CustomMessage("common.Attention", player), player), HtmlUtils.show(new CustomMessage("scripts.events.fightclub.result.10", player), player));
		}
	}

	private void sendResult(Player player, String title, String text)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(HTML_RESULT);
		html.replace("%title%", title);
		html.replace("%text%", text);
		player.sendPacket(html);
	}

	private int getPagesCount(int count)
	{
		if(count % Config.FIGHT_CLUB_PLAYERS_PER_PAGE > 0)
			return count / Config.FIGHT_CLUB_PLAYERS_PER_PAGE + 1;
		return count / Config.FIGHT_CLUB_PLAYERS_PER_PAGE;
	}

	private Object callScripts(String methodName, Object[] args)
	{
		return Functions.callScripts("events.FightClub.FightClubManager", methodName, args);
	}

	public static class Rate
	{
		private String playerName;
		private int playerLevel;
		private int playerClass;
		private int _itemId;
		private String itemName;
		private int _itemCount;
		private long playerStoredId;

		public Rate(Player player, int itemId, int itemCount)
		{
			playerName = player.getName();
			playerLevel = player.getLevel();
			playerClass = player.getClassId().getId();
			_itemId = itemId;
			_itemCount = itemCount;
			itemName = ItemFunctions.createItem(itemId).getTemplate().getName();
			playerStoredId = player.getStoredId();
		}

		public String getPlayerName()
		{
			return playerName;
		}

		public int getPlayerLevel()
		{
			return playerLevel;
		}

		public int getPlayerClass()
		{
			return playerClass;
		}

		public int getItemId()
		{
			return _itemId;
		}

		public int getItemCount()
		{
			return _itemCount;
		}

		public String getItemName()
		{
			return itemName;
		}

		public long getStoredId()
		{
			return playerStoredId;
		}
	}
}