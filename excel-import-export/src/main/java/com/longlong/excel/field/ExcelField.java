
package com.longlong.excel.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel注解定义
 *
 * @author liaolonglong
 * @version 2013-03-10
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelField {

    /**
     * 导出字段名（默认调用当前字段的“get”方法，如指定导出字段为对象，请填写“对象名.对象属性”，例：“area.name”、“office.
     * name”）
     */
    String value() default "";

    /**
     * 导出字段标题（需要添加批注请用“**”分隔，标题**批注，仅对导出模板有效）
     */
    String title() default "";

    /**
     * 字段类型（0：导出导入；1：仅导出；2：仅导入）
     */
    int type() default 0;

    /**
     * 导出字段对齐方式（0：自动；1：靠左；2：居中；3：靠右）
     */
    int align() default 0;

    /**
     * 导出字段字段排序（升序）
     */
    int sort() default 0;

    /**
     * 如果是字典类型，请设置字典的type值
     */
    String dictType() default "";

    /**
     * 反射类型
     */
    Class<?> fieldType() default Class.class;

    /**
     * 字段归属组（根据分组导出导入）
     */
    int[] groups() default {};

    /**
     * 此列之前的列进行冻结，不包含此列，只需在当前列设置一次即可
     */
    boolean isFreeze() default false;

    /**
     * 导出条件的列数
     */
    int cols() default 1;

    /**
     * 导出条件的行数
     */
    int rows() default 1;

    /**
     * 全局设置才有效<br/>
     * 导出条件是否按属性顺序排序，默认为false
     */
    boolean isSortByField() default false;

    /**
     * 导出字段的值和数据库的值进行转换<br/>
     * 例如:{"D:国内","I:国际"} 'D'为数据库的值,'国内'为导出时报表对应的值
     */
    String[] valueConvert() default {};

    /**
     * 合并单元格的行数
     * 
     * @return
     */
    int mergeCellRows() default 1;

    /**
     * 合并单元格的上一个单元格的名字<br/>
     * 下标位置表示上[i+1]单元格
     * @return
     */
    String[] parentTitle() default {};
    
    /**
     * 字体是否高亮显示
     * @return
     */
    boolean isHighlight() default false;
    
    /**
     * 父标题合并单元格数
     * @return
     */
    int parentTitleColspan() default 1;
    
    /**
     * 是否导出该列
     * 接收一个标识字段
     * @return
     */
    String isExport() default "";
    
}
