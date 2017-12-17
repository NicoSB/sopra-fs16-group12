package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.Marshal;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.MoveMarshalResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;

public class MoveMarshalAction implements Action {

    private GameRepository gameRepo;
    private MarshalRepository marshalRepo;
    private WagonLevelRepository wagonLevelRepo;

    public MoveMarshalAction(GameRepository gameRepo, MarshalRepository marshalRepo, WagonLevelRepository wagonLevelRepo) {
        this.gameRepo = gameRepo;
        this.marshalRepo = marshalRepo;
        this.wagonLevelRepo = wagonLevelRepo;
    }

    @Override
    public void execute(ActionResponseDTO response) {
        if (!(response instanceof MoveMarshalResponseDTO))
            throw new IllegalArgumentException("Must be a MoveMarshallResponseDTO, was " + response.getClass().getName() + " instead.");

        MoveMarshalResponseDTO marshalResponse = (MoveMarshalResponseDTO) response;

        GameDTO game = gameRepo.findOne(marshalResponse.getSpielId());

        Marshal marshal = marshalRepo.findOne(game.getMarshal().getId());
        WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());
        WagonLevel newWl = wagonLevelRepo.findOne(marshalResponse.getWagonLevelId());

        wl.setMarshal(null);
        newWl.setMarshal(marshal);
        marshal.setWagonLevel(newWl);

        wagonLevelRepo.save(wl);
        wagonLevelRepo.save(newWl);
        marshalRepo.save(marshal);
    }
}
