package jts.gameserver.network.serverpackets.components;

import jts.gameserver.data.StringHolder;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.item.ItemTemplate;

public class CustomMessage
{
	private String _text;
	private int mark = 0;

	/**
	 * Создает новый инстанс сообщения.
	 * @param address адрес(ключ) параметра с языком интернационализации
	 * @param player игрок у которого будет взят язык
	 * @param args
	 */
	public CustomMessage(String address, Player player, Object... args)
	{
		_text = StringHolder.getInstance().getNotNull(player, address);
		add(args);
	}

	/**
	 * Заменяет следующий елемент числом.<br>
	 * {0} {1} ... {Integer.MAX_VALUE}
	 * @param number чем мы хотим заменить
	 * @return этот инстанс уже с имененным текстом
	 */
	public CustomMessage addNumber(long number)
	{
		_text = _text.replace("{" + mark + "}", String.valueOf(number));
		mark++;
		return this;
	}

	public CustomMessage add(Object... args)
	{
		for(Object arg : args)
			if(arg instanceof String)
				addString((String) arg);
			else if(arg instanceof Integer)
				addNumber((Integer) arg);
			else if(arg instanceof Long)
				addNumber((Long) arg);
			else if(arg instanceof ItemTemplate)
				addItemName((ItemTemplate) arg);
			else if(arg instanceof ItemInstance)
				addItemName((ItemInstance) arg);
			else if(arg instanceof Creature)
				addCharName((Creature) arg);
			else if(arg instanceof Skill)
				this.addSkillName((Skill) arg);
			else
			{
				System.out.println("unknown CustomMessage arg type: " + arg);
				Thread.dumpStack();
			}

		return this;
	}

	/**
	 * Заменяет следующий елемент строкой.<br>
	 * {0} {1} ... {Integer.MAX_VALUE}
	 * @param str чем мы хотим заменить
	 * @return этот инстанс уже с имененным текстом
	 */
	public CustomMessage addString(String str)
	{
		_text = _text.replace("{" + mark + "}", str);
		mark++;
		return this;
	}

	/**
	 * Заменяет следующий елемент именем скилла.<br>
	 * {0} {1} ... {Integer.MAX_VALUE}
	 * @param skill именем которого мы хотим заменить.
	 * @return этот инстанс уже с имененным текстом
	 */
	public CustomMessage addSkillName(Skill skill)
	{
		_text = _text.replace("{" + mark + "}", skill.getName());
		mark++;
		return this;
	}

	/**
	 * Заменяет следующий елемент именем скилла.<br>
	 * {0} {1} ... {Integer.MAX_VALUE}
	 * @param skillId именем которого мы хотим заменить.
	 * @param skillLevel уровень скилла
	 * @return этот инстанс уже с имененным текстом
	 */
	public CustomMessage addSkillName(int skillId, int skillLevel)
	{
		return addSkillName(SkillTable.getInstance().getInfo(skillId, skillLevel));
	}

	/**
	 * Заменяет следующий елемент именем предмета.<br>
	 * {0} {1} ... {Integer.MAX_VALUE}
	 * @param item именем которого мы хотим заменить.
	 * @return этот инстанс уже с имененным текстом
	 */
	public CustomMessage addItemName(ItemTemplate item)
	{
		_text = _text.replace("{" + mark + "}", item.getName());
		mark++;
		return this;
	}

	/**
	 * Заменяет следующий елемент именем предмета.<br>
	 * {0} {1} ... {Integer.MAX_VALUE}
	 * @param itemId именем которого мы хотим заменить.
	 * @return этот инстанс уже с имененным текстом
	 */
	public CustomMessage addItemName(int itemId)
	{
		return addItemName(ItemHolder.getInstance().getTemplate(itemId));
	}

	/**
	 * Заменяет следующий елемент именем предмета.<br>
	 * {0} {1} ... {Integer.MAX_VALUE}
	 * @param item именем которого мы хотим заменить.
	 * @return этот инстанс уже с имененным текстом
	 */
	public CustomMessage addItemName(ItemInstance item)
	{
		return addItemName(item.getTemplate());
	}

	/**
	 * Заменяет следующий елемент именем персонажа.<br>
	 * {0} {1} ... {Integer.MAX_VALUE}
	 * @param cha именем которого мы хотим заменить.
	 * @return этот инстанс уже с имененным текстом
	 */
	public CustomMessage addCharName(Creature cha)
	{
		_text = _text.replace("{" + mark + "}", cha.getName());
		mark++;
		return this;
	}

	/**
	 * Возвращает локализированную строку, полученную после всех действий.
	 * @return строка.
	 */
	@Override
	public String toString()
	{
		return _text;
	}
}