package it.uniba.socialcde4android.data.operation;

import it.uniba.socialcde4android.config.Config;
import it.uniba.socialcde4android.costants.Consts;
import it.uniba.socialcde4android.costants.Error_consts;
import it.uniba.socialcde4android.preferences.Preferences;
import it.uniba.socialcde4android.shared.library.JsonDateDeserializer;
import it.uniba.socialcde4android.shared.library.WFeature;
import it.uniba.socialcde4android.shared.library.WPost;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetWposts_Operation implements Operation{

	@Override
	public Bundle execute(Context context, Request r)
			throws ConnectionException, DataException, CustomRequestException {

		String request = r.getString(Consts.REQUEST);
		String request_type_string = r.getString(Consts.REQUEST_TYPE_STRING);
		String host = r.getString(Preferences.PROXYSERVER) + "/SocialTFSProxy.svc";
		int type_request = r.getInt(Consts.REQUEST_TYPE);

		int status = 0;

		Boolean noMoreMessages = false;
		Boolean data_error = false;

		WPost[] wpost;
		wpost = new WPost[2];

		try {
			URL url = new URL(host + request_type_string);
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
			writer.write(request);

			writer.close();
			out.close();
			Log.i("statuuuuuuuuusprima",String.valueOf(status));

			status = conn.getResponseCode();
			Log.i("statuuuuuuuuus",String.valueOf(status));
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

				int count = 0;
				if (result.equals("[]")) {
					wpost = new WPost[0];
					noMoreMessages=true;
				} else {
					String haystack = result;
					char needle = '{';
					for (int i = 0; i < haystack.length(); i++) {
						if (haystack.charAt(i) == needle) 
							count++;	}
					if (count == 0) count += 1;

					wpost = new WPost[count/3];
					Gson gson = new GsonBuilder().registerTypeAdapter(
							Calendar.class, new JsonDateDeserializer()).create();
					wpost = gson.fromJson(result, WPost[].class);
				}

			} else if (status == 400){
				wpost = new WPost[0];
				noMoreMessages = true;
			}else{
			
				wpost = new WPost[0];
			}
			conn.disconnect();
		} catch(java.net.SocketTimeoutException e) {
			throw new ConnectionException	("Error retrieving new posts", Error_consts.ERROR_RETRIEVING_WPOSTS * Error_consts.TIMEOUT_FACTOR);		
		}  catch (Exception e) {
			throw new ConnectionException	("Error retrieving new posts", Error_consts.ERROR_RETRIEVING_WPOSTS);		
		}

		Bundle bundle = new Bundle();

		//		if (wpost != null && wpost.length>0){
		//			bundle.putParcelableArray(Consts.WPOSTS, wpost);
		//			bundle.putBoolean(Consts.FOUND_WFEATURES, true);
		//			bundle.putInt(Consts.SERVICE_ID, service_id);
		//		}else{
		//			bundle.putBoolean(Consts.FOUND_WFEATURES, false);
		//		}

		if (wpost != null && wpost.length>0){
			bundle.putParcelableArray(Consts.WPOSTS, wpost);
			bundle.putBoolean(Consts.FOUND_WPOSTS, true);
		}else{
			bundle.putBoolean(Consts.FOUND_WPOSTS, false);
		}
		bundle.putBoolean(Consts.NO_MORE_MESSAGES, noMoreMessages);
		bundle.putInt(Consts.TYPE_REQUEST, type_request);
		bundle.putInt(Consts.REQUEST_TYPE, Consts.REQUESTTYPE_GET_WPOSTS);
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
