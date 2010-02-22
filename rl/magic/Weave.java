package rl.magic;

import jade.core.Actor;
import java.awt.Color;
import rl.creature.Creature;

public class Weave extends Actor
{
	public enum Effect
	{
		FIRE(false, Color.red), FIRE_RES(true, Color.orange),
		CHANNEL(true, Color.white);

		private boolean undoNeeded;
		private Color color;

		Effect(boolean undoNeeded, Color color)
		{
			this.undoNeeded = undoNeeded;
			this.color = color;
		}
	}

	private Effect effect;
	private int magnitude;
	private int duration;
	private Creature affected;

	public Weave(Effect effect, int magnitude, int duration)
	{
		super('*', effect.color);
		this.magnitude = magnitude;
		this.effect = effect;
		this.duration = duration;
	}

	@Override
	public void act()
	{
		Creature target = world().getActorAt(x(), y(), Creature.class);
		if(effect.undoNeeded)
			actUndo(target);
		else
			actNoUndo(target);
		if(duration-- == 0)
			expire();
	}

	public Effect effect()
	{
		return effect;
	}

	@Override
	public void expire()
	{
		super.expire();
		deactivate();
	}

	protected void deactivate()
	{
		if(affected != null)
			undoIt();
	}

	private void actUndo(Creature target)
	{
		if(affected != null && affected != target)
			undoIt();
		if(target != null && target != affected)
			doIt(target);
	}

	private void actNoUndo(Creature target)
	{
		if(target != null)
			doIt(target);
	}

	private void doIt(Creature target)
	{
		switch(effect)
		{
		case FIRE:
			fire(target);
			break;
		case FIRE_RES:
			fireRes(target);
			break;
		case CHANNEL:
			channel(target);
			break;
		}
		affected = target;
	}

	private void undoIt()
	{
		switch(effect)
		{
		case FIRE:
			break;
		case FIRE_RES:
			fireResUndo();
			break;
		case CHANNEL:
			channelUndo();
			break;
		}
		affected = null;
	}

	private void fire(Creature target)
	{
		float resist = 1 - (float)target.fireRes().value() / 100;
		int dmg = (int)(magnitude * resist);
		if(dmg > 0)
		{
			target.hp().modifyValue(-dmg);
			target.appendMessage(target + " burns");
		}
		else
			target.appendMessage(target + " resists fire");
	}

	private void fireRes(Creature target)
	{
		target.fireRes().modifyValue(magnitude);
		appendMessage(target + " gets " + effect);
	}

	private void fireResUndo()
	{
		affected.fireRes().modifyValue(-magnitude);
		appendMessage(affected + " loses " + effect);
	}

	private void channel(Creature target)
	{
		target.mp().modifyValue(magnitude);
		appendMessage(target + " powers up");
	}

	private void channelUndo()
	{
		affected.mp().modifyValue(-magnitude);
		appendMessage(affected + " loses power");
	}
}