package com.yellowforktech.littlefamilytree.games;

import android.content.Context;

import com.yellowforktech.littlefamilytree.activities.tasks.ChildrenLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.ParentsLoaderTask;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by jfinlay on 9/3/2015.
 */
public class TreeWalker {
    private List<LittlePerson> people;
    private List<LittlePerson> parents;
    private LinkedList<LittlePerson> loadQueue;
    private Set<Integer> usedPeople;
    private Context context;
    private LittlePerson selectedPerson;
    private Listener listener;

    public TreeWalker(Context context, LittlePerson person, Listener listener) {
        this.context = context;
        this.selectedPerson = person;
        this.listener = listener;
        people = new ArrayList<>();
        usedPeople = new HashSet<>();
        loadQueue = new LinkedList<>();
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
                    if (!usedPeople.contains(parent.getId())) {
                        people.add(parent);
                        usedPeople.add(parent.getId());
                        loadQueue.add(parent);
                    }
                }

                parents = family;

                ChildrenLoaderTask ctask = new ChildrenLoaderTask(siblingListener, context, false);
                LittlePerson[] people = new LittlePerson[family.size()];
                people = family.toArray(people);
                ctask.execute(people);
            }

            ChildrenLoaderTask ctask2 = new ChildrenLoaderTask(childListener, context, false);
            ctask2.execute(selectedPerson);
        }
    };

    private ChildrenLoaderTask.Listener childListener = new ChildrenLoaderTask.Listener() {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            if (family!=null && family.size()>0) {
                for (LittlePerson child : family) {
                    if (!usedPeople.contains(child.getId())) {
                        people.add(child);
                        usedPeople.add(child.getId());
                        if (child.getTreeLevel() <= 2) {
                            loadQueue.add(child);
                        }
                    }
                }
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
                    if (!usedPeople.contains(child.getId())) {
                        people.add(child);
                        usedPeople.add(child.getId());
                        if (child.getTreeLevel() <= 2) {
                            loadQueue.add(child);
                        }
                    }
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
                    if (!usedPeople.contains(child.getId())) {
                        people.add(child);
                        usedPeople.add(child.getId());
                        loadQueue.add(child);
                    }
                }
                parents = family;
            }

            if (count<8 && people.size()<4) {
                loadMorePeople();
            }
            else if (listener!=null) {
                listener.onComplete(people);
            }
        }
    };

    public void loadMorePeople() {
        if (loadQueue.size()>0){
            LittlePerson person = loadQueue.poll();
            if (person.getTreeLevel() <= 2) {
                ChildrenLoaderTask ctask = new ChildrenLoaderTask(siblingListener, context, false);
                ctask.execute(person);
            } else {
                ParentsLoaderTask gptask = new ParentsLoaderTask(grandParentListener, context);
                gptask.execute(person);
            }
        } else {
            //-- no more people in the queue so start over
            usedPeople.clear();
            loadFamilyMembers();
        }
    }

    public interface Listener {
        public void onComplete(List<LittlePerson> people);
    }
}
