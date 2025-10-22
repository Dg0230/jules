
import { promises as fs } from 'fs';
import * as path from 'path';
import { Module } from './bundle-parser';

export class ModuleSplitter {
  public async splitAll(modules: Module[], outputDir: string): Promise<void> {
    for (const module of modules) {
      const filePath = path.join(outputDir, module.id);
      await fs.mkdir(path.dirname(filePath), { recursive: true });
      await fs.writeFile(filePath, module.code);
    }
  }

  public async generateIndex(modules: Module[], outputDir:string): Promise<void> {
    const indexPath = path.join(outputDir, 'index.json');
    const moduleIndex = modules.map(m => ({ id: m.id, path: m.id }));
    await fs.writeFile(indexPath, JSON.stringify(moduleIndex, null, 2));
  }
}
