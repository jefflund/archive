package jade.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Allows for easy passing and aggregation of messages. A message is consists of
 * a Messenger source and a String content.
 */
public class Messenger
{
    private List<Message> messages;

    /**
     * Constructs a new Messenger with an empty message cache.
     */
    public Messenger()
    {
        messages = new ArrayList<Message>();
    }

    /**
     * Appends a message to the Messenger cache, with this Messenger as the
     * source of the message.
     * @param message the contents of the message
     */
    public void appendMessage(String message)
    {
        messages.add(new Message(this, message));
    }

    /**
     * Clears the messages in the Messenger cache.
     */
    public void clearMessages()
    {
        messages.clear();
    }

    /**
     * Returns the concatination of all messages in the messenger cache, then
     * clears the cache.
     * @return the concatination of all messages in the messenger cache
     */
    public String getMessages()
    {
        String result = "";
        for(Message message : messages)
            result += message.contents;
        clearMessages();
        return result;
    }

    /**
     * Returns the concatination of all messages in this Messenger cache,
     * provided the source of the message is in the Collection of sources. The
     * contents of the cache are cleared.
     * @param sources the Collection of sources of the messages that will be
     * included in the return value
     * @return the concatination of all messages in this Messenger cache,
     * provided the source of the message is in the Collection of sources
     */
    public String getMessages(Collection<Messenger> sources)
    {
        String result = "";
        for(Message message : messages)
            if(sources.contains(message.source))
                result += message.contents;
        messages.clear();
        return result;
    }

    /**
     * Adds the messages held by the given Messenger to the cache of this
     * Messenger, then clears the cache of the other Messenger. The source of
     * each message is preserved.
     * @param messenger the messenger from which to retrieve messages
     */
    public void retrieveMessages(Messenger messenger)
    {
        messages.addAll(messenger.messages);
        messenger.clearMessages();
    }

    private class Message
    {
        public final Messenger source;
        public final String contents;

        public Message(Messenger source, String contents)
        {
            this.source = source;
            this.contents = contents;
        }
    }
}
