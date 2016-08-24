package org.librairy.modeler.w2v.helper;

import org.apache.commons.io.FileUtils;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
public class LocalFSHelper implements StorageHelper {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFSHelper.class);

    private final String baseDir;
    private final String endpoint;

    public LocalFSHelper(String endpoint, String baseDir){
        this.baseDir = baseDir;
        this.endpoint = (endpoint.endsWith("/"))? endpoint.substring(0,endpoint.length()-2) : endpoint;
    }

    @Override
    public String getPath(String id) {


        String folderName = (id.startsWith("http"))? URIGenerator.retrieveId(id) : id;
//        String time     = TimeUtils.asISO();
        Path path = Paths.get(baseDir,folderName); //Paths.get(baseDir,domainId, time)

        return path.toAbsolutePath().toString();
    }


    public boolean deleteIfExists(String path){
        try {
            Path folder = Paths.get(path);

            FileUtils.deleteDirectory(folder.toFile());
            Files.createDirectories(folder);
            return true;
        } catch (IOException e) {
            LOG.warn("Error deleting/creating folder at: " + path,e);
            return false;
        }
    }

    @Override
    public boolean save(String path, File file) {
        Path filePath = Paths.get(path);
        try {
            FileUtils.copyFile(file,filePath.toFile());
            return true;
        } catch (IOException e) {
            LOG.warn("Error creating file at: " + path,e);
            return false;
        }
    }

}
