package ma.glasnost.orika.loader.nodetypes;

/**
* @author ikozar
* date     13.02.13
*/
public enum EMappings implements IElement {
    CONFIGURATION("configuration", false, ElementType.IGNORED),
    CUSTOM_CONVERTERS("custom-converters", false, ElementType.IGNORED),
    CONVERTER("converter", false, ElementType.IGNORED),
    CLASS_A("class-a", false, ElementType.IGNORED),
    CLASS_B("class-b", false, ElementType.IGNORED),
    MAPPING("mapping", false, ElementType.INFO),
    MAPPINGS("mappings", false, ElementType.ENDED);

    private String localPart;
    private boolean isRequired;
    private ElementType type;

    private EMappings(String localPart, boolean required, ElementType type) {
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
