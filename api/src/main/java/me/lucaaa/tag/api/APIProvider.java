package me.lucaaa.tag.api;

/**
 * API Provider for the TagAPI class.
 * @hidden
 * INTERNAL USE ONLY - DO NOT USE!
 */
public abstract class APIProvider {
    private static TagAPI implementation;

    public static TagAPI getImplementation() {
        if (APIProvider.implementation == null) {
            throw new IllegalStateException("The TagAPI implementation is not set yet.");
        }
        return APIProvider.implementation;
    }

    public static void setImplementation(TagAPI implementation) {
        if (APIProvider.implementation != null) {
            throw new IllegalStateException("The AdvancedLocksAPI implementation is already set.");
        }
        APIProvider.implementation = implementation;
    }

    public abstract TagAPI get();
}