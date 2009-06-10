package jade.util;

/**
 * Tools contains lots of methods that are generally useful, but do not fit in
 * any one jade class.
 */
public final class Tools
{
	private Tools()
	{
		throw new IllegalAccessError("Tools cannot be instantiated");
	}

	/**
	 * This method takes a keypress and turns it into a direction. The x and y
	 * coordinates of the returned direction will be between -1 and 1. This coord
	 * can then be used to translate other coord in the direction of the keypress.
	 * 
	 * @param key the character of the keypress
	 * @param vi if true, then keyboard controls for moving the cursor in vim will
	 * be allowed.
	 * @param numeric if true, then keyboard controls for moving the cursor on a
	 * numpad will be allowed
	 * @return the direction of the key press, or null if the key was
	 * non-direction based on the choosen keyset(s)
	 */
	public static Coord keyToDir(char key, boolean vi, boolean numeric)
	{
		assert (vi || numeric);
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

	/**
	 * Returns true if the string has the specified suffix. The check is case
	 * sensitive.
	 * 
	 * @param str the string to be tested
	 * @param suffix the suffix to be tested
	 * @return true if the string has the specified suffix
	 */
	public static boolean strHasSuffix(String str, String suffix)
	{
		assert (str.length() >= suffix.length());
		return str.substring(str.length() - suffix.length()).equals(suffix);
	}

	/**
	 * Returns a string that has the specified suffix. If the input string already
	 * has the suffix, then it will be returned unchanged, otherwise, return value
	 * will be the string concatonated with the suffix.
	 * 
	 * @param str the string in question
	 * @param suffix the suffix to tested
	 * @return a string that has the specified suffix
	 */
	public static String strEnsureSuffix(String str, String suffix)
	{
		if(strHasSuffix(str, suffix))
			return str;
		return str + suffix;
	}

	/**
	 * Returns a string that has at least the specified length. If the input
	 * string has a length greater or equal to the given length, then it will be
	 * returned unchanged. Otherwise, the string will be returned after
	 * concationation with enough spaces to meet the length requirement.
	 * 
	 * @param str the string in question
	 * @param length the minimum length of the return value
	 * @return a string that is at least the specified length
	 */
	public static String strEnsureLength(String str, int length)
	{
		while(str.length() < length)
			str += " ";
		return str;
	}

	/**
	 * Translates an integer to a character where 0 maps to 'a', 1 to 'b', 2 to
	 * 'c' and so on.
	 * 
	 * @param i the integer to be converted
	 * @return the character value of i
	 */
	public static char intToAlpha(int i)
	{
		return (char)(i + 'a');
	}

	/**
	 * Translates a character to an integer where 'a' maps to 0, 'b' to 1, 'c' to
	 * 2 and so on.
	 * 
	 * @param ch the character to be converted
	 * @return the integer value of ch
	 */
	public static int alphaToInt(char ch)
	{
		return (int)(ch - 'a');
	}
}
