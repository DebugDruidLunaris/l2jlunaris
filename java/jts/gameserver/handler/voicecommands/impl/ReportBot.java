package jts.gameserver.handler.voicecommands.impl;
 
 import jts.gameserver.Config;
 import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
 import jts.gameserver.instancemanager.AutoHuntingManager;
 import jts.gameserver.model.Player;
 import jts.gameserver.scripts.Functions;
 
 public class ReportBot extends Functions
   implements IVoicedCommandHandler
 {
   private String[] _commandList = { "botreport" };
 
   public boolean useVoicedCommand(String command, Player activeChar, String args)
   {
     if (command.equals("botreport"))
     {
       if (Config.ENABLE_AUTO_HUNTING_REPORT)
       {
         if ((activeChar.getTarget() instanceof Player))
         {
          Player reported = (Player)activeChar.getTarget();
           if (!AutoHuntingManager.getInstance().validateBot(reported, activeChar))
             return false;
           if (!AutoHuntingManager.getInstance().validateReport(activeChar))
            return false;
           try
           {
             AutoHuntingManager.getInstance().reportBot(reported, activeChar);
           }
           catch (Exception e)
           {
             e.printStackTrace();
           }
         }
         else {
           activeChar.sendMessage("Ваша цель должна быть Игроком");
         }
       }
       else activeChar.sendMessage("Действие отключено.");
     }
     return false;
   }
 
   public String[] getVoicedCommandList()
   {
     return this._commandList;
   }
 }