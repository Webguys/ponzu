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
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.webguys.ponzu.api.block.function.Function;
import com.webguys.ponzu.api.block.predicate.Predicate;
import com.webguys.ponzu.api.block.procedure.ObjectIntProcedure;
import com.webguys.ponzu.api.block.procedure.Procedure;
import com.webguys.ponzu.api.list.ListIterable;
import com.webguys.ponzu.api.multimap.MutableMultimap;
import com.webguys.ponzu.impl.block.procedure.MultimapPutProcedure;
import com.webguys.ponzu.impl.list.fixed.ArrayAdapter;
import com.webguys.ponzu.impl.multimap.list.SynchronizedPutFastListMultimap;
import com.webguys.ponzu.impl.utility.Iterate;

import static com.webguys.ponzu.impl.factory.Iterables.*;

/**
 * The ParallelIterate class contains several parallel algorithms that work with Collections.  All of the higher
 * level parallel algorithms depend on the basic parallel algorithm named {@code forEach}.  The forEach algorithm employs
 * a batching fork and join approach.
 * <p/>
 * All Collections that are not either a {@link RandomAccess} or {@link List} are first converted to a Java array
 * using {@link Iterate#toArray(Iterable)}, and then run with one of the {@code ParallelArrayIterate.forEach} methods.
 *
 * @see ParallelArrayIterate
 */
public final class ParallelIterate
{
    static final int DEFAULT_MIN_FORK_SIZE = 10000;
    static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    static final int TASK_RATIO = 2;
    static final int DEFAULT_PARALLEL_TASK_COUNT = ParallelIterate.getDefaultTaskCount();
    static final ExecutorService EXECUTOR_SERVICE = ParallelIterate.newPooledExecutor(ParallelIterate.class.getSimpleName(), true);

    private ParallelIterate()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    static boolean isExecutorShutdown()
    {
        return ParallelIterate.EXECUTOR_SERVICE.isShutdown();
    }

    static void shutdownExecutor()
    {
        ParallelIterate.EXECUTOR_SERVICE.shutdown();
    }

    /**
     * Iterate over the collection specified, in parallel batches using default runtime parameter values.  The
     * {@code ObjectIntProcedure} used must be stateless, or use concurrent aware objects if they are to be shared.
     * <p/>
     * e.g.
     * <pre>
     * {@code final Map<Integer,Object> chm = new ConcurrentHashMap<Integer,Object>();}
     * ParallelIterate.<b>forEachWithIndex</b>(collection, new ObjectIntProcedure()
     * {
     *     public void value(Object object, int index)
     *     {
     *         chm.put(index, object);
     *     }
     * });
     * </pre>
     */
    public static <T> void forEachWithIndex(
            Iterable<T> iterable,
            ObjectIntProcedure<? super T> objectIntProcedure)
    {
        ParallelIterate.forEachWithIndex(iterable, objectIntProcedure, ParallelIterate.EXECUTOR_SERVICE);
    }

    /**
     * Iterate over the collection specified in parallel batches using the default runtime parameters.  The
     * ObjectIntProcedure used must be stateless, or use concurrent aware objects if they are to be shared.  The code
     * is executed against the specified executor.
     * <p/>
     * <pre>e.g.
     * {@code final Map<Integer,Object> chm = new ConcurrentHashMap<Integer,Object>();}
     * ParallelIterate.<b>forEachWithIndex</b>(collection, new ObjectIntProcedure()
     * {
     *     public void value(Object object, int index)
     *     {
     *         chm.put(index, object);
     *     }
     * }, executor);
     * </pre>
     *
     * @param executor Use this executor for all execution.
     */
    public static <T, BT extends ObjectIntProcedure<? super T>> void forEachWithIndex(
            Iterable<T> iterable,
            BT procedure,
            Executor executor)
    {
        ParallelIterate.forEachWithIndex(iterable,
                new PassThruObjectIntProcedureFactory<BT>(procedure),
                new PassThruCombiner<BT>(), executor);
    }

    /**
     * Iterate over the collection specified in parallel batches.  The
     * ObjectIntProcedure used must be stateless, or use concurrent aware objects if they are to be shared.  The
     * specified minimum fork size and task count are used instead of the default values.
     *
     * @param minForkSize Only run in parallel if input collection is longer than this.
     * @param taskCount   How many parallel tasks to submit to the executor.
     * @see #forEachWithIndex(Iterable, ObjectIntProcedure)
     */
    public static <T, BT extends ObjectIntProcedure<? super T>> void forEachWithIndex(
            Iterable<T> iterable,
            BT procedure,
            int minForkSize,
            int taskCount)
    {
        ParallelIterate.forEachWithIndex(
                iterable,
                new PassThruObjectIntProcedureFactory<BT>(procedure),
                new PassThruCombiner<BT>(),
                minForkSize,
                taskCount);
    }

    public static <T, BT extends ObjectIntProcedure<? super T>> void forEachWithIndex(
            Iterable<T> iterable,
            ObjectIntProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            Executor executor)
    {
        int taskCount = Math.max(
                ParallelIterate.DEFAULT_PARALLEL_TASK_COUNT,
                Iterate.sizeOf(iterable) / ParallelIterate.DEFAULT_MIN_FORK_SIZE);
        ParallelIterate.forEachWithIndex(
                iterable,
                procedureFactory,
                combiner,
                ParallelIterate.DEFAULT_MIN_FORK_SIZE,
                taskCount,
                executor);
    }

    public static <T, BT extends ObjectIntProcedure<? super T>> void forEachWithIndex(
            Iterable<T> iterable,
            ObjectIntProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int minForkSize,
            int taskCount)
    {
        ParallelIterate.forEachWithIndex(iterable, procedureFactory, combiner, minForkSize, taskCount, ParallelIterate.EXECUTOR_SERVICE);
    }

    public static <T, BT extends ObjectIntProcedure<? super T>> void forEachWithIndex(
            Iterable<T> iterable,
            ObjectIntProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int minForkSize,
            int taskCount,
            Executor executor)
    {
        if (Iterate.notEmpty(iterable))
        {
            if (iterable instanceof RandomAccess || iterable instanceof ListIterable
                    && iterable instanceof List)
            {
                ParallelIterate.forEachWithIndexInListOnExecutor(
                        (List<T>) iterable,
                        procedureFactory,
                        combiner,
                        minForkSize,
                        taskCount,
                        executor);
            }
            else
            {
                ParallelIterate.forEachWithIndexInListOnExecutor(
                        ArrayAdapter.adapt((T[]) Iterate.toArray(iterable)),
                        procedureFactory,
                        combiner,
                        minForkSize,
                        taskCount,
                        executor);
            }
        }
    }

    public static <T, BT extends ObjectIntProcedure<? super T>> void forEachWithIndexInListOnExecutor(
            List<T> list,
            ObjectIntProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int minForkSize,
            int taskCount,
            Executor executor)
    {
        int size = list.size();
        if (size < minForkSize)
        {
            BT procedure = procedureFactory.create();
            Iterate.forEachWithIndex(list, procedure);
            if (combiner.useCombineOne())
            {
                combiner.combineOne(procedure);
            }
            else
            {
                combiner.combineAll(iList(procedure));
            }
        }
        else
        {
            int threadCount = Math.min(size, taskCount);
            ObjectIntProcedureFJTaskRunner<T, BT> runner =
                    new ObjectIntProcedureFJTaskRunner<T, BT>(combiner, threadCount);
            runner.executeAndCombine(executor, procedureFactory, list);
        }
    }

    /**
     * Iterate over the collection specified in parallel batches using default runtime parameter values.  The
     * {@code Procedure} used must be stateless, or use concurrent aware objects if they are to be shared.
     * <p/>
     * e.g.
     * <pre>
     * {@code final Map<Object,Boolean> chm = new ConcurrentHashMap<Object,Boolean>();}
     * ParallelIterate.<b>forEach</b>(collection, new Procedure()
     * {
     *     public void value(Object object)
     *     {
     *         chm.put(object, Boolean.TRUE);
     *     }
     * });
     * </pre>
     */
    public static <T> void forEach(Iterable<T> iterable, Procedure<? super T> procedure)
    {
        ParallelIterate.forEach(iterable, procedure, ParallelIterate.EXECUTOR_SERVICE);
    }

    /**
     * Iterate over the collection specified in parallel batches using default runtime parameter values.  The
     * {@code Procedure} used must be stateless, or use concurrent aware objects if they are to be shared.
     * <p/>
     * e.g.
     * <pre>
     * {@code final Map<Object,Boolean> chm = new ConcurrentHashMap<Object,Boolean>();}
     * ParallelIterate.<b>forEachBatchSize</b>(collection, new Procedure()
     * {
     *     public void value(Object object)
     *     {
     *         chm.put(object, Boolean.TRUE);
     *     }
     * }, 100);
     * </pre>
     */
    public static <T> void forEach(Iterable<T> iterable, Procedure<? super T> procedure, int batchSize)
    {
        ParallelIterate.forEach(iterable, procedure, batchSize, ParallelIterate.EXECUTOR_SERVICE);
    }

    public static <T> void forEach(Iterable<T> iterable, Procedure<? super T> procedure, int batchSize, Executor executor)
    {
        ParallelIterate.forEach(iterable, procedure, batchSize, ParallelIterate.calculateTaskCount(iterable, batchSize), executor);
    }

    /**
     * Iterate over the collection specified in parallel batches using default runtime parameter values
     * and the specified executor.
     * The {@code Procedure} used must be stateless, or use concurrent aware objects if they are to be shared.
     *
     * @param executor Use this executor for all execution.
     * @see #forEach(Iterable, Procedure)
     */
    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            BT procedure,
            Executor executor)
    {
        ParallelIterate.forEach(
                iterable,
                new PassThruProcedureFactory<BT>(procedure),
                new PassThruCombiner<BT>(),
                executor);
    }

    /**
     * Iterate over the collection specified in parallel batches using the specified minimum fork and task count sizes.
     * The {@code Procedure} used must be stateless, or use concurrent aware objects if they are to be shared.
     *
     * @param minForkSize Only run in parallel if input collection is longer than this.
     * @param taskCount   How many parallel tasks to submit to the executor.
     *                    TODO: How does the taskCount relate to the number of threads in the executor?
     * @see #forEach(Iterable, Procedure)
     */
    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            BT procedure,
            int minForkSize,
            int taskCount)
    {
        ParallelIterate.forEach(iterable, procedure, minForkSize, taskCount, ParallelIterate.EXECUTOR_SERVICE);
    }

    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            BT procedure,
            int minForkSize,
            int taskCount,
            Executor executor)
    {
        ParallelIterate.forEach(
                iterable,
                new PassThruProcedureFactory<BT>(procedure),
                new PassThruCombiner<BT>(),
                minForkSize,
                taskCount,
                executor);
    }

    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            ProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            Executor executor)
    {
        ParallelIterate.forEach(iterable, procedureFactory, combiner, ParallelIterate.DEFAULT_MIN_FORK_SIZE, executor);
    }

    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            ProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner)
    {
        ParallelIterate.forEach(iterable, procedureFactory, combiner, ParallelIterate.EXECUTOR_SERVICE);
    }

    /**
     * Iterate over the collection specified in parallel batches using the default values for the task size.  The
     * ProcedureFactory can create stateful closures that will be transformed and combined using the specified Combiner.
     * <p/>
     * <pre>e.g. The <b>ParallelIterate.filter()</b> implementation
     * <p/>
     * {@code CollectionCombiner<T, SelectProcedure<T>> combiner = CollectionCombiner.forSelect(collection);}
     * ParallelIterate.<b>forEach</b>(collection,{@code new SelectProcedureFactory<T>(predicate, taskSize), combiner, 1000);}
     * </pre>
     */
    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            ProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int batchSize)
    {
        ParallelIterate.forEach(iterable, procedureFactory, combiner, batchSize, ParallelIterate.EXECUTOR_SERVICE);
    }

    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            ProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int batchSize,
            Executor executor)
    {
        ParallelIterate.forEach(iterable, procedureFactory, combiner, batchSize, ParallelIterate.calculateTaskCount(iterable, batchSize), executor);
    }

    /**
     * Iterate over the collection specified in parallel batches using the default values for the task size.  The
     * ProcedureFactory can create stateful closures that will be transformed and combined using the specified Combiner.
     * <p/>
     * <pre>e.g. The <b>ParallelIterate.filter()</b> implementation
     * <p/>
     * int taskCount = Math.max(DEFAULT_PARALLEL_TASK_COUNT, collection.size() / DEFAULT_MIN_FORK_SIZE);
     * final int taskSize = collection.size() / taskCount / 2;
     * {@code CollectionCombiner<T, SelectProcedure<T>> combiner = CollectionCombiner.forSelect(collection);}
     * ParallelIterate.<b>forEach</b>(collection,{@code new SelectProcedureFactory<T>(predicate, taskSize), combiner, DEFAULT_MIN_FORK_SIZE, taskCount);}
     * </pre>
     */
    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            ProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int minForkSize,
            int taskCount)
    {
        ParallelIterate.forEach(iterable, procedureFactory, combiner, minForkSize, taskCount, ParallelIterate.EXECUTOR_SERVICE);
    }

    public static <T, BT extends Procedure<? super T>> void forEach(
            Iterable<T> iterable,
            ProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int minForkSize,
            int taskCount,
            Executor executor)
    {
        if (Iterate.notEmpty(iterable))
        {
            if ((iterable instanceof RandomAccess || iterable instanceof ListIterable)
                    && iterable instanceof List)
            {
                ParallelIterate.forEachInListOnExecutor(
                        (List<T>) iterable,
                        procedureFactory,
                        combiner,
                        minForkSize,
                        taskCount,
                        executor);
            }
            else if (iterable instanceof BatchIterable)
            {
                ParallelIterate.forEachInBatchWithExecutor(
                        (BatchIterable<T>) iterable,
                        procedureFactory,
                        combiner,
                        minForkSize,
                        taskCount,
                        executor);
            }
            else
            {
                ParallelArrayIterate.forEachOn(
                        (T[]) Iterate.toArray(iterable),
                        procedureFactory,
                        combiner,
                        minForkSize,
                        taskCount,
                        executor);
            }
        }
    }

    public static <T, BT extends Procedure<? super T>> void forEachInListOnExecutor(
            List<T> list,
            ProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int minForkSize,
            int taskCount,
            Executor executor)
    {
        int size = list.size();
        if (size < minForkSize)
        {
            BT procedure = procedureFactory.create();
            Iterate.forEach(list, procedure);
            if (combiner.useCombineOne())
            {
                combiner.combineOne(procedure);
            }
            else
            {
                combiner.combineAll(iList(procedure));
            }
        }
        else
        {
            int threadCount = Math.min(size, taskCount);
            ProcedureFJTaskRunner<T, BT> runner =
                    new ProcedureFJTaskRunner<T, BT>(combiner, threadCount);
            runner.executeAndCombine(executor, procedureFactory, list);
        }
    }

    public static <T, BT extends Procedure<? super T>> void forEachInBatchWithExecutor(
            BatchIterable<T> set,
            ProcedureFactory<BT> procedureFactory,
            Combiner<BT> combiner,
            int minForkSize,
            int taskCount,
            Executor executor)
    {
        int size = set.size();
        if (size < minForkSize)
        {
            BT procedure = procedureFactory.create();
            set.forEach(procedure);
            if (combiner.useCombineOne())
            {
                combiner.combineOne(procedure);
            }
            else
            {
                combiner.combineAll(iList(procedure));
            }
        }
        else
        {
            int threadCount = Math.min(size, Math.min(taskCount, set.getBatchCount((int) Math.ceil((double) size / (double) taskCount))));
            BatchIterableProcedureFJTaskRunner<T, BT> runner =
                    new BatchIterableProcedureFJTaskRunner<T, BT>(combiner, threadCount);
            runner.executeAndCombine(executor, procedureFactory, set);
        }
    }

    /**
     * Same effect as {@link Iterate#filter(Iterable, Predicate)}, but executed in parallel batches.
     *
     * @return The filter elements. The Collection will be of the same type as the input (List or Set)
     *         and will be in the same order as the input (if it is an ordered collection).
     * @see ParallelIterate#filter(Iterable, Predicate, boolean)
     */
    public static <T> Collection<T> filter(
            Iterable<T> iterable,
            Predicate<? super T> predicate)
    {
        return ParallelIterate.filter(iterable, predicate, false);
    }

    /**
     * Same effect as {@link Iterate#filter(Iterable, Predicate)}, but executed in parallel batches,
     * and with a potentially reordered result.
     *
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The filtered elements. The Collection will be of the same type (List or Set) as the input.
     */
    public static <T> Collection<T> filter(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            boolean allowReorderedResult)
    {
        return ParallelIterate.filter(iterable, predicate, null, allowReorderedResult);
    }

    /**
     * Same effect as {@link Iterate#filter(Iterable, Predicate)}, but executed in parallel batches,
     * and writing output into the specified collection.
     *
     * @param target               Where to write the output.
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The 'target' collection, with the filtered elements added.
     */
    public static <T, R extends Collection<T>> R filter(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            R target,
            boolean allowReorderedResult)
    {
        return ParallelIterate.filter(
                iterable,
                predicate,
                target,
                ParallelIterate.DEFAULT_MIN_FORK_SIZE,
                ParallelIterate.EXECUTOR_SERVICE,
                allowReorderedResult);
    }

    /**
     * Same effect as {@link Iterate#filter(Iterable, Predicate)}, but executed in parallel batches,
     * and writing output into the specified collection.
     *
     * @param target               Where to write the output.
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The 'target' collection, with the filtered elements added.
     */
    public static <T, R extends Collection<T>> R filter(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            R target,
            int batchSize,
            Executor executor,
            boolean allowReorderedResult)
    {
        FilterProcedureCombiner<T> combiner = new FilterProcedureCombiner<T>(iterable, target, 10, allowReorderedResult);
        FilterProcedureFactory<T> procedureFactory = new FilterProcedureFactory<T>(predicate, batchSize);
        ParallelIterate.forEach(
                iterable,
                procedureFactory,
                combiner,
                batchSize,
                ParallelIterate.calculateTaskCount(iterable, batchSize),
                executor);
        return (R) combiner.getResult();
    }

    private static <T> int calculateTaskCount(Iterable<T> iterable, int batchSize)
    {
        if (iterable instanceof BatchIterable<?>)
        {
            return ParallelIterate.calculateTaskCount((BatchIterable<?>) iterable, batchSize);
        }
        return ParallelIterate.calculateTaskCount(Iterate.sizeOf(iterable), batchSize);
    }

    private static <T> int calculateTaskCount(BatchIterable<T> batchIterable, int batchSize)
    {
        return Math.max(2, batchIterable.getBatchCount(batchSize));
    }

    private static <T> int calculateTaskCount(int size, int batchSize)
    {
        return Math.max(2, size / batchSize);
    }

    /**
     * Same effect as {@link Iterate#filterNot(Iterable, Predicate)}, but executed in parallel batches.
     *
     * @return The filtered elements. The Collection will be of the same type as the input (List or Set)
     *         and will be in the same order as the input (if it is an ordered collection).
     * @see ParallelIterate#filterNot(Iterable, Predicate, boolean)
     */
    public static <T> Collection<T> filterNot(
            Iterable<T> iterable,
            Predicate<? super T> predicate)
    {
        return ParallelIterate.filterNot(iterable, predicate, false);
    }

    /**
     * Same effect as {@link Iterate#filterNot(Iterable, Predicate)}, but executed in parallel batches,
     * and with a potentially reordered result.
     *
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The filtered elements. The Collection will be of the same type (List or Set) as the input.
     */
    public static <T> Collection<T> filterNot(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            boolean allowReorderedResult)
    {
        return ParallelIterate.filterNot(iterable, predicate, null, allowReorderedResult);
    }

    /**
     * Same effect as {@link Iterate#filterNot(Iterable, Predicate)}, but executed in parallel batches,
     * and writing output into the specified collection.
     *
     * @param target               Where to write the output.
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The 'target' collection, with the filtered elements added.
     */
    public static <T, R extends Collection<T>> R filterNot(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            R target,
            boolean allowReorderedResult)
    {
        return ParallelIterate.filterNot(
                iterable,
                predicate,
                target,
                ParallelIterate.DEFAULT_MIN_FORK_SIZE,
                ParallelIterate.EXECUTOR_SERVICE,
                allowReorderedResult);
    }

    public static <T, R extends Collection<T>> R filterNot(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            R target,
            int batchSize,
            Executor executor,
            boolean allowReorderedResult)
    {
        FilterNotProcedureCombiner<T> combiner = new FilterNotProcedureCombiner<T>(iterable, target, 10, allowReorderedResult);
        FilterNotProcedureFactory<T> procedureFactory = new FilterNotProcedureFactory<T>(predicate, batchSize);
        ParallelIterate.forEach(
                iterable,
                procedureFactory,
                combiner,
                batchSize,
                ParallelIterate.calculateTaskCount(iterable, batchSize),
                executor);
        return (R) combiner.getResult();
    }

    /**
     * Same effect as {@link Iterate#count(Iterable, Predicate)}, but executed in parallel batches.
     *
     * @return The number of elements which satisfy the predicate.
     */
    public static <T> int count(Iterable<T> iterable, Predicate<? super T> predicate)
    {
        CountCombiner<T> combiner = new CountCombiner<T>();
        CountProcedureFactory<T> procedureFactory = new CountProcedureFactory<T>(predicate);
        ParallelIterate.forEach(
                iterable,
                procedureFactory,
                combiner,
                ParallelIterate.DEFAULT_MIN_FORK_SIZE,
                ParallelIterate.EXECUTOR_SERVICE);
        return combiner.getCount();
    }

    /**
     * Same effect as {@link Iterate#transform(Iterable, Function)},
     * but executed in parallel batches.
     *
     * @return The transformed elements. The Collection will be of the same type as the input (List or Set)
     *         and will be in the same order as the input (if it is an ordered collection).
     * @see ParallelIterate#transform(Iterable, Function, boolean)
     */
    public static <T, V> Collection<V> transform(
            Iterable<T> iterable,
            Function<? super T, V> function)
    {
        return ParallelIterate.transform(iterable, function, false);
    }

    /**
     * Same effect as {@link Iterate#transform(Iterable, Function)}, but executed in parallel batches,
     * and with potentially reordered result.
     *
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The transformed elements. The Collection will be of the same type
     *         (List or Set) as the input.
     */
    public static <T, V> Collection<V> transform(
            Iterable<T> iterable,
            Function<? super T, V> function,
            boolean allowReorderedResult)
    {
        return ParallelIterate.transform(iterable, function, null, allowReorderedResult);
    }

    /**
     * Same effect as {@link Iterate#transform(Iterable, Function)}, but executed in parallel batches,
     * and writing output into the specified collection.
     *
     * @param target               Where to write the output.
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The 'target' collection, with the transformed elements added.
     */
    public static <T, V, R extends Collection<V>> R transform(
            Iterable<T> iterable,
            Function<? super T, V> function,
            R target,
            boolean allowReorderedResult)
    {
        return ParallelIterate.transform(
                iterable,
                function,
                target,
                ParallelIterate.DEFAULT_MIN_FORK_SIZE,
                ParallelIterate.EXECUTOR_SERVICE,
                allowReorderedResult);
    }

    public static <T, V, R extends Collection<V>> R transform(
            Iterable<T> iterable,
            Function<? super T, V> function,
            R target,
            int batchSize,
            Executor executor,
            boolean allowReorderedResult)
    {
        int size = Iterate.sizeOf(iterable);
        TransformProcedureCombiner<T, V> combiner = new TransformProcedureCombiner<T, V>(iterable, target, size, allowReorderedResult);
        int taskCount = ParallelIterate.calculateTaskCount(size, batchSize);
        TransformProcedureFactory<T, V> procedureFactory = new TransformProcedureFactory<T, V>(function, size / taskCount);
        ParallelIterate.forEach(
                iterable,
                procedureFactory,
                combiner,
                batchSize,
                taskCount,
                executor);
        return (R) combiner.getResult();
    }

    public static <T, V> Collection<V> flatTransform(
            Iterable<T> iterable,
            Function<? super T, Collection<V>> function)
    {
        return ParallelIterate.flatTransform(iterable, function, false);
    }

    public static <T, V> Collection<V> flatTransform(
            Iterable<T> iterable,
            Function<? super T, Collection<V>> function,
            boolean allowReorderedResult)
    {
        return ParallelIterate.flatTransform(iterable, function, null, allowReorderedResult);
    }

    public static <T, V, R extends Collection<V>> R flatTransform(
            Iterable<T> iterable,
            Function<? super T, Collection<V>> function,
            R target,
            boolean allowReorderedResult)
    {
        return ParallelIterate.flatTransform(
                iterable,
                function,
                target,
                ParallelIterate.DEFAULT_MIN_FORK_SIZE,
                ParallelIterate.EXECUTOR_SERVICE,
                allowReorderedResult);
    }

    public static <T, V, R extends Collection<V>> R flatTransform(
            Iterable<T> iterable,
            Function<? super T, Collection<V>> function,
            R target,
            int batchSize,
            Executor executor,
            boolean allowReorderedResult)
    {
        int size = Iterate.sizeOf(iterable);
        int taskSize = size / ParallelIterate.DEFAULT_PARALLEL_TASK_COUNT;
        FlatTransformProcedureCombiner<T, V> combiner =
                new FlatTransformProcedureCombiner<T, V>(iterable, target, size, allowReorderedResult);
        FlatTransformProcedureFactory<T, V> procedureFactory = new FlatTransformProcedureFactory<T, V>(function, taskSize);
        ParallelIterate.forEach(
                iterable,
                procedureFactory,
                combiner,
                batchSize,
                ParallelIterate.calculateTaskCount(size, batchSize),
                executor);
        return (R) combiner.getResult();
    }

    /**
     * Same effect as {@link Iterate#transformIf(Iterable, Predicate, Function)},
     * but executed in parallel batches.
     *
     * @return The transformed elements. The Collection will be of the same type as the input (List or Set)
     *         and will be in the same order as the input (if it is an ordered collection).
     * @see ParallelIterate#transformIf(Iterable, Predicate, Function, boolean)
     */
    public static <T, V> Collection<V> transformIf(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            Function<? super T, V> function)
    {
        return ParallelIterate.transformIf(iterable, predicate, function, false);
    }

    /**
     * Same effect as {@link Iterate#transformIf(Iterable, Predicate, Function)},
     * but executed in parallel batches, and with potentially reordered results.
     *
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The transformed elements. The Collection will be of the same type
     *         as the input (List or Set)
     */
    public static <T, V> Collection<V> transformIf(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            Function<? super T, V> function,
            boolean allowReorderedResult)
    {
        return ParallelIterate.transformIf(iterable, predicate, function, null, allowReorderedResult);
    }

    /**
     * Same effect as {@link Iterate#transformIf(Iterable, Predicate, Function)},
     * but executed in parallel batches, and writing output into the specified collection.
     *
     * @param target               Where to write the output.
     * @param allowReorderedResult If the result can be in a different order.
     *                             Allowing reordering may yield faster execution.
     * @return The 'target' collection, with the transformed elements added.
     */
    public static <T, V, R extends Collection<V>> R transformIf(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            Function<? super T, V> function,
            R target,
            boolean allowReorderedResult)
    {
        return ParallelIterate.transformIf(
                iterable,
                predicate,
                function,
                target,
                ParallelIterate.DEFAULT_MIN_FORK_SIZE,
                ParallelIterate.EXECUTOR_SERVICE,
                allowReorderedResult);
    }

    public static <T, V, R extends Collection<V>> R transformIf(
            Iterable<T> iterable,
            Predicate<? super T> predicate,
            Function<? super T, V> function,
            R target,
            int batchSize,
            Executor executor,
            boolean allowReorderedResult)
    {
        TransformIfProcedureCombiner<T, V> combiner = new TransformIfProcedureCombiner<T, V>(iterable, target, 10, allowReorderedResult);
        TransformIfProcedureFactory<T, V> procedureFactory = new TransformIfProcedureFactory<T, V>(function, predicate, batchSize);
        ParallelIterate.forEach(
                iterable,
                procedureFactory,
                combiner,
                batchSize,
                ParallelIterate.calculateTaskCount(iterable, batchSize),
                executor);
        return (R) combiner.getResult();
    }

    /**
     * Same effect as {@link Iterate#groupBy(Iterable, Function)},
     * but executed in parallel batches, and writing output into a SynchronizedPutFastListMultimap.
     */
    public static <K, V> MutableMultimap<K, V> groupBy(
            Iterable<V> iterable,
            Function<? super V, ? extends K> function)
    {
        return ParallelIterate.groupBy(iterable, function, ParallelIterate.DEFAULT_MIN_FORK_SIZE, ParallelIterate.EXECUTOR_SERVICE);
    }

    /**
     * Same effect as {@link Iterate#groupBy(Iterable, Function)},
     * but executed in parallel batches, and writing output into a SynchronizedPutFastListMultimap.
     */
    public static <K, V, R extends MutableMultimap<K, V>> MutableMultimap<K, V> groupBy(
            Iterable<V> iterable,
            Function<? super V, ? extends K> function,
            R concurrentMultimap)
    {
        return ParallelIterate.groupBy(iterable, function, concurrentMultimap, ParallelIterate.DEFAULT_MIN_FORK_SIZE);
    }

    /**
     * Same effect as {@link Iterate#groupBy(Iterable, Function)},
     * but executed in parallel batches, and writing output into a SynchronizedPutFastListMultimap.
     */
    public static <K, V, R extends MutableMultimap<K, V>> MutableMultimap<K, V> groupBy(
            Iterable<V> iterable,
            Function<? super V, ? extends K> function,
            R concurrentMultimap,
            int batchSize)
    {
        return ParallelIterate.groupBy(iterable, function, concurrentMultimap, batchSize, ParallelIterate.EXECUTOR_SERVICE);
    }

    /**
     * Same effect as {@link Iterate#groupBy(Iterable, Function)},
     * but executed in parallel batches, and writing output into a SynchronizedPutFastListMultimap.
     */
    public static <K, V> MutableMultimap<K, V> groupBy(
            Iterable<V> iterable,
            Function<? super V, ? extends K> function,
            int batchSize)
    {
        return ParallelIterate.groupBy(iterable, function, batchSize, ParallelIterate.EXECUTOR_SERVICE);
    }

    /**
     * Same effect as {@link Iterate#groupBy(Iterable, Function)},
     * but executed in parallel batches, and writing output into a SynchronizedPutFastListMultimap.
     */
    public static <K, V> MutableMultimap<K, V> groupBy(
            Iterable<V> iterable,
            Function<? super V, ? extends K> function,
            int batchSize,
            Executor executor)
    {
        return ParallelIterate.groupBy(iterable, function, SynchronizedPutFastListMultimap.<K, V>newMultimap(), batchSize, executor);
    }

    /**
     * Same effect as {@link Iterate#groupBy(Iterable, Function)},
     * but executed in parallel batches, and writing output into a SynchronizedPutFastListMultimap.
     */
    public static <K, V, R extends MutableMultimap<K, V>> MutableMultimap<K, V> groupBy(
            Iterable<V> iterable,
            Function<? super V, ? extends K> function,
            R concurrentMultimap,
            int batchSize,
            Executor executor)
    {
        ParallelIterate.forEach(
                iterable,
                new PassThruProcedureFactory<Procedure<V>>(new MultimapPutProcedure<K, V>(concurrentMultimap, function)),
                Combiners.<Procedure<V>>passThru(),
                batchSize,
                executor);
        return concurrentMultimap;
    }

    /**
     * Returns a brand new ExecutorService using the specified poolName with the specified maximum thread pool size. The
     * same poolName may be used more than once resulting in multiple pools with the same name.
     * <p/>
     * The pool will be initialised with newPoolSize threads.  If that number of threads are in use and another thread
     * is requested, the pool will reject execution and the submitting thread will execute the task.
     */
    public static ExecutorService newPooledExecutor(int newPoolSize, String poolName, boolean useDaemonThreads)
    {
        return new ThreadPoolExecutor(
                newPoolSize,
                newPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new CollectionsThreadFactory(poolName, useDaemonThreads),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Returns a brand new ExecutorService using the specified poolName and uses the optional property named
     * to set the maximum thread pool size.  The same poolName may be used more than
     * once resulting in multiple pools with the same name.
     */
    public static ExecutorService newPooledExecutor(String poolName, boolean useDaemonThreads)
    {
        return ParallelIterate.newPooledExecutor(ParallelIterate.getDefaultMaxThreadPoolSize(), poolName, useDaemonThreads);
    }

    public static int getDefaultTaskCount()
    {
        return ParallelIterate.getDefaultMaxThreadPoolSize() * ParallelIterate.getTaskRatio();
    }

    public static int getDefaultMaxThreadPoolSize()
    {
        return Math.min(AVAILABLE_PROCESSORS + 1, 100);
    }

    public static int getTaskRatio()
    {
        return TASK_RATIO;
    }
}
