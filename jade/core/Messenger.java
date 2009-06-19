package jade.core;

import java.io.Serializable;
import jade.util.Tools;

/**
 * This class respresents an object that can remember String messages, and
 * retrieve messages from other messengers. Both jade World and Actor extend
 * this class with the motivation that the world could then retrieve all the
 * messages of its actors and store them in one place for easy access.
 */
public class Messenger implements Serializable
{
	private String messages;

	/**
	 * Constructs a new messenger
	 */
	public Messenger()
	{
		clearMessages();
	}

	/**
	 * Clears the messenger's messages
	 */
	public void clearMessages()
	{
		messages = "";
	}

	/**
	 * Adds a message for the messenger to remember. If the message does not end
	 * with ". " it will be appended as well.
	 * 
	 * @param message the message to be stored
	 */
	public void appendMessage(String message)
	{
		messages += Tools.strEnsureSuffix(message, ". ");
	}

	/**
	 * Retrieves and clears all messeges of the specified messenger and adds them
	 * to this messenger's messages.
	 * 
	 * @param messenger the messenger whos messages are to be retrieved
	 */
	public void retrieveMessages(Messenger messenger)
	{
		messages += messenger.getMessages();
	}

	/**
	 * Returns and clears this messengers messages.
	 * @return The messengers messegages.
	 */
	public String getMessages()
	{
		String result = messages;
		clearMessages();
		return result;
	}
}
