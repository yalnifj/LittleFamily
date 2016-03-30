package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.TreeNode;

import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class TreeLoaderTask extends AsyncTask<LittlePerson, Integer, TreeNode> {
    private Listener listener;
    private Context context;
    private DataService dataService;
    private int startingDepth;
    private int maxDepth;

    public TreeLoaderTask(Listener listener, Context context, int startingDepth, int maxDepth) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
        this.startingDepth = startingDepth;
        this.maxDepth = maxDepth;
    }

    @Override
    protected TreeNode doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting TreeLoaderTask.doInBackground " + persons);
        TreeNode root = new TreeNode();
        LittlePerson person = persons[0];
        try {
            root.setPerson(person);
            root.setDepth(startingDepth);
            root.setIsRoot(true);
            addSpouses(root);
            if (startingDepth < 2 ) {
                addChildren(root, 1);
                if (maxDepth > 1 && root.getChildren() != null && root.getChildren().size() > 0) {
                    maxDepth--; //-- reduce the depth if we have children
                }
            }
            addParents(root, false);
            //-- add siblings if root node has no children
            if (startingDepth < 2 && (root.getChildren()==null || root.getChildren().size()==0)) {
                TreeNode cn = root.getLeft();
                if (cn==null) cn = root.getRight();
                if (cn!=null) {
                    addChildren(cn, 1);
                }
                root.setIsChild(true);
            }
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
        }
        return root;
    }

    protected void addParents(TreeNode node, boolean isInLaw) throws Exception {
        if (node.getDepth() > startingDepth+maxDepth) {
            LittlePerson person = node.getPerson();
            if (person!=null) {
                List<LittlePerson> parents = dataService.getParents(person);
                if (parents != null && parents.size() > 0) {
                    node.setHasParents(true);
                } else {
                    LittlePerson spouse = node.getSpouse();
                    if (spouse != null) {
                        List<LittlePerson> sparents = dataService.getParents(spouse);
                        if (sparents != null && sparents.size() > 0) {
                            node.setHasParents(true);
                        }
                    }
                }
            }
            return;
        }

        TreeNode pNode = new TreeNode();
        pNode.setDepth(node.getDepth() + 1);
		pNode.setIsInLaw(isInLaw);
        LittlePerson person = node.getPerson();
        if (person!=null) {
            List<LittlePerson> parents = dataService.getParents(person);
            if (parents != null && parents.size() > 0) pNode.setPerson(parents.get(0));
            if (parents.size() > 1) {
                List<LittlePerson> spList = new ArrayList<>();
                spList.add(parents.get(1));
                pNode.setSpouses(spList);
            }
        }
        addParents(pNode,isInLaw);

        if (person!=null && person.getGender()== GenderType.Female) {
            node.setRight(pNode);
        } else {
            node.setLeft(pNode);
        }

        if (node.getDepth()>0 || node.getSpouse()!=null) {
			if (node.isRoot() && node.getDepth()==0) isInLaw=true;
            TreeNode prNode = new TreeNode();
            prNode.setDepth(node.getDepth() + 1);
			prNode.setIsInLaw(isInLaw);
            LittlePerson spouse = node.getSpouse();
            if (spouse != null) {
                List<LittlePerson> sparents = dataService.getParents(spouse);
                if (sparents != null && sparents.size() > 0) {
                    prNode.setPerson(sparents.get(0));
                    if (sparents.size() > 1) {
                        List<LittlePerson> spList = new ArrayList<>();
                        spList.add(sparents.get(1));
                        prNode.setSpouses(spList);
                    }
                }
            }
            addParents(prNode, isInLaw);
            if (node.getLeft()!=null || (spouse!=null && spouse.getGender()== GenderType.Female)) {
                node.setRight(prNode);
            } else {
                node.setLeft(prNode);
            }
        }
    }

    protected void addSpouses(TreeNode node) throws Exception {
        List<LittlePerson> spouses = dataService.getSpouses(node.getPerson());
        if (spouses!=null) {
            node.setSpouses(spouses);
        }
    }

    protected void addChildren(TreeNode node, int depth) throws Exception {
        if (depth>1) return;
        LittlePerson person = node.getPerson();
        if (node.getSpouse() != null) {
            List<LittlePerson> children = dataService.getChildrenForCouple(person, node.getSpouse());
            if (children != null) {
                node.setChildren(children);
            }
        } else {
            List<LittlePerson> children = dataService.getChildren(person);
            if (children != null) {
                node.setChildren(children);
            }
        }
    }

    @Override
    protected void onPostExecute(TreeNode root) {
        if (listener!=null) {
            listener.onComplete(root);
        }
    }

    public interface Listener {
        public void onComplete(TreeNode node);
    }
}
