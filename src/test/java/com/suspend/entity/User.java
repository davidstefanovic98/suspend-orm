package com.suspend.entity;

import com.suspend.annotation.*;
import com.suspend.mapping.FetchType;

@Entity
@Table(name = "user")
public class User {

    @Id
    private Integer id;
    @Column(name = "username")
    private String username;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="test_fk", referencedColumnName = "id")
    private TestEntity test;

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

    public TestEntity getTest() {
        return test;
    }

    public void setTest(TestEntity test) {
        this.test = test;
    }
}
