package services.community;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.SubClass;
import jts.gameserver.model.Zone;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.base.PlayerClass;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.HtmlUtils;
import jts.gameserver.utils.Util;

public class CommunityBoardClassMaster extends Functions implements ScriptFile, ICommunityBoardHandler
{

	@Override
	public void onLoad()
	{
		if(Config.BBS_CLASS_MASTER_ALLOW)
		{
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.BBS_CLASS_MASTER_ALLOW)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown() {}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbsclass" };
	}

	@Override
	public void onBypassCommand(Player player, String command)
	{
		if(command.equals("_bbsclass"))
			showClassPage(player);
		else if(command.startsWith("_bbsclass:change_class:"))
		{
			StringTokenizer selectedClass = new StringTokenizer(command, ":");
			selectedClass.nextToken();
			selectedClass.nextToken();
			int classID = Integer.parseInt(selectedClass.nextToken());
			int id = Integer.parseInt(selectedClass.nextToken());
			changeClass(player, classID, id);
		}
		else if(command.startsWith("_bbsclass:sub_class:"))
		{
			StringTokenizer selectedSubClass = new StringTokenizer(command, ":");
			selectedSubClass.nextToken();
			selectedSubClass.nextToken();

			int classId = 0, newClassId = 0, intVal = 0;

			for(String id : command.substring(20, command.length()).split(":"))
			{
				if(intVal == 0)
				{
					intVal = Integer.parseInt(id);
					continue;
				}
				if(classId > 0)
				{
					newClassId = Integer.parseInt(id);
					continue;
				}
				classId = Integer.parseInt(id);
			}
			changeSubClass(player, intVal, classId, newClassId);
		}
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>" + new CustomMessage("communityboard.notdone", player).addString(command) + "</center><br><br></body></html>", player);
	}

	private String page(CustomMessage text)
	{
		StringBuilder html = new StringBuilder();

		html.append("<tr>");
		html.append("<td WIDTH=20 align=left valign=top></td>");
		html.append("<td WIDTH=690 align=left valign=top>");
		html.append(text);
		html.append("</td>");
		html.append("</tr>");

		return html.toString();
	}

	private String block(String icon, CustomMessage text, CustomMessage action, String bypass)
	{
		StringBuilder html = new StringBuilder();

		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=720><img src=\"l2ui.squaregray\" width=\"720\" height=\"1\"></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<table border=0 cellspacing=4 cellpadding=3>");
		html.append("<tr>");
		html.append("<td FIXWIDTH=50 align=right valign=top><img src=\"" + icon + "\" width=32 height=32></td>");
		html.append("<td FIXWIDTH=576 align=left valign=top>");
		html.append(text);
		html.append("</td>");
		html.append("<td FIXWIDTH=95 align=center valign=top>");
		html.append("<button value=\"" + action + "\" action=\"" + bypass + "\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"80\" height=\"25\"/>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");

		return html.toString();
	}

	private void showClassPage(Player player)
	{
		ClassId classId = player.getClassId();
		int jobLevel = classId.getLevel();
		int level = player.getLevel();

		StringBuilder html = new StringBuilder();
		html.append("<table width=755>");
		html.append(page(new CustomMessage("communityboard.classmaster.welcome", player).addString(player.getName())));
		html.append(page(new CustomMessage("communityboard.classmaster.current.profession", player).addString(HtmlUtils.htmlClassNameNonClient(player, player.getClassId().getId()).toString())));
		html.append("</table>");

		if(Config.BBS_CLASS_MASTERS_ALLOW_LIST.isEmpty() || !Config.BBS_CLASS_MASTERS_ALLOW_LIST.contains(jobLevel))
			jobLevel = 4;

		if((level >= 20 && jobLevel == 1 || level >= 40 && jobLevel == 2 || level >= 76 && jobLevel == 3) && Config.BBS_CLASS_MASTERS_ALLOW_LIST.contains(jobLevel))
		{
			int id = jobLevel - 1;
			for(ClassId cid : ClassId.VALUES)
			{
				if(cid == ClassId.inspector)
					continue;
				if(cid.childOf(classId) && cid.level() == classId.level() + 1)
				{
					html.append("<table border=0 cellspacing=0 cellpadding=0>");
					html.append("<tr>");
					html.append("<td width=755><center><img src=\"l2ui.squaregray\" width=\"720\" height=\"1\"></center></td>");
					html.append("</tr>");
					html.append("</table>");
					html.append("<table border=0 cellspacing=4 cellpadding=3>");
					html.append("<tr>");
					html.append("<td FIXWIDTH=50 align=right valign=top><img src=\"icon.etc_royal_membership_i00\" width=32 height=32></td>");
					html.append("<td FIXWIDTH=576 align=left valign=top>");
					html.append("<font color=\"0099FF\">" + HtmlUtils.htmlClassNameNonClient(player, cid.getId()) + ".</font>&nbsp;<br1>›&nbsp;");
					html.append(new CustomMessage("scripts.services.cost", player).addString(Util.formatAdena(Config.BBS_CLASS_MASTER_PRICE_COUNT[id])).addString(DifferentMethods.getItemName(Config.BBS_CLASS_MASTER_PRICE_ITEM[id])));
					html.append("</td>");
					html.append("<td FIXWIDTH=95 align=center valign=top>");
					html.append("<button value=\"" + new CustomMessage("communityboard.classmaster.change", player) + "\" action=\"bypass _bbsclass:change_class:" + cid.getId() + ":" + (id) + "\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"80\" height=\"25\"/>");
					html.append("</td>");
					html.append("</tr>");
					html.append("</table>");
				}
			}
		}
		else
			switch(jobLevel)
			{
				case 1:
					html.append("<table width=755>");
					html.append(page(new CustomMessage("communityboard.classmaster.profession.need", player).addNumber(20)));
					html.append(page(new CustomMessage("communityboard.classmaster.subclass.need", player)));
					html.append(page(new CustomMessage("communityboard.classmaster.noblesse.need", player)));
					html.append("</table>");
					break;
				case 2:
					html.append("<table width=755>");
					html.append(page(new CustomMessage("communityboard.classmaster.profession.need", player).addNumber(40)));
					html.append(page(new CustomMessage("communityboard.classmaster.subclass.need", player)));
					html.append(page(new CustomMessage("communityboard.classmaster.noblesse.need", player)));
					html.append("</table>");
					break;
				case 3:
					html.append("<table width=755>");
					html.append(page(new CustomMessage("communityboard.classmaster.profession.need", player).addNumber(76)));
					html.append(page(new CustomMessage("communityboard.classmaster.subclass.need", player)));
					html.append(page(new CustomMessage("communityboard.classmaster.noblesse.need", player)));
					html.append("</table>");
					break;
				case 4:
					if(level >= 75)
					{
						html.append("<table width=755>");
						html.append(page(new CustomMessage("communityboard.classmaster.subclass.enable", player)));

						if(!player.isNoble() && player.getSubLevel() < 75)
						{
							html.append(page(new CustomMessage("communityboard.classmaster.noblesse.need", player)));
						}
						else if(!player.isNoble() && player.getSubLevel() >= 75)
						{
							html.append(page(new CustomMessage("communityboard.classmaster.noblesse.enable", player)));
						}
						else if(player.isNoble())
						{
							html.append(page(new CustomMessage("communityboard.classmaster.isnoblesse", player)));
						}
						html.append("</table>");

						String itemName = DifferentMethods.getItemName(Config.BBS_CLASS_MASTER_SUB_PRICE_ITEM);

						if(Config.BBS_CLASS_MASTER_ADD_SUB_CLASS)
							html.append(block("icon.etc_quest_subclass_reward_i00", new CustomMessage("communityboard.classmaster.subclass.add.info", player).addNumber(Config.BBS_CLASS_MASTER_SUB_ADD_PRICE_COUNT).addString(itemName), new CustomMessage("communityboard.classmaster.subclass.add", player), "bypass _bbsclass:sub_class:1"));

						if(Config.BBS_CLASS_MASTER_CHANGE_SUB_CLASS)
							html.append(block("icon.etc_quest_subclass_reward_i00", new CustomMessage("communityboard.classmaster.subclass.change.info", player).addNumber(Config.BBS_CLASS_MASTER_SUB_CHANGE_PRICE_COUNT).addString(itemName), new CustomMessage("communityboard.classmaster.subclass.change", player), "bypass _bbsclass:sub_class:2"));

						if(Config.BBS_CLASS_MASTER_CANCEL_SUB_CLASS)
							html.append(block("icon.etc_quest_subclass_reward_i00", new CustomMessage("communityboard.classmaster.subclass.cancel.info", player).addNumber(Config.BBS_CLASS_MASTER_SUB_CANCEL_PRICE_COUNT).addString(itemName), new CustomMessage("communityboard.classmaster.subclass.cancel", player), "bypass _bbsclass:sub_class:3"));

						if(Config.BBS_CLASS_MASTER_BUY_NOBLESSE)
						{
							if(!player.isNoble())
								html.append(block("icon.skill1323", new CustomMessage("communityboard.classmaster.noblesse.buy.info", player).addNumber(Config.SERVICES_NOBLESS_SELL_PRICE).addString(DifferentMethods.getItemName(Config.SERVICES_NOBLESS_SELL_ITEM)), new CustomMessage("communityboard.classmaster.noblesse.buy", player), "bypass -h scripts_services.NoblessSell:get"));
							else
								html.append(block("icon.skill1323", new CustomMessage("communityboard.classmaster.noblesse.buy.no", player).addNumber(Config.SERVICES_NOBLESS_SELL_PRICE).addString(DifferentMethods.getItemName(Config.SERVICES_NOBLESS_SELL_ITEM)), new CustomMessage("common.not.available", player), "bypass _bbsclass"));
						}
					}
					break;
			}

		String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/services/classmanager.htm", player);
		content = content.replace("%classmaster%", html.toString());
		ShowBoard.separateAndSend(content, player);
	}

	private void changeClass(Player player, int classID, int id)
	{
		if(player == null)
			return;

		if(DifferentMethods.getPay(player, Config.BBS_CLASS_MASTER_PRICE_ITEM[id], Config.BBS_CLASS_MASTER_PRICE_COUNT[id], true))
		{
			player.setClassId(classID, false, false);
			player.updateStats();
			player.broadcastUserInfo(true);

			if(player.getClassId().getLevel() == 3)
				player.sendPacket(Msg.YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS);
			else
				player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS);
		}
		showClassPage(player);
	}

	private void changeSubClass(Player player, int intVal, int classId, int newClassId)
	{
		StringBuilder html = new StringBuilder("<html noscrollbar><title>Класс Мастер</title><body><table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td valign=\"top\" align=\"center\">");

		if(!checkCondition(player))
			return;

		Map<Integer, SubClass> playerClassList = player.getSubClasses();
		Set<PlayerClass> subsAvailable;

		if(player.getLevel() < 40)
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/services/classmanager.htm", player);
			content = content.replace("%classmaster%", "Вы должны быть 40 уровня или больше, чтобы брать суб-классы.");
			ShowBoard.separateAndSend(content, player);
			return;
		}

		switch(intVal)
		{
			case 1: // Возвращает список сабов, которые можно взять (см case 4)
				subsAvailable = getAvailableSubClasses(player, true);

				if(subsAvailable != null && !subsAvailable.isEmpty())
				{
					html.append("<center><br><br><font name=\"hs12\">Sub-classes disponíveis</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");

					for(PlayerClass subClass : subsAvailable)
						html.append("<button value=\"" + formatClassForDisplay(subClass) + "\" action=\"bypass _bbsclass:sub_class:4:" + subClass.ordinal() + "\" width=200 height=29 back=L2UI_CT1.OlympiadWnd_DF_Info_Down fore=L2UI_CT1.OlympiadWnd_DF_Info><br1>");
					html.append("</center></td></tr></table></body></html>");
					showClassPage(player);
					show(html.toString(), player);
				}
				break;
			case 2: // Instalando já tomadas saba (ver caso 5)
				html.append("<center><br><br><font name=\"hs12\">Customizar sub-classe</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");

				final int baseClassId = player.getBaseClassId();

				if(playerClassList.size() < 2)
					html.append("У вас нет Саб Класса, но вы можете добавить его прямо сейчас!<br><a action=\"bypass _bbsclass:sub_class:1\">Add Sub-Classe</a>");
				else
				{
					html.append("Какой подкласс вы хотите использовать?<br>");

					if(baseClassId == player.getActiveClassId())
						html.append("<button value=\"" + HtmlUtils.htmlClassNameNonClient(player, baseClassId) + "\" width=200 height=29 back=L2UI_CT1.OlympiadWnd_DF_Watch_Down fore=L2UI_CT1.OlympiadWnd_DF_Watch><br1>");
					else
						html.append("<button value=\"" + HtmlUtils.htmlClassNameNonClient(player, baseClassId) + " (Classe Base)" + "\" action=\"bypass _bbsclass:sub_class:5:" + baseClassId + "\" width=200 height=29 back=L2UI_CT1.OlympiadWnd_DF_HeroConfirm_Down fore=L2UI_CT1.OlympiadWnd_DF_HeroConfirm><br1>");

					for(SubClass subClass : playerClassList.values())
					{
						if(subClass.isBase())
							continue;
						int subClassId = subClass.getClassId();

						if(subClassId == player.getActiveClassId())
							html.append("<button value=\"" + HtmlUtils.htmlClassNameNonClient(player, subClassId) + "\" width=200 height=29 back=L2UI_CT1.OlympiadWnd_DF_Watch_Down fore=L2UI_CT1.OlympiadWnd_DF_Watch><br1>");
						else
							html.append("<button value=\"" + HtmlUtils.htmlClassNameNonClient(player, subClassId) + "\" action=\"bypass _bbsclass:sub_class:5:" + subClassId + "\" width=200 height=29 back=L2UI_CT1.OlympiadWnd_DF_Info_Down fore=L2UI_CT1.OlympiadWnd_DF_Info><br1>");
					}
				}
				html.append("</center></td></tr></table></body></html>");
				showClassPage(player);
				show(html.toString(), player);
				break;
			case 3: // Отмена сабкласса - список имеющихся (см case 6)
				html.append("<center><br><br><font name=\"hs12\">Cancelar sub-classe</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
				html.append("<br>Какой из существующих подклассов вы хотите заменить?<br>");

				for(SubClass sub : playerClassList.values())
					if(!sub.isBase())
						html.append("<button value=\"" + HtmlUtils.htmlClassNameNonClient(player, sub.getClassId()) + "\" action=\"bypass _bbsclass:sub_class:6:" + sub.getClassId() + "\" width=200 height=29 back=L2UI_CT1.OlympiadWnd_DF_Info_Down fore=L2UI_CT1.OlympiadWnd_DF_Info><br1>");

				html.append("<br>Изменить суб-класс, можно будет с<br1> 40 уровня со второй профессией");
				html.append("</center></td></tr></table></body></html>");
				showClassPage(player);
				show(html.toString(), player);
				break;
			case 4: // Добавление сабкласса - обработка выбора из case 1
				boolean allowAddition = true;

				// Проверка хватает ли уровня
				if(player.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
				{
					player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", player).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
					allowAddition = false;
				}

				if(!playerClassList.isEmpty())
					for(SubClass subClass : playerClassList.values())
						if(subClass.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
						{
							player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", player).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
							allowAddition = false;
							break;
						}

				if(!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS && !playerClassList.isEmpty() && playerClassList.size() < 2 + Config.ALT_GAME_SUB_ADD)
					if(player.isQuestCompleted("_234_FatesWhisper"))
					{
						if(player.getRace() == Race.kamael)
						{
							allowAddition = player.isQuestCompleted("_236_SeedsOfChaos");
							if(!allowAddition)
								player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.QuestSeedsOfChaos", player));
						}
						else
						{
							allowAddition = player.isQuestCompleted("_235_MimirsElixir");
							if(!allowAddition)
								player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.QuestMimirsElixir", player));
						}
					}
					else
					{
						player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.QuestFatesWhisper", player));
						allowAddition = false;
					}

				if(allowAddition)
				{
					if(!player.getInventory().destroyItemByItemId(Config.BBS_CLASS_MASTER_SUB_PRICE_ITEM, Config.BBS_CLASS_MASTER_SUB_ADD_PRICE_COUNT))
					{
						if(Config.BBS_CLASS_MASTER_SUB_PRICE_ITEM == 57)
							player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						else
							player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
						html.append("<center><br><br><font name=\"hs12\" color=\"FF0000\">Erro:</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
						html.append("<br><br>Недостаточности денежных средств.<br>Для включения подкласса необходимо иметь:<br1>");
						html.append(ItemHolder.getInstance().getTemplate(Config.BBS_CLASS_MASTER_SUB_PRICE_ITEM).getName()).append(" no valor de: ").append(String.valueOf(Config.BBS_CLASS_MASTER_SUB_ADD_PRICE_COUNT));
					}
					else if(!player.addSubClass(classId, true, 0))
					{
						html.append("<center><br><br><font name=\"hs12\" color=\"FF0000\">erro</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
						html.append("<br><br>У вас максимальное количество подклассов.");
					}
					else
					{
						html.append("<center><br><br><font name=\"hs12\" color=\"LEVEL\">Поздравляю</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
						html.append("<br><br>Sub-classe <font color=\"LEVEL\">" + HtmlUtils.htmlClassNameNonClient(player, classId) + "</font> adicionada com sucesso!");
						player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
						player.sendMessage(player.isLangRus() ? "desapareceu " + String.valueOf(Config.BBS_CLASS_MASTER_SUB_ADD_PRICE_COUNT) + " " + ItemHolder.getInstance().getTemplate(Config.BBS_CLASS_MASTER_SUB_PRICE_ITEM).getName() : String.valueOf(Config.BBS_CLASS_MASTER_SUB_ADD_PRICE_COUNT) + " " + ItemHolder.getInstance().getTemplate(Config.BBS_CLASS_MASTER_SUB_PRICE_ITEM).getName() + " disappeared");
					}
				}
				else
				{
					html.append("<center><br><br><font name=\"hs12\" color=\"FF0000\">erro:</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
					html.append("<br><br>Вы не можете добавить подкласс.<br>Для получения подкласса, вы должны получить<br1> <font color=F2C202>Уровень 76</font><br>");
				}
				html.append("</center></td></tr></table></body></html>");
				showClassPage(player);
				show(html.toString(), player);
				break;
			case 5: // Смена саба на другой из уже взятых - обработка выбора из case 2
				player.setActiveSubClass(classId, true);

				html.append("<br>Ваш суб-класс, сейчас активный: <font color=\"LEVEL\">" + HtmlUtils.htmlClassNameNonClient(player, player.getActiveClassId()) + "</font>.");
				html.append("</center></td></tr></table></body></html>");
				showClassPage(player);
				show(html.toString(), player);
				player.sendPacket(SystemMsg.YOU_HAVE_SUCCESSFULLY_SWITCHED_TO_YOUR_SUBCLASS);
				break;
			case 6: // Отмена сабкласса - обработка выбора из case 3
				html.append("<center><br><br><font name=\"hs12\">Выберите саб-класс</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
				html.append("<br>*Берегись! * <br1>Все навыки этого суб-класса будут удалены.<br>");

				subsAvailable = getAvailableSubClasses(player, false);

				if(!subsAvailable.isEmpty())
					for(PlayerClass subClass : subsAvailable)
						html.append("<button value=\"" + HtmlUtils.htmlClassNameNonClient(player, subClass.ordinal()) + "\" action=\"bypass _bbsclass:sub_class:7:" + classId + ":" + subClass.ordinal() + "\" width=200 height=29 back=L2UI_CT1.OlympiadWnd_DF_Info_Down fore=L2UI_CT1.OlympiadWnd_DF_Info><br1>");
				else
				{
					player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", player));
					return;
				}
				html.append("</center></td></tr></table></body></html>");
				showClassPage(player);
				show(html.toString(), player);
				break;
			case 7: // Отмена сабкласса - обработка выбора из case 6
				if(player.modifySubClass(classId, newClassId))
				{

					html.append("<br>Ваш саб-класс изменен на: <font color=\"LEVEL\">" + HtmlUtils.htmlClassNameNonClient(player, newClassId) + "</font>.");
					player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
				}
				else
				{
					player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", player));
					return;
				}
				html.append("</center></td></tr></table></body></html>");
				showClassPage(player);
				show(html.toString(), player);
				break;
		}
	}

	private Set<PlayerClass> getAvailableSubClasses(Player player, boolean isNew)
	{
		final int charClassId = player.getBaseClassId();

		PlayerClass currClass = PlayerClass.values()[charClassId];

		Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return Collections.emptySet();

		availSubs.remove(currClass);

		for(PlayerClass availSub : availSubs)
		{
			for(SubClass subClass : player.getSubClasses().values())
			{
				if(availSub.ordinal() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
				if(parent != null && parent.getId() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
				if(subParent != null && subParent.getId() == availSub.ordinal())
					availSubs.remove(availSub);
			}

			if(availSub.isOfRace(Race.kamael))
			{
				if((currClass == PlayerClass.MaleSoulHound || currClass == PlayerClass.FemaleSoulHound || currClass == PlayerClass.FemaleSoulbreaker || currClass == PlayerClass.MaleSoulbreaker) && (availSub == PlayerClass.FemaleSoulbreaker || availSub == PlayerClass.MaleSoulbreaker))
					availSubs.remove(availSub);

				if(currClass == PlayerClass.Berserker || currClass == PlayerClass.Doombringer || currClass == PlayerClass.Arbalester || currClass == PlayerClass.Trickster)
					if(player.getSex() == 1 && availSub == PlayerClass.MaleSoulbreaker || player.getSex() == 0 && availSub == PlayerClass.FemaleSoulbreaker)
						availSubs.remove(availSub);

				if(availSub == PlayerClass.Inspector && player.getSubClasses().size() < (isNew ? 3 : 4))
					availSubs.remove(availSub);
			}
		}
		return availSubs;
	}

	private String formatClassForDisplay(PlayerClass className)
	{
		String classNameStr = className.toString();
		char[] charArray = classNameStr.toCharArray();

		for(int i = 1; i < charArray.length; i++)
			if(Character.isUpperCase(charArray[i]))
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);

		return classNameStr;
	}

	private boolean checkCondition(Player player)
	{
		if(player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isAttackingNow())
		{
			player.sendMessage(player.isLangRus() ? "Смена суб-класса, в вашем состояние не возможна" : "You can`t change sub-class in this condition");
			return false;
		}

		if(player.getPet() != null)
		{
			player.sendPacket(SystemMsg.A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED);
			return false;
		}

		if(player.isActionsDisabled() || player.getTransformation() != 0)
		{
			player.sendPacket(SystemMsg.SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
			return false;
		}

		if(player.getWeightPenalty() >= 3)
		{
			player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
			return false;
		}

		if(player.getInventoryLimit() * 0.8 < player.getInventory().getSize())
		{
			player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
			return false;
		}

		if(player.isInCombat())
		{
			player.sendMessage(player.isLangRus() ? "Не возможно Изменить ваш суб-класс, находясь в режиме боя" : "You can`t change sub-class in fight mode");
			return false;
		}

		if(player.isInZone(Zone.ZoneType.battle_zone) || player.isInZone(Zone.ZoneType.no_escape) || player.isInZone(Zone.ZoneType.epic) || player.isInZone(Zone.ZoneType.SIEGE) || player.isInZone(Zone.ZoneType.RESIDENCE) || player.getVar("jailed") != null)
		{
			player.sendMessage(player.isLangRus() ? "Вы не можете изменить подкласс в этом месте" : "You can`t change sub-class in this location");
			return false;
		}

		if(player.getActiveWeaponFlagAttachment() != null)
		{
			player.sendMessage(player.isLangRus() ? "Смена суб-класса с флагом невозможна" : "You can`t change sub-class with handing the flag");
			return false;
		}

		if(Config.OLYMPIAD_ENABLE && Olympiad.isRegisteredInComp(player) || player.isInOlympiadMode())
		{
			player.sendMessage(player.isLangRus() ? "Во время Олимпиады, изменить суб-класс Не возможно" : "You can`t change sub-class during the Olympiad running");
			return false;
		}

		if(player.getReflection() != ReflectionManager.DEFAULT)
		{
			player.sendMessage(player.isLangRus() ? "Вы не можете изменить подкласс, находясь в временой зоне" : "You can`t change sub-class being in time zone");
			return false;
		}

		if(player.isInDuel() || player.getTeam() != TeamType.NONE || player.getIsInDuel())
		{
			player.sendMessage(player.isLangRus() ? "Невозможно сменить Суб класс во время Дуэли" : "You can`t change sub-class during a duel");
			return false;
		}

		if(player.isInCombat() || player.getPvpFlag() != 0)
		{
			player.sendMessage(player.isLangRus() ? "Невозможно сменить суб класс во время боя" : "You can`t change sub-class during the fight");
			return false;
		}

		if(player.isOnSiegeField() || player.isInZoneBattle())
		{
			player.sendMessage(player.isLangRus() ? "Во время битв крупных (Осад) - осады замков, дворцы, залы клана, изменить суб-класс Не возможно" : "You can`t change sub-class in siege battle");
			return false;
		}

		if(player.isFlying())
		{
			player.sendMessage(player.isLangRus() ? "Во время полета, заменить суб-класс Не возможно" : "You can`t change sub-class during the flight");
			return false;
		}

		if(player.isInWater() || player.isInBoat())
		{
			player.sendMessage(player.isLangRus() ? "Вы не можете изменить подкласс, в то время как вы находитесь в воде" : "You can`t change sub-class being in water");
			return false;
		}
		return true;
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}