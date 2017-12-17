package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.MoveResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;

public class MoveAction implements Action {

    private UserRepository userRepo;
    private WagonLevelRepository wagonLevelRepo;

    public MoveAction(UserRepository userRepo, WagonLevelRepository wagonLevelRepo) {
        this.userRepo = userRepo;
        this.wagonLevelRepo = wagonLevelRepo;
    }

    @Override
    public void execute(ActionResponseDTO response) {
        if (!(response instanceof MoveResponseDTO))
            throw new IllegalArgumentException("Must be a MoveResponseDTO, was " + response.getClass().getName() + " instead.");

        MoveResponseDTO moveResponse = (MoveResponseDTO) response;
        User user = userRepo.findOne(moveResponse.getUserId());

        WagonLevel newWl = wagonLevelRepo.findOne(moveResponse.getWagonLevelId());
        WagonLevel oldWl = wagonLevelRepo.findOne(user.getWagonLevel().getId());

        oldWl.removeUserById(user.getId());
        wagonLevelRepo.save(oldWl);

        newWl.getUsers().add(user);

        user.setWagonLevel(newWl);

        wagonLevelRepo.save(newWl);
        userRepo.save(user);
    }
}
