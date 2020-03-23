package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        // スプラッシュthemeを通常themeに変更する
//        setTheme(R.style.AppTheme);
        Intent intent;
        if (Util.needAgreement(this)) {
            intent = new Intent(this, AgreementActivity.class);
        } else {
            intent = new Intent(this, AuthenticationActivity.class);
        }
        startActivity(intent);
    }
}
