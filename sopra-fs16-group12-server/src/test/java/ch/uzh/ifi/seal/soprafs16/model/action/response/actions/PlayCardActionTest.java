package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.ActionResponse;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.Equality.areEqual;

@RunWith(MockitoJUnitRunner.class)
public class PlayCardActionTest {

    private PlayCardAction action;
    private HandCard card;
    private PlayerDeck<HandCard> handDeck;
    private GameDeck<ActionCard> commonDeck;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @Before
    public void setUp() {
        GameDTO game = new GameDTO();
        game.setId(1L);
        game.setActionRequestCounter(0);

        commonDeck = new GameDeck<>();
        game.setCommonDeck(commonDeck);

        card = new ActionCard() {
            @Override
            public ActionRequestDTO createActionRequest(GameDTO game, User user) {
                return null;
            }
        };
        card.setId(1L);

        handDeck = new PlayerDeck<>();
        handDeck.add(card);
        card.setDeck(handDeck);

        User user = new User();
        user.setHandDeck(handDeck);
        user.setId(1L);

        List<User> users = new ArrayList<>();
        users.add(user);
        game.setUsers(users);

        when(gameRepository.findOne(anyLong())).thenReturn(game);
        when(userRepository.findOne(anyLong())).thenReturn(user);
        when(cardRepository.findOne(anyLong())).thenReturn(card);

        action = new PlayCardAction(gameRepository, userRepository, cardRepository, deckRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenResponseIsNotDrawCardResponse_throwsIllegalArgumentException() {
        // when
        ActionResponseDTO response = new ActionResponseDTO() {};
        action.execute(response);

        // then throws
    }

    @Test
    public void whenResponseIsValid_movesCardToCommonDeck() {
        // when
        PlayCardResponseDTO response = new PlayCardResponseDTO();
        response.setPlayedCardId(card.getId());

        action.execute(response);

        // then
        areEqual(0, handDeck.size());
        areEqual(1, commonDeck.size());
        areEqual(commonDeck, card.getDeck());
        areEqual(card, commonDeck.get(0));
    }
}
