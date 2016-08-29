package org.librairy.modeler.w2v.helper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
public interface StorageHelper {

    String getHome();

    String path(String domainId, String fileName);

    String absolutePath(String path);

    boolean deleteIfExists(String path);

    boolean save (String path, File file);

    boolean exists(String path);

    File read (String path) throws URISyntaxException, IOException;
}
