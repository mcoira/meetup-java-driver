package com.ecomnext;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Only one cluster object is necessary for the whole project.
 * Only one session per keyspace is necessary for the whole project.
 * The cluster must be closed before exit the app.
 */
public enum CassandraFactory {
    INSTANCE;

    private final Logger logger;
    private Properties props = new Properties();
    private Cluster cluster;
    private Session session;

    private CassandraFactory() {
        logger = LoggerFactory.getLogger(CassandraFactory.class);

        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("cassandra-config.properties"));
            cluster = cluster();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        session = this.cluster.connect(props.getProperty("cassandra.keyspace"));

        // shutdownHook to close the cluster
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Closing cassandra cluster ...");
            try {
                cluster.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * In the cluster object we can define how we must to connect to the Cassandra ring.
     * We will provide authentication, SSL config, retry policies, load balancing policies,
     * and so on.
     * The java driver is able to discover the nodes in the cassandra ring even when
     * you do not provide all of them. Awesome!!
     */
    private Cluster cluster() throws Exception {
        logger.info("Cassandra host used to connect: {}", props.getProperty("cassandra.connection.host"));
        logger.info("Cassandra port used to connect: {}", props.getProperty("cassandra.connection.port"));

        Cluster.Builder builder = new Cluster.Builder()
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .addContactPoints(props.getProperty("cassandra.connection.host"))
                .withPort(Integer.parseInt(props.getProperty("cassandra.connection.port")));

        return builder.build();
    }

    public static Session getSession() {
        return INSTANCE.session;
    }
}
