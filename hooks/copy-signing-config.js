const fs = require('fs');
const path = require('path');

module.exports = function (context) {
	const projectRoot = context.opts.projectRoot;
	const pluginRoot = path.resolve(__dirname, '..');

	const files = [
		{
			source: path.join(pluginRoot, 'cert', 'MioCertificato'),
			target: path.join(projectRoot, 'MioCertificato')
		},
		{
			source: path.join(pluginRoot, 'build.json'),
			target: path.join(projectRoot, 'build.json')
		}
	];

	for (const file of files) {
		if (!fs.existsSync(file.source)) {
			throw new Error(`File non trovato nel plugin: ${file.source}`);
		}

		fs.copyFileSync(file.source, file.target);

		console.log(`[CordovaUtilsPlugin] Copiato: ${file.target}`);
	}
};