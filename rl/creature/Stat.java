package rl.creature;

import jade.util.Tools;

public class Stat
{
	private int value;
	private int base;
	private float progress;

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

	public void modifyBase(int change)
	{
		base += change;
	}

	public final void modifyBaseCapped(int change)
	{
		modifyBase(change);
		base = Tools.clampToRange(base, 0, base);
	}

	public String toString()
	{
		return Integer.valueOf(value).toString();
	}

	public float progress()
	{
		return progress;
	}

	public void train(float training)
	{
		progress += training;
		while(progress > 1)
		{
			progress--;
			base++;
			value++;
		}
	}
}