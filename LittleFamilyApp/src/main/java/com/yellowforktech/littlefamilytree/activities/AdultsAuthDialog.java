package com.yellowforktech.littlefamilytree.activities;

import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by jfinlay on 5/27/2015.
 */
public class AdultsAuthDialog extends DialogFragment {
	private EditText passwordField;
	private AuthCompleteAction action;
	
	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		action = (AuthCompleteAction) args.getSerializable("action");
	}
	
    
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

        ImageView closeBtn = (ImageView) v.findViewById(R.id.closeImage);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LittleFamilyActivity activity = (LittleFamilyActivity) getActivity();
                activity.hideAdultAuthDialog();
            }
        });

        Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LittleFamilyActivity activity = (LittleFamilyActivity) getActivity();
                activity.hideAdultAuthDialog();
            }
        });

        Button signInBtn = (Button) v.findViewById(R.id.signInBtn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				if (action!=null)
                	action.doAction(authenticated());
            }
        });

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
            Log.e(getClass().getName(), "error closing loadingdialog", e);
        } catch (IllegalAccessException e) {
            Log.e(getClass().getName(), "error closing loadingdialog", e);
        }
    }

    public boolean authenticated() {
        String password = passwordField.getText().toString();
        DataService dataService = DataService.getInstance();
        try {
            String testToken = dataService.getRemoteService().createEncodedAuthToken(dataService.getDBHelper().getProperty(DataService.SERVICE_USERNAME), password);
            String goodToken = dataService.getEncryptedProperty(dataService.getRemoteService().getClass().getSimpleName() + DataService.SERVICE_TOKEN);
            if (goodToken.equals(testToken)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
	
	public interface AuthCompleteAction extends Serializable {
		public void doAction(boolean success);
	}
}
