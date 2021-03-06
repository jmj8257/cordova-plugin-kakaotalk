package com.jmj.plugin.kakao;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.link.LinkClient;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.ListTemplate;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.common.util.Utility;
import com.tqsoft.tqmobilemystore.R;
import com.utils.Generics;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class KakaoTalk extends CordovaPlugin {

	private static final String LOG_TAG = "KakaoTalk";
	private static volatile Activity currentActivity;

	/**
	 * Initialize cordova plugin kakaotalk
	 *
	 * @param cordova
	 * @param webView
	 */
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		Log.v(LOG_TAG, "kakao : initialize");
		super.initialize(cordova, webView);
		currentActivity = this.cordova.getActivity();
		KakaoSdk.init(this.cordova.getActivity(), this.cordova.getActivity().getResources().getString(R.string.kakao_app_key));
	}

	/**
	 * Execute plugin
	 *
	 * @param action
	 * @param options
	 * @param callbackContext
	 */
	public boolean execute(final String action, JSONArray options, final CallbackContext callbackContext) throws JSONException {
		Log.v(LOG_TAG, "kakao : execute " + action);
		cordova.setActivityResultCallback(this);

		if (action.equals("login")) {
			this.login(callbackContext);
			return true;
		}
		else if (action.equals("logout")) {
			this.logout(callbackContext);
			return true;
		}
		else if (action.equals("requestMe")) {
			this.requestMe(callbackContext, null);
			return true;
		}
		else if (action.equals("share")) {
			try {
				this.share(options, callbackContext);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	private void share(JSONArray options, final CallbackContext callbackContext) {
		try {
			final JSONObject parameters = options.getJSONObject(0);
			String webLinkText = "";
			String webLinkUrl = "";
			String appLinkText = "";
			String appLinkUrl = "";
			String paramsTitle = "";
			String paramsDesc= "";
			String paramsImageUrl = "";
			String paramsLink = "";

			if (parameters.has("text")) {
				// TODO:
			}

			if (parameters.has("params")) {
				JSONObject paramsObj = parameters.getJSONObject("params");
				if(paramsObj.has("title") && paramsObj.has("desc") && paramsObj.has("imageUrl") && paramsObj.has("link")) {
					paramsTitle = paramsObj.getString("title");
					paramsDesc = paramsObj.getString("desc");
					paramsImageUrl = paramsObj.getString("imageUrl");
					paramsLink = paramsObj.getString("link");
				}
			}

			if (parameters.has("weblink")) {
				JSONObject weblinkObj = parameters.getJSONObject("weblink");
				if(weblinkObj.has("text") && weblinkObj.has("url")) {
					webLinkText = weblinkObj.getString("text");
					webLinkUrl = weblinkObj.getString("url");
				}
			}

			if (parameters.has("applink")) {
				JSONObject applinkObj = parameters.getJSONObject("applink");
				if(applinkObj.has("text") && applinkObj.has("url")) {
					appLinkText = applinkObj.getString("text");
					appLinkUrl = applinkObj.getString("url");
				}
			}

			List<Content> contents = Generics.newArrayList();
			contents.add(new Content(
					paramsTitle,
					paramsImageUrl,
					new Link(webLinkUrl,appLinkUrl),
					paramsDesc));
			contents.add(new Content(
					paramsTitle,
					paramsImageUrl,
					new Link(webLinkUrl,appLinkUrl),
					paramsDesc));
			contents.add(new Content(
					paramsTitle,
					paramsImageUrl,
					new Link(webLinkUrl,appLinkUrl),
					paramsDesc));

			List<com.kakao.sdk.template.model.Button> listButtons = Generics.newArrayList();
			listButtons.add(new com.kakao.sdk.template.model.Button(webLinkText, new Link(webLinkUrl,appLinkUrl)));
			listButtons.add(new com.kakao.sdk.template.model.Button(appLinkText, new Link(webLinkUrl,appLinkUrl)));
			ListTemplate listTemplate = new ListTemplate(
					paramsTitle,
					new Link(webLinkUrl,appLinkUrl),
					contents,
					listButtons);

			List<com.kakao.sdk.template.model.Button> buttons = Generics.newArrayList();
			buttons.add(new com.kakao.sdk.template.model.Button(webLinkText, new Link(webLinkUrl,appLinkUrl)));
			buttons.add(new com.kakao.sdk.template.model.Button(appLinkText, new Link(webLinkUrl,appLinkUrl)));
			FeedTemplate feedTemplate = new FeedTemplate(
					new Content(
							paramsTitle,
							paramsImageUrl,
							new Link(webLinkUrl,appLinkUrl),
							paramsDesc),
					null/*new Social(286,45,845)*/);

			LinkClient.getInstance().defaultTemplate(this.cordova.getActivity(), feedTemplate, (linkResult, error) -> {
				if (error != null) {
					System.out.println(error);
				}
				else if (linkResult != null) {
					this.cordova.getActivity().startActivity(linkResult.getIntent());
				}
				return null;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Log in
	 */
	private void login(CallbackContext callbackContext) {
		cordova.getActivity().runOnUiThread(() -> {
			// ???????????? ?????? ?????? ??????
			if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(cordova.getContext())) {
				// ??????????????? ???????????? ????????? ???????????? ????????? ?????? ??????
				UserApiClient.getInstance().loginWithKakaoTalk(cordova.getContext(), (token, loginError) -> {
					if (loginError != null) {
						// ????????? ??????
						callbackContext.error(loginError.getMessage());
					} else {
						// ????????? ??????
						Log.i(LOG_TAG,"?????? ?????? : "+ token);
						requestMe(callbackContext, token.getAccessToken());
						// ????????? ?????? ??????
//                        callbackContext.success(token.getAccessToken());
					}
					return null;
				});
			}
			else {
				// ??????????????? ???????????? ?????? ?????? ?????? ??? ?????? ?????? ???????????? ??????????????? ?????? ??????
				UserApiClient.getInstance().loginWithKakaoAccount(cordova.getContext(), (token, loginError) -> {
					if (loginError != null) {
						// ????????? ??????
						callbackContext.error(loginError.getMessage());
					} else {
						// ????????? ??????
						Log.i(LOG_TAG, "?????? ?????? : "+ token);
						requestMe(callbackContext, token.getAccessToken());
						// ????????? ?????? ??????
//						callbackContext.success(token.getAccessToken());
					}
					return null;
				});
			}
		});
	}

	/**
	 *
	 * @param callbackContext
	 */
	private void requestMe(CallbackContext callbackContext, String token) {
		// ????????? ?????? ?????? (??????)
		UserApiClient.getInstance().me((user, error) -> {
			if (error != null) {
				Log.e(LOG_TAG, "????????? ?????? ?????? ??????", error);
			}
			else if (user != null) {
				Log.i(LOG_TAG, "????????? ?????? ?????? ??????" +
						"\n???????????? :" + user.getId() +
						"\n????????? : " + user.getKakaoAccount().getEmail() +
						"\n??????????????? : " + user.getKakaoAccount().getPhoneNumber() +
						"\n????????? : " + user.getKakaoAccount().getName() +
						"\n??????????????? : " + user.getKakaoAccount().getProfile().getThumbnailImageUrl());
				JSONObject userJson = new JSONObject();
				JSONObject resultJson = new JSONObject();
				try {
					userJson.put("id",user.getId());
					userJson.put("email",user.getKakaoAccount().getEmail());
					userJson.put("phoneNumber",user.getKakaoAccount().getPhoneNumber());
					userJson.put("name",user.getKakaoAccount().getName());
					userJson.put("thumbnailImageUrl",user.getKakaoAccount().getProfile().getThumbnailImageUrl());
					userJson.put("age",user.getKakaoAccount().getAgeRange());
					userJson.put("birthday", user.getKakaoAccount().getBirthday());
					userJson.put("birthyear", user.getKakaoAccount().getBirthyear());

					resultJson.put("user", userJson);
					if (!token.isEmpty())
						resultJson.put("token", token);

				} catch (JSONException e) {
					e.printStackTrace();
				}

				callbackContext.success(resultJson);
			}

			return null;
		});
	}

	/**
	 * Log out
	 *
	 * @param callbackContext
	 */
	private void logout(final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(() -> UserApiClient.getInstance().logout(throwable -> {
			if (throwable != null) {
				Log.e(LOG_TAG, "???????????? ??????. SDK?????? ?????? ?????????", throwable);
				callbackContext.error("???????????? ??????. SDK?????? ?????? ?????????");
			}
			else {
				Log.i(LOG_TAG, "???????????? ??????. SDK?????? ?????? ?????????");
				callbackContext.success("???????????? ??????. SDK?????? ?????? ?????????");
			}

			return null;
		}));
	}

	/**
	 * ????????? ????????? ????????????
	 */
	private void getAccessToken(CallbackContext callbackContext) {
		String keyHash = Utility.INSTANCE.getKeyHash(cordova.getContext());
		System.out.println("keyHash : " + keyHash);

		// ?????? ?????? ??????
		UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, throwable) -> {
			if (throwable != null) {
				Log.e("TAG", "?????? ?????? ?????? ??????" + throwable.getMessage());
				callbackContext.success(throwable.getMessage());
			}
			else if (accessTokenInfo != null) {
				Log.i("", "?????? ?????? ?????? ??????" +
						"\n??????: " + accessTokenInfo.getAppId() +
						"\n????????????:"+ accessTokenInfo.getId() +
						"\n????????????: {"+ accessTokenInfo.getExpiresIn() +"} ???");

				callbackContext.success(accessTokenInfo.getAppId());

			}
			return null;
		});
	}

	/**
	 * On activity result
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param intent
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		Log.v(LOG_TAG, "kakao : onActivityResult : " + requestCode + ", code: " + resultCode);
		super.onActivityResult(requestCode, resultCode, intent);
	}

	/**
	 * Return current activity
	 */
	public static Activity getCurrentActivity() {
		return currentActivity;
	}

	/**
	 * Set current activity
	 */
	public static void setCurrentActivity(Activity currentActivity) {
		currentActivity = currentActivity;
	}
}
