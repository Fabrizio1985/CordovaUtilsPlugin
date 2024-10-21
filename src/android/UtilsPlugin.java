package android;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

public class UtilsPlugin extends CordovaPlugin {
	
	public static final int READ_EXTERNAL_STORAGE = 112;


	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

		Log.i("EXECUTE ACTION ===========", action);

		try {
			if (action.equals("installUpdate")) {
				return this.installUpdate(args, callbackContext);

			} else if (action.equals("createFile")) {
				this.createFile(args, callbackContext);

			} else if (action.equals("readFile")) {
				this.readFile(args, callbackContext);

			} else if (action.equals("removeFile")) {
				this.removeFile(args, callbackContext);

			} else if (action.equals("writeFile")) {
				this.writeFile(args, callbackContext);

			} else if (action.equals("checkFileExist")) {
				this.checkFileExist(args, callbackContext);

			} else if (action.equals("createFolder")) {
				this.createFolder(args, callbackContext);

			} else if (action.equals("readFolder")) {
				this.readFolder(args, callbackContext);

			} else if (action.equals("getTempPath")) {
				this.getTempPath(callbackContext);

			} else if (action.equals("executeCommand")) {
				this.executeCommand(args, callbackContext);

			} else if (action.equals("getUserDataFolder")) {
				this.getUserDataFolder(callbackContext);

			} else if (action.equals("uploadGoogle")) {
				this.uploadGoogle(args, callbackContext);
				
			} else if (action.equals("keepAwake")) {
				this.keepAwake(args, callbackContext);
				
			} else if (action.equals("resolveUri")) {
				this.resolveUri(args, callbackContext);
				
			} else if(action.equals("sdkVersion")){
				this.sdkVersion(callbackContext);
				
			} else if (action.equals("exit")) {
				
				Activity activity = this.cordova.getActivity();
				activity.finish();
	            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
	        }
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			callbackContext.error(e.getMessage());
		}

		return false;
	}

	private void resolveUri(JSONArray args, CallbackContext callbackContext) throws Exception {
		final String uri = args.getString(0);
		Context context = cordova.getActivity().getApplicationContext();
		
		callbackContext.success(UriUtils.getPathFromUri(context, uri));
	}

	private void keepAwake(JSONArray args, final CallbackContext callbackContext) {
		
		cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
            	cordova.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            	callbackContext.success();
            }
		});
	}

	private void uploadGoogle(JSONArray args, CallbackContext callbackContext) throws Exception {

		final String path = args.getString(0);

		File file = new File(path);

		Uri uploadUri = Uri.fromFile(file);

		if (Build.VERSION.SDK_INT >= 24) {
			Context context = cordova.getActivity().getApplicationContext();
			uploadUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
		}

		Intent uploadIntent = ShareCompat.IntentBuilder.from(cordova.getActivity())
				.setText("Share Document")
				.setType("application/txt")
				.setStream(uploadUri)
				.getIntent()
				.setPackage("com.google.android.apps.docs");

		cordova.getActivity().startActivity(uploadIntent);
		
		callbackContext.success();
	}

	private void executeCommand(final JSONArray args, final CallbackContext callbackContext) throws Exception {

		final JSONArray commands = args.getJSONArray(0);
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < commands.length(); i++) {
			list.add(commands.getString(i));
		}

		sudo(list.toArray(new String[commands.length()]));

		callbackContext.success();
	}

	private void getTempPath(final CallbackContext callbackContext) {

		callbackContext.success(cordova.getActivity().getExternalCacheDir().getAbsolutePath());
	}

	private void getUserDataFolder(final CallbackContext callbackContext) {

		callbackContext.success(cordova.getActivity().getExternalFilesDir(null).getAbsolutePath());
	}

	private void createFolder(final JSONArray args, final CallbackContext callbackContext) throws Exception {

		final String path = args.getString(0);

		Files.createDirectory(Paths.get(path));

		callbackContext.success();
	}

	private void checkFileExist(final JSONArray args, final CallbackContext callbackContext) throws Exception {

		final String path = args.getString(0);

		callbackContext.success(String.valueOf(Files.exists(Paths.get(path))));
	}

	private void readFolder(final JSONArray args, final CallbackContext callbackContext) throws Exception {

		final String path = args.getString(0);

		JSONArray results = new JSONArray();

		Iterator<File> files = FileUtils.iterateFiles(new File(path), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

		while (files.hasNext()) {
			File next = files.next();

			JSONObject json = new JSONObject();
			json.put("name", next.getName());

			results.put(json);
		}

		callbackContext.success(results);
	}

	private void createFile(final JSONArray args, final CallbackContext callbackContext) throws Exception {

		final String path = args.getString(0);
		final String data = args.getString(1);

		File file = new File(path);

		FileUtils.writeStringToFile(file, data, StandardCharsets.UTF_8, false);

		callbackContext.success(file.getAbsolutePath());
	}

	private void writeFile(final JSONArray args, final CallbackContext callbackContext) throws Exception {

		final String path = args.getString(0);
		final String data = args.getString(1);

		FileUtils.writeStringToFile(new File(path), data, StandardCharsets.UTF_8, true);

		callbackContext.success();
	}

	private void removeFile(final JSONArray args, final CallbackContext callbackContext) throws Exception {
		final String name = args.getString(0);

		File file = new File(name);

		if (file.exists()) {
			FileUtils.deleteQuietly(file);
		}

		callbackContext.success();
	}

	private void readFile(final JSONArray args, final CallbackContext callbackContext) throws Exception {

		final String name = args.getString(0);

		PluginResult result = new PluginResult(PluginResult.Status.OK, FileUtils.readFileToByteArray(new File(name)));
		
		callbackContext.sendPluginResult(result);
		//callbackContext.success();
	}
	
	private void sdkVersion(final CallbackContext callbackContext) {
		callbackContext.success( android.os.Build.VERSION.SDK_INT );
	}

	private boolean installUpdate(final JSONArray args, final CallbackContext callbackContext) {

		final String MYME_TYPE_APK = "application/vnd.android.package-archive";

		cordova.getThreadPool().execute(new Runnable() {

			public void run() {

				File file = null;
				
				try {
					String dataBade64 = args.getString(0);

					byte[] decode = Base64.decode(dataBade64, 0);

					file = new File(cordova.getActivity().getExternalCacheDir(), "update.apk");

					if (file.exists()) {
						FileUtils.deleteQuietly(file);
					}

					FileUtils.writeByteArrayToFile(file, decode);

					Intent intent = new Intent(Intent.ACTION_VIEW);
					// without this flag android returned a intent error!
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

					Uri apkUri = Uri.fromFile(file);

					if (Build.VERSION.SDK_INT >= 24) {
						Context context = cordova.getActivity().getApplicationContext();
						apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
					}

					intent.setDataAndType(apkUri, MYME_TYPE_APK);

					cordova.getActivity().getApplicationContext().startActivity(intent);

					callbackContext.success();

				} catch (Exception e) {
					e.printStackTrace();
					callbackContext.error(e.getMessage());
					
				} finally {
					
					if (null != file && file.exists()) {
						//FileUtils.deleteQuietly(file);
					}
				} 
			}
		});
		return true;
	}

	private void sudo(String... strings) throws IOException, InterruptedException {

		DataOutputStream outputStream = null;

		Process su = Runtime.getRuntime().exec("su");
		outputStream = new DataOutputStream(su.getOutputStream());

		for (String s : strings) {

			outputStream.writeBytes(s + "\n");
			outputStream.flush();
		}

		outputStream.writeBytes("exit\n");
		outputStream.flush();

		su.waitFor();
		outputStream.close();
	}

}
