package ma.glasnost.orika.test.generator.janino;

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.test.DynamicSuite;
import ma.glasnost.orika.test.DynamicSuite.Scenario;
import ma.glasnost.orika.test.DynamicSuite.TestCasePattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * This provides the equivalent of a test suite which will run all the defined
 * test cases (matching ".*TestCase.class") using JaninoCompilerStrategy as the
 * compiler strategy instead of JaninoCompilerStrategy which is the default.
 * 
 * @author elaatifi@gmail.com
 *
 */
@RunWith(DynamicSuite.class)
@TestCasePattern(".*TestCase")
@Scenario(name = "janino")
public class JaninoCompilerStrategyTestSuite {
    
    @BeforeClass
    public static void janino() {
        System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY, JaninoCompilerStrategyTestSuite.class.getCanonicalName());
    }
    
    @AfterClass
    public static void tearDown() {
        System.clearProperty(OrikaSystemProperties.COMPILER_STRATEGY);
    }
    
}
