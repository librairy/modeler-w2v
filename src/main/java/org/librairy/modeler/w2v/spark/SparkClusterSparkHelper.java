package org.librairy.modeler.w2v.spark;

import org.apache.spark.SparkConf;
import org.librairy.modeler.w2v.helper.StorageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
@Conditional(SparkClusterCondition.class)
public class SparkClusterSparkHelper extends AbstractSparkHelper {

    @Value("#{environment['LIBRAIRY_SPARK_URI']?:'${librairy.w2v.spark}'}")
    private String master;

    @Override
    protected String getMaster() {
        return master;
    }

    @Override
    protected SparkConf initializeConf(SparkConf conf) {

        return conf;
    }

}
