package com.comphenix.protocol.injector;

import com.comphenix.protocol.events.ListenerPriority;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

/**
 * Represents a listener with a priority.
 * 
 * @author Kristian
 */
public class PrioritizedListener<TListener> implements Comparable<PrioritizedListener<TListener>> {

	private TListener listener;
	private ListenerPriority priority;
	
	public PrioritizedListener(TListener listener, ListenerPriority priority) {
		this.listener = listener;
		this.priority = priority;
	}

	@Override
	public int compareTo(PrioritizedListener<TListener> other) {
		// This ensures that lower priority listeners are executed first
		return Ints.compare(
			this.getPriority().getSlot(),
			other.getPriority().getSlot());
	}
	
	// Note that this equals() method is NOT consistent with compareTo(). 
	// But, it's a private class so who cares.
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		// We only care about the listener - priority itself should not make a difference
	    if(obj instanceof PrioritizedListener){
	        final PrioritizedListener<TListener> other = (PrioritizedListener<TListener>) obj;
	        return Objects.equal(listener, other.listener);
	    } else {
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(listener);
	}
	
	/**
	 * Retrieve the underlying listener.
	 * @return Underlying listener.
	 */
	public TListener getListener() {
		return listener;
	}

	/**
	 * Retrieve the priority of this listener.
	 * @return Listener priority.
	 */
	public ListenerPriority getPriority() {
		return priority;
	}
}
