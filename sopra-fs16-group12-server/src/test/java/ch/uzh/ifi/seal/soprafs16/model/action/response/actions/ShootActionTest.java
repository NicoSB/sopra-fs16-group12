package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.Wagon;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.ShootResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.cards.Deck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Character;
import ch.uzh.ifi.seal.soprafs16.model.characters.Tuco;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.Equality.areEqual;

@RunWith(MockitoJUnitRunner.class)
public class ShootActionTest {

    private ShootAction action;

    private User attacker;
    private User victim;
    private BulletCard bulletCard;
    private WagonLevel connectedWagonLevel;
    private WagonLevel victimWagonLevel;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WagonLevelRepository wagonLevelRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardRepository cardRepository;

    @Before
    public void setUp() {
        bulletCard = new BulletCard();
        bulletCard.setId(1L);

        PlayerDeck<BulletCard> attackerBulletsDeck = new PlayerDeck<>();
        attackerBulletsDeck.add(bulletCard);

        attacker = new User();
        attacker.setCharacter(new Character());
        attacker.setId(1L);
        attacker.setBulletsDeck(attackerBulletsDeck);

        PlayerDeck<HandCard> victimHiddenDeck = new PlayerDeck<>();

        victim = new User();
        victim.setCharacter(new Character());
        victim.setId(2L);
        victim.setHiddenDeck(victimHiddenDeck);

        when(userRepository.findOne(eq(attacker.getId()))).thenReturn(attacker);
        when(userRepository.findOne(eq(victim.getId()))).thenReturn(victim);
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        when(deckRepository.findOne(eq(attackerBulletsDeck.getId()))).thenReturn(attackerBulletsDeck);
        when(deckRepository.findOne(eq(victimHiddenDeck.getId()))).thenReturn(victimHiddenDeck);
        when(deckRepository.save(any(Deck.class))).then(returnsFirstArg());

        when(cardRepository.findOne(eq(bulletCard.getId()))).thenReturn(bulletCard);
        when(cardRepository.save(any(Card.class))).then(returnsFirstArg());

        action = new ShootAction(userRepository, wagonLevelRepository, deckRepository, cardRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenResponseIsNotCollectItemResponse_throwsIllegalArgumentException() {
        // when
        ActionResponseDTO response = new ActionResponseDTO() {};
        action.execute(response);

        // then throws
    }

    @Test
    public void whenVictimIsShot_bulletCardIsTransferred() {
        // when
        ShootResponseDTO response = new ShootResponseDTO();
        response.setUserId(attacker.getId());
        response.setVictimId(victim.getId());

        action.execute(response);

        // then
        areEqual(0, attacker.getBulletsDeck().size());
        areEqual(1, victim.getHiddenDeck().size());
        areEqual(bulletCard, victim.getHiddenDeck().get(0));
        areEqual(victim.getHandDeck(), bulletCard.getDeck());
    }

    @Test
    public void whenAttackerHasNoBulletsLeft_nothingHappens() {
        // given
        attacker.setBulletsDeck(new PlayerDeck<>());

        // when
        ShootResponseDTO response = new ShootResponseDTO();
        response.setUserId(attacker.getId());
        response.setVictimId(victim.getId());

        action.execute(response);

        // then
        areEqual(0, attacker.getBulletsDeck().size());
        areEqual(0, victim.getHiddenDeck().size());
    }

    @Test
    public void whenAttackerIsTucoAndIsInFront_victimIsPushedBack() {
        // given
        attacker.setCharacter(new Tuco());
        initWagonLevels(true, true);

        // when
        ShootResponseDTO response = new ShootResponseDTO();
        response.setUserId(attacker.getId());
        response.setVictimId(victim.getId());

        action.execute(response);

        // then
        areEqual(1, connectedWagonLevel.getUsers());
        areEqual(connectedWagonLevel, victim.getWagonLevel());
        areEqual(victim, connectedWagonLevel.getUsers().get(0));
    }

    @Test
    public void whenTucoIsInFrontAndVictimIsOnLastWagon_victimIsNotPushedBack() {
        // given
        attacker.setCharacter(new Tuco());
        initWagonLevels(true, false);

        // when
        ShootResponseDTO response = new ShootResponseDTO();
        response.setUserId(attacker.getId());
        response.setVictimId(victim.getId());

        action.execute(response);

        // then
        areEqual(1, victimWagonLevel.getUsers());
        areEqual(victimWagonLevel, victim.getWagonLevel());
        areEqual(victim, victimWagonLevel.getUsers().get(0));
    }

    @Test
    public void whenAttackerIsTucoAndIsBehind_victimIsPushedForwards() {
        // given
        attacker.setCharacter(new Tuco());
        initWagonLevels(false, true);

        // when
        ShootResponseDTO response = new ShootResponseDTO();
        response.setUserId(attacker.getId());
        response.setVictimId(victim.getId());

        action.execute(response);

        // then
        areEqual(1, connectedWagonLevel.getUsers());
        areEqual(connectedWagonLevel, victim.getWagonLevel());
        areEqual(victim, connectedWagonLevel.getUsers().get(0));
    }

    @Test
    public void whenTucoIsBehindAndVictimIsInFirstWagon_victimIsPushedForwards() {
        // given
        attacker.setCharacter(new Tuco());
        initWagonLevels(false, false);

        // when
        ShootResponseDTO response = new ShootResponseDTO();
        response.setUserId(attacker.getId());
        response.setVictimId(victim.getId());

        action.execute(response);

        // then
        areEqual(1, victimWagonLevel.getUsers());
        areEqual(victimWagonLevel, victim.getWagonLevel());
        areEqual(victim, victimWagonLevel.getUsers().get(0));
    }

    private void initWagonLevels(boolean tucoIsInFront, boolean wagonsAreConnected) {
        long tucoWagonId = tucoIsInFront ? 1L : 2L;
        long victimWagonId = tucoIsInFront ? 2L : 1L;

        WagonLevel tucoWagonLevel = new WagonLevel();
        tucoWagonLevel.setId(0L);
        attacker.setWagonLevel(tucoWagonLevel);

        Wagon tucoWagon = new Wagon();
        tucoWagon.setId(tucoWagonId);
        tucoWagon.setBottomLevel(tucoWagonLevel);
        tucoWagonLevel.setWagon(tucoWagon);

        victimWagonLevel = new WagonLevel();
        List<User> users = new ArrayList<>();
        users.add(victim);
        victimWagonLevel.setUsers(users);

        victim.setWagonLevel(victimWagonLevel);

        Wagon victimWagon = new Wagon();
        victimWagon.setId(victimWagonId);
        victimWagon.setBottomLevel(victimWagonLevel);
        victimWagonLevel.setWagon(victimWagon);

        if (wagonsAreConnected) {
            connectedWagonLevel = new WagonLevel();
            connectedWagonLevel.setId(3L);
            connectedWagonLevel.setUsers(new ArrayList<>());

            victimWagonLevel.setWagonLevelBefore(tucoIsInFront ? null : connectedWagonLevel);
            victimWagonLevel.setWagonLevelAfter(tucoIsInFront ? connectedWagonLevel : null);

            when(wagonLevelRepository.findOne(eq(connectedWagonLevel.getId()))).thenReturn(connectedWagonLevel);
        }

        when(wagonLevelRepository.findOne(eq(tucoWagonLevel.getId()))).thenReturn(tucoWagonLevel);
        when(wagonLevelRepository.findOne(eq(victimWagonLevel.getId()))).thenReturn(victimWagonLevel);
    }
}
