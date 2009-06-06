package jade.util;

public class Coord implements Comparable<Coord>
{
	private int x;
	private int y;

	public Coord()
	{
		move(0, 0);
	}

	public Coord(int x, int y)
	{
		move(x, y);
	}

	public Coord(Coord coord)
	{
		move(coord);
	}

	public void move(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public void move(Coord coord)
	{
		move(coord.x, coord.y);
	}

	public void translate(int dx, int dy)
	{
		x += dx;
		y += dy;
	}

	public void translate(Coord coord)
	{
		translate(coord.x, coord.y);
	}

	public int x()
	{
		return x;
	}

	public int y()
	{
		return y;
	}

	public int compareTo(Coord other)
	{
		if(x == other.x)
			return y - other.y;
		return x - other.x;
	}

	public boolean equals(Coord other)
	{
		return x() == other.x() && y() == other.y();
	}

	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
}
