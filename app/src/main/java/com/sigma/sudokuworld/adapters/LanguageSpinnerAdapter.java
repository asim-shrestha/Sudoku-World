package com.sigma.sudokuworld.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.persistence.db.entities.Language;

import java.util.ArrayList;

public class LanguageSpinnerAdapter extends ArrayAdapter<Language> {

    public LanguageSpinnerAdapter(Context context, ArrayList<Language> languageItems) {
        super(context,0,languageItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_language_spinner, parent, false);
        }

        Language languageName = getItem(position);
        if (languageName != null) {
            TextView textView = convertView.findViewById(R.id.langName);
            textView.setText(languageName.getName());
        }

        return convertView;
    }
}


