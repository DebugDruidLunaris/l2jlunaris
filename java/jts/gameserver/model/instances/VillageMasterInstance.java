package jts.gameserver.model.instances;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.SubClass;
import jts.gameserver.model.Zone;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.base.ClassType;
import jts.gameserver.model.base.PlayerClass;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.model.entity.events.impl.CastleSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.entity.residence.Dominion;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.pledge.Alliance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.pledge.SubUnit;
import jts.gameserver.model.pledge.UnitMember;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import jts.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import jts.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import jts.gameserver.network.serverpackets.PledgeStatusChanged;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.ValidateLocation;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.ClanTable;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.CertificationFunctions;
import jts.gameserver.utils.HtmlUtils;
import jts.gameserver.utils.SiegeUtils;
import jts.gameserver.utils.Util;

@SuppressWarnings("serial")
public final class VillageMasterInstance extends NpcInstance
{
	public VillageMasterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("create_clan") && command.length() > 12)
		{
			String val = command.substring(12);
			createClan(player, val);
		}
		else if(command.startsWith("create_academy") && command.length() > 15)
		{
			String sub = command.substring(15, command.length());
			createSubPledge(player, sub, Clan.SUBUNIT_ACADEMY, 5, "");
		}
		else if(command.startsWith("create_royal") && command.length() > 15)
		{
			String[] sub = command.substring(13, command.length()).split(" ", 2);
			if(sub.length == 2)
				createSubPledge(player, sub[1], Clan.SUBUNIT_ROYAL1, 6, sub[0]);
		}
		else if(command.startsWith("create_knight") && command.length() > 16)
		{
			String[] sub = command.substring(14, command.length()).split(" ", 2);
			if(sub.length == 2)
				createSubPledge(player, sub[1], Clan.SUBUNIT_KNIGHT1, 7, sub[0]);
		}
		else if(command.startsWith("assign_subpl_leader") && command.length() > 22)
		{
			String[] sub = command.substring(20, command.length()).split(" ", 2);
			if(sub.length == 2)
				assignSubPledgeLeader(player, sub[1], sub[0]);
		}
		else if(command.startsWith("assign_new_clan_leader") && command.length() > 23)
		{
			String val = command.substring(23);
			setLeader(player, val);
		}
		if(command.startsWith("create_ally") && command.length() > 12)
		{
			String val = command.substring(12);
			createAlly(player, val);
		}
		else if(command.startsWith("dissolve_ally"))
			dissolveAlly(player);
		else if(command.startsWith("dissolve_clan"))
			dissolveClan(player);
		else if(command.startsWith("increase_clan_level"))
			levelUpClan(player);
		else if(command.startsWith("learn_clan_skills"))
			showClanSkillList(player);
		else if(command.startsWith("ShowCouponExchange"))
		{
			if(Functions.getItemCount(player, 8869) > 0 || Functions.getItemCount(player, 8870) > 0)
				command = "Multisell 800";
			else
				command = "Link villagemaster/reflect_weapon_master_noticket.htm";
			super.onBypassFeedback(player, command);
		}
		else if(command.equalsIgnoreCase("CertificationList"))
			CertificationFunctions.showCertificationList(this, player);
		else if(command.equalsIgnoreCase("GetCertification65"))
			CertificationFunctions.getCertification65(this, player);
		else if(command.equalsIgnoreCase("GetCertification70"))
			CertificationFunctions.getCertification70(this, player);
		else if(command.equalsIgnoreCase("GetCertification80"))
			CertificationFunctions.getCertification80(this, player);
		else if(command.equalsIgnoreCase("GetCertification75List"))
			CertificationFunctions.getCertification75List(this, player);
		else if(command.equalsIgnoreCase("GetCertification75C"))
			CertificationFunctions.getCertification75(this, player, true);
		else if(command.equalsIgnoreCase("GetCertification75M"))
			CertificationFunctions.getCertification75(this, player, false);
		else if(command.startsWith("Subclass"))
		{
			if(player.getPet() != null)
			{
				player.sendPacket(SystemMsg.A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED);
				return;
			}

			// Саб класс нельзя получить или поменять, пока используется скилл или персонаж находится в режиме трансформации
			if(player.isActionsDisabled() || player.getTransformation() != 0)
			{
				player.sendPacket(SystemMsg.SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
				return;
			}
			if(player.getWeightPenalty() >= 3)
			{
				player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
				return;
			}

			if(player.getInventoryLimit() * 0.8 < player.getInventory().getSize())
			{
				player.sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
				player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
				return;
			}

			StringBuilder content = new StringBuilder("<html><body>");
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);

			Map<Integer, SubClass> playerClassList = player.getSubClasses();
			Set<PlayerClass> subsAvailable;

			if(player.getLevel() < 40)
			{
				content.append("You must be level 40 or more to operate with your sub-classes.");
				content.append("</body></html>");
				html.setHtml(content.toString());
				player.sendPacket(html);
				return;
			}

			int classId = 0;
			int newClassId = 0;
			int intVal = 0;

			try
			{
				for(String id : command.substring(9, command.length()).split(" "))
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			switch(intVal)
			{
				case 1: // Возвращает список сабов, которые можно взять (см case 4)
					if(!checkCondition(player))
						return;

					subsAvailable = getAvailableSubClasses(player, true);

					if(subsAvailable != null && !subsAvailable.isEmpty())
					{
						if (player.isLangRus())
						content.append("Добавить сабкласс:<br>Какой из сабклассов вы хотите добавить?<br>");
						else
						content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");

						for(PlayerClass subClass : subsAvailable)
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 4 ").append(subClass.ordinal()).append("\">").append(HtmlUtils.htmlClassName(subClass.ordinal())).append("</a><br>");
					}
					else
					{
						player.sendMessage(player.isLangRus() ? "В данный момент нет доступных сабклассов" : "There are no sub classes available at this time.");
						return;
					}
					break;
				case 2: // Установка уже взятого саба (см case 5)
					if(!checkCondition(player))
						return;
					if (player.isLangRus())
						content.append("Смена сабкласс:<br>");
					else
						content.append("Change Subclass:<br>");
					final int baseClassId = player.getBaseClassId();

					if(playerClassList.size() < 2)
						if (player.isLangRus())
						content.append("Вы не можете сменить сабкласс, потому что у вас нет сабкласса.<br><a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 1\">Добавить сабкласс.</a>");
						else
						content.append("You can't change subclasses when you don't have a subclass to begin with.<br><a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 1\">Add subclass.</a>");
					else
					{
						if (player.isLangRus())
							content.append("На какой сабкласс вы хотите переключиться?<br>");
						else
							content.append("Which class would you like to switch to?<br>");

						if(baseClassId == player.getActiveClassId())
							content.append(HtmlUtils.htmlClassName(baseClassId)).append(" <font color=\"LEVEL\">(Base Class)</font><br><br>");
						else
							if (player.isLangRus())
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(baseClassId).append("\">").append(HtmlUtils.htmlClassName(baseClassId)).append("</a> " +  " <font color=\"LEVEL\">(Базовый Класс)</font><br><br>");
							else
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(baseClassId).append("\">").append(HtmlUtils.htmlClassName(baseClassId)).append("</a> " +  " <font color=\"LEVEL\">(Base Class)</font><br><br>");
						for(SubClass subClass : playerClassList.values())
						{
							if(subClass.isBase())
								continue;
							int subClassId = subClass.getClassId();

							if(subClassId == player.getActiveClassId())
								content.append(HtmlUtils.htmlClassName(subClassId)).append("<br>");
							else
								content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(subClassId).append("\">").append(HtmlUtils.htmlClassName(subClassId)).append("</a><br>");
						}
					}
					break;
				case 3: // Отмена сабкласса - список имеющихся (см case 6)
					if(!checkCondition(player))
						return;

					if (player.isLangRus())
					content.append("Смена сабкласса:<br>Выберите сабкласс который вы хотите сменить<br>");
					else
					content.append("Change Subclass:<br>Which of the following sub-classes would you like to change?<br>");

					for(SubClass sub : playerClassList.values())
					{
						content.append("<br>");
						if(!sub.isBase())
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 6 ").append(sub.getClassId()).append("\">").append(HtmlUtils.htmlClassName(sub.getClassId())).append("</a><br>");
					}
					if (player.isLangRus())
					content.append("<br>Если вы смените сабкласс, ваш стартовых уровень будет 40 и 2 профессия.");
					else
					content.append("<br>If you change a sub-class, you'll start at level 40 after the 2nd class transfer.");
					break;
				case 4: // Добавление сабкласса - обработка выбора из case 1
					if(!checkCondition(player))
						return;
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

					if(Config.OLYMPIAD_ENABLE && Olympiad.isRegisteredInComp(player))
					{
						player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
						return;
					}

					/*
					 * Если требуется квест - проверка прохождения Mimir's Elixir (Path to Subclass)
					 * Для камаэлей квест 236_SeedsOfChaos
					 * Если саб первый, то проверить начилие предмета, если не первый, то даём сабкласс.
					 * Если сабов нету, то проверяем наличие предмета.
					 */
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
						if(!player.addSubClass(classId, true, 0))
						{
							player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", player));
							return;
						}

						content.append("Add Subclass:<br>The subclass of <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(classId)).append("</font> has been added.");
						player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
					}
					else
						html.setFile("villagemaster/SubClass_Fail.htm");
					break;
				case 5: // Смена саба на другой из уже взятых - обработка выбора из case 2
					if(!checkCondition(player))
						return;
					player.getEffectList().stopAllEffects();
					player.sendPacket(new ValidateLocation(player));


					if(Config.OLYMPIAD_ENABLE && Olympiad.isRegisteredInComp(player))
					{
						player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
						return;
					}

					player.setActiveSubClass(classId, true);
					if (player.isLangRus())
						content.append("Смена сабкласса:<br>Вы активировалы сабкласс <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(player.getActiveClassId())).append("</font>.");
					else
						content.append("Change Subclass:<br>Your active sub class is now a <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(player.getActiveClassId())).append("</font>.");
					
					player.sendPacket(SystemMsg.YOU_HAVE_SUCCESSFULLY_SWITCHED_TO_YOUR_SUBCLASS);
					// completed.
					break;
				case 6: // Отмена сабкласса - обработка выбора из case 3
					if(!checkCondition(player))
						return;
					if (player.isLangRus())
						content.append("Пожалуйста выберите сабкласс для его смены. Если тот, кого вы ищете не здесь, поищите мастера соответствующего классу.<br><font color=\"LEVEL\">Внимание!</font> Все скилы этого класса будут удалены!<br><br>");
					else
						content.append("Please choose a sub class to change to. If the one you are looking for is not here, please seek out the appropriate master for that class.<br><font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");

					subsAvailable = getAvailableSubClasses(player, false);

					if(!subsAvailable.isEmpty())
						for(PlayerClass subClass : subsAvailable)
							content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 7 ").append(classId).append(" ").append(subClass.ordinal()).append("\">").append(HtmlUtils.htmlClassName(subClass.ordinal())).append("</a><br>");
					else
					{
						player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", player));
						return;
					}
					break;
				case 7: // Отмена сабкласса - обработка выбора из case 6
					player.getEffectList().stopAllEffects();
					player.sendPacket(new ValidateLocation(player));
					
					if(Config.OLYMPIAD_ENABLE && Olympiad.isRegisteredInComp(player))
					{
						player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
						return;
					}

					if(player.modifySubClass(classId, newClassId))
					{
						if (player.isLangRus())
						{
							content.append("Смена сабкласса:<br>Ваш сабкласс был изменен на <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(newClassId)).append("</font>.");
							player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
						}
							else
							{
								content.append("Change Subclass:<br>Your subclass has been changed to <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(newClassId)).append("</font>.");
							player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
							}
						}
					else
					{
						player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", player));
						return;
					}
					break;
			}
			content.append("</body></html>");

			// If the content is greater than for a basic blank page,
			// then assume no external HTML file was assigned.
			if(content.length() > 26)
				html.setHtml(content.toString());

			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "villagemaster/" + pom + ".htm";
	}

	// Private stuff
	public void createClan(Player player, String clanName)
	{
		if(player.getLevel() < 10)
		{
			player.sendPacket(Msg.YOU_ARE_NOT_QUALIFIED_TO_CREATE_A_CLAN);
			return;
		}

		if(player.getClanId() != 0)
		{
			player.sendPacket(Msg.YOU_HAVE_FAILED_TO_CREATE_A_CLAN);
			return;
		}

		if(!player.canCreateClan())
		{
			// you can't create a new clan within 10 days
			player.sendPacket(Msg.YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return;
		}
		if(clanName.length() > 16)
		{
			player.sendPacket(Msg.CLAN_NAMES_LENGTH_IS_INCORRECT);
			return;
		}
		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
		{
			// clan name is not matching template
			player.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
			return;
		}

		Clan clan = ClanTable.getInstance().createClan(player, clanName);
		if(clan == null)
		{
			// clan name is already taken
			player.sendPacket(Msg.THIS_NAME_ALREADY_EXISTS);
			return;
		}

		// should be update packet only
		player.sendPacket(clan.listAll());
		player.sendPacket(new PledgeShowInfoUpdate(clan), Msg.CLAN_HAS_BEEN_CREATED);
		player.updatePledgeClass();
		player.broadcastCharInfo();

		if(Config.SERVICES_CLAN_LEVEL_CREATE > 0)
			increaseClanLevel(player, Config.SERVICES_CLAN_LEVEL_CREATE);
	}

	public void setLeader(Player leader, String newLeader)
	{
		if(!leader.isClanLeader())
		{
			leader.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}

		if(leader.getEvent(SiegeEvent.class) != null)
		{
			leader.sendMessage(new CustomMessage("scripts.services.Rename.SiegeNow", leader));
			return;
		}

		Clan clan = leader.getClan();
		SubUnit mainUnit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
		UnitMember member = mainUnit.getUnitMember(newLeader);

		if(member == null)
		{
			//FIX ME зачем 2-ве мессаги(VISTALL)
			//leader.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.S1IsNotMemberOfTheClan", leader).addString(newLeader));
			showChatWindow(leader, "villagemaster/clan-20.htm");
			return;
		}

		if(member.getLeaderOf() != Clan.SUBUNIT_NONE)
		{
			leader.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.CannotAssignUnitLeader", leader));
			return;
		}

		setLeader(leader, clan, mainUnit, member);
	}

	public static void setLeader(Player player, Clan clan, SubUnit unit, UnitMember newLeader)
	{
		player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.ClanLeaderWillBeChangedFromS1ToS2", player).addString(clan.getLeaderName()).addString(newLeader.getName()));
		//TODO: В данной редакции смена лидера производится сразу же.
		// Надо подумать над реализацией смены кланлидера в запланированный день недели.

		/*if(clan.getLevel() >= CastleSiegeManager.getSiegeClanMinLevel())
		{
			if(clan.getLeader() != null)
			{
				L2Player oldLeaderPlayer = clan.getLeader().getPlayer();
				if(oldLeaderPlayer != null)
					SiegeUtils.removeSiegeSkills(oldLeaderPlayer);
			}
			L2Player newLeaderPlayer = newLeader.getPlayer();
			if(newLeaderPlayer != null)
				SiegeUtils.addSiegeSkills(newLeaderPlayer);
		}
		            */
		unit.setLeader(newLeader, true);

		clan.broadcastClanStatus(true, true, false);
	}

	public void createSubPledge(Player player, String clanName, int pledgeType, int minClanLvl, String leaderName)
	{
		UnitMember subLeader = null;

		Clan clan = player.getClan();

		if(clan == null || !player.isClanLeader())
		{
			player.sendPacket(Msg.YOU_HAVE_FAILED_TO_CREATE_A_CLAN);
			return;
		}

		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
		{
			player.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
			return;
		}

		Collection<SubUnit> subPledge = clan.getAllSubUnits();
		for(SubUnit element : subPledge)
			if(element.getName().equals(clanName))
			{
				player.sendPacket(Msg.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
				return;
			}

		if(ClanTable.getInstance().getClanByName(clanName) != null)
		{
			player.sendPacket(Msg.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
			return;
		}

		if(clan.getLevel() < minClanLvl)
		{
			player.sendPacket(Msg.THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET);
			return;
		}

		SubUnit unit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);

		if(pledgeType != Clan.SUBUNIT_ACADEMY)
		{
			subLeader = unit.getUnitMember(leaderName);
			if(subLeader == null)
			{
				player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader", player));
				return;
			}
			else if(subLeader.getLeaderOf() != Clan.SUBUNIT_NONE)
			{
				player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.ItCantBeSubUnitLeader", player));
				return;
			}
		}

		pledgeType = clan.createSubPledge(player, pledgeType, subLeader, clanName);
		if(pledgeType == Clan.SUBUNIT_NONE)
			return;

		clan.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(clan.getSubUnit(pledgeType)));

		SystemMessage sm;
		if(pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			sm = new SystemMessage(SystemMessage.CONGRATULATIONS_THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else if(pledgeType >= Clan.SUBUNIT_KNIGHT1)
		{
			sm = new SystemMessage(SystemMessage.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else if(pledgeType >= Clan.SUBUNIT_ROYAL1)
		{
			sm = new SystemMessage(SystemMessage.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else
			sm = Msg.CLAN_HAS_BEEN_CREATED;

		player.sendPacket(sm);

		if(subLeader != null)
		{
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subLeader));
			if(subLeader.isOnline())
			{
				subLeader.getPlayer().updatePledgeClass();
				subLeader.getPlayer().broadcastCharInfo();
			}
		}
	}

	public void assignSubPledgeLeader(Player player, String clanName, String leaderName)
	{
		Clan clan = player.getClan();

		if(clan == null)
		{
			player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.ClanDoesntExist", player));
			return;
		}

		if(!player.isClanLeader())
		{
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}

		SubUnit targetUnit = null;
		for(SubUnit unit : clan.getAllSubUnits())
		{
			if(unit.getType() == Clan.SUBUNIT_MAIN_CLAN || unit.getType() == Clan.SUBUNIT_ACADEMY)
				continue;
			if(unit.getName().equalsIgnoreCase(clanName))
				targetUnit = unit;

		}
		if(targetUnit == null)
		{
			player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.SubUnitNotFound", player));
			return;
		}
		SubUnit mainUnit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
		UnitMember subLeader = mainUnit.getUnitMember(leaderName);
		if(subLeader == null)
		{
			player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader", player));
			return;
		}

		if(subLeader.getLeaderOf() != Clan.SUBUNIT_NONE)
		{
			player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.ItCantBeSubUnitLeader", player));
			return;
		}

		targetUnit.setLeader(subLeader, true);
		clan.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(targetUnit));

		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subLeader));
		if(subLeader.isOnline())
		{
			subLeader.getPlayer().updatePledgeClass();
			subLeader.getPlayer().broadcastCharInfo();
		}

		player.sendMessage(new CustomMessage("jts.gameserver.model.instances.L2VillageMasterInstance.NewSubUnitLeaderHasBeenAssigned", player));
	}

	private void dissolveClan(Player player)
	{
		if(player == null || player.getClan() == null)
			return;
		Clan clan = player.getClan();

		if(!player.isClanLeader())
		{
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}
		if(clan.getAllyId() != 0)
		{
			player.sendPacket(Msg.YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE);
			return;
		}
		if(clan.isAtWar() > 0)
		{
			player.sendPacket(Msg.YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR);
			return;
		}
		if(clan.getCastle() != 0 || clan.getHasHideout() != 0 || clan.getHasFortress() != 0)
		{
			player.sendPacket(Msg.UNABLE_TO_DISPERSE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS);
			return;
		}

		for(Residence r : ResidenceHolder.getInstance().getResidences())
			if(r.getSiegeEvent().getSiegeClan(SiegeEvent.ATTACKERS, clan) != null || r.getSiegeEvent().getSiegeClan(SiegeEvent.DEFENDERS, clan) != null || r.getSiegeEvent().getSiegeClan(CastleSiegeEvent.DEFENDERS_WAITING, clan) != null)
			{
				player.sendPacket(SystemMsg.UNABLE_TO_DISSOLVE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE);
				return;
			}

		ClanTable.getInstance().dissolveClan(player);
	}

	public void levelUpClan(Player player)
	{
		Clan clan = player.getClan();
		if(clan == null)
			return;
		if(!player.isClanLeader())
		{
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}

		boolean increaseClanLevel = false;

		switch(clan.getLevel())
		{
			case 0:
				// Upgrade to 1
				if(player.getSp() >= Config.REQUIREMEN_CLAN_LEVEL_UP_TO_1 && player.getAdena() >= Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_1)
				{
					player.setSp(player.getSp() - Config.REQUIREMEN_CLAN_LEVEL_UP_TO_1);
					player.reduceAdena(Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_1, true);
					increaseClanLevel = true;
				}
				break;
			case 1:
				// Upgrade to 2
				if(player.getSp() >= Config.REQUIREMEN_CLAN_LEVEL_UP_TO_2 && player.getAdena() >= Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_2)
				{
					player.setSp(player.getSp() - Config.REQUIREMEN_CLAN_LEVEL_UP_TO_2);
					player.reduceAdena(Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_2, true);
					increaseClanLevel = true;
				}
				break;
			case 2:
				// Upgrade to 3
				// itemid 1419 == Blood Mark
				if(player.getSp() >= Config.REQUIREMEN_CLAN_LEVEL_UP_TO_3 && player.getInventory().destroyItemByItemId(1419, Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_3))
				{
					player.setSp(player.getSp() - Config.REQUIREMEN_CLAN_LEVEL_UP_TO_3);
					increaseClanLevel = true;
				}
				break;
			case 3:
				// Upgrade to 4
				// itemid 3874 == Alliance Manifesto
				if(player.getSp() >= Config.REQUIREMEN_CLAN_LEVEL_UP_TO_4 && player.getInventory().destroyItemByItemId(3874, Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_4))
				{
					player.setSp(player.getSp() - Config.REQUIREMEN_CLAN_LEVEL_UP_TO_4);
					increaseClanLevel = true;
				}
				break;
			case 4:
				// Upgrade to 5
				// itemid 3870 == Seal of Aspiration
				if(player.getSp() >= Config.REQUIREMEN_CLAN_LEVEL_UP_TO_5 && player.getInventory().destroyItemByItemId(3870, Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_5))
				{
					player.setSp(player.getSp() - Config.REQUIREMEN_CLAN_LEVEL_UP_TO_5);
					increaseClanLevel = true;
				}
				break;
			case 5:
				// Upgrade to 6
				if(clan.getReputationScore() >= Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_6 && clan.getAllSize() >= Config.MEMBER_CLAN_LEVEL_UP_TO_6)
				{
					clan.incReputation(-Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_6, false, "LvlUpClan");
					increaseClanLevel = true;
				}
				break;
			case 6:
				// Upgrade to 7
				if(clan.getReputationScore() >= Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_7 && clan.getAllSize() >= Config.MEMBER_CLAN_LEVEL_UP_TO_7)
				{
					clan.incReputation(-Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_7, false, "LvlUpClan");
					increaseClanLevel = true;
				}
				break;
			case 7:
				// Upgrade to 8
				if(clan.getReputationScore() >= Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_8 && clan.getAllSize() >= Config.MEMBER_CLAN_LEVEL_UP_TO_8)
				{
					clan.incReputation(-Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_8, false, "LvlUpClan");
					increaseClanLevel = true;
				}
				break;
			case 8:
				// Upgrade to 9
				// itemId 9910 == Blood Oath
				if(clan.getReputationScore() >= Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_9 && clan.getAllSize() >= Config.MEMBER_CLAN_LEVEL_UP_TO_9)
				{
					ItemInstance item = player.getInventory().getItemByItemId(9910);
					if(item != null && item.getCount() >= Config.REQUIREMEN_CLAN_LEVEL_UP_TO_9)
					{
						clan.incReputation(-Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_9, false, "LvlUpClan");
						player.getInventory().destroyItemByItemId(9910, Config.REQUIREMEN_CLAN_LEVEL_UP_TO_9);
						increaseClanLevel = true;
					}
				}
				break;
			case 9:
				// Upgrade to 10
				// itemId 9911 == Blood Alliance
				if(clan.getReputationScore() >= Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_10 && clan.getAllSize() >= Config.MEMBER_CLAN_LEVEL_UP_TO_10)
				{
					ItemInstance item = player.getInventory().getItemByItemId(9911);
					if(item != null && item.getCount() >= Config.REQUIREMEN_CLAN_LEVEL_UP_TO_10)
					{
						clan.incReputation(-Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_10, false, "LvlUpClan");
						player.getInventory().destroyItemByItemId(9911, Config.REQUIREMEN_CLAN_LEVEL_UP_TO_10);
						increaseClanLevel = true;
					}
				}
				break;
			case 10:
				// Upgrade to 11
				if(clan.getReputationScore() >= Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_11 && clan.getAllSize() >= Config.MEMBER_CLAN_LEVEL_UP_TO_11 && clan.getCastle() > 0)
				{
					Castle castle = ResidenceHolder.getInstance().getResidence(clan.getCastle());
					if(castle == null)
					{
						player.sendMessage(player.isLangRus() ? "Ваш клан не имеет замка!!!" : "Your clan has no castle!");
						increaseClanLevel = false;
						break;
					}
					Dominion dominion = castle.getDominion();
					if(dominion.getLordObjectId() == player.getObjectId())
					{
						clan.incReputation(-Config.REQUIREMEN_COST_CLAN_LEVEL_UP_TO_11, false, "LvlUpClan");
						increaseClanLevel = true;
					}
				}
				break;
		}

		if(increaseClanLevel)
		{
			clan.setLevel(clan.getLevel() + 1);
			clan.updateClanInDB();

			player.broadcastCharInfo();

			doCast(SkillTable.getInstance().getInfo(5103, 1), player, true);

			if(clan.getLevel() >= 4)
				SiegeUtils.addSiegeSkills(player);

			if(clan.getLevel() == 5)
				player.sendPacket(Msg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);

			// notify all the members about it
			PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
			PledgeStatusChanged ps = new PledgeStatusChanged(clan);
			for(UnitMember mbr : clan)
				if(mbr.isOnline())
				{
					mbr.getPlayer().updatePledgeClass();
					mbr.getPlayer().sendPacket(Msg.CLANS_SKILL_LEVEL_HAS_INCREASED, pu, ps);
					mbr.getPlayer().broadcastCharInfo();
				}
		}
		else
			player.sendPacket(Msg.CLAN_HAS_FAILED_TO_INCREASE_SKILL_LEVEL);
	}

	public void increaseClanLevel(Player player, int levelClan)
	{
		Clan clan = player.getClan();
		clan.setLevel(levelClan);
		clan.updateClanInDB();

		player.broadcastCharInfo();

		doCast(SkillTable.getInstance().getInfo(5103, 1), player, true);

		if(clan.getLevel() >= 4)
			SiegeUtils.addSiegeSkills(player);

		if(clan.getLevel() >= 5)
			player.sendPacket(Msg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);

		// notify all the members about it
		PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
		PledgeStatusChanged ps = new PledgeStatusChanged(clan);
		for(UnitMember mbr : clan)
			if(mbr.isOnline())
			{
				mbr.getPlayer().updatePledgeClass();
				mbr.getPlayer().sendPacket(Msg.CLANS_SKILL_LEVEL_HAS_INCREASED, pu, ps);
				mbr.getPlayer().broadcastCharInfo();
			}
	}

	public void createAlly(Player player, String allyName)
	{
		// D5 You may not ally with clan you are battle with.
		// D6 Only the clan leader may apply for withdraw from alliance.
		// DD No response. Invitation to join an
		// D7 Alliance leaders cannot withdraw.
		// D9 Different Alliance
		// EB alliance information
		// Ec alliance name $s1
		// ee alliance leader: $s2 of $s1
		// ef affilated clans: total $s1 clan(s)
		// f6 you have already joined an alliance
		// f9 you cannot new alliance 10 days
		// fd cannot accept. clan ally is register as enemy during siege battle.
		// fe you have invited someone to your alliance.
		// 100 do you wish to withdraw from the alliance
		// 102 enter the name of the clan you wish to expel.
		// 202 do you realy wish to dissolve the alliance
		// 502 you have accepted alliance
		// 602 you have failed to invite a clan into the alliance
		// 702 you have withdraw

		if(!player.isClanLeader())
		{
			player.sendPacket(Msg.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
			return;
		}
		if(player.getClan().getAllyId() != 0)
		{
			player.sendPacket(Msg.YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE);
			return;
		}
		if(allyName.length() > 16)
		{
			player.sendPacket(Msg.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME);
			return;
		}
		if(!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
		{
			player.sendPacket(Msg.INCORRECT_ALLIANCE_NAME);
			return;
		}
		if(player.getClan().getLevel() < 5)
		{
			player.sendPacket(Msg.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return;
		}
		if(ClanTable.getInstance().getAllyByName(allyName) != null)
		{
			player.sendPacket(Msg.THIS_ALLIANCE_NAME_ALREADY_EXISTS);
			return;
		}
		if(!player.getClan().canCreateAlly())
		{
			player.sendPacket(Msg.YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_1_DAY_AFTER_DISSOLUTION);
			return;
		}

		Alliance alliance = ClanTable.getInstance().createAlliance(player, allyName);
		if(alliance == null)
			return;

		player.broadcastCharInfo();
		player.sendMessage("Alliance " + allyName + " has been created.");
	}

	private void dissolveAlly(Player player)
	{
		if(player == null || player.getAlliance() == null)
			return;

		if(!player.isAllyLeader())
		{
			player.sendPacket(Msg.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
			return;
		}

		if(player.getAlliance().getMembersCount() > 1)
		{
			player.sendPacket(Msg.YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE);
			return;
		}

		ClanTable.getInstance().dissolveAlly(player);
	}

	private Set<PlayerClass> getAvailableSubClasses(Player player, boolean isNew)
	{
		final int charClassId = player.getBaseClassId();
		final Race npcRace = getVillageMasterRace();
		final ClassType npcTeachType = getVillageMasterTeachType();

		PlayerClass currClass = PlayerClass.values()[charClassId];// .valueOf(charClassName);

		/**
		 * If the race of your main class is Elf or Dark Elf, you may not select
		 * each class as a subclass to the other class, and you may not select
		 * Overlord and Warsmith class as a subclass.
		 *
		 * You may not select a similar class as the subclass. The occupations
		 * classified as similar classes are as follows:
		 *
		 * Treasure Hunter, Plainswalker and Abyss Walker Hawkeye, Silver Ranger
		 * and Phantom Ranger Paladin, Dark Avenger, Temple Knight and Shillien
		 * Knight Warlocks, Elemental Summoner and Phantom Summoner Elder and
		 * Shillien Elder Swordsinger and Bladedancer Sorcerer, Spellsinger and
		 * Spellhowler
		 *
		 * Kamael могут брать только сабы Kamael
		 * Другие классы не могут брать сабы Kamael
		 *
		 */
		Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return Collections.emptySet();

		// Из списка сабов удаляем мейн класс игрока
		availSubs.remove(currClass);

		for(PlayerClass availSub : availSubs)
		{
			// Удаляем из списка возможных сабов, уже взятые сабы и их предков
			for(SubClass subClass : player.getSubClasses().values())
			{
				if(availSub.ordinal() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов их родителей, если таковые есть у чара
				ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
				if(parent != null && parent.getId() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов родителей текущих сабклассов, иначе если взять саб berserker
				// и довести до 3ей профы - doombringer, игроку будет предложен berserker вновь (дежавю)
				ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
				if(subParent != null && subParent.getId() == availSub.ordinal())
					availSubs.remove(availSub);
			}

			if(!availSub.isOfRace(Race.human) && !availSub.isOfRace(Race.elf))
			{
				if(!availSub.isOfRace(npcRace))
					availSubs.remove(availSub);
			}
			else if(!availSub.isOfType(npcTeachType))
				availSubs.remove(availSub);

			// Особенности саб классов камаэль
			if(availSub.isOfRace(Race.kamael))
			{
				// Для Soulbreaker-а и SoulHound не предлагаем Soulbreaker-а другого пола
				if((currClass == PlayerClass.MaleSoulHound || currClass == PlayerClass.FemaleSoulHound || currClass == PlayerClass.FemaleSoulbreaker || currClass == PlayerClass.MaleSoulbreaker) && (availSub == PlayerClass.FemaleSoulbreaker || availSub == PlayerClass.MaleSoulbreaker))
					availSubs.remove(availSub);

				// Для Berserker(doombringer) и Arbalester(trickster) предлагаем Soulbreaker-а только своего пола
				if(currClass == PlayerClass.Berserker || currClass == PlayerClass.Doombringer || currClass == PlayerClass.Arbalester || currClass == PlayerClass.Trickster)
					if(player.getSex() == 1 && availSub == PlayerClass.MaleSoulbreaker || player.getSex() == 0 && availSub == PlayerClass.FemaleSoulbreaker)
						availSubs.remove(availSub);

				// Inspector доступен, только когда вкачаны 2 возможных первых саба камаэль(+ мейн класс)
				if(availSub == PlayerClass.Inspector && player.getSubClasses().size() < (isNew ? 3 : 4))
					availSubs.remove(availSub);
			}
		}
		return availSubs;
	}

	private Race getVillageMasterRace()
	{
		switch(getTemplate().getRace())
		{
			case 14:
				return Race.human;
			case 15:
				return Race.elf;
			case 16:
				return Race.darkelf;
			case 17:
				return Race.orc;
			case 18:
				return Race.dwarf;
			case 25:
				return Race.kamael;
		}
		return null;
	}

	private ClassType getVillageMasterTeachType()
	{
		switch(getNpcId())
		{
			case 30031:
			case 30037:
			case 30070:
			case 30120:
			case 30191:
			case 30289:
			case 30857:
			case 30905:
			case 32095:
			case 30141:
			case 30305:
			case 30358:
			case 30359:
			case 31336:
				return ClassType.Priest;

			case 30115:
			case 30174:
			case 30175:
			case 30176:
			case 30694:
			case 30854:
			case 31331:
			case 31755:
			case 31996:
			case 32098:
			case 32147:
			case 32160:
			case 30154:
			case 31285:
			case 31288:
			case 31326:
			case 31977:
			case 32150:
				return ClassType.Mystic;
			default:
		}

		return ClassType.Fighter;
	}

	private boolean checkCondition(Player player)
	{

		if(Config.OLYMPIAD_ENABLE && Olympiad.isRegisteredInComp(player))
		{
			player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
			return false;
		}

		if(player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isAttackingNow())
		{
			player.sendMessage(player.isLangRus() ? "Сменить саб-класс в вашем состоянии невозможно" : "You can`t change sub-class in this condition");
			return false;
		}

		if(player.isInCombat())
		{
			player.sendMessage(player.isLangRus() ? "Сменить саб-класс в боевом режиме нельзя" : "You can`t change sub-class in fight mode");
			return false;
		}

		if(!player.isInZone(Zone.ZoneType.peace_zone) || player.isInZone(Zone.ZoneType.battle_zone) || player.isInZone(Zone.ZoneType.no_escape) || player.isInZone(Zone.ZoneType.epic) || player.isInZone(Zone.ZoneType.SIEGE) || player.isInZone(Zone.ZoneType.RESIDENCE) || player.getVar("jailed") != null)
		{
			player.sendMessage(player.isLangRus() ? "Нельзя сменить саб-класс в данной локации" : "You can`t change sub-class in this location");
			return false;
		}

		if(player.getActiveWeaponFlagAttachment() != null)
		{
			player.sendMessage(player.isLangRus() ? "Сменить саб-класс со Знаменем невозможно" : "You can`t change sub-class with handing the flag");
			return false;
		}

		if(player.isInOlympiadMode())
		{
			player.sendMessage(player.isLangRus() ? "Во время Олимпиады сменить саб-класс невозможно" : "You can`t change sub-class during the Olympiad running");
			return false;
		}

		if(player.getReflection() != ReflectionManager.DEFAULT)
		{
			player.sendMessage(player.isLangRus() ? "Вы не можете сменить саб-класс, находясь во временной зоне" : "You can`t change sub-class being in time zone");
			return false;
		}

		if(player.isInDuel() || player.getTeam() != TeamType.NONE || player.getIsInDuel())
		{
			player.sendMessage(player.isLangRus() ? "Во время дуэли сменить саб-класс невозможно" : "You can`t change sub-class during a duel");
			return false;
		}

		if(player.isInCombat() || player.getPvpFlag() != 0)
		{
			player.sendMessage(player.isLangRus() ? "Во время боя сменить саб-класс невозможно" : "You can`t change sub-class during the fight");
			return false;
		}

		if(player.isOnSiegeField() || player.isInZoneBattle())
		{
			player.sendMessage(player.isLangRus() ? "Во время полномасштабных сражений - осад крепостей, замков, холлов клана, сменить саб-класс невозможно" : "You can`t change sub-class in siege battle");
			return false;
		}
		if(!isInRange(player, INTERACTION_DISTANCE))
		{
		}
		if(player.isFlying())
		{
			player.sendMessage(player.isLangRus() ? "Во время полета сменить саб-класс невозможно" : "You can`t change sub-class during the flight");
			return false;
		}

		if(player.isInWater() || player.isInBoat())
		{
			player.sendMessage(player.isLangRus() ? "Вы не можете сменить саб-класс, находясь в воде" : "You can`t change sub-class being in water");
			return false;
		}

		return true;
	}
}