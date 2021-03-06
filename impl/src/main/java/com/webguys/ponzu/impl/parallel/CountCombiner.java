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

import com.webguys.ponzu.impl.block.procedure.CountProcedure;

/**
 * Combines the results of a Collection of CountBlocks which each hold onto a filtered sum (count where) result.
 */
public final class CountCombiner<T>
        extends AbstractProcedureCombiner<CountProcedure<T>>
{
    private static final long serialVersionUID = 1L;
    private int count = 0;

    public CountCombiner()
    {
        super(true);
    }

    public void combineOne(CountProcedure<T> procedure)
    {
        this.count += procedure.getCount();
    }

    public int getCount()
    {
        return this.count;
    }
}
