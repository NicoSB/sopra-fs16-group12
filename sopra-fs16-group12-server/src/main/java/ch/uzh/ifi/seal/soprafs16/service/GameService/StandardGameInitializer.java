package ch.uzh.ifi.seal.soprafs16.service.GameService;

import ch.uzh.ifi.seal.soprafs16.constant.*;
import ch.uzh.ifi.seal.soprafs16.model.*;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.*;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.*;
import ch.uzh.ifi.seal.soprafs16.model.characters.Doc;
import ch.uzh.ifi.seal.soprafs16.model.repositories.*;
import ch.uzh.ifi.seal.soprafs16.model.turns.*;
import ch.uzh.ifi.seal.soprafs16.service.GameCacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

@Component(value = "gameInitializer")
public class StandardGameInitializer extends GameInitializer {

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
    private CardRepository cardRepo;
    @Autowired
    private DeckRepository deckRepo;
    @Autowired
    private TurnRepository turnRepo;
    @Autowired
    private GameCacherService gameCacherService;

    @Override
    void initGameWagons() {
        int maxWagons; //Locomotive is also a wagon, so 4 Wagons means 3 carriages and 1 locomotive
        if (game.getUsers().size() <= 3) {
            maxWagons = 4;
        } else {
            maxWagons = game.getUsers().size() + 1;
        }

        // TODO factory method?
        for (int i = 0; i < maxWagons; i++) {
            Wagon wagon = new Wagon();
            wagon.setGame(game);
            game.getWagons().add(wagon);
            wagonRepo.save(wagon);

            WagonLevel topLevel = new WagonLevel();
            topLevel.setLevelType(LevelType.TOP);
            topLevel.setItems(new ArrayList<Item>());
            wagon.setTopLevel(topLevel);
            topLevel.setWagon(wagon);
            topLevel.setUsers(new ArrayList<User>());
            wagonLevelRepo.save(topLevel);

            WagonLevel botLevel = new WagonLevel();
            botLevel.setLevelType(LevelType.BOTTOM);
            botLevel.setItems(new ArrayList<Item>());
            wagon.setBottomLevel(botLevel);
            botLevel.setWagon(wagon);
            botLevel.setUsers(new ArrayList<User>());
            wagonLevelRepo.save(botLevel);
        }

        int counter = 0;
        for (Wagon w : game.getWagons()) {
            Wagon thisWagon = game.getWagons().get(counter);
            if (counter != 0) {
                Wagon wagonBefore = game.getWagons().get(counter - 1);
                thisWagon.getTopLevel().setWagonLevelBefore(wagonBefore.getTopLevel());
                thisWagon.getBottomLevel().setWagonLevelBefore(wagonBefore.getBottomLevel());
            }
            if (counter != maxWagons - 1) {
                Wagon wagonAfter = game.getWagons().get(counter + 1);
                thisWagon.getTopLevel().setWagonLevelAfter(wagonAfter.getTopLevel());
                thisWagon.getBottomLevel().setWagonLevelAfter(wagonAfter.getBottomLevel());
            }
            wagonRepo.save(thisWagon);
            counter++;
        }
    }

    @Override
    void initPlayerDecks() {
        for (User user : game.getUsers()) {

            //give personal Bulletcards to each player
            PlayerDeck<BulletCard> bulletsDeck = new PlayerDeck<BulletCard>();

            bulletsDeck.setUser(user);
            user.setBulletsDeck(bulletsDeck);
            deckRepo.save(bulletsDeck);
            userRepo.save(user);
            if (user.getCharacter() != null) {
                for (int i = 0; i < 6; i++) {
                    BulletCard bulletCard = new BulletCard();
                    bulletCard.setBulletCounter(i + 1);
                    SourceType st = SourceType.valueOf(user.getCharacter().getClass().getSimpleName().toUpperCase());
                    bulletCard.setSourceType(st);
                    bulletsDeck.getCards().add(bulletCard);
                    bulletCard.setDeck(bulletsDeck);
                    cardRepo.save(bulletCard);
                }
            } else {
                return;
            }

            //give handcards to each player (6 for each, except doc gets 7)
            PlayerDeck<HandCard> handDeck = new PlayerDeck<>();
            handDeck.setUser(user);
            user.setHandDeck(handDeck);
            deckRepo.save(handDeck);
            userRepo.save(user);

            ArrayList<ActionCard> allActionCards = createActionCards(user);

            int drawCardsAmount = user.getCharacter().getClass().equals(Doc.class) ? 7 : 6;

            final int[] randomChosenHandCards = new Random().ints(0, 10).distinct().limit(drawCardsAmount).toArray();
            for (int randomIndex : randomChosenHandCards) {
                handDeck.getCards().add(allActionCards.get(randomIndex));
                allActionCards.get(randomIndex).setDeck(handDeck);
                allActionCards.get(randomIndex).setPlayedByUserId(user.getId());
            }

            //put the rest of the player's actioncards into his hiddendeck
            PlayerDeck<HandCard> hiddenDeck = new PlayerDeck<>();
            hiddenDeck.setUser(user);
            user.setHiddenDeck(hiddenDeck);
            deckRepo.save(hiddenDeck);
            userRepo.save(user);

            for (int i = 0; i < 10; i++) {
                final int finalI = i;
                if (!IntStream.of(randomChosenHandCards).anyMatch(x -> x == finalI)) {
                    hiddenDeck.getCards().add(allActionCards.get(i));
                    allActionCards.get(i).setDeck(hiddenDeck);
                    allActionCards.get(i).setPlayedByUserId(user.getId());
                }
            }
        }
    }

    private ArrayList<ActionCard> createActionCards(User user) {
        ArrayList<ActionCard> actionCards = new ArrayList<>();
        //10ActionCards in Total, 2x Move, 2x ChangeLevel, 1x Punch, 1x MoveMarshal, 2x Shoot, 2x Collect
        actionCards.add(new MoveCard());
        actionCards.add(new MoveCard());
        actionCards.add(new ChangeLevelCard());
        actionCards.add(new ChangeLevelCard());
        actionCards.add(new PunchCard());
        actionCards.add(new MarshalCard());
        actionCards.add(new ShootCard());
        actionCards.add(new ShootCard());

        actionCards.add(new CollectCard());
        actionCards.add(new CollectCard());
        cardRepo.save(actionCards);

        return actionCards;
    }

    @Override
    void initGameDecks() {
        ArrayList<RoundCard> possibleRoundCards = setPatternOnRoundCards();
        //choose 4 Random Roundcards
        final int[] randomChosenRoundCards = new Random().ints(0, 7).distinct().limit(4).toArray();
        GameDeck<RoundCard> roundCardDeck = new GameDeck<>();
        roundCardDeck.setGame(game);
        game.setRoundCardDeck(roundCardDeck);
        deckRepo.save(roundCardDeck);
        gameRepo.save(game);
        gameCacherService.saveGame(game);

        for (int randomIndex : randomChosenRoundCards) {
            roundCardDeck.getCards().add(possibleRoundCards.get(randomIndex));
            possibleRoundCards.get(randomIndex).setDeck(roundCardDeck);
        }

        //add a stationcard
        setPatternOnStationCards();
        Random rn = new Random();
        int stationCardId = rn.nextInt(3);

        RoundCard stationCard = setPatternOnStationCards().get(stationCardId);
        roundCardDeck.getCards().add(stationCard);
        stationCard.setDeck(roundCardDeck);
        //endregion
        //region neutralBulletsDeck
        GameDeck<BulletCard> neutralBulletsDeck = new GameDeck<BulletCard>();
        neutralBulletsDeck.setGame(game);
        game.setNeutralBulletsDeck(neutralBulletsDeck);
        deckRepo.save(neutralBulletsDeck);
        gameRepo.save(game);
        gameCacherService.saveGame(game);
        for (int i = 0; i < 13; i++) {
            BulletCard bulletCard = new BulletCard();
            bulletCard.setBulletCounter(i + 1);
            bulletCard.setSourceType(SourceType.MARSHAL);
            neutralBulletsDeck.getCards().add(bulletCard);
            bulletCard.setDeck(neutralBulletsDeck);
            cardRepo.save(bulletCard);
        }
        //endregion
        //region commonDeck
        GameDeck<ActionCard> commonDeck = new GameDeck<ActionCard>();
        commonDeck.setGame(game);
        game.setCommonDeck(commonDeck);
        deckRepo.save(commonDeck);
        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }

    // TODO this can be refactored.
    private ArrayList<RoundCard> setPatternOnRoundCards() {
        ArrayList<RoundCard> possibleRoundCards = new ArrayList<>();

        //AngryMarshalCard
        AngryMarshalCard angryMarshalCard = (AngryMarshalCard) createTurnPattern(new AngryMarshalCard());
        possibleRoundCards.add(angryMarshalCard);
        //PivotablePoleCard
        PivotablePoleCard pivotablePoleCard = (PivotablePoleCard) createTurnPattern(new PivotablePoleCard());
        possibleRoundCards.add(pivotablePoleCard);
        //BrakingCard
        BrakingCard brakingCard = (BrakingCard) createTurnPattern(new BrakingCard());
        possibleRoundCards.add(brakingCard);
        //GetItAllCard
        GetItAllCard getItAllCard = (GetItAllCard) createTurnPattern(new GetItAllCard());
        possibleRoundCards.add(getItAllCard);
        //PassengerRebellionCard
        PassengerRebellionCard passengerRebellionCard = (PassengerRebellionCard) createTurnPattern(new PassengerRebellionCard());
        possibleRoundCards.add(passengerRebellionCard);
        //BlankTunnelCard
        BlankTunnelCard blankTunnelCard = (BlankTunnelCard) createTurnPattern(new BlankTunnelCard());
        possibleRoundCards.add(blankTunnelCard);
        //BlankBridgeCard
        BlankBridgeCard blankBridgeCard = (BlankBridgeCard) createTurnPattern(new BlankBridgeCard());
        possibleRoundCards.add(blankBridgeCard);

        return possibleRoundCards;
    }

    // TODO Refactor
    private RoundCard createTurnPattern(RoundCard roundCard) {
        ArrayList<Turn> pattern = new ArrayList<>();
        roundCard.setPattern(pattern);
        cardRepo.save(roundCard);
        for (char c : roundCard.getStringPattern().toCharArray()) {
            switch (c) {
                case 'N':
                    NormalTurn normalTurn = new NormalTurn();
                    normalTurn.setRoundCard(roundCard);
                    pattern.add(normalTurn);
                    turnRepo.save(normalTurn);
                    break;
                case 'T':
                    TunnelTurn tunnelTurn = new TunnelTurn();
                    tunnelTurn.setRoundCard(roundCard);
                    pattern.add(tunnelTurn);
                    turnRepo.save(tunnelTurn);
                    break;
                case 'R':
                    ReverseTurn reverseTurn = new ReverseTurn();
                    reverseTurn.setRoundCard(roundCard);
                    pattern.add(reverseTurn);
                    turnRepo.save(reverseTurn);
                    break;
                case 'S':
                    SpeedupTurn speedupTurn = new SpeedupTurn();
                    speedupTurn.setRoundCard(roundCard);
                    pattern.add(speedupTurn);
                    turnRepo.save(speedupTurn);
                    break;
                default:
                    return null;
            }
        }

        return roundCard;
    }


    private ArrayList<RoundCard> setPatternOnStationCards() {
        ArrayList<RoundCard> possibleStationCards = new ArrayList<>();

        //PickPocketingCard
        PickPocketingCard pickPocketingCard = (PickPocketingCard) createTurnPattern(new PickPocketingCard());
        possibleStationCards.add(pickPocketingCard);
        //MarshallsRevengeCard
        MarshallsRevengeCard marshallsRevengeCard = (MarshallsRevengeCard) createTurnPattern(new MarshallsRevengeCard());
        possibleStationCards.add(marshallsRevengeCard);
        //HostageCard
        HostageCard hostageCard = (HostageCard) createTurnPattern(new HostageCard());
        possibleStationCards.add(hostageCard);

        return possibleStationCards;
    }

    @Override
    void initGameFigurines() {
        //set Random 1st Player
        Random rn = new Random();
        int firstPlayer = rn.nextInt(game.getUsers().size());
        game.setCurrentPlayerIndex(firstPlayer);

        //place odd-number-players in last waggon, even-number-players in second-last
        WagonLevel lastWagonLevelBot = game.getWagons().get(game.getWagons().size() - 1).getBottomLevel();
        WagonLevel secondLastWagonLevelBot = lastWagonLevelBot.getWagonLevelBefore();

        boolean isEven = false;
        int userCount = game.getUsers().size();
        for (int i = 0; i < userCount; i++) {
            int userIndex = (firstPlayer + i) % userCount;
            User user = game.getUsers().get(userIndex);
            if (isEven) {
                secondLastWagonLevelBot.getUsers().add(user);
                user.setWagonLevel(secondLastWagonLevelBot);
                isEven = false;
            } else {
                lastWagonLevelBot.getUsers().add(user);
                user.setWagonLevel(lastWagonLevelBot);
                isEven = true;
            }

        }

        //place Marshal
        Marshal marshal = new Marshal();
        marshal.setGame(game);
        game.setMarshal(marshal);
        game.getWagons().get(0).getBottomLevel().setMarshal(marshal);
        marshal.setWagonLevel(game.getWagons().get(0).getBottomLevel());
        marshalRepo.save(marshal);
    }

    @Override
    void initGameItems() {
        for (User user : game.getUsers()) {
            Item bag = new Item();
            bag.setValue(250);
            bag.setItemType(ItemType.BAG);
            user.getItems().add(bag);
            bag.setUser(user);
            itemRepo.save(bag);
            userRepo.save(user);
        }

        //put
        WagonLevel locomotiveBot = game.getWagons().get(0).getBottomLevel();
        Item moneyCase = new Item();
        moneyCase.setValue(1000);
        moneyCase.setItemType(ItemType.CASE);
        moneyCase.setWagonLevel(locomotiveBot);
        locomotiveBot.getItems().add(moneyCase);
        itemRepo.save(moneyCase);

        //put all the possible wagontypes in the wagonTypes list, the tuples are <#Gems,#Bags>
        ArrayList<AbstractMap.SimpleImmutableEntry<Integer, Integer>> wagonTypes = new ArrayList<>();
        wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(3, 0));
        wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(1, 1));
        wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(0, 1));
        wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(1, 3));
        wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(1, 4));
        int carriageCount = game.getWagons().size() - 1;
        //randomly select which wagonTypes will be used in this game (if less than 5 players play, not all wagons are taken)
        final int[] randomChosenWagonTypes = new Random().ints(0, 5).distinct().limit(carriageCount).toArray();

        //put all the possible bag-types in the bagTypes list, values from 300$ to 500$ appear twice, 250$ appear 8times, but at the start every player already gets 1 250$ bag
        ArrayList<Integer> bagTypes = new ArrayList<>();
        for (int i = 0; i < 8 - game.getUsers().size(); i++) {
            bagTypes.add(250);
        }
        for (int i = 0; i < 5; i++) {
            bagTypes.add(300 + i * 50);
            bagTypes.add(300 + i * 50);
        }

        int bagsToDistribute = 0;
        for (int i : randomChosenWagonTypes) {
            bagsToDistribute += wagonTypes.get(i).getValue();
        }

        //randomly choose the bags that will be distributed on the wagons out of the pool of possible bags
        final int[] randomChosenItemTypes = new Random().ints(0, 18 - game.getUsers().size()).distinct().limit(bagsToDistribute).toArray();

        //put the diamond/bag combinations into the wagons (bottomLevel)
        int wagonTypeCounter = 0;
        int bagTypeCounter = 0;
        for (Wagon w : game.getWagons().subList(1, game.getWagons().size())) {
            int gems = wagonTypes.get(randomChosenWagonTypes[wagonTypeCounter]).getKey();
            int bags = wagonTypes.get(randomChosenWagonTypes[wagonTypeCounter]).getValue();
            WagonLevel botLevel = w.getBottomLevel();
            for (int d = 0; d < gems; d++) {
                Item gem = new Item();
                gem.setItemType(ItemType.GEM);
                gem.setValue(500);
                gem.setWagonLevel(botLevel);
                botLevel.getItems().add(gem);
                itemRepo.save(gem);
            }
            for (int b = 0; b < bags; b++) {
                Item bag = new Item();
                bag.setItemType(ItemType.BAG);
                bag.setValue(bagTypes.get(randomChosenItemTypes[bagTypeCounter++]));
                bag.setWagonLevel(botLevel);
                botLevel.getItems().add(bag);
                itemRepo.save(bag);
            }
            wagonLevelRepo.save(botLevel);
            wagonTypeCounter++;
        }

    }

    @Override
    void initGameStatus() {
        game.setCurrentRound(0);
        game.setCurrentTurn(0);
        game.setCurrentPhase(PhaseType.PLANNING);
        game.setRoundStarter(game.getCurrentPlayerIndex());
        game.setActionRequestCounter(0);
        game.setRoundPattern(((RoundCard) (game.getRoundCardDeck().getCards().get(0))).getStringPattern());

        game.setStatus(GameStatus.RUNNING);
        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }
}
