package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.Actions;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ResponseDTOs.PunchResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.characters.Cheyenne;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;

public class PunchAction implements Action {

    private UserRepository userRepo;
    private GameRepository gameRepo;
    private ItemRepository itemRepo;
    private WagonLevelRepository wagonLevelRepo;

    public PunchAction(UserRepository userRepo, GameRepository gameRepo, ItemRepository itemRepo, WagonLevelRepository wagonLevelRepo) {
        this.userRepo = userRepo;
        this.gameRepo = gameRepo;
        this.itemRepo = itemRepo;
        this.wagonLevelRepo = wagonLevelRepo;
    }

    @Override
    public void execute(ActionResponseDTO response) {
        if (!(response instanceof PunchResponseDTO))
            throw new IllegalArgumentException("Must be a PunchResponseDTO, was " + response.getClass().getName() + " instead.");

        PunchResponseDTO punchResponse = (PunchResponseDTO) response;

        User user = userRepo.findOne(punchResponse.getUserId());
        GameDTO game = gameRepo.findOne(punchResponse.getSpielId());

        User victim = userRepo.findOne(punchResponse.getVictimId());
        WagonLevel moveWl = wagonLevelRepo.findOne(punchResponse.getWagonLevelId());
        WagonLevel dropWl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
        Item item = getRandomItemFromUser(punchResponse.getItemType(), victim);

        if (item != null) {
            item = itemRepo.findOne(item.getId());
            // Drop Item
            victim.getItems().remove(item);
            item.setUser(null);
            victim = userRepo.save(victim);
            item = itemRepo.save(item);
            // Cheyenne Character Skill
            if (user.getCharacter().getClass().equals(Cheyenne.class) && item.getItemType() == ItemType.BAG) {
                item.setUser(user);
                item = itemRepo.save(item);
                user.getItems().add(item);
                user = userRepo.save(user);
            } else {
                item.setUser(null);
                item.setWagonLevel(dropWl);
                item = itemRepo.save(item);
                dropWl.getItems().add(item);
                dropWl = wagonLevelRepo.save(dropWl);
            }
        }

        dropWl.getUsers().remove(victim);

        victim.setWagonLevel(moveWl);
        Hibernate.initialize(moveWl.getUsers());
        moveWl.getUsers().add(victim);

        gameRepo.findOne(game.getId());

        userRepo.save(user);
        userRepo.save(victim);
        wagonLevelRepo.save(dropWl);
        wagonLevelRepo.save(moveWl);
    }

    private Item getRandomItemFromUser(ItemType type, User user) {
        if (type != ItemType.BAG) {
            for (int i = 0; i < user.getItems().size(); i++) {
                if (user.getItems().get(i).getItemType() == type) {
                    return user.getItems().get(i);
                }
            }
        } else {
            List<Item> bags = new ArrayList<>();
            for (int i = 0; i < user.getItems().size(); i++) {
                if (user.getItems().get(i).getItemType() == ItemType.BAG) {
                    bags.add(user.getItems().get(i));
                }
            }
            return bags.get((int) (Math.random() * bags.size()));
        }
        return null;
    }
}
