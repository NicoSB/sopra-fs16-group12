package ch.uzh.ifi.seal.soprafs16.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import ch.uzh.ifi.seal.soprafs16.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;

@Entity
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UserStatus status;

    @ManyToOne
    @JsonIgnore
    private GameDTO game;

    @ManyToOne
    @JsonIgnore
    private WagonLevel wagonLevel;

    @OneToMany//(fetch = FetchType.EAGER)
    private List<Item> items;

    @OneToOne(targetEntity = ch.uzh.ifi.seal.soprafs16.model.characters.Character.class)
    private ch.uzh.ifi.seal.soprafs16.model.characters.Character character;

    @OneToOne
    private PlayerDeck<HandCard> handDeck;

    @OneToOne
    private PlayerDeck<HandCard> hiddenDeck;

    @OneToOne
    private PlayerDeck<BulletCard> bulletsDeck;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public GameDTO getGame() {
        return game;
    }

    public void setGame(GameDTO game) {
        this.game = game;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public WagonLevel getWagonLevel() {
        return wagonLevel;
    }

    public void setWagonLevel(WagonLevel wagonLevel) {
        this.wagonLevel = wagonLevel;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public ch.uzh.ifi.seal.soprafs16.model.characters.Character getCharacter() {
        return character;
    }

    public void setCharacter(ch.uzh.ifi.seal.soprafs16.model.characters.Character character) {
        this.character = character;
    }

    public PlayerDeck<HandCard> getHandDeck() {
        return handDeck;
    }

    public void setHandDeck(PlayerDeck<HandCard> handDeck) {
        this.handDeck = handDeck;
    }

    public PlayerDeck<HandCard> getHiddenDeck() {
        return hiddenDeck;
    }

    public void setHiddenDeck(PlayerDeck<HandCard> hiddenDeck) {
        this.hiddenDeck = hiddenDeck;
    }

    public PlayerDeck<BulletCard> getBulletsDeck() {
        return bulletsDeck;
    }

    public void setBulletsDeck(PlayerDeck<BulletCard> bulletsDeck) {
        this.bulletsDeck = bulletsDeck;
    }

    public boolean removeItemById(Long id) {
        for (Item i : items) {
            if (i.getId().equals(id)) {
                return items.remove(i);
            }
        }

        return false;
    }
}
