package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public void play(android.view.View view) {
        Intent quizSettingsActivity = new Intent(AuthenticationActivity.this, QuizSettingsActivity.class);
        startActivity(quizSettingsActivity);
    }

    public void signOut(android.view.View view) {
        System.out.println("signOut");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        layoutUserInfo = (LinearLayout)findViewById(R.id.layoutUserInfo);
        buttonSignOut = (Button)findViewById(R.id.buttonSignOut);
        buttonSignIn = (SignInButton)findViewById(R.id.buttonSignIn);
        textViewUserName = (TextView)findViewById(R.id.textViewUserName);
        textViewUserEmail = (TextView)findViewById(R.id.textViewUserEmail);
        imageViewUserPicture = (ImageView)findViewById(R.id.imageViewUserPicture);

        // layoutUserInfo.setVisibility(View.GONE);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();


        buttonSignIn.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(intent, REQ_CODE);
                }
            }
        );
    }

    private void handleResult(GoogleSignInResult result) {
System.out.println( "isSuccess="+result.isSuccess() );        
        // if (result.isSuccess()) {
        //     GoogleSignInAccount account = result.getSignInAccount();
        //     String name = account.getDisplayName();
        //     String email = account.getEmail();
        //     // String pictureUrl = account.getPhotoUrl();
        //     textViewUserName.setText(name);
        //     textViewUserEmail.setText(email);
        //     // Glide.with(this).load(pictureUrl).into(imageViewUserPicture);
        //     updateUI(true);
        // }
        // else
        //     updateUI(false);
    }

    private void updateUI(boolean isAuthenticated) {
        if (isAuthenticated) {
            layoutUserInfo.setVisibility(View.VISIBLE);
            buttonSignIn.setVisibility(View.GONE);
        }
        else {
            layoutUserInfo.setVisibility(View.GONE);
            buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
System.out.println( "onActivityResult requestCode="+requestCode+ " resultCode="+resultCode + " data="+data );

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
System.out.println( "result="+result );

            handleResult(result);
        }
    }

    private LinearLayout layoutUserInfo;
    private Button buttonSignOut;
    private SignInButton buttonSignIn;
    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private ImageView imageViewUserPicture;

    private GoogleApiClient googleApiClient;

    private static final int REQ_CODE = 9001;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {

    }
}
