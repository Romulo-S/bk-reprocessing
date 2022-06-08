package com.cerc.utils.reprocessing.utils;

import java.util.Arrays;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;


public class Compressor {

    private static int decompressedLength;
    private static LZ4Factory factory = LZ4Factory.fastestInstance();
    private static LZ4Compressor compressor = factory.fastCompressor();
    private static LZ4FastDecompressor decompressor = factory.fastDecompressor();

    public static byte[] compress(byte[] src, int srcLen) {
        decompressedLength = srcLen;
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        int compressLen = compressor.compress(src, 0, decompressedLength, compressed, 0, maxCompressedLength);
        byte[] finalCompressedArray = Arrays.copyOf(compressed, compressLen);
        return finalCompressedArray;
    }

    public static byte[] decompress(byte[] finalCompressedArray, int decompressedLength) {
        byte[] restored = new byte[decompressedLength];
        restored = decompressor.decompress(finalCompressedArray, decompressedLength);
        return restored;
    }
}
