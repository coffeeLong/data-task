package com.longlong.excel.importer;

import com.longlong.excel.field.ExcelFieldEx;
import com.longlong.exporter.exception.ImportException;
import com.longlong.exporter.utils.Reflections;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 导入Excel文件（支持“XLS”和“XLSX”格式）
 * 
 * @author liaolonglong
 * @version 2013-03-10
 */
public class ImportExcelEx extends AbstractImportExcel {

	/** 指定导入类的ExcelField属性 */
	private List<ExcelFieldEx> excelFields = new ArrayList<>();
	/** 当前读取的行 */
	private Row currentRow;

	public ImportExcelEx(String fileName, InputStream is, int headerNum, int sheetIndex) throws ImportException, IOException {
		super(fileName, is, headerNum, sheetIndex);
	}

	public ImportExcelEx addExcelField(ExcelFieldEx excelField) {
		excelFields.add(excelField);
		return this;
	}

	public ImportExcelEx addExcelField(String fieldName) {
		return addExcelField(new ExcelFieldEx(fieldName));
	}

	public ImportExcelEx addExcelField(String fieldName, String title) {
		return addExcelField(new ExcelFieldEx(fieldName, title));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> getDataList(Class<E> cls, int... groups) throws InstantiationException, IllegalAccessException {
		boolean isPrimitive = cls.isPrimitive() || cls.equals(String.class);
		if (!isPrimitive) {
			excelFields.forEach(ef -> {
				try {
					ef.setField(cls.getDeclaredField(ef.getValue()));
					ef.getField().setAccessible(true);
				} catch (NoSuchFieldException | SecurityException e) {
					e.printStackTrace();
				}
			});
		}
		// Get excel data
		List<E> dataList = new ArrayList<>();
		int i;
		currentRow = this.getRow(i = this.getDataRowNum());
		while (null != currentRow) {
			AtomicInteger column = new AtomicInteger(0);
			if (!isPrimitive) {
				E e = (E) cls.newInstance();
				StringBuilder sb = new StringBuilder();
				AtomicBoolean isAdd = new AtomicBoolean(false);
				excelFields.forEach(ef -> {
					Object val = this.getCellValue(currentRow, column.getAndIncrement());
					if (val != null && !val.toString().equals("")) {
						if (!isAdd.get()) {
							isAdd.set(true);
						}
						// Get param type and type cast
						Class<?> valType = ef.getField().getType();
						try {
							if (valType == String.class) {
								String s = String.valueOf(val.toString());
								if (StringUtils.endsWith(s, ".0")) {
									val = StringUtils.substringBefore(s, ".0");
								} else {
									val = String.valueOf(val.toString());
								}
							} else if (valType == Integer.class || valType == int.class) {
								val = Double.valueOf(val.toString()).intValue();
							} else if (valType == Long.class || valType == long.class) {
								val = Double.valueOf(val.toString()).longValue();
							} else if (valType == Double.class || valType == double.class) {
								val = Double.valueOf(val.toString());
							} else if (valType == Float.class || valType == float.class) {
								val = Float.valueOf(val.toString());
							} else if (valType == Date.class) {
								try {
									val = DateUtil.getJavaDate((Double) val);
								} catch (Exception e2) {
									// 处理Excel日期列，不是日期类型的情况
									String val1 = ((String) val).replace("/", "-");
									val = DateUtils.parseDate(val1);
								}

							} else {
								if (ef.getFieldType() != Class.class) {
									val = ef.getFieldType().getMethod("getValue", String.class).invoke(null, val.toString());
								} else {
									val = Class.forName(this.getClass().getName().replaceAll(this.getClass().getSimpleName(), "fieldtype." + valType.getSimpleName() + "Type"))
											.getMethod("getValue", String.class).invoke(null, val.toString());
								}
							}
						} catch (Exception ex) {
							log.info("Get cell value [" + currentRow.getRowNum() + "," + column + "] error: " + ex.toString());
							val = null;
						}
						if (null != ef.getValueConvert() && ef.getValueConvert().length > 0) {
							for (String vc : ef.getValueConvert()) {
								String[] valueConvert = vc.split(":");
								if (valueConvert[1].equals(val)) {
									val = valueConvert[0];
								}
							}
						}
						// set entity value
						Reflections.setFieldValue(e, ef.getFieldName(), val);
					}
					sb.append(val + ", ");
				});
				if (isAdd.get()) {
					dataList.add(e);
				}
				log.debug("Read success: [" + i + "] " + sb.toString());
				currentRow = this.getRow(++i);
			} else {
				Object val = getCellValue(this.getRow(++i), column.get());
				dataList.add((E) val);
				log.debug("Read success: [" + i + "] ");
			}
		}
		return dataList;
	}
}
