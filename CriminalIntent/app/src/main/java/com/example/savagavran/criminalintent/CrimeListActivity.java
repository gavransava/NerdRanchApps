package com.example.savagavran.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by sava.gavran on 10/12/2016.
 */

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(){
        return new CrimeListFragment();
    }
}