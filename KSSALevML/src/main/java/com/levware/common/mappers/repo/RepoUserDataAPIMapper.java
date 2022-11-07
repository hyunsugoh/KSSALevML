package com.levware.common.mappers.repo;
/**
* MyBatis로 Repository DataBase에 연동 Mapper 클래스  
* <p><b>NOTE:</b> 
* 
* @author 최진
* @since 2019.03.05
* @version 1.0
* @see
*
* <pre>
* == 개정이력(Modification Information) ==
*
* 수정일	수정자	수정내용
* -------	--------	---------------------------
* 2019.03.05	최진	최초 생성
*
* </pre>
*/

import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

import org.apache.ibatis.annotations.Param;

import com.levware.user.service.OlapConditionVO;
import com.levware.user.service.OlapObjectDetailInfoVO;
import com.levware.user.service.OlapObjectRelVO;
import com.levware.user.service.OlapObjectVO;
import com.levware.user.service.OlapSavedDataDelVO;
import com.levware.user.service.OlapSavedDataVO;

@Mapper("repoUserDataApiMapper")
public interface RepoUserDataAPIMapper {

	public List<OlapObjectVO> getObjectSelect() throws Exception;
	
	public List<OlapObjectRelVO> getObjectRelSelect(String stdTableName) throws Exception;
	
	public List<OlapObjectDetailInfoVO> getObjectDetailInfo(String tableName) throws Exception;
	
	public List<OlapConditionVO> getConditionData(String tableName) throws Exception; 
	
	/**
	 *  
	 * 로그 인서트
	 * @return 
	 * 
	 * @throws Exception
	 */
	public void logHisInsert(String userId) throws Exception;

	public int getCountUserSaveSeq(String userName) throws Exception;

	
	public void insertUserDataset(Map<String, Object> setData) throws Exception;

	public List<OlapSavedDataVO> getUserSavedDataList(String userName) throws Exception;

	public void deleteUserDataset(OlapSavedDataDelVO setData) throws Exception;

	public void updateUserDataset(Map<String, Object> setData) throws Exception;

	public String getStandDateCol(String tbName01) throws Exception;

	public int getCountQryHistSeq(String userName) throws Exception;

	public void inserSelectHistory(Map<String, Object> setData);
	
	public List<Map<String, Object>> getSelectList(@Param("tableName") String tableName,@Param("columnName") String columnName) throws Exception;
	
	
	public List<Map<String, Object>> getUnitList(String unitParam) throws Exception;

	public List<Map<String, Object>> getUnitRelInfo(String tableName);
	
	public List<Map<String, Object>> getUnitCalc(String grpParam) throws Exception;

	public List<Map<String, Object>> getUnitDyValue(@Param("tbName")  String tbName, @Param("uCode") String uCode, @Param("targetColName") String targetColName, @Param("value") String value);

	public List<Map<String, Object>> getSelectASHistoryGridData(String bomId) throws Exception;

}
