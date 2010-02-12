package jade.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents an ini config file
 */
public class Config
{
	public static void main(String[] args) throws FileNotFoundException,
			IOException
	{
		Config config = new Config(new FileReader("config.ini"));
		for(String section : config.getSections())
		{
			System.out.println("[" + section + "]");
			for(String parameter : config.getParameters(section))
				System.out.println(parameter + "="
						+ config.getProperty(section, parameter));
		}
	}

	private Map<String, Map<String, String>> sections;

	/**
	 * Constructs a new empty config
	 */
	public Config()
	{
		sections = new HashMap<String, Map<String, String>>();
	}

	/**
	 * Constructs a new config with the values specified by reader. See load for
	 * the format of the input.
	 */
	public Config(Reader reader) throws IOException
	{
		this();
		load(reader);
	}

	/**
	 * Returns the value of the parameter under the given section, or null if
	 * there is none.
	 */
	public String getProperty(String section, String parameter)
	{
		Map<String, String> parameters = sections.get(section);
		if(parameters != null)
			return parameters.get(parameter);
		else
			return null;
	}

	/**
	 * Returns the value of the parameter under the given section, or defValue if
	 * there is none.
	 */
	public String getProperty(String section, String parameter, String defValue)
	{
		String value = getProperty(section, parameter);
		return value != null ? value : defValue;
	}

	/**
	 * Sets the value of the parameter under the given section. Overwrites any
	 * previous value.
	 */
	public void setProperty(String section, String parameter, String value)
	{
		if(!sections.containsKey(section))
			sections.put(section, new HashMap<String, String>());
		Map<String, String> parameters = sections.get(section);
		parameters.put(parameter, value);
	}

	/**
	 * Loads values into the config from a basic ini file. The general format is
	 * as follows:<br>
	 * #comment<br>
	 * [section]<br>
	 * parameter=value<br>
	 * Each parameter is part of a section. If there is no specified section, it
	 * will belong to the section null. Each parameter will be everything before
	 * the first '=' with white space trimmed. Each value will be everything after
	 * the first '=' with the white space trimmed.
	 */
	public void load(Reader reader) throws IOException
	{
		BufferedReader in = new BufferedReader(reader);
		String section = null;
		while(in.ready())
		{
			String line = in.readLine().trim();
			//handle comments - #comment
			int comment = line.indexOf('#');
			if(comment >= 0)
				line = line.substring(0, comment);
			if(line.length() == 0)
				continue;
			//section
			if(line.startsWith("[") && line.endsWith("]"))
				section = line.substring(1, line.length() - 1);
			else
			//parameter
			{
				int equals = line.indexOf('=');
				if(equals < 0)
					throw new IOException("Malformed line: " + line);
				String parameter = line.substring(0, equals);
				String value = line.substring(equals + 1);
				if(parameter.length() == 0 || value.length() == 0)
					throw new IOException("Malformed line: " + line);
				setProperty(section, parameter.trim(), value.trim());
			}
		}
		in.close();
	}

	/**
	 * Returns all the sections currently in the config
	 */
	public Set<String> getSections()
	{
		return sections.keySet();
	}

	/**
	 * Returns all the parameters currently in the given section
	 */
	public Set<String> getParameters(String section)
	{
		return sections.get(section).keySet();
	}
}
