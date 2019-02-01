package com.longlong.excel.exporter;

import com.longlong.excel.field.ExcelFieldEx;
import com.longlong.exporter.utils.Reflections;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 导出Excel文件（导出“XLSX”格式，支持大数据量导出 @see org.apache.poi.ss.SpreadsheetVersion）
 *
 * @author liaolonglong
 * @version 2013-04-21
 */
public class ExportExcelEx extends AbstractExportExcel<ExportExcelEx, ExcelFieldEx> {

	private List<ExcelFieldEx> dataFields;

	/**
	 *
	 * @param title
	 *            表格标题，传“空值”，表示无标题
	 * @param condition
	 *            导出条件对象
	 * @param headerList
	 *            表头列表
	 * @param freezeCount
	 *            冻结列数
	 * @param excelVersion
	 *            导出excel的版本 2003为EXCEL97对象 2007及以上为EXCEL2007对象
	 */
	public ExportExcelEx(String title, Object condition, List<String> headerList, int freezeCount, SpreadsheetVersion excelVersion) {
		initialize(title, condition, headerList, freezeCount, excelVersion);
	}

	@Override
	protected void createSheetEx(Sheet sheet, List<String> headerList) {
		// 初始化需要合并head的上级单元格
		if (dataGlobalField != null) {
			for (int i = 0; i < dataGlobalField.getMergeCellRows() - 1; i++) {
				Row mergeRow = sheet.createRow(rownum++);
				mergeRow.setRowStyle(styles.get("data"));
				String parentTitle = null;
				ExcelFieldEx temp;
				for (int j = 0; j < getDataFields().size(); j++) {
					Cell cell = mergeRow.createCell(j + (placeHolderColumn != -1 ? 1 : 0));
					cell.setCellStyle(styles.get("header"));
					temp = getDataFields().get(j);
					if (temp.getParentTitle().length != 0 && (temp.getParentTitle()[i]) != null) {
						cell.setCellValue(temp.getParentTitle()[i]);
						if (!temp.getParentTitle()[i].equals(parentTitle)) {
							// if (parentTitle != null) {
							sheet.addMergedRegion(new CellRangeAddress(mergeRow.getRowNum(), mergeRow.getRowNum(), j + (this.placeHolderColumn != -1 ? 1 : 0),
									j + (this.placeHolderColumn != -1 ? 1 : 0) + temp.getParentTitleColspan() - 1));
							// }
							parentTitle = temp.getParentTitle()[i];
						}
					} else if (parentTitle != null) {
						// sheet.addMergedRegion(new CellRangeAddress(mergeRow.getRowNum(),
						// mergeRow.getRowNum(), startMergeIndex, j - 1));
						sheet.addMergedRegion(new CellRangeAddress(mergeRow.getRowNum(), mergeRow.getRowNum(), j + (this.placeHolderColumn != -1 ? 1 : 0),
								j + (this.placeHolderColumn != -1 ? 1 : 0) + temp.getParentTitleColspan()));
						parentTitle = null;
					}
					sheet.autoSizeColumn(i);
				}
			}
		}
		// 冻结行
		Row headerRow = sheet.createRow(rownum++);
		if (dataGlobalField != null && dataGlobalField.isFreeze())
			sheet.createFreezePane(freezeCount, rownum);
		headerRow.setHeightInPoints(16);
		this.headerRow = headerRow;

		for (int i = 0; i < headerList.size(); i++) {
			Cell cell = headerRow.createCell(i);
			CellStyle cellStyle = styles.get("header");
			if (getDataFields().size() == headerList.size()) {
				if (getDataFields().get(i).isHighlight()) {
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
			if (getDataFields().size() == headerList.size()) {
				ExcelFieldEx temp = getDataFields().get(i);
				if (dataGlobalField != null && temp.getMergeCellRows() > 1 && temp.getParentTitle().length + 1 < dataGlobalField.getMergeCellRows()) {
					for (int j = 1; j < dataGlobalField.getMergeCellRows(); j++) {
						Cell parentCell = sheet.getRow(headerRow.getRowNum() - j).getCell(i);
						parentCell.setCellValue(cell.getStringCellValue());
						parentCell.setCellComment(cell.getCellComment());
					}
					sheet.addMergedRegion(new CellRangeAddress(headerRow.getRowNum() - dataGlobalField.getMergeCellRows() + temp.getParentTitle().length + 1, headerRow.getRowNum(), i, i));
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
		} else if (getDataFields().size() == 0 && e.getClass().isArray()) {
			Object[] arrE = (Object[]) e;
			for (int j = 0; j < arrE.length; j++) {
				this.addCell(row, colunm++, arrE[j], 2, Class.class);
				sb.append(arrE[j] + ", ");
			}
		} else {
			int temp = 0;
			for (ExcelFieldEx ef : getDataFields()) {
				if (this.placeHolderColumn == temp)
					colunm++;
				temp++;
				Object val = null;
				// Get entity value
				try {
					val = Reflections.getFieldValue(e, ef.getValue());
				} catch (Exception ex) {
					// Failure to ignore
					log.info(ex.toString());
					val = "";
				}
				int align = ef.getAlign();
				if (align == 0 && globalField != null)
					align = globalField.align();
				if (null != ef.getValueConvert() && ef.getValueConvert().length > 0) {
					for (String vc : ef.getValueConvert()) {
						String[] convertValue = vc.split(":");

						if (convertValue.length >= 3) {
							Object fieldValue = Reflections.getFieldValue(e, convertValue[2]);
							if (fieldValue == null)
								fieldValue = "";
							if (!convertValue[3].trim().equals(fieldValue.toString())) {
								continue;
							}
						}
						String val2 = convertValue[0].trim();
						if (val2.equals(val.toString())) {
							val = convertValue[1];
						}
					}
				}
				this.addCell(row, colunm++, val, align, ef.getFieldType());
				row.setRowStyle(styles.get("data"));
				sb.append(val + ", ");
			}
		}
	}

	public List<ExcelFieldEx> getDataFields() {
		return dataFields;
	}

	public ExportExcelEx setExcelFields(List<ExcelFieldEx> excelFields) {
		this.dataFields = excelFields;
		if (null == this.headerList)
			this.headerList = new ArrayList<>();
		excelFields.forEach(ef -> {
			this.headerList.add(ef.getTitle());
		});
		return this;
	}

	public ExportExcelEx addExcelField(ExcelFieldEx excelField) {
		if (this.dataFields == null)
			this.dataFields = new ArrayList<>();
		this.dataFields.add(excelField);
		if (null == this.headerList)
			this.headerList = new ArrayList<>();
		this.headerList.add(excelField.getTitle());
		return this;
	}

	public ExportExcelEx addExcelField(String fieldName, String title) {
		return addExcelField(new ExcelFieldEx(fieldName, title));
	}

	public ExportExcelEx addExcelField(String fieldName, String title, boolean isShow) {
		if (isShow) {
			return addExcelField(new ExcelFieldEx(fieldName, title));
		} else {
			return this;
		}
	}

	public ExportExcelEx addExcelField(String fieldName, String title, boolean isShow, String property, Object value) {
		if (isShow) {
			return addExcelField(fieldName, title, property, value);
		} else {
			return this;
		}
	}

	public ExportExcelEx addExcelField(String fieldName, String title, String property, Object value) {
		ExcelFieldEx excelFieldEx = new ExcelFieldEx(fieldName, title);
		Reflections.setFieldValue(excelFieldEx, property, value);
		return addExcelField(excelFieldEx);
	}
}
