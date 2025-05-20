package ai;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.GameTimeController;
import jts.gameserver.ai.CharacterAI;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.instancemanager.HellboundManager;
import jts.gameserver.listener.game.OnDayNightChangeListener;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

/**
 * AI Shadai для Hellbound. Раз в сутки по ночам спавнится на определённом месте
 * на острове с шансом 40%. На оффе некоторые ждут по 2 недели.
 */
public class Shadai extends CharacterAI 
{


    public Shadai(NpcInstance actor) 
    {
        super(actor);
        GameTimeController.getInstance().addListener(new ShadaiDayNightListener());
    }

    private class ShadaiDayNightListener implements OnDayNightChangeListener 
    {

        private ShadaiDayNightListener() 
        {
            if (GameTimeController.getInstance().isNowNight()) 
            {
                onNight();
            }
            else
            {
                onDay();
            }
        }

        /**
         * Вызывается, когда на сервере наступает день
         */
        @Override
        public void onDay()
        {
            getActor().teleToLocation(new Location(16882, 238952, 9776));
        }

        /**
         * Вызывается, когда на сервере наступает ночь
         */
        @Override
        public void onNight() 
        {
            HellboundManager.getInstance();
			int hellboundLevel = HellboundManager.getHellboundLevel();
            if (hellboundLevel >= 9 && Rnd.chance(Config.SHADAI_SPAWN_CHANCE))
            {
                getActor().teleToLocation(new Location(9064, 253037, -1928));
                if (Config.ANNOUNCE_SHADAI_SPAWN) 
                {
    				DifferentMethods.sayToAll("scripts.ai.hellbound.Shadai", null );
                }
            }
        }
    }

    public boolean isGlobalAI()
    {
        return true;
    }

    protected boolean randomWalk()
    {
        return false;
    }
}
