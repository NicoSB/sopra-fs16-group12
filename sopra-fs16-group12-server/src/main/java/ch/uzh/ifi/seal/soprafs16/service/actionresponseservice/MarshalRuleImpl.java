package ch.uzh.ifi.seal.soprafs16.service.actionresponseservice;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MarshalRuleImpl implements MarshalRule {

    private UserRepository userRepo;
    private WagonLevelRepository wagonLevelRepo;
    private CardRepository cardRepo;
    private DeckRepository deckRepo;

    @Autowired
    public MarshalRuleImpl(RepositoryBundle repositories) {
        userRepo = repositories.getUserRepository();
        wagonLevelRepo = repositories.getWagonLevelRepository();
        cardRepo = repositories.getCardRepository();
        deckRepo = repositories.getDeckRepository();
    }

    @Override
    public void execute(GameDTO game) {     // If users are in the same wagonLevel as the marshal
        WagonLevel marshalWagonLevel = wagonLevelRepo.findOne(game.getMarshal().getWagonLevel().getId());
        giveBulletsToPlayersInCoach(game, marshalWagonLevel);
    }

    private void giveBulletsToPlayersInCoach(GameDTO game, WagonLevel marshalWagonLevel) {
        if (!marshalWagonLevel.getUsers().isEmpty()) {
            GameDeck<BulletCard> neutralBulletsDeck = (GameDeck<BulletCard>) deckRepo.findOne(game.getNeutralBulletsDeck().getId());
            while(!marshalWagonLevel.getUsers().isEmpty()){
                User victim = marshalWagonLevel.getUsers().get(0);
                putBulletCardInHandDeck(neutralBulletsDeck, victim);
                putPlayerOnRoof(victim);
            }
        }
    }

    private void putBulletCardInHandDeck(GameDeck<BulletCard> neutralBulletsDeck, User user) {
        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
        BulletCard bc = (BulletCard) neutralBulletsDeck.remove(0);
        bc.setDeck(hiddenDeck);
        deckRepo.save(neutralBulletsDeck);
        hiddenDeck.add(bc);

        cardRepo.save(bc);
        deckRepo.save(hiddenDeck);
    }

    void putPlayerOnRoof(User user) {
        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
        WagonLevel newWl = wagonLevelRepo.findOne(user.getWagonLevel().getWagon().getTopLevel().getId());

        wl.removeUserById(user.getId());
        wagonLevelRepo.save(wl);

        user.setWagonLevel(newWl);
        userRepo.save(user);

        newWl.getUsers().add(user);
        newWl = wagonLevelRepo.save(newWl);
    }
}
