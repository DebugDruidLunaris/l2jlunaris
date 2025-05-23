package jts.gameserver.data;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import jts.commons.data.xml.AbstractHolder;
import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.utils.Language;

public final class StringHolder extends AbstractHolder
{
	private static final StringHolder _instance = new StringHolder();
	private Map<Language, Map<String, String>> _strings = new HashMap<Language, Map<String, String>>();

	public static StringHolder getInstance()
	{
		return _instance;
	}

	private StringHolder() {}

	public String getNullable(Player player, String name)
	{
		Language lang = player == null ? Language.ENGLISH : player.getLanguage();
		return get(lang, name);
	}

	public String getNotNull(Player player, String name)
	{
		Language lang = player == null ? Language.ENGLISH : player.getLanguage();

		String text = get(lang, name);
		if(text == null && player != null)
		{
			text = "Not find string: " + name + "; for lang: " + lang;
			_strings.get(lang).put(name, text);
		}

		return text;
	}

	private String get(Language lang, String address)
	{
		Map<String, String> strings = _strings.get(lang);

		return strings.get(address);
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_strings.put(lang, new HashMap<String, String>());

			File f = new File(Config.DATAPACK_ROOT, "data/string/strings_" + lang.getShortName() + ".ini");
			if(!f.exists())
			{
				warn("Not find file: " + f.getAbsolutePath());
				continue;
			}

			LineNumberReader reader = null;
			try
			{
				reader = new LineNumberReader(new FileReader(f));
				String line = null;

				while((line = reader.readLine()) != null)
				{
					if(line.startsWith("#"))
						continue;

					StringTokenizer token = new StringTokenizer(line, "=");
					if(token.countTokens() < 2)
					{
						error("Error on line: " + line + "; file: " + f.getName());
						continue;
					}

					String name = token.nextToken();
					String value = token.nextToken();
					while(token.hasMoreTokens())
						value += "=" + token.nextToken();

					Map<String, String> strings = _strings.get(lang);

					strings.put(name, value);
				}
			}
			catch(Exception e)
			{
				error("Exception StringHolder: " + e, e);
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch(Exception e)
				{}
			}
		}

		log();
	}

	public void reload()
	{
		clear();
		load();
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, Map<String, String>> entry : _strings.entrySet())
			info("load strings: " + entry.getValue().size() + " for lang: " + entry.getKey());
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		_strings.clear();
	}
}