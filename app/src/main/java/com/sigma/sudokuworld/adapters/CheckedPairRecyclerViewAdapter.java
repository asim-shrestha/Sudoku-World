package com.sigma.sudokuworld.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.persistence.db.views.WordPair;
import com.sigma.sudokuworld.masterdetail.detail.AddSetFragment;

import java.util.ArrayList;
import java.util.List;


public class CheckedPairRecyclerViewAdapter extends RecyclerView.Adapter<CheckedPairRecyclerViewAdapter.ViewHolder> {

    private final AddSetFragment.OnFragmentInteractionListener mListener;
    private List<WordPair> mWordPairs;

    public CheckedPairRecyclerViewAdapter(AddSetFragment.OnFragmentInteractionListener listener) {
        mWordPairs = new ArrayList<>();
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checked_pair_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mWordPair = mWordPairs.get(position);
        holder.mNativeWord.setText(mWordPairs.get(position).getNativeWord().getWord());
        holder.mForeignWord.setText(mWordPairs.get(position).getForeignWord().getWord());
        holder.mCount.setText(Long.toString(mWordPairs.get(position).getIncorrectCount()));

        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onCheckChangedFragmentInteraction(holder.mWordPair, ((CheckBox) v).isChecked());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWordPairs.size();
    }

    public void setItems(List<WordPair> wordPairs) {
        mWordPairs = wordPairs;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mNativeWord;
        final TextView mForeignWord;
        final CheckBox mCheckBox;
        final TextView mCount;
        WordPair mWordPair;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mCheckBox = view.findViewById(R.id.checkBox);
            mNativeWord = view.findViewById(R.id.nWord);
            mForeignWord = view.findViewById(R.id.fWord);
            mCount = view.findViewById(R.id.incorrectCount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNativeWord.getText() + "'";
        }
    }
}