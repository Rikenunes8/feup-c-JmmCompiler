package pt.up.fe.comp.optimization.register_allocation.useless;

public class LivenessRange {
    private int start;
    private int end;
    public LivenessRange(int start, int end) {
        this.start = start;
        this.end = end;
    }
}
