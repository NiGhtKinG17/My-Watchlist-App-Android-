package com.hstudio.mywatchlist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference reference;

    private ProgressDialog loader;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;

    private String key = "";
    private String name;
    private String description;

    private Toolbar toolbar;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);



        toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);

        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        assert mUser != null;
        onlineUserID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("names").child(onlineUserID);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> addTask());
    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_file, null);
        myDialog.setView(myView);
        AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText name = myView.findViewById(R.id.etName);
        final EditText description = myView.findViewById(R.id.etDesc);
        Button save = myView.findViewById(R.id.btnSaveTask);
        Button cancel = myView.findViewById(R.id.btnCancelTask);

        cancel.setOnClickListener(v -> dialog.dismiss());

        save.setOnClickListener(view -> {
            String mName = name.getText().toString();
            String mDescription = description.getText().toString();
            String id = reference.push().getKey();

            if(mName.isEmpty()){
                name.setError("Name is required");
            }
            if(mDescription.isEmpty()){
                description.setError("Description is required");
            }else{
                loader.setMessage("Adding your data");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                Model model = new Model(mName, mDescription, id);
                assert id != null;
                reference.child(id).setValue(model).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(HomeActivity.this, "Data has been inserted successfully", Toast.LENGTH_SHORT).show();
                    }else{
                        String error = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(HomeActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                    }
                    loader.dismiss();
                });
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(reference, Model.class).build();
        FirebaseRecyclerAdapter<Model, myViewHolder> adapter = new FirebaseRecyclerAdapter<Model, myViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Model model) {
                holder.setRName(model.getName());
                holder.setRDesc(model.getDescription());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(holder.getBindingAdapterPosition()).getKey();
                        name = model.getName();
                        description = model.getDescription();

                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieved_layout, parent,false);
                return new myViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void updateTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_data,null);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();

        EditText uName = view.findViewById(R.id.etUpName);
        EditText uDesc = view.findViewById(R.id.etUpDesc);

        uName.setText(name);
        uName.setSelection(name.length());

        uDesc.setText(description);
        uDesc.setSelection(description.length());

        Button delBtn = view.findViewById(R.id.btnDeleteUpTask);
        Button updateBtn = view.findViewById(R.id.btnSaveUpTask);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = uName.getText().toString();
                description = uDesc.getText().toString();

                Model upModel = new Model(name, description,key);

                reference.child(key).setValue(upModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Data has been updated successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Data has been deleted successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String err = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Error: "+err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setRName(String name){
            TextView rNameTv = mView.findViewById(R.id.tvRname);
            rNameTv.setText(name);
        }
        public void setRDesc(String desc){
            TextView rDescTv = mView.findViewById(R.id.tvRdesc);
            rDescTv.setText(desc);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}