package org.librairy.modeler.w2v.storage;

import org.librairy.modeler.w2v.helper.StorageHelper;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created on 24/08/16:
 *
 * @author cbadenes
 */
public abstract class AbstractStorage implements StorageHelper{

    // hdfs://zavijava.dia.fi.upm.es:8020
    @Value("#{environment['LIBRAIRY_FS']?:'${librairy.w2v.fs}'}")
    protected String fileSystem;

    @Value("#{environment['LIBRAIRY_HOME']?:'${librairy.w2v.home}'}")
    protected String homeFolder;

    protected String normalizedHome(String separator){
        if (homeFolder.startsWith(separator) && homeFolder.endsWith(separator)){
            return homeFolder;
        }else if (homeFolder.startsWith(separator)){
            return homeFolder + separator;
        }else if (homeFolder.endsWith(separator)){
            return separator + homeFolder;
        }else{
            return separator + homeFolder + separator;
        }
    }

}
