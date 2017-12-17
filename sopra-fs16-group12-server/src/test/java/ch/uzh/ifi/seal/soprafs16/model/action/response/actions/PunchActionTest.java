package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.PunchResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.characters.Character;
import ch.uzh.ifi.seal.soprafs16.model.characters.Cheyenne;
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

import static org.junit.Assert.assertNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.Equality.areEqual;

@RunWith(MockitoJUnitRunner.class)
public class PunchActionTest {

    private PunchAction action;

    private User attacker;
    private User victim;
    private Item item;
    private WagonLevel wagonLevel;
    private WagonLevel targetWagonLevel;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private WagonLevelRepository wagonLevelRepository;

    @Before
    public void setUp() {
        item = new Item();
        item.setItemType(ItemType.BAG);
        item.setId(1L);

        List<Item> items = new ArrayList<>();
        items.add(item);

        attacker = new User();
        attacker.setId(1L);
        attacker.setItems(new ArrayList<>());

        victim = new User();
        victim.setId(2L);
        victim.setItems(items);
        item.setUser(victim);

        List<User> users = new ArrayList<>();
        users.add(attacker);
        users.add(victim);

        wagonLevel = new WagonLevel();
        wagonLevel.setId(1L);
        wagonLevel.setUsers(users);
        wagonLevel.setItems(new ArrayList<>());

        targetWagonLevel = new WagonLevel();
        targetWagonLevel.setId(2L);
        targetWagonLevel.setUsers(new ArrayList<>());

        victim.setWagonLevel(wagonLevel);
        attacker.setWagonLevel(wagonLevel);

        when(userRepository.findOne(eq(attacker.getId()))).thenReturn(attacker);
        when(userRepository.findOne(eq(victim.getId()))).thenReturn(victim);
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        when(itemRepository.findOne(eq(item.getId()))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).then(returnsFirstArg());

        when(wagonLevelRepository.findOne(eq(wagonLevel.getId()))).thenReturn(wagonLevel);
        when(wagonLevelRepository.findOne(eq(targetWagonLevel.getId()))).thenReturn(targetWagonLevel);
        when(wagonLevelRepository.save(any(WagonLevel.class))).then(returnsFirstArg());

        action = new PunchAction(userRepository, itemRepository, wagonLevelRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenResponseIsNotCollectItemResponse_throwsIllegalArgumentException() {
        // when
        ActionResponseDTO response = new ActionResponseDTO() {};
        action.execute(response);

        // then throws
    }

    @Test
    public void whenVictimHasItemOfType_DropsItemToWagonLevel() {
        // given
        attacker.setCharacter(new Character());
        victim.setCharacter(new Character());

        // when
        PunchResponseDTO response = new PunchResponseDTO();
        response.setVictimId(victim.getId());
        response.setUserId(attacker.getId());
        response.setItemType(ItemType.BAG);
        response.setWagonLevelId(targetWagonLevel.getId());

        action.execute(response);

        // then
        areEqual(0, victim.getItems().size());
        areEqual(1, wagonLevel.getItems().size());
        assertNull(item.getUser());
        areEqual(wagonLevel, item.getWagonLevel());
    }

    @Test
    public void whenVictimHasItemOfType_MovesVictimToTargetWagonLevel() {
        // given
        attacker.setCharacter(new Character());
        victim.setCharacter(new Character());

        // when
        PunchResponseDTO response = new PunchResponseDTO();
        response.setVictimId(victim.getId());
        response.setUserId(attacker.getId());
        response.setItemType(ItemType.BAG);
        response.setWagonLevelId(targetWagonLevel.getId());

        action.execute(response);

        // then
        areEqual(1, wagonLevel.getUsers().size());
        areEqual(1, targetWagonLevel.getUsers().size());
        areEqual(attacker, wagonLevel.getUsers().get(0));
        areEqual(victim, targetWagonLevel.getUsers().get(0));
        areEqual(targetWagonLevel, victim.getWagonLevel());
    }

    @Test
    public void whenAttackerIsCheyenne_CollectsItem() {
        // given
        attacker.setCharacter(new Cheyenne());
        victim.setCharacter(new Character());

        // when
        PunchResponseDTO response = new PunchResponseDTO();
        response.setVictimId(victim.getId());
        response.setUserId(attacker.getId());
        response.setItemType(ItemType.BAG);
        response.setWagonLevelId(targetWagonLevel.getId());

        action.execute(response);

        // then
        areEqual(0, victim.getItems().size());
        areEqual(0, wagonLevel.getItems().size());
        areEqual(1, attacker.getItems().size());
        assertNull(item.getWagonLevel());
        areEqual(attacker, item.getUser());
    }
}
