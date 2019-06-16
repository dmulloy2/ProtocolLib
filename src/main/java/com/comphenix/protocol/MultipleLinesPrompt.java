package com.comphenix.protocol;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ExactMatchConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

/**
 * Represents a conversation prompt that accepts a list of lines.
 * 
 * @author Kristian
 */
class MultipleLinesPrompt extends StringPrompt {
	/**
	 * Represents a canceller that determines if the multiple lines prompt is finished.
	 * @author Kristian
	 */
	public static interface MultipleConversationCanceller extends ConversationCanceller {
		@Override
		public boolean cancelBasedOnInput(ConversationContext context, String currentLine);

		/**
		 * Determine if the current prompt is done based on the context, last
		 * line and collected lines.
		 * 
		 * @param context - current context.
		 * @param currentLine - current (last) line.
		 * @param lines - collected lines.
		 * @param lineCount - number of lines.
		 * @return TRUE if we are done, FALSE otherwise.
		 */
		public boolean cancelBasedOnInput(ConversationContext context, String currentLine, 
										  StringBuilder lines, int lineCount);
	}

	/**
	 * A wrapper class for turning a ConversationCanceller into a MultipleConversationCanceller.
	 * @author Kristian
	 */
	private static class MultipleWrapper implements MultipleConversationCanceller {
		private ConversationCanceller canceller;

		public MultipleWrapper(ConversationCanceller canceller) {
			this.canceller = canceller;
		}
		
		@Override
		public boolean cancelBasedOnInput(ConversationContext context, String currentLine) {
			return canceller.cancelBasedOnInput(context, currentLine);
		}
		
		@Override
		public boolean cancelBasedOnInput(ConversationContext context, String currentLine, 
										  StringBuilder lines, int lineCount) {
			return cancelBasedOnInput(context, currentLine);
		}

		@Override
		public void setConversation(Conversation conversation) {
			canceller.setConversation(conversation);
		}
		
		@Override
		public MultipleWrapper clone() {
			return new MultipleWrapper(canceller.clone());
		}
	}

	// Feels a bit like Android
	private static final String KEY = "multiple_lines_prompt";
	private static final String KEY_LAST = KEY + ".last_line";
	private static final String KEY_LINES = KEY + ".linecount";

	private final MultipleConversationCanceller endMarker;
	private final String initialPrompt;

	/**
	 * Retrieve and remove the current accumulated input.
	 * 
	 * @param context
	 *            - conversation context.
	 * @return The accumulated input, or NULL if not found.
	 */
	public String removeAccumulatedInput(ConversationContext context) {
		Object result = context.getSessionData(KEY);

		if (result instanceof StringBuilder) {
			context.setSessionData(KEY, null);
			context.setSessionData(KEY_LINES, null);
			return ((StringBuilder) result).toString();
		} else {
			return null;
		}
	}

	/**
	 * Construct a multiple lines input prompt with a specific end marker.
	 * <p>
	 * This is usually an empty string.
	 * 
	 * @param endMarker - the end marker.
	 */
	public MultipleLinesPrompt(String endMarker, String initialPrompt) {
		this(new ExactMatchConversationCanceller(endMarker), initialPrompt);
	}

	/**
	 * Construct a multiple lines input prompt with a specific end marker implementation.
	 * <p>
	 * Note: Use {@link #MultipleLinesPrompt(MultipleConversationCanceller, String)} if implementing a custom canceller.
	 * @param endMarker - the end marker.
	 * @param initialPrompt - the initial prompt text.
	 */
	public MultipleLinesPrompt(ConversationCanceller endMarker, String initialPrompt) {
		this.endMarker = new MultipleWrapper(endMarker);
		this.initialPrompt = initialPrompt;
	}
	
	/**
	 * Construct a multiple lines input prompt with a specific end marker implementation.
	 * @param endMarker - the end marker.
	 * @param initialPrompt - the initial prompt text.
	 */
	public MultipleLinesPrompt(MultipleConversationCanceller endMarker, String initialPrompt) {
		this.endMarker = endMarker;
		this.initialPrompt = initialPrompt;
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String in) {
		StringBuilder result = (StringBuilder) context.getSessionData(KEY);
		Integer count = (Integer) context.getSessionData(KEY_LINES);
		
		// Handle first run
		if (result == null) 
			context.setSessionData(KEY, result = new StringBuilder());
		if (count == null)
			count = 0;
		
		// Save the last line as well
		context.setSessionData(KEY_LAST, in);
		context.setSessionData(KEY_LINES, ++count);
		result.append(in + "\n");

		// And we're done
		if (endMarker.cancelBasedOnInput(context, in, result, count))
			return Prompt.END_OF_CONVERSATION;
		else
			return this;
	}

	@Override
	public String getPromptText(ConversationContext context) {
		Object last = context.getSessionData(KEY_LAST);

		if (last instanceof String)
			return (String) last;
		else
			return initialPrompt;
	}
}
