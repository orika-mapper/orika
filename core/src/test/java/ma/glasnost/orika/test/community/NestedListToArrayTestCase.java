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

package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NestedListToArrayTestCase {

	
	public static class L1 {
	       private List<V1> list = new ArrayList<>();

	       public final List<V1> getList() {
	               return list;
	       }

	       public final void setList(final List<V1> list) {
	               this.list = list;
	       }
	}

	public static class L2 {
	       private V2[]    list;

	       public final V2[] getList() {
	               return list;
	       }

	       public final void setList(final V2[] list) {
	               this.list = list;
	       }
	}

	public static class R1 {
	       private List<L1> list = new ArrayList<>();

	       public final void setList(final List<L1> list) {
	               this.list = list;
	       }

	       public List<L1> getList() {
	               return list;
	       }
	}

	public static class R2 {
	       private L2[]    list;

	       public final L2[] getList() {
	               return list;
	       }

	       public final void setList(final L2[] list) {
	               this.list = list;
	       }
	}

	public static class V1 {
	       public enum V1Type {
	               A, B
           }

	       private V1Type  type    = V1Type.A;

	       public final V1Type getType() {
	               return type;
	       }


	       public final void setType(final V1Type type) {
	               this.type = type;
	       }
	}

	public static class V2 {
	       public enum V2Type {
	               A, B
           }

	       private V2Type  type    = V2Type.A;


	       public final V2Type getType() {
	               return type;
	       }

	       public final void setType(final V2Type type) {
	               this.type = type;
	       }
	}
	
	
	@Test
	public void testMapping() {
		
		
		MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        final ClassMapBuilder<R1, R2> builder = mapperFactory.classMap(R1.class, R2.class);

        mapperFactory.registerClassMap(builder.byDefault().toClassMap());
        
        final MapperFacade facade = mapperFactory.getMapperFacade();
        
        
        
        final R1 r1 = new R1();
        final List<L1> list = new ArrayList<>();
        final L1 l1 = new L1();
        final List<V1> v1List = new ArrayList<>();
        final V1 v1 = new V1();
        v1List.add(v1);
        l1.setList(v1List);
        list.add(l1);
        r1.setList(list);
        
        final R2 r2 = facade.map(r1, R2.class);

		Assert.assertNotNull(r2);
		Assert.assertNotNull(r2.getList());
		for (L2 l2: r2.getList()) {
			Assert.assertNotNull(l2);
			Assert.assertNotNull(l2.getList());
			for (V2 v2: l2.getList()) {
				Assert.assertNotNull(v2);
				Assert.assertNotNull(v2.getType());
			}
		}
		
		Assert.assertEquals(r1.getList().get(0).getList().get(0).getType().toString(), 
				r2.getList()[0].getList()[0].getType().toString());
		
	}
	
	
	
}
