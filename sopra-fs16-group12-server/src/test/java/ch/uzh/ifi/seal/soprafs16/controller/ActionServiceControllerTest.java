package ch.uzh.ifi.seal.soprafs16.controller;


import ch.uzh.ifi.seal.soprafs16.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.ArrayList;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.request.CollectItemRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.request.MoveMarshalRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.request.MoveRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.request.PunchRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.request.ShootRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.CollectCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.MarshalCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.MoveCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.PunchCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ShootCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Belle;
import ch.uzh.ifi.seal.soprafs16.model.characters.Cheyenne;
import ch.uzh.ifi.seal.soprafs16.model.characters.Django;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
@Transactional
/**
 * Created by Timon Willi on 22.04.2016.
 */
public class ActionServiceControllerTest {
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
        i++;

        User user1 = new User();
        user1.setName("user1");
        user1.setUsername("username1_" + i);
        user1.setToken("t1_" + i);
        user1.setStatus(UserStatus.ONLINE);
        user1.setItems(new ArrayList<Item>());
        userRepo.save(user1);
//        String token1 = template.postForObject(base + "users", user1, String.class);
//
        GameDTO game1 = new GameDTO();
        game1.setName("game1");
        game1.setOwner("user1");
        game1.setUsers(new ArrayList<User>());
        game1.setActions(new ArrayList<ActionRequestDTO>());
        game1.setWagons(new ArrayList<Wagon>());
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);
        gameId = game1.getId();
//        Long gameId1 = template.postForObject(base + "games?token=" + token1, game1, Long.class);
//        template.postForObject(base + "games/" + gameId1 + "/start?token=" + token1, null, Void.class);
//        GameDTO testerResponse = template.getForObject(base + "games/" + gameId1, GameDTO.class);
//        gameId = testerResponse.getId();
//region useless shit
//        User someUser = new User();
//        someUser.setName("dude");
//        someUser.setUsername("usern");
//        someUser.setToken("someToken");
//        someUser.setStatus(UserStatus.OFFLINE);
//        someUser.setItems(new ArrayList<Item>());
//        userRepo.save(someUser);
//        someUser.setGame(testerResponse);
//        testerResponse.getUsers().add(someUser);

//            game1.setUsers(new ArrayList<User>());
//            game1.setWagons(new ArrayList<Wagon>());
//            //user1.setToken(token1);
//            user1 = userRepo.findByToken(token1);
//            user1.setStatus(UserStatus.ONLINE);

        user1.setGame(game1);
        game1.getUsers().add(user1);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);


        User user2 = new User();
        user2.setName("user2");
        user2.setUsername("username2_" + i);
        user2.setToken("t2" + i);
        user2.setStatus(UserStatus.ONLINE);
        user2.setItems(new ArrayList<Item>());
        userRepo.save(user2);
        user2.setGame(game1);
        game1.getUsers().add(user2);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);


        User user3 = new User();
        user3.setName("user3");
        user3.setUsername("username3_" + i);
        user3.setToken("t3" + i);
        user3.setStatus(UserStatus.ONLINE);
        user3.setItems(new ArrayList<Item>());
        userRepo.save(user3);
        user3.setGame(game1);
        game1.getUsers().add(user3);
        user3.setCharacter(new Django());


        User user4 = new User();
        user4.setName("user4");
        user4.setUsername("username4_" + i);
        user4.setToken("t4" + i);
        user4.setStatus(UserStatus.ONLINE);
        user4.setItems(new ArrayList<Item>());
        user4.setCharacter(new Django());
        userRepo.save(user4);
        user4.setGame(game1);
        game1.getUsers().add(user4);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);

        User user5 = new User();
        user5.setName("user5");
        user5.setUsername("username5_" + i);
        user5.setToken("t5" + i);
        user5.setStatus(UserStatus.ONLINE);
        user5.setItems(new ArrayList<Item>());
        userRepo.save(user5);
        user5.setGame(game1);
        game1.getUsers().add(user5);
        userRepo.save(user5);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);

        User user6 = new User();
        user6.setName("user6");
        user6.setUsername("username6_" + i);
        user6.setToken("t6" + i);
        user6.setStatus(UserStatus.ONLINE);
        user6.setItems(new ArrayList<Item>());
        user6.setCharacter(new Cheyenne());
        userRepo.save(user6);
        user6.setGame(game1);
        game1.getUsers().add(user6);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);

        User user7 = new User();
        user7.setName("user7");
        user7.setUsername("username7_" + i);
        user7.setToken("t7" + i);
        user7.setStatus(UserStatus.ONLINE);
        user7.setItems(new ArrayList<Item>());
        user7.setCharacter(new Belle());
        userRepo.save(user7);
        user7.setGame(game1);
        game1.getUsers().add(user7);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);


        User user8 = new User();
        user8.setName("user8");
        user8.setUsername("username8_" + i);
        user8.setToken("t8" + i);
        user8.setStatus(UserStatus.ONLINE);
        user8.setCharacter(new Cheyenne());
        user8.setItems(new ArrayList<Item>());
        userRepo.save(user8);
        user8.setGame(game1);
        game1.getUsers().add(user8);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);


        User user9 = new User();
        user9.setName("user9");
        user9.setUsername("username9_" + i);
        user9.setToken("t9" + i);
        user9.setStatus(UserStatus.ONLINE);
        userRepo.save(user9);
        user9.setGame(game1);
        game1.getUsers().add(user9);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);

        User user10 = new User();
        user10.setName("user10");
        user10.setUsername("username10_" + i);
        user10.setToken("t10" + i);
        user10.setStatus(UserStatus.ONLINE);
        user10.setItems(new ArrayList<Item>());
        userRepo.save(user10);
        user10.setGame(game1);
        game1.getUsers().add(user10);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);

        User user11 = new User();
        user11.setName("user11");
        user11.setUsername("username11_" + i);
        user11.setToken("t11" + i);
        user11.setStatus(UserStatus.ONLINE);
        user11.setItems(new ArrayList<Item>());
        userRepo.save(user11);
        user11.setGame(game1);
        game1.getUsers().add(user11);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);


        //game1.setOwner("user1");
        //game1.setName("game1");


        Wagon wagon1 = new Wagon();
        wagon1.setGame(game1);
        game1.getWagons().add(wagon1);
        wagonRepo.save(wagon1);
        Wagon wagon2 = new Wagon();
        wagon2.setGame(game1);
        game1.getWagons().add(wagon2);
        wagonRepo.save(wagon2);
        Wagon wagon3 = new Wagon();
        wagon3.setGame(game1);
        game1.getWagons().add(wagon3);
        wagonRepo.save(wagon3);
        Wagon wagon4 = new Wagon();
        wagon4.setGame(game1);
        game1.getWagons().add(wagon4);
        wagonRepo.save(wagon4);

        WagonLevel wagonlevel1_1 = new WagonLevel();
        wagonlevel1_1.setUsers(new ArrayList<User>());
        wagonlevel1_1.setItems(new ArrayList<Item>());
        wagonlevel1_1.setLevelType(LevelType.BOTTOM);
        wagonLevelRepo.save(wagonlevel1_1);

        WagonLevel wagonlevel2_1 = new WagonLevel();
        wagonlevel2_1.setUsers(new ArrayList<User>());
        wagonlevel2_1.setItems(new ArrayList<Item>());
        wagonlevel2_1.setLevelType(LevelType.TOP);
        wagonLevelRepo.save(wagonlevel2_1);

        WagonLevel wagonlevel1_2 = new WagonLevel();
        wagonlevel1_2.setUsers(new ArrayList<User>());
        wagonlevel1_2.setItems(new ArrayList<Item>());
        wagonlevel1_2.setLevelType(LevelType.BOTTOM);
        wagonLevelRepo.save(wagonlevel1_2);

        WagonLevel wagonlevel2_2 = new WagonLevel();
        wagonlevel2_2.setUsers(new ArrayList<User>());
        wagonlevel2_2.setItems(new ArrayList<Item>());
        wagonlevel2_2.setLevelType(LevelType.TOP);
        wagonLevelRepo.save(wagonlevel2_2);

        WagonLevel wagonlevel1_3 = new WagonLevel();
        wagonlevel1_3.setUsers(new ArrayList<User>());
        wagonlevel1_3.setItems(new ArrayList<Item>());
        wagonlevel1_3.setLevelType(LevelType.BOTTOM);
        wagonLevelRepo.save(wagonlevel1_3);

        WagonLevel wagonlevel2_3 = new WagonLevel();
        wagonlevel2_3.setUsers(new ArrayList<User>());
        wagonlevel2_3.setItems(new ArrayList<Item>());
        wagonlevel2_3.setLevelType(LevelType.TOP);
        wagonLevelRepo.save(wagonlevel2_3);

        WagonLevel wagonlevel1_4 = new WagonLevel();
        wagonlevel1_4.setUsers(new ArrayList<User>());
        wagonlevel1_4.setItems(new ArrayList<Item>());
        wagonlevel1_4.setLevelType(LevelType.BOTTOM);
        wagonLevelRepo.save(wagonlevel1_4);

        WagonLevel wagonlevel2_4 = new WagonLevel();
        wagonlevel2_4.setUsers(new ArrayList<User>());
        wagonlevel2_4.setItems(new ArrayList<Item>());
        wagonlevel2_4.setLevelType(LevelType.TOP);
        wagonLevelRepo.save(wagonlevel2_4);

        wagonlevel1_1.setWagonLevelBefore(null);
        wagonlevel1_1.setWagonLevelAfter(wagonlevel1_2);
        wagonlevel1_2.setWagonLevelBefore(wagonlevel1_1);

        wagonlevel2_1.setWagonLevelBefore(null);
        wagonlevel2_1.setWagonLevelAfter(wagonlevel2_2);
        wagonlevel2_2.setWagonLevelBefore(wagonlevel1_2);

        wagonlevel1_2.setWagonLevelAfter(wagonlevel1_3);
        wagonlevel1_3.setWagonLevelBefore(wagonlevel1_2);

        wagonlevel2_2.setWagonLevelAfter(wagonlevel2_3);
        wagonlevel2_3.setWagonLevelBefore(wagonlevel2_2);

        wagonlevel1_3.setWagonLevelAfter(wagonlevel1_4);
        wagonlevel1_4.setWagonLevelBefore(wagonlevel1_3);

        wagonlevel2_3.setWagonLevelAfter(wagonlevel2_4);
        wagonlevel2_4.setWagonLevelBefore(wagonlevel2_3);


        wagon1.setBottomLevel(wagonlevel1_1);
        wagonlevel1_1.setWagon(wagon1);
        wagon1.setTopLevel(wagonlevel2_1);
        wagonlevel2_1.setWagon(wagon1);

        wagon2.setBottomLevel(wagonlevel1_2);
        wagonlevel1_2.setWagon(wagon2);
        wagon2.setTopLevel(wagonlevel2_2);
        wagonlevel2_2.setWagon(wagon2);

        wagon3.setBottomLevel(wagonlevel1_3);
        wagonlevel1_3.setWagon(wagon3);
        wagon3.setTopLevel(wagonlevel2_3);
        wagonlevel2_3.setWagon(wagon3);

        wagon4.setBottomLevel(wagonlevel1_4);
        wagonlevel1_4.setWagon(wagon4);
        wagon4.setTopLevel(wagonlevel2_4);
        wagonlevel2_4.setWagon(wagon4);

        user1.setWagonLevel(wagonlevel2_1);
        wagonlevel2_1.getUsers().add(user1);
        user2.setWagonLevel(wagonlevel1_1);
        wagonlevel1_1.getUsers().add(user2);
        user3.setWagonLevel(wagonlevel1_2);
        wagonlevel1_2.getUsers().add(user3);
        user4.setWagonLevel(wagonlevel2_3);
        wagonlevel2_3.getUsers().add(user4);
        user5.setWagonLevel(wagonlevel1_3);
        wagonlevel1_3.getUsers().add(user5);
        user6.setWagonLevel(wagonlevel1_3);
        wagonlevel1_3.getUsers().add(user6);
        user7.setWagonLevel(wagonlevel2_4);
        wagonlevel2_4.getUsers().add(user7);
        user8.setWagonLevel(wagonlevel2_4);
        wagonlevel2_4.getUsers().add(user8);
        user9.setWagonLevel(wagonlevel1_4);
        wagonlevel1_4.getUsers().add(user9);
        user10.setWagonLevel(wagonlevel2_2);
        wagonlevel2_2.getUsers().add(user10);
        user11.setWagonLevel(wagonlevel2_4);
        wagonlevel2_4.getUsers().add(user11);

        Marshal marshal1 = new Marshal();
        marshal1.setWagonLevel(wagonlevel1_1);
        wagonlevel1_1.setMarshal(marshal1);
        marshalRepo.save(marshal1);
        marshal1.setGame(game1);
        game1.setMarshal(marshal1);
        marshalRepo.save(marshal1);
        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);

        Item item1_bag = new Item();
        item1_bag.setItemType(ItemType.BAG);
        Item item2_gem = new Item();
        item2_gem.setItemType(ItemType.GEM);
        Item item3_case = new Item();
        item3_case.setItemType(ItemType.CASE);
        Item item4_bag = new Item();
        item4_bag.setItemType(ItemType.BAG);
        Item item5_case = new Item();
        item5_case.setItemType(ItemType.CASE);
        Item item6_gem = new Item();
        item6_gem.setItemType(ItemType.GEM);
        Item item7_bag = new Item();
        item7_bag.setItemType(ItemType.BAG);

        wagonlevel1_3.getItems().add(item1_bag);
        item1_bag.setWagonLevel(wagonlevel1_3);
        wagonlevel1_3.getItems().add(item3_case);
        item3_case.setWagonLevel(wagonlevel1_3);
        wagonlevel1_3.getItems().add(item2_gem);
        item2_gem.setWagonLevel(wagonlevel1_3);

        wagonlevel1_4.getItems().add(item5_case);
        item5_case.setWagonLevel(wagonlevel1_4);
        wagonlevel1_4.getItems().add(item6_gem);
        item6_gem.setWagonLevel(wagonlevel1_4);

        user7.getItems().add(item4_bag);
        item4_bag.setUser(user7);

        user8.getItems().add(item7_bag);
        item7_bag.setUser(user8);

        gameRepo.save(game1);
//        gameCacherService.saveGame(game1);
        gameRepo.findOne(game1.getId());

        marshalRepo.save(marshal1);
        itemRepo.save(item1_bag);
        itemRepo.save(item2_gem);
        itemRepo.save(item3_case);
        itemRepo.save(item4_bag);
        itemRepo.save(item5_case);
        itemRepo.save(item6_gem);
        itemRepo.save(item7_bag);
        wagonLevelRepo.save(wagonlevel1_1);
        wagonLevelRepo.save(wagonlevel2_1);
        wagonLevelRepo.save(wagonlevel1_2);
        wagonLevelRepo.save(wagonlevel2_2);
        wagonLevelRepo.save(wagonlevel1_3);
        wagonLevelRepo.save(wagonlevel2_3);
        wagonLevelRepo.save(wagonlevel1_4);
        wagonLevelRepo.save(wagonlevel2_4);
        wagonRepo.save(wagon1);
        wagonRepo.save(wagon2);
        wagonRepo.save(wagon3);
        wagonRepo.save(wagon4);
        userRepo.save(user2);
        userRepo.save(user3);
        userRepo.save(user4);
        userRepo.save(user5);
        userRepo.save(user6);
        userRepo.save(user7);
        userRepo.save(user8);
        userRepo.save(user9);
        userRepo.save(user10);
        userRepo.save(user11);
//endregion
    }

    @Test
    public void processRequest_CollectItemIsCorrect() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(5).getId());
        User user2 = userRepo.findOne(game.getUsers().get(6).getId());

        CollectCard clc = new CollectCard();

        //games/{gameId}/action - GET
        ActionRequestDTO test = clc.createActionRequest(game, user1);

        assertTrue(test instanceof CollectItemRequestDTO);
        CollectItemRequestDTO crq = (CollectItemRequestDTO) test;

        assertEquals(game.getWagons().get(2).getBottomLevel().getItems().get(0).getItemType().equals(ItemType.BAG), crq.getHasBag());
        assertEquals(game.getWagons().get(2).getBottomLevel().getItems().get(1).getItemType().equals(ItemType.CASE), crq.getHasCase());
        assertEquals(game.getWagons().get(2).getBottomLevel().getItems().get(2).getItemType().equals(ItemType.GEM), crq.getHasGem());
    }

    @Test
    public void processRequest_ShootRequestIsCorrect_Roof() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(9).getId());

        ShootCard sc = new ShootCard();

        ActionRequestDTO test = sc.createActionRequest(game, user1);
        assertTrue(test instanceof ShootRequestDTO);
        ShootRequestDTO srq = (ShootRequestDTO) test;
        assertEquals(2, srq.getShootableUserIds().size());
    }

    @Test
    public void processRequest_ShootRequestIsCorrect_Bottom() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(4).getId());

        ShootCard sc = new ShootCard();

        ActionRequestDTO test = sc.createActionRequest(game, user1);
        assertTrue(test instanceof ShootRequestDTO);
        ShootRequestDTO srq = (ShootRequestDTO) test;
        assertEquals(2, srq.getShootableUserIds().size());
    }

    @Test
    public void processRequest_ShootableIsCorrect_Django_Roof() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(3).getId());

        ShootCard sc = new ShootCard();

        ActionRequestDTO test = sc.createActionRequest(game, user1);
        assertTrue(test instanceof ShootRequestDTO);
        ShootRequestDTO srq = (ShootRequestDTO) test;
        assertEquals(5, srq.getShootableUserIds().size());
    }

    @Test
    public void processRequest_ShootableIsCorrect_Django_Bottom() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(2).getId());

        ShootCard sc = new ShootCard();

        ActionRequestDTO test = sc.createActionRequest(game, user1);
        assertTrue(test instanceof ShootRequestDTO);
        ShootRequestDTO srq = (ShootRequestDTO) test;
        assertEquals(3, srq.getShootableUserIds().size());
    }

    @Test
    public void processRequest_MovableIsCorrect_Roof() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(3).getId());

        MoveCard mc = new MoveCard();

        ActionRequestDTO test = mc.createActionRequest(game, user1);
        assertTrue(test instanceof MoveRequestDTO);
        MoveRequestDTO mrq = (MoveRequestDTO) test;
        assertEquals(3, mrq.getMovableWagonsLvlIds().size());
    }

    @Test
    public void processRequest_MovableIsCorrect_Bottom() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(2).getId());

        MoveCard mc = new MoveCard();

        ActionRequestDTO test = mc.createActionRequest(game, user1);
        assertTrue(test instanceof MoveRequestDTO);
        MoveRequestDTO mrq = (MoveRequestDTO) test;
        assertEquals(2, mrq.getMovableWagonsLvlIds().size());
    }

    @Test
    public void processRequest_PunchableIsCorrect() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(4).getId());

        PunchCard pc = new PunchCard();

        ActionRequestDTO test = pc.createActionRequest(game, user1);
        assertTrue(test instanceof PunchRequestDTO);
        PunchRequestDTO prq = (PunchRequestDTO) test;
        assertEquals(1, prq.getPunchableUserIds().size());
    }

    @Test
    public void processRequest_PunchableIsCorrect_Belle() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(7).getId());

        PunchCard pc = new PunchCard();


        ActionRequestDTO test = pc.createActionRequest(game, user1);
        assertTrue(test instanceof PunchRequestDTO);
        PunchRequestDTO prq = (PunchRequestDTO) test;
        assertEquals(1, prq.getPunchableUserIds().size());
    }

    @Test
    public void processRequest_MarshalIsCorrect() {
        GameDTO game = gameRepo.findOne(gameId);
        User user1 = userRepo.findOne(game.getUsers().get(7).getId());

        MarshalCard mc = new MarshalCard();
        ActionRequestDTO test = mc.createActionRequest(game, user1);
        assertTrue(test instanceof MoveMarshalRequestDTO);
        MoveMarshalRequestDTO mmrq = (MoveMarshalRequestDTO) test;
        assertEquals(1, mmrq.getMovableWagonsLvlIds().size());
    }
}

