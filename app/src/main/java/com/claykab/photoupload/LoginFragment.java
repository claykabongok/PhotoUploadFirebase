package com.claykab.photoupload;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.TextUtilsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.claykab.photoupload.databinding.FragmentLoginBinding;
import com.claykab.photoupload.utils.NetworkState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String TAG = "Login";

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentLoginBinding.inflate(inflater, container, false);

        // Inflate the layout for this fragment
        firebaseAuth=FirebaseAuth.getInstance();



        binding.btnLogin.setOnClickListener(v -> {
            boolean isDeviceConnected= NetworkState.isDeviceConnected(getContext());
            if(isDeviceConnected){
                fireBaseUserLogin(v);
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

        });


        //sign up link
        binding.tvSignUp.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_signUpFragment));


        return  binding.getRoot();
    }

    private void fireBaseUserLogin(final View v) {
        if(!IsInputValid()){
            return;
        }

        String username=binding.etLoginUsername.getEditText().getText().toString().trim();
        final String password=binding.etLoginPassword.getEditText().getText().toString().trim();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Signing in..");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(username,password).addOnCompleteListener(getActivity(), task -> {
            if (task.isSuccessful()){
                progressDialog.hide();
                FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

                Toast.makeText(getContext(),"User: "+firebaseUser.getEmail(),Toast.LENGTH_LONG).show();
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_FirstFragment);
            }else{
                progressDialog.hide();
                try {

                    Snackbar.make(getActivity().findViewById(R.id.nav_host_fragment), "Login not Successful"+task.getException(),
                            Snackbar.LENGTH_LONG)
                            .show();
                }
                catch (Exception ex){
                    Log.e(TAG,"Error: "+ex.getLocalizedMessage());
                }
                //Toast.makeText(getContext(), "Login not Successful"+task.getException(),Toast.LENGTH_LONG).show();
                //Log.w(TAG, "createUserWithEmail:failure", task.getException());


            }
        });
    }

    private boolean IsInputValid() {
        boolean inputValid=true;
        String username=binding.etLoginUsername.getEditText().getText().toString().trim();
        if(TextUtils.isEmpty(username)){
            binding.etLoginUsername.setError("Username required.");
            inputValid=false;
        }
        else{
            binding.etLoginUsername.setError(null);
        }

        String password=binding.etLoginPassword.getEditText().getText().toString().trim();
        if(TextUtils.isEmpty(password)){
            binding.etLoginPassword.setError("Password required.");
            inputValid=false;
        }
        else{
            binding.etLoginPassword.setError(null);
        }
        return inputValid;
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after {@link #onStop()} and before {@link #onDestroy()}.  It is called
     * <em>regardless</em> of whether {@link #onCreateView} returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(firebaseAuth.getCurrentUser() !=null){
            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_FirstFragment);
        }
    }


}
