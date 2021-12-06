package team.unnamed.emojis.export.impl;

import org.jetbrains.annotations.NotNull;
import team.unnamed.emojis.export.ResourceExporter;
import team.unnamed.emojis.io.AssetWriter;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.io.TreeOutputStream;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipOutputStream;

/**
 * Fluent-style class for exporting resource
 * packs and upload it using HTTP servers like
 * <a href="https://mc-packs.net">MCPacks</a>,
 * that requires us to compute the SHA-1 hash and
 * upload the file
 */
public class MCPacksHttpExporter implements ResourceExporter {

    private static final String UPLOAD_URL = "https://mc-packs.net/";
    private static final String DOWNLOAD_URL_TEMPLATE = "https://download.mc-packs.net/pack/" +
            "%HASH%.zip";

    private static final String BOUNDARY = "HephaestusBoundary";
    private static final String LINE_FEED = "\r\n";

    private final URL url;

    public MCPacksHttpExporter() throws MalformedURLException {
        this.url = new URL(UPLOAD_URL);
    }

    @Override
    @NotNull
    public UrlAndHash export(AssetWriter writer) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(10000);

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        connection.setRequestProperty("User-Agent", "Unnamed-Emojis");
        connection.setRequestProperty("Charset", "utf-8");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        String hashString;

        // write http request body
        try (OutputStream output = connection.getOutputStream()) {
            Streams.writeUTF(
                    output,
                    "--" + BOUNDARY + LINE_FEED
                            + "Content-Disposition: form-data; name=\"file\"; filename=\"emojis.zip\""
                            + LINE_FEED + "Content-Type: application/zip" + LINE_FEED + LINE_FEED
            );

            MessageDigest digest;

            try {
                digest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new IOException("Cannot find SHA-1 algorithm");
            }

            TreeOutputStream treeOutput = TreeOutputStream.forZip(
                    new ZipOutputStream(new DigestOutputStream(output, digest))
            );
            try {
                writer.write(treeOutput);
            } finally {
                treeOutput.finish();
            }

            byte[] hash = digest.digest();
            int len = hash.length;
            StringBuilder hashBuilder = new StringBuilder(len * 2);
            for (byte b : hash) {
                int part1 = (b >> 4) & 0xF;
                int part2 = b & 0xF;
                hashBuilder
                        .append(hex(part1))
                        .append(hex(part2));
            }

            hashString = hashBuilder.toString();

            Streams.writeUTF(
                    output,
                    LINE_FEED + "--" + BOUNDARY + "--" + LINE_FEED
            );
        }

        // execute request and close, no response expected
        connection.getInputStream().close();

        return new UrlAndHash(
                DOWNLOAD_URL_TEMPLATE.replace("%HASH%", hashString),
                hashString
        );
    }

    private char hex(int c) {
        return "0123456789abcdef".charAt(c);
    }

}
