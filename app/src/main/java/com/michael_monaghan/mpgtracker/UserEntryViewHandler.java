package com.michael_monaghan.mpgtracker;

import android.annotation.SuppressLint;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.michael_monaghan.mpgtracker.MileageDatabase.UserEntry;

import michael_monaghan.mpgtracker.R;

/**
 * Created by Michael on 8/21/2014.
 * Handles an Entry View Layout.
 */
public class UserEntryViewHandler {

    // Constants
    private final static String DEFAULT_ODOMETER = "", DEFAULT_GAS = "";
    private final static boolean DEFAULT_FULL_TANK = true, DEFAULT_MISSING_ENTRY = false;
    private final String empty;

    // Views
    public final TextView odometer, gasFilled;
    public final CheckBox fullTank;
    public final CheckBox missingEntry;
    public final Button addEntry;

    // Listeners
    private AddUserEntryListener addUserEntryListener;

    public UserEntryViewHandler(View rootView, AddUserEntryListener addUserEntryListener) {
        this(rootView);
        setAddUserEntryListener(addUserEntryListener);
    }

    public UserEntryViewHandler(View rootView) {
        odometer = (TextView) rootView.findViewById(R.id.odometer);
        gasFilled = (TextView) rootView.findViewById(R.id.gas_filled);
        fullTank = (CheckBox) rootView.findViewById(R.id.full_tank);
        missingEntry = (CheckBox) rootView.findViewById(R.id.missing_entry);
        addEntry = (Button) rootView.findViewById(R.id.add);
        addEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performAddEntry();
            }
        });
        gasFilled.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_UNSPECIFIED){
                    performAddEntry();
                    return true;
                }
                return false;
            }
        });
        empty = rootView.getContext().getString(R.string.empty);
    }

    public boolean performAddEntry() {
        if(addUserEntryListener != null && checkValid()) {
            addUserEntryListener.addEntry(getUserEntry());
            odometer.requestFocus();
            return true;
        } else {
            return false;
        }

    }

    public void clear() {
        odometer.setText(DEFAULT_ODOMETER);
        gasFilled.setText(DEFAULT_GAS);
        fullTank.setChecked(DEFAULT_FULL_TANK);
        missingEntry.setChecked(DEFAULT_MISSING_ENTRY);
    }

    /**
     * Checks to see if the views are empty. Sets an error message on the offending view if it is.
     * @return whether the views are empty or not.
     */
    public boolean checkValid() {
        boolean valid = true;
        if (odometer.getText().length() == 0) {
            odometer.setError(empty);
            valid = false;
        }
        if (gasFilled.getText().length() == 0) {
            gasFilled.setError(empty);
            valid = false;
        }
        return valid;
    }

    public UserEntry getUserEntry() {
        return new UserEntry(
                Float.parseFloat(odometer.getText().toString()),
                Float.parseFloat(gasFilled.getText().toString()),
                fullTank.isChecked(),
                missingEntry.isChecked(),
                System.currentTimeMillis()
        );
    }

    public void setAddUserEntryListener(AddUserEntryListener addUserEntryListener) {
        this.addUserEntryListener = addUserEntryListener;
    }

    public static interface AddUserEntryListener {
        public void addEntry(UserEntry entry);
    }
}
