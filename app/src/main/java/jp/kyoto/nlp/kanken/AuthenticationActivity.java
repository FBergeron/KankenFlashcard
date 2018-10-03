package jp.kyoto.nlp.kanken;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import org.json.JSONObject;

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

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public void play(android.view.View view) {
        Intent quizSettingsActivity = new Intent(AuthenticationActivity.this, QuizSettingsActivity.class);
        startActivity(quizSettingsActivity);
    }

    public void signOut(android.view.View view) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
            new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    URL signOutUrl;
                    try {
                        signOutUrl = new URL(appl.getServerBaseUrl() + signOutReqPath);

                        progressDialog = new ProgressDialog(AuthenticationActivity.this);
                        progressDialog.setMessage(getResources().getString(R.string.label_signing_out));
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        new SignOutTask().execute(signOutUrl);
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
            }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        layoutUserInfo = findViewById(R.id.layoutUserInfo);
        layoutLogin = findViewById(R.id.layoutLogin);
        Button buttonSignOut = findViewById(R.id.buttonSignOut);
        SignInButton buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        imageViewUserPicture = findViewById(R.id.imageViewUserPicture);
        TextView textViewAuthenticationInfoTitle = findViewById(R.id.textViewAuthenticationInfoTitle);
        TextView textViewAuthenticationInfo = findViewById(R.id.textViewAuthenticationInfo);

        // No title needed for now.  Delete it eventually.
        textViewAuthenticationInfoTitle.setVisibility(View.GONE);

        layoutUserInfo.setVisibility(View.GONE);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(Util.googleClientId).requestEmail().build();
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
            assert account != null;
            String name = account.getDisplayName();
            String email = account.getEmail();
            String idToken = account.getIdToken();

            Uri pictureUrl = account.getPhotoUrl();
            textViewUserName.setText(name);
            textViewUserEmail.setText(email);
           
            appl.setUserName(name);
            appl.setUserEmail(email);
            appl.setUserPictureUrl(pictureUrl);
            appl.setUserIdToken(idToken);

            Glide.with(this).load(pictureUrl).into(imageViewUserPicture);

            URL signInUrl;
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
            layoutLogin.setVisibility(View.GONE);
        }
        else {
            layoutUserInfo.setVisibility(View.GONE);
            layoutLogin.setVisibility(View.VISIBLE);
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

        public static final int MAX_ATTEMPTS = 3;

        protected Object doInBackground(Object... objs) {
            int attempt = 0; 
            while (attempt < MAX_ATTEMPTS) {
                URL signInUrl = (URL)objs[0];
                try {
                    String version = null;
                    try {
                        PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        version = pkgInfo.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("idToken", appl.getUserIdToken());
                    params.put("clientVersion", version);
                   
                    StringBuilder builder = new StringBuilder();
                    String delimiter = "";
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        builder.append(delimiter).append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                        delimiter = "&";
                    }
                    byte[] data = builder.toString().getBytes("UTF-8");

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
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String status = jsonResponse.getString("status");

                    List<String> cookieHeaders = con.getHeaderFields().get("Set-Cookie");
                    appl.setSessionCookie(cookieHeaders.get(0));

                    if ("client_too_old".equals(status))
                        exception = new Exception(status);
                    else if (!"ok".equals(status))
                        exception = new Exception("Server responded with status=" + status + ". Something is probably wrong.");

                    return null;
                }
                catch (IOException e) {
                    e.printStackTrace(); 
                    attempt++;
                    if (attempt == MAX_ATTEMPTS) {
                        exception = e;
                        System.err.println("I give up after " + attempt + " attempts.");
                    }
                    else 
                        System.err.println("Let's try again (attempt=" + attempt+")");
                }
                catch(JSONException e2) {
                    e2.printStackTrace(); 
                    exception = e2;
                }
            }

            return null;
        }

        protected void onPostExecute(final Object obj) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            if (exception != null) {
                String title;
                String msg;
                if ("client_too_old".equals(exception.getMessage())) {
                    title = getResources().getString(R.string.error_client_too_old_title);
                    msg = getResources().getString(R.string.error_client_too_old_msg);
                }
                else {
                    title = getResources().getString(R.string.error_server_unreachable_title);
                    msg = getResources().getString(R.string.error_server_unreachable_msg);
                }

                Log.e(tag, "An exception has occurred: " + exception);

                AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
                builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                 })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .show();

                return;
            }

            //updateUI(true);
            play(null);
        }

        private Exception exception;

    }

    private class SignOutTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            URL signOutUrl = (URL)objs[0];
            try {
                HttpURLConnection con = (HttpURLConnection) signOutUrl.openConnection();
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                    con.setFixedLengthStreamingMode(0);
                con.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                JSONObject jsonResponse = new JSONObject(response.toString());
                String status = jsonResponse.getString("status");
                Log.d(tag, "status=" + status);
                if (!"ok".equals(status))
                    exception = new Exception("Server responded with status=" + status + ". Something is probably wrong.");

                return null;
            }
            catch(IOException e) {
                e.printStackTrace(); 
                exception = e;
            }
            catch(JSONException e2) {
                e2.printStackTrace(); 
                exception = e2;
            }

            return null;
        }

        protected void onPostExecute(final Object obj) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            if (exception != null) {
                Log.e(tag, "An exception has occurred: " + exception);

                AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
                builder.setTitle(getResources().getString(R.string.error_server_unreachable_title))
                .setMessage(getResources().getString(R.string.error_server_unreachable_msg))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                 })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .show();

                return;
            }

            appl.setUserName(null);
            appl.setUserEmail(null);
            appl.setUserIdToken(null);
            appl.setSessionCookie(null);

            updateUI(false);
        }

        private Exception exception;

    }

    private LinearLayout layoutUserInfo;
    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private ImageView imageViewUserPicture;
    private LinearLayout layoutLogin;

    private GoogleApiClient googleApiClient;

    private KankenApplication appl = KankenApplication.getInstance();

    private ProgressDialog progressDialog;

    private static final int REQ_CODE = 9001;
   
    private static final String signInReqPath = "/cgi-bin/sign_in.cgi";
    private static final String signOutReqPath = "/cgi-bin/sign_out.cgi";

    private static final String tag = "AuthenticationActivity";

}
