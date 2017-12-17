package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.DrawCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.Deck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.Equality.areEqual;

@RunWith(MockitoJUnitRunner.class)
public class DrawCardActionTest {
    private DrawCardAction action;

    private User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardRepository cardRepository;

    @Before
    public void setUp() {
        user = new User();
        user.setItems(new ArrayList<>());
        user.setId(1L);

        when(userRepository.findOne(anyLong())).thenReturn(user);

        action = new DrawCardAction(userRepository, deckRepository, cardRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenResponseIsNotDrawCardResponse_throwsIllegalArgumentException() {
        // when
        ActionResponseDTO response = new ActionResponseDTO() {};
        action.execute(response);

        // then throws
    }

    @Test
    public void whenHiddenDeckHasThreeCards_drawsThreeCards() {
        // given
        HandCard card1 = new HandCard();
        card1.setId(1L);
        HandCard card2 = new HandCard();
        card1.setId(2L);
        HandCard card3 = new HandCard();
        card1.setId(3L);

        PlayerDeck<HandCard> hiddenDeck = new PlayerDeck<>();
        hiddenDeck.setId(1L);
        hiddenDeck.add(card1);
        hiddenDeck.add(card2);
        hiddenDeck.add(card3);
        user.setHiddenDeck(hiddenDeck);

        PlayerDeck<HandCard> handDeck = new PlayerDeck<>();
        handDeck.setId(2L);
        user.setHandDeck(handDeck);

        when(cardRepository.findOne(eq(1L))).thenReturn(card1);
        when(cardRepository.findOne(eq(2L))).thenReturn(card2);
        when(cardRepository.findOne(eq(3L))).thenReturn(card3);

        when(deckRepository.findOne(eq(1L))).thenReturn(hiddenDeck);
        when(deckRepository.findOne(eq(2L))).thenReturn(handDeck);
        when(deckRepository.save(any(Deck.class))).then(returnsFirstArg());

        // when
        action.execute(new DrawCardResponseDTO());

        // then
        areEqual(3, handDeck.size());
        areEqual(0, hiddenDeck.size());
    }

    @Test
    public void whenHiddenDeckHasLessThenThreeCards_drawsSomeCards() {
        // given
        HandCard card1 = new HandCard();
        card1.setId(1L);

        PlayerDeck<HandCard> hiddenDeck = new PlayerDeck<>();
        hiddenDeck.setId(1L);
        hiddenDeck.add(card1);
        user.setHiddenDeck(hiddenDeck);

        PlayerDeck<HandCard> handDeck = new PlayerDeck<>();
        handDeck.setId(2L);
        user.setHandDeck(handDeck);

        when(cardRepository.findOne(eq(1L))).thenReturn(card1);
        when(deckRepository.findOne(eq(1L))).thenReturn(hiddenDeck);
        when(deckRepository.findOne(eq(2L))).thenReturn(handDeck);
        when(deckRepository.save(any(Deck.class))).then(returnsFirstArg());

        // when
        action.execute(new DrawCardResponseDTO());

        // then
        areEqual(1, handDeck.size());
        areEqual(0, hiddenDeck.size());
    }
}
