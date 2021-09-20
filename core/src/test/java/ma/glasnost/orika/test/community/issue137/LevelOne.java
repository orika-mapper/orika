package ma.glasnost.orika.test.community.issue137;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: tgruenheit
 * Date: 06.12.13
 * Time: 19:34
 * To change this template use File | Settings | File Templates.
 */
public class LevelOne {

    private Set<LevelTwo> levelTwos;

    public Set<LevelTwo> getLevelTwos() {
        return levelTwos;
    }

    public void setLevelTwos(Set<LevelTwo> levelTwos) {
        this.levelTwos = levelTwos;
    }
}
