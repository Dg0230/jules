"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.extractWebpackModules = extractWebpackModules;
exports.detectBundler = detectBundler;
const parser = __importStar(require("@babel/parser"));
const traverse_1 = __importDefault(require("@babel/traverse"));
const t = __importStar(require("@babel/types"));
const generator_1 = require("@babel/generator");
function extractWebpackModules(content) {
    const ast = parser.parse(content, {
        sourceType: 'script',
        allowReturnOutsideFunction: true,
    });
    const modules = [];
    (0, traverse_1.default)(ast, {
        ObjectExpression(path) {
            // Heuristic: check if this object looks like a webpack module map
            const looksLikeWebpackModules = path.node.properties.every(prop => t.isObjectProperty(prop) &&
                t.isStringLiteral(prop.key) &&
                t.isFunctionExpression(prop.value));
            if (!looksLikeWebpackModules || path.node.properties.length === 0) {
                return;
            }
            path.get('properties').forEach(propPath => {
                if (propPath.isObjectProperty()) {
                    const propNode = propPath.node;
                    const keyNode = propNode.key;
                    const valueNode = propNode.value;
                    if (t.isStringLiteral(keyNode) && t.isFunctionExpression(valueNode)) {
                        const moduleId = keyNode.value;
                        const functionExpressionPath = propPath.get('value');
                        if (!functionExpressionPath.isFunctionExpression())
                            return;
                        const functionBodyNode = valueNode.body;
                        const code = (0, generator_1.generate)(functionBodyNode).code;
                        const dependencies = [];
                        // Traverse the function's path to find dependencies
                        functionExpressionPath.traverse({
                            CallExpression(innerPath) {
                                if (t.isIdentifier(innerPath.node.callee) &&
                                    innerPath.node.callee.name === '__webpack_require__' &&
                                    innerPath.node.arguments.length > 0 &&
                                    t.isStringLiteral(innerPath.node.arguments[0])) {
                                    dependencies.push(innerPath.node.arguments[0].value);
                                }
                            },
                        });
                        modules.push({
                            id: moduleId,
                            code: code.slice(2, -2).trim(),
                            dependencies,
                        });
                    }
                }
            });
            path.skip();
        },
    });
    return modules;
}
function detectBundler(content) {
    if (content.includes('__webpack_require__') || content.includes('__webpack_modules__')) {
        return 'webpack';
    }
    if (content.includes('parcelRequire')) {
        return 'parcel';
    }
    // Vite and Rollup are harder to distinguish. For now, we'll use a simple check.
    // A more robust solution would involve AST parsing.
    if (content.match(/import\s*\{[^}]*\}\s*from\s*['"][^'"]+['"];/)) {
        return 'vite'; // Or rollup
    }
    if (content.match(/\(function\s*\(\)\s*\{/)) {
        return 'rollup';
    }
    return 'unknown';
}
