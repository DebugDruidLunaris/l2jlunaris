package jts.gameserver.data.xml.parser;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jts.commons.collections.MultiValueSet;
import jts.commons.data.xml.AbstractFileParser;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.FishDataHolder;
import jts.gameserver.templates.item.support.FishGroup;
import jts.gameserver.templates.item.support.FishTemplate;
import jts.gameserver.templates.item.support.LureTemplate;
import jts.gameserver.templates.item.support.LureType;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class FishDataParser extends AbstractFileParser<FishDataHolder>
{
	private static final FishDataParser _instance = new FishDataParser();

	public static FishDataParser getInstance()
	{
		return _instance;
	}

	private FishDataParser()
	{
		super(FishDataHolder.getInstance());
	}

	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/other/fishdata.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "fishdata.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element e = iterator.next();
			if("fish".equals(e.getName()))
			{
				MultiValueSet<String> map = new MultiValueSet<String>();
				for(Iterator<Attribute> attributeIterator = e.attributeIterator(); attributeIterator.hasNext();)
				{
					Attribute attribute = attributeIterator.next();
					map.put(attribute.getName(), attribute.getValue());
				}

				getHolder().addFish(new FishTemplate(map));
			}
			else if("lure".equals(e.getName()))
			{
				MultiValueSet<String> map = new MultiValueSet<String>();
				for(Iterator<Attribute> attributeIterator = e.attributeIterator(); attributeIterator.hasNext();)
				{
					Attribute attribute = attributeIterator.next();
					map.put(attribute.getName(), attribute.getValue());
				}

				Map<FishGroup, Integer> chances = new HashMap<FishGroup, Integer>();
				for(Iterator<Element> elementIterator = e.elementIterator(); elementIterator.hasNext();)
				{
					Element chanceElement = elementIterator.next();
					chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.parseInt(chanceElement.attributeValue("value")));
				}
				map.put("chances", chances);
				getHolder().addLure(new LureTemplate(map));
			}
			else if("distribution".equals(e.getName()))
			{
				int id = Integer.parseInt(e.attributeValue("id"));

				for(Iterator<Element> forLureIterator = e.elementIterator(); forLureIterator.hasNext();)
				{
					Element forLureElement = forLureIterator.next();

					LureType lureType = LureType.valueOf(forLureElement.attributeValue("type"));
					Map<FishGroup, Integer> chances = new HashMap<FishGroup, Integer>();

					for(Iterator<Element> chanceIterator = forLureElement.elementIterator(); chanceIterator.hasNext();)
					{
						Element chanceElement = chanceIterator.next();
						chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.parseInt(chanceElement.attributeValue("value")));
					}
					getHolder().addDistribution(id, lureType, chances);
				}
			}
		}
	}
}