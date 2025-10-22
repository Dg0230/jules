"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ModuleRegistry = void 0;
class ModuleRegistry {
    constructor() {
        this.modules = new Map();
    }
    register(module) {
        this.modules.set(module.id, module);
    }
    findById(id) {
        return this.modules.get(id) || null;
    }
    findByName(name) {
        const result = [];
        for (const module of this.modules.values()) {
            if (module.id.includes(name)) {
                result.push(module);
            }
        }
        return result;
    }
    search(query) {
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
    filter(predicate) {
        const result = [];
        for (const module of this.modules.values()) {
            if (predicate(module)) {
                result.push(module);
            }
        }
        return result;
    }
    getAllModules() {
        return Array.from(this.modules.values());
    }
}
exports.ModuleRegistry = ModuleRegistry;
