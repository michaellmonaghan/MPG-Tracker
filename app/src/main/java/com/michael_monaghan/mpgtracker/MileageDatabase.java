package com.michael_monaghan.mpgtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Michael on 8/9/2014.
 * Provides access to the Mileage Database stored on the device.
 * Uses an SQLite3 database and stores each car's data in a separate table
 */
public class MileageDatabase {

    private final Context context;
    private MileageDatabaseOpenHelper openHelper;

    private static final String TIME_COLUMN = "time";
    private static final String ODOMETER_COLUMN = "odometer";
    private static final String GAS_FILLED_COLUMN = "gas_filled";
    private static final String FULL_TANK_COLUMN = "full_tank";
    private static final String MISSING_PREVIOUS_ENTRY_COLUMN = "missing_previous_entry";
    private static final String ID_COLUMN = "rowid";


    public MileageDatabase(Context context) {
        this.context = context;
        openHelper = new MileageDatabaseOpenHelper(context);
    }

    private SQLiteDatabase getWritableDatabase() {
        return openHelper.getWritableDatabase();
    }

    private SQLiteDatabase getReadableDatabase() {
        return openHelper.getReadableDatabase();
    }

    /**
     * @return the names of the car tables in the database.
     */
    public String[] getCarNames() {
        SQLiteDatabase db = getReadableDatabase();
        String[] names = getCarNames(db);
        db.close();
        return names;
    }
    private static String[] getCarNames(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        c.moveToFirst();
        String[] s = new String[c.getCount() - 1]; // There exists a table called android_metadata that is ignored.
        int index = c.getColumnIndex("name");
        int i = 0;
        while (!c.isAfterLast()) {
            String name = c.getString(index);
            if (!name.equals("android_metadata"))
                s[i++] = name;
            c.moveToNext();
        }
        c.close();
        return s;
    }

    /**
     * Adds a car to the database
     * @param carName The name of the car to add
     * @return Whether the car already exists.
     */
    public boolean addCar(String carName) {
        SQLiteDatabase db = getWritableDatabase();
        boolean exists = addCar(db, carName);
        db.close();
        return exists;
    }

    private static boolean addCar(SQLiteDatabase db, String carName) {
        if (carExists(db, carName))
            return true;
        else {
            db.execSQL("CREATE TABLE '" + carName + "'(" + ODOMETER_COLUMN + " REAL, " + GAS_FILLED_COLUMN + " REAL, " + FULL_TANK_COLUMN + " INTEGER, " + MISSING_PREVIOUS_ENTRY_COLUMN + " INTEGER, " + TIME_COLUMN + " INTEGER);");
            return false;
        }
    }

    /**
     * Checks if the car table carName exists
     * @param db A readable database.
     * @param carName Car table name.
     * @return Whether it exists.
     */
    private static boolean carExists(SQLiteDatabase db, String carName) {
        String [] names = getCarNames(db);
        for (String n : names) {
            if (carName.equals(n)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an entry to a car's table.
     * @param carName The name of the car's table.
     * @param entry The UserEntry to add.
     */
    public void addEntry(String carName, UserEntry entry) {
        SQLiteDatabase db = getWritableDatabase();
        addEntry(db, carName, entry);
        db.close();
    }

    private static void addEntry(SQLiteDatabase db, String carName, UserEntry entry) {

        ContentValues cv = new ContentValues();
        cv.put(ODOMETER_COLUMN, entry.getOdometer());
        cv.put(GAS_FILLED_COLUMN, entry.getGasFilled());
        cv.put(FULL_TANK_COLUMN, entry.hasFullTank());
        cv.put(TIME_COLUMN, entry.whatTimeIsIt());
        cv.put(MISSING_PREVIOUS_ENTRY_COLUMN, entry.missingPreviousEntry());
        db.insert("'" + carName + "'", null, cv);
    }

    public void deleteEntry(String carName, DatabaseEntry entry) {
        deleteEntry(carName, entry.getId());
    }

    public void deleteEntry(String carName, long id) {
        SQLiteDatabase db = getWritableDatabase();
        deleteEntry(db, carName, id);
        db.close();
    }

    private static void deleteEntry(SQLiteDatabase db, String carName, DatabaseEntry entry) {
        deleteEntry(db, carName, entry.getId());
    }

    private static void deleteEntry(SQLiteDatabase db, String carName, long id) {
        db.delete("'" + carName + "'", ID_COLUMN + "=" + id, null);
    }

    /**
     * Returns all of the Entries from a car's table.
     * @param carName The name of the car's table.
     * @return An DatabaseEntry array of all the entries.
     */
    public DatabaseEntry[] getEntries(String carName) {
        SQLiteDatabase db = getReadableDatabase();
        DatabaseEntry[] entries = getEntries(db, carName);
        db.close();
        return entries;
    }

    private static DatabaseEntry[] getEntries(SQLiteDatabase db, String carName) {
        String [] columns = {ID_COLUMN, ODOMETER_COLUMN, GAS_FILLED_COLUMN, FULL_TANK_COLUMN, MISSING_PREVIOUS_ENTRY_COLUMN, TIME_COLUMN};
        Cursor c = db.query("'" + carName + "'", columns, null, null, null, null, ODOMETER_COLUMN + " DESC");

        int count = c.getCount();
        if (count == 0) {
            return new DatabaseEntry[0];
        }

        DatabaseEntry[] entries = new DatabaseEntry[count];
        int id = c.getColumnIndex(ID_COLUMN);
        int odo = c.getColumnIndex(ODOMETER_COLUMN);
        int gas = c.getColumnIndex(GAS_FILLED_COLUMN);
        int full = c.getColumnIndex(FULL_TANK_COLUMN);
        int time = c.getColumnIndex(TIME_COLUMN);
        int noEnt = c.getColumnIndex(MISSING_PREVIOUS_ENTRY_COLUMN);

        c.moveToPosition(0);
        for (int i = 0; i < count; i++) {
            entries[i] = new DatabaseEntry(c.getLong(id), c.getFloat(odo), c.getFloat(gas), c.getInt(full) == 1, c.getInt(noEnt) == 1, c.getLong(time));
            c.moveToNext();
        }
        c.close();

        return entries;
    }

    public void editEntry(DatabaseEntry databaseEntry, String carName) {
        SQLiteDatabase db = getWritableDatabase();
        editEntry(db, databaseEntry, carName);
        db.close();
    }

    private static void editEntry(SQLiteDatabase db, DatabaseEntry databaseEntry, String carName) {
        ContentValues cv = new ContentValues();
        cv.put(ODOMETER_COLUMN, databaseEntry.getOdometer());
        cv.put(GAS_FILLED_COLUMN, databaseEntry.getGasFilled());
        cv.put(FULL_TANK_COLUMN, databaseEntry.hasFullTank());
        cv.put(MISSING_PREVIOUS_ENTRY_COLUMN, databaseEntry.missingPreviousEntry());
        cv.put(TIME_COLUMN, databaseEntry.whatTimeIsIt());
        db.update(carName, cv,ID_COLUMN + "=" + databaseEntry.getId(), null);
    }

    public MileageEntry[] getMileageEntries(String carName) {
        return getMileageEntries(getEntries(carName));
    }

    public static MileageEntry[] getMileageEntries(DatabaseEntry[] entries) {
        if (entries.length == 0) {
            return new MileageEntry[0];
        }

        MileageEntry[] mileageEntries = new MileageEntry[entries.length];

        boolean lastFullTankIndexValid = false;
        int lastFullTankIndex = 0;
        float gasSinceFullTank = 0;
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].hasFullTank()) {
                if (lastFullTankIndexValid) {
                    float mileage;
                    if (gasSinceFullTank != 0) {
                        mileage = (entries[lastFullTankIndex].getOdometer() - entries[i].getOdometer()) / gasSinceFullTank;
                    } else {
                        mileage = 0;
                    }
                    mileageEntries[lastFullTankIndex] = new MileageEntry(entries[lastFullTankIndex], mileage);
                }
                if (entries[i].missingPreviousEntry()) {
                    mileageEntries[i] = new MileageEntry(entries [i]);
                    lastFullTankIndexValid = false;
                } else {
                    lastFullTankIndex = i;
                    lastFullTankIndexValid = true;
                }
                gasSinceFullTank = entries[i].getGasFilled();
            } else {
                if (entries[i].missingPreviousEntry()) {
                    mileageEntries[lastFullTankIndex] = new MileageEntry(entries[lastFullTankIndex]);
                    lastFullTankIndexValid = false;
                } else {
                    if (lastFullTankIndexValid) {
                        gasSinceFullTank += entries[i].getGasFilled();
                    }
                }
                mileageEntries[i] = new MileageEntry(entries[i]);
            }
        }
        if (lastFullTankIndexValid) {
            mileageEntries[lastFullTankIndex] = new MileageEntry(entries[lastFullTankIndex]);
        }

        return mileageEntries;
    }

    public static class DatabaseEntry extends UserEntry {

        private final long id;

        DatabaseEntry(long id, float odometer, float gallons_filled, boolean fullTank, boolean noPreviousEntry, long time) {
            super(odometer, gallons_filled, fullTank, noPreviousEntry, time);
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    public static class UserEntry {

        private final float odometer;
        private final float gasFilled;
        private final boolean fullTank;
        private final long time;
        private final boolean noPreviousEntry;

        UserEntry(float odometer, float gallons_filled, boolean fullTank, boolean noPreviousEntry, long time) {
            this.odometer = odometer;
            this.gasFilled = gallons_filled;
            this.fullTank = fullTank;
            this.time = time;
            this.noPreviousEntry = noPreviousEntry;
        }

        public float getOdometer() {
            return odometer;
        }

        public float getGasFilled() {
            return gasFilled;
        }

        public boolean hasFullTank() {
            return fullTank;
        }

        /**
         *
         * @return Adventure Time!
         */
        public long whatTimeIsIt() {
            return /*Adventure*/ time;
        }

        public Boolean missingPreviousEntry() {
            return noPreviousEntry;
        }
    }

    private static class MileageDatabaseOpenHelper extends SQLiteOpenHelper {
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "Mileage Database.db";

        public MileageDatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //sqLiteDatabase.execSQL("CREATE TABLE Testing(test TEXT)");
            addCar(sqLiteDatabase, "Test Car 1");
            addCar(sqLiteDatabase, "Test Car 2");
            addCar(sqLiteDatabase, "Test Car 3");
            addCar(sqLiteDatabase, "Test Car 4");
            addEntry(sqLiteDatabase, "Test Car 2", new UserEntry(10, 4, true, true, System.currentTimeMillis()));
            addEntry(sqLiteDatabase, "Test Car 2", new UserEntry(35, 3, true, false, System.currentTimeMillis()));
            addEntry(sqLiteDatabase, "Test Car 2", new UserEntry(40, 1, false, false, System.currentTimeMillis()));
            addEntry(sqLiteDatabase, "Test Car 2", new UserEntry(50,3,true, false, System.currentTimeMillis()));
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        }
    }

    public static class MileageEntry extends DatabaseEntry {

        private final float mileage;
        private final boolean unknownMileage;

        @Deprecated
        public DatabaseEntry getEntry() {
            return this;
        }

        public float getMileage() {
            return mileage;
        }

        public boolean unknownMileage() {
            return unknownMileage;
        }

        MileageEntry(DatabaseEntry databaseEntry, float mileage) {
            super(databaseEntry.getId(), databaseEntry.getOdometer(), databaseEntry.getGasFilled(), databaseEntry.hasFullTank(), databaseEntry.missingPreviousEntry(), databaseEntry.whatTimeIsIt());
            this.mileage = mileage;
            unknownMileage = false;
        }

        MileageEntry(DatabaseEntry databaseEntry) {
            super(databaseEntry.getId(), databaseEntry.getOdometer(), databaseEntry.getGasFilled(), databaseEntry.hasFullTank(), databaseEntry.missingPreviousEntry(), databaseEntry.whatTimeIsIt());
            this.mileage = 0;
            unknownMileage = true;
        }
    }
}
