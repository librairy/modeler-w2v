/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.cache;

import lombok.Data;
import org.librairy.computing.cluster.ComputingContext;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Data
public class ModelKey {

    ComputingContext context;

    String uri;

    public ModelKey(ComputingContext context, String uri){
        this.context = context;
        this.uri = uri;
    }

}
