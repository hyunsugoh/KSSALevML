package com.levware.common;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import egovframework.rte.fdl.property.EgovPropertyService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@Service
public class FileService {
	
	//@Value("${upload.base.dir}") private String uploadBase;
	
	
	// properties
	@Resource(name="propertyService")
	protected EgovPropertyService propertyService;
	
	
	public Map<String,Object> upload(MultipartFile files,String menuId,String fGroupId){
		Map<String,Object> resultMap = new HashMap<String,Object>();
		
		String uploadBase = propertyService.getString("upload.base.dir");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date dt = new Date();
		String fileGroupId;
		if(fGroupId==null || fGroupId.length()==0 || fGroupId.equals("null")) {
			fileGroupId= UUID.randomUUID().toString();
		}else {
			fileGroupId=fGroupId;
		}
		
		if(menuId.length()==0 || menuId.equals("null")) {
			menuId= "temp";
		}
		
		FileOutputStream fos = null;
		//Map<String,Object> fileInfo = new HashMap<String,Object>();
		String fileId = "";
		String fileName = "";
		String fileNameOrg = "";
		String filePath = "";
		String extension = "";
		Long fileSize = files.getSize();
		String result = "S";
		
		try{
			fileId = UUID.randomUUID().toString();
			//fileNameOrg = new String(files.getOriginalFilename().getBytes("8859_1"),"utf-8");//한글깨질경우
			fileNameOrg = files.getOriginalFilename();
			if("temp".equals(menuId)){
				filePath = uploadBase+"/"+menuId;
				fileName = fileNameOrg+"_"+fileId;
			}else {
				filePath = uploadBase+"/"+menuId+"/"+sdf.format(dt);
				fileName = fileNameOrg;
			}
			
			//파일 확장자 구하기
			int index = fileNameOrg.lastIndexOf(".");
			extension = fileNameOrg.substring( index+1 );
			
			//자료실 업로드의 경우 fileName을 난수로 처리하여 저장
			if("ref_data".equals(menuId)){
				fileName = UUID.randomUUID().toString()+"."+extension;
			}
			
			File dir = new File(filePath);
			if(!dir.exists()) {
				dir.mkdirs();
			}
			
            byte fileData[] = files.getBytes();
            fos = new FileOutputStream(filePath+"/"+fileName);
            fos.write(fileData);
            
		}catch(Exception e){
			e.printStackTrace();
			result = "E";
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		resultMap.put("file_group_id", fileGroupId);
		resultMap.put("file_name", fileName);
		resultMap.put("file_name_org", fileNameOrg);
		resultMap.put("file_path", filePath);
		resultMap.put("file_ext", extension);
		resultMap.put("file_size", fileSize/1024);
		resultMap.put("result", result);
		
		return resultMap;
	}
	
	public void download(String filePath,String fileName,String fileNameOrg, HttpServletRequest request, HttpServletResponse response)throws Exception {
        try {
        	String realPath = filePath+"/"+fileName;
        	File file = new File(realPath);
            if (!file.exists()) {
            	response.sendRedirect("file_error");
            }
        	response.setContentType("application/octet-stream; charset=utf-8"); 
            response.setContentLength((int) file.length()); 
            String browser = getBrowser(request); 
            String disposition = "";
            if ( !"".equals(fileNameOrg)) {
            	disposition=getDisposition(fileNameOrg, browser);
            }else {
            	disposition=getDisposition(fileName, browser);
            }
			/*response.setHeader("Content-type", "application/force-download");*/
            //response.setHeader("Content-type", "image/jpeg");
            response.setHeader("Content-Disposition", disposition); 
            response.setHeader("Content-Transfer-Encoding", "binary");
            OutputStream out = response.getOutputStream(); 
            FileInputStream fis = null; fis = new FileInputStream(file); 
            FileCopyUtils.copy(fis, out); 
            
            if (fis != null) fis.close(); 
            out.flush(); 
            out.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("IOException");
            ex.printStackTrace();
        } finally {
        	//Log Write
        }
    }
	//
	public void download2(String fileName,HttpServletRequest request, HttpServletResponse response)throws Exception {
		String excelTemplatefile = propertyService.getString("excelTemplatefile"); //경로
		try {
        	String realPath = excelTemplatefile+"/"+fileName;//copy된 엑셀파일 위치
        	File file = new File(realPath);
            if (!file.exists()) {
            	response.sendRedirect("file_error");
            }
 
        	response.setContentType("application/octet-stream; charset=utf-8"); 
            response.setContentLength((int) file.length());
            String browser = getBrowser(request); 
            String disposition = "";
            if ( !"".equals(fileName)) {
            	disposition=getDisposition(fileName, browser);
            }else {
            	disposition=getDisposition(fileName, browser);
            }
			/* response.setHeader("Content-type", "application/force-download"); */
            response.setHeader("Content-Disposition", disposition); 
            response.setHeader("Content-Transfer-Encoding", "binary");
            OutputStream out = response.getOutputStream(); 
            FileInputStream fis = null; fis = new FileInputStream(file); 
            FileCopyUtils.copy(fis, out); 
            
            if (fis != null) fis.close(); 
            out.flush(); 
            out.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException");
        } catch (IOException ex) {
            System.out.println("IOException");
        } finally {
        	//Log Write
        }
    }
	
	private String getDisposition(String filename, String browser) throws UnsupportedEncodingException { 
		String dispositionPrefix = "attachment;filename="; 
		//String dispositionPrefix = "inline;filename=";
		String encodedFilename = null; 
		if (browser.equals("MSIE")) { 
			encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20"); 
		} else if (browser.equals("Firefox")) { 
			encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\""; 
		} else if (browser.equals("Opera")) { 
			encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\""; 
		} else if (browser.equals("Chrome")) { 
			StringBuffer sb = new StringBuffer(); 
			for (int i = 0; i < filename.length(); i++) { 
				char c = filename.charAt(i); 
				if (c > '~') { 
					sb.append(URLEncoder.encode("" + c, "UTF-8")); 
				} else { 
					sb.append(c); 
				} 
			} 
			encodedFilename = sb.toString(); 
		} 
		return dispositionPrefix + encodedFilename; 
	}
	
	private String getBrowser(HttpServletRequest request) { 
		String header = request.getHeader("User-Agent"); 
		if (header.indexOf("MSIE") > -1 || header.indexOf("Trident") > -1) 
			return "MSIE"; 
		else if (header.indexOf("Chrome") > -1) 
			return "Chrome"; 
		else if (header.indexOf("Opera") > -1) 
			return "Opera"; 
		return "Firefox"; 
	}
	
	public static String getRemoteAddr(HttpServletRequest request) {
        String ip = null;
        ip = request.getHeader("X-Forwarded-For");
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("Proxy-Client-IP"); 
        } 
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("WL-Proxy-Client-IP"); 
        } 
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("HTTP_CLIENT_IP"); 
        } 
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("X-Real-IP"); 
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("X-RealIP"); 
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("REMOTE_ADDR");
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getRemoteAddr(); 
        }
        return ip;
    }
	
	public void deleteFile(String filePath, String fileName) throws Exception{ 
		String fileFullPath = filePath+"/"+fileName;
		File file = new File(fileFullPath); 
		if( file.exists() ){ 
			if(file.delete()){
				System.out.println("파일삭제 성공"); 
			}else{
				System.out.println("파일삭제 실패");
			}	
		}else{ 
			System.out.println("파일이 존재하지 않습니다."); 
		} 
	}

}


