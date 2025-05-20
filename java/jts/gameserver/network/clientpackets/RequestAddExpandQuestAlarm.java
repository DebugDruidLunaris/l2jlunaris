package jts.gameserver.network.clientpackets;

import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExQuestNpcLogList;

public class RequestAddExpandQuestAlarm extends L2GameClientPacket
{
	private int _questId;

	@Override
	protected void readImpl() throws Exception
	{
		_questId = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest quest = QuestManager.getQuest(_questId);
		if(quest == null)
			return;

		QuestState state = player.getQuestState(quest.getClass());
		if(state == null)
			return;

		player.sendPacket(new ExQuestNpcLogList(state));
	}
}