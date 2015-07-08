package org.finlayfamily.littlefamily.activities;

import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.DataService;

import java.lang.reflect.Field;

/**
 * Created by jfinlay on 5/27/2015.
 */
public class AdultsAuthDialog extends DialogFragment {
    private EditText passwordField;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_adult_auth, container, false);
        ImageView logo = (ImageView) v.findViewById(R.id.remoteLogo);
        DataService dataService = DataService.getInstance();
        if (dataService.getServiceType().equals(DataService.SERVICE_TYPE_PHPGEDVIEW)) {
            logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pgv_logo));
        }
        EditText usernameTxt = (EditText) v.findViewById(R.id.txtUsername);
        usernameTxt.setEnabled(false);
        try {
            usernameTxt.setText(dataService.getDBHelper().getProperty(DataService.SERVICE_USERNAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        passwordField = (EditText) v.findViewById(R.id.password);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean authenticated() {
        String password = passwordField.getText().toString();
        DataService dataService = DataService.getInstance();
        try {
            String testToken = dataService.getRemoteService().createEncodedAuthToken(dataService.getDBHelper().getProperty(DataService.SERVICE_USERNAME), password);
            if (dataService.getRemoteService().getEncodedAuthToken().equals(testToken)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
