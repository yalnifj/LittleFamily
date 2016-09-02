package com.yellowforktech.littlefamilytree.activities;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.db.FireHelper;

import java.lang.reflect.Field;

/**
 * Created by jfinlay on 5/27/2015.
 */
public class EnterCodeDialog extends DialogFragment {
	private EditText codeField;
    private TextView statusText;
    private ProgressBar spinner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View v = inflater.inflate(R.layout.fragment_enter_code, container, false);

        codeField = (EditText) v.findViewById(R.id.code);
        statusText = (TextView) v.findViewById(R.id.status);
        spinner = (ProgressBar) v.findViewById(R.id.spinner);

        ImageView closeBtn = (ImageView) v.findViewById(R.id.closeImage);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterCodeDialog.this.dismissAllowingStateLoss();
            }
        });

        boolean premium = false;
        String premStr = null;
        try {
            premStr = DataService.getInstance().getDBHelper().getProperty(LittleFamilyActivity.PROP_HAS_PREMIUM);
            if ("true".equals(premStr)) {
                premium = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (premium) {
            codeField.setEnabled(false);
            statusText.setText(getResources().getString(R.string.already_premium));
        }

        Button validateBtn = (Button) v.findViewById(R.id.validateBtn);
        if (premium) validateBtn.setVisibility(View.GONE);
        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                statusText.setText("Validating code...");

                String code = codeField.getText().toString();
                if (code.isEmpty()) {
                    spinner.setVisibility(View.INVISIBLE);
                    statusText.setText("Please enter a code.");
                }

                try {
                    final String username = DataService.getInstance().getDBHelper().getProperty(DataService.SERVICE_USERNAME);
                    FireHelper.getInstance().validateCode(username, code, new FireHelper.ValidateListener() {
                        @Override
                        public void results(boolean valid, String status) {
                            spinner.setVisibility(View.INVISIBLE);
                            if (valid) {
                                statusText.setText("Code successfully validated.");
                                try {
                                    DataService.getInstance().getDBHelper().saveProperty(LittleFamilyActivity.PROP_HAS_PREMIUM, "true");
                                    String serviceType = DataService.getInstance().getServiceType();
                                    FireHelper.getInstance().createOrUpdateUser(username, serviceType, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                statusText.setText("Unable to validate code. "+status);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
}
