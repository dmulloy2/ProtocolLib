package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

/**
 * Context of a mutable asynchronous packet listener.
 */
public interface MutableAsyncPacketListenerContext extends MutableSyncPacketListenerContext {

	/**
	 * Singles the listener is done with processing the packet. Handing
	 * it over to the next asynchronous packet listener.
	 */
	void resumeProcessing();

	/**
	 * Singles the listener is done with processing the packet and finished
	 * with an exception. Handing it over to the next asynchronous packet
	 * listener.
	 * 
	 * @param throwable the processing exception
	 */
	void resumeProcessingWithException(@NotNull Throwable throwable);

}
