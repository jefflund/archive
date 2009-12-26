package rl.creature;

import jade.util.Tools;

public class Stat
{
	private int value;
	private int base;

	public Stat(int base)
	{
		this.value = base;
		this.base = base;
	}

	public int value()
	{
		return value;
	}

	public void modifyValue(int change)
	{
		value += change;
	}

	public final void modifyValueCapped(int change)
	{
		modifyValue(change);
		value = Tools.clampToRange(value, 0, base);
	}

	public int base()
	{
		return base;
	}

	public String toString()
	{
		return Integer.valueOf(value).toString();
	}
}