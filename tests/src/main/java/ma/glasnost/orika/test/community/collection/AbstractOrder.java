package ma.glasnost.orika.test.community.collection;

import java.util.Collection;

public class AbstractOrder<P extends IPosition> {
    private Collection<P> positions;
    
    // sun.reflect.generics.reflectiveObjects.TypeVariableImpl cannot be cast to
    // java.lang.Class
    public Collection<P> getPositions() {
        return positions;
    }
    
    // sun.reflect.generics.reflectiveObjects.WildcardTypeImpl cannot be cast to
    // java.lang.Class
    public Collection<?> getPositionen2() {
        return positions;
    }
    
    public void setPositions(Collection<P> positions) {
        this.positions = positions;
    }
}
