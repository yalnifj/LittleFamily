package com.yellowforktech.littlefamilytree.games;

import android.content.Context;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.TreeNode;

import org.gedcomx.types.GenderType;

import java.util.Random;

/**
 * Created by jfinlay on 7/23/2015.
 */
public class TreeSearchGame {
    private Context context;
    private LittlePerson rootPerson;
    private TreeNode rootNode;
    private LittlePerson targetPerson;
    private TreeNode targetNode;
    private boolean complete;
    private TreeClue[] clues;
    private int clueNumber;

    public TreeSearchGame(Context context) {
        this.context = context;
        complete = true;
        clues = new TreeClue[4];
        clues[0] = new GenderTreeClue();
        clues[1] = new NameTreeClue();
        clues[2] = new FullNameTreeClue();
        clues[3] = new RelationshipTreeClue();
    }

    public boolean isComplete() {
        return complete;
    }

    public TreeNode getTargetNode() {
        return targetNode;
    }

    public LittlePerson getTargetPerson() {
        return targetPerson;
    }

    public void findRandomPerson(TreeNode root) {
        rootNode = root;
        rootPerson = root.getPerson();
        Random rand = new Random();
        targetPerson = null;
        int upDown = rand.nextInt(3);
        if (upDown==0) {
            if (root.getChildren()!=null && root.getChildren().size()>0) {
                targetNode = root;
                targetPerson = root.getChildren().get(rand.nextInt(root.getChildren().size()));
            } else if (root.getLeft()!=null && root.getLeft().getChildren()!=null && root.getLeft().getChildren().size()>0) {
                targetNode = root.getLeft();
                targetPerson = root.getLeft().getChildren().get(rand.nextInt(root.getLeft().getChildren().size()));
            }
        }
        if (targetPerson==null) {
            targetNode = root;
            int p = rand.nextInt(2);
            if (targetNode.getLeft() != null) targetNode = targetNode.getLeft();
            if (p > 0 && targetNode.getRight() != null) targetNode = targetNode.getRight();
            while (targetPerson == null) {
                int n = rand.nextInt(4);
                switch (n) {
                    case 0:
                        if (targetNode.getLeft() != null) targetNode = targetNode.getLeft();
                        else {
                            targetPerson = targetNode.getPerson();
                        }
                        break;
                    case 1:
                        if (targetNode.getRight() != null) targetNode = targetNode.getRight();
                        else targetPerson = targetNode.getSpouse();
                        break;
                    case 2:
                        targetPerson = targetNode.getPerson();
                        break;
                    case 3:
                        targetPerson = targetNode.getSpouse();
                        break;
                }
            }
        }

        complete = false;
        clueNumber = rand.nextInt(clues.length);
    }

    public String getClueText() {
        return clues[clueNumber].getClueText();
    }

    public void nextClue() {
        clueNumber++;
        if (clueNumber >= clues.length) clueNumber = 0;
    }

    public boolean isMatch(TreeNode node, boolean isSpouse) {
        TreeClue clue = clues[clueNumber];
        boolean ret = clue.isMatch(node, isSpouse);
        if (ret) complete = true;
        return ret;
    }

    public interface TreeClue {
        public String getClueText();
        public boolean isMatch(TreeNode node, boolean isSpouse);
    }

    public class NameTreeClue implements TreeClue {
        @Override
        public String getClueText() {
            String clue = String.format(context.getResources().getString(R.string.clue_find_by_name), targetPerson.getGivenName());
            return clue;
        }

        @Override
        public boolean isMatch(TreeNode node, boolean isSpouse) {
            LittlePerson person = node.getPerson();
            if (isSpouse) person = node.getSpouse();
            if (person!=null) {
                if (person==targetPerson) return true;
                if (person.getGivenName().equalsIgnoreCase(targetPerson.getGivenName())) return true;
            }
            return false;
        }
    }

    public class FullNameTreeClue implements TreeClue {
        @Override
        public String getClueText() {
            String clue = String.format(context.getResources().getString(R.string.clue_find_by_name), targetPerson.getName());
            return clue;
        }

        @Override
        public boolean isMatch(TreeNode node, boolean isSpouse) {
            LittlePerson person = node.getPerson();
            if (isSpouse) person = node.getSpouse();
            if (person!=null) {
                if (person==targetPerson) return true;
                if (person.getName().equalsIgnoreCase(targetPerson.getName())) return true;
            }
            return false;
        }
    }

    public class RelationshipTreeClue implements TreeClue {
        @Override
        public String getClueText() {
            String relationship = targetNode.getAncestralRelationship(targetPerson);
            String clue = String.format(context.getResources().getString(R.string.clue_find_by_relationship), relationship);
            return clue;
        }

        @Override
        public boolean isMatch(TreeNode node, boolean isSpouse) {
            LittlePerson person = node.getPerson();
            if (isSpouse) person = node.getSpouse();
            if (person!=null) {
                if (person==targetPerson) return true;
                String relationship1 = targetNode.getAncestralRelationship(targetPerson);
                String relationship2 = node.getAncestralRelationship(person);
                if (relationship1.equals(relationship2)) return true;
            }
            return false;
        }
    }

    public class GenderTreeClue implements TreeClue {
        @Override
        public String getClueText() {
            String genderType = context.getResources().getString(R.string.boy);
            if (targetPerson.getGender()== GenderType.Female) {
                genderType = context.getResources().getString(R.string.girl);
            }
            String clue = String.format(context.getResources().getString(R.string.clue_find_by_gender), genderType);
            return clue;
        }

        @Override
        public boolean isMatch(TreeNode node, boolean isSpouse) {
            LittlePerson person = node.getPerson();
            if (isSpouse) person = node.getSpouse();
            if (person!=null) {
                if (person==targetPerson) return true;
                GenderType gender1 = targetPerson.getGender();
                GenderType gender2 = person.getGender();
                if (gender1.equals(gender2)) return true;
            }
            return false;
        }
    }
}
