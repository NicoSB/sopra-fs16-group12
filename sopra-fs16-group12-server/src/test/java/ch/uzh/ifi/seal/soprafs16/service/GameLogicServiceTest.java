package ch.uzh.ifi.seal.soprafs16.service;

import ch.uzh.ifi.seal.soprafs16.model.*;
import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ChangeLevelCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.CollectCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.AngryMarshalCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.BrakingCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.GetItAllCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.HostageCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.MarshallsRevengeCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PassengerRebellionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PickPocketingCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PivotablePoleCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Character;
import ch.uzh.ifi.seal.soprafs16.model.characters.Cheyenne;
import ch.uzh.ifi.seal.soprafs16.model.characters.Doc;
import ch.uzh.ifi.seal.soprafs16.model.characters.Ghost;
import ch.uzh.ifi.seal.soprafs16.model.characters.Tuco;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CharacterRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.TurnRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.NormalTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.ReverseTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.SpeedupTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Nico on 13.04.2016.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
@Transactional
public class GameLogicServiceTest {
    //region Repositories
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private WagonRepository wagonRepo;
    @Autowired
    private WagonLevelRepository wagonLevelRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private MarshalRepository marshalRepo;
    @Autowired
    private CharacterRepository characterRepo;
    @Autowired
    private CardRepository cardRepo;
    @Autowired
    private DeckRepository deckRepo;
    @Autowired
    private TurnRepository turnRepo;
    @Autowired
    private GameLogicService gls;
    @Autowired
    private ActionResponseService ars;
    @Autowired
    private GameCacherService  gameCacherService;

    private GameDTO tester;
    private Long gameId;
    @Value("${local.server.port}")
    private int port;

    private URL base;
    private RestTemplate template;
    private static int i = 0;

    @Before
    public void setUp()
            throws Exception {
        this.base = new URL("http://localhost:" + port + "/");
        this.template = new TestRestTemplate();
    }

    @Before
    public void init() {
        //region helper
        i++;
        User user1 = new User();
        user1.setName("name1_glServiceTest" + i);
        user1.setUsername("username1_glServiceTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);
        User user2 = new User();
        user2.setName("name2_glServiceTest" + i);
        user2.setUsername("username2_glServiceTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper2 = template.postForObject(base + "users", user2, UserAuthenticationWrapper.class);
        User user3 = new User();
        user3.setName("name3_glServiceTest" + i);
        user3.setUsername("username3_glServiceTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper3 = template.postForObject(base + "users", user3, UserAuthenticationWrapper.class);
        User user4 = new User();
        user4.setName("name4_glServiceTest" + i);
        user4.setUsername("username4_glServiceTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper4 = template.postForObject(base + "users", user4, UserAuthenticationWrapper.class);

        tester = new GameDTO();
        tester.setName("game1_2_glServiceTest" + i);
        Long gameId1_2 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), tester, Long.class);
        Long userIdGameJoined2 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper2.getUserToken(), null, Long.class);
        Long userIdGameJoined3 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper3.getUserToken(), null, Long.class);
        Long userIdGameJoined4 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper4.getUserToken(), null, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Cheyenne());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);
        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper2.getUserToken());
        HttpEntity<Character> characterRequest2 = new HttpEntity<>(new Ghost());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, characterRequest2, User.class);
        UriComponentsBuilder builder3 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper3.getUserToken());
        HttpEntity<Character> characterRequest3 = new HttpEntity<>(new Doc());
        HttpEntity<User> userResponse3 = template.exchange(builder3.build().encode().toUri(), HttpMethod.PUT, characterRequest3, User.class);
        UriComponentsBuilder builder4 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper4.getUserToken());
        HttpEntity<Character> characterRequest4 = new HttpEntity<>(new Tuco());
        HttpEntity<User> userResponse4 = template.exchange(builder4.build().encode().toUri(), HttpMethod.PUT, characterRequest4, User.class);

        template.postForObject(base + "games/" + gameId1_2 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        GameDTO testerResponse = template.getForObject(base + "games/" + gameId1_2, GameDTO.class);
        gameId = testerResponse.getId();

        tester = gameRepo.findOne(gameId);

        gameRepo.save(tester);
//        gameCacherService.saveGame(tester);

        tester.setCurrentPlayer(0);
        tester.setRoundStarter(0);
        GameDeck<ActionCard> roundCardDeck = (GameDeck<ActionCard>) deckRepo.findOne(tester.getRoundCardDeck().getId());
        Hibernate.initialize(roundCardDeck.getCards());
        roundCardDeck.getCards().clear();

        // Create RoundCards
        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speed = new SpeedupTurn();
            Turn reverse = new ReverseTurn();

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speed);
            turns.add(reverse);

            BrakingCard rc = new BrakingCard();
            rc.setPattern(turns);
            rc.setDeck(roundCardDeck);

            normal.setRoundCard(rc);
            speed.setRoundCard(rc);
            reverse.setRoundCard(rc);

            roundCardDeck.add(rc);
            cardRepo.save(rc);

            turnRepo.save(normal);
            turnRepo.save(speed);
            turnRepo.save(reverse);
        }

        deckRepo.save(roundCardDeck);
        tester = gameRepo.save(tester);

        for(User user: tester.getUsers()){
            // replace ChangeLevelCards because they fuck up the test
            for(int i = 0; i < user.getHandDeck().size(); i++){
                if(user.getHandDeck().get(i) instanceof ChangeLevelCard){
                    CollectCard cc = new CollectCard();
                    cc.setDeck(user.getHandDeck());
                    cc.setPlayedHidden(false);
                    cc.setPlayedByUserId(user.getId());
                    cc = cardRepo.save(cc);

                    PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
                    handDeck.remove(i);
                    handDeck.add(cc);
                    deckRepo.save(handDeck);
                }
            }
            for(int i = 0; i < user.getHiddenDeck().size(); i++){
                if(user.getHiddenDeck().get(i) instanceof ChangeLevelCard){
                    CollectCard cc = new CollectCard();
                    cc.setDeck(user.getHiddenDeck());
                    cc.setPlayedHidden(false);
                    cc.setPlayedByUserId(user.getId());
                    cc = cardRepo.save(cc);

                    PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
                    handDeck.remove(i);
                    handDeck.add(cc);
                    deckRepo.save(handDeck);
                }
            }
        }
    }

    @Test
    public void gls_nextPlayerIsCorrect() {
        int i = 0;
        //for (int i = 0; i < 5; i++) {
            // 4 responses for Normal Turn
            tester = gameRepo.findOne(gameId);
            assertEquals((0 + i) % 4, (long) tester.getCurrentPlayer());

            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((1 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((2 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((3 + i) % 4, (long) tester.getCurrentPlayer());

            // 8 responses for Speed-Up-Turn
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((0 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((0 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((1 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((1 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((2 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((2 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((3 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((3 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            // eighth response triggers turn change
            assertEquals((0 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((3 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((2 + i) % 4, (long) tester.getCurrentPlayer());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals((1 + i) % 4, (long) tester.getCurrentPlayer());
            tester = gameRepo.findOne(gameId);
            simulatePlayCardResponse();

            GameDTO game = gameRepo.findOne(gameId);
            GameDeck<ActionCard> commonDeck = (GameDeck<ActionCard>)deckRepo.findOne(game.getCommonDeck().getId());

            while(!commonDeck.getCards().isEmpty()){
                gls.update(tester.getId());
                commonDeck = (GameDeck<ActionCard>)deckRepo.findOne(commonDeck.getId());
            }

            gls.update(tester.getId());
        //}
    }

    private void simulatePlayCardResponse() {
        User user = userRepo.findOne(tester.getUsers().get(tester.getCurrentPlayer()).getId());
        PlayCardResponseDTO pc = new PlayCardResponseDTO();
        pc.setSpielId(tester.getId());
        pc.setUserId(user.getId());
        pc.setPlayedCardId(user.getHandDeck().get(0).getId());
        ars.processResponse(pc);
        gls.update(tester.getId());
    }

    @Test
    public void gls_TurnIsCorrect() {
        // P1 Request
        int i = 0;
        //for (int i = 0; i < 5; i++) {
            tester = gameRepo.findOne(gameId);
            assertEquals(0, (long) tester.getCurrentTurn());

            // P1 response
            simulatePlayCardResponse();
            // P2 Request
            tester = gameRepo.findOne(gameId);
            assertEquals(0, (long) tester.getCurrentTurn());
            // P2 response
            simulatePlayCardResponse();
            // P3 Request
            tester = gameRepo.findOne(gameId);
            assertEquals(0, (long) tester.getCurrentTurn());
            // P3 response
            simulatePlayCardResponse();
            // P4 Request
            tester = gameRepo.findOne(gameId);
            assertEquals(0, (long) tester.getCurrentTurn());
            // P4 response
            simulatePlayCardResponse();
            // Turn change, P1 request
            tester = gameRepo.findOne(gameId);
            assertEquals(1, (long) tester.getCurrentTurn());

            // 8 responses for Speed-Up-Turn
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(1, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(1, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(1, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(1, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(1, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(1, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(1, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            // eighth response triggers turn change
            assertEquals(2, (long) tester.getCurrentTurn());


            // 4 Responses for Normal-Turn
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(2, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(2, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            tester = gameRepo.findOne(gameId);
            assertEquals(2, (long) tester.getCurrentTurn());
            simulatePlayCardResponse();
            // P4 response
            // Simulate responses
            GameDTO game = gameRepo.findOne(gameId);
            GameDeck<ActionCard> commonDeck = (GameDeck<ActionCard>)deckRepo.findOne(game.getCommonDeck().getId());

            while(!commonDeck.getCards().isEmpty()){
                gls.update(tester.getId());
                commonDeck = (GameDeck<ActionCard>)deckRepo.findOne(commonDeck.getId());
            }
            gls.update(tester.getId());
     //   }
    }

    @Test
    public void gls_PhaseIsCorrect() {
        // simulate 16 ActionResponses (first round)
        for (int i = 0; i < 16; i++) {
            tester = gameRepo.findOne(gameId);
            assertEquals(PhaseType.PLANNING, tester.getCurrentPhase());
            simulatePlayCardResponse();
        }
        tester = gameRepo.findOne(gameId);
        assertEquals(PhaseType.EXECUTION, tester.getCurrentPhase());
        simulateRound(tester);
        assertEquals(PhaseType.PLANNING, tester.getCurrentPhase());
    }

    @Test
    public void gls_RoundIsCorrect() {
        tester = gameRepo.findOne(gameId);
        assertEquals(0, (long) tester.getCurrentRound());

        int y = 0;
        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }

        GameDTO game = gameRepo.findOne(gameId);
        GameDeck<ActionCard> commonDeck = (GameDeck<ActionCard>)deckRepo.findOne(game.getCommonDeck().getId());

        while(!commonDeck.getCards().isEmpty()){
            tester = gameRepo.findOne(gameId);
            assertEquals(y, (long) tester.getCurrentRound());
            gls.update(tester.getId());
            commonDeck = (GameDeck<ActionCard>)deckRepo.findOne(commonDeck.getId());
        }
        gls.update(tester.getId());
        // last update call for ActionCard - Actions
    }

    @Test
    public void execute_AngryMarshalGivesBullet() {
        User u = userRepo.findOne(tester.getUsers().get(0).getId());
        GameDTO game = gameRepo.findOne(gameId);
        GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
        roundCardDeck.getCards().clear();
        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            AngryMarshalCard rc = new AngryMarshalCard();
            rc.setPattern(turns);
            rc.setDeck(roundCardDeck);
            cardRepo.save(rc);

            normal.setRoundCard(rc);
            speedup.setRoundCard(rc);
            reverse.setRoundCard(rc);

            roundCardDeck.add(rc);
            cardRepo.save(rc);
        }


        WagonLevel wl = wagonLevelRepo.findOne(u.getWagonLevel().getId());
        wl.removeUserById(u.getId());
        WagonLevel newWl = wagonLevelRepo.findOne(tester.getMarshal().getWagonLevel().getWagon().getTopLevel().getId());
        u.setWagonLevel(newWl);
        newWl.getUsers().add(u);
        wagonLevelRepo.save(wl);
        wagonLevelRepo.save(newWl);
        userRepo.save(u);
        game = gameRepo.findOne(game.getId());
        gls.update(gameId);

        BulletCard bc = (BulletCard) cardRepo.findOne(game.getNeutralBulletsDeck().get(0).getId());
        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }
        for (int i = 0; i < 16; i++) {
            gls.update(gameId);
        }

        u = userRepo.findOne(u.getId());
        assertTrue(u.getHandDeck().removeById(bc.getId()) || u.getHiddenDeck().removeById(bc.getId()));
    }

    @Test
    public void execute_BrakingCardMovesUsersOnRoof() {
        User u = userRepo.findOne(tester.getUsers().get(0).getId());
        GameDTO game = gameRepo.findOne(gameId);
        GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
        roundCardDeck.getCards().clear();

        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            BrakingCard rc = new BrakingCard();
            rc.setPattern(turns);
            rc.setDeck(roundCardDeck);
            cardRepo. save(rc);

            normal.setRoundCard(rc);
            speedup.setRoundCard(rc);
            reverse.setRoundCard(rc);

            roundCardDeck.add(rc);
            cardRepo.save(rc);
        }

        WagonLevel wl = wagonLevelRepo.findOne(u.getWagonLevel().getId());
        wl.removeUserById(u.getId());
        WagonLevel newWl = wagonLevelRepo.findOne(u.getWagonLevel().getWagon().getTopLevel().getId());
        u.setWagonLevel(newWl);
        newWl.getUsers().add(u);
        wl = wagonLevelRepo.save(wl);
        newWl = wagonLevelRepo.save(newWl);
        userRepo.save(u);
        game = gameRepo.findOne(game.getId());
        gls.update(gameId);

        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }
        for (int i = 0; i < 16; i++) {
            gls.update(gameId);
        }

        u = userRepo.findOne(u.getId());
        if(u.getWagonLevel().getLevelType() == LevelType.TOP){
            assertEquals(newWl.getWagonLevelBefore().getId(), u.getWagonLevel().getId());
        }
    }

    @Test
    public void execute_GetItAllCaseIsPlaced() {
        GameDTO game = gameRepo.findOne(gameId);
        Marshal marshal = marshalRepo.findOne(game.getMarshal().getId());

        GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
        roundCardDeck.getCards().clear();
        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            GetItAllCard giac = new GetItAllCard();
            giac.setPattern(turns);
            giac.setDeck(roundCardDeck);
            cardRepo.save(giac);

            normal.setRoundCard(giac);
            speedup.setRoundCard(giac);
            reverse.setRoundCard(giac);

            roundCardDeck.add(giac);
            cardRepo.save(giac);
        }

        WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());
        wl.setMarshal(null);
        WagonLevel newWl = wagonLevelRepo.findOne(marshal.getWagonLevel().getWagon().getTopLevel().getId());
        marshal.setWagonLevel(newWl);
        newWl.setMarshal(marshal);
        wagonLevelRepo.save(wl);
        newWl = wagonLevelRepo.save(newWl);
        marshalRepo.save(marshal);
        gameRepo.findOne(game.getId());
        gls.update(gameId);

        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }
        for (int i = 0; i < 16; i++) {
            gls.update(gameId);
        }

        newWl = wagonLevelRepo.findOne(newWl.getId());
        assertEquals(ItemType.CASE, newWl.getItems().get(newWl.getItems().size() - 1).getItemType());
    }

    @Test
    public void execute_HostageCardUsersInLocGetBag() {
        GameDTO game = gameRepo.findOne(gameId);

        GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
        roundCardDeck.getCards().clear();
        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            HostageCard hc = new HostageCard();
            hc.setPattern(turns);
            hc.setDeck(roundCardDeck);
            cardRepo.save(hc);

            normal.setRoundCard(hc);
            speedup.setRoundCard(hc);
            reverse.setRoundCard(hc);

            roundCardDeck.add(hc);
            cardRepo.save(hc);
        }

        User u1 = userRepo.findOne(game.getUsers().get(0).getId());
        WagonLevel wl = u1.getWagonLevel();
        WagonLevel wlLocTop = wagonLevelRepo.findOne(game.getWagons().get(0).getTopLevel().getId());
        wl.removeUserById(u1.getId());
        wagonLevelRepo.save(wl);
        wlLocTop.getUsers().add(u1);
        u1.setWagonLevel(wlLocTop);
        wagonLevelRepo.save(wlLocTop);

        User u2 = userRepo.findOne(game.getUsers().get(1).getId());
        WagonLevel wl2 = u2.getWagonLevel();
        WagonLevel wlLocBot = wagonLevelRepo.findOne(game.getWagons().get(0).getBottomLevel().getId());
        wl2.removeUserById(u2.getId());
        wagonLevelRepo.save(wl2);
        wlLocBot.getUsers().add(u2);
        u2.setWagonLevel(wlLocBot);
        wagonLevelRepo.save(wlLocTop);


        gameRepo.findOne(game.getId());
        gls.update(gameId);

        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }
        for (int i = 0; i < 16; i++) {
            gls.update(gameId);
        }

        u1 = userRepo.findOne(u1.getId());
        assertEquals(ItemType.BAG, u1.getItems().get(u1.getItems().size() - 1).getItemType());
        assertEquals(250, u1.getItems().get(u1.getItems().size() - 1).getValue());
        assertEquals(ItemType.BAG, u2.getItems().get(u2.getItems().size() - 1).getItemType());
        assertEquals(250, u2.getItems().get(u2.getItems().size() - 1).getValue());
    }

    @Test
    public void execute_MarshalsRevengeCardUserLosesLeastBag() {
        GameDTO game = gameRepo.findOne(gameId);

        GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
        roundCardDeck.getCards().clear();
        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            MarshallsRevengeCard mrc = new MarshallsRevengeCard();
            mrc.setPattern(turns);
            mrc.setDeck(roundCardDeck);
            cardRepo.save(mrc);

            normal.setRoundCard(mrc);
            speedup.setRoundCard(mrc);
            reverse.setRoundCard(mrc);

            roundCardDeck.add(mrc);
            cardRepo.save(mrc);
        }

        User u = userRepo.findOne(game.getUsers().get(0).getId());
        WagonLevel wl = u.getWagonLevel();
        WagonLevel newWl = wagonLevelRepo.findOne(game.getMarshal().getWagonLevel().getWagon().getTopLevel().getId());
        wl.removeUserById(u.getId());
        wl = wagonLevelRepo.save(wl);
        newWl.getUsers().add(u);
        u.setWagonLevel(newWl);
        newWl = wagonLevelRepo.save(newWl);
        game = gameRepo.findOne(game.getId());
        gls.update(gameId);

        Item item = itemRepo.findOne(getMinPurse(u).getId());

        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }
        for (int i = 0; i < 16; i++) {
            gls.update(gameId);
        }

        u = userRepo.findOne(u.getId());
        assertFalse(u.removeItemById(item.getId()));
    }

   @Test
    public void execute_PassengerRebellionBanditsInsideGetBulletCard() {
        GameDTO game = gameRepo.findOne(gameId);

        GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
        roundCardDeck.getCards().clear();
        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            PassengerRebellionCard mrc = new PassengerRebellionCard();
            mrc.setPattern(turns);
            mrc.setDeck(roundCardDeck);
            cardRepo.save(mrc);

            normal.setRoundCard(mrc);
            speedup.setRoundCard(mrc);
            reverse.setRoundCard(mrc);

            roundCardDeck.add(mrc);
            cardRepo.save(mrc);
        }


        // On initialize, every user is inside a wagon
        BulletCard bc0 = (BulletCard) cardRepo.findOne(game.getNeutralBulletsDeck().get(0).getId());
        BulletCard bc1 = (BulletCard) cardRepo.findOne(game.getNeutralBulletsDeck().get(1).getId());
        BulletCard bc2 = (BulletCard) cardRepo.findOne(game.getNeutralBulletsDeck().get(2).getId());
        BulletCard bc3 = (BulletCard) cardRepo.findOne(game.getNeutralBulletsDeck().get(3).getId());

        BulletCard[] bcs = {bc0, bc1, bc2, bc3};
        gls.update(gameId);

        game = gameRepo.findOne(gameId);
        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }
        for (int i = 0; i < 15; i++) {
            gls.update(gameId);
        }

        int i = 0;
        for (User u : game.getUsers()) {
            u = userRepo.findOne(u.getId());
            assertTrue(u.getHiddenDeck().removeById(bcs[i].getId()) || u.getHandDeck().removeById(bcs[i].getId()) || u.getWagonLevel().getLevelType() == LevelType.TOP);
            if(u.getWagonLevel().getLevelType() == LevelType.BOTTOM){
                i++;
            }
        }
    }

    @Test
    public void execute_PickPocketingGrantsLoneBanditPurse() {
        GameDTO game = gameRepo.findOne(gameId);

        GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
        roundCardDeck.getCards().clear();
        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            PickPocketingCard mrc = new PickPocketingCard();
            mrc.setPattern(turns);
            mrc.setDeck(roundCardDeck);
            cardRepo.save(mrc);

            normal.setRoundCard(mrc);
            speedup.setRoundCard(mrc);
            reverse.setRoundCard(mrc);

            roundCardDeck.add(mrc);
            cardRepo.save(mrc);
        }

        User u = userRepo.findOne(game.getUsers().get(0).getId());
        WagonLevel wl = u.getWagonLevel();
        WagonLevel newWl = wagonLevelRepo.findOne(game.getWagons().get(2).getBottomLevel().getId());

        int itemCounter = u.getItems().size();

        wl.removeUserById(u.getId());
        wl = wagonLevelRepo.save(wl);
        newWl.getUsers().add(u);
        u.setWagonLevel(newWl);
        newWl = wagonLevelRepo.save(newWl);

        boolean wagonContainedBag = containsBag(newWl.getItems());
        game = gameRepo.findOne(game.getId());
        gls.update(gameId);

        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }
        for (int i = 0; i < 16; i++) {
            gls.update(gameId);
        }

        u = userRepo.findOne(u.getId());
        if (wagonContainedBag) {
            assertEquals(ItemType.BAG, u.getItems().get(u.getItems().size() - 1).getItemType());
        } else {
            assertTrue(u.getItems().size() == itemCounter);
            Item bag = new Item();
            bag.setItemType(ItemType.BAG);
            bag.setUser(null);
            bag.setWagonLevel(newWl);
            bag.setValue(250);
            newWl.getItems().add(bag);

            itemRepo.save(bag);
            wagonLevelRepo.save(newWl);
            execute_PickPocketingGrantsLoneBanditPurse();
        }
    }

    @Test
    public void execute_PivotablePoleMovesBanditsOnTopToCaboose() {
        GameDTO game = gameRepo.findOne(gameId);

        GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
        roundCardDeck.getCards().clear();
        for (int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            PivotablePoleCard ppc = new PivotablePoleCard();
            ppc.setPattern(turns);
            ppc.setDeck(roundCardDeck);
            cardRepo.save(ppc);

            normal.setRoundCard(ppc);
            speedup.setRoundCard(ppc);
            reverse.setRoundCard(ppc);

            roundCardDeck.add(ppc);
            cardRepo.save(ppc);
        }

        User u = userRepo.findOne(game.getUsers().get(0).getId());
        WagonLevel wl = u.getWagonLevel();
        WagonLevel newWl = wagonLevelRepo.findOne(game.getMarshal().getWagonLevel().getWagon().getTopLevel().getId());
        wl.removeUserById(u.getId());
        wl = wagonLevelRepo.save(wl);
        newWl.getUsers().add(u);
        u.setWagonLevel(newWl);
        newWl = wagonLevelRepo.save(newWl);
        game = gameRepo.findOne(game.getId());
        gls.update(gameId);

        Item item = itemRepo.findOne(getMinPurse(u).getId());

        for (int i = 0; i < 16; i++) {
            simulatePlayCardResponse();
        }
        for (int i = 0; i < 16; i++) {
            gls.update(gameId);
        }

        u = userRepo.findOne(u.getId());
        game = gameRepo.findOne(gameId);
        assertTrue(game.getWagons().get(game.getWagons().size() - 1).getTopLevel().removeUserById(u.getId()));
    }

    @Test
    public void checkMarshal_movesPlayerOnTop() {
        GameDTO game = gameRepo.findOne(gameId);
        User u = userRepo.findOne(game.getUsers().get(0).getId());

        WagonLevel wl = u.getWagonLevel();
        wl.getUsers().remove(u);
        wagonLevelRepo.save(wl);

        WagonLevel marshalWl = game.getMarshal().getWagonLevel();
        marshalWl.getUsers().add(u);
        u.setWagonLevel(marshalWl);

        userRepo.save(u);
        wagonLevelRepo.save(marshalWl);

        int hiddenDeckCounter = u.getHiddenDeck().size();

        ars.checkMarshal(gameRepo.findOne(game.getId()));

        u = userRepo.findOne(u.getId());
        marshalWl = wagonLevelRepo.findOne(marshalWl.getId());
        wl = wagonLevelRepo.findOne(wl.getId());

        assertEquals(hiddenDeckCounter + 1, u.getHiddenDeck().size());
        assertEquals(marshalWl.getWagon().getTopLevel().getId(), u.getWagonLevel().getId());
        assertTrue(marshalWl.getUsers().isEmpty());
        assertFalse(wl.removeUserById(u.getId()));
    }

    @Test
    public void changeLevel_works() {
        GameDTO game = gameRepo.findOne(gameId);
        User u = userRepo.findOne(game.getUsers().get(0).getId());

        Long wlId = u.getWagonLevel().getId();
        Long wlChanged = u.getWagonLevel().getWagon().getTopLevel().getId();

        ars.changeLevel(u);

        u = userRepo.findOne(u.getId());
        assertEquals(wlChanged, u.getWagonLevel().getId());

        ars.changeLevel(u);

        u = userRepo.findOne(u.getId());
        assertEquals(wlId, u.getWagonLevel().getId());
    }

    @Test
    public void evaluateGunslingerBonus_assignsIfOnlyOneMax(){
        GameDTO game = gameRepo.findOne(gameId);
        game.setCurrentRound(4);
        game.setCurrentPhase(PhaseType.EXECUTION);

        User gunslinger = userRepo.findOne(game.getUsers().get(0).getId());
        gunslinger.getBulletsDeck().remove(0);
        userRepo.save(gunslinger);

        int items = gunslinger.getItems().size();
        gls.update(gameId);

        gunslinger = userRepo.findOne(gunslinger.getId());
        assertEquals(items + 1, gunslinger.getItems().size());
        assertEquals(1000, gunslinger.getItems().get(gunslinger.getItems().size() - 1).getValue());
    }

    @Test
    public void evaluateGunslingerBonus_doesNotAssignIfNotMax(){
        GameDTO game = gameRepo.findOne(gameId);
        game.setCurrentRound(4);
        game.setCurrentPhase(PhaseType.EXECUTION);

        User gunslinger = userRepo.findOne(game.getUsers().get(0).getId());
        gunslinger.getBulletsDeck().remove(0);
        userRepo.save(gunslinger);

        User notGunslinger = userRepo.findOne(game.getUsers().get(1).getId());
        int items = notGunslinger.getItems().size();

        gls.update(gameId);
        notGunslinger = userRepo.findOne(notGunslinger.getId());

        assertEquals(items, notGunslinger.getItems().size());
    }

    @Test
    public void evaluateGunslingerBonus_doesAssignMultipleBonuses(){
        GameDTO game = gameRepo.findOne(gameId);
        game.setCurrentRound(4);
        game.setCurrentPhase(PhaseType.EXECUTION);

        User gunslinger1 = userRepo.findOne(game.getUsers().get(0).getId());
        gunslinger1.getBulletsDeck().remove(0);
        userRepo.save(gunslinger1);
        int items1 = gunslinger1.getItems().size();

        User gunslinger2 = userRepo.findOne(game.getUsers().get(0).getId());
        gunslinger2.getBulletsDeck().remove(0);
        userRepo.save(gunslinger2);
        int items2 = gunslinger1.getItems().size();

        gls.update(gameId);

        gunslinger1 = userRepo.findOne(gunslinger1.getId());
        gunslinger2 = userRepo.findOne(gunslinger1.getId());

        assertEquals(items1 + 1, gunslinger1.getItems().size());
        assertEquals(items2 + 1, gunslinger2.getItems().size());
    }

    /*
    * HELPER FUNCTIONS
     */

    private boolean containsBag(List<Item> items) {
        for (Item item : items) {
            if (item.getItemType().equals(ItemType.BAG)) return true;
        }
        return false;
    }

    private void simulateRound(GameDTO game) {
        for (int i = 0; i < 4; i++) {
            simulateTurn(game);
        }
    }

    private void simulateTurn(GameDTO game) {
        for (int i = 0; i < game.getUsers().size(); i++) {
            gls.update(game.getId());
        }
    }

    private Item getMinPurse(User user) {
        Item min = new Item();
        min.setValue(Integer.MAX_VALUE);
        for (Item item : user.getItems()) {
            if (item.getItemType() == ItemType.BAG && item.getValue() < min.getValue()) {
                min = item;
            }
        }
        if (min.getValue() < Integer.MAX_VALUE) {
            return min;
        }
        return null;
    }

}