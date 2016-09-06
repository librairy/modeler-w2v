package org.librairy.modeler.w2v.services;

import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.librairy.modeler.w2v.tasks.W2VTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
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

    @Value("#{environment['LIBRAIRY_W2V_EVENT_DELAY']?:${librairy.w2v.event.delay}}")
    protected Long delay;

    @Autowired
    ModelingHelper helper;

    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ssZ");

    @PostConstruct
    public void setup(){
        this.tasks = new ConcurrentHashMap<>();

        this.threadpool = new ThreadPoolTaskScheduler();
        this.threadpool.setPoolSize(50);
        this.threadpool.initialize();

    }


    public void buildModel(String domainUri){

        LOG.info("A new task for building a word embedding (W2V) for the domain: " + domainUri + " has been scheduled" +
                "at " + timeFormatter.format(new Date(System.currentTimeMillis() + delay)));

        ScheduledFuture<?> task = tasks.get(domainUri);
        if (task != null) task.cancel(false);
        task = this.threadpool.schedule(new W2VTask(domainUri,helper), new Date(System.currentTimeMillis() + delay));
        tasks.put(domainUri,task);

    }

}
