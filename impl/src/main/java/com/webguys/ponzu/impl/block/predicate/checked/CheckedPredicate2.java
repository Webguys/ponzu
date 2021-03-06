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

package com.webguys.ponzu.impl.block.predicate.checked;

import com.webguys.ponzu.api.block.predicate.Predicate2;

public abstract class CheckedPredicate2<T, P>
        implements Predicate2<T, P>
{
    private static final long serialVersionUID = 1L;

    public boolean accept(T item, P param)
    {
        try
        {
            return this.safeAccept(item, param);
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Checked exception caught in Predicate", e);
        }
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    public abstract boolean safeAccept(T object, P param) throws Exception;
}
