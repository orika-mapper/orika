package ma.glasnost.orika.test.community.issue121.bobjects;

import java.util.List;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

/**
 *
 * This need, for example, for REST
 */

public class BContainerListBObject1 {

    private List<BObject1> list;

    public BContainerListBObject1() {
    }

    public List<BObject1> getList() {
        return list;
    }

    public void setList(List<BObject1> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "BContainerListBObject1{" +
                "list=" + list +
                '}';
    }
}
