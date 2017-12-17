package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.CollectItemResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;

import java.util.ArrayList;
import java.util.List;

public class CollectItemAction implements Action {

    private UserRepository userRepo;
    private WagonLevelRepository wagonLevelRepo;
    private ItemRepository itemRepo;

    public CollectItemAction(UserRepository userRepo, WagonLevelRepository wagonLevelRepo, ItemRepository itemRepo) {
        this.userRepo = userRepo;
        this.wagonLevelRepo = wagonLevelRepo;
        this.itemRepo = itemRepo;
    }

    @Override
    public void execute(ActionResponseDTO response) {
        if (!(response instanceof CollectItemResponseDTO))
            throw new IllegalArgumentException("Must be a CollectItemRequest, was " + response.getClass().getName() + " instead.");

        CollectItemResponseDTO collectResponse = (CollectItemResponseDTO) response;
        User user = userRepo.findOne(collectResponse.getUserId());

        WagonLevel wagonLevel = wagonLevelRepo.findOne(user.getWagonLevel().getId());
        Item item = getRandomItemFromWagonLevel(collectResponse.getCollectedItemType(), wagonLevel);
        if (item != null) {
            item = itemRepo.findOne(item.getId());
            wagonLevel.removeItemById(item.getId());

            item.setWagonLevel(null);
            item.setUser(user);
            user.getItems().add(item);

            itemRepo.save(item);
            wagonLevelRepo.save(wagonLevel);
            userRepo.save(user);
        }
    }

    private Item getRandomItemFromWagonLevel(ItemType type, WagonLevel wagonLevel) {
        if (type != ItemType.BAG) {
            for (int i = 0; i < wagonLevel.getItems().size(); i++) {
                if (wagonLevel.getItems().get(i).getItemType() == type) {
                    return wagonLevel.getItems().get(i);
                }
            }
        } else {
            List<Item> bags = new ArrayList<>();
            for (int i = 0; i < wagonLevel.getItems().size(); i++) {
                if (wagonLevel.getItems().get(i).getItemType() == ItemType.BAG) {
                    bags.add(wagonLevel.getItems().get(i));
                }
            }
            return bags.size() > 0 ? bags.get((int) (Math.random() * bags.size())) : null;
        }
        return null;
    }
}
