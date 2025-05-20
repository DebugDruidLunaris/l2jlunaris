package jts.gameserver.handler.voicecommands.impl;

import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.handler.voicecommands.VoicedCommandHandler;
import jts.gameserver.listener.script.OnInitScriptListener;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.actor.instances.player.Bonus;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;

public class PartyInfo
implements IVoicedCommandHandler, OnInitScriptListener
{
private String[] _commandList = { "party" };

  public boolean useVoicedCommand(String command, Player activeChar, String target)
  {
    if (command.equals("party"))
    {
      Party party = activeChar.getParty();
      if (party == null) {
        return false;
      }
      String html = HtmCache.getInstance().getHtml("scripts/services/Party/index.htm", activeChar);
      int i = 1;
      
      String leader = party.getPartyLeader().getName();
      html = html.replace("{leader}", leader);
      Bonus premium = null;
     if (activeChar.hasBonus()) {
        premium = activeChar.getBonus();
      }
      int result = premium != null ? (int)((premium.getRateXp() - 1.0D) * 100.0D) : 0;
      html = html.replace("{myrate}", result > 0 ? "<font color=\"LEVEL\">+" + result + "%</font>" : "<font color=\"669933\">+0%</font>");
      double rate = 0.0D;
      int level = 0;
      for (Player player : party.getPartyMembers())
      {
        String name = player.getName().length() > 15 ? player.getName().substring(0, 14) : player.getName();
        html = html.replace("{player_" + i + "}", player.getName().equals(leader) ? "<font color=\"3399CC\">" + name + "</font>" : player.getName().equals(activeChar.getName()) ? "<font color=\"FF3333\">" + name + "</font>" : name);
        int myLevel = player.getLevel();
        level += myLevel;
        html = html.replace("{level_" + i + "}", String.valueOf(myLevel));
        if (player.getNetConnection() != null)
        {
          Bonus bonus = null;
          if (player.hasBonus()) {
            bonus = player.getBonus();
          }
          int result2 = bonus != null ? (int)((bonus.getRateXp() - 1.0D) * 100.0D) : 0;
          html = html.replace("{rate_" + i + "}", result > 0 ? "<font color=\"LEVEL\">+" + result2 + "%</font>" : "<font color=\"669933\">+0%</font>");
          
          rate += result2;
        }
        else
        {
          html = html.replace("{rate_" + i + "}", "<font color\"FF0000\">no connection</font>");
          rate += 1.0D;
        }
        i++;
      }
      int count = party.getMemberCount();
      double result3 = rate / count;
      if (result3 < 0.0D) {
        result3 = 0.0D;
      }
      html = html.replace("{count}", String.valueOf(count));
      html = html.replace("{rate}", "+" + result3 + "%");
      html = html.replace("{level}", String.valueOf(level / count));
      for (int j = 1; j <= 10; j++)
      {
        html = html.replace("{player_" + j + "}", "...");
        html = html.replace("{rate_" + j + "}", "...");
        html = html.replace("{level_" + j + "}", "...");
      }
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile(html));
      return true;
    }
    return true;
  }
  
  public void onInit()
  {
    VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
  }
  
  public String[] getVoicedCommandList()
  {
    return this._commandList;
  }
}
