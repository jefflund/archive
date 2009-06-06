package jade.util;

public abstract class Tools
{
	public static Coord keyToDir(char key, boolean vi, boolean numeric)
	{
		if(vi && (Character.isLetter(key) || key == '.'))
			return viToDir(key);
		else if(numeric && Character.isDigit(key))
			return numToDir(key);
		else
			return null;
	}

	private static Coord numToDir(char key)
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
		default:
			return null;
		}
	}

	private static Coord viToDir(char key)
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
		default:
			return null;
		}
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
		return str + suffix;
	}

	public static String strEnsureLength(String str, int length)
	{
		while(str.length() < length)
			str += " ";
		return str;
	}
	
	public static char intToAlpha(int i)
	{
		return (char)(i + 'a');
	}
	
	public static int alphaToInt(char ch)
	{
		return (int)(ch - 'a');
	}
}
