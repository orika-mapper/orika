package ma.glasnost.orika.test.generator;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SingleGetterCallTestCase {

    public static class Container1 {
        private long longValue1;
        private String stringValue1;
        private List<String> listOfString1;
        private String[] arrayOfString1;
        private int[] arrayOfInt1;
        private Map<String, Object> map1;
        private MapNullsTestCase.Position enumValue1;
        private char charValue1;
        private byte byteValue1;
        private boolean booleanValue1;
        private float floatValue1;
        private double doubleValue1;

        int longValue1GetterCalls, stringValue1GetterCalls, listOfString1GetterCalls,
                arrayOfString1GetterCalls, arrayOfInt1GetterCalls, map1GetterCalls, enumValue1GetterCalls,
                charValue1GetterCalls, byteValue1GetterCalls, booleanValue1GetterCalls, floatValue1GetterCalls,
                doubleValue1GetterCalls;

        public long getLongValue() {
            ++longValue1GetterCalls;
            return longValue1;
        }

        public void setLongValue(long longValue) {
            this.longValue1 = longValue;
        }

        public String getStringValue() {
            ++stringValue1GetterCalls;
            return stringValue1;
        }

        public void setStringValue(String stringValue) {
            this.stringValue1 = stringValue;
        }

        public List<String> getListOfString() {
            ++listOfString1GetterCalls;
            return listOfString1;
        }

        public void setListOfString(List<String> listOfString) {
            this.listOfString1 = listOfString;
        }

        public String[] getArrayOfString() {
            ++arrayOfString1GetterCalls;
            return arrayOfString1;
        }

        public void setArrayOfString(String[] arrayOfString) {
            this.arrayOfString1 = arrayOfString;
        }

        public int[] getArrayOfInt() {
            ++arrayOfInt1GetterCalls;
            return arrayOfInt1;
        }

        public void setArrayOfInt(int[] arrayOfInt) {
            this.arrayOfInt1 = arrayOfInt;
        }

        public Map<String, Object> getMap() {
            ++map1GetterCalls;
            return map1;
        }

        public void setMap(Map<String, Object> map) {
            this.map1 = map;
        }

        public MapNullsTestCase.Position getEnumValue() {
            ++enumValue1GetterCalls;
            return enumValue1;
        }

        public void setEnumValue(MapNullsTestCase.Position enumValue) {
            this.enumValue1 = enumValue;
        }

        public char getCharValue1() {
            ++charValue1GetterCalls;
            return charValue1;
        }

        public void setCharValue1(char charValue1) {
            this.charValue1 = charValue1;
        }

        public byte getByteValue1() {
            ++byteValue1GetterCalls;
            return byteValue1;
        }

        public void setByteValue1(byte byteValue1) {
            this.byteValue1 = byteValue1;
        }

        public boolean isBooleanValue1() {
            ++booleanValue1GetterCalls;
            return booleanValue1;
        }

        public void setBooleanValue1(boolean booleanValue1) {
            this.booleanValue1 = booleanValue1;
        }

        public float getFloatValue1() {
            ++floatValue1GetterCalls;
            return floatValue1;
        }

        public void setFloatValue1(float floatValue1) {
            this.floatValue1 = floatValue1;
        }

        public double getDoubleValue1() {
            ++doubleValue1GetterCalls;
            return doubleValue1;
        }

        public void setDoubleValue1(double doubleValue1) {
            this.doubleValue1 = doubleValue1;
        }
    }

    public static class Container2 {
        private long longValue1;
        private String stringValue1;
        private List<String> listOfString1;
        private String[] arrayOfString1;
        private int[] arrayOfInt1;
        private Map<String, Object> map1;
        private MapNullsTestCase.Position enumValue1;
        private char charValue1;
        private byte byteValue1;
        private boolean booleanValue1;
        private float floatValue1;
        private double doubleValue1;

        public long getLongValue() {
            return longValue1;
        }

        public void setLongValue(long longValue) {
            this.longValue1 = longValue;
        }

        public String getStringValue() {
            return stringValue1;
        }

        public void setStringValue(String stringValue) {
            this.stringValue1 = stringValue;
        }

        public List<String> getListOfString() {
            return listOfString1;
        }

        public void setListOfString(List<String> listOfString) {
            this.listOfString1 = listOfString;
        }

        public String[] getArrayOfString() {
            return arrayOfString1;
        }

        public void setArrayOfString(String[] arrayOfString) {
            this.arrayOfString1 = arrayOfString;
        }

        public int[] getArrayOfInt() {
            return arrayOfInt1;
        }

        public void setArrayOfInt(int[] arrayOfInt) {
            this.arrayOfInt1 = arrayOfInt;
        }

        public Map<String, Object> getMap() {
            return map1;
        }

        public void setMap(Map<String, Object> map) {
            this.map1 = map;
        }

        public MapNullsTestCase.Position getEnumValue() {
            return enumValue1;
        }

        public void setEnumValue(MapNullsTestCase.Position enumValue) {
            this.enumValue1 = enumValue;
        }

        public char getCharValue1() {
            return charValue1;
        }

        public void setCharValue1(char charValue1) {
            this.charValue1 = charValue1;
        }

        public byte getByteValue1() {
            return byteValue1;
        }

        public void setByteValue1(byte byteValue1) {
            this.byteValue1 = byteValue1;
        }

        public boolean isBooleanValue1() {
            return booleanValue1;
        }

        public void setBooleanValue1(boolean booleanValue1) {
            this.booleanValue1 = booleanValue1;
        }

        public float getFloatValue1() {
            return floatValue1;
        }

        public void setFloatValue1(float floatValue1) {
            this.floatValue1 = floatValue1;
        }

        public double getDoubleValue1() {
            return doubleValue1;
        }

        public void setDoubleValue1(double doubleValue1) {
            this.doubleValue1 = doubleValue1;
        }
    }

    //FIXME: problem with primitive types. Somewhere it should be a wrapped type and somewhere it should as raw type but we are using a wrapped type everywhere
    @Test
    public void shouldMapWithSingleGetterCall(){

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().captureFieldContext(true).build();
        mapperFactory.classMap(Container1.class, Container2.class)
                .mapNulls(true)
                .mapNullsInReverse(true)
                .byDefault()
                .register();

        Container1 container1 = buildContainer1();
        Container2 container2 = new Container2();

        mapperFactory.getMapperFacade().map(container1, container2);

        assertContainersEquals(container1, container2);

        //TODO: check getter calls amount
    }

    private Container1 buildContainer1(){

        Container1 container1 = new Container1();

        container1.longValue1 = 1L;
        container1.stringValue1 = "TEST A";
        container1.arrayOfString1 = new String[]{"a", "b", "c"};
        container1.arrayOfInt1 = new int[] {1,2,3};
        container1.listOfString1 = Arrays.asList("l1","l2");
        container1.map1 = Collections.singletonMap("key1", "value1");
        container1.enumValue1 = MapNullsTestCase.Position.FIRST;

        return container1;
    }

    private void assertContainersEquals(Container1 container1, Container2 container2){
        Assert.assertEquals(container1.getLongValue(), container2.getLongValue());
        Assert.assertEquals(container1.getStringValue(), container2.getStringValue());
        Assert.assertEquals(container1.getListOfString(), container2.getListOfString());
        Assert.assertArrayEquals(container1.getArrayOfInt(), container2.getArrayOfInt());
        Assert.assertArrayEquals(container1.getArrayOfString(), container2.getArrayOfString());
        Assert.assertEquals(container1.getMap(), container2.getMap());
        Assert.assertEquals(container1.getEnumValue(), container2.getEnumValue());
    }

}
