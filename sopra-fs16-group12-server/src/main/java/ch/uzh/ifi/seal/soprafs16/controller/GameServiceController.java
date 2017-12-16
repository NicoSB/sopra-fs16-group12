package ch.uzh.ifi.seal.soprafs16.controller;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import ch.uzh.ifi.seal.soprafs16.GameConstants;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.characters.Character;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ActionRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ActionResponseRepository;
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
import ch.uzh.ifi.seal.soprafs16.service.ActionResponseService;
import ch.uzh.ifi.seal.soprafs16.service.GameCacherService;
import ch.uzh.ifi.seal.soprafs16.service.GameLogicService;
import ch.uzh.ifi.seal.soprafs16.service.GameService.GameService;

@RestController
public class GameServiceController extends GenericService {

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
    private ActionRepository actionRepo;
    @Autowired
    private TurnRepository turnRepo;
    @Autowired
    private ActionResponseRepository actionResponseRepo;
    @Autowired
    private GameService gameService;
    @Autowired
    private ActionResponseService actionResponseService;
    @Autowired
    private GameLogicService gameLogicService;
    @Autowired
    private GameCacherService gameCacherService;

    private static final String CONTEXT = "/games";

    //games - GET
    @RequestMapping(value = CONTEXT)
    @ResponseStatus(HttpStatus.OK)
    public String listGames() {
        logger.info("listGames");
        return gameCacherService.getAllSerializedGames();
    }

    //games - GET v
    @RequestMapping(value = CONTEXT, params = {"status"})
    @ResponseStatus(HttpStatus.OK)
    public String listGamesFiltered(@RequestParam("status") String statusFilter) {
        logger.info("listGamesFiltered");
        return gameCacherService.getSerializedGamesFiltered(statusFilter);
    }

    //games - POST
    @RequestMapping(value = CONTEXT, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Long addGame(@RequestBody GameDTO game, @RequestParam("token") String userToken) {
        logger.info("addGame: " + game);

        User owner = userRepo.findByToken(userToken);

        if (owner != null) {
            if (owner.getCharacter() != null) {
                Character oldChar = owner.getCharacter();
                oldChar.setUser(null);
                owner.setCharacter(null);
                userRepo.save(owner);
                characterRepo.delete(oldChar);
            }

            game.setStatus(GameStatus.PENDING);
            owner.setGame(game);
            game.setUsers(new ArrayList<User>());
            game.getUsers().add(owner);
            game.setOwner(owner.getName());
            gameRepo.save(game);
            gameCacherService.saveGame(game);

            return game.getId();
        } else {
            return null;
        }
    }

    //games/{game-id} - GET
    @RequestMapping(value = CONTEXT + "/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public String getGame(@PathVariable Long gameId) {
        return gameCacherService.getSerializedGame(gameId);
    }

    //games/{game-id}/start - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/start", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void startGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.info("startGame: " + gameId);

        GameDTO game = gameRepo.findOne(gameId);
        User owner = userRepo.findByToken(userToken);

        if (owner != null && game != null && game.getOwner().equals(owner.getName()) && game.getStatus() != GameStatus.RUNNING) {

            long returnId = gameService.startGame(gameId);
            gameLogicService.update(gameId);
        }
    }

    //games/{game-id}/startDemo - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/startDemo", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void startDemo(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.info("startGameDemo: " + gameId);

        GameDTO game = gameRepo.findOne(gameId);
        User owner = userRepo.findByToken(userToken);

        if (owner != null && game != null && game.getOwner().equals(owner.getName()) && game.getStatus() != GameStatus.RUNNING) {
            gameService.startDemoGame(gameId);
            gameLogicService.update(gameId);
        }
    }

    //games/{game-id}/stop - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/stop", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void stopGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.info("stopGame: " + gameId);

        GameDTO game = gameRepo.findOne(gameId);
        User owner = userRepo.findByToken(userToken);

        if (owner != null && game != null && game.getOwner().equals(owner.getName())) {
            game.setStatus(GameStatus.FINISHED);
            gameRepo.save(game);
            gameCacherService.saveGame(game);
        } else {
            logger.info("stopGame: owner or game is null, gameId: " + gameId);
        }
    }

    //games/{game-id}/users - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Long addPlayer(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.info("addUser: " + userToken);

        GameDTO game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (game != null && user != null && game.getUsers().size() < GameConstants.MAX_PLAYERS && game.getStatus() == GameStatus.PENDING) {
            if (user.getCharacter() != null) {
                Character oldChar = user.getCharacter();
                oldChar.setUser(null);
                user.setCharacter(null);
                userRepo.save(user);
                characterRepo.delete(oldChar);
            }
            game.getUsers().add(user);
            user.setGame(game);
            logger.info("GameDTO: " + game.getName() + " - user added: " + user.getUsername());
            game = gameRepo.save(game);
            gameCacherService.saveGame(game);
            userRepo.save(user);
            return (long) (game.getUsers().size() - 1);
        } else {
            logger.error("Error adding user with token: " + userToken);
            return null;
        }
    }

    //games/{game-id}/users - PUT
    @RequestMapping(value = CONTEXT + "/{gameId}/users", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public User modifyUserCharacter(@PathVariable Long gameId, @RequestParam("token") String userToken, @RequestBody Character character) {
        GameDTO game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (user != null && game != null) {
            try {
                for (User u : game.getUsers()) {
                    if (u.getCharacter() != null && !u.getId().equals(user.getId())) {
                        if (u.getCharacter().getClass() == character.getClass()) {
                            //other user already chose this character
                            return null;
                        }
                    }
                }

                if (user.getCharacter() != null) {
                    ch.uzh.ifi.seal.soprafs16.model.characters.Character oldChar = user.getCharacter();
                    oldChar.setUser(null);
                    user.setCharacter(null);
                    userRepo.save(user);
                    characterRepo.delete(oldChar);
                }
                user.setCharacter(character);
                character.setUser(user);
                characterRepo.save(character);
                userRepo.save(user);
                game = gameRepo.findOne(gameId);
                gameCacherService.saveGame(game);
                return user;
            } catch (IllegalArgumentException iae) {
                logger.error(iae.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    //games/{game-id}/users - DELETE
    @RequestMapping(value = CONTEXT + "/{gameId}/users", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public Long removePlayer(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        GameDTO game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (game != null && user != null) {
            if (game.getUsers().size() > 1) {
                if (game.getOwner().equals(user.getName())) {
                    if (game.getUsers().get(0).getName().equals(game.getOwner())) {
                        game.setOwner(game.getUsers().get(1).getName());
                    } else {
                        game.setOwner(game.getUsers().get(0).getName());
                    }
                }
                gameRepo.save(game);
                gameCacherService.saveGame(game);

                gameService.removeUser(user, game);

            } else {
                gameService.removeUser(user, game);
                gameService.deleteGame(game);
            }
            return gameId;
        } else {
            logger.error("Error removing user with token: " + userToken);
            return null;
        }
    }

    //games/{game-id}/actions - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/actions", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Long processResponse(@PathVariable Long gameId, @RequestParam("token") String userToken, @RequestBody ActionResponseDTO actionResponseDTO) {
        try {
            GameDTO game = gameRepo.findOne(gameId);
            if (!userToken.equals(game.getUsers().get(game.getCurrentPlayer()).getToken())) {
                logger.error("Authentication error with token: " + userToken);
                return (long) -1;
            }
            if (actionResponseDTO != null) {
                actionResponseDTO = actionResponseRepo.save(actionResponseDTO);
                actionResponseService.processResponse(actionResponseDTO);
                gameLogicService.update(gameId);
                return gameId;
            } else {
                logger.error("Actionresponse is null");
                return (long) -1;
            }

        } catch (Exception ex) {
            logger.error("Error adding Actionresponse");
            logger.error(ex.getMessage());
            ex.printStackTrace();
            return (long) -1;
        }
    }

    //games/{gameId}/actions - GET
    @RequestMapping(value = CONTEXT + "/{gameId}/actions")
    @ResponseStatus(HttpStatus.OK)
    public ActionRequestDTO getActionRequest(@PathVariable Long gameId) {
        GameDTO game = gameRepo.findOne(gameId);
        Long id = game.getActions().get(game.getActions().size() - 1).getId();
        return actionRepo.findOne(id);
    }//games/{gameId}/actions - POST
}