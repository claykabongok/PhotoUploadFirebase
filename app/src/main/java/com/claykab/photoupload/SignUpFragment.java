package com.claykab.photoupload;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.claykab.photoupload.databinding.FragmentLoginBinding;
import com.claykab.photoupload.databinding.FragmentSignUpBinding;
import com.claykab.photoupload.utils.NetworkState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String TAG = "Registration";

    public SignUpFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_sign_up, container, false);
        binding=FragmentSignUpBinding.inflate(inflater, container, false);
        View root=binding.getRoot();
        // Inflate the layout for this fragment
        firebaseAuth=FirebaseAuth.getInstance();
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                boolean isDeviceConnected= NetworkState.isDeviceConnected(getContext());
                if(isDeviceConnected){
                    fireBaseUserSignUp(v);
                }else {
                    //notify the user
                    try {

                        Snackbar.make(getActivity().findViewById(R.id.nav_host_fragment), "Device offline, please connect to a Wifi or Cellular network.",
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                    catch (Exception ex){
                        //Log.e(TAG,"Error: "+ex.getLocalizedMessage());
                    }

                }


            }
        });

        return root;
    }

    private void fireBaseUserSignUp(final View v) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Signing in..");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String username=binding.etLoginUsername.getEditText().getText().toString().trim();
        String password=binding.etLoginPassword.getEditText().getText().toString().trim();


        firebaseAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    //Toast.makeText(getContext(), "Registration Successful",Toast.LENGTH_LONG).show();
                    //FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
                    progressDialog.hide();
                    Navigation.findNavController(v).navigate(R.id.action_signUpFragment_to_loginFragment);

                }else{
                    progressDialog.hide();
                    //notify the user
                    try {

                        Snackbar.make(getActivity().findViewById(R.id.nav_host_fragment), "Registration not Successful"+task.getException(),
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                    catch (Exception ex){
                        Log.e(TAG,"Error: "+ex.getLocalizedMessage());
                    }
                    //Toast.makeText(getContext(), "Registration not Successful"+task.getException(),Toast.LENGTH_LONG).show();
                    //Log.w(TAG, "createUserWithEmail:failure", task.getException());

                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }
}
