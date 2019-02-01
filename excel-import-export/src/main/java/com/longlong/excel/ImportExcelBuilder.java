package com.longlong.excel;

import com.longlong.excel.importer.ImportExcel;
import com.longlong.excel.importer.ImportExcelEx;
import com.longlong.exporter.exception.ImportException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 从Excel导入数据构建器
 * 
 * @author liaolonglong
 *
 */
public class ImportExcelBuilder {
	/**
	 * @param filePath
	 *            文件绝对路径
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcel build(String filePath, int headerNum) throws ImportException, IOException {
		return build(new File(filePath), headerNum, 0);
	}

	/**
	 * @param fileName
	 *            文件绝对路径
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcelEx buildEx(String fileName, int headerNum) throws ImportException, IOException {
		return buildEx(new File(fileName), headerNum, 0);
	}

	/**
	 * @param file
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcel build(File file, int headerNum) throws ImportException, IOException {
		return build(file, headerNum, 0);
	}

	/**
	 * @param file
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcelEx buildEx(File file, int headerNum) throws ImportException, IOException {
		return buildEx(file, headerNum, 0);
	}

	/**
	 * @param filePath
	 *            文件绝对路径
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcel build(String filePath, int headerNum, int sheetIndex) throws ImportException, IOException {
		return build(new File(filePath), headerNum, sheetIndex);
	}

	/**
	 * @param file
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcelEx buildEx(String fileName, int headerNum, int sheetIndex) throws ImportException, IOException {
		return buildEx(new File(fileName), headerNum, sheetIndex);
	}

	/**
	 * @param file
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcel build(File file, int headerNum, int sheetIndex) throws ImportException, IOException {
		return build(file.getName(), new FileInputStream(file), headerNum, sheetIndex);
	}

	/**
	 * @param file
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcelEx buildEx(File file, int headerNum, int sheetIndex) throws ImportException, IOException {
		return buildEx(file.getName(), new FileInputStream(file), headerNum, sheetIndex);
	}
	
	/**
	 * 
	 * @param fileName
	 *            导入文件名
	 * @param is
	 *            导出文件流
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcel build(String fileName, InputStream is, int headerNum, int sheetIndex) throws ImportException, IOException {
		return new ImportExcel(fileName, is, headerNum, sheetIndex);
	}

	/**
	 * 
	 * @param fileName
	 *            导入文件名
	 * @param is
	 *            导出文件流
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @return
	 * @return 导出工具类对象
	 * @throws ImportException
	 * @throws IOException
	 */
	public static ImportExcelEx buildEx(String fileName, InputStream is, int headerNum, int sheetIndex) throws ImportException, IOException {
		return new ImportExcelEx(fileName, is, headerNum, sheetIndex);
	}
}
