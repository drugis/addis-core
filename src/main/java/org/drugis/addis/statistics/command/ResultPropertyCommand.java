package org.drugis.addis.statistics.command;

/**
 * Created by joris on 31-1-17.
 */
public class ResultPropertyCommand {
  String name;
  Double value;

  public ResultPropertyCommand() {
  }

  public String getName() {
    return name;
  }

  public Double getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ResultPropertyCommand that = (ResultPropertyCommand) o;

    if (!name.equals(that.name)) return false;
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
