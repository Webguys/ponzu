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

package com.webguys.ponzu.impl.utility.internal;

import com.webguys.ponzu.api.block.procedure.ObjectIntProcedure;
import com.webguys.ponzu.api.block.procedure.Procedure;

public final class InternalArrayIterate
{
    private InternalArrayIterate()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static <T> void forEachWithoutChecks(T[] objectArray, int from, int to, Procedure<? super T> procedure)
    {
        if (from <= to)
        {
            for (int i = from; i <= to; i++)
            {
                procedure.value(objectArray[i]);
            }
        }
        else
        {
            for (int i = from; i >= to; i--)
            {
                procedure.value(objectArray[i]);
            }
        }
    }

    public static <T> void forEachWithIndexWithoutChecks(T[] objectArray, int from, int to, ObjectIntProcedure<? super T> objectIntProcedure)
    {
        if (from <= to)
        {
            for (int i = from; i <= to; i++)
            {
                objectIntProcedure.value(objectArray[i], i);
            }
        }
        else
        {
            for (int i = from; i >= to; i--)
            {
                objectIntProcedure.value(objectArray[i], i);
            }
        }
    }
}
