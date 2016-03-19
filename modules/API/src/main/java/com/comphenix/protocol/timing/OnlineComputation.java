package com.comphenix.protocol.timing;

/**
 * Represents an online computation.
 * @author Kristian
 */
public abstract class OnlineComputation {
    /**
     * Retrieve the number of observations.
     * @return Number of observations.
     */
	public abstract int getCount();

	/**
     * Observe a value.
     * @param value - the observed value.
     */
	public abstract void observe(double value);
	
	/**
	 * Construct a copy of the current online computation.
	 * @return The new copy.
	 */
	public abstract OnlineComputation copy();
	
	/**
	 * Retrieve a wrapper for another online computation that is synchronized.
	 * @param computation - the computation.
	 * @return The synchronized wrapper.
	 */
	public static OnlineComputation synchronizedComputation(final OnlineComputation computation) {
		return new OnlineComputation() {
			@Override
			public synchronized void observe(double value) {
				computation.observe(value);
			}
			
			@Override
			public synchronized int getCount() {
				return computation.getCount();
			}
			
			@Override
			public synchronized OnlineComputation copy() {
				return computation.copy();
			}
		};
	}
}