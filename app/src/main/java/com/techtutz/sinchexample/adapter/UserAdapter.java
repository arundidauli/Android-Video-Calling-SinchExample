package com.techtutz.sinchexample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techtutz.sinchexample.R;
import com.techtutz.sinchexample.listnener.MyItemClickListener;
import com.techtutz.sinchexample.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
    private Context context;
    private List<User> studentList;
    private MyItemClickListener myItemClickListener;

    public UserAdapter(Context context, List<User> studentList) {
        this.context = context;
        this.studentList = studentList;
    }

    public UserAdapter(Context context, List<User> studentList, MyItemClickListener myItemClickListener) {
        this.context = context;
        this.studentList = studentList;
        this.myItemClickListener = myItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(studentList.get(position).getPhoto_url())
                .placeholder(R.drawable.icon_agora_large)
                .into(holder.user_image);
        holder.user_name.setText(studentList.get(position).getName());
        holder.user_email.setText(studentList.get(position).getUser_id());

        holder.call_btn.setOnClickListener(v -> {
            myItemClickListener.fireEvent(studentList.get(position).getUser_id());
        });



    }


    @Override
    public int getItemCount() {
        return studentList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView user_name, user_email;
        private ImageView user_image, call_btn;


        MyViewHolder(View view) {
            super(view);
            user_name = view.findViewById(R.id.txt_user_name);
            user_email = view.findViewById(R.id.txt_email);
            user_image = view.findViewById(R.id.user_photo);
            call_btn = view.findViewById(R.id.call_btn);

        }
    }

}
