package jade.util;

public abstract class Tools
{
	public static Coord keyToDir(char key, boolean vi, boolean numeric)
	{
		if(vi && Character.isLetter(key))
		{
			switch(key)
			{
			case 'l':
				return new Coord(1, 0);
			case 'h':
				return new Coord(-1, 0);
			case 'k':
				return new Coord(0, -1);
			case 'j':
				return new Coord(0, 1);
			case 'b':
				return new Coord(-1, 1);
			case 'n':
				return new Coord(1, 1);
			case 'u':
				return new Coord(1, -1);
			case 'y':
				return new Coord(-1, -1);
			case '.':
				return new Coord(0, 0);
			}
		}
		else if(numeric && Character.isDigit(key))
		{
			switch(key)
			{
			case '6':
				return new Coord(1, 0);
			case '4':
				return new Coord(-1, 0);
			case '8':
				return new Coord(0, -1);
			case '2':
				return new Coord(0, 1);
			case '1':
				return new Coord(-1, 1);
			case '3':
				return new Coord(1, 1);
			case '9':
				return new Coord(1, -1);
			case '7':
				return new Coord(-1, -1);
			case '5':
				return new Coord(0, 0);
			}
		}
		return null;
	}

	public static boolean strHasSuffix(String str, String suffix)
	{
		assert (str.length() >= suffix.length());
		return str.substring(str.length() - suffix.length()).equals(suffix);
	}

	public static String strEnsureSuffix(String str, String suffix)
	{
		if(strHasSuffix(str, suffix))
			return str;
		else
			return str + suffix;
	}
}
