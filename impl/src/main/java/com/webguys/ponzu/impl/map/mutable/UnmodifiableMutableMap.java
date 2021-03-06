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

package com.webguys.ponzu.impl.map.mutable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import com.webguys.ponzu.api.LazyIterable;
import com.webguys.ponzu.api.RichIterable;
import com.webguys.ponzu.api.bag.MutableBag;
import com.webguys.ponzu.api.block.function.Function;
import com.webguys.ponzu.api.block.function.Function2;
import com.webguys.ponzu.api.block.function.Generator;
import com.webguys.ponzu.api.block.function.primitive.DoubleObjectToDoubleFunction;
import com.webguys.ponzu.api.block.function.primitive.IntObjectToIntFunction;
import com.webguys.ponzu.api.block.function.primitive.LongObjectToLongFunction;
import com.webguys.ponzu.api.block.predicate.Predicate;
import com.webguys.ponzu.api.block.predicate.Predicate2;
import com.webguys.ponzu.api.block.procedure.ObjectIntProcedure;
import com.webguys.ponzu.api.block.procedure.Procedure;
import com.webguys.ponzu.api.block.procedure.Procedure2;
import com.webguys.ponzu.api.collection.MutableCollection;
import com.webguys.ponzu.api.list.MutableList;
import com.webguys.ponzu.api.map.ImmutableMap;
import com.webguys.ponzu.api.map.MutableMap;
import com.webguys.ponzu.api.map.sorted.MutableSortedMap;
import com.webguys.ponzu.api.multimap.MutableMultimap;
import com.webguys.ponzu.api.partition.PartitionMutableCollection;
import com.webguys.ponzu.api.set.MutableSet;
import com.webguys.ponzu.api.set.sorted.MutableSortedSet;
import com.webguys.ponzu.api.tuple.Pair;
import com.webguys.ponzu.impl.UnmodifiableIteratorAdapter;
import com.webguys.ponzu.impl.UnmodifiableMap;
import com.webguys.ponzu.impl.factory.Maps;
import com.webguys.ponzu.impl.tuple.AbstractImmutableEntry;
import com.webguys.ponzu.impl.utility.LazyIterate;

/**
 * An unmodifiable view of a map.
 *
 * @see MutableMap#asUnmodifiable()
 */
public class UnmodifiableMutableMap<K, V>
        extends UnmodifiableMap<K, V>
        implements MutableMap<K, V>
{
    private static final long serialVersionUID = 1L;

    protected UnmodifiableMutableMap(MutableMap<K, V> map)
    {
        super(map);
    }

    /**
     * This method will take a MutableMap and wrap it directly in a UnmodifiableMutableMap.  It will
     * take any other non-GS-map and first adapt it will a MapAdapter, and then return a
     * UnmodifiableMutableMap that wraps the adapter.
     */
    public static <K, V, M extends Map<K, V>> UnmodifiableMutableMap<K, V> of(M map)
    {
        if (map == null)
        {
            throw new IllegalArgumentException("cannot create a UnmodifiableMutableMap for null");
        }
        return new UnmodifiableMutableMap<K, V>(MapAdapter.adapt(map));
    }

    public MutableMap<K, V> newEmpty()
    {
        return this.getMutableMap().newEmpty();
    }

    public boolean notEmpty()
    {
        return this.getMutableMap().notEmpty();
    }

    public void forEachValue(Procedure<? super V> procedure)
    {
        this.getMutableMap().forEachValue(procedure);
    }

    public void forEachKey(Procedure<? super K> procedure)
    {
        this.getMutableMap().forEachKey(procedure);
    }

    public void forEachKeyValue(Procedure2<? super K, ? super V> procedure)
    {
        this.getMutableMap().forEachKeyValue(procedure);
    }

    public <E> MutableMap<K, V> transformKeysAndValues(
            Collection<E> collection,
            Function<? super E, ? extends K> keyFunction,
            Function<? super E, ? extends V> valueFunction)
    {
        throw new UnsupportedOperationException();
    }

    public V removeKey(K key)
    {
        throw new UnsupportedOperationException();
    }

    public V getIfAbsentPut(K key, Generator<? extends V> function)
    {
        V result = this.get(key);
        if (this.isAbsent(result, key))
        {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    public <P> V getIfAbsentPutWith(
            K key,
            Function<? super P, ? extends V> function,
            P parameter)
    {
        V result = this.get(key);
        if (this.isAbsent(result, key))
        {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    public V getIfAbsent(K key, Generator<? extends V> function)
    {
        V result = this.get(key);
        if (this.isAbsent(result, key))
        {
            return function.value();
        }
        return result;
    }

    public <P> V getIfAbsentWith(
            K key,
            Function<? super P, ? extends V> function,
            P parameter)
    {
        V result = this.get(key);
        if (this.isAbsent(result, key))
        {
            return function.valueOf(parameter);
        }
        return result;
    }

    private boolean isAbsent(V result, K key)
    {
        return result == null && !this.containsKey(key);
    }

    public <A> A ifPresentApply(K key, Function<? super V, ? extends A> function)
    {
        return this.getMutableMap().ifPresentApply(key, function);
    }

    public MutableMap<K, V> withKeyValue(K key, V value)
    {
        throw new UnsupportedOperationException("Cannot call withKeyValue() on " + this.getClass().getSimpleName());
    }

    public MutableMap<K, V> withAllKeyValues(Iterable<? extends Pair<? extends K, ? extends V>> keyValues)
    {
        throw new UnsupportedOperationException("Cannot call withAllKeyValues() on " + this.getClass().getSimpleName());
    }

    public MutableMap<K, V> withAllKeyValueArguments(Pair<? extends K, ? extends V>... keyValuePairs)
    {
        throw new UnsupportedOperationException("Cannot call withAllKeyValueArguments() on " + this.getClass().getSimpleName());
    }

    public MutableMap<K, V> withoutKey(K key)
    {
        throw new UnsupportedOperationException("Cannot call withoutKey() on " + this.getClass().getSimpleName());
    }

    public MutableMap<K, V> withoutAllKeys(Iterable<? extends K> keys)
    {
        throw new UnsupportedOperationException("Cannot call withoutAllKeys() on " + this.getClass().getSimpleName());
    }

    @Override
    public MutableMap<K, V> clone()
    {
        return this;
    }

    public MutableMap<K, V> asUnmodifiable()
    {
        return this;
    }

    public MutableMap<K, V> asSynchronized()
    {
        return SynchronizedMutableMap.of(this);
    }

    public void forEach(Procedure<? super V> procedure)
    {
        this.getMutableMap().forEach(procedure);
    }

    public void forEachWithIndex(ObjectIntProcedure<? super V> objectIntProcedure)
    {
        this.getMutableMap().forEachWithIndex(objectIntProcedure);
    }

    public <P> void forEachWith(Procedure2<? super V, ? super P> procedure, P parameter)
    {
        this.getMutableMap().forEachWith(procedure, parameter);
    }

    public Iterator<V> iterator()
    {
        return new UnmodifiableIteratorAdapter<V>(this.getMutableMap().iterator());
    }

    @Override
    public int hashCode()
    {
        return this.getMutableMap().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.getMutableMap().equals(obj);
    }

    protected MutableMap<K, V> getMutableMap()
    {
        return (MutableMap<K, V>) this.delegate;
    }

    public RichIterable<K> keysView()
    {
        return LazyIterate.adapt(this.keySet());
    }

    public RichIterable<V> valuesView()
    {
        return LazyIterate.adapt(this.values());
    }

    public RichIterable<Pair<K, V>> keyValuesView()
    {
        return LazyIterate.adapt(this.entrySet()).transform(AbstractImmutableEntry.<K, V>getPairFunction());
    }

    public ImmutableMap<K, V> toImmutable()
    {
        return Maps.immutable.ofMap(this);
    }

    public <R> MutableMap<K, R> transformValues(Function2<? super K, ? super V, ? extends R> function)
    {
        return this.getMutableMap().transformValues(function);
    }

    public <K2, V2> MutableMap<K2, V2> transform(Function2<? super K, ? super V, Pair<K2, V2>> function)
    {
        return this.getMutableMap().transform(function);
    }

    public MutableMap<K, V> filter(Predicate2<? super K, ? super V> predicate)
    {
        return this.getMutableMap().filter(predicate);
    }

    public MutableMap<K, V> filterNot(Predicate2<? super K, ? super V> predicate)
    {
        return this.getMutableMap().filterNot(predicate);
    }

    public Pair<K, V> find(Predicate2<? super K, ? super V> predicate)
    {
        return this.getMutableMap().find(predicate);
    }

    public boolean allSatisfy(Predicate<? super V> predicate)
    {
        return this.getMutableMap().allSatisfy(predicate);
    }

    public boolean anySatisfy(Predicate<? super V> predicate)
    {
        return this.getMutableMap().anySatisfy(predicate);
    }

    public void appendString(Appendable appendable)
    {
        this.getMutableMap().appendString(appendable);
    }

    public void appendString(Appendable appendable, String separator)
    {
        this.getMutableMap().appendString(appendable, separator);
    }

    public void appendString(Appendable appendable, String start, String separator, String end)
    {
        this.getMutableMap().appendString(appendable, start, separator, end);
    }

    public MutableBag<V> toBag()
    {
        return this.getMutableMap().toBag();
    }

    public LazyIterable<V> asLazy()
    {
        return this.getMutableMap().asLazy();
    }

    public MutableList<V> toList()
    {
        return this.getMutableMap().toList();
    }

    public <NK, NV> MutableMap<NK, NV> toMap(
            Function<? super V, ? extends NK> keyFunction,
            Function<? super V, ? extends NV> valueFunction)
    {
        return this.getMutableMap().toMap(keyFunction, valueFunction);
    }

    public <NK, NV> MutableSortedMap<NK, NV> toSortedMap(
            Function<? super V, ? extends NK> keyFunction,
            Function<? super V, ? extends NV> valueFunction)
    {
        return this.getMutableMap().toSortedMap(keyFunction, valueFunction);
    }

    public <NK, NV> MutableSortedMap<NK, NV> toSortedMap(
            Comparator<? super NK> comparator,
            Function<? super V, ? extends NK> keyFunction,
            Function<? super V, ? extends NV> valueFunction)
    {
        return this.getMutableMap().toSortedMap(comparator, keyFunction, valueFunction);
    }

    public MutableSet<V> toSet()
    {
        return this.getMutableMap().toSet();
    }

    public MutableList<V> toSortedList()
    {
        return this.getMutableMap().toSortedList();
    }

    public MutableList<V> toSortedList(Comparator<? super V> comparator)
    {
        return this.getMutableMap().toSortedList(comparator);
    }

    public <R extends Comparable<? super R>> MutableList<V> toSortedListBy(Function<? super V, ? extends R> function)
    {
        return this.getMutableMap().toSortedListBy(function);
    }

    public MutableSortedSet<V> toSortedSet()
    {
        return this.getMutableMap().toSortedSet();
    }

    public MutableSortedSet<V> toSortedSet(Comparator<? super V> comparator)
    {
        return this.getMutableMap().toSortedSet(comparator);
    }

    public <R extends Comparable<? super R>> MutableSortedSet<V> toSortedSetBy(Function<? super V, ? extends R> function)
    {
        return this.getMutableMap().toSortedSetBy(function);
    }

    public RichIterable<RichIterable<V>> chunk(int size)
    {
        return this.getMutableMap().chunk(size);
    }

    public <R, C extends Collection<R>> C transform(Function<? super V, ? extends R> function, C target)
    {
        return this.getMutableMap().transform(function, target);
    }

    public <R, C extends Collection<R>> C transformIf(Predicate<? super V> predicate, Function<? super V, ? extends R> function, C target)
    {
        return this.getMutableMap().transformIf(predicate, function, target);
    }

    public <P, R, C extends Collection<R>> C transformWith(Function2<? super V, ? super P, ? extends R> function, P parameter, C targetCollection)
    {
        return this.getMutableMap().transformWith(function, parameter, targetCollection);
    }

    public boolean contains(Object object)
    {
        return this.containsValue(object);
    }

    public boolean containsAllArguments(Object... elements)
    {
        return this.getMutableMap().containsAllArguments(elements);
    }

    public boolean containsAllIterable(Iterable<?> source)
    {
        return this.getMutableMap().containsAllIterable(source);
    }

    public boolean containsAll(Collection<?> source)
    {
        return this.containsAllIterable(source);
    }

    public int count(Predicate<? super V> predicate)
    {
        return this.getMutableMap().count(predicate);
    }

    public V find(Predicate<? super V> predicate)
    {
        return this.getMutableMap().find(predicate);
    }

    public V findIfNone(Predicate<? super V> predicate, Generator<? extends V> function)
    {
        return this.getMutableMap().findIfNone(predicate, function);
    }

    public <R, C extends Collection<R>> C flatTransform(Function<? super V, ? extends Iterable<R>> function, C target)
    {
        return this.getMutableMap().flatTransform(function, target);
    }

    public V getFirst()
    {
        return this.getMutableMap().getFirst();
    }

    public V getLast()
    {
        return this.getMutableMap().getLast();
    }

    public <R> MutableMultimap<R, V> groupBy(Function<? super V, ? extends R> function)
    {
        return this.getMutableMap().groupBy(function);
    }

    public <R, C extends MutableMultimap<R, V>> C groupBy(Function<? super V, ? extends R> function, C target)
    {
        return this.getMutableMap().groupBy(function, target);
    }

    public <R> MutableMultimap<R, V> groupByEach(Function<? super V, ? extends Iterable<R>> function)
    {
        return this.getMutableMap().groupByEach(function);
    }

    public <R, C extends MutableMultimap<R, V>> C groupByEach(Function<? super V, ? extends Iterable<R>> function, C target)
    {
        return this.getMutableMap().groupByEach(function, target);
    }

    public <IV> IV foldLeft(IV initialValue, Function2<? super IV, ? super V, ? extends IV> function)
    {
        return this.getMutableMap().foldLeft(initialValue, function);
    }

    public int foldLeft(int initialValue, IntObjectToIntFunction<? super V> function)
    {
        return this.getMutableMap().foldLeft(initialValue, function);
    }

    public long foldLeft(long initialValue, LongObjectToLongFunction<? super V> function)
    {
        return this.getMutableMap().foldLeft(initialValue, function);
    }

    public double foldLeft(double initialValue, DoubleObjectToDoubleFunction<? super V> function)
    {
        return this.getMutableMap().foldLeft(initialValue, function);
    }

    public String makeString()
    {
        return this.getMutableMap().makeString();
    }

    public String makeString(String separator)
    {
        return this.getMutableMap().makeString(separator);
    }

    public String makeString(String start, String separator, String end)
    {
        return this.getMutableMap().makeString(start, separator, end);
    }

    public V max()
    {
        return this.getMutableMap().max();
    }

    public V max(Comparator<? super V> comparator)
    {
        return this.getMutableMap().max(comparator);
    }

    public <R extends Comparable<? super R>> V maxBy(Function<? super V, ? extends R> function)
    {
        return this.getMutableMap().maxBy(function);
    }

    public V min()
    {
        return this.getMutableMap().min();
    }

    public V min(Comparator<? super V> comparator)
    {
        return this.getMutableMap().min(comparator);
    }

    public <R extends Comparable<? super R>> V minBy(Function<? super V, ? extends R> function)
    {
        return this.getMutableMap().minBy(function);
    }

    public <R extends Collection<V>> R filterNot(Predicate<? super V> predicate, R target)
    {
        return this.getMutableMap().filterNot(predicate, target);
    }

    public <P, R extends Collection<V>> R filterNotWith(Predicate2<? super V, ? super P> predicate, P parameter, R targetCollection)
    {
        return this.getMutableMap().filterNotWith(predicate, parameter, targetCollection);
    }

    public <R extends Collection<V>> R filter(Predicate<? super V> predicate, R target)
    {
        return this.getMutableMap().filter(predicate, target);
    }

    public <P, R extends Collection<V>> R filterWith(Predicate2<? super V, ? super P> predicate, P parameter, R targetCollection)
    {
        return this.getMutableMap().filterWith(predicate, parameter, targetCollection);
    }

    public Object[] toArray()
    {
        return this.getMutableMap().toArray();
    }

    public <T> T[] toArray(T[] a)
    {
        return this.getMutableMap().toArray(a);
    }

    public <S, R extends Collection<Pair<V, S>>> R zip(Iterable<S> that, R target)
    {
        return this.getMutableMap().zip(that, target);
    }

    public <R extends Collection<Pair<V, Integer>>> R zipWithIndex(R target)
    {
        return this.getMutableMap().zipWithIndex(target);
    }

    public <R> MutableCollection<R> transform(Function<? super V, ? extends R> function)
    {
        return this.getMutableMap().transform(function).toList();
    }

    public <R> MutableCollection<R> transformIf(Predicate<? super V> predicate, Function<? super V, ? extends R> function)
    {
        return this.getMutableMap().transformIf(predicate, function).toList();
    }

    public <R> MutableCollection<R> flatTransform(Function<? super V, ? extends Iterable<R>> function)
    {
        return this.getMutableMap().flatTransform(function).toList();
    }

    public MutableCollection<V> filterNot(Predicate<? super V> predicate)
    {
        return this.getMutableMap().filterNot(predicate).toList();
    }

    public MutableCollection<V> filter(Predicate<? super V> predicate)
    {
        return this.getMutableMap().filter(predicate).toList();
    }

    public PartitionMutableCollection<V> partition(Predicate<? super V> predicate)
    {
        return this.getMutableMap().partition(predicate);
    }

    public <S> MutableCollection<Pair<V, S>> zip(Iterable<S> that)
    {
        return this.getMutableMap().zip(that);
    }

    public MutableCollection<Pair<V, Integer>> zipWithIndex()
    {
        return this.getMutableMap().zipWithIndex();
    }
}
