package ma.glasnost.orika.test.community.collection;

public class Order extends AbstractOrder<Position> {
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
