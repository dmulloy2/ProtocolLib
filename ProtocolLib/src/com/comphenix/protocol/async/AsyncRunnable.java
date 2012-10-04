package com.comphenix.protocol.async;

/**
 * A runnable representing a asynchronous event listener.
 * 
 * @author Kristian
 */
public interface AsyncRunnable extends Runnable {
	
	/**
	 * Retrieve a unique worker ID.
	 * @return Unique worker ID.
	 */
	public int getID();
	
	/**
	 * Stop the given runnable.
	 * <p>
	 * This may not occur right away.
	 * @return TRUE if the thread was stopped, FALSE if it was already stopped.
	 */
	public boolean stop() throws InterruptedException;
	
	/**
	 * Determine if we're running or not.
	 * @return TRUE if we're running, FALSE otherwise.
	 */
	public boolean isRunning();

	/**
	 * Determine if this runnable has already run its course.
	 * @return TRUE if it has been stopped, FALSE otherwise.
	 */
	boolean isFinished();
}
