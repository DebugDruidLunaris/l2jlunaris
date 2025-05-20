package jts.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import jts.commons.data.xml.AbstractFileParser;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.StaticObjectHolder;
import jts.gameserver.templates.StaticObjectTemplate;
import jts.gameserver.templates.StatsSet;

import org.dom4j.Element;

public final class StaticObjectParser extends AbstractFileParser<StaticObjectHolder>
{
	private static StaticObjectParser _instance = new StaticObjectParser();

	public static StaticObjectParser getInstance()
	{
		return _instance;
	}

	private StaticObjectParser()
	{
		super(StaticObjectHolder.getInstance());
	}

	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/other/staticobjects.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "staticobjects.dtd";
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element staticObjectElement = (Element) iterator.next();

			StatsSet set = new StatsSet();
			set.set("uid", staticObjectElement.attributeValue("id"));
			set.set("stype", staticObjectElement.attributeValue("stype"));
			set.set("path", staticObjectElement.attributeValue("path"));
			set.set("map_x", staticObjectElement.attributeValue("map_x"));
			set.set("map_y", staticObjectElement.attributeValue("map_y"));
			set.set("name", staticObjectElement.attributeValue("name"));
			set.set("x", staticObjectElement.attributeValue("x"));
			set.set("y", staticObjectElement.attributeValue("y"));
			set.set("z", staticObjectElement.attributeValue("z"));
			set.set("spawn", staticObjectElement.attributeValue("spawn"));

			getHolder().addTemplate(new StaticObjectTemplate(set));
		}
	}
}