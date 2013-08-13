package org.codeandmagic.findmefood.service;

import android.text.TextUtils;
import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.Consts.Http.ENCODING;

/**
 * Created by evelyne24.
 */
public abstract class HttpRequest {

    protected static final String PARAM_API_KEY = "key";

    protected String url;
    private Map<String, Boolean> paramNames;
    private Map<String, String> paramValues;
    private Map<String, String> defaultValues;
    private List<NameValuePair> params;

    public HttpRequest() {
        paramNames = new HashMap<String, Boolean>();
        paramValues = new HashMap<String, String>();
        defaultValues = new HashMap<String, String>();
        params = new ArrayList<NameValuePair>();

        // Setup the API key as a required param for all requests
        addParamName(PARAM_API_KEY, true);
    }

    protected HttpRequest addParamName(String paramName, boolean optional, String... defaultValue) {
        paramNames.put(paramName, optional);
        if ((defaultValue != null) && (defaultValue.length > 0)) {
            defaultValues.put(paramName, defaultValue[0]);
        }
        return this;
    }

    protected HttpRequest addParam(String paramName, String paramValue) {
        paramValues.put(paramName, paramValue);
        return this;
    }

    /**
     * @return The base URL for this request, without query params.
     */
    public abstract String getBaseUrl();

    public String buildUrl() throws MalformedRequestException {
        if (TextUtils.isEmpty(getBaseUrl())) {
            throw new MalformedRequestException("The base url cannot be empty/null.");
        }

        for (String paramName : paramNames.keySet()) {
            Boolean isRequired = paramNames.get(paramName);
            String paramValue = paramValues.get(paramName);
            String defaultValue = defaultValues.get(paramName);

            if ((isRequired != null && isRequired) && paramValue == null && defaultValue == null) {
                throw new MalformedRequestException(MessageFormat.format("Missing required parameter '{0}'.", paramName));
            }

            if (paramValue != null) {
                params.add(new BasicNameValuePair(paramName, paramValue));
            } else if (defaultValue != null) {
                params.add(new BasicNameValuePair(paramName, defaultValue));
            } else {
                Log.w(APP_TAG, MessageFormat.format("Ignoring parameter '{0}'.", paramName));
            }
        }

        return url = (getBaseUrl() + URLEncodedUtils.format(params, ENCODING));
    }

    /**
     * @return The full URL for this request, including query params.
     */
    public String getUrl() {
        return url;
    }
}
