package ch.uzh.ifi.seal.soprafs16.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

@Entity
public class GameDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String owner;

    @Column
    private GameStatus status;

    @Column
    private Integer currentPlayer;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)//, fetch = FetchType.EAGER)
    private List<User> users;

    @OneToMany(mappedBy = "game")//, fetch = FetchType.EAGER)
    private List<Wagon> wagons;

    @JsonIgnore
    @OneToOne
    private Marshal marshal;

    @Column
    private Integer currentRound;

    @Column
    private Integer currentTurn;

    @JsonIgnore
    @Column
    private String roundPattern;

    @Column
    private PhaseType currentPhase;

    @Column
    private int roundStarter;

    @JsonIgnore
    @Column
    private Integer actionRequestCounter;

    @OneToMany
    private List<ActionRequestDTO> actions;

    @OneToOne
    private GameDeck<RoundCard> roundCardDeck;

    @JsonIgnore
    @OneToOne
    private GameDeck<BulletCard> neutralBulletsDeck;

    @OneToOne
    private GameDeck<ActionCard> commonDeck;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<User> getUsers() {
            return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Integer getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Integer currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public List<Wagon> getWagons() {
        return wagons;
    }

    public void setWagons(List<Wagon> wagons) {
        this.wagons = wagons;
    }

    public Marshal getMarshal() {
        return marshal;
    }

    public void setMarshal(Marshal marshal) {
        this.marshal = marshal;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public Integer getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(Integer currentTurn) {
        this.currentTurn = currentTurn;
    }

    public PhaseType getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(PhaseType currentPhase) {
        this.currentPhase = currentPhase;
    }

    public void setActions(List<ActionRequestDTO> actions) {
        this.actions = actions;
    }

    public List<ActionRequestDTO> getActions() {
        return actions;
    }

    @JsonIgnore
    public Turn getCurrentTurnType(){
        if(roundCardDeck != null) {
            return ((RoundCard)roundCardDeck.getCards().get(currentRound)).getPattern().get(currentTurn);
        }
        return null;
    }

    public void setRoundStarter(int roundStarter) {
        this.roundStarter = roundStarter;
    }

    public String getRoundPattern() {
        return roundPattern;
    }

    public void setRoundPattern(String roundPattern) {
        this.roundPattern = roundPattern;
    }

    public GameDeck<ActionCard> getCommonDeck() {
        return commonDeck;
    }

    public void setCommonDeck(GameDeck<ActionCard> commonDeck) {
        this.commonDeck = commonDeck;
    }

    public GameDeck<RoundCard> getRoundCardDeck() {
        return roundCardDeck;
    }

    public void setRoundCardDeck(GameDeck<RoundCard> roundCardDeck) {
        this.roundCardDeck = roundCardDeck;
    }

    public GameDeck<BulletCard> getNeutralBulletsDeck() {
        return neutralBulletsDeck;
    }

    public void setNeutralBulletsDeck(GameDeck<BulletCard> neutralBulletsDeck) {
        this.neutralBulletsDeck = neutralBulletsDeck;
    }

    public Integer getRoundStarter() {
        return roundStarter;
    }

    public void setRoundStarter(Integer roundStarter) {
        this.roundStarter = roundStarter;
    }

    public Integer getActionRequestCounter() {
        return actionRequestCounter;
    }

    public void setActionRequestCounter(Integer actionRequestCounter) {
        this.actionRequestCounter = actionRequestCounter;
    }
}
