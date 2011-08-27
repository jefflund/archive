package jade.gen.feature;

import jade.core.Actor;
import jade.core.World;
import jade.gen.Generator;
import jade.util.Dice;

public class Sprinkler extends FeatureGenerator
{
    private SprinklerPart part;

    public Sprinkler(Generator chained, SprinklerPart part)
    {
        super(chained);
        this.part = part;
    }

    @Override
    protected void generateStep(World world, Dice dice)
    {
        for(int x = 0; x < world.width(); x++)
            for(int y = 0; y < world.height(); y++)
                if(world.passableAt(x, y) && part.decide(dice, x, y))
                    world.addActor(part.getActor(dice, x, y), x, y);
    }

    public interface SprinklerPart
    {
        public boolean decide(Dice dice, int x, int y);

        public Actor getActor(Dice dice, int x, int y);
    }
}
