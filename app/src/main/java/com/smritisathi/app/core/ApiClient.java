package com.smritisathi.app.core;

import android.os.Handler;import android.os.Looper;
import com.google.gson.*;import java.io.*;import java.net.*;import java.nio.charset.StandardCharsets;import java.util.*;import java.util.concurrent.*;

public class ApiClient {
 public static final String BASE_URL="http://10.0.2.2:5000/api"; // Android Emulator -> host machine
 public interface Callback { void onResult(boolean ok, JsonObject data, String error); }
 private static final ExecutorService EXEC=Executors.newCachedThreadPool(); private static final Handler MAIN=new Handler(Looper.getMainLooper());
 public static void request(String method,String path,String token,JsonElement body,Callback cb){ EXEC.execute(()->{ try{ URL u=new URL(BASE_URL+path); HttpURLConnection c=(HttpURLConnection)u.openConnection(); c.setRequestMethod(method); c.setConnectTimeout(8000);c.setReadTimeout(12000);c.setRequestProperty("Accept","application/json"); if(token!=null&&!token.isEmpty())c.setRequestProperty("Authorization","Bearer "+token); if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
 int code=c.getResponseCode(); InputStream is=code>=200&&code<400?c.getInputStream():c.getErrorStream(); String s=read(is); JsonObject o; try{o=JsonParser.parseString(s).getAsJsonObject();}catch(Exception e){o=new JsonObject();o.addProperty("message",s);} final JsonObject resObj=o; boolean ok=code>=200&&code<300; MAIN.post(()->cb.onResult(ok,resObj,ok?null:resObj.has("message")?resObj.get("message").getAsString():"HTTP "+code)); c.disconnect(); }catch(Exception e){MAIN.post(()->cb.onResult(false,new JsonObject(),e.getMessage()));}}); }
 private static String read(InputStream is)throws Exception{if(is==null)return "{}";BufferedReader r=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();String x;while((x=r.readLine())!=null)b.append(x);return b.toString();}
 public static JsonObject obj(Object... kv){JsonObject o=new JsonObject();for(int i=0;i<kv.length;i+=2)o.addProperty(String.valueOf(kv[i]),String.valueOf(kv[i+1]));return o;}
}
