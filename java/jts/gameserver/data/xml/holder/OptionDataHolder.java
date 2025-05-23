package jts.gameserver.data.xml.holder;

import jts.commons.data.xml.AbstractHolder;
import jts.gameserver.templates.OptionDataTemplate;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public final class OptionDataHolder extends AbstractHolder
{
	private static final OptionDataHolder _instance = new OptionDataHolder();

	private IntObjectMap<OptionDataTemplate> _templates = new HashIntObjectMap<OptionDataTemplate>();

	public static OptionDataHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(OptionDataTemplate template)
	{
		_templates.put(template.getId(), template);
	}

	public OptionDataTemplate getTemplate(int id)
	{
		return _templates.get(id);
	}

	@Override
	public int size()
	{
		return _templates.size();
	}

	@Override
	public void clear()
	{
		_templates.clear();
	}
}