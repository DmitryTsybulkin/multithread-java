package com.multi;


import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Scanner;

/** Класс, наследующий:
 * @see SimpleFileVisitor
 * Переопределяем нужные нам методы и добавляем свои
 * для упрощения поиска
 */
class OurFileVisitor extends SimpleFileVisitor<Path> {

    /** Искомый текст */
    private String text;

    /** Метод читает и записывает искомый текст в переменную
     * @see Multi */
    public void getText() throws IOException {
        System.out.print("Введите искомый текст: ");
        Scanner sc = new Scanner(System.in);
        text = sc.nextLine();
    }

    /**
     * Метод проверяет директорию на пустоту
     * @param dir - путь к директории
     * @return - true, если директория пуста, иначе - false
     * @throws IOException
     */
    private static boolean isDirEmpty(final Path dir) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
            return !dirStream.iterator().hasNext();
        }
    }


    /**
     * Переопределённый, унаследованный метод, для осуществления
     * действий перед посещением директории
     * @param dir - путь к директории
     * @param attrs - атрибуты файла
     * @return FileVisitResult.SKIP_SUBTREE - пропуск директории, если она пуста,
     * затем продолжить обход файловой системы
     * @throws IOException
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.println("Посещённая директория: " + dir);
        if (isDirEmpty(dir)) {
            System.out.println("Директория пуста: " + dir.getFileName());
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }


    /**
     * Переопределённый, унаследованный метод, для осуществления действии,
     * при нахождении файла.
     * @param file - путь к файлу
     * @param attrs - атрибуты файла
     * @return - возвращает FileVisitResult.TERMINATE - прервать обход файловой
     * системы, если искомый текст есть в файле, во всех остальных случаях поиск продолжается
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        System.out.println("Имя файла: " + file.getFileName());
        /** Список, в который будут записываться строки файла */
        List<String> content = null;
        /** Тип данных, содержащихся в файле */
        String mimetype = Files.probeContentType(file);

        /** Проверка на символическую ссылку */
        if (attrs.isSymbolicLink()) {
            return FileVisitResult.CONTINUE;
        }

        /** Если тип данных файла соответствует изображению или
         * системному файлу, то обход продолжается */
        if (mimetype != null && mimetype.split("/")[0].equals("image, system")) {
            return FileVisitResult.CONTINUE;
        } else {
            try {
                /** Попытка прочитать все строки файла, соответственно кодировке UTF_8 */
                content = Files.readAllLines(file, StandardCharsets.UTF_8);
                /** Если прочитанные строки содержат искомый текст, выводится название файла
                 * и абсолютный путь к нему */
                if (content.toString().contains(text)) {
                    System.out.println("ТЕКСТ НАЙДЕН в: " + file.getFileName() + "\n" + "Путь к файлу: " + file.toAbsolutePath());
                    content.clear();
                    return FileVisitResult.TERMINATE;
                }
                /** 1 исключение - если файл не соответствует кодировке UTF_8
                 * 2 исключение - если файл является системным и доступ к нему ограничен */
            } catch (MalformedInputException e) {
                e.getMessage();
                System.out.println("Файл " + file.getFileName() + " не будет прочитан, потому что его кодировка не соответствует UTF_8");
            } catch (FileSystemException e) {
                e.getMessage();
            }
        }
        /** Чистим список, чтобы избежать переполнения кучи */
        if (content != null) {
            content.clear();
        }
        return FileVisitResult.CONTINUE;
    }


    /**
     * Переопределённый, унаследованный метод, для осуществления действии,
     * при ошибке посещения файла
     * @param file - путь к файлу
     * @return - FileVisitResult.CONTINUE
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println(exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

}
