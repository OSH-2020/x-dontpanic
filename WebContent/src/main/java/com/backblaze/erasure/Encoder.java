/**
 * Modified from sample encoder
 *
 * Copyright 2015, Backblaze, Inc.
 */

package com.backblaze.erasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Encoder {

	public static final int BYTES_IN_SHARDS = 500000;
    public static final int BYTES_IN_INT = 4;

    public static boolean encode(File inputFile, File shardsFolder, int fid) throws IOException {

        if (!inputFile.exists()) {
            System.out.println("Cannot read input file: " + inputFile);
            return false;
        }
             
        // Get the size of the input file.  (Files bigger that
        // Integer.MAX_VALUE will fail here!)
        final int fileSize = (int) inputFile.length();
        final int dataShards = fileSize / BYTES_IN_SHARDS + 1;
        final int totalShards = 2 * dataShards;

        // Figure out how big each shard will be.  The total size stored
        // will be the file size (8 bytes) plus the file.
        final int storedSize = fileSize + BYTES_IN_INT;
        final int shardSize = (storedSize + dataShards - 1) / dataShards;

        // Create a buffer holding the file size, followed by
        // the contents of the file.
        final int bufferSize = shardSize * dataShards;
        final byte [] allBytes = new byte[bufferSize];
        ByteBuffer.wrap(allBytes).putInt(fileSize);
        InputStream in = new FileInputStream(inputFile);
        int bytesRead = in.read(allBytes, BYTES_IN_INT, fileSize);
        in.close();
        if (bytesRead != fileSize) {       	
            throw new IOException("not enough bytes read");
        }

        // Make the buffers to hold the shards.
        byte [] [] shards = new byte [totalShards] [shardSize];

        // Fill in the data shards
        for (int i = 0; i < dataShards; i++) {
            System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
        }

        // Use Reed-Solomon to calculate the parity.
        ReedSolomon reedSolomon = ReedSolomon.create(dataShards, dataShards);
        reedSolomon.encodeParity(shards, 0, shardSize);

        // Write out the resulting files.
        for (int i = 0; i < totalShards; i++) {
            File outputFile = new File(shardsFolder, Integer.toString(fid * 100 + i));
            OutputStream out = new FileOutputStream(outputFile);
            out.write(shards[i]);
            out.close();
        }
        
        System.out.println("Encode Success");
        return true;
    }
}
