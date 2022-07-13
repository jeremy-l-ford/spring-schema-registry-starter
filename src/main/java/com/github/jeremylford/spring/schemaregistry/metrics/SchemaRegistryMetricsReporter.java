/*
 * Copyright 2019 Jeremy Ford
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS ISBASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jeremylford.spring.schemaregistry.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.Measurable;
import org.apache.kafka.common.metrics.MetricsContext;
import org.apache.kafka.common.metrics.MetricsReporter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class SchemaRegistryMetricsReporter implements MetricsReporter {

    @Override
    public void init(List<KafkaMetric> list) {
        for (KafkaMetric kafkaMetric : list) {
            registerWithMicrometer(kafkaMetric);
        }
    }

    @Override
    public void metricRemoval(KafkaMetric metric) {

    }

    @Override
    public Set<String> reconfigurableConfigs() {
        return MetricsReporter.super.reconfigurableConfigs();
    }

    @Override
    public void validateReconfiguration(Map<String, ?> configs) throws ConfigException {
        MetricsReporter.super.validateReconfiguration(configs);
    }

    @Override
    public void reconfigure(Map<String, ?> configs) {
        MetricsReporter.super.reconfigure(configs);
    }

    @Override
    public void contextChange(MetricsContext metricsContext) {
        MetricsReporter.super.contextChange(metricsContext);
    }

    private void registerWithMicrometer(KafkaMetric kafkaMetric) {
        List<Tag> tags = kafkaMetric.metricName().tags().entrySet().stream()
                .map(x -> Tag.of(x.getKey(), x.getValue())).collect(Collectors.toList());

        Metrics.gauge(kafkaMetric.metricName().name(),
                tags, kafkaMetric, value -> {
                    Object result = value.metricValue();
                    if (result instanceof Double) {
                        return (Double)result;
                    }
                    return 0.0d;
                });
    }

    @Override
    public void metricChange(KafkaMetric kafkaMetric) {
        registerWithMicrometer(kafkaMetric);
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}
