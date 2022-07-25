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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import io.confluent.kafka.schemaregistry.exceptions.SchemaRegistryException;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.extensions.SchemaRegistryResourceExtension;
import io.confluent.kafka.schemaregistry.rest.filters.ContextFilter;
import io.confluent.kafka.schemaregistry.rest.filters.RestCallMetricFilter;
import io.confluent.kafka.schemaregistry.storage.KafkaSchemaRegistry;
import io.confluent.rest.Application;
import io.confluent.rest.RestConfig;
import io.confluent.rest.exceptions.ConstraintViolationExceptionMapper;
import io.confluent.rest.exceptions.GenericExceptionMapper;
import io.confluent.rest.exceptions.WebApplicationExceptionMapper;
import io.confluent.rest.metrics.MetricsResourceMethodApplicationListener;
import io.confluent.rest.validation.JacksonMessageBodyProvider;
import org.apache.kafka.common.metrics.JmxReporter;
import org.apache.kafka.common.metrics.KafkaMetricsContext;
import org.apache.kafka.common.metrics.MetricConfig;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.validation.ValidationFeature;
import org.glassfish.jersey.servlet.init.FilterUrlMappingsProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Configurable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@AutoConfigureBefore(JerseyAutoConfiguration.class)
public class JerseyConfiguration extends ResourceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyConfiguration.class);

    @Autowired
    public JerseyConfiguration(KafkaSchemaRegistry kafkaSchemaRegistry,
                               SchemaRegistryConfig schemaRegistryConfig) {
        register(new io.confluent.kafka.schemaregistry.rest.resources.CompatibilityResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.ConfigResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.ContextsResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.ModeResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.RootResource());
        register(new io.confluent.kafka.schemaregistry.rest.resources.SchemasResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.ServerMetadataResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.SubjectsResource(kafkaSchemaRegistry));
        register(new io.confluent.kafka.schemaregistry.rest.resources.SubjectVersionsResource(kafkaSchemaRegistry));

        register(new ConstraintViolationExceptionMapper());
        register(new WebApplicationExceptionMapper(schemaRegistryConfig));
        register(new GenericExceptionMapper(schemaRegistryConfig));

        register(new ContextFilter());

        register(new RestCallMetricFilter(
                kafkaSchemaRegistry.getMetricsContainer().getApiCallsSuccess(),
                kafkaSchemaRegistry.getMetricsContainer().getApiCallsFailure()));

        register(new FilterUrlMappingsProviderImpl());
        property("jersey.config.beanValidation.enableOutputValidationErrorEntity.server", true);
        property("jersey.config.server.wadl.disableWadl", true);

        configureMetrics(schemaRegistryConfig);
        registerJsonProvider(this, schemaRegistryConfig, true);
        registerFeatures(this, schemaRegistryConfig);

        List<SchemaRegistryResourceExtension> restResourceExtensions = schemaRegistryConfig.getConfiguredInstances(
                schemaRegistryConfig.definedResourceExtensionConfigName(),
                SchemaRegistryResourceExtension.class
        );
        for (SchemaRegistryResourceExtension restResourceExtension : restResourceExtensions) {
            try {
                restResourceExtension.register(this, schemaRegistryConfig, kafkaSchemaRegistry);
            } catch (SchemaRegistryException e) {
                LOGGER.error("Failed to register resource extension {}", restResourceExtension.getClass(), e);
                System.exit(1);
            }
        }
    }

    protected void registerJsonProvider(Configurable<?> config, SchemaRegistryConfig restConfig, boolean registerExceptionMapper) {
        ObjectMapper jsonMapper = new ObjectMapper();
        JacksonMessageBodyProvider jsonProvider = new JacksonMessageBodyProvider(jsonMapper);
        config.register(jsonProvider);
        if (registerExceptionMapper) {
            config.register(JsonParseExceptionMapper.class);
        }
    }

    protected void registerFeatures(Configurable<?> config, SchemaRegistryConfig restConfig) {
        config.register(ValidationFeature.class);
    }

    private void configureMetrics(SchemaRegistryConfig schemaRegistryConfig) {
        //TODO: verify metric configuration
        MetricConfig metricConfig = new MetricConfig()
                .samples(schemaRegistryConfig.getInt(RestConfig.METRICS_NUM_SAMPLES_CONFIG))
                .timeWindow(schemaRegistryConfig.getLong(RestConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG),
                        TimeUnit.MILLISECONDS);

        List<MetricsReporter> reporters =
                schemaRegistryConfig.getConfiguredInstances(RestConfig.METRICS_REPORTER_CLASSES_CONFIG,
                        MetricsReporter.class);

        MetricsReporter metricsReporter = new JmxReporter();
        metricsReporter.contextChange(new KafkaMetricsContext(
                schemaRegistryConfig.getString(RestConfig.METRICS_JMX_PREFIX_CONFIG)
        ));
        reporters.add(metricsReporter);
        Metrics metrics = new Metrics(
                metricConfig, reporters, schemaRegistryConfig.getTime());

        Map<String, String> configuredTags = Application.parseListToMap(
                schemaRegistryConfig.getList(RestConfig.METRICS_TAGS_CONFIG)
        );

        register(new MetricsResourceMethodApplicationListener(metrics, "jersey",
                configuredTags, schemaRegistryConfig.getTime()));
    }
}
