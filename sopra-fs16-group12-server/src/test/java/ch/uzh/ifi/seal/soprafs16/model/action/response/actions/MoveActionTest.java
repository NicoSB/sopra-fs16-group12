package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.MoveResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.Equality.areEqual;

@RunWith(MockitoJUnitRunner.class)
public class MoveActionTest {

    private MoveAction action;

    private User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WagonLevelRepository wagonLevelRepository;

    @Before
    public void setUp() {
        user = new User();
        user.setItems(new ArrayList<>());
        user.setId(1L);

        when(userRepository.findOne(anyLong())).thenReturn(user);

        action = new MoveAction(userRepository, wagonLevelRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenResponseIsNotDrawCardResponse_throwsIllegalArgumentException() {
        // when
        ActionResponseDTO response = new ActionResponseDTO() {};
        action.execute(response);

        // then throws
    }

    @Test
    public void whenResponseIsValid_movesUserToGivenWagonLevel() {
        // given
        WagonLevel wagonLevel1 = new WagonLevel();
        wagonLevel1.setId(1L);

        List<User> users = new ArrayList<>();
        users.add(user);
        wagonLevel1.setUsers(users);

        WagonLevel wagonLevel2 = new WagonLevel();
        wagonLevel2.setId(2L);
        wagonLevel2.setUsers(new ArrayList<>());

        when(wagonLevelRepository.findOne(eq(wagonLevel1.getId()))).thenReturn(wagonLevel1);
        when(wagonLevelRepository.findOne(eq(wagonLevel2.getId()))).thenReturn(wagonLevel2);
        when(wagonLevelRepository.save(any(WagonLevel.class))).then(returnsFirstArg());

        user.setWagonLevel(wagonLevel1);

        // when
        MoveResponseDTO response = new MoveResponseDTO();
        response.setWagonLevelId(wagonLevel2.getId());

        action.execute(response);

        // then
        areEqual(0, wagonLevel1.getUsers().size());
        areEqual(1, wagonLevel2.getUsers ().size());
        areEqual(wagonLevel2, user.getWagonLevel());
    }
}
