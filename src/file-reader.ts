
import { createReadStream, promises as fs } from 'fs';
import { Readable } from 'stream';

export interface FileInfo {
  path: string;
  size: number;
  encoding: string;
}

export class FileReader {
  public readStream(filePath: string): Readable {
    return createReadStream(filePath, { encoding: 'utf8' });
  }

  public async getFileInfo(filePath: string): Promise<FileInfo> {
    const stats = await fs.stat(filePath);
    return {
      path: filePath,
      size: stats.size,
      encoding: 'utf8',
    };
  }

  public async checkFileSize(filePath: string): Promise<number> {
    const stats = await fs.stat(filePath);
    return stats.size;
  }
}
