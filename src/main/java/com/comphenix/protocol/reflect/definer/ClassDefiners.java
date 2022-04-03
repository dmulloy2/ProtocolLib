package com.comphenix.protocol.reflect.definer;

public final class ClassDefiners {

	// the definer which best fits for the jvm, lazily initialized
	private static ClassDefiner currentDefiner;

	private ClassDefiners() {
		// sealed
	}

	public static ClassDefiner availableDefiner() {
		// initialize if needed
		if (currentDefiner == null) {
			currentDefiner = findDefiner();
		}

		return currentDefiner;
	}

	private static ClassDefiner findDefiner() {
		// try the modern Lookup based definer first
		ClassDefiner definer = new HiddenClassDefiner();
		if (definer.isAvailable()) {
			return definer;
		}
		// try the Unsafe definer
		definer = new AnonymousClassDefiner();
		if (definer.isAvailable()) {
			return definer;
		}
		// fallback definer
		return new LoaderClassDefiner();
	}
}
