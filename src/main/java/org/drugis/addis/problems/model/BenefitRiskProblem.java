package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.List;
import java.util.Map;

/**
 * Created by connor on 4-3-16.
 */
public class BenefitRiskProblem extends AbstractProblem {

  private Map<String, CriterionEntry> criteria;
  private Map<String, AlternativeEntry> alternatives;
  private List<PerformanceTableEntry> performanceTable;

  public BenefitRiskProblem(Map<String, CriterionEntry> criteria, Map<String, AlternativeEntry> alternatives, List<PerformanceTableEntry> performanceTable) {
    this.criteria = criteria;
    this.alternatives = alternatives;
    this.performanceTable = performanceTable;
  }

  public Map<String, CriterionEntry> getCriteria() {
    return criteria;
  }

  public Map<String, AlternativeEntry> getAlternatives() {
    return alternatives;
  }

  public List<PerformanceTableEntry> getPerformanceTable() {
    return performanceTable;
  }

  public static class PerformanceTableEntry {
    private String criterion;
    private Performance performance;

    public PerformanceTableEntry(String criterion, Performance performance) {
      this.criterion = criterion;
      this.performance = performance;
    }

    public String getCriterion() {
      return criterion;
    }

    public Performance getPerformance() {
      return performance;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PerformanceTableEntry that = (PerformanceTableEntry) o;

      if (!criterion.equals(that.criterion)) return false;
      return performance.equals(that.performance);

    }

    @Override
    public int hashCode() {
      int result = criterion.hashCode();
      result = 31 * result + performance.hashCode();
      return result;
    }

    public static class Performance {
      private String type;
      private Parameters parameters;

      public Performance(String type, Parameters parameters) {
        this.type = type;
        this.parameters = parameters;
      }

      public String getType() {
        return type;
      }

      public Parameters getParameters() {
        return parameters;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Performance that = (Performance) o;

        if (!type.equals(that.type)) return false;
        return parameters.equals(that.parameters);

      }

      @Override
      public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
      }

      public static class Parameters {
        @JsonRawValue
        private String baseline;
        private Relative relative;

        public Parameters(String baseline, Relative relative) {
          this.baseline = baseline;
          this.relative = relative;
        }

        @JsonRawValue
        public String getBaseline() {
          return baseline;
        }

        public Relative getRelative() {
          return relative;
        }

        @Override
        public boolean equals(Object o) {
          if (this == o) return true;
          if (o == null || getClass() != o.getClass()) return false;

          Parameters that = (Parameters) o;

          if (!baseline.equals(that.baseline)) return false;
          return relative.equals(that.relative);

        }

        @Override
        public int hashCode() {
          int result = baseline.hashCode();
          result = 31 * result + relative.hashCode();
          return result;
        }

        public static class Baseline {
          private String type = "dnorm";
          private String name;
          private Double mu;
          private Double sigma;

          public Baseline(String name, Double mu, Double sigma) {
            this.name = name;
            this.mu = mu;
            this.sigma = sigma;
          }

          public String getType() {
            return type;
          }

          public String getName() {
            return name;
          }

          public Double getMu() {
            return mu;
          }

          public Double getSigma() {
            return sigma;
          }

          @Override
          public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Baseline baseline = (Baseline) o;

            if (!type.equals(baseline.type)) return false;
            if (!name.equals(baseline.name)) return false;
            if (!mu.equals(baseline.mu)) return false;
            return sigma.equals(baseline.sigma);

          }

          @Override
          public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + mu.hashCode();
            result = 31 * result + sigma.hashCode();
            return result;
          }
        }

        public static class Relative {
          private String type;
          private Map<String, Double> mu;
          private CovarianceMatrix cov;

          public Relative(String type, Map<String, Double> mu, CovarianceMatrix cov) {
            this.type = type;
            this.mu = mu;
            this.cov = cov;
          }

          public String getType() {
            return type;
          }

          public Map<String, Double> getMu() {
            return mu;
          }

          public CovarianceMatrix getCov() {
            return cov;
          }

          @Override
          public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Relative relative = (Relative) o;

            if (!type.equals(relative.type)) return false;
            if (!mu.equals(relative.mu)) return false;
            return cov.equals(relative.cov);

          }

          @Override
          public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + mu.hashCode();
            result = 31 * result + cov.hashCode();
            return result;
          }

          public static class CovarianceMatrix {
            private List<String> rownames;
            private List<String> colnames;
            private List<List<Double>> data;

            public CovarianceMatrix(List<String> rownames, List<String> colnames, List<List<Double>> data) {
              this.rownames = rownames;
              this.colnames = colnames;
              this.data = data;
            }

            public List<String> getRownames() {
              return rownames;
            }

            public List<String> getColnames() {
              return colnames;
            }

            public List<List<Double>> getData() {
              return data;
            }

            @Override
            public boolean equals(Object o) {
              if (this == o) return true;
              if (o == null || getClass() != o.getClass()) return false;

              CovarianceMatrix that = (CovarianceMatrix) o;

              if (!rownames.equals(that.rownames)) return false;
              if (!colnames.equals(that.colnames)) return false;
              return data.equals(that.data);

            }

            @Override
            public int hashCode() {
              int result = rownames.hashCode();
              result = 31 * result + colnames.hashCode();
              result = 31 * result + data.hashCode();
              return result;
            }
          }
        }
      }
    }
  }
}
