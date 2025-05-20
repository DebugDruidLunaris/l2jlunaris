package jts.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import jts.commons.data.xml.AbstractDirParser;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.PetDataHolder;
import jts.gameserver.templates.StatsSet;

import org.dom4j.Element;

public class PetDataParser extends AbstractDirParser<PetDataHolder>
{
	private static PetDataParser _instance = new PetDataParser();
	
	public static PetDataParser getInstance()
	{
		return _instance;
	}
	
	protected PetDataParser(PetDataHolder holder)
	{
		super(holder);
	}

	public PetDataParser()
	{
		super(PetDataHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/pets/");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "pets.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for (Iterator<Element> iterator = rootElement.elementIterator("pet"); iterator.hasNext(); )
		{
			Element pet = iterator.next();
			StatsSet set = new StatsSet();
			set.set("id", pet.attributeValue("id"));
			set.set("index", pet.attributeValue("index"));
			
			for (Iterator<Element> i1 = pet.elementIterator(); i1.hasNext(); )
			{				
				Element element = i1.next();
				
				if (element.getName().equals("set"))
				{
					set.set(element.attributeValue("name"),element.attributeValue("val"));
				}
				else if (element.getName().equals("stats"))
				{
					for (Iterator<Element> itr = element.elementIterator("stat"); itr.hasNext();)
					{
						Element stat = itr.next();		
						set.set("level", stat.attributeValue("level"));
						
						for (Iterator<Element> it = stat.elementIterator("set"); it.hasNext();)
						{
							Element e = it.next();
							set.set(e.attributeValue("name"), e.attributeValue("val"));
						}
					}
				}
			}
			
			getHolder().addPetData(set);
		}
	}

}
