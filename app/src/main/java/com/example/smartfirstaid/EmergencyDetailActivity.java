package com.example.smartfirstaid;

import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfirstaid.data.db.MongoHelper;   // <- helper you created
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmergencyDetailActivity extends AppCompatActivity {

    private String key;
    private String title;

    private TextView tvTitle, tvDo, tvDont;
    private ProgressBar progress;
    private Button btnVoice;

    private List<String> voiceScript = new ArrayList<>();
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);

        // --- get extras from the previous screen ---
        key = getIntent().getStringExtra("key");      // e.g., "snake_bite"
        title = getIntent().getStringExtra("title");  // UI title

        // --- bind views ---
        tvTitle = findViewById(R.id.tvTitle);
        tvDo    = findViewById(R.id.tvDoList);
        tvDont  = findViewById(R.id.tvDontList);
        progress= findViewById(R.id.progress);
        btnVoice= findViewById(R.id.btnVoiceGuide);

        tvTitle.setText(title != null ? title : "Emergency");

        // --- init TTS (optional voice guidance) ---
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.95f);
            }
        });

        btnVoice.setOnClickListener(v -> speakVoiceScript());

        // --- fetch details from MongoDB ---
        new LoadProcedureTask().execute(key);
    }

    /** AsyncTask to load one procedure document by key */
    private class LoadProcedureTask extends AsyncTask<String, Void, Document> {
        private String error;

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            btnVoice.setEnabled(false);
        }

        @Override
        protected Document doInBackground(String... keys) {
            try {
                MongoCollection<Document> col = MongoHelper.procedures();
                // projection excludes _id
                return col.find(new Document("key", keys[0]))
                        .projection(new Document("_id", 0))
                        .first();
            } catch (Exception e) {
                error = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Document d) {
            progress.setVisibility(View.GONE);

            if (d == null) {
                Toast.makeText(EmergencyDetailActivity.this,
                        "Failed to load instructions: " + (error == null ? "Unknown error" : error),
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Safely extract lists
            List<String> doList   = castStringList(d.get("do"));
            List<String> dontList = castStringList(d.get("dont"));
            voiceScript            = castStringList(d.get("voiceScript"));

            tvDo.setText(toBullets(doList));
            tvDont.setText(toBullets(dontList));
            btnVoice.setEnabled(!voiceScript.isEmpty());
        }
    }

    /** Defensive cast helper */
    @SuppressWarnings("unchecked")
    private List<String> castStringList(Object o) {
        if (o instanceof List<?>) {
            List<?> raw = (List<?>) o;
            List<String> out = new ArrayList<>();
            for (Object item : raw) if (item != null) out.add(item.toString());
            return out;
        }
        return new ArrayList<>();
    }

    /** Render bullet list */
    private String toBullets(List<String> items) {
        if (items == null || items.isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        for (String s : items) sb.append("• ").append(s).append("\n\n");
        return sb.toString();
    }

    /** Speak the voiceScript lines */
    private void speakVoiceScript() {
        if (tts == null || voiceScript.isEmpty()) return;
        String script = String.join(". ", voiceScript);
        tts.speak(script, TextToSpeech.QUEUE_FLUSH, null, "SFA_VOICE");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
