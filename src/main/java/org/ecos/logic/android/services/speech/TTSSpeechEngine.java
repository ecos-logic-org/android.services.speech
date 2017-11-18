package org.ecos.logic.android.services.speech;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

import org.ecos.logic.services.interfaces.exceptions.NotInitialized;
import org.ecos.logic.android.core.services.resolvers.ApplicationContextResolver;
import org.ecos.logic.services.interfaces.speech.SpeechEngine;
import org.ecos.logic.services.interfaces.speech.SpeechFinishedAction;

import java.util.HashMap;

public class TTSSpeechEngine implements SpeechEngine, OnInitListener, OnUtteranceCompletedListener {
    private static final String TAG = TTSSpeechEngine.class.getSimpleName();

    private Context mApplicationContext;

    private TextToSpeech mTextToSpeech;

    private boolean mInitialized = false;

    private SpeechFinishedAction mOnSpeechFinishedAction;

    private void init() {
        if (!mInitialized) {
            mTextToSpeech = new TextToSpeech(mApplicationContext, this);
            mInitialized = true;
        }

    }

    public TTSSpeechEngine(ApplicationContextResolver applicationContextResolver) {
        mApplicationContext = applicationContextResolver.getApplicationContext();

        init();
    }

    @Override
    public void speak(String text) throws NotInitialized {
        init();
        if (mInitialized) {
            HashMap<String, String> myHashAlarm = new HashMap<>();
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");

            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
            Log.d("SpeechEngine", "mTextToSpeech.speak");
        } else {
            throw new NotInitialized(TAG + ": The service can't be mInitialized.");
        }

    }

    @Override
    public void speak(String text, SpeechFinishedAction speechFinishedAction) throws NotInitialized {
        mOnSpeechFinishedAction = speechFinishedAction;
        speak(text);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TextToSpeech.SUCCESS");
            mInitialized = true;
            mTextToSpeech.setOnUtteranceCompletedListener(this);
        } else
            mInitialized = false;
    }


    @Override
    public void onUtteranceCompleted(String utteranceId) {
        Log.d(TAG, "OnUtteranceCompletedListener");
        if (mOnSpeechFinishedAction != null) {
            try {
                mOnSpeechFinishedAction.fireFinished();
            } catch (NotInitialized notInitialized) {
                notInitialized.printStackTrace();
            }finally {
                mOnSpeechFinishedAction = null;
            }
        }
    }

}