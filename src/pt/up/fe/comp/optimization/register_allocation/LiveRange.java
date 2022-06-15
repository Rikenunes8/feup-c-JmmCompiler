package pt.up.fe.comp.optimization.register_allocation;

public class LiveRange {
    private int start;
    private int end;

    public LiveRange(int start) {
        this.start = start;
        this.end = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStart() {
        return start;
    }
    public int getEnd() {
        return end;
    }

    public String toString() {
        return start + "-" + end;
    }
}
