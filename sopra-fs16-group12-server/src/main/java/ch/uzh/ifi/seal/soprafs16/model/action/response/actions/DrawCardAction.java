package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.DrawCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;

public class DrawCardAction implements Action{

    private UserRepository userRepo;
    private DeckRepository deckRepo;
    private CardRepository cardRepo;

    public DrawCardAction(UserRepository userRepo, DeckRepository deckRepo, CardRepository cardRepo) {
        this.userRepo = userRepo;
        this.deckRepo = deckRepo;
        this.cardRepo = cardRepo;
    }

    @Override
    public void execute(ActionResponseDTO response) {
        if(!(response instanceof DrawCardResponseDTO))
            throw new IllegalArgumentException("Must be a DrawCardResponse, was " + response.getClass().getName() + " instead.");

        User user = userRepo.findOne(response.getUserId());

        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne(user.getHiddenDeck().getId());
        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>) deckRepo.findOne(user.getHandDeck().getId());

        if(hiddenDeck.size() > 0) {
            for (int i = 0; i < 3 && hiddenDeck.size() > 0; i++) {
                HandCard handCard = (HandCard) hiddenDeck.get((int) (Math.random() * hiddenDeck.size()));
                hiddenDeck.getCards().remove(handCard);
                hiddenDeck = deckRepo.save(hiddenDeck);
                handDeck.getCards().add(handCard);
                handCard.setDeck(handDeck);

                cardRepo.save(handCard);
                deckRepo.save(handDeck);
            }
        }
    }
}
