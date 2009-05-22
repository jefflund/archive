package jade.core;

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
		messages += message;
	}

	public String getMessages()
	{
		String result = messages;
		clearMessages();
		return result;
	}
}
