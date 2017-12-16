package ch.uzh.ifi.seal.soprafs16.service.GameService;

import ch.uzh.ifi.seal.soprafs16.model.*;
import ch.uzh.ifi.seal.soprafs16.service.GameCacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.cards.Deck;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
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
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

/**
 * Created by Christoph on 06/04/16.
 */


@Service("gameService")
public class GameService {

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
    private GameCacherService gameCacherService;

    @Autowired
    @Qualifier("gameInitializer")
    private GameInitializer gameInitializer;

    @Autowired
    @Qualifier("demoGameInitializer")
    private GameInitializer demoGameInitializer;

    @Autowired
    public GameService(GameInitializer gameInitializer) {
        this.gameInitializer = gameInitializer;
    }

    Logger logger = LoggerFactory.getLogger(GameService.class);

    /**
     * @param gameId
     * @return
     */
    public Long startGame(Long gameId) {
        try {
            GameDTO game = gameRepo.findOne(gameId);
            gameInitializer.startGame(game);

            return game.getId();
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            return (long) -1;
        }
    }

    public boolean removeUser(User user, GameDTO game) {
        try {
            boolean successful = true;
            user.setGame(null);
            game.getUsers().remove(user);
            if (user.getCharacter() != null) {
                ch.uzh.ifi.seal.soprafs16.model.characters.Character oldChar = user.getCharacter();
                user.setCharacter(null);
                oldChar.setUser(null);
                characterRepo.delete(oldChar);
            }
            if (user.getItems() != null) {
                int itemCounter = user.getItems().size();
                //with standard foreach loop exception is thrown -> user.getItems can be empty although we are already in the list
                for (int i = itemCounter - 1; i >= 0; i--) {
                    successful = deleteItem(user.getItems().get(i)) && successful;
                }
                userRepo.save(user);
                user.setItems(null);
                userRepo.save(user);
            }
            if (user.getWagonLevel() != null) {
                WagonLevel wagonLevel = user.getWagonLevel();
                wagonLevel.getUsers().remove(user);
                user.setWagonLevel(null);
                wagonLevelRepo.save(wagonLevel);
                userRepo.save(user);
            }
            if (user.getBulletsDeck() != null) {
                PlayerDeck<BulletCard> bulletCardDeck = user.getBulletsDeck();
                int cardCounter = bulletCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) bulletCardDeck.getCards().get(i));
                }
                bulletCardDeck.setCards(null);
                bulletCardDeck.setUser(null);
                user.setBulletsDeck(null);
                deckRepo.delete(bulletCardDeck);
                userRepo.save(user);
            }
            if (user.getHandDeck() != null) {
                PlayerDeck<HandCard> handCardDeck = user.getHandDeck();
                int cardCounter = handCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) handCardDeck.getCards().get(i));
                }
                handCardDeck.setCards(null);
                handCardDeck.setUser(null);
                user.setHandDeck(null);
                deckRepo.delete(handCardDeck);
                userRepo.save(user);
            }
            if (user.getHiddenDeck() != null) {
                PlayerDeck<HandCard> hiddenCardDeck = user.getHiddenDeck();
                int cardCounter = hiddenCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) hiddenCardDeck.getCards().get(i));
                }
                hiddenCardDeck.setCards(null);
                hiddenCardDeck.setUser(null);
                user.setHiddenDeck(null);
                deckRepo.delete(hiddenCardDeck);
                userRepo.save(user);
            }
            gameRepo.save(game);
            gameCacherService.saveGame(game);
            userRepo.save(user);

            return successful;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
    }

    private boolean deleteItem(Item item) {
        try {
            if (item.getUser() != null) {
                User user = item.getUser();
                user.getItems().remove(item);
                item.setUser(null);
                userRepo.save(user);
            }
            if (item.getWagonLevel() != null) {
                WagonLevel wagonLevel = item.getWagonLevel();
                wagonLevel.getItems().remove(item);
                item.setWagonLevel(null);
                wagonLevelRepo.save(wagonLevel);
            }
            itemRepo.delete(item);
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
    }

    private boolean deleteCard(Card card) {
        try {
            Deck deck = card.getDeck();
            deck.getCards().remove(card);
            card.setDeck(null);
            cardRepo.delete(card);
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
    }

    public boolean deleteGame(GameDTO game) {
        try {
            boolean successful = true;
            if (game.getRoundCardDeck() != null) {
                GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
                int cardCounter = roundCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    int turnCounter = ((RoundCard) (roundCardDeck.getCards().get(i))).getPattern().size();
                    for (int tc = turnCounter - 1; tc >= 0; tc--) {
                        Turn turn = ((RoundCard) (roundCardDeck.getCards().get(i))).getPattern().get(tc);
                        ((RoundCard) (roundCardDeck.getCards().get(i))).getPattern().remove(turn);
                        turn.setRoundCard(null);
                        turnRepo.delete(turn);
                    }
                    ((RoundCard) (roundCardDeck.getCards().get(i))).setPattern(null);
                    cardRepo.save((Card) roundCardDeck.getCards().get(i));
                    successful = deleteCard((Card) roundCardDeck.getCards().get(i));
                }
                roundCardDeck.setCards(null);
                roundCardDeck.setGame(null);
                game.setRoundCardDeck(null);
                deckRepo.delete(roundCardDeck);
                gameRepo.save(game);
                gameCacherService.saveGame(game);
            }
            if (game.getNeutralBulletsDeck() != null) {
                GameDeck<BulletCard> bulletCardDeck = game.getNeutralBulletsDeck();
                int cardCounter = bulletCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) bulletCardDeck.getCards().get(i));
                }
                bulletCardDeck.setCards(null);
                bulletCardDeck.setGame(null);
                game.setNeutralBulletsDeck(null);
                deckRepo.delete(bulletCardDeck);
                gameRepo.save(game);
                gameCacherService.saveGame(game);
            }
            if (game.getCommonDeck() != null) {
                GameDeck<ActionCard> commonDeck = game.getCommonDeck();
                int cardCounter = commonDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) commonDeck.getCards().get(i));
                }
                commonDeck.setCards(null);
                commonDeck.setGame(null);
                game.setCommonDeck(null);
                deckRepo.delete(commonDeck);
                gameRepo.save(game);
                gameCacherService.saveGame(game);
            }
            if (game.getMarshal() != null) {
                Marshal marshal = game.getMarshal();
                marshal.setGame(null);
                game.setMarshal(null);
                marshalRepo.save(marshal);
                gameRepo.save(game);
                gameCacherService.saveGame(game);
            }
            if (game.getWagons() != null) {
                int wagonCounter = game.getWagons().size();
                for (int i = wagonCounter - 1; i >= 0; i--) {
                    Wagon wagon = game.getWagons().get(i);

                    WagonLevel wagonLevelTop = wagon.getTopLevel();
                    int itemCounterTop = wagonLevelTop.getItems().size();
                    for (int ict = itemCounterTop - 1; ict >= 0; ict--) {
                        successful = deleteItem(wagonLevelTop.getItems().get(ict)) && successful;
                    }
                    wagonLevelTop.setItems(null);
                    wagonLevelRepo.save(wagonLevelTop);

                    WagonLevel wagonLevelBot = wagon.getBottomLevel();
                    int itemCounterBot = wagonLevelBot.getItems().size();
                    for (int icb = itemCounterBot - 1; icb >= 0; icb--) {
                        successful = deleteItem(wagonLevelBot.getItems().get(icb)) && successful;
                    }
                    wagonLevelBot.setItems(null);
                    wagonLevelRepo.save(wagonLevelBot);

                    wagonLevelTop.setWagon(null);
                    wagon.setTopLevel(null);
                    wagonLevelRepo.save(wagonLevelTop);
                    wagonRepo.save(wagon);
                    wagonLevelBot.setWagon(null);
                    wagon.setBottomLevel(null);
                    wagonLevelRepo.save(wagonLevelBot);
                    wagonRepo.save(wagon);

                    wagon.setGame(null);
                    game.getWagons().remove(wagon);
                    wagonRepo.delete(wagon);
                }
                game.setWagons(null);
                gameRepo.save(game);
                gameCacherService.saveGame(game);
            }

            gameCacherService.deleteGame(game);
            gameRepo.delete(game);
            return successful;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
    }

    public Long startDemoGame(Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);

        demoGameInitializer.startGame(game);
        return game.getId();
    }
}