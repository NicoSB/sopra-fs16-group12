package ch.uzh.ifi.seal.soprafs16.service.GameService;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;

/**
 * Provides a template method for game creation.
 */
public abstract class GameInitializer {
    protected GameDTO game;

    public GameDTO startGame(GameDTO game) throws IllegalStateException{
        this.game = game;
        try {
            initGameWagons();
            initPlayerDecks();
            initGameDecks();
            initGameFigurines();
            initGameItems();
            initGameStatus();

            return game;
        } catch (Exception e) {
            throw new IllegalStateException("The game could not be created!", e);
        }
    }

    abstract void initGameWagons();
    abstract void initPlayerDecks();
    abstract void initGameDecks();
    abstract void initGameFigurines();
    abstract void initGameItems();
    abstract void initGameStatus();
}
