package com.sigma.sudokuworld.masterdetail.detail;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApi;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.sigma.sudokuworld.R;

import java.sql.Array;

public class AddPairFragment extends AbstractDrillDownFragment {
    private TextInputEditText mNativeWordInput;
    private TextInputEditText mForeignWordInput;
    private Button mTranslateButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pair, container, false);

        mAppBarLayout.setTitle("Add Word Pair");
        mNativeWordInput = view.findViewById(R.id.nativeInput);
        mForeignWordInput = view.findViewById(R.id.foreignInput);
        mTranslateButton = view.findViewById(R.id.translateButton);

        mTranslateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mNativeWordInput.getText().toString();
                new TranslationService().execute(text);
            }
        });

        return view;
    }

    public String getNativeWord() {
        return mNativeWordInput.getText().toString();
    }

    public String getForeignWord() {
        return mForeignWordInput.getText().toString();
    }

    private class TranslationService extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            if (!strings[0].isEmpty()) {
                Translate translate = TranslateOptions.getDefaultInstance().getService();

                Translation translation = translate.translate(strings[0],
                        Translate.TranslateOption.sourceLanguage("en"),
                        Translate.TranslateOption.targetLanguage("fr"));

                return translation.getTranslatedText();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mForeignWordInput.setText(s);
        }
    }
}

