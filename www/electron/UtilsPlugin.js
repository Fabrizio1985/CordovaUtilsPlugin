module.exports = {

	executeCommand: (commands) => {
		const childProcess = window.require('child_process');

		return Promise.all(commands.map(c => {
			return new Promise((resolve, reject) => {
				childProcess.exec(c, (error, data, _getter) => null != error ? reject(error) : resolve(data));
			});
		}));
	},

	writeFile: (name, data) => {
		const fsPromises = window.require('fs').promises;
		return fsPromises.writeFile(name, data).then(() => nameTemp);
	},

	createFolder: (name) => {
		const fsPromises = window.require('fs').promises;
		return fsPromises.mkdir(name);
	},

	readFile: (name) => {
		const fsPromises = window.require('fs').promises;
		const nameTemp = typeof name === 'string' ? name : name.electron;

		return fsPromises.readFile(nameTemp, 'utf-8');
	},

	selectFile: (properties) => {
		const remote = window.require('@electron/remote');

		const obj = Object.assign({}, properties);
		obj.defaultPath = null != obj.defaultPath ? obj.defaultPath : remote.app.getPath('home');

		return remote.dialog.showOpenDialog(obj);
	},

	readFolder: (name) => {
		const fsPromises = window.require('fs').promises;
		return fsPromises.readdir(name, { withFileTypes: true })
	},

	localStorage: {
		// Return Promise
		getItem: (key) => {
			const remote = window.require('@electron/remote');
			return remote.getCurrentWebContents().executeJavaScript("window.localStorage.getItem('" + key + "')");
		},

		setItem: (key, data) => {
			const remote = window.require('@electron/remote');
			return remote.getCurrentWebContents().executeJavaScript("window.localStorage.setItem('" + key + "' ," + data + ")");
		},

		removeItem: (key) => {
			const remote = window.require('@electron/remote');
			return remote.getCurrentWebContents().executeJavaScript("window.localStorage.removeItem('" + key + "')");
		}
	},

	installUpdate: async (blob) => {
		const platform = window.require('os').platform();

		const arrayBuffer = await blob.arrayBuffer();
		const buffer = Buffer.from(arrayBuffer);
		const tempPath = await this.cordova.utilsPlugin.getTempPath();

		const name = tempPath + '/installer.deb';
		const fileSaved = await this.cordova.utilsPlugin.createFile(name, buffer);

		if (platform == 'linux') {
			await this.cordova.utilsPlugin.executeCommand(['qapt-deb-installer ' + fileSaved + ' > /dev/null 2>&1']);
		}
	},

	getTempPath: () => {
		return Promise.resolve(window.require('os').tmpdir());
	},

	getPlatform: () => {
		return window.require('os').platform();
	},

	getAppVersion: () => {
		const remote = window.require('@electron/remote');
		return Promise.resolve(remote.app.getVersion());
	},

	checkFileExist: (path) => {
		const fs = window.require('fs');
		try {
			fs.accessSync(path, fs.constants.F_OK)
			return Promise.resolve(true);	
		} catch(err) {
			return Promise.resolve(false);
		}
	},

	unlinkFile: (path) => {
		const fsPromises = window.require('fs').promises;
		return fsPromises.unlink(path);
	},

	symlinkFile: (from, to, type) => {
		const fsPromises = window.require('fs').promises;
		return fsPromises.symlink(from, to, type);
	},

	removeFolder: (path) => {
		const fs = window.require('fs');
		const pathCmd = window.require("path");

		const removeDir = function(innerPath) {
			if (fs.existsSync(innerPath)) {
				const files = fs.readdirSync(innerPath)

				if (files.length > 0) {

					files.forEach(function(filename) {
						var f = pathCmd.join(innerPath, filename);

						if (fs.statSync(f).isDirectory()) {
							removeDir(f)
						} else {
							fs.unlinkSync(f)
						}
					})
					fs.rmdirSync(innerPath)
				} else {
					fs.rmdirSync(innerPath)
				}
			} else {
				console.log("Directory path not found.")
			}
			return null;
		};

		return Promise.resolve(removeDir(path));
	},

	removeFile: (file) => {
		const fsPromises = window.require('fs').promises;
		return fsPromises.unlink(file);
	},

	pathJoin: (...path) => {
		const pathCmd = window.require('path');
		return pathCmd.join(...path);
	},
	
	getUserDataFolder: () => {
		const electron = window.require('electron');
		const remote = window.require('@electron/remote');
		
		return Promise.resolve((electron.app || remote.app).getPath('userData'));
	},
	
	createWriteStream: (file) => {
		const fs = window.require('fs');
		
		return fs.createWriteStream(file, { flags: 'a' });
	}
}
