package com.longlong.excel.exporter;

import com.longlong.excel.field.ExcelField;
import com.longlong.excel.util.Reflections;
import com.longlong.base.DataTaskException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 导出Excel文件（导出“XLSX”格式，支持大数据量导出 @see org.apache.poi.ss.SpreadsheetVersion）
 *
 * @author liaolonglong
 * @version 2013-04-21
 */
public class ExportExcel extends AbstractExportExcel<ExportExcel, ExcelField> {
	/**
	 * 注解列表（Object[]{ ExcelField, Field/Method }）
	 */
	protected List<Object[]> annotationList;
	
	public ExportExcel(File srcFile, int start) throws DataTaskException {
		super(srcFile, start);
	}

	public ExportExcel(File srcFile, Map<String, Integer> useModelDataStart) throws DataTaskException {
		super(srcFile, useModelDataStart);
	}

	/**
	 * 构造函数
	 *
	 * @param title
	 *            表格标题，传“空值”，表示无标题
	 * @param condition
	 *            导出表单条件的对象
	 * @param cls
	 *            实体对象，通过annotation.ExportField获取标题
	 * @param type
	 *            导出类型（1:导出数据；2：导出模板）
	 * @param excelVersion
	 *            导出excel的版本 2003为EXCEL97对象 2007及以上为EXCEL2007对象
	 * @param placeHolderColumn
	 *            占位符位置
	 * @param groups
	 *            导入分组
	 */
	public ExportExcel(String title, Object condition, Class<?> cls, int type, SpreadsheetVersion excelVersion, int placeHolderColumn, int... groups) {
		this.placeHolderColumn = placeHolderColumn;
		initializeEx(title, condition, cls, type, excelVersion, placeHolderColumn, groups);
	}

	protected void initializeEx(String title, Object condition, Class<?> cls, int type, SpreadsheetVersion excelVersion, int placeHolderColumn, int... groups) {
		this.placeHolderColumn = placeHolderColumn;
		annotationList = new ArrayList<>();
		setDataGlobalField(cls.getAnnotation(ExcelField.class));
		Field[] fs = cls.getDeclaredFields();
		for (Field f : fs) {
			ExcelField ef = f.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type() == 0 || ef.type() == type)) {
				if (groups != null && groups.length > 0) {
					boolean inGroup = false;
					for (int g : groups) {
						if (inGroup) {
							break;
						}
						for (int efg : ef.groups()) {
							if (g == efg) {
								inGroup = true;
								annotationList.add(new Object[] { ef, f });
								break;
							}
						}
					}
				} else {
					annotationList.add(new Object[] { ef, f });
				}
			}
		}
		// Get annotation method
		Method[] ms = cls.getDeclaredMethods();
		for (Method m : ms) {
			ExcelField ef = m.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type() == 0 || ef.type() == type)) {
				if (groups != null && groups.length > 0) {
					boolean inGroup = false;
					for (int g : groups) {
						if (inGroup) {
							break;
						}
						for (int efg : ef.groups()) {
							if (g == efg) {
								inGroup = true;
								annotationList.add(new Object[] { ef, m });
								break;
							}
						}
					}
				} else {
					annotationList.add(new Object[] { ef, m });
				}
			}
		}
		// Field sorting
		ExcelField annotation = cls.getAnnotation(ExcelField.class);
		if ((null != annotation && annotation.isSortByField()) || annotation == null) {
			Collections.sort(annotationList, new Comparator<Object[]>() {
				@Override
				public int compare(Object[] o1, Object[] o2) {
					return new Integer(((ExcelField) o1[0]).sort()).compareTo(new Integer(((ExcelField) o2[0]).sort()));
				};
			});
		}

		int count = 0;
		// Initialize
		for (Object[] os : annotationList) {
			String t = ((ExcelField) os[0]).title();
			// 如果是导出，则去掉注释
			if (type == 1) {
				String[] ss = StringUtils.split(t, "**", 2);
				if (ss.length == 2) {
					t = ss[0];
				}
			}
			// 测试占位符
			if (headerList.size() == this.placeHolderColumn) {
				headerList.add("placeHolder");
			}
			headerList.add(t);
			// 判断是否冻结
			if (((ExcelField) os[0]).isFreeze()) {
				freezeCount = count;
			} else {
				count++;
			}
		}
		initialize(title, condition, headerList, count, excelVersion);
	}

	@Override
	protected void createSheetEx(Sheet sheet, List<String> headerList) {
		// 初始化需要合并head的上级单元格
		if (dataGlobalField != null) {
			for (int i = 0; i < dataGlobalField.mergeCellRows() - 1; i++) {
				Row mergeRow = sheet.createRow(rownum++);
				mergeRow.setRowStyle(styles.get("data"));
				String parentTitle = null;
				ExcelField temp;
				for (int j = 0; j < annotationList.size(); j++) {
					Cell cell = mergeRow.createCell(j + (placeHolderColumn != -1 ? 1 : 0));
					cell.setCellStyle(styles.get("header"));
					temp = (ExcelField) annotationList.get(j)[0];
					if (temp.parentTitle().length != 0 && (temp.parentTitle()[i]) != null) {
						cell.setCellValue(temp.parentTitle()[i]);
						if (!temp.parentTitle()[i].equals(parentTitle)) {
							// if (parentTitle != null) {
							sheet.addMergedRegion(new CellRangeAddress(mergeRow.getRowNum(), mergeRow.getRowNum(), j + (this.placeHolderColumn != -1 ? 1 : 0),
									j + (this.placeHolderColumn != -1 ? 1 : 0) + temp.parentTitleColspan() - 1));
							// }
							parentTitle = temp.parentTitle()[i];
						}
					} else if (parentTitle != null) {
						// sheet.addMergedRegion(new CellRangeAddress(mergeRow.getRowNum(),
						// mergeRow.getRowNum(), startMergeIndex, j - 1));
						sheet.addMergedRegion(new CellRangeAddress(mergeRow.getRowNum(), mergeRow.getRowNum(), j + (this.placeHolderColumn != -1 ? 1 : 0),
								j + (this.placeHolderColumn != -1 ? 1 : 0) + temp.parentTitleColspan()));
						parentTitle = null;
					}
					sheet.autoSizeColumn(i);
				}
			}
		}
		Row headerRow = sheet.createRow(rownum++);
		if (dataGlobalField != null && dataGlobalField.isFreeze()) {
			sheet.createFreezePane(freezeCount, rownum);
		}
		headerRow.setHeightInPoints(16);
		this.headerRow = headerRow;

		for (int i = 0; i < headerList.size(); i++) {
			Cell cell = headerRow.createCell(i);
			CellStyle cellStyle = styles.get("header");
			if (annotationList.size() == headerList.size()) {
				ExcelField temp = (ExcelField) annotationList.get(i)[0];
				if (temp.isHighlight()) {
					cellStyle = styles.get("highLightHeader");
				}
			}
			cell.setCellStyle(cellStyle);
			String[] ss = StringUtils.split(headerList.get(i), "**", 2);
			if (ss.length == 2) {
				cell.setCellValue(ss[0]);
				Comment comment = sheet.createDrawingPatriarch().createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
				comment.setString(new XSSFRichTextString(ss[1]));
				cell.setCellComment(comment);
			} else {
				cell.setCellValue(headerList.get(i));
			}
			if (annotationList.size() == headerList.size()) {
				ExcelField temp = (ExcelField) annotationList.get(i)[0];
				if (dataGlobalField != null && temp.mergeCellRows() > 1 && temp.parentTitle().length + 1 < dataGlobalField.mergeCellRows()) {
					for (int j = 1; j < dataGlobalField.mergeCellRows(); j++) {
						Cell parentCell = sheet.getRow(headerRow.getRowNum() - j).getCell(i);
						parentCell.setCellValue(cell.getStringCellValue());
						parentCell.setCellComment(cell.getCellComment());
					}
					sheet.addMergedRegion(new CellRangeAddress(headerRow.getRowNum() - dataGlobalField.mergeCellRows() + temp.parentTitle().length + 1, headerRow.getRowNum(), i, i));

				}
			}
			sheet.autoSizeColumn(i);
		}
	}

	@Override
	protected <E> void setData(Row row, E e) {
		int colunm = 0;
		StringBuilder sb = new StringBuilder();
		if (e instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, ?> eMp = (Map<String, ?>) e;
			for (String mapKey : dataKey) {
				this.addCell(row, colunm++, eMp.get(mapKey), 2, Class.class);
				sb.append(eMp.get(mapKey) + ", ");
			}
		} else if (annotationList.size() == 0 && e.getClass().isArray()) {
			Object[] arrE = (Object[]) e;
			for (int j = 0; j < arrE.length; j++) {
				this.addCell(row, colunm++, arrE[j], 2, Class.class);
				sb.append(arrE[j] + ", ");
			}
		} else {
			int temp = 0;
			for (Object[] os : annotationList) {
				if (this.placeHolderColumn == temp) {
					colunm++;
				}
				temp++;
				ExcelField ef = (ExcelField) os[0];
				Object val = null;
				// Get entity value
				try {
					if (StringUtils.isNotBlank(ef.value())) {
						val = Reflections.invokeGetter(e, ef.value());
					} else {
						if (os[1] instanceof Field) {
							val = Reflections.getFieldValue(e, ((Field) os[1]).getName());
						} else if (os[1] instanceof Method) {
							val = Reflections.invokeMethod(e, ((Method) os[1]).getName(), new Class[] {}, new Object[] {});
						}
					}
				} catch (Exception ex) {
					// Failure to ignore
					log.info(ex.toString());
					val = "";
				}
				int align = ef.align();
				if (align == 0 && globalField != null) {
					align = globalField.align();
				}
				if (ef.valueConvert().length > 0) {
					for (String vc : ef.valueConvert()) {
						String[] convertValue = vc.split(":");
						String val2 = convertValue[0].trim();
						if (val2.equals(val)) {
							val = convertValue[1];
						}
					}
				}
				this.addCell(row, colunm++, val, align, ef.fieldType());
				row.setRowStyle(styles.get("data"));
				sb.append(val + ", ");
			}
		}

	}

}
