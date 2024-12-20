package com.suspend.core;

import com.suspend.configuration.Configuration;
import com.suspend.core.internal.SessionFactoryImpl;
import com.suspend.entity.TestEntity;
import com.suspend.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    SessionFactory sessionFactory;

    @BeforeAll
    static void init() {
        Configuration configuration = Configuration.getInstance();
        configuration.setEntityPackageName("com.suspend.entity");
        configuration.setPropertiesFile("application.properties");
        configuration.buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        sessionFactory = SessionFactoryImpl.getInstance();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getResultList() throws SQLException {
        Session session = sessionFactory.openSession();

        Query<TestEntity> query = session.createQuery("select * from test", TestEntity.class);
        List<TestEntity> tests = query.getResultList();
        assertEquals(2, tests.size());

        List<User> users = tests.get(0).getUsers();

        int size = users.size();

        for (User user : users) {
            TestEntity test = user.getTest();
            String name = test.getTestName();
            assertEquals("Marija", name);
        }

        assertEquals(1, size);
    }

    @Test
    void getResultListUsers() throws SQLException {
        Session session = sessionFactory.openSession();

        Query<User> query = session.createQuery("select * from user", User.class);
        List<User> users = query.getResultList();
        assertEquals(3, users.size());

        TestEntity test = users.get(0).getTest();

        String name = test.getTestName();

        assertEquals("David", name);
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