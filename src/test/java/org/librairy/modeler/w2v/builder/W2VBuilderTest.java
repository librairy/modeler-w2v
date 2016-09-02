package org.librairy.modeler.w2v.builder;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.modeler.w2v.Config;
import org.librairy.modeler.w2v.models.W2VModel;
import org.librairy.modeler.w2v.models.WordDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cbadenes on 13/01/16.
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
@TestPropertySource(properties = {
        "librairy.columndb.host = wiener.dia.fi.upm.es",
        "librairy.columndb.port = 5011",
        "librairy.documentdb.host = wiener.dia.fi.upm.es",
        "librairy.documentdb.port = 5021",
        "librairy.graphdb.host = wiener.dia.fi.upm.es",
        "librairy.graphdb.port = 5030",
        "librairy.eventbus.host = local",
        "librairy.w2v.model.dimension=20",
        "librairy.w2v.model.iterations=2"
})
public class W2VBuilderTest {

    private static final Logger LOG = LoggerFactory.getLogger(W2VBuilderTest.class);

    @Autowired
    W2VBuilder wordEmbeddingBuilder;

    @Test
    public void simulateByDomain(){

        String domainURI = "http://drinventor.eu/domains/4f56ab24bb6d815a48b8968a3b157470";


        LocalTime start = LocalTime.now();

        W2VModel model = wordEmbeddingBuilder.build(domainURI);

        LocalTime end = LocalTime.now();

        List<String> vocabulary = model.getVocabulary();

        List<WordDistribution> synonyms = model.find("image");
        System.out.println(synonyms);


        Duration elapsedTime = Duration.between(start, end);

        System.out.println("Elapsed Time: " + elapsedTime.getSeconds() + "secs");


    }

    @Test
    public void simulateByUris(){

        List<String> uris = Arrays.asList(new String[]{
                "http://drinventor.eu/items/9c8b49fbc507cfe9903fc9f08dc2a8c8",
        "http://drinventor.eu/items/ec934613dfb9acddd68f89c579f24aff",
        "http://drinventor.eu/items/7da1c096e73093f6404cd28946d313a6",
        "http://drinventor.eu/items/71b295f128318469a3f04d25ae8b18c",
        "http://drinventor.eu/items/cc9ca0ab0fe51b6ec99c09a2cba75249",
        "http://drinventor.eu/items/2073b01f3c011e62903d27f8549167a8",
        "http://drinventor.eu/items/6cead06fa9b7ec59daf55d186f1085b3",
        "http://drinventor.eu/items/2906920d70fb1fc1a67d171245160e02",
        "http://drinventor.eu/items/548b6e832a232c101f8d9ab3a16d6d95",
        "http://drinventor.eu/items/d75ddc6e097d193a5cfee8396aab8e21"
        });

        W2VModel model = wordEmbeddingBuilder.build("test", uris);
        System.out.println("Model: " + model);

        List<WordDistribution> synonyms = model.find("image");
        System.out.println(synonyms);


    }

    @Test
    public void loadByDomain(){

        String domainURI = "http://drinventor.eu/domains/4f56ab24bb6d815a48b8968a3b157470";


        LocalTime start = LocalTime.now();

        W2VModel model = wordEmbeddingBuilder.load(domainURI);

        LocalTime end = LocalTime.now();


        List<WordDistribution> synonyms = model.find("image");
        System.out.println(synonyms);
//

        Duration elapsedTime = Duration.between(start, end);

        System.out.println("Elapsed Time: " + elapsedTime.getSeconds() + "secs");


    }
}
