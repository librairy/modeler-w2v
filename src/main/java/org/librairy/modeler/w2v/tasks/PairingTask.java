/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.tasks;

import org.librairy.model.domain.resources.Resource;
import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by cbadenes on 13/01/16.
 */
public class PairingTask implements Runnable{

    private static final Logger LOG = LoggerFactory.getLogger(PairingTask.class);

    private final ModelingHelper helper;

    private final String domainUri;

    private final String wordUri;

    public PairingTask(String wordUri, String domainUri, ModelingHelper modelingHelper) {
        this.wordUri = wordUri;
        this.domainUri = domainUri;
        this.helper = modelingHelper;
    }


    @Override
    public void run() {

        try{

            LOG.info("Pairing word: " + wordUri + " to similar words in domain: " + domainUri);

            // Reading word
            Optional<Resource> word = helper.getUdm().read(Resource.Type.WORD).byUri(wordUri);

            if (!word.isPresent()){
                LOG.warn("No word found by uri: " + wordUri);
                return;
            }

            helper.getPairing().relateWord(word.get().asWord(), domainUri);

        }catch (RuntimeException e){
            LOG.warn(e.getMessage(),e);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }


}
