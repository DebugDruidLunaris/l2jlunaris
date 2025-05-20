package services.community;

import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

public class ShowInfo extends Functions implements ScriptFile
{
	public void show(String[] param)
	{
		Player player = getSelf();
		String info_folder = "";
		String info_page = "";

		if(player == null)
			return;

		if(param.length != 2)
		{
			String html = HtmCache.getInstance().getNotNull("scripts/services/popup/error_page.htm", player);
			show(html, player);
			return;
		}

		info_folder = String.valueOf(param[0]);
		info_page = String.valueOf(param[1]);

		String html = HtmCache.getInstance().getNotNull("scripts/services/popup/" + info_folder + "/" + info_page + ".htm", player);
		show(html, player);
	}

	@Override
	public void onLoad() {}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}