package com.longlong.excel.importer;

import com.longlong.excel.field.ExcelField;
import com.longlong.excel.util.Reflections;
import com.longlong.exporter.exception.ImportException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 导入Excel文件（支持“XLS”和“XLSX”格式） 使用注解设置导入对象属性
 * 
 * @author liaolonglong
 * @version 2013-03-10
 */
public class ImportExcel extends AbstractImportExcel {

	public ImportExcel(String fileName, InputStream is, int headerNum, int sheetIndex) throws ImportException, IOException {
		super(fileName, is, headerNum, sheetIndex);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> getDataList(Class<E> cls, int... groups) throws InstantiationException, IllegalAccessException {
		boolean isPrimitive = cls.isPrimitive() || cls.equals(String.class);
		List<Object[]> annotationList = new ArrayList<>();
		if (!isPrimitive) {
			ExcelField globalExcelField = cls.getAnnotation(ExcelField.class);
			// Get annotation field
			Field[] fs = cls.getDeclaredFields();
			for (Field f : fs) {
				ExcelField ef = f.getAnnotation(ExcelField.class);
				if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
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
				if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
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
			if (globalExcelField.isSortByField()) {
				Collections.sort(annotationList, new Comparator<Object[]>() {
					public int compare(Object[] o1, Object[] o2) {
						return new Integer(((ExcelField) o1[0]).sort()).compareTo(new Integer(((ExcelField) o2[0]).sort()));
					};
				});
			}
		}
		// log.debug("Import column count:"+annotationList.size());
		// Get excel data
		List<E> dataList = new ArrayList<>();
		int i;
		Row row = this.getRow(i = this.getDataRowNum());
		while (null != row) {
			int column = 0;
			if (!isPrimitive) {
				E e = (E) cls.newInstance();
				StringBuilder sb = new StringBuilder();
				boolean isAdd = false;
				for (Object[] os : annotationList) {
					Object val = this.getCellValue(row, column++);
					if (val != null && !val.toString().equals("")) {
						if (!isAdd)
							isAdd = true;
						ExcelField ef = (ExcelField) os[0];
						Class<?> valType = Class.class;
						if (os[1] instanceof Field) {
							valType = ((Field) os[1]).getType();
						} else if (os[1] instanceof Method) {
							Method method = ((Method) os[1]);
							if ("get".equals(method.getName().substring(0, 3))) {
								valType = method.getReturnType();
							} else if ("set".equals(method.getName().substring(0, 3))) {
								valType = ((Method) os[1]).getParameterTypes()[0];
							}
						}

						// log.debug("Import value type: ["+i+","+column+"] " +
						// valType);
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
								if (ef.fieldType() != Class.class) {
									val = ef.fieldType().getMethod("getValue", String.class).invoke(null, val.toString());
								} else {
									val = Class.forName(this.getClass().getName().replaceAll(this.getClass().getSimpleName(), "fieldtype." + valType.getSimpleName() + "Type"))
											.getMethod("getValue", String.class).invoke(null, val.toString());
								}
							}
						} catch (Exception ex) {
							log.info("Get cell value [" + i + "," + column + "] error: " + ex.toString());
							val = null;

						}
						for (int j = 0; j < ef.valueConvert().length; j++) {
							String[] valueConvert = ef.valueConvert()[j].split(":");
							if (valueConvert[1].equals(val)) {
								val = valueConvert[0];
							}
						}
						// set entity value
						if (os[1] instanceof Field) {
							Reflections.setFieldValue(e, ((Field) os[1]).getName(), val);
						} else if (os[1] instanceof Method) {
							String mthodName = ((Method) os[1]).getName();
							if ("get".equals(mthodName.substring(0, 3))) {
								mthodName = "set" + StringUtils.substringAfter(mthodName, "get");
							}
							Reflections.invokeMethod(e, mthodName, new Class[] { valType }, new Object[] { val });
						}
					}
					sb.append(val + ", ");
				}
				if (isAdd) {
					dataList.add(e);
				}

				log.debug("Read success: [" + i + "] " + sb.toString());
				row = this.getRow(++i);
			} else {
				row = this.getRow(i++);
				Object val = this.getCellValue(row, column);
				if (val != null && !val.toString().equals("")) {
					dataList.add((E) val);
				}

				log.debug("Read success: [" + i + "] ");

			}
		}
		return dataList;
	}

}
