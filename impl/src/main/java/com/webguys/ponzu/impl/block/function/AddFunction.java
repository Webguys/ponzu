/*
 * Copyright 2011 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webguys.ponzu.impl.block.function;

import java.util.Collection;

import com.webguys.ponzu.api.block.function.Function2;
import com.webguys.ponzu.api.block.function.primitive.DoubleObjectToDoubleFunction;
import com.webguys.ponzu.api.block.function.primitive.IntObjectToIntFunction;
import com.webguys.ponzu.api.block.function.primitive.LongObjectToLongFunction;

/**
 * Provides static Function2s which can be used by Iterate.foldLeft for adding primitives or to a collection
 */
public final class AddFunction
{
    public static final IntObjectToIntFunction<Integer> INTEGER_TO_INT = new AddIntegerToIntFunction();
    public static final LongObjectToLongFunction<Integer> INTEGER_TO_LONG = new AddIntegerToLongFunction();
    public static final DoubleObjectToDoubleFunction<Integer> INTEGER_TO_DOUBLE = new AddIntegerToDoubleFunction();
    public static final DoubleObjectToDoubleFunction<Double> DOUBLE_TO_DOUBLE = new AddDoubleToDoubleFunction();
    public static final Function2<Integer, Integer, Integer> INTEGER = new AddIntegerFunction();
    public static final Function2<Double, Double, Double> DOUBLE = new AddDoubleFunction();
    public static final Function2<Long, Long, Long> LONG = new AddLongFunction();
    public static final Function2<String, String, String> STRING = new AddStringFunction();
    public static final Function2<Collection<?>, ?, Collection<?>> COLLECTION = new AddCollectionFunction();

    private AddFunction()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    private static class AddIntegerToIntFunction implements IntObjectToIntFunction<Integer>
    {
        private static final long serialVersionUID = 1L;

        public int intValueOf(int intParameter, Integer objectParameter)
        {
            return intParameter + objectParameter;
        }
    }

    private static class AddIntegerToLongFunction implements LongObjectToLongFunction<Integer>
    {
        private static final long serialVersionUID = 1L;

        public long longValueOf(long longParameter, Integer objectParameter)
        {
            return longParameter + objectParameter.longValue();
        }
    }

    private static class AddIntegerToDoubleFunction implements DoubleObjectToDoubleFunction<Integer>
    {
        private static final long serialVersionUID = 1L;

        public double doubleValueOf(double doubleParameter, Integer objectParameter)
        {
            return doubleParameter + objectParameter;
        }
    }

    private static class AddDoubleToDoubleFunction implements DoubleObjectToDoubleFunction<Double>
    {
        private static final long serialVersionUID = 1L;

        public double doubleValueOf(double doubleParameter, Double objectParameter)
        {
            return doubleParameter + objectParameter;
        }
    }

    private static class AddIntegerFunction implements Function2<Integer, Integer, Integer>
    {
        private static final long serialVersionUID = 1L;

        public Integer value(Integer argument1, Integer argument2)
        {
            return argument1 + argument2;
        }
    }

    private static class AddDoubleFunction implements Function2<Double, Double, Double>
    {
        private static final long serialVersionUID = 1L;

        public Double value(Double argument1, Double argument2)
        {
            return argument1 + argument2;
        }
    }

    private static class AddLongFunction implements Function2<Long, Long, Long>
    {
        private static final long serialVersionUID = 1L;

        public Long value(Long argument1, Long argument2)
        {
            return argument1 + argument2;
        }
    }

    private static class AddStringFunction implements Function2<String, String, String>
    {
        private static final long serialVersionUID = 1L;

        public String value(String argument1, String argument2)
        {
            if (argument1 != null && argument2 != null)
            {
                return argument1 + argument2;
            }

            return argument1 == null ? argument2 : argument1;
        }
    }

    private static class AddCollectionFunction<T> implements Function2<Collection<T>, T, Collection<T>>
    {
        private static final long serialVersionUID = 1L;

        public Collection<T> value(Collection<T> collection, T addElement)
        {
            collection.add(addElement);
            return collection;
        }
    }
}
