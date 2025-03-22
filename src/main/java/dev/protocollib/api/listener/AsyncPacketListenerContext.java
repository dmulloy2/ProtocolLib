package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

/**
 * Representing the context of an asynchronous packet listener.
 */
public interface AsyncPacketListenerContext extends SyncPacketListenerContext {

	/**
	 * Singles the listener is done with processing the packet.
	 */
	void resumeProcessing();

	/**
	 * Singles the listener is done with processing the packet and finished
	 * with an exception.
	 * 
	 * @param throwable the processing exception
	 */
	void resumeProcessingWithException(@NotNull Throwable throwable);

}
