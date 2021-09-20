/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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
package ma.glasnost.orika.test.perf;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DefaultMapperFactoryGenerationConcurrencyTestCase {
    
    static public class A {

        private String property;
                
        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }

    static public class B {

        private String property;       

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }
        
    private static Throwable throwable = null;
 
    private class ConcurrencyTestRunnable implements Runnable {
  
        public void run() {
            try {

                
                MapperFactory factory = new DefaultMapperFactory.Builder().build();
                
                ClassMap<A, B> classMap = factory.classMap(A.class, B.class).byDefault().toClassMap();
                
                factory.registerClassMap(classMap);
                
                MapperFacade mapper = factory.getMapperFacade();
                
                A from = new A();
                from.setProperty("test");
                mapper.map(from, B.class);
            }
            
            catch(Exception e) {
                throwable = e;
            }
        }        
    }
    

    @Test 
    public void concurrencyTest() throws Exception {

        List<Thread> threads = new ArrayList<>();
        
        for(int i=0; i<50; ++i) {
            threads.add(new Thread(new DefaultMapperFactoryGenerationConcurrencyTestCase.ConcurrencyTestRunnable()));
        }
        
        for(Thread t : threads) {
            t.start();
        }
        
        for(Thread t : threads) {
            t.join();
        }
        
        Assert.assertNull("caught unexpected exception: " + throwable, throwable);
        
    }
}
