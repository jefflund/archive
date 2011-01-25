package jade.gen.maps;

import jade.core.World;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Dice;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Uses cellular automata to generate cavelike maps. Maps are always guaranteed
 * to be complete connected, either by regerenerating the whole map (generally
 * for small World sizes), or by filling in the small unconnected parts
 * (generally on large maps). Note that on extreamly small maps, the algorithm
 * may fail. However, the algorithm will simply quit after a large number of
 * attempts.
 */
public class Cellular extends MapGen
{
    private static final int MAX_ATTEMPTS = 100;

    private int wallChance;
    private int minCount1;
    private int maxCount2;
    private ColoredChar floorTile;
    private ColoredChar wallTile;

    /**
     * Initializes the Cellular with default parameters.
     */
    public Cellular()
    {
        this(new ColoredChar('.'), new ColoredChar('#'));
    }

    /**
     * Initializes the cellular with default parameters, but custom tiles.
     * @param floorTile the face of the floor tiles
     * @param wallTile the face of the wall tiles
     */
    public Cellular(ColoredChar floorTile, ColoredChar wallTile)
    {
        this(40, 5, 3, floorTile, wallTile);
    }

    /**
     * Initializes the Cellular with custom parameters. Be careful with the
     * values choosen, as many configurations may lead to impossible results,
     * which means the generation will fail and quit after too many attempts.
     * @param wallChance the chance (in 100) of starting as a wall, and the
     * minimum amount of wall tiles the map must end with. On very small maps,
     * this value is often what will make map generation fail.
     * @param minCount1 the number of walls at distance 1 needed to make a wall
     * in the cellular automata
     * @param maxCount2 if minCount1 fails, but at distance 2 there are no more
     * than maxCount2 walls, a wall will be generated anyway.
     * @param floorTile the face of the floor tiles
     * @param wallTile the face of the wall tiles
     */
    public Cellular(int wallChance, int minCount1, int maxCount2,
            ColoredChar floorTile, ColoredChar wallTile)
    {
        this.wallChance = wallChance;
        this.minCount1 = minCount1;
        this.maxCount2 = maxCount2;
        this.floorTile = floorTile;
        this.wallTile = wallTile;
    }

    @Override
    protected void genStep(World world, Dice dice)
    {
        boolean[][] temp = new boolean[world.width()][world.height()];
        int attempts = 0;
        do
        {
            boolean[][] buffer = init(world, dice);
            for(int i = 0; i < 5; i++)
                apply45(buffer, temp, dice);
            finish(world, buffer);
        }
        while(!connected(world, dice) && attempts < MAX_ATTEMPTS);
    }

    private boolean connected(World world, Dice dice)
    {
        if(!openExists(world))
            return false;
        Set<Coord> filled = floodFilled(world, dice);
        int fillPercent = filled.size() * 100 / world.width() / world.height();
        if(fillPercent > wallChance)
        {
            deleteExtra(world, filled);
            return true;
        }
        return false;
    }

    private void deleteExtra(World world, Set<Coord> filled)
    {
        for(int x = 1; x < world.width() - 1; x++)
            for(int y = 1; y < world.height() - 1; y++)
            {
                if(!filled.contains(new Coord(x, y)))
                    world.setTile(x, y, wallTile, false);
            }
    }

    private Set<Coord> floodFilled(World world, Dice dice)
    {
        Stack<Coord> stack = new Stack<Coord>();
        stack.push(world.getOpenTile(dice));
        Set<Coord> filled = new HashSet<Coord>();
        while(!stack.isEmpty())
        {
            Coord curr = stack.pop();
            if(!filled.contains(curr) && world.passable(curr))
            {
                filled.add(curr);
                stack.push(curr.translated(1, 0));
                stack.push(curr.translated(-1, 0));
                stack.push(curr.translated(0, 1));
                stack.push(curr.translated(0, -1));
                stack.push(curr.translated(1, 1));
                stack.push(curr.translated(1, -1));
                stack.push(curr.translated(-1, 1));
                stack.push(curr.translated(-1, -1));
            }
        }
        return filled;
    }

    private boolean openExists(World world)
    {
        for(int x = 1; x < world.width() - 1; x++)
            for(int y = 1; y < world.height() - 1; y++)
                if(world.passable(x, y))
                    return true;
        return false;
    }

    private void apply45(boolean[][] buffer, boolean[][] temp, Dice dice)
    {
        for(int x = 1; x < buffer.length - 1; x++)
            for(int y = 1; y < buffer[0].length - 1; y++)
            {
                boolean minCond = wallcount(buffer, x, y, 1) >= minCount1;
                boolean maxCond = wallcount(buffer, x, y, 2) <= maxCount2;
                temp[x][y] = !minCond && !maxCond;
            }
        for(int x = 1; x < buffer.length - 1; x++)
            for(int y = 1; y < buffer[0].length - 1; y++)
                buffer[x][y] = temp[x][y];
    }

    private int wallcount(boolean[][] buffer, int x, int y, int r)
    {
        int count = 0;
        for(int dx = x - r; dx <= x + r; dx++)
            for(int dy = y - r; dy <= y + r; dy++)
            {
                if(outsideBounds(buffer, dx, dy))
                    continue;
                if(!buffer[dx][dy])
                    count++;
            }
        return count;
    }

    private boolean outsideBounds(boolean[][] buffer, int x, int y)
    {
        return x < 0 || y < 0 || x >= buffer.length || y >= buffer[0].length;
    }

    private void finish(World world, boolean[][] buffer)
    {
        for(int x = 0; x < world.width(); x++)
            for(int y = 0; y < world.height(); y++)
            {
                if(buffer[x][y])
                    world.setTile(x, y, floorTile, true);
                else
                    world.setTile(x, y, wallTile, false);
            }
    }

    private boolean[][] init(World world, Dice dice)
    {
        boolean[][] buffer = new boolean[world.width()][world.height()];
        for(int x = 1; x < world.width() - 1; x++)
            for(int y = 1; y < world.height() - 1; y++)
            {
                if(dice.chance(wallChance))
                    buffer[x][y] = false;
                else
                    buffer[x][y] = true;
            }
        for(int x = 1; x < world.width() - 1; x++)
        {
            buffer[x][0] = false;
            buffer[x][world.height() - 1] = false;
        }
        for(int y = 1; y < world.height() - 1; y++)
        {
            buffer[0][y] = false;
            buffer[world.width() - 1][y] = false;
        }
        return buffer;
    }
}
