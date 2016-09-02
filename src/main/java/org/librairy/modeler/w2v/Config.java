package org.librairy.modeler.w2v;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by cbadenes on 11/01/16.
 */
@Configuration("modeler-w2v")
@ComponentScan({"org.librairy"})
@PropertySource({"classpath:w2v-modeler.properties","classpath:boot.properties","classpath:computing.properties"})
public class Config {

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
