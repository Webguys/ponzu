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

package com.webguys.ponzu.impl.list.fixed;

import java.util.ListIterator;

import com.webguys.ponzu.api.block.procedure.ObjectIntProcedure;
import com.webguys.ponzu.api.list.MutableList;
import com.webguys.ponzu.impl.block.factory.Procedures2;
import com.webguys.ponzu.impl.block.procedure.CollectionAddProcedure;
import com.webguys.ponzu.impl.factory.Lists;
import com.webguys.ponzu.impl.list.mutable.FastList;
import com.webguys.ponzu.impl.test.SerializeTestHelper;
import com.webguys.ponzu.impl.test.Verify;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit test for {@link TripletonList}.
 */
public class TripletonListTest extends AbstractMemoryEfficientMutableListTestCase
{
    @Override
    protected int getSize()
    {
        return 3;
    }

    @Override
    protected Class<?> getListType()
    {
        return TripletonList.class;
    }

    @Test
    public void testClone()
    {
        MutableList<String> growableList = this.list.clone();
        Verify.assertEqualsAndHashCode(this.list, growableList);
        Verify.assertInstanceOf(TripletonList.class, growableList);
    }

    @Test
    public void testContains()
    {
        Assert.assertTrue(this.list.contains("1"));
        Assert.assertTrue(this.list.contains("2"));
        Assert.assertTrue(this.list.contains("3"));
        Assert.assertFalse(this.list.contains("4"));
    }

    @Test
    public void testRemove()
    {
        Verify.assertThrows(UnsupportedOperationException.class, new Runnable()
        {
            public void run()
            {
                TripletonListTest.this.list.remove(0);
            }
        });
        this.assertUnchanged();
    }

    @Test
    public void testAddAtIndex()
    {
        Verify.assertThrows(UnsupportedOperationException.class, new Runnable()
        {
            public void run()
            {
                TripletonListTest.this.list.add(0, "1");
            }
        });
        this.assertUnchanged();
    }

    @Test
    public void testAdd()
    {
        Verify.assertThrows(UnsupportedOperationException.class, new Runnable()
        {
            public void run()
            {
                TripletonListTest.this.list.add("1");
            }
        });
        this.assertUnchanged();
    }

    @Test
    public void testAddingAllToOtherList()
    {
        MutableList<String> newList = FastList.newList(this.list);
        newList.add("4");
        Assert.assertEquals(FastList.newListWith("1", "2", "3", "4"), newList);
    }

    @Test
    public void testGet()
    {
        Verify.assertStartsWith(this.list, "1", "2", "3");
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                TripletonListTest.this.list.get(3);
            }
        });
    }

    @Test
    public void testSet()
    {
        final MutableList<String> list = Lists.fixedSize.of("1", "2", "3");
        Assert.assertEquals("1", list.set(0, "3"));
        Assert.assertEquals("2", list.set(1, "2"));
        Assert.assertEquals("3", list.set(2, "1"));
        Assert.assertEquals(FastList.newListWith("3", "2", "1"), list);
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                list.set(3, "0");
            }
        });
    }

    private void assertUnchanged()
    {
        Verify.assertInstanceOf(TripletonList.class, this.list);
        Verify.assertSize(3, this.list);
        Verify.assertNotContains("4", this.list);
        Assert.assertEquals(FastList.newListWith("1", "2", "3"), this.list);
    }

    @Test
    public void testSerializableEqualsAndHashCode()
    {
        Verify.assertPostSerializedEqualsAndHashCode(this.list);
        MutableList<String> copyOfList = SerializeTestHelper.serializeDeserialize(this.list);
        Assert.assertNotSame(this.list, copyOfList);
    }

    @Test
    public void testCreate1()
    {
        MutableList<String> list = Lists.fixedSize.of("1");
        Verify.assertSize(1, list);
        Verify.assertItemAtIndex("1", 0, list);
    }

    @Test
    public void testEqualsAndHashCode()
    {
        MutableList<String> one = Lists.fixedSize.of("1", "2", "3");
        MutableList<String> oneA = FastList.newList(one);
        Verify.assertEqualsAndHashCode(one, oneA);
        Verify.assertPostSerializedEqualsAndHashCode(one);
    }

    @Test
    public void testForEach()
    {
        MutableList<String> result = Lists.mutable.of();
        MutableList<String> source = Lists.fixedSize.of("1", "2", "3");
        source.forEach(CollectionAddProcedure.on(result));
        Assert.assertEquals(FastList.newListWith("1", "2", "3"), result);
    }

    @Test
    public void testForEachFromTo()
    {
        MutableList<String> result = Lists.mutable.of();
        MutableList<String> source = Lists.fixedSize.of("1", "2", "3");
        source.forEach(0, 2, CollectionAddProcedure.on(result));
        Assert.assertEquals(FastList.newListWith("1", "2", "3"), result);
    }

    @Test
    public void testForEachWithIndex()
    {
        final int[] indexSum = new int[1];
        final MutableList<String> result = Lists.mutable.of();
        MutableList<String> source = Lists.fixedSize.of("1", "2", "3");
        source.forEachWithIndex(new ObjectIntProcedure<String>()
        {
            public void value(String each, int index)
            {
                result.add(each);
                indexSum[0] += index;
            }
        });
        Assert.assertEquals(FastList.newListWith("1", "2", "3"), result);
        Assert.assertEquals(3, indexSum[0]);
    }

    @Test
    public void testForEachWithIndexFromTo()
    {
        final int[] indexSum = new int[1];
        final MutableList<String> result = Lists.mutable.of();
        MutableList<String> source = Lists.fixedSize.of("1", "2", "3");
        source.forEachWithIndex(0, 2, new ObjectIntProcedure<String>()
        {
            public void value(String each, int index)
            {
                result.add(each);
                indexSum[0] += index;
            }
        });
        Assert.assertEquals(FastList.newListWith("1", "2", "3"), result);
        Assert.assertEquals(3, indexSum[0]);
    }

    @Test
    public void testForEachWith()
    {
        MutableList<String> result = Lists.mutable.of();
        MutableList<String> source = Lists.fixedSize.of("1", "2", "3");
        source.forEachWith(Procedures2.fromProcedure(CollectionAddProcedure.on(result)), null);
        Assert.assertEquals(FastList.newListWith("1", "2", "3"), result);
    }

    @Test
    public void testGetFirstGetLast()
    {
        MutableList<String> list3 = Lists.fixedSize.of("1", "2", "3");
        Assert.assertEquals("1", list3.getFirst());
        Assert.assertEquals("3", list3.getLast());
    }

    @Test
    public void testForLoop()
    {
        MutableList<String> list = Lists.fixedSize.of("one", "two", "three");
        MutableList<String> upperList = Lists.fixedSize.of("ONE", "TWO", "THREE");
        for (String each : list)
        {
            Verify.assertContains(each.toUpperCase(), upperList);
        }
    }

    @Test
    public void testSubList()
    {
        MutableList<String> list = Lists.fixedSize.of("one", "two", "three");
        MutableList<String> subList = list.subList(0, 3);
        MutableList<String> upperList = Lists.fixedSize.of("ONE", "TWO", "THREE");
        for (String each : subList)
        {
            Verify.assertContains(each.toUpperCase(), upperList);
        }
        Assert.assertEquals("one", subList.getFirst());
        Assert.assertEquals("three", subList.getLast());
        MutableList<String> subList2 = list.subList(1, 2);
        Assert.assertEquals("two", subList2.getFirst());
        Assert.assertEquals("two", subList2.getLast());
        MutableList<String> subList3 = list.subList(0, 1);
        Assert.assertEquals("one", subList3.getFirst());
        Assert.assertEquals("one", subList3.getLast());
        MutableList<String> subList4 = subList.subList(1, 3);
        Assert.assertEquals("two", subList4.getFirst());
        Assert.assertEquals("three", subList4.getLast());
    }

    @Test
    public void testListIterator()
    {
        MutableList<String> list = Lists.fixedSize.of("one", "two", "three");
        ListIterator<String> iterator = list.listIterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertFalse(iterator.hasPrevious());
        Assert.assertEquals("one", iterator.next());
        Assert.assertEquals("two", iterator.next());
        Assert.assertEquals("three", iterator.next());
        Assert.assertTrue(iterator.hasPrevious());
        Assert.assertEquals("three", iterator.previous());
        Assert.assertEquals("two", iterator.previous());
        Assert.assertEquals("one", iterator.previous());
        iterator.set("1");
        Assert.assertEquals("1", iterator.next());
        Assert.assertEquals("1", list.getFirst());
        list.subList(1, 3);
    }

    @Test
    public void testSubListListIterator()
    {
        MutableList<String> list = Lists.fixedSize.of("one", "two", "three");
        MutableList<String> subList = list.subList(1, 3);
        ListIterator<String> iterator = subList.listIterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertFalse(iterator.hasPrevious());
        Assert.assertEquals("two", iterator.next());
        Assert.assertEquals("three", iterator.next());
        Assert.assertTrue(iterator.hasPrevious());
        Assert.assertEquals("three", iterator.previous());
        Assert.assertEquals("two", iterator.previous());
        iterator.set("2");
        Assert.assertEquals("2", iterator.next());
        Assert.assertEquals("2", subList.getFirst());
        Assert.assertEquals("2", list.get(1));
    }

    @Test
    public void testSubListSet()
    {
        MutableList<String> list = Lists.fixedSize.of("one", "two", "three");
        MutableList<String> subList = list.subList(1, 3);
        Assert.assertEquals("two", subList.set(0, "2"));
        Assert.assertEquals("2", subList.getFirst());
        Assert.assertEquals("2", list.get(1));
    }

    @Test
    public void testNewEmpty()
    {
        MutableList<String> list = Lists.fixedSize.of("one", "two", "three");
        Verify.assertEmpty(list.newEmpty());
    }

    @Test
    public void testSubListForEach()
    {
        MutableList<String> list = Lists.fixedSize.of("1", "2", "3");
        MutableList<String> source = list.subList(1, 3);
        MutableList<String> result = Lists.mutable.of();
        source.forEach(CollectionAddProcedure.<String>on(result));
        Assert.assertEquals(FastList.newListWith("2", "3"), result);
    }

    @Test
    public void testSubListForEachWithIndex()
    {
        MutableList<String> list = Lists.fixedSize.of("1", "2", "3");
        MutableList<String> source = list.subList(1, 3);
        final int[] indexSum = new int[1];
        final MutableList<String> result = Lists.mutable.of();
        source.forEachWithIndex(new ObjectIntProcedure<String>()
        {
            public void value(String each, int index)
            {
                result.add(each);
                indexSum[0] += index;
            }
        });
        Assert.assertEquals(FastList.newListWith("2", "3"), result);
        Assert.assertEquals(1, indexSum[0]);
    }

    @Test
    public void testSubListForEachWith()
    {
        MutableList<String> list = Lists.fixedSize.of("1", "2", "3");
        MutableList<String> source = list.subList(1, 3);
        MutableList<String> result = Lists.mutable.of();
        source.forEachWith(Procedures2.fromProcedure(CollectionAddProcedure.<String>on(result)), null);
        Assert.assertEquals(FastList.newListWith("2", "3"), result);
    }

    @Test
    public void testIndexOf()
    {
        MutableList<String> list = Lists.fixedSize.of("1", null, "3");
        Assert.assertEquals(0, list.indexOf("1"));
        Assert.assertEquals(1, list.indexOf(null));
        Assert.assertEquals(2, list.indexOf("3"));
        Assert.assertEquals(-1, list.indexOf("4"));
    }

    @Test
    public void testLastIndexOf()
    {
        MutableList<String> list = Lists.fixedSize.of("1", null, "1");
        Assert.assertEquals(2, list.lastIndexOf("1"));
        Assert.assertEquals(1, list.lastIndexOf(null));
        Assert.assertEquals(-1, list.lastIndexOf("4"));
    }

    @Test
    public void without()
    {
        MutableList<Integer> list = new TripletonList<Integer>(2, 3, 2);
        Assert.assertSame(list, list.without(9));
        list = list.without(2);
        Verify.assertListsEqual(FastList.newListWith(3, 2), list);
        Verify.assertInstanceOf(DoubletonList.class, list);
    }
}
