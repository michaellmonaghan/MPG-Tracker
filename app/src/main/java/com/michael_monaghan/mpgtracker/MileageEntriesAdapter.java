package com.michael_monaghan.mpgtracker;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import michael_monaghan.mpgtracker.R;

/**
 * Created by Michael on 8/15/2014.
 * Forms an interface to an array of MileageEntries
 */
public class MileageEntriesAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater layoutInflater;
    private MileageDatabase.MileageEntry[] mileageEntries;
    private boolean empty = true;

    // Locale info
    final Locale locale = Locale.getDefault();
    final String odometerUnits, gasUnits, mpgUnits, unknownMileage;

    public MileageEntriesAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        odometerUnits = context.getString(R.string.odometer_units);
        gasUnits = context.getString(R.string.gas_units);
        mpgUnits = context.getString(R.string.mpg_units);
        unknownMileage = context.getString(R.string.unknown_mileage);
    }

    /**
     * Sets the entries to be displayed.
     * @param entries Entries to be displayed.
     */
    public void setMileageEntries(MileageDatabase.MileageEntry[] entries) {
        if (entries == null || entries.length == 0) {
            empty = true;
        } else {
            mileageEntries = entries;
            empty = false;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (empty) {
            return 1;
        } else {
            return mileageEntries.length;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public MileageDatabase.MileageEntry getItem(int i) {
        if(empty) {
            return null;
        } else {
            return mileageEntries[i];
        }
    }

    @Override
    public long getItemId(int i) {
        if (empty) {
            return -1;
        } else {
            return mileageEntries[i].getId();
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return !empty;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (empty) {
            if (view == null) {
                TextView tv = new TextView(context);
                tv.setText (R.string.empty);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                return tv;
            } else {
                return view;
            }
        } else {
            ViewManager vh;
            if (view == null/* || view.getTag() == null*/) {
                view = layoutInflater.inflate(R.layout.mileage_entry_list_view_item, viewGroup, false);
                vh = new ViewManager(view);
                view.setTag(vh);
            } else {
                vh = (ViewManager) view.getTag();
            }
            vh.setViews(mileageEntries[i]);
        }
        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return empty ? 0 : 1;
    }

    private class ViewManager {
        final TextView odometer;
        final TextView gasFilled;
        final ImageView fullTank;
        final TextView mpg;
        final TextView missingEntry;
        ViewManager(View view) {
            odometer = (TextView) view.findViewById(R.id.odometer);
            gasFilled = (TextView) view.findViewById(R.id.gas_filled);
            fullTank = (ImageView) view.findViewById(R.id.full_tank);
            mpg = (TextView) view.findViewById(R.id.mpg);
            missingEntry = (TextView) view.findViewById(R.id.missing_entry);
        }
        void setViews(MileageDatabase.MileageEntry entry) {
            odometer.setText(format(entry.getOdometer()) + " " + odometerUnits);
            gasFilled.setText(format(entry.getGasFilled()) + " " + gasUnits);
            if (entry.hasFullTank()) {
                fullTank.setVisibility(View.GONE);
            } else {
                fullTank.setVisibility(View.VISIBLE);
            }
            if(entry.unknownMileage()){
                mpg.setText(unknownMileage);
            } else {
                mpg.setText(format(entry.getMileage()) + " " + mpgUnits);
            }
            if (entry.missingPreviousEntry()) {
                missingEntry.setVisibility(View.VISIBLE);
            } else {
                missingEntry.setVisibility(View.GONE);
            }
        }
        private String format(float number) {
            if (number == (int) number) {
                return String.format(locale, "%,d", (int) number);
            } else {
                return String.format(locale, "%,.1f", number + .05f); // Round up.
            }
        }
    }
}
