package caf.transform.datastructure;

import caf.datastructure.Argument;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javafx.util.Pair;
import net.sf.tweety.logics.pl.syntax.Proposition;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CafFormula {

    public final static String ACC_PROPOSITION = "ACC";
    public final static String ATTACK_PROPOSITION = "ATT";
    public final static String ON_PROPOSITION = "ON";

    private BiMap<Argument, Proposition> onAcPropositions;
    private BiMap<Argument, Proposition> onUPropositions;
    private BiMap<Pair<Argument, Argument>, Proposition> attPropositions;
    private BiMap<Argument, Proposition> accPropositions;
    private Set<Proposition> fakeVariables;
    private BiMap<Proposition, Integer> identifiers;

    private PropositionalFormula formula;
    private boolean shouldUpdateIdentifers;

    public CafFormula() {
        onAcPropositions = HashBiMap.create();
        onUPropositions = HashBiMap.create();
        attPropositions = HashBiMap.create();
        accPropositions = HashBiMap.create();
        fakeVariables = new HashSet<>();
        shouldUpdateIdentifers = false;
    }

    public void addAccFor(Argument a) {
        accPropositions.put(a, new Proposition(ACC_PROPOSITION + a.getName()));
        shouldUpdateIdentifers = true;
    }

    public void addOnAcFor(Argument a) {
        onAcPropositions.put(a, new Proposition(ON_PROPOSITION + a.getName()));
        shouldUpdateIdentifers = true;
    }

    public void addOnUFor(Argument a) {
        onUPropositions.put(a, new Proposition(ON_PROPOSITION + a.getName()));
        shouldUpdateIdentifers = true;
    }

    public void addAttFor(Argument source, Argument target) {
        attPropositions.put(new Pair<>(source, target), new Proposition(ATTACK_PROPOSITION + source.getName() + target.getName()));
        shouldUpdateIdentifers = true;
    }

    public Proposition getOnAcFor(Argument arg) {
        return onAcPropositions.get(arg);
    }

    public Proposition getOnUFor(Argument arg) {
        return onUPropositions.get(arg);
    }

    public Proposition getAttFor(Argument source, Argument target) {
        return attPropositions.get(new Pair<>(source, target));
    }

    public Proposition getAccFor(Argument arg) {
        return accPropositions.get(arg);
    }

    public void addFakeVariables(Set<Proposition> fakeVariables) {
        this.fakeVariables = fakeVariables;
    }

    public void setFormula(PropositionalFormula formula) {
        this.formula = formula;
    }

    public PropositionalFormula getFormula() {
        return formula;
    }

    public Collection<Proposition> getAllOnAc() {
        return onAcPropositions.values();
    }

    public Collection<Proposition> getAllOnU() {
        return onUPropositions.values();
    }

    public Collection<Proposition> getAllAtt() {
        return attPropositions.values();
    }

    public Collection<Proposition> getAllAcc() {
        return accPropositions.values();
    }

    public Collection<Proposition> getFakeVariables() {
        return fakeVariables;
    }

    public int getIdentifier(Proposition var) {
        if(shouldUpdateIdentifers) {
            updateIdentifiers();
        }
        return identifiers.get(var);
    }

    private void updateIdentifiers() {
        this.identifiers = HashBiMap.create();
        int i = 1;
        for(Proposition p : onAcPropositions.values())
            identifiers.put(p,++i);

        for(Proposition p : onUPropositions.values())
            identifiers.put(p,++i);

        for(Proposition p : attPropositions.values())
            identifiers.put(p,++i);

        for(Proposition p : accPropositions.values())
            identifiers.put(p,++i);

        for(Proposition p : fakeVariables)
            identifiers.put(p,++i);

        shouldUpdateIdentifers = false;
    }

    public int getNbVariables() {
        if(shouldUpdateIdentifers) {
            updateIdentifiers();
        }
        return identifiers.size();
    }

    public Argument getArgumentFor(Integer i) {
        Proposition prop = identifiers.inverse().get(i);

        BiMap<Proposition, Argument> inv = onAcPropositions.inverse();
        if(inv.containsKey(prop)) {
            return inv.get(prop);
        }

        inv = onUPropositions.inverse();
        if(inv.containsKey(prop)) {
            return inv.get(prop);
        }

        inv = accPropositions.inverse();
        if(inv.containsKey(prop)) {
            return inv.get(prop);
        }

        return null;
    }

    public Pair<Argument, Argument> getAttackFor(Integer i) {
        Proposition prop = identifiers.inverse().get(i);
        BiMap<Proposition, Pair<Argument, Argument>> inv2 = attPropositions.inverse();
        if(inv2.containsKey(prop)) {
            return inv2.get(prop);
        }
        return null;
    }
}
