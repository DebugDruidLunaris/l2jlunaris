package services;

import java.util.List;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.network.serverpackets.ExStartScenePlayer;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.ReflectionUtils;

public class LindviorMovie implements ScriptFile
{
	private static long movieDelay = 3 * 60 * 60 * 1000L; // Ð¿Ð¾ÐºÐ°Ð·ÑÐ²Ð°ÑÑ Ð¼ÑÐ²Ð¸Ðº ÑÐ°Ð· Ð² n ÑÐ°ÑÐ¾Ð²

	@Override
	public void onLoad()
	{
		Zone zone = ReflectionUtils.getZone("[keucereus_alliance_base_town_peace]");
		zone.setActive(true);

		ThreadPoolManager.getInstance().scheduleAtFixedRate(new ShowLindviorMovie(zone), movieDelay, movieDelay);
	}

	public class ShowLindviorMovie extends RunnableImpl
	{
		Zone _zone;

		public ShowLindviorMovie(Zone zone)
		{
			_zone = zone;
		}

		@Override
		public void runImpl() throws Exception
		{
			List<Player> insideZoners = _zone.getInsidePlayers();

			if(insideZoners != null && !insideZoners.isEmpty())
				for(Player player : insideZoners)
					if(!player.isInBoat() && !player.isInFlyingTransform())
						player.showQuestMovie(ExStartScenePlayer.SCENE_LINDVIOR);
		}
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}