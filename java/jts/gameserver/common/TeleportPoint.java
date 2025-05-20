package jts.gameserver.common;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import jts.gameserver.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TeleportPoint
{
	private static final Logger _log = LoggerFactory.getLogger(TeleportPoint.class);
	public static HashMap<Integer, TeleportPoint> teleport = new HashMap<Integer, TeleportPoint>();

	private int id;
	private String name;
	private int priceId;
	private int count;
	private int min;
	private int max;
	private boolean pk;
	private boolean premium;
	private int premiumPriceId;
	private int premiumCount;
	private int x;
	private int y;
	private int z;

	public TeleportPoint(int id, String name, int priceId, int count, int min, int max, boolean pk, boolean premium, int premiumPriceId, int premiumCount, int x, int y, int z)
	{
		this.id = id;
		this.name = name;
		this.priceId = priceId;
		this.count = count;
		this.min = min;
		this.max = max;
		this.pk = pk;
		this.premium = premium;
		this.premiumPriceId = premiumPriceId;
		this.premiumCount = premiumCount;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static void load()
	{
		try
		{
			int PointCount = 0;
			File file = new File(Config.DATAPACK_ROOT + "/data/xml/other/TeleportPoint.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			Document document = (Document) dbf.newDocumentBuilder().parse(file);

			for(Node list = document.getFirstChild(); list != null; list = list.getNextSibling())
			{
				if("list".equalsIgnoreCase(list.getNodeName()))
				{
					for(Node point = list.getFirstChild(); point != null; point = point.getNextSibling())
					{
						if("point".equalsIgnoreCase(point.getNodeName()))
						{
							PointCount++;
							NamedNodeMap PointMap = point.getAttributes();
							int id = Integer.parseInt(PointMap.getNamedItem("id").getNodeValue());
							String name = PointMap.getNamedItem("name").getNodeValue();
							int min = Integer.parseInt(PointMap.getNamedItem("min").getNodeValue());
							int max = Integer.parseInt(PointMap.getNamedItem("max").getNodeValue());
							boolean pk = Boolean.parseBoolean(PointMap.getNamedItem("pk").getNodeValue());
							boolean isPremiumPoint = Boolean.parseBoolean(PointMap.getNamedItem("isPremiumPoint").getNodeValue());

							// Настройка для па.
							int premiumPriceId = 0;
							int premiumCount = 0;
							int priceId = 0;
							int count = 0;

							Node cost = point.getFirstChild();
							if(cost != null)
							{
								cost = cost.getNextSibling();
								if("cost".equalsIgnoreCase(cost.getNodeName()))
								{
									NamedNodeMap CostMap = cost.getAttributes();
									priceId = Integer.parseInt(CostMap.getNamedItem("Item").getNodeValue());
									count = Integer.parseInt(CostMap.getNamedItem("Count").getNodeValue());
									premiumPriceId = Integer.parseInt(CostMap.getNamedItem("PremiumItem").getNodeValue());
									premiumCount = Integer.parseInt(CostMap.getNamedItem("PremiumCount").getNodeValue());
								}
							}

							// Настройка координат. Если не подхватывается или отсутствует ставим тп в гиран.
							int x = 83352, y = 145616, z = -3430;

							Node coordinates = point.getLastChild();
							if(coordinates != null)
							{
								coordinates = coordinates.getPreviousSibling();
								if("coordinates".equalsIgnoreCase(coordinates.getNodeName()))
								{
									NamedNodeMap CordMap = coordinates.getAttributes();
									x = Integer.parseInt(CordMap.getNamedItem("x").getNodeValue());
									y = Integer.parseInt(CordMap.getNamedItem("y").getNodeValue());
									z = Integer.parseInt(CordMap.getNamedItem("z").getNodeValue());
								}
							}
//							_log.info("======================LOADING POINT №" + id + "==========================");
//							_log.info("Point name: " + name.toString() + ".");
//							_log.info("Level min: " + min + ".");
//							_log.info("Level max: " + max + ".");
//							_log.info("Can use PK: " + priceId + ".");
//							_log.info("Point only for premium: " + isPremiumPoint + ".");
//							_log.info("Price item ID: " + priceId + ".");
//							_log.info("Price count: " + count + ".");
//							_log.info("Price item ID for premium: " + premiumPriceId + ".");
//							_log.info("Price count for premium: " + premiumCount + ".");
//							_log.info("x cord: " + x + ".");
//							_log.info("y cord: " + y + ".");
//							_log.info("z cord: " + z + ".");
							TeleportPoint data = new TeleportPoint(id, name, priceId, count, min, max, pk, isPremiumPoint, premiumPriceId, premiumCount, x, y, z);
							teleport.put(id, data);
						}
					}
				}
			}
			_log.info("Carregado " + PointCount + " teleport points.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public int getPriceId()
	{
		return priceId;
	}

	public int getPriceCount()
	{
		return count;
	}

	public int getMinLevel()
	{
		return min;
	}

	public int getMaxLevel()
	{
		return max;
	}

	public boolean getPkAccess()
	{
		return pk;
	}

	public boolean getPremiumOnly()
	{
		return premium;
	}

	public int getPremiumPriceId()
	{
		return premiumPriceId;
	}

	public int getPremiumPriceCount()
	{
		return premiumCount;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}
}