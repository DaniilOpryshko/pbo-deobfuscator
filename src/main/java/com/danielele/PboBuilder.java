package com.danielele;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PboBuilder
{
    private final MappedByteBuffer buffer;
    private final int dataStartOffset;
    private Pbo pbo;
    private final int threadCount;
    private final int saveThreadCount;

    public PboBuilder(MappedByteBuffer buffer) throws IOException
    {
        this.buffer = buffer;
        this.threadCount = Math.min(Runtime.getRuntime().availableProcessors(), 8);
        this.saveThreadCount = Math.min(Runtime.getRuntime().availableProcessors() * 2, 16);

        ReadResult result = readPboStructure();
        this.pbo = result.pbo;
        this.dataStartOffset = result.dataStartOffset;
    }

    public Pbo build() throws InterruptedException, ExecutionException
    {
        processEntries();
        filterEmptyEntries();
        return pbo;
    }

    public void saveToDirectory(Path rootDir) throws IOException, InterruptedException, ExecutionException
    {
        Files.createDirectories(rootDir);
        saveFilesParallel(rootDir);
    }

    private void processEntries() throws InterruptedException, ExecutionException
    {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = new ArrayList<>();

        int currentOffset = dataStartOffset;
        for (PboEntry entry : pbo.entries)
        {
            int entryOffset = currentOffset;
            futures.add(executor.submit(() ->
            {
                decodeEntry(entry, entryOffset);
                if (!FileUtil.isBinaryUTF8(entry.content))
                {
                    entry.binary = false;
                    entry.noBinaryContet = new String(entry.content);
                }
                else
                {
                    entry.binary = true;

                    if (entry.content.length >= 4 && (entry.content[0] == 0x00 && entry.content[1] == 'r' && entry.content[2] == 'a' && entry.content[3] == 'P'))
                    {
                        entry.rapified = true;
                    }
                }
                return null;
            }));
            currentOffset += entry.dataSize;
        }

        for (Future<Void> future : futures)
        {
            future.get();
        }
        executor.shutdown();
    }

    private void decodeEntry(PboEntry entry, int offset)
    {
        byte[] data = extractData(offset, entry.dataSize);

        if (entry.packagingMethod.equals("srpC"))
        {
            entry.content = PboLzssDecoder.decodeBlock(data, 0, (int) entry.dataSize,
                    (int) entry.originalSize, false);
        }
        else
        {
            entry.content = data;
        }
    }

    private byte[] extractData(int offset, long size)
    {
        MappedByteBuffer slice = buffer.duplicate();
        slice.position(offset);
        slice.limit(offset + (int) size);

        byte[] data = new byte[(int) size];
        slice.get(data);
        return data;
    }

    private void filterEmptyEntries()
    {
        pbo.entries = pbo.entries.stream()
                .filter(e -> e.dataSize != 0)
                .filter(e -> !e.filename.equals("\\\\\\"))
                .filter(e ->
                {
                    if (e.packagingMethod.equals("srpC") && (e.originalSize == 0 || (e.originalSize < e.dataSize)))
                    {
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                })
                .filter(e ->
                {
                    if (e.noBinaryContet != null)
                    {
                        return !isEmpty(e.noBinaryContet);
                    }
                    else
                    {
                        return true;
                    }
                })
                .collect(Collectors.toUnmodifiableList());

        pbo.entries.forEach(entry ->
        {
            if (entry.noBinaryContet != null)
            {
                entry.noBinaryContet = formatCode(entry.noBinaryContet);
            }
        });
    }

    private String formatCode(String code)
    {
        String[] lines = code.split("\n");
        StringBuilder result = new StringBuilder();
        int indentLevel = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                result.append("\n");
                continue;
            }

            if (trimmed.startsWith("}")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            result.append("\t".repeat(indentLevel)).append(trimmed).append("\n");

            if (trimmed.contains("{")) {
                indentLevel++;
            }

        }

        return result.toString();
    }

    private boolean isEmpty(String content)
    {
        Pattern multiline = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL); //pbo.entries.stream().filter(e -> e.filename.contains("4_world")).collect(Collectors.toUnmodifiableList())
        String withoutMultiline = multiline.matcher(content).replaceAll("");

        String withoutComments = withoutMultiline.replaceAll("//.*", "");

        String cleaned = withoutComments.replaceAll("\\s+", "");

        return cleaned.isBlank();
    }

    private void saveFilesParallel(Path rootDir) throws InterruptedException, ExecutionException
    {
        ExecutorService executor = Executors.newFixedThreadPool(saveThreadCount);
        List<Future<Void>> futures = new ArrayList<>();

        for (PboEntry entry : pbo.entries)
        {
            futures.add(executor.submit(() ->
            {
                saveEntry(entry, rootDir);
                return null;
            }));
        }

        for (Future<Void> future : futures)
        {
            future.get();
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    private void saveEntry(PboEntry entry, Path rootDir)
    {
        try
        {
            Path filePath = rootDir.resolve(PathSanitizer.sanitize(entry.filename));
            Files.createDirectories(filePath.getParent());

            if (!FileUtil.isBinaryUTF8(entry.content))
            {
                if (entry.noBinaryContet != null)
                {
                    Files.writeString(filePath, entry.noBinaryContet);
                }
                else
                {
                    Files.writeString(filePath, new String(entry.content, StandardCharsets.UTF_8));
                }
            }
            else
            {
                if (entry.rapified && !entry.unrapified.isEmpty())
                {
                    if (entry.filename.endsWith(".bin"))
                    {
                        filePath = filePath.resolveSibling(
                                filePath.getFileName().toString().replaceAll("\\.bin$", ".cpp")
                        );
                    }
                    Files.writeString(filePath, entry.unrapified);
                }
                else
                {
                    Files.write(filePath, entry.content);
                }
            }
            System.out.println("Saved: " + filePath);
        }
        catch (IOException e)
        {
            System.err.println("Error while saving " + entry.filename + ": " + e.getMessage());
        }
    }

    private ReadResult readPboStructure() throws IOException
    {
        MappedReader reader = new MappedReader(buffer);
        ReadResult result = new ReadResult();
        result.pbo = new Pbo();

        while (true)
        {
            PboEntry entry = reader.readPboEntry();

            if (entry.filename.isEmpty())
            {
                if (entry.packagingMethod.equals("sreV"))
                {
                    readHeaders(reader, result.pbo);
                }
                else
                {
                    break;
                }
            }
            else
            {
                result.pbo.entries.add(entry);
            }
        }

        result.dataStartOffset = reader.getPosition();
        return result;
    }

    private void readHeaders(MappedReader reader, Pbo pbo) throws IOException
    {
        while (true)
        {
            String key = reader.readStringZ();
            if (key.isEmpty()) break;
            String value = reader.readStringZ();
            pbo.headers.put(key, value);
        }
    }
}