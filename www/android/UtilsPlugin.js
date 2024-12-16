module.exports = {

	exit: () => {
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", "exit", []));
	},

	android: {
		resolveUri: (uri) => {
			return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", "resolveUri", [uri]));
		},
		sdkVersion: () => {
			return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'sdkVersion', []));
		},
		uploadGoogle: (path) => {
			return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'uploadGoogle', [path]));
		},
		keepAwake: () => {
			return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'keepAwake', []));
		},
		passkeyAssertion: (options) => {
			return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'passkeyAssertion', [options]));
		},
		createPasskey: (options) => {
			return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'createPasskey', [options]));
		}
	},

	executeCommand: (commands) => {
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", "executeCommand", [commands]));
	},

	createFile: (name, data) => {
		const nameTemp = typeof name === 'string' ? name : name.android;
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", "createFile", [nameTemp, data]));
	},

	readFile: (name) => {
		const nameTemp = typeof name === 'string' ? name : name.android;
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", "readFile", [nameTemp]));
	},

	selectFile: (properties) => {

	},

	removeFile: (path) => {
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'removeFile', [path]));
	},

	writeFile: (path, data) => {
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'writeFile', [path, data]));
	},

	localStorage: {

		getItem: (key) => {
			return Promise.resolve(window.localStorage.getItem(key));
		},

		setItem: (key, data) => {
			Promise.resolve(window.localStorage.setItem(key, data));
		},

		removeItem: (key) => {
			Promise.resolve(window.localStorage.removeItem(key));
		}
	},

	installUpdate: (blob) => {

		return new Promise((resolve, reject) => {
			const reader = new FileReader();
			reader.readAsDataURL(blob);
			reader.onloadend = () => resolve(reader.result.replace('data:application/vnd.android.package-archive;base64,', ''));
			reader.onerror = () => reject(reader.error);
		}).then(dataBase64 => {
			return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", "installUpdate", [dataBase64]));
		});
	},

	getTempPath: () => {
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'getTempPath', []));
	},

	getPlatform: () => {
		return 'android';
	},

	getAppVersion: () => {
		return cordova.getAppVersion.getVersionNumber();
	},

	getUserDataFolder: () => {
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'getUserDataFolder', []));
	},

	pathJoin: (...path) => {
		return path.join('/');
	},

	checkFileExist: (path) => {
		return new Promise((resolve, reject) => cordova.exec((value) => {
			resolve(value === 'true')
		}, reject, "UtilsPlugin", 'checkFileExist', [path]));
	},

	createFolder: (path) => {
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'createFolder', [path]));
	},

	readFolder: (path) => {
		return new Promise((resolve, reject) => cordova.exec(resolve, reject, "UtilsPlugin", 'readFolder', [path]));
	},

	createWriteStream: (path) => {
		return null;
	}
}