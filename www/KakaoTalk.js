import exec from 'cordova/exec';

let KakaoTalk = {
	login: (successCallback, errorCallback) => {
		exec(successCallback, errorCallback, "KakaoTalk", "login", []);
    },
	logout: (successCallback, errorCallback) => {
		exec(successCallback, errorCallback, 'KakaoTalk', 'logout', []);
	},
	requestMe: (successCallback, errorCallback) => {
		exec(successCallback, errorCallback, 'KakaoTalk', 'requestMe', []);
	},
	share: (options, successCallback, errorCallback) => {

		for(let options_key in options){
			if(typeof options[options_key] === 'object'){
				for(let key in options[options_key]){
					options[options_key][key] = options[options_key][key] || '';
				};
			}else{
				options[options_key] = options[options_key] || '';
			}
		};
	    exec(successCallback, errorCallback, "KakaoTalk", "share", [options]);
	}
};

module.exports = KakaoTalk;
