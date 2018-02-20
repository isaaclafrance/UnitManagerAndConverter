package com.isaacapps.unitconverterapp.dao.api;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.net.http.AndroidHttpClient;

public class ApiRequester {
	public static String DEFAULT_USER_AGENT = "(Android Device)sometestemail@gmail.com";
	
	public static HttpResponse getHttpResponse(String requestURI, String userAgent) throws IOException{
		return AndroidHttpClient
                .newInstance(userAgent)
                .execute(new HttpGet(requestURI));
	}
	public static HttpResponse getHttpResponse(String requestURI) throws IOException{
		return getHttpResponse(requestURI, DEFAULT_USER_AGENT);
	}
	
	public static String getReponseString(HttpResponse httpResponse) throws IOException{
		return EntityUtils.toString(httpResponse.getEntity());
	}
}
