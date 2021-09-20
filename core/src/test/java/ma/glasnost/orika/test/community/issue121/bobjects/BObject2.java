package ma.glasnost.orika.test.community.issue121.bobjects;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public class BObject2 {
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BObject2{" +
                "id=" + id +
                '}';
    }
}
