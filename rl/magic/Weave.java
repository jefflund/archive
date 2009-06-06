package rl.magic;

import jade.core.Actor;
import java.awt.Color;

public class Weave extends Actor
{
	private int duration;
	
	public Weave(int duration)
	{
		super('*', Color.red);
		this.duration = duration;
	}
	
	public void act()
	{
		duration--;
		if(duration == 0)
			expire();
	}
}
