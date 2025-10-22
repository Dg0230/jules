
import { Module } from './bundle-parser';

export interface SearchQuery {
  name?: string;
  minSize?: number;
  maxSize?: number;
  hasPattern?: RegExp;
}

export class ModuleRegistry {
  private modules: Map<string, Module> = new Map();

  public register(module: Module): void {
    this.modules.set(module.id, module);
  }

  public findById(id: string): Module | null {
    return this.modules.get(id) || null;
  }

  public findByName(name: string): Module[] {
    const result: Module[] = [];
    for (const module of this.modules.values()) {
      if (module.id.includes(name)) {
        result.push(module);
      }
    }
    return result;
  }

  public search(query: SearchQuery): Module[] {
    return this.filter(module => {
      if (query.name && !module.id.includes(query.name)) {
        return false;
      }
      if (query.minSize && module.code.length < query.minSize) {
        return false;
      }
      if (query.maxSize && module.code.length > query.maxSize) {
        return false;
      }
      if (query.hasPattern && !query.hasPattern.test(module.code)) {
        return false;
      }
      return true;
    });
  }

  public filter(predicate: (m: Module) => boolean): Module[] {
    const result: Module[] = [];
    for (const module of this.modules.values()) {
      if (predicate(module)) {
        result.push(module);
      }
    }
    return result;
  }

  public getAllModules(): Module[] {
    return Array.from(this.modules.values());
  }
}
