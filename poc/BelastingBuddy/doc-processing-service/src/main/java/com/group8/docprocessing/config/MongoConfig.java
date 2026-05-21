package com.group8.docprocessing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MongoConfig {

    /**
    * The name of the GridFS bucket to use for storing files, sourced from application properties.
    */
    @Value("${spring.data.mongodb.gridfs.bucket}")
    private String gridFsBucketName;

    @Bean
    public GridFsTemplate gridFsTemplate(MongoDatabaseFactory mongoDbFactory, MongoConverter converter) {
        return new GridFsTemplate(mongoDbFactory, converter, gridFsBucketName);
    }
}