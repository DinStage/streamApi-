package java.ru.dubrovin;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ClientInfoStatus;
import java.text.DateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class StreamApi {
    public static void main(String[] args) {

        File directory = new File("./src/main/java");
        String[] names = directory.list(((dir, name) -> name.endsWith(".java")));
        System.out.println(Arrays.asList(names));

        //☺ При многосрочном режиме нужно добавлять return
        File directoryFile = new File("./src/main/java");
        String[] namesStrings = directory.list(((dir, name) -> {
            return name.endsWith(".java");
        }));

        System.out.println(Arrays.asList(namesStrings));

        //☺Печать стрима
        //-----------------------------------------------------------------------------------------
        Stream.of(3, 1, 4, 5, 6, 18).forEach(x -> System.out.println(x)); //1
        Stream.of(3, 1, 4, 5, 6, 18).forEach(System.out::println); //2

        Consumer<Integer> printer = System.out::println; // Через создание консьюмера
        Stream.of(1, 56, 2, 1, 56, 7).forEach(printer);
        //-----------------------------------------------------------------------------------------

        //☺Генерация рандомного стрима
        Stream.generate(Math::random).limit(10).forEach(System.out::println);
        //-----------------------------------------------------------------------------------------


        //☺ Сортировка строк
        List<String> strings = Arrays.asList("this ", "is", "a", "list");
        List<String> sorted =
                strings.stream().sorted((o1, o2) -> o1.compareTo(o2)).collect(Collectors.toList());
        // Аналогично
        List<String> sorted2 = strings.stream().sorted(String::compareTo).collect(Collectors.toList());

        Stream.of("this ", "is", "a", "list").map(String::length).forEach(System.out::println);
        //-----------------------------------------------------------------------------------------

        //☺ Создание  листа объектов из стрима
        List<String> stringList = Arrays.asList("Вася", "пПетя", "Коля");
        List<Person> personList = stringList.stream().map(Person::new).collect(Collectors.toList());
        //-----------------------------------------------------------------------------------------

        //Заваричиваем объекты в листы
        Person before = new Person("Garce Place");
        List<Person> personList1 = Stream.of(before).collect(Collectors.toList());
        //-----------------------------------------------------------------------------------------

        //☺ Использование конструктора с переменным числом аргументов
        List<String> namesStrings1 = Arrays.asList("Иванов иван", "Сидоров Петр");
        List<Person> dellimitterStrings = namesStrings1.stream().map(name -> name.split(" ")).map(Person::new).collect(Collectors.toList());
        dellimitterStrings.forEach(System.out::println);


        //-----------------------------------------------------------------------------------------------------------
        //Генерация последовательностей

        List<BigDecimal> bigDecimals = Stream.iterate(BigDecimal.ONE, n -> n.add(BigDecimal.ONE)).limit(10).collect(Collectors.toList());
        System.out.println(bigDecimals);
        // boxed Оборачивает int в Integer
        // последний не входит  range
        List<Integer> integerList = IntStream.range(10, 15).boxed().collect(Collectors.toList());
        System.out.println(integerList);

        // Последний элемент входит rangeClosed
        List<Integer> integerList1 = IntStream.rangeClosed(10, 15).boxed().collect(Collectors.toList());
        System.out.println(integerList1);
        //-----------------------------------------------------------------------------------------------------------
        //Редуция Вписываем любую формулу и она читается в зависимолсти от предыдущего и последующего аргумеентов
        int sum = IntStream.rangeClosed(1, 10).reduce((left, right) -> left + 2 * right).orElse(0); // x +2 *y
        System.out.println(sum);

        int sumNew = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).reduce(0, Integer::sum); //обозначенм что суммирование начинаем с 0
        System.out.println(sumNew);
        // поиск макимального элемента с помощью редуции
        Integer max = Stream.of(3, 1, 4, 5, 6, 12, 1, 45)
                .reduce(Integer.MIN_VALUE, Integer::max);
        System.out.println(max);


        String s = Stream.of(" Hello", " ", " my ", "frend").reduce("", String::concat);
        System.out.println(s);
        //-----------------------------------------------------------------------------------------------------------


        //Использование метода peek - метод peek необходимо удалять при проозводственной эксплуатации

        int li = IntStream.rangeClosed(1, 100).peek(n -> System.out.println("Исходное значение = " + n))
                .map(n -> n * 2)
                .peek(n -> System.out.println("Увеличенное значение = " + n))
                .sum();

//-----------------------------------------------------------------------------------------------------------
        //Опционалы
        // unordered() Возвращает эквивалентный поток, который является неупорядоченным .
        Optional<Integer> any = Stream.of(1, 3, 4, 4, 1, 1, 3, 54, 6)
                .unordered()
                .parallel()
                .map(n -> n * 2)
                .findAny();
        System.out.println(any.orElse(0));

        //-----------------------------------------------------------------------------------------------------------
        //Сортировки

        // Сортивока по длине Comparator.comparing(String::length) , а зетем лексикографическая
        List<String> list = Stream.of("Оля", "Ваня", "Варька")
                .sorted(Comparator.comparing(String::length)
                        .thenComparing(Comparator.naturalOrder()))
                .collect(Collectors.toList());

        List<Golfer> golferList = Arrays.asList(
                new Golfer("Джек", "Никлаус", 68)
                , new Golfer("Иван", "Дубровин", 28)
                , new Golfer("Ольга", "Дубровина", 12)
                , new Golfer("Макс", "Махалов", 128)
        );
        // Сортировка по возрасту
        golferList.stream().sorted(Comparator.comparingInt(Golfer::getScore)
                .thenComparing(Golfer::getFirst)
                .thenComparing(Golfer::getLast))
                .forEach(System.out::println);

        Map<Integer, Golfer> integerGolferHashMap = golferList.stream().collect(Collectors.toMap(Golfer::getScore, value -> value));

        integerGolferHashMap.forEach((integer, golfer) -> System.out.println(integer + " - " + golfer));

        //-----------------------------------------------------------------------------------------------------------
        //Сумма числе в стриме
        List<Integer> nums = Arrays.asList(3, 3, 1, 12, 12, 123, 12, 2);
        int total = 0;
        for (int n : nums) {
            total += n;
        }

        // аналогично
        total = 0;
        nums.stream().mapToInt(Integer::valueOf).sum();

        //-----------------------------------------------------------------------------------------------------------
        //    ФУНКЦИИ
        Function<Integer, Integer> add = x -> x + 2; // обозначаем функцию
        Function<Integer, Integer> mult3 = x -> x * 3; // обозначаем функцию
        // Определяем исполнение последовательности функий в виде новой функции
        // Function<Входной аргуемент , Выходной аргумент>
        Function<Integer, Integer> mult3Add = add.compose(mult3); //Сначало mult 3 атем add
        Function<Integer, Integer> addMult3 = add.andThen(mult3); //Сначало add  атем mult 3

        System.out.println("add : " + add.apply(100));
        System.out.println("mult3 : " + mult3.apply(100));
        System.out.println("mult3Add : " + mult3Add.apply(100));
        System.out.println("addMult3 : " + addMult3.apply(100));

        // Возвести в квадрат и перевести в строку
        Function<Integer, Integer> add2 = x -> x * x;
        Function<Integer, String> add2Tostring = add2.andThen(Objects::toString);
        System.out.println(add2Tostring.apply(5));


        //Пример на функциях
        // Создаются 2 потребителя , 3 потребитель формируется из 2х , 3ий потребитель применяется на обходе стрима
        Logger log = Logger.getLogger("  ");
        Consumer<String> println = System.out::println;
        Consumer<String> logInformer = log::info;

        Consumer<String> printlnLogInformer = println.andThen(logInformer);
        Stream.of("1", "2", "3", "4", "5", "123", "123").forEach(printlnLogInformer);

        //-----------------------------------------------------------------------------------------------------------
        // OPTIONAL

        System.out.println("рез : " + Optional.ofNullable(null).orElse(0));


        Optional<Integer> integerOptional = Optional.ofNullable(100);
        String str = integerOptional.stream().map(Objects::toString).findFirst().get();


        //-----------------------------------------------------------------------------------------------------------
        // РАБОТА С ФАЙЛАМИ
        // Получение строк длинее указанной длины из файла , сортировк по длине
        try {
            Stream<String> lines = lines = Files.lines(Paths.get("C:\\AppServ\\www\\bootgen\\akt.php"));
            lines.filter(ss -> ss.length() > 100)
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .limit(10)
                    .forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //-----------------------------------------------------------------------------------------------------------
        //Сепаратор в зависимости от ОС на которой исполнется код
        System.out.println(File.separator);
        //-----------------------------------------------------------------------------------------------------------
        // Обход файловой системы в глубину функция WALK
        Stream<Path> paths = null;
        try {
            paths = Files.walk(Paths.get("C:" + File.separator + "AppServ" + File.separator + "www" + File.separator + "bootgen")).limit(10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        paths.forEach(System.out::println);
        //-----------------------------------------------------------------------------------------------------------

        // Поиск у казанном каталоге
        try {
            // Каталог  , максимальный уровень опускать , и лябмда поиска
            Stream<Path> pathStream = Files.find(Paths.get("C:\\AppServ\\www\\bootgen\\"), Integer.MAX_VALUE,
                    (path, basicFileAttributes) -> !basicFileAttributes.isDirectory() && path.toString().contains(".php"));
            pathStream.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //-----------------------------------------------------------------------------------------------------------
        //JAVA TIME

        System.out.println("Instant.now() " + Instant.now());
        System.out.println("LocalDate.now() " + LocalDate.now());
        System.out.println("LocalTime.now() " + LocalTime.now());
        System.out.println("LocalDateTime.now() " + LocalDateTime.now());
        System.out.println("ZonedDateTime.now() " + ZonedDateTime.now());

        System.out.println("Первая высадка на луне");
        LocalDate localDate = LocalDate.of(1968, Month.JULY, 20);
        LocalTime localTime = LocalTime.of(20, 18);
        System.out.println("Дата : " + localDate);
        System.out.println("Время : " + localTime);

        System.out.println("Нил Армстронг выходит на поверхность : ");
        LocalTime worTime = LocalTime.of(20, 2, 56, 150_000_000);
        LocalDateTime workDateTime = LocalDateTime.of(localDate, worTime);
        System.out.println(workDateTime);

        // прибавление 2х месяцев 2х дней
        // ! Написать функцию расчета Возраста!!!!
        LocalDate localDate1 = LocalDate.of(2021, Month.DECEMBER, 01).plusMonths(2).plusDays(2);
        System.out.println(localDate1);
        //-----------------------------------------------------------------------------------------------------------

        //Форматирование Дат
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate2 = LocalDate.of(2021, 01, 03);

        System.out.println(localDate2.format(formatter));

        //-----------------------------------------------------------------------------------------------------------
        Period period = Period.of(1, 1, 15);
        LocalDate localDate3 = LocalDate.of(2021, 12, 05);
        System.out.println("прибавление периода к дате :" + localDate3.plus(period));
        System.out.println("Отнимание  периода от даты :" + localDate3.minus(period));

        // Исправление отпределенной части даты с Помощью with
        LocalDate localDate4 = LocalDate.of(2021, 05, 24);
        localDate4 = localDate4.withYear(1993); // исправим год!
        System.out.println(localDate4);

        // Вернуть первую дату следущего месяца
        localDate4 = localDate4.with(TemporalAdjusters.firstDayOfNextMonth());
        System.out.println("  первую дату следущего месяца :" + localDate4);
        //вернуть последнюю дату в месяце
        localDate4 = localDate4.with(TemporalAdjusters.lastDayOfMonth());
        System.out.println("  Последняя дата в месяце :" + localDate4);
        //вернуть перую  дату в году
        localDate4 = localDate4.with(TemporalAdjusters.firstDayOfYear());
        System.out.println("  Последняя дата в году :" + localDate4);
        //-----------------------------------------------------------------------------------------------------------

        // Пример генерации последовательносте дней
        IntStream.rangeClosed(15, 30)
                .mapToObj(day -> LocalDate.of(2021, Month.DECEMBER, day))
                .limit(3)
                .forEach(System.out::println);


        System.out.println(Stream.of(1, 6, 78, 234, 768678, 123).isParallel());
        Stream.iterate(1, n -> n + 1).isParallel();
        Stream.generate(Math::random).isParallel();
        List<Integer> integerList2 = Arrays.asList(1, 3, 1, 3214, 234, 1, 23, 123);
        // Паралельные стримы
        // parallelStream  и   .parallel() - создает из последовательного параельный стрим
        //sequential - создает последовательный из параельного

        //-----------------------------------------------------------------------------------------------------------
        //ПАРАЛЕЛЬНЫЕ СТРИМЫ


        System.out.println((integerList2.parallelStream().isParallel()));
        System.out.println(" Создание паралельного  из Последовательного стрима : " +
                Stream.of(123, 12, 4534, 12, 2332, 12)
                        .parallel()
                        .isParallel());

        List<Integer> list1 = List.of(12312, 123, 123, 45, 345, 345);
        System.out.println(" Создание последовательного из таралельного : " + list1.parallelStream()
                .sequential()
                .isParallel());


        // удвоить в параельном режиме отсортировать в последовательном
        //-----------------------------------------------------------------------------------------------------------
        // При таком подходе все происходит в последовательном режиме
        // Если хотите распаралелиться создайте 2 потока
        list1.parallelStream().map(value -> value * 2).sequential().sorted().forEach(System.out::println);
        //-----------------------------------------------------------------------------------------------------------

        // вот так делать правильно !1 Стрим ситает ПАРАЛЕЛЬНО 2 ой выводит
        list1 = list1.parallelStream().map(value -> value * 2).collect(Collectors.toList());
        list.stream().sorted().forEach(System.out::println);
        //-----------------------------------------------------------------------------------------------------------

        /***************************************************************************************************************/
        /**РАЗБИТИЕ ПО ПОТОКАМ*/

        // Ориентируемся на стандартное количество поток = количеству процессоров
        Long start = System.currentTimeMillis();
        Long result = LongStream.rangeClosed(1, 1000).parallel()
                .map(StreamApi::doubleID) // Функция замедливания
                .sum();
        Long finish = System.currentTimeMillis();
        System.out.println("Время:" + (finish - start));
        System.out.println("Сумма удвоенных числе:" + result);


        // Устанавливаем СВОЕ   количество поток = ForkJoinPool(7) 8ядер - 1 на основной поток программы
        /// Указание Количества активных потоков
        //  ForkJoinPool pool и  ForkJoinTask<Long> task
        Long start1 = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool(20);
        ForkJoinTask<Long> task = pool.submit(() -> LongStream.rangeClosed(1, 1000)
                .parallel()
                .map(StreamApi::doubleID)
                .sum());
        try {
            Long result1 = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Long finish1 = System.currentTimeMillis();
        System.out.println("Время:" + (finish1 - start1));


    }

    public static Long doubleID(Long n) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return n;
    }

}

class Golfer {
    private String first;
    private String last;
    private int score;

    public Golfer(String first, String last, int score) {
        this.first = first;
        this.last = last;
        this.score = score;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "Golfer{" +
                "first='" + first + '\'' +
                ", last='" + last + '\'' +
                ", score=" + score +
                '}';
    }
}


class Person {
    String name;


    Person(String name) {
        this.name = name;

    }

    Person(String... names) {
        this.name = Arrays.stream(names).collect(Collectors.joining(" ")); // Сслепить строки

    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                '}';
    }
}
