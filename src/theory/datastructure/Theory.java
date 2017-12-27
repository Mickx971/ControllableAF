package theory.datastructure;

import Communication.datastructure.Argument;
import Communication.datastructure.Attack;
import theory.datastructure.Offer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Theory {

    private Map<Offer, Set<String>> offers;

    public void removeOfferSupport(Offer offerName, String argumentName) {
        offers.get(offerName).remove(argumentName);
        // retirer de la theory (argument + attaques)
    }

    public boolean hasSupportForOffer(Offer offerName) {
        return !offers.get(offerName).isEmpty();
    }

    public String getSupportForOffer(Offer offer) {
        if(hasSupportForOffer(offer)) {
            return offers.get(offer).stream().findFirst().get();
        }
        return null;
    }

    public void removeOffer(Offer offer) {

    }

    public void removeOfferSupports(Offer offer) {
        for(String support : offers.get(offer)) {
            // retirer de la theory (argument + attaques)
        }
        offers.remove(offer);
    }

    public Offer getNextOffer() {
        return null;
    }

    public boolean argumentIsCredulouslyAccepted(Argument practicalArgument) {
        return false;
    }

    public void update(Collection<Argument> justificationArguments, Collection<Attack> justificationAttacks) {

    }
}
