package org.drugis.addis.projects;

/**
 * Created by joris on 2-2-17.
 */
public class CopyCommand {
  private String newTitle;

  public CopyCommand() {
  }

  public CopyCommand(String newTitle) {
    this.newTitle = newTitle;
  }

  public String getNewTitle() {
    return newTitle;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CopyCommand that = (CopyCommand) o;

    return newTitle.equals(that.newTitle);
  }

  @Override
  public int hashCode() {
    return newTitle.hashCode();
  }
}
