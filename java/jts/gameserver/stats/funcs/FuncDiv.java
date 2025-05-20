package jts.gameserver.stats.funcs;

import jts.gameserver.stats.Env;
import jts.gameserver.stats.Stats;

public class FuncDiv extends Func
{
	public FuncDiv(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public void calc(Env env)
	{
		env.value /= value;
	}
}