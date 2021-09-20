package ma.glasnost.orika.test.community.issue121.bobjects;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public class BObject1 {
    private Integer id;
    private String name;
    private Integer key;
    private BObject2Container container;

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

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public BObject2Container getContainer() {
        return container;
    }

    public void setContainer(BObject2Container container) {
        this.container = container;
    }

    @Override
    public String toString() {
        return "BObject1{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", key=" + key +
                ", container=" + container +
                '}';
    }
}
