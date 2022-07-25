package com.github.jeremylford.spring.schemaregistry;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.google.common.collect.ImmutableList;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

//@SpringBootTest(
//        classes = SchemaRegistryApplication.class,
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        properties = {
//                "schemaregistry.kafkastore.bootstrapServers=localhost:9092"
//        }
//)
//@ContextConfiguration(
//        initializers = SchemaRegistryIntegrationTest.RedpandaContextInitializer.class
//)
//@ExtendWith(SpringExtension.class)
public class SchemaRegistryIntegrationTest {

//    @Autowired
//    private ServletWebServerApplicationContext webServerAppCtxt;

    private final RedpandaContainer container = new RedpandaContainer();

    @BeforeEach
    public void setup() {
        container.start();
        List<String> args = ImmutableList.of(
                "--schemaregistry.kafkastore.bootstrapServers=localhost:" + container.getMappedPort(9092),
                "--server.port=8080"
        );
        SpringApplication.run(SchemaRegistryApplication.class, args.toArray(new String[0]));
    }

    @AfterEach
    public void after() {
        container.stop();
    }

    @Test
    public void test() throws RestClientException, IOException {
//        int port = webServerAppCtxt.getWebServer().getPort();
//        System.out.println(port);



        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(
                "http://localhost:8080/api", 10
        );
        int id = schemaRegistryClient.register("tes", new JsonSchema("{}"), false);
        System.out.println(id);
    }

    static class RedpandaContainer extends GenericContainer<RedpandaContainer> {

        private static final String STARTER_SCRIPT = "/testcontainers_start.sh";

        public RedpandaContainer() {
            super("vectorized/redpanda:v21.11.15");

            withExposedPorts(9092, 9092);
            withCreateContainerCmdModifier(cmd -> {
                cmd.withEntrypoint("sh");
            });

            withCommand("-c", "while [ ! -f " + STARTER_SCRIPT + " ]; do sleep 0.1; done; " + STARTER_SCRIPT);
            waitingFor(Wait.forLogMessage(".*Started Kafka API server.*", 1));
        }

        @Override
        protected void containerIsStarting(InspectContainerResponse containerInfo) {
            super.containerIsStarting(containerInfo);

            String command = "#!/bin/bash\n";

            command += "/usr/bin/rpk redpanda start --check=false --node-id 0 ";
            command += "--kafka-addr PLAINTEXT://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092 ";
            command += "--advertise-kafka-addr PLAINTEXT://kafka:29092,OUTSIDE://" + getHost() + ":" + getMappedPort(9092);

//            command += "redpanda start --smp 1 --reserve-memory 0M --overprovisioned " +
//                    "--node-id 0 " +
//                    "--kafka-addr PLAINTEXT://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092 " +
//                    "--advertise-kafka-addr PLAINTEXT://redpanda:29092,OUTSIDE://localhost:9092 " +
//                    "--pandaproxy-addr PLAINTEXT://0.0.0.0:28082,OUTSIDE://0.0.0.0:8082 " +
//                    "--advertise-pandaproxy-addr PLAINTEXT://redpanda:28082,OUTSIDE://localhost:8082";

            copyFileToContainer(
                    Transferable.of(command.getBytes(StandardCharsets.UTF_8), 0777),
                    STARTER_SCRIPT
            );
        }
    }

    public static class RedpandaContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private final RedpandaContainer container = new RedpandaContainer();
        private static final AtomicInteger KAFKA_PORT = new AtomicInteger();

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            container.start();
//            System.out.println(container.getBoundPortNumbers());
//            System.out.println(container.getFirstMappedPort());
//            System.out.println(container.getExposedPorts());
//            System.out.println(container.getPortBindings());
            KAFKA_PORT.set(container.getMappedPort(9092));

            try {
                AdminClient adminClient = AdminClient.create(ImmutableMap.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:60474"));
                System.out.println("Admin Topics");
                adminClient.listTopics().listings().get().forEach(System.out::println);
                System.out.println("done");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            applicationContext.addApplicationListener(event -> {
                if (event instanceof ContextStartedEvent) {

                }
                if (event instanceof ContextStoppedEvent) {
                    container.stop();
                }
            });
        }
    }
}
