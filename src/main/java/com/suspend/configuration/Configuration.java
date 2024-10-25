package com.suspend.configuration;

import com.suspend.core.exception.SuspendException;
import com.suspend.core.internal.SessionFactoryImpl;
import com.suspend.mapping.EntityMetadata;
import com.suspend.mapping.Relationship;
import com.suspend.mapping.RelationshipResolver;
import com.suspend.util.ReflectionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Configuration {

    private Properties properties;
    private static Configuration instance = null;
    private String propertiesFile;
    private String entityPackageName;

    public Configuration() {}

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(getPropertiesFile())) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new SuspendException(String.format("Config file %s not found", getPropertiesFile()));
        }
        return properties;
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public void buildSessionFactory() {
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) SessionFactoryImpl.getInstance();
        this.properties = loadProperties();
        populateEntityMetadata(sessionFactory);
    }

    public void setEntityPackageName(String entityPackageName) {
        this.entityPackageName = entityPackageName;
    }

    private void populateEntityMetadata(SessionFactoryImpl sessionFactory) {
        Set<Class<?>> entityClasses = ReflectionUtil.getEntityClasses(entityPackageName);

        for (Class<?> entityClass : entityClasses) {
            List<Relationship> relationships = RelationshipResolver.getRelationships(entityClass);

            EntityMetadata entityMetadata = new EntityMetadata(
                    entityClass,
                    ReflectionUtil.getIdField(entityClass),
                    relationships
            );

            sessionFactory.addEntityMetadata(entityMetadata);
        }
    }
}
