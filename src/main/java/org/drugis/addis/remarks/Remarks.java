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
    private Integer analysisId;

    @JsonRawValue
    private String remarks;

    public Remarks() {
    }

  public Remarks(Integer analysisId, String remarks) {
    this.analysisId = analysisId;
    this.remarks = remarks;
  }

    public Integer getAnalysisId() {
        return analysisId;
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

    if (!analysisId.equals(remarks1.analysisId)) return false;
    if (remarks != null ? !remarks.equals(remarks1.remarks) : remarks1.remarks != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = analysisId.hashCode();
    result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
    return result;
  }
}
