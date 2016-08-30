package org.librairy.modeler.w2v.storage;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
@Component
@Conditional(LocalFSCondition.class)
public class LocalFSStorage extends AbstractStorage {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFSStorage.class);

    private String basedir;

    @PostConstruct
    public void setup(){
        this.basedir    = new StringBuilder().append(getHome()).append("domains").append(File
                .separator).toString();
    }


    @Override
    public String getHome() {
        return normalizedHome(File.separator);
    }

    @Override
    public String path(String domainId, String fileName) {
        return new StringBuilder()
                .append(basedir)
                .append(domainId)
                .append(File.separator)
                .append(fileName)
                .toString();
    }

    @Override
    public String absolutePath(String path) {
        return path;
    }


    public boolean deleteIfExists(String path) {
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
            FileUtils.forceMkdir(file);
            FileUtils.copyFile(file,filePath.toFile());
            return true;
        } catch (IOException e) {
            LOG.warn("Error creating file at: " + path,e);
            return false;
        }
    }

    @Override
    public boolean exists(String path) {
        return Paths.get(path).toFile().exists();
    }

    @Override
    public File read(String path) {
        return new File(path);
    }

}
