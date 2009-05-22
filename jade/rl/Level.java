package jade.rl;

import jade.core.Actor;
import jade.core.World;
import jade.util.ColoredChar;

public class Level extends World
{
	public Level(int width, int height)
	{
		super(width, height);
	}
	
	public void tick()
	{
		for(Actor creature : getActors(Creature.class))
			creature.act();
		for(Actor creature : getActors(Creature.class))
			appendMessage(creature.getMessages());
		removeExpired();
	}
	
	public ColoredChar look(int x, int y)
	{
		Actor creature = getActorAt(x, y, Creature.class);
		if(creature == null)
			return super.look(x, y);
		else
			return creature.look();
	}
}
