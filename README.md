Cordova Plugin KakaoTalk
========================
![Platform](https://img.shields.io/badge/platform-android%20%7C%20ios-lightgrey)
![](https://img.shields.io/badge/kakao-v2.x-orange)

이 Cordova 플러그인은 taejaehan의 Cordova-Kakaotalk-Plugin를 수정하였습니다.

- https://github.com/taejaehan/Cordova-Kakaotalk-Plugin

해당 플러그인을 사용하기에 앞서 카카오 개발자 홈페이지에서 카카오 앱을 등록하여 KAKAO_APP_KEY를 발급 받아야합니다.

- https://developers.kakao.com

Cordova Install Note:
========================

플러그인 추가

cordova plugin add cordova-plugin-jminjun-kakaotalk --variable KAKAO_APP_KEY=YOUR_KAKAO_APP_KEY

[Android]
* 카카오 개발자 센터에서 키 해시를 등록해야 합니다 
* (https://developers.kakao.com/docs/android#getting-started-launch-sample-app)

[iOS]
* 아래의 코드를 appDelegate에 추가 해야합니다.

```
#import <KakaoOpenSDK/KakaoOpenSDK.h>

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url
                                       sourceApplication:(NSString *)sourceApplication
                                              annotation:(id)annotation {
    ...
    if ([KOSession isKakaoAccountLoginCallback:url]){return [KOSession handleOpenURL:url];}
    ...
}

- (void)applicationDidBecomeActive:(UIApplication *)application{[KOSession handleDidBecomeActive];}
```

* Ohter Linker Flags 추가

open platforms/ios/*.xcodeproj
        Build Settings > Linking > Other Linker Flags > add '-all_load'

How to use the plugin
========================

### Usage

This plugin adds an object to the window. Right now, you can login, logout and share.

##### Login

Login using the `.login` method:
```
KakaoTalk.login(
    function (result) {
      console.log('Successful login!');
      console.log(result);
    },
    function (message) {
      console.log('Error logging in');
      console.log(message);
    }
);
```

The login reponse object is defined as:
```
{
  id: '<KakaoTalk User Id>',
  nickname: '<KakaoTalk User Nickname>',
  profile_image: '<KakaoTalk User ProfileImage>'
}
```

##### Logout

Logout using the `.logout` method:
```
KakaoTalk.logout(
	function() {
		console.log('Successful logout!');
	}, function() {
		console.log('Error logging out');
	}
);
```

##### Share

Share using the `.share` method:
```
KakaoTalk.share({
    text : 'Share Message',
    image : {
      src : 'https://developers.kakao.com/assets/img/link_sample.jpg',
      width : 138, 
      height : 90,
    },
    weblink :{
      url : 'YOUR-WEBSITE URL',
      text : 'web사이트로 이동'
    },
    applink :{
      url : 'YOUR-WEBSITE URL', 
      text : '앱으로 이동',
    },
    params :{
      paramKey1 : 'paramVal',
      param1 : 'param1Value',
      cardId : '27',
      keyStr : 'hey'
    }
  },
  function (success) {
    console.log('kakao share success');
  },
  function (error) {
    console.log('kakao share error');
  });
```

- you can use text, image, weblink and applink(params) separately or together
- Min image width(80)xheight(80)
- weblink(text-link), applink(button-link)
- if you use applink, you can set any params(optional)

- https://developers.kakao.com/docs/ios#카카오링크
- https://developers.kakao.com/docs/android#카카오링크
