package jp.kyoto.nlp.kanken;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

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

public class AuthenticationActivity extends BaseActionActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "Authentication";

    public void play(android.view.View view) {
        Intent quizSettingsActivity = new Intent(AuthenticationActivity.this, QuizSettingsActivity.class);
        quizSettingsActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(quizSettingsActivity);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Stay there.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        // スプラッシュthemeを通常themeに変更する
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_authentication);

        SignInButton buttonSignIn = findViewById(R.id.buttonSignIn);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(Util.googleClientId).requestEmail().build();
        Log.d(TAG, "onCreate: " + Util.googleClientId);
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

    @Override
    protected void onStart() {
        super.onStart();
        appl.stopBackgroundMusic();
    }

    private void handleResult(GoogleSignInResult result) {
        Log.d(TAG, "handleResult: " + result.getStatus().getStatusMessage());
        Log.d(TAG, "handleResult: " + result.getStatus().toString());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            assert account != null;
            String name = account.getDisplayName();
            String email = account.getEmail();
            String idToken = account.getIdToken();
            Log.d(TAG, "handleResult: " + String.format("%s,%s,%s", name, email, idToken));

            Uri pictureUrl = account.getPhotoUrl();

            appl.setUserName(name);
            appl.setUserEmail(email);
            appl.setUserPictureUrl(pictureUrl);
            appl.setUserIdToken(idToken);

            URL signInUrl;
            try {
                signInUrl = new URL(appl.getServerBaseUrl() + KankenApplication.signInReqPath);

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

    private class SignInTask extends AsyncTask {

        public static final int MAX_ATTEMPTS = 3;

        protected Object doInBackground(Object... objs) {
            int attempt = 0;
            while (attempt < MAX_ATTEMPTS) {
                URL signInUrl = (URL)objs[0];
                Log.d(TAG, "doInBackground: " + signInUrl.toString());
                try {
                    String version = "2.0";

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("idToken", appl.getUserIdToken());
                    params.put("clientVersion", version);
                    Log.d(TAG, "doInBackground: " + params.toString());

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

                    Log.d(TAG, "doInBackground: " + response.toString());

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String status = jsonResponse.getString("status");

                    List<String> cookieHeaders = con.getHeaderFields().get("Set-Cookie");
                    appl.setSessionCookie(cookieHeaders.get(0));

                    if ("client_too_old".equals(status)) {
                        String minVersion = jsonResponse.getString("min_version");
                        exception = new Exception(status + " minVersion=" + minVersion);
                    }
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
                if (exception.getMessage().startsWith("client_too_old")) {
                    String minVersion = exception.getMessage().substring(exception.getMessage().indexOf("=") + 1);
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

            play(null);
        }

        private Exception exception;

    }

    private GoogleApiClient googleApiClient;

    private KankenApplication appl = KankenApplication.getInstance();

    private ProgressDialog progressDialog;

    private static final int REQ_CODE = 9001;

    private static final String tag = "AuthenticationActivity";

    public void onClickTermsOfService(View view) {
        openUrl(R.string.link_terms_of_usage);
    }

    public void onClickHowToPlay(View view) {
        openUrl(R.string.link_directions);
    }

    public void onClickKanken(View v) {
        openUrl(R.string.link_kanken);
    }

    public void onClickKyotoUniversity(View v) {
        openUrl(R.string.link_kyoto_university);
    }

    public void onClickYomiuri(View v) {
        openUrl(R.string.link_yomiuri);
    }

    private void openUrl(@StringRes int resId) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        String termsOfUsageLink = getResources().getString(resId);
        httpIntent.setData(Uri.parse(termsOfUsageLink));
        startActivity(httpIntent);
    }

}
