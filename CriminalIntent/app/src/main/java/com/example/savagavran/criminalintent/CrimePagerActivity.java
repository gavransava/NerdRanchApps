package com.example.savagavran.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_ID =
            "com.example.savagavran.criminalintent.crime_id";
    private static final String EXTRA_SUBTITLE_VISIBILITY =
            "com.example.savagavran.criminalintent.subtitle_visibility";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private CrimeLab mCrimeLab;
    private boolean mSubtitleVisibility;

    public static Intent newIntent(Context packageContext, UUID crimeId, boolean subtitleVisibility) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        intent.putExtra(EXTRA_SUBTITLE_VISIBILITY, subtitleVisibility);
        return intent;
    }

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        mSubtitleVisibility = (boolean) getIntent().getSerializableExtra(EXTRA_SUBTITLE_VISIBILITY);

        mViewPager = (ViewPager) findViewById(R.id.activity_crime_pager_view_pager);

        mCrimeLab = CrimeLab.get(this);
        mCrimes = mCrimeLab.getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        Crime crime = mCrimeLab.getCrime(crimeId);
        for(Crime c : mCrimes) {
            if(c.getId().equals(crime.getId()))
                mViewPager.setCurrentItem(mCrimes.indexOf(c));
        }

    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        CrimeListFragment.reCreateIntent(intent, mSubtitleVisibility);
        return intent;
    }
}
