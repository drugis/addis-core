package org.drugis.addis.covariates;

/**
 * Created by connor on 12/1/15.
 */
public class AddCovariateCommand {

  private CovariateOption definition;
  private String name;
  private String motivation;

  public AddCovariateCommand() {
  }

  public AddCovariateCommand(CovariateOption definition, String name) {
    this(definition, name, null);
  }

  public AddCovariateCommand(CovariateOption definition, String name, String motivation) {
    this.definition = definition;
    this.name = name;
    this.motivation = motivation;
  }

  public CovariateOption getDefinition() {
    return definition;
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

    if (definition != that.definition) return false;
    if (!name.equals(that.name)) return false;
    return !(motivation != null ? !motivation.equals(that.motivation) : that.motivation != null);

  }

  @Override
  public int hashCode() {
    int result = definition.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    return result;
  }
}
