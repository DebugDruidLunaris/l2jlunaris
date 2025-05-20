package jts.gameserver.instancemanager;

import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.SetupGauge;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.network.serverpackets.components.CustomMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class AwayManager {

    protected static final Logger _log = LoggerFactory.getLogger(AwayManager.class);
    private Map<Player, RestoreData> _awayPlayers;

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {

        protected static final AwayManager _instance = new AwayManager();
    }

    public static AwayManager getInstance()
    {
        return SingletonHolder._instance;
    }

    private final class RestoreData {

        private final String _originalTitle;
        private final int _originalTitleColor;
        private final boolean _sitForced;

        public RestoreData(Player activeChar) 
        {
            _originalTitle = activeChar.getTitle();
            _originalTitleColor = activeChar.getTitleColor();
            _sitForced = !activeChar.isSitting();
        }

        public boolean isSitForced() {
            return _sitForced;
        }

        public void restore(Player activeChar)
        {
            activeChar.setTitleColor(_originalTitleColor);
            activeChar.setTitle(_originalTitle);
        }
    }

    private AwayManager() 
    {
        _awayPlayers = Collections.synchronizedMap(new WeakHashMap<Player, RestoreData>());
    }

    public void setAway(Player activeChar, String text) 
    {
        activeChar.setAwayingMode(true);
        activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 9));
        activeChar.sendMessage(new CustomMessage("jts.gameserver.instancemanager.AwayManager.setAway", activeChar, Config.AWAY_TIMER));
        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        SetupGauge sg = new SetupGauge(activeChar, SetupGauge.BLUE, Config.AWAY_TIMER * 1000);
        activeChar.sendPacket(sg);
        activeChar.startImmobilized();
        ThreadPoolManager.getInstance().schedule(new setPlayerAwayTask(activeChar, text), Config.AWAY_TIMER * 1000);
    }

    public void setBack(Player activeChar) {
        activeChar.sendMessage(new CustomMessage("jts.gameserver.instancemanager.AwayManager.setBack", activeChar, Config.BACK_TIMER));
        SetupGauge sg = new SetupGauge(activeChar, SetupGauge.BLUE, Config.BACK_TIMER * 1000);
        activeChar.sendPacket(sg);
        ThreadPoolManager.getInstance().schedule(new setPlayerBackTask(activeChar), Config.BACK_TIMER * 1000);
    }

    public void extraBack(Player activeChar) {
        if (activeChar == null) {
            return;
        }
        RestoreData rd = _awayPlayers.get(activeChar);
        if (rd == null) 
        {
            return;
        }

        rd.restore(activeChar);
        _awayPlayers.remove(activeChar);
    }

    private class setPlayerAwayTask implements Runnable
    {

        private final Player _activeChar;
        private final String _awayText;

        setPlayerAwayTask(Player activeChar, String awayText)
        {
            _activeChar = activeChar;
            _awayText = awayText;
        }

        @Override
        public void run() {
            if (_activeChar == null) 
            {
                return;
            }
            if (_activeChar.isAttackingNow() || _activeChar.isCastingNow())
            {
                return;
            }
            if (_activeChar.isSitting()) 
            {
                _activeChar.sendMessage(new CustomMessage("jts.gameserver.instancemanager.AwayManager.setPlayerAwayTask.Sitting", _activeChar));
                return;
            }

            _awayPlayers.put(_activeChar, new RestoreData(_activeChar));

            _activeChar.abortAttack(true, false);
            _activeChar.abortCast(true, false);
            _activeChar.setTarget(null);
            _activeChar.stopImmobilized();
            _activeChar.sitDown(null);
            if (_awayText.length() <= 1) 
            {
                _activeChar.sendMessage(new CustomMessage("jts.gameserver.instancemanager.AwayManager.setPlayerAwayTask.NoText", _activeChar));
            }
            else
            {
                _activeChar.sendMessage(new CustomMessage("jts.gameserver.instancemanager.AwayManager.setPlayerAwayTask", _activeChar, _awayText));
            }

            _activeChar.setTitleColor(Config.AWAY_TITLE_COLOR);

            if (_awayText.length() <= 1)
            {
                _activeChar.setTitle(_activeChar.isLangRus() ? "*Отошел АФК*" : "*Away AFK*");
            } 
            else 
            {
                _activeChar.setTitle((_activeChar.isLangRus() ? "Отошел АФК*" : "Away AFK*") + _awayText + "*");
            }

            _activeChar.broadcastUserInfo(false);
            _activeChar.startParalyzed();
        }
    }

    private class setPlayerBackTask implements Runnable 
    {

        private final Player _activeChar;

        setPlayerBackTask(Player activeChar)
        {
            _activeChar = activeChar;
        }

        @Override
        public void run() 
        {
            if (_activeChar == null) 
            {
                return;
            }
            RestoreData rd = _awayPlayers.get(_activeChar);

            if (rd == null)
            {
                return;
            }

            _activeChar.stopParalyzed();

            if (rd.isSitForced()) 
            {
                _activeChar.standUp();
            }

            rd.restore(_activeChar);
            _awayPlayers.remove(_activeChar);
            _activeChar.broadcastUserInfo(false);
            _activeChar.setAwayingMode(false);
            _activeChar.sendMessage(new CustomMessage("jts.gameserver.instancemanager.AwayManager.setPlayerBackTask", _activeChar));
        }
    }
}
