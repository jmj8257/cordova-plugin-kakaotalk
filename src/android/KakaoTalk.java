package com.jmj.plugin.kakao;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.common.util.KakaoCustomTabsClient;
import com.kakao.sdk.share.ShareClient;
import com.kakao.sdk.share.WebSharerClient;
import com.kakao.sdk.template.model.Button;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.DefaultTemplate;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.ItemContent;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.ListTemplate;
import com.kakao.sdk.template.model.Social;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.common.util.Utility;
import com.celler.cellerclientapp.R;
import com.utils.Generics;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;

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
				this.share(options, callbackContext, cordova.getContext());
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	private void share(JSONArray options, final CallbackContext callbackContext, Context context) {
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
			String userToken = "";

			//content 데이터 확인
			if (parameters.has("params")) {
				JSONObject paramsObj = parameters.getJSONObject("params");
				if(paramsObj.has("title") && paramsObj.has("desc") && paramsObj.has("imageUrl") && paramsObj.has("link")) {
					paramsTitle = paramsObj.getString("title");
					paramsDesc = paramsObj.getString("desc");
					paramsImageUrl = paramsObj.getString("imageUrl");
					paramsLink = paramsObj.getString("link");
					if(paramsObj.getString("recommendUserToken") != null)
						userToken = paramsObj.getString("recommendUserToken");
				}
			}

			//web url 확인
			if (parameters.has("weblink")) {
				JSONObject weblinkObj = parameters.getJSONObject("weblink");
				if(weblinkObj.has("text") && weblinkObj.has("url")) {
					webLinkText = weblinkObj.getString("text");
					webLinkUrl = weblinkObj.getString("url");
					if(!userToken.equals(""))
						webLinkUrl += "?recommendUserToken=" + userToken;
				}
			}

			//app url 확인
			if (parameters.has("applink")) {
				JSONObject applinkObj = parameters.getJSONObject("applink");
				if(applinkObj.has("text") && applinkObj.has("url")) {
					appLinkText = applinkObj.getString("text");
					appLinkUrl = applinkObj.getString("url");
				}
			}

			//item content 데이터 확인
			if (parameters.has("itemContent")) {
				JSONObject itemObj = parameters.getJSONObject("itemContent");
			}

			List<Button> buttons = Generics.newArrayList();

			//안드로이드 카카오톡 앱 버튼 url
			Map<String, String> androidExecutionParams = new HashMap<>();
			androidExecutionParams.put("android_execution_params", appLinkUrl);

			//ios 카카오톡 앱 버튼 url
			Map<String, String> iosExecutionParams = new HashMap<>();

			//content url link 객체
			Link contentLink = new Link(paramsLink, paramsLink , null, null);
			//web url link 객체
			Link webLink = new Link(webLinkUrl, webLinkUrl, null, null);
			//app url link 객체
			Link appLink = new Link(null, null, androidExecutionParams, iosExecutionParams);

			buttons.add(new Button(webLinkText, webLink));
			buttons.add(new Button(appLinkText, appLink));
			//feed type template 생성
			FeedTemplate feedTemplate = new FeedTemplate(
					new Content(
							paramsTitle,
							paramsImageUrl,
							contentLink,
							paramsDesc),
					null,
					null,
					buttons);

			// 카카오톡 설치여부 확인
			if (ShareClient.getInstance().isKakaoTalkSharingAvailable(context)) {
				// 카카오톡으로 카카오톡 공유 가능
				ShareClient.getInstance().shareDefault(context, feedTemplate, null, (sharingResult, error) -> {
					if (error != null) {
						Log.e(LOG_TAG, "카카오톡 공유 실패", error);
						callbackContext.error(error.getMessage());
					}
					else if (sharingResult != null) {
						Log.d(LOG_TAG, "카카오톡 공유 성공 : " + sharingResult.getIntent());
						cordova.getContext().startActivity(sharingResult.getIntent());

						// 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
						Log.w(LOG_TAG, "Warning Msg:" + sharingResult.getWarningMsg());
						Log.w(LOG_TAG, "Argument Msg:" + sharingResult.getArgumentMsg());
					}

					callbackContext.success("카카오톡 공유 성공");
					return Unit.INSTANCE;
				});
			}
			else {
				// 카카오톡 미설치: 웹 공유 사용 권장
				// 웹 공유 예시 코드
				Uri sharerUrl = WebSharerClient.getInstance().makeDefaultUrl(feedTemplate);

				// CustomTabs으로 웹 브라우저 열기

				// 1. CustomTabsServiceConnection 지원 브라우저 열기
				// ex) Chrome, 삼성 인터넷, FireFox, 웨일 등
				try {
					KakaoCustomTabsClient.INSTANCE.openWithDefault(context, sharerUrl);
				} catch(UnsupportedOperationException e) {
					// CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
				}

				// 2. CustomTabsServiceConnection 미지원 브라우저 열기
				// ex) 다음, 네이버 등
				try {
					KakaoCustomTabsClient.INSTANCE.open(context, sharerUrl);
				} catch (ActivityNotFoundException e) {
					Log.w(LOG_TAG,e.getMessage());
					// 디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Log in
	 */
	private void login(CallbackContext callbackContext) {
		cordova.getActivity().runOnUiThread(() -> {
			// 카카오톡 설치 여부 확인
			if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(cordova.getContext())) {
				// 카카오톡이 설치되어 있으면 카톡으로 로그인 확인 요청
				UserApiClient.getInstance().loginWithKakaoTalk(cordova.getContext(), (token, loginError) -> {
					if (loginError != null) {
						// 로그인 실패
						callbackContext.error(loginError.getMessage());
					} else {
						// 로그인 성공
						Log.i(LOG_TAG,"토큰 성공 : "+ token);
						requestMe(callbackContext, token.getAccessToken());
						// 사용자 정보 받기
//                        callbackContext.success(token.getAccessToken());
					}
					return null;
				});
			}
			else {
				// 카카오톡이 설치되어 있지 않은 경우 앱 내장 웹뷰 방식으로 카카오계정 확인 요청
				UserApiClient.getInstance().loginWithKakaoAccount(cordova.getContext(), (token, loginError) -> {
					if (loginError != null) {
						// 로그인 실패
						callbackContext.error(loginError.getMessage());
					} else {
						// 로그인 성공
						Log.i(LOG_TAG, "토큰 성공 : "+ token);
						requestMe(callbackContext, token.getAccessToken());
						// 사용자 정보 요청
//						callbackContext.success(token.getAccessToken());
					}
					return null;
				});
			}
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
				Log.e(LOG_TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", throwable);
				callbackContext.error("로그아웃 실패. SDK에서 토큰 삭제됨");
			}
			else {
				Log.i(LOG_TAG, "로그아웃 성공. SDK에서 토큰 삭제됨");
				callbackContext.success("로그아웃 성공. SDK에서 토큰 삭제됨");
			}

			return null;
		}));
	}


	/**
	 *
	 * @param callbackContext
	 */
	private void requestMe(CallbackContext callbackContext, String token) {
		// 사용자 정보 요청 (기본)
		UserApiClient.getInstance().me((user, error) -> {
			if (error != null) {
				Log.e(LOG_TAG, "사용자 정보 요청 실패", error);
			}
			else if (user != null) {
				Log.i(LOG_TAG, "사용자 정보 요청 성공" +
						"\n회원번호 :" + user.getId() +
						"\n이메일 : " + user.getKakaoAccount().getEmail() +
						"\n휴대폰번호 : " + user.getKakaoAccount().getPhoneNumber() +
						"\n닉네임 : " + user.getKakaoAccount().getName() +
						"\n프로필사진 : " + user.getKakaoAccount().getProfile().getThumbnailImageUrl());
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
	 * 액세스 토큰을 가져온다
	 */
	private void getAccessToken(CallbackContext callbackContext) {
		String keyHash = Utility.INSTANCE.getKeyHash(cordova.getContext());
		System.out.println("keyHash : " + keyHash);

		// 토큰 정보 보기
		UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, throwable) -> {
			if (throwable != null) {
				Log.e("TAG", "토큰 정보 보기 실패" + throwable.getMessage());
				callbackContext.success(throwable.getMessage());
			}
			else if (accessTokenInfo != null) {
				Log.i("", "토큰 정보 보기 성공" +
						"\n토큰: " + accessTokenInfo.getAppId() +
						"\n회원번호:"+ accessTokenInfo.getId() +
						"\n만료시간: {"+ accessTokenInfo.getExpiresIn() +"} 초");

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
