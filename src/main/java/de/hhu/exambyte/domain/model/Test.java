package de.hhu.exambyte.domain.model;

import java.util.List;

public class Test {
    private String name;
    private String id;
//    private List<Question> questions;

    public Test(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
