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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import org.junit.Test;

/**
 * Correctly implement compareTo contract for Type.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/pull/10">https://github.com/orika-mapper/orika</a>
 */
public class PullRequest10TestCase {
	public static class Base {
	}

	public static class SubType1 extends Base {
	}

	public static class SubSubType11 extends SubType1 {
	}

	public static class SubSubType12 extends SubType1 {
	}

	public static class SubType2 extends Base {
	}

	public static class SubSubType21 extends SubType2 {
	}

	public static class SubSubType22 extends SubType2 {
	}

	@Test
	public void testClassInheritanceSorting() {
		List<Type<?>> types = Arrays.<Type<?>> asList(
				TypeFactory.valueOf(Base.class),
				TypeFactory.valueOf(SubType1.class),
				TypeFactory.valueOf(SubType2.class),
				TypeFactory.valueOf(SubSubType11.class),
				TypeFactory.valueOf(SubSubType12.class),
				TypeFactory.valueOf(SubSubType21.class),
				TypeFactory.valueOf(SubSubType22.class));
		
		for (Type<?> x : types) {
			for (Type<?> y : types) {
                assertThat("sgn(x.compareTo(y)) == -sgn(y.compareTo(x)) for x="
                        + x.getSimpleName() + ", y=" + y.getSimpleName(),
                        sgn(x.compareTo(y)), is(-sgn(y.compareTo(x))));
				
				for (Type<?> z : types) {
					if (x.compareTo(y) > 0 && y.compareTo(z) > 0) {
                        assertThat(
								"(x.compareTo(y)>0 && y.compareTo(z)>0) implies x.compareTo(z)>0 for x="
										+ x.getSimpleName() + ", y="
										+ y.getSimpleName() + ", z="
                                        + z.getSimpleName(),
                                x.compareTo(z), is(greaterThan(0)));
					}
					if (x.compareTo(y) == 0) {
                        assertThat(
								"x.compareTo(y)==0 implies sgn(x.compareTo(z)) == sgn(y.compareTo(z)) for x="
										+ x.getSimpleName() + ", y="
										+ y.getSimpleName() + ", z="
										+ z.getSimpleName(),
                                sgn(x.compareTo(z)), is(sgn(y.compareTo(z))));
					}
				}
			}
		}
	}

	private int sgn(int value) {
		return Math.round(Math.signum(value));
	}
}
