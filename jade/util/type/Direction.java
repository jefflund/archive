package jade.util.type;

/**
 * Represents a compass direction
 */
public enum Direction
{
	N(0, -1), NW(-1, -1), W(-1, 0), SW(-1, 1), S(0, 1), SE(1, 1), E(1, 0),
	NE(1, -1), O(0, 0);

	public final int dx;
	public final int dy;

	private Direction(int dx, int dy)
	{
		this.dx = dx;
		this.dy = dy;
	}

	/**
	 * Returns the Direction opposite this one on the compass.
	 */
	public Direction opposite()
	{
		switch(this)
		{
		case N:
			return S;
		case NW:
			return SE;
		case W:
			return E;
		case SW:
			return NE;
		case S:
			return N;
		case SE:
			return NW;
		case E:
			return W;
		case NE:
			return SW;
		default:
			return O;
		}
	}
}
