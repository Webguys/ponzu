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

package com.webguys.ponzu.impl.bag.immutable;

import com.webguys.ponzu.api.bag.ImmutableBag;
import com.webguys.ponzu.api.factory.bag.ImmutableBagFactory;
import com.webguys.ponzu.impl.utility.Iterate;

public final class ImmutableBagFactoryImpl implements ImmutableBagFactory
{
    public <T> ImmutableBag<T> of()
    {
        return (ImmutableBag<T>) ImmutableEmptyBag.INSTANCE;
    }

    public <T> ImmutableBag<T> of(T element)
    {
        return new ImmutableSingletonBag<T>(element);
    }

    public <T> ImmutableBag<T> of(T... elements)
    {
        if (elements == null || elements.length == 0)
        {
            return this.of();
        }
        if (elements.length == 1)
        {
            return this.of(elements[0]);
        }
        if (elements.length < ImmutableArrayBag.MAXIMUM_USEFUL_ARRAY_BAG_SIZE)
        {
            return ImmutableArrayBag.newBagWith(elements);
        }
        return ImmutableHashBag.newBagWith(elements);
    }

    public <T> ImmutableBag<T> ofAll(Iterable<? extends T> items)
    {
        if (items instanceof ImmutableBag<?>)
        {
            return (ImmutableBag<T>) items;
        }

        return this.of((T[]) Iterate.toArray(items));
    }
}
