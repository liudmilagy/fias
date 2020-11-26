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
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ImportServiceImpl implements ru.sibdigital.fias.service.ImportService {

    private final static Logger fiasLogger = LoggerFactory.getLogger("fiasLogger");

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("./xsds/example/")
    private String fiasPath;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    public String importData(File file) {
        try {
            importFiasData(file);
            return ("Загрузка окончена.");
        }
        catch (Exception e) {
            fiasLogger.info("Не удалось загрузить файл.");
            e.printStackTrace();
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
                    fiasLogger.error(e.getMessage());
                }
            }
        } else {
            fiasLogger.error("Файлы не найдены по пути " + fiasPath);
        }
        fiasLogger.info("Импорт тестовых данных ФИАС окончен");
    }

    /**
     * Поиск имени таблицы в БД по имени файла и имени узла
     */
    private String findTableName(String nodeName, String filename) {
        String tableName = nodeName.toLowerCase();

        String subFileName = filename.substring(4, filename.indexOf("_2")).toLowerCase();
        switch (tableName) {
            case ("item"):
                if (subFileName.equals("address_objects_division")) {
                    tableName = "addr_obj_division_item";
                }
                else {
                    tableName = subFileName + "_item";
                }
                break;
            case ("param"):
                subFileName = subFileName.substring(0, subFileName.indexOf("_params")).toLowerCase();
                if (subFileName.equals("address_objects_params")) {
                    tableName = "addr_obj_param";
                }
                else {
                    tableName = subFileName + "_param";
                }
                break;
            case ("object"):
                if (subFileName.equals("address_objects")) {
                    tableName = "addr_object";
                }
                else {
                    subFileName = subFileName.substring(0, subFileName.indexOf("_obj")).toLowerCase();
                    tableName = subFileName + "_object";
                }
                break;
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
            case ("reestr_object"):
                primaryKey = "objectid";
                break;
            default:
                primaryKey =  "id";
                break;
        }
        return primaryKey;
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

    /**
     * Получаем все имена колонок существующей таблицы
     */
    private Set<String> getTableColumnNames(String tablename) throws SQLException {
        Set<String> set = new HashSet<String>();

        Connection connection = DriverManager.getConnection(url, username, password);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM "+ tablename);
        ResultSetMetaData metadata = resultSet.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            String columnName = metadata.getColumnName(i);
            set.add(columnName);
        }

        return set;
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

    private String generateInsertQuery(Node node, String tableName, String primaryKeyName, Set<String> columnNames) {
        String query = "";

        if (node.hasAttributes()) {
            NamedNodeMap nodeMap = node.getAttributes();

            String queryParams = "";
            String queryValues = "";
            String queryUpsert = "";

            for (int k = 0; k < nodeMap.getLength(); k++) {
                Node attribute = nodeMap.item(k);
                String key = attribute.getNodeName().toLowerCase();
                String value = attribute.getNodeValue();
                if (columnNames.contains(key)) {
                    queryParams += "\"" + key + "\",";
                    queryValues += "'" + value + "',";
                    queryUpsert += (key.equals("desc") ? "\"desc\"" : key) + " = EXCLUDED." + key + ",";
                }
                else {
                    fiasLogger.error("Не найдена колонка " + key + " в таблице " + tableName);
                }
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
                fiasLogger.error("Не удалось прочитать xml-файл из zip-файла");
                e.printStackTrace();
            }
            catch (ParserConfigurationException e) {
                fiasLogger.error("Не удалось распарсить xml-файл");
                e.printStackTrace();
            }
            catch (SAXException e) {
                fiasLogger.error("Не удалось распарсить xml-файл");
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
            if (root.getChildNodes().getLength() > 0) {
                Node firstNode = root.getChildNodes().item(0);
                // Найдем 1 раз наим-е таблицы, имя primary key, имена колонок таблицы
                String nodeName = firstNode.getNodeName();
                String tableName = findTableName(nodeName, filename);
                String primaryKeyName = getPrimaryKeyName(tableName);

                Set<String> columnNames = new HashSet<>();
                try {
                    columnNames = getTableColumnNames(tableName);
                }
                catch (SQLException e) {
                    fiasLogger.error("Не удалось подключиться к БД и получить имена колонок таблицы.");
                    e.printStackTrace();
                }
                catch (Exception e) {
                    fiasLogger.error("Не удалось получить имена колонок таблицы.");
                    e.printStackTrace();
                }
                Set<String> finalColumnNames = columnNames;

                fiasLogger.info("Создание и выполнение inserts");


                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        for (int j = 0; j < root.getChildNodes().getLength(); j++) {
                            Node node = root.getChildNodes().item(j);
                            String insertQuery = generateInsertQuery(node, tableName, primaryKeyName, finalColumnNames);
                            jdbcTemplate.update(insertQuery);
                        }
                    }
                });
            }

        }
        fiasLogger.info("Обработка файла " + filename + " закончена");
    }

}
