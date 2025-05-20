package services;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.SkillList;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import quests._234_FatesWhisper;

public class NoblessSell extends Functions
{
	public void get()
	{
		Player player = getSelf();

		if(player.isNoble())
		{
			player.sendMessage(player.isLangRus() ? "Вы уже дворянин." : "You have a Noblless.");
			return;
		}

		if(player.getSubLevel() < 75)
		{
			player.sendMessage(player.isLangRus() ? "Ваш саб-класс должен быть не менее 75 уровня." : "Your sub-class must be 75 level.");
			return;
		}

		if(player.getInventory().destroyItemByItemId(Config.SERVICES_NOBLESS_SELL_ITEM, Config.SERVICES_NOBLESS_SELL_PRICE))
		{
			makeSubQuests();
			becomeNoble();
		}
		else if(Config.SERVICES_NOBLESS_SELL_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public void makeSubQuests()
	{
		Player player = getSelf();
		if(player == null)
			return;
		Quest q = QuestManager.getQuest(_234_FatesWhisper.class);
		QuestState qs = player.getQuestState(q.getClass());
		if(qs != null)
			qs.exitQuest(true);
		q.newQuestState(player, Quest.COMPLETED);

		if(player.getRace() == Race.kamael)
		{
			q = QuestManager.getQuest("_236_SeedsOfChaos");
			qs = player.getQuestState(q.getClass());
			if(qs != null)
				qs.exitQuest(true);
			q.newQuestState(player, Quest.COMPLETED);
		}
		else
		{
			q = QuestManager.getQuest("_235_MimirsElixir");
			qs = player.getQuestState(q.getClass());
			if(qs != null)
				qs.exitQuest(true);
			q.newQuestState(player, Quest.COMPLETED);
		}
	}

	public void becomeNoble()
	{
		Player player = getSelf();
		if(player == null || player.isNoble())
			return;

		Olympiad.addNoble(player);
		player.setNoble(true);
		player.updatePledgeClass();
		player.updateNobleSkills();
		player.sendPacket(new SkillList(player));
		player.broadcastUserInfo(true);
	}
}