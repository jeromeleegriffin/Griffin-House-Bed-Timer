package com.griffinhouse.bedtimer;
import android.app.*;import android.content.*;import android.os.Build;
public class AlarmReceiver extends android.content.BroadcastReceiver{
 @Override public void onReceive(Context c,Intent i){
  int bed=i.getIntExtra("bed",-1);
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  String channel="bed_due_silent_v2";
  if(Build.VERSION.SDK_INT>=26){NotificationChannel nc=new NotificationChannel(channel,"Bed due alarms",NotificationManager.IMPORTANCE_HIGH);nc.enableVibration(false);nc.setSound(null,null);nm.createNotificationChannel(nc);}
  Intent open=new Intent(c,MainActivity.class);
  PendingIntent p=PendingIntent.getActivity(c,202,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(c,channel).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Bed "+(bed>0?bed:"")+" coming out").setContentText("Next bed is due now").setContentIntent(p).setAutoCancel(true).setCategory(Notification.CATEGORY_ALARM).build();
  try{nm.notify(202,n);}catch(SecurityException ignored){}
  try { Intent play=new Intent(c,AlarmPlaybackService.class);play.putExtra("bed",bed);c.startForegroundService(play); } catch(Exception ignored){}
  AlarmScheduler.reschedule(c);
 }
}
