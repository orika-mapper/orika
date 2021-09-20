package ma.glasnost.orika.test.community.issue121.aobjects;

import ma.glasnost.orika.test.community.issue121.util.RandomUtils;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public class AObject2 {

    public static AObject2 instance() {
        AObject2 aObject2 = new AObject2();
        aObject2.id = RandomUtils.randomInt();
        return aObject2;
    }

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AObject2{" +
                "id=" + id +
                '}';
    }
}
