package com.example.romka.realmvssqlite.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created on 24.05.2016.
 */
public class SingleTestModel extends RealmObject {
    @PrimaryKey
    public Integer id;
    public String label;
    public Long longValue;

    public SingleTestModel() {}

    public SingleTestModel(Integer id, String label, Long longValue) {
        this.id = id;
        this.label = label;
        this.longValue = longValue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }
}
