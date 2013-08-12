package org.codeandmagic.findmefood.service;

import android.text.TextUtils;
import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by evelyne24.
 */
public class RequestBuilder {

    private static final String TAG = RequestBuilder.class.getName();
    private static final String ENCODING = "UTF-8";
    private String url;
    private Map<String, Boolean> paramNames;
    private Map<String, String> paramValues;
    private Map<String, String> defaultValues;
    private List<NameValuePair> params;

    public RequestBuilder() {
        paramNames = new HashMap<String, Boolean>();
        paramValues = new HashMap<String, String>();
        defaultValues = new HashMap<String, String>();
        params = new ArrayList<NameValuePair>();
    }

    protected RequestBuilder addParamName(String paramName, boolean optional, String... defaultValue) {
        paramNames.put(paramName, optional);
        if ((defaultValue != null) && (defaultValue.length >= 1)) {
            defaultValues.put(paramName, defaultValue[0]);
        }
        return this;
    }

    protected RequestBuilder addParam(String paramName, String paramValue) {
        paramValues.put(paramName, paramValue);
        return this;
    }

    public String build() throws RequestMalformedException {
        for (String paramName : paramNames.keySet()) {
            Boolean isRequired = paramNames.get(paramName);
            String paramValue = paramValues.get(paramName);
            String defaultValue = paramValues.get(paramName);

            if (paramValue == null) {
                if (isRequired != null && isRequired) {
                    throw new RequestMalformedException("Missing required parameter '" + paramName + "'");
                }

                if (defaultValue != null) {
                    params.add(new BasicNameValuePair(paramName, defaultValue));
                } else {
                    Log.w(TAG, "Ignoring null parameter '" + paramName + "'");
                }

            } else {
                params.add(new BasicNameValuePair(paramName, paramValue));
            }
        }

        if (TextUtils.isEmpty(url)) {
            throw new RequestMalformedException("Url cannot be empty.");
        }
        return url + URLEncodedUtils.format(params, ENCODING);
    }
}
