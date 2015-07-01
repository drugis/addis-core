package org.drugis.addis.models;

/**
 * Created by connor on 6/24/15.
 */
public class ModelCommand {

    String title;

    public ModelCommand() {
    }

    public ModelCommand(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelCommand that = (ModelCommand) o;

        return !(title != null ? !title.equals(that.title) : that.title != null);

    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }
}
