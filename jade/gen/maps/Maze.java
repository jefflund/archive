package jade.gen.maps;

import jade.core.World;
import jade.util.Dice;
import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Generates perfect mazes using the recursive back tracking algorithm. This
 * algorithm generates perfect mazes (no loops, and all areas are accessible).
 * It will tend to have long, twisty paths through the maze. It also has a
 * fairly good run time as far as perfect maze generation goes.
 */
public class Maze extends MapGen
{
    private ColoredChar floorFace = new ColoredChar('.', Color.white);
    private ColoredChar wallFace = new ColoredChar('#', Color.white);

    @Override
    public void genStep(World world, Dice dice)
    {
        Set<Coord> cells = initMaze(world);
        Stack<Coord> stack = new Stack<Coord>();
        stack.push(world.getOpenTile(dice));
        cells.remove(stack.peek());
        while(!stack.isEmpty())
        {
            Coord curr = stack.peek();
            Coord next = getNext(curr, cells, dice);
            if(next == null)
                stack.pop();
            else
            {
                Coord dig = curr.getTranslated(curr.directionTo(next));
                world.setTile(floorFace, true, dig);
                stack.push(next);
            }
        }
    }

    private Coord getNext(Coord curr, Set<Coord> cells, Dice dice)
    {
        List<Coord> possible = new ArrayList<Coord>();
        tryAddNext(curr.getTranslated(2, 0), cells, possible);
        tryAddNext(curr.getTranslated(-2, 0), cells, possible);
        tryAddNext(curr.getTranslated(0, 2), cells, possible);
        tryAddNext(curr.getTranslated(0, -2), cells, possible);
        if(possible.isEmpty())
            return null;
        else
        {
            Coord next = dice.nextItem(possible);
            cells.remove(next);
            return next;
        }
    }

    private void tryAddNext(Coord next, Set<Coord> cells, List<Coord> possible)
    {
        if(cells.contains(next))
            possible.add(next);
    }

    private Set<Coord> initMaze(World world)
    {
        Set<Coord> cells = new HashSet<Coord>();
        for(int x = 0; x < world.width(); x++)
            for(int y = 0; y < world.height(); y++)
            {
                if((x + 1) % 2 == 0 && (y + 1) % 2 == 0
                        && x < world.width() - 1 && y < world.height() - 1)
                {
                    world.setTile(floorFace, true, x, y);
                    cells.add(new Coord(x, y));
                }
                else
                    world.setTile(wallFace, false, x, y);
            }
        return cells;
    }
}
