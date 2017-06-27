package com.kerbless.kerb.fragments;

/**
 * Created by rmulla on 11/13/15.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kerbless.kerb.R;

import org.w3c.dom.Text;
// ...

public class FilterFragment extends DialogFragment {

    private EditText mEditText;
    Button btnD2;
    Button btnP1;
    Button btnW2;
    TextView tvCancel;

    public FilterFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static FilterFragment newInstance(String title) {
        FilterFragment frag = new FilterFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filters, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        //mEditText = (EditText) view.findViewById(R.id.txt_your_name);
        btnD2 = (Button)view.findViewById(R.id.btnD2);
        btnP1 = (Button)view.findViewById(R.id.btnP1);
        btnW2 = (Button)view.findViewById(R.id.btnW2);
        tvCancel = (TextView) view.findViewById(R.id.tvCancel);

        btnD2.setPressed(true);
        btnP1.setPressed(true);
        btnW2.setPressed(true);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        //mEditText.requestFocus();
        //getDialog().getWindow().setSoftInputMode(
                //WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
