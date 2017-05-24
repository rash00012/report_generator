package com.texuna.test;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

public class Generator {

    public static int pageHeight;
    public static int pageWidth;
    public static ArrayList<String> columnTitles = new ArrayList<>();
    public static ArrayList<Integer> columnWidths = new ArrayList<>();
    public static String pageSeparator = "~";
    public static String columnSeparator = "|";
    public static String lineSeparator = "-";
    public static int lineCounter = 0;

    public static String writeReportLines(String[] srcStringParts) {
        String reportLine="";
        if (lineCounter==0){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pageWidth; i++) {
                sb.append(lineSeparator);
            }
            reportLine += sb.toString()+"\r\n";
        }

        ArrayList[] cellParts = new ArrayList[srcStringParts.length];
        int cellsHeight = 0;

        for (int i = 0; i < srcStringParts.length; i++) {
            cellParts[i] = splitString(srcStringParts[i], columnWidths.get(i));
            if (cellsHeight <= cellParts[i].size()) {
                cellsHeight = cellParts[i].size();
            }
        }

        String whitespaces;

        for (int i = 0; i < cellsHeight; i++) {
            reportLine = columnSeparator;
            for (int j = 0; j < cellParts.length; j++) {
                reportLine += " ";
                if (cellParts[j].size() > i) {
                    String cellPart = cellParts[j].get(i).toString();
                    whitespaces = getWhitespacesString(columnWidths.get(j) - cellPart.length());

                    reportLine += cellPart + whitespaces;
                } else {
                    reportLine += getWhitespacesString(columnWidths.get(j));
                }
                reportLine += " " + columnSeparator;
            }
            reportLine+="\r\n";
            lineCounter++;
        }
        System.out.print(reportLine);
        return reportLine;
    }

    static String getWhitespacesString(int whitespacesCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < whitespacesCount; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * метод получает значение ячейки в виде строки, разбивает по длине и возвращает ArrayList
     *
     * @param srcString    - исходная строка
     * @param stringLength - максимальная длина строк в возвращаемом массиве
     * @return ArrayList
     */
    public static ArrayList<String> splitString(String srcString, int stringLength) {
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

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        String settingsFileName = args[0];
        String srcDataFileName = args[1];
        String reportFileName = args[2];

        // getting page settings
        File settingsFile = new File(settingsFileName);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(settingsFile);
        Element rootEl = document.getDocumentElement();
        Element pageElement = (Element) rootEl.getElementsByTagName("page").item(0);
        pageWidth = Integer.parseInt(pageElement.getElementsByTagName("width").item(0).getTextContent());
        pageHeight = Integer.parseInt(pageElement.getElementsByTagName("height").item(0).getTextContent());

        //getting column settings
        Element columnsEl = (Element) rootEl.getElementsByTagName("columns").item(0);
        NodeList columnTitleNodes = columnsEl.getElementsByTagName("title");
        for (int i = 0; i < columnTitleNodes.getLength(); i++) {
            columnTitles.add(i, columnTitleNodes.item(i).getTextContent());
        }
        NodeList columnWidthNodes = columnsEl.getElementsByTagName("width");
        for (int i = 0; i < columnTitleNodes.getLength(); i++) {
            columnWidths.add(i, Integer.parseInt(columnWidthNodes.item(i).getTextContent()));
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(reportFileName), "UTF-16"));
//        String header
//        String pageHeader = columnSeparator+" "+columnTitles.get(0)+;
//        bw.write(columnSeparator);
        bw.close();

        String[] arr = new String[3];
        arr[0] = "1";
        arr[1] = "29/11/2009";
        arr[2] = "Юлианна-Оксана Сухово-Кобылина";
        writeReportLines(arr);
    }
}
