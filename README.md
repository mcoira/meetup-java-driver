

# Cassandra set up.

[Download](http://cassandra.apache.org/download/) the latest version of Cassandra.

Unzip it:

	tar -xvzf apache-cassandra-2.0.7-bin.tar.gz


Edit `CASSANDRA_HOME/conf/cassandra.yaml` and modify the following params:

	data_file_directories: /var/lib/cassandra/data
	commitlog_directory: /var/lib/cassandra/commitlog
	saved_caches_directory: /var/lib/cassandra/saved_caches

Edit `CASSANDRA_HOME/log4j-server.properties` to set the right log file path:

	log4j.appender.R.File=/var/log/cassandra/system.log

# Run Cassandra

	CASSANDRA_HOME/bin/cassandra

# Connect to Cassandra and create the keyspace

	CASSANDRA_HOME/bin/cqlsh
	CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};
	create the tables defined in schema.cql

