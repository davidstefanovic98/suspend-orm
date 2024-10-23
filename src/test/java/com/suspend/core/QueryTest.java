package com.suspend.core;

import com.suspend.configuration.Configuration;
import com.suspend.entity.TestEntity;
import com.suspend.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    SessionFactory sessionFactory;

    @BeforeEach
    void setUp() {
        Configuration configuration = Configuration.getInstance();

        configuration.setEntityPackageName("com.suspend.entity");
        configuration.setPropertiesFile("application.properties");
        sessionFactory = configuration.buildSessionFactory();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getResultList() throws SQLException {
        Query<TestEntity> query = sessionFactory
                .openSession()
                .createQuery("select * from test", TestEntity.class);
        List<TestEntity> tests = query.getResultList();
        assertEquals(2, tests.size());

        List<User> users = tests.get(0).getUsers();

        int size = users.size();

        assertEquals(1, size);
    }

    @Test
    void uniqueResult_returnsSingleResult() throws SQLException {
        Query<TestEntity> query = sessionFactory
                .openSession()
                .createQuery("select * from test where id = 1", TestEntity.class);

        TestEntity actualEntity = query.uniqueResult();

        assertEquals(1, actualEntity.getId());
    }

    @Test
    void uniqueResult_throwsExceptionIfMoreThanOneResult() throws SQLException {
        Query<TestEntity> query = sessionFactory
                .openSession()
                .createQuery("select * from test", TestEntity.class);

        assertThrows(SQLException.class, query::uniqueResult);
    }
}