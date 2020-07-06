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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.techtutz.sinchexample.adapter.UserAdapter;
import com.techtutz.sinchexample.listnener.MyItemClickListener;
import com.techtutz.sinchexample.model.User;
import com.techtutz.sinchexample.util.Constant;
import com.techtutz.sinchexample.util.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GetAllLoginUserActivity extends BaseActivity implements MyItemClickListener, SinchService.StartFailedListener {
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getSinchServiceInterface() != null) {
                    getSinchServiceInterface().startClient(Prefs.getInstance(context).GetValue(Constant.User_Id));
                }
            }
        },4000);

    }


    private void setRecyclerViewCategory() {
        RecyclerView recyclerView = findViewById(R.id.rv_student);
        UserAdapter studentAdapter = new UserAdapter(context, userList,this);
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
                        if (!Objects.equals(ds.child("user_id").getValue(String.class), Prefs.getInstance(context).GetValue(Constant.User_Id))){
                            User student = ds.getValue(User.class);
                            userList.add(student);

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
        mDatabaseReference.addListenerForSingleValueEvent(eventListener);


    }

    @Override
    protected void onResume() {
        if (getSinchServiceInterface() != null) {
            progressDialog.show();
            progressDialog.setMessage(Constant.Please_Wait);
            getSinchServiceInterface().startClient(Prefs.getInstance(context).GetValue(Constant.User_Id));

        }
        super.onResume();
    }

    //this method is invoked when the connection is established with the SinchService
    @Override
    public void onServiceConnected(IBinder iBinder) {
        super.onServiceConnected(iBinder);
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    public void onServiceConnected() {

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
       /* if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }*/
        super.onBackPressed();

    }

    @Override
    protected void onStart() {
        if (getSinchServiceInterface() != null) {
            progressDialog.show();
            progressDialog.setMessage(Constant.Please_Wait);
            getSinchServiceInterface().startClient(Prefs.getInstance(context).GetValue(Constant.User_Id));

        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onStop();
    }

    @Override
    protected void onRestart() {
        progressDialog.dismiss();

        if (getSinchServiceInterface() != null) {
            progressDialog.show();
            progressDialog.setMessage(Constant.Please_Wait);
            getSinchServiceInterface().startClient(Prefs.getInstance(context).GetValue(Constant.User_Id));

        }
        super.onRestart();
    }

    @Override
    public void fireEvent(String User_id) {
        if (User_id==null){
            Toast.makeText(getApplicationContext(),"Another user not available",Toast.LENGTH_LONG).show();
            return;
        }

        Log.e(TAG,"======================"+User_id);
        Call call = getSinchServiceInterface().callUserVideo(User_id);
        String callId = call.getCallId();
        if (callId==null){
            Toast.makeText(getApplicationContext(),"Another user not available",Toast.LENGTH_LONG).show();
            return;
        }
        Log.e(TAG,"======================"+callId);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent callScreen = new Intent(GetAllLoginUserActivity.this, CallScreenActivity.class);
                callScreen.putExtra(SinchService.CALL_ID, callId);
                startActivity(callScreen);
            }
        },1500);

    }

    @Override
    public void onStartFailed(SinchError error) {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "failed "+error.getMessage(), Toast.LENGTH_LONG).show();

    }

    @Override
    public void onStarted() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Service Started ", Toast.LENGTH_LONG).show();

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(Prefs.getInstance(context).GetValue(Constant.User_Id));
        } else {
            GetAllUser();
        }

    }
}