package jts.gameserver.geodata.geoeditorcon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;


public class GeoEditorThread extends Thread
{
	// How many times per second send packet
	int ticks = 2;

	boolean working = true;

	private int _port;
	private String _hostname;

	private Socket _geSocket;

	private BufferedOutputStream _out;
	@SuppressWarnings("unused")
	private BufferedInputStream _in;

	private GeoEditorConnector _geCon;

	public GeoEditorThread(GeoEditorConnector ge)
	{
		_port = 2109;
		_hostname = Config.GEO_EDITOR_HOST;

		_geCon = ge;
	}

	@Override
	public void run()
	{
		try
		{
			_geCon.sendMessage("... connecting to GeoEditor.");
			_geSocket = new Socket(_hostname, _port);
			_geCon.sendMessage("Connection established.");

			_in = new BufferedInputStream(_geSocket.getInputStream());
			_out = new BufferedOutputStream(_geSocket.getOutputStream());

			while (working)
			{
				try
				{
					TimeUnit.MILLISECONDS.sleep(1000 / ticks);
				}
				catch(Exception e)
				{}
				for(Player gm : _geCon.getGMs())
					sendGmPosition(gm);
			}
			_geCon.stoppedConnection();
			_geCon.sendMessage("Connection with GeoEditor broken.");
		}
		catch(UnknownHostException e)
		{
			_geCon.stoppedConnection();
			_geCon.sendMessage("Couldn't connect to GeoEditor.");
		}
		catch(IOException e)
		{
			_geCon.stoppedConnection();
			_geCon.sendMessage("Connection with GeoEditor broken.");
		}
		finally
		{
			try
			{
				_geSocket.close();
			}
			catch(Exception e)
			{}
		}
	}

	private void sendGmPosition(Player _gm) throws IOException
	{
		int gx = _gm.getX() - World.MAP_MIN_X >> 4;
		int gy = _gm.getY() - World.MAP_MIN_Y >> 4;

		byte bx = (byte) ((gx >> 3) % 256);
		byte by = (byte) ((gy >> 3) % 256);

		byte cx = (byte) (gx % 8);
		byte cy = (byte) (gy % 8);

		short z = (short) _gm.getZ();

		// 6 bytes
		_out.write(bx);
		_out.write(by);
		_out.write(cx);
		_out.write(cy);
		sendShort(z);
		_out.flush();
	}

	private void sendShort(short v) throws IOException
	{
		_out.write(v >> 8);
		_out.write(v & 0xFFFF);
	}

	public void stopRecording()
	{
		working = false;
	}

	public void setTicks(int t)
	{
		if(t < 1)
			t = 1;
		else if(t > 5)
			t = 5;
		ticks = t;
	}
}
