package org.librairy.modeler.w2v.storage;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
@Component
@Conditional(HDFSCondition.class)
public class HDFSStorage extends AbstractStorage {

    private static final Logger LOG = LoggerFactory.getLogger(HDFSStorage.class);

    private String endpoint;
    private String basedir;

    @PostConstruct
    public void setup(){
        this.endpoint   = (fileSystem.endsWith("/"))? fileSystem.substring(0,fileSystem.length()-2) : fileSystem;
        this.basedir    = getHome()+"domains/";
    }

    @Override
    public String getHome() {
        return normalizedHome("/");
    }

    @Override
    public String path(String domainId, String fileName) {
        return new StringBuilder()
                .append(basedir)
                .append(domainId)
                .append("/")
                .append(fileName)
                .toString();
    }

    @Override
    public String absolutePath(String path) {

        if (path.startsWith(endpoint)) return path;

        return new StringBuilder()
                .append(endpoint)
                .append(path)
                .toString()
                ;

    }



    @Override
    /**
     * set the HADOOP_USER_NAME environment variable when specific user
     */
    public boolean deleteIfExists(String path) {

        Configuration conf = new Configuration();

        try {
            FileSystem hdfs = FileSystem.get(new URI( endpoint ), conf);

            Path remoteFile = new Path(path);

            // delete existing directory
            if (hdfs.exists(remoteFile)) {
                hdfs.delete(remoteFile, true);
            }

            hdfs.close();
        } catch (IOException | URISyntaxException e) {
            LOG.warn("Error deleting file: " + path,e);
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
        try {
            FileSystem hdfs = FileSystem.get( new URI( endpoint), configuration );

            Path localFile = new Path(existingFile.getAbsolutePath());

            String normalizedPath = path.startsWith("/") ? path : "/" + path;
            Path remoteFile = new Path(normalizedPath);
            Path remoteDirectory = new Path(StringUtils.substringBeforeLast(normalizedPath,"/"));

            hdfs.mkdirs(remoteDirectory);
            hdfs.copyFromLocalFile(localFile, remoteFile);

            hdfs.close();
        } catch (IOException | URISyntaxException e) {
            LOG.warn("Error saving file: " + absolutePath(path),e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean exists(String path) {
        Configuration conf = new Configuration();

        try {
            FileSystem hdfs = FileSystem.get(new URI( endpoint ), conf);

            Path remoteFile = new Path(path);

            boolean exists =  (hdfs.exists(remoteFile));

            hdfs.close();
            return exists;
        } catch (IOException | URISyntaxException e) {
            LOG.warn("Error deleting file: " + absolutePath(path),e);
            return false;
        }
    }

    @Override
    public File read(String path) throws URISyntaxException, IOException {
        Configuration configuration = new Configuration();

        FileSystem hdfs = FileSystem.get( new URI( endpoint), configuration );

        String tmpDir   = System.getProperty("java.io.tmpdir");
        String fileName = StringUtils.substringAfterLast(path,"/");
        Path localFile  = new Path(tmpDir + File.separator + fileName);

        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        Path remoteFile = new Path(normalizedPath);

        hdfs.copyToLocalFile(remoteFile,localFile);

        hdfs.close();

        return new File(localFile.toString());

    }

}
