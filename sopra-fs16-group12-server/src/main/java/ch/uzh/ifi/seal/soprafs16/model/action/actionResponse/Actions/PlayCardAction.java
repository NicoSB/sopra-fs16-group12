package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.Actions;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ResponseDTOs.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Ghost;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.TunnelTurn;

public class PlayCardAction implements Action {

    private GameRepository gameRepo;
    private UserRepository userRepo;
    private CardRepository cardRepo;
    private DeckRepository deckRepo;

    public PlayCardAction(GameRepository gameRepo, UserRepository userRepo, CardRepository cardRepo, DeckRepository deckRepo) {
        this.gameRepo = gameRepo;
        this.userRepo = userRepo;
        this.cardRepo = cardRepo;
        this.deckRepo = deckRepo;
    }

    @Override
    public void execute(ActionResponseDTO response) {
        if (!(response instanceof PlayCardResponseDTO))
            throw new IllegalArgumentException("Must be a PlayCardResponseDTO, was " + response.getClass().getName() + " instead.");

        PlayCardResponseDTO playResponse = (PlayCardResponseDTO) response;

        GameDTO game = gameRepo.findOne(playResponse.getSpielId());
        User user = userRepo.findOne(playResponse.getUserId());

        ActionCard ac = (ActionCard) cardRepo.findOne(playResponse.getPlayedCardId());
        PlayerDeck<HandCard> handDeck = user.getHandDeck();
        handDeck.removeById(ac.getId());

        GameDeck<ActionCard> commonDeck = game.getCommonDeck();
        commonDeck.add(ac);
        ac.setDeck(commonDeck);
        ac.setPos(game.getActionRequestCounter());
        ac.setPlayedByUserId(user.getId());
        ac.setPlayedHidden(false);

        // TunnelTurn and Ghost character skill
        if(game.getCurrentTurnType() instanceof TunnelTurn ||
                user.getCharacter() instanceof Ghost && game.getCurrentTurn() == 0){
            ac.setPlayedHidden(true);
        }

        cardRepo.save(ac);
        deckRepo.save(handDeck);
        deckRepo.save(commonDeck);
    }
}
