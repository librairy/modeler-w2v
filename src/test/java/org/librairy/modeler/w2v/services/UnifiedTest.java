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

        cleaningService.clean(domainUri,1000);


        modelingService.train(domainUri,5000);

        Thread.currentThread().sleep(6000000);
        LOG.info("time to sleep..");

    }
}
