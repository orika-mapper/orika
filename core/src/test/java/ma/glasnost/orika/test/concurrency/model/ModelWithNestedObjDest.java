package ma.glasnost.orika.test.concurrency.model;

import java.util.List;

public class ModelWithNestedObjDest {

    private List<NestedModel> value;

    public List<NestedModel> getValue() {
        return value;
    }

    public void setValue(List<NestedModel> value) {
        this.value = value;
    }
}
