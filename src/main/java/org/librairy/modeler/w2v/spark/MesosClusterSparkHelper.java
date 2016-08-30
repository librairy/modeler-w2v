package org.librairy.modeler.w2v.spark;

import org.apache.mesos.MesosNativeLibrary;
import org.apache.spark.SparkConf;
import org.librairy.modeler.w2v.helper.OSHelper;
import org.librairy.modeler.w2v.helper.StorageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
@Conditional(MesosClusterCondition.class)
public class MesosClusterSparkHelper extends AbstractSparkHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MesosClusterSparkHelper.class);

    @Value("#{environment['LIBRAIRY_SPARK_URI']?:'${librairy.w2v.spark}'}")
    private String master;

    @Value("#{environment['MESOS_SPARK_HOME']?:'/var/lib/spark'}")
    private String mesosHome;

    @Value("#{environment['SPARK_MEMORY']?:'-1'}")
    private String mesosMem;

    @Value("#{environment['MESOS_USER_NAME']?:'-1'}")
    private String mesosUserName;

    @Value("#{environment['MESOS_USER_PWD']?:'-1'}")
    private String mesosUserPwd;

    @Value("#{environment['MESOS_USER_ROLE']?:'-1'}")
    private String mesosUserRole;

    @Override
    protected String getMaster() {
        return master;
    }

    @Autowired
    StorageHelper storageHelper;

    @Override
    protected SparkConf initializeConf(SparkConf conf) {

        String homePath     = storageHelper.getHome();
        LOG.info("librairy home=" + homePath);

        try {
            String extension = OSHelper.isMac()? "dylib" : "so";
            String nativeLibPath = homePath + "lib/libmesos." + extension;
            LOG.info("loading MESOS_NATIVE_LIB from: " + nativeLibPath);
            MesosNativeLibrary.load(storageHelper.read(homePath + "lib/libmesos." + extension).getAbsolutePath());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }


        String sparkPath    = storageHelper.absolutePath(homePath+"lib/spark-1.5.2-bin-hadoop2.6.tgz");
        LOG.info("loading SPARK_EXECUTOR_URI from: " + sparkPath);

        String libPath      = storageHelper.absolutePath(homePath+"lib/librairy-dependencies.jar");
        LOG.info("loading librairy dependencies from: " + libPath);

        LOG.info("loading MESOS_SPARK_HOME from: " + mesosHome);

        SparkConf auxConf = conf
                .set("spark.executor.uri", sparkPath)
                .set("spark.mesos.executor.home", mesosHome)
                .setJars(new String[]{libPath});

        if (!mesosMem.equalsIgnoreCase("-1")) {
            LOG.info("setting 'spark.executor.memory="+mesosMem+"'");
            auxConf = auxConf.set("spark.executor.memory", mesosMem);
        }

        if (!mesosUserName.equalsIgnoreCase("-1")){
            LOG.info("setting 'spark.mesos.principal="+mesosUserName+"'");
            auxConf = auxConf.set("spark.mesos.principal", mesosUserName);
        }

        if (!mesosUserPwd.equalsIgnoreCase("-1")){
            LOG.info("setting 'spark.mesos.secret="+mesosUserPwd+"'");
            auxConf = auxConf.set("spark.mesos.secret", mesosUserPwd);
        }

        if (!mesosUserRole.equalsIgnoreCase("-1")){
            LOG.info("setting 'spark.mesos.role="+mesosUserRole+"'");
            auxConf = auxConf.set("spark.mesos.role", mesosUserRole);
        }

        return auxConf;
    }

}
