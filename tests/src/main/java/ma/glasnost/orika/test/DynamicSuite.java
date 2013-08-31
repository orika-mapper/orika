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

package ma.glasnost.orika.test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;

/**
 * DynamicSuite resolves and runs a test suite dynamically containing all
 * classes matched by a specified pattern.<br>
 * Use the <code>@RunWith</code> annotation, specifying DyanimcSuite.class as
 * the value in order to run a test class as a dynamic suite. <br>
 * <br>
 * 
 * The pattern may be customized by specifying an value with the
 * <code>TestCasePattern</code> annotation.<br>
 * <br>
 * 
 * The tests may also be run as a "scenario" by marking the class with the
 * <code>@Scenario</code> annotation. Running tests as a scenario will cause all
 * of the resolved test cases' methods to be suffixed with the scenario name.<br>
 * This is necessary in case you want to run these tests again, under a new
 * "scenario", since normally, JUnit attempts to avoid running the same test
 * method more than once. <br>
 * <br>
 * The JUnit 4+ <code>@BeforeClass</code> and <code>@AfterClass</code>
 * annotations may used to define setup and tear-down methods to be performed
 * before and after the entire suite, respectively.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public class DynamicSuite extends ParentRunner<Runner> {

	private static final String DEFAULT_TEST_CASE_PATTERN = ".*TestCase";

	/**
	 * The <code>TestCasePattern</code> annotation specifies the pattern from
	 * which test case classes should be matched.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface TestCasePattern {
		public String value();
	}

	/**
	 * The <code>Scenario</code> annotation is used to mark the dynamic suite
	 * with a specific name that should be appended to each executed test name.
	 * This is useful in the case where you want to create multiple copies of a
	 * particular dynamic suite definition, but would like to run them with
	 * slightly different configuration for the entire suite (which could be
	 * achieved using the <code>@BeforeClass</code> and <code>@AfterClass</code>
	 * annotations for setup/tear-down of the entire suite).<br>
	 * <br>
	 * If the 'name' parameter is not supplied, then the class simpleName is
	 * used as a default.<br>
	 * Without the unique scenario name, multiple copies of the tests resolved
	 * by the suite would not be run as JUnit avoids running the same test more
	 * than once, where uniqueness based on test name.
	 * 
	 * @see @@org.junit.BeforeClass, @org.junit.AfterClass
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Scenario {
		public String name() default "";
	}

	/**
	 * Resolves the <code>@Scenario</code> annotation if present; if found, the
	 * scenario will be given a unique name suffix for all of the tests,
	 * otherwise, a default scenario is run with no name suffix.
	 * 
	 * @param testClass
	 *            the class which defines the DynamicSuite
	 * @return
	 */
	private static String getScenarioName(TestClass testClass) {

		Scenario s = testClass.getJavaClass().getAnnotation(Scenario.class);
		String name = null;
		if (s != null) {
			name = "".equals(s.name().trim()) ? testClass.getJavaClass()
					.getSimpleName() : s.name();
		}
		return name;
	}

	/**
	 * Resolves the test classes that are matched by the specified test pattern.
	 * 
	 * @param theClass
	 *            the root class which defines the DynamicSuite; the
	 *            <code>@TestCasePattern</code> annotation will be used to
	 *            determine the pattern of test cases to include, and the root
	 *            folder will be determined based on the root folder for the
	 *            class-loader of <code>theClass</code>.
	 * @param
	 * @return
	 */
	public static List<Class<?>> findTestCases(Class<?> theClass) {
		List<File> classFolders = new ArrayList<File>();
		try {
		    ClassLoader loader = theClass.getClassLoader();
		    if (loader instanceof URLClassLoader) {
		        for (URL url: ((URLClassLoader)loader).getURLs()) {
		            File file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		            if (file.isDirectory()) {
		                classFolders.add(file);
		            }
		        }
		    } else {
		        classFolders.add(new File(URLDecoder.decode(theClass.getResource("/").getFile(), "UTF-8")));
		    }
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		String testCaseRegex = getTestCasePattern(theClass);
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (File classFolder: classFolders) {
		    classes.addAll(findTestCases(classFolder, testCaseRegex));
		}
		return classes;
	}

	private static boolean containsTests(Class<?> testClass) {
		boolean containsTests = false;
		for (Method m: testClass.getMethods()) {
			Test test = m.getAnnotation(Test.class);
			if (test != null) {
				containsTests = true;
				break;
			}
			if (m.getName().startsWith("test") 
					&& Modifier.isPublic(m.getModifiers()) 
					&& !Modifier.isStatic(m.getModifiers())) {
				containsTests = true;
				break;
			}
		}
		return containsTests;
	}
	
	/**
	 * Resolves the test classes that are matched by the specified test pattern.
	 * 
	 * @param classFolder
	 *            the root folder under which to search for test cases
	 * @param testCaseRegex
	 *            the pattern to use when looking for test cases to include;
	 *            send null to use the value annotated on the class designated
	 *            by the class parameter.
	 * @return
	 */
	public static List<Class<?>> findTestCases(File classFolder,
			String testCaseRegex) {
		try {
			Pattern testCasePattern = Pattern.compile(testCaseRegex);

			ClassLoader tccl = Thread.currentThread().getContextClassLoader();
			List<Class<?>> testCases = new ArrayList<Class<?>>();

			int classFolderPathLength = classFolder.getAbsolutePath().length();

			LinkedList<File> stack = new LinkedList<File>();
			stack.addAll(Arrays.asList(classFolder.listFiles()));
			File currentDirectory = classFolder;
			String currentPackage = "";
			while (!stack.isEmpty()) {

				File file = stack.removeFirst();

				if (file.isDirectory()) {
					// push
					stack.addAll(Arrays.asList(file.listFiles()));
				} else {
					if (file.getName().endsWith(".class")) {
						String className = file.getAbsolutePath().replace(
								classFolder.getAbsolutePath() + File.separator,
								"");
						className = className.replaceAll("[\\\\/]", ".")
								.replace(".class", "");
						if (testCasePattern.matcher(className).matches()) {
							if (!currentDirectory.equals(file.getParentFile())) {
								currentDirectory = file.getParentFile();
								currentPackage = currentDirectory
										.getAbsolutePath().substring(
												classFolderPathLength + 1);
								currentPackage = currentPackage.replaceAll(
										"[\\/]", ".");
							}
							Class<?> theClass = Class.forName(className, false, tccl);
							if (containsTests(theClass)) {
								testCases.add(theClass);
							}
						}
					}
				}
			}

			return testCases;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Resolves a test class pattern (regular expression) which is used to
	 * resolve the names of classes that will be included in this test suite.
	 * 
	 * @param klass
	 *            the class which defines the DynamicSuite
	 * @return the compiled Pattern
	 */
	private static String getTestCasePattern(Class<?> klass) {

		String pattern = DEFAULT_TEST_CASE_PATTERN;
		TestCasePattern annotation = klass.getAnnotation(TestCasePattern.class);
		if (annotation != null) {
			pattern = annotation.value();
		}
		return pattern;
	}

	// =============================================================================

	private final List<Runner> fRunners;
	private final String name;
	private final String scenarioName;
	private final TestScenarioProxyFactory proxyFactory;

	public DynamicSuite(Class<?> klass, RunnerBuilder builder)
			throws InitializationError {
		this(builder, klass, findTestCases(klass).toArray(new Class<?>[0]));
	}

	public DynamicSuite(RunnerBuilder builder, Class<?>[] classes)
			throws InitializationError {
		this(null, builder.runners(null, classes));
	}

	protected DynamicSuite(Class<?> klass, Class<?>[] suiteClasses)
			throws InitializationError {
		this(new AllDefaultPossibilitiesBuilder(true), klass, suiteClasses);
	}

	protected DynamicSuite(RunnerBuilder builder, Class<?> klass,
			Class<?>[] suiteClasses) throws InitializationError {
		this(klass, builder.runners(klass, suiteClasses));
	}

	protected DynamicSuite(Class<?> klass, List<Runner> runners)
			throws InitializationError {
		super(klass);
		try {
			this.proxyFactory = new TestScenarioProxyFactory();
			this.scenarioName = getScenarioName(getTestClass());

			if (scenarioName == null) {
				this.fRunners = runners;
				this.name = klass.getName();
			} else {
				this.name = klass.getName() + "[" + scenarioName + "]";
				this.fRunners = new ArrayList<Runner>(runners.size());
				for (Runner runner : runners) {
					if (!(runner instanceof BlockJUnit4ClassRunner)) {
						throw new IllegalArgumentException(
								"Unexpected Runner type: "
										+ runner.getClass().getName());
					}
					fRunners.add(proxyFactory.forTestScenario(runner,
							scenarioName));
				}
			}
		} catch (Exception e) {
			throw new InitializationError(e);
		}

	}

	@Override
	protected String getName() {
		return name;
	}

	@Override
	protected List<Runner> getChildren() {
		return fRunners;
	}

	@Override
	protected Description describeChild(Runner child) {
		return child.getDescription();
	}

	@Override
	protected void runChild(Runner runner, final RunNotifier notifier) {
		runner.run(notifier);
	}

	/**
	 * Provides a unique name for each test based on appending the scenario
	 * name; this is accomplished by sub-classing the existing runner with a
	 * class which will provide the modified testName value.
	 * 
	 * @author matt.deboer@gmail.com
	 * 
	 */
	private static class TestScenarioProxyFactory {

		private ClassPool pool;
		private ConcurrentHashMap<String, Class<Runner>> proxyCache;

		private TestScenarioProxyFactory() {
			this.pool = new ClassPool();
			this.pool.appendSystemPath();
			this.proxyCache = new ConcurrentHashMap<String, Class<Runner>>();
		}

		@SuppressWarnings("unchecked")
		public Runner forTestScenario(Runner delegate, String scenarioName) {

			final String proxyName = delegate.getClass().getName()
					+ "_$$_Proxy";
			Class<? extends Runner> proxyClass = proxyCache.get(proxyName);
			if (proxyClass == null) {

				try {
					try {
						/*
						 * Try to load the class; in case another javassist pool
						 * has already created this class, just reuse it.
						 */
						proxyClass = (Class<? extends Runner>) pool
								.getClassLoader().loadClass(proxyName);
					} catch (ClassNotFoundException e) {
						synchronized (pool) {
							try {
								proxyClass = (Class<? extends Runner>) pool
										.getClassLoader().loadClass(proxyName);
							} catch (ClassNotFoundException e2) {
								/*
								 * Generate a subclass of the provided type,
								 * where the testName method is overridden to
								 * provide the modified name which includes the
								 * scenario
								 */
								CtClass superClass = pool.get(delegate
										.getClass().getName());
								CtClass subClass = pool.makeClass(proxyName);
								subClass.setSuperclass(superClass);

								CtField scenarioNameField = CtField
										.make("private java.lang.String scenarioName;",
												subClass);
								subClass.addField(scenarioNameField);

								CtMethod overriddenMethod = CtMethod
										.make("protected java.lang.String testName(org.junit.runners.model.FrameworkMethod method) { \n"
												+ "return java.lang.String.format(\"%s[%s]\", new Object[]{ super.testName(method), scenarioName});\n"
												+ "}", subClass);
								subClass.addMethod(overriddenMethod);

								CtConstructor constructor = new CtConstructor(
										new CtClass[] {
												pool.get(Class.class.getName()),
												pool.get(String.class.getName()) },
										subClass);
								constructor
										.setBody("{ super($1); this.scenarioName = $2; }");
								subClass.addConstructor(constructor);
								proxyClass = subClass.toClass();

							}
						}
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				Class<Runner> existing = proxyCache.putIfAbsent(proxyName,
						(Class<Runner>) proxyClass);
				if (existing != null) {
					proxyClass = existing;
				}
			}

			try {
				return proxyClass.getConstructor(Class.class, String.class)
						.newInstance(delegate.getDescription().getTestClass(),
								scenarioName);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

}
