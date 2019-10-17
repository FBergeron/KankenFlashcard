package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        ImageButton btnAgreement = findViewById(R.id.btnAgreement);

        WebView webView = findViewById(R.id.webView);
        CustomWebViewClient webViewClient = new CustomWebViewClient();
        webViewClient.setOnPageFinshedListener((view, url) -> btnAgreement.setVisibility(View.VISIBLE));
        webView.setWebViewClient(webViewClient);
        webView.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        webView.loadUrl(getString(R.string.link_terms_of_usage));
    }

    public void onClickAgreement(View view) {
        Util.setAgreement(this);
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);
    }
}
