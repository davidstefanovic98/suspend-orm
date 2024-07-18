package com.suspend.core;

import com.suspend.annotation.*;
import com.suspend.core.internal.SessionFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryTest {

    @Entity
    @Table(name="test")
    public static final class Test  {
        @Id
        private Integer id;
        @Column(name = "name")
        private String testName;

        @OneToMany(mappedBy = "test")
        private List<User> users;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }
    }

    @Entity
    @Table(name = "user")
    public static final class User  {
        @Id
        private Integer id;
        @Column(name = "username")
        private String username;

        @ManyToOne
        @JoinColumn(name="test_fk", referencedColumnName = "id")
        private Test test;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Test getTest() {
            return test;
        }

        public void setTest(Test test) {
            this.test = test;
        }
    }

    SessionFactory sessionFactory;

    @BeforeEach
    void setUp() {
        sessionFactory = SessionFactoryImpl.getInstance();
    }

    @AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void getResultList() throws SQLException {
        Query<Test> query = sessionFactory
                .openSession()
                .createQuery("select * from test", Test.class);
        List<Test> tests = query.getResultList();
    }
}