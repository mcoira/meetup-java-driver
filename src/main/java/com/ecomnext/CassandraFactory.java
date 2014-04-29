package com.ecomnext;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public enum CassandraFactory {
    INSTANCE;

    private final Logger logger;
    private Cluster cluster;
    private Session session;

    private CassandraFactory() {
        logger = LoggerFactory.getLogger(CassandraFactory.class);

        cluster = new Cluster.Builder()
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .addContactPoints("127.0.0.1")
                .withPort(9142)
                .build();

        session = this.cluster.connect("test");

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

    public static Session getSession() {
        return INSTANCE.session;
    }
}
