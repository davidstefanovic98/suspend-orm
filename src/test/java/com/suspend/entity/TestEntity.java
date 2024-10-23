package com.suspend.entity;

import com.suspend.annotation.*;
import com.suspend.mapping.FetchType;

import java.util.List;

@Entity
@Table(name = "test")
public class TestEntity {

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
