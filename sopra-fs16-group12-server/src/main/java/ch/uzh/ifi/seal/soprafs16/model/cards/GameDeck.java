package ch.uzh.ifi.seal.soprafs16.model.cards;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class GameDeck<T extends Card>  extends Deck implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @OneToOne
    @JsonIgnore
    private GameDTO game;

    public GameDeck(){
        this.setCards(new ArrayList<T>());
    }

    public GameDTO getGame() {
        return game;
    }

    public void setGame(GameDTO game) {
        this.game = game;
    }

    public boolean isEmpty() {
        return getCards().isEmpty();
    }
}
