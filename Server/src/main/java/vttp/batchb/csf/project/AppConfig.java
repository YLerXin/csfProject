package vttp.batchb.csf.project;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


@Configuration
public class AppConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean("redis-string")
    public RedisTemplate<String, String> createRedisTemplate() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);

        if (!redisUsername.trim().isEmpty()) {
            config.setUsername(redisUsername);
            config.setPassword(redisPassword);
        }

        JedisClientConfiguration jedisClient = JedisClientConfiguration.builder().build();
        JedisConnectionFactory jedisFactory = new JedisConnectionFactory(config, jedisClient);
        jedisFactory.afterPropertiesSet();

        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
    @Value("${do.storage.key}")
    private String accessKey;

    @Value("${do.storage.secret}")
    private String secretKey;

    @Value("${do.storage.endpoint}")
    private String endPoint;

    @Value("${do.storage.endpoint.region}")
    private String endPointRegion;


    @Bean
    public AmazonS3 createS3Client() {
        System.out.println("Creating S3 client");
        System.out.println("accessKey = " + accessKey);
        System.out.println("endPoint = " + endPoint);
        System.out.println("secretKey = " + secretKey);
        System.out.println("endPointRegion = " + endPointRegion);
        
        
        BasicAWSCredentials crd
                 = new BasicAWSCredentials(accessKey, secretKey);
        EndpointConfiguration endpoint
                 = new EndpointConfiguration(endPoint, endPointRegion);
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(crd))
                .build();
    }
}