package rl.magic;

import rl.creature.Creature;

public class Instant
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
