package pt.up.fe.comp.optimization.register_allocation;

public class LivelinessRange {
    private int start;
    private int end;
    public LivelinessRange(int start) {
        this.start = start;
        this.end = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public String toString() {
        return start + "-" + end;
    }
}
