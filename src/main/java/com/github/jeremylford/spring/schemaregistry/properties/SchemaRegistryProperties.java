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
package com.github.jeremylford.spring.schemaregistry.properties;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

import static com.github.jeremylford.spring.schemaregistry.properties.PropertySupport.putArray;
import static com.github.jeremylford.spring.schemaregistry.properties.PropertySupport.putBoolean;
import static com.github.jeremylford.spring.schemaregistry.properties.PropertySupport.putInteger;
import static com.github.jeremylford.spring.schemaregistry.properties.PropertySupport.putString;

@ConfigurationProperties("schemaregistry")
public class SchemaRegistryProperties {

    /**
     * Use this setting to override the group.id for the Kafka group used when Kafka is used for master election.\nWithout this configuration, group.id will be \"schema-registry\". If you want to run more than one schema registry cluster against a single Kafka cluster you should make this setting unique for each cluster.
     */
    private String schemaRegistryGroupId;
    private KafkaStore kafkaStore = new KafkaStore();

    /**
     * If true, this node can participate in master election. In a multi-colo setup, turn this off for clusters in the slave data center.
     */
    private boolean leaderEligibility = SchemaRegistryConfig.DEFAULT_LEADER_ELIGIBILITY;

    /**
     * If true, this node will allow mode changes if it is the master.
     */
    private boolean modeMutability = SchemaRegistryConfig.DEFAULT_MODE_MUTABILITY;

    /**
     * The host name advertised in Zookeeper. Make sure to set this if running SchemaRegistry with multiple nodes.
     */
    private String hostName;

    /**
     * The Avro compatibility type. Valid values are: none (new schema can be any valid Avro schema), backward (new schema can read data produced by latest registered schema), forward (latest registered schema can read data produced by the new schema), full (new schema is backward and forward compatible with latest registered schema)
     */
    private CompatibilityLevel compatibilityLevel = CompatibilityLevel.BACKWARD; //SchemaRegistryConfig.COMPATIBILITY_DEFAULT;

    /**
     * A list of classes to use as SchemaRegistryResourceExtension. Implementing the interface  <code>SchemaRegistryResourceExtension</code> allows you to inject user defined resources  like filters to Schema Registry. Typically used to add custom capability like logging,  security, etc. The schema.registry.resource.extension.class name is deprecated; prefer using resource.extension.class instead.
     */
    private String[] resourceExtensions = new String[0];

    /**
     * A list of classpath resources containing static resources to serve using the default servlet.
     */
    private String[] resourceStaticLocations = new String[0];

    /**
     * The protocol used while making calls between the instances of schema registry. The slave to master node calls for writes and deletes will use the specified protocol. The default value would be `http`. When `https` is set, `ssl.keystore.` and `ssl.truststore.` configs are used while making the call. The schema.registry.inter.instance.protocol name is deprecated; prefer using inter.instance.protocol instead.
     */
    private String innerInstanceProtocol;

    /**
     * A list of ``http`` headers to forward from slave to master, in addition to ``Content-Type``, ``Accept``, ``Authorization``.
     */
    private String[] innerInstanceHeadersWhitelist = new String[0];

    public String getSchemaRegistryGroupId() {
        return schemaRegistryGroupId;
    }

    public void setSchemaRegistryGroupId(String schemaRegistryGroupId) {
        this.schemaRegistryGroupId = schemaRegistryGroupId;
    }

    public KafkaStore getKafkaStore() {
        return kafkaStore;
    }

    public void setKafkaStore(KafkaStore kafkaStore) {
        this.kafkaStore = kafkaStore;
    }

    public boolean isLeaderEligibility() {
        return leaderEligibility;
    }

    public void setLeaderEligibility(boolean leaderEligibility) {
        this.leaderEligibility = leaderEligibility;
    }

    public boolean isModeMutability() {
        return modeMutability;
    }

    public void setModeMutability(boolean modeMutability) {
        this.modeMutability = modeMutability;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public CompatibilityLevel getCompatibilityLevel() {
        return compatibilityLevel;
    }

    public void setCompatibilityLevel(CompatibilityLevel compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
    }

    public String[] getResourceExtensions() {
        return resourceExtensions;
    }

    public void setResourceExtensions(String[] resourceExtensions) {
        this.resourceExtensions = resourceExtensions;
    }

    public String[] getResourceStaticLocations() {
        return resourceStaticLocations;
    }

    public void setResourceStaticLocations(String[] resourceStaticLocations) {
        this.resourceStaticLocations = resourceStaticLocations;
    }

    public String getInnerInstanceProtocol() {
        return innerInstanceProtocol;
    }

    public void setInnerInstanceProtocol(String innerInstanceProtocol) {
        this.innerInstanceProtocol = innerInstanceProtocol;
    }

    public String[] getInnerInstanceHeadersWhitelist() {
        return innerInstanceHeadersWhitelist;
    }

    public void setInnerInstanceHeadersWhitelist(String[] innerInstanceHeadersWhitelist) {
        this.innerInstanceHeadersWhitelist = innerInstanceHeadersWhitelist;
    }

    public Properties asProperties() {
        Properties properties = new Properties();

        properties.putAll(kafkaStore.asProperties());
        putBoolean(properties, SchemaRegistryConfig.MASTER_ELIGIBILITY, leaderEligibility);
        putBoolean(properties, SchemaRegistryConfig.MODE_MUTABILITY, modeMutability);
        putString(properties, SchemaRegistryConfig.HOST_NAME_CONFIG, hostName);
        putString(properties, SchemaRegistryConfig.COMPATIBILITY_CONFIG, compatibilityLevel.name());
        putArray(properties, SchemaRegistryConfig.RESOURCE_EXTENSION_CONFIG, resourceExtensions);
        putArray(properties, SchemaRegistryConfig.RESOURCE_STATIC_LOCATIONS_CONFIG, resourceStaticLocations);
        putString(properties, SchemaRegistryConfig.INTER_INSTANCE_PROTOCOL_CONFIG, innerInstanceProtocol);
//        putArray(properties, SchemaRegistryConfig.INTER_INSTANCE_HEADERS_WHITELIST_CONFIG, innerInstanceHeadersWhitelist);


        /*
        private static final String SCHEMAREGISTRY_LISTENERS_DEFAULT = "";

        protected static final String KAFKASTORE_SECURITY_PROTOCOL_DOC = "The security protocol to use when connecting with Kafka, the underlying persistent storage. Values can be `PLAINTEXT`, `SSL`, `SASL_PLAINTEXT`, or `SASL_SSL`.";
        protected static final String KAFKASTORE_SSL_TRUSTSTORE_LOCATION_DOC = "The location of the SSL trust store file.";
        protected static final String KAFKASTORE_SSL_TRUSTSTORE_PASSWORD_DOC = "The password to access the trust store.";
        protected static final String KAFAKSTORE_SSL_TRUSTSTORE_TYPE_DOC = "The file format of the trust store.";
        protected static final String KAFKASTORE_SSL_TRUSTMANAGER_ALGORITHM_DOC = "The algorithm used by the trust manager factory for SSL connections.";
        protected static final String KAFKASTORE_SSL_KEYSTORE_LOCATION_DOC = "The location of the SSL keystore file.";
        protected static final String KAFKASTORE_SSL_KEYSTORE_PASSWORD_DOC = "The password to access the keystore.";
        protected static final String KAFAKSTORE_SSL_KEYSTORE_TYPE_DOC = "The file format of the keystore.";
        protected static final String KAFKASTORE_SSL_KEYMANAGER_ALGORITHM_DOC = "The algorithm used by key manager factory for SSL connections.";
        protected static final String KAFKASTORE_SSL_KEY_PASSWORD_DOC = "The password of the key contained in the keystore.";
        protected static final String KAFAKSTORE_SSL_ENABLED_PROTOCOLS_DOC = "Protocols enabled for SSL connections.";
        protected static final String KAFAKSTORE_SSL_PROTOCOL_DOC = "The SSL protocol used.";
        protected static final String KAFAKSTORE_SSL_PROVIDER_DOC = "The name of the security provider used for SSL.";
        protected static final String KAFKASTORE_SSL_CIPHER_SUITES_DOC = "A list of cipher suites used for SSL.";
        protected static final String KAFKASTORE_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_DOC = "The endpoint identification algorithm to validate the server hostname using the server certificate.";
        public static final String KAFKASTORE_SASL_KERBEROS_SERVICE_NAME_DOC = "The Kerberos principal name that the Kafka client runs as. This can be defined either in the JAAS config file or here.";
        public static final String KAFKASTORE_SASL_MECHANISM_DOC = "The SASL mechanism used for Kafka connections. GSSAPI is the default.";
        public static final String KAFKASTORE_SASL_KERBEROS_KINIT_CMD_DOC = "The Kerberos kinit command path.";
        public static final String KAFKASTORE_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN_DOC = "The login time between refresh attempts.";
        public static final String KAFKASTORE_SASL_KERBEROS_TICKET_RENEW_JITTER_DOC = "The percentage of random jitter added to the renewal time.";
        public static final String KAFKASTORE_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR_DOC = "Login thread will sleep until the specified window factor of time from last refresh to ticket's expiry has been reached, at which time it will try to renew the ticket.";
        private static final String METRICS_JMX_PREFIX_DEFAULT_OVERRIDE = "kafka.schema.registry";
        private static final ConfigDef config = baseSchemaRegistryConfigDef();
        private ZkUtils zkUtils;


        public static final String KAFKASTORE_SECURITY_PROTOCOL_CONFIG = "kafkastore.security.protocol";
        public static final String KAFKASTORE_SSL_TRUSTSTORE_LOCATION_CONFIG = "kafkastore.ssl.truststore.location";
        public static final String KAFKASTORE_SSL_TRUSTSTORE_PASSWORD_CONFIG = "kafkastore.ssl.truststore.password";
        public static final String KAFKASTORE_SSL_KEYSTORE_LOCATION_CONFIG = "kafkastore.ssl.keystore.location";
        public static final String KAFKASTORE_SSL_TRUSTSTORE_TYPE_CONFIG = "kafkastore.ssl.truststore.type";
        public static final String KAFKASTORE_SSL_TRUSTMANAGER_ALGORITHM_CONFIG = "kafkastore.ssl.trustmanager.algorithm";
        public static final String KAFKASTORE_SSL_KEYSTORE_PASSWORD_CONFIG = "kafkastore.ssl.keystore.password";
        public static final String KAFKASTORE_SSL_KEYSTORE_TYPE_CONFIG = "kafkastore.ssl.keystore.type";
        public static final String KAFKASTORE_SSL_KEYMANAGER_ALGORITHM_CONFIG = "kafkastore.ssl.keymanager.algorithm";
        public static final String KAFKASTORE_SSL_KEY_PASSWORD_CONFIG = "kafkastore.ssl.key.password";
        public static final String KAFKASTORE_SSL_ENABLED_PROTOCOLS_CONFIG = "kafkastore.ssl.enabled.protocols";
        public static final String KAFKASTORE_SSL_PROTOCOL_CONFIG = "kafkastore.ssl.protocol";
        public static final String KAFKASTORE_SSL_PROVIDER_CONFIG = "kafkastore.ssl.provider";
        public static final String KAFKASTORE_SSL_CIPHER_SUITES_CONFIG = "kafkastore.ssl.cipher.suites";
        public static final String KAFKASTORE_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG = "kafkastore.ssl.endpoint.identification.algorithm";
        public static final String KAFKASTORE_SASL_KERBEROS_SERVICE_NAME_CONFIG = "kafkastore.sasl.kerberos.service.name";
        public static final String KAFKASTORE_SASL_MECHANISM_CONFIG = "kafkastore.sasl.mechanism";
        public static final String KAFKASTORE_SASL_KERBEROS_KINIT_CMD_CONFIG = "kafkastore.sasl.kerberos.kinit.cmd";
        public static final String KAFKASTORE_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN_CONFIG = "kafkastore.sasl.kerberos.min.time.before.relogin";
        public static final String KAFKASTORE_SASL_KERBEROS_TICKET_RENEW_JITTER_CONFIG = "kafkastore.sasl.kerberos.ticket.renew.jitter";
        public static final String KAFKASTORE_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR_CONFIG = "kafkastore.sasl.kerberos.ticket.renew.window.factor";


         */


        return properties;
    }

    public static class KafkaStore {
        /**
         * Zookeeper URL for the Kafka cluster
         */
        private String connectionUrl;

        /**
         * A list of Kafka brokers to connect to. For example, `PLAINTEXT://hostname:9092,SSL://hostname2:9092`
         * <p>
         * The effect of this setting depends on whether you specify `kafkastore.connection.url`.
         * If `kafkastore.connection.url` is not specified, then the Kafka cluster containing these bootstrap servers will be used both to coordinate schema registry instances (master election) and store schema data.
         * If `kafkastore.connection.url` is specified, then this setting is used to control how the schema registry connects to Kafka to store schema data and is particularly important when Kafka security is enabled. When this configuration is not specified, the Schema Registry's internal Kafka clients will get their Kafka bootstrap server list from ZooKeeper (configured with `kafkastore.connection.url`). In that case, all available listeners matching the `kafkastore.security.protocol` setting will be used.
         * By specifiying this configuration, you can control which endpoints are used to connect to Kafka. Kafka may expose multiple endpoints that all will be stored in ZooKeeper, but the Schema Registry may need to be configured with just one of those endpoints, for example to control which security protocol it uses.
         */
        private String[] bootstrapServers = new String[0];

        /**
         * Use this setting to override the group.id for the KafkaStore consumer.
         * This setting can become important when security is enabled, to ensure stability over the schema registry consumer's group.id
         * Without this configuration, group.id will be "schema-registry-<host>-<port>"
         */
        private String groupId;

        /**
         * The durable single partition topic that actsas the durable log for the data
         */
        private String topic = SchemaRegistryConfig.DEFAULT_KAFKASTORE_TOPIC;

        /**
         * The desired replication factor of the schema topic. The actual replication factor will be the smaller of this value and the number of live Kafka brokers.
         */
        private int topicReplicationFactor = SchemaRegistryConfig.DEFAULT_KAFKASTORE_TOPIC_REPLICATION_FACTOR;

        /**
         * The timeout for an operation on the Kafka store
         */
        private int timeout = 500;

        /**
         * The timeout for initialization of the Kafka store, including creation of the Kafka topic that stores schema data.
         */
        private int initTimeout = 60000;

        public String getConnectionUrl() {
            return connectionUrl;
        }

        public void setConnectionUrl(String connectionUrl) {
            this.connectionUrl = connectionUrl;
        }

        public String[] getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String[] bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public int getTopicReplicationFactor() {
            return topicReplicationFactor;
        }

        public void setTopicReplicationFactor(int topicReplicationFactor) {
            this.topicReplicationFactor = topicReplicationFactor;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getInitTimeout() {
            return initTimeout;
        }

        public void setInitTimeout(int initTimeout) {
            this.initTimeout = initTimeout;
        }

        public Properties asProperties() {
            Properties properties = new Properties();
            putString(properties, SchemaRegistryConfig.KAFKASTORE_CONNECTION_URL_CONFIG, connectionUrl);
            putArray(properties, SchemaRegistryConfig.KAFKASTORE_BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            putString(properties, SchemaRegistryConfig.KAFKASTORE_TOPIC_CONFIG, topic);
            putInteger(properties, SchemaRegistryConfig.KAFKASTORE_TOPIC_REPLICATION_FACTOR_CONFIG, topicReplicationFactor);
            putInteger(properties, SchemaRegistryConfig.KAFKASTORE_INIT_TIMEOUT_CONFIG, initTimeout);
            putInteger(properties, SchemaRegistryConfig.KAFKASTORE_TIMEOUT_CONFIG, timeout);

            return properties;
        }

        protected static final String KAFKASTORE_WRITE_RETRIES_DOC = "Retry a failed register schema request to the underlying Kafka store up to this many times,  for example in case of a Kafka broker failure";
        protected static final String KAFKASTORE_WRITE_RETRY_BACKOFF_MS_DOC = "The amount of time in milliseconds to wait before attempting to retry a failed write to the Kafka store";

    }
}
