/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.librairy.computing.cluster.ComputingContext;
import org.librairy.modeler.w2v.builder.ModelTrainer;
import org.librairy.modeler.w2v.data.W2VModel;
import org.librairy.boot.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created on 09/09/16:
 *
 * @author cbadenes
 */
@Component
public class ModelCache {

    private static final Logger LOG = LoggerFactory.getLogger(ModelCache.class);

    @Autowired
    ModelTrainer modelTrainer;

    LoadingCache<ModelKey, W2VModel> cache;

    @PostConstruct
    public void setup(){
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<ModelKey, W2VModel>() {
                            public W2VModel load(ModelKey key) {
                                return modelTrainer.load(key.getContext(), URIGenerator.retrieveId(key.getUri()));
                            }
                        });
    }

    public W2VModel get(ComputingContext context, String domainUri){
        try {
            return cache.get(new ModelKey(context, domainUri));
        } catch (ExecutionException e) {
            throw new RuntimeException("Invalid cache",e);
        }
    }

}
