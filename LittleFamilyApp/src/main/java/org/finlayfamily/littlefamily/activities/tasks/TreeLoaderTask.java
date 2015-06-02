package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.TreeNode;
import org.gedcomx.types.GenderType;

import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class TreeLoaderTask extends AsyncTask<LittlePerson, Integer, TreeNode> {
    private Listener listener;
    private Context context;
    private DataService dataService;
    private int startingDepth;

    public TreeLoaderTask(Listener listener, Context context, int startingDepth) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
        this.startingDepth = startingDepth;
    }

    @Override
    protected TreeNode doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting TreeLoaderTask.doInBackground "+persons);
        TreeNode root = new TreeNode();
        for (LittlePerson person : persons) {
            try {
                root.setPerson(person);
                root.setRelationship("");
                root.setDepth(startingDepth);
                addParents(root);
                addChildren(root, 1);
                addSpouses(root);
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        return root;
    }

    protected void addParents(TreeNode node) throws Exception {
        if (node.getDepth() > startingDepth+2) return;
        LittlePerson person = node.getPerson();
        List<LittlePerson> parents = dataService.getParents(person);
        if (parents!=null && parents.size()>0) {
            if (parents.get(0).getGender()== GenderType.Female) {
                TreeNode mom = new TreeNode();
                mom.setPerson(parents.get(0));
                node.setRight(mom);
                mom.setDepth(node.getDepth() + 1);
                setAncestralRelationship(mom);
                addParents(mom);
            } else {
                TreeNode dad = new TreeNode();
                dad.setPerson(parents.get(0));
                node.setLeft(dad);
                dad.setDepth(node.getDepth() + 1);
                setAncestralRelationship(dad);
                addParents(dad);
            }
            if (parents.size()>1) {
                TreeNode dad = new TreeNode();
                dad.setPerson(parents.get(1));
                if (node.getLeft()==null) {
                    node.setLeft(dad);
                }
                else if (node.getRight()==null) {
                    node.setRight(dad);
                }
                dad.setDepth(node.getDepth()+1);
                setAncestralRelationship(dad);
                addParents(dad);
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

    protected void setAncestralRelationship(TreeNode node) {
        String rel = "";
        for(int g=3; g<=node.getDepth(); g++) {
            rel += "Great ";
        }
        if (node.getDepth()>=2) {
            rel += "Grand ";
        }
        if (node.getPerson().getGender()==GenderType.Female) {
            rel += "Mother";
        }
        else if (node.getPerson().getGender()==GenderType.Male) {
            rel += "Father";
        } else {
            rel += "Parent";
        }
        node.setRelationship(rel);
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