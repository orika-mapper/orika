package ma.glasnost.orika.test.community.issue121.aobjects;

import java.util.Map;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public class AMapAObjects1 extends AbstractOrderedMap<Integer, AObject1> {
    public AMapAObjects1(Map<Integer, AObject1> map) {
        super(map);
    }
}
