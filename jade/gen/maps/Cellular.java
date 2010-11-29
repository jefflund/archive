package jade.gen.maps;

import jade.core.World;
import jade.util.Dice;
import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * A random map generator that uses cellular automata to generate cavelike
 * levels. These levels are guaranteed to be completly connected, either by
 * regenerating the level (usually done on small maps), or removing small
 * unconnected rooms (usually done on large maps). Note that certain conditions
 * such as small maps, or unusual values for generation parameters can result in
 * the algorithm never generating a complete map (see the docs for each
 * individual setter for further guidelines). In that case, the algorithm gives
 * up after a prespecified number of attempts and returns the world in state
 * which will most likely be undesirable (you have been warned).
 */
public class Cellular extends MapGen
{
    private ColoredChar openFace = new ColoredChar('.', Color.white);
    private ColoredChar wallFace = new ColoredChar('#', Color.white);
    private int tileChance = 40;
    private int minCount1 = 5;
    private int maxCount2 = 3;
    private int iterations = 5;
    private int maxGen = 20;

    @Override
    public void genStep(World world, Dice dice)
    {
        boolean[][] buffer = new boolean[world.width()][world.height()];
        boolean[][] temp = new boolean[world.width()][world.height()];
        int gens = 0;
        do
        {
            initCells(buffer, dice);
            for(int i = 0; i < iterations; i++)
                cellular(buffer, temp);
            applyBuffer(world, buffer);
            gens++;
        }
        while(!connect(world, dice) && gens < maxGen);
    }

    private boolean connect(World world, Dice dice)
    {
        if(!openExist(world))
            return false;

        Set<Coord> open = getOpenRegion(world, dice);
        if((open.size() * 100 / (world.width() * world.height())) > tileChance)
        {
            for(int x = 0; x < world.width(); x++)
                for(int y = 0; y < world.height(); y++)
                {
                    if(world.passable(x, y) && !open.contains(new Coord(x, y)))
                        world.setTile(wallFace, false, x, y);
                }
            return true;
        }
        else
            return false;
    }

    private boolean openExist(World world)
    {
        for(int x = 0; x < world.width(); x++)
            for(int y = 0; y < world.height(); y++)
                if(world.passable(x, y))
                    return true;
        return false;
    }

    private Set<Coord> getOpenRegion(World world, Dice dice)
    {
        Set<Coord> region = new HashSet<Coord>();
        Stack<Coord> stack = new Stack<Coord>();
        stack.push(world.getOpenTile(dice));
        int count = 1;
        while(!stack.isEmpty())
        {
            Coord curr = stack.pop();
            if(!region.contains(curr) && world.passable(curr))
            {
                count++;
                region.add(curr);
                for(int dx = -1; dx <= 1; dx++)
                    for(int dy = -1; dy <= 1; dy++)
                        if(dx != 0 || dy != 0)
                            stack.push(curr.getTranslated(dx, dy));
            }
        }
        return region;
    }

    private void cellular(boolean[][] buffer, boolean[][] temp)
    {
        for(int x = 1; x < buffer.length - 1; x++)
            for(int y = 1; y < buffer[x].length - 1; y++)
            {
                if(wallcount(buffer, x, y, 1) > minCount1
                        || wallcount(buffer, x, y, 2) <= maxCount2)
                    temp[x][y] = false;
                else
                    temp[x][y] = true;
            }
        for(int x = 1; x < buffer.length - 1; x++)
            for(int y = 1; y < buffer[x].length - 1; y++)
                buffer[x][y] = temp[x][y];
    }

    private int wallcount(boolean[][] buffer, int x, int y, int range)
    {
        int count = 0;
        for(int dx = x - range; dx <= x + range; dx++)
            for(int dy = y - range; dy <= y + range; dy++)
            {
                if(outsideRange(buffer, dx, dy))
                    continue;
                if(buffer[dx][dy] == false)
                    count++;
            }
        return count;
    }

    private boolean outsideRange(boolean[][] buffer, int x, int y)
    {
        return x < 0 || y < 0 || x >= buffer.length || y >= buffer[x].length;
    }

    private void initCells(boolean[][] buffer, Dice dice)
    {
        for(int x = 1; x < buffer.length - 1; x++)
            for(int y = 1; y < buffer[x].length - 1; y++)
            {
                if(dice.nextChance(tileChance))
                    buffer[x][y] = true;
                else
                    buffer[x][y] = false;
            }
        for(int x = 0; x < buffer.length; x++)
        {
            buffer[x][0] = false;
            buffer[x][buffer[x].length - 1] = false;
        }
        for(int y = 1; y < buffer[0].length - 1; y++)
        {
            buffer[0][y] = false;
            buffer[buffer.length - 1][y] = false;
        }

    }

    private void applyBuffer(World world, boolean[][] buffer)
    {
        for(int x = 0; x < world.width(); x++)
            for(int y = 0; y < world.height(); y++)
            {
                if(buffer[x][y])
                    world.setTile(openFace, true, x, y);
                else
                    world.setTile(wallFace, false, x, y);
            }
    }
}
