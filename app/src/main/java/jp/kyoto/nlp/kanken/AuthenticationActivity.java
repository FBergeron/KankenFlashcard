package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public void play(android.view.View view) {
        Intent quizSettingsActivity = new Intent(AuthenticationActivity.this, QuizSettingsActivity.class);
        startActivity(quizSettingsActivity);
    }

    public void signOut(android.view.View view) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                appl.setUserName(null);
                appl.setUserEmail(null);
                appl.setUserIdToken(null);
                appl.setSessionCookie(null);

                updateUI(false);
            }
        });
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
        textViewAuthenticationInfoTitle = (TextView)findViewById(R.id.textViewAuthenticationInfoTitle);
        textViewAuthenticationInfo = (TextView)findViewById(R.id.textViewAuthenticationInfo);

        layoutUserInfo.setVisibility(View.GONE);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(clientId).requestEmail().build();
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
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            String email = account.getEmail();
            String idToken = account.getIdToken();

            Uri pictureUrl = account.getPhotoUrl();
            textViewUserName.setText(name);
            textViewUserEmail.setText(email);
           
            appl.setUserName(name);
            appl.setUserEmail(email);
            appl.setUserIdToken(idToken);

            Glide.with(this).load(pictureUrl).into(imageViewUserPicture);

            URL signInUrl = null;
            try {
                signInUrl = new URL(appl.getServerBaseUrl() + signInReqPath);
                
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getResources().getString(R.string.label_signing_in));
                progressDialog.setCancelable(false);
                progressDialog.show();

                new SignInTask().execute(signInUrl);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch(IOException e2) {
                e2.printStackTrace();
            }
            catch(JSONException e3) {
                e3.printStackTrace();
            }
        }
        else {
            appl.setUserName(null);
            appl.setUserEmail(null);

            updateUI(false);
        }
    }

    private void updateUI(boolean isAuthenticated) {
        if (isAuthenticated) {
            layoutUserInfo.setVisibility(View.VISIBLE);
            buttonSignIn.setVisibility(View.GONE);
            textViewAuthenticationInfoTitle.setVisibility(View.GONE);
            textViewAuthenticationInfo.setVisibility(View.GONE);
        }
        else {
            layoutUserInfo.setVisibility(View.GONE);
            buttonSignIn.setVisibility(View.VISIBLE);
            textViewAuthenticationInfoTitle.setVisibility(View.VISIBLE);
            textViewAuthenticationInfo.setVisibility(View.VISIBLE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {

    }

    private class SignInTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            URL signInUrl = (URL)objs[0];
            try {
                Map<String, String> params = new HashMap<String, String>();
                params.put("idToken", appl.getUserIdToken());
                
                StringJoiner joiner = new StringJoiner("&");
                for (Map.Entry<String, String> entry : params.entrySet())
                    joiner.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
                byte[] data = joiner.toString().getBytes("UTF-8");

                HttpURLConnection con = (HttpURLConnection) signInUrl.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                con.setFixedLengthStreamingMode(data.length);
                con.connect();

                OutputStream writer = con.getOutputStream();
                writer.write(data);
                writer.flush();
                writer.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                List<String> cookieHeaders = con.getHeaderFields().get("Set-Cookie");
                System.out.println( "cookie first header="+cookieHeaders.get(0));
                appl.setSessionCookie(cookieHeaders.get(0));
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(final Object obj) {
            if (exception != null) {
                System.out.println("An exception has occured: " + exception);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                return;
            }

            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            updateUI(true);
        }

        private Exception exception;

    }

    private LinearLayout layoutUserInfo;
    private Button buttonSignOut;
    private SignInButton buttonSignIn;
    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private ImageView imageViewUserPicture;
    private TextView textViewAuthenticationInfoTitle;
    private TextView textViewAuthenticationInfo;

    private GoogleApiClient googleApiClient;

    private KankenApplication appl = KankenApplication.getInstance();

    private ProgressDialog progressDialog;

    private static final int REQ_CODE = 9001;

    private static final String clientId = "20392918182-4qlj5ff67m0hbm3raiq92cn9lokag1a6.apps.googleusercontent.com";
   
    private static final String signInReqPath = "/cgi-bin/sign_in.cgi";

}
