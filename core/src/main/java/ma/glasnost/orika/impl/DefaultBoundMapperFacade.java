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
package ma.glasnost.orika.impl;

import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

/**
 * DefaultBoundMapperFacade is the base implementation of BoundMapperFacade
 * 
 * @author matt.deboer@gmail.com
 * 
 */
class DefaultBoundMapperFacade<A, B> implements BoundMapperFacade<A, B> {
    
    /*
     * Keep small cache of strategies; we expect the total size to be == 1 in most cases,
     * but some polymorphism is possible
     */
    protected final BoundStrategyCache aToB;
    protected final BoundStrategyCache bToA;
    protected final BoundStrategyCache aToBInPlace;
    protected final BoundStrategyCache bToAInPlace;
    
    protected volatile ObjectFactory<A> objectFactoryA;
    protected volatile ObjectFactory<B> objectFactoryB;
    
    
    protected final java.lang.reflect.Type rawAType;
    protected final java.lang.reflect.Type rawBType;
    protected final Type<A> aType;
    protected final Type<B> bType;
    protected final MapperFactory mapperFactory;
    protected final MappingContextFactory contextFactory;
    
    
    /**
     * Constructs a new instance of DefaultBoundMapperFacade
     * 
     * @param mapperFactory
     * @param contextFactory
     * @param typeOfA
     * @param typeOfB
     */
    DefaultBoundMapperFacade(MapperFactory mapperFactory, MappingContextFactory contextFactory,  java.lang.reflect.Type typeOfA, java.lang.reflect.Type typeOfB) {
        this.mapperFactory = mapperFactory;
        this.contextFactory = contextFactory;
        this.rawAType = typeOfA;
        this.rawBType = typeOfB;
        this.aType = TypeFactory.valueOf(typeOfA);
        this.bType = TypeFactory.valueOf(typeOfB);
        this.aToB = new BoundStrategyCache(aType, bType, mapperFactory.getMapperFacade(), mapperFactory.getUserUnenhanceStrategy(), false);
        this.bToA = new BoundStrategyCache(bType, aType, mapperFactory.getMapperFacade(), mapperFactory.getUserUnenhanceStrategy(), false);
        this.aToBInPlace = new BoundStrategyCache(aType, bType, mapperFactory.getMapperFacade(), mapperFactory.getUserUnenhanceStrategy(), true);
        this.bToAInPlace = new BoundStrategyCache(bType, aType, mapperFactory.getMapperFacade(), mapperFactory.getUserUnenhanceStrategy(), true);
    }
    
    public Type<A> getAType() {
        return aType;
    }
    
    public Type<B> getBType() {
        return bType;
    }
    
    public B map(A instanceA) {
        MappingContext context = contextFactory.getContext();
        try {
            return map(instanceA, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public A mapReverse(B source) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapReverse(source, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public B map(A instanceA, B instanceB) {
        MappingContext context = contextFactory.getContext();
        try {
           return map(instanceA, instanceB, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public A mapReverse(B instanceB, A instanceA) {
        MappingContext context = contextFactory.getContext();
        try {
           return mapReverse(instanceB, instanceA, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapAtoB(java.lang.Object,
     * ma.glasnost.orika.MappingContext)
     */
    @SuppressWarnings("unchecked")
    public B map(A instanceA, MappingContext context) {
        B result = (B) context.getMappedObject(instanceA, bType);
        if (result == null && instanceA != null) {
            result = (B) aToB.getStrategy(instanceA, context).map(instanceA, null, context);
        }
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapBtoA(java.lang.Object,
     * ma.glasnost.orika.MappingContext)
     */
    @SuppressWarnings("unchecked")
    public A mapReverse(B instanceB, MappingContext context) {
        A result = (A) context.getMappedObject(instanceB, aType);
        if (result == null && instanceB != null) {
            result = (A) bToA.getStrategy(instanceB, context).map(instanceB, null, context);
        }
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapAtoB(java.lang.Object,
     * java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    @SuppressWarnings("unchecked")
    public B map(A instanceA, B instanceB, MappingContext context) {
        B result = (B) context.getMappedObject(instanceA, bType);
        if (result == null && instanceA != null) {
            result = (B) aToBInPlace.getStrategy(instanceA, context).map(instanceA, instanceB, context);
        }
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapBtoA(java.lang.Object,
     * java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    @SuppressWarnings("unchecked")
    public A mapReverse(B instanceB, A instanceA, MappingContext context) {
        A result = (A) context.getMappedObject(instanceB, aType);
        if (result == null && instanceB != null) {
            result = (A) bToAInPlace.getStrategy(instanceB, context).map(instanceB, instanceA, context);
        }
        return result;
    }
    
    public String toString() {
        return getClass().getSimpleName() + "(" + aType +", " + bType + ")";
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.DedicatedMapperFacade#newObjectB(java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public B newObject(A source, MappingContext context) {
        if (objectFactoryB == null) {
            synchronized(this) {
                if (objectFactoryB == null) {
                    objectFactoryB = mapperFactory.lookupObjectFactory(bType, aType);
                }
            }
        }
        return objectFactoryB.create(source, context);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.DedicatedMapperFacade#newObjectA(java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public A newObjectReverse(B source, MappingContext context) {
        if (objectFactoryA == null) {
            synchronized(this) {
                if (objectFactoryA == null) {
                    objectFactoryA = mapperFactory.lookupObjectFactory(aType, bType);
                }
            }
        }
        return objectFactoryA.create(source, context);
    }
    
    /**
     * BoundStrategyCache attempts to optimize caching of MappingStrategies for a particular
     * situation based on the assumption that the most common case involves mapping with a single
     * source type class (no polymorphism within most BoundMapperFacades); it accomplishes this
     * by caching a single MappingStrategy as a default case which is always fast at hand, falling
     * back to a (small) hashmap of backup strategies, keyed by source Class (since all of the other
     * inputs to resolve the strategy are fixed to the BoundStrategyCache instance).
     * 
     * @author matt.deboer@gmail.com
     *
     */
    private static class BoundStrategyCache {
        private final Type<?> aType;
        private final Type<?> bType;
        private final boolean inPlace;
        private final MapperFacade mapperFacade;
        private final UnenhanceStrategy unenhanceStrategy;
        protected final ConcurrentHashMap<Class<?>, MappingStrategy> strategies = new ConcurrentHashMap<Class<?>, MappingStrategy>(2);
        
        private volatile Class<?> idClass;
        private volatile MappingStrategy defaultStrategy;
        
        private BoundStrategyCache(Type<?> aType, Type<?> bType, MapperFacade mapperFacade, UnenhanceStrategy unenhanceStrategy, boolean inPlace) {
            this.aType = aType;
            this.bType = bType;
            this.mapperFacade = mapperFacade;
            this.unenhanceStrategy = unenhanceStrategy;
            this.inPlace = inPlace;
        }
        
        public MappingStrategy getStrategy(Object sourceObject, MappingContext context) {
            MappingStrategy strategy = null;
            Class<?> sourceClass = getClass(sourceObject);
            if (defaultStrategy != null && sourceClass.equals(idClass)) {
                strategy = defaultStrategy;
            } else if (defaultStrategy == null) {
                synchronized(this) {
                    if (defaultStrategy == null) {
                        defaultStrategy = mapperFacade.resolveMappingStrategy(sourceObject, aType, bType, inPlace, context);
                        idClass = sourceClass;
                        strategies.put(idClass, defaultStrategy);
                    }
                }
                strategy = defaultStrategy;
            } else {
                strategy = strategies.get(sourceClass);
                if (strategy == null) {
                    strategy = mapperFacade.resolveMappingStrategy(sourceObject, aType, bType, inPlace, context);
                    strategies.put(sourceClass, strategy);
                }
            }
            
            /*
             * Set the resolved types on the current mapping context; this can be used
             * by downstream Mappers to determine the originally resolved types
             */
            context.setResolvedSourceType(strategy.getAType());
            context.setResolvedDestinationType(strategy.getBType());
            
            return strategy;
        }
        
        protected Class<?> getClass(Object object) {
            if (this.unenhanceStrategy == null) {
                return object.getClass();
            } else {
                return unenhanceStrategy.unenhanceObject(object, TypeFactory.TYPE_OF_OBJECT).getClass();
            }
        }
    }
}
