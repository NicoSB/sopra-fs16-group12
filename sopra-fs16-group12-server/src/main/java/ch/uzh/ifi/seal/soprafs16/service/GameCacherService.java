package ch.uzh.ifi.seal.soprafs16.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.seal.soprafs16.SerializedGameWrapper;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;

/**
 * Created by Christoph on 20.05.2016.
 */
@Service("gameCacherService")
public class GameCacherService {
    @Autowired
    private GameRepository gameRepo;

    Logger logger = LoggerFactory.getLogger(GameCacherService.class);

    //  private static Map<Long, GameDTO> cachedGamesMap = new HashMap<>();
    private static Map<Long, SerializedGameWrapper> cachedSerializedGamesMap = new HashMap<>();

    public void saveGame(GameDTO game) {
        Long gameId = game.getId();
        GameDTO repoGame = gameRepo.findOne(gameId);
        //initializeLazyLoading(repoGame);
        serializeGame(repoGame);
    }

//    private void initializeLazyLoading(GameDTO repoGame) {
//        Hibernate.initialize(repoGame.getUsers());
//        if (repoGame.getUsers() != null) {
//            for (User u : repoGame.getUsers()) {
//                Hibernate.initialize(u.getItems());
//                if (u.getBulletsDeck() != null) {
//                    Hibernate.initialize(u.getBulletsDeck());
//                }
//                if (u.getHiddenDeck() != null) {
//                    Hibernate.initialize(u.getHiddenDeck());
//                }
//                if (u.getHandDeck() != null) {
//                    Hibernate.initialize(u.getHandDeck());
//                }
//            }
//        }
//        Hibernate.initialize(repoGame.getWagons());
//        if (repoGame.getWagons() != null) {
//            for (Wagon w : repoGame.getWagons()) {
//                Hibernate.initialize(w.getBottomLevel().getItems());
//                Hibernate.initialize(w.getTopLevel().getItems());
//            }
//        }
//        Hibernate.initialize(repoGame.getActions()); //TODO remove actions from game
//        if (repoGame.getNeutralBulletsDeck() != null) {
//            Hibernate.initialize(repoGame.getNeutralBulletsDeck());
//        }
//        if (repoGame.getCommonDeck() != null) {
//            Hibernate.initialize(repoGame.getCommonDeck());
//        }
//        if (repoGame.getRoundCardDeck() != null) {
//            Hibernate.initialize(repoGame.getRoundCardDeck());
//        }
//    }

    public void deleteGame(GameDTO game) {
        System.out.println("GameCacherService: delete game");
        cachedSerializedGamesMap.remove(game.getId());
    }

    public String getSerializedGame(long gameId) {
        String gameSerialized = cachedSerializedGamesMap.containsKey(gameId) ? cachedSerializedGamesMap.get(gameId).getSerializedGame() : null;
        return gameSerialized;
    }

    public String getAllSerializedGames() {
        System.out.println("GameCacherService: get all games");
        boolean needsComma = false;
        //List<String> result = new ArrayList<>();
        String serializedList = "[";
        for (SerializedGameWrapper sgw : cachedSerializedGamesMap.values()) {
            //result.add(sgw.getSerializedGame());
            if(needsComma){
                serializedList += ",";
            }
            else{
                needsComma = true;
            }
            serializedList += sgw.getSerializedGame();
        }
        serializedList += "]";
        return serializedList;
    }

    public String getSerializedGamesFiltered(String statusFilter) {
        System.out.println("GameCacherService: get filtered games");
        boolean needsComma = false;
        //List<String> result = new ArrayList<>();
        String serializedList = "[";
        for (SerializedGameWrapper sgw : cachedSerializedGamesMap.values()) {
            if (sgw.getGameStatus().toString().equals(statusFilter)) {
              //  result.add(sgw.getSerializedGame());
                if(needsComma){
                    serializedList += ",";
                }
                else{
                    needsComma = true;
                }
                serializedList += sgw.getSerializedGame();
            }
        }
        serializedList += "]";
        return serializedList;
    }

    private void serializeGame(GameDTO game) {
        String gameSerialized = null;
        try {
            gameSerialized = new ObjectMapper().writeValueAsString(game);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        SerializedGameWrapper sgw = new SerializedGameWrapper();
        sgw.setGameStatus(game.getStatus());
        sgw.setSerializedGame(gameSerialized);
        cachedSerializedGamesMap.put(game.getId(), sgw);
    }


}