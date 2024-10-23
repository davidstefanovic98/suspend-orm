package com.suspend.configuration;

import com.suspend.core.SessionFactory;
import com.suspend.core.exception.SuspendException;
import com.suspend.core.internal.SessionFactoryImpl;
import com.suspend.mapping.EntityMetadata;
import com.suspend.mapping.EntityMetadataContainer;
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
    private EntityMetadataContainer entityMetadataContainer;
    private String entityPackageName;

    public Configuration() {
        entityMetadataContainer = new EntityMetadataContainer();
    }

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

    public EntityMetadataContainer getEntityMetadataContainer() {
        return entityMetadataContainer;
    }

    public void setEntityMetadataContainer(EntityMetadataContainer entityMetadataContainer) {
        this.entityMetadataContainer = entityMetadataContainer;
    }

    public SessionFactory buildSessionFactory() {
        this.properties = loadProperties();
        populateEntityReferenceContainer();
        return new SessionFactoryImpl();
    }

    public String getEntityPackageName() {
        return entityPackageName;
    }

    public void setEntityPackageName(String entityPackageName) {
        this.entityPackageName = entityPackageName;
    }

    private void populateEntityReferenceContainer() {
        Set<Class<?>> entityClasses = ReflectionUtil.getEntityClasses(entityPackageName);

        for (Class<?> entityClass : entityClasses) {
            List<Relationship> relationships = RelationshipResolver.getRelationships(entityClass);

            EntityMetadata entityMetadata = new EntityMetadata(
                    entityClass,
                    ReflectionUtil.getIdField(entityClass),
                    relationships
            );

            entityMetadataContainer.addEntityMetadata(entityMetadata);
        }
    }
}
