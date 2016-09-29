/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.tasks;

import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.librairy.modeler.w2v.data.W2VModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cbadenes on 13/01/16.
 */
public class TrainingTask implements Runnable{

    private static final Logger LOG = LoggerFactory.getLogger(TrainingTask.class);

    private final ModelingHelper helper;

    private final String domainUri;

    public TrainingTask(String domainUri, ModelingHelper modelingHelper) {
        this.domainUri = domainUri;
        this.helper = modelingHelper;
    }


    @Override
    public void run() {

        try{

            LOG.info("Building a new W2V model in domain: " + domainUri);
            W2VModel model = helper.getWordEmbeddingBuilder().build(domainUri);

        }catch (RuntimeException e){
            LOG.warn(e.getMessage(),e);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }
}
