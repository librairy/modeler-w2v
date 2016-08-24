package org.librairy.modeler.w2v.services;

import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.librairy.modeler.w2v.scheduler.W2VTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
public class W2VModelingService {

    private static final Logger LOG = LoggerFactory.getLogger(W2VModelingService.class);

    private ConcurrentHashMap<String,ScheduledFuture<?>> tasks;

    private ThreadPoolTaskScheduler threadpool;

    @Value("${librairy.modeler.delay}")
    protected Long delay;

    @Autowired
    ModelingHelper helper;

    @PostConstruct
    public void setup(){
        this.tasks = new ConcurrentHashMap<>();

        this.threadpool = new ThreadPoolTaskScheduler();
        this.threadpool.setPoolSize(50);
        this.threadpool.initialize();

    }


    public void buildModel(String domainUri){

        // TODO Implement Multi Domain
        LOG.info("Planning a new task to build a new word2vec model in domain: " + domainUri);

        ScheduledFuture<?> task = tasks.get(domainUri);
        if (task != null) task.cancel(false);
        task = this.threadpool.schedule(new W2VTask(domainUri,helper), new Date(System.currentTimeMillis() + delay));
        tasks.put(domainUri,task);

    }

}
