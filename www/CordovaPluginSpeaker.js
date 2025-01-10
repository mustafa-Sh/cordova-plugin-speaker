var exec = require('cordova/exec');

exports.unifiedMethod = function (arg0, arg1, success, error) {
    if (cordova.platformId === 'android') {
        exec(success, error, 'CordovaPluginSpeaker', 'coolMethod', [arg0, arg1]);
    } else if (cordova.platformId === 'ios') {
        exec(success, error, 'CordovaPluginIos', 'coolMethodd', [arg0]);
    } else {
        error('Unsupported platform');
    }
};