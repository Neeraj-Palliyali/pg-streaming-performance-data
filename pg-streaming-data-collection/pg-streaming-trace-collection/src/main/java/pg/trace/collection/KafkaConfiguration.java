/*
 * Copyright (c) 2021 fortiss - Research Institute of the Free State of Bavaria
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package pg.trace.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class KafkaConfiguration {

    public enum CollectionSource {
        NAGIOS,
        TELEGRAF,
        NONE
    }

    private final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);
    private String bootstrapServers;
    private String groupId;
    private String inputTopic;
    private int inputTopicPartitions = 1;
    private short inputTopicReplicationFactor = 1;
    private String outputTopic;
    private int outputTopicPartitions = 1;
    private short outputTopicReplicationFactor = 1;
    private CollectionSource collectionSource = CollectionSource.NONE;
    private String schemaRegistryUrl;

    public KafkaConfiguration() {
        initConfig();
        initConfigFromEnvironmentVariables();
        checkConfig();
    }

    public KafkaConfiguration(String bootstrapServers, String groupId, String inputTopic, String outputTopic, CollectionSource collectionSource, String schemaRegistryUrl) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.collectionSource = collectionSource;
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    private void initConfig() {
        log.info("Evaluating configuration file...");
        try (InputStream input = TraceCollectionKafaStreams.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            this.bootstrapServers = prop.getProperty("kafka.bootstrap-servers");
            this.groupId = prop.getProperty("kafka.group-id");
            this.inputTopic = prop.getProperty("kafka.input-topic");
            this.inputTopicPartitions = Integer.parseInt(prop.getProperty("kafka.input-topic-partitions"));
            this.inputTopicReplicationFactor = Short.parseShort(prop.getProperty("kafka.input-topic-replication-factor"));
            this.outputTopic = prop.getProperty("kafka.output-topic");
            this.outputTopicPartitions = Integer.parseInt(prop.getProperty("kafka.output-topic-partitions"));
            this.outputTopicReplicationFactor = Short.parseShort(prop.getProperty("kafka.output-topic-replication-factor"));
            this.collectionSource = this.parseCollectionSource(prop.getProperty("collection.source"));
            this.schemaRegistryUrl = prop.getProperty("schema.registry.url");
        } catch (Exception io) {
            log.info("Could not load complete configuration from file");
        }
    }

    private void initConfigFromEnvironmentVariables() {
        log.info("Evaluating environment variables...");
        // environment variables should override the configuration file (as they do for Spring applications)
        if (System.getenv("kafka.bootstrap-servers") != null) {
            this.bootstrapServers = System.getenv("kafka.bootstrap-servers");
        }
        if (System.getenv("kafka.group-id") != null) {
            this.groupId = System.getenv("kafka.group-id");
        }
        if (System.getenv("kafka.input-topic") != null) {
            this.inputTopic = System.getenv("kafka.input-topic");
        }
        if (System.getenv("kafka.output-topic") != null) {
            this.outputTopic = System.getenv("kafka.output-topic");
        }
        if (System.getenv("collection.source") != null) {
            this.collectionSource = this.parseCollectionSource(System.getenv("collection.source"));
        }
        if (System.getenv("schema.registry.url") != null) {
            this.schemaRegistryUrl = System.getenv("schema.registry.url");
        }
        try {
            if (System.getenv("kafka.input-topic-partitions") != null) {
                this.inputTopicPartitions = Integer.parseInt(System.getenv("kafka.input-topic-partitions"));
            }
            if (System.getenv("kafka.input-topic-replication-factor") != null) {
                this.inputTopicReplicationFactor = Short.parseShort(System.getenv("kafka.input-topic-replication-factor"));
            }
            if (System.getenv("kafka.output-topic-partitions") != null) {
                this.outputTopicPartitions = Integer.parseInt(System.getenv("kafka.output-topic-partitions"));
            }
            if (System.getenv("kafka.output-topic-replication-factor") != null) {
                this.outputTopicReplicationFactor = Short.parseShort(System.getenv("kafka.output-topic-replication-factor"));
            }
        } catch (NumberFormatException nfe) {
            log.info("Could not load parse integer", nfe);
        }
    }

    private CollectionSource parseCollectionSource(String collectionSource) {
        try {
            return CollectionSource.valueOf(collectionSource.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return this.collectionSource;
        }
    }

    private void checkConfig() {
        log.info("Using the following configuration " + this.toString());
        if (this.bootstrapServers == null ||
                this.groupId == null ||
                this.inputTopic == null ||
                this.inputTopicPartitions < 0 ||
                this.inputTopicReplicationFactor < 0 ||
                this.outputTopic == null ||
                this.outputTopicPartitions < 0 ||
                this.outputTopicReplicationFactor < 0 ||
                this.collectionSource == CollectionSource.NONE ||
                this.schemaRegistryUrl == null) {
            throw new RuntimeException("Configuration information is missing. Cannot continue.");
        }
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public int getInputTopicPartitions() {
        return inputTopicPartitions;
    }

    public short getInputTopicReplicationFactor() {
        return inputTopicReplicationFactor;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public int getOutputTopicPartitions() {
        return outputTopicPartitions;
    }

    public short getOutputTopicReplicationFactor() {
        return outputTopicReplicationFactor;
    }

    public CollectionSource getCollectionSource() {
        return collectionSource;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    @Override
    public String toString() {
        return "KafkaConfiguration{" +
                "bootstrapServers='" + bootstrapServers + '\'' +
                ", groupId='" + groupId + '\'' +
                ", inputTopic='" + inputTopic + '\'' +
                ", inputTopicPartitions=" + inputTopicPartitions +
                ", inputTopicReplicationFactor=" + inputTopicReplicationFactor +
                ", outputTopic='" + outputTopic + '\'' +
                ", outputTopicPartitions=" + outputTopicPartitions +
                ", outputTopicReplicationFactor=" + outputTopicReplicationFactor +
                ", collectionSource=" + collectionSource +
                ", schemaRegistryUrl='" + schemaRegistryUrl + '\'' +
                '}';
    }
}
