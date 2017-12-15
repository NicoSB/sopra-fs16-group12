package ch.uzh.ifi.seal.soprafs16.service.GameService;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.*;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("demoGameInitializer")
public class DemoGameInitializer extends StandardGameInitializer {
    @Autowired
    private UserRepository userRepo;
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

    @Override
    void initGameStatus() {
        super.initGameStatus();

        game.setCurrentRound(game.getRoundCardDeck().getCards().size() - 1);
        placeBulletsDemo(game);
        giveItemsDemo(game);
        relocateMarshal(game, 1);
        relocatePlayer(game.getUsers().get(0), game.getWagons().get(0).getBottomLevel());
        relocatePlayer(game.getUsers().get(1), game.getWagons().get(3).getTopLevel());

    }

    private void placeBulletsDemo(GameDTO game) {
        addPlayerBulletToDeck(game.getUsers().get(0), game.getUsers().get(1));

        addPlayerBulletToDeck(game.getUsers().get(1), game.getUsers().get(0));
        addPlayerBulletToDeck(game.getUsers().get(1), game.getUsers().get(0));

        //both players got shot once by the marshal
        addMarshalBulletToDeck(game, game.getUsers().get(0));
        addMarshalBulletToDeck(game, game.getUsers().get(1));
    }

    private void addMarshalBulletToDeck(GameDTO fromGame, User toUser) {
        GameDeck<BulletCard> bulletsDeckFrom = fromGame.getNeutralBulletsDeck();
        PlayerDeck<HandCard> handDeckToUser = toUser.getHandDeck();
        int id = bulletsDeckFrom.getCards().size() - 1;

        //take a bullet from the shooter to the shot user
        BulletCard bulletCard = (BulletCard) bulletsDeckFrom.getCards().get(id);
        bulletsDeckFrom.getCards().remove(id);
        deckRepo.save(bulletsDeckFrom);

        bulletCard.setDeck(handDeckToUser);
        handDeckToUser.getCards().add(bulletCard);
        cardRepo.save(bulletCard);

        relevelHandDeck(toUser);
    }

    private void giveItemsDemo(GameDTO game) {
        handOverItem(game.getUsers().get(0), ItemType.BAG, game);
        handOverItem(game.getUsers().get(0), ItemType.CASE, game);
        handOverItem(game.getUsers().get(1), ItemType.GEM, game);
        handOverItem(game.getUsers().get(1), ItemType.GEM, game);
        handOverItem(game.getUsers().get(1), ItemType.BAG, game);
    }

    private void handOverItem(User user, ItemType itemType, GameDTO game) {
        for (Wagon wagon : game.getWagons()) {
            int index = wagonLevelContainsItem(wagon.getBottomLevel(), itemType);
            if (index != -1) {
                Item item = wagon.getBottomLevel().getItems().get(index);
                item.setWagonLevel(null);
                wagon.getBottomLevel().getItems().remove(index);
                wagonLevelRepo.save(wagon.getBottomLevel());
                itemRepo.save(item);
                user.getItems().add(item);
                item.setUser(user);
                itemRepo.save(item);
                userRepo.save(user);
            }
        }
    }

    private int wagonLevelContainsItem(WagonLevel wagonLevel, ItemType itemType) {
        int index = 0;
        for (Item item : wagonLevel.getItems()) {
            if (item.getItemType().equals(itemType)) {
                return index;
            }
            index++;
        }
        return -1;
    }


    private void relocateMarshal(GameDTO game, int wagonId) {
        WagonLevel wagonLevelNew = game.getWagons().get(wagonId).getBottomLevel();
        Marshal marshal = game.getMarshal();
        WagonLevel wagonLevelOld = marshal.getWagonLevel();
        wagonLevelOld.setMarshal(null);
        wagonLevelRepo.save(wagonLevelOld);
        marshal.setWagonLevel(wagonLevelNew);
        wagonLevelNew.setMarshal(marshal);
        wagonLevelRepo.save(wagonLevelNew);
        marshalRepo.save(marshal);
    }

    //put an actioncard to the hiddendeck, so that the total number of handcard stays the same
    private void relevelHandDeck(User user) {
        HandCard handCard = (HandCard) user.getHandDeck().getCards().get(0);
        user.getHandDeck().getCards().remove(0);
        deckRepo.save(user.getHandDeck());

        handCard.setDeck(user.getHiddenDeck());
        user.getHiddenDeck().getCards().add(handCard);
        cardRepo.save(handCard);
    }

    private void addPlayerBulletToDeck(User fromUser, User toUser) {
        PlayerDeck<BulletCard> bulletsDeckFromUser = fromUser.getBulletsDeck();
        PlayerDeck<HandCard> handDeckToUser = toUser.getHandDeck();
        int id = bulletsDeckFromUser.getCards().size() - 1;

        //take a bullet from the shooter to the shot user
        BulletCard bulletCard = (BulletCard) bulletsDeckFromUser.getCards().get(id);
        bulletsDeckFromUser.getCards().remove(id);
        deckRepo.save(bulletsDeckFromUser);

        bulletCard.setDeck(handDeckToUser);
        handDeckToUser.getCards().add(bulletCard);
        cardRepo.save(bulletCard);

        relevelHandDeck(toUser);
    }

    private void relocatePlayer(User user, WagonLevel wagonLevelNew) {
        WagonLevel wagonLevelOld = user.getWagonLevel();
        wagonLevelOld.getUsers().remove(user);
        wagonLevelRepo.save(wagonLevelOld);
        user.setWagonLevel(wagonLevelNew);
        wagonLevelNew.getUsers().add(user);
        wagonLevelRepo.save(wagonLevelNew);
        userRepo.save(user);
    }
}
