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

package ponzu.impl.multimap.bag;

import ponzu.api.collection.MutableCollection;
import ponzu.api.multimap.bag.ImmutableBagMultimap;
import ponzu.impl.factory.Bags;
import ponzu.impl.multimap.AbstractImmutableMultimapTestCase;

public class ImmutableBagMultimapTest extends AbstractImmutableMultimapTestCase
{
    @Override
    protected ImmutableBagMultimap<String, String> classUnderTest()
    {
        return HashBagMultimap.<String, String>newMultimap().toImmutable();
    }

    @Override
    protected MutableCollection<String> mutableCollection()
    {
        return Bags.mutable.of();
    }

    @Override
    public void noDuplicates()
    {
        // Bags allow duplicates
    }
}