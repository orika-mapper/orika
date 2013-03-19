package ma.glasnost.orika.loader.nodetypes;

/**
* @author ikozar
* date     13.02.13
*/
public enum EMapping implements IElement {
    CLASS_A("class-a", true, ElementType.INFO),
    CLASS_B("class-b", true, ElementType.EVALUATED),
    EXCLUDE("exclude", false, ElementType.IGNORED),
    FIELD("field", false, ElementType.INFO),
    MAPPING("mapping", false, ElementType.ENDED);

    private String localPart;
    private boolean isRequired;
    private ElementType type;

    private EMapping(String localPart, boolean required, ElementType type) {
        this.localPart = localPart;
        isRequired = required;
        this.type = type;
    }

    public String getLocalPart() {
        return localPart;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean isEnded() {
        return type.isEnded();
    }

    public boolean isEvaluated() {
        return type.isEvaluated();
    }

    public boolean isIgnored() {
        return type.isIgnored();
    }
}
