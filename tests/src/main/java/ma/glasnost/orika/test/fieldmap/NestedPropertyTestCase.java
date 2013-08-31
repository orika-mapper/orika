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

package ma.glasnost.orika.test.fieldmap;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NestedPropertyTestCase {
    
    private MapperFactory mapperFactory;
    
    @Before
    public void setUp() {
        mapperFactory = MappingUtil.getMapperFactory();
        
        ClassMap<LineDTO, Line> classMap = ClassMapBuilder.map(LineDTO.class, Line.class).field("x0", "start.x").field("y0", "start.y")
                .field("x1", "end.x").field("y1", "end.y").toClassMap();
        mapperFactory.registerClassMap(classMap);
        
        mapperFactory.build();
    }
    
    @Test
    public void testNestedProperty() {
        
        Point start = new Point(2, 4);
        Point end = new Point(8, 9);
        
        Line line = new Line(start, end);
        
        LineDTO dto = mapperFactory.getMapperFacade().map(line, LineDTO.class);
        
        Assert.assertEquals(start.getX(), dto.getX0());
        Assert.assertEquals(start.getY(), dto.getY0());
        Assert.assertEquals(end.getX(), dto.getX1());
        Assert.assertEquals(end.getY(), dto.getY1());
    }
    
    @Test
    public void testNestedNullProperty() {
        
        Point start = new Point(2, 4);
        
        Line line = new Line(start, null);
        
        LineDTO dto = mapperFactory.getMapperFacade().map(line, LineDTO.class);
        
        Assert.assertEquals(start.getX(), dto.getX0());
        Assert.assertEquals(start.getY(), dto.getY0());
        Assert.assertEquals(0, dto.getX1());
        Assert.assertEquals(0, dto.getY1());
    }
    
    public static class Point {
        private int x, y;
        
        public Point(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
    }
    
    public static class Line {
        private Point start;
        private Point end;
        
        public Line(Point start, Point end) {
            super();
            this.start = start;
            this.end = end;
        }
        
        public Point getStart() {
            return start;
        }
        
        public void setStart(Point start) {
            this.start = start;
        }
        
        public Point getEnd() {
            return end;
        }
        
        public void setEnd(Point end) {
            this.end = end;
        }
    }
    
    public static class LineDTO {
        private int x0, y0, x1, y1;
        
        public int getX0() {
            return x0;
        }
        
        public void setX0(int x0) {
            this.x0 = x0;
        }
        
        public int getY0() {
            return y0;
        }
        
        public void setY0(int y0) {
            this.y0 = y0;
        }
        
        public int getX1() {
            return x1;
        }
        
        public void setX1(int x1) {
            this.x1 = x1;
        }
        
        public int getY1() {
            return y1;
        }
        
        public void setY1(int y1) {
            this.y1 = y1;
        }
        
    }
}
