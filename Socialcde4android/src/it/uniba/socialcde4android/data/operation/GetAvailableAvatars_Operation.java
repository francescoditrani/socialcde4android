package it.uniba.socialcde4android.data.operation;

import it.uniba.socialcde4android.config.Config;
import it.uniba.socialcde4android.costants.Consts;
import it.uniba.socialcde4android.costants.Error_consts;
import it.uniba.socialcde4android.preferences.Preferences;
import it.uniba.socialcde4android.shared.library.WFeature;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import android.net.Uri;
import java.net.URL;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.google.gson.Gson;

public class GetAvailableAvatars_Operation implements Operation{

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {


		String username = request.getString(Preferences.USERNAME);
		String password = request.getString(Preferences.PASSWORD);
		String host = request.getString(Preferences.PROXYSERVER) + "/SocialTFSProxy.svc";
		int status = 0;


		URL[] urls = new URL[0];

		try {
			URL url = new URL(host + "/GetAvailableAvatars");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(Config.CONN_TIMEOUT_MS);
			conn.setReadTimeout(Config.READ_TIMEOUT_MS);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setAllowUserInteraction(false);
			conn.setRequestProperty("Content-Type", "application/json");

			// Create the form content
			OutputStream out = conn.getOutputStream();
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			writer.write("{ \"username\":\"" + username + "\", \"password\":\""
					+ password + "\"}");

			writer.close();
			out.close();
			status = conn.getResponseCode();

			if (status >= 200 && status <= 299) {
				InputStreamReader in = new InputStreamReader(
						conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				String output;
				String result = "";
				while ((output = br.readLine()) != null) {
					result += output;
				}
				br.close();
				Gson gson = new Gson();
				urls = new URL[countOccurrences(result, '{')];
				urls = gson.fromJson(result, URL[].class);
			}else{
				throw new ConnectionException	("Error retrieving available avatars", Error_consts.ERROR_RETRIEVING_AVATARS);		

			}

			conn.disconnect();
		} catch(java.net.SocketTimeoutException e) {
			throw new ConnectionException	("Error retrieving available avatars", Error_consts.ERROR_RETRIEVING_AVATARS * Error_consts.TIMEOUT_FACTOR);		
		}  catch (Exception e) {

			throw new ConnectionException	("Error retrieving available avatars", Error_consts.ERROR_RETRIEVING_AVATARS);		
		}

		Bundle bundle = new Bundle();

		String[] uri = new String[urls.length];

		for (int i=0 ; i<urls.length; i++){
			uri[i] =  urls[i].toString();
		}

		if (uri != null && uri.length>0){
			bundle.putStringArray(Consts.URI, uri);
			bundle.putBoolean(Consts.FOUND_AVATAR_IMAGES, true);
		}else{
			bundle.putBoolean(Consts.FOUND_AVATAR_IMAGES, false);
		}
		bundle.putInt(Consts.REQUEST_TYPE, Consts.REQUESTTYPE_GET_AVAILABLE_AVATARS);
		return bundle;
	}



	private static int countOccurrences(String haystack, char needle) {
		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle) {
				count++;
			}
		}

		if (count == 0) {
			count += 1;
		}

		return count;
	}
}
