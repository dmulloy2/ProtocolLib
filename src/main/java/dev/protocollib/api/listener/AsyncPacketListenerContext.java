package dev.protocollib.api.listener;

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
	void resumeProcessingWithException(Throwable throwable);

}
