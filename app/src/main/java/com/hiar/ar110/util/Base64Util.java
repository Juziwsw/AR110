package com.hiar.ar110.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Base64Util {
    public static void gcBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle(); // 回收图片所占的内存
            bitmap = null;
            System.gc(); // 提醒系统及时回收
        }
    }

    /**
     *
     * @Title: bitmapToBase64
     * @Description: TODO(Bitmap 转换为字符串)
     * @param @param bitmap
     * @param @return    设定文件
     * @return String    返回类型
     * @throws
     */

    public static String bitmapToBase64(Bitmap bitmap) {

        // 要返回的字符串
        String reslut = null;

        ByteArrayOutputStream baos = null;

        try {

            if (bitmap != null) {

                baos = new ByteArrayOutputStream();
                /**
                 * 压缩只对保存有效果bitmap还是原来的大小
                 */
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);

                baos.flush();
                baos.close();
                // 转换为字节数组
                byte[] byteArray = baos.toByteArray();

                // 转换为字符串
                reslut = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return reslut;

    }

    /**
     *
     * @Title: base64ToBitmap
     * @Description: TODO(base64l转换为Bitmap)
     * @param @param base64String
     * @param @return    设定文件
     * @return Bitmap    返回类型
     * @throws
     */
    public static Bitmap base64ToBitmap(String base64String){
        if(base64String == null) {
            return null;
        }
        byte[] decode = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        return bitmap;
    }

    /**
     * 本地图片转换成base64字符串
     * @param imgFile	图片本地路径
     * @return
     *
     * @author ZHANGJL
     * @dateTime 2018-02-23 14:40:46
     */
    public static String imageToBase64ByLocal(String imgFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;

        // 读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    /**
     * 将文件内容转为Base64编码的字符串
     * @param file
     * @return
     */
    public static String base64(File file){
        FileInputStream stream=null;
        try{
            stream=new FileInputStream(file);

            byte[] bytes=new byte[(int)file.length()];
            int size=0,readed=0;

            while(-1!=(size=stream.read(bytes,readed,8192))){
                readed+=size;
            }
            return Base64.encodeToString(bytes,Base64.DEFAULT);
        }catch (Exception e){
            return null;
        }finally {
            if(null!=stream){
                try{
                    stream.close();
                }catch (Exception e){

                }
            }
        }
    }

    public static byte[] base64ToByte(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    public static String byteToBase64(byte[] basearray) {
        return Base64.encodeToString(basearray, Base64.DEFAULT);
    }

    public static String byteToBase64NoLine(byte[] basearray) {
        String base64 = Base64.encodeToString(basearray, Base64.DEFAULT);
        String temp = base64.replaceAll("[\\s*\t\n\r]", "");
        return temp;
    }
}