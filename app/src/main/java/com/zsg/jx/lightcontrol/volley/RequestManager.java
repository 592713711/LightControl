package com.zsg.jx.lightcontrol.volley;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.util.Config;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * @author Doots
 */
public class RequestManager {


    public static RequestQueue mRequestQueue = Volley.newRequestQueue(MyApplication.getInstance());

    private RequestManager() {
        // no instances
    }

    /**
     * @param url
     * @param tag
     * @param listener
     */
    public static void get(String url, Object tag, RequestListener listener) {
        get(url, tag, null, listener);
    }

    /**
     * @param url
     * @param tag
     * @param params
     * @param listener
     */
    public static void get(String url, Object tag, RequestParams params, RequestListener listener) {
        ByteArrayRequest request = new ByteArrayRequest(Method.GET, url, null, responseListener(listener), responseError(listener));
        addRequest(request, tag);
    }

    /**
     * @param url
     * @param tag
     * @param listener
     */
    public static void post(String url, Object tag, RequestListener listener) {
        post(url, tag, null, listener);
    }

    /**
     * @param url
     * @param tag
     * @param params
     * @param listener
     */
    public static void post(String url, Object tag, RequestParams params, RequestListener listener) {
         ByteArrayRequest request = new ByteArrayRequest(Method.POST, url, params, responseListener(listener), responseError(listener)){
             @Override
             public Map<String, String> getHeaders() throws AuthFailureError {
                 HashMap<String, String> headers = new HashMap<String, String>();
                 headers.put("Authorization", "Bearer " + MyApplication.getInstance().mToken);
                 return headers;
             }
         };
        addRequest(request,tag);
    }

    public static void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
    }

    public static void cancelAll(Object tag) {
        mRequestQueue.cancelAll(tag);
    }


    /**
     * 成功消息监听
     *
     * @param l
     * @return
     */
    protected static Response.Listener<byte[]> responseListener(final RequestListener l) {
        return new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] arg0) {
                String data = null;
                try {
                    data = new String(arg0, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                l.requestSuccess(data);
            }
        };
    }

    /**
     * 返回错误监听
     *
     * @param l
     * @return
     */
    protected static Response.ErrorListener responseError(final RequestListener l) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                l.requestError(e);
            }
        };
    }


    /**
     * 提交表单并上传文件到网站
     *
     * @param url
     *            提交的接口
     * @param param
     *            参数 <键，值>
     */
    public static String postForm(String url, Map<String, String> param) {
        try {
//			url = "http://localhost:4657" + "/api/SaveNeed";
            HttpPost post = new HttpPost(url);
            HttpClient client = new DefaultHttpClient();
            String BOUNDARY = "*****"; // 边界标识
            MultipartEntity entity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY, null);
            if (param != null && !param.isEmpty()) {
                entity.addPart(Config.IMAGE, new FileBody(new File(param.get(Config.IMAGE))));
                entity.addPart(Config.USER_ID, new StringBody(
                        param.get(Config.USER_ID), Charset.forName("UTF-8")));
            }
            post.setEntity(entity);

            HttpResponse response;

            response = client.execute(post);

            int stateCode = response.getStatusLine().getStatusCode();
            StringBuffer sb = new StringBuffer();
            if (stateCode == HttpStatus.SC_OK) {
                HttpEntity result = response.getEntity();
                if (result != null) {
                    InputStream is = result.getContent();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    String tempLine;
                    while ((tempLine = br.readLine()) != null) {
                        sb.append(tempLine);
                    }
                }
            }
            post.abort();

            return sb.toString();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
