package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private ImageAdaptor mAdaptor;
    private ImageButton gridButton;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    public static final String RETURN_FACES_DETECTED = "numFaces";
    public static final String FILE_PATH = "filePath";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_PHOTO_FACES = 3;

    private Crime mCrime;
    private ArrayList<File> mPhotoFiles;
    private File mPhotoFile;
    private String mCurrentPhotoPath;
    private EditText mTitleField;
    private TextView newText;
    private Button mDateButton;
    private CheckBox mSolvedCheckbox;
    private CheckBox mFaceTracker;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private String json;
    private int index = 0;
    private int numFaces;
    private int numFaces2;
    private boolean wantFace;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        index = 0;
        numFaces = 0;
        wantFace = false;
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime, index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        gridButton = (ImageButton) v.findViewById(R.id.gridBtn);

        mPhotoFiles = new ArrayList<File>();
        mAdaptor = new ImageAdaptor(mPhotoFiles);

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<File>>(){}.getType();
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        newText = (TextView) v.findViewById(R.id.faceCount);

        if((gson.fromJson(mCrime.getJlist(), listType)) != null && !((ArrayList) gson.fromJson(mCrime.getJlist(), listType)).isEmpty()) {
            mPhotoFiles.addAll((ArrayList) gson.fromJson(mCrime.getJlist(), listType));
            index = mPhotoFiles.size() - 1;
            mAdaptor.setImages(mPhotoFiles);
            mAdaptor.notifyDataSetChanged();

            mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime, index);

            Bitmap newestPhoto = PictureUtils.getScaledBitmap(
                        (mPhotoFiles.get(index)).getPath(), getActivity());
            mPhotoView.setImageBitmap(newestPhoto);

            index = mPhotoFiles.size();
            mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime, index);
        }

        if(index < 6) {
            gridButton.setVisibility(INVISIBLE);
            gridButton.setClickable(false);
        }

        mCrimeRecyclerView = (RecyclerView) v
                .findViewById(R.id.new_recycler);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mCrimeRecyclerView.setAdapter(mAdaptor);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckbox.setChecked(mCrime.isSolved());
        mSolvedCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mFaceTracker = (CheckBox) v.findViewById(R.id.faceNum);
        mFaceTracker.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                wantFace = isChecked;
                newText.setText("number of faces is: " + numFaces);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;

        final Intent captureImage2 = new Intent(getContext(), FaceTrackerActivity.class);
        //boolean canTakePhoto2 = mPhotoFile != null &&
        //       captureImage2.resolveActivity(packageManager) != null;

        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick (View v){


                    if(wantFace) {

                        startActivityForResult(captureImage2, REQUEST_PHOTO_FACES);

                    } else {

                        try {
                            mPhotoFile = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Uri uri = FileProvider.getUriForFile(getActivity(),
                                "com.bignerdranch.android.criminalintent.fileprovider",
                                mPhotoFile);

                        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                        List<ResolveInfo> cameraActivities = getActivity()
                                .getPackageManager().queryIntentActivities(captureImage,
                                        PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo activity : cameraActivities) {
                            getActivity().grantUriPermission(activity.activityInfo.packageName,
                                    uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        startActivityForResult(captureImage, REQUEST_PHOTO);
                    }
                }
        });

        gridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCrimeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

        //TODO: store images here
        Gson gson = new Gson();
        String json = gson.toJson(mPhotoFiles);
        mCrime.setJlist(json);
        mCrime.setNumFaces(mCrime.getNumFaces() + numFaces);
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if(requestCode == REQUEST_PHOTO_FACES) {

            numFaces = data.getIntExtra(RETURN_FACES_DETECTED, numFaces2) + numFaces;
            String filePath = data.getStringExtra(FILE_PATH);
            mCurrentPhotoPath = filePath;
            mPhotoFile = new File(filePath);

            newText.setText("number of faces is: " + numFaces);
            Toast.makeText(getActivity(), Integer.toString(numFaces),
                    Toast.LENGTH_LONG).show();

            updatePhotoView();
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile);

            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private void updatePhotoView() {

        mPhotoFiles.add(mPhotoFile);
        index = mPhotoFiles.size();

        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else if(mPhotoFile != null){
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }

        if(mPhotoFiles != null){
            mAdaptor.setImages(mPhotoFiles);
            mAdaptor.notifyDataSetChanged();
        }
        if(index >= 6) {
            gridButton.setClickable(true);
            gridButton.setVisibility(VISIBLE);
        }
        galleryAddPic();
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime, index);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getFilesDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);
        Toast.makeText(getActivity(), mCurrentPhotoPath,
                Toast.LENGTH_LONG).show();
    }


    private class ImageHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private File image;
        private ImageView ImageView;

        public ImageHolder(View v) {
            super(v);
            ImageView = (ImageView) itemView.findViewById(R.id.horizontal_item_view_image);
            /*v.setOnClickListener(new v.OnClickListener() {

                                 });*/
        }

        @Override
        public void onClick(View view) {
            //Intent intent = CrimePagerActivity.newIntent(getActivity(), image.getId());
            //startActivity(intent);
        }

    }

    private class ImageAdaptor extends RecyclerView.Adapter<ImageHolder> {

        private ArrayList<File> imageList;
        private File newImage;

        public ImageAdaptor(ArrayList<File> images) {
            imageList = images;
        }

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_image, parent, false);
            return new ImageHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            File image = imageList.get(position);
            Bitmap chosenPic = PictureUtils.getScaledBitmap(image.getPath(), getActivity());
            holder.ImageView.setImageBitmap(chosenPic);
            //ImageView.setImageBitmap(chosenPic);
        }

        @Override
        public int getItemCount() {
            return imageList.size();
        }

        public void setImages(ArrayList<File> images) {
            imageList = images;
        }

    }

}
