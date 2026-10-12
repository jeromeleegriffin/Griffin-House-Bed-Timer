package com.griffinhouse.bedtimer;
import android.app.*;import android.os.*;import android.content.*;import android.webkit.*;import android.Manifest;import android.content.pm.PackageManager;import android.provider.Settings;import android.net.Uri;import androidx.webkit.WebViewAssetLoader;
public class MainActivity extends Activity {
 WebView web;ValueCallback<Uri[]> fileCallback;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  web=new WebView(this);setContentView(web);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);
  web.addJavascriptInterface(new Object(){
   @JavascriptInterface public void schedule(String json){runOnUiThread(()->{try{org.json.JSONObject o=new org.json.JSONObject(json);org.json.JSONArray a=o.getJSONArray("beds");int[] beds=new int[a.length()];for(int j=0;j<beds.length;j++)beds[j]=a.getInt(j);AlarmScheduler.schedule(MainActivity.this,o.getLong("start"),o.getLong("interval"),o.getInt("count"),beds,o.optString("alarmId","chime"),o.optString("personaId","sexy"),o.optString("sayText","Bed is coming out."));}catch(Exception ignored){}});}
   @JavascriptInterface public void cancel(){runOnUiThread(()->AlarmScheduler.cancel(MainActivity.this));}
  },"BedNative");
  WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return loader.shouldInterceptRequest(request.getUrl());}
  });
  web.loadUrl("https://appassets.androidplatform.net/assets/bed-times.html");
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},42);
  if(Build.VERSION.SDK_INT>=31){AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);if(!am.canScheduleExactAlarms()){try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception ignored){}}}
 }
 @Override protected void onResume(){super.onResume();AlarmScheduler.reschedule(this);}
}
