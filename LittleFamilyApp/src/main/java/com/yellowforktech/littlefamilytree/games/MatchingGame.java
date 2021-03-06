package com.yellowforktech.littlefamilytree.games;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.MatchPerson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jfinlay on 1/9/2015.
 */
public class MatchingGame {
    private int level;
    private List<LittlePerson> peoplePool;
    private List<MatchPerson> board;
	private int[] frames = { 
		R.drawable.frame1,
		R.drawable.frame2,
		R.drawable.frame3,
		R.drawable.frame4,
		R.drawable.frame5,
		R.drawable.frame6,
        R.drawable.frame7
	};

    public MatchingGame(int startLevel, List<LittlePerson> people) {
        this.level = startLevel;
        if (this.level < 1) this.level = 1;
        this.peoplePool = people;
    }

    public void setupLevel() {
        int gridSize = level+1;
		Random rand = new Random();
        board = new ArrayList<>(gridSize*2);
        for(int i=0; i<gridSize; i++) {
            LittlePerson p = peoplePool.get(i % peoplePool.size());
            MatchPerson m1 = new MatchPerson();
            m1.setFlipped(false);
            m1.setMatched(false);
            m1.setPerson(p);
			m1.setFrame(frames[rand.nextInt(frames.length)]);
            board.add(m1);
            MatchPerson m2 = new MatchPerson();
            m2.setFlipped(false);
            m2.setMatched(false);
            m2.setPerson(p);
			m2.setFrame(frames[rand.nextInt(frames.length)]);
            board.add(m2);
        }

        randomizeBoard();
    }

    public void randomizeBoard() {
        Random rand = new Random();
        for(int i=0; i<board.size(); i++) {
            int r1 = rand.nextInt(board.size());
            int r2 = rand.nextInt(board.size());
            MatchPerson p1 = board.get(r1);
            MatchPerson p2 = board.get(r2);
            board.set(r2, p1);
            board.set(r1, p2);
        }
    }

    public boolean isMatch(int pos1, int pos2) {
        if (pos1!=pos2 && pos1>=0 && pos2>=0 && pos1<board.size() && pos2<board.size()) {
            MatchPerson p1 = board.get(pos1);
            MatchPerson p2 = board.get(pos2);
            if (p1.getPerson()==p2.getPerson()) return true;
        }
        return false;
    }

    public boolean allMatched() {
        for(MatchPerson p : board) {
            if (!p.isMatched()) return false;
        }
        return true;
    }

    public void setLevel(int level) {
        this.level = level;
    }
	
	public void levelUp() {
		this.level++;
		setupLevel();
	}

    public List<MatchPerson> getBoard() {
        return board;
    }
}
