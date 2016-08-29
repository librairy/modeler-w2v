package org.librairy.modeler.w2v.spark;

import com.google.common.base.Strings;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Created on 24/08/16:
 *
 * @author cbadenes
 */
public class LocalSparkCondition implements Condition{

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String envVar  = System.getenv("LIBRAIRY_SPARK");
        return (Strings.isNullOrEmpty(envVar)
                && conditionContext.getEnvironment().getProperty("librairy.w2v.spark").startsWith("local"))
                ||
                (!Strings.isNullOrEmpty(envVar) && envVar.startsWith("local"));
    }
}
