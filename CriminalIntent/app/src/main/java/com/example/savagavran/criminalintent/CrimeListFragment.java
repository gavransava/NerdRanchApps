package com.example.savagavran.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CrimeListFragment extends Fragment {

    private final String DATE_FORMAT1 = "EEE MMM d";
    private final String DATE_FORMAT2 = "z y";
    private static final String SAVED_SUBTITLE_VISIBLE_INTENT = "subtitle_intent";
    private static final String SAVED_SUBTITLE_VISIBLE_BUNDLE = "subtitle_bundle";
    private static final int REQUEST_DELETE_CRIME = 0;
    private static final int BACK_INSERTING = 0;
    private static final String DELETE_CRIME = "delete_crime";
    private RecyclerView mCrimeRecycleView;
    private LinearLayout mNoCrimesLayout;
    private CrimeAdapter mAdapter;
    private UUID mCrimePosition;
    private Button mNewCrimeButton;
    private boolean mSubtitleVisible;

    public static Intent reCreateIntent(Intent intent, boolean subtitleVisible) {
        intent.putExtra(SAVED_SUBTITLE_VISIBLE_INTENT, subtitleVisible);
        return intent;
    }

    public static Intent deleteCrime(Intent intent, UUID crimeId) {
        intent.putExtra(DELETE_CRIME, crimeId);
        return intent;
    }

    public CrimeListFragment() {

    }

    private void addNewCrime() {
        Crime crime = new Crime();
        CrimeLab.get(getActivity()).addCrime(crime);
        Intent intent = CrimePagerActivity
                .newIntent(getActivity(), crime.getId(), mSubtitleVisible);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity().getIntent()
                .getSerializableExtra(SAVED_SUBTITLE_VISIBLE_INTENT) != null)
            mSubtitleVisible = (boolean) getActivity().getIntent().
                    getSerializableExtra(SAVED_SUBTITLE_VISIBLE_INTENT);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE_BUNDLE);
        }

        mNoCrimesLayout = (LinearLayout) view.findViewById(R.id.no_crimes);
        mNewCrimeButton = (Button) view.findViewById(R.id.new_crime_button);

        mNewCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewCrime();
            }
        });

        mCrimeRecycleView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE_BUNDLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list,menu);

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if(mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_new_crime:
                addNewCrime();
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);


        if(!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        // Remove the crime for which the title is not set //
        if(crimes.size() != 0) {
            mNoCrimesLayout.setVisibility(View.INVISIBLE);
            if (crimes.get(crimes.size() - 1).getTitle() == null) {
                if(crimes.size() == 1)
                    mNoCrimesLayout.setVisibility(View.VISIBLE);
                crimeLab.removeLastCrime();
            }
        }
        else {
            mNoCrimesLayout.setVisibility(View.VISIBLE);
        }

        if(mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecycleView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            if (mCrimePosition != null) {
                for(Crime c : crimes) {
                    if(c.getId().equals(mCrimePosition))
                        mAdapter.notifyItemChanged(crimes.indexOf(c));
                }
            }
        }
        updateSubtitle();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;

        public CrimeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_date_text_view);
            mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_check_box);
        }

        public void bindCrime(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mergeDateTime(mCrime));
            mSolvedCheckBox.setChecked(mCrime.isSolved());
            mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mCrime.setSolved(isChecked);
                    CrimeLab.get(getActivity()).updateCrime(mCrime);
                }
            });
        }

        @Override
        public void onClick(View v) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(),mCrime.getId(), mSubtitleVisible);
            mCrimePosition = mCrime.getId();
            startActivityForResult(intent, REQUEST_DELETE_CRIME);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            if(crimes.size() > mCrimes.size()) {
                mCrimes = crimes;
                notifyItemInserted(BACK_INSERTING);
            }
            mCrimes = crimes;
        }

        public void deleteCrime(UUID mCrimeId) {
            for (Crime c : mCrimes) {
                if (c.getId().equals(mCrimeId)){
                    CrimeLab.get(getActivity()).deleteCrime(mCrimeId);
                    notifyItemRemoved(mCrimes.indexOf(c));
                }
            }
        }
    }

    private String mergeDateTime(Crime crime) {
        crime.getDate();
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT1, Locale.US);
        String date1 = fmt.format(crime.getDate());
        fmt = new SimpleDateFormat(DATE_FORMAT2, Locale.US);
        String date2 = fmt.format(crime.getDate());
        fmt = new SimpleDateFormat(CrimeFragment.TIME_FORMAT, Locale.US);
        return date1 + " " + fmt.format(crime.getTime()) + " " + date2;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if(requestCode == REQUEST_DELETE_CRIME){
            UUID mCrimeId = (UUID) data.getSerializableExtra(DELETE_CRIME);
            mAdapter.deleteCrime(mCrimeId);

        }
    }
}
