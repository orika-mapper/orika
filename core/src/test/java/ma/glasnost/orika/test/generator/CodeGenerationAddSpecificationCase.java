package ma.glasnost.orika.test.generator;

import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.CodeGenerationStrategy;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.Specification;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.generator.specification.Convert;
import ma.glasnost.orika.metadata.FieldMap;
import org.junit.Assert;
import org.junit.Test;

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
public class CodeGenerationAddSpecificationCase {

    @Test
    public void testAddSpecification() {
        Specification testSpec = new TestSpecification();
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        CodeGenerationStrategy codeGenerationStrategy = builder.getCodeGenerationStrategy();
        codeGenerationStrategy.addSpecification(testSpec, CodeGenerationStrategy.Position.IN_PLACE_OF, Convert.class);
        DefaultMapperFactory mapperFactory= builder.build();

        Assert.assertTrue(codeGenerationStrategy.getSpecifications().contains(testSpec));
    }

    private class TestSpecification extends Convert {
        @Override
        public boolean appliesTo(FieldMap fieldMap) {
            return super.appliesTo(fieldMap);
        }

        @Override
        public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination,
                                               SourceCodeContext code) {
            return super.generateEqualityTestCode(fieldMap, source, destination, code);
        }

        @Override
        public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination,
                                          SourceCodeContext code) {
            return super.generateMappingCode(fieldMap, source, destination, code);
        }
    }
}
