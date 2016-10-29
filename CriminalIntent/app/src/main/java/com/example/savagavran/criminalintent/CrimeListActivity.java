package com.example.savagavran.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.UUID;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks{

    @Override
    protected Fragment createFragment(){
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onCrimeSelected(Crime crime, boolean mSubtitleVisible, int requestCode) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId(), mSubtitleVisible);
            startActivityForResult(intent, requestCode);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if(requestCode == CrimeListFragment.REQUEST_DELETE_CRIME){
            FragmentManager manager = getSupportFragmentManager();
            CrimeListFragment listFragment = (CrimeListFragment)
                    manager.findFragmentById(R.id.fragment_container);
            listFragment.onActivityResult(requestCode, resultCode, data);
        }
        else if(requestCode == CrimeFragment.REQUEST_DATE) {
            FragmentManager manager = getSupportFragmentManager();
            CrimeFragment listFragment = (CrimeFragment)
                    manager.findFragmentById(R.id.detail_fragment_container);
            listFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
