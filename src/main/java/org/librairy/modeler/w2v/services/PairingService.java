/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.services;

import org.librairy.modeler.w2v.tasks.PairingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
public class PairingService extends AbstractService{

    private static final Logger LOG = LoggerFactory.getLogger(PairingService.class);

    public void pair(String wordUri, String domainUri, long delay){

        LOG.info("A new task for pairing words based on a word embedding model for the domain: " + domainUri + " has " +
                "been " +
                "scheduled" +
                "at " + timeFormatter.format(new Date(System.currentTimeMillis() + delay)));

        ScheduledFuture<?> task = tasks.get(domainUri);
        if (task != null) task.cancel(false);
        task = this.threadpool.schedule(new PairingTask(wordUri, domainUri, helper), new Date(System.currentTimeMillis
                () +
                delay));
        tasks.put(domainUri,task);

    }

}
