
import { ModuleSplitter } from '../src/module-splitter';
import { Module } from '../src/bundle-parser';
import { promises as fs } from 'fs';
import * as path from 'path';

describe('ModuleSplitter', () => {
  const outputDir = './test-output';
  const modules: Module[] = [
    { id: './src/index.js', code: 'console.log("hello")', dependencies: [] },
    { id: './src/a.js', code: 'console.log("a")', dependencies: [] },
  ];

  beforeEach(async () => {
    await fs.mkdir(outputDir, { recursive: true });
  });

  afterEach(async () => {
    await fs.rm(outputDir, { recursive: true, force: true });
  });

  it('should split all modules into separate files', async () => {
    const splitter = new ModuleSplitter();
    await splitter.splitAll(modules, outputDir);

    const indexContent = await fs.readFile(path.join(outputDir, 'src/index.js'), 'utf8');
    expect(indexContent).toBe(modules[0].code);

    const aContent = await fs.readFile(path.join(outputDir, 'src/a.js'), 'utf8');
    expect(aContent).toBe(modules[1].code);
  });

  it('should generate an index file', async () => {
    const splitter = new ModuleSplitter();
    await splitter.generateIndex(modules, outputDir);

    const indexPath = path.join(outputDir, 'index.json');
    const indexContent = await fs.readFile(indexPath, 'utf8');
    const indexJson = JSON.parse(indexContent);

    expect(indexJson).toEqual([
      { id: './src/index.js', path: './src/index.js' },
      { id: './src/a.js', path: './src/a.js' },
    ]);
  });
});
