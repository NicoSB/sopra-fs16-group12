package ch.uzh.ifi.seal.soprafs16.model.action.response;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.actions.*;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.springframework.util.Assert.isTrue;

@RunWith(MockitoJUnitRunner.class)
public class ActionResponseFactoryImplTest {

    private ActionResponseFactoryImpl factory = new ActionResponseFactoryImpl();

    @Test
    public void whenInputIsCollectItem_returnsCollectItemAction() {
        // when
        ActionResponseDTO response = new CollectItemResponseDTO();
        ActionResponse actionResponse = factory.createActionResponse(response);

        // then
        assertEquals(response, actionResponse.getResponse());
        isTrue(actionResponse.getAction() instanceof CollectItemAction);
    }

    @Test
    public void whenInputIsDrawCard_returnsDrawCardAction() {
        // when
        ActionResponseDTO response = new DrawCardResponseDTO();
        ActionResponse actionResponse = factory.createActionResponse(response);

        // then
        assertEquals(response, actionResponse.getResponse());
        isTrue(actionResponse.getAction() instanceof DrawCardAction);
    }

    @Test
    public void whenInputIsMove_returnsMoveAction() {
        // when
        ActionResponseDTO response = new MoveResponseDTO();
        ActionResponse actionResponse = factory.createActionResponse(response);

        // then
        assertEquals(response, actionResponse.getResponse());
        isTrue(actionResponse.getAction() instanceof MoveAction);
    }

    @Test
    public void whenInputIsMoveMarshal_returnsMoveMarshalAction() {
        // when
        ActionResponseDTO response = new MoveMarshalResponseDTO();
        ActionResponse actionResponse = factory.createActionResponse(response);

        // then
        assertEquals(response, actionResponse.getResponse());
        isTrue(actionResponse.getAction() instanceof MoveMarshalAction);
    }

    @Test
    public void whenInputIsPlayCard_returnsPlayCardAction() {
        // when
        ActionResponseDTO response = new PlayCardResponseDTO();
        ActionResponse actionResponse = factory.createActionResponse(response);

        // then
        assertEquals(response, actionResponse.getResponse());
        isTrue(actionResponse.getAction() instanceof PlayCardAction);
    }

    @Test
    public void whenInputIsPunch_returnsPunchAction() {
        // when
        ActionResponseDTO response = new PunchResponseDTO();
        ActionResponse actionResponse = factory.createActionResponse(response);

        // then
        assertEquals(response, actionResponse.getResponse());
        isTrue(actionResponse.getAction() instanceof PunchAction);
    }

    @Test
    public void whenInputIsShoot_returnsShootAction() {
        // when
        ActionResponseDTO response = new ShootResponseDTO();
        ActionResponse actionResponse = factory.createActionResponse(response);

        // then
        assertEquals(response, actionResponse.getResponse());
        isTrue(actionResponse.getAction() instanceof ShootAction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInputIsOther_throwsIllegalArgumentException() {
        // when
        ActionResponseDTO response = new ActionResponseDTO() {};
        factory.createActionResponse(response);

        // throws
    }
//    @Test
//    public void processResponse_DrawCardIsCorrect(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
//        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
//        // Assert correct playerDeck sizes
//        assertEquals(4, hiddenDeck.size());
//        assertEquals(6, handDeck.size());
//
//        DrawCardResponseDTO dcr = new DrawCardResponseDTO();
//        dcr.setUserId(user.getId());
//        dcr.setSpielId(gameId);
//        ars.processResponse(dcr);
//
//        hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
//        handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
//
//        assertEquals(1, hiddenDeck.size());
//        assertEquals(9, handDeck.size());
//    }


//    @Test
//    public void processResponse_DrawCardIsCorrectWhenEmpty(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
//        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
//        // Assert correct playerDeck sizes
//        assertEquals(4, hiddenDeck.size());
//        assertEquals(6, handDeck.size());
//
//        DrawCardResponseDTO dcr = new DrawCardResponseDTO();
//        dcr.setUserId(user.getId());
//        dcr.setSpielId(gameId);
//        ars.processResponse(dcr);
//        ars.processResponse(dcr);
//
//        hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
//        handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
//
//        assertEquals(0, hiddenDeck.size());
//        assertEquals(10, handDeck.size());
//    }
//
//    @Test
//    public void processResponse_playCardIsCorrect(){
//        GameDTO game = gameRepo.findOne(gameId);
//
//        Hibernate.initialize(game.getUsers());
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
//        ActionCard ac = (ActionCard)cardRepo.findOne(handDeck.get(0).getId());
//
//        PlayCardResponseDTO pcr = new PlayCardResponseDTO();
//        pcr.setUserId(user.getId());
//        pcr.setSpielId(game.getId());
//        pcr.setPlayedCardId(ac.getId());
//
//        ars.processResponse(pcr);
//
//        handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
//        GameDeck<ActionCard> commonDeck = (GameDeck<ActionCard>)deckRepo.findOne(game.getCommonDeck().getId());
//
//        assertFalse(handDeck.getCards().contains(ac));
//        assertEquals(ac.getId(), ((ActionCard)commonDeck.getCards().get(commonDeck.size()-1)).getId());
//    }
//
//    @Test
//    public void processResponse_moveIsCorrect(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
//
//        MoveResponseDTO mr = new MoveResponseDTO();
//        mr.setUserId(user.getId());
//        mr.setSpielId(game.getId());
//        mr.setWagonLevelId(wl.getWagonLevelBefore().getId());
//
//        ars.processResponse(mr);
//
//        wl = wagonLevelRepo.findOne(wl.getId());
//        WagonLevel newWl = wagonLevelRepo.findOne(wl.getWagonLevelBefore().getId());
//        assertTrue(newWl.removeUserById(user.getId()));
//        assertFalse(wl.removeUserById(user.getId()));
//    }
//
//    @Test
//    public void processResponse_collectItemIsCorrect(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
//
//        CollectItemResponseDTO cir = new CollectItemResponseDTO();
//        cir.setUserId(user.getId());
//        cir.setSpielId(game.getId());
//        cir.setCollectedItemType(wl.getItems().get(0).getItemType());
//
//        int userItemCount = user.getItems().size();
//        int wlItemCount = wl.getItems().size();
//
//        ars.processResponse(cir);
//
//        wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
//        user = userRepo.findOne(user.getId());
//
//        assertEquals(userItemCount + 1, user.getItems().size());
//        assertEquals(wlItemCount - 1, wl.getItems().size());
//    }
//
//    @Test
//    public void processResponse_PunchIsCorrect(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        User victim = userRepo.findOne(game.getUsers().get(1).getId());
//        WagonLevel wl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
//        WagonLevel newWl = wagonLevelRepo.findOne(victim.getWagonLevel().getWagonLevelBefore().getId());
//
//        Hibernate.initialize(victim.getItems());
//        Hibernate.initialize(wl.getItems());
//        int vicItemCount = victim.getItems().size();
//        int wlItemCount = wl.getItems().size();
//
//        PunchResponseDTO pr = new PunchResponseDTO();
//        pr.setVictimId(victim.getId());
//        pr.setItemType(ItemType.BAG);
//        pr.setWagonLevelId(newWl.getId());
//        pr.setSpielId(gameId);
//        pr.setUserId(user.getId());
//
//        ars.processResponse(pr);
//
//        wl = wagonLevelRepo.findOne(wl.getId());
//        newWl = wagonLevelRepo.findOne(newWl.getId());
//        victim = userRepo.findOne(game.getUsers().get(1).getId());
//
//        assertEquals(vicItemCount - 1, victim.getItems().size());
//        assertEquals(wlItemCount + 1, wl.getItems().size());
//        assertEquals(newWl.getId(), victim.getWagonLevel().getId());
//        assertTrue(newWl.removeUserById(victim.getId()));
//    }
//
//    @Test
//    public void processResponse_ShootIsCorrect(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        User victim = userRepo.findOne(game.getUsers().get(1).getId());
//
//        Hibernate.initialize(user.getBulletsDeck());
//        Hibernate.initialize(victim.getHiddenDeck());
//        int bulletCounter = user.getBulletsDeck().size();
//        int victimHiddenDeckSize = victim.getHiddenDeck().size();
//        BulletCard bc = (BulletCard)user.getBulletsDeck().getCards().get(user.getBulletsDeck().size() - 1);
//
//        ShootResponseDTO sr = new ShootResponseDTO();
//        sr.setSpielId(game.getId());
//        sr.setUserId(user.getId());
//        sr.setVictimId(victim.getId());
//
//        ars.processResponse(sr);
//
//        user = userRepo.findOne(game.getUsers().get(0).getId());
//        victim = userRepo.findOne(game.getUsers().get(1).getId());
//        bc = (BulletCard)cardRepo.findOne(bc.getId());
//
//        assertEquals(bulletCounter - 1, user.getBulletsDeck().size());
//        assertEquals(victimHiddenDeckSize + 1, victim.getHiddenDeck().getCards().size());
//        assertEquals(victim.getHiddenDeck().getId(), bc.getDeck().getId());
//    }
//
//    @Test
//    public void processResponse_MoveMarshalIsCorrect(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        Marshal marshal = marshalRepo.findOne(game.getMarshal().getId());
//        WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());
//        WagonLevel newWl = wagonLevelRepo.findOne(marshal.getWagonLevel().getWagonLevelAfter().getId());
//
//        MoveMarshalResponseDTO mmr = new MoveMarshalResponseDTO();
//        mmr.setSpielId(game.getId());
//        mmr.setUserId(user.getId());
//        mmr.setWagonLevelId(newWl.getId());
//
//        ars.processResponse(mmr);
//
//        marshal = marshalRepo.findOne(game.getMarshal().getId());
//        wl = wagonLevelRepo.findOne(wl.getId());
//        newWl = wagonLevelRepo.findOne(newWl.getId());
//
//        assertNull(wl.getMarshal());
//        assertEquals(marshal.getId(), newWl.getMarshal().getId());
//        assertEquals(newWl.getId(), marshal.getWagonLevel().getId());
//    }
//
//    @Test
//    public void changeLevel_isCorrect(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//
//        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
//        WagonLevel newWl = wagonLevelRepo.findOne(wl.getWagon().getTopLevel().getId());
//
//        ars.changeLevel(user);
//
//        wl = wagonLevelRepo.findOne(wl.getId());
//        newWl = wagonLevelRepo.findOne(newWl.getId());
//        user = userRepo.findOne(user.getId());
//
//        assertEquals(newWl.getId(), user.getWagonLevel().getId());
//        assertFalse(wl.removeUserById(user.getId()));
//        assertTrue(newWl.removeUserById(user.getId()));
//    }
//
//    @Test
//    public void processResponse_Punch_CheyenneCollectsItem(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User cheyenne = userRepo.findOne(game.getUsers().get(0).getId());
//        cheyenne.setCharacter(new Cheyenne());
//
//        User victim = userRepo.findOne(game.getUsers().get(1).getId());
//        WagonLevel wl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
//        WagonLevel newWl = wagonLevelRepo.findOne(victim.getWagonLevel().getWagonLevelBefore().getId());
//
//        Hibernate.initialize(victim.getItems());
//        Hibernate.initialize(wl.getItems());
//        int vicItemCount = victim.getItems().size();
//        int wlItemCount = wl.getItems().size();
//        int cheyenneItemCount = cheyenne.getItems().size();
//
//        PunchResponseDTO pr = new PunchResponseDTO();
//        pr.setVictimId(victim.getId());
//        pr.setItemType(ItemType.BAG);
//        pr.setWagonLevelId(newWl.getId());
//        pr.setSpielId(gameId);
//        pr.setUserId(cheyenne.getId());
//
//        ars.processResponse(pr);
//
//        wl = wagonLevelRepo.findOne(wl.getId());
//        newWl = wagonLevelRepo.findOne(newWl.getId());
//        victim = userRepo.findOne(victim.getId());
//        cheyenne = userRepo.findOne(cheyenne.getId());
//
//        assertEquals(vicItemCount - 1, victim.getItems().size());
//        assertEquals(wlItemCount, wl.getItems().size());
//        assertEquals(cheyenneItemCount + 1, cheyenne.getItems().size());
//        assertEquals(newWl.getId(), victim.getWagonLevel().getId());
//        assertTrue(newWl.removeUserById(victim.getId()));
//    }
//
//
//    @Test
//    public void processResponse_TucoShooter_VictimIsMoved(){
//        GameDTO game = gameRepo.findOne(gameId);
//        User user = userRepo.findOne(game.getUsers().get(0).getId());
//        user.setCharacter(new Tuco());
//
//        User victim = userRepo.findOne(game.getUsers().get(1).getId());
//        WagonLevel wlTuco = wagonLevelRepo.findOne(user.getWagonLevel().getId());
//        WagonLevel wlVictim = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
//        WagonLevel newWlVictim = wagonLevelRepo.findOne(game.getWagons().get(2).getBottomLevel().getId());
//        WagonLevel newWlTuco = wagonLevelRepo.findOne(game.getWagons().get(1).getBottomLevel().getId());
//        // move Tuco
//        wlTuco.getUsers().remove(user);
//        wagonLevelRepo.save(wlTuco);
//
//        user.setWagonLevel(newWlTuco);
//        newWlTuco.getUsers().add(user);
//        userRepo.save(user);
//        wagonLevelRepo.save(newWlTuco);
//
//        // move victim
//        wlVictim.getUsers().remove(victim);
//        wagonLevelRepo.save(wlVictim);
//
//        victim.setWagonLevel(newWlVictim);
//        newWlVictim.getUsers().add(victim);
//        userRepo.save(victim);
//        wagonLevelRepo.save(newWlVictim);
//
//        Hibernate.initialize(user.getBulletsDeck());
//        Hibernate.initialize(victim.getHiddenDeck());
//        int bulletCounter = user.getBulletsDeck().size();
//        int victimHiddenDeckSize = victim. getHiddenDeck().size();
//        BulletCard bc = (BulletCard)user.getBulletsDeck().getCards().get(user.getBulletsDeck().size() - 1);
//
//        ShootResponseDTO sr = new ShootResponseDTO();
//        sr.setSpielId(game.getId());
//        sr.setUserId(user.getId());
//        sr.setVictimId(victim.getId());
//
//        ars.processResponse(sr);
//
//        user = userRepo.findOne(game.getUsers().get(0).getId());
//        victim = userRepo.findOne(game.getUsers().get(1).getId());
//        bc = (BulletCard)cardRepo.findOne(bc.getId());
//
//        assertEquals(bulletCounter - 1, user.getBulletsDeck().size());
//        assertEquals(victimHiddenDeckSize + 1, victim.getHiddenDeck().getCards().size());
//        assertEquals(game.getWagons().get(3).getBottomLevel().getId(), victim.getWagonLevel().getId());
//        assertEquals(victim.getHiddenDeck().getId(), bc.getDeck().getId());
//    }
}
