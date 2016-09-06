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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by jfinlay on 5/27/2015.
 */
public class AdultsAuthDialog extends DialogFragment {
	private EditText passwordField;
	private AuthCompleteAction action;
    private CheckBox rememberChk;
    private boolean isMyHeritage = false;
    private LittlePerson person = null;

    public AuthCompleteAction getAction() {
        return action;
    }

    public void setAction(AuthCompleteAction action) {
        this.action = action;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View v = inflater.inflate(R.layout.fragment_adult_auth, container, false);
        ImageView logo = (ImageView) v.findViewById(R.id.remoteLogo);
        DataService dataService = DataService.getInstance();
        if (dataService.getServiceType()!=null) {
            if (dataService.getServiceType().equals(DataService.SERVICE_TYPE_PHPGEDVIEW)) {
                logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pgv_logo));
            } else if (dataService.getServiceType().equals(DataService.SERVICE_TYPE_MYHERITAGE)) {
                logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.myheritage_logo));
                isMyHeritage = true;
            }
        }

        EditText usernameTxt = (EditText) v.findViewById(R.id.txtUsername);
        if (isMyHeritage) {
            usernameTxt.setVisibility(View.GONE);

            try {
                person = DataService.getInstance().getDBHelper().getFirstPerson();
                TextView explanation = (TextView) v.findViewById(R.id.authExpl);
                if (person.getBirthDate()==null) {
                    List<LittlePerson> people = DataService.getInstance().getDBHelper().getRelativesForPerson(person.getId());
                    for(LittlePerson p : people) {
                        if (p.getBirthDate() != null) {
                            person = p;
                            break;
                        }
                    }
                }
                explanation.setText(getResources().getString(R.string.adult_auth_explanation2, person.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            usernameTxt.setEnabled(false);
            try {
                usernameTxt.setText(dataService.getDBHelper().getProperty(DataService.SERVICE_USERNAME));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        passwordField = (EditText) v.findViewById(R.id.password);
        if (isMyHeritage) {
            passwordField.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            passwordField.setHint(R.string.birthYear);
        }

        ImageView closeBtn = (ImageView) v.findViewById(R.id.closeImage);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LittleFamilyActivity activity = (LittleFamilyActivity) getActivity();
                if (action!=null)
                    action.onClose();
                else
                    activity.hideAdultAuthDialog();
            }
        });

        Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LittleFamilyActivity activity = (LittleFamilyActivity) getActivity();
                if (action!=null)
                    action.onClose();
                else
                    activity.hideAdultAuthDialog();
            }
        });

        Button signInBtn = (Button) v.findViewById(R.id.signInBtn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LittleFamilyActivity activity = (LittleFamilyActivity) getActivity();
				if (action!=null)
                	action.doAction(authenticated());
                else
                    activity.hideAdultAuthDialog();
            }
        });

        rememberChk = (CheckBox) v.findViewById(R.id.rememberChk);

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
        if (isMyHeritage) {
            if (person.getBirthDate()==null) {
                if (password.isEmpty()) return true;
                return false;
            }
            DateFormat df = new SimpleDateFormat("yyyy");
            String realYear = df.format(person.getBirthDate());
            if (realYear.equals(password)) {
                return true;
            }
        } else {
            DataService dataService = DataService.getInstance();
            try {
                String testToken = dataService.getRemoteService().createEncodedAuthToken(dataService.getDBHelper().getProperty(DataService.SERVICE_USERNAME), password);
                String goodToken = dataService.getEncryptedProperty(dataService.getRemoteService().getClass().getSimpleName() + DataService.SERVICE_TOKEN);
                if (goodToken.equals(testToken)) {
                    if (rememberChk.isChecked()) {
                        Date now = new Date();
                        DataService.getInstance().getDBHelper().saveProperty(DataService.PROPERY_REMEMBER_ME, String.valueOf(now.getTime()));
                    } else {
                        DataService.getInstance().getDBHelper().saveProperty(DataService.PROPERY_REMEMBER_ME, "0");
                    }
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
	
	public interface AuthCompleteAction extends Serializable {
		public void doAction(boolean success);
        public void onClose();
	}
}
