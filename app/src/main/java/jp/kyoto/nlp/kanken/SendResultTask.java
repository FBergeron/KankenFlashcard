package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

class SendResultTask extends AsyncTask {

    public SendResultTask(KankenApplication appl, Context context, boolean quitAppl) {
        this.appl = appl;
        this.context = context;
        this.quitAppl = quitAppl;
    }

    protected Object doInBackground(Object... objs) {
        URL storeResultUrl = (URL)objs[0];
        try {
            Map<String, String> params = new HashMap<String, String>();

            int index = appl.getQuiz().getAnswerCount() - 1;
            if (index < 0)
                return null;

            Problem problem = appl.getQuiz().getProblem(index);
            String answer = appl.getQuiz().getAnswer(index);
            Boolean isRightAnswer = appl.getQuiz().getRightAnswer(index);
            Integer familiarity = appl.getQuiz().getFamiliarity(index);
            Boolean isReportedAsIncorrect = appl.getQuiz().getReportedAsIncorrect(index);

            params.put("problemId", problem.getId());
            params.put("problemJuman", problem.getJumanInfo());
            params.put("problemRightAnswer", (isRightAnswer ? 1 : 0) + "");
            params.put("problemFamiliarity", familiarity + "");
            params.put("problemAnswer", answer);
            params.put("problemReportedAsIncorrect", (isReportedAsIncorrect ? 1 : 0) + "");

            StringBuilder builder = new StringBuilder();
            String delimiter = "";
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.append(delimiter).append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                delimiter = "&";
            }
            byte[] data = builder.toString().getBytes("UTF-8");

            HttpURLConnection con = (HttpURLConnection) storeResultUrl.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            String cookie = appl.getSessionCookie();
            if (cookie != null)
                con.setRequestProperty("Cookie", cookie);
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
            Log.d(TAG, "status=" + status);
            if (!"ok".equals(status))
                exception = new Exception("Server responded with status=" + status + ". Something is probably wrong.");
        }
        catch (IOException e) {
            e.printStackTrace();

            exception = e;
        }
        catch (JSONException e2) {
            e2.printStackTrace();

            exception = e2;
        }

        return null;
    }

    protected void onPostExecute(final Object obj) {
        Log.e(TAG, "onPostExecute obj="+obj+" exception="+exception+ " quitAppl="+quitAppl+" appl="+appl);
        if (exception != null) {
            Log.e(TAG, "An exception has occurred: " + exception);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getResources().getString(R.string.error_server_unreachable_title))
            .setMessage(context.getResources().getString(R.string.error_server_unreachable_msg))
            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
             })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .show();

            return;
        }

        if (quitAppl)
            appl.quit();
    }

    private Exception exception;

    private KankenApplication appl;

    private Context context;

    private boolean quitAppl;

    private static final String TAG = "SendResultTask";

}



