/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.cassandra.lucene.varia;

import static com.stratio.cassandra.lucene.builder.Builder.field;
import static com.stratio.cassandra.lucene.builder.Builder.integerMapper;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.stratio.cassandra.lucene.BaseTest;
import com.stratio.cassandra.lucene.util.CassandraUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Andres de la Pena <adelapena@stratio.com>
 */

public class SortWithSkinnyRowsTest extends BaseTest {

    private static CassandraUtils utils;

    @BeforeAll
    public static void before() {
        utils = CassandraUtils.builder("sort_with_wide_rows")
            .withPartitionKey("pk")
            .withColumn("pk", "int", integerMapper())
            .withColumn("rc", "int", integerMapper())
            .build()
            .createKeyspace()
            .createTable()
            .createIndex();
        for (int i = 0; i < 10; i++) {
            utils.insert(new String[]{"pk", "rc"}, new Object[]{i, i});
        }
        utils.refresh();
    }

    @AfterAll
    public static void after() {
        CassandraUtils.dropKeyspaceIfNotNull(utils);
    }

    @Test
    public void testSortAsc() {
        utils.sort(field("rc").reverse(false)).limit(3).checkOrderedColumns("rc", 0, 1, 2);
    }

    @Test
    public void testSortDesc() {
        utils.sort(field("rc").reverse(true)).limit(3).checkOrderedColumns("rc", 9, 8, 7);
    }

    @Test
    public void testSortPartitionAsc() {
        utils.sort(field("rc").reverse(false)).andEq("pk", 1).limit(3).checkUnorderedColumns("rc", 1);
    }

    @Test
    public void testSortPartitionDesc() {
        utils.sort(field("rc").reverse(true)).andEq("pk", 1).limit(3).checkUnorderedColumns("rc", 1);
    }

    @Test
    public void testSortTokenRangeAsc() {
        utils.sort(field("rc").reverse(false))
            .andGt("token(pk)", 0)
            .limit(3)
            .checkOrderedColumns("rc", 3, 6, 7);
    }

    @Test
    public void testSortTokenRangeDesc() {
        utils.sort(field("rc").reverse(true))
            .andGt("token(pk)", 0)
            .limit(3)
            .checkOrderedColumns("rc", 9, 7, 6);
    }

    @Test
    public void testSortNotExistingColumn() {
        utils.sort(field("missing").reverse(true))
            .check(InvalidQueryException.class, "No mapper found for sortFields field 'missing'");
    }
}
