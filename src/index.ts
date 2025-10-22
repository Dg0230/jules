#!/usr/bin/env node

import { Command } from 'commander';
import { FileReader } from './file-reader';
import { detectBundler, extractWebpackModules } from './bundle-parser';
import { ModuleSplitter } from './module-splitter';
import { promises as fs } from 'fs';

const program = new Command();

program
  .version('0.0.1')
  .command('analyze <filePath>')
  .description('Analyze a JavaScript bundle file')
  .option('-o, --output <dir>', 'Output directory for split modules', './output')
  .option('--split', 'Split modules into separate files')
  .action(async (filePath, options) => {
    console.log(`Analyzing ${filePath}...`);

    const fileReader = new FileReader();
    const content = await fs.readFile(filePath, 'utf-8');

    const bundlerType = detectBundler(content);
    console.log(`Detected bundler: ${bundlerType}`);

    if (bundlerType === 'webpack') {
      const modules = extractWebpackModules(content);
      console.log(`Found ${modules.length} modules.`);

      if (options.split) {
        const outputDir = options.output;
        console.log(`Splitting modules into ${outputDir}...`);
        const splitter = new ModuleSplitter();
        await splitter.splitAll(modules, outputDir);
        await splitter.generateIndex(modules, outputDir);
        console.log('Modules split successfully.');
      }
    } else {
      console.log('Only webpack bundles are currently supported for module extraction.');
    }
  });

program.parse(process.argv);
