package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepositoryBundle {

    @Autowired
    private ActionRepository actionRepository;
    @Autowired
    private ActionResponseRepository actionResponseRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CharacterRepository characterRepository;
    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private MarshalRepository marshalRepository;
    @Autowired
    private TurnRepository turnRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WagonRepository wagonRepository;
    @Autowired
    private WagonLevelRepository wagonLevelRepository;

    public ActionRepository getActionRepository() {
        return actionRepository;
    }

    public ActionResponseRepository getActionResponseRepository() {
        return actionResponseRepository;
    }

    public CardRepository getCardRepository() {
        return cardRepository;
    }

    public CharacterRepository getCharacterRepository() {
        return characterRepository;
    }

    public DeckRepository getDeckRepository() {
        return deckRepository;
    }

    public GameRepository getGameRepository() {
        return gameRepository;
    }

    public ItemRepository getItemRepository() {
        return itemRepository;
    }

    public MarshalRepository getMarshalRepository() {
        return marshalRepository;
    }

    public TurnRepository getTurnRepository() {
        return turnRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public WagonRepository getWagonRepository() {
        return wagonRepository;
    }

    public WagonLevelRepository getWagonLevelRepository() {
        return wagonLevelRepository;
    }
}
