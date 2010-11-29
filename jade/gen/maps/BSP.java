package jade.gen.maps;

import jade.core.World;
import jade.util.Dice;
import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Uses a binary space partitioning algorithm to generate rectangular rooms
 * connected by corridors. The algorithm simply takes the world, and repeatedly
 * divides the space into two randomly sized partitions until the whole world
 * has beened divided into small chunks. Each division is stored in a tree
 * structure. Rooms are then randomly placed into the leaves (ie the last and
 * smallest partitions). Each sibling pair in the tree is then connected in a
 * bottom up fashion. A few random cycles are then added to remove fix the
 * rather boring tree structure of the resulting map.
 */
public class BSP extends MapGen
{
    private ColoredChar wallFace = new ColoredChar('#', Color.white);
    private ColoredChar floorFace = new ColoredChar('.', Color.white);
    private int minSize = 3;

    @Override
    public void genStep(World world, Dice dice)
    {
        for(int x = 0; x < world.width(); x++)
            for(int y = 0; y < world.height(); y++)
                world.setTile(wallFace, false, x, y);
        BSPNode head = new BSPNode(world);
        head.divide(dice);
        head.makeRooms(world, dice);
        head.connect(world, dice);
        head.addCycles(world, dice);
    }

    private class BSPNode
    {
        private final int x1;
        private final int y1;
        private final int x2;
        private final int y2;
        private int rx1;
        private int ry1;
        private int rx2;
        private int ry2;
        private BSPNode left;
        private BSPNode right;
        private BSPNode parent;
        private boolean connected;
        private boolean readyConnect;

        public BSPNode(World world)
        {
            connected = true;
            readyConnect = true;
            x1 = 0;
            y1 = 0;
            x2 = world.width() - 1;
            y2 = world.height() - 1;
        }

        private BSPNode(BSPNode parent, int div, boolean vert, boolean left)
        {
            this.parent = parent;
            connected = false;
            readyConnect = true;
            x1 = parent.x1 + (vert && !left ? div + 1 : 0);
            y1 = parent.y1 + (!vert && !left ? div + 1 : 0);
            x2 = vert && left ? parent.x1 + div : parent.x2;
            y2 = !vert && left ? parent.y1 + div : parent.y2;
        }

        public void divide(Dice dice)
        {
            while(divideAux(dice))
                ;
        }

        private boolean divideAux(Dice dice)
        {
            if(!isLeaf())
            {
                final boolean divleft = left.divideAux(dice);
                final boolean divRight = right.divideAux(dice);
                return divleft || divRight;
            }
            boolean vert = dice.nextChance();
            int min = minSize + 4;
            if(divTooSmall(vert, min))
                vert = !vert;
            if(divTooSmall(vert, min))
                return false;
            final int div = dice.nextInt(min, (vert ? x2 - x1 : y2 - y1) - min);
            left = new BSPNode(this, div, vert, true);
            right = new BSPNode(this, div, vert, false);
            readyConnect = false;
            return true;
        }

        private boolean divTooSmall(boolean vert, int min)
        {
            min *= 2;
            return vert ? (x2 - x1) < min : (y2 - y1) < min;
        }

        private boolean isLeaf()
        {
            return left == null && right == null;
        }

        public void makeRooms(World world, Dice dice)
        {
            if(isLeaf())
            {
                rx1 = dice.nextInt(x1 + 1, x2 - 1 - minSize);
                rx2 = dice.nextInt(rx1 + minSize, x2 - 1);
                ry1 = dice.nextInt(y1 + 1, y2 - 1 - minSize);
                ry2 = dice.nextInt(ry1 + minSize, y2 - 1);
                for(int x = rx1; x <= rx2; x++)
                    for(int y = ry1; y <= ry2; y++)
                        world.setTile(floorFace, true, x, y);
            }
            else
            {
                left.makeRooms(world, dice);
                right.makeRooms(world, dice);
            }
        }

        public void connect(World world, Dice dice)
        {
            if(!isLeaf())
            {
                left.connect(world, dice);
                right.connect(world, dice);
            }
            readyConnect = true;
            connectToSibling(world, dice);
        }

        private void connectToSibling(World world, Dice dice)
        {
            if(connected)
                return;
            BSPNode sibling = this == parent.left ? parent.right : parent.left;
            if(!sibling.readyConnect)
                return;
            connectTo(world, sibling, dice);
            connected = true;
            sibling.connected = true;
        }

        private void connectTo(World world, BSPNode other, Dice dice)
        {
            Coord start = getOpenTile(world, dice);
            Coord end = other.getOpenTile(world, dice);
            List<Coord> corridor = new LinkedList<Coord>();
            Coord curr = new Coord(start);
            while(!curr.equals(end))
            {
                corridor.add(new Coord(curr));
                boolean digging = !world.passable(curr);
                if(curr.x() == end.x())
                    curr.translate(0, curr.y() < end.y() ? 1 : -1);
                else
                    curr.translate(curr.x() < end.x() ? 1 : -1, 0);
                if(digging && world.passable(curr))
                {
                    if(other.inBounds(curr))
                        break;
                    corridor.clear();
                }
            }
            for(Coord coord : corridor)
                world.setTile(floorFace, true, coord);
        }

        private Coord getOpenTile(World world, Dice dice)
        {
            return world.getOpenTile(dice, x1, y1, x2, y2);
        }

        private boolean inBounds(Coord coord)
        {
            return coord.x() < x2 && coord.x() > x1 && coord.y() < y2
                    && coord.y() > y1;
        }

        public void addCycles(World world, Dice dice)
        {
            List<BSPNode> leaves = new ArrayList<BSPNode>(getLeaves());
            if(leaves.size() < 5)
                return;
            for(int i = 0; i < dice.nextInt(leaves.size() / 2, leaves.size()); i++)
            {
                BSPNode leaf1 = leaves.remove(leaves.size() - 1);
                BSPNode leaf2 = leaves.get(dice.nextInt(leaves.size()));
                leaf1.connectTo(world, leaf2, dice);
            }
        }

        private Set<BSPNode> getLeaves()
        {
            Set<BSPNode> leaves = new HashSet<BSPNode>();
            getLeavesAux(leaves);
            return leaves;
        }

        private void getLeavesAux(Set<BSPNode> leaves)
        {
            if(isLeaf())
                leaves.add(this);
            else
            {
                if(left != null)
                    left.getLeavesAux(leaves);
                if(right != null)
                    right.getLeavesAux(leaves);
            }
        }
    }
}
