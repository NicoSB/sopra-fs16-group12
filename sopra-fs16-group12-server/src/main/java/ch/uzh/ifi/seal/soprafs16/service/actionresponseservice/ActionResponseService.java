package ch.uzh.ifi.seal.soprafs16.service.actionresponseservice;

import ch.uzh.ifi.seal.soprafs16.model.action.response.ActionResponse;
import ch.uzh.ifi.seal.soprafs16.model.action.response.ActionResponseFactory;
import ch.uzh.ifi.seal.soprafs16.model.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

@Service
@Transactional
public class ActionResponseService {

    private GameRepository gameRepo;
    private ActionResponseFactory actionResponseFactory;
    private MarshalRule marshalRule;

    @Autowired
    public ActionResponseService(RepositoryBundle repositories, ActionResponseFactory actionResponseFactory,
                                 MarshalRule marshalRule) {
        gameRepo = repositories.getGameRepository();
        this.actionResponseFactory = actionResponseFactory;
        this.marshalRule = marshalRule;
    }

    public void processResponse(ActionResponseDTO responseDTO) {
        GameDTO game = gameRepo.findOne(responseDTO.getSpielId());

        ActionResponse response = actionResponseFactory.createActionResponse(responseDTO);
        response.executeAction();

        marshalRule.execute(game);
    }

    public void executeMarshalRule(GameDTO game) {
        marshalRule.execute(game);
    }
}
