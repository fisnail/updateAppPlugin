cordova.define("com.fisnail.updateapp.UpdateAppPlugin.UpdateApp", function(require, exports, module) { 
var exec = require("cordova/exec");

var updateApp = { 
	update: function(jsonObj, s, f) {
	console.log(jsonObj);
	 cordova.exec(s, f, "UpdateAppPlugin", "update", [jsonObj]);
	}
};
module.exports = updateApp;

});
