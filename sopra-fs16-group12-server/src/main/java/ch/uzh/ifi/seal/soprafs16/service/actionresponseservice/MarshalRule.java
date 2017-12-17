package ch.uzh.ifi.seal.soprafs16.service.actionresponseservice;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;

public interface MarshalRule {
    void execute(GameDTO game);
}
