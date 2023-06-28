package com.github.bluecatlee.redundance;

import com.alibaba.fastjson.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

// 这个丑陋的json工具类有空干掉吧
public class JSONUtil {

    public JSONUtil() {
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }


    public static <T> String getJSONStrFromBean(T bean) {
        return JSONObject.fromObject(bean).toString();
    }

    public static String getJSONStrFromMap(Map map) {
        return JSONObject.fromObject(map).toString();
    }

    public static <T> String getJSONStrFromList(List list) {
        return JSONArray.fromObject(list).toString();
    }

//    public static JSONObject getJSONObjectFromStr(String jsonStr) {
//        JSONObject jObject = null;
//
//        try {
//            jsonStr = jsonStr.trim();
//            jsonStr = StringUtil.safeReplace(jsonStr, ":null", ":\"\"");
//            jObject = (JSONObject) JSONSerializer.toJSON(jsonStr);
//        } catch (Exception var3) {
//            ;
//        }
//
//        return jObject;
//    }
//
//    public static com.alibaba.fastjson.JSONObject getJSONObjectFromFastjson(String jsonStr) {
//        com.alibaba.fastjson.JSONObject jObject = null;
//
//        try {
//            jsonStr = jsonStr.trim();
//            jsonStr = StringUtil.safeReplace(jsonStr, ":null", ":\"\"");
//            jObject = JSON.parseObject(jsonStr);
//        } catch (Exception var3) {
//            ;
//        }
//
//        return jObject;
//    }

//    public static Map<String, String> json2Map(String jsonStr) {
//        return json2Map(getJSONObjectFromStr(jsonStr));
//    }
//
//    public static Map<String, String> json2Map(JSONObject jObject) {
//        HashMap map = new HashMap();
//
//        try {
//            JSONObject json = jObject;
//            Iterator var4 = jObject.keySet().iterator();
//
//            while(var4.hasNext()) {
//                Object k = var4.next();
//                Object v = json.get(k);
//                map.put(k.toString(), v.toString());
//            }
//        } catch (Exception var6) {
//            ;
//        }
//
//        return map;
//    }

    public static Map<String, Object> json2Map(String jsonStr, Map<String, Object> map) {
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        map = JSONObject.fromObject(jsonObject);
        Iterator var4 = map.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry)var4.next();
            json2mapEach(entry, map);
        }

        return map;
    }

    public static Map<String, Object> json2mapEach(Map.Entry<String, Object> entry, Map<String, Object> map) {
        if (entry.getValue() instanceof Map) {
            JSONObject jsonObject = JSONObject.fromObject(entry.getValue());
            if (jsonObject.isNullObject()) {
                map.put((String)entry.getKey(), "");
            } else {
                Map<String, Object> mapEach = JSONObject.fromObject(jsonObject);
                Iterator var5 = ((Map)mapEach).entrySet().iterator();

                while(var5.hasNext()) {
                    Map.Entry<String, Object> entryEach = (Map.Entry)var5.next();
                    mapEach = json2mapEach(entryEach, (Map)mapEach);
                    map.put((String)entry.getKey(), mapEach);
                }
            }
        } else if (entry.getValue() instanceof List) {
            JSONArray jsonArray = JSONArray.fromObject(entry.getValue());
            List<Map<String, Object>> mapEachList = new ArrayList();

            for(int i = 0; i < jsonArray.size(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> mapEach = JSONObject.fromObject(jsonObject);

                Map.Entry entryEach;
                for(Iterator var8 = ((Map)mapEach).entrySet().iterator(); var8.hasNext(); mapEach = json2mapEach(entryEach, (Map)mapEach)) {
                    entryEach = (Map.Entry)var8.next();
                }

                mapEachList.add(mapEach);
            }

            map.put((String)entry.getKey(), mapEachList);
        }

        return map;
    }

    public static JSONArray getJSONArrayFromStr(String jsonStr) {
        JSONArray jsonArray = null;

        try {
            jsonArray = (JSONArray)JSONSerializer.toJSON(jsonStr);
        } catch (Exception var3) {
            ;
        }

        return jsonArray;
    }



    public static String object2json(Object obj) {
        StringBuilder json = new StringBuilder();
        if (obj == null) {
            json.append("\"\"");
        } else if (!(obj instanceof String) && !(obj instanceof Integer) && !(obj instanceof Float) && !(obj instanceof Boolean) && !(obj instanceof Short) && !(obj instanceof Double) && !(obj instanceof Long) && !(obj instanceof BigDecimal) && !(obj instanceof BigInteger) && !(obj instanceof Byte)) {
            if (obj instanceof Date) {
                json.append("\"").append(date2json((Date)obj)).append("\"");
            } else if (obj instanceof Object[]) {
                json.append(array2json((Object[])obj));
            } else if (obj instanceof List) {
                json.append(list2json((List)obj));
            } else if (obj instanceof Map) {
                json.append(map2json((Map)obj));
            } else if (obj instanceof Set) {
                json.append(set2json((Set)obj));
            } else {
                json.append(bean2json(obj));
            }
        } else {
            json.append("\"").append(string2json(obj.toString())).append("\"");
        }

        return json.toString();
    }

    public static String bean2json(Object bean) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        PropertyDescriptor[] props = null;

        try {
            props = Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors();
        } catch (IntrospectionException var7) {
            ;
        }

        if (props != null) {
            for(int i = 0; i < props.length; ++i) {
                try {
                    String name = object2json(props[i].getName());
                    String value = object2json(props[i].getReadMethod().invoke(bean));
                    json.append(name);
                    json.append(":");
                    json.append(value);
                    json.append(",");
                } catch (Exception var6) {
                    ;
                }
            }

            json.setCharAt(json.length() - 1, '}');
        } else {
            json.append("}");
        }

        return json.toString();
    }

    public static String list2json(List<?> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (list != null && list.size() > 0) {
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
                Object obj = var3.next();
                json.append(object2json(obj));
                json.append(",");
            }

            json.setCharAt(json.length() - 1, ']');
        } else {
            json.append("]");
        }

        return json.toString();
    }

    public static String array2json(Object[] array) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (array != null && array.length > 0) {
            Object[] var5 = array;
            int var4 = array.length;

            for(int var3 = 0; var3 < var4; ++var3) {
                Object obj = var5[var3];
                json.append(object2json(obj));
                json.append(",");
            }

            json.setCharAt(json.length() - 1, ']');
        } else {
            json.append("]");
        }

        return json.toString();
    }

    public static String map2json(Map<?, ?> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        if (map != null && map.size() > 0) {
            Iterator var3 = map.keySet().iterator();

            while(var3.hasNext()) {
                Object key = var3.next();
                json.append(object2json(key));
                json.append(":");
                json.append(object2json(map.get(key)));
                json.append(",");
            }

            json.setCharAt(json.length() - 1, '}');
        } else {
            json.append("}");
        }

        return json.toString();
    }

    public static String set2json(Set<?> set) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (set != null && set.size() > 0) {
            Iterator var3 = set.iterator();

            while(var3.hasNext()) {
                Object obj = var3.next();
                json.append(object2json(obj));
                json.append(",");
            }

            json.setCharAt(json.length() - 1, ']');
        } else {
            json.append("]");
        }

        return json.toString();
    }

    public static String date2json(Date d) {
        String strReturn = "";

        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            strReturn = dt.format(d);
        } catch (Exception var3) {
            ;
        }

        return strReturn;
    }

    public static String string2json(String s) {
        if (s == null) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < s.length(); ++i) {
                char ch = s.charAt(i);
                switch(ch) {
                    case '\b':
                        sb.append("");
                        continue;
                    case '\t':
                        sb.append("");
                        continue;
                    case '\n':
                        sb.append("");
                        continue;
                    case '\f':
                        sb.append("");
                        continue;
                    case '\r':
                        sb.append("");
                        continue;
                    case '"':
                        sb.append("\\\"");
                        continue;
                    case '/':
                        sb.append("\\/");
                        continue;
                    case '\\':
                        sb.append("\\\\");
                        continue;
                }

                if (ch >= 0 && ch <= 31) {
                    String ss = Integer.toHexString(ch);
                    sb.append("");

                    for(int k = 0; k < 4 - ss.length(); ++k) {
                        sb.append('0');
                    }

                    sb.append(ss.toUpperCase());
                } else {
                    sb.append(ch);
                }
            }

            return sb.toString();
        }
    }

    public static String xml2json(String xml) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        return xmlSerializer.read(xml).toString();
    }

}
