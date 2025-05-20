package services;

import java.util.ArrayList;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.templates.item.ItemTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewsInformer extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(NewsInformer.class);

	private static final ArrayList<SimpleSpawner> _spawns_cat = new ArrayList<SimpleSpawner>();
	private static final ArrayList<SimpleSpawner> _spawns_queen = new ArrayList<SimpleSpawner>();

	private static int TEST_SERVER_HELPER_MR_CAT = 31756;
	private static int TEST_SERVER_HELPER_MISS_QUEEN = 31757;

	private void spawnMrCat()
	{
		final int MR_CAT[][] = 
		{
			{ 147728, 27408, -2198, 16500 },
			{ 148560, -57952, -2974, 53000 },
			{ 110592, 220400, -3667, 0 },
			{ 117200, 75824, -2725, 25000 },
			{ 116224, -181728, -1373, 0 },
			{ 114880, -178144, -827, 0 },
			{ 83076, 147912, -3467, 32000 },
			{ 81136, 54576, -1517, 32000 },
			{ 45472, 49312, -3067, 53000 },
			{ 47648, 51296, -2989, 38500 },
			{ 17956, 170536, -3499, 48000 },
			{ 15584, 142784, -2699, 16500 },
			{ 11340, 15972, -4577, 14000 },
			{ 10968, 17540, -4567, 55000 },
			{ -14048, 123184, -3115, 32000 },
			{ -44979, -113508, -194, 32000 },
			{ -84119, 243254, -3725, 8000 },
			{ -84336, 242156, -3725, 24500 },
			{ -82032, 150160, -3122, 16500 },
			{ 147865, -58047, -2979, 48999 },
			{ 147300, -56466, -2779, 11500 },
			{ 44176, -48732, -800, 33000 },
			{ 44333, -47639, -800, 49999 },
			{ 87596, -140674, -1542, 16500 },
			{ 87824, -142256, -1343, 44000 },
			{ -116948, 46841, 367, 49151 }, 
		};

		SpawnNPCs(TEST_SERVER_HELPER_MR_CAT, MR_CAT, _spawns_cat);
	}

	private void spawnMissQueen()
	{
		final int MISS_QUEEN[][] = 
		{
			{ 147761, 27408, -2198, 16500 },
			{ 148514, -57972, -2974, 53000 },
			{ 110592, 220443, -3667, 0 },
			{ 117160, 75784, -2725, 25000 },
			{ 116218, -181793, -1379, 0 },
			{ 114880, -178196, -827, 0 },
			{ 83082, 147845, -3467, 32000 },
			{ 81126, 54519, -1517, 32000 },
			{ 45414, 49296, -3067, 53000 },
			{ 47680, 51255, -2989, 38500 },
			{ 17913, 170536, -3499, 48000 },
			{ 15631, 142778, -2699, 16500 },
			{ 11353, 16022, -4577, 14000 },
			{ 10918, 17511, -4567, 55000 },
			{ -14050, 123229, -3115, 32000 },
			{ -44983, -113554, -194, 32000 },
			{ -84047, 243193, -3725, 8000 },
			{ -84294, 242204, -3725, 24500 },
			{ -81967, 150160, -3122, 16500 },
			{ 147906, -58047, -2979, 48999 },
			{ 147333, -56483, -2784, 11500 },
			{ 44176, -48688, -800, 33000 },
			{ 44371, -47638, -800, 49999 },
			{ 87644, -140674, -1542, 16500 },
			{ 87856, -142272, -1344, 44000 },
			{ -116902, 46841, 367, 49151 }, 
		};

		SpawnNPCs(TEST_SERVER_HELPER_MISS_QUEEN, MISS_QUEEN, _spawns_queen);
	}

	public String DialogAppend_31756(Integer val)
	{
		if(val == 0)
		{
			Player player = getSelf();
			if(Config.NEWS_INFORMER_RND_ITEM)
				return player.isLangRus() ? "<br>[scripts_services.NewsInformer:get_random_item_window|Получить ежедневный приз.]" : "<br>[scripts_services.NewsInformer:get_random_item_window|Receive everyday gift.]";
			if(Config.NEWS_INFORMER_ONE_ITEM)
				return player.isLangRus() ? "<br>[scripts_services.NewsInformer:get_one_item_window|Получить разовый приз.]" : "<br>[scripts_services.NewsInformer:get_one_item_window|Receive gift.]";
		}
		return "";
	}

	public String DialogAppend_31757(Integer val)
	{
		if(val == 0)
		{
			Player player = getSelf();
			if(Config.NEWS_INFORMER_RND_ITEM)
				return player.isLangRus() ? "<br>[scripts_services.NewsInformer:get_random_item_window|Получить ежедневный приз.]" : "<br>[scripts_services.NewsInformer:get_random_item_window|Receive everyday gift.]";
			if(Config.NEWS_INFORMER_ONE_ITEM)
				return player.isLangRus() ? "<br>[scripts_services.NewsInformer:get_one_item_window|Получить разовый приз.]" : "<br>[scripts_services.NewsInformer:get_one_item_window|Receive gift.]";
		}
		return "";
	}

	public void get_random_item_window()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.ALLOW_NEWS_INFORMER)
		{
			show("Сервис отключен.", player);
			return;
		}
		String out = "";
		if(player.isLangRus())
		{
			out += "<html><body>Ежедневно Вы можете получить в награду случайный предмет. Да прибудет с Вами удача юный падаван :)";
			out += "предметы которые можно получить в награду:<br><br>";
			for(int i = 1; i < Config.NEWS_INFORMER_RND_ITEM_LIST.length; i++)
			{
				ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.NEWS_INFORMER_RND_ITEM_LIST[i]);
				out += "<font color=\"Yellow\">" + item.getName() + "</font>, ";
			}
			out = out.substring(0, out.length() - 2);
			out += "<br><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.NewsInformer:get_random_item\" value=\"Получить случайный предмет\">";
			out += "</body></html>";
		}
		else
		{
			out += "<html><body>Ежедневно Вы можете получить в награду случайный предмет. Да прибудет с Вами удача юный падаван :)";
			out += "предметы которые можно получить в награду:<br><br>";
			for(int i = 1; i < Config.NEWS_INFORMER_RND_ITEM_LIST.length; i++)
			{
				ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.NEWS_INFORMER_RND_ITEM_LIST[i]);
				out += "<font color=\"Yellow\">" + item.getName() + "</font>, ";
			}
			out = out.substring(0, out.length() - 2);
			out += "<br><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.NewsInformer:get_random_item\" value=\"Получить случайный предмет\">";
			out += "</body></html>";
		}
		show(out, player);
	}

	public void get_one_item_window()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.ALLOW_NEWS_INFORMER)
		{
			show("Сервис отключен.", player);
			return;
		}
		String out = "";
		if(player.isLangRus())
		{
			out += "<html><body>Вы можете получить в награду некий предмет. Да прибудет с Вами удача юный падаван :)";
			out += "предмет который можно получить в награду:<br><br>";
			ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.NEWS_INFORMER_ONE_ITEM_ID);
			out += "<font color=\"Yellow\">" + item.getName() + "</font>, ";
			out = out.substring(0, out.length() - 2);
			out += "<br><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.NewsInformer:get_one_item\" value=\"Получить случайный предмет\">";
			out += "</body></html>";
		}
		else
		{
			out += "<html><body>Вы можете получить в награду некий предмет. Да прибудет с Вами удача юный падаван :)";
			out += "предмет который можно получить в награду:<br><br>";
			ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.NEWS_INFORMER_ONE_ITEM_ID);
			out += "<font color=\"Yellow\">" + item.getName() + "</font>, ";
			out = out.substring(0, out.length() - 2);
			out += "<br><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.NewsInformer:get_one_item\" value=\"Получить случайный предмет\">";
			out += "</body></html>";
		}
		show(out, player);
	}

	public void get_random_item()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(player.getVarLong("daily_item") < System.currentTimeMillis())
		{
			extract_item_r(Config.NEWS_INFORMER_RND_ITEM_LIST, Config.NEWS_INFORMER_RND_ITEM_COUNTS, Config.NEWS_INFORMER_RND_ITEM_CHANCES, player);
			player.setVar("daily_item", System.currentTimeMillis() + 86400000, System.currentTimeMillis() + 86400000);
		}
	}

	public void get_one_item()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!player.getVarB("one_item"))
		{
			Functions.addItem(player, Config.NEWS_INFORMER_ONE_ITEM_ID, Config.NEWS_INFORMER_ONE_ITEM_COUNT);
			player.setVar("one_item", 1, System.currentTimeMillis() + 31536000000L);
		}
	}

	private static void extract_item_r(int[] list, int[] counts, int[] chances, Player player)
	{
		int sum = 0;

		for(int i = 0; i < list.length; i++)
			sum += chances[i];

		int[] table = new int[sum];
		int k = 0;

		for(int i = 0; i < list.length; i++)
			for(int j = 0; j < chances[i]; j++)
			{
				table[k] = i;
				k++;
			}

		int i = table[Rnd.get(table.length)];
		int item = list[i];
		int count = counts[i];

		Functions.addItem(player, item, count);
	}

	@Override
	public void onLoad()
	{
		if(Config.ALLOW_NEWS_INFORMER)
		{
			spawnMrCat();
			spawnMissQueen();
			_log.info("Loaded Service: Test Server Helper");
		}
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}