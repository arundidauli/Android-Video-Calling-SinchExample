package com.techtutz.sinchexample;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sinch.android.rtc.calling.Call;
import com.techtutz.sinchexample.adapter.UserAdapter;
import com.techtutz.sinchexample.listnener.MyItemClickListener;
import com.techtutz.sinchexample.model.User;
import com.techtutz.sinchexample.util.Constant;
import com.techtutz.sinchexample.util.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaceCallActivity extends BaseActivity implements MyItemClickListener {
    private static String TAG = PlaceCallActivity.class.getSimpleName();
    private Context context;
    private ProgressDialog progressDialog;
    private EditText et_category, et_title, et_contact, et_details;
    private ImageView student_image;
    private StorageReference storageRef, imageRef;
    private FirebaseAuth firebaseAuth;
    private Button mCallButton;
    private EditText mCallName;
    private List<User> userList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabaseReferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = PlaceCallActivity.this;
        progressDialog = new ProgressDialog(this);
        userList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReferences = firebaseDatabase.getReference("user_list");
        FirebaseStorage storage = FirebaseStorage.getInstance();


        findViewById(R.id.stop_service).setOnClickListener(view -> {
            stopButtonClicked();
        });
    }

    // invoked when the connection with Sinch Server is established
    @Override
    public void onServiceConnected() {
        TextView userName = findViewById(R.id.loggedInName);
        userName.setText(getSinchServiceInterface().getUserName());
        UpdateStatus(true);
        GetAllUser();
    }
    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
            UpdateStatus(false);
        }
        super.onDestroy();
    }

    //to kill the current session of Sinch Service
    private void stopButtonClicked() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
            UpdateStatus(false);
        }
        finish();
    }


    private void UpdateStatus(boolean IsActive) {
        User user = new User();
        user.setId(firebaseAuth.getUid());
        user.setUser_id(Prefs.getInstance(context).GetValue(Constant.User_Id));
        user.setName(Prefs.getInstance(context).GetValue(Constant.Name));
        user.setPhoto_url(Prefs.getInstance(context).GetValue(Constant.Photo_url));
        user.setActive(IsActive);
        mDatabaseReferences.child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(user);

    }


    private void setRecyclerViewCategory() {
        RecyclerView recyclerView = findViewById(R.id.rv_user_list);
        UserAdapter studentAdapter = new UserAdapter(context, userList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(studentAdapter);
    }

    private void GetAllUser() {
        progressDialog.show();
        progressDialog.setMessage(Constant.Please_Wait);
        ValueEventListener eventListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        if (!Objects.equals(ds.child("id").getValue(String.class), firebaseAuth.getUid())) {
                            User student = ds.getValue(User.class);
                            userList.add(student);
                            if (userList.size() == 0) {
                                Toast.makeText(getApplicationContext(), "No one online", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                }
                setRecyclerViewCategory();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
                progressDialog.dismiss();
            }
        };
        mDatabaseReferences.addListenerForSingleValueEvent(eventListener);
    }


    @Override
    public void fireEvent(String User_id) {
        // String userName = mCallName.getText().toString();
        Log.e(TAG, "******** Calling to " + User_id);
        if (User_id.isEmpty()) {
            Toast.makeText(this, "User Not online", Toast.LENGTH_LONG).show();
            return;
        }
        Call call = getSinchServiceInterface().callUserVideo(User_id);
        String callId = call.getCallId();
        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        Prefs.getInstance(context).SetValue(SinchService.CALL_ID, callId);
        startActivity(callScreen);
    }
}
