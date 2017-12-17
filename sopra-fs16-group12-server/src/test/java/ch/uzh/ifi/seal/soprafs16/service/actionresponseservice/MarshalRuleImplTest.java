package ch.uzh.ifi.seal.soprafs16.service.actionresponseservice;

import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.*;
import ch.uzh.ifi.seal.soprafs16.model.action.response.ActionResponse;
import ch.uzh.ifi.seal.soprafs16.model.action.response.ActionResponseFactory;
import ch.uzh.ifi.seal.soprafs16.model.action.response.actions.Action;
import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.cards.Deck;
import ch.uzh.ifi.seal.soprafs16.model.repositories.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MarshalRuleImplTest {

    private GameDTO game;
    private Marshal marshal;
    private User user;
    private WagonLevel bottomLevel;
    private WagonLevel topLevel;
    private PlayerDeck<HandCard> playerDeck;
    private BulletCard bulletCard;

    @Mock
    WagonLevelRepository wagonLevelRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    GameRepository gameRepository;
    @Mock
    DeckRepository deckRepository;
    @Mock
    CardRepository cardRepository;
    @Mock
    MarshalRepository marshalRepository;
    @Mock
    ActionResponseFactory actionResponseFactory;

    @InjectMocks
    RepositoryBundle bundle;

    @Before
    public void setUp() {
        marshal = new Marshal();

        user = new User();
        user.setId(1L);

        List<User> users = new ArrayList<>();

        bottomLevel = new WagonLevel();
        bottomLevel.setId(1L);
        bottomLevel.setUsers(users);
        bottomLevel.setMarshal(marshal);
        bottomLevel.setLevelType(LevelType.BOTTOM);
        marshal.setWagonLevel(bottomLevel);

        topLevel = new WagonLevel();
        topLevel.setId(2L);
        topLevel.setLevelType(LevelType.TOP);
        topLevel.setUsers(new ArrayList<User>());

        Wagon marshalWagon = new Wagon();
        marshalWagon.setBottomLevel(bottomLevel);
        marshalWagon.setTopLevel(topLevel);
        bottomLevel.setWagon(marshalWagon);
        topLevel.setWagon(marshalWagon);

        bulletCard = new BulletCard();

        GameDeck<BulletCard> neutralBulletsDeck = new GameDeck<>();
        neutralBulletsDeck.setId(22L);
        neutralBulletsDeck.add(bulletCard);

        playerDeck = new PlayerDeck<>();
        playerDeck.setId(11L);

        user.setHiddenDeck(playerDeck);

        user.setWagonLevel(bottomLevel);
        users.add(user);

        ArrayList<Wagon> wagons = new ArrayList<>();
        wagons.add(marshalWagon);

        game = new GameDTO();
        game.setMarshal(marshal);
        game.setWagons(wagons);
        game.setNeutralBulletsDeck(neutralBulletsDeck);
        neutralBulletsDeck.setGame(game);

        Action voidAction = response -> {};
        ActionResponse voidResponse = new ActionResponse();
        voidResponse.setAction(voidAction);

        when(actionResponseFactory.createActionResponse(any())).thenReturn(voidResponse);

        when(wagonLevelRepository.findOne(1L)).thenReturn(bottomLevel);
        when(wagonLevelRepository.findOne(2L)).thenReturn(topLevel);
        when(wagonLevelRepository.save(any(WagonLevel.class))).then(returnsFirstArg());

        when(userRepository.findOne(anyLong())).thenReturn(user);
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        when(gameRepository.findOne(anyLong())).thenReturn(game);
        when(gameRepository.save(any(GameDTO.class))).then(returnsFirstArg());

        when(deckRepository.findOne(eq(playerDeck.getId()))).thenReturn(playerDeck);
        when(deckRepository.findOne(AdditionalMatchers.not(eq(playerDeck.getId())))).thenReturn(neutralBulletsDeck);
        when(deckRepository.save(any(Deck.class))).then(returnsFirstArg());

        when(cardRepository.save(any(Card.class))).then(returnsFirstArg());

        when(marshalRepository.findOne(anyLong())).thenReturn(marshal);
    }

    @Test
    public void whenPlayerIsInSameCoachAsMarshal_movesPlayerToRoof() {
        MarshalRuleImpl marshalRule = new MarshalRuleImpl(bundle);

        // when
        marshalRule.execute(game);

        // then
        assertEquals(0, bottomLevel.getUsers().size());
        assertEquals(1, topLevel.getUsers().size());
        assertEquals(topLevel, user.getWagonLevel());
    }

    @Test
    public void whenPlayerIsInSameCoachAsMarshal_playerReceivesBulletCard() {
        MarshalRuleImpl marshalRule = new MarshalRuleImpl(bundle);

        // when
        marshalRule.execute(game);

        // then
        assertEquals(1, playerDeck.size());
        assertEquals(bulletCard, playerDeck.get(0));
    }
}
