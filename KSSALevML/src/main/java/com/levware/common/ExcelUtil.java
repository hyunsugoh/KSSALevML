package com.levware.common;

import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;

public  class  ExcelUtil {
	
	
	public static String getExcelValue(Cell cell) {
        return getExcelValue(cell, null);
    }
	
	public static String getExcelValue(Cell cell, FormulaEvaluator formulaEval) {
		
        switch (cell.getCellType()) { // 각 셀에 담겨있는 데이터의 타입을 체크하고, 해당 타입에 맞게 가져온다.
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue()).trim();
                }
            case STRING:
                return cell.getStringCellValue().trim();
            case BLANK:
                return "";
            case ERROR:
                return String.valueOf(cell.getErrorCellValue()).trim();
            case FORMULA:
            	
            	if (formulaEval==null) {
            		return "";
            	}else {
            		CellValue evaluate = formulaEval.evaluate(cell);
            		if( evaluate != null ) {
            			return (evaluate.formatAsString()).trim();
            		}else {
            			return 	"";
            		}
            	}
            default : 
                return cell.getStringCellValue();
        }
    }
	
	
	public static void setExcelValue(Cell cell, Object val) {
		
		System.out.println("--getCellType-->"+cell.getCellType());// switch case 사용시 숫자가아닌 텍스트로 떨어지기 때문에 if 문으로 분기처리.0331
		System.out.println("--val-->"+val);
		
		if(cell.getCellType() == CellType.NUMERIC){
			if (DateUtil.isCellDateFormatted(cell)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                cell.setCellValue(dateFormat.format(val));
            } else {
            	double d_feature_value = Double.parseDouble(val.toString()); //숫자로 인식하기위해 더블형으로 형변환.
            	if(val != null){
            		cell.setCellValue(d_feature_value);
            	}
            }
			
		}else if(cell.getCellType() == CellType.STRING){
			cell.setCellValue(val==null?null:String.valueOf(val));
		}else if(cell.getCellType() == CellType.BLANK){
			cell.setCellValue(val==null?null:String.valueOf(val));
		}else if(cell.getCellType() == CellType.FORMULA){
			cell.setCellValue(val==null?null:String.valueOf(val));
		}else{
			cell.setCellValue(val==null?null:String.valueOf(val));
		}
		/*switch (cell.getCellType()) { // 각 셀에 담겨있는 데이터의 타입을 체크하고, 해당 타입에 맞게 가져온다.
	        case NUMERIC:
	            if (DateUtil.isCellDateFormatted(cell)) {
	                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	                cell.setCellValue(dateFormat.format(val));
	            } else {
	            	//cell.setCellValue(val==null?null:Double.valueOf(val.toString())); 원본
	            	double d_feature_value = Double.parseDouble(val.toString()); //숫자로 인식하기위해 더블형으로 형변환.
	            	if(val != null){
	            		cell.setCellValue(d_feature_value);
	            	}
	            	
	            }
	        case STRING:
	        	cell.setCellValue(val==null?null:String.valueOf(val));
	        case BLANK:	
	        	cell.setCellValue(val==null?null:String.valueOf(val));
	        case FORMULA:
	        	cell.setCellValue(val==null?null:String.valueOf(val));
	        default : 
	        	cell.setCellValue(val==null?null:String.valueOf(val));
		}*/
	}

}
