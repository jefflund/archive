package jade.core;

import jade.util.Tools;
import java.io.Serializable;

/**
 * Base class for both Actor and World. Allows all the messages to be propagated
 * to one central spot and given to the user.
 */
public class Messenger implements Serializable
{
	private String messages;

	/**
	 * Creates a new Messenger
	 */
	public Messenger()
	{
		clearMessages();
	}

	/**
	 * Clears any messages the Messenger has.
	 */
	public final void clearMessages()
	{
		messages = "";
	}

	/**
	 * Appends a new message to the previous messages. The message is ensured to
	 * end with ". "
	 */
	public final void appendMessage(String message)
	{
		if(!message.equals(""))
			messages += Tools.strEnsureSuffix(message, ". ");
	}

	/**
	 * Removes the messages from the given Messenger and appends them to this
	 * Messenger's messages.
	 */
	public final void retrieveMessages(Messenger messenger)
	{
		messages += messenger.getMessages();
	}

	/**
	 * Retrieves the messages from this Messenger, leaving the Messenger's
	 * messages blank.
	 */
	public final String getMessages()
	{
		String result = messages;
		clearMessages();
		return result;
	}
}