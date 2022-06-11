package pt.up.fe.comp.optimization.register_allocation.useless;

import java.util.List;

public class Web {
    private String name;
    private List<LivenessRange> ranges;

    public Web(String name) {
        this.name = name;
    }
}
