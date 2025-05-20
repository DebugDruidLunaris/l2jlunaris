package jts.gameserver.network.clientpackets;

import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.utils.Log_New;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _questID;

	@Override
	protected void readImpl()
	{
		_questID = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		Quest quest = QuestManager.getQuest(_questID);
		if(activeChar == null || quest == null)
			return;

		if(!quest.canAbortByPacket())
			return;

		QuestState qs = activeChar.getQuestState(quest.getClass());
		if(qs != null && !qs.isCompleted())
			qs.abortQuest();
		Log_New.LogEvent(activeChar.getName(), "Quests", "AbortQuest", new String[] { "quest ID: " + quest.getQuestIntId() + " aborted" });
	}
}