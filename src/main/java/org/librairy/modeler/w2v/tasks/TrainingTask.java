/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.tasks;

import org.librairy.boot.storage.generator.URIGenerator;
import org.librairy.computing.cluster.ComputingContext;
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

        final ComputingContext context = helper.getComputingHelper().newContext("w2v.training."+ URIGenerator.retrieveId(domainUri));

        helper.getComputingHelper().execute(context, () -> {
            try{

                LOG.info("Building a new W2V model in domain: " + domainUri);
                helper.getWordEmbeddingBuilder().build(context, domainUri);

            }catch (RuntimeException e){
                LOG.warn(e.getMessage(),e);
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        });

    }
}
