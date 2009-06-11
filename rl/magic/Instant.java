package rl.magic;

import java.io.Serializable;

import rl.creature.Creature;

public class Instant implements Serializable
{
	public boolean doIt(Creature target)
	{
		if(target != null)
		{
			target.expire();
			target.appendMessage(target + " disappears in a flash");
			return true;
		}
		return false;
	}
}
