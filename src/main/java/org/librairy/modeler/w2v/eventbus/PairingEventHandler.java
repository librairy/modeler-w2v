/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.eventbus;

import org.librairy.model.Event;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.modules.BindingKey;
import org.librairy.model.modules.EventBus;
import org.librairy.model.modules.EventBusSubscriber;
import org.librairy.model.modules.RoutingKey;
import org.librairy.modeler.w2v.services.PairingService;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 09/09/16:
 *
 * @author cbadenes
 */
@Component
public class PairingEventHandler implements EventBusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(PairingEventHandler.class);

    @Autowired
    protected EventBus eventBus;

    @Autowired
    PairingService pairingService;

    @Autowired
    UDM udm;

    @PostConstruct
    public void init(){
        BindingKey bindingKey = BindingKey.of(RoutingKey.of(Relation.Type.MENTIONS_FROM_TOPIC, Relation.State.CREATED),
                "w2v-modeler-topic-new-word");
        LOG.info("Trying to register as subscriber of '" + bindingKey + "' events ..");
        eventBus.subscribe(this,bindingKey );
        LOG.info("registered successfully");
    }

    @Override
    public void handle(Event event) {
        LOG.debug("New word created event received: " + event);
        try{
            Relation relation = event.to(Relation.class);

            String topicUri = relation.getStartUri();
            String wordUri  = relation.getEndUri();

            udm.find(Resource.Type.DOMAIN).from(Resource.Type.TOPIC, topicUri).forEach(domain -> {
                pairingService.pair(wordUri, domain.getUri(),1000);
            });


        } catch (Exception e){
            LOG.error("Error scheduling a new topic model for Items from domain: " + event, e);
        }
    }
}
