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

package com.webguys.ponzu.impl

trait UnmodifiableListIterableTestTrait extends UnmodifiableIterableTestTrait
{
    val classUnderTest: java.util.List[String]

    import org.junit.Test

    @Test(expected = classOf[UnsupportedOperationException])
    def listIterator_remove_throws
    {
        val iterator = classUnderTest.listIterator
        if (iterator.hasNext) iterator.next
        iterator.remove
    }

    @Test(expected = classOf[UnsupportedOperationException])
    def listIterator_add_throws
    {
        val iterator = classUnderTest.listIterator
        if (iterator.hasNext) iterator.next
        iterator.add("")
    }

    @Test(expected = classOf[UnsupportedOperationException])
    def listIterator_set_throws
    {
        val iterator = classUnderTest.listIterator
        if (iterator.hasNext) iterator.next
        iterator.set("")
    }
}
