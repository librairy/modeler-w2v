/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.eventbus;

import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.BindingKey;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.EventBusSubscriber;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.UDM;
import org.librairy.modeler.w2v.cache.DelayCache;
import org.librairy.modeler.w2v.services.ModelingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 09/09/16:
 *
 * @author cbadenes
 */
@Component
public class DomainUpdatedEventHandler implements EventBusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUpdatedEventHandler.class);

    @Autowired
    protected EventBus eventBus;

    @Autowired
    ModelingService modelingService;

    @Autowired
    DelayCache delayCache;



    @PostConstruct
    public void init(){
        BindingKey bindingKey = BindingKey.of(RoutingKey.of(Resource.Type.DOMAIN, Resource.State.UPDATED), "modeler.w2v.domain.updated");
        LOG.info("Trying to register as subscriber of '" + bindingKey + "' events ..");
        eventBus.subscribe(this,bindingKey );
        LOG.info("registered successfully");
    }

    @Override
    public void handle(Event event) {
        LOG.debug("Domain updated event received: " + event);
        try{
            Resource resource = event.to(Resource.class);

            Long delay = delayCache.getDelay(resource.getUri());

            modelingService.train(resource.getUri(),delay);

        } catch (Exception e){
            LOG.error("Error scheduling a new topic model for Items from domain: " + event, e);
        }
    }
}
