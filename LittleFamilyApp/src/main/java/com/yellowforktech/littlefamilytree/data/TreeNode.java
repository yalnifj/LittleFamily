package com.yellowforktech.littlefamilytree.data;

import org.gedcomx.types.GenderType;

import java.util.List;

/**
 * Created by Parents on 5/29/2015.
 */
public class TreeNode {
    private LittlePerson person;
    private List<LittlePerson> spouses;
    private int shownSpouse = 0;
    private TreeNode left;
    private TreeNode right;
    private int depth;
    private boolean isRoot;
    private boolean hasParents;
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

    public int getShownSpouse() {
        return shownSpouse;
    }

    public void setShownSpouse(int shownSpouse) {
        this.shownSpouse = shownSpouse;
    }

    public boolean isHasParents() {
        return hasParents;
    }

    public void setHasParents(boolean hasParents) {
        this.hasParents = hasParents;
    }

    public LittlePerson getSpouse() {
        if (this.spouses==null) return null;
        if (this.spouses.size()<=this.shownSpouse) return null;
        return this.spouses.get(this.shownSpouse);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setIsRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public String getAncestralRelationship(LittlePerson p) {
        String rel = "";
        for(int g=3; g<=this.getDepth(); g++) {
            rel += "Great ";
        }
        if (this.getDepth()>=2) {
            rel += "Grand ";
        }
        if (p.getGender()== GenderType.Female) {
            if (this.getDepth()==0) {
                if (this.isRoot()) {
                    if (p==this.getPerson()) rel = "You";
                    else rel = "Wife";
                } else {
                    rel = "Sister";
                }
            } else {
                rel += "Mother";
            }
        }
        else if (p.getGender()==GenderType.Male) {
            if (this.getDepth()==0) {
                if (this.isRoot()) {
                    if (p==this.getPerson()) rel = "You";
                    else rel = "Husband";
                } else {
                    rel = "Brother";
                }
            } else {
                rel += "Father";
            }
        } else {
            rel += "Parent";
        }
        return rel;
    }
}