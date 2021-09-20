package ma.glasnost.orika.test.community.issue137;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: tgruenheit
 * Date: 06.12.13
 * Time: 19:34
 * To change this template use File | Settings | File Templates.
 */
public class LevelThree {

    private String someValue = UUID.randomUUID().toString();

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }
}
