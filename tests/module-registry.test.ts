
import { ModuleRegistry } from '../src/module-registry';
import { Module } from '../src/bundle-parser';

describe('ModuleRegistry', () => {
  let registry: ModuleRegistry;
  const module1: Module = { id: 'module1', code: 'console.log("hello")', dependencies: [] };
  const module2: Module = { id: 'module2', code: 'console.log("a much longer world")', dependencies: ['module1'] };

  beforeEach(() => {
    registry = new ModuleRegistry();
    registry.register(module1);
    registry.register(module2);
  });

  it('should register and find a module by id', () => {
    expect(registry.findById('module1')).toEqual(module1);
    expect(registry.findById('module3')).toBeNull();
  });

  it('should find modules by name', () => {
    expect(registry.findByName('module')).toEqual([module1, module2]);
    expect(registry.findByName('1')).toEqual([module1]);
  });

  it('should get all modules', () => {
    expect(registry.getAllModules()).toEqual([module1, module2]);
  });

  it('should filter modules with a predicate', () => {
    const filtered = registry.filter(m => m.id === 'module1');
    expect(filtered).toEqual([module1]);
  });

  it('should search modules by name', () => {
    const result = registry.search({ name: 'module1' });
    expect(result).toEqual([module1]);
  });

  it('should search modules by minSize', () => {
    const result = registry.search({ minSize: 30 });
    expect(result).toEqual([module2]);
  });

  it('should search modules by maxSize', () => {
    const result = registry.search({ maxSize: 30 });
    expect(result).toEqual([module1]);
  });

  it('should search modules by pattern', () => {
    const result = registry.search({ hasPattern: /world/ });
    expect(result).toEqual([module2]);
  });
});
