package org.librairy.modeler.w2v.storage;

import com.google.common.base.Strings;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Created on 24/08/16:
 *
 * @author cbadenes
 */
public class HDFSCondition implements Condition{

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String envVar  = System.getenv("LIBRAIRY_FS");
        return (Strings.isNullOrEmpty(envVar)
                && conditionContext.getEnvironment().getProperty("librairy.w2v.fs").startsWith("hdfs"))
                ||
                (!Strings.isNullOrEmpty(envVar) && envVar.startsWith("hdfs"));
    }
}
