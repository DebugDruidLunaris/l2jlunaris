package npc.model.services;

import java.util.List;
import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.templates.npc.NpcTemplate;
import services.BonusManager;

public class BonusManagerInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private static final String HTML_INDEX = "scripts/services/BonusManager/index.htm";
	private static final String HTML_REWARD = "scripts/services/BonusManager/reward.htm";
	private static final String HTML_INFO = "scripts/services/BonusManager/info.htm";
	private static final String HTML_RESULT = "scripts/services/BonusManager/result.htm";

	private static final int STATUS_OK = 0;
	private static final int STATUS_NO = 1;
	private static final int STATUS_ERROR = 2;

	public BonusManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		BonusManager.getInstance();
	}

	@Override
	public void showChatWindow(Player player, int val, Object... args)
	{
		player.sendPacket(new NpcHtmlMessage(player, this, HtmCache.getInstance().getNotNull(HTML_INDEX, player), val));
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(Config.BONUS_SERVICE_ENABLE)
		{
			sendResult(player, "Ошибка", "Сервис временно недоступен");
			return;
		}

		if(command.equalsIgnoreCase("index"))
			player.sendPacket(new NpcHtmlMessage(player, this, HtmCache.getInstance().getNotNull(HTML_INDEX, player), 0));

		else if(command.equalsIgnoreCase("info"))
			player.sendPacket(new NpcHtmlMessage(player, this, HtmCache.getInstance().getNotNull(HTML_INFO, player), 0));

		else if(command.equalsIgnoreCase("getClanBonus"))
			getClanBonus(player);

		else if(command.equalsIgnoreCase("getPartyBonus"))
			getPartyBonus(player);

		else if(command.startsWith("giveReward"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int hash = Integer.parseInt(st.nextToken());
			giveReward(player, hash);
		}
	}

	private void giveReward(Player player, int hash)
	{
		int result = BonusManager.getInstance().doReward(hash);

		switch(result)
		{
			case STATUS_OK:
				sendResult(player, "Выполнено!", "Награда успешно выдана!");
				break;
			case STATUS_NO:
				sendResult(player, "Ошибка!", "Вы не подавали заявку на участие или бонус уже был получен!");
			case STATUS_ERROR:
				sendResult(player, "Ошибка!", "Системная ошибка! Сообщите о ней администрации.");
		}
	}

	private void getClanBonus(Player player)
	{
		if(player.getClan() == null || !player.isClanLeader())
		{
			sendResult(player, "Ошибка", "Вы должны являться лидером клана!");
			return;
		}

		if(BonusManager.getInstance().hasReward(player.getName(), BonusManager.CLAN_NAME))
		{
			sendResult(player, "Ошибка", "Ваш клан уже получал бонус или его нет в списке регистрации!");
			return;
		}

		List<Player> players = player.getClan().getOnlineMembers(0);
		BonusManager.getInstance().sortPlayers(players);
		makeRewardHtml(player, players, player.getClan().getName(), BonusManager.CLAN_NAME);
	}

	private void getPartyBonus(Player player)
	{
		if(player.getParty() == null || player.getParty().getPartyLeader() != player)
		{
			sendResult(player, "Ошибка", "Вы должны являться лидером группы!");
		}

		if(BonusManager.getInstance().hasReward(player.getName(), BonusManager.PARTY_LEADER_NAME))
		{
			sendResult(player, "Ошибка", "Ваша группа уже получала бонус или её нет в списке регистрации!");
			return;
		}

		List<Player> players = player.getParty().getPartyMembers();
		BonusManager.getInstance().sortPlayers(players);
		makeRewardHtml(player, players, player.getName(), BonusManager.PARTY_LEADER_NAME);
	}

	private void makeRewardHtml(Player player, List<Player> players, String name, String type)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(HtmCache.getInstance().getNotNull(HTML_REWARD, player));

		StringBuilder data = new StringBuilder();

		for(Player p : players)
			data.append(p.getName()).append("<br>");
		html.replace("%data%", data.toString());
		html.replace("%hash%", String.valueOf(players.hashCode()));
		BonusManager.getInstance().makeRewardList(player, players, type);
		player.sendPacket(html);
	}

	public void sendResult(Player player, String title, String data)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(HtmCache.getInstance().getNotNull(HTML_RESULT, player));
		html.replace("%title%", title);
		html.replace("%data%", data);
		player.sendPacket(html);
	}
}