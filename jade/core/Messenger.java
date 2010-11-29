package jade.core;

import jade.util.Tools;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is the base class for both Actor and Level. This class allows for easy
 * message passing and aggregation so all game messeges are easily collected and
 * displayed to the player.
 */
public class Messenger implements Serializable
{
    private Set<Messege> messeges;

    /**
     * Creates a new Messenger with no messenges.
     */
    public Messenger()
    {
        messeges = new HashSet<Messege>();
    }

    /**
     * Appends a new messenge in the cache of this Messenger. This Messenger
     * will be considered the source of the messege. If the suffix ". ", is not
     * present, it will be appended to the message. However, empty strings will
     * be ignored.
     * @param messege the text of the messege.
     */
    public final void appendMessege(String messege)
    {
        appendMessege(messege, ". ");
    }

    /**
     * Appends a new messenge in the cache of this Messenger. This Messenger
     * will be considered the source of the messege. If the suffix is not
     * present, it will be appended to the message. However, empty strings will
     * be ignored.
     * @param messege the text of the messege
     * @param suffix the suffix of the messege
     */
    public void appendMessege(String messege, String suffix)
    {
        if(messege.length() > 0)
        {
            messege = Tools.ensureSuffix(messege, suffix);
            messeges.add(new Messege(this, messege));
        }
    }

    /**
     * Concatinates all the messeges currently cached by this messenger. The
     * messege cache is cleared in the process.
     * @return the text of all the messeges currently cached by the Messenger
     */
    public String getMesseges()
    {
        String text = "";
        for(Messege messege : messeges)
            text += messege.text;
        messeges.clear();
        return text;
    }

    /**
     * Appends the messeges of the given Messenger to the cache of this
     * Messenger. This clears the cache of the other Messenger. All sources are
     * preserved in the retreval of messeges.
     * @param messenger the messenger whos cache is to be retrieved
     */
    public void retrieveMesseges(Messenger messenger)
    {
        messeges.addAll(messenger.messeges);
        messenger.messeges.clear();
    }

    /**
     * Filters the cache of this messenger by source. Only the messeges in the
     * sources will be kept. All other messeges will be discarded.
     * @param sources the sources whose messeges should be kept.
     */
    public void filterSources(Collection<Messenger> sources)
    {
        Iterator<Messege> iter = messeges.iterator();
        while(iter.hasNext())
        {
            if(!sources.contains(iter.next().source))
                iter.remove();
        }
    }

    /**
     * Combines the behavior of filterSources and getMesseges. Returns the
     * messeges which were retrieved from messengers in the sources. All
     * messeges in the the cache of this Messenger will be cleared, reguardless
     * of whether the messege will be returned.
     * @param sources the sources whose messeges should be kept.
     */
    public final String getFilteredMesseges(Collection<Messenger> sources)
    {
        filterSources(sources);
        return getMesseges();
    }

    private class Messege
    {
        public final Messenger source;
        public final String text;

        public Messege(Messenger source, String text)
        {
            this.source = source;
            this.text = text;
        }
    }
}
