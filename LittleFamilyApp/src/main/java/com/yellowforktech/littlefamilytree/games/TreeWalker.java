package com.yellowforktech.littlefamilytree.games;

import android.content.Context;

import com.yellowforktech.littlefamilytree.activities.tasks.ChildrenLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.ParentsLoaderTask;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 9/3/2015.
 */
public class TreeWalker {
    private List<LittlePerson> people;
    private List<LittlePerson> parents;
    private Context context;
    private LittlePerson selectedPerson;
    private Listener listener;

    public TreeWalker(Context context, LittlePerson person, Listener listener) {
        this.context = context;
        this.selectedPerson = person;
        this.listener = listener;
        people = new ArrayList<>();
    }

    public List<LittlePerson> getParents() {
        return parents;
    }

    public List<LittlePerson> getPeople() {
        return people;
    }

    public void loadFamilyMembers() {
        ParentsLoaderTask ptask = new ParentsLoaderTask(parentsListener, context);
        ptask.execute(selectedPerson);
    }

    private ParentsLoaderTask.Listener parentsListener = new ParentsLoaderTask.Listener() {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            if (family!=null && family.size()>0) {
                for (LittlePerson parent : family) {
                    if (parent.getGender() == GenderType.Female)
                        parent.setRelationship("Mommy");
                    else
                        parent.setRelationship("Daddy");
                    people.add(parent);
                }

                parents = family;

                ChildrenLoaderTask ctask = new ChildrenLoaderTask(siblingListener, context);
                LittlePerson[] people = new LittlePerson[family.size()];
                people = family.toArray(people);
                ctask.execute(people);
            }

            ChildrenLoaderTask ctask2 = new ChildrenLoaderTask(childListener, context);
            ctask2.execute(selectedPerson);
        }
    };

    private ChildrenLoaderTask.Listener childListener = new ChildrenLoaderTask.Listener() {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            if (family!=null && family.size()>0) {
                for (LittlePerson child : family) {
                    if (child.getGender() == GenderType.Female)
                        child.setRelationship("Daughter");
                    else
                        child.setRelationship("Son");
                    people.add(child);
                }
                //view.setFamily(people);
            }
            if (parents==null || parents.size()==0) {
                if (listener!=null) {
                    listener.onComplete(people);
                }
            }
        }
        @Override
        public void onStatusUpdate(String message) { }
    };

    private ChildrenLoaderTask.Listener siblingListener = new ChildrenLoaderTask.Listener() {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            if (family!=null && family.size()>0) {
                for (LittlePerson child : family) {
                    if (child.getGender() == GenderType.Female)
                        child.setRelationship("Sister");
                    else
                        child.setRelationship("Brother");
                    people.add(child);
                }
            }

            ParentsLoaderTask gptask = new ParentsLoaderTask(grandParentListener, context);
            LittlePerson[] people = new LittlePerson[parents.size()];
            people = parents.toArray(people);
            gptask.execute(people);
        }
        @Override
        public void onStatusUpdate(String message) { }
    };

    private ParentsLoaderTask.Listener grandParentListener = new ParentsLoaderTask.Listener() {
        int count = 0;
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            count++;
            if (family!=null && family.size()>0) {
                for (LittlePerson child : family) {
                    if (child.getGender() == GenderType.Female)
                        child.setRelationship("Grand mother");
                    else
                        child.setRelationship("Grand father");
                    people.add(child);
                }
                parents = family;
            }

            if (count<8 && people.size()<4) {
                loadMorePeople();
            }
            //view.setFamily(people);
            if (listener!=null) {
                listener.onComplete(people);
            }
        }
    };

    public void loadMorePeople() {
        ParentsLoaderTask gptask = new ParentsLoaderTask(grandParentListener, context);
        LittlePerson[] people = new LittlePerson[parents.size()];
        people = parents.toArray(people);
        gptask.execute(people);
    }

    public interface Listener {
        public void onComplete(List<LittlePerson> people);
    }
}
