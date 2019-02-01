package com.longlong.excel;

import com.longlong.excel.exporter.ExportExcel;
import com.longlong.excel.exporter.ExportExcelEx;
import org.apache.poi.ss.SpreadsheetVersion;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 导出数据到Excel构建器
 * 
 * @author liaolonglong
 *
 */
public class ExportExcelBuilder {

	public static ExportExcel build(String title, Class<?> cls) {
		return build(title, null, cls, 1, null, -1, null);
	}

	public static ExportExcel build(String title, Class<?> cls, SpreadsheetVersion excelVersion) {
		return build(title, null, cls, 1, excelVersion, -1, null);
	}

	public static ExportExcel build(String title, Class<?> cls, int type, int... group) {
		return build(title, null, cls, type, null, -1, group);
	}

	public static ExportExcel build(String title, Class<?> cls, int type, SpreadsheetVersion excelVersion, int... groups) {
		return build(title, null, cls, type, excelVersion, -1, groups);
	}

	public static ExportExcel build(String title, Object condition, Class<?> cls) {
		return build(title, condition, cls, 1, null, -1, null);
	}

	public static ExportExcel build(String title, Object condition, Class<?> cls, int placeHolderColumn) {
		return build(title, condition, cls, 1, null, placeHolderColumn, null);
	}

	public static ExportExcel build(String title, Object condition, Class<?> cls, int type, SpreadsheetVersion excelVersion, int... groups) {
		return build(title, condition, cls, type, excelVersion, -1, groups);
	}

	/**
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
	public static ExportExcel build(String title, Object condition, Class<?> cls, int type, SpreadsheetVersion excelVersion, int placeHolderColumn, int... groups) {
		return new ExportExcel(title, condition, cls, type, excelVersion, placeHolderColumn, groups);
	}

	public static ExportExcelEx buildEx(String title, String[] headers) {
		return buildEx(title, null,Arrays.asList(headers), 0, null);
	}

	public static ExportExcelEx buildEx(String title, String[] headers, SpreadsheetVersion excelVersion) {
		return buildEx(title, null, Arrays.asList(headers), 0, excelVersion);
	}

	public static ExportExcelEx buildEx(String title, List<String> headerList) {
		return buildEx(title, null, headerList, 0, null);
	}

	public static ExportExcelEx buildEx(String title, List<String> headerList, SpreadsheetVersion excelVersion) {
		return buildEx(title, null, headerList, 0, excelVersion);
	}

	public static ExportExcelEx buildEx(String title, Object condition) {
		return buildEx(title, condition, null, 0, null);
	}

	public static ExportExcelEx buildEx(String title) {
		return buildEx(title, null, null, 0, null);
	}

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
	public static ExportExcelEx buildEx(String title, Object condition, List<String> headerList, int freezeCount, SpreadsheetVersion excelVersion) {
		return new ExportExcelEx(title, condition, headerList, freezeCount, excelVersion);
	}

	public static Object buildReplaceData(File fileFromResource, Map<String, Integer> start) {
		return null;
	}

}
