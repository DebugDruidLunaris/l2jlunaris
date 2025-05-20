package jts.gameserver.utils;

import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.Date;
import jts.gameserver.Config;

public class Log_New
{
private static File getCharFolder(String folderName)
 {
    return new File(Config.DATAPACK_ROOT, "log/chars/" + folderName);
 }
   
   private static File getCharFileByEvent(File dir, String fileName)
   {
     return new File(Config.DATAPACK_ROOT, "log/chars/" + dir + "/" + fileName + ".txt");
   }
   
   public static void LogEvent(String name, String IP, String event, String... args)
   {
     File dir = getCharFolder(name);
     if (((dir == null) || (!dir.exists())) && 
       (new File("./log/chars/" + name + "/").mkdir())) {
       dir = getCharFolder(name);
     }
     File file = getCharFileByEvent(dir, event);
     if ((file == null) || (!file.exists())) {
       try
       {
        file = new File(dir, "" + event + ".txt");
         file.createNewFile();
       }
       catch (Exception e) {}
     }
     StringBuilder output = new StringBuilder();
     Date date = new Date();
     output.append(new Timestamp(date.getTime()));
     output.append(":");
     output.append("Player: ");
     output.append(name);
     output.append(" ");
     output.append(IP);
     for (String elements : args)
     {
       output.append(" ");
       output.append(elements);
     }
     try
     {
       FileWriter writer = new FileWriter(file, true);
      writer.write(output.toString() + "\n");
      writer.close();
     }
     catch (Exception e) {}
   }
 }
