package org.drugis.addis.models;

/**
 * Created by connor on 6/24/15.
 */
public class ModelCommand {

    String title;
    String linearModel;

    public ModelCommand() {
    }

    public ModelCommand(String title, String linearModel) {
        this.title = title;
        this.linearModel = linearModel;
    }

    public String getTitle() {
        return title;
    }

    public String getLinearModel() {
        return linearModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelCommand that = (ModelCommand) o;

        if (!title.equals(that.title)) return false;
        return linearModel.equals(that.linearModel);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + linearModel.hashCode();
        return result;
    }
}
