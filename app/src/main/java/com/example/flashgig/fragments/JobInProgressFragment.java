package com.example.flashgig.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.example.flashgig.GlideApp;
import com.example.flashgig.R;
import com.example.flashgig.adapters.HorizontalImageRecyclerViewAdapter;
import com.example.flashgig.databinding.FragmentDetailBinding;
import com.example.flashgig.databinding.FragmentJobInProgressBinding;
import com.example.flashgig.models.Job;
import com.example.flashgig.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JobInProgressFragment extends Fragment implements HorizontalImageRecyclerViewAdapter.ItemClickListener{
    private FragmentJobInProgressBinding binding;

    private StorageReference storageRef;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private DocumentSnapshot document;

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private String curUser;

    private Task<QuerySnapshot> curJob;
    private User clientUser;
    private Job job;

    private ImageView profilePicDetail, jobImage0, jobImage1, jobImage2, jobImage3;

    private TextView textJobTitle, textJobDate, textJobBudget, textJobLocation, textJobClientEmail, textJobClientName, textJobDescription, textJobWorkers;

    private RecyclerView imageRecyclerView;

    public JobInProgressFragment(String jobId) {
        db = FirebaseFirestore.getInstance();
        curUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        curJob = db.collection("jobs").whereEqualTo("jobId",jobId).get();
        storageRef = FirebaseStorage.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJobInProgressBinding.inflate(inflater, container, false);

        textJobTitle = binding.textJobTitle;
        textJobLocation = binding.textJobLocation;
        textJobDate = binding.textJobDate;
        textJobClientEmail = binding.textJobClientEmail;
        textJobClientName = binding.textJobClientName;
        textJobDescription = binding.textJobDescription;
        textJobBudget = binding.textJobBudget;
        textJobWorkers = binding.textJobWorkers;
        profilePicDetail = binding.profilePicDetail;
        imageRecyclerView = binding.imageRecyclerView;

        jobImage0 = binding.jobImageDetail0;
        jobImage1 = binding.jobImageDetail1;
        jobImage2 = binding.jobImageDetail2;
        jobImage3 = binding.jobImageDetail3;

        curJob.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    document = task.getResult().getDocuments().get(0);
                    job = document.toObject(Job.class);
                    if(!curUser.equals(job.getClient())){
                        binding.btnApplyForJob.setVisibility(View.VISIBLE);
                    }
                    // get client user id
                    db.collection("users").whereEqualTo("email", job.getClient()).get().addOnCompleteListener(task1 -> {
                        if(task1.getResult().getDocuments().isEmpty()){
                            Log.d("Detail Fragment", "onComplete: User not found");
                            Toast.makeText(getContext(), "Client user not found!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        clientUser = task1.getResult().getDocuments().get(0).toObject(User.class);
                        setViews();
                        loadImages();
                    });
                }
            }
        });

        FragmentManager fm = getActivity().getSupportFragmentManager();

        binding.backButton.setOnClickListener(view ->{
            fm.popBackStackImmediate();
        });



        binding.btnApplyForJob.setOnClickListener(view ->{
            if(job.getBidders().contains(curUser)){
                binding.btnApplyForJob.setBackgroundColor(65536);
                Toast.makeText(getActivity(),"Applied for job already!",Toast.LENGTH_SHORT).show();
            }
            else {
                final Map<String, Object> addUsertoArrayMap = new HashMap<>();
                addUsertoArrayMap.put("bidders", FieldValue.arrayUnion(curUser));
                // something wrong here
                db.collection("jobs").document(document.getId()).update(addUsertoArrayMap);
                Toast.makeText(getActivity(),"Applied for job!",Toast.LENGTH_SHORT).show();
                binding.btnApplyForJob.setBackgroundColor(808080);
                fm.popBackStackImmediate();
            }
        });

        return binding.getRoot();
    }
    private void setViews(){
        textJobTitle.setText(job.getTitle());
        textJobLocation.setText(job.getLocation());
        textJobDate.setText(job.getDate());
        textJobClientEmail.setText(job.getClient());
        textJobClientName.setText(clientUser.getFullName());
        textJobDescription.setText(job.getDescription());
        textJobWorkers.setText(String.valueOf(job.getNumWorkers()));
        textJobBudget.setText(job.getBudget());

        for (String category : job.getCategories()){
            switch (category) {
                case "Carpentry":
                    binding.chipCarpentry.setVisibility(View.VISIBLE);
                    break;
                case "Plumbing":
                    binding.chipPlumbing.setVisibility(View.VISIBLE);
                    break;
                case "Electrical":
                    binding.chipElectrical.setVisibility(View.VISIBLE);
                    break;
                case "Electronics":
                    binding.chipElectronics.setVisibility(View.VISIBLE);
                    break;
                case "Personal Shopping":
                case "Shopping":
                    binding.chipPersonalShopping.setVisibility(View.VISIBLE);
                    break;
                case "Virtual Assistant":
                case "Assistant":
                    binding.chipVirtualAssistant.setVisibility(View.VISIBLE);
                    break;
                case "Other":
                    binding.chipOther.setVisibility(View.VISIBLE);
                    break;
            }
        }
        if(job.getBidders().contains(curUser)){
            binding.btnApplyForJob.setBackgroundColor(808080);
        }

    }
    private void loadImages() {
        String clientId = clientUser.getUserId();
        // load client profile pic
        StorageReference userRef = storageRef.child("media/images/profile_pictures/" + clientId);
        userRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            try {
                GlideApp.with(this)
                        .load(userRef)
                        .error(R.drawable.default_profile)
                        .signature(new ObjectKey(String.valueOf(storageMetadata.getCreationTimeMillis())))
                        .fitCenter()
                        .into(profilePicDetail);
                binding.progressBarDetail.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.d("Detail Fragment", "loadImage: "+e.toString());
            }
        }).addOnFailureListener(e -> {
            binding.progressBarDetail.setVisibility(View.GONE);
            profilePicDetail.setImageResource(R.drawable.default_profile);
            Log.d("Detail Fragment", "retrieveInfo: "+e.toString());
        });
        // load job images
        ArrayList<String> jobImageUris = new ArrayList<>(job.getJobImages());
        ArrayList<Uri> jobImageArrayList = new ArrayList<>();
        StorageReference jobImagesRef = storageRef.child("/media/images/addjob_pictures/");
        final Integer[] imageCounter = {0};
        for(String imageUriString: jobImageUris){
            Log.d("detail fragg", "loadImages: "+String.valueOf(imageCounter[0]));
            StorageReference jobImageRef = jobImagesRef.child(imageUriString);
            jobImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                imageCounter[0]++;
                jobImageArrayList.add(uri);
                // if last image uri is fetched, set adapter
                if(imageCounter[0] == jobImageUris.size()){
//                    Toast.makeText(getContext(), "last image is"+String.valueOf(imageCounter[0]), Toast.LENGTH_SHORT).show();
                    HorizontalImageRecyclerViewAdapter adapter = new HorizontalImageRecyclerViewAdapter(getContext(), jobImageArrayList, this);
                    LinearLayoutManager layoutManager
                            = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);

                    imageRecyclerView.setLayoutManager(layoutManager);
                    imageRecyclerView.setAdapter(adapter);
                }
            });
        }
    }

    @Override
    public void onItemClick(Uri imageUri) {
        ImagePopupFragment imagePopupFragment = new ImagePopupFragment(imageUri);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.add(R.id.frameLayout, imagePopupFragment,"imagePopup").addToBackStack(null).commit();
        return;
    }
}