package com.dragon.excel;

import com.dragon.excel.entity.TestEntity;
import com.longlong.excel.ImportExcelBuilder;
import com.longlong.exporter.exception.ImportException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImportTest {

	private File file = new File("src/test/resources", "testImport.xlsx");

	@Test
	public void importField() throws InstantiationException, IllegalAccessException, ImportException, IOException {
		List<TestEntity> data = ImportExcelBuilder.buildEx(file, 0, 0).addExcelField("id").addExcelField("cityPos").addExcelField("countRequests")
				.addExcelField("countPassedRequests").addExcelField("countCanceledRequests").getDataList(TestEntity.class);
		print(data);
	}

	@Test
	public void importAnno() throws InstantiationException, IllegalAccessException, ImportException, IOException {
		List<TestEntity> data = ImportExcelBuilder.build(file, 0, 0).getDataList(TestEntity.class);
		print(data);
	}

	private <T> void print(List<T> data) {
		System.out.println(data);
		System.out.println(data.size());
	}


	@Test
	public void test(){
		Integer i = 0;
		System.out.println(0 == i);
		System.out.println(i == null);

	}
}
