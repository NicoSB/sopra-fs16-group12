package ch.uzh.ifi.seal.soprafs16.model.action.response.actions;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.Marshal;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.MoveMarshalResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.internal.matchers.Equality.areEqual;
import static org.springframework.util.Assert.isNull;

@RunWith(MockitoJUnitRunner.class)
public class MoveMarshalActionTest {

    private MoveMarshalAction action;
    private Marshal marshal;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private MarshalRepository marshalRepository;

    @Mock
    private WagonLevelRepository wagonLevelRepository;

    @Before
    public void setUp() {
        action = new MoveMarshalAction(gameRepository, marshalRepository, wagonLevelRepository);
        marshal = new Marshal();
        marshal.setId(1L);

        GameDTO game = new GameDTO();
        game.setId(1L);
        game.setMarshal(marshal);

        when(gameRepository.findOne(anyLong())).thenReturn(game);
        when(marshalRepository.findOne(anyLong())).thenReturn(marshal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenResponseIsNotDrawCardResponse_throwsIllegalArgumentException() {
        // when
        ActionResponseDTO response = new ActionResponseDTO() {
        };
        action.execute(response);

        // then throws
    }

    @Test
    public void whenResponseIsValid_movesMarshal() {
        // given
        WagonLevel wagonLevel1 = new WagonLevel();
        wagonLevel1.setId(1L);

        WagonLevel wagonLevel2 = new WagonLevel();
        wagonLevel1.setId(2L);

        wagonLevel1.setMarshal(marshal);
        marshal.setWagonLevel(wagonLevel1);

        when(wagonLevelRepository.findOne(eq(wagonLevel1.getId()))).thenReturn(wagonLevel1);
        when(wagonLevelRepository.findOne(eq(wagonLevel2.getId()))).thenReturn(wagonLevel2);

        // when
        MoveMarshalResponseDTO response = new MoveMarshalResponseDTO();
        response.setWagonLevelId(wagonLevel2.getId());

        action.execute(response);

        // then
        isNull(wagonLevel1.getMarshal());
        areEqual(marshal, wagonLevel2.getMarshal());
        areEqual(wagonLevel2, marshal.getWagonLevel());
    }

}
