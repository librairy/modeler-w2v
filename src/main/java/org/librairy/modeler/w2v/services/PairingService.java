/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.services;

import org.librairy.modeler.w2v.tasks.PairingTask;
import org.librairy.storage.executor.ParallelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
public class PairingService extends AbstractService{

    private static final Logger LOG = LoggerFactory.getLogger(PairingService.class);


    private ParallelExecutor executor;

    @PostConstruct
    public void setup(){
        this.executor = new ParallelExecutor();
    }

    public void pair(String wordUri, String domainUri, long delay){
        executor.execute(new PairingTask(wordUri, domainUri, helper));
    }

}
