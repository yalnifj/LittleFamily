package com.yellowforktech.littlefamilytree.games;

import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.TreeNode;

import java.util.Random;

/**
 * Created by jfinlay on 7/23/2015.
 */
public class TreeSearchGame {
    private LittlePerson rootPerson;
    private TreeNode rootNode;
    private LittlePerson targetPerson;
    private TreeNode targetNode;

    public TreeSearchGame() {
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
        targetNode = root;
        while(targetPerson==null) {
            int n = rand.nextInt(4);
            switch(n) {
                case 0:
                    if (targetNode.getLeft()!=null) targetNode = targetNode.getLeft();
                    else {
                        targetPerson = targetNode.getPerson();
                    }
                    break;
                case 1:
                    if (targetNode.getRight()!=null) targetNode = targetNode.getRight();
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
}
