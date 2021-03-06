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

package com.webguys.ponzu.impl.block.procedure;

import com.webguys.ponzu.api.block.procedure.Procedure;
import com.webguys.ponzu.api.list.MutableList;
import com.webguys.ponzu.impl.block.factory.Predicates;
import com.webguys.ponzu.impl.factory.Lists;
import com.webguys.ponzu.impl.list.mutable.FastList;
import com.webguys.ponzu.impl.test.Verify;
import com.webguys.ponzu.impl.utility.Iterate;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfProcedureTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IfProcedureTest.class);

    @Test
    public void procedure()
    {
        MutableList<String> list1 = Lists.mutable.of();
        MutableList<String> list2 = Lists.mutable.of();
        Procedure<String> procedure1 = CollectionAddProcedure.<String>on(list1);
        Procedure<String> procedure2 = CollectionAddProcedure.<String>on(list2);
        Procedure<String> ifProcedure = new IfProcedure<String>(Predicates.equal("1"), procedure1, procedure2);
        LOGGER.info("{}", ifProcedure);
        MutableList<String> list = FastList.newListWith("1", "2");
        Iterate.forEach(list, ifProcedure);
        Assert.assertEquals(1, list1.size());
        Verify.assertContains("1", list1);
        Assert.assertEquals(1, list2.size());
        Verify.assertContains("2", list2);
    }
}
