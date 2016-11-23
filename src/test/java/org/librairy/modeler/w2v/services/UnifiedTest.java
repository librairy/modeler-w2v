/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.services;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.modeler.w2v.Config;
import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.librairy.modeler.w2v.tasks.TrainingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created on 02/05/16:
 *
 * @author cbadenes
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class UnifiedTest {

    private static final Logger LOG = LoggerFactory.getLogger(UnifiedTest.class);

    @Autowired
    ModelingService modelingService;

    @Autowired
    CleaningService cleaningService;

    @Test
    public void buildModel() throws InterruptedException {

        String domainUri = "http://librairy.org/domains/default";

        modelingService.train(domainUri,1000);

        Thread.currentThread().sleep(Integer.MAX_VALUE);
        LOG.info("time to sleep..");

    }
}
