package ch.uzh.ifi.seal.soprafs16.service;

import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ActionResponse;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ActionResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;

/**
 * Created by Nico on 12.04.2016.
 */
@Service
@Transactional
public class ActionResponseService {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private WagonLevelRepository wagonLevelRepo;
    @Autowired
    private CardRepository cardRepo;
    @Autowired
    private DeckRepository deckRepo;
    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private ActionResponseFactory actionResponseFactory;

    public void processResponse(ActionResponseDTO responseDTO) {
        GameDTO game = gameRepo.findOne(responseDTO.getSpielId());
        ActionResponse response = actionResponseFactory.createActionResponse(responseDTO);
        response.executeAction();
        checkMarshal(game);
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
}
