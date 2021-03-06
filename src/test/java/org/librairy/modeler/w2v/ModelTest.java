/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.librairy.modeler.w2v.tasks.TrainingTask;
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
@TestPropertySource(properties = {
        "librairy.cassandra.contactpoints       = wiig.dia.fi.upm.es",
        "librairy.cassandra.port                = 5011",
        "librairy.cassandra.keyspace            = research",
        "librairy.elasticsearch.contactpoints   = wiig.dia.fi.upm.es",
        "librairy.elasticsearch.port            = 5021",
        "librairy.neo4j.contactpoints           = wiig.dia.fi.upm.es",
        "librairy.neo4j.port                    = 5030",
        "librairy.eventbus.host                 = wiig.dia.fi.upm.es",
        "librairy.eventbus.port                 = 5041",
        "librairy.w2v.event.delay = 200000"
})
public class ModelTest {

    @Autowired
    ModelingHelper helper;

    @Test
    public void buildModel(){
        new TrainingTask("http://librairy.org/domains/default",helper).run();
    }
}
