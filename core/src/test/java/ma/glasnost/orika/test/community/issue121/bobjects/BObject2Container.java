package ma.glasnost.orika.test.community.issue121.bobjects;

import java.util.List;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public class BObject2Container {

    private List<BObject2> list;

    public List<BObject2> getList() {
        return list;
    }

    public void setList(List<BObject2> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "BObject2Container{" +
                "list=" + list +
                '}';
    }
}
