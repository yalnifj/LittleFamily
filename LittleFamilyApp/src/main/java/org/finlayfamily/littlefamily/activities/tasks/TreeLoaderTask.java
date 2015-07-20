package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.TreeNode;

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
        Log.d(this.getClass().getSimpleName(), "Starting TreeLoaderTask.doInBackground "+persons);
        TreeNode root = new TreeNode();
        LittlePerson person = persons[0];
        try {
            /*
            List<LittlePerson> children = dataService.getChildren(person);
            if (children==null || children.size()==0) {
                List<LittlePerson> parents = dataService.getParents(person);
                if (parents!=null && parents.size()>0) {
                    person = parents.get(0);
                }
            }
            */
            root.setPerson(person);
            root.setDepth(startingDepth);
            root.setIsRoot(true);
            addSpouses(root);
            addChildren(root, 1);
            if (maxDepth>1 && root.getChildren()!= null && root.getChildren().size()>0) {
                maxDepth--; //-- reduce the depth if we have children
            }
            addParents(root);
            //-- add siblings if root node has no children
            if (root.getChildren()==null || root.getChildren().size()==0) {
                addChildren(root.getLeft(), 1);
            }
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
        }
        return root;
    }

    protected void addParents(TreeNode node) throws Exception {
        if (node.getDepth() > startingDepth+maxDepth) {
            LittlePerson person = node.getPerson();
            List<LittlePerson> parents = dataService.getParents(person);
            if (parents!=null && parents.size()>0) {
                node.setHasParents(true);
            } else {
                LittlePerson spouse = node.getSpouse();
                if (spouse!=null) {
                    List<LittlePerson> sparents = dataService.getParents(spouse);
                    if (sparents != null && sparents.size() > 0) {
                        node.setHasParents(true);
                    }
                }
            }
            return;
        }

        LittlePerson person = node.getPerson();
        List<LittlePerson> parents = dataService.getParents(person);
        if (parents!=null && parents.size()>0) {
            TreeNode pNode = new TreeNode();
            pNode.setDepth(node.getDepth() + 1);
            pNode.setPerson(parents.get(0));
            if (parents.size()>1) {
                List<LittlePerson> spList = new ArrayList<>();
                spList.add(parents.get(1));
                pNode.setSpouses(spList);
            }
            addParents(pNode);
            node.setLeft(pNode);
        }
        LittlePerson spouse = node.getSpouse();
        if (spouse!=null) {
            List<LittlePerson> sparents = dataService.getParents(spouse);
            if (sparents!=null && sparents.size()>0) {
                TreeNode pNode = new TreeNode();
                pNode.setDepth(node.getDepth()+1);
                pNode.setPerson(sparents.get(0));
                if (sparents.size()>1) {
                    List<LittlePerson> spList = new ArrayList<>();
                    spList.add(sparents.get(1));
                    pNode.setSpouses(spList);
                }
                addParents(pNode);
                node.setRight(pNode);
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
        List<LittlePerson> children = dataService.getChildren(person);
        if (children!=null) {
            node.setChildren(children);
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