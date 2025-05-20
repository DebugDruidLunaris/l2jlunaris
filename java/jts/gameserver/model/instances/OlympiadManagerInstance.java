package jts.gameserver.model.instances;

import java.util.List;

import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.model.entity.olympiad.CompType;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.entity.olympiad.OlympiadDatabase;
import jts.gameserver.network.serverpackets.ExHeroList;
import jts.gameserver.network.serverpackets.ExReceiveOlympiad;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.npc.NpcTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class OlympiadManagerInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadManagerInstance.class);

	public OlympiadManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		if(Config.OLYMPIAD_ENABLE && template.npcId == 31688)
			Olympiad.addOlympiadNpc(this);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(checkForDominionWard(player))
			return;

		if(!Config.OLYMPIAD_ENABLE)
			return;

		if(command.startsWith("OlympiadNoble"))
		{
			if(!Config.OLYMPIAD_ENABLE)
				return;

			int val = Integer.parseInt(command.substring(14));
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);

			switch(val)
			{
				case 1:
					Olympiad.unRegisterNoble(player);
					showChatWindow(player, 0);
					break;
				case 2:
					if(Olympiad.isRegistered(player))
					{
						player.sendPacket(html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "manager_noregister.htm"));
					}
					else
					{
						player.sendPacket(html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "manager_register.htm"));
						html.replace("%period%", String.valueOf(Olympiad.getPeriod()));
						html.replace("%cycle%", String.valueOf(Olympiad.getCurrentCycle()));
						html.replace("%opponents%", String.valueOf(Olympiad.getCountOpponents()));
						player.sendPacket(html);
					}

					break;
				case 4:
					Olympiad.registerNoble(player, CompType.NON_CLASSED);
					break;
				case 5:
					Olympiad.registerNoble(player, CompType.CLASSED);
					break;
				case 6:
					int passes = Olympiad.getNoblessePasses(player);
					if(passes > 0)
					{
						player.getInventory().addItem(Config.OLYMPIAD_COMP_RITEM, passes);
						player.sendPacket(SystemMessage2.obtainItems(Config.OLYMPIAD_COMP_RITEM, passes, 0));
					}
					else
						player.sendPacket(html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "manager_nopoints.htm"));
					break;
				case 7:
					MultiSellHolder.getInstance().SeparateAndSend(102, player, 0);
					break;
				case 9:
					MultiSellHolder.getInstance().SeparateAndSend(103, player, 0);
					break;
				case 10:
					Olympiad.registerNoble(player, CompType.TEAM);
					break;
				default:
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else if(command.startsWith("Olympiad"))
		{
			if(!Config.OLYMPIAD_ENABLE)
				return;
			int val = Integer.parseInt(command.substring(9, 10));

			NpcHtmlMessage reply = new NpcHtmlMessage(player, this);

			switch(val)
			{
				case 1:
					if(!Olympiad.inCompPeriod() || Olympiad.isOlympiadEnd())
					{
						player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
						return;
					}
					player.sendPacket(new ExReceiveOlympiad.MatchList());
					break;
				case 2:
					// for example >> Olympiad 1_88
					int classId = Integer.parseInt(command.substring(11));
					if(classId >= 88)
					{
						reply.setFile(Olympiad.OLYMPIAD_HTML_PATH + "manager_ranking.htm");

						List<String> names = OlympiadDatabase.getClassLeaderBoard(classId);

						int index = 1;
						for(String name : names)
						{
							reply.replace("%place" + index + "%", String.valueOf(index));
							reply.replace("%rank" + index + "%", name);
							index++;
							if(index > 10)
								break;
						}
						for(; index <= 10; index++)
						{
							reply.replace("%place" + index + "%", "");
							reply.replace("%rank" + index + "%", "");
						}

						player.sendPacket(reply);
					}
					// TODO Send player each class rank
					break;
				case 3:
					if(!Config.OLYMPIAD_ENABLE_SPECTATING)
						break;
					Olympiad.addSpectator(Integer.parseInt(command.substring(11)), player);
					break;
				case 4:
					player.sendPacket(new ExHeroList());
					break;
				case 5:
					if(Hero.getInstance().isInactiveHero(player.getObjectId()))
					{
						Hero.getInstance().activateHero(player);
						reply.setFile(Olympiad.OLYMPIAD_HTML_PATH + "monument_give_hero.htm");
					}
					else
						reply.setFile(Olympiad.OLYMPIAD_HTML_PATH + "monument_dont_hero.htm");
					player.sendPacket(reply);
					break;
				default:
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(checkForDominionWard(player))
			return;

		String fileName = Olympiad.OLYMPIAD_HTML_PATH;
		int npcId = getNpcId();
		switch(npcId)
		{
			case 31688: // Grand Olympiad Manager
				fileName += "manager";
				break;
			default: // Monument of Heroes
				fileName += "monument";
				break;
		}
		if(player.isNoble())
			fileName += "_n";
		if(val > 0)
			fileName += "-" + val;
		fileName += ".htm";
		player.sendPacket(new NpcHtmlMessage(player, this, fileName, val));
	}
}