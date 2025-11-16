package com.danielele;

import com.danielele.rapify.Rapifier;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class PboReader
{
    public static void main(String[] args)
    {
//        if (args.length == 0)
//        {
//            System.out.println("Usage: PboReader <pbo file>");
//            return;
//        }
//
//        if (args.length > 1)
//        {
//            System.out.println("Only single file supported. Usage: PboReader <pbo file>");
//            return;
//        }
//
//        String path = args[0];
//
//        String[] split = path.split("/");
//
//        for (int i = 0; i < split.length; i++)
//        {
//            if (i == (split.length - 1))
//            {
//                if (!split[i].endsWith(".pbo"))
//                {
//                    System.out.println("File must end with .pbo");
//                    return;
//                }
//            }
//        }
//
//        Path pathToPbo = Path.of(args[0]);

        Path pathToPbo = Path.of("NoFreecam.pbo");

        Path parentDir;

        if (pathToPbo.isAbsolute())
        {
            parentDir = pathToPbo.getParent();
        }
        else
        {
            parentDir = pathToPbo.toAbsolutePath().getParent();
        }

        String fileNameWithoutExtension = pathToPbo.getFileName().toString().substring(0, pathToPbo.getFileName().toString().lastIndexOf('.'));

        long startTime = System.currentTimeMillis();

        try (RandomAccessFile raf = new RandomAccessFile(pathToPbo.toString(), "r");
             FileChannel channel = raf.getChannel())
        {

            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
            buffer.load();

            PboBuilder pboBuilder = new PboBuilder(buffer);
            Pbo pbo = pboBuilder.build();
            pbo.entries.forEach(entry ->
            {
                if (entry.rapified)
                {
                    entry.unrapified = Rapifier.main(entry.content);
                }
            });

            Path resolve = parentDir.resolve(fileNameWithoutExtension);

            pboBuilder.saveToDirectory(Path.of(resolve.toString()));


            System.out.println("Total unpack time: " + (System.currentTimeMillis() - startTime) + " ms");
            System.in.read();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}