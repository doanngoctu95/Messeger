package chotot.prect.aptech.zinzamessenger.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;

import chotot.prect.aptech.zinzamessenger.R;
import chotot.prect.aptech.zinzamessenger.model.User;
import chotot.prect.aptech.zinzamessenger.utils.Utils;

/**
 * Created by dell on 13/02/2017.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private Button mBtnSignUp;
    private Button mBtnLogin;
    private EditText mEdtEmail;
    private EditText mEdtPassword;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    //Constants
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 10;

    private Button mSignInButton;
    //Firebase and GoogleApiClient
    private GoogleApiClient mGoogleApiClient;
    private Button mBtnFbSignIn;
    private CallbackManager mCallbackManager;


    private DatabaseReference mReference;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        setDbReference();
        initGgSignIn();
        initFbSignIn();
        setAuthInstance();
    }

    private void initGgSignIn() {
        if (!Utils.verifyConnection(this)){
            Utils.showToast(Utils.INTERNET,this);
            finish();
        }
        mSignInButton = (Button) findViewById(R.id.sign_in_gg_button);
        mSignInButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, (GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void initFbSignIn(){
        if (!Utils.verifyConnection(this)){
            Utils.showToast(Utils.INTERNET,this);
            finish();
        }
        mBtnFbSignIn = (Button)findViewById(R.id.sign_in_fb_button);
        mBtnFbSignIn.setOnClickListener(this);

        FacebookSdk.sdkInitialize(this);
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance() .registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!= null){
                    goMainScreen();
                }
            }
        };

    }
    private void handleFacebookAccessToken(AccessToken accessToken){
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Utils.showToast("Firebase fb login error",getApplicationContext());
                    }
            }
        });
    }
    private void goMainScreen(){
        startActivity(new Intent(LoginActivity.this,MessageFriendActivity.class));
    }
    private void initView() {
        mBtnSignUp = (Button) findViewById(R.id.btnSignUp);

        mBtnLogin= (Button) findViewById(R.id.btnLogin);
        mEdtEmail = (EditText) findViewById(R.id.edtEmailLogin);
        mEdtPassword = (EditText) findViewById(R.id.edtPassWrLogin);

        mBtnSignUp.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id= view.getId();
        switch (id){
            case R.id.btnSignUp:
                Intent intent2 = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent2);
                break;
            case R.id.btnLogin:
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                logIn(getEmail(),getPassword());
                break;
            case R.id.sign_in_gg_button:
                signIn();
                showProgress("Log in google","Loading...");
                break;
            case R.id.sign_in_fb_button:
                signInFB();
                break;
            default:
                return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    private String getEmail(){
        return mEdtEmail.getText().toString().trim();
    }
    private String getPassword(){
        return mEdtPassword.getText().toString().trim();
    }
    private boolean validateLogin(){
        if(getEmail().equals("") || getPassword().equals("")){
            return false;
        } else {
            return true;
        }
    }
    private void setAuthInstance(){
        mAuth = FirebaseAuth.getInstance();
    }
    private void logIn(String email,String password){
        showProgress("Login","Logging");
        if(validateLogin()){
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mProgressDialog.dismiss();
                    if(task.isSuccessful()){
                        Utils.showToast("Login Success",getApplicationContext());
                        Intent intent1 = new Intent(LoginActivity.this, MessageFriendActivity.class);
                        startActivity(intent1);
                    } else {
                        Utils.showToast("Login failed",getApplicationContext());
                    }
                }
            });
        } else {
            mProgressDialog.dismiss();
            Utils.showToast("Not enough information to login",this);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                checkExitedUser(account);
            } else {
                Log.e(TAG, "Google Sign In failed.");
                mProgressDialog.dismiss();
            }
        }
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
    }

    //check user existed or not
    private void checkExitedUser(final GoogleSignInAccount account){
        mReference.child("users").orderByChild("mEmail").equalTo(account.getEmail()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){

                   firebaseAuthWithGoogle(account);
                }
                else {
                    // init gmail user in users table db
                    initUserDatabase(account);
                    firebaseAuthWithGoogle(account);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setDbReference(){
        mReference = mDatabase.getInstance().getReference("users");
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);//AuthUI.getInstance()
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void initUserDatabase(GoogleSignInAccount account) {
//        setDbReference();
        String id = mReference.push().getKey();
        String displayName= account.getDisplayName();
        String email = account.getEmail();
        String password = "";
        String avata = account.getPhotoUrl()+"";

        User mUser = new User(id,displayName,email,password,avata,"",1,"",createAt());
        mReference.child(id).setValue(mUser);

    }
    private String createAt(){
        return java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Utils.showToast("Google Play Services error.",this);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Utils.showToast("Authentication failed",LoginActivity.this);
                        } else {
                            startActivity(new Intent(LoginActivity.this, MessageFriendActivity.class));
                            mProgressDialog.dismiss();
                            finish();
                        }
                    }
                });
    }
    private void signInFB(){
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("public_profile", "user_friends","email"));
        LoginManager.getInstance().logInWithPublishPermissions(
                this,
                Arrays.asList("publish_actions"));

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showProgress(String title, String message){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }
}
