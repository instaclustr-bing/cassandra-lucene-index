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
package com.stratio.cassandra.lucene.geospatial;

import static com.stratio.cassandra.lucene.builder.Builder.geoBBox;
import static com.stratio.cassandra.lucene.builder.Builder.geoPointMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import com.stratio.cassandra.lucene.BaseTest;
import com.stratio.cassandra.lucene.util.CassandraUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GeoBBoxSearchTest extends BaseTest {

    protected static CassandraUtils utils;

    public static final Map<String, String> data1;

    static {
        data1 = new LinkedHashMap<>();
        data1.put("place", "'Madrid'");
        data1.put("latitude", "0.0");
        data1.put("longitude", "0.0");
    }

    public static final Map<String, String> data2;

    static {
        data2 = new LinkedHashMap<>();
        data2.put("place", "'Barcelona'");
        data2.put("latitude", "50.000002");
        data2.put("longitude", "50.000002");
    }

    @BeforeAll
    public static void before() {
        utils = CassandraUtils.builder("bbox_search")
            .withPartitionKey("place")
            .withClusteringKey()
            .withColumn("place", "text", null)
            .withColumn("latitude", "decimal", null)
            .withColumn("longitude", "decimal", null)
            .withMapper("location", geoPointMapper("latitude", "longitude"))
            .build()
            .createKeyspace()
            .createTable()
            .createIndex()
            .insert(data1)
            .insert(data2)
            .refresh();
    }

    @AfterAll
    public static void after() {
        CassandraUtils.dropKeyspaceIfNotNull(utils);
    }

    @Test
    public void testGeoBBoxSearchBasicSuccess() {
        utils.query(geoBBox("location", 0.0, 0.0, 0.0, 0.0)).check(1)
            .query(geoBBox("location", 0.0, 1.0, 0.0, 1.0)).check(1)
            .query(geoBBox("location", 0.0, 1.0, -1.0, 0.0)).check(1)
            .query(geoBBox("location", 0.0, 0.0, 1.0, 2.0)).check(0)
            .query(geoBBox("location", 1.0, 2.0, 0.0, 0.0)).check(0)
            .query(geoBBox("location", -0000.1, 0.0001, -0.0001, 0.0001)).check(1)
            .query(geoBBox("location", 50.000001, 50.000003, 50.000001, 50.000003)).check(1);
    }

}