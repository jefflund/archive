package jade.core;

import jade.util.Tools;

public class Messenger
{
	private String messages;

	public Messenger()
	{
		clearMessages();
	}
	
	public void clearMessages()
	{
		messages = "";
	}

	public void appendMessage(String message)
	{
		messages += Tools.strEnsureSuffix(message, ". ");
	}
	
	public void retrieveMessages(Messenger messenger)
	{
		messages += messenger.getMessages();
	}

	public String getMessages()
	{
		String result = messages;
		clearMessages();
		return result;
	}
}
