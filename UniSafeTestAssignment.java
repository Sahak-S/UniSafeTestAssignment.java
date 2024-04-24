package org.unisafe.pj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UniSafeTestAssignment {

    public static final double kMulX = 7.0843;
    public static final double kMulY = 7.0855;

    /*
     Это тестовое задание на собеседование в UniSafe llc
     На должность Junior Java Developer
     Чтобы задание считалось выполненным необходимо выполнить все 4 пункта
     Нам интересен твой подход к решению и условный % выполнения задания
     Вопросы можно задать тут galaev@team.usafe.ru

     В этом списке (listOfFigures) фигуры записанные координатами
     фигура может быть из 2 или 6 координат, это прямые и кривые, соответственно
     кривые фигуры записываются 5 координатами, потому что последняя 6ая - это первая координата следующей фигуры
     Все фигуры замкнуты
     List< ... > - список больших фигур
     List<List< ... > - список элементов одной фигуры
     List<List<List<Integer>>> - координаты элемента фигуры

     фигуры отправляются на плоттер и вырезаются на защитной пленке
     координаты для реза идут в том порядке, что и в списке
     соответственно есть начало реза по координатам и направление этого реза
     плоттер не всегда дорезает фигуры до конца, поэтому нужно всегда повторять первый элемент фигуры последним
     todo 1: напиши функцию чтобы добавлять первый элемент фигуры последним к каждой фигуре
     при вырезе мелкие фигуры могут задевать большие, поэтому порядок реза важен
     todo 2: напиши функцию чтобы изменить порядок реза фигур от самой маленькой к самой большой
     чтобы повысить качество реза нужно проделать несколько шагов
     чтобы нож не создавал брак пока разворачивает лезвие на большой градус,
     нужно чтобы все фигуры вырезались по часовой (изначально они случайны)
     todo 3: напиши функцию, которая разворачивает все фигуры по часовой меняя координаты местами
     у плоттеров есть особенность, нож которым вырезаются фигуры имеет направление
     todo 4: напиши функцию, которая меняет начало реза этой фигуры в направлении окончания реза предыдущей
     Например круг состоит из 5 кривых, это 4 четверти круга + 1 первая которую мы добавили в конце (чтобы круг хорошо прорезался).
     Если начало реза прошлого круга было справа относительно центра круга, то рез закончится через 5 четвертей, т.е. снизу.
     Таким образом последний элемент фигуры это кривая справа-вниз по часовой.
     После нее лезвие ножа направлено на лево.
     Соответственно, следующая фигура после этого круга должна начинаться в направлении лево.
    */

    public static void main(String[] args) {

        // todo замени путь к файлу
        String filePath = "C:\\Users\\User\\Desktop\\unisafeTest\\camera_block.eps";
       // String filePath = "C:\\Users\\User\\Desktop\\unisafeTest\\14_pro_test.eps";
       // String filePath = "C:\\Users\\User\\Desktop\\unisafeTest\\debug_block.eps";


        List<List<List<Integer>>> listOfFigures = getFromEps(filePath);
        ShowList(listOfFigures);
        System.out.println("------------------------------");


        // Применяем функции для обработки фигур
        addFirstElementAsLast(listOfFigures); // todo 1
        sortFiguresBySize(listOfFigures); // todo 2
        rotateClockwise(listOfFigures); // todo 3
        adjustCuttingDirection(listOfFigures); // todo 4

        // Выводим результат
        for (List<List<Integer>> figure : listOfFigures) {
            for (List<Integer> element : figure) {
                System.out.println(element);
            }
            System.out.println("---------------");
        }

    }

    public static void ShowList (List<List<List<Integer>>> listOfFigures) {
        for (List<List<Integer>> listOfFigure : listOfFigures) {
            System.out.println(listOfFigure);
        }
    }

    public static List<List<List<Integer>>> getFromEps(String filePath){
        List<List<List<Integer>>> listOfFigures = new ArrayList<>();

        List<String> blocks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean reachedEndData = false;
            boolean reachedBeginData = false;
            boolean blockStarted = false;

            while ((line = reader.readLine()) != null) {
                if (!reachedBeginData) {
                    if (line.trim().startsWith("%%EndPageSetup")) {
                        reachedBeginData = true;
                    }
                } else if (!reachedEndData) {

                    if (line.startsWith("%ADO")) {
                        reachedEndData = true;
                    } else {
                        if(line.contains("mo") && Character.isDigit(line.charAt(0))){
                            listOfFigures.add(new ArrayList<>());
                            blockStarted = true;
                            blocks.add(line);
                        } else if (line.contains("m") && Character.isDigit(line.charAt(0))) {
                            listOfFigures.add(new ArrayList<>());
                            blockStarted = true;
                            blocks.add(line);
                        } else if (line.trim().equals("cp") || line.trim().equals("@c") || line.trim().equals("@")) {
                            blockStarted = false;
                        } else if (blockStarted) {
                            blocks.add(line);
                        }
                    }
                } else {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading EPS file: " + e.getMessage());
            return new ArrayList<>();
        }

        int current_figure = -1;
        for (String block : blocks) {
            String[] line_parts = block.split(" ");

            if (Objects.equals(line_parts[line_parts.length - 1], "mo") || Objects.equals(line_parts[line_parts.length - 1], "m")) {
                List<Integer> listN = new ArrayList<>();
                current_figure++;
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            } else if (Objects.equals(line_parts[line_parts.length - 1], "li")) {
                List<Integer> listN = new ArrayList<>();
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            } else if (Objects.equals(line_parts[line_parts.length - 1], "cv") || Objects.equals(line_parts[line_parts.length - 1], "C")) {
                List<Integer> listN = new ArrayList<>();
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            }
        }

        removeEmptyLists(listOfFigures);
        removeNotCycledFigures(listOfFigures);

        return listOfFigures;
    }

    public static void removeEmptyLists(List<List<List<Integer>>> listOfFigures) {
        listOfFigures.removeIf(List::isEmpty);
    }
    public static void removeNotCycledFigures(List<List<List<Integer>>> listOfFigures) {
        Iterator<List<List<Integer>>> iterator = listOfFigures.iterator();
        while (iterator.hasNext()) {
            List<List<Integer>> listOfFigure = iterator.next();
            int last_x = listOfFigure.get(listOfFigure.size() - 1).get(listOfFigure.get(listOfFigure.size() - 1).size() - 2);
            int last_y = listOfFigure.get(listOfFigure.size() - 1).get(listOfFigure.get(listOfFigure.size() - 1).size() - 1);
            int first_x = listOfFigure.get(0).get(0);
            int first_y = listOfFigure.get(0).get(1);
            if (first_x != last_x || first_y != last_y) {
                iterator.remove();
            }
        }
    }

    private static void getNumericalWithDot(int current_figure, List<List<List<Integer>>> listOfFigures, String[] line_parts, List<Integer> listN) {
        for (int j = 0; j < line_parts.length - 1; j++) {
            if (line_parts[j].startsWith(".")) {
                line_parts[j] = "0" + line_parts[j];
            }
            double calk;
            if (j % 2 != 0) {
                calk = (Double.parseDouble(line_parts[j]) + 1.5) * kMulX;
            } else {
                calk = (Double.parseDouble(line_parts[j]) + 1.5) * kMulY;
            }
            int this_int = (int) Math.round(calk);
            listN.add(this_int);
        }
        listOfFigures.get(current_figure).add(listN);
    }



//

    // todo 1: функция для добавления первого элемента фигуры последним
    public static void addFirstElementAsLast(List<List<List<Integer>>> figure) {
        for (List<List<Integer>> element : figure) {
            List<Integer> firstElement = element.get(0);
            element.add(firstElement); // Добавляем первый элемент в конец
        }
    }

    // todo 2: функция для изменения порядка реза фигур от самой маленькой к самой большой
    public static void sortFiguresBySize(List<List<List<Integer>>> listOfFigures) {
        Collections.sort(listOfFigures, Comparator.comparingInt(List::size));
    }

    // todo 3: функция для разворота всех фигур по часовой стрелке
    public static void rotateClockwise(List<List<List<Integer>>> listOfFigures) {
        for (List<List<Integer>> figure : listOfFigures) {
            Collections.reverse(figure); // Разворачиваем список координат
        }
    }

    // todo 4: функция для изменения начала реза в направлении окончания реза предыдущей фигуры
    public static void adjustCuttingDirection(List<List<List<Integer>>> listOfFigures) {
        // Начинаем с первой фигуры
        List<List<Integer>> prevFigure = listOfFigures.get(0);
        // Проходим по остальным фигурам
        for (int i = 1; i < listOfFigures.size(); i++) {
            List<List<Integer>> currentFigure = listOfFigures.get(i);
            // Находим координаты окончания реза предыдущей фигуры
            List<Integer> endOfPrevCut = prevFigure.get(prevFigure.size() - 1);
            // Находим начало реза текущей фигуры
            List<Integer> startOfCurrentCut = currentFigure.get(0);
            // Меняем начало реза текущей фигуры в направлении окончания реза предыдущей
            startOfCurrentCut.set(0, endOfPrevCut.get(0)); // Устанавливаем новую координату x
            startOfCurrentCut.set(1, endOfPrevCut.get(1)); // Устанавливаем новую координату y
            // Обновляем предыдущую фигуру для следующей итерации
            prevFigure = currentFigure;
        }
    }


}
