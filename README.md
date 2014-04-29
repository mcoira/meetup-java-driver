# Summary

This project shows many of the configuration possibilities of Cassandra Java Driver as well as
many different ways to execute statements.

The whole list of topics:

*	Connection to Cassandra ring and config params
*	Synchronous statement execution
*	Asynchronous statement execution
*	Programming statements with QueryBuilder
*	Prepared statements
*	Batches - Atomic, Non-atomic, and Counters
*	Reactive programming with ListenableFuture
*	Testing with an embedded Cassandra

# Cassandra set up.

## Cassandra install and config

[Download](http://cassandra.apache.org/download/) the latest version of Cassandra.

Unzip it:

	tar -xvzf apache-cassandra-X.Y.Z-bin.tar.gz


Edit `CASSANDRA_HOME/conf/cassandra.yaml` and modify the paths to point your filesystem:

	data_file_directories: /var/lib/cassandra/data
	commitlog_directory: /var/lib/cassandra/commitlog
	saved_caches_directory: /var/lib/cassandra/saved_caches

Edit `CASSANDRA_HOME/log4j-server.properties` to set the right log file path:

	log4j.appender.R.File=/var/log/cassandra/system.log

## Run Cassandra

	CASSANDRA_HOME/bin/cassandra

## Connect to Cassandra and create the keyspace

	CASSANDRA_HOME/bin/cqlsh
	CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};
	use test;
	[create the tables defined in schema.cql]

