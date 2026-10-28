package com.pianotools.garden;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.getcapacitor.BridgeActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/* Partage Android → Garden : un chant ChordPro / texte partagé ou ouvert arrive dans « Mes chants » */
public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleShare(getIntent(), 2500);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleShare(intent, 300);
  }

  private void handleShare(Intent i, int delay) {
    if (i == null) return;
    String a = i.getAction();
    if (!Intent.ACTION_SEND.equals(a) && !Intent.ACTION_VIEW.equals(a)) return;
    String txt = null;
    try {
      if (Intent.ACTION_SEND.equals(a)) txt = i.getStringExtra(Intent.EXTRA_TEXT);
      Uri u = null;
      if (txt == null && Intent.ACTION_SEND.equals(a)) u = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
      if (txt == null && u == null) u = i.getData();
      if (txt == null && u != null) {
        InputStream in = getContentResolver().openInputStream(u);
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line; int n = 0;
        while ((line = r.readLine()) != null && n < 20000) { sb.append(line).append('\n'); n++; }
        r.close();
        txt = sb.toString();
      }
    } catch (Exception e) { txt = null; }
    if (txt == null || txt.trim().isEmpty()) return;
    final String js = "window.__ffShared=" + JSONObject.quote(txt) + ";if(window.ffImport)ffImport(window.__ffShared);";
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      try { getBridge().getWebView().evaluateJavascript(js, null); } catch (Exception e) {}
    }, delay);
  }
}
