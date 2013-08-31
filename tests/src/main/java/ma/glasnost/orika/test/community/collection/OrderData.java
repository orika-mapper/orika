package ma.glasnost.orika.test.community.collection;

import java.util.ArrayList;
import java.util.List;

public class OrderData {
    private String name;
    private List<PositionData> positions;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<PositionData> getPositions() {
        return positions;
    }
    
    public void setPositions(List<PositionData> positions) {
        this.positions = positions;
    }
    
    public void add(PositionData data) {
        if (positions == null) {
            positions = new ArrayList<PositionData>();
        }
        positions.add(data);
    }
}
