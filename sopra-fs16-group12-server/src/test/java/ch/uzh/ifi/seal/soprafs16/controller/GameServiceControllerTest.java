package ch.uzh.ifi.seal.soprafs16.controller;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.UserAuthenticationWrapper;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.CollectItemResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.DrawCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.MoveMarshalResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.MoveResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.PunchResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.ShootResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.StationCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Character;
import ch.uzh.ifi.seal.soprafs16.model.characters.Cheyenne;
import ch.uzh.ifi.seal.soprafs16.model.characters.Doc;
import ch.uzh.ifi.seal.soprafs16.model.characters.Ghost;
import ch.uzh.ifi.seal.soprafs16.model.characters.Tuco;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})


/**
 * Created by Christoph on 02.04.2016.
 */
public class GameServiceControllerTest {
    @Value("${local.server.port}")
    private int port;

    private URL base;
    private RestTemplate template;

    @Before
    public void setUp()
            throws Exception {
        this.base = new URL("http://localhost:" + port + "/");
        this.template = new TestRestTemplate();
    }

    @Test
    public void testLobbyLogic() throws Exception {
        //region create User - UserServiceController.login()
        User user1 = new User();
        user1.setName("name1_lobbyTest");
        user1.setUsername("username1_lobbyTest");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);
        Assert.assertNotNull(userAuthenticationWrapper1);
        Assert.assertNotNull(userAuthenticationWrapper1.getUserId());
        Assert.assertNotNull(userAuthenticationWrapper1.getUserToken());
        //endregion
        //region create GameDTO - GameServiceController.addGame()
        GameDTO game1 = new GameDTO();
        game1.setName("game1_lobbyTest");
        Long gameId1 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1, Long.class);
        Assert.assertNotNull(gameId1);
        //endregion
        //region all pending Games - GameServiceController.listGames() with filter
        String filter = "status=PENDING";
        List<GameDTO> list = template.getForObject(base + "games?" + filter, List.class);
        //endregion
        //region User joins GameDTO  - GameServiceController.addPlayer
        User user2 = new User();
        user2.setName("name2_lobbyTest");
        user2.setUsername("username2_lobbyTest");
        UserAuthenticationWrapper userAuthenticationWrapper2 = template.postForObject(base + "users", user2, UserAuthenticationWrapper.class);
        Long userIdGameJoined = template.postForObject(base + "games/" + gameId1 + "/users?token=" + userAuthenticationWrapper2.getUserToken(), null, Long.class);
        Assert.assertNotNull(userIdGameJoined);
        //endregion
        //region get UserId of User - UserServiceController.getUserId
        User user6 = new User();
        user6.setName("name6_lobbyTest");
        user6.setUsername("username6_lobbyTest");
        UserAuthenticationWrapper userAuthenticationWrapper6 = template.postForObject(base + "users", user6, UserAuthenticationWrapper.class);
        Long userId = template.getForObject(base + "users?token=" + userAuthenticationWrapper6.getUserToken(), Long.class);
        Assert.assertNotNull(userId);
        //endregion
        //region modify character of User - GameServiceController.modifyUserCharacter
        //region helper
        User user7 = new User();
        user7.setName("name7_lobbyTest");
        user7.setUsername("username7_lobbyTest");
        UserAuthenticationWrapper userAuthenticationWrapper7 = template.postForObject(base + "users", user7, UserAuthenticationWrapper.class);
        GameDTO game7 = new GameDTO();
        game7.setName("game7_lobbyTest");
        Long gameId7 = template.postForObject(base + "games?token=" + userAuthenticationWrapper7.getUserToken(), game7, Long.class);
        //endregion
        UriComponentsBuilder builder7 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", userAuthenticationWrapper7.getUserToken());
        HttpEntity<Character> characterRequest7 = new HttpEntity<>(new Cheyenne());
        HttpEntity<User> userResponse7 = template.exchange(builder7.build().encode().toUri(), HttpMethod.PUT, characterRequest7, User.class);
        Assert.assertEquals(Cheyenne.class, userResponse7.getBody().getCharacter().getClass());
        //endregion

    }

    @Test
    public void testResponseSpeed() {
        User user1 = new User();
        user1.setName("name1_speedTest");
        user1.setUsername("username1_speedTest");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);
        User user2 = new User();
        user2.setName("name2_speedTest");
        user2.setUsername("username2_speedTest");
        UserAuthenticationWrapper userAuthenticationWrapper2 = template.postForObject(base + "users", user2, UserAuthenticationWrapper.class);

        GameDTO game1_2 = new GameDTO();
        game1_2.setName("game1_2_speedTest");
        Long gameId1_2 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1_2, Long.class);

        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper2.getUserToken(), null, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Cheyenne());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper2.getUserToken());
        HttpEntity<Character> characterRequest2 = new HttpEntity<>(new Doc());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, characterRequest2, User.class);
        //endregion
        template.postForObject(base + "games/" + gameId1_2 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);


        for (int i = 0; i < 10; i++) {
            long startFull = System.currentTimeMillis();

            GameDTO game1_2Response2 = template.getForObject(base + "games/" + gameId1_2, GameDTO.class);

            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("@@@@@@ TOTAL GET GAME: " + (System.currentTimeMillis() - startFull) + "ms");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        }
    }

    @Test
    public void testStartStopGame() {
        //region helper
        User user1 = new User();
        user1.setName("name1_startGameTest");
        user1.setUsername("username1_startGameTest");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);
        User user2 = new User();
        user2.setName("name2_startGameTest");
        user2.setUsername("username2_startGameTest");
        UserAuthenticationWrapper userAuthenticationWrapper2 = template.postForObject(base + "users", user2, UserAuthenticationWrapper.class);

        GameDTO game1_2 = new GameDTO();
        game1_2.setName("game1_2_startGameTest");
        Long gameId1_2 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1_2, Long.class);
        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper2.getUserToken(), null, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Cheyenne());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper2.getUserToken());
        HttpEntity<Character> characterRequest2 = new HttpEntity<>(new Doc());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, characterRequest2, User.class);
        //endregion
        template.postForObject(base + "games/" + gameId1_2 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        GameDTO game1_2Response = template.getForObject(base + "games/" + gameId1_2, GameDTO.class);
        // Object returnObject = template.getForObject(base + "games/" + gameId1_2, GameDTO.class);
        //region Assertions
        //is the the current player (startplayer) placed in the last wagon
        WagonLevel lastWagonLevel = game1_2Response.getWagons().get((game1_2Response.getWagons().size() - 1)).getBottomLevel();
        //Assert.assertTrue(containsUserId(lastWagonLevel.getUsers(), game1_2Response.getUsers().get(game1_2Response.getCurrentPlayerIndex()).getId()));
        for (User u : game1_2Response.getUsers()) {
            //does every Player have a 250$ bag
            Assert.assertEquals(250, u.getItems().get(0).getValue());
            //does the bulletsdeck contain 6 bullets
            Assert.assertEquals(6, u.getBulletsDeck().getCards().size());
            //does the handdeck contain 6 cards (or 7 if Doc)
            int drawCardsAmount = u.getCharacter().getClass().equals(Doc.class) ? 7 : 6;
            Assert.assertEquals(drawCardsAmount, u.getHandDeck().getCards().size());
            //does the hiddendeck contains 4 cards (3 for doc)
            int hiddenCardsAmount = u.getCharacter().getClass().equals(Doc.class) ? 3 : 4;
            Assert.assertEquals(hiddenCardsAmount, u.getHiddenDeck().getCards().size());
        }
        //does the roundcarddeck contain 5 cards
        Assert.assertEquals(5, game1_2Response.getRoundCardDeck().getCards().size());
        //is the last roundcard a stationcard? TODO

        //does the neutralbulletdeck contain 13 cards (marshalbullets)
        //Assert.assertEquals(13, game1_2Response.getNeutralBulletsDeck().getCards().size());
        //does the commondeck exist?
        Assert.assertNotNull(game1_2Response.getCommonDeck());
        //assert gamevariables
        Assert.assertEquals((Integer) 0, game1_2Response.getCurrentRound());
        Assert.assertEquals((Integer) 0, game1_2Response.getCurrentTurn());
        Assert.assertEquals(PhaseType.PLANNING, game1_2Response.getCurrentPhase());
        //endregion

        //region test stop game
        template.postForObject(base + "games/" + gameId1_2 + "/stop?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        GameDTO game1_2ResponseStopped = template.getForObject(base + "games/" + gameId1_2, GameDTO.class);
        Assert.assertEquals(GameStatus.FINISHED, game1_2ResponseStopped.getStatus());
        //endregion
    }

    @Test
    public void testDeleteFunctions() {
        //region set up
        User user1 = new User();
        user1.setName("name1_deleteTest");
        user1.setUsername("username1_deleteTest");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);
        User user2 = new User();
        user2.setName("name2_deleteTest");
        user2.setUsername("username2_deleteTest");
        UserAuthenticationWrapper userAuthenticationWrapper2 = template.postForObject(base + "users", user2, UserAuthenticationWrapper.class);
        User user3 = new User();
        user3.setName("name3_deleteTest");
        user3.setUsername("username3_deleteTest");
        UserAuthenticationWrapper userAuthenticationWrapper3 = template.postForObject(base + "users", user3, UserAuthenticationWrapper.class);

        GameDTO game = new GameDTO();
        game.setName("gameDeleteTest");
        Long gameId = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game, Long.class);
        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId + "/users?token=" + userAuthenticationWrapper2.getUserToken(), null, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Cheyenne());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);
        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId + "/users")
                .queryParam("token", userAuthenticationWrapper2.getUserToken());
        HttpEntity<Character> characterRequest2 = new HttpEntity<>(new Doc());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, characterRequest2, User.class);
        UriComponentsBuilder builder3 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId + "/users")
                .queryParam("token", userAuthenticationWrapper3.getUserToken());
        HttpEntity<Character> characterRequest3 = new HttpEntity<>(new Tuco());
        HttpEntity<User> userResponse3 = template.exchange(builder3.build().encode().toUri(), HttpMethod.PUT, characterRequest3, User.class);

        template.postForObject(base + "games/" + gameId + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        //endregion
        //region remove a user that is not the owner
        HttpEntity<User> httpEntityUser3 = new HttpEntity<User>(user3);
        ResponseEntity<Long> httpResponse3 = template.exchange(base + "games/" + gameId + "/users?token=" + userAuthenticationWrapper3.getUserToken(), HttpMethod.DELETE, httpEntityUser3, Long.class);
        GameDTO gameRemoveNotOwner = template.getForObject(base + "games/" + gameId, GameDTO.class);
        Assert.assertTrue(!containsUserName(gameRemoveNotOwner.getUsers(), user3.getUsername()));
        Assert.assertTrue(containsUserName(gameRemoveNotOwner.getUsers(), user1.getUsername()));
        Assert.assertTrue(containsUserName(gameRemoveNotOwner.getUsers(), user2.getUsername()));
        //endregion
        //region remove a user that is the owner. check if owner is correctly "handed over"
        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
        ResponseEntity<Long> httpResponse1 = template.exchange(base + "games/" + gameId + "/users?token=" + userAuthenticationWrapper1.getUserToken(), HttpMethod.DELETE, httpEntityUser1, Long.class);
        GameDTO gameRemoveOwner = template.getForObject(base + "games/" + gameId, GameDTO.class);
        Assert.assertTrue(gameRemoveOwner.getOwner().equals(user2.getName()));
        //endregion
        //region remove the last user and trigger game deletion
        //List<GameDTO> allGamesBefore = template.getForObject(base + "games", List.class);
        List<String> allGamesBeforeSerialized = template.getForObject(base + "games", List.class); //TODO deserialize into List<GameDTO>, check code on client
        List<LinkedHashMap> allGamesBefore = template.getForObject(base + "games", List.class);
        // Assert.assertTrue(containsGameId(allGamesBefore, gameId));
        HttpEntity<User> httpEntityUser2 = new HttpEntity<User>(user2);

        ResponseEntity<Long> httpResponse2 = template.exchange(base + "games/" + gameId + "/users?token=" + userAuthenticationWrapper2.getUserToken(), HttpMethod.DELETE, httpEntityUser2, Long.class);
//        List<LinkedHashMap> allGamesAfter = template.getForObject(base + "games", List.class);
//        Assert.assertTrue(!containsGameId(allGamesAfter, gameId));

        // Assert.assertEquals(gameId, httpResponse2.getBody());
        //endregion
    }

    @Test
    public void testStartDemoGame() {
        //region helper
        User user1 = new User();
        user1.setName("name1_startDemoGameTest");
        user1.setUsername("username1_startDemoGameTest");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);
        User user2 = new User();
        user2.setName("name2_startDemoGameTest");
        user2.setUsername("username2_startDemoGameTest");
        UserAuthenticationWrapper userAuthenticationWrapper2 = template.postForObject(base + "users", user2, UserAuthenticationWrapper.class);

        GameDTO game1_2 = new GameDTO();
        game1_2.setName("game1_2_startDemoGameTest");
        Long gameId1_2 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1_2, Long.class);
        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper2.getUserToken(), null, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Cheyenne());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);
        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper2.getUserToken());
        HttpEntity<Character> characterRequest2 = new HttpEntity<>(new Doc());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, characterRequest2, User.class);
        //endregion
        template.postForObject(base + "games/" + gameId1_2 + "/startDemo?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        GameDTO game1_2Response = template.getForObject(base + "games/" + gameId1_2, GameDTO.class);
        //region Assertions
        Assert.assertEquals(StationCard.class, game1_2Response.getRoundCardDeck().getCards().get(game1_2Response.getCurrentRound()).getClass().getSuperclass());

        Assert.assertNotNull(game1_2Response.getWagons().get(1).getBottomLevel().getMarshal());
        Assert.assertEquals(user1.getUsername(), game1_2Response.getWagons().get(0).getBottomLevel().getUsers().get(0).getUsername());
        Assert.assertEquals(user2.getUsername(), game1_2Response.getWagons().get(3).getTopLevel().getUsers().get(0).getUsername());
        //endregion
    }

    @Test
    public void processResponse_CollectItemResponseIsAdded() {
        User user1 = new User();
        user1.setName("name1_collectItemResponseTest");
        user1.setUsername("username1_collectItemResponseTest");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);


        GameDTO game1 = new GameDTO();
        game1.setName("game1_collectItemResponseTest");
        Long gameId1 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Cheyenne());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        template.postForObject(base + "games/" + gameId1 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);

        CollectItemResponseDTO collectItemResponseDTO = new CollectItemResponseDTO();
        collectItemResponseDTO.setCollectedItemType(ItemType.BAG);
        collectItemResponseDTO.setUserId(userAuthenticationWrapper1.getUserId());
        collectItemResponseDTO.setSpielId(gameId1);

        Long gameId_ActionResponse = template.postForObject(base + "games/" + gameId1 + "/actions?token=" + userAuthenticationWrapper1.getUserToken(), collectItemResponseDTO, Long.class);

        Assert.assertEquals(gameId1, gameId_ActionResponse);
    }


    @Test
    public void processResponse_DrawCardResponseIsAdded() {
        User user1 = new User();
        user1.setName("name1_drawCardResponseIsAdded");
        user1.setUsername("username1_drawCardResponseIsAdded");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);

        GameDTO game1 = new GameDTO();
        game1.setName("game1_drawCardResponseTest");
        Long gameId1 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Ghost());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        template.postForObject(base + "games/" + gameId1 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        game1 = template.getForObject(base + "games/" + gameId1, GameDTO.class);
        DrawCardResponseDTO drawCardResponseDTO = new DrawCardResponseDTO();
        drawCardResponseDTO.setUserId(userAuthenticationWrapper1.getUserId());
        drawCardResponseDTO.setSpielId(gameId1);

        Long gameId_ActionResponse = template.postForObject(base + "games/" + gameId1 + "/actions?token=" + userAuthenticationWrapper1.getUserToken(), drawCardResponseDTO, Long.class);

        Assert.assertEquals(gameId1, gameId_ActionResponse);
    }


    @Test
    public void processResponse_MoveMarshalResponseIsAdded() {
        User user1 = new User();
        user1.setName("name1_moveMarshal");
        user1.setUsername("username1_moveMarshal");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);

        GameDTO game1 = new GameDTO();
        game1.setName("game1_moveMarshalResponseTest");
        Long gameId1 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1, Long.class);


        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Ghost());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        template.postForObject(base + "games/" + gameId1 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        game1 = template.getForObject(base + "games/" + gameId1, GameDTO.class);
        MoveMarshalResponseDTO moveMarshallResponseDTO = new MoveMarshalResponseDTO();
        moveMarshallResponseDTO.setUserId(userAuthenticationWrapper1.getUserId());
        moveMarshallResponseDTO.setSpielId(gameId1);
        moveMarshallResponseDTO.setWagonLevelId(game1.getWagons().get(0).getTopLevel().getId());

        Long gameId_ActionResponse = template.postForObject(base + "games/" + gameId1 + "/actions?token=" + userAuthenticationWrapper1.getUserToken(), moveMarshallResponseDTO, Long.class);

        Assert.assertEquals(gameId1, gameId_ActionResponse);
    }

    @Test
    public void processResponse_PlayCardResponseIsAdded() {
        User user1 = new User();
        user1.setName("name1_playCardResponseIsAdded");
        user1.setUsername("username1_playCardResponseIsAdded");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);

        GameDTO game1 = new GameDTO();
        game1.setName("game1_playCardResponseTest");
        Long gameId1 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1, Long.class);


        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Ghost());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        template.postForObject(base + "games/" + gameId1 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        game1 = template.getForObject(base + "games/" + gameId1, GameDTO.class);
        PlayCardResponseDTO playCardlResponseDTO = new PlayCardResponseDTO();
        playCardlResponseDTO.setUserId(userAuthenticationWrapper1.getUserId());
        playCardlResponseDTO.setSpielId(gameId1);
        playCardlResponseDTO.setPlayedCardId(game1.getUsers().get(0).getHandDeck().get(1).getId());

        Long gameId_ActionResponse = template.postForObject(base + "games/" + gameId1 + "/actions?token=" + userAuthenticationWrapper1.getUserToken(), playCardlResponseDTO, Long.class);

        Assert.assertEquals(gameId1, gameId_ActionResponse);
    }

    @Test
    public void processResponse_MoveResponseIsAdded() {
        User user1 = new User();
        user1.setName("name1_moveResponseIsAdded");
        user1.setUsername("username1_moveResponseIsAdded");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);

        GameDTO game1 = new GameDTO();
        game1.setName("game1_moveResponseTest");
        Long gameId1 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Cheyenne());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        template.postForObject(base + "games/" + gameId1 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        game1 = template.getForObject(base + "games/" + gameId1, GameDTO.class);

        MoveResponseDTO movelResponseDTO = new MoveResponseDTO();
        movelResponseDTO.setUserId(userAuthenticationWrapper1.getUserId());
        movelResponseDTO.setSpielId(gameId1);
        movelResponseDTO.setWagonLevelId(game1.getWagons().get(1).getTopLevel().getId());

        Long gameId_ActionResponse = template.postForObject(base + "games/" + gameId1 + "/actions?token=" + userAuthenticationWrapper1.getUserToken(), movelResponseDTO, Long.class);

        Assert.assertEquals(gameId1, gameId_ActionResponse);
    }


    @Test
    public void processResponse_PunchResponseIsAdded() {
        User user1 = new User();
        user1.setName("name1_punchCardResponseIsAdded");
        user1.setUsername("username1_punchCardResponseIsAdded");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);

        GameDTO game1 = new GameDTO();
        game1.setName("game1_punchCardResponseTest");
        Long gameId1 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1, Long.class);


        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Ghost());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        template.postForObject(base + "games/" + gameId1 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        game1 = template.getForObject(base + "games/" + gameId1, GameDTO.class);
        PunchResponseDTO punchlResponseDTO = new PunchResponseDTO();
        punchlResponseDTO.setUserId(userAuthenticationWrapper1.getUserId());
        punchlResponseDTO.setSpielId(gameId1);
        punchlResponseDTO.setVictimId(userAuthenticationWrapper1.getUserId());
        punchlResponseDTO.setWagonLevelId(game1.getWagons().get(1).getTopLevel().getId());
        punchlResponseDTO.setItemType(ItemType.BAG);

        Long gameId_ActionResponse = template.postForObject(base + "games/" + gameId1 + "/actions?token=" + userAuthenticationWrapper1.getUserToken(), punchlResponseDTO, Long.class);

        Assert.assertEquals(gameId1, gameId_ActionResponse);
    }

    @Test
    public void processResponse_ShootResponseIsAdded() {
        User user1 = new User();
        user1.setName("name1_shootCardResponseIsAdded");
        user1.setUsername("username1_shootCardResponseIsAdded");
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);

        GameDTO game1 = new GameDTO();
        game1.setName("game1_shootResponseTest");
        Long gameId1 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), game1, Long.class);


        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Ghost());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        template.postForObject(base + "games/" + gameId1 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        game1 = template.getForObject(base + "games/" + gameId1, GameDTO.class);
        ShootResponseDTO shootlResponseDTO = new ShootResponseDTO();
        shootlResponseDTO.setUserId(userAuthenticationWrapper1.getUserId());
        shootlResponseDTO.setSpielId(gameId1);
        shootlResponseDTO.setVictimId(userAuthenticationWrapper1.getUserId());

        Long gameId_ActionResponse = template.postForObject(base + "games/" + gameId1 + "/actions?token=" + userAuthenticationWrapper1.getUserToken(), shootlResponseDTO, Long.class);

        Assert.assertEquals(gameId1, gameId_ActionResponse);
    }

    //region helper methods
    private static boolean containsUserName(List<User> list, String username) {
        for (User user : list) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsUserId(List<User> list, Long userId) {
        for (User user : list) {
            if (user.getId() == userId) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsGameId(List<LinkedHashMap> list, Long gameId) {
        for (LinkedHashMap game : list) {
            String idValue = game.get("id").toString();
            if (idValue.equals(gameId.toString())) {
                return true;
            }
        }
        return false;
    }
    //endregion
}