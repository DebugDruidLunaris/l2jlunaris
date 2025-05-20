package jts.gameserver.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.captcha.CaptchaValidator;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.handler.admincommands.AdminCommandHandler;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.handler.voicecommands.VoicedCommandHandler;
import jts.gameserver.instancemanager.BypassManager.DecodedBypass;
import jts.gameserver.instancemanager.OfflineBufferManager;
import jts.gameserver.instancemanager.OlympiadHistoryManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.OlympiadManagerInstance;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBypassToServer extends L2GameClientPacket
{
	//Format: cS
	private static final Logger _log = LoggerFactory.getLogger(RequestBypassToServer.class);

	private DecodedBypass bp = null;

	@Override
	protected void readImpl()
	{
		String bypass = readS();
		if(!bypass.isEmpty())
		{
			bp = getClient().getActiveChar().decodeBypass(bypass);
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || bp == null)
			return;
		/*	if(bp.bypass.startsWith("_bbsbuff;buff;"))
			{
				StringTokenizer buffOne = new StringTokenizer(bp.bypass, ";");
				buffOne.nextToken();
				buffOne.nextToken();
				int BuffIdUse = Integer.parseInt(buffOne.nextToken());
				int BuffLvL = Integer.parseInt(buffOne.nextToken());
				if(!Config.BBS_BUFFER_ALLOWED_BUFF.contains(BuffIdUse))
				{
					activeChar.sendMessage("You banned and disconnected!");
					_log.warn("Player: " + activeChar + " used not allow buff: " + BuffIdUse + " lvl " + BuffLvL + " - Player: " + activeChar + " BANNED!!! Packet");
					activeChar.setAccessLevel(-100); // banned (Access Level - 100)
					activeChar.kick(); // Kick player to server
					return;
				}
			}*/
		try
		{
			NpcInstance npc = activeChar.getLastNpc();
			GameObject target = activeChar.getTarget();
			if(npc == null && target != null && target.isNpc())
			{
				npc = (NpcInstance) target;
			}

			if(bp.bypass.startsWith("admin_"))
			{
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bp.bypass);
			}
			else if((bp.bypass.startsWith("_bbs") || bp.bypass.startsWith("_mail") || bp.bypass.startsWith("_friend")) && (activeChar.isDead() && !Config.BBS_CHECK_DEATH || activeChar.isMovementDisabled() && !Config.BBS_CHECK_MOVEMENT_DISABLE || activeChar.isOnSiegeField() && !Config.BBS_CHECK_ON_SIEGE_FIELD || activeChar.isInCombat() && !Config.BBS_CHECK_IN_COMBAT || activeChar.isAttackingNow() && !Config.BBS_CHECK_ATTACKING_NOW || activeChar.isInOlympiadMode() && !Config.BBS_CHECK_IN_OLYMPIAD_MODE || activeChar.getVar("jailed") != null || activeChar.isFlying() && !Config.BBS_CHECK_FLYING || activeChar.isInDuel() && !Config.BBS_CHECK_IN_DUEL || activeChar.getReflectionId() > 0 && !Config.BBS_CHECK_IN_INSTANCE || activeChar.isOutOfControl() && !Config.BBS_CHECK_OUT_OF_CONTROL || (activeChar.isInDeathMatch() | activeChar.isInCtF() | activeChar.isInLastHero() | activeChar.isInFightClub() | activeChar.isInTvT() | activeChar.isInTVTArena()) == true && !Config.BBS_CHECK_IN_EVENT || !activeChar.isInZonePeace() && activeChar.getNetConnection().getBonus() <= 1 && Config.BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM))
			{
				activeChar.sendPacket(new ExShowScreenMessage(new CustomMessage("communityboard.checkcondition.false.screen", activeChar).toString(), 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				activeChar.sendMessage(new CustomMessage("communityboard.checkcondition.false.chat", activeChar));

				String html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/terms.htm", activeChar);
				String check = " <font color=\"LEVEL\">*</font>";
				String isTrue = "<font color=\"66FF33\">" + new CustomMessage("common.allowed", activeChar).toString() + "</font>";
				String isFalse = "<font color=\"FF0000\">" + new CustomMessage("common.prohibited", activeChar).toString() + "</font>";
				String onlyPremium = "<font color=\"LEVEL\">" + new CustomMessage("common.need.premium", activeChar).toString() + "</font>";

				html = html.replace("%config_isInZonePeace%", Config.BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM ? onlyPremium : isTrue);
				html = html.replace("%config_isDead%", Config.BBS_CHECK_DEATH ? isTrue : isFalse);
				html = html.replace("%config_isMovementDisabled%", Config.BBS_CHECK_MOVEMENT_DISABLE ? isTrue : isFalse);
				html = html.replace("%config_isOnSiegeField%", Config.BBS_CHECK_ON_SIEGE_FIELD ? isTrue : isFalse);
				html = html.replace("%config_isInCombat%", Config.BBS_CHECK_IN_COMBAT ? isTrue : isFalse);
				html = html.replace("%config_isAttackingNow%", Config.BBS_CHECK_ATTACKING_NOW ? isTrue : isFalse);
				html = html.replace("%config_isInOlympiadMode%", Config.BBS_CHECK_IN_OLYMPIAD_MODE ? isTrue : isFalse);
				html = html.replace("%config_isFlying%", Config.BBS_CHECK_FLYING ? isTrue : isFalse);
				html = html.replace("%config_isInDuel%", Config.BBS_CHECK_IN_DUEL ? isTrue : isFalse);
				html = html.replace("%config_isInInstance%", Config.BBS_CHECK_IN_INSTANCE ? isTrue : isFalse);
				html = html.replace("%config_isInJailed%", Config.BBS_CHECK_IN_JAILED ? isTrue : isFalse);
				html = html.replace("%config_isOutOfControl%", Config.BBS_CHECK_OUT_OF_CONTROL ? isTrue : isFalse);
				html = html.replace("%config_isInEvent%", Config.BBS_CHECK_IN_EVENT ? isTrue : isFalse);

				html = html.replace("%check_isInZonePeace%", !activeChar.isInZonePeace() && activeChar.getNetConnection().getBonus() <= 1 && Config.BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM ? check : "");
				html = html.replace("%check_isDead%", activeChar.isDead() && !Config.BBS_CHECK_DEATH ? check : "");
				html = html.replace("%check_isMovementDisabled%", activeChar.isMovementDisabled() && !Config.BBS_CHECK_MOVEMENT_DISABLE ? check : "");
				html = html.replace("%check_isOnSiegeField%", activeChar.isOnSiegeField() && !Config.BBS_CHECK_ON_SIEGE_FIELD ? check : "");
				html = html.replace("%check_isInCombat%", activeChar.isInCombat() && !Config.BBS_CHECK_IN_COMBAT ? check : "");
				html = html.replace("%check_isAttackingNow%", activeChar.isAttackingNow() && !Config.BBS_CHECK_ATTACKING_NOW ? check : "");
				html = html.replace("%check_isInOlympiadMode%", activeChar.isInOlympiadMode() && !Config.BBS_CHECK_IN_OLYMPIAD_MODE ? check : "");
				html = html.replace("%check_isFlying%", activeChar.isFlying() && !Config.BBS_CHECK_FLYING ? check : "");
				html = html.replace("%check_isInDuel%", (activeChar.isInDuel() || activeChar.getIsInDuel()) && !Config.BBS_CHECK_IN_DUEL ? check : "");
				html = html.replace("%check_isInInstance%", activeChar.getReflectionId() > 0 && !Config.BBS_CHECK_IN_INSTANCE ? check : "");
				html = html.replace("%check_isInJailed%", activeChar.getVar("jailed") != null && !Config.BBS_CHECK_IN_JAILED ? check : "");
				html = html.replace("%check_isOutOfControl%", activeChar.isOutOfControl() && !Config.BBS_CHECK_OUT_OF_CONTROL ? check : "");
				html = html.replace("%check_isInEvent%", (activeChar.isInDeathMatch() |activeChar.isInTvT() | activeChar.isInCtF() | activeChar.isInTvT() | activeChar.isInLastHero() | activeChar.isInFightClub() | activeChar.isInTVTArena()) == true && !Config.BBS_CHECK_IN_EVENT ? check : "");

				ShowBoard.separateAndSend(html, activeChar);
				return;
			}
			else if(bp.bypass.equals("come_here") && activeChar.isGM())
			{
				comeHere(getClient());
			}
			else if(bp.bypass.startsWith("player_help "))
			{
				playerHelp(activeChar, bp.bypass.substring(12));
			}
			else if(bp.bypass.startsWith("scripts_"))
			{
				String command = bp.bypass.substring(8).trim();
				String[] word = command.split("\\s+");
				String[] args = command.substring(word[0].length()).trim().split("\\s+");
				String[] path = word[0].split(":");
				if(path.length != 2)
				{
					_log.warn("Bad Script bypass!");
					return;
				}

				Map<String, Object> variables = null;
				if(npc != null)
				{
					variables = new HashMap<String, Object>(1);
					variables.put("npc", npc.getRef());
				}

				if(word.length == 1)
				{
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], variables);
				}
				else
				{
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], new Object[] { args }, variables);
				}
			}
			else if(bp.bypass.startsWith("user_"))
			{
				String command = bp.bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);

				if(vch != null)
				{
					vch.useVoicedCommand(word, activeChar, args);
				}
				else
				{
					_log.warn("Unknow voiced command '" + word + "'");
				}
			}
			else if(bp.bypass.startsWith("npc_"))
			{
				int endOfId = bp.bypass.indexOf('_', 5);
				String id;
				if(endOfId > 0)
				{
					id = bp.bypass.substring(4, endOfId);
				}
				else
				{
					id = bp.bypass.substring(4);
				}
				GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
				if(object != null && object.isNpc() && endOfId > 0 && activeChar.isInRange(object.getLoc(), Creature.INTERACTION_DISTANCE))
				{
					activeChar.setLastNpc((NpcInstance) object);
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
				}
			}
			else if(bp.bypass.startsWith("_olympiad?"))
			{
				String[] ar = bp.bypass.replace("_olympiad?", "").split("&");
				String firstVal = ar[0].split("=")[1];
				String secondVal = ar[1].split("=")[1];

				if(firstVal.equalsIgnoreCase("move_op_field"))
				{
					if(!Config.OLYMPIAD_ENABLE_SPECTATING)
						return;

					// Переход в просмотр олимпа разрешен только от менеджера или с арены.
					if(activeChar.getLastNpc() instanceof OlympiadManagerInstance && activeChar.getLastNpc().isInRange(activeChar, Creature.INTERACTION_DISTANCE) || activeChar.getOlympiadObserveGame() != null)
					{
						Olympiad.addSpectator(Integer.parseInt(secondVal) - 1, activeChar);
					}
				}
			}
			else if(bp.bypass.startsWith("_diary"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if(heroid > 0)
				{
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
				}
			}
			else if(bp.bypass.startsWith("_match"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);

				OlympiadHistoryManager.getInstance().showHistory(activeChar, heroclass, heropage);
			}
			else if(bp.bypass.startsWith("manor_menu_select?")) // Navigate throught Manor windows
			{
				GameObject object = activeChar.getTarget();
				if(object != null && object.isNpc())
				{
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass);
				}
			}
			else if(bp.bypass.startsWith("multisell "))
			{
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(bp.bypass.substring(10)), activeChar, 0);
			}
			else if(bp.bypass.startsWith("Quest "))
			{
				String p = bp.bypass.substring(6).trim();
				int idx = p.indexOf(' ');
				if(idx < 0)
				{
					activeChar.processQuestEvent(p, "", npc);
				}
				else
				{
					activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim(), npc);
				}
			}
			else if (bp.bypass.startsWith("BuffStore"))
			{
				try
				{
					OfflineBufferManager.getInstance().processBypass(activeChar, bp.bypass);
				}
				catch (Exception ex) {}
			}
			else if(bp.handler != null)
				if(!Config.COMMUNITYBOARD_ENABLED)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
				}
				else
				{
					bp.handler.onBypassCommand(activeChar, bp.bypass);
				}
				else if(bp.bypass.startsWith("captcha_"))
					CaptchaValidator.processCaptchaBypass(bp.bypass, activeChar);
			}
		catch(Exception e)
		{

		}
	}

	private static void comeHere(GameClient client)
	{
		GameObject obj = client.getActiveChar().getTarget();
		if(obj != null && obj.isNpc())
		{
			NpcInstance temp = (NpcInstance) obj;
			Player activeChar = client.getActiveChar();
			temp.setTarget(activeChar);
			temp.moveToLocation(activeChar.getLoc(), 0, true);
		}
	}

	private static void playerHelp(Player activeChar, String path)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(path);
		activeChar.sendPacket(html);
	}
}