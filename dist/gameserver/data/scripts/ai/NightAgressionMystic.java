package ai;

import jts.gameserver.GameTimeController;
import jts.gameserver.ai.Mystic;
import jts.gameserver.listener.game.OnDayNightChangeListener;
import jts.gameserver.model.instances.NpcInstance;

public class NightAgressionMystic extends Mystic
{
	public NightAgressionMystic(NpcInstance actor)
	{
		super(actor);
		GameTimeController.getInstance().addListener(new NightAgressionDayNightListener());
	}

	private class NightAgressionDayNightListener implements OnDayNightChangeListener
	{
		private NightAgressionDayNightListener()
		{
			if(GameTimeController.getInstance().isNowNight())
				onNight();
			else
				onDay();
		}

		/**
		 * Вызывается, когда на сервере наступает день
		 */
		@Override
		public void onDay()
		{
			getActor().setAggroRange(0);
		}

		/**
		 * Вызывается, когда на сервере наступает ночь
		 */
		@Override
		public void onNight()
		{
			getActor().setAggroRange(-1);
		}
	}
}