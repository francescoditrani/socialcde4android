package it.uniba.socialcde4android.data.operation;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.Bundle;

import it.uniba.socialcde4android.config.Config;
import it.uniba.socialcde4android.costants.Consts;
import it.uniba.socialcde4android.costants.Error_consts;
import it.uniba.socialcde4android.preferences.Preferences;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;


public class IsWebServiceRunning_Operation implements Operation {

	//private static final String TAG = RetrieveServices_Operation.class.getSimpleName();

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {
		String host = request.getString(Preferences.PROXYSERVER)+ "/SocialTFSProxy.svc";
		String output = "";
		Boolean online = false ;
		int status = 0;
		try {

			URL url = new URL(host + "/IsWebServiceRunning");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(Config.CONN_TIMEOUT_MS);
			conn.setReadTimeout(Config.READ_TIMEOUT_MS);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			status = conn.getResponseCode();

			if (status >= 200 && status <= 299) {
				InputStreamReader in = new InputStreamReader(
						conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				output = br.readLine();

			}else{
				throw new ConnectionException("Error connecting to the server",Error_consts.ERROR_WEBSERVICE_RUNNING);
			}
		}catch(java.net.SocketTimeoutException e) {
			throw new ConnectionException("Error connecting to the server",Error_consts.ERROR_WEBSERVICE_RUNNING * Error_consts.TIMEOUT_FACTOR);

		} catch (Exception e) {
			throw new ConnectionException("Error connecting to the server",Error_consts.ERROR_WEBSERVICE_RUNNING);

		}

		if (output.equals("true")){
			online = true;
		} else {

			online = false;
		}
		Bundle bundle = new Bundle();
		bundle.putBoolean(Consts.WEBSERVICE_AVAILABLE, online);
		bundle.putInt(Consts.REQUEST_TYPE, Consts.REQUESTTYPE_WEBSERVICEVAILABLE);

		return bundle;

	}

}
