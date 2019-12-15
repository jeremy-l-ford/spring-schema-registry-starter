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

import io.confluent.kafka.schemaregistry.exceptions.SchemaRegistryException;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.extensions.SchemaRegistryResourceExtension;
import io.confluent.kafka.schemaregistry.storage.KafkaSchemaRegistry;
import io.confluent.rest.exceptions.ConstraintViolationExceptionMapper;
import io.confluent.rest.exceptions.KafkaExceptionMapper;
import io.confluent.rest.exceptions.WebApplicationExceptionMapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.init.FilterUrlMappingsProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AutoConfigureBefore(JerseyAutoConfiguration.class)
public class JerseyConfiguration extends ResourceConfig {

    @Autowired
    public JerseyConfiguration(KafkaSchemaRegistry kafkaSchemaRegistry,
                               SchemaRegistryConfig schemaRegistryConfig) {
        register(new io.confluent.kafka.schemaregistry.rest.resources.CompatibilityResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.ModeResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.RootResource());
        register(new io.confluent.kafka.schemaregistry.rest.resources.SchemasResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.SubjectsResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.SubjectVersionsResource(kafkaSchemaRegistry));

        register(new ConstraintViolationExceptionMapper());
        register(new WebApplicationExceptionMapper(schemaRegistryConfig));
        register(new KafkaExceptionMapper(schemaRegistryConfig));

        register(new FilterUrlMappingsProviderImpl());

        List<SchemaRegistryResourceExtension> restResourceExtensions = schemaRegistryConfig.getConfiguredInstances(
                schemaRegistryConfig.definedResourceExtensionConfigName(),
                SchemaRegistryResourceExtension.class
        );
        for (SchemaRegistryResourceExtension restResourceExtension : restResourceExtensions) {
            try {
                restResourceExtension.register(this, schemaRegistryConfig, kafkaSchemaRegistry);
            } catch (SchemaRegistryException e) {
                System.exit(1);
            }
        }
    }
}
