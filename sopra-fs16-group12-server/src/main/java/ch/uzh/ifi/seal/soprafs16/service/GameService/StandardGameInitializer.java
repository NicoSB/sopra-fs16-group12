package ch.uzh.ifi.seal.soprafs16.service.gameservice;

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

import java.util.ArrayList;
import java.util.Random;

@Component(value = "gameInitializer")
public class StandardGameInitializer extends GameInitializer {

    public static final int NUMBER_OF_BULLETCARDS = 6;
    public static final int NUMBER_OF_ROUNDS = 4;
    public static final int NEUTRAL_BULLETS_DECK_SIZE = 13;
    public static final int TOTAL_BAGS = 18;
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
        int maxWagons = calculateMaxWagons();
        addWagons(maxWagons);
        connectWagons(maxWagons);
    }

    private int calculateMaxWagons() {
        return game.getUsers().size() <= 3 ? 4 : game.getUsers().size() + 1;
    }

    private void addWagons(int maxWagons) {
        for (int i = 0; i < maxWagons; i++) {
            addWagon();
        }
    }

    private void addWagon() {
        Wagon wagon = createWagon();
        initTopLevel(wagon);
        initBottomLevel(wagon);
    }

    private Wagon createWagon() {
        Wagon wagon = new Wagon();
        wagon.setGame(game);
        game.getWagons().add(wagon);
        wagonRepo.save(wagon);
        return wagon;
    }

    private void initTopLevel(Wagon wagon) {
        WagonLevel topLevel = new WagonLevel();
        topLevel.setLevelType(LevelType.TOP);
        topLevel.setItems(new ArrayList<>());
        wagon.setTopLevel(topLevel);
        topLevel.setWagon(wagon);
        topLevel.setUsers(new ArrayList<>());
        wagonLevelRepo.save(topLevel);
    }

    private void initBottomLevel(Wagon wagon) {
        WagonLevel botLevel = new WagonLevel();
        botLevel.setLevelType(LevelType.BOTTOM);
        botLevel.setItems(new ArrayList<>());
        wagon.setBottomLevel(botLevel);
        botLevel.setWagon(wagon);
        botLevel.setUsers(new ArrayList<>());
        wagonLevelRepo.save(botLevel);
    }

    private void connectWagons(int maxWagons) {
        int counter = 0;
        for (Wagon wagon : game.getWagons()) {
            if (counter != 0) {
                Wagon wagonBefore = game.getWagons().get(counter - 1);
                wagon.getTopLevel().setWagonLevelBefore(wagonBefore.getTopLevel());
                wagon.getBottomLevel().setWagonLevelBefore(wagonBefore.getBottomLevel());
            }
            if (counter != maxWagons - 1) {
                Wagon wagonAfter = game.getWagons().get(counter + 1);
                wagon.getTopLevel().setWagonLevelAfter(wagonAfter.getTopLevel());
                wagon.getBottomLevel().setWagonLevelAfter(wagonAfter.getBottomLevel());
            }
            wagonRepo.save(wagon);
            counter++;
        }
    }

    @Override
    void initPlayerDecks() {
        for (User user : game.getUsers()) {
            createBulletDeck(user);
            ArrayList<ActionCard> actionCards = createActionCards();

            initHandDeck(user, actionCards);
            initHiddenDeck(user, actionCards);
        }
    }

    private void createBulletDeck(User user) {
        if (user.getCharacter() == null)
            throw new IllegalArgumentException("User " + user.getId() + "does not have a character!");

        PlayerDeck<BulletCard> bulletsDeck = new PlayerDeck<>();
        bulletsDeck.setUser(user);
        user.setBulletsDeck(bulletsDeck);

        deckRepo.save(bulletsDeck);
        userRepo.save(user);

        for (int i = 0; i < NUMBER_OF_BULLETCARDS; i++) {
            insertBulletCard(user, bulletsDeck, i);
        }
    }

    private void insertBulletCard(User user, PlayerDeck<BulletCard> bulletsDeck, int bulletCounter) {
        BulletCard bulletCard = new BulletCard();
        bulletCard.setBulletCounter(bulletCounter + 1);
        SourceType st = SourceType.valueOf(user.getCharacter().getClass().getSimpleName().toUpperCase());
        bulletCard.setSourceType(st);
        bulletsDeck.getCards().add(bulletCard);
        bulletCard.setDeck(bulletsDeck);
        cardRepo.save(bulletCard);
    }

    private ArrayList<ActionCard> createActionCards() {
        ArrayList<ActionCard> actionCards = new ArrayList<>();
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

    private void initHandDeck(User user, ArrayList<ActionCard> allActionCards) {
        PlayerDeck<HandCard> handDeck = createHandDeck(user);
        moveRandomCards(user, allActionCards, handDeck);
    }

    private PlayerDeck<HandCard> createHandDeck(User user) {
        PlayerDeck<HandCard> handDeck = new PlayerDeck<>();
        handDeck.setUser(user);
        user.setHandDeck(handDeck);
        deckRepo.save(handDeck);
        userRepo.save(user);
        return handDeck;
    }

    private void moveRandomCards(User user, ArrayList<ActionCard> sourceList, PlayerDeck<HandCard> targetDeck) {
        int drawCardsAmount = user.getCharacter() instanceof Doc ? 7 : 6;

        for (int i = 0; i < drawCardsAmount; i++) {
            moveRandomCard(user, sourceList, targetDeck);
        }
    }

    private void moveRandomCard(User user, ArrayList<ActionCard> sourceList, PlayerDeck<HandCard> targetDeck) {
        int randomIndex = new Random().nextInt(sourceList.size());
        sourceList.get(randomIndex).setDeck(targetDeck);
        sourceList.get(randomIndex).setPlayedByUserId(user.getId());
        targetDeck.getCards().add(sourceList.get(randomIndex));
        sourceList.remove(randomIndex);
    }

    private void initHiddenDeck(User user, ArrayList<ActionCard> actionCards) {
        PlayerDeck<HandCard> hiddenDeck = new PlayerDeck<>();
        hiddenDeck.setUser(user);
        user.setHiddenDeck(hiddenDeck);
        deckRepo.save(hiddenDeck);
        userRepo.save(user);

        for (ActionCard actionCard: actionCards) {
            actionCard.setDeck(hiddenDeck);
            hiddenDeck.add(actionCard);
        }
    }

    @Override
    void initGameDecks() {
        initRoundCardDeck();
        initNeutralBulletsDeck();
        initCommonDeck();

        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }

    private void initRoundCardDeck() {
        GameDeck<RoundCard> roundCardDeck = createRoundCardDeck();
        addRandomRoundCards(roundCardDeck);

        addStationCard(roundCardDeck);

        deckRepo.save(roundCardDeck);
        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }

    private GameDeck<RoundCard> createRoundCardDeck() {
        GameDeck<RoundCard> roundCardDeck = new GameDeck<>();
        roundCardDeck.setGame(game);
        game.setRoundCardDeck(roundCardDeck);
        deckRepo.save(roundCardDeck);

        return roundCardDeck;
    }

    private void addRandomRoundCards(GameDeck<RoundCard> roundCardDeck) {
        ArrayList<RoundCard> possibleRoundCards = setPatternOnRoundCards();

        final int[] randomChosenRoundCards = new Random()
                .ints(0, possibleRoundCards.size())
                .distinct()
                .limit(NUMBER_OF_ROUNDS)
                .toArray();

        for (int randomIndex : randomChosenRoundCards) {
            roundCardDeck.getCards().add(possibleRoundCards.get(randomIndex));
            possibleRoundCards.get(randomIndex).setDeck(roundCardDeck);
        }
    }

    private void addStationCard(GameDeck<RoundCard> roundCardDeck) {
        setPatternOnStationCards();
        int stationCardId = new Random().nextInt(3);

        RoundCard stationCard = setPatternOnStationCards().get(stationCardId);
        roundCardDeck.getCards().add(stationCard);
        stationCard.setDeck(roundCardDeck);
    }

    private void initNeutralBulletsDeck() {
        GameDeck<BulletCard> neutralBulletsDeck = createNeutralBulletsDeck();

        for (int i = 0; i < NEUTRAL_BULLETS_DECK_SIZE; i++) {
            createNeutralBulletCard(neutralBulletsDeck, i + 1);
        }
    }

    private GameDeck<BulletCard> createNeutralBulletsDeck() {
        GameDeck<BulletCard> neutralBulletsDeck = new GameDeck<BulletCard>();
        neutralBulletsDeck.setGame(game);
        game.setNeutralBulletsDeck(neutralBulletsDeck);
        deckRepo.save(neutralBulletsDeck);
        gameRepo.save(game);
        gameCacherService.saveGame(game);
        return neutralBulletsDeck;
    }

    private void createNeutralBulletCard(GameDeck<BulletCard> neutralBulletsDeck, int bulletCounter) {
        BulletCard bulletCard = new BulletCard();
        bulletCard.setBulletCounter(bulletCounter);
        bulletCard.setSourceType(SourceType.MARSHAL);
        neutralBulletsDeck.getCards().add(bulletCard);
        bulletCard.setDeck(neutralBulletsDeck);
        cardRepo.save(bulletCard);
    }

    private void initCommonDeck() {
        GameDeck<ActionCard> commonDeck = new GameDeck<ActionCard>();
        commonDeck.setGame(game);
        game.setCommonDeck(commonDeck);
        deckRepo.save(commonDeck);
    }

    private ArrayList<RoundCard> setPatternOnRoundCards() {
        ArrayList<RoundCard> possibleRoundCards = new ArrayList<>();

        AngryMarshalCard angryMarshalCard = (AngryMarshalCard) createTurnPattern(new AngryMarshalCard());
        possibleRoundCards.add(angryMarshalCard);

        PivotablePoleCard pivotablePoleCard = (PivotablePoleCard) createTurnPattern(new PivotablePoleCard());
        possibleRoundCards.add(pivotablePoleCard);

        BrakingCard brakingCard = (BrakingCard) createTurnPattern(new BrakingCard());
        possibleRoundCards.add(brakingCard);

        GetItAllCard getItAllCard = (GetItAllCard) createTurnPattern(new GetItAllCard());
        possibleRoundCards.add(getItAllCard);

        PassengerRebellionCard passengerRebellionCard = (PassengerRebellionCard) createTurnPattern(new PassengerRebellionCard());
        possibleRoundCards.add(passengerRebellionCard);

        BlankTunnelCard blankTunnelCard = (BlankTunnelCard) createTurnPattern(new BlankTunnelCard());
        possibleRoundCards.add(blankTunnelCard);

        BlankBridgeCard blankBridgeCard = (BlankBridgeCard) createTurnPattern(new BlankBridgeCard());
        possibleRoundCards.add(blankBridgeCard);

        return possibleRoundCards;
    }

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

        PickPocketingCard pickPocketingCard = (PickPocketingCard) createTurnPattern(new PickPocketingCard());
        possibleStationCards.add(pickPocketingCard);

        MarshallsRevengeCard marshallsRevengeCard = (MarshallsRevengeCard) createTurnPattern(new MarshallsRevengeCard());
        possibleStationCards.add(marshallsRevengeCard);

        HostageCard hostageCard = (HostageCard) createTurnPattern(new HostageCard());
        possibleStationCards.add(hostageCard);

        return possibleStationCards;
    }

    @Override
    void initGameFigurines() {
        int firstPlayerIndex = setRandomFirstPlayer();
        placeUsersOnWagons(firstPlayerIndex);
        initMarshal();
    }

    private int setRandomFirstPlayer() {
        int firstPlayerIndex = new Random().nextInt(game.getUsers().size());
        game.setCurrentPlayerIndex(firstPlayerIndex);
        return firstPlayerIndex;
    }

    private void placeUsersOnWagons(int firstPlayerIndex) {
        WagonLevel lastWagonLevelBot = game.getWagons().get(game.getWagons().size() - 1).getBottomLevel();
        WagonLevel secondLastWagonLevelBot = lastWagonLevelBot.getWagonLevelBefore();

        int userCount = game.getUsers().size();
        for (int i = 0; i < userCount; i++) {
            // User Order is based on "sitting order". Therefore, the user list has to be cycled through.
            int userIndex = (firstPlayerIndex + i) % userCount;
            User user = game.getUsers().get(userIndex);
            WagonLevel wagonLevel = (i + 1) % 2 == 0 ? secondLastWagonLevelBot : lastWagonLevelBot;

            placeUserOnWagonLevel(user, wagonLevel);
        }
    }

    private void placeUserOnWagonLevel(User user, WagonLevel wagonLevel) {
        wagonLevel.getUsers().add(user);
        user.setWagonLevel(wagonLevel);
    }

    private void initMarshal() {
        Marshal marshal = new Marshal();
        marshal.setGame(game);
        game.setMarshal(marshal);
        game.getWagons().get(0).getBottomLevel().setMarshal(marshal);
        marshal.setWagonLevel(game.getWagons().get(0).getBottomLevel());
        marshalRepo.save(marshal);
    }

    @Override
    void initGameItems() {
        distributeStartItems();
        placeMoneyCase();
        distributeItems();
    }

    private void distributeStartItems() {
        for (User user : game.getUsers()) {
            Item bag = new Item();
            bag.setValue(250);
            bag.setItemType(ItemType.BAG);
            user.getItems().add(bag);
            bag.setUser(user);
            itemRepo.save(bag);
            userRepo.save(user);
        }
    }

    private void placeMoneyCase() {
        WagonLevel locomotiveBottom = game.getWagons().get(0).getBottomLevel();
        Item moneyCase = new Item();
        moneyCase.setValue(1000);
        moneyCase.setItemType(ItemType.CASE);
        moneyCase.setWagonLevel(locomotiveBottom);
        locomotiveBottom.getItems().add(moneyCase);
        itemRepo.save(moneyCase);
    }

    private void distributeItems() {
        final ArrayList<WagonItemCounter> randomItemCounters = getRandomItemCounters(game.getWagons().size());
        ArrayList<Integer> bagTypes = initBags();

        int requiredBagCount = calculateNumberOfNeededBags(randomItemCounters);
        placeRandomItems(randomItemCounters, bagTypes, requiredBagCount);
    }

    private ArrayList<WagonItemCounter> getRandomItemCounters(int count) {
        ArrayList<WagonItemCounter> itemCounters = createItemCounters();
        final int[] randomIndices = getRandomIndices(itemCounters.size(), count);

        ArrayList<WagonItemCounter> randomItemCounters = new ArrayList<>();
        for (int index : randomIndices) {
            randomItemCounters.add(itemCounters.get(index));
        }

        return randomItemCounters;
    }

    private ArrayList<WagonItemCounter> createItemCounters() {
        ArrayList<WagonItemCounter> wagonTypes = new ArrayList<>();
        wagonTypes.add(new WagonItemCounter(3, 0));
        wagonTypes.add(new WagonItemCounter(1, 1));
        wagonTypes.add(new WagonItemCounter(0, 1));
        wagonTypes.add(new WagonItemCounter(1, 3));
        wagonTypes.add(new WagonItemCounter(1, 4));
        return wagonTypes;
    }

    private ArrayList<Integer> initBags() {
        ArrayList<Integer> bagTypes = new ArrayList<>();

        // Rule: values from 300$ to 500$ appear twice, 250$ appear 8times, but at the start every player already gets 1 250$ bag
        for (int i = 0; i < 8 - game.getUsers().size(); i++) {
            bagTypes.add(250);
        }
        for (int i = 0; i < 5; i++) {
            bagTypes.add(300 + i * 50);
            bagTypes.add(300 + i * 50);
        }
        return bagTypes;
    }

    private int calculateNumberOfNeededBags(ArrayList<WagonItemCounter> randomItemCounters) {
        int bagsToDistribute = 0;
        for (WagonItemCounter counter : randomItemCounters) {
            bagsToDistribute += counter.bagCount;
        }
        return bagsToDistribute;
    }

    private void placeRandomItems(ArrayList<WagonItemCounter> randomItemCounters, ArrayList<Integer> bagTypes, int bagsToDistribute) {
        final int[] randomItemTypeIndices = getRandomIndices(TOTAL_BAGS - game.getUsers().size(), bagsToDistribute);
        placeItems(randomItemCounters, bagTypes, randomItemTypeIndices);
    }

    private int[] getRandomIndices(int maxValue, int count) {
        return new Random().ints(0, maxValue)
                .distinct()
                .limit(count)
                .toArray();
    }

    private void placeItems(ArrayList<WagonItemCounter> randomItemCounters, ArrayList<Integer> bagTypes, int[] randomItemTypeIndices) {
        int wagonTypeCounter = 0;
        int bagTypeCounter = 0;
        for (Wagon w : game.getWagons().subList(1, game.getWagons().size())) {
            WagonLevel botLevel = w.getBottomLevel();
            int gemCount = randomItemCounters.get(wagonTypeCounter).gemCount;
            int bagCount = randomItemCounters.get(wagonTypeCounter).bagCount;

            for (int d = 0; d < gemCount; d++) {
                placeGem(botLevel);
            }
            for (int b = 0; b < bagCount; b++) {
                placeItem(bagTypes.get(randomItemTypeIndices[bagTypeCounter]), botLevel, ItemType.BAG);
                bagTypeCounter++;
            }

            wagonLevelRepo.save(botLevel);
            wagonTypeCounter++;
        }
    }

    private void placeGem(WagonLevel botLevel) {
        placeItem(500, botLevel, ItemType.GEM);
    }

    private void placeItem(Integer index, WagonLevel botLevel, ItemType bag2) {
        Item bag = new Item();
        bag.setItemType(bag2);
        bag.setValue(index);
        bag.setWagonLevel(botLevel);
        botLevel.getItems().add(bag);
        itemRepo.save(bag);
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
