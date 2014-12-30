package org.finlayfamily.littlefamily.activities.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.conclusion.Person;

import java.util.List;

public class FamilyLoader extends AsyncTaskLoader<List<Person>> {
    public FamilyLoader(Context context, Bundle args) {
        super(context);
    }

    @Override
    public List<Person> loadInBackground() {
        FamilySearchService service = FamilySearchService.getInstance();
        try {
            List<Person> family = service.getCloseRelatives();
            return family;
        } catch(FamilySearchException e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
            Toast.makeText(this.getContext(), "Error communicating with FamilySearch. " + e, Toast.LENGTH_LONG).show();
        }
        return null;
    }
}
