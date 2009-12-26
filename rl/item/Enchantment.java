package rl.item;

import rl.creature.Creature;
import rl.magic.Weave;

public class Enchantment extends Weave
{
	private boolean active;

	public Enchantment(Effect effect, int magnitude)
	{
		super(effect, magnitude, -1);
		active = false;
	}

	@Override
	public void act()
	{
		if(active)
		{
			super.act();
			Creature target = world().getActorAt(x(), y(), Creature.class);
			if(target != null)
				appendMessage(target + " is enchanted");
		}
	}

	@Override
	public void deactivate()
	{
		super.deactivate();
		active = false;
	}

	public void activate()
	{
		active = true;
	}
}
