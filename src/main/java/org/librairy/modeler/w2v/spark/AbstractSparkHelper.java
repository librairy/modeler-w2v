package org.librairy.modeler.w2v.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.librairy.modeler.w2v.helper.SparkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

/**
 * Created by cbadenes on 11/01/16.
 */
public abstract class AbstractSparkHelper implements SparkHelper{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSparkHelper.class);

    @Value("#{environment['LIBRAIRY_COLUMNDB_HOST']?:'${librairy.columndb.host}'}")
    private String cassandraHost;

    @Value("#{environment['LIBRAIRY_COLUMNDB_PORT']?:${librairy.columndb.port}}")
    private String cassandraPort;

    protected SparkConf conf;

    protected JavaSparkContext sc;


    public JavaSparkContext getContext(){
        return this.sc;
    }

    public SparkConf getConf(){
        return this.conf;
    }


    protected abstract String getMaster();


    protected abstract SparkConf initializeConf(SparkConf conf);

    @PostConstruct
    public void setup(){

        // Initialize Spark Context
        LOG.info("Spark configured at: " + getMaster());
        this.conf = initializeConf(new SparkConf().
                setMaster(getMaster()).
                setAppName("librairy.w2v")
                .set("spark.app.id","librairy.w2v")
                .set("spark.cassandra.connection.host", cassandraHost)
                .set("spark.cassandra.connection.port", cassandraPort)
                .set("spark.driver.maxResultSize","0")
//                .set("spark.executor.extraJavaOptions","-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8095 -Dcom.sun.management.jmxremote.rmi.port=8096 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=138.100.15.128 -Djava.net.preferIPv4Stack=true")
        );
        sc = new JavaSparkContext(conf);
    }

}
