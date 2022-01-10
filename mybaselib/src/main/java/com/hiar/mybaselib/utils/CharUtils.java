package com.hiar.mybaselib.utils;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public class CharUtils {
    private static final String TAG = "CharUtils";

    /**
     * 根据Unicode编码判断中文汉字和中文符号
     *
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }


    /**
     * 合法的英文判断：
     *
     * @param c
     * @return
     */
    public static boolean isEnglishByREG(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * 根据正则表达式判断部分CJK字符（CJK统一汉字）
     *
     * @param str
     * @return
     */
    public static boolean isChineseByREG(String str) {
        if (TextUtils.isEmpty(str)) return false;
        Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
        return pattern.matcher(str.trim()).find();
    }

    /**
     * 过滤掉非中英文
     *
     * @param str
     * @return
     */
    public static String getLetterAndChinese(String str) {
        Log.e(TAG, "要判断的字符串是:" + str);
        char[] ch = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : ch)
            if (isChineseByREG("" + c) ||
                    isEnglishByREG(
                            c))
                sb.append("").append(c);
        Log.e(TAG, "最终显示的名字是:" + sb.toString());

        return sb.toString();
    }

    /**
     * 判断是否是中英文
     *
     * @param str
     * @return
     */
    public static boolean isLetterAndChinese(String str) {
        Log.e(TAG, "要判断的字符串是:" + str);
        char[] ch = str.toCharArray();
        for (char c : ch)
            if (isChineseByREG("" + c) ||
                    isEnglishByREG(
                            c))
                return true;


        return false;
    }

    /**
     * 全角转半角
     *
     * @param str
     * @return
     * @author mjorcen
     * @email mjorcen@gmail.com
     * @dateTime Sep 27, 2014 2:51:50 PM
     * @version 1
     */
    @Deprecated
    public static final String toSingleByte(String str) {
        StringBuilder outStrBuf = new StringBuilder();

        String Tstr;
        byte[] b;
        for (int i = 0; i < str.length(); i++) {
            Tstr = str.substring(i, i + 1);
            // 全角空格转换成半角空格
            if (Tstr.equals("　")) {
                outStrBuf.append(" ");
                continue;
            }
            try {
                b = Tstr.getBytes("unicode");
                // 得到 unicode 字节数据
                if (b[2] == -1) {
                    // 表示全角
                    b[3] = (byte) (b[3] + 32);
                    b[2] = 0;
                    outStrBuf.append(new String(b, "unicode"));
                } else {
                    outStrBuf.append(Tstr);
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } // end for.
        return outStrBuf.toString();

    }

    /**
     * 半角转全角
     *
     * @param str
     * @return
     * @author mjorcen
     * @email mjorcen@gmail.com
     * @dateTime Sep 27, 2014 2:52:06 PM
     * @version 1
     */
    @Deprecated
    public static final String toDoubleByte(String str) {
        StringBuilder outStrBuf = new StringBuilder();
        String Tstr;
        byte[] b;
        for (int i = 0; i < str.length(); i++) {
            Tstr = str.substring(i, i + 1);
            if (Tstr.equals(" ")) {
                // 半角空格
                outStrBuf.append(Tstr);
                continue;
            }
            try {
                b = Tstr.getBytes("unicode");
                if (b[2] == 0) {
                    // 半角
                    b[3] = (byte) (b[3] - 32);
                    b[2] = -1;
                    outStrBuf.append(new String(b, "unicode"));
                } else {
                    outStrBuf.append(Tstr);
                }
                return outStrBuf.toString();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return outStrBuf.toString();
    }

    /**
     * 半角转全角
     *
     * @param str
     * @return
     * @author mjorcen
     * @email mjorcen@gmail.com
     * @dateTime Sep 27, 2014 2:52:31 PM
     * @version 1
     */
    public static String ToSBC(String str) {
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);

            }
        }
        return new String(c);
    }

    /**
     * 全角转半角
     *
     * @param str
     * @return
     * @author mjorcen
     * @email mjorcen@gmail.com
     * @dateTime Sep 27, 2014 2:52:50 PM
     * @version 1
     */
    public static String ToDBC(String str) {
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);

            }
        }
        String returnString = new String(c);
        return returnString;
    }

    public static void main(String[] args) {
        long l = SystemClock.elapsedRealtime();
        for (int i = 0; i < 100000; i++) {
            String str = "８１４乡道阿斯蒂芬１２３／．１２，４１２看２家１快２看了就２；看了２叫看来＋看来家１２考虑就２３；了３接口２了２会２，．水电费苦辣时间的２　　１２５１２３１２３１２１２０９－０２１～！＠＃＄％＾＆＊（）＿";
            String result = ToDBC(str);
            ToSBC(result);
        }
        System.out.println(SystemClock.elapsedRealtime() - l);
    }

}
