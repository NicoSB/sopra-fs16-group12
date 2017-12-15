package ch.uzh.ifi.seal.soprafs16.service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.Marshal;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.CollectItemResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.DrawCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.MoveMarshalResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.MoveResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PunchResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ShootResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Cheyenne;
import ch.uzh.ifi.seal.soprafs16.model.characters.Ghost;
import ch.uzh.ifi.seal.soprafs16.model.characters.Tuco;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.TunnelTurn;

/**
 * Created by Nico on 12.04.2016.
 */
@Service
@Transactional
public class ActionResponseService {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private WagonLevelRepository wagonLevelRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private CardRepository cardRepo;
    @Autowired
    private DeckRepository deckRepo;
    @Autowired
    private MarshalRepository marshalRepo;

    public boolean processResponse(ActionResponseDTO ar) {
        if (ar instanceof DrawCardResponseDTO) {
            return processResponse((DrawCardResponseDTO) ar);
        } else if (ar instanceof PlayCardResponseDTO) {
            processResponse((PlayCardResponseDTO) ar);
        } else if (ar instanceof MoveResponseDTO) {
            processResponse((MoveResponseDTO) ar);
        } else if (ar instanceof CollectItemResponseDTO) {
            processResponse((CollectItemResponseDTO) ar);
        } else if (ar instanceof PunchResponseDTO) {
            processResponse((PunchResponseDTO) ar);
        } else if (ar instanceof ShootResponseDTO) {
            processResponse((ShootResponseDTO) ar);
        } else if (ar instanceof MoveMarshalResponseDTO) {
            processResponse((MoveMarshalResponseDTO) ar);
        }
        return true;
    }

    public boolean processResponse(DrawCardResponseDTO dcr) {
        User user = userRepo.findOne(dcr.getUserId());
        GameDTO game = gameRepo.findOne(dcr.getSpielId());
        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne(user.getHiddenDeck().getId());
        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>) deckRepo.findOne(user.getHandDeck().getId());
        if(hiddenDeck.size() > 0) {
            for (int i = 0; i < 3 && hiddenDeck.size() > 0; i++) {
                HandCard hc = (HandCard) hiddenDeck.get((int) (Math.random() * hiddenDeck.size()));
                hc = (HandCard) cardRepo.findOne(hc.getId());
                hiddenDeck.getCards().remove(hc);
                hiddenDeck = deckRepo.save(hiddenDeck);
                handDeck.getCards().add(hc);
                hc.setDeck(handDeck);

                cardRepo.save(hc);
                deckRepo.save(handDeck);
            }
            return true;
        }
        else{
            return false;
        }
    }

    public void processResponse(PlayCardResponseDTO pcr) {
        GameDTO game = gameRepo.findOne(pcr.getSpielId());
        User user = userRepo.findOne(pcr.getUserId());

        ActionCard ac = (ActionCard) cardRepo.findOne(pcr.getPlayedCardId());
        PlayerDeck<HandCard> handDeck = user.getHandDeck();
        handDeck.removeById(ac.getId());

        GameDeck<ActionCard> commonDeck = game.getCommonDeck();
        commonDeck.add(ac);
        ac.setDeck(commonDeck);
        ac.setPos(game.getActionRequestCounter());
        ac.setPlayedByUserId(user.getId());
        ac.setPlayedHidden(false);

        // TunnelTurn and Ghost character skill
        if(game.getCurrentTurnType() instanceof TunnelTurn ||
                user.getCharacter() instanceof Ghost && game.getCurrentTurn() == 0){
            ac.setPlayedHidden(true);
        }

        cardRepo.save(ac);
        deckRepo.save(handDeck);
        deckRepo.save(commonDeck);
    }

    public void processResponse(MoveResponseDTO mr) {
        User user = userRepo.findOne(mr.getUserId());

        WagonLevel newWl = wagonLevelRepo.findOne(mr.getWagonLevelId());
        WagonLevel oldWl = wagonLevelRepo.findOne(user.getWagonLevel().getId());

        oldWl.removeUserById(user.getId());
        wagonLevelRepo.save(oldWl);

        newWl.getUsers().add(user);

        user.setWagonLevel(newWl);

        wagonLevelRepo.save(newWl);
        userRepo.save(user);
        if(newWl.getLevelType() == LevelType.BOTTOM) checkMarshal(user.getGame());
    }

    public void processResponse(CollectItemResponseDTO cir) {
        User user = userRepo.findOne(cir.getUserId());

        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
        Item item = getRandomItem(cir.getCollectedItemType(), wl);
        if (item != null) {
            item = itemRepo.findOne(item.getId());
            wl.removeItemById(item.getId());

            item.setWagonLevel(null);
            item.setUser(user);
            user.getItems().add(item);

            itemRepo.save(item);
            wagonLevelRepo.save(wl);
            userRepo.save(user);
        }
    }

    public void processResponse(PunchResponseDTO pr) {
        User user = userRepo.findOne(pr.getUserId());
        GameDTO game = gameRepo.findOne(pr.getSpielId());

        User victim = userRepo.findOne(pr.getVictimId());
        WagonLevel moveWl = wagonLevelRepo.findOne(pr.getWagonLevelId());
        WagonLevel dropWl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
        Item item = getRandomItem(pr.getItemType(), victim);

        if (item != null) {
            item = itemRepo.findOne(item.getId());
            // Drop Item
            victim.getItems().remove(item);
            item.setUser(null);
            victim = userRepo.save(victim);
            item = itemRepo.save(item);
            // Cheyenne Character Skill
            if (user.getCharacter().getClass().equals(Cheyenne.class) && item.getItemType() == ItemType.BAG) {
                item.setUser(user);
                item = itemRepo.save(item);
                user.getItems().add(item);
                user = userRepo.save(user);
            } else {
                item.setUser(null);
                item.setWagonLevel(dropWl);
                item = itemRepo.save(item);
                dropWl.getItems().add(item);
                dropWl = wagonLevelRepo.save(dropWl);
            }
        }

        // Move user
        dropWl.getUsers().remove(victim);

        victim.setWagonLevel(moveWl);
        Hibernate.initialize(moveWl.getUsers());
        moveWl.getUsers().add(victim);

        game = gameRepo.findOne(game.getId());

        userRepo.save(user);
        userRepo.save(victim);
        wagonLevelRepo.save(dropWl);
        wagonLevelRepo.save(moveWl);

        if(moveWl.getLevelType() == LevelType.BOTTOM) checkMarshal(user.getGame());
    }

    public void processResponse(ShootResponseDTO sr) {
        User user = userRepo.findOne(sr.getUserId());

        if (user.getBulletsDeck().size() > 0) {
            User victim = userRepo.findOne(sr.getVictimId());
            PlayerDeck<BulletCard> bulletCardDeck = user.getBulletsDeck();
            PlayerDeck<HandCard> hiddenDeck = victim.getHiddenDeck();
            BulletCard bc = (BulletCard) bulletCardDeck.remove(user.getBulletsDeck().size() - 1);
            hiddenDeck.add(bc);

            bc.setDeck(hiddenDeck);

            // Character Skill Tuco
            if (user.getCharacter().getClass().equals(Tuco.class)) {
                WagonLevel wl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
                WagonLevel wlNew = null;
                if (user.getWagonLevel().getWagon().getId() < victim.getWagonLevel().getWagon().getId() && wl.getWagonLevelAfter() != null) {
                    wlNew = wagonLevelRepo.findOne(wl.getWagonLevelAfter().getId());
                } else if(user.getWagonLevel().getWagon().getId() > victim.getWagonLevel().getWagon().getId() && wl.getWagonLevelBefore() != null) {
                    wlNew = wagonLevelRepo.findOne(wl.getWagonLevelBefore().getId());
                }

                if(wlNew != null) {
                    wl.removeUserById(victim.getId());
                    wlNew.getUsers().add(victim);
                    victim.setWagonLevel(wlNew);

                    wagonLevelRepo.save(wl);
                    wagonLevelRepo.save(wlNew);

                    if (wlNew.getLevelType() == LevelType.BOTTOM) checkMarshal(user.getGame());
                }
            }
            deckRepo.save(bulletCardDeck);
            deckRepo.save(hiddenDeck);
            userRepo.save(victim);
            userRepo.save(user);
            cardRepo.save(bc);
        }
    }

    public void processResponse(MoveMarshalResponseDTO mmr) {
        GameDTO game = gameRepo.findOne(mmr.getSpielId());

        Marshal marshal = marshalRepo.findOne(game.getMarshal().getId());
        WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());
        WagonLevel newWl = wagonLevelRepo.findOne(mmr.getWagonLevelId());

        wl.setMarshal(null);
        newWl.setMarshal(marshal);
        marshal.setWagonLevel(newWl);

        wagonLevelRepo.save(wl);
        wagonLevelRepo.save(newWl);
        marshalRepo.save(marshal);

        checkMarshal(game);
    }

    public void changeLevel(User user) {
        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
        WagonLevel newWl = null;
        if (wl.getLevelType() == LevelType.TOP) {
            newWl = wagonLevelRepo.findOne(user.getWagonLevel().getWagon().getBottomLevel().getId());
        } else if (wl.getLevelType() == LevelType.BOTTOM) {
            newWl = wagonLevelRepo.findOne(user.getWagonLevel().getWagon().getTopLevel().getId());
        }
        if(newWl != null) {
            wl.removeUserById(user.getId());
            wagonLevelRepo.save(wl);

            user.setWagonLevel(newWl);
            userRepo.save(user);

            newWl.getUsers().add(user);
            newWl = wagonLevelRepo.save(newWl);

            if (newWl.getLevelType() == LevelType.BOTTOM) {
                checkMarshal(user.getGame());
            }
        }
    }

    private Item getRandomItem(ItemType type, User user) {
        if (type != ItemType.BAG) {
            for (int i = 0; i < user.getItems().size(); i++) {
                if (user.getItems().get(i).getItemType() == type) {
                    return user.getItems().get(i);
                }
            }
        } else {
            List<Item> bags = new ArrayList<>();
            for (int i = 0; i < user.getItems().size(); i++) {
                if (user.getItems().get(i).getItemType() == ItemType.BAG) {
                    bags.add(user.getItems().get(i));
                }
            }
            return bags.get((int) (Math.random() * bags.size()));
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
            return bags.size() > 0 ? bags.get((int) (Math.random() * bags.size())) : null;
        }
        return null;
    }

    public void checkMarshal(GameDTO game) {
        // If users are in the same wagonLevel as the marshal
        WagonLevel marshalWl = wagonLevelRepo.findOne(game.getMarshal().getWagonLevel().getId());
        if (!marshalWl.getUsers().isEmpty()) {
            GameDeck<BulletCard> neutralBulletsDeck = (GameDeck<BulletCard>) deckRepo.findOne(game.getNeutralBulletsDeck().getId());
            while(!marshalWl.getUsers().isEmpty()){
                User u = marshalWl.getUsers().get(0);
                u = userRepo.findOne(u.getId());
                PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(u.getHiddenDeck().getId());
                BulletCard bc = (BulletCard)neutralBulletsDeck.remove(0);
                bc.setDeck(hiddenDeck);
                deckRepo.save(neutralBulletsDeck);
                hiddenDeck.add(bc);

                cardRepo.save(bc);
                deckRepo.save(hiddenDeck);
                userRepo.save(u);
                wagonLevelRepo.save(marshalWl);

                changeLevel(u);
            }
        }
    }
}
