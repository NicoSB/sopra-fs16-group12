package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.Actions.*;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ResponseDTOs.*;
import ch.uzh.ifi.seal.soprafs16.model.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActionResponseFactoryImpl implements ActionResponseFactory {
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

    public ActionResponse createActionResponse(ActionResponseDTO responseDTO) {
        ActionResponse response = new ActionResponse();
        response.setResponse(responseDTO);

        if(responseDTO.getClass() == CollectItemResponseDTO.class) {
            response.setAction(new CollectItemAction(userRepo, wagonLevelRepo, itemRepo));
        } else if (responseDTO.getClass() == DrawCardResponseDTO.class) {
            response.setAction(new DrawCardAction(userRepo, deckRepo, cardRepo));
        } else if (responseDTO.getClass() == MoveMarshalResponseDTO.class) {
            response.setAction(new MoveMarshalAction(gameRepo, marshalRepo, wagonLevelRepo));
        } else if (responseDTO.getClass() == MoveResponseDTO.class) {
            response.setAction(new MoveAction(userRepo, wagonLevelRepo));
        } else if (responseDTO.getClass() == PlayCardResponseDTO.class) {
            response.setAction(new PlayCardAction(gameRepo, userRepo, cardRepo, deckRepo));
        } else if (responseDTO.getClass() == PunchResponseDTO.class) {
            response.setAction(new PunchAction(userRepo, gameRepo, itemRepo, wagonLevelRepo));
        } else if (responseDTO.getClass() == ShootResponseDTO.class) {
            response.setAction(new ShootAction(userRepo, wagonLevelRepo, deckRepo, cardRepo));
        } else {
            throw new IllegalArgumentException(responseDTO.getClass().getName() + " is not defined!");
        }

        return response;
    }
}
