/**
 * Modified from sample decoder
 *
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package com.backblaze.erasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Decoder {

    public static final int BYTES_IN_INT = 4;

    public static boolean decode(File shardsFolder, File decodedFile, int fid, int noa) throws IOException {

        final int totalShards = noa;
        final int dataShards = noa / 2;      
        
        // Read in any of the shards that are present.
        // (There should be checking here to make sure the input
        // shards are the same size, but there isn't.)
        final byte [] [] shards = new byte [totalShards] [];
        final boolean [] shardPresent = new boolean [totalShards];
        int shardSize = 0;
        int shardCount = 0;
        for (int i = 0; i < totalShards; i++) {
            File shardFile = new File(shardsFolder, Integer.toString(fid * 100 + i));
            if (shardFile.exists()) {
                shardSize = (int) shardFile.length();
                shards[i] = new byte [shardSize];
                shardPresent[i] = true;
                shardCount += 1;
                InputStream in = new FileInputStream(shardFile);
                in.read(shards[i], 0, shardSize);
                in.close();
            }
        }

        // We need at least dataShards to be able to reconstruct the file.
        if (shardCount < dataShards) {
            System.out.println("Not enough shards present");
            return false;
        }

        // Make empty buffers for the missing shards.
        for (int i = 0; i < totalShards; i++) {
            if (!shardPresent[i]) {
                shards[i] = new byte [shardSize];
            }
        }

        // Use Reed-Solomon to fill in the missing shards
        ReedSolomon reedSolomon = ReedSolomon.create(dataShards, dataShards);
        reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

        // Combine the data shards into one buffer for convenience.
        // (This is not efficient, but it is convenient.)
        byte [] allBytes = new byte [shardSize * dataShards];
        for (int i = 0; i < dataShards; i++) {
            System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
        }

        // Extract the file length
        int fileSize = ByteBuffer.wrap(allBytes).getInt();

        // Write the decoded file
        if (decodedFile.exists())
        	decodedFile.delete();
        OutputStream out = new FileOutputStream(decodedFile);
        out.write(allBytes, BYTES_IN_INT, fileSize);
        out.close();
        
        System.out.println("Decode Success");          
        return true;
    }
}
