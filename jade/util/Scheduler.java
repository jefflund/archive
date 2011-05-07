package jade.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Scheduler<E>
{
    private EventKey<E> head;

    public void schedule(int delta, E event)
    {
        if(head == null)
            head = new EventKey<E>(delta, null, event);
        else
            head = head.insert(delta, event);
    }

    public Set<E> next()
    {
        if(head == null)
            return Collections.emptySet();
        else
        {
            Set<E> events = head.events;
            head = head.next;
            return events;
        }
    }

    public void filter(Filter<E> filter)
    {
        EventKey<E> curr = head;
        while(curr != null)
        {
            Set<E> filtered = new HashSet<E>();
            for(E event : curr.events)
                if(filter.predicate(event))
                    filtered.add(event);
            curr.events.removeAll(filtered);
            curr = curr.next;
        }
    }

    public interface Filter<T>
    {
        public boolean predicate(T instance);
    }

    private class EventKey<T>
    {
        private int delta;
        private EventKey<T> next;
        private Set<T> events;

        private EventKey(int delta, EventKey<T> next, T event)
        {
            this.delta = delta;
            this.next = next;
            events = new HashSet<T>();
            events.add(event);
        }

        public EventKey<T> insert(int delta, T event)
        {
            if(delta == this.delta)
            {
                events.add(event);
                return this;
            }
            else if(delta > this.delta)
            {
                delta -= this.delta;
                if(next == null)
                    next = new EventKey<T>(delta, null, event);
                else
                    next = next.insert(delta, event);
                return this;
            }
            else
            {
                EventKey<T> key = new EventKey<T>(delta, this, event);
                this.delta -= delta;
                return key;
            }
        }
    }
}
