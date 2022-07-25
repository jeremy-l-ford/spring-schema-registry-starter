package com.github.jeremylford.spring.schemaregistry;

import io.confluent.kafka.schemaregistry.exceptions.SchemaRegistryException;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.storage.KafkaSchemaRegistry;
import io.confluent.rest.Application;
import org.apache.kafka.common.config.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfluentUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfluentUtils.class);

    static KafkaSchemaRegistry.SchemeAndPort getSchemeAndPortForIdentity(int port, List<String> configuredListeners,
                                                                         String requestedScheme)
            throws SchemaRegistryException {
        List<URI> listeners = Application.parseListeners(configuredListeners, port,
                Arrays.asList(
                        SchemaRegistryConfig.HTTP,
                        SchemaRegistryConfig.HTTPS
                ), SchemaRegistryConfig.HTTP
        );
        if (requestedScheme.isEmpty()) {
            requestedScheme = SchemaRegistryConfig.HTTP;
        }
        for (URI listener : listeners) {
            if (requestedScheme.equalsIgnoreCase(listener.getScheme())) {
                return new KafkaSchemaRegistry.SchemeAndPort(listener.getScheme(), listener.getPort());
            }
        }
        throw new SchemaRegistryException(" No listener configured with requested scheme "
                + requestedScheme);
    }

    public static List<NamedURI> parseListeners(
            List<String> listeners,
            Map<String, String> listenerProtocolMap,
            int deprecatedPort,
            List<String> supportedSchemes,
            String defaultScheme) {

        // handle deprecated case, using PORT_CONFIG.
        // TODO: remove this when `PORT_CONFIG` is deprecated, because LISTENER_CONFIG
        // will have a default value which includes the default port.
        if (listeners.isEmpty() || listeners.get(0).isEmpty()) {
            LOGGER.warn(
                    "DEPRECATION warning: `listeners` configuration is not configured. "
                            + "Falling back to the deprecated `port` configuration.");
            listeners = new ArrayList<>(1);
            listeners.add(defaultScheme + "://0.0.0.0:" + deprecatedPort);
        }

        List<NamedURI> uris = listeners.stream()
                .map(listener -> constructNamedURI(listener, listenerProtocolMap, supportedSchemes))
                .collect(Collectors.toList());
        List<NamedURI> namedUris =
                uris.stream().filter(uri -> uri.getName() != null).collect(Collectors.toList());
        List<NamedURI> unnamedUris =
                uris.stream().filter(uri -> uri.getName() == null).collect(Collectors.toList());

        if (namedUris.stream().map(a -> a.getName()).distinct().count() != namedUris.size()) {
            throw new ConfigException(
                    "More than one listener was specified with same name. Listener names must be unique.");
        }
        if (namedUris.isEmpty() && unnamedUris.isEmpty()) {
            throw new ConfigException(
                    "No listeners are configured. At least one listener must be configured.");
        }

        return uris;
    }

    static NamedURI constructNamedURI(
            String listener,
            Map<String, String> listenerProtocolMap,
            List<String> supportedSchemes) {
        URI uri;
        try {
            uri = new URI(listener);
        } catch (URISyntaxException e) {
            throw new ConfigException(
                    "Listener '" + listener + "' is not a valid URI.");
        }
        if (uri.getPort() == -1) {
            throw new ConfigException(
                    "Listener '" + listener + "' must specify a port.");
        }
        if (supportedSchemes.contains(uri.getScheme())) {
            return new NamedURI(uri, null); // unnamed.
        }
        String uriName = uri.getScheme().toLowerCase();
        String protocol = listenerProtocolMap.get(uriName);
        if (protocol == null) {
            throw new ConfigException(
                    "Listener '" + uri + "' has an unsupported scheme '" + uri.getScheme() + "'");
        }
        try {
            return new NamedURI(
                    UriBuilder.fromUri(listener).scheme(protocol).build(),
                    uriName);
        } catch (UriBuilderException e) {
            throw new ConfigException(
                    "Listener '" + listener + "' with protocol '" + protocol + "' is not a valid URI.");
        }
    }

    public static final class NamedURI {
        private final URI uri;
        private final String name;

        NamedURI(URI uri, String name) {
            this.uri = uri;
            this.name = name;
        }

        public URI getUri() {
            return uri;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            if (name == null) {
                return uri.toString();
            }
            return "'" + name + "' " + uri.toString();
        }
    }

}

