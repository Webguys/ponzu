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

package com.webguys.ponzu.impl.parallel;

import java.util.Collection;

import com.webguys.ponzu.impl.block.procedure.FilterNotProcedure;

/**
 * Combines the results of a Collection of SelectBlocks which each hold onto a negative filtered collection of results.
 */
public final class FilterNotProcedureCombiner<T>
        extends AbstractPredicateBasedCombiner<T, FilterNotProcedure<T>>
{
    private static final long serialVersionUID = 1L;

    public FilterNotProcedureCombiner(
            Iterable<T> sourceCollection,
            Collection<T> targetCollection,
            int initialCapacity,
            boolean combineOne)
    {
        super(combineOne, sourceCollection, initialCapacity, targetCollection);
    }

    public void combineOne(FilterNotProcedure<T> procedureCombine)
    {
        this.result.addAll(procedureCombine.getCollection());
    }
}
