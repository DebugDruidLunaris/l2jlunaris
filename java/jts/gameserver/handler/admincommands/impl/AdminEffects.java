package jts.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.base.InvisibleType;
import jts.gameserver.network.serverpackets.Earthquake;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.Say2;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.network.serverpackets.components.IStaticPacket;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.skills.AbnormalEffect;
import jts.gameserver.skills.SkillType;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Util;

public class AdminEffects implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_invis,
		admin_vis,
		admin_offline_vis,
		admin_offline_invis,
		admin_earthquake,
		admin_para_everybody,
		admin_para,
		admin_unpara_everybody,
		admin_unpara,
		admin_block,
		admin_unblock,
		admin_changename,
		admin_gmspeed,
		admin_invul,
		admin_setinvul,
		admin_getinvul,
		admin_social,
		admin_abnormal,
		admin_transform,
		admin_showmovie,
		admin_cast
	}

	@SuppressWarnings({ "rawtypes", "incomplete-switch" })
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().GodMode)
			return false;

		int val;
		AbnormalEffect ae = AbnormalEffect.NULL;
		GameObject target = activeChar.getTarget();

		switch(command)
		{
			case admin_invis:
			case admin_vis:
				if(activeChar.isInvisible())
				{
					activeChar.setInvisibleType(InvisibleType.NONE);
					activeChar.broadcastCharInfo();
					if(activeChar.getPet() != null)
					{
						activeChar.getPet().broadcastCharInfo();
					}
				}
				else
				{
					activeChar.setInvisibleType(InvisibleType.NORMAL);
					activeChar.sendUserInfo(true);
					World.removeObjectFromPlayers(activeChar);
				}
				break;
			case admin_gmspeed:
				if(wordList.length < 2)
				{
					val = 0;
				}
				else
				{
					try
					{
						val = Integer.parseInt(wordList[1]);
					}
					catch(Exception e)
					{
						activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
						return false;
					}
				}
				List<Effect> superhaste = activeChar.getEffectList().getEffectsBySkillId(7029);
				int sh_level = superhaste == null ? 0 : superhaste.isEmpty() ? 0 : superhaste.get(0).getSkill().getLevel();

				if(val == 0)
				{
					if(sh_level != 0)
					{
						activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true); //снимаем еффект
					}
					activeChar.unsetVar("gm_gmspeed");
				}
				else if(val >= 1 && val <= 4)
				{
					if(Config.SAVE_GM_EFFECTS)
					{
						activeChar.setVar("gm_gmspeed", String.valueOf(val), -1);
					}
					if(val != sh_level)
					{
						if(sh_level != 0)
						{
							activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true); //снимаем еффект
						}
						activeChar.doCast(SkillTable.getInstance().getInfo(7029, val), activeChar, true);
					}
				}
				else
				{
					activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
				}
				break;
			case admin_invul:
				handleInvul(activeChar, activeChar);
				if(activeChar.isInvul())
				{
					if(Config.SAVE_GM_EFFECTS)
					{
						activeChar.setVar("gm_invul", "true", -1);
					}
				}
				else
				{
					activeChar.unsetVar("gm_invul");
				}
				break;
		}

		if(!activeChar.isGM())
			return false;

		switch(command)
		{
			case admin_offline_vis:
				for(Player player : GameObjectsStorage.getAllPlayers())
					if(player != null && player.isInOfflineMode())
					{
						player.setInvisibleType(InvisibleType.NONE);
						player.decayMe();
						player.spawnMe();
					}
				break;
			case admin_offline_invis:
				for(Player player : GameObjectsStorage.getAllPlayers())
					if(player != null && player.isInOfflineMode())
					{
						player.setInvisibleType(InvisibleType.NORMAL);
						player.decayMe();
					}
				break;
			case admin_earthquake:
				try
				{
					int intensity = Integer.parseInt(wordList[1]);
					int duration = Integer.parseInt(wordList[2]);
					activeChar.broadcastPacket(new Earthquake(activeChar.getLoc(), intensity, duration));
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //earthquake intensity duration");
					return false;
				}
				break;
			case admin_para_everybody:
			case admin_para:
				Collection<Creature> targets = new ArrayList<>();
				int minutes = -1;
				String reason = null;
				if (command == Commands.admin_para_everybody)
				{
					for (Player playerToPara : GameObjectsStorage.getAllPlayersForIterate())
						if (playerToPara.isOnline() && playerToPara.getNetConnection() != null && !playerToPara.isGM())
							targets.add(playerToPara);
				}
				else if (wordList.length == 2)
				{
					int radius = Integer.parseInt(wordList[1]);
					targets.addAll(World.getAroundPlayables(activeChar, radius, 500));
				}
				else if (target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				else
				{
					targets.add((Creature) activeChar.getTarget());
					if (wordList.length >= 3)
					{
						minutes = Integer.parseInt(wordList[1]);
						StringBuilder reasonBuilder = new StringBuilder();
						for (int i = 2; i < wordList.length; i++)
							reasonBuilder.append(wordList[i]).append(' ');
						reason = reasonBuilder.toString();
					}
				}

				IStaticPacket packet = new Say2(activeChar.getObjectId(), ChatType.TELL, "Paralyze", "Вы порализованы на " + minutes + " Минут ! по Причине: " + reason);
				for (Creature c : targets)
				{
					if (c.isBlocked())
						continue;
					c.startAbnormalEffect(AbnormalEffect.HOLD_1);
					c.abortAttack(true, false);
					c.abortCast(true, false);
					c.block();

					if (minutes > 0 && c.isPlayable())
					{
						c.getPlayer().setVar("Para", reason, System.currentTimeMillis() + minutes * 60000L);
						c.sendPacket(packet);
					}
				}
				activeChar.sendMessage("Target" + (targets.size() > 1 ? "s" : "") + " blocked!");
				break;
			case admin_unpara_everybody:
			case admin_unpara:
				targets = new ArrayList<>();
				if (command == Commands.admin_unpara_everybody)
				{
					for (Player playerToPara : GameObjectsStorage.getAllPlayersForIterate())
						if (playerToPara.isOnline() && playerToPara.getNetConnection() != null && !playerToPara.isGM())
							targets.add(playerToPara);
				}
				else if (wordList.length > 1)
				{
					int radius = Integer.parseInt(wordList[1]);
					targets.addAll(World.getAroundPlayables(activeChar, radius, 500));
				}
				else if (target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				else
				{
					targets.add((Creature) activeChar.getTarget());
				}
				for (Creature c : targets)
				{
					if (!c.isBlocked())
						continue;
					c.unblock();
					c.stopAbnormalEffect(AbnormalEffect.HOLD_1);
					if (c.isPlayable())
						c.getPlayer().unsetVar("Para");
				}
				activeChar.sendMessage("Targets unblocked");
				break;
			case admin_block:
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(Msg.INVALID_TARGET);
					return false;
				}
				if(((Creature) target).isBlocked())
					return false;
				((Creature) target).abortAttack(true, false);
				((Creature) target).abortCast(true, false);
				((Creature) target).block();
				activeChar.sendMessage("Target blocked.");
				break;
			case admin_unblock:
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(Msg.INVALID_TARGET);
					return false;
				}
				if(!((Creature) target).isBlocked())
					return false;
				((Creature) target).unblock();
				activeChar.sendMessage("Target unblocked.");
				break;
			case admin_changename:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //changename newName");
					return false;
				}
				if(target == null)
				{
					target = activeChar;
				}
				if(!target.isCreature())
				{
					activeChar.sendPacket(Msg.INVALID_TARGET);
					return false;
				}
				String oldName = ((Creature) target).getName();
				String newName = Util.joinStrings(" ", wordList, 1);

				((Creature) target).setName(newName);
				((Creature) target).broadcastCharInfo();

				activeChar.sendMessage("Changed name from " + oldName + " to " + newName + ".");
				break;
			case admin_setinvul:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(Msg.INVALID_TARGET);
					return false;
				}
				handleInvul(activeChar, (Player) target);
				break;
			case admin_getinvul:
				if(target != null && target.isCreature())
				{
					activeChar.sendMessage("Target " + target.getName() + "(object ID: " + target.getObjectId() + ") is " + (!((Creature) target).isInvul() ? "NOT " : "") + "invul");
				}
				break;
			case admin_social:
				if(wordList.length < 2)
				{
					val = Rnd.get(1, 7);
				}
				else
				{
					try
					{
						val = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException nfe)
					{
						activeChar.sendMessage("USAGE: //social value");
						return false;
					}
				}
				if(target == null || target == activeChar)
				{
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), val));
				}
				else if(target.isCreature())
				{
					((Creature) target).broadcastPacket(new SocialAction(target.getObjectId(), val));
				}
				break;
			case admin_abnormal:
				try
				{
					if(wordList.length > 1)
					{
						ae = AbnormalEffect.getByName(wordList[1]);
					}
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //abnormal name");
					activeChar.sendMessage("//abnormal - Clears all abnormal effects");
					return false;
				}

				Creature effectTarget = target == null ? activeChar : (Creature) target;

				if(ae == AbnormalEffect.NULL)
				{
					effectTarget.startAbnormalEffect(AbnormalEffect.NULL);
					effectTarget.sendMessage("Abnormal effects clearned by admin.");
					if(effectTarget != activeChar)
					{
						effectTarget.sendMessage("Abnormal effects clearned.");
					}
				}
				else
				{
					effectTarget.startAbnormalEffect(ae);
					effectTarget.sendMessage("Admin added abnormal effect: " + ae.getName());
					if(effectTarget != activeChar)
					{
						effectTarget.sendMessage("Added abnormal effect: " + ae.getName());
					}
				}
				break;
			case admin_transform:
				try
				{
					val = Integer.parseInt(wordList[1]);
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //transform transform_id");
					return false;
				}
				activeChar.setTransformation(val);
				break;
			case admin_showmovie:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //showmovie id");
					return false;
				}
				int id;
				try
				{
					id = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("You must specify id");
					return false;
				}
				activeChar.showQuestMovie(id);
				break;
			case admin_cast:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //cast id");
					return false;
				}
				int skillid;
				try
				{
					skillid = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("You must specify id");
					return false;
				}

				List<Creature> player = new ArrayList<Creature>();

				SkillType type = SkillTable.getInstance().getInfo(skillid, 1).getSkillType();
				int maxlevel = SkillTable.getInstance().getMaxLevel(skillid);
				int baselevel = SkillTable.getInstance().getBaseLevel(skillid);
				String name = SkillTable.getInstance().getInfo(skillid, maxlevel).getName();
				int hittime = SkillTable.getInstance().getInfo(skillid, maxlevel).getHitTime();

				player.add(activeChar);

				activeChar.sendMessage("-------------------------START-------------------------");
				activeChar.sendMessage("Skill id: " + skillid);
				activeChar.sendMessage("Skill name: " + name);
				activeChar.sendMessage("Skill enchant max level: " + maxlevel);
				activeChar.sendMessage("Skill base max level: " + baselevel);
				activeChar.sendMessage("Skill type: " + type);
				activeChar.sendMessage("--------------------------END--------------------------");
				if(type != SkillType.NOTDONE || type != SkillType.NOTUSED || type != SkillType.PASSIVE)
				{
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, skillid, maxlevel, hittime, 0));
				}
				activeChar.callSkill(SkillTable.getInstance().getInfo(skillid, baselevel), player, true);
			}
				break;
		}

		return true;
	}

	private void handleInvul(Player activeChar, Player target)
	{
		if(target.isInvul())
		{
			target.setIsInvul(false);
			target.stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			if(target.getPet() != null)
			{
				target.getPet().setIsInvul(false);
				target.getPet().stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			}
			activeChar.sendMessage(target.getName() + " is now mortal.");
		}
		else
		{
			target.setIsInvul(true);
			target.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			if(target.getPet() != null)
			{
				target.getPet().setIsInvul(true);
				target.getPet().startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			}
			activeChar.sendMessage(target.getName() + " is now immortal.");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}