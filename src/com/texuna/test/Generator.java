package com.texuna.test;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Генератор текстовых отчетов
 * принимает 3 аргумента:
 * 1 аргумент - файл настроек исходных данных (.xml);
 * 2 аргумент - файл, содержащий исходные данные, разделенные табуляцией в кодировке UTF-16 (.tsv);
 * 3 аргумент - файл для вывода отчета (.txt)
 */
public class Generator {

    static int pageHeight;
    static int pageWidth;
    static ArrayList<String> columnTitles = new ArrayList<>();
    static ArrayList<Integer> columnWidths = new ArrayList<>();
    static String pageSeparator = "~";
    static String columnSeparator = "|";
    static String lineSeparator = "-";
    static int lineCounter = 0;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        String settingsFileName = args[0];
        String srcDataFileName = args[1];
        String reportFileName = args[2];

        // получаем параметры страницы
        File settingsFile = new File(settingsFileName);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(settingsFile);
        Element rootEl = document.getDocumentElement();
        Element pageElement = (Element) rootEl.getElementsByTagName("page").item(0);
        pageWidth = Integer.parseInt(pageElement.getElementsByTagName("width").item(0).getTextContent());
        pageHeight = Integer.parseInt(pageElement.getElementsByTagName("height").item(0).getTextContent());

        //получаем параметры колонок
        Element columnsEl = (Element) rootEl.getElementsByTagName("columns").item(0);
        NodeList columnTitleNodes = columnsEl.getElementsByTagName("title");
        for (int i = 0; i < columnTitleNodes.getLength(); i++) {
            columnTitles.add(i, columnTitleNodes.item(i).getTextContent());
        }
        NodeList columnWidthNodes = columnsEl.getElementsByTagName("width");
        for (int i = 0; i < columnTitleNodes.getLength(); i++) {
            columnWidths.add(i, Integer.parseInt(columnWidthNodes.item(i).getTextContent()));
        }


        // получаем строки из исходного файла и печатаем отчет
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(reportFileName), "UTF-16"));
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(srcDataFileName), "UTF-16"));

        String srcLine;
        while ((srcLine = br.readLine()) != null) {
            String[] srcLineArr = srcLine.split("\\t");
            ArrayList<String> srcLineList = new ArrayList<>(Arrays.asList(srcLineArr));
            bw.write(getReport(srcLineList));
        }

        br.close();
        bw.close();
    }


    /**
     * метод получает список данных из одной строки исходного файла и возвращает отчет для вывода
     * с заголовком и разделителями
     *
     * @param srcStringParts - список данных из одной строки исходного файла
     * @return String - текст отчета
     */
    static String getReport(ArrayList<String> srcStringParts) {
        String report = "";
        int reportLineHeight = 0;

        // если это первая строка на странице, то добавляем в начало отчета заголовок рекурсивным вызовом этого метода
        if (lineCounter == 0 && !srcStringParts.get(0).equals(columnTitles.get(0))) {
            report += getReport(columnTitles);
        }

        // содержимое каждой ячейки разбиваем на список строк, создаем список ячеек из списков строк каждой ячейки
        ArrayList<ArrayList<String>> cellLinesList = new ArrayList<>();

        for (int i = 0; i < srcStringParts.size(); i++) {
            cellLinesList.add(splitString(srcStringParts.get(i), columnWidths.get(i)));
            if (reportLineHeight <= cellLinesList.get(i).size()) {
                // получаем высоту строки отчета по высоте самой большой ячейки в строке
                reportLineHeight = cellLinesList.get(i).size();
            }
        }

        // если отчет не уместится на странице после добавления текущих данных, то переносим данные на новую страницу
        if (lineCounter + reportLineHeight >= pageHeight) {
            report += pageSeparator + "\r\n";
            lineCounter = 0;
            // на новой странице добавляем заголовок
            report += getReport(columnTitles);
        }

        //если это не первая строка данных на странице, то печатаем разделитель строк данных перед данными
        if (lineCounter != 0) {
            String lineSeparatorString = getCompletedString("",pageWidth,lineSeparator);
            report = report.concat(lineSeparatorString).concat("\r\n");
            lineCounter++;
        }

        // составляем строку данных
        for (int i = 0; i < reportLineHeight; i++) {
            report = report.concat(columnSeparator);
            // в первую строку добавляем первые элементы из каждого списка строк ячеек и тд
            for (int j = 0; j < cellLinesList.size(); j++) {
                report = report.concat(" ");
                // печатаем данные в строку ячейки, оставшееся место заполняем пробелами
                if (cellLinesList.get(j).size() > i) {
                    String cellLine = getCompletedString(cellLinesList.get(j).get(i), columnWidths.get(j)," ");
                    report = report.concat(cellLine);
                } else {// если для текущей строки ячейки нет данных, заполняем пробелами всю строку ячейки
                    report = report.concat(getCompletedString("", columnWidths.get(j)," "));
                }
                report = report.concat(" ").concat(columnSeparator);
            }
            report = report.concat("\r\n");
            lineCounter++;
        }
        return report;
    }

    /**
     * дополняет строку нужным символом до необходимой длины
     *
     * @param srcString - исходная строка
     * @param stringLength - необходимая длина строки
     * @param symbol - символ для дополнения строки
     * @return String - дополненная строка
     */
    static String getCompletedString(String srcString, int stringLength, String symbol) {
        String result = srcString;

        for (int i = 0; i < stringLength - srcString.length(); i++) {
            result = result.concat(symbol);
        }
        return result;
    }

    /**
     * метод получает строку, разбивает по длине и возвращает список строк
     *
     * @param srcString    - исходная строка
     * @param stringLength - максимальная длина строк в возвращаемом массиве
     * @return ArrayList - список разбитых строк
     */
    static ArrayList<String> splitString(String srcString, int stringLength) {
        ArrayList<String> result = new ArrayList<>();
        String remainingString = srcString;
        int separatorIndex = 0;

        //проходим по каждому символу в строке
        for (int i = 0; i < remainingString.length(); i++) {

            char currentChar = remainingString.charAt(i);

            //если символ - разделитель, записываем его индекс
            if ((!Character.isLetter(currentChar) && !Character.isDigit(currentChar))) {
                separatorIndex = i;
            }

            if (i == stringLength) {//при достижении максимальной длины строки
                if (separatorIndex == 0) {//если разделителей не было
                    result.add(remainingString.substring(0, i));//добавляем в массив часть слова
                    remainingString = remainingString.substring(i);//вырезаем из исходной строки эту часть
                    i = 0;
                } else {//если были разделители
                    //если разделитель умещается в строку, увеличим индекс, чтобы включить его
                    if (separatorIndex < stringLength) separatorIndex++;

                    result.add(remainingString.substring(0, separatorIndex));
                    //trim`ом уберем пробел, если он перенесся на новую строку
                    remainingString = remainingString.substring(separatorIndex).trim();

                    //обнулим значения, чтобы начать с начала оставшейся строки
                    i = 0;
                    separatorIndex = 0;
                }
            }

            //если это последний символ в оставшейся строке, добавим в массив остаток
            if (i == remainingString.length() - 1) {
                result.add(remainingString);
            }
        }

        return result;
    }
}
