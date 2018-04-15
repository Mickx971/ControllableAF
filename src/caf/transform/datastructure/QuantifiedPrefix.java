package caf.transform.datastructure;

import net.sf.tweety.logics.pl.syntax.Proposition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class QuantifiedPrefix {

    public enum QuantifiedPrefixType {
        EXIST,
        ALL
    }

    private Set<Proposition> quantifiedPropositions;
    private QuantifiedPrefixType type;

    public QuantifiedPrefix(QuantifiedPrefixType type) {
        quantifiedPropositions = new HashSet<>();
        this.type = type;
    }

    public void add(Proposition proposition) {
        quantifiedPropositions.add(proposition);
    }

    public void addAll(Collection<Proposition> propositions) {
        quantifiedPropositions.addAll(propositions);
    }

    public Collection<Proposition> getQuantifiedProposition() {
        return quantifiedPropositions;
    }

    public QuantifiedPrefixType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "QuantifiedPrefix{" +
                "type=" + type +
                ", quantifiedPropositions=" + quantifiedPropositions +
                '}';
    }
}
