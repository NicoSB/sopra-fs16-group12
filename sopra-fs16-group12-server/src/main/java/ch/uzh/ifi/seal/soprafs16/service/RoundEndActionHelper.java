package ch.uzh.ifi.seal.soprafs16.service;


import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.*;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.*;
import ch.uzh.ifi.seal.soprafs16.model.repositories.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
// TODO: refactor with Design Pattern
class RoundEndActionHelper {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GameRepository gameRepo;
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
    private ActionResponseService actionResponseService;
    @Autowired
    private GameCacherService gameCacherService;

    public void execute(RoundCard rc, Long gameId) {
        if (rc instanceof AngryMarshalCard) {
            execute((AngryMarshalCard) rc, gameId);
        } else if (rc instanceof BrakingCard) {
            execute((BrakingCard) rc, gameId);
        } else if (rc instanceof GetItAllCard) {
            execute((GetItAllCard) rc, gameId);
        } else if (rc instanceof HostageCard) {
            execute((HostageCard) rc, gameId);
        } else if (rc instanceof MarshallsRevengeCard) {
            execute((MarshallsRevengeCard) rc, gameId);
        } else if (rc instanceof PassengerRebellionCard) {
            execute((PassengerRebellionCard) rc, gameId);
        } else if (rc instanceof PickPocketingCard) {
            execute((PickPocketingCard) rc, gameId);
        } else if (rc instanceof PivotablePoleCard) {
            execute((PivotablePoleCard) rc, gameId);
        }

        GameDTO game = gameRepo.findOne(gameId);
        gameCacherService.saveGame(game);
    }

    private void execute(AngryMarshalCard amc, Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);
        Marshal marshal = marshalRepo.findOne(game.getMarshal().getId());
        WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());
        WagonLevel wlTop = wagonLevelRepo.findOne(wl.getWagon().getTopLevel().getId());
        GameDeck<BulletCard> neutralBulletsDeck = (GameDeck<BulletCard>) deckRepo.findOne(game.getNeutralBulletsDeck().getId());

        Hibernate.initialize(wlTop.getUsers());
        for (User u : wlTop.getUsers()) {
            if (game.getNeutralBulletsDeck().size() > 0) {
                BulletCard bc = (BulletCard) neutralBulletsDeck.remove(0);
                PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne((u.getHiddenDeck().getId()));
                bc.setDeck(hiddenDeck);
                hiddenDeck.add(bc);
                cardRepo.save(bc);
                deckRepo.save(hiddenDeck);
            }
        }

        deckRepo.save(neutralBulletsDeck);

        if (wl.getWagonLevelAfter() != null) {
            WagonLevel wlAfter = wagonLevelRepo.findOne(wl.getWagonLevelAfter().getId());
            wl.setMarshal(null);
            marshal.setWagonLevel(wlAfter);
            wlAfter.setMarshal(marshal);

            actionResponseService.checkMarshal(game);
            marshalRepo.save(marshal);
            actionResponseService.checkMarshal(game);
            wagonLevelRepo.save(wl);
            wagonLevelRepo.save(wlAfter);
        }
    }

    private void execute(BrakingCard bc, Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);
        for (User u : game.getUsers()) {
            User user = userRepo.findOne(u.getId());
            if (user.getWagonLevel().getLevelType() == LevelType.TOP
                    && user.getWagonLevel().getWagon().getId() != game.getWagons().get(0).getId()) {
                WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
                WagonLevel before = wagonLevelRepo.findOne(wl.getWagonLevelBefore().getId());

                user.setWagonLevel(before);
                wl.removeUserById(user.getId());
                before.getUsers().add(user);

                userRepo.save(user);
                wagonLevelRepo.save(wl);
                wagonLevelRepo.save(before);
            }
        }
    }

    private void execute(GetItAllCard giac, Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);
        Marshal marshal = game.getMarshal();
        WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());

        Item moneyCase = new Item();
        moneyCase.setUser(null);
        moneyCase.setWagonLevel(wl);
        moneyCase.setItemType(ItemType.CASE);
        moneyCase.setValue(1000);

        wl.getItems().add(moneyCase);

        itemRepo.save(moneyCase);
        wagonLevelRepo.save(wl);
    }

    private void execute(HostageCard hc, Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);
        WagonLevel locTop = wagonLevelRepo.findOne(game.getWagons().get(0).getTopLevel().getId());
        WagonLevel locBottom = wagonLevelRepo.findOne(game.getWagons().get(0).getBottomLevel().getId());

        for (User u : locTop.getUsers()) {
            Item bag = new Item();
            bag.setValue(250);
            bag.setUser(u);
            bag.setWagonLevel(null);
            bag.setItemType(ItemType.BAG);

            itemRepo.save(bag);
            u.getItems().add(bag);
            userRepo.save(u);
        }

        for (User u : locBottom.getUsers()) {
            Item bag = new Item();
            bag.setValue(250);
            bag.setUser(u);
            bag.setWagonLevel(null);
            bag.setItemType(ItemType.BAG);

            itemRepo.save(bag);
            u.getItems().add(bag);
            userRepo.save(u);
        }
    }

    private void execute(MarshallsRevengeCard mrc, Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);
        WagonLevel wl = wagonLevelRepo.findOne(game.getMarshal().getWagonLevel().getWagon().getTopLevel().getId());

        for (User u : wl.getUsers()) {
            Item item = getMinPurse(u);

            if (item != null) {
                u = userRepo.findOne(u.getId());
                item = itemRepo.findOne(item.getId());
                u.removeItemById(item.getId());
                item.setUser(null);
                item.setWagonLevel(wl);
                wl.getItems().add(item);

                itemRepo.save(item);
                userRepo.save(u);
                wagonLevelRepo.save(wl);
            }
        }
    }

    private void execute(PassengerRebellionCard prc, Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);
        for (User u : game.getUsers()) {
            if (u.getWagonLevel().getLevelType() == LevelType.BOTTOM) {
                if (game.getNeutralBulletsDeck().size() > 0) {
                    u = userRepo.findOne(u.getId());
                    GameDeck<BulletCard> neutralBulletsDeck = (GameDeck<BulletCard>) deckRepo.findOne(game.getNeutralBulletsDeck().getId());
                    PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne(u.getHiddenDeck().getId());
                    BulletCard bc = (BulletCard) cardRepo.findOne(neutralBulletsDeck.remove(0).getId());

                    bc.setDeck(hiddenDeck);
                    hiddenDeck.add(bc);

                    cardRepo.save(bc);
                    deckRepo.save(neutralBulletsDeck);
                    deckRepo.save(hiddenDeck);
                }
            }
        }
    }

    private void execute(PickPocketingCard ppc, Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);

        for (User u : game.getUsers()) {
            if (u.getWagonLevel().getUsers().size() == 1) {
                Item item = getRandomItem(ItemType.BAG, u.getWagonLevel());
                if (item != null) {
                    u = userRepo.findOne(u.getId());
                    item = itemRepo.findOne(item.getId());
                    WagonLevel wl = wagonLevelRepo.findOne(u.getWagonLevel().getId());

                    u.getItems().add(item);
                    wl.removeItemById(item.getId());
                    item.setWagonLevel(null);
                    item.setUser(u);

                    userRepo.save(u);
                    itemRepo.save(item);
                    wagonLevelRepo.save(wl);
                }
            }
        }
    }

    private void execute(PivotablePoleCard ppc, Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);

        WagonLevel caboose = wagonLevelRepo.findOne(game.getWagons().get(game.getWagons().size() - 1).getTopLevel().getId());
        for (User u : game.getUsers()) {
            if (u.getWagonLevel().getLevelType() == LevelType.TOP) {
                u = userRepo.findOne(u.getId());
                WagonLevel wl = wagonLevelRepo.findOne(u.getWagonLevel().getId());
                u.setWagonLevel(caboose);
                wl.removeUserById(u.getId());
                caboose.getUsers().add(u);

                userRepo.save(u);
                wagonLevelRepo.save(wl);
            }
        }
        wagonLevelRepo.save(caboose);
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

    private Item getRandomItem(ItemType type, WagonLevel wagonLevel) {
        if (type != ItemType.BAG) {
            for (int i = 0; i < wagonLevel.getItems().size(); i++) {
                if (wagonLevel.getItems().get(i).getItemType() == type) {
                    return wagonLevel.getItems().get(i);
                }
            }
        } else {
            List<Item> bags = new ArrayList<>();
            for (int i = 0; i < wagonLevel.getItems().size(); i++) {
                if (wagonLevel.getItems().get(i).getItemType() == ItemType.BAG) {
                    bags.add(wagonLevel.getItems().get(i));
                }
            }
            return !bags.isEmpty() ? bags.get((int) (Math.random() * bags.size())) : null;
        }
        return null;
    }
}