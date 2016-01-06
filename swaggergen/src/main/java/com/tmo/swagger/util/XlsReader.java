package com.tmo.swagger.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tmo.swagger.exception.EmptyXlsRows;
import com.tmo.swagger.model.RowData;

public class XlsReader {
	public static void main(String[] args) throws EmptyXlsRows {
		XlsReader xlsReader = new XlsReader();
		xlsReader.readExcel("C://Users/anandigam/Documents/My Received Files/config/searchProductOfferSummary_res.xlsx","searchProductOffer");
	}

	public List<RowData> readExcel(String filePath,String sheetNname) throws EmptyXlsRows {
		List<RowData> lisRowDatas = new ArrayList<RowData>();
		try {
			FileInputStream file = new FileInputStream(new File(filePath));
			String extension = filePath.substring(filePath.lastIndexOf(".") + 1,
					filePath.length());
			Sheet sheet = null;
			if (extension.equalsIgnoreCase("xls")) {
				HSSFWorkbook workbook = new HSSFWorkbook(file);
				sheet = workbook.getSheet(sheetNname);
			} else if (extension.equalsIgnoreCase("xlsx")) {
				XSSFWorkbook workbook = new XSSFWorkbook(file);
				sheet = workbook.getSheet(sheetNname);
			}
			for (Row myrow : sheet) {
				int rowNum = myrow.getRowNum();
				Cell includeCell = myrow.getCell(0);
				if (includeCell != null
						&& includeCell.getCellType() != Cell.CELL_TYPE_BLANK
						&& includeCell.getStringCellValue().equalsIgnoreCase(
								"Y") && rowNum !=0) {
					RowData rd = new RowData();
					String xPath = getCellValue(myrow.getCell(1), rowNum);
					rd.setXpath(xPath.substring(1));
					String minOcc = getCellValue(myrow.getCell(2), rowNum);
					rd.setMin(minOcc);
					String maxOcc = getCellValue(myrow.getCell(3), rowNum);
					rd.setMax(maxOcc);
					String xsdType = getCellValue(myrow.getCell(4), rowNum);
					rd.setXsdType(xsdType);
					List<String> list=getEnumCellValue(myrow.getCell(5));
					rd.setEnumcell(list);
					String jsonType = getCellValue(myrow.getCell(6), rowNum);
					rd.setJsonType(jsonType);
					String jsonFormat = getCellValue(myrow.getCell(7));
					rd.setJsonFormat(jsonFormat);
					lisRowDatas.add(rd);
				}
			}

			file.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lisRowDatas;
	}

	public static String getCellValue(Cell cell, int rowNum)
			throws EmptyXlsRows {
		String s = "";
		
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			s = cell.getStringCellValue();
		} else {
			throw new EmptyXlsRows(rowNum);
		}
		return s;
	}
	
	public static String getCellValue(Cell cell)
			throws EmptyXlsRows {
		String s = "";
		
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			s = cell.getStringCellValue();
		} 
		return s;
	}
	
	public static List<String> getEnumCellValue(Cell cell)
			throws EmptyXlsRows {
		List<String> list=new ArrayList<String>();
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
		String s1 = cell.getStringCellValue();
			String ar[]=s1.split(Pattern.quote("|"));
			for(String a:ar){
				list.add(a);
			}
		} 
		return list;
	}

}
