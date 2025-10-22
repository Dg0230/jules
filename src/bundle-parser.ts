
import * as parser from '@babel/parser';
import traverse from '@babel/traverse';
import * as t from '@babel/types';
import { Node } from '@babel/types';
import { generate } from '@babel/generator';

export type BundlerType = 'webpack' | 'rollup' | 'vite' | 'parcel' | 'unknown';

export interface Module {
  id: string;
  code: string;
  dependencies: string[];
}

export function extractWebpackModules(content: string): Module[] {
  const ast = parser.parse(content, {
    sourceType: 'script',
    allowReturnOutsideFunction: true,
  });

  const modules: Module[] = [];

  traverse(ast, {
    ObjectExpression(path) {
      // Heuristic: check if this object looks like a webpack module map
      const looksLikeWebpackModules = path.node.properties.every(
        prop =>
          t.isObjectProperty(prop) &&
          t.isStringLiteral(prop.key) &&
          t.isFunctionExpression(prop.value),
      );

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
            if (!functionExpressionPath.isFunctionExpression()) return;

            const functionBodyNode = valueNode.body;
            const code = generate(functionBodyNode).code;
            const dependencies: string[] = [];

            // Traverse the function's path to find dependencies
            functionExpressionPath.traverse({
              CallExpression(innerPath) {
                if (
                  t.isIdentifier(innerPath.node.callee) &&
                  innerPath.node.callee.name === '__webpack_require__' &&
                  innerPath.node.arguments.length > 0 &&
                  t.isStringLiteral(innerPath.node.arguments[0])
                ) {
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


export function detectBundler(content:string): BundlerType {
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
