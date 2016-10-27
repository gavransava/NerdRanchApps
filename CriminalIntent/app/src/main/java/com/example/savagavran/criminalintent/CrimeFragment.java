package com.example.savagavran.criminalintent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    //private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_PHOTO = "DialogPhoto";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;
    private static final int DISPLAY_PHOTO = 4;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;
    private CheckBox mSolvedCheckBox;
    private String mSuspectPhoneNumber;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private PackageManager mPackageManager;
    private LinearLayout mFragmentCrimeLayout;
    private int mPhotoWidth;
    private int mPhotoHeight;
    private final String DATE_FORMAT = "EEEE, MMM d, y";
    public static final String TIME_FORMAT = "H:m:s";
    public static final int PERMISSION_REQUEST_READ_CONTACTS = 3;
    public static final int PERMISSION_REQUEST_CALL_CONTACT = 4;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void wireTitleField() {
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s){}
        });
    }

    private void wireDateButton() {
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = DatePickerActivity.newIntent(getActivity(), mCrime.getDate());
                startActivityForResult(intent, REQUEST_DATE);
            }
        });
    }

    private void wireTimeButton() {
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getTime());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

    }

    private void wireSolvedCheckBox() {
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });
    }

    private void wireSendCrimeButton() {
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTitleField.getText().toString().isEmpty()) {
                    IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText(getCrimeReport())
                            .setSubject(getString(R.string.crime_report_subject))
                            .setChooserTitle(getString(R.string.send_report))
                            .startChooser();
                }
            }
        });
    }

    private void wireSuspectButton(final Intent pickContact) {
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        mCallSuspectButton.setEnabled(false);
        if(mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
            mCallSuspectButton.setEnabled(true);
        }

        if(mPackageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null ) {
            mSuspectButton.setEnabled(false);
        }
    }

    private void wireCallSuspectButton(){
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSuspectButton.getText().toString().equals(getString(R.string.crime_suspect_text))) {
                    if(ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.READ_CONTACTS},
                                PERMISSION_REQUEST_READ_CONTACTS);
                    }
                    else {
                        getSuspectPhoneNumber();
                    }
                }
            }
        });
    }

    private void wirePhotoButton(){
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(mPackageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        if(canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
    }

    private void wirePhotoView() {
        mPhotoView.setEnabled(false);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomInPhoto();
            }
        });
    }

    private void zoomInPhoto() {
        FragmentManager manager = getFragmentManager();
        PhotoFragment dialog = PhotoFragment.newInstance(mPhotoFile);
        dialog.show(manager, DIALOG_PHOTO);
    }

    private String getSuspectPhoneNumber() {
        String number = "";
        Cursor phones = getActivity().getContentResolver().
                query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = " + "'" +
                                mSuspectButton.getText().toString() + "'",
                        null, null);
        try {
            if (phones.getCount() == 0) {
                return "";
            }
            phones.moveToFirst();
            number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ;

            if(ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CALL_PHONE},
                        PERMISSION_REQUEST_CALL_CONTACT);
            } else {
                callSuspect(number);
            }
        } finally {
            phones.close();
        }
        return number;
    }

    private void callSuspect(String number) {
        mCallSuspectButton.setEnabled(true);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    private void updateDate() {
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        mDateButton.setText(fmt.format(mCrime.getDate()));
    }

    private void updateTime() {
        SimpleDateFormat fmt = new SimpleDateFormat(TIME_FORMAT, Locale.US);
        mTimeButton.setText(fmt.format(mCrime.getTime()));
    }

    private String getCrimeReport() {
        String solvedString;
        if(mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView() {
        ViewTreeObserver vto = mFragmentCrimeLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ImageView img = (ImageView) mFragmentCrimeLayout.findViewById(R.id.crime_photo);
                mPhotoWidth  = img.getMeasuredWidth();
                mPhotoHeight = img.getMeasuredHeight();
            }
        });
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(),mPhotoWidth, mPhotoHeight);

            bitmap = PictureUtils.rotateBitmap(bitmap, 90);
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setEnabled(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mCallSuspectButton = (Button) v.findViewById(R.id.call_suspect);
        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        mPackageManager = getActivity().getPackageManager();

        wireTitleField();
        wireDateButton();
        wireTimeButton();
        wireSendCrimeButton();
        wireSolvedCheckBox();
        wirePhotoButton();
        wirePhotoView();

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        wireSuspectButton(pickContact);
        wireCallSuspectButton();

        mFragmentCrimeLayout = (LinearLayout) v.findViewById(R.id.fragment_crime_id);
        updatePhotoView();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if(requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();

        } else if(requestCode == REQUEST_TIME) {
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setTime(time);
            updateTime();

        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try{
                if(c.getCount() == 0){
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
                mCallSuspectButton.setEnabled(true);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_delete_crime:
                if(mCrime.getTitle() != null) {
                    Intent intent = new Intent();
                    getActivity().setResult(Activity.RESULT_OK, CrimeListFragment.deleteCrime(intent, mCrime.getId()));
                    getActivity().finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public  void onRequestPermissionsResult(int requestCode,
                                            String permissions[], int[] grantResults){
        switch(requestCode) {
            case PERMISSION_REQUEST_READ_CONTACTS: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSuspectPhoneNumber = getSuspectPhoneNumber();
                } else {
                    mCallSuspectButton.setEnabled(false);
                }
                break;
            }
            case PERMISSION_REQUEST_CALL_CONTACT : {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callSuspect(mSuspectPhoneNumber);
                } else {
                    mCallSuspectButton.setEnabled(false);
                }
            }
        }
    }
}
