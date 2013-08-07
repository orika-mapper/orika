package ma.glasnost.orika.test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.factory.javassist.JavassistProxyFactory;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * @author ikozar
 * date    07.08.13
 */
public class CopyByResfernceTest {

    public static class S {
        public Name name;
        public Name name1;
        public String description;
    }

    public static class D {
        public Name name;
        public Name name1;
        public String description;
    }

    public static class Name {
        public String first;
        public String last;
    }

    public static class InterceptorTester implements Interceptor
    {
        public Object intercept( Invocation methodInvocation ) throws Throwable
        {
            return methodInvocation.proceed();
        }
    }

    @Test
    public void test() {
        ProxyFactory pf = new JavassistProxyFactory();
        MapperFactory factory = new DefaultMapperFactory.Builder().copyByReference(true).build();
        MapperFacade mapper = factory.getMapperFacade();

        S source = new S();
        Name n = new Name();
        n.first = "John";
        n.last = "Doe";
        source.name = n;
        source.name1 = (Name) pf.createInterceptorProxy(n, new InterceptorTester(), new Class[] {Name.class});
        source.description = "description";

        D dest = mapper.map(source, D.class);

        assertNotNull(dest.name);
        assertTrue(source.name == dest.name);
        assertTrue(source.name1 == dest.name1);
    }

}
