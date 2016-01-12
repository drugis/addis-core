package org.drugis.addis.covariates;

/**
 * Created by connor on 12/1/15.
 */
public class AddCovariateCommand {

  private String covariateDefinitionKey;
  private String name;
  private String motivation;

  public AddCovariateCommand() {
  }

  public AddCovariateCommand(String covariateDefinitionKey, String name) {
    this(covariateDefinitionKey, name, null);
  }

  public AddCovariateCommand(String covariateDefinitionKey, String name, String motivation) {
    this.covariateDefinitionKey = covariateDefinitionKey;
    this.name = name;
    this.motivation = motivation;
  }

  public String getCovariateDefinitionKey() {
    return covariateDefinitionKey;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AddCovariateCommand that = (AddCovariateCommand) o;

    if (!covariateDefinitionKey.equals(that.covariateDefinitionKey)) return false;
    if (!name.equals(that.name)) return false;
    return !(motivation != null ? !motivation.equals(that.motivation) : that.motivation != null);

  }

  @Override
  public int hashCode() {
    int result = covariateDefinitionKey.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    return result;
  }
}
