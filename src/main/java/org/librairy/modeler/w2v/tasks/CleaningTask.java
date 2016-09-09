package org.librairy.modeler.w2v.tasks;

import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cbadenes on 13/01/16.
 */
public class CleaningTask implements Runnable{

    private static final Logger LOG = LoggerFactory.getLogger(CleaningTask.class);

    private final ModelingHelper helper;

    private final String domainUri;

    public CleaningTask(String domainUri, ModelingHelper modelingHelper) {
        this.domainUri = domainUri;
        this.helper = modelingHelper;
    }


    @Override
    public void run() {
        LOG.info("Cleaning previous words in domain: " + domainUri);
        this.helper.getCleaner().delete(domainUri);
    }
}
