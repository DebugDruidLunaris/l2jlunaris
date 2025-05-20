package services;

import java.util.List;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.dao.ClanListServiceDAO;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.EventHolder;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.ClanListObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.pledge.SubUnit;
import jts.gameserver.model.pledge.UnitMember;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.JoinPledge;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import jts.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import jts.gameserver.network.serverpackets.PledgeSkillList;
import jts.gameserver.network.serverpackets.SkillList;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.ClanTable;
import jts.gameserver.templates.item.ItemTemplate;

public class ClanList extends Functions
{
	public void list()
	{
		Player player = getSelf();
		
		if(!Config.SERVICES_CLAN_ACADEM_ENABLED)
		{
			player.sendMessage(player.isLangRus() ? "Данный сервис отключен." : "This service is disabled.");
			return;
		}
		
		String NoMercenaryAvail = player.isLangRus() ? "Вам недоступно наемничество." : "You unavailable mercenary.";
		String BottomValueClan = player.isLangRus() ? "\"Поиск клана\"" : "\"Search Clan\"";
		String BottomValueAcadem = player.isLangRus() ? "\"Поиск академии\"" : "\"Search Academy\"";
		String BottomValueCandidate = player.isLangRus() ? "\"Поиск наемника\"" : "\"Search Mercenary\"";
		String BottomValueEnroll = player.isLangRus() ? "\"Записаться\"" : "\"Enroll\"";
		String BottomValueEnrollClan = player.isLangRus() ? "\"Записать Клан\"" : "\"Enroll Clan\"";
		String BottomValueEnrollAcadem = player.isLangRus() ? "\"Записать Академию\"" : "\"Enroll Academ\"";
		String BottomValueCancel = player.isLangRus() ? "\"Отозвать заявку\"" : "\"Withdraw\"";
		
		String add = "";
		if(player.getClanId() == 0 && player.getLevel() < 40)
		{
			boolean cancelready = true;
			
			List<ClanListObject> listCandidate = ClanListServiceDAO.getInstance().getData(1);
			for(ClanListObject object : listCandidate)
				if(player.getObjectId() == object.get_obj_id() && cancelready)
				{
					add += "<tr><td align=center><button value=" + BottomValueCancel + " action=\"bypass _bbsscripts:services.ClanList:removeCandidate\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Info_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Info\"></td></tr>";
					cancelready = false;
					break;
				}
			
			listCandidate.clear();
			
			if(cancelready)
				add += "<tr><td align=center><button value=" + BottomValueEnroll + " action=\"bypass _bbsscripts:services.ClanList:addCandidate\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Info_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Info\"></td></tr>";
			add += "<tr><td align=center><button value=" + BottomValueAcadem + " action=\"bypass _bbsscripts:services.ClanList:listAcadem\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"></td></tr>";
		}
		else if(player.getClanId() == 0)
		{
			boolean cancelready = true;
			
			List<ClanListObject> listCandidate = ClanListServiceDAO.getInstance().getData(1);
			for(ClanListObject object : listCandidate)
				if(player.getObjectId() == object.get_obj_id() && cancelready)
				{
					add += "<tr><td align=center><button value=" + BottomValueCancel + " action=\"bypass _bbsscripts:services.ClanList:removeCandidate\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Info_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Info\"></td></tr>";
					cancelready = false;
					break;
				}
			
			listCandidate.clear();
			
			if(cancelready)
				add += "<tr><td align=center><button value=" + BottomValueEnroll + " action=\"bypass _bbsscripts:services.ClanList:addCandidate\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Info_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Info\"></td></tr>";
			add += "<tr><td align=center><button value=" + BottomValueClan + " action=\"bypass _bbsscripts:services.ClanList:listClan\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"></td></tr>";
		}
		else if(player.isClanLeader())
		{
			boolean cancelready = true;
			boolean canEnrollClan = true;
			boolean canEnrollAcadem = true;
			
			List<ClanListObject> listClan = ClanListServiceDAO.getInstance().getData(3);
			for(ClanListObject object : listClan)
				if(player.getClanId() == object.get_obj_id())
				{
					if(cancelready)
						add += "<tr><td align=center><button value=" + BottomValueCancel + " action=\"bypass _bbsscripts:services.ClanList:removeClanAcadem\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Info_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Info\"></td></tr>";
					canEnrollClan = false;
					cancelready = false;
					break;
				}
			
			listClan.clear();
			
			List<ClanListObject> listAcadem = ClanListServiceDAO.getInstance().getData(2);
			for(ClanListObject object : listAcadem)
				if(player.getClanId() == object.get_obj_id())
				{
					if(cancelready)
						add += "<tr><td align=center><button value=" + BottomValueCancel + " action=\"bypass _bbsscripts:services.ClanList:removeClanAcadem\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Info_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Info\"></td></tr>";
					canEnrollAcadem = false;
					cancelready = false;
					break;
				}
			
			listAcadem.clear();		
			
			if(canEnrollClan)
				add += "<tr><td align=center><button value=" + BottomValueEnrollClan + " action=\"bypass _bbsscripts:services.ClanList:addClan\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Info_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Info\"></td></tr>";
			if(player.getClan().getSubUnit(Clan.SUBUNIT_ACADEMY) != null && canEnrollAcadem)
				add += "<tr><td align=center><button value=" + BottomValueEnrollAcadem + " action=\"bypass _bbsscripts:services.ClanList:addAcadem\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Info_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Info\"></td></tr>";
			
			add += "<tr><td align=center><button value=" + BottomValueCandidate + " action=\"bypass _bbsscripts:services.ClanList:listCandidate\" width=200 height=26 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"></td></tr>";
		}
		else
			add += NoMercenaryAvail;
		
		String html = HtmCache.getInstance().getNotNull("scripts/services/clanlist.htm", player);
		html = html.replaceFirst("%toreplace%", add);
		show(html, player);
	}
	
	public void listClan()
	{
		Player player = getSelf();
		
		StringBuilder append = new StringBuilder();
		List<ClanListObject> list = ClanListServiceDAO.getInstance().getData(3);
		String listEmpty = player.isLangRus() ? "Вакансий нет" : "There are no vacancies";
		String gangin = player.isLangRus() ? "Набор в" : "Recruiting";
		
		if(!list.isEmpty())
		{
			append.append("<table>");
			for(ClanListObject object : list)
			{
				Clan clan = ClanTable.getInstance().getClan(object.get_obj_id());
				
				if(clan != null && clan.getUnitMembersSize(Clan.SUBUNIT_MAIN_CLAN) < clan.getSubPledgeLimit(Clan.SUBUNIT_MAIN_CLAN))
				{
					String clanId = Integer.toString(clan.getClanId());
					append.append("<tr><td>" + gangin + " <font color=00CCFF>" + clan.getName() + "</font> Ур." + clan.getLevel() + " <font color=LEVEL>Ур.Кандидата " + object.get_price() + "</font></td><td><button width=25 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ClanList:joinClan " + clanId + "\" value=\"+\"></td></tr>");
				}
				else
					ClanListServiceDAO.getInstance().deleteFromList(object.get_obj_id());
				
			}
			append.append("</table>");
		}
		else
			append.append(listEmpty);
		
		show(append.toString(), player, null);
		list.clear();
	}
	
	public void listAcadem()
	{
		Player player = getSelf();
		
		StringBuilder append = new StringBuilder();
		List<ClanListObject> list = ClanListServiceDAO.getInstance().getData(2);
		String listEmpty = player.isLangRus() ? "Вакансий нет" : "There are no vacancies";
		String gangin = player.isLangRus() ? "Набор в" : "Recruiting";
		
		if(!list.isEmpty())
		{
			append.append("<table>");
			for(ClanListObject object : list)
			{
				Clan clan = ClanTable.getInstance().getClan(object.get_obj_id());
				
				if(clan != null && clan.getUnitMembersSize(Clan.SUBUNIT_ACADEMY) < clan.getSubPledgeLimit(Clan.SUBUNIT_ACADEMY))
				{
					String clanId = Integer.toString(clan.getClanId());
					append.append("<tr><td>" + gangin + " <font color=00CCFF>" + clan.getName() + "</font> Ур." + clan.getLevel() + " <font color=LEVEL>Плата " + object.get_price() + "</font> " + ItemHolder.getInstance().getTemplate(object.get_item_id()).getName() + "</td><td><button width=25 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ClanList:joinAcadem " + clanId + "\" value=\"+\"></td></tr>");
				}
				else
					ClanListServiceDAO.getInstance().deleteFromList(object.get_obj_id());
			}
			append.append("</table>");
		}
		else
			append.append(listEmpty);
		
		show(append.toString(), player, null);
		list.clear();
	}
	
	public void listCandidate()
	{
		Player player = getSelf();
		
		StringBuilder append = new StringBuilder();
		List<ClanListObject> list = ClanListServiceDAO.getInstance().getData(1);
		String listEmpty = player.isLangRus() ? "Кандидатов нет" : "There are no candidates";
		String gangAcadem = player.isLangRus() ? "В академию" : "In Academy";
		String gangClan = player.isLangRus() ? "В клан" : "In Clan";
		
		if(!list.isEmpty())
		{
			append.append("<table>");
			for(ClanListObject object : list)
			{
				Player candidate = World.getPlayer(object.get_obj_id());
				
				if(candidate == null)
					continue;
				
				String candidateObjId = Integer.toString(object.get_obj_id());
					
				if(candidate.getClanId() == 0 && candidate.getLevel() < 40)
					append.append("<tr><td>" + gangAcadem + " <font color=00CCFF>" + candidate.getName() + "</font> Ур." + candidate.getLevel() + " <font color=LEVEL>Плата " + object.get_price() + "</font> " + ItemHolder.getInstance().getTemplate(object.get_item_id()).getName() + "</td><td><button width=25 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ClanList:inviteCandidate -1 " + candidateObjId + "\" value=\"+\"></td></tr>");
				else if(candidate.getClanId() == 0)
					append.append("<tr><td>" + gangClan + " <font color=00CCFF>" + candidate.getName() + "</font> Ур." + candidate.getLevel() + " <font color=LEVEL>Плата " + object.get_price() + "</font> " + ItemHolder.getInstance().getTemplate(object.get_item_id()).getName() + "</td><td><button width=25 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ClanList:inviteCandidate 0 " + candidateObjId + "\" value=\"+\"></td></tr>");
				else
					ClanListServiceDAO.getInstance().deleteFromList(object.get_obj_id());
			}
			append.append("</table>");
		}
		else
			append.append(listEmpty);
		
		show(append.toString(), player, null);
		list.clear();
	}
	
	public void addClan()
	{
		Player player = getSelf();
		
		StringBuilder append = new StringBuilder();
		
		String info = player.isLangRus() ? "Укажите минимальный уровень кандидата." : "Specify the minimum level of the candidate.";
		String no = player.isLangRus() ? "В вашем клане нет мест." : "Your clan has no free slot.";
		Clan clan = player.getClan();
		
		if(clan.getUnitMembersSize(Clan.SUBUNIT_MAIN_CLAN) < clan.getSubPledgeLimit(Clan.SUBUNIT_MAIN_CLAN))
		{
			append.append(info);
			append.append("<table><tr>");
			append.append("<td><edit var=\"count\" width=100 height=12></td>");
			append.append("<td><button width=25 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ClanList:DoAddClan $count\" value=\"+\"></td>");
			append.append("</tr></table>");
		}
		else
			append.append(no);
		
		show(append.toString(), player, null);
	}
	
	public void addAcadem()
	{
		Player player = getSelf();
		
		StringBuilder append = new StringBuilder();
		
		String info = player.isLangRus() ? "Укажите плату за одного наемника." : "Specify a fee for a mercenary.";
		String no = player.isLangRus() ? "В вашей академии нет мест." : "В вашей академии нет мест.";
		Clan clan = player.getClan();
		
		if(clan.getSubUnit(Clan.SUBUNIT_ACADEMY) != null && clan.getUnitMembersSize(Clan.SUBUNIT_ACADEMY) < clan.getSubPledgeLimit(Clan.SUBUNIT_ACADEMY))
		{
			append.append(info);
			append.append("<table><tr>");
			append.append("<td><edit var=\"count\" width=100 height=12></td>");
			append.append("<td><button width=25 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ClanList:DoAddAcadem $count\" value=\"+\"></td>");	
			append.append("</tr></table>");
		}
		else
			append.append(no);
		
		show(append.toString(), player, null);
	}
	
	public void addCandidate()
	{
		Player player = getSelf();
		
		StringBuilder append = new StringBuilder();
		
		String info = player.isLangRus() ? "Укажите желаемую награду за найм." : "Specify the desired reward for hiring.";
		String no = player.isLangRus() ? "Вы уже состоите в клане." : "You are already in a clan.";
		
		if(player.getClanId() == 0)
		{
			append.append(info);
			append.append("<table><tr>");
			append.append("<td><edit var=\"count\" width=100 height=12></td>");
			append.append("<td><button width=25 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ClanList:DoAddCandidate $count\" value=\"+\"></td>");
			append.append("</tr></table>");
		}
		else
			append.append(no);	
		
		show(append.toString(), player, null);
	}
	
	public void DoAddClan(String[] param)
	{
		Player player = getSelf();
		
		int count = Integer.parseInt(param[0]);
		if(count > 39 && count < 86)
		{
			if(player.getClan().getUnitMembersSize(Clan.SUBUNIT_MAIN_CLAN) < player.getClan().getSubPledgeLimit(Clan.SUBUNIT_MAIN_CLAN))
			{
				player.setLectureMark(2);
				ClanListServiceDAO.getInstance().insertToList(player.getClanId(), 3, ItemTemplate.ITEM_ID_ADENA, count);
				player.broadcastUserInfo(true);
			}
			else
				player.sendMessage(player.isLangRus() ? "В вашем клане нет мест." : "Your clan has no free slot.");
		}
		else
			player.sendMessage(player.isLangRus() ? "Введен некорректный уровень." : "Entered an incorrect level.");
		
		list();
	}
	
	public void DoAddAcadem(String[] param)
	{
		Player player = getSelf();
		
		int count = Integer.parseInt(param[0]);
		if(count > 0 && count < 999999999)
		{
			if(player.getClan().getUnitMembersSize(Clan.SUBUNIT_ACADEMY) < player.getClan().getSubPledgeLimit(Clan.SUBUNIT_ACADEMY))
			{
				if(player.getInventory().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, count))
				{
					player.setLectureMark(2);
					ClanListServiceDAO.getInstance().insertToList(player.getClanId(), 2, ItemTemplate.ITEM_ID_ADENA, count);
					player.broadcastUserInfo(true);
				}
				else
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			else
				player.sendMessage(player.isLangRus() ? "В вашем академии нет мест." : "Your academy has no free slot.");
		}
		else
			player.sendMessage(player.isLangRus() ? "Введена некорректная сумма." : "Entered an incorrect amount.");
		
		list();
	}
	
	public void DoAddCandidate(String[] param)
	{
		Player player = getSelf();
		
		int count = Integer.parseInt(param[0]);
		if(count > 0 && count < 999999999)
		{
			if(player.getClanId() == 0)
			{
				player.setLectureMark(1);
				ClanListServiceDAO.getInstance().insertToList(player.getObjectId(), 1, ItemTemplate.ITEM_ID_ADENA, count);
				player.broadcastUserInfo(true);
			}
			else
				player.sendMessage(player.isLangRus() ? "Вы уже состоите в клане." : "You are already in a clan.");
		}
		else
			player.sendMessage(player.isLangRus() ? "Введена некорректная сумма." : "Entered an incorrect amount.");
		
		list();
	}
	
	public void removeClanAcadem()
	{
		Player player = getSelf();
		
		if(player.isClanLeader())
		{
			ClanListServiceDAO.getInstance().deleteFromList(player.getClanId());
			player.setLectureMark(3);
			player.broadcastUserInfo(true);
		}
		else
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
		
		list();
	}
	
	public void removeCandidate()
	{
		Player player = getSelf();
		
		ClanListServiceDAO.getInstance().deleteFromList(player.getObjectId());
		player.setLectureMark(3);
		player.broadcastUserInfo(true);
		
		list();
	}
	
	public void joinClan(String[] param)
	{
		Player player = getSelf();
		Clan clan = ClanTable.getInstance().getClan(Integer.parseInt(param[0]));	
		
		int requrelvl = 99;
		
		List<ClanListObject> listClan = ClanListServiceDAO.getInstance().getData(3);
		for(ClanListObject object : listClan)
			if(clan.getClanId() == object.get_obj_id())
			{
				requrelvl = object.get_price();
				break;
			}
		
		listClan.clear();
		
		if(player.getClanId() != 0)
		{
			player.sendMessage(player.isLangRus() ? "Вы уже состоите в клане." : "You are already in a clan.");
			list();
			return;	
		}
		else if(player.getLevel() < 40 || player.getLevel() < requrelvl)
		{
			player.sendMessage(player.isLangRus() ? "Ваш уровень не соответствует требованиям." : "Your level does not meet the requirements.");
			list();
			return;
		}
		else if(clan.getUnitMembersSize(Clan.SUBUNIT_MAIN_CLAN) >= clan.getSubPledgeLimit(Clan.SUBUNIT_MAIN_CLAN))
		{
			player.sendMessage(player.isLangRus() ? "В клане нет мест." : "Clan has no free slot.");
			list();
			return;
		}
		else
		{
			player.sendPacket(new JoinPledge(clan.getClanId()));

			int pledgeType = Clan.SUBUNIT_MAIN_CLAN;
			SubUnit subUnit = clan.getSubUnit(pledgeType);
			if(subUnit == null)
				return;

			UnitMember member = new UnitMember(clan, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), pledgeType, player.getPowerGrade(), player.getApprentice(), player.getSex(), Clan.SUBUNIT_NONE);
			subUnit.addUnitMember(member);

			player.setPledgeType(pledgeType);
			player.setClan(clan);

			member.setPlayerInstance(player, false);

			member.setPowerGrade(clan.getAffiliationRank(player.getPledgeType()));

			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(member), player);
			clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.S1_HAS_JOINED_THE_CLAN).addString(player.getName()), new PledgeShowInfoUpdate(clan));

			player.sendPacket(SystemMsg.ENTERED_THE_CLAN);
			player.sendPacket(player.getClan().listAll());
			player.setLeaveClanTime(0);
			player.updatePledgeClass();
			clan.addSkillsQuietly(player);
			player.sendPacket(new PledgeSkillList(clan));
			player.sendPacket(new SkillList(player));

			EventHolder.getInstance().findEvent(player);
			
			if(clan.getWarDominion() > 0)
			{
				DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
				siegeEvent.updatePlayer(player, true);
			}

			player.store(false);
			
			player.setLectureMark(3);
			player.broadcastUserInfo(true);
			
			ClanListServiceDAO.getInstance().deleteFromList(player.getObjectId());
			
			list();
		}
	}
	
	public void joinAcadem(String[] param)
	{
		Player player = getSelf();
		Clan clan = ClanTable.getInstance().getClan(Integer.parseInt(param[0]));
		
		int price = 0;
		
		List<ClanListObject> listAcadem = ClanListServiceDAO.getInstance().getData(2);
		for(ClanListObject object : listAcadem)
			if(clan.getClanId() == object.get_obj_id())
			{
				price = object.get_price();
				break;
			}
		
		listAcadem.clear();
		
		if(player.getClanId() != 0)
		{
			player.sendMessage(player.isLangRus() ? "Вы уже состоите в клане." : "You are already in a clan.");
			list();
			return;	
		}
		else if(player.getLevel() > 39)
		{
			player.sendMessage(player.isLangRus() ? "Ваш уровень не соответствует требованиям." : "Your level does not meet the requirements.");
			list();
			return;
		}
		else if(clan.getUnitMembersSize(Clan.SUBUNIT_ACADEMY) >= clan.getSubPledgeLimit(Clan.SUBUNIT_ACADEMY))
		{
			player.sendMessage(player.isLangRus() ? "В академии нет мест." : "Academy has no free slot.");
			list();
			return;
		}
		else if(clan.getAdenaCount() < price)
		{
			player.sendMessage(player.isLangRus() ? "В казне клана нет средств для оплаты вашего найма." : "In the treasury of the clan can not afford to pay your rent.");
			ClanListServiceDAO.getInstance().deleteFromList(clan.getClanId());
			
			Player clanLeader = World.getPlayer(clan.getLeaderId());
			if(clanLeader != null)
			{
				clanLeader.sendMessage(player.isLangRus() ? "В казне клана больше нет средств для оплаты наемников." : "In the treasury of the clan no longer have the means to pay mercenaries.");
				clanLeader.setLectureMark(3);
				clanLeader.broadcastUserInfo(true);
			}
			
			list();
			return;
		}
		else
		{
			if(player.getVar("ClanListAcadem") != null)
				player.unsetVar("ClanListAcadem");		
			clan.getWarehouse().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, price);
			player.setVar("ClanListAcadem", price, -1);
			
			player.sendPacket(new JoinPledge(clan.getClanId()));

			int pledgeType = Clan.SUBUNIT_ACADEMY;
			SubUnit subUnit = clan.getSubUnit(pledgeType);
			if(subUnit == null)
				return;

			UnitMember member = new UnitMember(clan, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), pledgeType, player.getPowerGrade(), player.getApprentice(), player.getSex(), Clan.SUBUNIT_NONE);
			subUnit.addUnitMember(member);

			player.setPledgeType(pledgeType);
			player.setClan(clan);

			member.setPlayerInstance(player, false);

			player.setLvlJoinedAcademy(player.getLevel());

			member.setPowerGrade(clan.getAffiliationRank(player.getPledgeType()));

			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(member), player);
			clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.S1_HAS_JOINED_THE_CLAN).addString(player.getName()), new PledgeShowInfoUpdate(clan));

			player.sendPacket(SystemMsg.ENTERED_THE_CLAN);
			player.sendPacket(player.getClan().listAll());
			player.setLeaveClanTime(0);
			player.updatePledgeClass();
			clan.addSkillsQuietly(player);
			player.sendPacket(new PledgeSkillList(clan));
			player.sendPacket(new SkillList(player));

			EventHolder.getInstance().findEvent(player);
			if(clan.getWarDominion() > 0)
			{
				DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
				siegeEvent.updatePlayer(player, true);
			}

			player.store(false);
			
			player.setLectureMark(3);
			player.broadcastUserInfo(true);
			
			ClanListServiceDAO.getInstance().deleteFromList(player.getObjectId());
			
			list();
		}
	}
	
	public void inviteCandidate(String[] param)
	{
		Player player = getSelf();
		Player candidate = World.getPlayer(Integer.parseInt(param[1]));
		int pledgeType = Integer.parseInt(param[0]);
		Clan clan = player.getClan();
		
		int price = 0;
		
		List<ClanListObject> listCandidate = ClanListServiceDAO.getInstance().getData(1);
		for(ClanListObject object : listCandidate)
			if(Integer.parseInt(param[1]) == object.get_obj_id())
			{
				price = object.get_price();
				break;
			}
		
		listCandidate.clear();
		
		if(candidate == null)
		{
			player.sendMessage(player.isLangRus() ? "Кандидат уже не в сети." : "Сandidate is not online.");
			list();
			return;
		}
		else if(candidate.getClanId() != 0)
		{
			player.sendMessage(player.isLangRus() ? "Кандидат уже состоит в клане." : "Сandidate is already in a clan.");
			list();
			return;	
		}
		else if(candidate.getLevel() < 40 && pledgeType == Clan.SUBUNIT_MAIN_CLAN)
		{
			player.sendMessage(player.isLangRus() ? "Уровень кандидата не соответствует требованиям." : "Level of the candidate does not meet the requirements.");
			ClanListServiceDAO.getInstance().deleteFromList(candidate.getObjectId());
			candidate.setLectureMark(3);
			candidate.broadcastUserInfo(true);
			list();
			return;
		}
		else if(candidate.getLevel() > 39 && pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			player.sendMessage(player.isLangRus() ? "Уровень кандидата не соответствует требованиям." : "Level of the candidate does not meet the requirements.");
			ClanListServiceDAO.getInstance().deleteFromList(candidate.getObjectId());
			candidate.setLectureMark(3);
			candidate.broadcastUserInfo(true);
			list();
			return;
		}
		else if(clan.getSubUnit(pledgeType) == null || clan.getUnitMembersSize(pledgeType) >= clan.getSubPledgeLimit(pledgeType))
		{
			player.sendMessage(player.isLangRus() ? "Нет мест." : "No free slot.");
			list();
			return;
		}
		else if(clan.getAdenaCount() < price)
		{
			player.sendMessage(player.isLangRus() ? "В казне клана нет средств для оплаты этого наемника." : "In the treasury of the clan can not afford to pay for this mercenary.");	
			list();
			return;
		}
		else
		{
			if(pledgeType == Clan.SUBUNIT_MAIN_CLAN)
			{
				clan.getWarehouse().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, price);
				candidate.addAdena(price, true);
			}
			else if(pledgeType == Clan.SUBUNIT_ACADEMY)
			{
				if(candidate.getVar("ClanListAcadem") != null)
					candidate.unsetVar("ClanListAcadem");		
				clan.getWarehouse().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, price);
				candidate.setVar("ClanListAcadem", price, -1);
			}
			
			candidate.sendPacket(new JoinPledge(clan.getClanId()));

			SubUnit subUnit = clan.getSubUnit(pledgeType);
			if(subUnit == null)
				return;

			UnitMember member = new UnitMember(clan, candidate.getName(), candidate.getTitle(), candidate.getLevel(), candidate.getClassId().getId(), candidate.getObjectId(), pledgeType, candidate.getPowerGrade(), candidate.getApprentice(), candidate.getSex(), Clan.SUBUNIT_NONE);
			subUnit.addUnitMember(member);

			candidate.setPledgeType(pledgeType);
			candidate.setClan(clan);

			member.setPlayerInstance(candidate, false);

			if(pledgeType == Clan.SUBUNIT_ACADEMY)
				candidate.setLvlJoinedAcademy(candidate.getLevel());

			member.setPowerGrade(clan.getAffiliationRank(candidate.getPledgeType()));

			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(member), candidate);
			clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.S1_HAS_JOINED_THE_CLAN).addString(candidate.getName()), new PledgeShowInfoUpdate(clan));

			candidate.sendPacket(SystemMsg.ENTERED_THE_CLAN);
			candidate.sendPacket(candidate.getClan().listAll());
			candidate.setLeaveClanTime(0);
			candidate.updatePledgeClass();
			clan.addSkillsQuietly(candidate);
			candidate.sendPacket(new PledgeSkillList(clan));
			candidate.sendPacket(new SkillList(candidate));

			EventHolder.getInstance().findEvent(candidate);
			if(clan.getWarDominion() > 0)
			{
				DominionSiegeEvent siegeEvent = candidate.getEvent(DominionSiegeEvent.class);
				siegeEvent.updatePlayer(candidate, true);
			}

			candidate.store(false);
			
			candidate.setLectureMark(3);
			candidate.broadcastUserInfo(true);
			
			ClanListServiceDAO.getInstance().deleteFromList(candidate.getObjectId());
			
			list();
		}
	}
}