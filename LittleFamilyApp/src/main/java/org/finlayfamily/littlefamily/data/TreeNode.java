package org.finlayfamily.littlefamily.data;

import java.util.List;

/**
 * Created by Parents on 5/29/2015.
 */
public class TreeNode {
    private LittlePerson person;
    private TreeNode left;
    private TreeNode right;
    private String relationship;
    private int depth;
    private List<LittlePerson> spouses;
    private List<LittlePerson> children;

    public LittlePerson getPerson() {
        return person;
    }

    public void setPerson(LittlePerson person) {
        this.person = person;
    }

    public TreeNode getLeft() {
        return left;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }

    public List<LittlePerson> getChildren() {
        return children;
    }

    public void setChildren(List<LittlePerson> children) {
        this.children = children;
    }

    public List<LittlePerson> getSpouses() {
        return spouses;
    }

    public void setSpouses(List<LittlePerson> spouses) {
        this.spouses = spouses;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
