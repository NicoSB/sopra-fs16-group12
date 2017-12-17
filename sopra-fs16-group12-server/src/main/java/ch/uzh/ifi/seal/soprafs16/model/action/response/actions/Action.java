package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

public interface Action {
    void execute(ActionResponseDTO response);
}
