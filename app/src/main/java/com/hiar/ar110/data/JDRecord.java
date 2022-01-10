package com.hiar.ar110.data;

import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.hiar.ar110.data.cop.CopTaskRecord;
import com.hiar.ar110.data.cop.CopTaskResult;
import com.hiar.ar110.util.Util;
import com.hiar.mybaselib.utils.AR110Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class JDRecord {
    private  HashMap<String, CopTaskRecord> mJQMap = new HashMap<>();
    private static JDRecord mInstance = null;
    public static final String mDataRootAddr = "/sdcard/.jwt/com.hiar.ar110/";
    private ArrayList<CopTaskRecord> getJQList() {
        ArrayList<CopTaskRecord> jqlist = new ArrayList<>();
        Iterator it = mJQMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            jqlist.add(mJQMap.get(key));
        }

        return jqlist;
    }

    private JDRecord() {
        File privateFolder = new File(mDataRootAddr);
        if(!privateFolder.exists()) {
            privateFolder.mkdirs();
        }
    }

    public static JDRecord getInstance() {
        if(null == mInstance) {
            mInstance = new JDRecord();
        }

        return mInstance;
    }



    public ArrayList<CopTaskRecord> updateJQlist() {
        mJQMap.clear();
        File dataRoot = new File(mDataRootAddr);
        if(!dataRoot.exists()) {
            dataRoot.mkdirs();
            return null;
        }

        File[] fileArray = dataRoot.listFiles();
        if(fileArray == null) {
            return null;
        }

        int len = fileArray.length;
        for(int i=0; i<len; i++) {
            if(fileArray[i].isFile()) {
                continue;
            }

            if(fileArray[i].getName().equals("..")) {
                continue;
            }

            if(fileArray[i].getName().equals(".")) {
                continue;
            }

            String jdFilePaht = fileArray[i].getAbsolutePath()+"/jd.txt";
            CopTaskRecord content = getCopTaskRecordFromFile(jdFilePaht);
            if(content != null) {
                mJQMap.put(content.cjdbh, content);
            }
        }

        return getJQList();
    }

    public  void saveCopTaskRecord(CopTaskRecord content) {
        if(mJQMap.get(content.cjdbh) == null) {
            mJQMap.put(content.cjdbh, content);
        }

        saveJDToText(content);
    }

    public static void saveJDToText(CopTaskRecord data) {
        if(data == null) {
            return;
        }

        String jdFolder = mDataRootAddr + data.cjdbh;
        File jdfolderFile = new File(jdFolder);
        if(!jdfolderFile.exists()) {
            jdfolderFile.mkdirs();
        }

        String  jqPath = mDataRootAddr + data.cjdbh + "/" + "jd.txt";
        Gson gson = new Gson();
        String wstr = gson.toJson(data);
        File jdFile = new File(jqPath);
        if(jdFile.exists()) {
            return;
        }

        try {
            jdFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(jqPath);
            if(writer != null) {
                writer.write(wstr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static CopTaskResult getTaskResult(String path) {
        if(null == path) {
            return null;
        }

        File jdFile = new File(path);
        if(!jdFile.exists()) {
            return null;
        }

        FileReader reader = null;
        BufferedReader bf = null;
        try {
            reader = new FileReader(jdFile);
            StringBuffer buffer = new StringBuffer();
            String s = null;
            bf = new BufferedReader(reader);
            while((s = bf.readLine())!=null){//使用readLine方法，一次读一行
                buffer.append(s.trim());
            }

            s = buffer.toString();
            Gson gson = new Gson();
            try{
                CopTaskResult content = gson.fromJson(s, CopTaskResult.class);
                return content;
            } catch(JsonSyntaxException ejson) {

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static CopTaskRecord getCopTaskRecordFromFile(String path) {
        if(null == path) {
            return null;
        }

        File jdFile = new File(path);
        if(!jdFile.exists()) {
            return null;
        }

        FileReader reader = null;
        BufferedReader bf = null;
        try {
            reader = new FileReader(jdFile);
            StringBuffer buffer = new StringBuffer();
            String s = null;
            bf = new BufferedReader(reader);
            while((s = bf.readLine())!=null){//使用readLine方法，一次读一行
                buffer.append(s.trim());
            }

            s = buffer.toString();
            Gson gson = new Gson();
            try{
                CopTaskRecord content = gson.fromJson(s, CopTaskRecord.class);
                return content;
            } catch(JsonSyntaxException ejson) {
                ejson.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static CopTaskResult getTaskResultFromRaw(int id) {
        InputStream inputStream = Utils.getApp().getResources().openRawResource(id);
        String str = getString(inputStream);
        if(str == null) {
            AR110Log.i("JDRecord","read from raw is null");
        }

        AR110Log.i("JDRecord",str);
        Gson gson = new Gson();
        try{
            CopTaskResult content = gson.fromJson(str, CopTaskResult.class);
            return content;
        } catch(JsonSyntaxException e) {
            e.printStackTrace();
            AR110Log.i("JDRecord", e.toString());
        }
        return null;
    }

    public static CopTaskResult parseTaskResultFromRaw(int id) {
        InputStream inputStream = Utils.getApp().getResources().openRawResource(id);
        String str = getString(inputStream);
        if(str == null) {
            AR110Log.i("JDRecord","read from raw is null");
        }

        AR110Log.i("JDRecord",str);

        try {
            JSONObject jobj = new JSONObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }

    private static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
