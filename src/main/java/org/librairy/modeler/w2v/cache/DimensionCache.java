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
import org.librairy.boot.storage.dao.ParametersDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class DimensionCache {

    private static final Logger LOG = LoggerFactory.getLogger(DimensionCache.class);

    @Value("#{environment['LIBRAIRY_W2V_MODEL_DIMENSION']?:${librairy.w2v.model.dimension}}")
    Integer vectorSize;

    @Autowired
    ParametersDao parametersDao;

    private LoadingCache<String, Integer> cache;

    @PostConstruct
    public void setup(){
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, Integer>() {
                            public Integer load(String domainUri) {
                                try {
                                    String parameter = parametersDao.get(domainUri, "w2v.dim");
                                    return Integer.valueOf(parameter);
                                } catch (Exception dataNotFound) {
                                    LOG.error("Error reading parameters from '" + domainUri + "'");
                                    return vectorSize;
                                }
                            }
                        });
    }


    public Integer getDimension(String domainUri) {
        try {
            return this.cache.get(domainUri);
        } catch (ExecutionException e) {
            LOG.error("Error getting dimension from domain. Using default value", e);
            return vectorSize;
        }
    }

}
