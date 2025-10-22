
import { FileReader } from '../src/file-reader';
import { promises as fs } from 'fs';
import { Readable } from 'stream';

describe('FileReader', () => {
  const filePath = './test-file.txt';
  const fileContent = 'hello world';

  beforeAll(async () => {
    await fs.writeFile(filePath, fileContent);
  });

  afterAll(async () => {
    await fs.unlink(filePath);
  });

  it('should get file info', async () => {
    const fileReader = new FileReader();
    const fileInfo = await fileReader.getFileInfo(filePath);
    expect(fileInfo.path).toBe(filePath);
    expect(fileInfo.size).toBe(fileContent.length);
  });

  it('should check file size', async () => {
    const fileReader = new FileReader();
    const fileSize = await fileReader.checkFileSize(filePath);
    expect(fileSize).toBe(fileContent.length);
  });

  it('should read a file as a stream', (done) => {
    const fileReader = new FileReader();
    const stream = fileReader.readStream(filePath);
    let data = '';

    stream.on('data', (chunk) => {
      data += chunk;
    });

    stream.on('end', () => {
      expect(data).toBe(fileContent);
      done();
    });

    stream.on('error', (err) => {
      done(err);
    });
  });
});
