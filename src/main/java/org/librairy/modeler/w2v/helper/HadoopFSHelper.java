package org.librairy.modeler.w2v.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
public class HadoopFSHelper implements StorageHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HadoopFSHelper.class);

    private final String baseDir;

    private final String endpoint;

    public HadoopFSHelper(String endpoint, String baseDir){
        this.baseDir = baseDir.startsWith("/")? baseDir.substring(1,baseDir.length()) : baseDir ;
        this.endpoint = (endpoint.endsWith("/"))? endpoint.substring(0,endpoint.length()-2) : endpoint;
    }

    @Override
    public String getPath(String id) {
        String folderName = (id.startsWith("http"))? URIGenerator.retrieveId(id) : id;
        return endpoint + "/" + baseDir + "/" + folderName;
    }

    @Override
    /**
     * set the HADOOP_USER_NAME environment variable when specific user
     */
    public boolean deleteIfExists(String path) {

        Configuration conf = new Configuration();

        FileSystem hdfs = null;
        try {
            hdfs = FileSystem.get(new URI( endpoint ), conf);

            Path remoteFile = new Path(StringUtils.remove(path,endpoint));

            // delete existing directory
            if (hdfs.exists(remoteFile)) {
                hdfs.delete(remoteFile, true);
            }

            hdfs.close();
        } catch (IOException | URISyntaxException e) {
            LOG.warn("Error deleting existing file: " + endpoint + path,e);
            return false;
        }
        return true;
    }

    @Override
    /**
     * set the HADOOP_USER_NAME environment variable when specific user
     */
    public boolean save(String path, File existingFile) {

        Configuration configuration = new Configuration();
        FileSystem hdfs = null;
        try {
            hdfs = FileSystem.get( new URI( endpoint), configuration );

            Path localFile = new Path(existingFile.getAbsolutePath());
            Path remoteFile = new Path((path.startsWith("/") ? path : "/" + path));

            hdfs.copyFromLocalFile(localFile, remoteFile);

            hdfs.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
