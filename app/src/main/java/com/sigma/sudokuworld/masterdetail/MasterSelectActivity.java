package com.sigma.sudokuworld.masterdetail;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.adapters.MasterDetailTabPagerAdapter;
import com.sigma.sudokuworld.persistence.db.entities.Set;
import com.sigma.sudokuworld.persistence.db.views.WordPair;
import com.sigma.sudokuworld.persistence.firebase.FireBaseSet;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;
import com.sigma.sudokuworld.masterdetail.detail.AddPairActivity;
import com.sigma.sudokuworld.masterdetail.detail.AddSetActivity;
import com.sigma.sudokuworld.masterdetail.detail.SetDetailActivity;
import com.sigma.sudokuworld.viewmodels.MasterDetailViewModel;
import org.jetbrains.annotations.NotNull;


public class MasterSelectActivity extends AppCompatActivity implements
        SetListFragment.OnFragmentInteractionListener,
        OnlineSetListFragment.OnFragmentInteractionListener,
        PairListFragment.OnFragmentInteractionListener {

    private static int RC_SET_DETAIL = 295;

    ViewPager mViewPager;
    TabLayout mTabLayout;
    FloatingActionButton mFloatingActionButton;
    MasterDetailViewModel mMasterDetailViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_select);

        mMasterDetailViewModel = ViewModelProviders.of(this).get(MasterDetailViewModel.class); //TODO: make frags and master share the same ViewModel

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) actionBar.setTitle("Set Builder");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_menu);

            if (actionBar != null) actionBar.setBackgroundDrawable(avd);
            if (avd != null) avd.start();

        }

        mFloatingActionButton = findViewById(R.id.fab);
        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.tabPager);
        mViewPager.setAdapter(new MasterDetailTabPagerAdapter(getSupportFragmentManager()));

        mTabLayout.setupWithViewPager(mViewPager);

        mFloatingActionButton.setOnClickListener(new FloatingActionButtonListener());
        mFloatingActionButton.setImageResource(R.drawable.ic_add_black_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SET_DETAIL) {
            if (resultCode == SetDetailActivity.RESULT_SELECTED) {
                Snackbar.make(mFloatingActionButton, "Set Selected", Snackbar.LENGTH_SHORT).show();
            }
        }

    }

    //Fire base listeners
    @Override
    public void onClickSetFragmentInteraction(FireBaseSet set) {
        showFirebaseSetDialog(set);
    }

    @Override
    public void onLongClickSetFragmentInteraction(View view, final FireBaseSet set) {
        //Stub
    }

    //Set fragment listener
    @Override
    public void onClickSetFragmentInteraction(Set set) {
        Intent intent = new Intent(this, SetDetailActivity.class);
        intent.putExtra(KeyConstants.SET_ID_KEY, set.getSetID());
        startActivityForResult(intent, RC_SET_DETAIL);
    }

    @Override
    public void onLongClickSetFragmentInteraction(View view, final Set set) {
        showLocalSetDialog(set);
    }

    //Pair fragment listeners
    @Override
    public void onClickPairFragmentInteraction(WordPair wordPair) {
        //TODO disabled for demo
//        Intent intent = new Intent(this, PairDetailActivity.class);
//        intent.putExtra(KeyConstants.PAIR_ID_KEY, wordPair.getPairID());
//        startActivity(intent);
    }

    @Override
    public void onLongPairClickFragmentInteraction(final WordPair wordPair) {
        String msg = wordPair.getNativeWord().getWord() + " " + wordPair.getForeignWord().getWord();

        Snackbar.make(findViewById(R.id.tabPager), msg, Snackbar.LENGTH_LONG)
                .setAction("Delete", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMasterDetailViewModel.deletePair(wordPair);
                    }
                }).show();
    }

    public class FloatingActionButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int pos = mTabLayout.getSelectedTabPosition();

            Intent intent;
            if (pos == 0 || pos == 1) {
                intent = new Intent(getBaseContext(), AddSetActivity.class);
            } else {
                intent = new Intent(getBaseContext(), AddPairActivity.class);
            }

            startActivity(intent);
        }
    }

    private void showFirebaseSetDialog(@NotNull final FireBaseSet set) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(set.getName());
        builder.setMessage(set.getDescription());

        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMasterDetailViewModel.downLoadSet(set);
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMasterDetailViewModel.deleteSet(set);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    private void showLocalSetDialog(@NotNull final Set set) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(set.getName());
        builder.setMessage(set.getDescription());
        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMasterDetailViewModel.uploadSet(set);
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //User cannot delete the last remaining set
        if(mMasterDetailViewModel.getNumberOfLocalSets() > 1) {
            builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMasterDetailViewModel.deleteSet(set);
                }
            });
        }

        builder.show();
    }
}

