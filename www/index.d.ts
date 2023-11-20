export interface CordovaUtils {
	exit: () => Promise<void>;
	executeCommand: (commands: any) => any;
	createFile: (path: string, data: string) => Promise<string>;
	readFile: (path: string | { android?: string, electron?: string }) => Promise<ArrayBuffer>;
	selectFile: (properties: any) => Promise<{ filePaths: string[], canceled: boolean }>;
	removeFile: (path: string) => Promise<void>;
	writeFile: (path: string, data: string) => Promise<void>;
	installUpdate: (blob: Blob) => Promise<void>;
	getTempPath: () => Promise<string>;
	getPlatform: () => string;
	getAppVersion: () => Promise<string>;
	getUserDataFolder: () => Promise<string>;
	pathJoin: (...path: string[]) => string;
	checkFileExist: (path: string) => Promise<true>;
	createFolder: (path: string) => Promise<string>;
	readFolder: (path: string) => Promise<Array<{ name: string }>>;
	createWriteStream(path: string): any;
	keepAwake: () => Promise<void>;

	android: {
		resolveUri: (uri: string) => Promise<string>;
		sdkVersion: () => Promise<number>;
		uploadGoogle: (path: string) => Promise<void>;
		keepAwake: () => Promise<void>;
	},

	localStorage: {
		getItem: (key: string) => Promise<string>;
		setItem: (key: string, data: string) => Promise<void>;
		removeItem: (key: string) => Promise<void>;
	}
}