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

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.webguys.ponzu.api.LazyIterable;
import com.webguys.ponzu.api.block.function.Function;
import com.webguys.ponzu.api.block.function.Function2;
import com.webguys.ponzu.api.block.predicate.Predicate;
import com.webguys.ponzu.api.block.procedure.Procedure2;
import com.webguys.ponzu.api.factory.set.FixedSizeSetFactory;
import com.webguys.ponzu.api.factory.set.ImmutableSetFactory;
import com.webguys.ponzu.api.factory.set.MutableSetFactory;
import com.webguys.ponzu.api.set.MutableSet;
import com.webguys.ponzu.api.tuple.Pair;
import com.webguys.ponzu.impl.block.factory.Comparators;
import com.webguys.ponzu.impl.set.fixed.FixedSizeSetFactoryImpl;
import com.webguys.ponzu.impl.set.immutable.ImmutableSetFactoryImpl;
import com.webguys.ponzu.impl.set.mutable.MutableSetFactoryImpl;
import com.webguys.ponzu.impl.set.mutable.SetAdapter;
import com.webguys.ponzu.impl.set.mutable.UnifiedSet;
import com.webguys.ponzu.impl.tuple.Tuples;
import com.webguys.ponzu.impl.utility.ArrayIterate;
import com.webguys.ponzu.impl.utility.Iterate;
import com.webguys.ponzu.impl.utility.LazyIterate;

/**
 * Set algebra operations.
 * <p/>
 * Most operations are non-destructive, i.e. no input sets are modified during execution.
 * The exception is operations ending in "Into." These accept the target collection of
 * the final calculation as the first parameter.
 * <p/>
 * Some effort is made to return a <tt>SortedSet</tt> if any input set is sorted, but
 * this is not guaranteed (e.g., this will not be the case for collections proxied by
 * Hibernate). When in doubt, specify the target collection explicitly with the "Into"
 * version.
 */
@SuppressWarnings("ConstantNamingConvention")
public final class Sets
{
    public static final ImmutableSetFactory immutable = new ImmutableSetFactoryImpl();
    public static final FixedSizeSetFactory fixedSize = new FixedSizeSetFactoryImpl();
    public static final MutableSetFactory mutable = new MutableSetFactoryImpl();

    private static final Predicate<Set<?>> INSTANCE_OF_SORTED_SET_PREDICATE = new Predicate<Set<?>>()
    {
        public boolean accept(Set<?> set)
        {
            return set instanceof SortedSet;
        }
    };

    private static final Predicate<Set<?>> HAS_NON_NULL_COMPARATOR = new Predicate<Set<?>>()
    {
        public boolean accept(Set<?> set)
        {
            return ((SortedSet<?>) set).comparator() != null;
        }
    };

    private Sets()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static <E> MutableSet<E> union(
            Set<? extends E> setA,
            Set<? extends E> setB)
    {
        return unionInto(newSet(setA, setB), setA, setB);
    }

    public static <E, R extends Set<E>> R unionInto(
            R targetSet,
            Set<? extends E> setA,
            Set<? extends E> setB)
    {
        return setA.size() > setB.size() ? fillSet(targetSet, Sets.<E, R>addAllProcedure(), setA, setB)
                : fillSet(targetSet, Sets.<E, R>addAllProcedure(), setB, setA);
    }

    public static <E> MutableSet<E> unionAll(Set<? extends E>... sets)
    {
        return unionAllInto(newSet(sets), sets);
    }

    public static <E, R extends Set<E>> R unionAllInto(
            R targetSet,
            Set<? extends E>... sets)
    {
        ArrayIterate.sort(sets, sets.length, Comparators.descendingCollectionSizeComparator());
        return fillSet(targetSet, Sets.<E, R>addAllProcedure(), sets);
    }

    public static <E> MutableSet<E> intersect(
            Set<? extends E> setA,
            Set<? extends E> setB)
    {
        return intersectInto(newSet(setA, setB), setA, setB);
    }

    public static <E, R extends Set<E>> R intersectInto(
            R targetSet,
            Set<? extends E> setA,
            Set<? extends E> setB)
    {
        return setA.size() < setB.size() ? fillSet(targetSet, Sets.<E, R>retainAllProcedure(), setA, setB)
                : fillSet(targetSet, Sets.<E, R>retainAllProcedure(), setB, setA);
    }

    public static <E> MutableSet<E> intersectAll(Set<? extends E>... sets)
    {
        return intersectAllInto(newSet(sets), sets);
    }

    public static <E, R extends Set<E>> R intersectAllInto(
            R targetSet,
            Set<? extends E>... sets)
    {
        ArrayIterate.sort(sets, sets.length, Comparators.ascendingCollectionSizeComparator());
        return fillSet(targetSet, Sets.<E, R>retainAllProcedure(), sets);
    }

    public static <E> MutableSet<E> difference(
            Set<? extends E> minuendSet,
            Set<? extends E> subtrahendSet)
    {
        return differenceInto(newSet(minuendSet, subtrahendSet), minuendSet, subtrahendSet);
    }

    public static <E, R extends Set<E>> R differenceInto(
            R targetSet,
            Set<? extends E> minuendSet,
            Set<? extends E> subtrahendSet)
    {
        return fillSet(targetSet, Sets.<E, R>removeAllProcedure(), minuendSet, subtrahendSet);
    }

    public static <E> MutableSet<E> differenceAll(Set<? extends E>... sets)
    {
        return differenceAllInto(newSet(sets), sets);
    }

    public static <E, R extends Set<E>> R differenceAllInto(
            R targetSet,
            Set<? extends E>... sets)
    {
        return fillSet(targetSet, Sets.<E, R>removeAllProcedure(), sets);
    }

    public static <E> MutableSet<E> symmetricDifference(
            Set<? extends E> setA,
            Set<? extends E> setB)
    {
        return symmetricDifferenceInto(newSet(setA, setB), setA, setB);
    }

    public static <E, R extends Set<E>> R symmetricDifferenceInto(
            R targetSet,
            Set<? extends E> setA,
            Set<? extends E> setB)
    {
        return unionInto(
                targetSet,
                differenceInto(newSet(setA, setB), setA, setB),
                differenceInto(newSet(setA, setB), setB, setA));
    }

    public static <E> boolean isSubsetOf(
            Set<? extends E> candidateSubset,
            Set<? extends E> candidateSuperset)
    {
        return candidateSubset.size() <= candidateSuperset.size()
                && candidateSuperset.containsAll(candidateSubset);
    }

    public static <E> boolean isProperSubsetOf(
            Set<? extends E> candidateSubset,
            Set<? extends E> candidateSuperset)
    {
        return candidateSubset.size() < candidateSuperset.size()
                && candidateSuperset.containsAll(candidateSubset);
    }

    private static <E> MutableSet<E> newSet(Set<? extends E>... sets)
    {
        Comparator<? super E> comparator = extractComparator(sets);
        if (comparator != null)
        {
            // TODO: this should return a SortedSetAdapter once implemented
            return SetAdapter.adapt(new TreeSet<E>(comparator));
        }
        return UnifiedSet.newSet();
    }

    private static <E> Comparator<? super E> extractComparator(Set<? extends E>... sets)
    {
        Collection<Set<? extends E>> sortedSetCollection = ArrayIterate.filter(sets, INSTANCE_OF_SORTED_SET_PREDICATE);
        if (sortedSetCollection.isEmpty())
        {
            return null;
        }
        SortedSet<E> sortedSetWithComparator = (SortedSet<E>) Iterate.find(sortedSetCollection, HAS_NON_NULL_COMPARATOR);
        if (sortedSetWithComparator != null)
        {
            return sortedSetWithComparator.comparator();
        }
        return Comparators.safeNullsLow(Comparators.naturalOrder());
    }

    private static <E, R extends Set<E>> R fillSet(
            R targetSet,
            Procedure2<Set<? extends E>, R> procedure,
            Set<? extends E>... sets)
    {
        targetSet.addAll(sets[0]);
        for (int i = 1; i < sets.length; i++)
        {
            procedure.value(sets[i], targetSet);
        }
        return targetSet;
    }

    private static <E, R extends Set<E>> Procedure2<Set<? extends E>, R> addAllProcedure()
    {
        return new Procedure2<Set<? extends E>, R>()
        {
            public void value(Set<? extends E> argumentSet, R targetSet)
            {
                targetSet.addAll(argumentSet);
            }
        };
    }

    private static <E, R extends Set<E>> Procedure2<Set<? extends E>, R> retainAllProcedure()
    {
        return new Procedure2<Set<? extends E>, R>()
        {
            public void value(Set<? extends E> argumentSet, R targetSet)
            {
                targetSet.retainAll(argumentSet);
            }
        };
    }

    private static <E, R extends Set<E>> Procedure2<Set<? extends E>, R> removeAllProcedure()
    {
        return new Procedure2<Set<? extends E>, R>()
        {
            public void value(Set<? extends E> argumentSet, R targetSet)
            {
                targetSet.removeAll(argumentSet);
            }
        };
    }

    public static <T> MutableSet<MutableSet<T>> powerSet(Set<T> set)
    {
        MutableSet<MutableSet<T>> seed = UnifiedSet.<MutableSet<T>>newSetWith(UnifiedSet.<T>newSet());
        return Iterate.foldLeft(seed, set, new Function2<MutableSet<MutableSet<T>>, T, MutableSet<MutableSet<T>>>()
        {
            public MutableSet<MutableSet<T>> value(MutableSet<MutableSet<T>> accumulator, final T element)
            {
                return Sets.union(accumulator, accumulator.transform(new Function<MutableSet<T>, MutableSet<T>>()
                {
                    public MutableSet<T> valueOf(MutableSet<T> innerSet)
                    {
                        return innerSet.toSet().with(element);
                    }
                }));
            }
        });
    }

    public static <A, B> LazyIterable<Pair<A, B>> cartesianProduct(Set<A> set1, final Set<B> set2)
    {
        return LazyIterate.flatTransform(set1, new Function<A, LazyIterable<Pair<A, B>>>()
        {
            public LazyIterable<Pair<A, B>> valueOf(final A first)
            {
                return LazyIterate.transform(set2, new Function<B, Pair<A, B>>()
                {
                    public Pair<A, B> valueOf(B second)
                    {
                        return Tuples.pair(first, second);
                    }
                });
            }
        });
    }
}
