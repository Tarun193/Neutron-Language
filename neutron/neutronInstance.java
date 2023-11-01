public class neutronInstance {
    private neutronClass klass;

    neutronInstance(neutronClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance.";
    }
}
