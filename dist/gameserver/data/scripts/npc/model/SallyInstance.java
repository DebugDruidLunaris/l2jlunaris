package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.templates.npc.NpcTemplate;
import quests._250_WatchWhatYouEat;

@SuppressWarnings("serial")
public class SallyInstance extends NpcInstance
{
	public SallyInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("ask_about_rare_plants"))
		{
			QuestState qs = player.getQuestState(_250_WatchWhatYouEat.class);
			if(qs != null && qs.isCompleted())
				showChatWindow(player, 3);
			else
				showChatWindow(player, 2);
		}
		else
			super.onBypassFeedback(player, command);
	}
}