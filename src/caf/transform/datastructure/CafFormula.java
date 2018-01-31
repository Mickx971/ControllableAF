package caf.transform.datastructure;

import caf.datastructure.Argument;
import caf.datastructure.Attack;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.sf.tweety.commons.util.Pair;
import net.sf.tweety.logics.pl.syntax.Proposition;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;

import java.util.*;
import java.util.stream.Collectors;

public class CafFormula {

    public final static String ACC_PROPOSITION = "Acc";
    public final static String ATTACK_PROPOSITION = "Att";
    public final static String UDATTACK_PROPOSITION = "UdAtt";
    public final static String ON_PROPOSITION = "On";
    public final static Attack FAKE_ATTACK = new Attack();

    private BiMap<Argument, Proposition> onAcPropositions;
    private BiMap<Argument, Proposition> onUPropositions;
    private BiMap<Pair<Argument, Argument>, Proposition> attPropositions;
    private BiMap<Pair<Argument, Argument>, Proposition> uattPropositions;
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
        uattPropositions = HashBiMap.create();
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

    public void setUAtt(Attack att) {
        Pair<Argument, Argument> pair = new Pair<>(att.getSource(), att.getTarget());
        uattPropositions.put(pair, attPropositions.get(pair));
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

    public Collection<Proposition> getAllUAtt() {
        return uattPropositions.values();
    }

    public Collection<Proposition> getAllAcc() {
        return accPropositions.values();
    }

    public Collection<Proposition> getFakeVariables() {
        return fakeVariables;
    }

    public Set<Proposition> getAllVariables() {
        Set<Proposition> all = new HashSet<>();
        all.addAll(getFakeVariables());
        all.addAll(getAllAcc());
        all.addAll(getAllAtt());
        all.addAll(getAllOnAc());
        all.addAll(getAllOnU());
        return all;
    }

    public int getIdentifier(Proposition var) {
        if(shouldUpdateIdentifers) {
            updateIdentifiers();
        }
        return identifiers.get(var);
    }

    private void updateIdentifiers() {
        this.identifiers = HashBiMap.create();
        int i = 0;
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

    public boolean isAttack(int id) {
        return attPropositions.inverse().containsKey(identifiers.inverse().get(id));
    }

    public boolean isArgument(int id) {
        return accPropositions.inverse().containsKey(identifiers.inverse().get(id));
    }

    public boolean isOnId(Integer i) {
        return onAcPropositions.inverse().containsKey(identifiers.inverse().get(i));
    }

    public Collection<Argument> getArgumentsFor(List<Integer> ids) {
        List<Argument> correspondingArguments = new ArrayList<>();
        for(Integer id: ids) {
            if(isOnId(id)) {
                correspondingArguments.add(onAcPropositions.inverse().get(identifiers.inverse().get(id)));
            }
        }
        return correspondingArguments;
    }
}
