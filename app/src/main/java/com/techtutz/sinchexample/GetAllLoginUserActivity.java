package com.techtutz.sinchexample;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.calling.Call;
import com.techtutz.sinchexample.adapter.UserAdapter;
import com.techtutz.sinchexample.listnener.MyItemClickListener;
import com.techtutz.sinchexample.model.User;
import com.techtutz.sinchexample.util.Constant;
import com.techtutz.sinchexample.util.Prefs;

import java.util.ArrayList;
import java.util.List;

public class GetAllLoginUserActivity extends BaseActivity implements MyItemClickListener {
    private static String TAG = GetAllLoginUserActivity.class.getSimpleName();
    private Context context;
    private List<User> userList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabaseReference,mDatabaseReferences;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_all_login_user);
        context = GetAllLoginUserActivity.this;
        userList = new ArrayList<>();
        progressDialog=new ProgressDialog(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = firebaseDatabase.getReference("user_list");
        mDatabaseReferences = firebaseDatabase.getReference();


        GetAllUser();
        saveUser();
    }

    private void saveUser() {
        String id = mDatabaseReferences.child("user_list").push().getKey();
        User user = new User();
        user.setId(id);
        user.setUser_id(Prefs.getInstance(context).GetValue(Constant.User_Id));
        user.setName(Prefs.getInstance(context).GetValue(Constant.Name));
        user.setPhoto_url(Prefs.getInstance(context).GetValue(Constant.Photo_url));
        mDatabaseReferences.child("user_list").child(id).setValue(user);
        Toast.makeText(getApplicationContext(), "User Add successfully", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), PlaceCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setRecyclerViewCategory() {
        RecyclerView recyclerView = findViewById(R.id.rv_student);
        UserAdapter studentAdapter = new UserAdapter(context, userList);
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

                        User student = ds.getValue(User.class);
                        userList.add(student);

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
        mDatabaseReference.addListenerForSingleValueEvent(eventListener);


    }
    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void fireEvent(String User_id) {
        Call call = getSinchServiceInterface().callUserVideo(User_id);
        String callId = call.getCallId();
        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);
    }
}