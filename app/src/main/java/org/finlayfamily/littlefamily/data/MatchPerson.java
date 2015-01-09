package org.finlayfamily.littlefamily.data;

/**
 * Created by jfinlay on 1/9/2015.
 */
public class MatchPerson {
    private boolean matched;
    private boolean flipped;
    private LittlePerson person;

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public LittlePerson getPerson() {
        return person;
    }

    public void setPerson(LittlePerson person) {
        this.person = person;
    }
}
