package ai;

import jts.gameserver.GameTimeController;
import jts.gameserver.ai.CharacterAI;
import jts.gameserver.data.xml.holder.EventHolder;
import jts.gameserver.listener.game.OnDayNightChangeListener;
import jts.gameserver.model.entity.events.EventType;
import jts.gameserver.model.entity.events.impl.FantasiIsleParadEvent;
import jts.gameserver.model.instances.NpcInstance;

public class FantasyIslePaddies extends CharacterAI
{
	public FantasyIslePaddies(NpcInstance actor)
	{
		super(actor);
		GameTimeController.getInstance().addListener(new StartEvent());
	}

	private class StartEvent implements OnDayNightChangeListener
	{
		private StartEvent()
		{
			if(GameTimeController.getInstance().isNowNight())
				onNight();
			else
				onDay();
		}

		/**
		 * Вызывается, когда на сервере наступает ночь
		 */
		@Override
		public void onNight()
		{
			NpcInstance actor = (NpcInstance) getActor();
			if(actor != null)
			{
				FantasiIsleParadEvent n_event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 10031);
				FantasiIsleParadEvent d_event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 10032);
				n_event.registerActions();
				d_event.stopEvent();
			}
		}

		/**
		 * Вызывается, когда на сервере наступает день
		 */
		@Override
		public void onDay()
		{
			NpcInstance actor = (NpcInstance) getActor();
			if(actor != null)
			{
				FantasiIsleParadEvent n_event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 10031);
				FantasiIsleParadEvent d_event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 10032);
				n_event.stopEvent();
				d_event.registerActions();
			}
		}
	}
}