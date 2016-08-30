package org.librairy.modeler.w2v.spark;

import org.apache.spark.SparkConf;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
@Conditional(LocalSparkCondition.class)
public class LocalSparkHelper extends AbstractSparkHelper {

    @Override
    protected String getMaster() {
        return "local[*]";
    }

    @Override
    protected SparkConf initializeConf(SparkConf conf) {
        return conf;
    }

}
