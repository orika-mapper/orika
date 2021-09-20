package ma.glasnost.orika.test.community.issue121.aobjects;

import java.util.Arrays;
import java.util.List;

import ma.glasnost.orika.test.community.issue121.util.RandomUtils;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public class AObject1 {

    public static AObject1 instance() {
        AObject1 aObject1 = new AObject1();
        aObject1.id = RandomUtils.randomInt();
        aObject1.name = RandomUtils.randomString();
        aObject1.list = Arrays.asList(AObject2.instance(),AObject2.instance());
        return aObject1;
    }

    private Integer id;
    private String name;
    private List<AObject2> list;

    public AObject1() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AObject2> getList() {
        return list;
    }

    public void setList(List<AObject2> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "AObject1{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", list=" + list +
                '}';
    }
}
