package org.librairy.modeler.w2v.helper;

import java.io.File;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
public interface StorageHelper {


    String getPath(String domainUri);

    boolean deleteIfExists(String path);

    boolean save (String path, File file);
}
