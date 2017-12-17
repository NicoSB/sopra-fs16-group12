package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.ShootResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Tuco;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

public class ShootAction implements Action {

    private UserRepository userRepo;
    private WagonLevelRepository wagonLevelRepo;
    private DeckRepository deckRepo;
    private CardRepository cardRepo;

    public ShootAction(UserRepository userRepo, WagonLevelRepository wagonLevelRepo, DeckRepository deckRepo, CardRepository cardRepo) {
        this.userRepo = userRepo;
        this.wagonLevelRepo = wagonLevelRepo;
        this.deckRepo = deckRepo;
        this.cardRepo = cardRepo;
    }

    @Override
    public void execute(ActionResponseDTO response) {
        if (!(response instanceof ShootResponseDTO))
            throw new IllegalArgumentException("Must be a ShootResponseDTO, was " + response.getClass().getName() + " instead.");

        ShootResponseDTO shootResponse = (ShootResponseDTO) response;

        User user = userRepo.findOne(shootResponse.getUserId());

        if (user.getBulletsDeck().size() > 0) {
            User victim = userRepo.findOne(shootResponse.getVictimId());
            PlayerDeck<BulletCard> bulletCardDeck = user.getBulletsDeck();
            PlayerDeck<HandCard> hiddenDeck = victim.getHiddenDeck();
            BulletCard bc = (BulletCard) bulletCardDeck.remove(user.getBulletsDeck().size() - 1);
            hiddenDeck.add(bc);

            bc.setDeck(hiddenDeck);

            // Character Skill Tuco
            if (user.getCharacter() instanceof Tuco) {
                WagonLevel wl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
                WagonLevel wlNew = null;
                if (user.getWagonLevel().getWagon().getId() < victim.getWagonLevel().getWagon().getId()
                        && wl.getWagonLevelAfter() != null) {
                    wlNew = wagonLevelRepo.findOne(wl.getWagonLevelAfter().getId());
                } else if (user.getWagonLevel().getWagon().getId() > victim.getWagonLevel().getWagon().getId() && wl.getWagonLevelBefore() != null) {
                    wlNew = wagonLevelRepo.findOne(wl.getWagonLevelBefore().getId());
                }

                if (wlNew != null) {
                    wl.removeUserById(victim.getId());
                    wlNew.getUsers().add(victim);
                    victim.setWagonLevel(wlNew);

                    wagonLevelRepo.save(wl);
                    wagonLevelRepo.save(wlNew);
                }
            }

            deckRepo.save(bulletCardDeck);
            deckRepo.save(hiddenDeck);
            userRepo.save(victim);
            userRepo.save(user);
            cardRepo.save(bc);
        }
    }
}
