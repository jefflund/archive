package rogue.creature;

import jade.util.Dice;
import jade.util.datatype.ColoredChar;

public class Monster extends Creature
{
    public Monster(ColoredChar face)
    {
        super(face);
    }

    @Override
    public void act()
    {
        move(Dice.global.nextDirection());
    }
}
