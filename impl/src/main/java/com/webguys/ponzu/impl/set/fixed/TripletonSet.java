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

package com.webguys.ponzu.impl.set.fixed;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.webguys.ponzu.api.block.procedure.ObjectIntProcedure;
import com.webguys.ponzu.api.block.procedure.Procedure;
import com.webguys.ponzu.api.block.procedure.Procedure2;
import com.webguys.ponzu.api.set.MutableSet;
import com.webguys.ponzu.impl.block.factory.Comparators;
import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
final class TripletonSet<T>
        extends AbstractMemoryEfficientMutableSet<T>
        implements Externalizable
{
    private static final long serialVersionUID = 1L;

    private T element1;
    private T element2;
    private T element3;

    @SuppressWarnings("UnusedDeclaration")
    public TripletonSet()
    {
        // For Externalizable use only
    }

    TripletonSet(T obj1, T obj2, T obj3)
    {
        this.element1 = obj1;
        this.element2 = obj2;
        this.element3 = obj3;
    }

    @Override
    public int size()
    {
        return 3;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof Set))
        {
            return false;
        }
        Collection<?> collection = (Collection<?>) o;
        return collection.size() == this.size()
                && collection.contains(this.element1)
                && collection.contains(this.element2)
                && collection.contains(this.element3);
    }

    @Override
    public int hashCode()
    {
        return this.nullSafeHashCode(this.element1)
                + this.nullSafeHashCode(this.element2)
                + this.nullSafeHashCode(this.element3);
    }

    // Weird implementation of clone() is ok on final classes
    @Override
    public TripletonSet<T> clone()
    {
        return new TripletonSet<T>(this.element1, this.element2, this.element3);
    }

    @Override
    public boolean contains(Object obj)
    {
        return Comparators.nullSafeEquals(obj, this.element1)
                || Comparators.nullSafeEquals(obj, this.element2)
                || Comparators.nullSafeEquals(obj, this.element3);
    }

    @Override
    public Iterator<T> iterator()
    {
        return new TripletonSetIterator();
    }

    @Override
    public T getFirst()
    {
        return this.element1;
    }

    T getSecond()
    {
        return this.element2;
    }

    @Override
    public T getLast()
    {
        return this.element3;
    }

    @Override
    public void forEach(Procedure<? super T> procedure)
    {
        procedure.value(this.element1);
        procedure.value(this.element2);
        procedure.value(this.element3);
    }

    @Override
    public void forEachWithIndex(ObjectIntProcedure<? super T> objectIntProcedure)
    {
        objectIntProcedure.value(this.element1, 0);
        objectIntProcedure.value(this.element2, 1);
        objectIntProcedure.value(this.element3, 2);
    }

    @Override
    public <P> void forEachWith(Procedure2<? super T, ? super P> procedure, P parameter)
    {
        procedure.value(this.element1, parameter);
        procedure.value(this.element2, parameter);
        procedure.value(this.element3, parameter);
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(this.element1);
        out.writeObject(this.element2);
        out.writeObject(this.element3);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        this.element1 = (T) in.readObject();
        this.element2 = (T) in.readObject();
        this.element3 = (T) in.readObject();
    }

    private class TripletonSetIterator
            extends MemoryEfficientSetIterator
    {
        @Override
        protected T getElement(int i)
        {
            if (i == 0)
            {
                return TripletonSet.this.element1;
            }
            if (i == 1)
            {
                return TripletonSet.this.element2;
            }
            if (i == 2)
            {
                return TripletonSet.this.element3;
            }
            throw new NoSuchElementException("i=" + i);
        }
    }

    public MutableSet<T> with(T element)
    {
        return this.contains(element) ? this : new QuadrupletonSet<T>(this.element1, this.element2, this.element3, element);
    }

    public MutableSet<T> without(T element)
    {
        if (Comparators.nullSafeEquals(element, this.element1))
        {
            return new DoubletonSet<T>(this.element2, this.element3);
        }
        if (Comparators.nullSafeEquals(element, this.element2))
        {
            return new DoubletonSet<T>(this.element1, this.element3);
        }
        if (Comparators.nullSafeEquals(element, this.element3))
        {
            return new DoubletonSet<T>(this.element1, this.element2);
        }
        return this;
    }
}
