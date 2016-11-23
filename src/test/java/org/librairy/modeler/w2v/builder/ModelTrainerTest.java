/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.builder;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.boot.storage.generator.URIGenerator;
import org.librairy.modeler.w2v.Config;
import org.librairy.modeler.w2v.data.W2VModel;
import org.librairy.modeler.w2v.data.WordDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
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
public class ModelTrainerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ModelTrainerTest.class);

    @Autowired
    ModelTrainer wordEmbeddingBuilder;

    @Test
    public void simulateByDomain(){

        String domainURI = "http://librairy.org/domains/default";


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
    public void loadByDomain(){

        String domainURI = "http://librairy.org/domains/default";


        LocalTime start = LocalTime.now();

        W2VModel model = wordEmbeddingBuilder.load(URIGenerator.retrieveId(domainURI));

        LocalTime end = LocalTime.now();


        List<WordDistribution> synonyms = model.find("image");
        System.out.println(synonyms);
//

        Duration elapsedTime = Duration.between(start, end);

        System.out.println("Elapsed Time: " + elapsedTime.getSeconds() + "secs");


    }
}
