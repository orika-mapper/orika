/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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
package ma.glasnost.orika.test.community;

import static org.junit.Assert.assertEquals;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class Issue53TestCase {
    
    @Test
    public void subClassSetterOrikaTest() {
        final MapperFactory mapperFactory = MappingUtil.getMapperFactory();/*new DefaultMapperFactory.Builder().build();*/
        final MapperFacade mapper = mapperFactory.getMapperFacade();
        
        final SearchRecord sr = new SearchRecord();
        sr.setScore(88);
        
        final Result result = new Result();
        result.setBaseRecords(new Record());
        result.setResult(sr);
        
        final Result2 mappedResult = mapper.map(result, Result2.class);
        
        assertEquals(88, mappedResult.getResult().getScore());
    }
    
    public static class Record {
    }
    
    public static class SearchRecord extends Record {
        private int score;
        
        public int getScore() {
            return score;
        }
        
        public void setScore(final int score) {
            this.score = score;
        }
    }
    
    public static class Result {
        private Record baseRecords;
        private SearchRecord result;
        
        public void setBaseRecords(final Record baseRecords) {
            this.baseRecords = baseRecords;
        }
        
        public Record getBaseRecords() {
            return baseRecords;
        }
        
        public SearchRecord getResult() {
            return result;
        }
        
        public void setResult(final SearchRecord result) {
            this.result = result;
        }
        
    }
    
    public static class Record2 {
    }
    
    public static class SearchRecord2 extends Record2 {
        private int score;
        
        public int getScore() {
            return score;
        }
        
        public void setScore(final int score) {
            this.score = score;
        }
    }
    
    public static class Result2 {
        private Record2 baseRecords;
        private SearchRecord2 result;
        
        public Record2 getBaseRecords() {
            return baseRecords;
        }
        
        public void setBaseRecords(final Record2 baseRecords) {
            this.baseRecords = baseRecords;
        }
        
        public SearchRecord2 getResult() {
            return result;
        }
        
        public void setResult(final SearchRecord2 result) {
            this.result = result;
        }
    }
}
