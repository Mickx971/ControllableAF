package caf.transform.datastructure;

import caf.datastructure.Argument;
import caf.datastructure.Attack;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javafx.util.Pair;
import net.sf.tweety.logics.pl.syntax.Conjunction;
import net.sf.tweety.logics.pl.syntax.Proposition;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CafFormula {

    public final static String ACC_PROPOSITION = "ACC";
    public final static String ATTACK_PROPOSITION = "ATT";
    public final static String UDATTACK_PROPOSITION = "UDATT";
    public final static String ON_PROPOSITION = "ON";
    public final static Attack FAKE_ATTACK = new Attack();

    private BiMap<Argument, Proposition> onAcPropositions;
    private BiMap<Argument, Proposition> onUPropositions;
    private BiMap<Pair<Argument, Argument>, Proposition> attPropositions;
    private BiMap<Pair<Argument, Argument>, Proposition> uattPropositions;
    private Map<Attack, BiMap<Argument, Proposition>> accPropositions;
    private Set<Proposition> fakeVariables;
    private BiMap<Proposition, Integer> identifiers;

    private PropositionalFormula formula;
    private boolean shouldUpdateIdentifers;

    public CafFormula() {
        onAcPropositions = HashBiMap.create();
        onUPropositions = HashBiMap.create();
        attPropositions = HashBiMap.create();
        accPropositions = new HashMap<>();
        uattPropositions = HashBiMap.create();
        fakeVariables = new HashSet<>();
        shouldUpdateIdentifers = false;
    }

    public void addAccFor(Argument a, Attack udAtt) {
        Proposition p;

        if(udAtt == null) {
            udAtt = FAKE_ATTACK;
            p = new Proposition(ACC_PROPOSITION + a.getName() + "_" + UDATTACK_PROPOSITION + "_FAKE");
        }
        else {
            Argument[] args = udAtt.getArguments();
            p = new Proposition(ACC_PROPOSITION + a.getName() + "_" + UDATTACK_PROPOSITION + "_" + args[0].getName() + args[1].getName());
        }

        if(!accPropositions.containsKey(udAtt)) {
            accPropositions.put(udAtt, HashBiMap.create());
        }

        accPropositions.get(udAtt).put(a, p);
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

    public Proposition getAccFor(Argument arg, Attack ua) {
        if(ua == null)
            ua = FAKE_ATTACK;
        if(accPropositions.containsKey(ua))
            return accPropositions.get(ua).get(arg);
        return null;
    }

    public Collection<Proposition> getACCsFor(Argument arg) {
        return accPropositions.values().stream()
                .filter(m -> m.containsKey(arg))
                .map(m -> m.get(arg)).collect(Collectors.toList());
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
        return accPropositions.values().stream()
                .flatMap(m -> m.values().stream())
                .collect(Collectors.toList());
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

        for(BiMap<Argument, Proposition> map : accPropositions.values()) {
            for(Proposition p: map.values()) {
                identifiers.put(p, ++i);
            }
        }

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

        for(BiMap<Argument, Proposition> m : accPropositions.values()) {
            if(m.inverse().containsKey(prop)) {
                return m.inverse().get(prop);
            }
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

    public boolean isAttack(int id)
    {
        return attPropositions.inverse().containsKey(identifiers.inverse().get(id));
    }

    public boolean isArgument(int id) {
        return accPropositions.values().stream()
                .anyMatch(
                        m -> m.inverse().containsKey(identifiers.inverse().get(id))
                );
    }

    public boolean isOnId(Integer i) {
        return onAcPropositions.inverse().containsKey(identifiers.inverse().get(i));
    }

    public Collection<Argument> getArgumentsFor(List<Integer> ids) {
        List<Argument> correspondingArguments = new ArrayList<>();
        for(Integer id: ids) {
            System.out.println(identifiers.inverse().get(id));
            if(isOnId(id)) {
                System.out.println("coucou");
                correspondingArguments.add(onAcPropositions.inverse().get(id));
            }
        }
        return correspondingArguments;

    }
}
