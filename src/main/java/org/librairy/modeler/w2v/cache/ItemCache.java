/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Domain;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.dao.ItemsDao;
import org.librairy.boot.storage.exception.DataNotFound;
import org.librairy.modeler.w2v.services.ModelingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class ItemCache {

    private static final Logger LOG = LoggerFactory.getLogger(ItemCache.class);

    @Autowired
    ItemsDao itemsDao;

    @Autowired
    ModelingService modelingService;

    @Autowired
    DelayCache delayCache;

    LoadingCache<ComposedKey, Boolean> cache;

    @PostConstruct
    public void setup(){
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<ComposedKey, Boolean>() {
                            public Boolean load(ComposedKey key) {
                                try {
                                    itemsDao.add(key.getDomainUri(), key.getResourceUri());

                                    Long delay = delayCache.getDelay(key.getDomainUri());

                                    modelingService.train(key.getDomainUri(),delay);

                                    return true;
                                } catch (DataNotFound dataNotFound) {
                                    LOG.debug(dataNotFound.getMessage());
                                    return false;
                                }

                            }
                        });

    }


    public Boolean updateItem(String domainUri, String itemUri){
        try {
            ComposedKey key = new ComposedKey();
            key.setDomainUri(domainUri);
            key.setResourceUri(itemUri);
            if (!cache.get(key)) cache.refresh(key);
            return true;
        } catch (ExecutionException e) {
            LOG.warn("Error reading cache", e);
            return false;
        }
    }

}
