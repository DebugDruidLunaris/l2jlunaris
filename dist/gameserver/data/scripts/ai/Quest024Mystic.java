package ai;

import jts.gameserver.ai.Mystic;
import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import quests._024_InhabitantsOfTheForestOfTheDead;

public class Quest024Mystic extends Mystic
{
	public Quest024Mystic(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		Quest q = QuestManager.getQuest(_024_InhabitantsOfTheForestOfTheDead.class);
		if(q != null)
			for(Player player : World.getAroundPlayers(getActor(), 300, 200))
			{
				QuestState questState = player.getQuestState(_024_InhabitantsOfTheForestOfTheDead.class);
				if(questState != null)
					q.notifyEvent("see_creature", questState, getActor());
			}
		return super.thinkActive();
	}
}