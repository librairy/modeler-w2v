package org.librairy.modeler.w2v.helper;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
public interface SparkHelper {

    SparkConf getConf();

    JavaSparkContext getContext();

}
