package org.librairy.modeler.w2v.services;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.modeler.w2v.Config;
import org.librairy.modeler.w2v.helper.StorageHelper;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
@TestPropertySource(properties = {
        "librairy.columndb.host = wiener.dia.fi.upm.es",
        "librairy.documentdb.host = wiener.dia.fi.upm.es",
        "librairy.graphdb.host = wiener.dia.fi.upm.es",
        "librairy.eventbus.host = local",
        "librairy.w2v.fs = local"
})
public class LocalFSStorageServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFSStorageServiceTest.class);

    @Autowired
    StorageHelper storageHelper;

    @Autowired
    URIGenerator uriGenerator;

    /**
     * set environment LIBRAIRY_FS = hdfs://zavijava.dia.fi.upm.es:8020
     */

    @Test
    public void save(){

        File file = new File("/Users/cbadenes/Downloads/mesos-0.28.1/build/src/.libs/libmesos.dylib");

        System.out.println(file.exists());

        storageHelper.save("/librairy/lib/libmesos.dylib", file);

    }


    @Test
    public void deleteIfExists(){


        String domainId = "4f56ab24bb6d815a48b8968a3b157470";
        String path = storageHelper.path(domainId,"w2v");
        storageHelper.deleteIfExists(path);

    }

    @Test
    public void read() throws IOException, URISyntaxException {

        File file = storageHelper.read("/librairy/domains/4f56ab24bb6d815a48b8968a3b157470/stopwords.txt");

        LOG.info("File Path: " + file);

        String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

    }


}
