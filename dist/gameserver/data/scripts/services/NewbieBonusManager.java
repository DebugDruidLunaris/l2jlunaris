package services;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jts.gameserver.Config;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

public class NewbieBonusManager extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(NewsInformer.class);
	
	private static final ArrayList<SimpleSpawner> _spawns_isidora = new ArrayList<SimpleSpawner>();
	
	private static int ASGARD_GUILD_NPC = 36617;
	
	private void spawnIsidora()
	{
		final int ISIDORA[][] = 
		{
			{ -73016, 256664, -3151, 55368 }, //Human Fighter
			{ -88984, 249384, -3600, 32767 }, //Human Mage
			{ -53288, -113768, -111, 15982 }, //Orc
			{ 46216, 42120, -3497, 49636 }, //Elven
			{ 108776, -173912, -435, 37368 }, //Dwarven
			{ 28088, 11032, -4258, 0 }, //Dark Elven
			{ -125267, 38112, 1183, 33154 } //Kamael
		};

		SpawnNPCs(ASGARD_GUILD_NPC, ISIDORA, _spawns_isidora);
	}
	
	@Override
	public void onLoad()
	{
		if(Config.ALLOW_NEWBIE_BONUS_MANAGER)
		{
			spawnIsidora();
			_log.info("Loaded Service: Asgard Guild");
		}
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}