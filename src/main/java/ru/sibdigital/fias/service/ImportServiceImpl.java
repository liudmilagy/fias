package ru.sibdigital.fias.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Service
public class ImportServiceImpl implements ru.sibdigital.fias.service.ImportService {

    private final static Logger fiasLogger = LoggerFactory.getLogger("fiasLogger");

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("./xsds/example/")
    private String fiasPath;

    public String importData(File file) {
        try {
            importFiasData(file);
            return ("Загрузка окончена.");
        }
        catch (Exception e) {
            System.out.println(e);
            return ("Ошибка. "+ e.getMessage());
        }
    }

    public void importTestData() {
        try {
            importFiasTestData();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Метод импорта данных ФИАС
     */

    private void importFiasData(File file) {
        fiasLogger.info("Импорт ФИАС начат");

        try {
            processZipFiasFile(file);
        }
        catch (Exception e ) {
            System.out.println(e);
        }


        fiasLogger.info("Импорт ФИАС окончен");
    }

    private void importFiasTestData() {
        fiasLogger.info("Импорт тестовых данных ФИАС начат");

        Collection<File> zipFiles = null;
        try {
            zipFiles = FileUtils.listFiles(new File(fiasPath),
                    new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        } catch (Exception e) {
            fiasLogger.info("Не удалось получить доступ к " + fiasPath);
            e.printStackTrace();
        }

        if (zipFiles != null && !zipFiles.isEmpty()) {
            for (File zipFile : zipFiles) {
                try {
                    processZipFiasFile(zipFile);
                }
                catch (Exception e ) {
                    System.out.println(e);
                }
            }
        } else {
            fiasLogger.info("Файлы не найдены по пути " + fiasPath);
        }
        fiasLogger.info("Импорт тестовых данных ФИАС окончен");
    }

    /**
     * Поиск имени таблицы в БД по имени файла и имени узла
     */
    private String findTableName(String nodeName, String filename) {
        String tableName = nodeName.toLowerCase();
        if (tableName.equals("item")) {
            String subFileName = filename.substring(4, filename.indexOf("_2")).toLowerCase();
            if (subFileName.equals("address_objects_division")) {
                tableName = "addr_obj_division_item";
            }
            else {
                tableName = subFileName + "_item";
            }
        }
        return tableName;
    }

    /**
     * Метод получения имени ключа таблицы
     */
    private String getPrimaryKeyName(String tableName) {
        String primaryKey;
        switch (tableName) {
            case ("change_history_item"):
                primaryKey = "changeid";
                break;
            case ("objectlevel"):
                primaryKey = "level";
                break;
            default:
                primaryKey =  "id";
                break;
        }
        return primaryKey;
    }

    /**
     * Метод генерации SQL query Insert
     */
    private String generateInsertQuery(Node node, String filename) {
        String query = "";
        String nodeName = node.getNodeName();
        String tableName = findTableName(nodeName, filename);
        String primaryKeyName = getPrimaryKeyName(tableName);
        if (node.hasAttributes()) {
            NamedNodeMap nodeMap = node.getAttributes();

            String queryParams = "";
            String queryValues = "";
            String queryUpsert = "";

            for (int k = 0; k < nodeMap.getLength(); k++) {
                Node attribute = nodeMap.item(k);
                String key = attribute.getNodeName().toLowerCase();
                String value = attribute.getNodeValue();
                queryParams += "\"" + key + "\",";
                queryValues += "'" + value + "',";
                queryUpsert += (key.equals("desc") ? "\"desc\"" : key) + " = EXCLUDED." + key + ",";
            }

            // Удаляем посл. запятые
            queryParams = queryParams.substring(0, queryParams.length() - 1);
            queryValues = queryValues.substring(0, queryValues.length() - 1);
            queryUpsert = queryUpsert.substring(0, queryUpsert.length() - 1);

            query = "INSERT INTO " + tableName + "(" + queryParams + ")" +
                    " VALUES(" + queryValues +") ON CONFLICT (" + primaryKeyName + ") DO UPDATE SET " + queryUpsert + ";";
        }

        return query;
    }

    private static ZipFile getZipFile(File file) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zipFile;
    }


    private void processZipFiasFile(File file) {
        ZipFile zipFile = getZipFile(file);
        if (zipFile != null) {
            try {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    String zipEntryName = zipEntry.getName().toLowerCase();
                    fiasLogger.info("Обход файлов. Папка/файл: " + zipEntryName);
                    if (zipEntryName.length() > 5 && zipEntryName.substring(zipEntryName.length()-4).equals(".xml")) {
                        InputStream is = zipFile.getInputStream(zipEntry);
                        String filename = zipEntryName;
                        if (zipEntryName.lastIndexOf('/') > 0) {
                            filename = zipEntryName.substring(zipEntryName.lastIndexOf('/'));
                        }
                        processFiasFileByDOM(is, filename);
                    }
                }
            } catch (IOException e) {
                fiasLogger.info("Не удалось прочитать xml-файл из zip-файла");
                e.printStackTrace();
            }
            catch (ParserConfigurationException e) {
                fiasLogger.info("Не удалось распарсить xml-файл");
                e.printStackTrace();
            }
            catch (SAXException e) {
                fiasLogger.info("Не удалось распарсить xml-файл");
                e.printStackTrace();
            }
            finally {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Метод обработки по узлам Xml-файла с помощью DOM
     */
    private void processFiasFileByDOM(InputStream is, String filename) throws ParserConfigurationException, IOException, SAXException {
        fiasLogger.info("Обработка файла " + filename);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();

        for (int i = 0; i < doc.getChildNodes().getLength(); i++) {
            Node root = doc.getChildNodes().item(i);
            fiasLogger.info("Создание и выполнение inserts");

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    for (int j = 0; j < root.getChildNodes().getLength(); j++) {
                        Node node = root.getChildNodes().item(j);
                        String insertQuery = generateInsertQuery(node, filename);
                        jdbcTemplate.update(insertQuery);
                    }
                }
            });
        }
        fiasLogger.info("Обработка файла " + filename + " закончена");
    }

}
