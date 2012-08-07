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

package ponzu.impl.parallel;

import java.util.Collection;

import ponzu.impl.block.procedure.FilterProcedure;

/**
 * Combines the results of a Collection of SelectBlocks which each hold onto a filtered collection of results.
 */
public final class FilterProcedureCombiner<T>
        extends AbstractPredicateBasedCombiner<T, FilterProcedure<T>>
{
    private static final long serialVersionUID = 1L;

    public FilterProcedureCombiner(
            Iterable<T> sourceCollection,
            Collection<T> targetCollection,
            int initialCapacity,
            boolean combineOne)
    {
        super(combineOne, sourceCollection, initialCapacity, targetCollection);
    }

    public void combineOne(FilterProcedure<T> procedureCombine)
    {
        this.result.addAll(procedureCombine.getCollection());
    }
}