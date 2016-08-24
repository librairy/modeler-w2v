package org.librairy.modeler.w2v.helper;

import lombok.Getter;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
public class SparkHelper {

    @Value("${spark.master}")
    private String master;

    @Value("${librairy.cassandra.contactpoints}")
    private String cassandraHost;

    @Value("${librairy.cassandra.port}")
    private String cassandraPort;

    private SparkConf conf;

    @Getter
    private JavaSparkContext sc;


    @PostConstruct
    public void setup(){

        // Initialize Spark Context
        this.conf = new SparkConf().
                setMaster(master).
                setAppName("librairy-w2v")
                .set("spark.cassandra.connection.host", cassandraHost)
                .set("spark.cassandra.connection.port", cassandraPort)
                //.set("spark.executor.memory", memory)
                .set("spark.driver.maxResultSize","0");
        sc = new JavaSparkContext(conf);
    }

}
