package ma.glasnost.orika.test.util;

import java.lang.reflect.ParameterizedType;
import ma.glasnost.orika.util.ClassHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ClassHelperTest {
   
   @Test
   public void testGenericParamTyping() {
      GenericInterface l1 = new DirectSub();
      GenericInterface l2 = new Level2Sub();
      GenericInterface l3 = new Level3Sub();
      
      Assert.assertEquals(Integer.class,ClassHelper.findParameterClasses(l1.getClass(), GenericInterface.class).get(2));
      
      /*
       * we cannot resolve these (yet) because the actual class of these parameters are not known 
       * in the declaration of the classes, only on the instance
       */
      Assert.assertEquals(null,ClassHelper.findParameterClasses(l1.getClass(), GenericInterface.class).get(0));
      Assert.assertEquals(null,ClassHelper.findParameterClasses(l2.getClass(), GenericInterface.class).get(1));

      Assert.assertEquals(Integer.class, ClassHelper.findParameterClasses(DirectSub.class, GenericInterface.class).get(2));
      Assert.assertEquals(Long.class, ClassHelper.findParameterClasses(Level2Sub.class, GenericInterface.class).get(0));
      
      Assert.assertEquals(Long.class, ClassHelper.findParameterClasses(Level3Sub.class, GenericInterface.class).get(0));
      Assert.assertEquals(Float.class, ClassHelper.findParameterClasses(Level3Sub.class, GenericInterface.class).get(1));
      Assert.assertEquals(Integer.class, ClassHelper.findParameterClasses(Level3Sub.class, GenericInterface.class).get(2));
   }
   
   @Test
   public void testClassOfType() {
      GenericInterface l1 = new DirectSub();
      GenericInterface l2 = new Level2Sub();
      GenericInterface l3 = new Level3Sub();
      
      Assert.assertEquals(Integer.class, ClassHelper.getClass(
          ( (ParameterizedType) DirectSub.class.getGenericInterfaces()[0] )
          .getActualTypeArguments()[2]));
      
      Assert.assertNull(ClassHelper.getClass(
          ( (ParameterizedType) DirectSub.class.getGenericInterfaces()[0] )
          .getActualTypeArguments()[1]));

      Assert.assertEquals(Float[].class, ClassHelper.getClass(
          ( (ParameterizedType) ArraySub.class.getGenericSuperclass() )
          .getActualTypeArguments()[0]));

      Assert.assertEquals(Level2Sub.class, ClassHelper.getClass(
          ArraySub.class.getGenericSuperclass()));
            
   }
   
   private class DirectSub<P1,P2> implements GenericInterface<P1, P2, Integer> {
      
   }
   // deliberately switch parameter position
   private class Level2Sub<P1> extends DirectSub<Long, P1>{
      
   }
   private class Level3Sub extends Level2Sub<Float>{
      
   }
   private class ArraySub extends Level2Sub<Float[]>{
      
   }
   

}
