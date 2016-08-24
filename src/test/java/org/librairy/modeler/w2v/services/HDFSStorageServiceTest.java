package org.librairy.modeler.w2v.services;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.resources.Resource;
import org.librairy.modeler.w2v.Config;
import org.librairy.modeler.w2v.helper.StorageHelper;
import org.librairy.storage.generator.URIGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

/**
 * Created on 23/08/16:
 *
 * @author cbadenes
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
@TestPropertySource(properties = {
        "librairy.modeler.learn = false",
        "librairy.comparator.delay = 1000",
        "librairy.cassandra.contactpoints = wiener.dia.fi.upm.es",
        "librairy.cassandra.port = 5011",
        "librairy.cassandra.keyspace = research",
        "librairy.elasticsearch.contactpoints = wiener.dia.fi.upm.es",
        "librairy.elasticsearch.port = 5021",
        "librairy.neo4j.contactpoints = wiener.dia.fi.upm.es",
        "librairy.neo4j.port = 5030",
        "librairy.eventbus.host = localhost",
        "librairy.eventbus.port = 5041",
        "spark.filesystem = hdfs://zavijava.dia.fi.upm.es:8020",
        "librairy.modeler.folder = /librairy/w2v-model",
        "librairy.vocabulary.size = 10000",
        "spark.master = local[*]",
        "librairy.modeler.vector.dimension = 10",
        "librairy.modeler.maxiterations = 2"
})
public class HDFSStorageServiceTest {

    @Autowired
    StorageService storageService;

    @Autowired
    URIGenerator uriGenerator;

    @Test
    public void write(){

        StorageHelper storageHelper = storageService.getHelper();

        File file = new File("out.txt");

        System.out.println(file.exists());

        storageHelper.save("/librairy/out.txt", file);



    }


    @Test
    public void deleteIfExists(){

        StorageHelper storageHelper = storageService.getHelper();

        String domainUri = uriGenerator.from(Resource.Type.DOMAIN, "4f56ab24bb6d815a48b8968a3b157470");

        String path = storageHelper.getPath(domainUri);

        storageHelper.deleteIfExists(path);

    }

}
