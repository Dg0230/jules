#!/usr/bin/env node
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const commander_1 = require("commander");
const file_reader_1 = require("./file-reader");
const bundle_parser_1 = require("./bundle-parser");
const module_splitter_1 = require("./module-splitter");
const fs_1 = require("fs");
const program = new commander_1.Command();
program
    .version('0.0.1')
    .command('analyze <filePath>')
    .description('Analyze a JavaScript bundle file')
    .option('-o, --output <dir>', 'Output directory for split modules', './output')
    .option('--split', 'Split modules into separate files')
    .action(async (filePath, options) => {
    console.log(`Analyzing ${filePath}...`);
    const fileReader = new file_reader_1.FileReader();
    const content = await fs_1.promises.readFile(filePath, 'utf-8');
    const bundlerType = (0, bundle_parser_1.detectBundler)(content);
    console.log(`Detected bundler: ${bundlerType}`);
    if (bundlerType === 'webpack') {
        const modules = (0, bundle_parser_1.extractWebpackModules)(content);
        console.log(`Found ${modules.length} modules.`);
        if (options.split) {
            const outputDir = options.output;
            console.log(`Splitting modules into ${outputDir}...`);
            const splitter = new module_splitter_1.ModuleSplitter();
            await splitter.splitAll(modules, outputDir);
            await splitter.generateIndex(modules, outputDir);
            console.log('Modules split successfully.');
        }
    }
    else {
        console.log('Only webpack bundles are currently supported for module extraction.');
    }
});
program.parse(process.argv);
