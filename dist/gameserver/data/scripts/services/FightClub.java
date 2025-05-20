package services;

import java.util.ArrayList;

import jts.gameserver.Config;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

public class FightClub extends Functions implements ScriptFile
{
	private static final ArrayList<SimpleSpawner> _spawns_fight_club_manager = new ArrayList<SimpleSpawner>();

	public static int FIGHT_CLUB_MANAGER = 13112;

	private void spawnFightClub()
	{
		final int FIGHT_CLUB_MANAGER_SPAWN[][] = {

		{ 82248, 147544, -3494, 13828 }, // Giran
		{ 147480, 27288, -2228, 49151 }, // Aden
		{ 82536, 53144, -1521, 0 }, // Oren
		{ 16184, 144440, -3054, 16383 }, // Dion
		{ 112488, 220264, -3627, 32767 }, // Heine
		{ -15048, 121944, -3074, 0 }, // Gludio
		{ 147384, -55352, -2759, 60699 }, // Goddard
		{ 87688, -143352, -1318, 29412 }, // Shuttgard
		{ -84776, 150904, -3154, 0 }, // Gludin
		{ 36312, -48232, -1120, 0 }, // Rune
		};

		SpawnNPCs(FIGHT_CLUB_MANAGER, FIGHT_CLUB_MANAGER_SPAWN, _spawns_fight_club_manager);
	}

	@Override
	public void onLoad()
	{
		if(Config.FIGHT_CLUB_ENABLED)
			spawnFightClub();
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}