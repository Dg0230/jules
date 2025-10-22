
import { detectBundler, extractWebpackModules } from '../src/bundle-parser';

describe('extractWebpackModules', () => {
    it('should extract modules from a webpack bundle', () => {
      const bundleContent = `
        (function(modules) {
          var installedModules = {};
          function __webpack_require__(moduleId) {
            if (installedModules[moduleId]) {
              return installedModules[moduleId].exports;
            }
            var module = installedModules[moduleId] = {
              i: moduleId,
              l: false,
              exports: {}
            };
            modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
            module.l = true;
            return module.exports;
          }
        })({
          "./src/index.js": (function(module, exports, __webpack_require__) {
            const a = __webpack_require__("./src/a.js");
            console.log('hello world');
          }),
          "./src/a.js": (function(module, exports) {
            module.exports = 'a';
          })
        });
      `;

      const modules = extractWebpackModules(bundleContent);

      expect(modules).toHaveLength(2);
      expect(modules[0].id).toBe('./src/index.js');
      expect(modules[0].dependencies).toEqual(['./src/a.js']);
      expect(modules[1].id).toBe('./src/a.js');
      expect(modules[1].dependencies).toEqual([]);
    });
  });

describe('detectBundler', () => {
  it('should detect webpack', () => {
    const content = 'function __webpack_require__(moduleId) { ... }';
    expect(detectBundler(content)).toBe('webpack');
  });

  it('should detect parcel', () => {
    const content = 'parcelRequire = (function (modules, cache, entry, globalName) { ... })';
    expect(detectBundler(content)).toBe('parcel');
  });

  it('should detect vite', () => {
    const content = "import { a } from './a';";
    expect(detectBundler(content)).toBe('vite');
  });

  it('should detect rollup', () => {
    const content = '(function () { ... })();';
    expect(detectBundler(content)).toBe('rollup');
  });

  it('should return unknown for other formats', () => {
    const content = 'console.log("hello world");';
    expect(detectBundler(content)).toBe('unknown');
  });
});
