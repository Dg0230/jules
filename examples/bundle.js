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