package ch.uzh.ifi.seal.soprafs16.model.cards;

import org.hibernate.annotations.OrderBy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Deck<T extends Card> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(targetEntity = Card.class, mappedBy = "deck")//,fetch = FetchType.EAGER)
    @OrderBy(clause = "pos ASC")
    private List<T> cards;

    public Deck(){
        cards = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<T> getCards() {
        return cards;
    }

    public void setCards(List<T> cards) {
        this.cards = cards;
    }

    public void add(T t){
        cards.add(t);
    }

    public T remove(int pos){
        return cards.remove(pos);
    }

    public boolean removeById(Long id){
        for(T t: cards){
            if(t.getId().equals(id)){
                return cards.remove(t);
            }
        }

        return false;
    }

    public T get(int pos){
        return cards.get(pos);
    }

    public int size(){
        return cards.size();
    }
}