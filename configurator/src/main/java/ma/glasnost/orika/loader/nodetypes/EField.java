package ma.glasnost.orika.loader.nodetypes;

/**
* @author ikozar
* date     13.02.13
 *
 * Nodes in node "field"
*/
public enum EField implements IElement {
    A("a", true, ElementType.INFO),
    B("b", true, ElementType.ENDED_EVALUATED);

    private String localPart;
    private boolean isRequired;
    private ElementType type;


    private EField(String localPart, boolean required, ElementType type) {
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
