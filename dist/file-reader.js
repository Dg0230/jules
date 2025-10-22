"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.FileReader = void 0;
const fs_1 = require("fs");
class FileReader {
    readStream(filePath) {
        return (0, fs_1.createReadStream)(filePath, { encoding: 'utf8' });
    }
    async getFileInfo(filePath) {
        const stats = await fs_1.promises.stat(filePath);
        return {
            path: filePath,
            size: stats.size,
            encoding: 'utf8',
        };
    }
    async checkFileSize(filePath) {
        const stats = await fs_1.promises.stat(filePath);
        return stats.size;
    }
}
exports.FileReader = FileReader;
