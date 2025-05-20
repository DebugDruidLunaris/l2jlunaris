package jts.gameserver.handler.voicecommands.impl;

import jts.gameserver.Config;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.instancemanager.AwayManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.network.serverpackets.components.CustomMessage;

import org.apache.commons.lang3.StringUtils;


public class Away implements IVoicedCommandHandler {

    private String[] VOICED_COMMANDS = new String[]{"afk_on", "afk_off"};

    @Override
    public boolean useVoicedCommand(String command, Player activeChar, String text) 
    {
        if (Config.AWAY_ONLY_FOR_PREMIUM && !activeChar.hasBonus())
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.PremiumOnly", activeChar));
            return false;
        }

        if (command.startsWith("afk_on")) 
        {
            return away(activeChar, text);
        } 
        else if
        (command.startsWith("afk_off")) 
        {
            return back(activeChar);
        }
        return false;
    }

    private boolean away(Player activeChar, String text) 
    {
        SiegeEvent<?, ?> siege = activeChar.getEvent(SiegeEvent.class);

        if (activeChar.isInAwayingMode())
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Already", activeChar));
            return false;
        }

        if (!activeChar.isInZone(ZoneType.peace_zone) && Config.AWAY_PEACE_ZONE)
        {
            activeChar.sendMessage(
                    new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.PieceOnly", activeChar));
            return false;
        }

        if (activeChar.isMovementDisabled() || activeChar.isAlikeDead())
        {
            return false;
        }

        if (siege != null) 
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Siege", activeChar));
            return false;
        }

        if (activeChar.isCursedWeaponEquipped()) 
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Cursed", activeChar));
            return false;
        }

        if (activeChar.isInDuel()) 
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Duel", activeChar));
            return false;
        }

        if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift()) 
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Rift", activeChar));
            return false;
        }

        // check player is in Olympiade
        if (activeChar.isInOlympiadMode() || activeChar.getOlympiadGame() != null) 
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Olympiad", activeChar));
            return false;
        }

        if (activeChar.isInObserverMode())
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Observer", activeChar));
            return false;
        }

        if (activeChar.getKarma() > 0 || activeChar.getPvpFlag() > 0)
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Pvp", activeChar));
            return false;
        }

        if (text == null) 
        {
            text = StringUtils.EMPTY;
        }

        if (text.length() > 10)
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Text", activeChar));
            return false;
        }

        if (activeChar.getTarget() == null)
        {
            AwayManager.getInstance().setAway(activeChar, text);
        } 
        else 
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Target", activeChar));
            return false;
        }

        return true;
    }

    private boolean back(Player activeChar)
    {
        if (!activeChar.isInAwayingMode()) 
        {
            activeChar.sendMessage(new CustomMessage("jts.gameserver.handler.voicecommands.impl.Away.Not", activeChar));
            return false;
        }
        AwayManager.getInstance().setBack(activeChar);
        return true;
    }

    @Override
    public String[] getVoicedCommandList() 
    {
        return VOICED_COMMANDS;
    }
}
