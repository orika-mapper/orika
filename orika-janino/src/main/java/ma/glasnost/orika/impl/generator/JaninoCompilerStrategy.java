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

package ma.glasnost.orika.impl.generator;

import ma.glasnost.orika.MappingException;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.IClassLoader;
import org.codehaus.janino.Java;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.UnitCompiler;
import org.codehaus.janino.util.ClassFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public class JaninoCompilerStrategy extends CompilerStrategy {
    
    private final static Logger LOG = LoggerFactory.getLogger(JaninoCompilerStrategy.class);
    
    private static final String WRITE_SOURCE_FILES_BY_DEFAULT = "false";
    private static final String WRITE_CLASS_FILES_BY_DEFAULT = "false";

    private final ByteArrayClassLoader classLoader;
    private IClassLoader iClassLoader = null;
    
    public JaninoCompilerStrategy() {
        super(WRITE_SOURCE_FILES_BY_DEFAULT, WRITE_CLASS_FILES_BY_DEFAULT);
        ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
        iClassLoader = new ClassLoaderIClassLoader(parentClassLoader);
        classLoader = new ByteArrayClassLoader(parentClassLoader);
    }
    
    @Override
    public Class<?> compileClass(SourceCodeContext sourceCode) throws SourceCodeGenerationException {
        
        Scanner scanner;
        try {
            scanner = new Scanner(sourceCode.getClassName(), new StringReader(sourceCode.toSourceFile()));
            Java.CompilationUnit localCompilationUnit = new Parser(scanner).parseCompilationUnit();
            UnitCompiler unitCompile = new UnitCompiler(localCompilationUnit, iClassLoader);
            ClassFile[] classes = unitCompile.compileUnit(false, false, false);
            return classLoader.findClass(classes[0].getThisClassName());
        } catch (Exception e) {
            LOG.error("Can not compile " + sourceCode.getClassName(), e);
            throw new MappingException("Can not compile the generated mapper", e);
        }
        
    }
    
    public void assureTypeIsAccessible(Class<?> type) throws SourceCodeGenerationException {
        //
    }
    
}
