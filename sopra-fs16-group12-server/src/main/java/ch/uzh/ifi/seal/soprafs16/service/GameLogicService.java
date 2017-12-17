package ch.uzh.ifi.seal.soprafs16.service;

import ch.uzh.ifi.seal.soprafs16.model.*;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.uzh.ifi.seal.soprafs16.GameConstants;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.controller.GenericService;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.DrawOrPlayCardRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
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
import ch.uzh.ifi.seal.soprafs16.model.characters.Doc;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ActionRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.ReverseTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.SpeedupTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

@Service
@Transactional
public class GameLogicService extends GenericService {

    Logger logger = LoggerFactory.getLogger(GameLogicService.class);

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
    private ActionRepository actionRepo;
    @Autowired
    private GameCacherService gameCacherService;
    @Autowired
    private RoundEndActionHelper roundEndActionHelper;

    public void updateGame(Long id) {
        GameDTO game = gameRepo.findOne(id);

        Hibernate.initialize(game.getUsers());
        setNextPhase(game);

        if (game.getStatus() == GameStatus.FINISHED)
            return;

        if (game.getCurrentPhase() == PhaseType.PLANNING) {
            processPlayerTurn(game);
        } else if (game.getCurrentPhase() == PhaseType.EXECUTION) {
            processCommonDeck(game);
        }
    }

    private void setNextPhase(GameDTO game) {
        if (isExecutionPhaseOver(game)) {
            game.setCurrentRound(game.getCurrentRound() + 1);

            if (isGameOver(game)) {
                finishGame(game);
            } else {
                game.setCurrentPhase(PhaseType.PLANNING);
                endRound(game);
            }
        } else if (isPlanningPhaseOver(game)) {
            game.setCurrentPhase(PhaseType.EXECUTION);
        }

        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }

    private boolean isExecutionPhaseOver(GameDTO game) {
        return game.getCurrentPhase() == PhaseType.EXECUTION && game.getCommonDeck().size() == 0;
    }

    private boolean isGameOver(GameDTO game) {
        return game.getCurrentRound().equals(GameConstants.ROUNDS);
    }

    private void finishGame(GameDTO game) {
        executeRoundEndAction(game);
        evaluateGunslingerBonus(game);

        game.setStatus(GameStatus.FINISHED);
        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }

    private void executeRoundEndAction(GameDTO game) {
        RoundCard rc = (RoundCard) cardRepo.findOne(game.getRoundCardDeck().get(game.getCurrentRound() - 1).getId());
        roundEndActionHelper.execute(rc, game.getId());
    }

    private void evaluateGunslingerBonus(GameDTO game) {
        List<User> gunslingers = getGunslingers(game);
        addGunslingerBoni(gunslingers);
    }

    private List<User> getGunslingers(GameDTO game) {
        List<User> gunslingers = new ArrayList<>();
        gunslingers.add(game.getUsers().get(0));

        for (int i = 1; i < game.getUsers().size(); i++) {
            User u = game.getUsers().get(i);
            if (u.getBulletsDeck().size() < gunslingers.get(0).getBulletsDeck().size()) {
                gunslingers.clear();
            }
            if (u.getBulletsDeck().size() <= gunslingers.get(0).getBulletsDeck().size()) {
                gunslingers.add(u);
            }
        }

        return gunslingers;
    }

    private void addGunslingerBoni(List<User> gunslingers) {
        for (User u : gunslingers) {
            Item bag = new Item();
            bag.setItemType(ItemType.BAG);
            bag.setUser(u);
            bag.setValue(1000);
            bag.setWagonLevel(null);
            itemRepo.save(bag);

            User user = userRepo.findOne(u.getId());
            user.getItems().add(bag);
            userRepo.save(user);
        }
    }

    private void endRound(GameDTO game) {
        System.out.println("round " + game.getCurrentRound() + " end");

        executeRoundEndAction(game);
        resetPlayerDecks(game);
        initNextRound(game);
    }

    private boolean isPlanningPhaseOver(GameDTO game) {
        return game.getCurrentPhase() == PhaseType.PLANNING
                && game.getActionRequestCounter() == calculateNumberOfRequiredRequests(game);
    }

    private int calculateNumberOfRequiredRequests(GameDTO game) {
        int sum = 0;
        RoundCard r = (RoundCard) game.getRoundCardDeck().get(game.getCurrentRound());
        for (Turn t : r.getPattern()) {
            sum += game.getUsers().size();
            if (t instanceof SpeedupTurn) {
                sum += game.getUsers().size();
            }
        }

        return sum;
    }

    private void setCurrentPlayer(GameDTO game, long id) {
        User player = userRepo.findOne(id);

        int index = game.getUsers().indexOf(player);
        game.setCurrentPlayerIndex(index);
    }

    private void resetPlayerDecks(GameDTO game) {
        Hibernate.initialize(game.getUsers());
        List<User> users = game.getUsers();
        for (User u : users) {
            resetHandDeck(u);
        }
    }
    private void resetHandDeck(User u) {
        u = userRepo.findOne(u.getId());
        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>) deckRepo.findOne(u.getHandDeck().getId());
        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne(u.getHiddenDeck().getId());

        moveAllCards(handDeck, hiddenDeck);

        int numberOfCards = u.getCharacter() instanceof Doc ? 7 : 6;
        for (int i = 0; i < numberOfCards; i++) {
            moveRandomCard(hiddenDeck, handDeck);
        }

        deckRepo.save(handDeck);
        deckRepo.save(hiddenDeck);
    }

    private void moveAllCards(PlayerDeck<HandCard> sourceDeck, PlayerDeck<HandCard> targetDeck) {
        while (sourceDeck.size() > 0) {
            HandCard hc = (HandCard) sourceDeck.remove(0);
            hc = (HandCard) cardRepo.findOne(hc.getId());
            hc.setDeck(targetDeck);
            targetDeck.add(hc);

            cardRepo.save(hc);
        }
    }

    private void moveRandomCard(PlayerDeck<HandCard> sourceDeck, PlayerDeck<HandCard> targetDeck) {
        HandCard hc = (HandCard) sourceDeck.remove((int) (Math.random() * sourceDeck.size()));
        hc = (HandCard) cardRepo.findOne(hc.getId());
        hc.setDeck(targetDeck);
        targetDeck.add(hc);

        cardRepo.save(hc);
    }

    private void initNextRound(GameDTO game) {
        game.setRoundStarter((game.getRoundStarter() + 1) % game.getUsers().size());
        game.setCurrentPlayerIndex(game.getRoundStarter());
        game.setCurrentTurn(0);
        game.setCurrentPhase(PhaseType.PLANNING);
        game.setActionRequestCounter(0);
    }

    private void processPlayerTurn(GameDTO game) {
        int size = game.getUsers().size();

        if (game.getActionRequestCounter() > 0) {
            setNextTurn(game, size);
            setNextPlayer(game, size);
        }

        issueDrawOrPlayRequest(game);
    }

    private void setNextTurn(GameDTO game, int playerCounter) {
        logger.debug("setNextTurn");

        if (isTurnOver(game, playerCounter)) {
            game.setCurrentTurn(game.getCurrentTurn() + 1);

            // This is a quick fix which allows the program in case of a ReverseTurn to continue normally.
            if (isEndOfReverseTurn(game)) {
                game.setCurrentPlayerIndex((game.getRoundStarter() + 1) % playerCounter);
            }
        }
        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }


    private boolean isTurnOver(GameDTO game, int playerCounter) {
        return (!(game.getCurrentTurnType() instanceof SpeedupTurn) && game.getActionRequestCounter() % playerCounter == 0)
                || (game.getCurrentTurnType() instanceof SpeedupTurn && game.getActionRequestCounter() == (game.getCurrentTurn() + 2) * playerCounter);
    }

    private boolean isEndOfReverseTurn(GameDTO game) {
        return game.getCurrentTurn() < ((RoundCard) (game.getRoundCardDeck().get(
                game.getCurrentRound()))).getPattern().size()
                && game.getCurrentTurnType() instanceof ReverseTurn;
    }

    private void setNextPlayer(GameDTO game, int playerCounter) {
        Turn t = game.getCurrentTurnType();

        if (isNextPlayersTurn(game, t)) {
            int step = (t instanceof ReverseTurn) ? - 1 : 1;
            game.setCurrentPlayerIndex(calculatePositiveRemainder(game.getCurrentPlayerIndex() + step, playerCounter));
        }

        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }

    private boolean isNextPlayersTurn(GameDTO game, Turn t) {
        return !(t instanceof SpeedupTurn) || game.getActionRequestCounter() % 2 == 0;
    }

    private int calculatePositiveRemainder(int a, int b) {
        int ret = a % b;
        return ret < 0 ? b + ret : ret;
    }

    private void issueDrawOrPlayRequest(GameDTO game) {
        User currentPlayer = game.getUsers().get(game.getCurrentPlayerIndex());
        DrawOrPlayCardRequestDTO drawOrPlayRequest = new DrawOrPlayCardRequestDTO();

        List<HandCard> handCards = (List<HandCard>) currentPlayer.getHandDeck().getCards();
        List<Long> playableCards = handCards
                .stream()
                .filter((card) -> !(card instanceof BulletCard))
                .map(HandCard::getId)
                .collect(Collectors.toList());

        drawOrPlayRequest.setPlayableCardsId(playableCards);
        drawOrPlayRequest.setSpielId(game.getId());
        drawOrPlayRequest.setUserId(currentPlayer.getId());
        drawOrPlayRequest.setGame(game);

        game.getActions().add(drawOrPlayRequest);
        game.setActionRequestCounter(game.getActionRequestCounter() + 1);

        actionRepo.save(drawOrPlayRequest);
        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }


    private void processCommonDeck(GameDTO game) {
        GameDeck<ActionCard> commonDeck = (GameDeck<ActionCard>) deckRepo.findOne(game.getCommonDeck().getId());

        if (commonDeck.isEmpty()) {
            updateGame(game.getId());
        } else {
            ActionCard actionCard = (ActionCard) cardRepo.findOne(commonDeck.remove(0).getId());
            User user = userRepo.findOne(actionCard.getPlayedByUserId());

            returnCardToHiddenDeck(actionCard, user, commonDeck);
            requestNextMove(game, actionCard, user);
        }

        gameRepo.save(game);
        gameCacherService.saveGame(game);
    }

    private void returnCardToHiddenDeck(ActionCard actionCard, User user, GameDeck<ActionCard> commonDeck) {
        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne(user.getHiddenDeck().getId());
        actionCard.setPlayedByUserId(user.getId());
        actionCard.setDeck(hiddenDeck);
        hiddenDeck.getCards().add(actionCard);

        deckRepo.save(hiddenDeck);
        deckRepo.save(commonDeck);
        cardRepo.save(actionCard);
    }

    private void requestNextMove(GameDTO game, ActionCard actionCard, User user) {
        setCurrentPlayer(game, actionCard.getPlayedByUserId());

        gameRepo.save(game);
        gameCacherService.saveGame(game);

        ActionRequestDTO request = actionCard.createActionRequest(game, user);

        if (request == null) {
            updateGame(game.getId());
        } else {
            request = actionRepo.save(request);
            game.getActions().add(request);

            gameRepo.save(game);
            gameCacherService.saveGame(game);
        }
    }
}


