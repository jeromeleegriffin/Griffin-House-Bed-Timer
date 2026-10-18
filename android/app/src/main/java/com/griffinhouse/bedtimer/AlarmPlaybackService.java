package com.griffinhouse.bedtimer;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import org.json.JSONObject;

/** One-shot alarm: selected chime, then the configured spoken message, then stop. */
public final class AlarmPlaybackService extends Service implements TextToSpeech.OnInitListener {
 private static final String CHANNEL="bed_playback_v1";
 private static final double[][] NOTES={
  {523.25,659.25,783.99,987.77,783.99,659.25,523.25},
  {784,988,1175,988,784,659},
  {392,494,587,740,880,740},
  {440,440,554,440,659,440},
  {349,440,523,659,784,880},
  {988,784,1175,659,880,523},
  {392,349,330,294,330,392},
  {880,1320,880,1320,1760}
 };
 private static final String[] IDS={"chime","bells","harp","pulse","dawn","drops","lull","ping"};
 private static final double[] STEPS={.38,.42,.4,.45,.4,.48,.46,.5};
 private static final double[] DURS={.55,.5,.6,.35,.58,.28,.62,.25};
 private static final double[] GAINS={.18,.16,.17,.2,.15,.16,.17,.14};
 private TextToSpeech tts;
 private final Handler main=new Handler(Looper.getMainLooper());
 private String phrase="Bed is coming out.", persona="sexy", voiceId="";
 private volatile boolean stopped=false;
 private int request=0;
 @Override public IBinder onBind(Intent intent){return null;}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(intent==null){stopSelf();return START_NOT_STICKY;}
  if(tts!=null){tts.stop();tts.shutdown();tts=null;}
  stopped=false;request=startId;
  boolean preview=intent.getBooleanExtra("previewVoiceOnly",false);
  String alarm="chime";
  try{
   JSONObject o=new JSONObject(getSharedPreferences(AlarmScheduler.PREF,0).getString(AlarmScheduler.KEY,"{}"));
   phrase=o.optString("sayText","Bed is coming out.").trim();
   if(phrase.length()>160)phrase=phrase.substring(0,160);
   persona=o.optString("personaId","sexy");
   alarm=o.optString("alarmId","chime");voiceId=o.optString("voiceId","");
  }catch(Exception ignored){}
  if(preview){phrase=intent.getStringExtra("phrase");if(phrase==null)phrase="Bed is coming out.";
    if(phrase.length()>160)phrase=phrase.substring(0,160);
    persona=intent.getStringExtra("persona");voiceId=intent.getStringExtra("voiceId");}
  NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  NotificationChannel channel=new NotificationChannel(CHANNEL,"Bed Timer playback",NotificationManager.IMPORTANCE_LOW);
  channel.setSound(null,null);channel.enableVibration(false);nm.createNotificationChannel(channel);
  int bed=intent.getIntExtra("bed",-1);
  Notification notification=new Notification.Builder(this,CHANNEL)
   .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
   .setContentTitle("Bed "+(bed>0?bed:"")+" coming out")
   .setContentText("Playing bed chime and announcement")
   .setCategory(Notification.CATEGORY_ALARM).setOngoing(true).build();
  startForeground(203,notification);
  final int choice=choice(alarm);
  if(preview){tts=new TextToSpeech(this,this);main.postDelayed(()->{if(!stopped)finish();},15000);return START_NOT_STICKY;}
  new Thread(()->{
   try{play(choice);}catch(Exception ignored){}
   main.post(()->{
    if(stopped)return;
    if(phrase.isEmpty()){finish();return;}
    tts=new TextToSpeech(this,this);
    main.postDelayed(()->{if(!stopped)finish();},15000);
   });
  },"BedChime").start();
  return START_NOT_STICKY;
 }
 private static int choice(String id){for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;return 0;}
 private void play(int id){
  final int rate=22050;
  int total=(int)(((NOTES[id].length-1)*STEPS[id]+DURS[id]+.06)*rate);
  short[] pcm=new short[total];
  for(int n=0;n<NOTES[id].length;n++){
   int start=(int)(n*STEPS[id]*rate),len=(int)(DURS[id]*rate);
   for(int i=0;i<len&&start+i<total;i++){
    double t=i/(double)rate;
    double attack=Math.min(1,t/.04),decay=Math.exp(-3*t/DURS[id]);
    double value=Math.sin(2*Math.PI*NOTES[id][n]*t)*attack*decay*GAINS[id];
    int sample=pcm[start+i]+(int)(value*32767);
    pcm[start+i]=(short)Math.max(-32768,Math.min(32767,sample));
   }
  }
  AudioTrack track=new AudioTrack.Builder()
   .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
   .setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
   .setBufferSizeInBytes(pcm.length*2).setTransferMode(AudioTrack.MODE_STATIC).build();
  try{
   track.write(pcm,0,pcm.length);track.play();
   long end=SystemClock.elapsedRealtime()+(long)(total*1000.0/rate)+300;
   while(!stopped&&SystemClock.elapsedRealtime()<end)Thread.sleep(60);
  }catch(Exception ignored){}finally{track.stop();track.release();}
 }
 @Override public void onInit(int status){
  if(stopped)return;
  if(status!=TextToSpeech.SUCCESS||tts==null){finish();return;}
  tts.setLanguage(Locale.US);
  if(voiceId!=null&&!voiceId.isEmpty())try{for(android.speech.tts.Voice v:tts.getVoices()){if(v.getName().equals(voiceId)){tts.setVoice(v);break;}}}catch(Exception ignored){}
  float pitch=1.05f,rate=.92f;
  switch(persona){
   case "calm":pitch=.99f;rate=.90f;break;
   case "whisper":pitch=1.02f;rate=.88f;break;
   case "cheerful":pitch=1.05f;rate=1.04f;break;
   case "tired":pitch=.97f;rate=.88f;break;
   case "firm":pitch=.98f;rate=.97f;break;
   case "mad":pitch=.96f;rate=1.06f;break;
   case "mean":pitch=.95f;rate=.98f;break;
   case "aggro":pitch=.94f;rate=1.08f;break;
   case "sweet":pitch=1.04f;rate=.96f;break;
  }
  tts.setPitch(pitch);tts.setSpeechRate(rate);
  tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener(){
   @Override public void onStart(String id){}
   @Override public void onDone(String id){main.post(()->finish());}
   @Override public void onError(String id){main.post(()->finish());}
  });
  if(tts.speak(phrase,TextToSpeech.QUEUE_FLUSH,null,"bed-due")!=TextToSpeech.SUCCESS)finish();
 }
 private void finish(){if(stopped)return;stopped=true;stopForeground(STOP_FOREGROUND_REMOVE);stopSelf(request);}
 @Override public void onDestroy(){stopped=true;if(tts!=null){tts.stop();tts.shutdown();tts=null;}super.onDestroy();}
}
