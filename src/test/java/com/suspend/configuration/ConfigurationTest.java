package com.suspend.configuration;

import com.suspend.core.SessionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void testConfiguration() {
        Configuration configuration = Configuration.getInstance();

        configuration.setEntityPackageName("com.suspend.entity");
        configuration.setPropertiesFile("application.properties");

        SessionFactory sessionFactory = configuration.buildSessionFactory();

        assertEquals(2, configuration.getEntityMetadataContainer().getAllEntityMetadata().size());
    }

}