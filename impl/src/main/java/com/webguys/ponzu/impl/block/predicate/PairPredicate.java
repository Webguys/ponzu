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

package com.webguys.ponzu.impl.block.predicate;

import com.webguys.ponzu.api.block.predicate.Predicate;
import com.webguys.ponzu.api.block.predicate.Predicate2;
import com.webguys.ponzu.api.tuple.Pair;

public abstract class PairPredicate<T1, T2>
        implements Predicate<Pair<T1, T2>>, Predicate2<T1, T2>
{
    private static final long serialVersionUID = 1L;

    public boolean accept(Pair<T1, T2> pair)
    {
        return this.accept(pair.getOne(), pair.getTwo());
    }
}
