package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.activities.MatchGameActivity;
import org.finlayfamily.littlefamily.data.DataHelper;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class FamilyLoaderTask extends AsyncTask<String, Integer, ArrayList<LittlePerson>> {
    private Person person;
    private Listener listener;
    private Context context;

    public FamilyLoaderTask(Person person, Listener listener, Context context) {
        this.person = person;
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected ArrayList<LittlePerson> doInBackground(String[] params) {
        ArrayList<LittlePerson> familyMembers = new ArrayList<>();
        FamilySearchService service = FamilySearchService.getInstance();
        try {
            List<Relationship> family = service.getCloseRelatives(person.getId());
            LittlePerson littlePerson = DataHelper.buildLittlePerson(person, context);
            familyMembers.add(littlePerson);

            for(Relationship r : family) {
                Log.d("onPostExecute", "Relationship " + r.getKnownType() + " with " + r.getPerson1().getResourceId() + ":" + r.getPerson2().getResourceId());
                if (!r.getPerson1().getResourceId().equals(littlePerson.getFamilySearchId())) {
                    Person fsPerson = service.getPerson(r.getPerson1().getResourceId());
                    LittlePerson person = DataHelper.buildLittlePerson(fsPerson, context);
                    familyMembers.add(person);
                }
                if (!r.getPerson2().getResourceId().equals(littlePerson.getFamilySearchId())) {
                    Person fsPerson = service.getPerson(r.getPerson2().getResourceId());
                    LittlePerson person = DataHelper.buildLittlePerson(fsPerson, context);
                    familyMembers.add(person);
                }
            }
        } catch(FamilySearchException e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
            Toast.makeText(context, "Error communicating with FamilySearch. " + e, Toast.LENGTH_LONG).show();
        }
        return familyMembers;
    }

    @Override
    protected void onPostExecute(ArrayList<LittlePerson> familyMembers) {
        if (listener!=null) {
            listener.onComplete(familyMembers);
        }
    }

    public interface Listener {
        public void onComplete(ArrayList<LittlePerson> family);
    }
}