public class CriticalityProcessor {
    public EventTag determineTag(String tag) {
        return EventTag.valueOf(tag.trim().toUpperCase());
    }

    public enum EventTag {
        GENERALINTEREST,
        CRITICAL,
        NORMAL
    }
}