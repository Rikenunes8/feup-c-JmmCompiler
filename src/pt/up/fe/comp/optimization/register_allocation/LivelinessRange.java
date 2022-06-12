package pt.up.fe.comp.optimization.register_allocation;

public class LivelinessRange {
    private int start;
    private int end;
    public LivelinessRange(int start) {
        this.start = start;
        this.end = -1;
    }

    public void setStart(int start) {
        this.start = start;
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

    public void removeEnd() {
        this.end = -1;
    }

    public boolean hasEnd() {
        return end != -1;
    }
}
