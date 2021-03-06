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

package com.webguys.ponzu.impl.factory;

import com.webguys.ponzu.api.factory.map.strategy.ImmutableHashingStrategyMapFactory;
import com.webguys.ponzu.api.factory.map.strategy.MutableHashingStrategyMapFactory;
import com.webguys.ponzu.impl.map.strategy.immutable.ImmutableHashingStrategyMapFactoryImpl;
import com.webguys.ponzu.impl.map.strategy.mutable.MutableHashingStrategyMapFactoryImpl;

@SuppressWarnings("ConstantNamingConvention")
public final class HashingStrategyMaps
{
    public static final ImmutableHashingStrategyMapFactory immutable = new ImmutableHashingStrategyMapFactoryImpl();
    public static final MutableHashingStrategyMapFactory mutable = new MutableHashingStrategyMapFactoryImpl();

    private HashingStrategyMaps()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }
}
