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
import org.librairy.boot.model.domain.resources.Domain;
import org.librairy.boot.storage.dao.DomainsDao;
import org.librairy.boot.storage.dao.ItemsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class DomainCache {

    private static final Logger LOG = LoggerFactory.getLogger(DomainCache.class);

    @Autowired
    DomainsDao domainsDao;

    @Autowired
    ItemsDao itemsDao;

    LoadingCache<String, List<Domain>> cache;

    @PostConstruct
    public void setup(){
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, List<Domain>>() {
                            public List<Domain> load(String uri) {
                                return itemsDao.listDomains(uri, 100, Optional.empty(), false).stream()
                                        .map(row -> {
                                            Domain domain = new Domain();
                                            domain.setUri(row.getUri());
                                            return domain;
                                        })
                                        .collect(Collectors.toList());
                            }
                        });

    }


    public List<Domain> getDomainsFrom(String uri){
        try {
            List<Domain> domains = cache.get(uri);
            return domains;
        } catch (ExecutionException e) {
            LOG.warn("Error reading cache", e);
            return Collections.emptyList();
        }
    }

}
