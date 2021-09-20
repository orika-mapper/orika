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
package ma.glasnost.orika.test;

public final class TestUtil {
    
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestUtil.class);
    
    /**
     * @see #expectException(MethodToCall, Class)
     * @param call
     *            The Method to test
     * @return The expected Exception
     */
    public static Exception expectException(final MethodToCall call) {
        return expectException(call, Exception.class);
    }
    /**
     * Test the Exception-Case of your Logic.
     * <p>
     * Example Usage:
     * </p>
     *
     * <pre>
     * MyException expectedException = TestUtils.expectException(() -&gt; myService.myMethodToTest(), MyException.class);
     *
     * assertThat(expectedException.getMessage(), containsString("..."));
     * </pre>
     *
     * @param <T>
     *            The type of the expected Exception.
     * @param call
     *            The Method to test
     * @param expectedExceptionType
     *            .
     * @return The expected Exception
     */
    @SuppressWarnings("unchecked")
    public static <T extends Exception> T expectException(final MethodToCall call, final Class<T> expectedExceptionType) {
        try {
            call.run();
            throw new AssertionError("The Method should throw an Exception");
        } catch (final Exception e) {
            if (!expectedExceptionType.isInstance(e)) {
                LOG.info("Wrong Exception: " + e.getMessage(), e);
                throw new AssertionError("Wrong Exception instance: " + e.getClass() + ". Message: " + e.getMessage());
            }
            return (T) e;
        }
    }
    
    /**
     * Functional Interface for {@link TestUtil#expectException(MethodToCall, Class)}.
     * 
     * @FunctionalInterface
     */
    public interface MethodToCall {
        void run() throws Exception;
    }
}
