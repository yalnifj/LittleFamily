package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.TreeNode;
import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class TreeLoaderTask extends AsyncTask<LittlePerson, Integer, TreeNode> {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public TreeLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected TreeNode doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting TreeLoaderTask.doInBackground "+persons);
        TreeNode root = new TreeNode();
        for (LittlePerson person : persons) {
            try {
                root.setPerson(person);
                List<LittlePerson> parents = dataService.getParents(person);
                if (parents!=null && parents.size()>0) {
                    if (parents.get(0).getGender()== GenderType.Female) {
                        TreeNode mom = new TreeNode();
                        mom.setPerson(parents.get(0));
                        root.setRight(mom);
                    } else {
                        TreeNode dad = new TreeNode();
                        dad.setPerson(parents.get(0));
                        root.setLeft(dad);
                    }
                    if (parents.size()>1) {
                        TreeNode dad = new TreeNode();
                        dad.setPerson(parents.get(1));
                        if (root.getLeft()==null) {
                            root.setLeft(dad);
                        }
                        else if (root.getRight()==null) {
                            root.setRight(dad);
                        }
                    }
                }
                List<LittlePerson> children = dataService.getChildren(person);
                if (children!=null) {
                    root.setChildren(children);
                }
                List<LittlePerson> spouses = dataService.getSpouses(person);
                if (spouses!=null) {
                    root.setSpouses(spouses);
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        return root;
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