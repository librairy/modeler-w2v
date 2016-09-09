package org.librairy.modeler.w2v.services;

import org.librairy.modeler.w2v.tasks.TrainingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
public class ModelingService extends AbstractService{

    private static final Logger LOG = LoggerFactory.getLogger(ModelingService.class);

    public void train(String domainUri, long delay){

        LOG.info("A new task for building a word embedding (W2V) for the domain: " + domainUri + " has been scheduled" +
                "at " + timeFormatter.format(new Date(System.currentTimeMillis() + delay)));

        ScheduledFuture<?> task = tasks.get(domainUri);
        if (task != null) task.cancel(false);
        task = this.threadpool.schedule(new TrainingTask(domainUri,helper), new Date(System.currentTimeMillis() + delay));
        tasks.put(domainUri,task);

    }

}
