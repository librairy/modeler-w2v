/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.helper;

import lombok.Getter;
import org.apache.spark.sql.cassandra.CassandraSQLContext;
import org.librairy.computing.helper.SparkHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class CassandraHelper {

    @Autowired
    SparkHelper sparkHelper;

    @Getter
    private CassandraSQLContext context;

    @PostConstruct
    public void setup(){
        this.context = new CassandraSQLContext(sparkHelper.getContext().sc());

    }
}