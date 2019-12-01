package rs.ac.singidunum;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import rs.ac.singidunum.R;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.DateFormat;
import java.util.Date;

import rs.ac.singidunum.Model.Data;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fabbtn;

    // baza
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RecyclerView recyclerView;

    private String post_key;
    private String name;
    private String description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ToDo App");

        mAuth= FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("All Data").child(uid);

        // recycler view

        recyclerView = findViewById(R.id.recyclerid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        fabbtn=findViewById(R.id.fabadd);

        fabbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddData();
            }
        });
    }

    private void AddData(){

        AlertDialog.Builder mydialog = new AlertDialog.Builder(this);
        LayoutInflater inflater  = LayoutInflater.from(this);

        View myview = inflater.inflate(R.layout.inputlayout, null);

        mydialog.setView(myview);
        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);

        final EditText name = myview.findViewById(R.id.name);
        final EditText description = myview.findViewById(R.id.description);

        Button btnCancel = myview.findViewById(R.id.btncancel);
        Button btnSave = myview.findViewById(R.id.btnsave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mName = name.getText().toString().trim();
                String mDescription = description.getText().toString().trim();

                if(TextUtils.isEmpty(mName)){
                    name.setError("Required Field!");
                    return;
                }

                String id = mDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(mName, mDescription, id, mDate);

                mDatabase.child(id).setValue(data);
                Toast.makeText(getApplicationContext(),"Data Uploaded!", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data,MyViewHolder>adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (
                        Data.class, R.layout.itemlayoutdesign, MyViewHolder.class, mDatabase
                ) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, final Data model, final int position) {

                viewHolder.setName(model.getName());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setDate(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(position).getKey();
                        name = model.getName();
                        description = model.getDescription();

                        updateData();
                    }
                });

            }
        };
        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView mName = mView.findViewById(R.id.name_item);
            mName.setText(name);
        }

        public void setDescription(String description){
            TextView mDescription = mView.findViewById(R.id.description_item);
            mDescription.setText(description);
        }

        public void setDate(String date){
            TextView mDate = mView.findViewById(R.id.date_item);
            mDate.setText(date);
        }
    }

    public void updateData(){

        AlertDialog.Builder mydialog=new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myview = inflater.inflate(R.layout.update_data,null);
        mydialog.setView(myview);

        final AlertDialog dialog=mydialog.create();

        final EditText mName = myview.findViewById(R.id.name);
        final EditText mDescription = myview.findViewById(R.id.description);

        mName.setText(name);
        mName.setSelection(name.length());

        mDescription.setText(description);
        mDescription.setSelection(description.length());

        Button btnDelete = myview.findViewById(R.id.btndelete);
        Button btnUpdate = myview.findViewById(R.id.btnupdate);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = mName.getText().toString().trim();
                description = mDescription.getText().toString().trim();

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(name, description, post_key, mDate);
                mDatabase.child(post_key).setValue(data);
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(post_key != null) {
                    mDatabase.child(post_key).removeValue();
                }
                dialog.dismiss();

            }
        });

        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.logout){
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }


}