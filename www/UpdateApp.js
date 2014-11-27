
var exec = require("cordova/exec");

var updateApp = { 
	update: function(jsonObj, s, f) {
	console.log(jsonObj);
	 cordova.exec(s, f, "UpdateAppPlugin", "update", [jsonObj]);
	}
};
module.exports = updateApp;


