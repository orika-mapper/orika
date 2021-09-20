/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2019 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.test.generator;

import com.google.common.collect.ImmutableMap;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RetrieveDestinationValueOnMappingTestCase {

    public static class Container1 {
        private long longValue1;
        private String stringValue1;
        private List<String> listOfString1;
        private String[] arrayOfString1;
        private int[] arrayOfInt1;
        private Map<String, Object> map1;
        private MapNullsTestCase.Position enumValue1;

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
    }

    public static class Container2 {
        private long myLongValue;
        private String myStringValue;
        private List<String> myListOfString;
        private String[] myArrayOfString;
        private int[] myArrayOfInt;
        private Map<String, Object> myMap;
        private MapNullsTestCase.Position myEnumValue;
        private Object myObject;

        private boolean longSet, stringSet, listOfStringSet, arrayOfStringSet, arrayOfIntSet, mapSet, enumSet;

        public long getLongValue() {
            if(!longSet){
                throw new RuntimeException();
            }
            return myLongValue;
        }

        public void setLongValue(long longValue) {
            this.myLongValue = longValue;
            longSet = true;
        }

        public String getStringValue() {
            if(!stringSet){
                throw new RuntimeException();
            }
            return myStringValue;
        }

        public void setStringValue(String stringValue) {
            this.myStringValue = stringValue;
            stringSet = true;
        }

        public List<String> getListOfString() {
            if(!listOfStringSet){
                throw new RuntimeException();
            }
            return myListOfString;
        }

        public void setListOfString(List<String> listOfString) {
            this.myListOfString = listOfString;
            listOfStringSet = true;
        }

        public String[] getArrayOfString() {
            if(!arrayOfStringSet){
                throw new RuntimeException();
            }
            return myArrayOfString;
        }

        public void setArrayOfString(String[] arrayOfString) {
            this.myArrayOfString = arrayOfString;
            arrayOfStringSet = true;
        }

        public int[] getArrayOfInt() {
            if(!arrayOfIntSet){
                throw new RuntimeException();
            }
            return myArrayOfInt;
        }

        public void setArrayOfInt(int[] arrayOfInt) {
            this.myArrayOfInt = arrayOfInt;
            arrayOfIntSet = true;
        }

        public Map<String, Object> getMap() {
            if(!mapSet){
                throw new RuntimeException();
            }
            return myMap;
        }

        public void setMap(Map<String, Object> map) {
            this.myMap = map;
            mapSet = true;
        }

        public MapNullsTestCase.Position getEnumValue() {
            if(!enumSet){
                throw new RuntimeException();
            }
            return myEnumValue;
        }

        public void setEnumValue(MapNullsTestCase.Position enumValue) {
            this.myEnumValue = enumValue;
            enumSet = true;
        }

        public Object getMyObject() {
            return myObject;
        }

        public void setMyObject(Object myObject) {
            this.myObject = myObject;
        }
    }

    @Test
    public void shouldMapWithDisabledRetrievalOfDestinationValueOnMapping(){

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().captureFieldContext(true)
                .getDestinationOnMapping(false).build();
        mapperFactory.classMap(Container1.class, Container2.class)
                .mapNulls(true)
                .mapNullsInReverse(true)
                .byDefault()
                .register();

        Container1 container1 = buildContainer1();
        Container2 container2 = new Container2();
        Object myObject = new Object();
        container2.setMyObject(myObject);
        mapperFactory.getMapperFacade().map(container1, container2);

        assertContainersEquals(container1, container2);
        Assert.assertEquals(myObject, container2.getMyObject());
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailWithEnabledRetrievalOfDestinationValueOnMapping(){

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().captureFieldContext(true).build();
        mapperFactory.classMap(Container1.class, Container2.class)
                .mapNulls(true)
                .mapNullsInReverse(true)
                .byDefault()
                .register();

        Container1 container1 = buildContainer1();
        Container2 container2 = new Container2();
        Object myObject = new Object();
        container2.setMyObject(myObject);

        mapperFactory.getMapperFacade().map(container1, container2);
    }

    @Test
    public void shouldMapWithEnabledRetrievalOfDestinationValueOnMapping(){

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().captureFieldContext(true).build();
        mapperFactory.classMap(Container1.class, Container2.class)
                .mapNulls(true)
                .mapNullsInReverse(true)
                .byDefault()
                .register();

        Container1 container1 = buildContainer1();
        Container2 container2 = buildContainer2();
        Object myObject = new Object();
        container2.setMyObject(myObject);

        mapperFactory.getMapperFacade().map(container1, container2);

        assertContainersEquals(container1, container2);
        Assert.assertEquals(myObject, container2.getMyObject());
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

    private Container2 buildContainer2(){

        Container2 container2 = new Container2();

        container2.setLongValue(0L);
        container2.setStringValue("TEST B");
        container2.setArrayOfString(new String[]{"d", "e"});
        container2.setArrayOfInt(new int[] {4, 5});

        container2.setListOfString(Arrays.asList("l3","l4","l5"));
        container2.setMap(ImmutableMap.of("key2", "value2", "key3", 3));
        container2.setEnumValue(MapNullsTestCase.Position.LAST);

        return container2;
    }
}
