package com.comphenix.protocol;

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
	// Feels a bit like Android
	private static final String KEY = "multiple_lines_prompt";
	private static final String KEY_LAST = KEY + ".last_line";
	
	private final ConversationCanceller endMarker;
	private final String initialPrompt;
	
	/**
	 * Retrieve and remove the current accumulated input.
	 * @param context - conversation context.
	 * @return The accumulated input, or NULL if not found.
	 */
	public String removeAccumulatedInput(ConversationContext context) {
		Object result = context.getSessionData(KEY);
		
		if (result instanceof StringBuilder) {
			context.setSessionData(KEY, null);
			return ((StringBuilder) result).toString();
		} else {
			return null;
		}
	}
	
	/**
	 * Construct a multiple lines input prompt with a specific end marker. 
	 * <p>
	 * This is usually an empty string.
	 * @param endMarker - the end marker.
	 */
	public MultipleLinesPrompt(String endMarker, String initialPrompt) {
		this(new ExactMatchConversationCanceller(endMarker), initialPrompt);
	}
	
	public MultipleLinesPrompt(ConversationCanceller endMarker, String initialPrompt) {
		this.endMarker = endMarker;
		this.initialPrompt = initialPrompt;
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String in) {
		StringBuilder result = (StringBuilder) context.getSessionData(KEY);
		
		if (result == null) {
			context.setSessionData(KEY, result = new StringBuilder());
		}
		
		// Save the last line as well
		context.setSessionData(KEY_LAST, in);
		result.append(in);
		
		// And we're done
		if (endMarker.cancelBasedOnInput(context, in))
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
