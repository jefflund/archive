package jade.gen.maps;

import jade.core.World;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Dice;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Uses random recursive back tracing to generate a perfect maze.
 */
public class Maze extends MapGen
{
    private ColoredChar floorTile;
    private ColoredChar wallTile;

    /**
     * Initializes Maze with default parameters.
     */
    public Maze()
    {
        floorTile = new ColoredChar('.');
        wallTile = new ColoredChar('#');
    }

    @Override
    protected void genStep(World world, Dice dice)
    {
        Set<Coord> cells = init(world);
        Stack<Coord> stack = new Stack<Coord>();
        stack.push(world.getOpenTile(dice));
        cells.remove(stack.peek());
        while(!stack.isEmpty())
        {
            Coord curr = stack.peek();
            Coord next = getNext(curr, cells, dice);// get random uncovered dir
            if(next == null)// no where to go, just backtrace
                stack.pop();
            else
            {
                // dig in random uncovered direction
                Coord dig = curr.translated(curr.directionTo(next));
                world.setTile(dig, floorTile, true);
                stack.push(next);// continue from where we dug
            }
        }
    }

    private Coord getNext(Coord curr, Set<Coord> cells, Dice dice)
    {
        List<Coord> possible = new ArrayList<Coord>(4);
        tryAddNext(possible, curr.translated(0, 2), cells);
        tryAddNext(possible, curr.translated(0, -2), cells);
        tryAddNext(possible, curr.translated(2, 0), cells);
        tryAddNext(possible, curr.translated(-2, 0), cells);
        if(possible.isEmpty())
            return null;
        Coord next = dice.choose(possible);
        cells.remove(next);
        return next;
    }

    private void tryAddNext(List<Coord> possible, Coord pos, Set<Coord> cells)
    {
        if(cells.contains(pos))
            possible.add(pos);
    }

    private Set<Coord> init(World world)
    {
        Set<Coord> cells = new HashSet<Coord>();
        for(int x = 0; x < world.width(); x++)
            for(int y = 0; y < world.height(); y++)
            {
                // every other square is an open to start
                if(x % 2 == 1 && y % 2 == 1 && x < world.width() - 1
                        && y < world.height() - 1)
                {
                    cells.add(new Coord(x, y));
                    world.setTile(x, y, floorTile, true);
                }
                else
                    world.setTile(x, y, wallTile, false);
            }
        return cells;
    }
}
