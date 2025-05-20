package jts.gameserver.data.xml;

import jts.gameserver.data.StringHolder;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.BuyListHolder;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.data.xml.holder.ProductHolder;
import jts.gameserver.data.xml.holder.RecipeHolder;
import jts.gameserver.data.xml.parser.AirshipDockParser;
import jts.gameserver.data.xml.parser.ArmorSetsParser;
import jts.gameserver.data.xml.parser.CharTemplateParser;
import jts.gameserver.data.xml.parser.CubicParser;
import jts.gameserver.data.xml.parser.DomainParser;
import jts.gameserver.data.xml.parser.DoorParser;
import jts.gameserver.data.xml.parser.EnchantItemParser;
import jts.gameserver.data.xml.parser.EventParser;
import jts.gameserver.data.xml.parser.FishDataParser;
import jts.gameserver.data.xml.parser.HennaParser;
import jts.gameserver.data.xml.parser.InstantZoneParser;
import jts.gameserver.data.xml.parser.ItemParser;
import jts.gameserver.data.xml.parser.NpcParser;
import jts.gameserver.data.xml.parser.OptionDataParser;
import jts.gameserver.data.xml.parser.PetDataParser;
import jts.gameserver.data.xml.parser.PetitionGroupParser;
import jts.gameserver.data.xml.parser.ResidenceParser;
import jts.gameserver.data.xml.parser.RestartPointParser;
import jts.gameserver.data.xml.parser.SkillAcquireParser;
import jts.gameserver.data.xml.parser.SoulCrystalParser;
import jts.gameserver.data.xml.parser.SpawnParser;
import jts.gameserver.data.xml.parser.StaticObjectParser;
import jts.gameserver.data.xml.parser.ZoneParser;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.tables.SkillTable;

public abstract class Parsers
{
	public static void parseAll()
	{
		HtmCache.getInstance().reload();
		StringHolder.getInstance().load();
		SkillTable.getInstance().load(); // - SkillParser.getInstance();
		OptionDataParser.getInstance().load();
		ItemParser.getInstance().load();
		NpcParser.getInstance().load();
		DomainParser.getInstance().load();
		RestartPointParser.getInstance().load();
		StaticObjectParser.getInstance().load();
		DoorParser.getInstance().load();
		ZoneParser.getInstance().load();
		SpawnParser.getInstance().load();
		InstantZoneParser.getInstance().load();
		ReflectionManager.getInstance();
		AirshipDockParser.getInstance().load();
		SkillAcquireParser.getInstance().load();
		CharTemplateParser.getInstance().load();
		ResidenceParser.getInstance().load();
		EventParser.getInstance().load();
		CubicParser.getInstance().load();
		BuyListHolder.getInstance();
		RecipeHolder.getInstance();
		MultiSellHolder.getInstance();
		ProductHolder.getInstance();
		// AgathionParser.getInstance();
		HennaParser.getInstance().load();
		EnchantItemParser.getInstance().load();
		SoulCrystalParser.getInstance().load();
		ArmorSetsParser.getInstance().load();
		FishDataParser.getInstance().load();
		PetitionGroupParser.getInstance().load();
		PetDataParser.getInstance().load();
	}
}