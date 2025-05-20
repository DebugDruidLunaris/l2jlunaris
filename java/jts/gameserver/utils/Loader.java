package jts.gameserver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import jts.gameserver.Shutdown;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.GameServer;
import jts.gameserver.ThreadPoolManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loader
  extends ClassLoader
{
  private static Loader _instance = null;
  private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
  String load;
  
  public static Loader getInstance()
  {	
    if (_instance == null) 
    {
	if(!Config.EXTERNAL_HOSTNAME.equalsIgnoreCase("127.0.0.1"))
		{
      _instance = new Loader();
		}
    }
    return _instance;
  }


  private Loader()
  {
    load = getTo("aHR0cDovLzk1LjIxMy4yMDAuMTEyL2d1YXJkL25wZ211cC5waHA=");
    BufferedReader reader = null;
    try
    {
      URL url = new URL(load + "?" + Base64.encodeBytes(Config.OWNER_NAME.getBytes()));
      reader = new BufferedReader(new InputStreamReader(url.openStream()));
    }
    catch (IOException e)
    {
      reader = null;
    }
    if (reader == null)
    {
      _log.info("License : Unable to connect license server!");
    //  ThreadPoolManager.getInstance().schedule(new Loader.Explode(), 120000 + Rnd.get(120000));
      System.exit(1);
      return;
    }
    try
    {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("Key=")) {
          Config.LICENSE_KEY = Integer.parseInt(line.substring(4).trim(), 16) << 8;
        }
      }
      if (Config.LICENSE_KEY == -1)
      {
        throw new Exception();
      }
    }
    catch (Exception e)
    {
      _log.info("Not correct License");
     // ThreadPoolManager.getInstance().schedule(new Loader.Explode(), 120000 + Rnd.get(120000));
      System.exit(1);
    }
}
  
   public static class Explode extends RunnableImpl
  {
   public void runImpl()
    {
       Shutdown.getInstance().run();
    }
  }
 
  private String getTo(String string)
  {
    try
    {
      return new String(Base64.decode(string), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {}
    return null;
  }
}