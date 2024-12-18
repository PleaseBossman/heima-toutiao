package com.heima.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过重新注册KafkaStreamsConfiguration对象，设置自定配置参数
 */

/**
 * @Setter：使用 Lombok 注解，自动为类中的所有字段生成 setter 方法。
 * @Getter：使用 Lombok 注解，自动为类中的所有字段生成 getter 方法。
 * @Configuration：将该类标记为 Spring 配置类，使其可以定义 @Bean 方法来创建和配置 Spring 容器中的对象。
 * @EnableKafkaStreams：启用 Kafka Streams 的支持，使 Spring Boot 能够自动配置和管理 Kafka Streams 的相关 Bean。
 * @ConfigurationProperties(prefix="kafka")：从配置文件中读取以 kafka 为前缀的配置项，将它们映射到这个类的字段中。
 */
@Setter
@Getter
@Configuration
@EnableKafkaStreams
@ConfigurationProperties(prefix="kafka")
public class KafkaStreamConfig {
    private static final int MAX_MESSAGE_SIZE = 16* 1024 * 1024;
    private String hosts;
    private String group;
    //name参数将此配置 Bean 命名为：kafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration defaultKafkaStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getGroup()+"_stream_aid");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, this.getGroup()+"_stream_cid");
        props.put(StreamsConfig.RETRIES_CONFIG, 10);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return new KafkaStreamsConfiguration(props);
        //创建并返回一个 KafkaStreamsConfiguration 对象，使用上面定义的配置属性 props。这个配置对象将被 Kafka Streams 使用
    }
}