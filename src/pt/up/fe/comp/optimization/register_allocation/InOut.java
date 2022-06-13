package pt.up.fe.comp.optimization.register_allocation;

import org.specs.comp.ollir.Instruction;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class InOut {
    private final Set<String> in;
    private final Set<String> out;

    public InOut() {
        this.in = new HashSet<>();
        this.out = new HashSet<>();
    }
    public InOut(Set<String> in, Set<String> out) {
        this.in = in;
        this.out = out;
    }

    public Set<String> getIn() {
        return in;
    }
    public Set<String> getOut() {
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InOut inOut = (InOut) o;
        return Objects.equals(in, inOut.in) && Objects.equals(out, inOut.out);
    }

    @Override
    public int hashCode() {
        return Objects.hash(in, out);
    }
}
