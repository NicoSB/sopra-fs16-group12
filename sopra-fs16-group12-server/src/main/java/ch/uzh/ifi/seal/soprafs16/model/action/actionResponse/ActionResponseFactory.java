package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

public interface ActionResponseFactory {
    ActionResponse createActionResponse(ActionResponseDTO responseDTO);
}
