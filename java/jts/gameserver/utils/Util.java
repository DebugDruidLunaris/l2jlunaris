package jts.gameserver.utils;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.dao.CharacterDAO;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.reward.RewardList;
import jts.gameserver.network.GameClient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Util
{
	static final String PATTERN = "0.0000000000E00";
	static final DecimalFormat df;

	/**
	 * Форматтер для адены.<br>
	 * Locale.KOREA заставляет его фортматировать через ",".<br>
	 * Locale.FRANCE форматирует через " "<br>
	 * Для форматирования через "." убрать с аргументов Locale.FRANCE
	 */
	private static NumberFormat adenaFormatter;

	static
	{
		adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern(PATTERN);
		df.setPositivePrefix("+");
	}

	/**
	 * Проверяет строку на соответсвие регулярному выражению
	 * @param text Строка-источник
	 * @param template Шаблон для поиска
	 * @return true в случае соответвия строки шаблону
	 */
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch(PatternSyntaxException e) // invalid template
		{
			e.printStackTrace();
		}
		if(pattern == null)
			return false;
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}

	public static String formatDouble(double x, String nanString, boolean forceExponents)
	{
		if(Double.isNaN(x))
			return nanString;
		if(forceExponents)
			return df.format(x);
		if((long) x == x)
			return String.valueOf((long) x);
		return String.valueOf(x);
	}
	public static double calculateDistance(Creature obj1, Creature obj2, boolean includeZAxis)
	{
		if(obj1 == null || obj2 == null)
			return 1000000;

		return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;

		if(includeZAxis)
		{
			final double dz = z1 - z2;
			return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		}
		return Math.sqrt(dx * dx + dy * dy);
	}
	public static String toProperCaseAll(String name)
	{
		StringTokenizer st = new StringTokenizer(name);
		String newString = "";

		newString = st.nextToken();
		name = newString.substring(0, 1).toUpperCase();
		if(newString.length() > 1)
			name += newString.substring(1).toLowerCase();

		while(st.hasMoreTokens())
		{
			newString = st.nextToken();

			if(newString.length() > 2)
			{
				name += " " + newString.substring(0, 1).toUpperCase();
				name += newString.substring(1).toLowerCase();
			}
			else
				name += " " + newString;
		}

		return name;
	}
	public static String convertToLineagePriceFormat(double price)
	{
		if(price < 10000)
			return Math.round(price) + "a";
		else if(price < 1000000)
			return Util.reduceDecimals(price / 1000, 1) + "k";
		else if(price < 1000000000)
			return Util.reduceDecimals(price / 1000 / 1000, 1) + "kk";
		else
			return Util.reduceDecimals(price / 1000 / 1000 / 1000, 1) + "kkk";
	}
	public static String reduceDecimals(double original, int nDecim)
	{
		return reduceDecimals(original, nDecim, false);
	}

	public static String reduceDecimals(double original, int nDecim, boolean round)
	{
		String decimals = "#";
		if(nDecim > 0)
		{
			decimals += ".";
			for(int i = 0; i < nDecim; i++)
				decimals += "#";
		}

		final DecimalFormat df = new DecimalFormat(decimals);
		return df.format((round ? Math.round(original) : original)).replace(",", ".");
	}
	/**
	 * Return amount of adena formatted with " " delimiter
	 * @param amount
	 * @return String formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		return adenaFormatter.format(amount);
	}

	/**
	 * форматирует время в секундах в дни/часы/минуты/секунды
	 */
	public static String formatTime(int time)
	{
		if(time == 0)
			return "now";
		time = Math.abs(time);
		String ret = "";
		long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
		if(numDays > 0)
			ret += numDays + "d ";
		if(numHours > 0)
			ret += numHours + "h ";
		if(numMins > 0)
			ret += numMins + "m ";
		if(numSeconds > 0)
			ret += numSeconds + "s ";
		return ret.trim();
	}
	
	public static String formatTime2(int time)
	{
		if(time == 0)
			return "now";
		time = Math.abs(time);
		String ret = "";
		long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
		if(numDays > 0)
			ret += numDays + " д ";
		if(numHours > 0)
			ret += numHours + " ч ";
		if(numMins > 0)
			ret += numMins + " м ";
		if(numSeconds > 0)
			ret += numSeconds + " с ";
		return ret.trim();
	}
	/**
	 * Инструмент для подсчета выпавших вещей с учетом рейтов.
	 * Возвращает 0 если шанс не прошел, либо количество если прошел.
	 * Корректно обрабатывает шансы превышающие 100%.
	 * Шанс в 1:1000000 (L2Drop.MAX_CHANCE)
	 */
	public static long rollDrop(long min, long max, double calcChance, boolean rate)
	{
		if(calcChance <= 0 || min <= 0 || max <= 0)
			return 0;
		int dropmult = 1;
		if(rate)
			calcChance *= Config.RATE_DROP_ITEMS;
		if(calcChance > RewardList.MAX_CHANCE)
			if(calcChance % RewardList.MAX_CHANCE == 0) // если кратен 100% то тупо умножаем количество
				dropmult = (int) (calcChance / RewardList.MAX_CHANCE);
			else
			{
				dropmult = (int) Math.ceil(calcChance / RewardList.MAX_CHANCE); // множитель равен шанс / 100% округление вверх
				calcChance = calcChance / dropmult; // шанс равен шанс / множитель
			}
		return Rnd.chance(calcChance / 10000.) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}

	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		int result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static long packLong(int[] a, int bits) throws Exception
	{
		int m = 64 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		long result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		int next;
		for(int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
			result[i - 1] = next - a * mval;
		}
		return result;
	}

	public static int[] unpackLong(long a, int bits)
	{
		int m = 64 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		long next;
		for(int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
			result[i - 1] = (int) (next - a * mval);
		}
		return result;
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, -1);
	}

	public static boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		return true;
	}

	public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
	{
		Class<?> cls = o.getClass();
		String val, type, result = "[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n";
		Object fldObj;
		List<Field> fields = new ArrayList<Field>();
		while(cls != null)
		{
			for(Field fld : cls.getDeclaredFields())
				if(!fields.contains(fld))
				{
					if(ignoreStatics && Modifier.isStatic(fld.getModifiers()))
						continue;
					fields.add(fld);
				}
			cls = cls.getSuperclass();
			if(!parentFields)
				break;
		}

		for(Field fld : fields)
		{
			fld.setAccessible(true);
			try
			{
				fldObj = fld.get(o);
				if(fldObj == null)
					val = "NULL";
				else
					val = fldObj.toString();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
				val = "<ERROR>";
			}
			type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();

			result += String.format("\t%s [%s] = %s;\n", fld.getName(), type, val);
		}

		result += "]\n";
		return result;
	}

	private static Pattern _pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", Pattern.DOTALL);

	public static HashMap<Integer, String> parseTemplate(String html)
	{
		Matcher m = _pattern.matcher(html);
		HashMap<Integer, String> tpls = new HashMap<Integer, String>();
		while(m.find())
		{
			tpls.put(Integer.parseInt(m.group(1)), m.group(2));
			html = html.replace(m.group(0), "");
		}

		tpls.put(0, html);
		return tpls;
	}

	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only numbers, {@code false} otherwise
	 */
	public static boolean isDigit(String text)
	{
		if(text == null || text.isEmpty())
			return false;
		for(char c : text.toCharArray())
			if(!Character.isDigit(c))
				return false;
		return true;
	}

	public static String getOSInfo()
	{
		return "Operating System: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version") + ", arch: " + System.getProperty("os.arch");
	}
    public static String[] getCpuInfo() {
        return new String[]{"Avaible CPU(s): " + Runtime.getRuntime().availableProcessors(),
            "CPU: " + System.getenv("PROCESSOR_IDENTIFIER")};
    }

	public static String getJavaInfo()
	{
		return "JMV: " + System.getProperty("java.vm.name") + " Build: " + System.getProperty("java.runtime.version");
	}
    public static int[] parseCommaSeparatedIntegerArray(String s) {
        if (s.isEmpty()) {
            return new int[0];
        }
        String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
        int[] val = new int[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            val[i] = Integer.parseInt(tmp[i]);
        }
        return val;
    }
    /**
     * Грузит игрока в мир для операций сериализации
     * в XML-RPC сервере
     * @param playerName имя игрока
     * @return инстанс загруженного игрока
     */
    public static Player loadPlayer(String playerName, boolean onlineStatus)
    {
        Player player = null;
        GameClient client = new GameClient(null);

        player = World.getPlayer(playerName);

        if(player != null)
        {
            return player;
        }

        player = Player.restore(CharacterDAO.getInstance().getObjectIdByName(playerName));

        if(player == null)
        {
            return null;
        }
        //TODO
        client.setActiveChar(player);
        player.setOnlineStatus(onlineStatus);
        client.setLoginName(player.getAccountName());
        client.setState(GameClient.GameClientState.IN_GAME);
        //player.setClient(client);
        return player;
    }
}