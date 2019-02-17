package com.longlong.excel.exporter;

import com.longlong.excel.field.ExcelField;
import com.longlong.excel.field.ExcelFieldEx;
import com.longlong.excel.util.Reflections;
import com.longlong.base.DataTaskException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;

/**
 * 导出Excel文件（导出“XLSX”格式，支持大数据量导出 @see org.apache.poi.ss.SpreadsheetVersion）
 *
 * @author liaolonglong
 * @version 2013-04-21
 */
public abstract class AbstractExportExcel<THIS extends AbstractExportExcel<THIS, EF>, EF> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 工作薄对象
     */
    private Workbook wb;

    /**
     * 文件名
     */
    private String fileName;
    /**
     * 标志是否不要title<br/>
     * 默认有title
     */
    private boolean noTitle = false;

    /**
     * 样式列表
     */
    protected Map<String, CellStyle> styles;

    /**
     * 当前行号
     */
    protected int rownum;

    /**
     * 表格标题，传“空值”，表示无标题
     */
    protected String title;

    /**
     * 冻结列的个数
     */
    protected int freezeCount;
    /**
     * 表格条件
     */
    protected Object condition;

    /**
     * 占位列
     */
    protected int placeHolderColumn = -1;

    /**
     * 导出excel的版本 2003为EXCEL97对象 2007及以上为EXCEL2007对象 默认为2007
     */
    private SpreadsheetVersion excelVersion = SpreadsheetVersion.EXCEL2007;

    protected Sheet currentSheet;

    private int dataListSize;
    /**
     * 当前工作表
     */
    protected Sheet sheet;

    protected List<String> existsSheet = new ArrayList<>();
    /**
     * 数据的表头
     */
    protected List<String> headerList = new ArrayList<>();

    /**
     * 全局设置的属性
     */
    protected ExcelField globalField;
    protected EF dataGlobalField;

    /**
     * 存储导数据为map时的key值
     */
    protected List<String> dataKey;

    protected Map<String, List<String>> dataKeyMap;

    protected Map<String, LinkedList<String>> headMap;

    protected Row headerRow = null;

    /**
     * 统计工作簿的sheet数
     */
    private volatile int countSheetNum = 1;

    /**
     * 使用模板
     */
    protected boolean useTemplate = false;
    /**
     * 自定义每个Sheet需要填充数据的开始位置
     */
    protected Map<String, Integer> useModelDataStart;
    /**
     * 定义所有sheet填充数据的属性与单元格类型
     */
    protected Map<String, List<ExcelFieldEx>> replaceDataFields;
    /**
     * 定义当前sheet填充数据的属性与单元格类型
     */
    protected ThreadLocal<List<ExcelFieldEx>> replaceDataField = new ThreadLocal<>();

    public AbstractExportExcel() {
    }

    public AbstractExportExcel(File srcFile, int start) throws DataTaskException {
        initUseModel(srcFile);
        rownum = start;
    }

    public AbstractExportExcel(File srcFile, Map<String, Integer> useModelDataStart) throws DataTaskException {
        initUseModel(srcFile);
        this.useModelDataStart = useModelDataStart;
    }

    private void initUseModel(File srcFile) throws DataTaskException {
        try {
            this.wb = new XSSFWorkbook(new FileInputStream(srcFile));
            useTemplate = true;
        } catch (Exception e) {
            throw new DataTaskException("初始化工作簿失败", e);
        }
    }

    /**
     * 初始化函数
     *
     * @param title        表格标题，传“空值”，表示无标题
     * @param condition    导出条件对象
     * @param headerList   表头列表
     * @param freezeCount  冻结列数
     * @param excelVersion 导出excel的版本 2003为EXCEL97对象 2007及以上为EXCEL2007对象
     */
    protected void initialize(String title, Object condition, List<String> headerList, int freezeCount, SpreadsheetVersion excelVersion) {
        this.title = title;
        this.condition = condition;
        this.headerList = headerList;
        this.freezeCount = freezeCount;

        if (null != excelVersion) {
            this.setExcelVersion(excelVersion);
        }
        // 创建工作簿对象
        if (this.getExcelVersion() == SpreadsheetVersion.EXCEL97) {
            this.wb = new HSSFWorkbook();
        } else {
            this.wb = new SXSSFWorkbook(100);
            // 生成的临时文件将进行gzip压缩
            ((SXSSFWorkbook) wb).setCompressTempFiles(true);
        }
        log.debug("Initialize success.");
    }

    /**
     * 创建工作表
     *
     * @param sheetName  工作表名称
     * @param headerList
     * @return
     */
    private Sheet createSheet(String sheetName, List<String> headerList) {
        Sheet sheet = wb.createSheet(sheetName);
        if (headerList == null)
            headerList = this.headerList;
        rownum = 0;
        this.currentSheet = sheet;
        this.styles = createStyles(wb);
        // Create title
        if (!noTitle && StringUtils.isNotBlank(title)) {
            Row titleRow = sheet.createRow(rownum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(styles.get("title"));
            titleCell.setCellValue(title);
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), titleRow.getRowNum(), headerList.size() > 0 ? (headerList.size() - 1) : 0));
        }
        // 导出条件
        if (condition != null) {
            Class<? extends Object> conCls = condition.getClass();
            globalField = conCls.getAnnotation(ExcelField.class);
            Field[] conFields = conCls.getDeclaredFields();
            List<Object[]> conAnnotationList = new ArrayList<>();
            for (Field conField : conFields) {
                if (conField.getAnnotation(ExcelField.class) != null) {
                    conAnnotationList.add(new Object[]{conField.getAnnotation(ExcelField.class), conField});
                }
            }
            Method[] methods = conCls.getMethods();
            for (Method method : methods) {
                if (method.getAnnotation(ExcelField.class) != null) {
                    Field field = null;
                    try {
                        field = conCls.getDeclaredField(((char) (method.getName().charAt(3) + 32)) + method.getName().substring(4));
                    } catch (NoSuchFieldException | SecurityException e) {
                        e.printStackTrace();
                    }
                    conAnnotationList.add(new Object[]{method.getAnnotation(ExcelField.class), field});
                }
            }
            if (null != globalField) {
                if (!globalField.isSortByField()) {
                    Collections.sort(conAnnotationList, new Comparator<Object[]>() {
                        @Override
                        public int compare(Object[] o1, Object[] o2) {
                            return new Integer(((ExcelField) o1[0]).sort()).compareTo(new Integer(((ExcelField) o2[0]).sort()));
                        }
                    });
                }
                int rows = globalField.rows();
                if (rows == 1) {
                    rows = conAnnotationList.size() / globalField.cols();
                    if (conAnnotationList.size() % globalField.cols() > 0)
                        rows++;
                }
                int merges = headerList.size() / (globalField.cols() * 2) - 1;
                if (merges < 0)
                    merges = 0;
                for (int i = 0; i < rows; i++) {
                    Row conRow = sheet.createRow(rownum++);
                    conRow.setRowStyle(styles.get("data"));
                    if (globalField != null && globalField.isFreeze())
                        sheet.createFreezePane(freezeCount, rownum);
                    conRow.setHeightInPoints(16);
                    int cellIndex = 0, annoIndex = 0;
                    for (int j = 0; j < globalField.cols(); j++) {
                        if ((annoIndex = i * globalField.cols() + j) < conAnnotationList.size()) {
                            Object[] anno = conAnnotationList.get(annoIndex);
                            ExcelField labelField = (ExcelField) anno[0];
                            addCell(conRow, cellIndex, labelField.title(), 3, String.class);
                            sheet.addMergedRegion(new CellRangeAddress(conRow.getRowNum(), conRow.getRowNum(), cellIndex, cellIndex + merges));
                            cellIndex += 1 + merges;
                            // Reflections.getFieldValue(condition, ((Field) anno[1]).getName());
                            Object fieldValue = Reflections.invokeGetter(condition, ((Field) anno[1]).getName());
                            if (labelField.valueConvert().length > 0 && fieldValue != null) {
                                for (String vc : labelField.valueConvert()) {
                                    String[] convertValue = vc.split(":");
                                    String val = convertValue[0].trim();
                                    if (val.equals(fieldValue.toString())) {
                                        fieldValue = convertValue[1];
                                    }
                                }
                            }
                            addCell(conRow, cellIndex, fieldValue, 1, ((Field) anno[1]).getType());
                            sheet.addMergedRegion(new CellRangeAddress(conRow.getRowNum(), conRow.getRowNum(), cellIndex, cellIndex + merges));
                            cellIndex += merges + 1;
                        } else {
                            break;
                        }
                    }
                }
            }
            Row tmpRow = sheet.createRow(rownum++);
            sheet.addMergedRegion(new CellRangeAddress(tmpRow.getRowNum(), tmpRow.getRowNum(), 0, headerList.size() - 1));
        }
        // Create header
        if (headerList == null) {
            throw new RuntimeException("headerList not null!");
        }
        createSheetEx(this.currentSheet, headerList);
        for (int i = 0; i < headerList.size(); i++) {
            int colWidth = sheet.getColumnWidth(i) * 2;
            sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);
        }
        return sheet;
    }

    protected abstract void createSheetEx(Sheet sheet, List<String> headerList);

    /**
     * 创建表格样式
     *
     * @param wb 工作薄对象
     * @return 样式列表
     */
    private Map<String, CellStyle> createStyles(Workbook wb) {
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();

        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        Font titleFont = wb.createFont();
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style.setFont(titleFont);
        styles.put("title", style);

        style = wb.createCellStyle();
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        Font dataFont = wb.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        style.setFont(dataFont);
        styles.put("data", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(CellStyle.ALIGN_LEFT);
        styles.put("data1", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(CellStyle.ALIGN_CENTER);
        styles.put("data2", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        styles.put("data3", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        // style.setWrapText(true);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font headerFont = wb.createFont();
        headerFont.setFontName("Arial");
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(headerFont);
        styles.put("header", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        // style.setWrapText(true);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font highLightheaderFont = wb.createFont();
        highLightheaderFont.setFontName("Arial");
        highLightheaderFont.setFontHeightInPoints((short) 10);
        highLightheaderFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        highLightheaderFont.setColor(IndexedColors.RED.getIndex());
        style.setFont(highLightheaderFont);
        styles.put("highLightHeader", style);

        return styles;
    }

    /**
     * 设置工作表需要替换的值
     *
     * @param replaceData
     * @return
     */
    public THIS setReplaceData(Map<String, String> replaceData) {
        String preffix = "#", suffix = "#";
        for (int i = 0; i < this.wb.getNumberOfSheets(); i++) {
            Sheet temp = this.wb.getSheetAt(i);
            for (int j = 0; j < temp.getLastRowNum(); j++) {
                Iterator<Cell> cellIterator = temp.getRow(j).iterator();
                for (; cellIterator.hasNext(); ) {
                    Cell cell = cellIterator.next();
                    if (cell.getCellType() == Cell.CELL_TYPE_STRING || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                        replaceData.forEach((key, value) -> {
                            if (cell.getStringCellValue().contains(preffix + key + suffix)) {
                                cell.setCellValue(cell.getStringCellValue().replaceAll(preffix + key + suffix, value));
                            }
                        });
                    }
                }
            }
        }
        return getThis();
    }

    /**
     * @param rowNumber
     * @param column
     * @param val
     * @param align
     */
    public void addCell(int rowNumber, int column, Object[] val, int align) {
        Row row = sheet.createRow(rowNumber);
        for (int i = 0; i < val.length; i++) {
            this.addCell(row, column + i, val[i], align, val[i].getClass());
        }
    }

    /**
     * 导出列占位
     *
     * @param column
     */
    public THIS setPlaceHoderColumn(int column) {
        this.placeHolderColumn = column;
        return getThis();
    }

    public void appendDataList(int column, String title, String value) {
        headerRow.createCell(column);
        Cell titleCell = currentSheet.getRow(headerRow.getRowNum() - 1).createCell(column);
        titleCell.setCellStyle(styles.get("header"));
        titleCell.setCellValue(title);
        currentSheet.addMergedRegion(new CellRangeAddress(headerRow.getRowNum() - 1, headerRow.getRowNum(), column, column));
        Row dataRow = currentSheet.getRow(headerRow.getRowNum() + 1);
        Cell dataCell = dataRow.createCell(column);
        dataCell.setCellStyle(styles.get("data2"));
        dataCell.setCellValue(value);
        currentSheet.addMergedRegion(new CellRangeAddress(headerRow.getRowNum() + 1, headerRow.getRowNum() + dataListSize, column, column));
    }

    /**
     * 添加一个单元格
     *
     * @param row    添加的行
     * @param column 添加列号
     * @param val    添加值
     * @return 单元格对象
     */
    public Cell addCell(Row row, int column, Object val) {
        return this.addCell(row, column, val, 0, Class.class);
    }

    /**
     * 添加一个单元格
     *
     * @param row    添加的行
     * @param column 添加列号
     * @param val    添加值
     * @param align  对齐方式（1：靠左；2：居中；3：靠右）
     * @return 单元格对象
     */
    public Cell addCell(Row row, int column, Object val, int align, Class<?> fieldType) {
        Cell cell = row.createCell(column);
        String cellFormatString = "@";
        try {
            if (val == null) {
                cell.setCellValue("");
            } else if (fieldType != Class.class) {
                cell.setCellValue((String) fieldType.getMethod("setValue", Object.class).invoke(null, val));
            } else {
                if (val instanceof String) {
                    cell.setCellValue((String) val);
                } else if (val instanceof Integer) {
                    cell.setCellValue((Integer) val);
                    cellFormatString = "0";
                } else if (val instanceof Long) {
                    cell.setCellValue((Long) val);
                    cellFormatString = "0";
                } else if (val instanceof Double) {
                    cell.setCellValue((Double) val);
                    cellFormatString = "0.00";
                } else if (val instanceof Float) {
                    cell.setCellValue((Float) val);
                    cellFormatString = "0.00";
                } else if (val instanceof Date) {
                    cell.setCellValue((Date) val);
                    cellFormatString = "yyyy-MM-dd HH:mm";
                } else {
                    cell.setCellValue((String) Class.forName(this.getClass().getName().replaceAll(this.getClass().getSimpleName(), "fieldtype." + val.getClass().getSimpleName() + "Type"))
                            .getMethod("setValue", Object.class).invoke(null, val));
                }
            }
        } catch (Exception ex) {
            // log.info("Set cell value [" + row.getRowNum() + "," + column + "]
            // error: " + ex.toString());
            cell.setCellValue(val.toString());
        }
        // if (val != null) {
        CellStyle colStyle = styles.get("data_column_" + column);
        if (colStyle == null) {
            colStyle = wb.createCellStyle();
            colStyle.cloneStyleFrom(styles.get("data" + (align >= 1 && align <= 3 ? align : "")));
            colStyle.setDataFormat(wb.createDataFormat().getFormat(cellFormatString));
            styles.put("data_column_" + align, colStyle);
        }
        cell.setCellStyle(colStyle);

        // }
        return cell;
    }

    /**
     * 添加数据（通过annotation.ExportField添加数据）
     *
     * @param list 数据列表
     * @return 当前对象
     */
    public <E> THIS setDataList(List<E> list) {
        return setDataList(list, "Export");
    }

    public <E> THIS setDataList(List<E> list, String key) {
        return setDataList(list, "Export", null, null);
    }

    public Map<String, List<ExcelFieldEx>> getReplaceDataFields() {
        return replaceDataFields;
    }

    public THIS setReplaceDataFields(Map<String, List<ExcelFieldEx>> dataFields) {
        this.replaceDataFields = dataFields;
        return getThis();
    }

    public THIS addReplaceDataFields(String sheetName, ExcelFieldEx dataField) {
        if (this.replaceDataFields == null)
            this.replaceDataFields = new HashMap<>();
        if (this.replaceDataFields.get(sheetName) == null)
            this.replaceDataFields.put(sheetName, new ArrayList<>());
        this.replaceDataFields.get(sheetName).add(dataField);
        return getThis();
    }

    public List<ExcelFieldEx> getReplaceDataField() {
        return replaceDataField.get();
    }

    public THIS setReplaceDataField(List<ExcelFieldEx> dataField) {
        this.replaceDataField.set(dataField);
        return getThis();
    }

    public THIS addReplaceDataField(ExcelFieldEx dataField) {
        if (this.replaceDataField.get() == null)
            this.replaceDataField.set(new ArrayList<>());
        this.replaceDataField.get().add(dataField);
        return getThis();
    }


    /**
     * 添加数据（通过annotation.ExportField添加数据）
     *
     * @param list      数据列表
     * @param sheetName 工作表名称
     * @return 当前对象
     */
    public <E> THIS setDataList(List<E> list, String sheetName, List<String> dataKey, List<String> headerList) {
        if (useTemplate) {
            sheet = wb.getSheet(sheetName);
            if (list.size() > 0) {
                if (null == replaceDataFields && null == replaceDataField.get()) {
                    // initAnnotation(list.get(0).getClass(), 1);
                }
                if (useModelDataStart != null)
                    rownum = useModelDataStart.get(sheetName);
            }
        } else {
            if (!existsSheet.contains(sheetName)) {
                sheet = createSheet(sheetName, headerList);
                existsSheet.add(sheetName);
            }
        }
        this.dataListSize += list.size();
        if (null != dataKey) {
            this.dataKey = dataKey;
        }
        Row row;
        for (int i = 0, len = list.size(); i < len; i++) {
            if (rownum == getExcelVersion().getMaxRows()) {
                sheet = createSheet(sheetName + "(" + ++countSheetNum + ")", headerList);
            }
            row = useTemplate ? sheet.getRow(rownum++) : sheet.createRow(rownum++);
            setData(row, list.get(i));
        }
        return getThis();
    }

    protected abstract <E> void setData(Row row, E e);

    public int getRownum() {
        return rownum;
    }

    public <E> THIS setDataMap(Map<String, List<E>> map) {
        Iterator<String> iterator = map.keySet().iterator();
        String key;
        for (; iterator.hasNext(); ) {
            setDataList(map.get(key = iterator.next()), key, null == dataKeyMap ? null : dataKeyMap.get(key), headMap == null ? null : headMap.get(key));
        }
        return getThis();
    }

    /**
     * 输出数据流
     *
     * @param os 输出数据流
     */
    public THIS write(OutputStream os) throws IOException {
        if (null == sheet)
            createSheet("Export", null);
        wb.write(os);
        return getThis();
    }

    /**
     * 输出到客户端 默认文件名为 工作簿名yyyyMMddHHmmss.xlsx
     *
     * @throws DataTaskException
     */
    public THIS write(HttpServletResponse response) throws IOException, DataTaskException {
        return write(response, getFileName());
    }

    /**
     * 输出到客户端
     *
     * @param fileName 输出文件名
     * @throws DataTaskException
     */
    public THIS write(HttpServletResponse response, String fileName) throws IOException, DataTaskException {
        if (null == response)
            throw new DataTaskException("导出没有设置response");
        String url = URLEncoder.encode(fileName, "UTF-8");
        url = url.replace("%26", "&");
        response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("utf8"), "iso8859-1"));
        write(response.getOutputStream());
        return getThis();
    }

    /**
     * 输出到文件
     *
     * @param name 输出文件名
     */
    public THIS write(String name) throws IOException {
        return write(new File(name));
    }

    public THIS write(String parent, String fileName) throws IOException {
        return write(new File(parent, fileName == null ? getFileName() : fileName));
    }

    public THIS write(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        this.write(os);
        IOUtils.closeQuietly(os);
        return getThis();
    }

    public String getFileName() {
        if (StringUtils.isNotBlank(fileName))
            return fileName;
        return fileName = (title + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + ".xlsx");
    }

    /**
     * 清理临时文件
     */
    public THIS dispose() {
        if (wb instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) wb).dispose();
        }
        return getThis();
    }

    public SpreadsheetVersion getExcelVersion() {
        return excelVersion;
    }

    public THIS setExcelVersion(SpreadsheetVersion excelVersion) {
        this.excelVersion = excelVersion;
        return getThis();
    }

    public List<String> getDataKey() {
        return dataKey;
    }

    public THIS setDataKey(List<String> dataKey) {
        this.dataKey = dataKey;
        return getThis();
    }

    public THIS setDataKey(Map<String, List<String>> keyMap) {
        this.dataKeyMap = keyMap;
        return getThis();
    }

    public Map<String, List<String>> getKeyMap() {
        return dataKeyMap;
    }

    public THIS addDataKey(String... keys) {
        if (keys.length > 0) {
            if (this.dataKey == null)
                this.dataKey = new ArrayList<String>();
            for (String key : keys) {
                this.dataKey.add(key);
            }
        }
        return getThis();
    }

    protected THIS setFileName(String fileName) {
        if (!fileName.endsWith(".xlsx")) {
            if (fileName.endsWith(".xls"))
                this.fileName = fileName + "x";
            else {
                this.fileName = fileName + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + ".xlsx";
            }
        } else {
            this.fileName = fileName;
        }
        return getThis();
    }

    public Map<String, LinkedList<String>> getHeadMap() {
        return headMap;
    }

    public THIS setHeadMap(LinkedHashMap<String, LinkedList<String>> heardMap) {
        this.headMap = heardMap;
        return getThis();
    }

    public THIS setNoTitle(boolean noTitle) {
        this.noTitle = noTitle;
        return getThis();
    }

    public THIS setDataGlobalField(EF ef) {
        this.dataGlobalField = ef;
        return getThis();
    }

    private THIS getThis() {
        return (THIS) this;
    }
}
