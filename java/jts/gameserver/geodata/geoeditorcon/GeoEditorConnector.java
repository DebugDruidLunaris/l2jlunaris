package jts.gameserver.geodata.geoeditorcon;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jts.gameserver.model.Player;
import jts.gameserver.model.World;


public class GeoEditorConnector
{
	private static GeoEditorConnector _instance = new GeoEditorConnector();
	private static final Logger _log = LoggerFactory.getLogger(GeoEditorConnector.class);

	public static GeoEditorConnector getInstance()
	{
		_log.info("GeoEditorConnector Initialized.");
		return _instance;

	}

	private GeoEditorThread _geThread;

	private List<Player> _gmList = new ArrayList<Player>();

	int RegionX;
	int RegionY;

	private GeoEditorConnector()
	{

	}

	public void connect(Player gm, int ticks)
	{
		if(_geThread != null)
		{
			gm.sendMessage("GameServer is already connected to GeoEditor.");
			if(!_gmList.contains(gm))
				join(gm);
			return;
		}
		RegionX = getRegionX(gm);
		RegionY = getRegionY(gm);

		_gmList.add(gm);

		_geThread = new GeoEditorThread(this);
		_geThread.setTicks(ticks);
		_geThread.start();
	}

	public void leave(Player gm)
	{
		_gmList.remove(gm);
		gm.sendMessage("You have been removed from the list");
		if(_gmList.isEmpty())
		{
			_geThread.stopRecording();
			_geThread = null;
			gm.sendMessage("Connection closed.");
		}
	}

	public void join(Player gm)
	{
		if(_geThread == null)
		{
			gm.sendMessage("GameServer is not connected to GeoEditor.");
			gm.sendMessage("Use //geoeditor connect <ticks>  first.");
			return;
		}
		if(_gmList.contains(gm))
		{
			gm.sendMessage("You are already on the list.");
			return;
		}
		if(getRegionX(gm) != RegionX || getRegionY(gm) != RegionY)
		{
			gm.sendMessage("Only people from region: [" + RegionX + "," + RegionY + "] can join.");
			return;
		}
		_gmList.add(gm);
		gm.sendMessage("You have been added to the list.");
	}

	public List<Player> getGMs()
	{
		return _gmList;
	}

	public void sendMessage(String msg)
	{
		for(Player gm : _gmList)
			gm.sendMessage(msg);
	}

	public void stoppedConnection()
	{
		_geThread = null;
		_gmList.clear();
	}

	private int getRegionX(Player g)
	{
		int gx = g.getX() - World.MAP_MIN_X >> 4;
		gx >>= 11;
		return gx + 16;
	}

	private int getRegionY(Player g)
	{
		int gy = g.getY() - World.MAP_MIN_Y >> 4;
		gy >>= 11;
		return gy + 10;
	}
}
