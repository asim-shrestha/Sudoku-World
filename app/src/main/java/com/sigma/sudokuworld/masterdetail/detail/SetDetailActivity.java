package com.sigma.sudokuworld.masterdetail.detail;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;

public class SetDetailActivity extends AbstractDrillDownActivity {
    public static int RESULT_SELECTED = 307;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       final long setID = getIntent().getLongExtra(KeyConstants.SET_ID_KEY, 1);

        mFAB.setImageResource(R.drawable.ic_check_black_24dp);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMasterDetailViewModel.setSelectedSet(setID);
                setResult(RESULT_SELECTED);
                finish();
            }
        });


        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(KeyConstants.SET_ID_KEY, setID);

            //Creating the detail view fragment
            SetDetailFragment fragment = new SetDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(mFragmentContainerID, fragment)
                    .commit();
        }
    }
}
