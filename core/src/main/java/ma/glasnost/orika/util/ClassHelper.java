package ma.glasnost.orika.util;


import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ClassHelper {

   private ClassHelper() {
   }

   /**
    * find the runtime class for a class parameter. Calls {@link #findParameterClasses(java.lang.Class, java.lang.Class)
    * }
    *
    * @param subclass the subclass of the generic class whose parameter class we want to know
    * @param classWithParameter the class that contains the parameter whose class we are looking for
    * @return
    */
   public static <T> Class<?> findParameterClass(int paramNum, Class<? extends T> subclass, Class<? extends T> classWithParameter) {
      return findParameterClasses(subclass, classWithParameter).get(paramNum);
   }

   /**
    * find the runtime classes of the parameters of a class or interface. The strategy is to visit the subclass and its
    * parent classes to find the classes of the classWithParameters
    *
    *
    * @param subclass the subclass of the generic class whose parameter classes we want to know
    * @param classWithParameter the class that contains the parameter whose class we are looking for
    * @return a List of java class for the class parameters, or null
    */
   public static <T> List<Class<?>> findParameterClasses(Class<? extends T> subclass, Class<? extends T> classWithParameter) {
      List<Class<?>> parameterClasses = null;
      // conditions
      TypeVariable[] params = classWithParameter.getTypeParameters();
      if (params.length > 0) { // parameters have to be present

         parameterClasses = new ArrayList<>(params.length);
         // to keep track of where parameters go in the class hierarchy
         Map<TypeVariable, Class<?>> varsPrevious = new HashMap<>(3);
         Map<TypeVariable, Class<?>> varsCurrent = new HashMap<>(3);
         Type parent = subclass;
         while ((parent = getGenericSuperType((Class) parent, classWithParameter)) != null) {
            if (parent instanceof ParameterizedType) {
               ParameterizedType pa = (ParameterizedType) parent;
               TypeVariable[] pars = getClass(pa.getRawType()).getTypeParameters();
               int i = 0;
               for (Type t : pa.getActualTypeArguments()) {
                  /*
                   * the number of getActualTypeArguments and getTypeParameters of the parent is always the same
                   *
                   * when t is a typevariable, we may find its class in previously processed type variables
                   *
                   * working this way finds the correct type, also when order of parameters changes in the hierarchy
                   */
                  if (t instanceof TypeVariable) {
                     varsCurrent.put(pars[i++], varsPrevious.getOrDefault(t, null));
                  } else {
                     varsCurrent.put(pars[i++], getClass(t));
                  }
               }
               parent = pa.getRawType();
            }
            varsPrevious.clear();
            varsPrevious.putAll(varsCurrent);
            varsCurrent.clear();
         }
         for (TypeVariable tv : params) {
            parameterClasses.add(varsPrevious.get(tv));
         }
      }
      return parameterClasses;
   }

   /**
    * returns either the generic superclass or a generic interface, but only when it is assignable from the typed
    * classWithParameter argument
    *
    * @param <T>
    * @param subclass
    * @param classWithParameter
    * @return
    */
   public static <T> Type getGenericSuperType(Class<? extends T> subclass, Class<? extends T> classWithParameter) {
      if (subclass.getSuperclass() != null && classWithParameter.isAssignableFrom(subclass.getSuperclass())) {
         return subclass.getGenericSuperclass();
      } else {
         int i = 0;
         for (Class in : subclass.getInterfaces()) {
            if (classWithParameter.isAssignableFrom(in)) {
               return subclass.getGenericInterfaces()[i];
            }
            i++;
         }
         return null;
      }
   }

   /**
    * when possible return the class of the type argument, otherwise null
    *
    * @param type
    * @return
    */
   public static Class<?> getClass(Type type) {
      if (type instanceof Class) {
         return (Class<?>) type;
      } else if (type instanceof ParameterizedType) {
         return (Class<?>) ((ParameterizedType) type).getRawType();
      } else if (type instanceof GenericArrayType) {
         Type componentType = ((GenericArrayType) type).getGenericComponentType();
         Class<?> componentClass = getClass(componentType);
         if (componentClass != null) {
            return Array.newInstance(componentClass, 0).getClass();
         } else {
            return null;
         }
      } else if (type instanceof TypeVariable) {
         return null;
      } else if (type instanceof WildcardType) {
         return null;
      } else {
         return null;
      }
   }


   public static Class<?> getPackageNeighbour(ma.glasnost.orika.metadata.Type<?> aType, ma.glasnost.orika.metadata.Type<?> bType) {
      boolean aIsPublic = Modifier.isPublic(aType.getRawType().getModifiers());
      boolean bIsPublic = Modifier.isPublic(bType.getRawType().getModifiers());

      if (aIsPublic) {
         if (bIsPublic) {
            // both public, no package needed
            return null;
         } else {
            // A public, B not --> use package of B
            return bType.getRawType();
         }
      } else {
         if (bIsPublic) {
            // A not public, B is --> use package of A
            return aType.getRawType();
         } else {
            // both package private --> make sure they're in the same package
            String aPackage = getPackageName(aType);
            if (aPackage.equals(getPackageName(bType))) {
               return aType.getRawType();
            } else {
               throw new RuntimeException(aType + " and " + bType + " are both package private but are in different packages");
            }
         }
      }
   }

   private static String getPackageName(ma.glasnost.orika.metadata.Type<?> type) {
      Package typePackage = type.getRawType().getPackage();
      return type == null ? "" : typePackage.getName();
   }
}
