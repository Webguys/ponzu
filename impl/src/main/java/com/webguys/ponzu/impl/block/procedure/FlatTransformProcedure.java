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

package com.webguys.ponzu.impl.block.procedure;

import java.util.Collection;

import com.webguys.ponzu.api.block.function.Function;
import com.webguys.ponzu.api.block.procedure.Procedure;
import com.webguys.ponzu.impl.utility.Iterate;

/**
 * Applies a function to an object and adds the result to a target collection.
 */
public final class FlatTransformProcedure<T, V> implements Procedure<T>
{
    private static final long serialVersionUID = 1L;

    private final Function<? super T, ? extends Iterable<V>> function;
    private final Collection<V> collection;

    public FlatTransformProcedure(
            Function<? super T, ? extends Iterable<V>> function,
            Collection<V> targetCollection)
    {
        this.function = function;
        this.collection = targetCollection;
    }

    @Override
    public void value(T object)
    {
        Iterate.addAllIterable(this.function.valueOf(object), this.collection);
    }

    public Collection<V> getCollection()
    {
        return this.collection;
    }
}
