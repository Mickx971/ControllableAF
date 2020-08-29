package math.paths;

import net.sf.tweety.arg.dung.DungTheory;
import net.sf.tweety.arg.dung.StableReasoner;
import net.sf.tweety.arg.dung.semantics.Extension;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.syntax.Attack;
import net.sf.tweety.commons.util.Pair;
import theory.datastructure.Theory;

import java.util.*;

public class PathsFinder {

    private List<Attack> findAllMinimalPathsFromExtension(DungTheory theory, Extension ext, Argument target) {
        List<Attack> attacks = new ArrayList<>();
        Queue<Argument> paths = new LinkedList<>();
        Set<Argument> visitedNodes = new HashSet<>();
        paths.add(target);

        while(!paths.isEmpty()) {
            Argument current = paths.poll();
            if(!visitedNodes.contains(current)) {
                Set<Argument> attackers = theory.getAttackers(current);
                if(!ext.contains(current)) {
                    attackers.removeIf(a -> !ext.contains(a));
                }
                paths.addAll(attackers);
                attackers.stream().forEach(a -> attacks.add(new Attack(a, current)));
                visitedNodes.add(current);
            }
        }

        return attacks;
    }

    public Pair<Collection<Argument>, List<Attack>> findReasonsFromExtension(Theory theory, Extension ext, String target) {
        Pair<Collection<Argument>, List<Attack>> reasons = new Pair<>();
        List<Attack> attacks = findAllMinimalPathsFromExtension(theory.getDungTheory(), ext, new Argument(target));
        Set<Argument> arguments = new HashSet<>();
        for(Attack attack: attacks) {
            arguments.add(attack.getAttacker());
            arguments.add(attack.getAttacked());
        }
        reasons.setFirst(arguments);
        reasons.setSecond(attacks);
        return reasons;
    }

    public static void main(String[] args) {
        PathsFinder pFinder = new PathsFinder();
        DungTheory theory = new DungTheory();

        List<Pair<String,String>> attacks = new ArrayList<>();

        //Graph1
        attacks.add(new Pair<>("0","1"));
        attacks.add(new Pair<>("5","1"));
        attacks.add(new Pair<>("4","1"));
        attacks.add(new Pair<>("1","6"));
        attacks.add(new Pair<>("1","2"));
        attacks.add(new Pair<>("1","3"));
        attacks.add(new Pair<>("2","3"));
        attacks.add(new Pair<>("2","4"));
        attacks.add(new Pair<>("3","4"));

        //Graph2
//        attacks.add(new Pair<>("1","3"));
//        attacks.add(new Pair<>("3","4"));
//        attacks.add(new Pair<>("4","2"));
//        attacks.add(new Pair<>("2","1"));
//        attacks.add(new Pair<>("1","7"));
//        attacks.add(new Pair<>("6","7"));
//        attacks.add(new Pair<>("4","5"));
//        attacks.add(new Pair<>("3","5"));

        // Graph3
//        attacks.add(new Pair<>("1","5"));
//        attacks.add(new Pair<>("1","2"));
//        attacks.add(new Pair<>("2","3"));
//        attacks.add(new Pair<>("3","1"));
//        attacks.add(new Pair<>("4","3"));

        //Graph4
//        attacks.add(new Pair<>("1","5"));
//        attacks.add(new Pair<>("1","6"));
//        attacks.add(new Pair<>("6","2"));
//        attacks.add(new Pair<>("2","3"));
//        attacks.add(new Pair<>("3","1"));
//        attacks.add(new Pair<>("4","3"));

        // Graph5
//        attacks.add(new Pair<>("1","5"));
//        attacks.add(new Pair<>("1","2"));
//        attacks.add(new Pair<>("2","3"));
//        attacks.add(new Pair<>("2","5"));
//        attacks.add(new Pair<>("3","1"));
//        attacks.add(new Pair<>("4","3"));

        for(int i = 0; i < attacks.size(); i++){
            Attack attack = new Attack(new Argument(attacks.get(i).getFirst()), new Argument(attacks.get(i).getSecond()));
            theory.add(attack.getAttacker());
            theory.add(attack.getAttacked());
            theory.add(attack);
        }

        StableReasoner reasoner = new StableReasoner(theory);
        Iterator<Extension> it = reasoner.getExtensions().iterator();
        Extension ext = it.next();

        Argument target = new Argument("4");
        System.out.println("Extension: " + ext + " Target: " + target.getName());
        Collection<Attack> res = pFinder.findAllMinimalPathsFromExtension(theory, ext, target);

        res.forEach(r -> System.out.println(r));
        System.out.println("ok");
    }
}
