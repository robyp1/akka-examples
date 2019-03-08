package sample.hello;

import java.io.Serializable;
import java.util.Objects;

public class TaskResult implements Serializable {
    private final Boolean result;

    public TaskResult(Boolean aBoolean) {
        this.result =aBoolean;
    }

    public Boolean getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "result=" + result +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskResult that = (TaskResult) o;
        return Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {

        return Objects.hash(result);
    }
}
