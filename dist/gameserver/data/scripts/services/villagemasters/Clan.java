package services.villagemasters;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.VillageMasterInstance;
import jts.gameserver.scripts.Functions;

public class Clan extends Functions
{
	public void CheckCreateClan()
	{
		if(getNpc() == null || getSelf() == null)
			return;
		Player pl = getSelf();
		String htmltext = "clan-02.htm";
		// Player less 10 levels, and can not create clan
		if(pl.getLevel() <= 9)
			htmltext = "clan-06.htm";
		// Player already is a clan by leader and can not newly create clan
		else if(pl.isClanLeader())
			htmltext = "clan-07.htm";
		// Player already consists in clan and can not create clan
		else if(pl.getClan() != null)
			htmltext = "clan-09.htm";
		((VillageMasterInstance) getNpc()).showChatWindow(pl, "villagemaster/" + htmltext);
	}

	public void CheckDissolveClan()
	{
		if(getNpc() == null || getSelf() == null)
			return;
		Player pl = getSelf();
		String htmltext = "clan-01.htm";
		if(pl.isClanLeader())
			htmltext = "clan-04.htm";
		else
		// Player already consists in clan and can not create clan
		if(pl.getClan() != null)
			htmltext = "9000-08.htm";
		// Player not in clan and can not dismiss clan
		else
			htmltext = "9000-11.htm";
		((VillageMasterInstance) getNpc()).showChatWindow(pl, "villagemaster/" + htmltext);
	}
}