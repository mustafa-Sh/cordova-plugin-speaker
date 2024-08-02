var exec = require('cordova/exec');

exports.coolMethod = function (arg0, arg1, success, error) {
    exec(success, error, 'CordovaPluginSpeaker', 'coolMethod', [arg0, arg1]);
};
