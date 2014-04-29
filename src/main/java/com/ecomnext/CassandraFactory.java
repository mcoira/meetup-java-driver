package com.ecomnext;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Only one cluster object is necessary for the whole project.
 * Only one session per keyspace is necessary for the whole project.
 * The cluster must be closed before exit the app.
 */
public enum CassandraFactory {
    INSTANCE;

    private final Logger logger;
    private Cluster cluster;
    private Session session;

    private CassandraFactory() {
        logger = LoggerFactory.getLogger(CassandraFactory.class);

        // in the cluster object we can define how we must to connect to the Cassandra ring.
        // we will provide authentication, SSL config, retry policies, load balancing policies,
        // and so on.
        // the java driver is able to discover the nodes in the cassandra ring even when
        // you do not provide all of them. Awesome!!
        cluster = new Cluster.Builder()
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .addContactPoints("localhost")
//                .withPort(9142)
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
