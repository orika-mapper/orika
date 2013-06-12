package ma.glasnost.orika.loader.nodetypes;

/**
 * @author ikozar
 * date     25.02.13
 */
public enum ElementType {
    IGNORED,        // ignored nodes in current versio
    INFO,           // node contain information, used later
    EVALUATED,      // node ends information needs for execute unit of build
    ENDED,          // node ends biold level
    ENDED_EVALUATED;

    public boolean isEnded() {
        return this == ElementType.ENDED || this == ElementType.ENDED_EVALUATED;
    }

    public boolean isEvaluated() {
        return this == ElementType.EVALUATED || this == ElementType.ENDED_EVALUATED;
    }

    public boolean isIgnored() {
        return this == ElementType.IGNORED;
    }

}
