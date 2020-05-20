package com.claykab.photoupload;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import com.claykab.photoupload.databinding.FragmentImageUploadBinding;
import com.claykab.photoupload.utils.NetworkState;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

public class ImageUploadFragment extends Fragment {
    private FragmentImageUploadBinding binding;
    Uri uriPicture;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        //get firebase instance used to verify user's authentication status
        firebaseAuth=FirebaseAuth.getInstance();


        binding= FragmentImageUploadBinding.inflate(inflater, container, false);
        //retrieve user's details
        try {
            firebaseUser=firebaseAuth.getCurrentUser();
            //create a storage for each user: userId/Pictures
            storageReference= FirebaseStorage.getInstance().getReference(firebaseUser.getUid()+"/Pictures");
        } catch (Exception e) {
            e.printStackTrace();
        }


        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //verify if the user is logged in if not redirect the user to login fragment
        if(firebaseAuth.getCurrentUser() ==null){
           Navigation.findNavController(getView()).navigate(R.id.action_FirstFragment_to_loginFragment);

        }



        //event to upload an image using progress dialog to display the status
        binding.buttonBtnUpload.setOnClickListener(v -> {
            boolean isDeviceConnected= NetworkState.isDeviceConnected(getContext());
            if(uriPicture != null && isDeviceConnected){
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Uploading image..");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);

                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
                uploadPicture();
            }else if(!isDeviceConnected){
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
            else if(uriPicture != null){
                try {
                    Snackbar.make(getActivity().findViewById(R.id.nav_host_fragment), "Please select a picture before you upload", Snackbar.LENGTH_LONG)
                            .setAction("OK", null).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });




        //pick an image for upload to firebase
        binding.fab.setOnClickListener(view1 -> selectPicture());
    }

    /**
     * retrieve image extension
     * @param uriPicture
     * @return
     */
   private String getImageExtension(Uri uriPicture){
        ContentResolver contentResolver=getActivity().getContentResolver();
       MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
       return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uriPicture));
   }
    private void uploadPicture() {
        StorageTask storageTask;

        StorageReference mStorageReference=
        storageReference.child(uriPicture.getLastPathSegment()+"."+ getImageExtension(uriPicture));

        mStorageReference.putFile(uriPicture)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get a URL to the uploaded content
                    //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    progressDialog.hide();
                    binding.imageView.setImageURI(null);
                    try {
                        Snackbar.make(getActivity().findViewById(R.id.nav_host_fragment), "Image successfully uploaded!", Snackbar.LENGTH_LONG)
                                .setAction("OK", null).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .addOnProgressListener(taskSnapshot -> {
                    //display upload status
                    double uploadProgress=(100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    progressDialog.setProgress((int) uploadProgress);



                })

                .addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    // ...
                    progressDialog.hide();
                    try {
                        Snackbar.make(getActivity().findViewById(R.id.nav_host_fragment), "Error uploading the image!"+exception.getLocalizedMessage(), Snackbar.LENGTH_LONG)
                                .setAction("OK", null).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void selectPicture() {
        //allow the user to select an image for upload to firebase
        Intent intentImagePicker= new Intent();
        intentImagePicker.setType("image/*");
        intentImagePicker.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentImagePicker,1);
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link @Activity #onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== RESULT_OK && data!=null && data.getData() != null){
       uriPicture=data.getData();
       binding.imageView.setImageURI(uriPicture);
            try {
                Snackbar.make(getActivity().findViewById(R.id.nav_host_fragment), "Your image has been loaded", Snackbar.LENGTH_LONG)
                        .setAction("OK", null).show();
            } catch (Exception e) {
                e.printStackTrace();
            }




        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            //Toast.makeText(getContext(),"", Toast.LENGTH_LONG).show();
            Navigation.findNavController(getView()).navigate(R.id.action_FirstFragment_to_loginFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater=getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);


        super.onCreateOptionsMenu(menu, inflater);



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
}
