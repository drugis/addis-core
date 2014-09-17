package org.drugis.addis.remarks;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.util.ObjectToStringDeserializer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by daan on 16-9-14.
 */
@Entity
public class Remarks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer scenarioId;

    @JsonRawValue
    private String remarks;

    public Remarks() {
    }

    public Remarks(Integer id, Integer scenarioId, String remarks) {
        this.id = id;
        this.scenarioId = scenarioId;
        this.remarks = remarks;
    }

    public Integer getId() {
        return id;
    }

    public Integer getScenarioId() {
        return scenarioId;
    }

    public String getRemarks() {
        return remarks;
    }

    @JsonDeserialize(using = ObjectToStringDeserializer.class)
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Remarks remarks1 = (Remarks) o;

        if (!id.equals(remarks1.id)) return false;
        if (!remarks.equals(remarks1.remarks)) return false;
        if (!scenarioId.equals(remarks1.scenarioId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + scenarioId.hashCode();
        result = 31 * result + remarks.hashCode();
        return result;
    }
}
