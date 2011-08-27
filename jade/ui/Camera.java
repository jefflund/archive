package jade.ui;

import jade.core.World;
import jade.util.datatype.Coordinate;
import java.util.Collection;

public interface Camera
{
    public int x();

    public int y();

    public World world();

    public Collection<Coordinate> getViewField();
}
