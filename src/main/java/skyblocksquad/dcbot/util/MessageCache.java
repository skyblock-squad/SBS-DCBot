package skyblocksquad.dcbot.util;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class MessageCache {
    private static final RandomAccessFile accessFile;

    static {
        try {
            accessFile = new RandomAccessFile("messageCache.bin", "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void addMessage(CachedMessage cachedMessage) {
        //String line = String.format("%s;%s;%s;%s", cachedMessage.getMessageId(), cachedMessage.getContentRaw(), cachedMessage.getAuthorTag(), cachedMessage.isBot());
        try {
            byte[] encodedContent = cachedMessage.contentRaw().getBytes(StandardCharsets.UTF_8);
            accessFile.seek(accessFile.length());
            accessFile.writeLong(cachedMessage.messageId());
            accessFile.writeShort(8 + 1 + encodedContent.length);
            accessFile.writeLong(cachedMessage.authorId());
            accessFile.writeBoolean(cachedMessage.isBot());
            accessFile.write(encodedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized CachedMessage getMessage(long messageId) {
        try {
            accessFile.seek(0);
            long readMessageId;
            int packLength;
            while (true) {
                readMessageId = accessFile.readLong();
                packLength = accessFile.readUnsignedShort();
                if (readMessageId == messageId) {
                    if (packLength < 9) throw new IOException("To less bytes available to read 'authorId' and 'isBot'");
                    long authorId = accessFile.readLong();
                    boolean isBot = accessFile.readBoolean();
                    byte[] encodedContent = new byte[packLength - 9];
                    accessFile.readFully(encodedContent);
                    return new CachedMessage(
                            readMessageId,
                            authorId,
                            isBot,
                            new String(encodedContent, StandardCharsets.UTF_8)
                    );
                } else {
                    accessFile.skipBytes(packLength);
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
            accessFile.seek(0);
            long readMessageId;
            int packLength;
            while (true) {
                readMessageId = accessFile.readLong();
                packLength = accessFile.readUnsignedShort();
                if (readMessageId == messageId) {
                    long writePointer = accessFile.getFilePointer() - 8 - 2; // 8 byte (long) messageId - 2 byte (short) pack length
                    long readPointer = accessFile.getFilePointer() + packLength;

                    byte[] buffer = new byte[1024];
                    int length;
                    while (true) {
                        accessFile.seek(readPointer);
                        if ((length = accessFile.read(buffer)) <= 0) break;
                        readPointer += length;
                        accessFile.seek(writePointer);
                        writePointer += length;
                        accessFile.write(buffer, 0, length);
                    }

                    accessFile.setLength(writePointer);
                    break;
                } else {
                    accessFile.skipBytes(packLength);
                }
            }
        } catch (IOException e) {
            if (!(e instanceof EOFException)) {
                e.printStackTrace();
            }
        }
    }
}
