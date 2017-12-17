package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.CollectItemResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectItemActionTest {

    private CollectItemAction action;

    private User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WagonLevelRepository wagonLevelRepository;

    @Mock
    private ItemRepository itemRepository;

    @Before
    public void setUp() {
        user = new User();
        user.setItems(new ArrayList<>());
        user.setId(1L);

        when(userRepository.findOne(anyLong())).thenReturn(user);

        action = new CollectItemAction(userRepository, wagonLevelRepository, itemRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenResponseIsNotCollectItemResponse_throwsIllegalArgumentException() {
        // when
        ActionResponseDTO response = new ActionResponseDTO() {};
        action.execute(response);

        // then throws
    }

    @Test
    public void whenItemIsAvailable_collectsRandomItem(){
        // given
        Item item = new Item();
        item.setId(1L);

        List<Item> items = new ArrayList<>();
        items.add(item);

        WagonLevel wagonLevel = new WagonLevel();
        wagonLevel.setId(1L);
        wagonLevel.setItems(items);

        user.setWagonLevel(wagonLevel);

        when(wagonLevelRepository.findOne(anyLong())).thenReturn(wagonLevel);
        when(itemRepository.findOne(anyLong())).thenReturn(item);

        // when
        ActionResponseDTO response = new CollectItemResponseDTO();
        response.setUserId(user.getId());

        action.execute(response);

        // then
        assertEquals(0, wagonLevel.getItems().size());
        assertEquals(1, user.getItems().size());
        assertEquals(item, user.getItems().get(0));
        assertEquals(user, item.getUser());
        assertNull(item.getWagonLevel());
    }

    @Test
    public void whenNoItemIsAvailable_doesNotCollectAnItem(){
        // given
        List<Item> items = new ArrayList<>();

        WagonLevel wagonLevel = new WagonLevel();
        wagonLevel.setId(1L);
        wagonLevel.setItems(items);

        user.setWagonLevel(wagonLevel);

        when(wagonLevelRepository.findOne(anyLong())).thenReturn(wagonLevel);

        // when
        ActionResponseDTO response = new CollectItemResponseDTO();
        response.setUserId(user.getId());

        action.execute(response);

        // then
        assertEquals(0, wagonLevel.getItems().size());
        assertEquals(0, user.getItems().size());
    }
}
