package com.comphenix.protocol.async;

/**
 * A runnable representing a asynchronous event listener.
 * 
 * @author Kristian
 */
public interface AsyncRunnable extends Runnable {
	
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
}
