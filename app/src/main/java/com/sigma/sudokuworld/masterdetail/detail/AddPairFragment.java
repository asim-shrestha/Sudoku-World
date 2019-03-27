package com.sigma.sudokuworld.masterdetail.detail;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.sigma.sudokuworld.R;

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

        final Translate translate = TranslateOptions.getDefaultInstance().getService();

        mTranslateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nativeWord = mNativeWordInput.getText().toString();

                if (!nativeWord.isEmpty()) {
                    Translation translation = translate.translate(nativeWord,
                            Translate.TranslateOption.sourceLanguage("en"),
                            Translate.TranslateOption.targetLanguage("fr"));

                    mForeignWordInput.setText(translation.getTranslatedText());
                }
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
}

