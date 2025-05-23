package handler.items;

import java.util.ArrayList;
import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.scripts.Functions;

import org.apache.commons.lang3.ArrayUtils;

//TODO вынести в датапак и реализовать через скилы
public class Extractable extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[] 
	{
		8538,
		8539,
		8540,
		5916,
		5944,
		5955,
		5966,
		5967,
		5968,
		5969,
		6007,
		6008,
		6009,
		6010,
		7725,
		7637,
		7636,
		7629,
		7630,
		7631,
		7632,
		7633,
		7634,
		7635,
		9599,
		10408,
		10473,
		13003,
		13004,
		13005,
		13006,
		13007,
		13695,
		13696,
		13697,
		13698,
		13699,
		13700,
		13701,
		13702,
		13703,
		13704,
		13705,
		13706,
		13707,
		13708,
		13709,
		13710,
		13711,
		13712,
		13713,
		13714,
		13715,
		13716,
		13717,
		13718,
		13719,
		13720,
		13721,
		13777,
		13778,
		13779,
		13780,
		13781,
		13782,
		13783,
		13784,
		13785,
		13786,
		13988,
		13989,
		13990,
		13991,
		13992,
		14531,
		14532,
		14533,
		14534,
		14535,
		14536,
		14537,
		14538,
		14539,
		14540,
		14541,
		14542,
		14543,
		14544,
		14545,
		14546,
		14547,
		14548,
		14549,
		14550,
		14551,
		14552,
		14553,
		14554,
		14555,
		14556,
		14557,
		14616,
		14833,
		14834,
		14841,
		14847,
		14884,
		14885,
		14886,
		14887,
		14888,
		14889,
		14890,
		14891,
		14892,
		14893,
		14894,
		14895,
		14896,
		14897,
		14898,
		14899,
		14900,
		14901,
		15482,
		15483,
		15358,
		17003,
		17138,
		17169,
		20195,
		20196,
		20197,
		20198,
		21169,
		21747,
		21748,
		21749,
		21752,
		21753,
		22000,
		22001,
		22002,
		22003,
		22004,
		22005,
		22087,
		22088,
		22202,
		22203,
		22204,
		22205,
		22206,
		22207,
		22310,
		22311,
		22339,
		22340 
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		if(!canBeExtracted(player, item))
			return false;

		if(!useItem(player, item, 1))
			return false;

		switch(itemId)
		{
			case 8538:
				use8538(player, ctrl);
				break;
			case 8539:
				use8539(player, ctrl);
				break;
			case 8540:
				use8540(player, ctrl);
				break;
			case 5916:
				use5916(player, ctrl);
				break;
			case 5944:
				use5944(player, ctrl);
				break;
			case 14841:
				use14841(player, ctrl);
				break;
			case 5955:
				use5955(player, ctrl);
				break;
			case 14847:
				use14847(player, ctrl);
				break;
			case 5966:
				use5966(player, ctrl);
				break;
			case 5967:
				use5967(player, ctrl);
				break;
			case 5968:
				use5968(player, ctrl);
				break;
			case 5969:
				use5969(player, ctrl);
				break;
			case 6007:
				use6007(player, ctrl);
				break;
			case 6008:
				use6008(player, ctrl);
				break;
			case 6009:
				use6009(player, ctrl);
				break;
			case 6010:
				use6010(player, ctrl);
				break;
			case 7725:
				use7725(player, ctrl);
				break;
			case 7637:
				use7637(player, ctrl);
				break;
			case 7636:
				use7636(player, ctrl);
				break;
			case 7629:
				use7629(player, ctrl);
				break;
			case 7630:
				use7630(player, ctrl);
				break;
			case 7631:
				use7631(player, ctrl);
				break;
			case 7632:
				use7632(player, ctrl);
				break;
			case 7633:
				use7633(player, ctrl);
				break;
			case 7634:
				use7634(player, ctrl);
				break;
			case 7635:
				use7635(player, ctrl);
				break;
			case 10408:
				use10408(player, ctrl);
				break;
			case 10473:
				use10473(player, ctrl);
				break;
			case 9599:
				use9599(player, ctrl);
				break;
			case 22000:
				use22000(player, ctrl);
				break;
			case 22001:
				use22001(player, ctrl);
				break;
			case 22002:
				use22002(player, ctrl);
				break;
			case 22003:
				use22003(player, ctrl);
				break;
			case 22004:
				use22004(player, ctrl);
				break;
			case 22005:
				use22005(player, ctrl);
				break;
			case 22087:
				use22087(player, ctrl);
				break;
			case 22088:
				use22088(player, ctrl);
				break;
			case 13713:
				use13713(player, ctrl);
				break;
			case 13714:
				use13714(player, ctrl);
				break;
			case 13715:
				use13715(player, ctrl);
				break;
			case 13716:
				use13716(player, ctrl);
				break;
			case 13717:
				use13717(player, ctrl);
				break;
			case 13718:
				use13718(player, ctrl);
				break;
			case 13719:
				use13719(player, ctrl);
				break;
			case 13720:
				use13720(player, ctrl);
				break;
			case 13721:
				use13721(player, ctrl);
				break;
			case 14549:
				use14549(player, ctrl);
				break;
			case 14550:
				use14550(player, ctrl);
				break;
			case 14551:
				use14551(player, ctrl);
				break;
			case 14552:
				use14552(player, ctrl);
				break;
			case 14553:
				use14553(player, ctrl);
				break;
			case 14554:
				use14554(player, ctrl);
				break;
			case 14555:
				use14555(player, ctrl);
				break;
			case 14556:
				use14556(player, ctrl);
				break;
			case 14557:
				use14557(player, ctrl);
				break;
			case 13695:
				use13695(player, ctrl);
				break;
			case 13696:
				use13696(player, ctrl);
				break;
			case 13697:
				use13697(player, ctrl);
				break;
			case 13698:
				use13698(player, ctrl);
				break;
			case 13699:
				use13699(player, ctrl);
				break;
			case 13700:
				use13700(player, ctrl);
				break;
			case 13701:
				use13701(player, ctrl);
				break;
			case 13702:
				use13702(player, ctrl);
				break;
			case 13703:
				use13703(player, ctrl);
				break;
			case 14531:
				use14531(player, ctrl);
				break;
			case 14532:
				use14532(player, ctrl);
				break;
			case 14533:
				use14533(player, ctrl);
				break;
			case 14534:
				use14534(player, ctrl);
				break;
			case 14535:
				use14535(player, ctrl);
				break;
			case 14536:
				use14536(player, ctrl);
				break;
			case 14537:
				use14537(player, ctrl);
				break;
			case 14538:
				use14538(player, ctrl);
				break;
			case 14539:
				use14539(player, ctrl);
				break;
			case 13704:
				use13704(player, ctrl);
				break;
			case 13705:
				use13705(player, ctrl);
				break;
			case 13706:
				use13706(player, ctrl);
				break;
			case 13707:
				use13707(player, ctrl);
				break;
			case 13708:
				use13708(player, ctrl);
				break;
			case 13709:
				use13709(player, ctrl);
				break;
			case 13710:
				use13710(player, ctrl);
				break;
			case 13711:
				use13711(player, ctrl);
				break;
			case 13712:
				use13712(player, ctrl);
				break;
			case 14540:
				use14540(player, ctrl);
				break;
			case 14541:
				use14541(player, ctrl);
				break;
			case 14542:
				use14542(player, ctrl);
				break;
			case 14543:
				use14543(player, ctrl);
				break;
			case 14544:
				use14544(player, ctrl);
				break;
			case 14545:
				use14545(player, ctrl);
				break;
			case 14546:
				use14546(player, ctrl);
				break;
			case 14547:
				use14547(player, ctrl);
				break;
			case 14548:
				use14548(player, ctrl);
				break;
			case 14884:
				use14884(player, ctrl);
				break;
			case 14885:
				use14885(player, ctrl);
				break;
			case 14886:
				use14886(player, ctrl);
				break;
			case 14887:
				use14887(player, ctrl);
				break;
			case 14888:
				use14888(player, ctrl);
				break;
			case 14889:
				use14889(player, ctrl);
				break;
			case 14890:
				use14890(player, ctrl);
				break;
			case 14891:
				use14891(player, ctrl);
				break;
			case 14892:
				use14892(player, ctrl);
				break;
			case 14893:
				use14893(player, ctrl);
				break;
			case 14894:
				use14894(player, ctrl);
				break;
			case 14895:
				use14895(player, ctrl);
				break;
			case 14896:
				use14896(player, ctrl);
				break;
			case 14897:
				use14897(player, ctrl);
				break;
			case 14898:
				use14898(player, ctrl);
				break;
			case 14899:
				use14899(player, ctrl);
				break;
			case 14900:
				use14900(player, ctrl);
				break;
			case 14901:
				use14901(player, ctrl);
				break;
			case 14616:
				use14616(player, ctrl);
				break;
			case 20195:
				use20195(player, ctrl);
				break;
			case 20196:
				use20196(player, ctrl);
				break;
			case 20197:
				use20197(player, ctrl);
				break;
			case 20198:
				use20198(player, ctrl);
				break;
			case 13777:
				use13777(player, ctrl);
				break;
			case 13778:
				use13778(player, ctrl);
				break;
			case 13779:
				use13779(player, ctrl);
				break;
			case 13780:
				use13780(player, ctrl);
				break;
			case 13781:
				use13781(player, ctrl);
				break;
			case 13782:
				use13782(player, ctrl);
				break;
			case 13783:
				use13783(player, ctrl);
				break;
			case 13784:
				use13784(player, ctrl);
				break;
			case 13785:
				use13785(player, ctrl);
				break;
			case 13786:
				use13786(player, ctrl);
				break;
			case 14834:
				use14834(player, ctrl);
				break;
			case 14833:
				use14833(player, ctrl);
				break;
			case 13988:
				use13988(player, ctrl);
				break;
			case 13989:
				use13989(player, ctrl);
				break;
			case 13003:
				use13003(player, ctrl);
				break;
			case 13004:
				use13004(player, ctrl);
				break;
			case 13005:
				use13005(player, ctrl);
				break;
			case 13006:
				use13006(player, ctrl);
				break;
			case 13007:
				use13007(player, ctrl);
				break;
			case 13990:
				use13990(player, ctrl);
				break;
			case 13991:
				use13991(player, ctrl);
				break;
			case 13992:
				use13992(player, ctrl);
				break;
			case 15358:
				use15358(player, ctrl);
				break;
			case 17138:
				use17138(player, ctrl);
				break;
			case 15482:
				use15482(player, ctrl);
				break;
			case 15483:
				use15483(player, ctrl);
				break;
			case 21747:
				use21747(player, ctrl);
				break;
			case 21748:
				use21748(player, ctrl);
				break;
			case 21749:
				use21749(player, ctrl);
				break;
			case 17169:
				use17169(player, ctrl);
				break;
			case 21169:
				use21169(player, ctrl);
				break;
			case 21753:
				use21753(player, ctrl);
				break;
			case 21752:
				use21752(player, ctrl);
				break;
			case 17003:
				use17003(player, ctrl);
				break;
			case 22202:
				use22202(player, ctrl);
				break;
			case 22203:
				use22203(player, ctrl);
				break;
			case 22204:
				use22204(player, ctrl);
				break;
			case 22205:
				use22205(player, ctrl);
				break;
			case 22206:
				use22206(player, ctrl);
				break;
			case 22207:
				use22207(player, ctrl);
				break;
			case 22310:
				use22310(player, ctrl);
				break;
			case 22311:
				use22311(player, ctrl);
				break;
			case 22339:
				use22339(player, ctrl);
				break;
			case 22340:
				use22340(player, ctrl);
				break;
			default:
				return false;
		}

		return true;
	}

	//------ Adventurer's Boxes ------	


	//Adventurer's Box: B-Grade Accessory (High Grade)
	private void use8538(Player player, boolean ctrl)
	{
		int[] list = new int[] { 864, 926, 895 };
		int[] chances = new int[] { 17, 17, 17 };
		int[] counts = new int[] { 1, 1, 1 };
		extract_item_r(list, counts, chances, player);
	}

	//Adventurer's Box: Hair Accessory
	private void use8539(Player player, boolean ctrl)
	{
		int[] list = new int[] { 8179, 8178, 8177 };
		int[] chances = new int[] { 10, 20, 30 };
		int[] counts = new int[] { 1, 1, 1 };
		extract_item_r(list, counts, chances, player);
	}

	//Adventurer's Box: Cradle of Creation
	private void use8540(Player player, boolean ctrl)
	{
		if(Rnd.chance(30))
			Functions.addItem(player, 8175, 1);
	}

	//Quest 370: A Wiseman Sows Seeds
	private void use5916(Player player, boolean ctrl)
	{
		int[] list = new int[] { 5917, 5918, 5919, 5920, 736 };
		int[] counts = new int[] { 1, 1, 1, 1, 1 };
		extract_item(list, counts, player);
	}

	//Quest 376: Giants Cave Exploration, Part 1
	private void use5944(Player player, boolean ctrl)
	{
		int[] list = { 5922, 5923, 5924, 5925, 5926, 5927, 5928, 5929, 5930, 5931, 5932, 5933, 5934, 5935, 5936, 5937, 5938, 5939, 5940, 5941, 5942, 5943 };
		int[] counts = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 5944, Functions.getItemCount(player, 5944));
			for(int[] res : mass_extract_item(item_count, list, counts, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item(list, counts, player);
	}

	//Quest 376: Giants Cave Exploration, Part 1
	private void use14841(Player player, boolean ctrl)
	{
		int[] list = { 14836, 14837, 14838, 14839, 14840 };
		int[] counts = { 1, 1, 1, 1, 1 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 14841, Functions.getItemCount(player, 14841));
			for(int[] res : mass_extract_item(item_count, list, counts, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item(list, counts, player);
	}

	//Quest 377: Giants Cave Exploration, Part 2, old
	private void use5955(Player player, boolean ctrl)
	{
		int[] list = { 5942, 5943, 5945, 5946, 5947, 5948, 5949, 5950, 5951, 5952, 5953, 5954 };
		int[] counts = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 5955, Functions.getItemCount(player, 5955));
			for(int[] res : mass_extract_item(item_count, list, counts, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item(list, counts, player);
	}

	//Quest 377: Giants Cave Exploration, Part 2, new
	private void use14847(Player player, boolean ctrl)
	{
		int[] list = { 14842, 14843, 14844, 14845, 14846 };
		int[] counts = { 1, 1, 1, 1, 1 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 14847, Functions.getItemCount(player, 14847));
			for(int[] res : mass_extract_item(item_count, list, counts, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item(list, counts, player);
	}

	//Quest 372: Legacy of Insolence
	private void use5966(Player player, boolean ctrl)
	{
		int[] list = new int[] { 5970, 5971, 5977, 5978, 5979, 5986, 5993, 5994, 5995, 5997, 5983, 6001 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		extract_item(list, counts, player);
	}

	//Quest 372: Legacy of Insolence
	private void use5967(Player player, boolean ctrl)
	{
		int[] list = new int[] { 5970, 5971, 5975, 5976, 5980, 5985, 5993, 5994, 5995, 5997, 5983, 6001 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		extract_item(list, counts, player);
	}

	//Quest 372: Legacy of Insolence
	private void use5968(Player player, boolean ctrl)
	{
		int[] list = new int[] { 5973, 5974, 5981, 5984, 5989, 5990, 5991, 5992, 5996, 5998, 5999, 6000, 5988, 5983, 6001 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		extract_item(list, counts, player);
	}

	//Quest 372: Legacy of Insolence
	private void use5969(Player player, boolean ctrl)
	{
		int[] list = new int[] { 5970, 5971, 5982, 5987, 5989, 5990, 5991, 5992, 5996, 5998, 5999, 6000, 5972, 6001 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		extract_item(list, counts, player);
	}

	/**
	 * Quest 373: Supplier of Reagents, from Hallate's Maid, Reagent Pouch (Gray)
	 * 2x Quicksilver (6019) 30%
	 * 2x Moonstone Shard (6013) 30%
	 * 1x Rotten Bone Piece (6014) 20%
	 * 1x Infernium Ore (6016) 20%
	 */
	private void use6007(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6019, 6013, 6014, 6016 };
		int[] counts = new int[] { 2, 2, 1, 1 };
		int[] chances = new int[] { 30, 30, 20, 20 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 6007, Functions.getItemCount(player, 6007));
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item_r(list, counts, chances, player);
	}

	/**
	 * Quest 373: Supplier of Reagents, from Platinum Tribe Shaman, Reagent Pouch (Yellow)
	 * 2x Blood Root (6017) 10%
	 * 2x Sulfur (6020) 20%
	 * 1x Rotten Bone Piece (6014) 35%
	 * 1x Infernium Ore (6016) 35%
	 */
	private void use6008(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6017, 6020, 6014, 6016 };
		int[] counts = new int[] { 2, 2, 1, 1 };
		int[] chances = new int[] { 10, 20, 35, 35 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 6008, Functions.getItemCount(player, 6008));
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item_r(list, counts, chances, player);
	}

	/**
	 * Quest 373: Supplier of Reagents, from Hames Orc Shaman, Reagent Pouch (Brown)
	 * 1x Lava Stone (6012) 20%
	 * 2x Volcanic Ash (6018) 20%
	 * 2x Quicksilver (6019) 20%
	 * 1x Moonstone Shard (6013) 40%
	 */
	private void use6009(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6012, 6018, 6019, 6013 };
		int[] counts = new int[] { 1, 2, 2, 1 };
		int[] chances = new int[] { 20, 20, 20, 40 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 6009, Functions.getItemCount(player, 6009));
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item_r(list, counts, chances, player);
	}

	/**
	 * Quest 373: Supplier of Reagents, from Platinum Guardian Shaman, Reagent Box
	 * 2x Blood Root (6017) 20%
	 * 2x Sulfur (6020) 20%
	 * 1x Infernium Ore (6016) 35%
	 * 2x Demon's Blood (6015) 25%
	 */
	private void use6010(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6017, 6020, 6016, 6015 };
		int[] counts = new int[] { 2, 2, 1, 2 };
		int[] chances = new int[] { 20, 20, 35, 25 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 6010, Functions.getItemCount(player, 6010));
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item_r(list, counts, chances, player);
	}

	//Quest 628: Hunt of Golden Ram
	private void use7725(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6035, 1060, 735, 1540, 1061, 1539 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1 };
		int[] chances = new int[] { 7, 39, 7, 3, 12, 32 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 7725, Functions.getItemCount(player, 7725));
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item_r(list, counts, chances, player);
	}

	//Quest 628: Hunt of Golden Ram
	private void use7637(Player player, boolean ctrl)
	{
		int[] list = new int[] { 4039, 4041, 4043, 4044, 4042, 4040 };
		int[] counts = new int[] { 4, 1, 4, 4, 2, 2 };
		int[] chances = new int[] { 20, 10, 20, 20, 15, 15 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 7637, Functions.getItemCount(player, 7637));
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item_r(list, counts, chances, player);
	}

	//Quest 628: Hunt of Golden Ram
	private void use7636(Player player, boolean ctrl)
	{
		int[] list = new int[] { 1875, 1882, 1880, 1874, 1877, 1881, 1879, 1876 };
		int[] counts = new int[] { 3, 3, 4, 1, 3, 1, 3, 6 };
		int[] chances = new int[] { 10, 20, 10, 10, 10, 12, 12, 16 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 7636, Functions.getItemCount(player, 7636));
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item_r(list, counts, chances, player);
	}

	//Looted Goods - White Cargo box
	private void use7629(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6688, 6689, 6690, 6691, 6693, 6694, 6695, 6696, 6697, 7579, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 330000 };
		int[] chances = new int[] { 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10 };
		extract_item_r(list, counts, chances, player);
	}

	//Looted Goods - Blue Cargo box #All chances of 8 should be 8.5, must be fixed if possible!!
	private void use7630(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6703, 6704, 6705, 6706, 6708, 6709, 6710, 6712, 6713, 6714, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 292000 };
		int[] chances = new int[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 20 };
		extract_item_r(list, counts, chances, player);
	}

	//Looted Goods - Yellow Cargo box
	private void use7631(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6701, 6702, 6707, 6711, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 930000 };
		int[] chances = new int[] { 20, 20, 20, 20, 20 };
		extract_item_r(list, counts, chances, player);
	}

	//Looted Goods - Red Filing Cabinet
	private void use7632(Player player, boolean ctrl)
	{
		int[] list;
		list = new int[] { 6857, 6859, 6861, 6863, 6867, 6869, 6871, 6875, 6877, 6879, 13100, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 340000 };
		int[] chances = new int[] { 8, 9, 8, 9, 8, 9, 8, 9, 8, 9, 8, 7 };
		extract_item_r(list, counts, chances, player);
	}

	//Looted Goods - Purple Filing Cabinet
	private void use7633(Player player, boolean ctrl)
	{
		int[] list;
		list = new int[] { 6853, 6855, 6865, 6873, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 850000 };
		int[] chances = new int[] { 20, 20, 20, 20, 20 };
		extract_item_r(list, counts, chances, player);
	}

	//Looted Goods - Brown Pouch
	private void use7634(Player player, boolean ctrl)
	{
		int[] list = new int[] { 1874, 1875, 1876, 1877, 1879, 1880, 1881, 1882, 57 };
		int[] counts = new int[] { 20, 20, 20, 20, 20, 20, 20, 20, 150000 };
		int[] chances = new int[] { 10, 10, 16, 11, 10, 5, 10, 18, 10 };
		extract_item_r(list, counts, chances, player);
	}

	//Looted Goods - Gray Pouch
	private void use7635(Player player, boolean ctrl)
	{
		int[] list = new int[] { 4039, 4040, 4041, 4042, 4043, 4044, 57 };
		int[] counts = new int[] { 4, 4, 4, 4, 4, 4, 160000 };
		int[] chances = new int[] { 20, 10, 10, 10, 20, 20, 10 };
		extract_item_r(list, counts, chances, player);
	}

	//Old Agathion
	private void use10408(Player player, boolean ctrl)
	{
		Functions.addItem(player, 6471, 20);
		Functions.addItem(player, 5094, 40);
		Functions.addItem(player, 9814, 3);
		Functions.addItem(player, 9816, 4);
		Functions.addItem(player, 9817, 4);
		Functions.addItem(player, 9815, 2);
		Functions.addItem(player, 57, 6000000);
	}

	//Magic Armor Set
	private void use10473(Player player, boolean ctrl)
	{
		Functions.addItem(player, 10470, 2); //Shadow Item - Red Crescent
		Functions.addItem(player, 10471, 2); //Shadow Item - Ring of Devotion
		Functions.addItem(player, 10472, 1); //Shadow Item - Necklace of Devotion
	}

	//Ancient Tome of the Demon
	private void use9599(Player player, boolean ctrl)
	{
		int[] list = new int[] { 9600, 9601, 9602 };
		int[] count_min = new int[] { 1, 1, 1 };
		int[] count_max = new int[] { 2, 2, 1 };
		int[] chances = new int[] { 4, 10, 1 };

		if(ctrl)
		{
			long item_count = 1 + Functions.removeItem(player, 9599, Functions.getItemCount(player, 9599));
			for(int[] res : mass_extract_item_r(item_count, list, count_min, count_max, chances, player))
				Functions.addItem(player, res[0], res[1]);
		}
		else
			extract_item_r(list, count_min, count_max, chances, player);
	}

	//****** Start Item Mall ******
	//Small fortuna box
	private void use22000(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 22006, 3 },
			{ 22007, 2 },
			{ 22008, 1 },
			{ 22014, 1 },
			{ 22022, 3 },
			{ 22023, 3 },
			{ 22024, 1 },
			{ 8743, 1 },
			{ 8744, 1 },
			{ 8745, 1 },
			{ 8753, 1 },
			{ 8754, 1 },
			{ 8755, 1 },
			{ 22025, 5 } 
		};
		double[] chances = new double[] 
		{
			20.55555,
			14.01515,
			6.16666,
			0.86999,
			3.19444,
			6.38888,
			5.75,
			10,
			8.33333,
			6.94444,
			2,
			1.6666,
			1.38888,
			12.77777 
		};
		extractRandomOneItem(player, list, chances);
	}

	//Middle fortuna box
	private void use22001(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 22007, 3 },
			{ 22008, 2 },
			{ 22009, 1 },
			{ 22014, 1 },
			{ 22015, 1 },
			{ 22022, 5 },
			{ 22023, 5 },
			{ 22024, 2 },
			{ 8746, 1 },
			{ 8747, 1 },
			{ 8748, 1 },
			{ 8756, 1 },
			{ 8757, 1 },
			{ 8758, 1 },
			{ 22025, 10 } 
		};
		double[] chances = new double[] { 27.27272, 9, 5, 0.93959, 0.32467, 3.75, 7.5, 5.625, 9.11458, 7.875, 6.5625, 1.82291, 1.575, 1.3125, 12.5 };
		extractRandomOneItem(player, list, chances);
	}

	//Large fortuna box
	private void use22002(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 22008, 2 },
			{ 22009, 1 },
			{ 22014, 1 },
			{ 22015, 1 },
			{ 22018, 1 },
			{ 22019, 1 },
			{ 22022, 10 },
			{ 22023, 10 },
			{ 22024, 5 },
			{ 8749, 1 },
			{ 8750, 1 },
			{ 8751, 1 },
			{ 8759, 1 },
			{ 8760, 1 },
			{ 8761, 1 },
			{ 22025, 20 } 
		};
		double[] chances = new double[] { 27, 15, 0.78299, 0.27056, 0.00775, 0.0027, 3.75, 7.5, 4.5, 9.75, 8.125, 6.77083, 1.95, 1.625, 1.35416, 12.5 };
		extractRandomOneItem(player, list, chances);
	}

	//Small fortuna cube
	private void use22003(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 22010, 3 },
			{ 22011, 2 },
			{ 22012, 1 },
			{ 22016, 1 },
			{ 22022, 3 },
			{ 22023, 3 },
			{ 22024, 1 },
			{ 8743, 1 },
			{ 8744, 1 },
			{ 8745, 1 },
			{ 8753, 1 },
			{ 8754, 1 },
			{ 8755, 1 },
			{ 22025, 5 } 
		};
		double[] chances = new double[] 
		{
			20.22222,
			13.78787,
			6.06666,
			0.69599,
			3.47222,
			6.94444,
			6.25,
			9.5,
			7.91666,
			6.59722,
			1.9,
			1.58333,
			1.31944,
			13.88888 
		};
		extractRandomOneItem(player, list, chances);
	}

	//Middle fortuna cube
	private void use22004(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 22011, 3 },
			{ 22012, 2 },
			{ 22013, 1 },
			{ 22016, 1 },
			{ 22017, 1 },
			{ 22022, 5 },
			{ 22023, 5 },
			{ 22024, 2 },
			{ 8746, 1 },
			{ 8747, 1 },
			{ 8748, 1 },
			{ 8756, 1 },
			{ 8757, 1 },
			{ 8758, 1 },
			{ 22025, 10 } 
		};
		double[] chances = new double[] { 26.51515, 8.75, 4.86111, 0.91349, 0.31565, 3.75, 7.5, 5.625, 9.54861, 8.25, 6.875, 1.90972, 1.65, 1.375, 12.5 };
		extractRandomOneItem(player, list, chances);
	}

	//Large fortuna cube
	private void use22005(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 22012, 2 },
			{ 22013, 1 },
			{ 22016, 1 },
			{ 22017, 1 },
			{ 22020, 1 },
			{ 22021, 1 },
			{ 22022, 10 },
			{ 22023, 10 },
			{ 22024, 5 },
			{ 8749, 1 },
			{ 8750, 1 },
			{ 8751, 1 },
			{ 8759, 1 },
			{ 8760, 1 },
			{ 8761, 1 },
			{ 22025, 20 } 
		};
		double[] chances = new double[] 
		{
			26.25,
			14.58333,
			0.69599,
			0.24049,
			0.00638,
			0.0022,
			3.95833,
			7.91666,
			4.75,
			9.58333,
			7.98611,
			6.65509,
			1.91666,
			1.59722,
			1.33101,
			13.19444 
		};
		extractRandomOneItem(player, list, chances);
	}

	// Allegra Box
	private void use22310(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 17139, 1 },
			{ 17140, 1 },
			{ 10649, 1 },
			{ 21235, 3 },
			{ 13276, 1 },
			{ 14248, 1 },
			{ 22341, 1 },
			{ 22342, 3 },
			{ 22343, 3 },
			{ 22344, 3 },
			{ 22345, 3 },
			{ 8753, 1 },
			{ 20392, 1 },
			{ 15440, 1 },
			{ 21602, 1 },
			{ 20220, 1 },
			{ 21166, 1 },
			{ 20798, 1 },
			{ 22312, 1 },
			{ 22313, 1 } 
		};
		double[] chances = new double[] 
		{
			20.55555,
			25.01515,
			18.01515,
			23.01515,
			20.01515,
			15.01515,
			25.01515,
			30.01515,
			60.01515,
			40.01515,
			80.01515,
			50.01515,
			35.01515,
			45.01515,
			35.01515,
			20.01515,
			15.01515,
			20.01515,
			25.01515,
			15.01515 
		};
		extractRandomOneItem(player, list, chances);
	}

	// Latus Box
	private void use22311(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 20521, 1 },
			{ 20519, 1 },
			{ 20518, 1 },
			{ 20520, 1 },
			{ 22314, 1 },
			{ 22315, 1 },
			{ 9627, 1 },
			{ 9552, 1 },
			{ 9553, 1 },
			{ 9554, 1 },
			{ 9555, 1 },
			{ 9576, 1 },
			{ 9575, 3 },
			{ 8762, 1 },
			{ 8752, 3 },
			{ 9551, 3 },
			{ 9547, 3 },
			{ 9549, 3 },
			{ 13757, 10 },
			{ 13758, 10 },
			{ 13759, 10 },
			{ 13760, 10 },
			{ 13761, 10 },
			{ 13762, 10 },
			{ 13763, 10 },
			{ 13764, 10 },
			{ 13765, 10 } 
		};
		double[] chances = new double[] 
		{
			1.55555,
			3.01515,
			5.55555,
			10.01515,
			15.55555,
			20.01515,
			25.01515,
			30.01515,
			30.01515,
			30.01515,
			30.01515,
			30.01515,
			30.01515,
			40.01515,
			45.01515,
			44.01515,
			50.01515,
			40.01515,
			40.01515,
			40.01515,
			35.01515,
			35.01515,
			35.01515,
			35.01515,
			35.01515,
			35.01515,
			35.01515 
		};
		extractRandomOneItem(player, list, chances);
	}

	//A Scroll Bundle of Fighter
	private void use22087(Player player, boolean ctrl)
	{
		Functions.addItem(player, 22039, 1);
		Functions.addItem(player, 22040, 1);
		Functions.addItem(player, 22041, 1);
		Functions.addItem(player, 22042, 1);
		Functions.addItem(player, 22043, 1);
		Functions.addItem(player, 22044, 1);
		Functions.addItem(player, 22047, 1);
		Functions.addItem(player, 22048, 1);
	}

	//A Scroll Bundle of Mage
	private void use22088(Player player, boolean ctrl)
	{
		Functions.addItem(player, 22045, 1);
		Functions.addItem(player, 22046, 1);
		Functions.addItem(player, 22048, 1);
		Functions.addItem(player, 22049, 1);
		Functions.addItem(player, 22050, 1);
		Functions.addItem(player, 22051, 1);
		Functions.addItem(player, 22052, 1);
		Functions.addItem(player, 22053, 1);
	}

	//****** End Item Mall ******

	//****** Belts ******
	//Gludio Supply Box - Belt: Grade B, C
	private void use13713(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Dion Supply Box - Belt: Grade B, C
	private void use13714(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Giran Supply Box - Belt: Grade B, C
	private void use13715(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Oren Supply Box - Belt: Grade B, C
	private void use13716(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Aden Supply Box - Belt: Grade B, C
	private void use13717(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Innadril Supply Box - Belt: Grade B, C
	private void use13718(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Goddard Supply Box - Belt: Grade B, C
	private void use13719(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Rune Supply Box - Belt: Grade B, C
	private void use13720(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Schuttgart Supply Box - Belt: Grade B, C
	private void use13721(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13894, 1); //Cloth Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13895, 1); //Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Gludio Supply Box - Belt: Grade S, A
	private void use14549(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Dion Supply Box - Belt: Grade S, A
	private void use14550(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Giran Supply Box - Belt: Grade S, A
	private void use14551(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Oren Supply Box - Belt: Grade S, A
	private void use14552(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Aden Supply Box - Belt: Grade S, A
	private void use14553(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Innadril Supply Box - Belt: Grade S, A
	private void use14554(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Goddard Supply Box - Belt: Grade S, A
	private void use14555(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Rune Supply Box - Belt: Grade S, A
	private void use14556(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Schuttgart Supply Box - Belt: Grade S, A
	private void use14557(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13896, 1); //Iron Belt
		else if(Rnd.chance(50))
			Functions.addItem(player, 13897, 1); //Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//****** Magic Pins ******
	//Gludio Supply Box - Magic Pin: Grade B, C
	private void use13695(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Dion Supply Box - Magic Pin: Grade B, C
	private void use13696(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Giran Supply Box - Magic Pin: Grade B, C
	private void use13697(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Oren Supply Box - Magic Pin: Grade B, C
	private void use13698(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Aden Supply Box - Magic Pin: Grade B, C
	private void use13699(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Innadril Supply Box - Magic Pin: Grade B, C
	private void use13700(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Goddard Supply Box - Magic Pin: Grade B, C
	private void use13701(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Rune Supply Box - Magic Pin: Grade B, C
	private void use13702(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Schuttgart Supply Box - Magic Pin: Grade B, C
	private void use13703(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13898, 1); //Sealed Magic Pin (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13899, 1); //Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Gludio Supply Box - Magic Pin: Grade S, A
	private void use14531(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Dion Supply Box - Magic Pin: Grade S, A
	private void use14532(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Giran Supply Box - Magic Pin: Grade S, A
	private void use14533(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Oren Supply Box - Magic Pin: Grade S, A
	private void use14534(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Aden Supply Box - Magic Pin: Grade S, A
	private void use14535(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Innadril Supply Box - Magic Pin: Grade S, A
	private void use14536(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Goddard Supply Box - Magic Pin: Grade S, A
	private void use14537(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Rune Supply Box - Magic Pin: Grade S, A
	private void use14538(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Schuttgart Supply Box - Magic Pin: Grade S, A
	private void use14539(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13900, 1); //Sealed Magic Pin (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13901, 1); //Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//****** Magic Pouchs ******
	//Gludio Supply Box - Magic Pouch: Grade B, C
	private void use13704(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Dion Supply Box - Magic Pouch: Grade B, C
	private void use13705(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Giran Supply Box - Magic Pouch: Grade B, C
	private void use13706(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Oren Supply Box - Magic Pouch: Grade B, C
	private void use13707(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Aden Supply Box - Magic Pouch: Grade B, C
	private void use13708(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Innadril Supply Box - Magic Pouch: Grade B, C
	private void use13709(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Goddard Supply Box - Magic Pouch: Grade B, C
	private void use13710(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Rune Supply Box - Magic Pouch: Grade B, C
	private void use13711(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Schuttgart Supply Box - Magic Pouch: Grade B, C
	private void use13712(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13918, 1); //Sealed Magic Pouch (C-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13919, 1); //Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Gludio Supply Box - Magic Pouch: Grade S, A
	private void use14540(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Dion Supply Box - Magic Pouch: Grade S, A
	private void use14541(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Giran Supply Box - Magic Pouch: Grade S, A
	private void use14542(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Oren Supply Box - Magic Pouch: Grade S, A
	private void use14543(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Aden Supply Box - Magic Pouch: Grade S, A
	private void use14544(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Innadril Supply Box - Magic Pouch: Grade S, A
	private void use14545(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Goddard Supply Box - Magic Pouch: Grade S, A
	private void use14546(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Rune Supply Box - Magic Pouch: Grade S, A
	private void use14547(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Schuttgart Supply Box - Magic Pouch: Grade S, A
	private void use14548(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 13920, 1); //Sealed Magic Pouch (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 13921, 1); //Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//****** Magic Rune Clip ******
	//Gludio Supply Box - Magic Rune Clip: Grade S, A
	private void use14884(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Dion Supply Box - Magic Rune Clip: Grade S, A
	private void use14885(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Giran Supply Box - Magic Rune Clip: Grade S, A
	private void use14886(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Oren Supply Box - Magic Rune Clip: Grade S, A
	private void use14887(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Aden Supply Box - Magic Rune Clip: Grade S, A
	private void use14888(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Innadril Supply Box - Magic Rune Clip: Grade S, A
	private void use14889(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Goddard Supply Box - Magic Rune Clip: Grade S, A
	private void use14890(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Rune Supply Box - Magic Rune Clip: Grade S, A
	private void use14891(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Schuttgart Supply Box - Magic Rune Clip: Grade S, A
	private void use14892(Player player, boolean ctrl)
	{
		if(Rnd.chance(50))
			Functions.addItem(player, 14902, 1); //Sealed Magic Rune Clip (A-Grade)
		else if(Rnd.chance(50))
			Functions.addItem(player, 14903, 1); //Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//****** Magic Ornament ******
	//Gludio Supply Box - Magic Ornament: Grade S, A
	private void use14893(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Dion Supply Box - Magic Ornament: Grade S, A
	private void use14894(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Giran Supply Box - Magic Ornament: Grade S, A
	private void use14895(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Oren Supply Box - Magic Ornament: Grade S, A
	private void use14896(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Aden Supply Box - Magic Ornament: Grade S, A
	private void use14897(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Innadril Supply Box - Magic Ornament: Grade S, A
	private void use14898(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Goddard Supply Box - Magic Ornament: Grade S, A
	private void use14899(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Rune Supply Box - Magic Ornament: Grade S, A
	private void use14900(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Schuttgart Supply Box - Magic Ornament: Grade S, A
	private void use14901(Player player, boolean ctrl)
	{
		if(Rnd.chance(20))
			Functions.addItem(player, 14904, 1); //Sealed Magic Ornament (A-Grade)
		else if(Rnd.chance(20))
			Functions.addItem(player, 14905, 1); //Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	//Gift from Santa Claus
	private void use14616(Player player, boolean ctrl)
	{
		//Santa Claus' Weapon Exchange Ticket - 12 Hour Expiration Period
		Functions.addItem(player, 20107, 1);

		//Christmas Red Sock
		Functions.addItem(player, 14612, 1);

		//Special Christmas Tree
		if(Rnd.chance(30))
			Functions.addItem(player, 5561, 1);

		//Christmas Tree
		if(Rnd.chance(50))
			Functions.addItem(player, 5560, 1);

		//Agathion Seal Bracelet - Rudolph (постоянный предмет)
		if(Functions.getItemCount(player, 10606) == 0 && Rnd.chance(5))
			Functions.addItem(player, 10606, 1);

		//Agathion Seal Bracelet: Rudolph - 30 дней со скилом на виталити
		if(Functions.getItemCount(player, 20094) == 0 && Rnd.chance(3))
			Functions.addItem(player, 20094, 1);

		//Chest of Experience (Event)
		if(Rnd.chance(30))
			Functions.addItem(player, 20575, 1);
	}

	//Simple Valentine Cake
	private void use20195(Player player, boolean ctrl)
	{
		//Velvety Valentine Cake
		if(Rnd.chance(20))
			Functions.addItem(player, 20196, 1);
		else
		{
			//Dragon Bomber Transformation Scroll
			if(Rnd.chance(5))
				Functions.addItem(player, 20371, 1);

			//Unicorn Transformation Scroll
			if(Rnd.chance(5))
				Functions.addItem(player, 20367, 1);

			//Quick Healing Potion
			if(Rnd.chance(10))
				Functions.addItem(player, 1540, 1);

			//Greater Healing Potion
			if(Rnd.chance(15))
				Functions.addItem(player, 1539, 1);
		}
	}

	//Velvety Valentine Cake
	private void use20196(Player player, boolean ctrl)
	{
		//Delectable Valentine Cake
		if(Rnd.chance(15))
			Functions.addItem(player, 20197, 1);
		else
		{
			//Scroll: Enchant Armor (C)
			if(Rnd.chance(10))
				Functions.addItem(player, 952, 1);

			//Scroll: Enchant Armor (B)
			if(Rnd.chance(5))
				Functions.addItem(player, 948, 1);

			//Blessed Scroll of Escape
			if(Rnd.chance(10))
				Functions.addItem(player, 1538, 1);

			//Blessed Scroll of Resurrection
			if(Rnd.chance(5))
				Functions.addItem(player, 3936, 1);

			//Agathion of Love - 3 Day Expiration Period
			if(Rnd.chance(10))
				Functions.addItem(player, 20200, 1);
		}
	}

	//Delectable Valentine Cake
	private void use20197(Player player, boolean ctrl)
	{
		//Decadent Valentine Cake
		if(Rnd.chance(10))
			Functions.addItem(player, 20198, 1);
		else
		{
			//Scroll: Enchant Weapon (C)
			if(Rnd.chance(10))
				Functions.addItem(player, 951, 1);

			//Scroll: Enchant Weapon (B)
			if(Rnd.chance(5))
				Functions.addItem(player, 947, 1);

			//Agathion of Love - 7 Day Expiration Period
			if(Rnd.chance(5))
				Functions.addItem(player, 20201, 1);
		}
	}

	//Decadent Valentine Cake
	private void use20198(Player player, boolean ctrl)
	{
		//Scroll: Enchant Weapon (S)
		if(Rnd.chance(5))
			Functions.addItem(player, 959, 1);

		//Scroll: Enchant Weapon (A)
		if(Rnd.chance(10))
			Functions.addItem(player, 729, 1);

		//Agathion of Love - 15 Day Expiration Period
		if(Rnd.chance(10))
			Functions.addItem(player, 20202, 1);

		//Agathion of Love - 30 Day Expiration Period
		if(Rnd.chance(5))
			Functions.addItem(player, 20203, 1);
	}

	private static final int[] SOI_books = 
	{ 
		14209, //Forgotten Scroll - Hide
		14212, //Forgotten Scroll - Enlightenment - Wizard
		14213, //Forgotten Scroll - Enlightenment - Healer
		10554, //Forgotten Scroll - Anti-Magic Armor
		14208, //Forgotten Scroll - Final Secret
		10577 //Forgotten Scroll - Excessive Loyalty
	};

	//Jewel Ornamented Duel Supplies
	private void use13777(Player player, boolean ctrl)
	{
		int rnd = Rnd.get(100);
		if(rnd <= 65)
		{
			Functions.addItem(player, 9630, 3); //3 Orichalcum
			Functions.addItem(player, 9629, 3); //3 Adamantine
			Functions.addItem(player, 9628, 4); //4 Leonard
			Functions.addItem(player, 8639, 6); //6 Elixir of CP (S-Grade)
			Functions.addItem(player, 8627, 6); //6 Elixir of Life (S-Grade)
			Functions.addItem(player, 8633, 6); //6 Elixir of Mental Strength (S-Grade)
		}
		else if(rnd <= 95)
			Functions.addItem(player, SOI_books[Rnd.get(SOI_books.length)], 1);
		else
			Functions.addItem(player, 14027, 1); //Collection Agathion Summon Bracelet
	}

	//Mother-of-Pearl Ornamented Duel Supplies
	private void use13778(Player player, boolean ctrl)
	{
		int rnd = Rnd.get(100);
		if(rnd <= 65)
		{
			Functions.addItem(player, 9630, 2); //3 Orichalcum
			Functions.addItem(player, 9629, 2); //3 Adamantine
			Functions.addItem(player, 9628, 3); //4 Leonard
			Functions.addItem(player, 8639, 5); //5 Elixir of CP (S-Grade)
			Functions.addItem(player, 8627, 5); //5 Elixir of Life (S-Grade)
			Functions.addItem(player, 8633, 5); //5 Elixir of Mental Strength (S-Grade)
		}
		else if(rnd <= 95)
			Functions.addItem(player, SOI_books[Rnd.get(SOI_books.length)], 1);
		else
			Functions.addItem(player, 14027, 1); //Collection Agathion Summon Bracelet
	}

	//Gold-Ornamented Duel Supplies
	private void use13779(Player player, boolean ctrl)
	{
		int rnd = Rnd.get(100);
		if(rnd <= 65)
		{
			Functions.addItem(player, 9630, 1); //1 Orichalcum
			Functions.addItem(player, 9629, 1); //1 Adamantine
			Functions.addItem(player, 9628, 2); //2 Leonard
			Functions.addItem(player, 8639, 4); //4 Elixir of CP (S-Grade)
			Functions.addItem(player, 8627, 4); //4 Elixir of Life (S-Grade)
			Functions.addItem(player, 8633, 4); //4 Elixir of Mental Strength (S-Grade)
		}
		else if(rnd <= 95)
			Functions.addItem(player, SOI_books[Rnd.get(SOI_books.length)], 1);
		else
			Functions.addItem(player, 14027, 1); //Collection Agathion Summon Bracelet
	}

	//Silver-Ornamented Duel Supplies
	private void use13780(Player player, boolean ctrl)
	{
		Functions.addItem(player, 8639, 4); //4 Elixir of CP (S-Grade)
		Functions.addItem(player, 8627, 4); //4 Elixir of Life (S-Grade)
		Functions.addItem(player, 8633, 4); //4 Elixir of Mental Strength (S-Grade)
	}

	//Bronze-Ornamented Duel Supplies
	private void use13781(Player player, boolean ctrl)
	{
		Functions.addItem(player, 8639, 4); //4 Elixir of CP (S-Grade)
		Functions.addItem(player, 8627, 4); //4 Elixir of Life (S-Grade)
		Functions.addItem(player, 8633, 4); //4 Elixir of Mental Strength (S-Grade)
	}

	//Non-Ornamented Duel Supplies
	private void use13782(Player player, boolean ctrl)
	{
		Functions.addItem(player, 8639, 3); //3 Elixir of CP (S-Grade)
		Functions.addItem(player, 8627, 3); //3 Elixir of Life (S-Grade)
		Functions.addItem(player, 8633, 3); //3 Elixir of Mental Strength (S-Grade)
	}

	//Weak-Looking Duel Supplies
	private void use13783(Player player, boolean ctrl)
	{
		Functions.addItem(player, 8639, 3); //3 Elixir of CP (S-Grade)
		Functions.addItem(player, 8627, 3); //3 Elixir of Life (S-Grade)
		Functions.addItem(player, 8633, 3); //3 Elixir of Mental Strength (S-Grade)
	}

	//Sad-Looking Duel Supplies
	private void use13784(Player player, boolean ctrl)
	{
		Functions.addItem(player, 8639, 3); //3 Elixir of CP (S-Grade)
		Functions.addItem(player, 8627, 3); //3 Elixir of Life (S-Grade)
		Functions.addItem(player, 8633, 3); //3 Elixir of Mental Strength (S-Grade)
	}

	//Poor-Looking Duel Supplies
	private void use13785(Player player, boolean ctrl)
	{
		Functions.addItem(player, 8639, 2); //2 Elixir of CP (S-Grade)
		Functions.addItem(player, 8627, 2); //2 Elixir of Life (S-Grade)
		Functions.addItem(player, 8633, 2); //2 Elixir of Mental Strength (S-Grade)
	}

	//Worthless Duel Supplies
	private void use13786(Player player, boolean ctrl)
	{
		Functions.addItem(player, 8639, 1); //1 Elixir of CP (S-Grade)
		Functions.addItem(player, 8627, 1); //1 Elixir of Life (S-Grade)
		Functions.addItem(player, 8633, 1); //1 Elixir of Mental Strength (S-Grade)
	}

	//Big Stakato Cocoon
	private void use14834(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{ 
			{ 9575, 1 }, //rare_80_s
			{ 10485, 1 }, //rare_82_s
			{ 10577, 1 }, //sb_excessive_loyalty
			{ 14209, 1 }, //sb_hide1
			{ 14208, 1 }, //sb_final_secret1
			{ 14212, 1 }, //sb_enlightenment_wizard1
			{ 960, 1 }, //scrl_of_ench_am_s
			{ 9625, 1 }, //codex_of_giant_forgetting
			{ 9626, 1 }, //codex_of_giant_training
			{ 959, 1 }, //scrl_of_ench_wp_s
			{ 10373, 1 }, //rp_icarus_sowsword_i
			{ 10374, 1 }, //rp_icarus_disperser_i
			{ 10375, 1 }, //rp_icarus_spirits_i
			{ 10376, 1 }, //rp_icarus_heavy_arms_i
			{ 10377, 1 }, //rp_icarus_trident_i
			{ 10378, 1 }, //rp_icarus_chopper_i
			{ 10379, 1 }, //rp_icarus_knuckle_i
			{ 10380, 1 }, //rp_icarus_wand_i
			{ 10381, 1 } 
		}; //rp_icarus_accipiter_i
		double[] chances = new double[] { 2.77, 2.31, 3.2, 3.2, 3.2, 3.2, 6.4, 3.2, 2.13, 0.64, 1.54, 1.54, 1.54, 1.54, 1.54, 1.54, 1.54, 1.54, 1.54 };
		extractRandomOneItem(player, items, chances);
	}

	//Small Stakato Cocoon
	private void use14833(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{ 
			{ 9575, 1 }, //rare_80_s
			{ 10485, 1 }, //rare_82_s
			{ 10577, 1 }, //sb_excessive_loyalty
			{ 14209, 1 }, //sb_hide1
			{ 14208, 1 }, //sb_final_secret1
			{ 14212, 1 }, //sb_enlightenment_wizard1
			{ 960, 1 }, //scrl_of_ench_am_s
			{ 9625, 1 }, //codex_of_giant_forgetting
			{ 9626, 1 }, //codex_of_giant_training
			{ 959, 1 }, //scrl_of_ench_wp_s
			{ 10373, 1 }, //rp_icarus_sowsword_i
			{ 10374, 1 }, //rp_icarus_disperser_i
			{ 10375, 1 }, //rp_icarus_spirits_i
			{ 10376, 1 }, //rp_icarus_heavy_arms_i
			{ 10377, 1 }, //rp_icarus_trident_i
			{ 10378, 1 }, //rp_icarus_chopper_i
			{ 10379, 1 }, //rp_icarus_knuckle_i
			{ 10380, 1 }, //rp_icarus_wand_i
			{ 10381, 1 } //rp_icarus_accipiter_i
		};
		double[] chances = new double[] 
		{
			2.36,
			1.96,
			2.72,
			2.72,
			2.72,
			2.72,
			5.44,
			2.72,
			1.81,
			0.54,
			1.31,
			1.31,
			1.31,
			1.31,
			1.31,
			1.31,
			1.31,
			1.31,
			1.31 
		};
		extractRandomOneItem(player, items, chances);
	}

	private void use13988(Player player, boolean ctrl)
	{
		int[] list = new int[] 
		{
			9442,
			9443,
			9444,
			9445,
			9446,
			9447,
			9448,
			9450,
			10252,
			10253,
			10215,
			10216,
			10217,
			10218,
			10219,
			10220,
			10221,
			10222,
			10223 
		};
		int[] chances = new int[] { 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 40, 40, 40, 40, 40, 40, 40, 40, 40 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		extract_item_r(list, counts, chances, player);
	}

	private void use13989(Player player, boolean ctrl)
	{
		int[] list = new int[] { 9514, 9515, 9516, 9517, 9518, 9519, 9520, 9521, 9522, 9523, 9524, 9525, 9526, 9527, 9528 };
		int[] chances = new int[] { 50, 63, 70, 75, 75, 50, 63, 70, 75, 75, 50, 63, 70, 75, 75 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		extract_item_r(list, counts, chances, player);
	}

	//Pathfinder's Reward - D-Grade
	private void use13003(Player player, boolean ctrl)
	{
		if(Rnd.chance(3.2))
			Functions.addItem(player, 947, 1); //Scroll: Enchant Weapon B
	}

	//Pathfinder's Reward - C-Grade
	private void use13004(Player player, boolean ctrl)
	{
		if(Rnd.chance(1.6111))
			Functions.addItem(player, 729, 1); //Scroll: Enchant Weapon A
	}

	//Pathfinder's Reward - B-Grade
	private void use13005(Player player, boolean ctrl)
	{
		if(Rnd.chance(1.14))
			Functions.addItem(player, 959, 1); //Scroll: Enchant Weapon S
	}

	//Pathfinder's Reward - A-Grade
	private void use13006(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{
			{ 9546, 1 },
			{ 9548, 1 },
			{ 9550, 1 },
			{ 959, 1 },
			{ 9442, 1 },
			{ 9443, 1 },
			{ 9444, 1 },
			{ 9445, 1 },
			{ 9446, 1 },
			{ 9447, 1 },
			{ 9448, 1 },
			{ 9449, 1 },
			{ 9450, 1 },
			{ 10252, 1 },
			{ 10253, 1 },
			{ 15645, 1 },
			{ 15646, 1 },
			{ 15647, 1 } 
		};
		double[] chances = new double[] { 19.8, 19.8, 19.8, 1.98, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 1, 1, 1 };
		extractRandomOneItem(player, items, chances);
	}

	//Pathfinder's Reward - S-Grade
	private void use13007(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{
			{ 9546, 1 },
			{ 9548, 1 },
			{ 9550, 1 },
			{ 959, 1 },
			{ 10215, 1 },
			{ 10216, 1 },
			{ 10217, 1 },
			{ 10218, 1 },
			{ 10219, 1 },
			{ 10220, 1 },
			{ 10221, 1 },
			{ 10222, 1 },
			{ 10223, 1 } 
		};
		double[] chances = new double[] { 26.4, 26.4, 26.4, 3.84, 0.13, 0.13, 0.13, 0.13, 0.13, 0.13, 0.13, 0.13, 0.13 };
		extractRandomOneItem(player, items, chances);
	}

	private void use13990(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6364, 6365, 6366, 6367, 6368, 6369, 6370, 6371, 6372, 6534, 6579, 7575 };
		int[] chances = new int[] { 83, 83, 83, 83, 83, 83, 83, 83, 83, 83, 83, 83 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		extract_item_r(list, counts, chances, player);
	}

	private void use13991(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6674, 6675, 6676, 6677, 6679, 6680, 6681, 6682, 6683, 6684, 6685, 6686, 6687 };
		int[] chances = new int[] { 70, 80, 95, 95, 90, 55, 95, 95, 90, 55, 95, 95, 90 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		extract_item_r(list, counts, chances, player);
	}

	private void use13992(Player player, boolean ctrl)
	{
		int[] list = new int[] { 6724, 6725, 6726 };
		int[] chances = new int[] { 25, 32, 42 };
		int[] counts = new int[] { 1, 1, 1 };
		extract_item_r(list, counts, chances, player);
	}

	//For PC Cafe exclusive Instant Lottery
	private void use15358(Player player, boolean ctrl)
	{
		int[][] list = new int[][] 
		{
			{ 15359, 1 }, //100,000 PC Cafe points wining lottery Chances: 0.001
			{ 15360, 1 }, //10,000 PC Cafe points wining lottery Chances: 0.1
			{ 15361, 1 }, //2000 PC Cafe points wining lottery Chances: 2.0
			{ 15362, 1 }, //1000 PC Cafe points wining lottery Chances: 20.0
			{ 15363, 1 } //100 PC Cafe points wining lottery Chances: 77.899
		}; 
		double[] chances = new double[] { 0.001, 0.1, 2.0, 20.0, 77.899 };
		extractRandomOneItem(player, list, chances);
	}

	//Freya's Gift
	private void use17138(Player player, boolean ctrl)
	{
		int[][] items = new int[][] { { 16026, 1 }, { 9627, 1 }, { 17139, 1 }, { 17140, 1 }, { 14052, 1 }, { 6622, 1 }, { 2134, 2 }, { 14701, 1 } };
		double[] chances = new double[] { 0.0001, 0.1417, 1.4172, 1.4172, 2.8345, 18.424, 21.2585, 54.5068 };
		extractRandomOneItem(player, items, chances);
	}

	//Santa Claus's Gift - Event
	private void use17003(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{
			{ 13457, 1 },
			{ 13458, 1 },
			{ 13459, 1 },
			{ 13460, 1 },
			{ 13461, 1 },
			{ 13462, 1 },
			{ 13463, 1 },
			{ 13464, 1 },
			{ 13465, 1 },
			{ 13466, 1 },
			{ 13467, 1 },
			{ 13468, 1 },
			{ 13469, 1 },
			{ 13470, 1 },
			{ 10215, 1 },
			{ 10216, 1 },
			{ 10217, 1 },
			{ 10218, 1 },
			{ 10219, 1 },
			{ 10220, 1 },
			{ 10221, 1 },
			{ 10222, 1 },
			{ 10223, 1 },
			{ 10224, 1 },
			{ 10225, 1 },
			{ 10226, 1 },
			{ 9380, 1 },
			{ 9446, 1 },
			{ 9384, 1 },
			{ 9449, 1 },
			{ 9450, 1 },
			{ 9445, 1 },
			{ 9443, 1 },
			{ 9442, 1 },
			{ 10252, 1 },
			{ 9448, 1 },
			{ 10253, 1 },
			{ 9384, 1 },
			{ 9444, 1 },
			{ 9447, 1 },
			{ 13143, 1 },
			{ 14105, 1 },
			{ 14108, 1 },
			{ 14109, 1 },
			{ 14110, 1 },
			{ 14111, 1 },
			{ 13144, 1 },
			{ 14106, 1 },
			{ 14112, 1 },
			{ 14113, 1 },
			{ 14114, 1 },
			{ 13145, 1 },
			{ 14107, 1 },
			{ 14115, 1 },
			{ 14116, 1 },
			{ 14117, 1 },
			{ 13887, 1 },
			{ 9514, 1 },
			{ 9515, 1 },
			{ 9516, 1 },
			{ 9517, 1 },
			{ 9518, 1 },
			{ 9529, 1 },
			{ 9519, 1 },
			{ 9520, 1 },
			{ 9521, 1 },
			{ 9522, 1 },
			{ 9523, 1 },
			{ 9524, 1 },
			{ 9525, 1 },
			{ 9526, 1 },
			{ 9527, 1 },
			{ 9528, 1 },
			{ 13886, 1 },
			{ 9570, 1 },
			{ 9571, 1 },
			{ 9572, 1 },
			{ 10480, 1 },
			{ 10481, 1 },
			{ 10482, 1 },
			{ 13071, 1 },
			{ 13072, 1 },
			{ 13073, 1 },
			{ 729, 1 },
			{ 959, 1 },
			{ 730, 1 },
			{ 960, 1 },
			{ 1538, 1 },
			{ 14686, 1 },
			{ 14687, 1 },
			{ 14693, 1 },
			{ 14692, 1 },
			{ 14698, 1 },
			{ 14699, 1 },
			{ 15439, 1 },
			{ 14611, 1 },
			{ 5560, 1 },
			{ 5562, 1 },
			{ 5563, 1 },
			{ 5564, 1 },
			{ 5565, 1 },
			{ 5566, 1 },
			{ 6622, 1 },
			{ 14701, 1 },
			{ 17005, 1 } 
		};
		double[] chances = new double[] 
		{
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0000498,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001162,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0001486,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0000733,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0001458,
			0.0008261,
			0.0008261,
			0.0008261,
			0.0004649,
			0.0004649,
			0.0004649,
			0.0002325,
			0.0002325,
			0.0002325,
			0.5263449,
			0.5263449,
			0.7655924,
			0.7655924,
			1.4975597,
			0.7681786,
			0.7681786,
			0.7686099,
			0.7686099,
			0.7681786,
			0.7681786,
			1.5217011,
			0.12009793,
			1.5389435,
			0.2401959,
			0.2401959,
			0.2401959,
			0.2401959,
			0.2401959,
			1.3044378,
			22.5439206,
			63.06608387 
		};
		extractRandomOneItem(player, items, chances);
	}

	//Beginner Adventurer's Treasure Sack
	private void use21747(Player player, boolean ctrl)
	{
		int group = Rnd.get(7);
		int[] items = new int[0];
		if(group < 4) //Low D-Grade rewards
			items = new int[] { 312, 167, 220, 258, 178, 221, 123, 156, 291, 166, 274 };
		else if(group >= 4) //Mid D-Grade rewards
			items = new int[] { 261, 224, 318, 93, 129, 294, 88, 90, 158, 172, 279, 169 };

		Functions.addItem(player, items[Rnd.get(items.length)], 1);
	}

	//Experienced Adventurer's Treasure Sack
	private void use21748(Player player, boolean ctrl)
	{
		int group = Rnd.get(10);
		int[] items = new int[0];
		if(group < 4) //Low C-Grade rewards
			items = new int[] { 160, 298, 72, 193, 192, 281, 7887, 226, 2524, 191, 71, 263 };
		else if(group >= 4 && group < 7) //Low B-Grade rewards
			items = new int[] { 78, 2571, 300, 284, 142, 267, 229, 148, 243, 92, 7892, 91 };
		else if(group >= 7 && group < 9) //Low A-Grade rewards
			items = new int[] { 98, 5233, 80, 235, 269, 288, 7884, 2504, 150, 7899, 212 };
		else if(group == 9) //Low S-Grade rewards
			items = new int[] { 6365, 6371, 6364, 6366, 6580, 7575, 6579, 6372, 6370, 6369, 6367 };

		Functions.addItem(player, items[Rnd.get(items.length)], 1);
	}

	//Great Adventurer's Treasure Sack
	private void use21749(Player player, boolean ctrl)
	{
		int group = Rnd.get(9);
		int[] items = new int[0];
		if(group < 5) //Top S-Grade rewards
			items = new int[] { 9447, 9384, 9449, 9380, 9448, 9443, 9450, 10253, 9445, 9442, 9446, 10004, 10252, 9376, 9444 };
		else if(group >= 5 && group < 8) //S80-Grade rewards
			items = new int[] { 10226, 10217, 10224, 10215, 10225, 10223, 10220, 10415, 10216, 10221, 10219, 10218, 10222 };
		else if(group == 8) //Low S84-Grade rewards
			items = new int[] { 13467, 13462, 13464, 13461, 13465, 13468, 13463, 13470, 13460, 52, 13466, 13459, 13457, 13469, 13458 };

		Functions.addItem(player, items[Rnd.get(items.length)], 1);
	}

	//Golden Spice Crate
	private void use15482(Player player, boolean ctrl)
	{
		if(Rnd.chance(10))
		{
			Functions.addItem(player, 15474, 40);
			if(Rnd.chance(50))
				Functions.addItem(player, 15476, 5);
			else
				Functions.addItem(player, 15478, 5);
		}
		else
			Functions.addItem(player, 15474, 50);
	}

	// S84 High-Grade Weapon Box
	private void use22339(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{
			{ 13467, 1 },
			{ 13462, 1 },
			{ 13464, 1 },
			{ 13461, 1 },
			{ 13465, 1 },
			{ 13468, 1 },
			{ 13463, 1 },
			{ 13470, 1 },
			{ 13460, 1 },
			{ 52, 1 },
			{ 13466, 1 },
			{ 13459, 1 },
			{ 13457, 1 },
			{ 13469, 1 },
			{ 13458, 1 } 
		};
		double[] chances = new double[] { 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0 };
		extractRandomOneItem(player, items, chances);
	}

	// Vorpal Armor Box
	private void use22340(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{
			{ 15726, 1 },
			{ 15727, 1 },
			{ 15728, 1 },
			{ 15729, 1 },
			{ 15730, 1 },
			{ 15731, 1 },
			{ 15732, 1 },
			{ 15733, 1 },
			{ 15734, 1 },
			{ 15735, 1 },
			{ 15736, 1 },
			{ 15737, 1 },
			{ 15738, 1 },
			{ 15739, 1 },
			{ 15740, 1 },
			{ 15741, 1 },
			{ 15742, 1 },
			{ 15760, 1 },
			{ 15761, 1 },
			{ 15762, 1 } 
		};
		double[] chances = new double[] 
		{
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0 
		};
		extractRandomOneItem(player, items, chances);
	}

	// S84 High-Grade Weapon Box
	private void use22202(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{
			{ 13467, 1 },
			{ 13462, 1 },
			{ 13464, 1 },
			{ 13461, 1 },
			{ 13465, 1 },
			{ 13468, 1 },
			{ 13463, 1 },
			{ 13470, 1 },
			{ 13460, 1 },
			{ 52, 1 },
			{ 13466, 1 },
			{ 13459, 1 },
			{ 13457, 1 },
			{ 13469, 1 },
			{ 13458, 1 } 
		};
		double[] chances = new double[] { 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0 };
		extractRandomOneItem(player, items, chances);
	}

	// Vorpal Armor Box
	private void use22203(Player player, boolean ctrl)
	{
		int[][] items = new int[][] 
		{
			{ 15743, 1 },
			{ 15744, 1 },
			{ 15745, 1 },
			{ 15746, 1 },
			{ 15747, 1 },
			{ 15748, 1 },
			{ 15749, 1 },
			{ 15750, 1 },
			{ 15751, 1 },
			{ 15752, 1 },
			{ 15753, 1 },
			{ 15754, 1 },
			{ 15755, 1 },
			{ 15756, 1 },
			{ 15757, 1 },
			{ 15759, 1 },
			{ 15763, 1 },
			{ 15764, 1 },
			{ 15765, 1 },
			{ 15758, 1 } 
		};
		double[] chances = new double[] 
		{
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0,
			100.0 
		};
		extractRandomOneItem(player, items, chances);
	}

	// Attribute Ore Box
	private void use22204(Player player, boolean ctrl)
	{
		int[][] items = new int[][] { { 9546, 1 }, { 9547, 1 }, { 9548, 1 }, { 9549, 1 }, { 9550, 1 }, { 9551, 1 } };
		double[] chances = new double[] { 100.0, 100.0, 100.0, 100.0, 100.0, 100.0 };
		extractRandomOneItem(player, items, chances);
	}

	// Attribute Crystal Box
	private void use22205(Player player, boolean ctrl)
	{
		int[][] items = new int[][] { { 9552, 1 }, { 9553, 1 }, { 9554, 1 }, { 9555, 1 }, { 9556, 1 }, { 9557, 1 } };
		double[] chances = new double[] { 100.0, 100.0, 100.0, 100.0, 100.0, 100.0 };
		extractRandomOneItem(player, items, chances);
	}

	// Soul Crystal - Step 17 Box
	private void use22206(Player player, boolean ctrl)
	{
		int[][] items = new int[][] { { 15541, 1 }, { 15542, 1 }, { 15543, 1 } };
		double[] chances = new double[] { 100.0, 100.0, 100.0 };
		extractRandomOneItem(player, items, chances);
	}

	// Soul Crystal - Step 18 Box
	private void use22207(Player player, boolean ctrl)
	{
		int[][] items = new int[][] { { 15826, 1 }, { 15827, 1 }, { 15828, 1 } };
		double[] chances = new double[] { 100.0, 100.0, 100.0 };
		extractRandomOneItem(player, items, chances);
	}

	//Crystal Spice Crate
	private void use15483(Player player, boolean ctrl)
	{
		if(Rnd.chance(10))
		{
			Functions.addItem(player, 15475, 40);
			if(Rnd.chance(50))
				Functions.addItem(player, 15477, 5);
			else
				Functions.addItem(player, 15479, 5);
		}
		else
			Functions.addItem(player, 15475, 50);
	}

	//Olympiad Treasure Chest
	private void use17169(Player player, boolean ctrl)
	{
		int[][] items = new int[][] { { 13750, 1 }, { 13751, 1 }, { 13754, 1 }, { 13753, 1 }, { 13752, 1 }, { 6622, 1 }, { 8621, 1 } };
		double[] chances = new double[] { 34.7, 12.3, 2.65, 1.2, 1.98, 46.5, 5.4 };
		if(Rnd.chance(60))
			extractRandomOneItem(player, items, chances);
		int[] counts = { 100, 150, 200, 250, 300, 350 };
		Functions.addItem(player, 13722, counts[Rnd.get(counts.length)]);
	}

	//Birthday Present Pack
	private void use21169(Player player, boolean ctrl)
	{
		Functions.addItem(player, 21170, 3);
		Functions.addItem(player, 21595, 1);
		Functions.addItem(player, 13488, 1);
	}

	//Pablo's Box
	private void use21753(Player player, boolean ctrl)
	{
		int category = Rnd.get(7);
		switch(category)
		{
			case 0:
				Functions.addItem(player, 21122, 1);
				break;
			case 1:
				Functions.addItem(player, 21118, 1);
				break;
			case 2:
				Functions.addItem(player, 21116, 1);
				break;
			case 3:
				Functions.addItem(player, 21114, 1);
				break;
			case 4:
				Functions.addItem(player, 21112, 1);
				break;
			case 5:
				Functions.addItem(player, 21120, 1);
				break;
			case 6:
				Functions.addItem(player, 21126, 1);
				break;
		}
	}

	//Rune Jewelry Box - Talisman
	private void use21752(Player player, boolean ctrl)
	{
		final List<Integer> talismans = new ArrayList<Integer>();

		//9914-9965
		for(int i = 9914; i <= 9965; i++)
			if(i != 9923)
				talismans.add(i);
		//10416-10424
		for(int i = 10416; i <= 10424; i++)
			talismans.add(i);
		//10518-10519
		for(int i = 10518; i <= 10519; i++)
			talismans.add(i);
		//10533-10543
		for(int i = 10533; i <= 10543; i++)
			talismans.add(i);

		Functions.addItem(player, talismans.get(Rnd.get(talismans.size())), 1);
	}

	private static void extract_item(int[] list, int[] counts, Player player)
	{
		int index = Rnd.get(list.length);
		int id = list[index];
		int count = counts[index];
		Functions.addItem(player, id, count);
	}

	private static List<int[]> mass_extract_item(long source_count, int[] list, int[] counts, Player player)
	{
		List<int[]> result = new ArrayList<int[]>((int) Math.min(list.length, source_count));

		for(int n = 1; n <= source_count; n++)
		{
			int index = Rnd.get(list.length);
			int item = list[index];
			int count = counts[index];

			int[] old = null;
			for(int[] res : result)
				if(res[0] == item)
					old = res;

			if(old == null)
				result.add(new int[] { item, count });
			else
				old[1] += count;
		}

		return result;
	}

	private static void extract_item_r(int[] list, int[] count_min, int[] count_max, int[] chances, Player player)
	{
		int[] counts = count_min;
		for(int i = 0; i < count_min.length; i++)
			counts[i] = Rnd.get(count_min[i], count_max[i]);
		extract_item_r(list, counts, chances, player);
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

	private static List<int[]> mass_extract_item_r(long source_count, int[] list, int[] count_min, int[] count_max, int[] chances, Player player)
	{
		int[] counts = count_min;
		for(int i = 0; i < count_min.length; i++)
			counts[i] = Rnd.get(count_min[i], count_max[i]);
		return mass_extract_item_r(source_count, list, counts, chances, player);
	}

	private static List<int[]> mass_extract_item_r(long source_count, int[] list, int[] counts, int[] chances, Player player)
	{
		List<int[]> result = new ArrayList<int[]>((int) Math.min(list.length, source_count));

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

		for(int n = 1; n <= source_count; n++)
		{
			int i = table[Rnd.get(table.length)];
			int item = list[i];
			int count = counts[i];

			int[] old = null;
			for(int[] res : result)
				if(res[0] == item)
					old = res;

			if(old == null)
				result.add(new int[] { item, count });
			else
				old[1] += count;
		}

		return result;
	}

	private static boolean extractRandomOneItem(Player player, int[][] items, double[] chances)
	{
		if(items.length != chances.length)
			return false;

		double extractChance = 0;
		for(double c : chances)
			extractChance += c;

		if(Rnd.chance(extractChance))
		{
			int[] successfulItems = new int[0];
			while(successfulItems.length == 0)
				for(int i = 0; i < items.length; i++)
					if(Rnd.chance(chances[i]))
						successfulItems = ArrayUtils.add(successfulItems, i);
			int[] item = items[successfulItems[Rnd.get(successfulItems.length)]];
			if(item.length < 2)
				return false;

			Functions.addItem(player, item[0], item[1]);
		}
		return true;
	}
}