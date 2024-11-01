package skyblocksquad.dcbot.util;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class MessageCache {

    private static final RandomAccessFile ACCESS_FILE;

    public static synchronized void addMessage(CachedMessage cachedMessage) {
        //String line = String.format("%s;%s;%s;%s", cachedMessage.getMessageId(), cachedMessage.getContentRaw(), cachedMessage.getAuthorTag(), cachedMessage.isBot());
        try {
            byte[] encodedContent = cachedMessage.contentRaw().getBytes(StandardCharsets.UTF_8);
            ACCESS_FILE.seek(ACCESS_FILE.length());
            ACCESS_FILE.writeLong(cachedMessage.messageId());
            ACCESS_FILE.writeShort(8 + 1 + encodedContent.length);
            ACCESS_FILE.writeLong(cachedMessage.authorId());
            ACCESS_FILE.writeBoolean(cachedMessage.isBot());
            ACCESS_FILE.write(encodedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized CachedMessage getMessage(long messageId) {
        try {
            ACCESS_FILE.seek(0);
            long readMessageId;
            int packLength;
            while (true) {
                readMessageId = ACCESS_FILE.readLong();
                packLength = ACCESS_FILE.readUnsignedShort();
                if (readMessageId == messageId) {
                    if (packLength < 9) throw new IOException("To less bytes available to read 'authorId' and 'isBot'");
                    long authorId = ACCESS_FILE.readLong();
                    boolean isBot = ACCESS_FILE.readBoolean();
                    byte[] encodedContent = new byte[packLength - 9];
                    ACCESS_FILE.readFully(encodedContent);
                    return new CachedMessage(
                            readMessageId,
                            authorId,
                            isBot,
                            new String(encodedContent, StandardCharsets.UTF_8)
                    );
                } else {
                    ACCESS_FILE.skipBytes(packLength);
                }
            }
        } catch (IOException e) {
            if (!(e instanceof EOFException)) e.printStackTrace();
        }
        return null;
    }

    public static synchronized void removeMessage(long messageId) {
        /*CachedMessage cachedMessage = getMessage(messageId);
        String line = String.format("%s;%s;%s;%s", messageId, cachedMessage.getContentRaw(), cachedMessage.getAuthorTag(), cachedMessage.isBot());
        FileHandler.removeLineFromFile(messageCacheFileName, line);*/
        try {
            ACCESS_FILE.seek(0);
            long readMessageId;
            int packLength;
            while (true) {
                readMessageId = ACCESS_FILE.readLong();
                packLength = ACCESS_FILE.readUnsignedShort();
                if (readMessageId == messageId) {
                    long writePointer = ACCESS_FILE.getFilePointer() - 8 - 2; // 8 byte (long) messageId - 2 byte (short) pack length
                    long readPointer = ACCESS_FILE.getFilePointer() + packLength;

                    byte[] buffer = new byte[1024];
                    int length;
                    while (true) {
                        ACCESS_FILE.seek(readPointer);
                        if ((length = ACCESS_FILE.read(buffer)) <= 0) break;
                        readPointer += length;
                        ACCESS_FILE.seek(writePointer);
                        writePointer += length;
                        ACCESS_FILE.write(buffer, 0, length);
                    }

                    ACCESS_FILE.setLength(writePointer);
                    break;
                } else {
                    ACCESS_FILE.skipBytes(packLength);
                }
            }
        } catch (IOException e) {
            if (!(e instanceof EOFException)) {
                e.printStackTrace();
            }
        }
    }

    static {
        try {
            ACCESS_FILE = new RandomAccessFile("messageCache.bin", "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
