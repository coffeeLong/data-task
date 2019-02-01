package com.longlong.excel.field;

import java.lang.reflect.Field;

/**
 * 设置导出属性扩展类
 * 
 * @author liaolonglong
 *
 */
public class ExcelFieldEx {

	public ExcelFieldEx() {
	}

	public ExcelFieldEx(String value) {
		this.value = value;
	}

	public ExcelFieldEx(String value, String title) {
		this.value = value;
		this.title = title;
	}

	/**
	 * 导出字段名（默认调用当前字段的“get”方法，如指定导出字段为对象，请填写“对象名.对象属性”，例：“area.name”、“office.name”）
	 */
	private String value = "";

	/** 导出字段标题（需要添加批注请用“**”分隔，标题**批注，仅对导出模板有效） */
	private String title = "";
	/** 字段类型（0：导出导入；1：仅导出；2：仅导入） */
	private String type;
	/** 导出字段对齐方式（0：自动；1：靠左；2：居中；3：靠右） */
	private int align;
	/** 导出字段字段排序（升序） */
	private int sort;
	/** 如果是字典类型，请设置字典的type值 */
	private String dictType = "";
	/** 反射类型 */
	private Class<?> fieldType = Class.class;
	/** 字段归属组（根据分组导出导入） */
	private String groups;
	/** 此列之前的列进行冻结，不包含此列，只需在当前列设置一次即可 */
	private boolean freeze;
	/** 导出条件的列数 */
	private int cols = 1;
	/** 导出条件的行数 */
	private int rows = 1;
	/** 全局设置才有效 导出条件是否按属性顺序排序，默认为false */
	private boolean sortByField;
	/**
	 * 导出字段的值和数据库的值进行转换<br/>
	 * 例如:{"D:国内","I:国际"} 'D'为数据库的值,'国内'为导出时报表对应的值
	 */
	private String[] valueConvert;
	/**
	 * 合并单元格的行数
	 */
	private int mergeCellRows = 1;
	/**
	 * 合并单元格的上一个单元格的名字<br/>
	 * 下标位置表示上[i+1]单元格
	 */
	private String[] parentTitle;
	/** 字体是否高亮显示 */
	private boolean highlight;
	/** 父标题合并单元格数 */
	private int parentTitleColspan = 1;
	/** 是否导出该列 接收一个标识字段 */
	private String export;
	// =======================================================================================================
	/** 以下属性由工具类使用，不建议开发人员调用 */
	/** 指定导出字段对应的类属性 */
	private Field field;

	public String getValue() {
		return value;
	}

	public ExcelFieldEx setValue(String value) {
		this.value = value;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public ExcelFieldEx setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getType() {
		return type;
	}

	public ExcelFieldEx setType(String type) {
		this.type = type;
		return this;
	}

	public int getAlign() {
		return align;
	}

	public ExcelFieldEx setAlign(int align) {
		this.align = align;
		return this;
	}

	public int getSort() {
		return sort;
	}

	public ExcelFieldEx setSort(int sort) {
		this.sort = sort;
		return this;
	}

	public String getDictType() {
		return dictType;
	}

	public ExcelFieldEx setDictType(String dictType) {
		this.dictType = dictType;
		return this;
	}

	public Class<?> getFieldType() {
		return fieldType;
	}

	public ExcelFieldEx setFieldType(Class<?> fieldType) {
		this.fieldType = fieldType;
		return this;
	}

	public String getGroups() {
		return groups;
	}

	public ExcelFieldEx setGroups(String groups) {
		this.groups = groups;
		return this;
	}

	public boolean isFreeze() {
		return freeze;
	}

	public ExcelFieldEx setFreeze(boolean freeze) {
		this.freeze = freeze;
		return this;
	}

	public int getCols() {
		return cols;
	}

	public ExcelFieldEx setCols(int cols) {
		this.cols = cols;
		return this;
	}

	public int getRows() {
		return rows;
	}

	public ExcelFieldEx setRows(int rows) {
		this.rows = rows;
		return this;
	}

	public boolean isSortByField() {
		return sortByField;
	}

	public ExcelFieldEx setSortByField(boolean sortByField) {
		this.sortByField = sortByField;
		return this;
	}

	public String[] getValueConvert() {
		return valueConvert;
	}

	public ExcelFieldEx setValueConvert(String[] valueConvert) {
		this.valueConvert = valueConvert;
		return this;
	}

	public int getMergeCellRows() {
		return mergeCellRows;
	}

	public ExcelFieldEx setMergeCellRows(int mergeCellRows) {
		this.mergeCellRows = mergeCellRows;
		return this;
	}

	public String[] getParentTitle() {
		return parentTitle;
	}

	public ExcelFieldEx setParentTitle(String[] parentTitle) {
		this.parentTitle = parentTitle;
		return this;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public ExcelFieldEx setHighlight(boolean highlight) {
		this.highlight = highlight;
		return this;
	}

	public int getParentTitleColspan() {
		return parentTitleColspan;
	}

	public ExcelFieldEx setParentTitleColspan(int parentTitleColspan) {
		this.parentTitleColspan = parentTitleColspan;
		return this;
	}

	public String getExport() {
		return export;
	}

	public ExcelFieldEx setExport(String export) {
		this.export = export;
		return this;
	}

	public Field getField() {
		return field;
	}

	public ExcelFieldEx setField(Field field) {
		this.field = field;
		return this;
	}

	public String getFileName() {
		return value;
	}

	public ExcelFieldEx setFileName(String fileName) {
		this.value = fileName;
		return this;
	}

	public String getFieldName() {
		return value;
	}

	public ExcelFieldEx setFieldName(String fieldName) {
		this.value = fieldName;
		return this;
	}

}
