package com.mainpackage;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    User curr_user;

    public BottomSheetFragment() {
        // Required empty public constructor
    }

    public void setCurr_user(User curr_user) {
        this.curr_user = curr_user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
        TextView tv_username = view.findViewById(R.id.username_bottom_sheet);
        LinearLayout live_tracking_bottom_sheet = view.findViewById(R.id.live_tracking_bottom_sheet);
        LinearLayout history_bottom_sheet =view.findViewById(R.id.history_bottom_sheet);

        live_tracking_bottom_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), LiveTrackingActivity.class);
                i.putExtra("phone_number", curr_user.getPhone());
                startActivity(i);
            }
        });
        history_bottom_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getActivity(),ViewLocationHistoryActivity.class);
                i.putExtra("phone_number",curr_user.getPhone());
                startActivity(i);
            }
        });
        if (curr_user != null) {
            String username = curr_user.getUsername().toUpperCase();
            tv_username.setText(username);
        }
        return view;
    }
}