package ma.glasnost.orika.test.concurrency.model;

import java.util.Objects;

public class NestedModel {

    private Object value;

    public NestedModel(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NestedModel other = (NestedModel) obj;
        return Objects.equals(value, other.value);
    }

}
