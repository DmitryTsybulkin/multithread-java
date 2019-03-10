package com.multi;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Multi {

    private static ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {

        /** Создаём объект класса:
         * @see OurFileVisitor */
        final OurFileVisitor visitor = new OurFileVisitor();

        /** Вызываем метод для прочтения и сохранения искомого текста в переменную
         * @see OurFileVisitor */
        visitor.getText();

        /** Записываем в переменную начальную директорию,
         * из которой будет осуществлён обход файловой системы */
        System.out.print("Введите коренную директорию в которой нужно осуществить поиск (по умолчанию поиск в директории - files): ");
        Scanner sc = new Scanner(System.in);
        String Dir = sc.nextLine();

        /** Читаем и преобразуем текстовую директорию в путь */
        final Path fileDir;
        if (sc.hasNextLine()) {
            fileDir = Paths.get(Dir);
        } else {
            fileDir = Paths.get("files");
        }
        /** Записываем время старта. Реализуем наследуемый интерфейс:
         * @see Runnable
         * Реализуем метод обхода файловой системы, в пуле потоков, передавая
         * как параметры: путь к начальной директории и объект класса OurFileVisitor
         */
        long start = System.currentTimeMillis();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Files.walkFileTree(fileDir, visitor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
        /** Выводим на экран затраченное время */
        long SpentTime = System.currentTimeMillis() - start;
        System.out.println("Затрачено времени: " + SpentTime + " миллисекунд");
    }
}
