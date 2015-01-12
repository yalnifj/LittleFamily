package org.finlayfamily.littlefamily.data;

/**
 * Created by jfinlay on 1/9/2015.
 */
public class MatchPerson {
    private boolean matched;
    private boolean flipped;
    private LittlePerson person;
	private String frame;

	public void setFrame(String frame)
	{
		this.frame = frame;
	}

	public String getFrame()
	{
		return frame;
	}

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
