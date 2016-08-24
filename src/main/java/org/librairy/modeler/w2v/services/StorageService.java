package org.librairy.modeler.w2v.services;

import lombok.Setter;
import org.librairy.modeler.w2v.helper.HadoopFSHelper;
import org.librairy.modeler.w2v.helper.LocalFSHelper;
import org.librairy.modeler.w2v.helper.StorageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
@Component
public class StorageService {

    @Value("${spark.filesystem}") @Setter
    String fileSystemEndpoint;

    @Value("${librairy.modeler.folder}")
    String modelFolder;


    public StorageHelper getHelper(){

        if (fileSystemEndpoint.startsWith("local"))
            return new LocalFSHelper(fileSystemEndpoint, modelFolder);
        else if (fileSystemEndpoint.startsWith("hdfs"))
            return new HadoopFSHelper(fileSystemEndpoint, modelFolder);
        else
            throw new RuntimeException("FileSystem: " + fileSystemEndpoint + " not handled");
    }

}
