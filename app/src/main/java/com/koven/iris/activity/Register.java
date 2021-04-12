package com.koven.iris.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.koven.iris.R;
import com.koven.iris.modal.User;
import com.koven.iris.util.Constants;
import com.koven.iris.util.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Register activity to register new user for first time login
 */
public class Register extends AppCompatActivity {

    /**
     * Request codes
     */
    private static final int SELECT_IMAGE = 1;

    /**
     * Shared preference to validate registered user
     */
    private SharedPreferences sharedPreferences;

    /**
     * image view for profile pic selection
     */
    private ImageView profileImage;
    private TextView registerBtn;
    private EditText usernameET;

    /**
     * User info fields
     */
    private Uri mImageUri = null;
    private String mUsername;
    private String mUserId;
    private String mPicUrl;

    /**
     * Task states
     */
    private int STATE_NONE = 0;
    private int STATE_UPLOADING = 1;
    private int mSTATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        profileImage = findViewById(R.id.profile_image);
        registerBtn = findViewById(R.id.register_btn);
        usernameET = findViewById(R.id.username_et);
        mSTATE = STATE_NONE;
//        deleteSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME);
    }

    @Override
    protected void onStart() {
        //create user if does not exist
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME, MODE_PRIVATE);

        //check if user is registered
        String userInfo = sharedPreferences.getString(Constants.USER_INFO, null);
        if (userInfo != null) {
            JSONObject userObject;
            try {
                userObject = new JSONObject(userInfo);
                Session.currentUser = new User(
                        "" + userObject.getString(Constants.USERNAME),
                        "" + userObject.getString(Constants.USER_ID),
                        "" + userObject.getString(Constants.PIC_URL)
                );
                startActivity(new Intent(getApplicationContext(), Users.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //if not registered
            setupRegisterActivity();
        }
        super.onStart();
    }

    private void setupRegisterActivity() {
        //setup register activity
        profileImage.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, SELECT_IMAGE);
        });

        usernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSTATE == STATE_UPLOADING)
                    return;
                if (s.toString().trim().length() == 0) {
                    registerBtn.setAlpha(0.45f);
                } else {
                    registerBtn.setAlpha(1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        registerBtn.setOnClickListener(v -> {
            if (mSTATE == STATE_UPLOADING)
                return;
            mUserId = System.currentTimeMillis() + "";
            mUsername = usernameET.getText().toString().trim();
            if (mUsername.length() == 0) {
                mSTATE = STATE_NONE;
                return;
            }
            if (mImageUri != null) {
                mSTATE = STATE_UPLOADING;
                registerUser();
            } else {
                mSTATE = STATE_NONE;
                Toast.makeText(this, "Picture not selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        registerBtn.setText("Registering...");
        FirebaseStorage.getInstance()
                .getReference("profile_pictures")
                .child(mUsername)
                .putFile(mImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getMetadata()
                            .getReference()
                            .getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                mPicUrl = uri.toString(); //received pic url
                                HashMap<String, String> map = new HashMap<>(); //mapping for storing information into database
                                map.put(Constants.USER_ID, mUserId);
                                map.put(Constants.USERNAME, mUsername);
                                map.put(Constants.PIC_URL, mPicUrl);
                                FirebaseFirestore.getInstance()
                                        .collection(Constants.USERS_COLLECTION)
                                        .document(mUserId)
                                        .set(map)
                                        .addOnSuccessListener(aVoid -> {
                                            //user registered
                                            //saving user info locally
                                            JSONObject jsonObject = new JSONObject();
                                            try {
                                                jsonObject.put(Constants.USERNAME, mUsername);
                                                jsonObject.put(Constants.USER_ID, mUserId);
                                                jsonObject.put(Constants.PIC_URL, mPicUrl);
                                                String jsonString = jsonObject.toString();
                                                Session.currentUser = new User("" + mUsername, "" + mUserId, "" + mPicUrl);
                                                sharedPreferences.edit().putString(Constants.USER_INFO, jsonString).apply();
                                                //redirect to users activity
                                                startActivity(new Intent(getApplicationContext(), Users.class));
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                finish();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            mSTATE = STATE_NONE;
                                            Toast.makeText(Register.this, "Task failed! Try again", Toast.LENGTH_SHORT).show();
                                        });
                            });
                })
                .addOnCanceledListener(() -> {
                    mSTATE = STATE_NONE;
                    Toast.makeText(Register.this, "Task cancelled", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    mSTATE = STATE_NONE;
                    Toast.makeText(Register.this, "Task failed! Try again", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    profileImage.setImageURI(selectedImageUri);
                    mImageUri = selectedImageUri;
                } else {
                    mImageUri = null;
                    profileImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user_vector));
                    Toast.makeText(this, "Can't load the picture", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            mImageUri = null;
            profileImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user_vector));
            Toast.makeText(this, "Picture not selected", Toast.LENGTH_SHORT).show();
        }
    }
}