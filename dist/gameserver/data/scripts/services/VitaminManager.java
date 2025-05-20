package services;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class VitaminManager extends Functions
{
	private static final int[] PetCoupon = { 13273, 13274, 13383, 21275, 21276 };
	private static final int[] SpecialPetCoupon = { 14065, 14074, 21279, 21280 };

	private static final int WeaselNeck = 13017;
	private static final int PrincNeck = 13018;
	private static final int BeastNeck = 13019;
	private static final int FoxNeck = 13020;

	private static final int KnightNeck = 14061;
	private static final int SpiritNeck = 14062;
	private static final int OwlNeck = 14063;
	private static final int TurtleNeck = 14064;

	public void giveWeasel()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext = npc.getNpcId() + "-no.htm";

		for(int coup : PetCoupon)
		{
			if(getItemCount(player, coup) > 0)
			{
				removeItem(player, coup, 1);
				addItem(player, WeaselNeck, 1);
				htmltext = npc.getNpcId() + "-ok.htm";
				break;
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}

	public void givePrinc()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext = npc.getNpcId() + "-no.htm";

		for(int coup : PetCoupon)
		{
			if(getItemCount(player, coup) > 0)
			{
				removeItem(player, coup, 1);
				addItem(player, PrincNeck, 1);
				htmltext = npc.getNpcId() + "-ok.htm";
				break;
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}

	public void giveBeast()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext = npc.getNpcId() + "-no.htm";

		for(int coup : PetCoupon)
		{
			if(getItemCount(player, coup) > 0)
			{
				removeItem(player, coup, 1);
				addItem(player, BeastNeck, 1);
				htmltext = npc.getNpcId() + "-ok.htm";
				break;
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}

	public void giveFox()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext = npc.getNpcId() + "-no.htm";

		for(int coup : PetCoupon)
		{
			if(getItemCount(player, coup) > 0)
			{
				removeItem(player, coup, 1);
				addItem(player, FoxNeck, 1);
				htmltext = npc.getNpcId() + "-ok.htm";
				break;
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}

	public void giveKnight()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext = npc.getNpcId() + "-no.htm";

		for(int scoup : SpecialPetCoupon)
		{
			if(getItemCount(player, scoup) > 0)
			{
				removeItem(player, scoup, 1);
				addItem(player, KnightNeck, 1);
				htmltext = npc.getNpcId() + "-ok.htm";
				break;
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}

	public void giveSpirit()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext = npc.getNpcId() + "-no.htm";

		for(int scoup : SpecialPetCoupon)
		{
			if(getItemCount(player, scoup) > 0)
			{
				removeItem(player, scoup, 1);
				addItem(player, SpiritNeck, 1);
				htmltext = npc.getNpcId() + "-ok.htm";
				break;
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}

	public void giveOwl()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext = npc.getNpcId() + "-no.htm";

		for(int scoup : SpecialPetCoupon)
		{
			if(getItemCount(player, scoup) > 0)
			{
				removeItem(player, scoup, 1);
				addItem(player, OwlNeck, 1);
				htmltext = npc.getNpcId() + "-ok.htm";
				break;
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}

	public void giveTurtle()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext = npc.getNpcId() + "-no.htm";

		for(int scoup : SpecialPetCoupon)
		{
			if(getItemCount(player, scoup) > 0)
			{
				removeItem(player, scoup, 1);
				addItem(player, TurtleNeck, 1);
				htmltext = npc.getNpcId() + "-ok.htm";
				break;
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}
}