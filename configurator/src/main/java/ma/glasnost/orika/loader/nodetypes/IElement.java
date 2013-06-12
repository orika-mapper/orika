package ma.glasnost.orika.loader.nodetypes;

/**
 * @author ikozar
 * date     25.02.13
 */
public interface IElement {
    String getLocalPart();
    boolean isRequired();
    boolean isEnded();
    boolean isEvaluated();
    boolean isIgnored();
}
