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
package com.github.jeremylford.spring.schemaregistry;

import com.github.jeremylford.spring.schemaregistry.metrics.SchemaRegistryMetricsReporter;
import com.github.jeremylford.spring.schemaregistry.properties.SchemaRegistryProperties;
import io.confluent.kafka.schemaregistry.exceptions.SchemaRegistryException;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.storage.KafkaSchemaRegistry;
import io.confluent.kafka.schemaregistry.storage.serialization.SchemaRegistrySerializer;
import io.confluent.rest.RestConfigException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.Properties;

@EnableConfigurationProperties({SchemaRegistryProperties.class})
@Configuration
@Import(JerseyConfiguration.class)
public class SchemaRegistryAutoConfiguration {

    @Bean
    public SchemaRegistryConfig schemaRegistryConfig(SchemaRegistryProperties schemaRegistryProperties) throws RestConfigException {
        Properties properties = schemaRegistryProperties.asProperties();

        properties.put(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG, Collections.singletonList(
                SchemaRegistryMetricsReporter.class.getName()
        ));
        return new SchemaRegistryConfig(properties);
    }

    @Bean
    public KafkaSchemaRegistry kafkaSchemaRegistry(SchemaRegistryConfig schemaRegistryConfig) throws SchemaRegistryException {
        KafkaSchemaRegistry kafkaSchemaRegistry = new KafkaSchemaRegistry(
                schemaRegistryConfig, new SchemaRegistrySerializer()
        );
        kafkaSchemaRegistry.init(); //TODO: consider life cycle wrapper
        return kafkaSchemaRegistry;
    }
}
