/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.bucket.missing;

import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.aggregations.AggregationExecutionException;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.AggregatorSupplier;
import org.elasticsearch.search.aggregations.support.CoreValuesSourceType;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.aggregations.support.ValuesSourceRegistry;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MissingAggregatorFactory extends ValuesSourceAggregatorFactory {

    public static void registerAggregators(ValuesSourceRegistry valuesSourceRegistry) {
        valuesSourceRegistry.register(
            MissingAggregationBuilder.NAME,
            List.of(
                CoreValuesSourceType.NUMERIC,
                CoreValuesSourceType.BYTES,
                CoreValuesSourceType.GEOPOINT,
                CoreValuesSourceType.RANGE,
                CoreValuesSourceType.IP,
                CoreValuesSourceType.BOOLEAN,
                CoreValuesSourceType.DATE
            ),
            (MissingAggregatorSupplier) MissingAggregator::new
        );
    }

    public MissingAggregatorFactory(String name, ValuesSourceConfig config, QueryShardContext queryShardContext,
                                    AggregatorFactory parent, AggregatorFactories.Builder subFactoriesBuilder,
                                    Map<String, Object> metadata) throws IOException {
        super(name, config, queryShardContext, parent, subFactoriesBuilder, metadata);
    }

    @Override
    protected MissingAggregator createUnmapped(SearchContext searchContext,
                                                Aggregator parent,
                                                Map<String, Object> metadata) throws IOException {
        return new MissingAggregator(name, factories, null, searchContext, parent, metadata);
    }

    @Override
    protected Aggregator doCreateInternal(ValuesSource valuesSource,
                                                    SearchContext searchContext,
                                                    Aggregator parent,
                                                    boolean collectsFromSingleBucket,
                                                    Map<String, Object> metadata) throws IOException {
        final AggregatorSupplier aggregatorSupplier = queryShardContext.getValuesSourceRegistry()
            .getAggregator(config.valueSourceType(), MissingAggregationBuilder.NAME);
        if (aggregatorSupplier instanceof MissingAggregatorSupplier == false) {
            throw new AggregationExecutionException("Registry miss-match - expected MissingAggregatorSupplier, found [" +
                aggregatorSupplier.getClass().toString() + "]");
        }

        return ((MissingAggregatorSupplier) aggregatorSupplier)
            .build(name, factories, valuesSource, searchContext, parent, metadata);
    }

}
