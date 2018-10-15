package jp.kyoto.nlp.kanken;

import android.media.MediaPlayer;

public class ImprovedMediaPlayer extends MediaPlayer {

    public float getVolume() {
        return currentVol;
    }

    public boolean setVolume(float newVol) {
        if (newVol <= 0)
            newVol = 0f;
        else if (newVol >= 1.0f)
            newVol = 1.0f;

        boolean volChanged = (Math.abs(newVol - currentVol) > 0.0001f);
        this.setVolume(newVol, newVol);
        return volChanged;
    }


    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        currentVol = leftVolume;
        super.setVolume(leftVolume, rightVolume);
    }

    private float currentVol = 1.0f;

}
