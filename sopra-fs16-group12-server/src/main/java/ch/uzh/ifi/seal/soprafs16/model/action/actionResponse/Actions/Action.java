package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.Actions;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

public interface Action {
    void execute(ActionResponseDTO response);
}
