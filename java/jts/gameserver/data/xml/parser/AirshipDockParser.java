package jts.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jts.commons.data.xml.AbstractFileParser;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.AirshipDockHolder;
import jts.gameserver.model.entity.events.objects.BoatPoint;
import jts.gameserver.network.serverpackets.components.SceneMovie;
import jts.gameserver.templates.AirshipDock;

import org.dom4j.Element;

public final class AirshipDockParser extends AbstractFileParser<AirshipDockHolder>
{
	private static final AirshipDockParser _instance = new AirshipDockParser();

	public static AirshipDockParser getInstance()
	{
		return _instance;
	}

	protected AirshipDockParser()
	{
		super(AirshipDockHolder.getInstance());
	}

	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/other/airship_docks.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "airship_docks.dtd";
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element dockElement = (Element) iterator.next();
			int id = Integer.parseInt(dockElement.attributeValue("id"));

			List<BoatPoint> teleportList = parsePoints(dockElement.element("teleportlist"));

			for(BoatPoint point : teleportList)
			{
				point.setTeleport(true);
				point.setSpeed1(-1);
				point.setSpeed2(-1);
			}

			List<AirshipDock.AirshipPlatform> platformList = new ArrayList<AirshipDock.AirshipPlatform>(2);
			for(Iterator platformIterator = dockElement.elementIterator("platform"); platformIterator.hasNext();)
			{
				Element platformElement = (Element) platformIterator.next();
				SceneMovie movie = SceneMovie.valueOf(platformElement.attributeValue("movie"));
				BoatPoint oustLoc = BoatPoint.parse(platformElement.element("oust"));
				BoatPoint spawnLoc = BoatPoint.parse(platformElement.element("spawn"));
				List<BoatPoint> arrivalList = parsePoints(platformElement.element("arrival"));
				List<BoatPoint> departList = parsePoints(platformElement.element("depart"));

				AirshipDock.AirshipPlatform platform = new AirshipDock.AirshipPlatform(movie, oustLoc, spawnLoc, arrivalList, departList);
				platformList.add(platform);
			}

			getHolder().addDock(new AirshipDock(id, teleportList, platformList));
		}
	}

	@SuppressWarnings("rawtypes")
	private List<BoatPoint> parsePoints(Element listElement)
	{
		if(listElement == null)
			return Collections.emptyList();
		List<BoatPoint> list = new ArrayList<BoatPoint>(5);
		for(Iterator iterator = listElement.elementIterator(); iterator.hasNext();)
			list.add(BoatPoint.parse((Element) iterator.next()));

		return list.isEmpty() ? Collections.<BoatPoint> emptyList() : list;
	}
}