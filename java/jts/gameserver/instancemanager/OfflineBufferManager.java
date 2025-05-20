package jts.gameserver.instancemanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.CharTemplateHolder;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.entity.residence.ClanHall;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.skills.SkillTargetType;
import jts.gameserver.skills.SkillType;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.TradeHelper;
import jts.gameserver.utils.Util;

public class OfflineBufferManager
{
	protected static final Logger _log = Logger.getLogger(OfflineBufferManager.class.getName());

	private static final int MAX_INTERACT_DISTANCE = 100;

	private final Map<Integer, BufferData> _buffStores = new ConcurrentHashMap<>();

	protected OfflineBufferManager()
	{
	}

	public Map<Integer, BufferData> getBuffStores()
	{
		return _buffStores;
	}

	public void processBypass(Player player, String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();

		switch (st.nextToken())
		{
			case "setstore":
			{
				try
				{
					final int price = Integer.parseInt(st.nextToken());
					String title = st.nextToken();
					while (st.hasMoreTokens())
					{
						title += " " + st.nextToken();
					}
					title = title.trim();

					if (_buffStores.containsKey(player.getObjectId()))
					{
						break;
					}

					if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
					{
						player.sendMessage("You already have a store");
						break;
					}

					if (!Config.BUFF_STORE_ALLOWED_CLASS_LIST.contains(player.getClassId().getId()))
					{
						player.sendMessage("Your profession is not allowed to set an Buff Store");
						break;
					}

					if (!TradeHelper.checksIfCanOpenStore(player, Player.STORE_PRIVATE_BUFF))
					{
						break;
					}

					if (title.isEmpty() || title.length() >= 29)
					{
						player.sendMessage("You must put a title for this store and it must have less than 29 characters");
						throw new Exception();
					}

					if (price < 1 || price > 10000000)
					{
						player.sendMessage("The price for each buff must be between 1 and 10kk");
						throw new Exception();
					}

					final ClanHall ch = ResidenceHolder.getInstance().getResidenceByObject(ClanHall.class, player);
					if (!player.isGM() && !player.isInZone(ZoneType.buff_store_only) && ch == null)
					{
						player.sendMessage("You can't put a buff store here. Look for special designated zones or clan halls");
						break;
					}

					if (player.isAlikeDead() || player.isInOlympiadMode() || player.isMounted() || player.isCastingNow()
						|| player.getOlympiadObserveGame() != null || player.getOlympiadGame() != null  || Olympiad.isRegisteredInComp(player))
					{
						player.sendMessage("You don't meet the required conditions to put a buff store right now");
						break;
					}

					final BufferData buffer = new BufferData(player, title, price, null);

					for (Skill skill : player.getAllSkills())
					{
						if (!skill.isActive())
							continue;

						if (skill.getSkillType() != SkillType.BUFF)
							continue;

						if (skill.isHeroic())
							continue;

						if (skill.getTargetType() == SkillTargetType.TARGET_SELF)
							continue;

						if (skill.getTargetType() == SkillTargetType.TARGET_PET)
							continue;

						if (player.getClassId().equalsOrChildOf(ClassId.doomcryer) && skill.getTargetType() == SkillTargetType.TARGET_CLAN)
							continue;

						if (player.getClassId().equalsOrChildOf(ClassId.dominator)
							&& (skill.getTargetType() == SkillTargetType.TARGET_PARTY || skill.getTargetType() == SkillTargetType.TARGET_ONE))
							continue;

						if (Config.BUFF_STORE_FORBIDDEN_SKILL_LIST.contains(skill.getId()))
							continue;

						buffer.getBuffs().put(skill.getId(), skill);
					}

					if (buffer.getBuffs().isEmpty())
					{
						player.sendMessage("You don't have any available buff to put on sale in the store");
						break;
					}

					_buffStores.put(player.getObjectId(), buffer);

					player.sitDown(null);
					player.setTitle(title);
					player.broadcastUserInfo(true);

					player.setPrivateStoreType(Player.STORE_PRIVATE_BUFF);

					player.sendMessage("Your Buff Store was set succesfully");
				}
				catch (NumberFormatException e)
				{
					player.sendMessage("The price for each buff must be between 1 and 10kk");

					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("command/buffstore/buff_store_create.htm");
					player.sendPacket(html);
				}
				catch (Exception e)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("command/buffstore/buff_store_create.htm");
					player.sendPacket(html);
				}
				break;
			}
			case "stopstore":
			{
				if (player.getPrivateStoreType() != Player.STORE_PRIVATE_BUFF)
				{
					player.sendMessage("You dont have any store set right now");
					break;
				}

				_buffStores.remove(player.getObjectId());

				player.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
				player.standUp();
				player.setTitle(null);
				player.broadcastUserInfo(true);

				player.sendMessage("Your Buff Store was removed succesfuly");

				break;
			}
			case "bufflist":
			{
				try
				{
					final int playerId = Integer.parseInt(st.nextToken());
					final boolean isPlayer = (st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("player") : true);
					final int page = (st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0);

					final BufferData buffer = _buffStores.get(playerId);
					if (buffer == null)
					{
						break;
					}

					if (Util.calculateDistance(player, buffer.getOwner(), true) > MAX_INTERACT_DISTANCE)
					{
						break;
					}

					if (!isPlayer && player.getPet() == null)
					{
						player.sendMessage("You don't have any active summon right now");

						showStoreWindow(player, buffer, !isPlayer, page);
						break;
					}

					showStoreWindow(player, buffer, isPlayer, page);
				}
				catch (Exception e)
				{

				}
				break;
			}
			case "purchasebuff":
			{
				try
				{
					final int playerId = Integer.parseInt(st.nextToken());
					final boolean isPlayer = (st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("player") : true);
					final int buffId = Integer.parseInt(st.nextToken());
					final int page = (st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0);

					final BufferData buffer = _buffStores.get(playerId);
					if (buffer == null)
					{
						break;
					}

					if (!buffer.getBuffs().containsKey(buffId))
					{
						break;
					}

					if (Util.calculateDistance(player, buffer.getOwner(), true) > MAX_INTERACT_DISTANCE)
					{
						break;
					}

					if (!isPlayer && player.getPet() == null)
					{
						player.sendMessage("You don't have any active summon right now");

						showStoreWindow(player, buffer, !isPlayer, page);
						break;
					}

					if (player.getPvpFlag() > 0 || player.isInCombat() || player.getKarma() > 0 || player.isAlikeDead()
						|| player.isInOlympiadMode() || player.isCursedWeaponEquipped()
						|| player.isInStoreMode() || player.isInTrade() || player.getEnchantScroll() != null || player.isFishing())
					{
						player.sendMessage("You don't meet the required conditions to use the buffer right now");
						break;
					}

					final double buffMpCost = (Config.BUFF_STORE_MP_ENABLED ? buffer.getBuffs().get(buffId).getMpConsume() * Config.BUFF_STORE_MP_CONSUME_MULTIPLIER : 0);

					if (buffMpCost > 0 && buffer.getOwner().getCurrentMp() < buffMpCost)
					{
						player.sendMessage("This store doesn't have enough mp to give sell you this buff");

						showStoreWindow(player, buffer, isPlayer, page);
						break;
					}

					final int buffPrice = (player.getClanId() == buffer.getOwner().getClanId() ? 0 : buffer.getBuffPrice());

					if (buffPrice > 0 && player.getAdena() < buffPrice)
					{
						player.sendMessage("You don't have enough adena to purchase a buff");
						break;
					}

					if (buffPrice > 0 && !player.reduceAdena(buffPrice, true))
					{
						player.sendMessage("You don't have enough adena to purchase a buff");
						break;
					}

					if (buffPrice > 0)
						buffer.getOwner().addAdena(buffPrice, true);

					if (buffMpCost > 0)
						buffer.getOwner().reduceCurrentMp(buffMpCost, null);

					if (isPlayer)
						buffer.getBuffs().get(buffId).getEffects(player, player, false, false);
					else
						buffer.getBuffs().get(buffId).getEffects(player.getPet(), player.getPet(), false, false);

					player.sendMessage("You have bought " + buffer.getBuffs().get(buffId).getName() + " from " + player.getName());

					showStoreWindow(player, buffer, isPlayer, page);
				}
				catch (Exception e)
				{

				}
				break;
			}
		}
	}

	private void showStoreWindow(Player player, BufferData buffer, boolean isForPlayer, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("command/buffstore/buff_store_buffer.htm");

		final int MAX_ENTRANCES_PER_ROW = 7;
		final double entrancesSize = buffer.getBuffs().size();
		final int maxPage = (int)Math.ceil(entrancesSize / MAX_ENTRANCES_PER_ROW) - 1;
		final int currentPage = Math.min(maxPage, page);

		final StringBuilder buffList = new StringBuilder();
		final Iterator<Skill> it = buffer.getBuffs().values().iterator();
		Skill buff;
		int i = 0;
		int baseMaxLvl;
		int enchantLvl;
		int enchantType;
		boolean changeColor = false;

		while (it.hasNext())
		{
			if (i < currentPage * MAX_ENTRANCES_PER_ROW)
			{
				it.next();
				i++;
				continue;
			}

			if (i >= (currentPage * MAX_ENTRANCES_PER_ROW + MAX_ENTRANCES_PER_ROW))
				break;

			buff = it.next();
			baseMaxLvl = SkillTable.getInstance().getBaseLevel(buff.getId());

			buffList.append("<tr>");
			buffList.append("<td fixwidth=300>");
			buffList.append("<table height=36 cellspacing=-1 bgcolor=" + (changeColor ? "171612" : "23221e") + ">");
			buffList.append("<tr>");
			buffList.append("<td width=42 valign=top><button value=\"\" action=\"bypass -h BuffStore purchasebuff " + buffer.getOwner().getObjectId() + " " + (isForPlayer ? "player" : "summon") + " " + buff.getId() + " " + currentPage + "\" width=32 height=32 back=" + buff.getIcon() + " fore=" + buff.getIcon() + "></td>");
			if (buff.getLevel() > baseMaxLvl)
			{
				enchantType = (buff.getLevel() - baseMaxLvl) / buff.getEnchantLevelCount();
				enchantLvl = (buff.getLevel() - baseMaxLvl) % buff.getEnchantLevelCount();
				enchantLvl = (enchantLvl == 0 ? buff.getEnchantLevelCount() : enchantLvl);

				buffList.append("<td fixwidth=240>" + buff.getName() + " <font color=a3a3a3>Lv</font> <font color=ae9978>" + baseMaxLvl + "</font>");
				buffList.append(" <font color=ffd969>+" + enchantLvl + " " + (enchantType >= 3 ? "Power" : (enchantType >= 2 ? "Cost" : "Time")) + "</font></td>");
			}
			else
			{
				buffList.append("<td fixwidth=240>" + buff.getName() + " <font color=a3a3a3>Lv</font> <font color=ae9978>" + buff.getLevel() + "</font></td>");
			}
			buffList.append("</tr>");
			buffList.append("</table>");
			buffList.append("</td>");
			buffList.append("</tr>");

			buffList.append("<tr>");
			buffList.append("<td height=10></td>");
			buffList.append("</tr>");

			i++;
			changeColor = !changeColor;
		}

		final String previousPageButton;
		final String nextPageButton;
		if (currentPage > 0)
			previousPageButton = "<button value=\"\" width=16 height=16 action=\"bypass -h BuffStore bufflist " + buffer.getOwner().getObjectId() + " " + (isForPlayer ? "player" : "summon")  + " " + (currentPage - 1) + "\" fore=L2UI_CH3.shortcut_prev_down back=L2UI_CH3.shortcut_prev>";
		else
			previousPageButton = "<button value=\"\" width=16 height=16 action=\"\" fore=L2UI_CH3.shortcut_prev_down back=L2UI_CH3.shortcut_prev>";

		if (currentPage < maxPage)
			nextPageButton = "<button value=\"\" width=16 height=16 action=\"bypass -h BuffStore bufflist " + buffer.getOwner().getObjectId() + " " + (isForPlayer ? "player" : "summon") + " " + (currentPage + 1) + "\" fore=L2UI_CH3.shortcut_next_down back=L2UI_CH3.shortcut_next>";
		else
			nextPageButton = "<button value=\"\" width=16 height=16 action=\"\" fore=L2UI_CH3.shortcut_next_down back=L2UI_CH3.shortcut_next>";

		html.replace("%bufferId%", buffer.getOwner().getObjectId());
		html.replace("%bufferClass%", Util.toProperCaseAll(CharTemplateHolder.getInstance().getTemplate(buffer.getOwner().getClassId(), false).className));
		html.replace("%bufferLvl%", (buffer.getOwner().getLevel() >= 76 && buffer.getOwner().getLevel() < 80 ? 76 : (buffer.getOwner().getLevel() >= 84 ? 84 : Math.round(buffer.getOwner().getLevel() / 10) * 10)));
		html.replace("%bufferName%", buffer.getOwner().getName());
		html.replace("%bufferMp%", (int)buffer.getOwner().getCurrentMp());
		html.replace("%buffPrice%", Util.convertToLineagePriceFormat(buffer.getBuffPrice()));
		html.replace("%target%", (isForPlayer ? "Player" : "Summon"));
		html.replace("%page%", currentPage);
		html.replace("%buffs%", buffList.toString());
		html.replace("%previousPageButton%", previousPageButton);
		html.replace("%nextPageButton%", nextPageButton);
		html.replace("%pageCount%", (currentPage + 1) + "/" + (maxPage + 1));

		player.sendPacket(html);
	}

	public static OfflineBufferManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final OfflineBufferManager _instance = new OfflineBufferManager();
	}

	public static class BufferData
	{
		private final Player _owner;
		private final String _saleTitle;
		private final int _buffPrice;
		private final Map<Integer, Skill> _buffs = new HashMap<>();

		public BufferData(Player player, String title, int price, List<Skill> buffs)
		{
			_owner = player;
			_saleTitle = title;
			_buffPrice = price;
			if (buffs != null)
			{
				for (Skill buff : buffs)
				{
					_buffs.put(buff.getId(), buff);
				}
			}
		}

		public Player getOwner()
		{
			return _owner;
		}

		public String getSaleTitle()
		{
			return _saleTitle;
		}

		public int getBuffPrice()
		{
			return _buffPrice;
		}

		public Map<Integer, Skill> getBuffs()
		{
			return _buffs;
		}
	}
}