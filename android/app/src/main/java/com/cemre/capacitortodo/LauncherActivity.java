package com.cemre.capacitortodo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LauncherActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setGravity(Gravity.CENTER);
    layout.setPadding(60, 60, 60, 60);

    TextView title = new TextView(this);
    title.setText("Native Android Ekranı");
    title.setTextSize(24);
    title.setGravity(Gravity.CENTER);

    EditText nameInput = new EditText(this);
    nameInput.setHint("İsminizi giriniz");

    Button button = new Button(this);
    button.setText("Todo Uygulamasına Geç");

    layout.addView(title);
    layout.addView(nameInput);
    layout.addView(button);

    setContentView(layout);

    button.setOnClickListener(view -> {
      String userName = nameInput.getText().toString().trim();
      String encodedName = Uri.encode(userName);

      Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
      intent.setAction(Intent.ACTION_VIEW);
      intent.setData(Uri.parse("capacitortodo://open?userName=" + encodedName));

      startActivity(intent);
      finish();
    });
  }
}