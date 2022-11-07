package com.levware.rb.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.base.Objects;
import com.levware.common.NumberUtil;
import com.levware.common.StringUtil;
import com.levware.common.mappers.repo.MLMapper;
import com.levware.common.mappers.repo.RBMapper;
import com.levware.ml.service.MLService;
import com.levware.rb.service.RBService;
import java.util.Map.Entry;
import java.util.Set;

@Service("rBService")
public class RBServiceImpl implements RBService {

	private static final Logger LOGGER = LogManager.getLogger(RBServiceImpl.class);

	@Resource(name = "rBMapper")
	private RBMapper rBMapper;

	@Resource(name = "mLMapper")
	private MLMapper mLMapper;

	@Resource(name = "mLService")
	private MLService mLService;

	private List<Map<String, Object>> _materialList = null;
	private List<Map<String, Object>> _productGroupInfo = null;
	private List<Map<String, Object>> _productGroupHierInfo = null;
	private List<Map<String, Object>> _pumpTypeGroupInfo = null;

	private List<Map<String, Object>> _sealTypeList = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> _apiPlanList = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> _noteList = new ArrayList<Map<String, Object>>();

	/*
	 * ==============================================================
	 * 
	 * 메인 Function
	 * 
	 * ==============================================================
	 */

	/*
	 * R_TYPE - 1 : FTA, END_USER에서 설정된 Seal - 2 : 우선순위로 설정된 Seal
	 */

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> predictSealByRuleBased(Map<String, Object> param) throws Exception {
		_sealTypeList.clear();
		_apiPlanList.clear();
		_noteList.clear();

		// 최종 결과 Map
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> result_items = new ArrayList<Map<String, Object>>();

		if (!(boolean) param.get("target4_check")) { // 실행체크가 되지 않은 경우

			// Seal 정보 추천 아이템 목록
			List<Map<String, Object>> predict_itemList = param.get("predict_list") == null
					? new ArrayList<Map<String, Object>>()
					: (List<Map<String, Object>>) param.get("predict_list");

			for (Map<String, Object> item : predict_itemList) {

				Map<String, Object> result_item = new HashMap<String, Object>(); // 결과

				Map<String, Object> itemRuleResult = new HashMap<String, Object>();
				itemRuleResult.put("RST", new ArrayList<Map<String, Object>>()); // 추천결과
				itemRuleResult.put("NOTE", new ArrayList<Map<String, Object>>());
				itemRuleResult.put("PROC", new ArrayList<Map<String, Object>>());

				result_item.put("predict_idx", String.valueOf(item.get("NO"))); // Item No.
				result_item.put("predict_msg", "complete");
				result_item.put("RESULT", itemRuleResult);
				result_item.put("param", item); // 조건

				result_items.add(result_item); // 결과리스트를 Add
			}
			result.put("RULE_RESULT", result_items);

			return result;
		}

		// ----------------------------
		// 공용 변수
		// ----------------------------
		_productGroupInfo = mLMapper.getGroupingInfo("product"); // Product Grouping Info.
		_pumpTypeGroupInfo = mLMapper.getGroupingInfo("pumpType");

		_materialList = rBMapper.selectMaterialList(null); // Material List

		_productGroupHierInfo = mLMapper.getGroupingHier(null); // Product Group Hierarchy

		// 단위변환처리 정보
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		List<Map<String, Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();// 단위코드정보
		List<Map<String, Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();// 단위변환정보
		List<Map<String, Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();// Text 입력값 변환정보
		List<Map<String, Object>> listSsuChg = mLMapper.getSsuChglInfo();// SSU 변환 정보 (Visc 용)

		// Shaft Size 기본값 (EPC단계)
		Map<String, Object> ptm = new HashMap<String, Object>();
		ptm.put("MCD", "E003");
		List<Map<String, Object>> listShaftSizeEpc = getRuleComListType1(ptm);

		// Seal 정보 추천 아이템 목록
		List<Map<String, Object>> predict_itemList = param.get("predict_list") == null
				? new ArrayList<Map<String, Object>>()
				: (List<Map<String, Object>>) param.get("predict_list");

		try {
			// -------------------------------------------------
			// 아이템별 Seal 추천정보 도출
			// -------------------------------------------------
			for (Map<String, Object> item : predict_itemList) {

				Map<String, Object> result_item = new HashMap<String, Object>(); // 결과

				// ----------------------------
				// 결과 변수
				// ----------------------------
				// int iPIdx = 0; // 추천 Index
				List<Map<String, Object>> sealRstList = new ArrayList<Map<String, Object>>(); // Seal Type 추천 결과 List
				List<Map<String, Object>> material1RstList = new ArrayList<Map<String, Object>>(); // Material 1st 추천 결과
																									// List
				List<Map<String, Object>> material2RstList = new ArrayList<Map<String, Object>>(); // Material 2nd 추천 결과
																									// List
				List<Map<String, Object>> material3RstList = new ArrayList<Map<String, Object>>(); // Material 3rd 추천 결과
																									// List
				List<Map<String, Object>> material4RstList = new ArrayList<Map<String, Object>>(); // Material 4th 추천 결과
																									// List
				List<Map<String, Object>> material1OutRstList = new ArrayList<Map<String, Object>>(); // Material 1st 추천
																										// 결과 List (Out)
				List<Map<String, Object>> material2OutRstList = new ArrayList<Map<String, Object>>(); // Material 2nd 추천
																										// 결과 List (Out)
				List<Map<String, Object>> material3OutRstList = new ArrayList<Map<String, Object>>(); // Material 3rd 추천
																										// 결과 List (Out)
				List<Map<String, Object>> material4OutRstList = new ArrayList<Map<String, Object>>(); // Material 4th 추천
																										// 결과 List (Out)
				List<Map<String, Object>> planRstList = new ArrayList<Map<String, Object>>(); // API Plan 추천 결과 List
				List<Map<String, Object>> noteRstList = new ArrayList<Map<String, Object>>(); // 추천결과 특이사항(Note) List
				List<Map<String, Object>> procRstList = new ArrayList<Map<String, Object>>(); // 추천결과 중간과정이력 List

				boolean isProcess = true; // 계속 진행 여부
				boolean isNo_Process_split = true; // 계속 진행 여부 (Split 미적용여부)
				boolean isNo_Process_fta = true; // 계속 진행 여부 (FTA 미적용여부)

				// Product Group Info (Array)
				String saProductGroupStr = (getGroupingStr(String.valueOf(item.get("PRODUCT")), _productGroupInfo,
						_productGroupHierInfo));
				String[] saProductGroup = saProductGroupStr.split("[+]");
				// Product Info (Array)
				String[] saProduct = (getProductStr(String.valueOf(item.get("PRODUCT")), _productGroupInfo, 0))
						.split("[+]");
				// Item No.
				String sNo = String.valueOf(item.get("NO"));

				LOGGER.debug("Rule 추천 Item : " + item);
				LOGGER.debug("Product Group : " + Arrays.asList(saProductGroup));
				LOGGER.debug("Product : " + Arrays.asList(saProduct));
				System.out.println(" Product Group : " + Arrays.asList(saProductGroup));
				System.out.println(" Product : " + Arrays.asList(saProduct));

				// 입력조건 단위환산처리
				convToStdUnit(item, "3", engine, listUnitCode, listUnitChg, listTransTxtVal, listSsuChg,
						listShaftSizeEpc);

				LOGGER.debug("Rule 추천 Item(Conv.) : " + item);
				System.out.println("단위변환 ITEM Ok : " + item);

				// Pump Type 별 Hori, Veri 구분
				String sPumpTypeG = getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")), _pumpTypeGroupInfo, null);
				String sEquipmentType = "";
				if ("OH1".equals(sPumpTypeG) || "OH2".equals(sPumpTypeG) || "BB".equals(sPumpTypeG)
						|| "NA".equals(StringUtil.get(item.get("PUMP_TYPE")))) {
					sEquipmentType = "H";
				} else {
					// if (!"VS4".equals(sPumpTypeG)) {
					sEquipmentType = "V";
					// }else {
					// sEquipmentType = "H";
					// }
				}
				item.put("PUMP_TYPE_HV", sEquipmentType);

				// Seal 지정한 경우
				if (!"".equals(StringUtil.get(item.get("SEAL_INNER_DIR")))
						|| !"".equals(StringUtil.get(item.get("SEAL_OUTER_DIR")))) {
					item.put("IS_DIR_SEAL", "Y");
				}

				// 파라미터 Map
				Map<String, Object> fp = new HashMap<String, Object>();
				fp.put("sealRstList", sealRstList);
				// fp.put("materialRstList", materialRstList);
				fp.put("planRstList", planRstList);
				fp.put("noteRstList", noteRstList);
				fp.put("procRstList", procRstList);
				fp.put("material1RstList", material1RstList);
				fp.put("material2RstList", material2RstList);
				fp.put("material3RstList", material3RstList);
				fp.put("material4RstList", material4RstList);
				fp.put("material1OutRstList", material1OutRstList);
				fp.put("material2OutRstList", material2OutRstList);
				fp.put("material3OutRstList", material3OutRstList);
				fp.put("material4OutRstList", material4OutRstList);
				fp.put("saProductGroup", saProductGroup);
				fp.put("saProduct", saProduct);

				// Item에 추가정보 Set
				item.put("P_IDX", 0); // idx 초기값 설정

				// -----------------------------
				// End User Check
				// -----------------------------
				if (isProcess)
					isProcess = step_endUser(item, fp);
				LOGGER.debug("End User isProcess Check : " + isProcess);

				// FTA 체크
				if (isProcess)
					isNo_Process_fta = step_fta(item, fp);
				LOGGER.debug("FTA isNoProcess Check : " + isNo_Process_fta);
				// FTA로 등록된 정보가 있을 경우
				if (!isNo_Process_fta) {
					step_arrangement_pre(item, fp);
					step_arrangement(item, fp);
					step_api_plan(item, fp);
					step_api_plan_after_process(item, fp);
					step_ftp_after_process(item, fp);
				}

				// Split 적용 체크
				if (isProcess)
					isNo_Process_split = step_split(item, fp, param);
				LOGGER.debug("SPlit isNoProcess Check : " + isNo_Process_split);
				System.out.println(fp);

				// 사용자 입력조건에 따른 Arrangement 우선적용
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					step_arrangement_pre(item, fp);

				System.out.println(fp);

				// 우선순위 적용
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					isProcess = step_prefered(item, fp, param);

				if (item.containsKey("__IS_PRODUCT_WATER_GUIDE") && item.get("__IS_PRODUCT_WATER_GUIDE").equals("Y")
						&& sPumpTypeG.equals("VS4")) {
					fp.put("material1RstList", new ArrayList<Map<String, Object>>());
					fp.put("material2RstList", new ArrayList<Map<String, Object>>());
					fp.put("material3RstList", new ArrayList<Map<String, Object>>());
					fp.put("material4RstList", new ArrayList<Map<String, Object>>());
				}

				// __ADD_PROCESS_0 : Arrangement 3으로 Seal Model 부터 추천 추가
				// __ADD_PROCESS_1 : A3, 53B Plan이 결정된 경우 BF 압력추가하여 Type 선정부터 재처리
				// __ADD_PROCESS_3 : 선정된 Seal (QBQLZ) 을 삭제하고 Arrangement3으로 다시 Seal Model 추천
				// __ADD_PROCESS_4 : API Plan에서 설정에 따른 재처리 - Seal Model 부터 재시작

				// -----------------------------
				// Type Check : Type A, B, C
				// -----------------------------
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					step_type(item, fp);
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					step_type_after(item, fp);

				// -----------------------------
				// Arrangement Check
				// -----------------------------
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					step_arrangement(item, fp);
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					step_arrangement_after(item, fp);

				// -----------------------------
				// Seal Type Check
				// -----------------------------
				if (isSealProcess(item, fp)) { // Seal Type 추천을 하지 않는경우 체크
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type(item, fp);
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type_after(item, fp);
				}

				// -----------------------------
				// API Plan Check
				// -----------------------------
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					step_api_plan(item, fp);

				// API Plan에서 __ADD_PROCESS_4 설정에 따른 재처리
				// Seal Model 부터 재시작
				if ("N".equals(StringUtil.get(item.get("__ADD_PROCESS_4")))) {

					// __ADD_PROCESS_4 설정 시 "Y" Case가 발생한 경우는
					// Arrangement2변경 후 다시 Seal 선택부터 진행되므로 Y케이스를 적용하는 기존 프로세스 결과를 삭제하지 않는다.
					if (!"Y".equals(StringUtil.get(item.get("__ADD_PROCESS_4_OPT")))) {
						// 프로세스 추천결과 삭제
						Iterator iter_remove = sealRstList.iterator();
						while (iter_remove.hasNext()) {
							Map<String, Object> m = (HashMap<String, Object>) iter_remove.next();
							Map<String, Object> addInfo = getMapMapData(m, "ADD_INFO");
							// R_TYPE이 빈값인 Seal 정보를 제거한다.
							if ("".equals(StringUtil.get(addInfo.get("R_TYPE")))) {
								iter_remove.remove();
							}
						}
					}

					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type(item, fp);
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type_after(item, fp);
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_api_plan(item, fp);

					item.put("__ADD_PROCESS_4", "Y");
				}

				// __ADD_PROCESS_3 : 선정된 Seal (QBQLZ) 을 삭제하고 Arrangement3으로 다시 SealModel 추천
				// N : 진행
				// Y : 완료
				if ("N".equals(StringUtil.get(item.get("__ADD_PROCESS_3")))
						&& !"Y".equals(StringUtil.get(item.get("IS_DIR_SEAL")))) {

					// QBQLZ 추천결과 삭제
					Iterator iter_remove = sealRstList.iterator();
					while (iter_remove.hasNext()) {
						Map<String, Object> m = (HashMap<String, Object>) iter_remove.next();

						if ("QBQLZ".equals(StringUtil.get(m.get("P_VAL")))) {
							iter_remove.remove();
						}
					}

					item.put("ARRANGEMENT", "3");

					// -----------------------------
					// Seal Type Check
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type(item, fp);
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type_after(item, fp);

					// -----------------------------
					// API Plan Check
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_api_plan(item, fp);
				}

				// -----------------------------
				// Seal Type 재설정 by API Plan 결과
				// -----------------------------
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					step_seal_type_set_after_api_plan(item, fp);

				// -----------------------------
				// 직접 지정된 Seal 정보 Set
				// -----------------------------
				if (isProcess && isNo_Process_split && isNo_Process_fta)
					step_api_plan_after_process(item, fp);

				// __ADD_PROCESS_0 : Arrangement 3으로 SealModel 추천 추가
				// N : 진행
				// Y : 완료

				if ("N".equals(StringUtil.get(item.get("__ADD_PROCESS_0")))
						&& !"Y".equals(StringUtil.get(item.get("IS_DIR_SEAL")))) {

					LOGGER.debug("Arrangement 3으로 SealModel 추천 추가 ");

					item.put("ARRANGEMENT", "3");

					// -----------------------------
					// Seal Type Check
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type(item, fp);
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type_after(item, fp);

					// -----------------------------
					// API Plan Check
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_api_plan(item, fp);

					// -----------------------------
					// Seal Type 재설정 by API Plan 결과
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type_set_after_api_plan(item, fp);

					// -----------------------------
					// 직접 지정된 Seal 정보 Set
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_api_plan_after_process(item, fp);

					item.put("__ADD_PROCESS_0", "Y");
				}

				// __ADD_PROCESS_1 : A3, 53B Plan이 결정된 경우 BF 압력추가하여 재처리
				// N : 진행
				// Y : 완료

				// 53B Plan 결과 도출에 따른 재처리 여부 확인 및 처리
				if (setProcessReset(item, "__ADD_PROCESS_1", fp)
						&& !"Y".equals(StringUtil.get(item.get("IS_DIR_SEAL")))) {

					LOGGER.debug("A3, 53B Plan이 결정된 경우 BF 압력추가하여 재처리 ");
					//System.out.println("__process_restart__ Seal List : " + sealRstList);
					//System.out.println("__process_restart__ Plan List : " + sealRstList);

					// process 간에 분기점 관련 정보 초기화
					item.put("process_step", "");

					// 우선순위 적용
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_prefered(item, fp, param);

					// -----------------------------
					// Type Check : Type A, B, C
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						isProcess = step_type(item, fp);
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						isProcess = step_type_after(item, fp);

					// -----------------------------
					// Arrangement Check
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_arrangement(item, fp);
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_arrangement_after(item, fp);

					// -----------------------------
					// Seal Type Check
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type(item, fp);
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type_after(item, fp);

					// -----------------------------
					// API Plan Check
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_api_plan(item, fp);

					// -----------------------------
					// Seal Type 재설정 by API Plan 결과
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_seal_type_set_after_api_plan(item, fp);

					// -----------------------------
					// 직접 지정된 Seal 정보 Set
					// -----------------------------
					if (isProcess && isNo_Process_split && isNo_Process_fta)
						step_api_plan_after_process(item, fp);

					// 추가된 압력정보를 원래값으로 변경한다.
					setResetItem(item, "__ADD_PROCESS_1");
				}

				// -----------------------------
				// Material Set
				// -----------------------------
				if (isProcess)
					step_material(item, fp);

				// -----------------------------
				// Check
				// -----------------------------
				// C1 Grade 체크
				// if(isProcess) {
				step_c1_chk(item, fp);
				step_c1_chk2(item, fp);
				// }

				// 재질선정 후 추가작업
				if (isProcess) {
					step_seal_type_etc(item, fp);
				}

				// Graph 압력 체크
				// if(isProcess) {
				step_restriction_chk(item, fp);
				// }

				// 상위 재질 한건 유지
				// if(isProcess) {
				step_result_filter(item, fp);
				// }

				// -----------------------------
				// 추가적인 Process
				// -----------------------------
				if (isProcess)
					step_additional_process(item, fp);

				if (item.containsKey("__IS_PRODUCT_WATER_GUIDE") && item.get("__IS_PRODUCT_WATER_GUIDE").equals("Y")
						&& sPumpTypeG.equals("VS4")) {
					String _seal = "";
					String _m1 = "";
					String _m2 = "";
					String _m3 = "";
					String _m4 = "";
					String _plan = "";
					Iterator<Map<String, Object>> rIterator = ((List<Map<String, Object>>) fp.get("FINAL_LIST"))
							.iterator();
					while (rIterator.hasNext()) {
						Map<String, Object> sm = (HashMap<String, Object>) rIterator.next();
						if (sm.get("P_IDX").equals(1)) {
							_seal = sm.get("SEAL").toString();
							_m1 = sm.get("MTRL1").toString();
							_m2 = sm.get("MTRL2").toString();
							_m3 = sm.get("MTRL3").toString();
							_m4 = sm.get("MTRL4").toString();
							_plan = sm.get("PLAN").toString();
						} else {
							if (_seal.equals(sm.get("SEAL")) && _m1.equals(sm.get("MTRL1"))
									&& _m2.equals(sm.get("MTRL2")) && _m3.equals(sm.get("MTRL3"))
									&& _m4.equals(sm.get("MTRL4")) && _plan.equals(sm.get("PLAN"))) {
								rIterator.remove();
							}
						}
					}
				}
				
				/*
				if (fp.get("FINAL_LIST") != null) {
					String sNoteTmp = "Xyfluor 860 (X790)으로 대체 가능한지 검토하시오.";
					for (Map<String, Object> fMap : (List<Map<String, Object>>) fp.get("FINAL_LIST")) {
						if (((Map<String, Object>) fMap.get("MTRL3_ADD_INFO")).get("MTRL_CD").equals("G014")) {
							setResultNoteList(noteRstList, Integer.parseInt(fMap.get("P_IDX").toString()),sNoteTmp, "m,MTRL_CD:G014");
						} else if (fMap.get("MTRL_OUT3_ADD_INFO") != null
								&& ((Map<String, Object>) fMap.get("MTRL_OUT3_ADD_INFO")).get("MTRL_CD").equals("G014")) {
							setResultNoteList(noteRstList, Integer.parseInt(fMap.get("P_IDX").toString()),sNoteTmp, "m,MTRL_CD:G014");
						}
					}
				}*/
				

				//노트 처리용 변수 ---<<
				Iterator<Map<String, Object>> nIterator_c = null;
				List<Map<String, Object>> nIterator_c_tmp = new ArrayList<Map<String, Object>>();
				// >>---
				
				Iterator<Map<String, Object>> nIterator = _noteList.iterator();
				while (nIterator.hasNext()) {
					Map<String, Object> noteMap = (HashMap<String, Object>) nIterator.next();
					boolean flag = false;
					for(Map<String,Object> nMap : noteRstList) {
						if(noteMap!=null && noteMap.containsKey("NOTE") && noteMap.get("NOTE").equals(nMap.get("NOTE"))) {
							flag = true;
							break;
						}
					}
					if(!flag) {
						nIterator.remove();
					}
				}
				
				/*
				for(Map<String,Object> n : _noteList) {
					System.out.println(n);
					if(n.get("SEAL").equals("") || n.get("SEAL").equals(null)) {
						for(Map<String,Object> s : _sealTypeList) {
							if(n.get("P_IDX").equals(s.get("P_IDX"))) {
								n.put("SEAL", s.get("SEAL"));
								//break;
							}
						}
					}
					if(n.get("PLAN").equals("") || n.get("PLAN").equals(null)) {
						for(Map<String,Object> p : _apiPlanList) {
							if(n.get("P_IDX").equals(p.get("P_IDX"))) {
								n.put("PLAN", p.get("PLAN"));
								//break;
							}
						}
					}
				}*/
				
				//System.out.println("_sealTypeList===================================");
				//System.out.println(_sealTypeList);
				//System.out.println("_noteList 1===================================");
				//System.out.println(_noteList);
				
				if(fp!=null && fp.containsKey("FINAL_LIST")) {
					int idseq = 1; 
					for(Map<String,Object> fMap : (List<Map<String,Object>>)fp.get("FINAL_LIST")) { 
						fMap.put("P_ID_SEQ",idseq); // 결과 최종 순서 부여
						//for(Map<String,Object> map : _noteList) {
							
							/*
							String fPlan = fMap.get("PLAN")!=null?fMap.get("PLAN").toString():"";
							String nPlan = map.get("PLAN")!=null?map.get("PLAN").toString():"";
							if(fPlan.length()>0 && fPlan.indexOf("/")>0) {
								fPlan=fPlan.substring(0,fPlan.indexOf("/"));
							}
							if(nPlan.length()>0 && nPlan.indexOf("/")>0) {
								nPlan=fPlan.substring(0,nPlan.indexOf("/"));
							}
							
							if(map.get("P_IDX").equals(0)) { 
								map.put("P_ID_SEQ", 0); 
							}else if(fMap.get("SEAL").equals(map.get("SEAL").toString().replace("__","").replace("_CHK_1","")) && 
									fPlan.equals(nPlan)){ 
								map.put("P_ID_SEQ", idseq);
							}else if(fMap.get("SEAL").equals(map.get("SEAL").toString().replace("__","").replace("_CHK_1",""))){
								if(!map.containsKey("P_ID_SEQ")) {
									map.put("P_ID_SEQ", idseq);
								}
							}
							*/
						//} 
						idseq++;
					}
				}
				
				nIterator_c = _noteList.iterator();
				nIterator_c_tmp.clear();
				while (nIterator_c.hasNext()) {
					Map<String, Object> noteMap = (HashMap<String, Object>) nIterator_c.next();
					
					// TYPE =p (특정 Plan Note)
					// AddInfo의 PLAN 값을 가지고 있는 결과에 Note를 연결 및 추가
					if (noteMap.get("TYPE").equals("p")) {
						Map<String,Object> noteAddInfo = (HashMap<String,Object>)noteMap.get("ADD_INFO");

						boolean bIsAdd = false;
						for(Map<String,Object> fMap : (List<Map<String,Object>>)fp.get("FINAL_LIST")) { 
							//System.out.println("===>"+fMap.get("SEAL"));
							String fPlan = fMap.get("PLAN")!=null?fMap.get("PLAN").toString():"";
							for(String plan : fPlan.split("/")) {
								//System.out.println("===>"+fMap.get("PLAN"));
								//Plan Note는 추천목록이 생성된 후 만들어지므로 Idx별로 생성됨. -> idx비교하여 노트 설정
								if (noteAddInfo.get("PLAN").equals(plan) && noteMap.get("P_IDX").equals(fMap.get("P_IDX"))) {
									Map<String,Object> addNoteMap = new HashMap<String,Object>();
									addNoteMap.put("P_IDX",fMap.get("P_IDX"));
									addNoteMap.put("P_SEQ",noteMap.get("P_SEQ"));
									addNoteMap.put("P_ID_SEQ",fMap.get("P_ID_SEQ"));
									//addNoteMap.put("SEAL", fMap.get("SEAL"));
									//addNoteMap.put("PLAN", fMap.get("PLAN"));
									addNoteMap.put("SEAL", "");
									addNoteMap.put("PLAN", "");
									addNoteMap.put("TYPE", noteMap.get("TYPE"));
									addNoteMap.put("NOTE",noteMap.get("NOTE"));
									nIterator_c_tmp.add(addNoteMap);
									bIsAdd=true;
									break;
								}
							}
						}
						if(bIsAdd) {
							nIterator_c.remove();
						}

					// TYPE = s (특정 Seal Note)
					// AddInfo의 SEAK 값과 동일한 SEAL정보를 가지고 있는 결과에 Note를 연결 및 추가	-> 결과에 없는 경우 Note 삭제
					}else if (noteMap.get("TYPE").equals("s")) { // Seal관련 Note 경우
						Map<String,Object> noteAddInfo = (HashMap<String,Object>)noteMap.get("ADD_INFO");
						
						for(Map<String,Object> fMap : (List<Map<String,Object>>)fp.get("FINAL_LIST")) { 
							String fSeal = fMap.get("SEAL")!=null?fMap.get("SEAL").toString():"";
							if (noteAddInfo.get("SEAL").equals(fSeal)) {
								Map<String,Object> addNoteMap = new HashMap<String,Object>();
								addNoteMap.put("P_IDX", fMap.get("P_IDX"));
								addNoteMap.put("P_SEQ", noteMap.get("P_SEQ"));
								addNoteMap.put("P_ID_SEQ", fMap.get("P_ID_SEQ"));
								addNoteMap.put("SEAL", fSeal);
								addNoteMap.put("PLAN", "");
								addNoteMap.put("TYPE", noteMap.get("TYPE"));
								addNoteMap.put("NOTE", noteMap.get("NOTE"));
								nIterator_c_tmp.add(addNoteMap);
							}
						}
						//결과 Seal에 매칭에 상관없이 원 Note는 삭제
						//->되추천과정에서 Seal이 변경될수있음.
						nIterator_c.remove();
						
					}else { //특정 조건이 없는 경우
						
						boolean bIsAdd = false;
						for(Map<String,Object> fMap : (List<Map<String,Object>>)fp.get("FINAL_LIST")) { 
							if(noteMap.get("P_IDX").equals(fMap.get("P_IDX"))  
									&& String.valueOf(noteMap.get("TYPE")).equals("")) { //특정 조건이 없는 경우
								
								Map<String,Object> addNoteMap = new HashMap<String,Object>();
								addNoteMap.put("P_IDX",fMap.get("P_IDX"));
								addNoteMap.put("P_SEQ",noteMap.get("P_SEQ"));
								addNoteMap.put("P_ID_SEQ",fMap.get("P_ID_SEQ"));
								//addNoteMap.put("SEAL", fMap.get("SEAL"));
								//addNoteMap.put("PLAN", fMap.get("PLAN"));
								addNoteMap.put("SEAL", "");
								addNoteMap.put("PLAN", "");
								addNoteMap.put("TYPE", noteMap.get("TYPE"));
								addNoteMap.put("NOTE",noteMap.get("NOTE"));
								nIterator_c_tmp.add(addNoteMap);
								
								bIsAdd=true;
							}
						}
						if(bIsAdd) {
							nIterator_c.remove();					
						}
					}
				}
				for(Map<String,Object> mTmp : nIterator_c_tmp) {
					_noteList.add(mTmp);
				}
				
				//System.out.println("_noteList 2===================================");
				//System.out.println(_noteList);
/*				
				nIterator_c = _noteList.iterator();
				nIterator_c_tmp.clear();
				while (nIterator_c.hasNext()) {
					Map<String, Object> noteMap = (HashMap<String, Object>) nIterator_c.next();
					
					boolean bIsAdd = false;
					for(Map<String,Object> fMap : (List<Map<String,Object>>)fp.get("FINAL_LIST")) { 
						if(noteMap.get("P_IDX").equals(fMap.get("P_IDX"))  
								&& String.valueOf(noteMap.get("TYPE")).equals("")) { //특정 조건이 없는 경우
							
							Map<String,Object> addNoteMap = new HashMap<String,Object>();
							addNoteMap.put("P_IDX",fMap.get("P_IDX"));
							addNoteMap.put("P_SEQ",noteMap.get("P_SEQ"));
							addNoteMap.put("P_ID_SEQ",fMap.get("P_ID_SEQ"));
							//addNoteMap.put("SEAL", fMap.get("SEAL"));
							//addNoteMap.put("PLAN", fMap.get("PLAN"));
							addNoteMap.put("SEAL", "");
							addNoteMap.put("PLAN", "");
							addNoteMap.put("TYPE", noteMap.get("TYPE"));
							addNoteMap.put("NOTE",noteMap.get("NOTE"));
							nIterator_c_tmp.add(addNoteMap);
							
							bIsAdd=true;
						}
					}
					if(bIsAdd) {
						nIterator_c.remove();					
					}
				}
				for(Map<String,Object> mTmp : nIterator_c_tmp) {
					_noteList.add(mTmp);
				}
			
				
*/	
				
				//공통타입의 Note에 대하여 전체 추천된 항목만큼 생성 후 O 인덱스 항목을 remove 한다.
				//P_ID_SEQ 가 설정된 후 처리.
				nIterator_c = _noteList.iterator();
				nIterator_c_tmp.clear();
				while (nIterator_c.hasNext()) {
					Map<String, Object> noteMap = (HashMap<String, Object>) nIterator_c.next();
					
					if(String.valueOf(noteMap.get("TYPE")).equals("c")) { // 인덱스가 0이고 Type="c" (공통 Note일 경우)
						
						for(Map<String,Object> fMap : (List<Map<String,Object>>)fp.get("FINAL_LIST")) { 
							Map<String,Object> addNoteMap = new HashMap<String,Object>();
							addNoteMap.put("P_IDX",fMap.get("P_IDX"));
							addNoteMap.put("P_SEQ",noteMap.get("P_SEQ"));
							addNoteMap.put("P_ID_SEQ",fMap.get("P_ID_SEQ"));
							addNoteMap.put("SEAL","");
							addNoteMap.put("PLAN","");
							addNoteMap.put("TYPE","");
							addNoteMap.put("NOTE",noteMap.get("NOTE"));
							nIterator_c_tmp.add(addNoteMap);
						}
						nIterator_c.remove();
					}
				}
				for(Map<String,Object> mTmp : nIterator_c_tmp) {
					_noteList.add(mTmp);
				}
				
				//System.out.println("_noteList 3===================================");
				//System.out.println(_noteList);
				
				// 노트 추가
				// G014 재질에 대한 추가 노트
				if (fp.get("FINAL_LIST") != null) {
					String sNoteTmp = "Xyfluor 860 (X790)으로 대체 가능한지 검토하시오.";
					for (Map<String, Object> fMap : (List<Map<String, Object>>) fp.get("FINAL_LIST")) {
						if (((Map<String, Object>) fMap.get("MTRL3_ADD_INFO")).get("MTRL_CD").equals("G014") ||
								(fMap.get("MTRL_OUT3_ADD_INFO") != null
									&& ((Map<String, Object>) fMap.get("MTRL_OUT3_ADD_INFO")).get("MTRL_CD").equals("G014"))
							) {
							
							Map<String,Object> addNoteMap = new HashMap<String,Object>();
							addNoteMap.put("P_ID_SEQ", fMap.get("P_ID_SEQ"));
							addNoteMap.put("P_IDX", fMap.get("P_IDX"));
							addNoteMap.put("P_SEQ", getMaxSeq2(_noteList));
							addNoteMap.put("SEAL", "");
							addNoteMap.put("PLAN", "");
							addNoteMap.put("TYPE","");
							addNoteMap.put("NOTE", sNoteTmp);
							_noteList.add(addNoteMap);
						}
					}
				}
				
				
				// ------------------------------------------------------------
				// 사용자 지정인자에 의한 Arrangement와 결과 Arrangement의 비교 1
				// ------------------------------------------------------------
				//최종목록에서 Note 추가하게 변경 : 21.10
				if (!"".equals(StringUtil.get(item.get("API_PLAN_DIR_ARRANGEMENT")))) { // 직접지정 Arrangement가 있을 경우
					
					int iAPI_PLAN_DIR_ARRANGEMENT = NumberUtil.toInt(item.get("API_PLAN_DIR_ARRANGEMENT"));
					
					for (Map<String, Object> fMap : (List<Map<String, Object>>) fp.get("FINAL_LIST")) {
						Map<String,Object> sealAddInfo = fMap.get("SEAL_ADD_INFO")==null?new HashMap<String,Object>():(HashMap<String,Object>)fMap.get("SEAL_ADD_INFO");
						
						if (iAPI_PLAN_DIR_ARRANGEMENT != NumberUtil.toInt(sealAddInfo.get("ARRANGEMENT"))){
							
							String sNote = "지정 Plan의 Arrangement : " + item.get("API_PLAN_DIR_ARRANGEMENT")
							+ " / 추천 Plan의 Arrangement : " + sealAddInfo.get("ARRANGEMENT");
							
							Map<String,Object> addNoteMap = new HashMap<String,Object>();
							addNoteMap.put("P_ID_SEQ", fMap.get("P_ID_SEQ"));
							addNoteMap.put("P_IDX", fMap.get("P_IDX"));
							addNoteMap.put("P_SEQ", getMaxSeq2(_noteList));
							addNoteMap.put("SEAL", "");
							addNoteMap.put("PLAN", "");
							addNoteMap.put("TYPE","");
							addNoteMap.put("NOTE", sNote);
							_noteList.add(addNoteMap);
							
						}
					}
				}
				
				// --------------------------------
				// 지정 Plan과 추천결과 Plan 비교  
				// --------------------------------
				// 최종목록에서 Note 추가하게 변경 : 21.10
				if (!"".equals(StringUtil.get(item.get("API_PLAN_DIR")))) { // 지정 Plan이 있을 경우
					
					for (Map<String, Object> fMap : (List<Map<String, Object>>) fp.get("FINAL_LIST")) {
						
						boolean bIsPlanEqual = true; // 지정Plan과 추천Plan 일치여부
						
						// plan 비교
						/*
						for (String sDirPlan : StringUtil.get(item.get("API_PLAN_DIR")).trim().split("/")) { // 지정 Plan
							if ("61".equals(sDirPlan)) {
								continue;
							} else {
								boolean bIsPlanTmp = false;
								for (String sPlan : ("" + fMap.get("PLAN")).split("/")) { // 추천 Plan
									if (sDirPlan.equals(sPlan)) {
										bIsPlanTmp = true;
										break;
									}
								}
								if (!bIsPlanTmp) {
									bIsPlanEqual = false;
								}
							}
						}*/
						
						//지정Plan이 추천Plan에 모두 포함되어 있는지 체크한다.
						for (String sSrcPlan : StringUtil.get(item.get("API_PLAN_DIR")).trim().split("/")) { // 지정 Plan
							if ("61".equals(sSrcPlan)) {
								continue;
							} else {
								boolean bIsPlanTmp = false;
								for (String sPlan : ("" + fMap.get("PLAN")).split("/")) { // 추천 Plan
									if (sSrcPlan.equals(sPlan)) {
										bIsPlanTmp = true;
										break;
									}
								}
								if (!bIsPlanTmp) {
									bIsPlanEqual = false;
								}
							}
						}
						
						//지정Plan이 추천Plan에 포함될경우 반대로도 체크한다.
						if(bIsPlanEqual) {
							for (String sSrcPlan : StringUtil.get(fMap.get("PLAN")).trim().split("/")) { // 추천 Plan
								if ("61".equals(sSrcPlan)) {
									continue;
								} else {
									boolean bIsPlanTmp = false;
									for (String sPlan : ("" + StringUtil.get(item.get("API_PLAN_DIR")).trim()).split("/")) { // 지정 Plan
										if (sSrcPlan.equals(sPlan)) {
											bIsPlanTmp = true;
											break;
										}
									}
									if (!bIsPlanTmp) {
										bIsPlanEqual = false;
									}
								}
							}
						}
						
						//String sDirPlan = StringUtil.get(item.get("API_PLAN_DIR")).trim();
						//String sPlan = StringUtil.get(fMap.get("PLAN")).trim();
						//sDirPlan = sDirPlan.replace("61", "").replace("/", "");
						//sPlan = sPlan.replace("61", "").replace("/", "");
						
						//if(!sDirPlan.equals(sPlan)) {
						//	bIsPlanEqual = false;
						//}
						
						if (!bIsPlanEqual) {
							Map<String,Object> addNoteMap = new HashMap<String,Object>();
							addNoteMap.put("P_ID_SEQ", fMap.get("P_ID_SEQ"));
							addNoteMap.put("P_IDX", fMap.get("P_IDX"));
							addNoteMap.put("P_SEQ", getMaxSeq2(_noteList));
							addNoteMap.put("SEAL", "");
							addNoteMap.put("PLAN", "");
							addNoteMap.put("TYPE","");
							addNoteMap.put("NOTE", "지정 Plan과 추천결과 Plan이 상이합니다");
							_noteList.add(addNoteMap);
						}
					}

				}
				
				// --------------------------------
				// 지정 Single/Dual 선택값과 추천결과 비교
				// --------------------------------
				// 최종목록에서 Note 추가하게 변경 : 21.10
				if (!"".equals(StringUtil.get(item.get("S_D_GB")))) {
					String sSDgb = StringUtil.get(item.get("S_D_GB"));
					
					for (Map<String, Object> fMap : (List<Map<String, Object>>) fp.get("FINAL_LIST")) {
						Map<String,Object> sealAddInfo = fMap.get("SEAL_ADD_INFO")==null?new HashMap<String,Object>():(HashMap<String,Object>)fMap.get("SEAL_ADD_INFO");
						
						int iSealArrangement = NumberUtil.toInt(sealAddInfo.get("ARRANGEMENT"));
						
						if (("D".equals(sSDgb) && iSealArrangement < 2)
								|| ("S".equals(sSDgb) && iSealArrangement >= 2)) {
							
							Map<String,Object> addNoteMap = new HashMap<String,Object>();
							addNoteMap.put("P_ID_SEQ", fMap.get("P_ID_SEQ"));
							addNoteMap.put("P_IDX", fMap.get("P_IDX"));
							addNoteMap.put("P_SEQ", getMaxSeq2(_noteList));
							addNoteMap.put("SEAL", "");
							addNoteMap.put("PLAN", "");
							addNoteMap.put("TYPE","");
							addNoteMap.put("NOTE", "지정 Single/Dual과 추천결과 Arrangement가 상이합니다");
							_noteList.add(addNoteMap);
							
						}
					}
				}
				
				
				// --------------------------------
				// 지정 Seal Configuration 선택값과 추천결과 비교
				// --------------------------------
				// 최종목록에서 Note 추가하게 변경 : 21.10
				if (!"".equals(StringUtil.get(item.get("SEAL_CONFIG")))) {
					
					int iSealConfigArrangement = NumberUtil.toInt(StringUtil.get(item.get("SEAL_CONFIG")).substring(0, 1));
					
					for (Map<String, Object> fMap : (List<Map<String, Object>>) fp.get("FINAL_LIST")) {
						Map<String,Object> sealAddInfo = fMap.get("SEAL_ADD_INFO")==null?new HashMap<String,Object>():(HashMap<String,Object>)fMap.get("SEAL_ADD_INFO");
						int iSealArrangement = NumberUtil.toInt(sealAddInfo.get("ARRANGEMENT"));
						if (iSealConfigArrangement != iSealArrangement) {
							Map<String,Object> addNoteMap = new HashMap<String,Object>();
							addNoteMap.put("P_ID_SEQ", fMap.get("P_ID_SEQ"));
							addNoteMap.put("P_IDX", fMap.get("P_IDX"));
							addNoteMap.put("P_SEQ", getMaxSeq2(_noteList));
							addNoteMap.put("SEAL", "");
							addNoteMap.put("PLAN", "");
							addNoteMap.put("TYPE","");
							addNoteMap.put("NOTE", "지정 Seal Configuration과 추천결과 Arrangement가 상이합니다");
							_noteList.add(addNoteMap);
						}
					}
				}
				
						
				// P_ID_SEQ 미부여 항목 : 0 처리.
				for(Map<String,Object> n : _noteList) {
					if(!n.containsKey("P_ID_SEQ")) {
						n.put("P_ID_SEQ", 0);
					}
				}
				
				// 중복 Note 제거
				String pIdSeq = "";
				String note = "";
				Iterator<Map<String, Object>> nIterator2 = _noteList.iterator();
				while (nIterator2.hasNext()) {
					Map<String, Object> noteMap = (HashMap<String, Object>) nIterator2.next();
					
					if(note.equals(noteMap.get("NOTE")) && pIdSeq.equals(noteMap.get("P_ID_SEQ").toString())){
						nIterator2.remove();
					}else {
						note=noteMap.get("NOTE").toString();
						pIdSeq=noteMap.get("P_ID_SEQ").toString();
					}
				}
				
				
				/*
				List<Map<String,Object>> subNoteList = new ArrayList<Map<String,Object>>();
				for(Map<String,Object> map : _noteList) {
					if(map.get("NOTE").toString().indexOf("Operating")>-1){

						pIdSeq = map.get("P_ID_SEQ").toString();
						String seal = map.get("SEAL").toString();
						
						for(Map<String,Object> fMap : (List<Map<String,Object>>)fp.get("FINAL_LIST")) {
							System.out.println(pIdSeq + " " + fMap.get("P_ID_SEQ"));
							System.out.println(seal + " " + fMap.get("SEAL"));
							
							if(!fMap.get("P_ID_SEQ").toString().equals(pIdSeq) && fMap.get("SEAL").equals(seal)) {
								Map<String,Object> sMap = new HashMap<String,Object>();
								sMap.put("P_ID_SEQ",fMap.get("P_ID_SEQ"));
								sMap.put("SEAL",fMap.get("SEAL"));
								sMap.put("PLAN",fMap.get("PLAN"));
								sMap.put("NOTE",map.get("NOTE"));
								subNoteList.add(sMap);
							}
						}
					}
				}*/
				
				List<Map<String,Object>> fNoteList = new ArrayList<Map<String,Object>>();
				
				fNoteList.addAll(_noteList);
				//fNoteList.addAll(subNoteList);
				
				//System.out.println(subNoteList);
				//System.out.println(_noteList);
				//System.out.println(fNoteList);
				
				/*
				Collections.sort(fNoteList, new Comparator<Map<String, Object>>() {
					@Override
					public int compare(Map<String, Object> o1, Map<String, Object> o2) {
						Integer is1 = Integer.parseInt(o1.get("P_ID_SEQ").toString());
						Integer is2 = Integer.parseInt(o2.get("P_ID_SEQ").toString());
						return is1.compareTo(is2);
					}
				});*/
				Collections.sort(fNoteList, new sortMap2());
				
				//System.out.println(fNoteList);
				
				Map<String, Object> itemRuleResult = new HashMap<String, Object>();
				// itemRuleResult.put("RST",rstList); // 추천결과
				itemRuleResult.put("RST", fp.get("FINAL_LIST")); // 추천결과
				//itemRuleResult.put("NOTE", noteRstList);
				itemRuleResult.put("NOTE", fNoteList);
				itemRuleResult.put("PROC", procRstList);

				result_item.put("predict_idx", sNo);
				result_item.put("predict_msg", "complete");
				result_item.put("RESULT", itemRuleResult);
				result_item.put("param", item); // 조건
				result_item.put("ProductGroupInfo", saProductGroupStr);

				result_items.add(result_item); // 결과리스트를 Add

			} // end for item

			result.put("RULE_RESULT", result_items); // 최종결과(전체 결과 리스트) Set

		} catch (Exception e) {
			System.out.println("ERROR : " + e.getMessage());
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

		return result;

	}

	@SuppressWarnings("unchecked")
	private boolean step_endUser(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		if (!"".equals(StringUtil.get(item.get("GS_CASE")))) { // End User GS Caltex에 대한 선택값이 있을 경우

			List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
			List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
			List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
			List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
			List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");
			List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");
			List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");
			List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");
			List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");
			List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
			List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
			List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("ATTR1", item.get("GS_CASE")); // GS Caltex 최종선택 Case
			param.put("TEMP_MIN", item.get("TEMP_MIN"));
			param.put("TEMP_MAX", item.get("TEMP_MAX"));
			param.put("SPEC_GRAVITY_MIN", item.get("SPEC_GRAVITY_MIN"));
			param.put("SPEC_GRAVITY_MAX", item.get("SPEC_GRAVITY_MAX"));
			param.put("SEAL_CHAM_MIN", item.get("SEAL_CHAM_MIN"));
			param.put("SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX"));

			// VPM 마진sd
			double vpm = 99999;
			double vpm1 = 99999, vpm2 = 99999, vpm3 = 99999;
			if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_NOR")))
					&& !"".equals(StringUtil.get(item.get("VAP_PRES_NOR")))) {
				vpm1 = (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) + 1)
						- NumberUtil.toDouble(item.get("VAP_PRES_NOR"));
			}
			if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_MIN")))
					&& !"".equals(StringUtil.get(item.get("VAP_PRES_MIN")))) {
				vpm2 = (NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) + 1)
						- NumberUtil.toDouble(item.get("VAP_PRES_MIN"));
			}
			if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_MAX")))
					&& !"".equals(StringUtil.get(item.get("VAP_PRES_MAX")))) {
				vpm3 = (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 1)
						- NumberUtil.toDouble(item.get("VAP_PRES_MAX"));
			}
//			double vpm1 = (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) + 1) - NumberUtil.toDouble(item.get("VAP_PRES_NOR"));
//			double vpm2 = (NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) + 1) - NumberUtil.toDouble(item.get("VAP_PRES_MIN"));
//			double vpm3 = (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 1) - NumberUtil.toDouble(item.get("VAP_PRES_MAX"));
			if (vpm > vpm1)
				vpm = vpm1;
			if (vpm > vpm2)
				vpm = vpm2;
			if (vpm > vpm3)
				vpm = vpm3;

			System.out.println("VPM : " + vpm);

			param.put("VPM", vpm / 0.069); // PSIA로 변경 PSIA = BARA / 0.069
			// param.put("VAP_PRES_MIN", NumberUtil.toDouble(item.get("VAP_PRES_MIN")) /
			// 0.069); // PSIA로 변경 PSIA = BARA / 0.069
			// param.put("VAP_PRES_MAX", NumberUtil.toDouble(item.get("VAP_PRES_MAX")) /
			// 0.069); // PSIA로 변경 PSIA = BARA / 0.069

			List<Map<String, Object>> list = rBMapper.selectRuleComListB801(param);

			System.out.println(item);
			System.out.println(list);

			int iPIdx = NumberUtil.toInt(item.get("P_IDX"));

			// 입력인자 중 API 682 적용으로 체크된 경우
			// End User, FTA 또는 다른 Rule에 의해서 Seal Model이 지정되어 있으면 그 중에서 API 682 Seal에 해당하는
			// Model로만 적용
			// 만약 API 682 Seal에 해당하는 것이 하나도 남지 않게 되는 경우는 대상이 없다고 알림이 필요
			String sAPI682 = StringUtil.get(item.get("API682_YN")); // API682 적용 유무
			List<Map<String, Object>> api682_list = null;
			if ("Y".equals(sAPI682)) { // API682 적용
				api682_list = rBMapper.selectSealTypeTInfo("A");
			}

			if (list.size() > 0) {

				iPIdx++;
				String sNote = null;
				for (Map<String, Object> m : list) {
					sNote = "";
					// Seal Type
					if (!StringUtil.isBlank(m.get("ATTR2"))) {
						for (String s : String.valueOf(m.get("ATTR2")).split(",")) {
							boolean bOk = false;
							if ("Y".equals(sAPI682)) { // API682 적용
								for (Map<String, Object> am : api682_list) {
									if (s.equals(StringUtil.get(am.get("SEAL_TYPE")))) {
										bOk = true;
										break;
									}
								}
							}

							if (bOk) {
								Map<String, Object> addInfo = new HashMap<String, Object>();
								addInfo.put("R_TYPE", "1");
								setResultList(sealRstList, iPIdx, s, addInfo, fp);
							} else {
								setResultNoteList(noteRstList, iPIdx, s + " : API 682에 포함되지 않는 Seal");
							}

						}
					}

					// Seal Type이 double type인지 체크
					boolean bIsDouble = false;
					if (StringUtil.get(m.get("ATTR2")).contains("/")) {
						bIsDouble = true;
					}

					// Material
					if (!StringUtil.isBlank(m.get("ATTR3"))) {
						for (String s : String.valueOf(m.get("ATTR3")).split(",")) {
							int i = 0;

							String[] sSeals = s.split("/");

							if (sSeals.length > 1) {
								for (String s_ : s.split("/")) {
									String[] s__ = s_.split(" ");
									if (i == 1) {
										if (s__.length > 0)
											setMaterialResultList_byDigit(material1OutRstList, "1", iPIdx,
													s__[0].trim(), null);
										if (s__.length > 1)
											setMaterialResultList_byDigit(material2OutRstList, "2", iPIdx,
													s__[1].trim(), null);
										if (s__.length > 2)
											setMaterialResultList_byDigit(material3OutRstList, "3", iPIdx,
													s__[2].trim(), null);
										if (s__.length > 3)
											setMaterialResultList_byDigit(material4OutRstList, "4", iPIdx,
													s__[3].trim(), null);
									} else {
										if (s__.length > 0)
											setMaterialResultList_byDigit(material1RstList, "1", iPIdx, s__[0].trim(),
													null);
										if (s__.length > 1)
											setMaterialResultList_byDigit(material2RstList, "2", iPIdx, s__[1].trim(),
													null);
										if (s__.length > 2)
											setMaterialResultList_byDigit(material3RstList, "3", iPIdx, s__[2].trim(),
													null);
										if (s__.length > 3)
											setMaterialResultList_byDigit(material4RstList, "4", iPIdx, s__[3].trim(),
													null);
									}

									i++;
								}
							} else {

								String[] s__ = s.split(" ");
								if (s__.length > 0)
									setMaterialResultList_byDigit(material1RstList, "1", iPIdx, s__[0].trim(), null);
								if (s__.length > 1)
									setMaterialResultList_byDigit(material2RstList, "2", iPIdx, s__[1].trim(), null);
								if (s__.length > 2)
									setMaterialResultList_byDigit(material3RstList, "3", iPIdx, s__[2].trim(), null);
								if (s__.length > 3)
									setMaterialResultList_byDigit(material4RstList, "4", iPIdx, s__[3].trim(), null);

								if (bIsDouble) {
									if (s__.length > 0)
										setMaterialResultList_byDigit(material1OutRstList, "1", iPIdx, s__[0].trim(),
												null);
									if (s__.length > 1)
										setMaterialResultList_byDigit(material2OutRstList, "2", iPIdx, s__[1].trim(),
												null);
									if (s__.length > 2)
										setMaterialResultList_byDigit(material3OutRstList, "3", iPIdx, s__[2].trim(),
												null);
									if (s__.length > 3)
										setMaterialResultList_byDigit(material4OutRstList, "4", iPIdx, s__[3].trim(),
												null);
								}

							}
						}
					}

					// API Plan
					if (!StringUtil.isBlank(m.get("ATTR4"))) {
						for (String s : String.valueOf(m.get("ATTR4")).split(",")) {
							setResultListPlan(planRstList, iPIdx, s, null, fp);

						}
					}

					// 특이사항
					// sNote += StringUtil.get(m.get("ATTR5")) + " , " +
					// StringUtil.get(m.get("ATTR6"));
					sNote += StringUtil.get(m.get("ATTR6"));
					if (!"".equals(sNote)) {
						setResultNoteList(noteRstList, iPIdx, sNote);
					}

					// 중간진행사항
					setResultProcList(procRstList, iPIdx, "[End User][GS] 기준 적용");
				}
			} else {
				iPIdx++;

				setResultProcList(procRstList, iPIdx, "[End User][GS] 범위에 해당하는 조건이 없음.");
				setResultNoteList(noteRstList, iPIdx, "범위에 해당하는 조건 없음, 고객지원팀과 협의");
			}

			// 추천 현재 Index Set
			item.put("P_IDX", iPIdx);

			return false;
		} else {
			return true;
		}

	}

	@SuppressWarnings("unchecked")
	private boolean step_fta(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		boolean bFta = true;

		// Prefered Seal 체크
		// API682 Seal 체크
		// [작업필요]

		if (!"".equals(StringUtil.get(item.get("EQUIPMENT")))) { // FTA Equipment 선택값

			List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
			List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
			List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
			List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
			List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");
			List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");
			List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");
			List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");
			List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");
			List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
			List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
			List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

			Map<String, Object> ptm = new HashMap<String, Object>();
			ptm.put("MCD", "B201");
			ptm.put("ATTR1", item.get("EQUIPMENT"));
			List<Map<String, Object>> rComList = getRuleComListType1(ptm);

			String sAPI682 = StringUtil.get(item.get("API682_YN")); // API682 적용 유무
			List<Map<String, Object>> api682_list = null;
			if ("Y".equals(sAPI682)) { // API682 적용
				api682_list = rBMapper.selectSealTypeTInfo("A");
			}

			if (!rComList.isEmpty()) {

				int iPIdx = NumberUtil.toInt(item.get("P_IDX"));

				// 씰정보별로 값이 있을 경우 저장한다.
				for (Map<String, Object> m : rComList) {

					// Seal Type
					if (!StringUtil.isBlank(m.get("ATTR2"))) {
						for (String s : String.valueOf(m.get("ATTR2")).split(",")) {

							boolean bOk = false;
							if ("Y".equals(sAPI682)) { // API682 적용
								for (Map<String, Object> am : api682_list) {
									if (s.equals(StringUtil.get(am.get("SEAL_TYPE")))) {
										bOk = true;
										break;
									}
								}
							} else {
								bOk = true;
							}

							if (bOk) {
								iPIdx++;
								Map<String, Object> addInfo = new HashMap<String, Object>();
								addInfo.put("R_TYPE", "0");
								setResultList(sealRstList, iPIdx, s, addInfo, fp);

								// Material
								if (!StringUtil.isBlank(m.get("ATTR3"))) {
									for (String sm : String.valueOf(m.get("ATTR3")).split(",")) {
										int i = 0;
										for (String s_ : sm.split("/")) {
											String[] s__ = s_.split(" ");
											if (i == 1) {
												if (s__.length > 0)
													setMaterialResultList_byDigit(material1OutRstList, "1", iPIdx,
															s__[0].trim(), null);
												if (s__.length > 1)
													setMaterialResultList_byDigit(material2OutRstList, "2", iPIdx,
															s__[1].trim(), null);
												if (s__.length > 2)
													setMaterialResultList_byDigit(material3OutRstList, "3", iPIdx,
															s__[2].trim(), null);
												if (s__.length > 3)
													setMaterialResultList_byDigit(material4OutRstList, "4", iPIdx,
															s__[3].trim(), null);
											} else {
												if (s__.length > 0)
													setMaterialResultList_byDigit(material1RstList, "1", iPIdx,
															s__[0].trim(), null);
												if (s__.length > 1)
													setMaterialResultList_byDigit(material2RstList, "2", iPIdx,
															s__[1].trim(), null);
												if (s__.length > 2)
													setMaterialResultList_byDigit(material3RstList, "3", iPIdx,
															s__[2].trim(), null);
												if (s__.length > 3)
													setMaterialResultList_byDigit(material4RstList, "4", iPIdx,
															s__[3].trim(), null);
											}
											i++;
										}
									}
								}

								// API Plan
								if (!StringUtil.isBlank(m.get("ATTR4"))) {
									for (String sp : String.valueOf(m.get("ATTR4")).split(",")) {
										setResultListPlan(planRstList, iPIdx, sp, null, fp);
									}
								}

							} else {
								setResultNoteList(noteRstList, iPIdx, "[FTA] API682에 포함되지 않는 Seal : " + s);
							}
						}
					}

					// 특이사항
					if (!StringUtil.isBlank(m.get("ATTR5"))) {
						setResultNoteList(noteRstList, iPIdx, String.valueOf(m.get("ATTR5")));
					}

				}

				// 중간진행사항
				setResultProcList(procRstList, 0, "[FTA] 기준 적용");

			}

			// 추천 현재 Index Set
			// item.put("P_IDX",iPIdx);

			bFta = false;
		}

		return bFta;
	}

	private void step_ftp_after_process(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		// 1.설정된 Arrangement가 사용가능한 Seal 인지 체크

		List<String> removeList = new ArrayList<String>();

		for (Map<String, Object> sm : sealRstList) {
			String sSealType = StringUtil.get(sm.get("P_VAL"));
			int iPIdx = NumberUtil.toInt(sm.get("P_IDX"));

			int ichk = 0;
			for (String sSeal : sSealType.split("/")) {
				// 압력 재설정
				Map<String, Object> c3_param = (Map<String, Object>) ((HashMap<String, Object>) item).clone();

				// Press : Plan이 설정된 이 후라 Inboard , Outboard에 따라 압력 조정
				double dC3CheckPress = getC3CheckPress(item, iPIdx, (ichk == 0 ? "IN" : "OUT"), fp);
				c3_param.put("SEAL_CHAM_NOR", dC3CheckPress);
				c3_param.put("SEAL_CHAM_MIN", dC3CheckPress);
				c3_param.put("SEAL_CHAM_MAX", dC3CheckPress);

				// Line Speed
				double dC3ChekSealSize = getSealSize(item, "MM", sSeal, sSealType, "1", fp);
				c3_param.put("SEAL_SIZE", dC3ChekSealSize);

				// Line Speed
				// 주속계산 시 Mean Dia 로 계산되는 Seal Type 처리
				double dC3ChekSealSize2 = getSealSize(item, "MM", sSeal, sSealType, "2", fp);
				// 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60
				// Max기준으로만 체크함.
				c3_param.put("L_SPD_NOR", 0);
				c3_param.put("L_SPD_MIN", 0);
				c3_param.put("L_SPD_MAX",
						3.14 * dC3ChekSealSize2 * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속

				if (!isC3OperatingCheck(c3_param, sSeal)) {
					if(c3_param.containsKey("C3_EXCEPTION")) {
						setResultProcList(procRstList, iPIdx, "[FTA] Operating Window 조건을 만족하지 않습니다. : " + sSeal + "\n["
								+ c3_param.get("C3_EXCEPTION").toString() + "]");
					}else {
						setResultProcList(procRstList, iPIdx, "[FTA] Operating Window 조건을 만족하지 않습니다. : " + sSeal);
					}
					
					removeList.add("" + iPIdx);
				}

				ichk++;
			}
		}

		// Seal , Plan 삭제
		for (String sIdx : removeList) {

			Iterator remove_iterator = sealRstList.iterator();
			while (remove_iterator.hasNext()) {
				Map<String, Object> sm = (HashMap<String, Object>) remove_iterator.next();

				if (StringUtil.get(sm.get("P_IDX")).equals(sIdx)) {
					remove_iterator.remove();
				}
			}

			remove_iterator = planRstList.iterator();
			while (remove_iterator.hasNext()) {
				Map<String, Object> sm = (HashMap<String, Object>) remove_iterator.next();

				if (StringUtil.get(sm.get("P_IDX")).equals(sIdx)) {
					remove_iterator.remove();
				}
			}

		}

		// 2.듀얼씰 여부 확인 및 Seal Type 표현 재처리
		if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {

			List<Map<String, Object>> rComList = null;
			Map<String, Object> param = new HashMap<String, Object>();
			for (Map<String, Object> sm : sealRstList) {

				String sSealType = StringUtil.get(sm.get("P_VAL"));

				// 이미 듀얼 형태의 정보인 경우 Skip
				if (sSealType.contains("/"))
					continue;

				param.clear();
				param.put("MCD", "E004");
				param.put("ATTR1", sSealType);
				rComList = getRuleComListType1(param);
				if (!rComList.isEmpty()) { // Single 또는 Dual을 나타내는 Seal이므로 별도처리 X
					// skip
				} else {
					sm.put("P_VAL", sSealType + "/" + sSealType);
				}
			}
		}

	}

	private boolean step_split(Map<String, Object> item, Map<String, Object> fp, Map<String, Object> param)
			throws Exception {

		boolean bIsProcess = true;
		// 1.Split Y 일때는 다음과 같이 적용
		// (1) Equip Type에 따른 Seal Model 선정
		// Equip Type이 Pump인 경우 -> PSS 4, PSS III 선정 (PSS 4가 PSS III가 다음 세대이지만 아직 혼용하고
		// 있어서 같이 선정합니다)
		// Equip Type이 Mixer or Other인 경우 -> MSS 선정
		// (2) C3 Operating Window에서 운전조건 대비 사용 가능 여부 판단 문제가 있는 경우 알림
		// (3) C1 Material Guide 및 C9 Seal Type별 표준 재질에 의건 재질 선정
		// (4) API Plan은 일반적인 Process로 선정할 수가 없으니, ‘API Plan은 별도로 선정할 것’으로 표시

		if ("Y".equals(StringUtil.get(item.get("SPLIT_YN")))) {

			List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
			List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
			List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

			bIsProcess = false;

			// Equip Type : Other Mixer Pump Unknown
			// 적용대상 Seal은 모두 Arrangement 1 타입으로 압력 별도 기준 고려하지 X
			String sEquipType = StringUtil.get(param.get("equip_type"));
			if ("Pump".equals(sEquipType)) {

				int iPIdx = getNextIdx(fp);
				setResultList(sealRstList, iPIdx, "PSS 4", null, fp);

				setResultProcList(procRstList, iPIdx, "Split 적용");
				if (!isC3OperatingCheck(item, "PSS 4")) {
					setResultNoteList(noteRstList, iPIdx,
							"Operating Window 운전조건 만족하지 않음" + "\n[" + item.get("C3_EXCEPTION").toString() + "]");
				}

				iPIdx = getNextIdx(fp);
				setResultList(sealRstList, iPIdx, "PSS III", null, fp);

				setResultProcList(procRstList, iPIdx, "Split 적용");
				if (!isC3OperatingCheck(item, "PSS III")) {
					setResultNoteList(noteRstList, iPIdx,
							"Operating Window 운전조건 만족하지 않음" + "\n[" + item.get("C3_EXCEPTION").toString() + "]");
				}

			} else if ("Mixer".equals(sEquipType) || "Other".equals(sEquipType)) {

				int iPIdx = getNextIdx(fp);
				setResultList(sealRstList, iPIdx, "MSS", null, fp);

				setResultProcList(procRstList, iPIdx, "Split 적용");
				if (!isC3OperatingCheck(item, "MSS")) {
					setResultNoteList(noteRstList, iPIdx,
							"Operating Window 운전조건 만족하지 않음" + "\n[" + item.get("C3_EXCEPTION").toString() + "]");
				}
			}

			// note
			setResultNoteList(noteRstList, 0, "Split 적용 : API Plan은 별도로 선정할 것");

		}

		return bIsProcess;

	}

	private boolean step_prefered(Map<String, Object> item, Map<String, Object> fp, Map<String, Object> param)
			throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");

		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		boolean bIsProcess = true;// Process 추천 로직 처리 유무

		// ---------------------------------
		// 우선순위 재질 기준 적용
		// ---------------------------------
		Map<String, Object> ptm = new HashMap<String, Object>();

		// [B1-2] Styrene Monomer Applications
		// Product Group = Styrene Monomer => SM
		// 적용 RULE 기준 : B1201
		if (isProduct("SM", saProductGroup, saProduct)) {
			ptm.clear();
			ptm.put("MCD", "B1201");
			ptm.put("SCD", "B1201010");
			List<Map<String, Object>> rComList = getRuleComListType1(ptm);

			if (!rComList.isEmpty()) {
				// bIsProcess = false;
				int iPIdx = getNextIdx(fp);

				// Seal Type
				Map<String, Object> sealAddInfo = new HashMap<String, Object>();
				sealAddInfo.put("R_TYPE", "2");
				setResultList(sealRstList, iPIdx, rComList.get(0).get("ATTR1"), sealAddInfo, fp);

				// Material
				for (String s : String.valueOf(rComList.get(0).get("ATTR2")).split(",")) {
					for (String s_ : s.split("/")) {
						String[] s__ = s_.split(" ");
						if (s__.length > 0)
							setMaterialResultList_byDigit(material1RstList, "1", iPIdx, s__[0].trim(), null);
						if (s__.length > 1)
							setMaterialResultList_byDigit(material2RstList, "2", iPIdx, s__[1].trim(), null);
						if (s__.length > 2)
							setMaterialResultList_byDigit(material3RstList, "3", iPIdx, s__[2].trim(), null);
						if (s__.length > 3)
							setMaterialResultList_byDigit(material4RstList, "4", iPIdx, s__[3].trim(), null);
					}
				}
				// 중간진행사항
				setResultProcList(procRstList, 0, "Styrene Monomer Applications Rule 적용");

			}
		}

		System.out.println("----- 우선적용 : [B1-2] Styrene Monomer Applications end ----");

		// [B1-6] HF alkylation 유체 사용 시
		// Step1에서 refinery 서비스 선택 후 Equipment가 HF Alkylation Unit인 Case
		if ("Z060385".equals(String.valueOf(item.get("EQUIPMENT")))) {
			ptm.clear();
			ptm.put("MCD", "B1601");
			ptm.put("SCD", "B1601010");
			List<Map<String, Object>> rComList = getRuleComListType1(ptm);

			if (!rComList.isEmpty()) {
				// bIsProcess = false;
				int iPIdx = getNextIdx(fp);

				// Seal Type
				Map<String, Object> sealAddInfo = new HashMap<String, Object>();
				sealAddInfo.put("R_TYPE", "2");
				setResultList(sealRstList, iPIdx, rComList.get(0).get("ATTR1"), sealAddInfo, fp);

				// String sSealTypeTmp = StringUtil.get(rComList.get(0).get("ATTR1")); //Seal
				// Type

				// Material
				for (String s : String.valueOf(rComList.get(0).get("ATTR2")).split(",")) {
					for (String s_ : s.split("/")) {
						String[] s__ = s_.split(" ");
						if (s__.length > 0)
							setMaterialResultList_byDigit(material1RstList, "1", iPIdx, s__[0].trim(), null);
						if (s__.length > 1)
							setMaterialResultList_byDigit(material2RstList, "2", iPIdx, s__[1].trim(), null);
						if (s__.length > 2)
							setMaterialResultList_byDigit(material3RstList, "3", iPIdx, s__[2].trim(), null);
						if (s__.length > 3)
							setMaterialResultList_byDigit(material4RstList, "4", iPIdx, s__[3].trim(), null);
					}
				}

				setResultListPlan(planRstList, iPIdx, rComList.get(0).get("ATTR3"), null, fp);// API Plan

				// 중간진행사항
				setResultProcList(procRstList, 0, "HF alkylation Fluid 적용 : Material Inner");

			}
		}

		System.out.println("----- [B1-6] HF alkylation 유체 사용 시  end ----");

		// [B4] (Hot) Water Guide
		// Water Guide 적용조건
		// 1.product가 water만 있는 경우 -> water 명이 포함된 그룹으로 수정필요
		// WATER
		// WATER - CONDENSATE
		// WATER - COOLING TOWER
		// WATER - DEIONIZED
		// WATER - DEMINERALIZED
		// WATER - DISTILLED
		// WHITE WATER
		// 2.OH1일 경우 미적용
		// 3.NON-API일 경우 미적용
		// 4.pump type이 H일 경우 - Seql, Material, Plan 정보 적용
		// V일 경우 - Seal, Material 정보 적용
		// 5.solid 구분이 N인 경우 적용

		String sPumpTypeG = getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")), _pumpTypeGroupInfo, null);

		//System.out.println("saProductGroup.length " + saProductGroup.length);
		//System.out.println("saProductGroup " + saProductGroup);
		//System.out.println("sPumpTypeG " + sPumpTypeG);
		//System.out.println("item.get(\"API682_YN\") " + item.get("API682_YN"));
		//System.out.println("item.get(\"PUMP_TYPE_HV\") " + item.get("PUMP_TYPE_HV"));
		//System.out.println("item.get(\"SOLID_GB\") " + item.get("SOLID_GB"));

		// WATER
		// WATER - CONDENSATE
		// WATER - COOLING TOWER
		// WATER - DEIONIZED
		// WATER - DEMINERALIZED
		// WATER - DISTILLED
		// WHITE WATER

		if (saProductGroup.length == 1 &&
		// saProductGroup[0].contains("WATER") &&
				(saProductGroup[0].equals("WATER") || saProductGroup[0].equals("WATER - CONDENSATE")
						|| saProductGroup[0].equals("WATER - COOLING TOWER")
						|| saProductGroup[0].equals("WATER - DEIONIZED")
						|| saProductGroup[0].equals("WATER - DEMINERALIZED")
						|| saProductGroup[0].equals("WATER - DISTILLED") || saProductGroup[0].equals("WHITE WATER"))
				&& !"OH1".equals(sPumpTypeG) && "Y".equals(StringUtil.get(item.get("API682_YN")))
				&& ("".equals(StringUtil.get(item.get("SOLID_GB")))
						|| "N".equals(StringUtil.get(item.get("SOLID_GB"))))) {

			item.put("__IS_PRODUCT_WATER_GUIDE", "Y"); // Water Guide 적용 여부 Set

			double dSealSize = getSealSize(item, "MM", "QBW", "", "1", fp);

			ptm.clear();
			// Seal Size는 QBW에서만 유효하게 처리되므로 QBW기준으로 설정하고 조회한다.
			ptm.put("SEAL_SIZE", dSealSize); // Seal Size
			ptm.put("TEMP_MAX", item.get("TEMP_MAX")); // 온도 최대
			ptm.put("SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX")); // 압력 최대
			List<Map<String, Object>> rComList = rBMapper.selectRuleComListB401(ptm);

			boolean bIsNeedChgSeal = false;

			if (rComList.isEmpty()) {

				ptm.clear();
				// Seal Size는 QBW에서만 유효하게 처리되므로 QBW기준으로 설정하고 조회한다.
				ptm.put("SEAL_SIZE", dSealSize); // Seal Size
				ptm.put("TEMP_MAX", item.get("TEMP_MAX")); // 온도 최대
				// 압력조건 제외
				// ptm.put("SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX")); // 압력 최대
				ptm.put("PRI_YN", "Y"); // 우선적용대상
				rComList = rBMapper.selectRuleComListB401(ptm);

				bIsNeedChgSeal = true;

			}

			// 중간진행사항
			setResultProcList(procRstList, 0, "Water Application Guide 적용");

			for (Map<String, Object> m : rComList) {

				int iPIdx = getNextIdx(fp);

				// Seal Add Info Map
				Map<String, Object> sealAddInfo = new HashMap<String, Object>();
				sealAddInfo.put("R_TYPE", "2");
				sealAddInfo.put("PRE_TYPE","WATER");//water guide 설정

				// 압력제한으로 Seal 변경이 필요한 경우
				// 한개의 Seal만 선택되어짐
				if (bIsNeedChgSeal) {
					ptm.clear();
					ptm.put("SEAL_SIZE", dSealSize); // Seal Size
					ptm.put("SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX")); // 압력 최대
					List<Map<String, Object>> rPressPermitList = rBMapper.selectRuleComListB403(ptm);

					Map<String, Object> cm = null;
					if (rPressPermitList.size() > 0) {
						cm = (HashMap<String, Object>) rPressPermitList.get(0);
						String sChgSeal = StringUtil.get(cm.get("ATTR1"));
						setResultList(sealRstList, iPIdx, sChgSeal, sealAddInfo, fp);

						setResultProcList(procRstList, 0, "Water Application Guide-Seal : " + sChgSeal);
					}

					// 압력에따라 Seal이 변경되는 경우 face 재질을 B403에서 별도로 잡는다.
					// Material 2 : face 2nd
					if (cm.get("ATTR6") != null && !"".equals(String.valueOf(cm.get("ATTR6")))) {
						for (String s : String.valueOf(cm.get("ATTR6")).split(",")) {
							setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, 0, s, null, "IN");
						}
					}

					// Material 4 : face 4th
					if (cm.get("ATTR7") != null && !"".equals(String.valueOf(cm.get("ATTR7")))) {
						for (String s : String.valueOf(cm.get("ATTR7")).split(",")) {
							setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, 0, s, null, "IN");
						}
					}

				} else {
					setResultList(sealRstList, iPIdx, m.get("ATTR3"), sealAddInfo, fp);

					setResultProcList(procRstList, 0, "Water Application Guide-Seal : " + m.get("ATTR3"));

					// Material 2 : face 2nd
					if (m.get("ATTR5") != null && !"".equals(String.valueOf(m.get("ATTR5")))) {
						for (String s : String.valueOf(m.get("ATTR5")).split(",")) {
							setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, 0, s, null, "IN");
						}
					}

					// Material 4 : face 4th
					if (m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
						for (String s : String.valueOf(m.get("ATTR7")).split(",")) {
							setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, 0, s, null, "IN");
						}
					}
				}

				// Material 1 : Metal 1st
				if (m.get("ATTR4") != null && !"".equals(String.valueOf(m.get("ATTR4")))) {
					for (String s : String.valueOf(m.get("ATTR4")).split(",")) {
						setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, s, null, "IN");
					}
				}

				// Material 2 : face 2nd
//				if(m.get("ATTR5") != null && !"".equals(String.valueOf(m.get("ATTR5")))) {
//					for(String s : String.valueOf(m.get("ATTR5")).split(",")) {
//						setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, 0, s, null, "IN");
//					}
//				}

				// Material 3 : Gasket 3nd
				if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
					for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
						setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, s, null, "IN");
					}
				}

				//System.out.println("###########################");
				//System.out.println(material3RstList);
				//System.out.println("###########################");

				// Material 4 : face 4th
//				if(m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
//					for(String s : String.valueOf(m.get("ATTR7")).split(",")) {
//						setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, 0, s, null, "IN");
//					}
//				}

				// Pump Type이 Horizontal일 경우 water guide plan 적용
				// Pump Type이 Vertical일 경우 Skip -> 이 후 Plan 설정 프로세스로 추천
				if ("H".equals(StringUtil.get(item.get("PUMP_TYPE_HV")))) {
					// API Plan
					if (m.get("ATTR8") != null && !"".equals(String.valueOf(m.get("ATTR8")))) {
						for (String s : String.valueOf(m.get("ATTR8")).split(",")) {
							setPlanResultList(planRstList, iPIdx, s, null, fp);
							setResultProcList(procRstList, 0, "Water Application Guide Seal-API Plan : " + s);
						}
					}
				}

				// 추가정보
				if (!"".equals(StringUtil.get(m.get("ATTR9")))) {
					ptm.clear();
					ptm.put("MCD", "B402"); // B4 추가정보
					ptm.put("ATTR1", m.get("ATTR9")); // 추가정보 구분
					List<Map<String, Object>> rComList2 = rBMapper.selectRuleComListType1(ptm);
					for (Map<String, Object> m2 : rComList2) {
						setResultNoteList(noteRstList, iPIdx, String.valueOf(m2.get("ATTR2")));
					}
				}

			}

		}

		// [B1-13] Product에 NaOH 가 포함될 경우
		// Material, API Plan = [B1-13]
		// NAOH = SODIUM HYDROXIDE
		if (isProduct("SODIUM HYDROXIDE", saProductGroup, saProduct)) {
			ptm.clear();
			ptm.put("MCD", "B11301");
			String sgb_tmp = getProductGb_byGrouping(item, "SODIUM HYDROXIDE", saProductGroup, saProduct);
			ptm.put("NAOH_CONT", "".equals(sgb_tmp) ? "0" : sgb_tmp.replace("%", ""));
			ptm.put("TEMP_MAX", item.get("TEMP_MAX"));

			System.out.println("========> NAOH_CONT : " + sgb_tmp);

			List<Map<String, Object>> rComList = rBMapper.selectRuleComListB11301(ptm);

			if (!rComList.isEmpty()) {
				ptm.clear();
				ptm.put("MCD", "B11302");
				ptm.put("ATTR1", rComList.get(0).get("ATTR1"));
				List<Map<String, Object>> rComList2 = getRuleComListType1(ptm); // B11302 - Caustic services Selections

				if (!rComList2.isEmpty()) {

					Map<String, Object> m = rComList2.get(0);

					// ---------------------------------------
					// 입력된 사용자 조건에 따른 Arrangement 반영
					// ---------------------------------------

					// Arrangement Add
					if (m.get("ATTR2") != null && !"".equals(String.valueOf(m.get("ATTR2")))) {

						int iArrangementTmp = NumberUtil.toInt(m.get("ATTR2"));

						// step_arrangement_pre 에서 사용자에 입력된 인자에 따른 arrangement가 설정된 경우 체크
						if (NumberUtil.toInt(item.get("ARRANGEMENT")) == 0
								|| NumberUtil.toInt(item.get("ARRANGEMENT")) <= iArrangementTmp) {

							item.put("ARRANGEMENT", "" + iArrangementTmp);

							// 중간진행사항
							setResultProcList(procRstList, 0, "유체 NaOH 기준 - Arrangement : " + m.get("ATTR2"));
						}

					}
				}
			}
		}

//		// [B1-8] 극저온 서비스
//		ptm.clear();
//		//ptm.put("ATTR6", sSealType);
//		ptm.put("TEMP_MIN", item.get("TEMP_MIN"));
//		ptm.put("TEMP_MAX", item.get("TEMP_MAX"));
//		ptm.put("SEAL_CHAM_MIN", item.get("SEAL_CHAM_MIN"));
//		ptm.put("SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX"));
//		
//		String sPumpTypeG = mLService.getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")),_pumpTypeGroupInfo,null);
//		String sEquipmentType = "";
//		if ("OH1".equals(sPumpTypeG) ||
//				"OH2".equals(sPumpTypeG) ||
//				"BB".equals(sPumpTypeG)) {
//			sEquipmentType = "Z120020"; //H
//		}else {
//			if (!"VS4".equals(sPumpTypeG)) {
//				sEquipmentType = "Z120010"; //V
//			}else {
//				sEquipmentType = "Z120020"; //H
//			}
//		}
//		ptm.put("EQUIPMENT_TYPE", sEquipmentType);
//				
//		List<Map<String,Object>> rComB1_8List = rBMapper.selectRuleComListB1801(ptm); 
//		
//		if (!rComB1_8List.isEmpty()) {
//			
//			// 중간진행사항
//			setResultProcList(procRstList, 0, "[B1-8] Cryogenic Applications Rule 적용");
//			
//			for(Map<String,Object> m : rComB1_8List) {
//				
//				int iPIdx = getNextIdx(fp);
//				
//				// Seal Type
//				if(m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
//					for(String s : String.valueOf(m.get("ATTR6")).split(",")) {
//						Map<String,Object> sealAddInfo = new HashMap<String,Object>();
//						sealAddInfo.put("R_TYPE","2");
//						setResultList(sealRstList, iPIdx, s, sealAddInfo, fp);
//					}
//				}
//				
//				
//				// Material
//				if(m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
//					
//					//int iPIdx = getNextIdx(fp);
//					
//					//Material
//					for(String s : String.valueOf(m.get("ATTR7")).split(",")) {
//						String[] s_ = s.split("/");
//						String[] s__ = s_[0].split(" ");
//						if (s__.length> 0 ) setMaterialResultList_byDigit(material1RstList,  "1", iPIdx, s__[0].trim(), null);
//						if (s__.length> 1 ) setMaterialResultList_byDigit(material2RstList,  "2", iPIdx, s__[1].trim(), null);
//						if (s__.length> 2 ) setMaterialResultList_byDigit(material3RstList,  "3", iPIdx, s__[2].trim(), null);
//						if (s__.length> 3 ) setMaterialResultList_byDigit(material4RstList,  "4", iPIdx, s__[3].trim(), null);
//						
//						if(s_.length > 1) {
//							s__ = s_[1].split(" ");
//							if (s__.length> 0 ) setMaterialResultList_byDigit(material1OutRstList,  "1", iPIdx, s__[0].trim(), null);
//							if (s__.length> 1 ) setMaterialResultList_byDigit(material2OutRstList,  "2", iPIdx, s__[1].trim(), null);
//							if (s__.length> 2 ) setMaterialResultList_byDigit(material3OutRstList,  "3", iPIdx, s__[2].trim(), null);
//							if (s__.length> 3 ) setMaterialResultList_byDigit(material4OutRstList,  "4", iPIdx, s__[3].trim(), null);
//						}
//					}
//					
//					// API Plan
//					if(m.get("ATTR8") != null && !"".equals(String.valueOf(m.get("ATTR8")))) {
//						for(String s : String.valueOf(m.get("ATTR8")).split(",")) {
//							setResultListPlan(planRstList, iPIdx, s, null, fp);
//						}
//					}
//					
//				}
//				
//				
//				//추가정보
//				if (m.get("ATTR9") != null ) {
//					ptm.clear();
//					ptm.put("MCD", "B1802"); // B4 추가정보
//					ptm.put("ATTR1",m.get("ATTR9")); // 추가정보 구분
//					List<Map<String,Object>> rComList2 = rBMapper.selectRuleComListType1(ptm);
//					for(Map<String,Object> m2 : rComList2) {
//						setResultNoteList(noteRstList, iPIdx, "[B1-8] Cryogenic Applications - " +String.valueOf(m2.get("ATTR2")));
//					}
//				}
//					
//			}
//		}

//		System.out.println("----- 극저온 서비스 end ----");

		// [C7-9] Sulfur 일 경우
		if (isProduct("SULFUR", saProductGroup, saProduct)) {
			// Molten Sulfur 또는 Sulfur 함유 & Product 비중 1.5 이상일 경우
			// BXRH, HXCU (Gland, Sleeve : 316SS), Plan 02/62

			if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 1.5
					&& NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) >= 1.5
					&& NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) >= 1.5) {

				// bIsProcess = false;
				int iPIdx = getNextIdx(fp);

				// Seal Type
				Map<String, Object> sealAddInfo = new HashMap<String, Object>();
				sealAddInfo.put("R_TYPE", "2");
				setResultList(sealRstList, iPIdx, "BXRH", sealAddInfo, fp);

				// 중간진행사항
				setResultProcList(procRstList, 0, "[C7-9] Sulfur Rule 적용 :  Seal : " + "BXRH");
			}

		}

		// [C7-7] EO / PO ETHYLENE OXIDE / PROPYLENE OXIDE
		// O-ring : Chemraz 605 적용
		if (isProduct("ETHYLENE OXIDE", saProductGroup, saProduct)
				|| isProduct("PROPYLENE OXIDE", saProductGroup, saProduct)) {
			item.put("ARRANGEMENT", "3"); // Arrangement 설정
			// 중간진행사항
			setResultProcList(procRstList, 0, "[C7-7] EO / PO 기준 적용 : Arrangement : 3");
		}

		// [C7-13] Residue 유체일 경우
		if (isProduct("[RESIDUE]", saProductGroup, saProduct)) {

			int iPIdx = getNextIdx(fp);

			// 중간진행사항
			setResultProcList(procRstList, 0, "[C7-13] Residue Rule 적용");

			// Clean 여부에 따라 다음과 같이 적용
			// Clean 하지 않거나 또는 정확한 정보 없는 경우 : Plan 32 적용
			// Clean 한 경우 : Pour point / 온도 하락 시 점도 변화 / 상온에서 굳는 성질이 있는지 확인 -> 3

			// Clean한 경우 유체 성질에 따른 Plan 선정
			// 굳는 성질이 있는 유체일 경우 : Arrangement 3 적용
			// 굳는 성질이 없는 유체일 경우 : Arrangement 2 적용

			// if("Y".equals(StringUtil.get(item.get("RESI_CLEAN_GB")))) {
			// Residue 클린여부 => Solid 유무 N으로 체크
			if ("N".equals(StringUtil.get(item.get("SOLID_GB"))) || "".equals(StringUtil.get(item.get("SOLID_GB")))) {

				// if("Y".equals(StringUtil.get(item.get("RESI_HRDN_GB")))) { // 굳는성질
				// Residue 굳는성질 => Cooling으로 굳는성질 체크로 대체
				if ("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { // Cooling으로 굳는성질

					// arrangement
					item.put("ARRANGEMENT", "3");

					setResultProcList(procRstList, 0, "[C7-13] Residue Rule 적용 : ARRANGEMENT : " + "3");

				} else {
					// arrangement
					if (item.get("ARRANGEMENT") == null || (NumberUtil.toDouble(item.get("ARRANGEMENT"))) == 1) {
						item.put("ARRANGEMENT", "2");
						setResultProcList(procRstList, 0, "[C7-13] Residue Rule 적용 : ARRANGEMENT : " + "2");
					} else {
						setResultProcList(procRstList, 0,
								"[C7-13] Residue Rule 적용 : 이미 Arrangement가 3이 적용되어 Arrangement 2가 무시됨");
					}
				}

			} else { // clean하지 않을 경우 => Plan 설정단계에서 처리
//				setResultListPlan(planRstList, iPIdx, "32", null, fp);
//				setResultProcList(procRstList, 0, "[C7-13] Residue Rule 적용 : API Plan : " + "32" );
			}

		}

//		// [C6-9] BUTADIENE 유체일 경우
//		// Chemraz 505  우선적용 : G005
//		if(isProduct("BUTADIENE",saProductGroup,saProduct)) {
//			
//			int iPIdx = getNextIdx(fp);
//			
//			// Material 3 : Gasket 
//			setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, "G005", null);
//			
//			// 중간진행사항
//			setResultProcList(procRstList, 0, "[C6] Butadiene Rule 적용 - Chemraz 505 우선 적용 ");
//		}
//		
//		
//		// Hydrocarbon 일 경우		
//		if(isProduct("HYDROCARBON",saProductGroup,saProduct)) {
//				
//			int iPIdx = getNextIdx(fp);
//			
//			//Material
//			setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, "X675", null);
//		
//			// 중간진행사항
//			setResultProcList(procRstList, 0, "Hydrocarbon O-Ring  :  X675 ");
//			
//		}
		// if(item.get("__IS_PRODUCT_WATER_GUIDE").equals("Y"))

		return bIsProcess;
	}

	/**
	 * Teyp 설정
	 * 
	 * @param item
	 * @param fp
	 * @return
	 * @throws Exception
	 */
	private boolean step_type(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		// 우선처리

		// ------------------------------------
		// 직접입력된 Seal Type이 있을 경우
		// ------------------------------------
		// SEAL_INNER_DIR - inboard
		// SEAL_OUTER_DIR - outboard
		String sSealTypeInDir = StringUtil.get(item.get("SEAL_INNER_DIR"));
		if (!"".equals(sSealTypeInDir)) {

			// Type 조회
			String sDirSeal_Type = "";
			String sDirSealConfig = "";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("SEAL_TYPE", sSealTypeInDir);
			List<Map<String, Object>> c3List = rBMapper.selectRuleC3(param);

			for (Map<String, Object> c3 : c3List) {
				String s = StringUtil.get(c3.get("SEAL_TYPE"));
				for (String ss : s.split(",")) {
					if (ss.equals(sSealTypeInDir)) {
						sDirSeal_Type = StringUtil.get(c3.get("SEAL_GB_TYPE"));
						sDirSealConfig = StringUtil.get(c3.get("CONFIG"));
						break;
					}
				}
			}

			// Type 설정
			if ("".equals(sDirSeal_Type) || "X".equals(sDirSeal_Type)) {
				setResultNoteList(noteRstList, 0,
						"[Type] 지정 Seal정보 또는 Type 정보가 올바르지 않습니다. - " + sDirSeal_Type + " : " + sDirSeal_Type);
				return false;
			} else {
				setResultProcList(procRstList, 0, "[Type] 지정 Seal에 의한 Result : " + sDirSeal_Type);
				item.put("ABC_TYPE", sDirSeal_Type);
				item.put("DIR_SEAL_CONFIG", sDirSealConfig); // 지정된 Seal의 config 정보
				return true;
			}

		}

		// ------------------------------------
		// Type 설정 프로세스
		// ------------------------------------

		// Temp > 176 여부
		String sType = "";
		String sProcess = "[TYPE]";
		String sTypeSetNote1 = "온도에 의해 API 682에서 Type C가 요구되나  다른 운전조건에 따라  Type  A로 변경됨";

		if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 176 || NumberUtil.toDouble(item.get("TEMP_MIN")) > 176
				|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 176) {
			setResultProcList(procRstList, 0, sProcess + " Temp > 176 : Y");

			// [WATER-BASE] : product group에 WATER가 포함된 경우로 체크
			if (isProduct("[WATER-BASE]", saProductGroup, saProduct)) {
				setResultProcList(procRstList, 0, sProcess + " Water service : Y");
				sType = "A";
				setResultNoteList(noteRstList, 0, sTypeSetNote1); // note
			} else {
				setResultProcList(procRstList, 0, sProcess + " Water service : N");
				if (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) <= 20
						&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) <= 20
						&& NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 20) {
					setResultProcList(procRstList, 0, sProcess + " Psc <= 20 : Y");

					// High Corrosive?
					if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
						setResultProcList(procRstList, 0, sProcess + " High Corrosive : Y");
						sType = "A";
						setResultNoteList(noteRstList, 0, sTypeSetNote1); // note
					} else {
						setResultProcList(procRstList, 0, sProcess + " High Corrosive : N");
						sType = "C";
					}
					// end
				} else {
					setResultProcList(procRstList, 0, sProcess + " Psc <= 20 : N");
					sType = "A";
					setResultNoteList(noteRstList, 0, sTypeSetNote1); // note
				}
				// end
			}
		} else {
			setResultProcList(procRstList, 0, sProcess + " Temp > 176 : N");

			if ("Y".equals(StringUtil.get(item.get("BELLOWS_YN")))) {
				setResultProcList(procRstList, 0, sProcess + " Bellows : Y");

				if (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 20) {
					setResultProcList(procRstList, 0, sProcess + " Psc <= 20 : Y");

					// High Corrosive?
					if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
						setResultProcList(procRstList, 0, sProcess + " High Corrosive : Y");
						sType = "A";
					} else {
						setResultProcList(procRstList, 0, sProcess + " High Corrosive : N");
						sType = "B";
					}
					// end
				} else {
					setResultProcList(procRstList, 0, sProcess + " Psc <= 20 : N");
					sType = "A";
				}
			} else {
				setResultProcList(procRstList, 0, sProcess + " Bellows : N");
				sType = "A";
			}
		}

		setResultProcList(procRstList, 0, "[Type] Result : " + sType);
		item.put("ABC_TYPE", sType);

		return true;
	}

	private boolean step_type_after(Map<String, Object> item, Map<String, Object> fp) throws Exception {

//		List<Map<String,Object>> sealRstList = (List<Map<String,Object>>)fp.get("sealRstList");
//		List<Map<String,Object>> material1RstList = (List<Map<String,Object>>)fp.get("material1RstList");//Inner
//		List<Map<String,Object>> material2RstList = (List<Map<String,Object>>)fp.get("material2RstList");//Inner
//		List<Map<String,Object>> material3RstList = (List<Map<String,Object>>)fp.get("material3RstList");//Inner
//		List<Map<String,Object>> material4RstList = (List<Map<String,Object>>)fp.get("material4RstList");//Inner
//		List<Map<String,Object>> material1OutRstList = (List<Map<String,Object>>)fp.get("material1OutRstList");//Outer
//		List<Map<String,Object>> material2OutRstList = (List<Map<String,Object>>)fp.get("material2OutRstList");//Outer
//		List<Map<String,Object>> material3OutRstList = (List<Map<String,Object>>)fp.get("material3OutRstList");//Outer
//		List<Map<String,Object>> material4OutRstList = (List<Map<String,Object>>)fp.get("material4OutRstList");//Outer
//		
//		List<Map<String,Object>> planRstList = (List<Map<String,Object>>)fp.get("planRstList");
//		
//		List<Map<String,Object>> noteRstList = (List<Map<String,Object>>)fp.get("noteRstList");
//		List<Map<String,Object>> procRstList  = (List<Map<String,Object>>)fp.get("procRstList");
//		
//		String[] saProductGroup = (String[])fp.get("saProductGroup");
//		String[] saProduct = (String[])fp.get("saProduct");

		return true;

	}

	private boolean step_arrangement_pre(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		// ---------------------------------
		// 지정된 Plan이 있을 경우 처리
		// ---------------------------------

		// Plan별 Arrangement, configuration 정보
		List<Map<String, Object>> planList = rBMapper.selectRuleComListE002(null);

		String sPlanDir = StringUtil.get(item.get("API_PLAN_DIR")).toUpperCase(); // 직접입력 plan

		//System.out.println("sPlanDir : " + sPlanDir);

		// 직접입력 Plan이 있을 경우
		if (!"".equals(sPlanDir)) {
			int iArrangement = 0;

//			MCD	SCD	CD_NM	ORD	  ATTR1	ATTR2	ATTR3	ATTR4	ATTR5	ATTR6	ATTR7	ATTR8	ATTR9	ATTR10	ATTR11	ATTR12	ATTR13	ATTR14	ATTR15	RMKS	REG_DT	REG_ID	MOD_DT	MOD_ID	ATTR16	ATTR17	ATTR18	ATTR19	ATTR20
//			E002	E002010	1CW	1	1	1	CW	01												[NULL]	[NULL]	[NULL]	[NULL]	[NULL]					
//			E002	E002020	1CW	2	1	1	CW	02												[NULL]	[NULL]	[NULL]	[NULL]	[NULL]					

			String sTmpArr = "";
			String sTmpConfig = "";
			for (String sPlan : sPlanDir.split("/")) {

				//System.out.println("sPlan : " + sPlan);

				for (Map<String, Object> pm : planList) {
					if (sPlan.equals(StringUtil.get(pm.get("ATTR4")))) {
						sTmpArr = StringUtil.get(pm.get("ATTR1")); // arrangement

						if (!"Q".equals(sTmpArr)) {
							if (iArrangement < NumberUtil.toInt(sTmpArr)) {
								iArrangement = NumberUtil.toInt(sTmpArr);
								sTmpConfig = "" + pm.get("ATTR2") + pm.get("ATTR3");
							}
						}

						break;
					}
				}
			}

			if (iArrangement > 0) {
				// item.put("ARRANGEMENT", ""+iArrangement);
				item.put("API_PLAN_DIR_ARRANGEMENT", "" + iArrangement);
				item.put("API_PLAN_DIR_CONFIG", "" + sTmpConfig);
				// setResultProcList(procRstList, 0, " API Plan 지정에 따른 Arrangement : " +
				// iArrangement);
			}
		}

		// Configuration 이 입력된 경우
		if (!"".equals(StringUtil.get(item.get("SEAL_CONFIG")))) {
			Map<String, Object> ptm = new HashMap<String, Object>();
			ptm.put("MCD", "Z150");
			ptm.put("SCD", item.get("SEAL_CONFIG"));
			List<Map<String, Object>> rComList = getRuleComListType1(ptm);

			if (!rComList.isEmpty()) {
				String sTmpArr = "" + (rComList.get(0)).get("ATTR1");
				// item.put("ARRANGEMENT", sTmpArr);

				// 직접입력 Plan과 비교하여 처리
				if (NumberUtil.toInt(item.get("API_PLAN_DIR_ARRANGEMENT")) < NumberUtil.toInt(sTmpArr)) {
					item.put("API_PLAN_DIR_ARRANGEMENT", "" + sTmpArr);
					item.put("API_PLAN_DIR_CONFIG", "" + item.get("SEAL_CONFIG"));
				}

				// setResultProcList(procRstList, 0, " Configuration 설정에 따른 Arrangement : " +
				// sTmpArr);
			}
		}

		// 사용자 지정 Type : Single : S, Dual : D
		String sSingleDualGb = StringUtil.get(item.get("S_D_GB"));
		if (!"".equals(sSingleDualGb)) {
			if ("D".equals(sSingleDualGb)) { // dual로 선택된 경우
				// 설정된 Arrangement가 1이하일 경우 A2로 설정한다.
				// A3로는?

				// Caustic 유체일 경우 Arrangement 3으로 설정한다.
				if (isProduct("SODIUM HYDROXIDE", saProductGroup, saProduct)) {
					setResultProcList(procRstList, 0, " 사용자 지정 Single/Dual 설정 & Caustic 유체에 따른 Arrangement : " + "3");
					item.put("API_PLAN_DIR_ARRANGEMENT", "3");
				} else {
					if (NumberUtil.toInt(item.get("API_PLAN_DIR_ARRANGEMENT")) <= 1) {
						setResultProcList(procRstList, 0, " 사용자 지정 Single/Dual 설정에 따른 Arrangement : " + "2");
						item.put("API_PLAN_DIR_ARRANGEMENT", "2");
					}
				}

			}

		}

		//System.out.println("API_PLAN_DIR_ARRANGEMENT : " + item.get("API_PLAN_DIR_ARRANGEMENT"));

		return true;
	}

	private boolean step_arrangement(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		// Arrangement가 설정되었으면 Skip
		// if(!"".equals(StringUtil.get(item.get("ARRANGEMENT")))) return true;

		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");
		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		String sArrangement = "";
		boolean bIsTurnPoint1 = false;
		String sProcess = "[Arrangement]";

		if ("Y".equals(StringUtil.get(item.get("PC_TOXIC_CHK")))) { // Toxic Service ?
			setResultProcList(procRstList, 0, sProcess + " Toxic  Service : Y");
			sArrangement = "3";

		} else {

			setResultProcList(procRstList, 0, sProcess + " Toxic  Service : N");
			if (NumberUtil.toDouble(item.get("TEMP_NOR")) < -50 && NumberUtil.toDouble(item.get("TEMP_MIN")) < -50
					&& NumberUtil.toDouble(item.get("TEMP_MAX")) < -50) {
				setResultProcList(procRstList, 0, sProcess + " Temp < -50 : Y");
				sArrangement = "3";
			} else {
				setResultProcList(procRstList, 0, sProcess + " Temp < -50 : N");

				// H2S = HYDROGEN SULFIDE
				if (isProduct("HYDROGEN SULFIDE", saProductGroup, saProduct)
						&& getProductCont(item, "HYDROGEN SULFIDE", "PPM") > 50) {
					setResultProcList(procRstList, 0, sProcess + " H2S, H2S > 50 PPM : Y");
					// H2S = HYDROGEN SULFIDE
					if (getProductCont(item, "HYDROGEN SULFIDE", "PPM") > 500) {
						setResultProcList(procRstList, 0, sProcess + " H2S > 500 PPM : Y");
						sArrangement = "3";
					} else {
						setResultProcList(procRstList, 0, sProcess + " H2S > 500 PPM : N");
						bIsTurnPoint1 = true;
					}
				} else {
					setResultProcList(procRstList, 0, sProcess + " H2S, H2S > 50 PPM : N");
					// Hazardous or Flammable service ?
					if ("Y".equals(StringUtil.get(item.get("PC_HAZARD_CHK")))
							|| "Y".equals(StringUtil.get(item.get("PC_FLAM_CHK")))) {
						setResultProcList(procRstList, 0, sProcess + " Hazardous or Flammable  service : Y");
						bIsTurnPoint1 = true;
					} else {
						setResultProcList(procRstList, 0, sProcess + " Hazardous or Flammable  service : N");

						// Hydrocarbon Service or VOC 규제 물질?
						// Voc 규제물질 유무
						boolean vIsVocProduct = isVOC(saProductGroup, saProduct);

						System.out.println("is Hydrocarbon : " + isProduct("HYDROCARBON", saProductGroup, saProduct));
						System.out.println("is voc : " + vIsVocProduct);

						if (isProduct("HYDROCARBON", saProductGroup, saProduct) || vIsVocProduct) {
							setResultProcList(procRstList, 0, sProcess + " Hydrocarbon Service or VOC 규제 물질 : Y");
							bIsTurnPoint1 = true;
						} else {
							setResultProcList(procRstList, 0, sProcess + " Hydrocarbon Service or VOC 규제 물질 : N");
							sArrangement = "1";
						}
					}

				}
			}
		}

		// 분기점 1이 활성화 된 경우
		if (bIsTurnPoint1) {

			if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) { // High Corrosive?
				setResultProcList(procRstList, 0, sProcess + " High Corrosive : Y");

				sArrangement = "3";

			} else {
				setResultProcList(procRstList, 0, sProcess + " High Corrosive : N");

				// pour point 유무
				boolean bIsPourPoint = false;
				// 굳는성질 Residue 굳는성질, Oil 굳는성질
				// if("Y".equals(StringUtil.get(item.get("RESI_HRDN_GB"))) ||
				// "Y".equals(StringUtil.get(item.get("OIL_HRDN_YN")))
				// ) {
				// 굳는성질 -> Cooling으로 굳는성질로 대체
				if ("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { // Cooling으로 굳는성질
					bIsPourPoint = true;
				}

				// pour point or 결정화
				if (bIsPourPoint || "Y".equals(StringUtil.get(item.get("PC_LEAKAGE_CHK")))) {
					setResultProcList(procRstList, 0, sProcess + " pour point or 결정화 : Y");
					sArrangement = "3";

				} else {
					setResultProcList(procRstList, 0, sProcess + " pour point or 결정화 : N");

					// VPM 확보? - a 또는 b 둘 중 하나 만족
					// a. Psc - V.P ≥ 3.5 bar -> barg로 맞춤
					// b. Psc ≥ V.P X 1.3 -> 절대압력 기준
					// V.P 는 barA 이므로 Psc의 barG와 단위를 맞추어 계산
					// 입력된 값이 있을 경우 서로 비교처리 - 02/21
					boolean is1A = isVPMchk1(item);
					boolean is1B = isVPMchk2(item);

					if (is1A || is1B) {
						setResultProcList(procRstList, 0, sProcess + " VPM 확보 : Y");
						sArrangement = "2";
					} else {
						setResultProcList(procRstList, 0, sProcess + " VPM 확보 : N");

						// Temp > 60℃ ?
						if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 60
								|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 60
								|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 60) {
							setResultProcList(procRstList, 0, sProcess + " Temp > 60℃ : Y");
							sArrangement = "2";
							// Plan : 23
							// item.put("PLAN_INNER", "23"); // API Plan Inner Set

						} else {
							setResultProcList(procRstList, 0, sProcess + " Temp > 60℃ : N");

							// QBQLZ 적용 가능?(Operating Window)
							Map<String, Object> c3Param = getC3CheckParam(item, "QBQLZ", "", fp);
							c3Param.put("type", item.get("ABC_TYPE")); // type

//							double dSealSizetmp = getSealSize(item, "MM", "QBQLZ", "", "1", fp);
//							setResultProcList(procRstList, 0, sProcess+" Seal Size[QBQLZ] : " + dSealSizetmp + " MM");
//							c3Param = (HashMap<String,Object>)((HashMap<String,Object>)item).clone(); // item Parma 복사
//							c3Param.put("SEAL_TYPE","QBQLZ");
//							c3Param.put("SEAL_SIZE", dSealSizetmp ); //  mm 로 변경
//							c3Param.put("type",item.get("ABC_TYPE")); // type
//							// 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60
//							c3Param.put("L_SPD_MAX",3.14 * dSealSizetmp  * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속
//							//c3Param.put("SEAL_CHAM_NOR",item.get("SEAL_CHAM_NOR"));
//							//c3Param.put("SEAL_CHAM_MIN",item.get("SEAL_CHAM_MIN"));
//							//c3Param.put("SEAL_CHAM_MAX",item.get("SEAL_CHAM_MAX"));

							if (isC3OperatingCheck(c3Param, "QBQLZ")) {
								setResultProcList(procRstList, 0, sProcess + " QBQLZ 적용 가능 : Y");

								// Solid/Particle 체크
								// ------------------------------
								// Solid 체크
								// ------------------------------
								boolean bSolidChk1 = false; // solid 체크 유무
								double dSolidCont = NumberUtil.toDouble(item.get("SOLID_CONT")); // solid 농도 ppm
								if ("".equals(item.get("SOLID_GB")) || "N".equals(item.get("SOLID_GB"))) {
									bSolidChk1 = false;
								} else if ("Y".equals(item.get("SOLID_GB"))) {
									bSolidChk1 = true;
								} else if ("Y1".equals(item.get("SOLID_GB"))) {
									double dSolidSize = 0.d;
									if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_NOR")))
										dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_NOR"));
									if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_MIN")))
										dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_MIN"));
									if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_MAX")))
										dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_MAX"));

									// Solid / Particle 체크 조건
									// * 20μm이상, 10000ppm 초과 -> solid size max 체크 시 사이즈 기준 여기적용
									// * 10 - 20μm, 400ppm 초과
									if (((dSolidSize >= 20
											|| "Y".equals(StringUtil.get(item.get("SOLID_SIZE_MAX_CHK"))))
											&& dSolidCont > 10000)
											|| (dSolidSize >= 10 && dSolidSize < 20 && dSolidCont > 400)) {
										bSolidChk1 = true;
									}
								}

								if (bSolidChk1) {
									setResultProcList(procRstList, 0, sProcess + " Solid/Particle 체크 : Y");
									sArrangement = "3";
								} else {
									setResultProcList(procRstList, 0, sProcess + " Solid/Particle 체크 : N");
									sArrangement = "2";
								}
								// end
							} else {
								setResultProcList(procRstList, 0, sProcess + " QBQLZ 적용 가능 : N");
								sArrangement = "3";
							}

//							//비중 0.4 ~ 0.6 and
//							//VPM 0.34 ~ 3.4 barg 이내  - VPM : Psc - V.P   :  Nor 기준
//							//-> barg로 단위 맞춤
//							boolean is2A = false;
//							
//							if(NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 0.4 &&
//									NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) >= 0.4 &&
//									NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) >= 0.4 &&
//									NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) <= 0.6 &&
//									NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) <= 0.6 &&
//									NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) <= 0.6) {
//								is2A = true;
//							}
//							
//							boolean is2B = isVPMchk3(item);
//							
//							if(is2A && is2B) {
//								setResultProcList(procRstList, 0, sProcess+" 비중, VPM 확보 : Y");
//								sArrangement = "2";
//								// Seal Type : QBQLZ 
//								item.put("SEAL_TYPE_INNER", "QBQLZ"); // Seal Type Inner Set
//							}else {
//								setResultProcList(procRstList, 0, sProcess+" 비중, VPM 확보 : N");
//								sArrangement = "3";
//							}
						}

					}
				}

			}

		}

		// Arrangement Set
		setResultProcList(procRstList, 0, sProcess + " result : " + sArrangement);
//		item.put("ARRANGEMENT",sArrangement);

		// Arrangement Set
		// process에서 설정된 arrangement가 사용자 입력결과 arrangement 보다 상위일 경우
		if (NumberUtil.toInt(sArrangement) < NumberUtil.toInt(item.get("API_PLAN_DIR_ARRANGEMENT"))) {
			setResultProcList(procRstList, 0, " 사용자 지정에 따라 Arrangement 변경 : " + item.get("API_PLAN_DIR_ARRANGEMENT"));
			item.put("ARRANGEMENT", item.get("API_PLAN_DIR_ARRANGEMENT"));
		} else { // process 결과 arrangement set
			// setResultProcList(procRstList, 0, sProcess+" result : " + sArrangement);
			item.put("ARRANGEMENT", sArrangement);
		}

		return true;
	}

	private boolean step_arrangement_after(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
//		List<Map<String,Object>> material1RstList = (List<Map<String,Object>>)fp.get("material1RstList");//Inner
//		List<Map<String,Object>> material2RstList = (List<Map<String,Object>>)fp.get("material2RstList");//Inner
//		List<Map<String,Object>> material3RstList = (List<Map<String,Object>>)fp.get("material3RstList");//Inner
//		List<Map<String,Object>> material4RstList = (List<Map<String,Object>>)fp.get("material4RstList");//Inner
//		List<Map<String,Object>> material1OutRstList = (List<Map<String,Object>>)fp.get("material1OutRstList");//Outer
//		List<Map<String,Object>> material2OutRstList = (List<Map<String,Object>>)fp.get("material2OutRstList");//Outer
//		List<Map<String,Object>> material3OutRstList = (List<Map<String,Object>>)fp.get("material3OutRstList");//Outer
//		List<Map<String,Object>> material4OutRstList = (List<Map<String,Object>>)fp.get("material4OutRstList");//Outer

		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");

//		List<Map<String,Object>> noteRstList = (List<Map<String,Object>>)fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		for (Map<String, Object> sm : sealRstList) {

			int iPIdx = 99999;

			if (sm.get("ADD_INFO") != null) {
				Map<String, Object> addInfo = (HashMap<String, Object>) sm.get("ADD_INFO");
				// End User, FTA 에서 등록된 Seal Type을 제외 한 idx 중 시작값
				if ("1".equals(addInfo.get("R_TYPE"))) {
					continue;
				} else {
					iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
				}
			} else {
				iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
			}

			// [C7-14] Hot Oil 일 경우
			if (isProduct("OIL", saProductGroup, saProduct)) {

				// 유체 성질에 따른 Plan 선정
				// 굳는 성질이 있는 유체일 경우
				// Single Seal : Plan 02/62 or 32/62 적용
				// Dual Seal : Plan 53A/B/C or 54 적용
				// 굳는 성질이 없는 유체일 경우
				// Single Seal : Plan 23/62
				// Dual Seal : Plan 23/52

				if (NumberUtil.toDouble(item.get("TEMP_NOR")) >= 80) { // 0il & 80C 이상

					// 중간진행사항
					setResultProcList(procRstList, iPIdx,
							"[C7-14] Hot Oil Rule 적용 : Arrangement : " + StringUtil.get(item.get("ARRANGEMENT"), "-"));

					if ("Y".equals(StringUtil.get(item.get("OIL_HRDN_YN")))) { // 굳는성질일 경우

						setResultProcList(procRstList, iPIdx, "[C7-14] 굳는성질 : Y");

						if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {

							setResultListPlan(planRstList, iPIdx, "53A", null, fp);
							setResultListPlan(planRstList, iPIdx, "53B", null, fp);
							setResultListPlan(planRstList, iPIdx, "53C", null, fp);
							setResultListPlan(planRstList, iPIdx, "54", null, fp);
							// 중간진행사항
							setResultProcList(procRstList, iPIdx, "[C7-14] Arrangement Dual : API Plan : 53A/B/C,54");

						} else {
							setResultListPlan(planRstList, iPIdx, "02/62", null, fp);
							setResultListPlan(planRstList, iPIdx, "32/62", null, fp);
							// 중간진행사항
							setResultProcList(procRstList, iPIdx,
									"[C7-14] Arrangement Single : API Plan : 02/62, 32/62");
						}
					} else {
						setResultProcList(procRstList, iPIdx, "[C7-14] 굳는성질 : N");

						if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {
							setResultListPlan(planRstList, iPIdx, "23/52", null, fp);

							// 중간진행사항
							setResultProcList(procRstList, iPIdx, "[C7-14] Arrangement Dual : API Plan : 23/52");

						} else {
							setResultListPlan(planRstList, iPIdx, "23/62", null, fp);

							// 중간진행사항
							setResultProcList(procRstList, iPIdx, "[C7-14] Arrangement Dual : API Plan : 23/62");
						}
					}

				}

			}

		}

		// Pump TYpe = VS4 인 경우
		// Arrangement 2일때 arrangement 3으로 변경
		String sPumpTypeG = getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")), _pumpTypeGroupInfo, null);
		if ("2".equals(StringUtil.get(item.get("ARRANGEMENT"))) && "VS4".equals(sPumpTypeG)) {
			item.put("ARRANGEMENT", "3");
			setResultProcList(procRstList, 0, "VS4에 따라 Arrangement 3으로 재설정");
		}

		return true;

	}

	/**
	 * 룰 추천 Process에서 SEAL Type 설정
	 * 
	 * @param item
	 * @param fp
	 * @return
	 * @throws Exception
	 */
	private boolean step_seal_type(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");

		// ------------------------------------
		// 직접입력된 Seal Type이 있을 경우 지정된 Seal C3 체크
		// ------------------------------------
		// SEAL_INNER_DIR - inboard
		// SEAL_OUTER_DIR - outboard
		String sSealTypeInDir = StringUtil.get(item.get("SEAL_INNER_DIR"));
		String sSealTypeOutDir = StringUtil.get(item.get("SEAL_OUTER_DIR"));
		String sSealTypeDir = "";
		if (!"".equals(sSealTypeOutDir))
			sSealTypeDir = sSealTypeInDir + "/" + sSealTypeOutDir;

		if (!"".equals(sSealTypeInDir) || !"".equals(sSealTypeOutDir)) {

			// 입력된 지정 Seal 체크
			if (!"".equals(sSealTypeOutDir) && "".equals(sSealTypeInDir)) {
				setResultNoteList(noteRstList, 0, "Inbaord Seal 정보가 잘못되었습니다..");
				return true;
			}

			String sDirSealConfig = StringUtil.get(item.get("DIR_SEAL_CONFIG"));

			// 입력된 지정 Seal의 Arrangement와 Process 결과로 나온 Arrangement 비교
			Map<String, Object> param = new HashMap<String, Object>();

			param.put("MCD", "E004");
			param.put("ATTR1", sSealTypeInDir);
			List<Map<String, Object>> rComList = getRuleComListType1(param);
			String sArrChkNote = "";
			if (!rComList.isEmpty()) {
				Map<String, Object> rc = rComList.get(0);
				String sSD_gb = StringUtil.get(rc.get("ATTR2")); // Seal 자체가 싱글 또는 듀얼인 경우 체크

				if ("S".equals(sSD_gb)) { // 자체가 싱글로만 쓰는 경우
					if (!"".equals(sSealTypeOutDir)) {
						sArrChkNote = "Single 타입 Seal에 Outboard Seal이 설정되었습니다.";
					} else if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {
						sArrChkNote = "지정 Seal의 Arrangement와 추천에 의한 Arrangement가 일치하지 않습니다. ";
					}
				} else if ("D".equals(sSD_gb)) { // 자체가 듀얼로만 쓰는 경우
					if (!"".equals(sSealTypeOutDir)) {
						sArrChkNote = "Dual 타입 Seal에 Outboard Seal이 설정되었습니다.";
					} else {
						boolean bIs = false;
						for (String c : sDirSealConfig.split(",")) {
							if (c.equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								bIs = true;
							}
						}
						if (!bIs) {
							sArrChkNote = "지정 Seal의 Arrangement와 추천에 의한 Arrangement가 일치하지 않습니다. ";
						}
					}
				}
			} else {
				if ("".equals(sSealTypeOutDir)) { // single
					if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {
						sArrChkNote = "지정 Seal의 Arrangement와 추천에 의한 Arrangement가 일치하지 않습니다. ";
					}
				} else { // dual
					if (NumberUtil.toInt(item.get("ARRANGEMENT")) == 1) {
						sArrChkNote = "지정 Seal의 Arrangement와 추천에 의한 Arrangement가 일치하지 않습니다. ";
					} else {
						boolean bIs = false;
						for (String c : sDirSealConfig.split(",")) {
							if (c.equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								bIs = true;
							}
						}
						if (!bIs) {
							sArrChkNote = "지정 Seal의 Arrangement와 추천에 의한 Arrangement가 일치하지 않습니다. ";
						}
					}
				}
			}

			if (!"".equals(sArrChkNote)) {
				setResultNoteList(noteRstList, 0, sArrChkNote);
				return true;
			}

			// double dShaftSize = NumberUtil.toInt(item.get("SHAFT_SIZE")); // shaft dia.

			boolean bIsSealInOk = true;
			boolean bIsSealOutOk = true;
			if (!"".equals(sSealTypeInDir)) {
				bIsSealInOk = isC3OperatingCheck2(item, sSealTypeInDir, sSealTypeDir, 0, fp);
			}

			if (!"".equals(sSealTypeOutDir)) {
				bIsSealOutOk = isC3OperatingCheck2(item, sSealTypeOutDir, sSealTypeDir, 0, fp);
			}

			if (bIsSealInOk && bIsSealOutOk) {
				// Seal 등록
				setResultList(sealRstList, -1,
						sSealTypeInDir + ("".equals(sSealTypeOutDir) ? "" : "/" + sSealTypeOutDir), null, fp);

			} else {
				if (!bIsSealInOk)
					setResultNoteList(noteRstList, 0, "지정된 Seal이 Operating Window 조건을 벗어남 : " + sSealTypeInDir);
				if (!bIsSealOutOk)
					setResultNoteList(noteRstList, 0, "지정된 Seal이 Operating Window 조건을 벗어남 : " + sSealTypeOutDir);
			}

			return true;

		}

		// ------------------------------------
		// Seal 선정 프로세스
		// ------------------------------------

		String sAPI682 = StringUtil.get(item.get("API682_YN")); // API682 적용 유무
		// if("N".equals(sAPI682)"NA".equals(StringUtil.get(item.get("PUMP_TYPE")))){ //
		// Non API Pump일 경우
		if ("N".equals(sAPI682)) { // Non API Pump일 경우
			step_seal_type_non_api(item, fp);
		}

		if (!"N".equals(sAPI682) || "seal_model_tp1".equals(StringUtil.get(item.get("process_step")))
				|| "seal_model_tp0".equals(StringUtil.get(item.get("process_step")))) {
			step_seal_type_api(item, fp); // API Pump 로직
		}

		return true;
	}

	private String step_seal_type_api(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		System.out.println("API Seal Process");

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");
		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		// 주속 (m/s) RPM -> m/s
		// 3.14 * Sahft dia. /1000 * 속도(RPM) / 60
		// double dLineSpeed = Math.round(3.14 *
		// NumberUtil.toDouble(item.get("SHAFT_SIZE")) *
		// NumberUtil.toDouble(item.get("RPM_NOR")) /1000 / 60 * 100.0) / 100.0 ;

		// Seal Size
		// double dSealSizeIN = getSealSize(item, "IN"); // IN
		// double dSealSizeMM = getSealSize(item, "MM"); // MM

		// System.out.println("step_seal_type_api / dSealSize MM : " + dSealSizeMM);

		String sSubStep = "";
		String sSubTypeA = "", sSubTypeC = "";
		// int iPIdx = NumberUtil.toInt(item.get("P_IDX"));

		// API Pump 유무
		boolean bIsAPIPump = "Y".equals(StringUtil.get(item.get("API682_YN"))) ? true : false;

		// seal_model_tp0 : Non-API Pump에서 API 682 Seal로 변경으로 지정된 경우
		// 이 단계에서 다시 API682 Plan을 탈수있게 한다.
		if ("seal_model_tp0".equals(StringUtil.get(item.get("process_step")))) {
			bIsAPIPump = true;
		}

		String sPumpTypeG = getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")), _pumpTypeGroupInfo, null); // pump
																												// type

		// ------------------------------
		// Solid 체크
		// ------------------------------
		boolean bSolidChk1 = false; // solid 체크 유무
		double dSolidCont = NumberUtil.toDouble(item.get("SOLID_CONT")); // solid 농도 ppm
		if ("".equals(item.get("SOLID_GB")) || "N".equals(item.get("SOLID_GB"))) {
			bSolidChk1 = false;
		} else if ("Y".equals(item.get("SOLID_GB"))) {
			bSolidChk1 = true;
		} else if ("Y1".equals(item.get("SOLID_GB"))) {
			double dSolidSize = 0.d;
			if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_NOR")))
				dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_NOR"));
			if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_MIN")))
				dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_MIN"));
			if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_MAX")))
				dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_MAX"));

			// Solid / Particle 체크 조건
			// * 20μm이상, 10000ppm 초과 -> solid size max 체크 시 사이즈 기준 여기적용
			// * 10 - 20μm, 400ppm 초과
			if (((dSolidSize >= 20 || "Y".equals(StringUtil.get(item.get("SOLID_SIZE_MAX_CHK")))) && dSolidCont > 10000)
					|| (dSolidSize >= 10 && dSolidSize < 20 && dSolidCont > 400)) {
				bSolidChk1 = true;
			}
		}

		// Type B
		if (bIsAPIPump && "B".equals(StringUtil.get(item.get("ABC_TYPE")))) {

			// 주속 23m/s 초과?
			// Seal Type이 정해지지 않으므로 Shaft Size로 산정
			// 3.14 * Sahft dia. /1000 * 속도(RPM) / 60
			double dLineSpeedWithoutSeal = Math.round(3.14 * NumberUtil.toDouble(item.get("SHAFT_SIZE"))
					* NumberUtil.toDouble(item.get("RPM_MAX")) / 1000 / 60 * 100.0) / 100.0;

			if (dLineSpeedWithoutSeal > 23) {

				setResultProcList(procRstList, 0, "[Seal API] dLineSpeed > 23 : Y -> " + dLineSpeedWithoutSeal);

				// Type A or Type C 변경 요
				sSubTypeA = "Y";
				sSubTypeC = "Y";
			} else {

				setResultProcList(procRstList, 0, "[Seal API] dLineSpeed > 23 : N -> " + dLineSpeedWithoutSeal);

				// Pump Type = OH1 ?
				if ("OH1".equals(sPumpTypeG)) {

					setResultProcList(procRstList, 0, "[Seal API] OH1 : Y ");

					// ISC2-682 운전 한계 이내?
					// 1) 비중 0.6이상
					// and
					// 2) 압력한계
					// Arrangement 1 & 2 - Operating window 참고
					// (ISC2-682BX, ISC2-682BB 2CW-CW)
					// Arrangement 3 - a and b 조건 만족
					// a. Psc ≤ 5.6 bar
					// b. Max Psc - Min Psc ≤ 2.0 bar

					boolean bc1 = false, bc2 = false;
					double dSealSizetmp = 0;
					if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 0.6
							|| NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) >= 0.6
							|| NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) >= 0.6) {
						bc1 = true;
					}

					if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))
							|| "".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						// ISC2-682BX - C3 조건 적용유무
						Map<String, Object> c3Param = getC3CheckParam(item, "ISC2-682BX", "", fp);
						c3Param.put("type", item.get("ABC_TYPE")); // type
//						dSealSizetmp = getSealSize(item, "MM", "ISC2-682BX", "", "1", fp);
//						setResultProcList(procRstList, 0, "[Seal API] Seal Size[ISC2-682BX기준] : " + dSealSizetmp + " MM");
//						Map<String,Object> c3Param = (HashMap<String,Object>)((HashMap<String,Object>)item).clone(); // item Parma 복사
//						c3Param.put("SEAL_TYPE","ISC2-682BX");
//						//c3Param.put("SEAL_SIZE",dSealSizeMM); // mm 로 변경
//						c3Param.put("SEAL_SIZE", dSealSizetmp ); //  mm 로 변경
//						c3Param.put("type",item.get("ABC_TYPE")); // type
//						// 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60
//						c3Param.put("L_SPD_NOR", 0);
//						c3Param.put("L_SPD_MIN", 0);
//						c3Param.put("L_SPD_MAX",3.14 * dSealSizetmp  * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속
//						//c3Param.put("SEAL_CHAM_NOR",item.get("SEAL_CHAM_NOR"));
//						//c3Param.put("SEAL_CHAM_MIN",item.get("SEAL_CHAM_MIN"));
//						//c3Param.put("SEAL_CHAM_MAX",item.get("SEAL_CHAM_MAX"));

						bc2 = isC3OperatingCheck(c3Param, "ISC2-682BX");

					} else if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						// ISC2-682BB 2CW-CW - C3 조건 적용유무
						Map<String, Object> c3Param = getC3CheckParam(item, "ISC2-682BB", "", fp);
						c3Param.put("type", item.get("ABC_TYPE")); // type
//						dSealSizetmp = getSealSize(item, "MM", "ISC2-682BB", "", "1", fp);
//						setResultProcList(procRstList, 0, "[Seal API] Seal Size[ISC2-682BB기준] : " + dSealSizetmp + " MM");
//						Map<String,Object> c3Param = (HashMap<String,Object>)((HashMap<String,Object>)item).clone(); // item Parma 복사
//						c3Param.put("SEAL_TYPE","ISC2-682BB");
//						c3Param.put("SEAL_SIZE", dSealSizetmp ); //  mm 로 변경
//						c3Param.put("type",item.get("ABC_TYPE")); // type
//						// 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60
//						c3Param.put("L_SPD_NOR", 0);
//						c3Param.put("L_SPD_MIN", 0);
//						c3Param.put("L_SPD_MAX",3.14 * dSealSizetmp  * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속
//						//c3Param.put("SEAL_CHAM_NOR",item.get("SEAL_CHAM_NOR"));
//						//c3Param.put("SEAL_CHAM_MIN",item.get("SEAL_CHAM_MIN"));
//						//c3Param.put("SEAL_CHAM_MAX",item.get("SEAL_CHAM_MAX"));
						c3Param.put("CONFIGURATION", "2CW-CW"); // Configuration

						bc2 = isC3OperatingCheck(c3Param, "ISC2-682BB");

					} else if ("3".equals(StringUtil.get(item.get("ARRANGEMENT")))) { // arrangement 3
						if (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) <= 5.6
								&& ((NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"))
										- NumberUtil.toDouble(item.get("SEAL_CHAM_MIN"))) <= 2)) {
							bc2 = true;
						}
					}

					if (bc1 && bc2) {
						setResultProcList(procRstList, 0, "[Seal API] ISC2-682 운전 한계 이내 : Y ");

						// A1 : Plan 23 - ISC2-682XB / 그 외 - ISC2-682BX
						// A2 and A3 : ISC2-682BB
						if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
							setResultProcList(procRstList, 0, "[Seal API] ARRANGEMENT 1 : Y");
							// 결정된 API에 따라 Seal Type 설정
							// iPIdx++;
							setResultList(sealRstList, -1, "__seal_model_type_B_api_1__", null, fp);

						} else {
							// iPIdx++;
							// setResultList(sealRstList, -1, getSealType(item, "ISC2-682BB"), null, fp);
							setResultList(sealRstList, -1, "ISC2-682BB", null, fp);

							setResultProcList(procRstList, 0, "[Seal API] ARRANGEMENT 1 : N");
							setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : ISC2-682BB");
						}

					} else {
						setResultProcList(procRstList, 0, "[Seal API] ISC2-682 운전 한계 이내 : N ");
						sSubStep = "type_B_sub_2";
					}
				} else {
					setResultProcList(procRstList, 0, "[Seal API] OH1 : N ");
					sSubStep = "type_B_sub_2";
				}
			}
		}

		// Type B 의 분기 2
		if ("type_B_sub_2".equals(sSubStep)) {

			// Arrangement 3?
			if ("3".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
				setResultProcList(procRstList, 0, "[Seal API] Arrangement 3 : Y ");
				sSubStep = "type_B_sub_1";
			} else {
				setResultProcList(procRstList, 0, "[Seal API] Arrangement 3 : N ");

				// VPM 확보?
				// a. Psc - V.P ≥ 3.5 bar -> barg로 단위맞춤
				// b. Psc ≥ V.P X 1.3 -> 절대압기준
				// a 또는 b 둘 중 하나 만족
				boolean is1A = isVPMchk1(item);
				boolean is1B = isVPMchk2(item);

				if (is1A || is1B) {
					setResultProcList(procRstList, 0, "[Seal API] VPM 확보 : Y ");

					if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXW");
						// BXW/BXW
						// iPIdx++;
						setResultList(sealRstList, -1, "BXW", null, fp);

					} else {
						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXW/BXW");
						// BXW/BXW
						// iPIdx++;
						setResultList(sealRstList, -1, "BXW/BXW", null, fp);
					}

				} else {
					setResultProcList(procRstList, 0, "[Seal API] VPM 확보 : N ");

					if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 60 || NumberUtil.toDouble(item.get("TEMP_MIN")) > 60
							|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 60) {
						setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : Y ");

						if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
							setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXW");
							setResultList(sealRstList, -1, "BXW", null, fp);

							item.put("PLAN_INNER", "23"); // API Plan Inner Set

						} else {
							setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXW/BXW");
							// PLAN 23 + BXW/BXW
							// iPIdx++;
							setResultList(sealRstList, -1, "BXW/BXW", null, fp);

							item.put("PLAN_INNER", "23"); // API Plan Inner Set
						}

					} else {
						setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : N ");

						setResultProcList(procRstList, 0, "[Seal API] Arrangement 3으로 변경");
						item.put("ARRANGEMENT", "3");
						sSubStep = "type_B_sub_1";
					}
				}
			}

			/*
			 * //Arrangement 1? if("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
			 * setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : Y "); //BXW
			 * //iPIdx++; setResultList(sealRstList, -1, "BXW", null, fp); }else {
			 * setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : N ");
			 * //Arrangement 2? if("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
			 * setResultProcList(procRstList, 0, "[Seal API] Arrangement 2 : Y "); //VPM 확보?
			 * //a. Psc - V.P ≥ 3.5 bar -> barg로 단위맞춤 //b. Psc ≥ V.P X 1.3 -> 절대압기준 //a 또는 b
			 * 둘 중 하나 만족 boolean is1A = isVPMchk1(item); boolean is1B = isVPMchk2(item);
			 * 
			 * if (is1A || is1B ) { setResultProcList(procRstList, 0,
			 * "[Seal API] VPM 확보 : Y "); setResultProcList(procRstList, 0,
			 * "[Seal API] Seal 선정 : BXW/BXW"); //BXW/BXW //iPIdx++;
			 * setResultList(sealRstList, -1, "BXW/BXW", null, fp); }else {
			 * setResultProcList(procRstList, 0, "[Seal API] VPM 확보 : N "); //Temp > 60℃ if
			 * ( NumberUtil.toDouble(item.get("TEMP_NOR")) > 60 ||
			 * NumberUtil.toDouble(item.get("TEMP_MIN")) > 60 ||
			 * NumberUtil.toDouble(item.get("TEMP_MAX")) > 60 ){
			 * 
			 * setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : Y ");
			 * setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXW/BXW"); //PLAN 23
			 * + BXW/BXW //iPIdx++; setResultList(sealRstList, -1, "BXW/BXW", null, fp);
			 * item.put("PLAN_INNER", "23"); // API Plan Inner Set }else {
			 * setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : N ");
			 * //Arrangement 3 변경 setResultProcList(procRstList, 0,
			 * "[Seal API] Arrangement 3으로 변경"); item.put("ARRANGEMENT","3"); sSubStep =
			 * "type_B_sub_1"; } }
			 * 
			 * }else { setResultProcList(procRstList, 0, "[Seal API] Arrangement 2 : N ");
			 * sSubStep = "type_B_sub_1"; } }
			 */

		}

		// Type B 의 분기 1
		if ("type_B_sub_1".equals(sSubStep)) {
			// Psc ≤ 5.6 barg
			if (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) <= 5.6
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) <= 5.6
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 5.6) {

				setResultProcList(procRstList, 0, "[Seal API] Psc ≤ 5.6 barg : Y ");

				// Plan 53B 요구?
				if (isPlan("53B", StringUtil.get(item.get("API_PLAN_DIR")))) {
					setResultProcList(procRstList, 0, "[Seal API] Plan 53B 요구 : Y ");
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXW/BXW");
					// 3CW-BB BXW/BXW
					// iPIdx++;
					setResultList(sealRstList, -1, "BXW/BXW", null, fp);

					item.put("SEAL_CONFIG", "3CW-BB");
				} else {
					setResultProcList(procRstList, 0, "[Seal API] Plan 53B 요구 : N ");
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXBW/BXW");
					// 3CW-FB BXBW/BXW
					// iPIdx++;
					setResultList(sealRstList, -1, "BXBW/BXW", null, fp);

					item.put("SEAL_CONFIG", "3CW-FB");
				}

			} else {
				setResultProcList(procRstList, 0, "[Seal API] Psc ≤ 5.6 barg : N ");
				setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXW/BXW");
				// 3CW-BB BXW/BXW
				// iPIdx++;
				setResultList(sealRstList, -1, "BXW/BXW", null, fp);

				item.put("SEAL_CONFIG", "3CW-BB");
			}
		}

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		// 주속 23m/s 초과?
		// Seal Type이 정해지지 않으므로 Shaft Size로 산정
		// 3.14 * Sahft dia. /1000 * 속도(RPM) / 60
		// double dLineSpeedWithoutSeal = Math.round(3.14 *
		// NumberUtil.toDouble(item.get("SHAFT_SIZE")) *
		// NumberUtil.toDouble(item.get("RPM_NOR")) /1000 / 60 * 100.0) / 100.0 ;

		// ------------------------
		// Type A
		// ------------------------
		// sSubTypeA =Y 일경우 추가
		if (bIsAPIPump && ("A".equals(StringUtil.get(item.get("ABC_TYPE"))) || "Y".equals(sSubTypeA))) {
			// QBW Seal 압력한계 초과? 1번그래프
			// if (isQBWPressChk(item, engine, "1", dSealSizeIN )) {

			double dSealSizetmp = getSealSize(item, "IN", "QBW", "", "1", fp);
			setResultProcList(procRstList, 0, "[Seal API] Seal Size[QBW기준] : " + dSealSizetmp + " IN");

			if (isQBWPressChk(item, engine, "1", NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")), dSealSizetmp)) { // In로
																													// 적용

				setResultProcList(procRstList, 0, "[Seal API] QBW Seal 압력한계 초과? 1번그래프 : Y ");

				if ("3".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
					setResultProcList(procRstList, 0, "[Seal API] Arrangement 3 : Y ");
					sSubStep = "type_A_sub_1";

				} else {
					setResultProcList(procRstList, 0, "[Seal API] Arrangement 3 : N ");

					// Solid / Particle
					// * 20μm이상, 10000ppm
					// or
					// * 10 - 20μm, 400ppm
					if (bSolidChk1) {
						setResultProcList(procRstList, 0, "[Seal API] Solid/Particle Check : Y ");
						sSubStep = "type_A_sub_1";

					} else {
						setResultProcList(procRstList, 0, "[Seal API] Solid/Particle Check : N ");

						// VPM 확보?
						// a. Psc - V.P ≥ 3.5 bar -> barg로 단위맞춤
						// b. Psc ≥ V.P X 1.3 -> 절대압기준
						// a 또는 b 둘 중 하나 만족

						if (isVPMchk1(item) || isVPMchk2(item)) {
							setResultProcList(procRstList, 0, "[Seal API] VPM확보 : Y ");
							sSubStep = "type_A_sub_1";
						} else {
							setResultProcList(procRstList, 0, "[Seal API] VPM확보 : N ");

							// Temp 60도 초과유무
							if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 60
									|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 60
									|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 60) {
								setResultProcList(procRstList, 0, "[Seal API] Temp > 60 C : Y ");
								sSubStep = "type_A_sub_1";
							} else {
								setResultProcList(procRstList, 0, "[Seal API] Temp > 60 C : N ");

								// QBW Seal 압력한계 초과? 2번그래프
								dSealSizetmp = getSealSize(item, "IN", "QBQLZ", "", "1", fp);
								setResultProcList(procRstList, 0,
										"[Seal API] Seal Size[QBQLZ기준] : " + dSealSizetmp + " IN");

								if (isQBWPressChk(item, engine, "2", NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")),
										dSealSizetmp)) { // In로 적용
									setResultProcList(procRstList, 0, "[Seal API] QBW Seal 압력한계 초과? 2번그래프: Y ");
									item.put("ARRANGEMENT", "3"); // Arrangement3으로 변경
									setResultProcList(procRstList, 0, "[Seal API] Arrangement3으로 변경");
									sSubStep = "type_A_sub_1";

								} else {
									setResultProcList(procRstList, 0, "[Seal API] QBW Seal 압력한계 초과? 2번그래프: N ");

									// QBQLZ 적용 가능?(Operating Window)
									Map<String, Object> c3Param = getC3CheckParam(item, "QBQLZ", "", fp);
									c3Param.put("type", item.get("ABC_TYPE")); // type
									boolean bIsB = isC3OperatingCheck(c3Param, "QBQLZ");

									if (bIsB) {
										setResultProcList(procRstList, 0, "[Seal API] QBQLZ 적용 가능 : Y ");

										if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
											item.put("ARRANGEMENT", "2"); // Arrangement2으로 변경
											setResultProcList(procRstList, 0, "[Seal API] Arrangement2으로 변경");
										}

										setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBQLZ/QBQW");
										setResultList(sealRstList, -1, "QBQLZ/QBQW", null, fp);

									} else {
										setResultProcList(procRstList, 0, "[Seal API] QBQLZ 적용 가능 : N ");
										item.put("ARRANGEMENT", "3"); // Arrangement3으로 변경
										setResultProcList(procRstList, 0, "[Seal API] Arrangement3으로 변경");
										sSubStep = "type_A_sub_1";
									}
								}
							}
						}
					}
				}

				/*
				 * if("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
				 * setResultProcList(procRstList, 0, "[Seal API] Arrangement 2 : Y ");
				 * 
				 * //QBW Seal 압력한계 초과? 2번그래프 //if(isQBWPressChk(item, engine, "2", dSealSizeIN
				 * )) { dSealSizetmp = getSealSize(item, "IN", "QBQLZ", "", "1", fp);
				 * setResultProcList(procRstList, 0, "[Seal API] Seal Size[QBQLZ기준] : " +
				 * dSealSizetmp + " IN");
				 * 
				 * if (isQBWPressChk(item, engine, "2",
				 * NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")), dSealSizetmp )) { // In로 적용
				 * setResultProcList(procRstList, 0, "[Seal API] QBW Seal 압력한계 초과? 2번그래프: Y ");
				 * sSubStep = "type_A_sub_1";
				 * 
				 * }else { setResultProcList(procRstList, 0,
				 * "[Seal API] QBW Seal 압력한계 초과? 2번그래프: N ");
				 * 
				 * 
				 * //a, b 모두 만족? //a. T < 60℃ //b. QBQLZ 적용 가능?(Operating Window)
				 * 
				 * //QBQLZ 적용 가능?(Operating Window) Map<String,Object> c3Param =
				 * getC3CheckParam(item, "QBQLZ", "", fp);
				 * c3Param.put("type",item.get("ABC_TYPE")); // type // double dSealSizeMMtmp =
				 * getSealSize(item, "MM", "QBQLZ", "", "1", fp); //
				 * setResultProcList(procRstList, 0, "[Seal API] Seal Size[QBQLZ] : " +
				 * dSealSizeMMtmp + " MM"); // Map<String,Object> c3Param =
				 * (HashMap<String,Object>)((HashMap<String,Object>)item).clone(); //
				 * c3Param.put("SEAL_TYPE","QBQLZ"); // c3Param.put("SEAL_SIZE", dSealSizeMMtmp
				 * ); // mm 로 변경 // c3Param.put("type",item.get("ABC_TYPE")); // type // //
				 * 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60 //
				 * c3Param.put("L_SPD_NOR", 0); // c3Param.put("L_SPD_MIN", 0); //
				 * c3Param.put("L_SPD_MAX",3.14 * dSealSizeMMtmp * 0.00328084 *
				 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속 //
				 * //c3Param.put("SEAL_CHAM_NOR",item.get("SEAL_CHAM_NOR")); //
				 * //c3Param.put("SEAL_CHAM_MIN",item.get("SEAL_CHAM_MIN")); //
				 * //c3Param.put("SEAL_CHAM_MAX",item.get("SEAL_CHAM_MAX")); boolean bIsB =
				 * isC3OperatingCheck(c3Param, "QBQLZ");
				 * 
				 * if( (NumberUtil.toDouble(item.get("TEMP_NOR")) < 60 &&
				 * NumberUtil.toDouble(item.get("TEMP_MIN")) < 60 &&
				 * NumberUtil.toDouble(item.get("TEMP_MAX")) < 60 ) && bIsB ) {
				 * 
				 * setResultProcList(procRstList, 0, "[Seal API] T < 60℃ & QBQLZ 적용 가능 : Y ");
				 * 
				 * //Solid / Particle //* 20μm이상, 10000ppm //or //* 10 - 20μm, 400ppm
				 * if(bSolidChk1) { setResultProcList(procRstList, 0,
				 * "[Seal API] Solid/Particle Check : Y "); sSubStep = "type_A_sub_1";
				 * 
				 * }else { setResultProcList(procRstList, 0,
				 * "[Seal API] Solid/Particle Check : N "); setResultProcList(procRstList, 0,
				 * "[Seal API] Seal 선정 : QBQLZ/QBQW"); //iPIdx++; setResultList(sealRstList, -1,
				 * "QBQLZ/QBQW", null, fp);
				 * 
				 * }
				 * 
				 * }else { setResultProcList(procRstList, 0,
				 * "[Seal API] T < 60℃ & QBQLZ 적용 가능 : N "); sSubStep = "type_A_sub_1";
				 * 
				 * }
				 * 
				 * // //And 조건으로 만족? // //a. Temp 60℃ 미만 // //b. 비중 0.4 ~ 0.6 // //c. VPM 0.34 ~
				 * 3.4bar 이내 -> 단위맞춤 Barg // if( // (NumberUtil.toDouble(item.get("TEMP_NOR")) <
				 * 60 && // NumberUtil.toDouble(item.get("TEMP_MIN")) < 60 && //
				 * NumberUtil.toDouble(item.get("TEMP_MAX")) < 60 ) // && //
				 * (NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 0.4 && //
				 * NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) >= 0.4 && //
				 * NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) >= 0.4 && //
				 * NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) <= 0.6 && //
				 * NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) <= 0.6 && //
				 * NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) <= 0.6) // && //
				 * isVPMchk3(item) // ) { // // setResultProcList(procRstList, 0,
				 * "[Seal API] Temp 60℃ 미만 & 비중 0.4 ~ 0.6 & VPM 0.34 ~ 3.4bar 이내 : Y "); //
				 * setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBQLZ/QBQW"); //
				 * //QBQLZ/QBQW // //iPIdx++; // setResultList(sealRstList, -1, "QBQLZ/QBQW",
				 * null, fp); // }else { // setResultProcList(procRstList, 0,
				 * "[Seal API] Temp 60℃ 미만 & 비중 0.4 ~ 0.6 & VPM 0.34 ~ 3.4bar 이내 : N "); //
				 * sSubStep = "type_A_sub_1"; // } }
				 * 
				 * }else { setResultProcList(procRstList, 0, "[Seal API] Arrangement 2 : N ");
				 * sSubStep = "type_A_sub_1"; }
				 */

			} else {
				setResultProcList(procRstList, 0, "[Seal API] QBW Seal 압력한계 초과? 1번그래프 : N ");

				// Temp > 200℃ and Water Service?
				// and Arrangement 1일 경우
				boolean bTempNWaterService = false;
				if ((NumberUtil.toDouble(item.get("TEMP_NOR")) > 200 || NumberUtil.toDouble(item.get("TEMP_MIN")) > 200
						|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 200)
						&& isProduct("[WATER-BASE]", saProductGroup, saProduct)
						&& "1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
					bTempNWaterService = true;
				}

				// High Corrosive?
				boolean bIsHighCorrosive = false;
				if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK"))))
					bIsHighCorrosive = true;

				if (bTempNWaterService && !bIsHighCorrosive) {
					setResultProcList(procRstList, 0, "[Seal API] Temp > 200℃ & Water Service & A1? : Y ");
					setResultProcList(procRstList, 0, "[Seal API] High Corrosive : N ");

					// 주속 23m/s 초과?
					// 3.14 * QB Seal Size /1000 * 속도(RPM) / 60 --> face mean size 기준
					double dSealSize = getSealSize(item, "MM", "DPW", "", "2", fp); // MM
					setResultProcList(procRstList, 0, "[Seal API] DPW Face Mean Size : " + dSealSize + " MM");
					double dLineSpeed = Math.round(
							3.14 * dSealSize * NumberUtil.toDouble(item.get("RPM_MAX")) / 1000 / 60 * 100.0) / 100.0;

					if (dLineSpeed > 23) {
						setResultProcList(procRstList, 0, "[Seal API] 주속 23m/s 초과 : Y -> : " + dLineSpeed);
						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : HSH");
						// setResultList(sealRstList, -1, getSealType(item, "HSH"), null, fp);
						setResultList(sealRstList, -1, "HSH", null, fp);

					} else {
						setResultProcList(procRstList, 0, "[Seal API] 주속 23m/s 초과 : N -> : " + dLineSpeed);
						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : DPW");
						// setResultList(sealRstList, -1, getSealType(item, "DPW"), null, fp);
						setResultList(sealRstList, -1, "DPW", null, fp);

					}

				} else {
					setResultProcList(procRstList, 0, "[Seal API] Temp > 200℃ & Water Service & A1? : N ");

					// water service가 Y이고 High Corrosive가 Y인 경우로 여기 진입 시 표시
					if (bTempNWaterService && bIsHighCorrosive) {
						setResultProcList(procRstList, 0, "[Seal API] High Corrosive : Y ");
					}

					// 주속 23m/s 초과?
					// 3.14 * QB Seal Size /1000 * 속도(RPM) / 60 --> face mean size 기준
					double dSealSize = getSealSize(item, "MM", "QBW", "", "2", fp); // MM
					setResultProcList(procRstList, 0, "[Seal API] QBW Face Mean Size : " + dSealSize + " MM");
					double dLineSpeed = Math.round(
							3.14 * dSealSize * NumberUtil.toDouble(item.get("RPM_MAX")) / 1000 / 60 * 100.0) / 100.0;

					if (dLineSpeed > 23) {

						setResultProcList(procRstList, 0, "[Seal API] 주속 23m/s 초과 : Y -> " + dLineSpeed);

						// A1 - QBRW
						// A2 or A3 - QBRW/QBRW
						// iPIdx++;
						if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
							setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : Y ");
							setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBRW");
							setResultList(sealRstList, -1, "QBRW", null, fp);

						} else {
							setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : N ");
							setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBRW/QBRW");
							setResultList(sealRstList, -1, "QBRW/QBRW", null, fp);

						}
					} else {

						setResultProcList(procRstList, 0, "[Seal API] 주속 23m/s 초과 : N -> " + dLineSpeed);

						// Pump Type = OH1 ?
						if ("OH1".equals(sPumpTypeG)) {

							setResultProcList(procRstList, 0, "[Seal API] Pump Type = OH1 : Y ");

							// ISC2-682 운전 한계 이내? ISC2-682 적용 가능?
							// 1) Operating Window 이내
							// (ISC2-682PX, ISC2-682PP)
							// and
							// 2) 압력한계
							// Arrangement 1 & 2 -> psc <= 20.7 barg
							// Arrangement 3 -> a and b 조건 만족
							// a. Psc ≤ 5.6 bar
							// b. Max Psc - Min Psc ≤ 2.0 bar

							boolean bc1 = false, bc2 = false;
//							if ( NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 0.6 ||
//									NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) >= 0.6 ||
//									NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) >= 0.6 
//									) {
//								bc1 = true;
//							}

							//
							boolean bIsISC2682 = false;
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))
									|| "".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

								Map<String, Object> c3Param = getC3CheckParam(item, "ISC2-682PX", "", fp);
								c3Param.put("type", item.get("ABC_TYPE")); // type
//								double dSealSizeMMtmp = getSealSize(item, "MM", "ISC2-682PX", "", "1", fp);
//								Map<String,Object> c3Param = (HashMap<String,Object>)((HashMap<String,Object>)item).clone(); // item Parma 복사
//								c3Param.put("SEAL_TYPE","ISC2-682PX");
//								c3Param.put("SEAL_SIZE", dSealSizeMMtmp ); //  mm 로 변경
//								c3Param.put("type",item.get("ABC_TYPE")); // type
//								// 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60
//								c3Param.put("L_SPD_NOR", 0);
//								c3Param.put("L_SPD_MIN", 0);
//								c3Param.put("L_SPD_MAX", 3.14 * dSealSizeMMtmp  * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속
//								//c3Param.put("SEAL_CHAM_NOR",item.get("SEAL_CHAM_NOR"));
//								//c3Param.put("SEAL_CHAM_MIN",item.get("SEAL_CHAM_MIN"));
//								//c3Param.put("SEAL_CHAM_MAX",item.get("SEAL_CHAM_MAX"));
								bIsISC2682 = isC3OperatingCheck(c3Param, "ISC2-682PX");
							} else {

								System.out.println("ISC2-682PP 기준 O/W 체크");

								Map<String, Object> c3Param = getC3CheckParam(item, "ISC2-682PP", "", fp);
								c3Param.put("type", item.get("ABC_TYPE")); // type
//								double dSealSizeMMtmp = getSealSize(item, "MM", "ISC2-682PP", "", "1", fp);
//								Map<String,Object> c3Param = (HashMap<String,Object>)((HashMap<String,Object>)item).clone(); // item Parma 복사
//								//c3Param.clear();
//								c3Param.put("SEAL_TYPE","ISC2-682PP");
//								c3Param.put("SEAL_SIZE", dSealSizeMMtmp ); //  mm 로 변경
//								c3Param.put("type",item.get("ABC_TYPE")); // type
//								// 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60
//								c3Param.put("L_SPD_NOR", 0);
//								c3Param.put("L_SPD_MIN", 0);
//								c3Param.put("L_SPD_MAX", 3.14 * dSealSizeMMtmp  * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속
//								//c3Param.put("SEAL_CHAM_NOR",item.get("SEAL_CHAM_NOR"));
//								//c3Param.put("SEAL_CHAM_MIN",item.get("SEAL_CHAM_MIN"));
//								//c3Param.put("SEAL_CHAM_MAX",item.get("SEAL_CHAM_MAX"));
								bIsISC2682 = isC3OperatingCheck(c3Param, "ISC2-682PP");
							}

							// if(bIsISC2682PX && bIsISC2682PP ) {
							if (bIsISC2682) {
								bc1 = true;
							}

							//System.out.println("ARRANGEMENT: " + item.get("ARRANGEMENT"));

							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))
									|| "2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

								System.out.println("NumberUtil.toDouble(item.get(\"SEAL_CHAM_MAX\")) : "
										+ NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")));

								if (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 20.7) {
									bc2 = true;
								}

							} else { // arrangement 3
								if ((NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) <= 5.6
										&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) <= 5.6
										&& NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 5.6)
										&& ((NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"))
												- NumberUtil.toDouble(item.get("SEAL_CHAM_MIN"))) <= 2)) {
									bc2 = true;
								}
							}

							//System.out.println("bIsISC2682 : " + bIsISC2682);
							//System.out.println("bc1 : " + bc1);
							//System.out.println("bc2 : " + bc2);

							if (bc1 && bc2) {

								setResultProcList(procRstList, 0, "[Seal API] ISC2-682 운전 한계 이내 : Y ");

								// A1 : Plan 23 - ISC2-682XP / 그 외 - ISC2-682PX
								// A2 and A3 : ISC2-682PP
								// 결정된 API에 따라 Seal Type 설정
								// iPIdx++;
								setResultList(sealRstList, -1, "__seal_model_type_A_api_2__", null, fp);

							} else {
								setResultProcList(procRstList, 0, "[Seal API] ISC2-682 운전 한계 이내 : N ");
								item.put("process_step", "seal_model_tp1"); // 단계 분기정보 Set - Seal Model 1번 분기
							}
						} else {
							setResultProcList(procRstList, 0, "[Seal API] Pump Type = OH1 : N ");
							item.put("process_step", "seal_model_tp1"); // 단계 분기정보 Set - Seal Model 1번 분기
						}
					}
				}

			}

		}

		// Type A 의 분기 1
		if ("type_A_sub_1".equals(sSubStep)) {
			// Psc ≤ 51.7 barg
			if (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) <= 51.7
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) <= 51.7
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 51.7) {

				setResultProcList(procRstList, 0, "[Seal API] Psc ≤ 51.7 barg : Y ");

				// A1. HSH
				// A2 and A3 : HSH/HSH
				if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
					setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : Y ");
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : HSH");
					// iPIdx++;
					setResultList(sealRstList, -1, "HSH", null, fp);

				} else { // arrangement 2,3
					setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : N ");
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : HSH/HSH");
					// iPIdx++;
					setResultList(sealRstList, -1, "HSH/HSH", null, fp);
				}
			} else {
				setResultProcList(procRstList, 0, "[Seal API] Psc ≤ 51.7 barg : N ");
				// A1 : Plan 23 - DHTW / 그 외 - UHTW
				// A2 : Plan 23 - DHTW/DHTW / 그 외 - UHTW/DHTW
				// A3 : DHTW/DHTW, 3CW-FF

				// 결정된 API에 따라 Seal Type 설정
				// iPIdx++;
				setResultList(sealRstList, -1, "__seal_model_type_A_api_1__", null, fp);
			}
		}

		// Type A 의 Seal 모델 간 분기 1
		if ("seal_model_tp1".equals(StringUtil.get(item.get("process_step")))) {

			// Arrangement 3?
			if ("3".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
				setResultProcList(procRstList, 0, "[Seal API] Arrangement 3 : Y ");
				sSubStep = "type_A_sub_3";
			} else {
				setResultProcList(procRstList, 0, "[Seal API] Arrangement 3 : N ");

				// VPM 확보?
				// a. Psc - V.P ≥ 3.5 bar -> barg로 단위맞춤
				// b. Psc ≥ V.P X 1.3 -> 절대압기준
				// a 또는 b 둘 중 하나 만족
				if (isVPMchk1(item) || isVPMchk2(item)) {
					setResultProcList(procRstList, 0, "[Seal API] VPM 확보 : Y ");
					sSubStep = "type_A_sub_2";
				} else {
					setResultProcList(procRstList, 0, "[Seal API] VPM 확보 : N ");

					// Temp > 60℃
					if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 60 || NumberUtil.toDouble(item.get("TEMP_MIN")) > 60
							|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 60) {

						setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : Y ");
						sSubStep = "type_A_sub_2";

					} else {
						setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : N ");

						// 비중 0.6이하 and
						// VPM 0.34 ~ 3.4 barg이내 -> barg로 단위맞춤
						if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) <= 0.6 && isVPMchk3(item)) {
							setResultProcList(procRstList, 0, "[Seal API] 비중 0.6이하 & VPM 0.34 ~ 3.4 barg이내: Y ");

							// Solid / Particle
							// * 20μm이상, 10000ppm
							// or
							// * 10 - 20μm, 400ppm
							if (bSolidChk1) {
								setResultProcList(procRstList, 0, "[Seal API] Solid/Particle Check : Y ");
								sSubStep = "type_A_sub_2";
							} else {
								setResultProcList(procRstList, 0, "[Seal API] Solid/Particle Check : N ");

								if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
									item.put("ARRANGEMENT", "2"); // Arrangement2으로 변경
									setResultProcList(procRstList, 0, "[Seal API] Arrangement2으로 변경");
								}

								setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBQLZ/QBQW");
								// QBQLZ /QBQW
								setResultList(sealRstList, -1, "QBQLZ/QBQW", null, fp);

							}

						} else {
							setResultProcList(procRstList, 0, "[Seal API] 비중 0.6이하 & VPM 0.34 ~ 3.4 barg이내: N ");

							item.put("ARRANGEMENT", "3");
							setResultProcList(procRstList, 0, "[Seal API] Arrangement 3으로 변경 ");

							sSubStep = "type_A_sub_3";
						}
					}
				}
			}

			/*
			 * //Arrangement 1? if("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
			 * setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : Y "); sSubStep
			 * = "type_A_sub_2";
			 * 
			 * }else { setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : N ");
			 * //Arrangement 2? if("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
			 * setResultProcList(procRstList, 0, "[Seal API] Arrangement 2 : Y "); //VPM 확보?
			 * //a. Psc - V.P ≥ 3.5 bar -> barg로 단위맞춤 //b. Psc ≥ V.P X 1.3 -> 절대압기준 //a 또는 b
			 * 둘 중 하나 만족
			 * 
			 * // if ( (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) -
			 * (NumberUtil.toDouble(item.get("VAP_PRES_NOR"))-1) >= 3.5 && //
			 * NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) -
			 * (NumberUtil.toDouble(item.get("VAP_PRES_MIN"))-1) >= 3.5 && //
			 * NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) -
			 * (NumberUtil.toDouble(item.get("VAP_PRES_MAX"))-1) >= 3.5 ) // || //
			 * ((NumberUtil.toDouble(item.get("SEAL_CHAM_NOR"))+1) >=
			 * NumberUtil.toDouble(item.get("VAP_PRES_NOR")) * 1.3 && //
			 * (NumberUtil.toDouble(item.get("SEAL_CHAM_MIN"))+1) >=
			 * NumberUtil.toDouble(item.get("VAP_PRES_MIN")) * 1.3 && //
			 * (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"))+1) >=
			 * NumberUtil.toDouble(item.get("VAP_PRES_MAX")) * 1.3 ) // ) { if (
			 * isVPMchk1(item) || isVPMchk2(item)) { setResultProcList(procRstList, 0,
			 * "[Seal API] VPM 확보 : Y "); sSubStep = "type_A_sub_2";
			 * 
			 * }else { setResultProcList(procRstList, 0, "[Seal API] VPM 확보 : N "); //Temp >
			 * 60℃ if(NumberUtil.toDouble(item.get("TEMP_NOR")) > 60 ||
			 * NumberUtil.toDouble(item.get("TEMP_MIN")) > 60 ||
			 * NumberUtil.toDouble(item.get("TEMP_MAX")) > 60 ) {
			 * 
			 * setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : Y "); //Plan 23 +
			 * QBQW/QBQW //iPIdx++; setResultList(sealRstList, -1, "QBQW/QBQW", null, fp);
			 * item.put("PLAN_INNER", "23"); // API Plan Inner Set }else {
			 * setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : N "); //비중 0.6이하
			 * and //VPM 0.34 ~ 3.4 barg이내 -> barg로 단위맞춤 if (
			 * NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) <= 0.6 && isVPMchk3(item) )
			 * {
			 * 
			 * setResultProcList(procRstList, 0,
			 * "[Seal API] 비중 0.6이하 & VPM 0.34 ~ 3.4 barg이내: Y ");
			 * setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBQLZ/QBQW"); //QBQLZ
			 * /QBQW //iPIdx++; setResultList(sealRstList, -1, "QBQLZ/QBQW", null, fp);
			 * }else { setResultProcList(procRstList, 0,
			 * "[Seal API] 비중 0.6이하 & VPM 0.34 ~ 3.4 barg이내: N ");
			 * 
			 * //Arrangement3의 경우로 이동하는 케이스로 Arangement를 3으로 변경 후 이동한다.
			 * item.put("ARRANGEMENT","3"); setResultProcList(procRstList, 0,
			 * "[Seal API] Arrangement 3으로 변경 ");
			 * 
			 * sSubStep = "type_A_sub_3"; } } } }else { setResultProcList(procRstList, 0,
			 * "[Seal API] Arrangement 2 : N "); sSubStep = "type_A_sub_3"; } }
			 */
		}

		// Type A 의 분기 2
		if ("type_A_sub_2".equals(sSubStep)) {
			// Hydrocarbon with SG < 0.65
			if (isProduct("HYDROCARBON", saProductGroup, saProduct)
					&& NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) < 0.65) {
				setResultProcList(procRstList, 0, "[Seal API] Hydrocarbon with SG < 0.65 : Y ");
				// A1 - QBQW
				// A2 - QBQW/QBQW
				if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
					setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : Y ");
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBQW");

					// iPIdx++;
					setResultList(sealRstList, -1, "QBQW", null, fp);

				} else {
					setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : N ");
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBQW/QBQW");

					// iPIdx++;
					setResultList(sealRstList, -1, "QBQW/QBQW", null, fp);
				}
			} else {
				setResultProcList(procRstList, 0, "[Seal API] Hydrocarbon with SG < 0.65 : N ");
				// End User = 현대오일뱅크
				if ("Z140020".equals(StringUtil.get(item.get("END_USER")))) {
					setResultProcList(procRstList, 0, "[Seal API] End User = 현대오일뱅크 : Y ");
					// A1 - QBQW
					// A2 - QBQW/QBQW
					if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : Y");
						// iPIdx++;
						setResultList(sealRstList, -1, "QBQW", null, fp);

						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBQW");

					} else {
						setResultProcList(procRstList, 0, "[Seal API] Arrangement 2,3 : Y");
						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBQW/QBQW");

						// iPIdx++;
						setResultList(sealRstList, -1, "QBQW/QBQW", null, fp);

					}
				} else {
					setResultProcList(procRstList, 0, "[Seal API] End User = 현대오일뱅크 : N ");
					// A1 - QBW
					// A2- QBW/QBW
					if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : Y");
						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBW");
						// iPIdx++;
						setResultList(sealRstList, -1, "QBW", null, fp);

					} else {
						setResultProcList(procRstList, 0, "[Seal API] Arrangement 2,3 : Y");
						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBW/QBW");
						// iPIdx++;
						setResultList(sealRstList, -1, "QBW/QBW", null, fp);

					}
				}
			}
		}

		// Type A 의 분기 3
		if ("type_A_sub_3".equals(sSubStep)) {
			// Psc ≤ 5.6 barg
			if (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) <= 5.6
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) <= 5.6
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 5.6) {

				setResultProcList(procRstList, 0, "[Seal API] Psc ≤ 5.6 barg : Y ");

				// 53B 요구여부
				if (isPlan("53B", StringUtil.get(item.get("API_PLAN_DIR")))) {
					setResultProcList(procRstList, 0, "[Seal API] 53B Plan 요구 : Y ");
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QB2B/QBW");
					// QBB/QBW
					// iPIdx++;
					setResultList(sealRstList, -1, "QB2B/QBW", null, fp);

				} else {
					setResultProcList(procRstList, 0, "[Seal API] 53B Plan 요구 : N ");
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QBB/QBW");
					// QBB/QBW
					// iPIdx++;
					setResultList(sealRstList, -1, "QBB/QBW", null, fp);
				}

			} else {
				setResultProcList(procRstList, 0, "[Seal API] Psc ≤ 5.6 barg : N ");
				setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : QB2B/QBW");
				// QB2B/QBW
				// iPIdx++;
				setResultList(sealRstList, -1, "QB2B/QBW", null, fp);
			}
		}

		// Type C
		// sSubTypeㅊ =Y 일경우 추가
		if (bIsAPIPump && ("C".equals(StringUtil.get(item.get("ABC_TYPE"))) || "Y".equals(sSubTypeC))) {
			// Arrangement 1?
			if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
				setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : Y ");
				setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXRH");
				// iPIdx++;
				setResultList(sealRstList, -1, "BXRH", null, fp);
			} else { // arrangement 2,3
				setResultProcList(procRstList, 0, "[Seal API] Arrangement 1 : N ");

				// 주속 23m/s 초과?
				// 결과가 BXHHS Seal이라 BXHHS 기준으로 주속 계산 - face mean size 기준
				// 3.14 * BXHHS/BXHHS Seal Size /1000 * 속도(RPM) / 60
				double dSealSize = getSealSize(item, "MM", "BXHHS/BXHHS", "", "2", fp); // MM
				setResultProcList(procRstList, 0, "[Seal API] BXHHS/BXHHS Face Mean Size : " + dSealSize + " MM");
				double dLineSpeed = Math
						.round(3.14 * dSealSize * NumberUtil.toDouble(item.get("RPM_MAX")) / 1000 / 60 * 100.0) / 100.0;

				if (dLineSpeed > 23) {
					setResultProcList(procRstList, 0, "[Seal API] 주속 23m/s 초과? : Y  -> " + dLineSpeed);
					setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXRH/BXRH");
					// BXRH /BXRH
					// iPIdx++;
					setResultList(sealRstList, -1, "BXRH/BXRH", null, fp);
				} else {
					setResultProcList(procRstList, 0, "[Seal API] 주속 23m/s 초과? : N  -> " + dLineSpeed);
					if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						setResultProcList(procRstList, 0, "[Seal API] Arrangement 2 : Y ");
						setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXHHSW/BXHHSW");
						// BXHHSW/BXHHSW
						// iPIdx++;
						setResultList(sealRstList, -1, "BXHHSW/BXHHSW", null, fp);
					} else {
						setResultProcList(procRstList, 0, "[Seal API] Arrangement 2 : N ");

						// a. b 조건 둘 다 만족
						// a. Psc ≤ 5.6barg
						// b. Seal size 3.750 이상 : Max Psc - Min Psc ≤ 1.0

						// 적용 Seal Type은 확인 필요
						double dSealSizeTmp = getSealSize(item, "IN", "BXHHSW/BXHHSW", "", "1", fp); // In
						setResultProcList(procRstList, 0,
								"[Seal API] Seal Size[BXHHSW/BXHHSW기준] :" + dSealSizeTmp + " IN");

						if ((NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) <= 5.6
								&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) <= 5.6
								&& NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 5.6)
								&& (dSealSizeTmp >= 3.750 && (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"))
										- NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) <= 1.0))) {

							setResultProcList(procRstList, 0,
									"[Seal API] Psc ≤ 5.6barg & (Seal size 3.750 미만 : Max Psc - Min Psc ≤ 2.8, Seal size 3.750 이상 : Max Psc - Min Psc ≤ 1.1)  : Y ");

							// Plan 53B 요구?
							if (isPlan("53B", StringUtil.get(item.get("API_PLAN_DIR")))) {
								setResultProcList(procRstList, 0, "[Seal API] Plan 53B 요구 : Y");
								setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXHHSW/BXHHSW");
								// 3CW-BB + BXHHSW/BXHHSW
								// iPIdx++;
								setResultList(sealRstList, -1, "BXHHSW/BXHHSW", null, fp);

							} else {
								setResultProcList(procRstList, 0, "[Seal API] Plan 53B 요구 : N");
								setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXHHSBW/BXHHSW");
								// BXHHSBW/BXHHSW + Plan 53A
								// iPIdx++;
								setResultList(sealRstList, -1, "BXHHSBW/BXHHSW", null, fp);
								item.put("PLAN_OUTER", "53A"); // API Plan Outer Set
							}

						} else {
							setResultProcList(procRstList, 0,
									"[Seal API] Psc ≤ 5.6barg & (Seal size 3.750 미만 : Max Psc - Min Psc ≤ 2.8, Seal size 3.750 이상 : Max Psc - Min Psc ≤ 1.1)  : N ");
							setResultProcList(procRstList, 0, "[Seal API] Seal 선정 : BXHHSW/BXHHSW");
							// 3CW-BB + BXHHSW/BXHHSW
							// iPIdx++;
							setResultList(sealRstList, -1, "BXHHSW/BXHHSW", null, fp);

							item.put("SEAL_CONFIG", "3CW-BB");
						}
					}

				}

			}
		}

		return "";
	}

	private String step_seal_type_non_api(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		System.out.println("Non-API Seal Process");

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		int iPIdx = NumberUtil.toInt(item.get("P_IDX"));

		// double dSealSizeMM = getSealSize(item, "MM");

		// Cartridge 선택
		// API682=Y => Cartridge가 기본
		// API682=N => Mon-Cartridge가 기본
		String sCartridgeType = StringUtil.get(item.get("CARTRIDGE_TYPE"));
		if ("".equals(sCartridgeType)) {
			if ("Y".equals(StringUtil.get(item.get("API682_YN")))) {
				sCartridgeType = "Z160010"; // 값이 없을 경우 cartridge로 설정
			} else {
				sCartridgeType = "Z160020"; // 값이 없을 경우 Mon-cartridge로 설정
			}
		}

		// Non-Cartridge를 먼저 체크
		// Cartridge 변경 프로세스 발생 시 변경 후 Cartridge 프로세스 다시 적용
		boolean _bIsChgCartridge = false;

		// ---------------------
		// Non-Cartridge Type
		// ---------------------
		if ("Z160020".equals(sCartridgeType)) {

			setResultProcList(procRstList, 0, "[Seal Non-API] Non-Cartridge Type : Y");

			// Arrangement 1?
			if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

				setResultProcList(procRstList, 0, "[Seal Non-API] Arrangement 1 : Y");

				// Shaft size 100mm 초과?
				if (NumberUtil.toDouble(item.get("SHAFT_SIZE")) > 100) {

					setResultProcList(procRstList, 0, "[Seal Non-API] Shaft size 100mm 초과 : Y");
					setResultProcList(procRstList, 0, "[Seal NON-API] Seal 선정 : QBW");
					// iPIdx ++;
					setResultList(sealRstList, -1, "QBW", null, fp);
				} else {

					setResultProcList(procRstList, 0, "[Seal Non-API] Shaft size 100mm 초과 : N");

					// Bellows seal 요구?
					if ("Y".equals(StringUtil.get(item.get("BELLOWS_YN")))) {

						setResultProcList(procRstList, 0, "[Seal Non-API] Bellows seal : Y");

						// High Corrosive?
						if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
							setResultProcList(procRstList, 0, "[Seal Non-API] High Corrosive : Y");

							// RO Seal 압력 한계 이내?
							// iPIdx ++;
							setResultList(sealRstList, -1, "__RO_CHK_1__", null, fp);

						} else {
							setResultProcList(procRstList, 0, "[Seal Non-API] High Corrosive : N");

							setResultProcList(procRstList, 0, "[Seal NON-API] Seal 선정 : CBR");
							// iPIdx ++;
							setResultList(sealRstList, -1, "CBR", null, fp);
						}

					} else {

						setResultProcList(procRstList, 0, "[Seal Non-API] Bellows seal : N");

						// RO Seal 압력 한계 이내?
						// iPIdx ++;
						setResultList(sealRstList, -1, "__RO_CHK_1__", null, fp);
					}
				}

			} else {

				setResultProcList(procRstList, 0, "[Seal Non-API] Arrangement 1 : N");

				// Shaft size 100mm 초과?
				if (NumberUtil.toDouble(item.get("SHAFT_SIZE")) > 100) {

					setResultProcList(procRstList, 0, "[Seal Non-API] Shaft size 100mm 초과 : Y");

					// Note
					setResultNoteList(noteRstList, 0, "적용가능 모델 없음. Cartridge Design으로 변경 추천");
					setResultProcList(procRstList, 0, "[Seal Non-API] 적용가능 모델 없음. Cartridge Design으로 변경 추천");

					// Cartridge로 변경하여 추천처리
					_bIsChgCartridge = true;

				} else {

					setResultProcList(procRstList, 0, "[Seal Non-API] Shaft size 100mm 초과 : N");

					if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

						setResultProcList(procRstList, 0, "[Seal Non-API] Arrangement 2 : Y");

						// Note
						setResultNoteList(noteRstList, 0,
								"적용가능 모델 없음. <br/>1) Cartridge Design으로 변경 추천<br/>2) Arrangement 3로 변경 진행</br/>");
						setResultProcList(procRstList, 0,
								"[Seal Non-API] 적용가능 모델 없음. <br/>1) Cartridge Design으로 변경 추천<br/>2) Arrangement 3로 변경 진행</br/>");

						// Cartridge로 변경하여 추천처리
						_bIsChgCartridge = true;

						// Arrangement 3으로 변경 추천
						item.put("__ADD_PROCESS_0", "N");

					} else {

						setResultProcList(procRstList, 0, "[Seal Non-API] Arrangement 2 : N");

						// RO 압력한계 이내?
						// iPIdx ++;
						setResultList(sealRstList, -1, "__RO_CHK_2__", null, fp);

//								if(true) {
//									//int iS1 = iPIdx++;
//									iPIdx ++;
//									//setResultList(sealRstList, iPIdx, "DBL RO", null);
//									setResultList(sealRstList, iPIdx, getSealType(item, "RO/RO"), null);
//								}else {
//									iPIdx ++;
//									setResultList(sealRstList, iPIdx, "RO/PTO", null);
//								}

					}
				}
			}

		}

		// ---------------------
		// Cartridge Type
		// 추가 : Non-Cartridge Type에서 Cartridge 변경 추천이 있을 경우
		// ---------------------
		if ("Z160010".equals(sCartridgeType) || _bIsChgCartridge) {

			setResultProcList(procRstList, 0, "[Seal Non-API] Cartridge Type : Y");

			// Pumping Temperature > 200℃ ?
			if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 200 || NumberUtil.toDouble(item.get("TEMP_MIN")) > 200
					|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 200) {

				setResultProcList(procRstList, 0, "[Seal Non-API] Temperature > 200℃ : Y");

				// High Corrosive?
				if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
					setResultProcList(procRstList, 0, "[Seal Non-API] High Corrosive : Y");

					item.put("process_step", "seal_model_tp1"); // 단계 분기정보 Set
				} else {

					// water and A1?
					if (isProduct("[WATER-BASE]", saProductGroup, saProduct)
							&& "1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

						setResultProcList(procRstList, 0, "[Seal Non-API] Water & A1 : Y");

						// 주속 23m/s 초과?
						// DPW 씰기준으로 주속계산
						// 3.14 * dia. /1000 * 속도(RPM) / 60
						double dSealSize = getSealSize(item, "MM", "DPW", "", "2", fp); // MM
						setResultProcList(procRstList, 0, "[Seal Non-API] DPW Face Mean Size : " + dSealSize + " MM");
						double dLineSpeed = Math.round(3.14 * NumberUtil.toDouble(item.get("SHAFT_SIZE"))
								* NumberUtil.toDouble(item.get("RPM_MAX")) / 1000 / 60 * 100.0) / 100.0;

						if (dLineSpeed > 23) {
							setResultProcList(procRstList, 0, "[Seal Non-API] 주속 > 23 : Y -> " + dLineSpeed);
							setResultProcList(procRstList, 0, "[Seal NON-API] Seal 선정 : HSH");
							// iPIdx++;
							// setResultList(sealRstList, -1, getSealType(item, "HSH"), null, fp);
							setResultList(sealRstList, -1, "HSH", null, fp);
						} else {
							setResultProcList(procRstList, 0, "[Seal Non-API] 주속 > 23 : N -> " + dLineSpeed);
							setResultProcList(procRstList, 0, "[Seal NON-API] Seal 선정 : DPW");
							// iPIdx++;
							// setResultList(sealRstList, -1, getSealType(item, "DPW"), null, fp);
							setResultList(sealRstList, -1, "DPW", null, fp);
						}
						// end

					} else {
						setResultProcList(procRstList, 0, "[Seal Non-API] Water & A1 : N");

						// Arrangement 1?
						if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

							setResultProcList(procRstList, 0, "[Seal Non-API] Arrangement 1 : Y");

							// 1) BXRH
							// 2) BXHHSW
							// 3) QBW + FFKM O-ring
							// Note : ** 장작 가능 여부 확인 후 적용 요.
							// 장착 불가 시 Special 검토 내부 협의 필요함
							// int iS1 = iPIdx++;
							int iS1 = setResultList(sealRstList, -1, "BXRH", null, fp);

							setResultProcList(procRstList, 0, "[Seal Non-API] BXRH 적용");

							// int iS2 = iPIdx++;
							int iS2 = setResultList(sealRstList, -2, "BXHHSW", null, fp);

							setResultProcList(procRstList, 0, "[Seal Non-API] BXHHSW 적용");

							// int iS3 = iPIdx++;
							int iS3 = setResultList(sealRstList, -2, "QBW", null, fp);

							setResultProcList(procRstList, 0, "[Seal Non-API] QBW 적용");
							setResultProcList(procRstList, 0, "[Seal Non-API] FFKM 적용");

							// iS3에 대한 FFKM O-Ring Set
							setMaterialResultListPrefer(material3RstList, sealRstList, "3", iS3, 0, "[FFKM]", null,
									"IN");

							// Note
							setResultNoteList(noteRstList, iS1,
									iS1 + "~" + iS3 + " 장작 가능 여부 확인 후 적용 요, 장착 불가 시 Special 검토 내부 협의 필요함");

						} else { // arrangement 2,3
							setResultProcList(procRstList, 0, "[Seal Non-API] Arrangement 1 : N");

							// Shaft size 1.875" or 2.625" ?
							double dShaftSize_inch = 0;
							if ("IN".equals(item.get("SHAFT_SIZE_UNIT"))
									|| "INCH".equals(item.get("SHAFT_SIZE_UNIT"))) {
								dShaftSize_inch = NumberUtil.toDouble(item.get("SHAFT_SIZE_O"));
							} else {
								// dShaftSize_inch = NumberUtil.toDouble(item.get("SHAFT_SIZE")) * 0.0393701;
								dShaftSize_inch = NumberUtil.toDouble(item.get("SHAFT_SIZE")) / 25.4;
								dShaftSize_inch = Math.round(dShaftSize_inch * 1000.0) / 1000.0;
							}

							if (dShaftSize_inch == 1.875 || dShaftSize_inch == 2.625) {

								setResultProcList(procRstList, 0, "[Seal Non-API] Shaft size 1.875 or 2.625 : Y");

								// Arrangement 2?
								// iPIdx ++;
								if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
									setResultProcList(procRstList, 0, "[Seal Non-API] Arrangement 2 : Y");
									setResultProcList(procRstList, 0, "[Seal NON-API] Seal 선정 : BXHHSW/SBR");

									setResultList(sealRstList, -1, "BXHHSW/SBR", null, fp);

								} else {
									setResultProcList(procRstList, 0, "[Seal Non-API] Arrangement 2 : N");

									// a. Psc ≤ 5.6barg
									if (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) <= 5.6
											&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) <= 5.6
											&& NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 5.6) {

										setResultProcList(procRstList, 0, "[Seal Non-API] Psc ≤ 5.6barg : Y");

										// Plan 53B 요구?
										if (isPlan("53B", StringUtil.get(item.get("API_PLAN_DIR")))) {
											setResultProcList(procRstList, 0, "[Seal NON-API] 53B 요구 : Y");

											setResultProcList(procRstList, 0,
													"[Seal NON-API] Seal 선정 : Speial 검토. 내부 협의 필요함");
											setResultNoteList(noteRstList, 0, "Speial 검토. 내부 협의 필요함.");
										} else {
											setResultProcList(procRstList, 0, "[Seal NON-API] 53B 요구 : N");

											setResultProcList(procRstList, 0, "[Seal NON-API] Seal 선정 : BXHHSBW/SBR");

											// iPIdx ++;
											setResultList(sealRstList, -1, "BXHHSBW/SBR", null, fp);
										}

									} else {
										setResultProcList(procRstList, 0, "[Seal Non-API] Psc ≤ 5.6barg : N");
										setResultProcList(procRstList, 0,
												"[Seal NON-API] Seal 선정 : Speial 검토. 내부 협의 필요함");
										setResultNoteList(noteRstList, 0, "Speial 검토. 내부 협의 필요함.");
									}
								}

							} else {

								setResultProcList(procRstList, 0, "[Seal Non-API] Shaft size 1.875 or 2.625 : N");

								// API 682 Seal로 변경
								item.put("process_step", "seal_model_tp0"); // 단계 분기

								// 노트 표시 :
								// 장작 가능 여부 확인 후 적용 요
								// 장착 불가 시 Special 검토 내부 협의 필요

								setResultProcList(procRstList, 0, "[Seal Non-API] 적합한 Seal이 없어 API 682 Seal로 변경");

								// Note
								setResultNoteList(noteRstList, 0, "적합한 Seal이 없어 API 682 Seal로 변경하여 추천.");
								setResultNoteList(noteRstList, 0,
										"장작 가능 여부 확인 후 적용 요.<br/>&nbsp;장착 불가 시 Special 검토 내부 협의 필요.");

//								//1) BXHHSW/BXHHSW
//								//2) QB + FFKM O-ring
//								//** 장작 가능 여부 확인 후 적용 요.
//								//장착 불가 시 Special 검토 내부 협의 필요함
//								//int iS1 = iPIdx++;
//								int iS1 = setResultList(sealRstList, -1, "BXHHSW/BXHHSW", null, fp);
//								setResultProcList(procRstList, 0, "[Seal NON-API] Seal 선정 : BXHHSW/BXHHSW");
//								//int iS2 = iPIdx++;
//								//setResultList(sealRstList, iS2, "QB", null);   //==> 적용 방안 확인 필요 
//								int iS2 = setResultList(sealRstList, -2, getSealType(item, "QB"), null, fp);   //==> 적용 방안 확인 필요
//								setResultProcList(procRstList, 0, "[Seal NON-API] Seal 선정 : QB");
//								// iS3에 대한 FFKM O-Ring Set
//								setMaterialResultListPrefer(material3RstList,  sealRstList, "3", iS2, 0, "[FFKM]", null, "IN");
//								
//								// Note
//								setResultNoteList(noteRstList, iS1, iS1 + "~" + iS2 + " 장작 가능 여부 확인 후 적용 요, 장착 불가 시 Special 검토 내부 협의 필요함");
							}
						}
					}

				}

			} else {
				setResultProcList(procRstList, 0, "[Seal Non-API] Temperature > 200℃ : N");

				// ISC2 Series 선정 가능한 압력 조건?
				// C3에서 Seal Size와 Pessure Outer Dia. Dynamic (Seal Cham 압력) 허용조건 체크

				// [ISC2 Series Operating WIndow 체크]
				// Bellows 적용조건 유무 = Y & High Corrosive = N
				// - A1 : ISC2-BX
				// - A2,A3 : ISC2-BB

				// Bellows 적용조건 유무 = Y & High Corrosive = Y
				// Bellows 적용조건 유무 = N
				// - A1 : ISC2-PX
				// - A2,A3 : ISC2-PP
				String sSealTmp = "";
				// double dSealSizeMMtmp = 0.0d; // 케이스에 따른 적용 Seal의 사이즈
				if ("Y".equals(StringUtil.get(item.get("BELLOWS_YN")))
						&& !"Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
					if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						sSealTmp = "ISC2-BX";
					} else {
						sSealTmp = "ISC2-BB";
					}
				} else if ("Y".equals(StringUtil.get(item.get("BELLOWS_YN")))
						&& "Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
					if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						sSealTmp = "ISC2-PX";
					} else {
						sSealTmp = "ISC2-PP";
					}
				} else if (!"Y".equals(StringUtil.get(item.get("BELLOWS_YN")))) {
					if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
						sSealTmp = "ISC2-PX";
					} else {
						sSealTmp = "ISC2-PP";
					}
				}

				// dSealSizeTmp = getSealSize(item, "MM", sSealTmp, "", "1", fp); // mm
				// setResultProcList(procRstList, 0, "[Seal Non-API] Seal Size["+sSealTmp+"] :"
				// + dSealSizeTmp + " MM");

				Map<String, Object> c3Param = getC3CheckParam(item, sSealTmp, "", fp);
				c3Param.put("type", item.get("ABC_TYPE")); // type
//				dSealSizeMMtmp = getSealSize(item, "MM", sSealTmp, "", "1", fp);
//				setResultProcList(procRstList, 0, "[Seal Non-API] Seal Size["+sSealTmp+"] :" + dSealSizeMMtmp + " MM");
//				Map<String,Object> c3Param = (HashMap<String,Object>)((HashMap<String,Object>)item).clone(); // item Parma 복사
//				c3Param.put("SEAL_TYPE", sSealTmp);
//				c3Param.put("SEAL_SIZE", dSealSizeMMtmp ); // mm 로 변경
//				c3Param.put("type",item.get("ABC_TYPE")); // type
//				// 속도(ft/s) : 3.14 * Seal Dia(mm) * 0.00328084 * RPM / 60
//				c3Param.put("L_SPD_NOR", 0);
//				c3Param.put("L_SPD_MIN", 0);
//				c3Param.put("L_SPD_MAX", 3.14 * dSealSizeMMtmp  * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속

				// if(isISC2PressChk(item, dSealSizeTmp)) {
				if (isC3OperatingCheck(c3Param, sSealTmp)) {

					setResultProcList(procRstList, 0, "[Seal Non-API] ISC2 Series  선정 가능한 압력 조건 : Y");

					// Bellows seal 적용 요구 조건이 있는지?
					// [작업필요]
					if ("Y".equals(StringUtil.get(item.get("BELLOWS_YN")))) {

						setResultProcList(procRstList, 0, "[Seal Non-API] Bellows seal 적용 : Y");

						// High Corrosive?
						if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
							setResultProcList(procRstList, 0, "[Seal Non-API] High Corrosive : Y");

							// A1 : Plan 23 - ISC2-XP
							// 그 외 ISC2-PX
							// A2 and A3 : ISC2-PP
							// iPIdx ++;
							// 결정된 API에 따라 Seal Type 설정
							setResultList(sealRstList, -1, "__seal_model_cartridge_api_2__", null, fp);

						} else {
							setResultProcList(procRstList, 0, "[Seal Non-API] High Corrosive : N");

							// A1 : Plan 23 - ISC2-XB
							// 그 외 ISC2-BX
							// A2 and A3 : ISC2-BB
							// iPIdx ++;
							// 결정된 API에 따라 Seal Type 설정
							setResultList(sealRstList, -1, "__seal_model_cartridge_api_1__", null, fp);
						}

					} else {

						setResultProcList(procRstList, 0, "[Seal Non-API] Bellows seal 적용 : N");

						// A1 : Plan 23 - ISC2-XP
						// 그 외 ISC2-PX
						// A2 and A3 : ISC2-PP
						// iPIdx ++;
						// 결정된 API에 따라 Seal Type 설정
						setResultList(sealRstList, -1, "__seal_model_cartridge_api_2__", null, fp);
					}

				} else {
					setResultProcList(procRstList, 0, "[Seal Non-API] ISC2 Series  선정 가능한 압력 조건 : N");

					item.put("process_step", "seal_model_tp1"); // 단계 분기정보 Set - Seal Model 1번 분기
				}
			}
		}

		System.out.println("step_seal_type_non_api sealRstList : " + sealRstList);
		System.out.println("step_seal_type_non_api material3RstList : " + material3RstList);

		return "";
	}

	private String step_api_plan(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		System.out.println("step_api_plan start - sealRstList : " + sealRstList);

		// double dSealSizeMM = getSealSize(item, "MM");

		// 추천 Process를 통해 시작된 pidx를 검색
		// int iPIdx = NumberUtil.toInt(item.get("P_IDX"));
//		int iPIdx = 99999;
//		for(Map<String,Object> m : sealRstList) {
//			if (m.get("ADD_INFO") != null) {
//				Map<String,Object> addInfo = (HashMap<String,Object>)m.get("ADD_INFO"); 
//				// End User, FTA 에서 등록된 Seal Type을 제외 한 idx 중 시작값 
//				if (!"1".equals(addInfo.get("R_TYPE"))) {
//					if(iPIdx > NumberUtil.toInt(m.get("P_IDX"))) {
//						iPIdx = NumberUtil.toInt(m.get("P_IDX"));
//					}
//				}
//			}else {
//				if(iPIdx > NumberUtil.toInt(m.get("P_IDX"))) {
//					iPIdx = NumberUtil.toInt(m.get("P_IDX"));
//				}
//			}
//		}

		// --------------------------------------
		// 처리대상 Seal목록 정보를 구성한다.
		// --------------------------------------
		List<Integer> sealListTmp = new ArrayList<Integer>();
		for (Map<String, Object> sm : sealRstList) {

			// int iPIdx = 99999;

			// Plan을 추가할 IDX NO를 확인한다.
			if (sm.get("ADD_INFO") != null) {
				Map<String, Object> addInfo = (HashMap<String, Object>) sm.get("ADD_INFO");
				// End User, FTA 에서 등록된 Seal Type을 제외 한 idx 중 시작값
				// API Process가 끝난 Seal은 SKip
				if ("1".equals(StringUtil.get(addInfo.get("R_TYPE")))
						|| "Y".equals(StringUtil.get(addInfo.get("API_PROCESS_SET")))) {
					continue;
				} else {
					// iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
					sealListTmp.add(NumberUtil.toInt(sm.get("P_IDX")));
				}
			} else {
				// iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
				sealListTmp.add(NumberUtil.toInt(sm.get("P_IDX")));
			}

		}

		int iAdd_GX200_idx = 0; // GX-200 Seal 추가체크를 위한 IDX

		// -----------------------------
		// InBound 처리
		// -----------------------------
		// for(Map<String,Object> sm : sealRstList) {
		for (Integer iPIdx : sealListTmp) {

//			System.out.println("seal m : " + sm);
//			int iPIdx = 99999;
			// Plan을 추가할 IDX MO를 확인한다.
//			if (sm.get("ADD_INFO") != null) {
//				Map<String,Object> addInfo = (HashMap<String,Object>)sm.get("ADD_INFO"); 
//				// End User, FTA 에서 등록된 Seal Type을 제외 한 idx 중 시작값 
//				if ("1".equals(addInfo.get("R_TYPE"))) {
//					continue;
//				}else {
//					iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
//				}
//			}else {
//				iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
//			}

			// 상위로직에서 설정된 Plan 정보가 있을 경우 SKIP
			boolean isPlan = false;
			for (Map<String, Object> p : planRstList) {
				if (NumberUtil.toInt(p.get("P_IDX")) == iPIdx) {
					isPlan = true;
					break;
				}
			}
			if (isPlan)
				continue;

			System.out.println("API 선정 시작");

			// 현재 씰정보의 씰타입
			String _sSealType = ""; // Seal
			String _sSealTypeIn = ""; // inboard Seal
			for (Map<String, Object> m : sealRstList) {
				if (iPIdx == NumberUtil.toInt(m.get("P_IDX"))) {
					_sSealType = "" + m.get("P_VAL");

					if (_sSealType.contains("/")) {
						_sSealTypeIn = _sSealType.split("/")[0];
					} else {
						_sSealTypeIn = _sSealType;
					}
				}
			}

			// ---------------------------------
			// API Plan 선정 시작
			// ---------------------------------
			String sSubStep = "";

			// ------------------------------
			// Solid 체크
			// ------------------------------
			boolean bSolidChk1 = false; // solid 체크 유무
			double dSolidCont = NumberUtil.toDouble(item.get("SOLID_CONT")); // solid 농도 ppm
			if ("".equals(item.get("SOLID_GB")) || "N".equals(item.get("SOLID_GB"))) {
				bSolidChk1 = false;
			} else if ("Y".equals(item.get("SOLID_GB"))) {
				bSolidChk1 = true;
			} else if ("Y1".equals(item.get("SOLID_GB"))) {
				double dSolidSize = 0.d;
				if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_NOR")))
					dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_NOR"));
				if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_MIN")))
					dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_MIN"));
				if (dSolidSize < NumberUtil.toDouble(item.get("SOLID_SIZE_MAX")))
					dSolidSize = NumberUtil.toDouble(item.get("SOLID_SIZE_MAX"));

				// Solid / Particle 체크 조건
				// * 20μm이상, 10000ppm 초과 -> solid size max 체크 시 사이즈 기준 여기적용
				// * 10 - 20μm, 400ppm 초과
				if (((dSolidSize >= 20 || "Y".equals(StringUtil.get(item.get("SOLID_SIZE_MAX_CHK"))))
						&& dSolidCont > 10000) || (dSolidSize >= 10 && dSolidSize < 20 && dSolidCont > 400)) {
					bSolidChk1 = true;
				}
			}

			// Pump Type 별 Hori, Veri 구분
			String sPumpTypeG = getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")), _pumpTypeGroupInfo, null);
			String sEquipmentType = "";
			if ("OH1".equals(sPumpTypeG) || "OH2".equals(sPumpTypeG) || "BB".equals(sPumpTypeG)
					|| "NA".equals(StringUtil.get(item.get("PUMP_TYPE")))) {
				sEquipmentType = "H";
			} else {
				if (!"VS4".equals(sPumpTypeG)) {
					sEquipmentType = "V";
				} else {
					sEquipmentType = "H";
				}
			}

			System.out.println("sEquipmentType : " + sEquipmentType);

			// setResultProcList(procRstList, 0, "[API Plan] Equipment Type : " + sPumpTypeG
			// + " : " + sEquipmentType);

			// inboard Seal API
			if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

				System.out.println("inboard Seal API - arrangement 1");

				// VS4 Pump ?
				if ("VS4".equals(StringUtil.get(item.get("PUMP_TYPE")))) {

					setResultProcList(procRstList, 0, "[API Plan] VS4 Pump  : Y ");

					// Plan 02 + GSL Seal
//					int iPIdx_next = getNextIdx(fp); // 신규 pidx
//					setResultList(sealRstList, iPIdx_next, "GSL", null);
//					setResultListPlan(planRstList, iPIdx_next, "02", null);

					// => Seal 정보를 추가하지 않고 Plan 02, GSL Seal로 변경한다.21.02.09
					for (Map<String, Object> s : sealRstList) {
						if (NumberUtil.toInt(s.get("P_IDX")) == iPIdx) {
							for (Map<String, Object> n : _noteList) {
								
								if(s.get("P_VAL").equals(n.get("SEAL"))){
									n.put("SEAL","GSL");
									n.put("PLAN","02");
								}
							}
							s.put("P_VAL", "GSL");
							
							setResultProcList(procRstList, 0, "[API Plan] Seal 변경 : GSL");
						}
					}
					// Plan 02
					setResultListPlan(planRstList, iPIdx, "02", null, fp);

					setResultProcList(procRstList, 0, "[API Plan] API Plan : 02");

				} else {

					setResultProcList(procRstList, 0, "[API Plan] VS4 Pump  : N ");

					// Solid / Particle
					// * 20μm이상, 10000ppm 초과
					// * 10 - 20μm, 400ppm 초과
					if (bSolidChk1) {

						setResultProcList(procRstList, 0, "[API Plan] Solid / Particle 조건  : Y ");

						// Temperature 조건
						// 1) Water base > 80℃
						// 2) Etc > 100℃

						// -> 조건변경
						// 온도 조건별 전개
						// 1) T ≤ 80℃ : No
						// 2) 80℃ < T ≤ 100℃ w/ Water : Yes
						// 3) 80℃ < T ≤ 100℃ w/o Water : No
						// 4) 100℃ < T : Yes

						boolean bYes = false, bNo = false;
						if (NumberUtil.toDouble(item.get("TEMP_NOR")) <= 80
								&& NumberUtil.toDouble(item.get("TEMP_MIN")) <= 80
								&& NumberUtil.toDouble(item.get("TEMP_MAX")) <= 80) {
							bNo = true;
						}

						if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 100
								|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 100
								|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 100) {
							bYes = true;
						}

						if ((NumberUtil.toDouble(item.get("TEMP_NOR")) > 80
								|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 80
								|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 80)
								&& (NumberUtil.toDouble(item.get("TEMP_NOR")) <= 100
										&& NumberUtil.toDouble(item.get("TEMP_MIN")) <= 100
										&& NumberUtil.toDouble(item.get("TEMP_MAX")) <= 100)) {
							if (isProduct("[WATER-BASE]", saProductGroup, saProduct)) {
								bYes = true;
							} else {
								bNo = true;
							}
						}
						System.out.println(" bYes4 : " + bYes);

						if (bYes) {
							setResultProcList(procRstList, 0,
									"[API Plan] Temperature 조건 (Water base > 80℃ / Etc > 100℃)  : Y ");

							// Plan 32
							setResultListPlan(planRstList, iPIdx, "32", null, fp);
							setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");
						}
						if (bNo) {

							setResultProcList(procRstList, 0,
									"[API Plan] Temperature 조건 (Water base > 80℃ / Etc > 100℃) : N ");

							// VPM확보?
							// a. Psc - V.P ≥ 3.5 bar -> barg로 단위맞춤
							// b. Psc ≥ V.P X 1.3 -> 절대압기준
							// a 또는 b 둘 중 하나 만족

							if (isVPMchk1(item) || isVPMchk2(item)) {
								setResultProcList(procRstList, 0, "[API Plan] VPM확보 : Y ");

								setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");
								setResultListPlan(planRstList, iPIdx, "32", null, fp);

								// 추가 Seal Plan
								if (dSolidCont <= 600000) {

									setResultProcList(procRstList, 0,
											"[API Plan] Plan 11 + Slurry Seal 추가 , Solid 농도 : " + dSolidCont);

									if (dSolidCont != 0) {
										// iPIdx = getNextIdx(fp); // 신규 pidx
										if (dSolidCont <= 100000) {
											int iPidx1 = setResultList(sealRstList, -2, "ISC2-PX", null, fp);
											setResultListPlan(planRstList, iPidx1, "11", null, fp);

											setResultProcList(procRstList, 0, "[API Plan] ISC2-PX Seal 추가");
											setResultProcList(procRstList, 0, "[API Plan] API Plan : 11");

										} else if (dSolidCont <= 200000) {
											int iPidx1 = setResultList(sealRstList, -2, "SLM-6000", null, fp);
											setResultListPlan(planRstList, iPidx1, "11", null, fp);

											setResultProcList(procRstList, 0, "[API Plan] SLM-6000 Seal 추가");
											setResultProcList(procRstList, 0, "[API Plan] API Plan : 11");

										} else if (dSolidCont <= 600000) {
											int iPidx1 = setResultList(sealRstList, -2, "SLC", null, fp);
											setResultListPlan(planRstList, iPidx1, "11", null, fp);

											setResultProcList(procRstList, 0, "[API Plan] SLC Seal 추가");
											setResultProcList(procRstList, 0, "[API Plan] API Plan : 11");
										}
										// setResultListPlan(planRstList, iPIdx, "11", null, fp);
									}

								}

							} else {
								setResultProcList(procRstList, 0, "[API Plan] VPM확보 : N ");

								setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");
								setResultListPlan(planRstList, iPIdx, "32", null, fp);

							}
						}

					} else {

						setResultProcList(procRstList, 0, "[API Plan] Solid / Particle 조건  : N ");

						// Temperature 조건
						// 1) Water base > 80℃
						// 2) Etc > 100℃
						// ------------------- 변경 ------------- 02/18
						// case 1 : 온도 <= 80 : N

						// 80 < 온도 < 100
						// case 2 - water가 포함일경우 : Y
						// case 3 - water가 포함되지 않은 경우 : Y/N 둘다 적용

						// case 2 : 100 > 온도 : Y

						boolean bYes = false, bNo = false;
						if (NumberUtil.toDouble(item.get("TEMP_NOR")) <= 80
								&& NumberUtil.toDouble(item.get("TEMP_MIN")) <= 80
								&& NumberUtil.toDouble(item.get("TEMP_MAX")) <= 80) {
							bNo = true;
						} else if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 100
								|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 100
								|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 100) {
							bYes = true;
						} else if ((NumberUtil.toDouble(item.get("TEMP_NOR")) > 80
								|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 80
								|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 80)
								&& (NumberUtil.toDouble(item.get("TEMP_NOR")) <= 100
										&& NumberUtil.toDouble(item.get("TEMP_MIN")) <= 100
										&& NumberUtil.toDouble(item.get("TEMP_MAX")) <= 100)) {
							if (isProduct("[WATER-BASE]", saProductGroup, saProduct)) {
								bYes = true;
							} else {
								bYes = true;
								bNo = true;
							}
						}

						if (bYes) { // Y case
							setResultProcList(procRstList, 0,
									"[API Plan] Temperature 조건 (Water base > 80℃ / Etc > 100℃)  : Y ");

							sSubStep = "plan_sub_1";

							/*
							 * //High Corrosive? if (
							 * "Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
							 * setResultProcList(procRstList, 0, "[API Plan] High Corrosive  : Y");
							 * 
							 * setResultListPlan(planRstList, iPIdx, "32", null, fp);
							 * setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");
							 * 
							 * }else { setResultProcList(procRstList, 0, "[API Plan] High Corrosive  : N");
							 * 
							 * //Cooling으로 인한 Trouble 우려가 있는지..? //ex) 온도 하락 시 굳는 유체 or Pour point 문제 등.
							 * //if ("Y".equals(StringUtil.get(item.get("RESI_HRDN_GB"))) || //
							 * "Y".equals(StringUtil.get(item.get("OIL_HRDN_YN")))){
							 * if("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { //
							 * Cooling으로 굳는성질 setResultProcList(procRstList, 0,
							 * "[API Plan] Cooling으로 인한 Trouble 우려  : Y,  API Plan : 32, 02/62");
							 * 
							 * //case : Plan 32 //case : Plan 02 + 62 setResultListPlan(planRstList, iPIdx,
							 * "32", null, fp); setResultListPlan(planRstList, iPIdx, "02/62", null, fp);
							 * }else {
							 * 
							 * setResultProcList(procRstList, 0,
							 * "[API Plan] Cooling으로 인한 Trouble 우려  : N, API Plan : 23 ");
							 * 
							 * //Plan 23 setResultListPlan(planRstList, iPIdx, "23", null, fp); }
							 * 
							 * bMulti = true; }
							 */

						}

						if (bNo) { // N Case
							setResultProcList(procRstList, 0,
									"[API Plan] Temperature 조건 (Water base > 80℃ / Etc > 100℃) : N ");

							// VPM확보?
							// a. Psc - V.P ≥ 3.5 bar -> barg로 단위맞춤
							// b. Psc ≥ V.P X 1.3 -> 절대압기준
							// a 또는 b 둘 중 하나 만족

							if (isVPMchk1(item) || isVPMchk2(item)) {
								setResultProcList(procRstList, 0, "[API Plan] VPM확보 : Y ");

								// Cooling으로 인한 Trouble 우려가 있는지..?
								// ex) 온도 하락 시 굳는 유체 or Pour point 문제 등.
								if ("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { // Cooling으로 굳는성질
									setResultProcList(procRstList, 0, "[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) : Y");

									setResultProcList(procRstList, 0, "[API Plan] API Plan : 02");
									setResultListPlan(planRstList, iPIdx, "02", null, fp);
								} else {
									setResultProcList(procRstList, 0, "[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) : N");

									// Pump Type Horizontal - Plan 11
									// Vertical - Plan 13

									// Horizontal - OH1,2, BB
									// Vertical - OH3~6, VS (except VS4)
									if ("H".equals(sEquipmentType)) {
										setResultProcList(procRstList, 0, "[API Plan] Pump Type : Horizontal");
										setResultProcList(procRstList, 0, "[API Plan] API Plan : 11");
										// if(bMulti) {
										// setPlanResultList(planRstList, -2, "11", null, fp);
										// }else {
										setResultListPlan(planRstList, iPIdx, "11", null, fp);
										// }
										// setResultListPlan(planRstList, iPIdx, "11", null, fp);
									} else {
										setResultProcList(procRstList, 0, "[API Plan] Pump Type : Vertical");
										setResultProcList(procRstList, 0, "[API Plan] API Plan : 13");
										// if(bMulti) {
										// setPlanResultList(planRstList, -2, "13", null, fp);
										// }else {
										setResultListPlan(planRstList, iPIdx, "13", null, fp);
										// }
										// setResultListPlan(planRstList, iPIdx, "13", null, fp);
									}
								}

							} else {
								setResultProcList(procRstList, 0, "[API Plan] VPM확보 : N ");

								if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 60
										|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 60
										|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 60) {

									setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : Y ");
									sSubStep = "plan_sub_1";

								} else {
									setResultProcList(procRstList, 0, "[Seal API] Temp > 60℃ : N ");

									// Arrangement2로 변경 후 Seal 선정부터 재처리
									item.put("ARRANGEMENT", "2");

									// -----------------------------------
									// 추가프로세스를 설정한다.
									// -----------------------------------
									item.put("__ADD_PROCESS_4", "N");
									// Y case도 발생한 경우는 이후 처리에서 프로세스로 설정된 Seal을 삭제하지 않는다.
									if (bYes) {
										item.put("__ADD_PROCESS_4_OPT", "Y");
									}
								}

							}

						}

						/*
						 * if ( (isProduct("WATER",saProductGroup,saProduct) &&
						 * (NumberUtil.toDouble(item.get("TEMP_NOR")) > 80 ||
						 * NumberUtil.toDouble(item.get("TEMP_MIN")) > 80 ||
						 * NumberUtil.toDouble(item.get("TEMP_MAX")) > 80 )) ||
						 * (NumberUtil.toDouble(item.get("TEMP_NOR")) > 100 ||
						 * NumberUtil.toDouble(item.get("TEMP_MIN")) > 100 ||
						 * NumberUtil.toDouble(item.get("TEMP_MAX")) > 100 ) ) {
						 * 
						 * setResultProcList(procRstList, 0,
						 * "[API Plan] Temperature 조건 (Water base > 80℃ / Etc > 100℃)  : Y ");
						 * 
						 * //Cooling으로 인한 Trouble 우려가 있는지..? //ex) 온도 하락 시 굳는 유체 or Pour point 문제 등. if
						 * ("Y".equals(StringUtil.get(item.get("RESI_HRDN_GB"))) ||
						 * "Y".equals(StringUtil.get(item.get("OIL_HRDN_YN")))){
						 * 
						 * setResultProcList(procRstList, 0,
						 * "[API Plan] Cooling으로 인한 Trouble 우려  : Y,  API Plan : 32, 02/62");
						 * 
						 * //case : Plan 32 //case : Plan 02 + 62 setResultListPlan(planRstList, iPIdx,
						 * "32", null, fp); setResultListPlan(planRstList, iPIdx, "02/62", null, fp);
						 * }else {
						 * 
						 * setResultProcList(procRstList, 0,
						 * "[API Plan] Cooling으로 인한 Trouble 우려  : N, API Plan : 23 ");
						 * 
						 * //Plan 23 setResultListPlan(planRstList, iPIdx, "23", null, fp); }
						 * 
						 * }else {
						 * 
						 * setResultProcList(procRstList, 0,
						 * "[API Plan] Temperature 조건 (Water base > 80℃ / Etc > 100℃)  : N ");
						 * 
						 * //Pump Type Horizontal - Plan 11 //Vertical - Plan 13
						 * 
						 * //Horizontal - OH1,2, BB //Vertical - OH3~6, VS (except VS4)
						 * if("H".equals(sEquipmentType)) {
						 * 
						 * setResultProcList(procRstList, 0,
						 * "[API Plan] Pump Type Horizontal : API Plan : " + "11");
						 * 
						 * setResultListPlan(planRstList, iPIdx, "11", null, fp); }else {
						 * 
						 * setResultProcList(procRstList, 0,
						 * "[API Plan] Pump Type Vertical : API Plan : " + "13");
						 * 
						 * setResultList(planRstList, iPIdx, "13", null, fp); } }
						 */
					}

				}

			}

			if ("plan_sub_1".equals(sSubStep)) {

				// High Corrosive?
				if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
					setResultProcList(procRstList, 0, "[API Plan] High Corrosive : Y");

					setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");
					setResultListPlan(planRstList, iPIdx, "32", null, fp);

				} else {
					setResultProcList(procRstList, 0, "[API Plan] High Corrosive : N");

					// Cooling으로 인한 Trouble 우려가 있는지..?
					// ex) 온도 하락 시 굳는 유체 or Pour point 문제 등.
					if ("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { // Cooling으로 굳는성질
						setResultProcList(procRstList, 0, "[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) : Y");

						// VPM확보?
						// a. Psc - V.P ≥ 3.5 bar -> barg로 단위맞춤
						// b. Psc ≥ V.P X 1.3 -> 절대압기준
						// a 또는 b 둘 중 하나 만족

						if (isVPMchk1(item) || isVPMchk2(item)) {
							setResultProcList(procRstList, 0, "[API Plan] VPM확보 : Y ");

							setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");
							setResultProcList(procRstList, 0, "[API Plan] API Plan : 02/62");
							// case : Plan 32
							// case : Plan 02 + 62
							setResultListPlan(planRstList, iPIdx, "32", null, fp);
							setResultListPlan(planRstList, iPIdx, "02/62", null, fp);
						} else {
							setResultProcList(procRstList, 0, "[API Plan] VPM확보 : N ");

							// case : Plan 32
							setResultListPlan(planRstList, iPIdx, "32", null, fp);
						}

					} else {

						setResultProcList(procRstList, 0, "[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) : N");

						setResultProcList(procRstList, 0, "[API Plan] API Plan : 23");
						// Plan 23
						setResultListPlan(planRstList, iPIdx, "23", null, fp);
					}

				}

			}

			// ---------------------------------
			// inboard Seal API - arrangement 2
			// ---------------------------------
			if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

				System.out.println("inboard Seal API - arrangement 2");

				setResultProcList(procRstList, 0, "[API Plan] Arrangement 2 inboard ");

				// VS4 Pump ?
				if ("VS4".equals(StringUtil.get(item.get("PUMP_TYPE")))) {

					setResultProcList(procRstList, 0, "[API Plan] VS4 Pump  : Y ");

					item.put("ARRANGEMENT", "3");
				} else {

					setResultProcList(procRstList, 0, "[API Plan] VS4 Pump  : N ");

					// Solid / Particle
					// * 20μm이상, 10000ppm
					// * 10 - 20μm, 400ppm
					if (bSolidChk1) {

						setResultProcList(procRstList, 0, "[API Plan] Solid / Particle  : Y ");

						// case 1 : Plan 32
						// case 2 : ARRANGEMENT 3
						setResultListPlan(planRstList, iPIdx, "32", null, fp);
						setResultProcList(procRstList, 0, "[API Plan] Case 1 : API Plan : 32");

						// item.put("ARRANGEMENT","3");
						item.put("__ADD_PROCESS_0", "N"); // 추천 추가 처리
						setResultProcList(procRstList, 0, "[API Plan] Case 2 : Arrangement3으로 추천");

					} else {

						// -------------------------------------------------------------------------
						// 이구간에서 온도체크에 따른 분기를 탈수 있게 로직 분기구간을 포함시킴
						// -------------------------------------------------------------------------
						boolean bTempProcessYes = false;

						setResultProcList(procRstList, 0, "[API Plan] Solid / Particle  : N ");

						// VPM 확보?
						// a. Psc - V.P ≥ 3.5 bar
						// b. Psc ≥ V.P X 1.3 (절대압)
						// a 또는 b 둘 중 하나 만족
						if (isVPMchk1(item) || isVPMchk2(item)) {

							setResultProcList(procRstList, 0, "[API Plan] VPM 확보  : Y ");

							// 온도 조건별 전개
							// 1) T ≤ 60℃ : No
							// 2) 60℃ < T w/ Flashing H/C : Yes
							// 3) 60℃ < T ≤ 80℃ w/o Flashing H/C : No
							// 4) 80℃ < T ≤ 100℃ w/ Water : Yes
							// 5) 80℃ < T ≤ 100℃ w/o Water : Yes & No
							// 6) 100℃ < T : Yes

							boolean bYes = false, bNo = false;
							if (NumberUtil.toDouble(item.get("TEMP_NOR")) <= 60
									&& NumberUtil.toDouble(item.get("TEMP_MIN")) <= 60
									&& NumberUtil.toDouble(item.get("TEMP_MAX")) <= 60) {
								bNo = true;
							} else if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 100
									|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 100
									|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 100) {
								bYes = true;
							} else if (isProduct("HYDROCARBON", saProductGroup, saProduct)
									&& (NumberUtil.toDouble(item.get("VAP_PRES_NOR")) > 1
											|| NumberUtil.toDouble(item.get("VAP_PRES_MIN")) > 1
											|| NumberUtil.toDouble(item.get("VAP_PRES_MAX")) > 1)
									&& (NumberUtil.toDouble(item.get("TEMP_NOR")) > 60
											|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 60
											|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 60)) {
								bYes = true;
							} else if ((NumberUtil.toDouble(item.get("TEMP_NOR")) > 60
									|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 60
									|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 60)
									&& (NumberUtil.toDouble(item.get("TEMP_NOR")) <= 80
											&& NumberUtil.toDouble(item.get("TEMP_MIN")) <= 80
											&& NumberUtil.toDouble(item.get("TEMP_MAX")) <= 80)
									&& (!isProduct("HYDROCARBON", saProductGroup, saProduct)
											|| (NumberUtil.toDouble(item.get("VAP_PRES_NOR")) <= 1
													&& NumberUtil.toDouble(item.get("VAP_PRES_MIN")) <= 1
													&& NumberUtil.toDouble(item.get("VAP_PRES_MAX")) <= 1))) {
								bNo = true;
							} else {
								if (isProduct("[WATER-BASE]", saProductGroup, saProduct)) {
									bYes = true;
								} else {
									bYes = true;
									bNo = true;
								}
							}

							if (bYes) {
								bTempProcessYes = true;
							}

							// 온도조건이 No일때
							if (bNo) {
								setResultProcList(procRstList, 0, "[API Plan] Temperature 조건 : N");

								// 온도하락시 굳는 유체 or Pour point 문제여부
								if ("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { // Cooling으로 굳는성질
									setResultProcList(procRstList, 0, "[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) : Y");

									setResultListPlan(planRstList, iPIdx, "02", null, fp);
									setResultProcList(procRstList, 0, "[API Plan] Horizontal, API Plan : 02");

								} else {
									setResultProcList(procRstList, 0, "[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) : N");

									// Pump Type Horizontal - Plan 11
									// Vertical - Plan 13

									// Horizontal - OH1,2, BB
									// Vertical - OH3~6, VS (except VS4)
									if ("H".equals(sEquipmentType)) {
										setResultListPlan(planRstList, iPIdx, "11", null, fp);
										setResultProcList(procRstList, 0, "[API Plan] Horizontal, API Plan : 11");
									} else {
										setResultListPlan(planRstList, iPIdx, "13", null, fp);
										setResultProcList(procRstList, 0, "[API Plan] Vertical, API Plan : 13");
									}
								}

							}

						} else {
							setResultProcList(procRstList, 0, "[API Plan] VPM 확보  : N ");

							// Temp > 60℃
							if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 60
									|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 60
									|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 60) {
								setResultProcList(procRstList, 0, "[API Plan] Temp > 60℃  : Y ");

								// 온도체크에 따른 Yes 분기로직을 처리함.
								bTempProcessYes = true;

							} else {
								setResultProcList(procRstList, 0, "[API Plan] Temp > 60℃  : N ");

								// QBQLZ?
								if ("QBQLZ".equals(_sSealTypeIn)) {
									setResultProcList(procRstList, 0, "[API Plan] QBQLZ Seal  : Y ");

									// Pump Type Horizontal - Plan 11
									// Vertical - Plan 13
									// Horizontal - OH1,2, BB
									// Vertical - OH3~6, VS (except VS4)
									if ("H".equals(sEquipmentType)) {
										setResultListPlan(planRstList, iPIdx, "11", null, fp);
										setResultProcList(procRstList, 0, "[API Plan] Horizontal, API Plan : 11");
									} else {
										setResultListPlan(planRstList, iPIdx, "13", null, fp);
										setResultProcList(procRstList, 0, "[API Plan] Vertical, API Plan : 13");
									}

								} else {
									setResultProcList(procRstList, 0, "[API Plan] QBQLZ Seal  : N ");

									setResultProcList(procRstList, 0, "[API Plan] Arrangement3으로 변경");
									item.put("__ADD_PROCESS_3", "N"); // 추천 추가 처리
									// item.put("ARRANGEMENT","3"); //Arrangement3으로 변경 후 API Plan 체크?

								}

							}

						}

						// ---------------------------------------
						// Temperature 조건이 YES일 경우
						// ---------------------------------------
						if (bTempProcessYes) {

							setResultProcList(procRstList, 0, "[API Plan] Temperature 조건 : Y");

							// Cooling으로 인한 Trouble 우려가 있는지..?
							// ex) 온도 하락 시 굳는 유체 or Pour point 문제 등.
							// if ("Y".equals(StringUtil.get(item.get("RESI_HRDN_GB"))) ||
							// "Y".equals(StringUtil.get(item.get("OIL_HRDN_YN")))){
							if ("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { // Cooling으로 굳는성질

								setResultProcList(procRstList, 0, "[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) : Y");

								// case : Plan 32
								// Arrangement 3 변경
								setResultListPlan(planRstList, iPIdx, "32", null, fp);
								setResultProcList(procRstList, 0, "[API Plan] Case 1: API Plan : 32");

								// item.put("ARRANGEMENT","3");
								item.put("__ADD_PROCESS_0", "N"); // 추천 추가 처리
								setResultProcList(procRstList, 0, "[API Plan] Case 2 : Arrangement3으로 추천");

							} else {

								setResultProcList(procRstList, 0, "[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) : N");

								// ISC2 Series ?
								// (ISC2-PP, ISC2-BB ISC2-682PP, ISC2-682BB)
								if (isISC2SeriesChk(sealRstList, iPIdx)) {

									// Plan 21
									setResultListPlan(planRstList, iPIdx, "21", null, fp);
									setResultProcList(procRstList, 0, "[API Plan] ISC2  Series = Y, API Plan : 21");
								} else {
									// Plan 23
									setResultListPlan(planRstList, iPIdx, "23", null, fp);
									setResultProcList(procRstList, 0, "[API Plan] ISC2  Series = N, API Plan : 23");
								}
							}

						}

//						//Temperature 조건
//						//1) Water base > 80℃
//						//2) Flashing H.C > 60℃   - Flashing 체크 : vap pres 1 bar 초과
//						//3) Etc > 100℃
//						if (
//							(
//							 isProduct("[WATER-BASE]",saProductGroup,saProduct) &&
//							 (NumberUtil.toDouble(item.get("TEMP_NOR")) > 80 ||
//								NumberUtil.toDouble(item.get("TEMP_MIN")) > 80 ||
//								NumberUtil.toDouble(item.get("TEMP_MAX")) > 80 )
//							) || (
//							 isProduct("HYDROCARBON",saProductGroup,saProduct) &&
//							 (NumberUtil.toDouble(item.get("VAP_PRES_NOR")) > 1 ||
//							  NumberUtil.toDouble(item.get("VAP_PRES_MIN")) > 1 ||
//							  NumberUtil.toDouble(item.get("VAP_PRES_MAX")) > 1 ) &&
//							 (NumberUtil.toDouble(item.get("TEMP_NOR")) > 60 ||
//							  NumberUtil.toDouble(item.get("TEMP_MIN")) > 60 ||
//							  NumberUtil.toDouble(item.get("TEMP_MAX")) > 60 )
//							) ||(
//							 NumberUtil.toDouble(item.get("TEMP_NOR")) > 100 ||
//							 NumberUtil.toDouble(item.get("TEMP_MIN")) > 100 ||
//							 NumberUtil.toDouble(item.get("TEMP_MAX")) > 100 
//							)) {
//							
//							setResultProcList(procRstList, 0, "[API Plan] Temperature 조건 : Y");
//							
//							//Cooling으로 인한 Trouble 우려가 있는지..?
//							//ex) 온도 하락 시 굳는 유체 or Pour point 문제 등.
//							//if ("Y".equals(StringUtil.get(item.get("RESI_HRDN_GB"))) ||  
//							//		"Y".equals(StringUtil.get(item.get("OIL_HRDN_YN")))){
//							if("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { // Cooling으로 굳는성질 	
//								//case : Plan 32 
//								//Arrangement 3 변경
//								setResultList(planRstList, iPIdx, "32", null, fp);
//								setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");
//								item.put("ARRANGEMENT","3");
//								
//								setResultProcList(procRstList, 0, "[API Plan] Cooling으로 인한 Trouble 우려 : Y");
//								setResultProcList(procRstList, 0, "[API Plan] Arrangement : 3");
//							}else {
//								
//								setResultProcList(procRstList, 0, "[API Plan] Cooling으로 인한 Trouble 우려 : N");
//								
//								//ISC2  Series ?
//								//(ISC2-PP, ISC2-BB ISC2-682PP, ISC2-682BB)
//								if(isISC2SeriesChk(sealRstList, iPIdx)) {
//								
//									//Plan 21 
//									setResultList(planRstList, iPIdx, "21", null, fp);
//									setResultProcList(procRstList, 0, "[API Plan] ISC2  Series = Y, API Plan : 21");
//								}else {
//									//Plan 23
//									setResultList(planRstList, iPIdx, "23", null, fp);
//									setResultProcList(procRstList, 0, "[API Plan] ISC2  Series = N, API Plan : 23");
//								}
//							}
//							
//						}else {
//							setResultProcList(procRstList, 0, "[API Plan] Temperature 조건 : N");
//							
//							//Pump Type Horizontal - Plan 11
//			                // Vertical - Plan 13
//
//			                // Horizontal - OH1,2, BB
//			                // Vertical - OH3~6, VS (except VS4)
//							if("H".equals(sEquipmentType)) {
//								setResultListPlan(planRstList, iPIdx, "11", null, fp);
//								setResultProcList(procRstList, 0, "[API Plan] Horizontal, API Plan : 11");
//							}else {
//								setResultListPlan(planRstList, iPIdx, "13", null, fp);
//								setResultProcList(procRstList, 0, "[API Plan] Vertical, API Plan : 13");
//							}
//						}
					}
				}
			}

			// ---------------------------------
			// inboard Seal API - arrangement 3
			// ---------------------------------
			if ("3".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

				System.out.println("inboard Seal API - arrangement 3");

				setResultProcList(procRstList, 0, "[API Plan] Arrangement 3 inboard ");

				if ("VS4".equals(StringUtil.get(item.get("PUMP_TYPE")))) {

					// Without Inboard Plan - 빈값으로 Plan정보 생성
					setResultListPlan(planRstList, iPIdx, "[BLANK]", null, fp);

					// setResultNoteList(noteRstList, iPIdx, "Without Inboard Plan");
					setResultProcList(procRstList, 0, "Without Inboard Plan");

				} else {
					if (bSolidChk1) {

						setResultProcList(procRstList, 0, "[API Plan] Solid Check : Y");

						// [Case 1 처리]
						// case 1 : Plan 32

						setResultListPlan(planRstList, iPIdx, "32", null, fp);
						setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");

						// [Case 2 처리]
						// case 2 : Plan 11 + Slurry Seal
						// ~10% : ISC2-PP
						// ~60% : SLM-6200
						// * 74 PLAN 요구가 있고 또는 3NC 요구가 있을 경우 CASE 2 무시
						if (isPlan("74", StringUtil.get(item.get("API_PLAN_DIR")))
								|| ("3NC".equals(StringUtil.get(item.get("SEAL_CONFIG"))))) {
							// skip
						} else {
							if (dSolidCont <= 600000) {
								// iPIdx = getNextIdx(fp); // 신규 pidx
								if (dSolidCont <= 100000) {
									int iPidx1 = setResultList(sealRstList, -2, "ISC2-PP", null, fp);
									setResultListPlan(planRstList, iPidx1, "11", null, fp);

									setResultProcList(procRstList, 0, "[API Plan] ISC2-PP 추가");
									setResultProcList(procRstList, 0, "[API Plan] API Plan : 11");
								} else if (dSolidCont <= 600000) {

									System.out.println("dSolidCont <= 600000 Y");

									int iPidx1 = setResultList(sealRstList, -2, "SLM-6200", null, fp);
									setResultListPlan(planRstList, iPidx1, "11", null, fp);

									setResultProcList(procRstList, 0, "[API Plan] SLM-6200 추가");
									setResultProcList(procRstList, 0, "[API Plan] API Plan : 11");

									System.out.println("dSolidCont <= 600000 end");

								}
								// setResultListPlan(planRstList, iPIdx, "11", null, fp);
							}
						}

					} else {

						setResultProcList(procRstList, 0, "[API Plan] Solid Check : N");

						// Seal Configuration
						// 3CW-FB ?
						if ("3CW-FB".equals(StringUtil.get(item.get("SEAL_CONFIG")))
								|| isSealConfig(item, _sSealType, "3CW-FB")) {

							setResultProcList(procRstList, 0, "[API Plan] 3CW-FB : Y");

							// Temperature 조건
							// > 100℃
							if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 100
									|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 100
									|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 100) {

								// High Corrosive?
								if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
									setResultProcList(procRstList, 0, "[API Plan] High Corrosive : Y");

									// Plan 32
									setResultListPlan(planRstList, iPIdx, "32", null, fp);
									setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");

								} else {
									setResultProcList(procRstList, 0, "[API Plan] High Corrosive : N");

									// Cooling으로 인한 Trouble 우려가 있는지..?
									// ex) 온도 하락 시 굳는 유체 or Pour point 문제 등.
									// if ("Y".equals(StringUtil.get(item.get("RESI_HRDN_GB"))) ||
									// "Y".equals(StringUtil.get(item.get("OIL_HRDN_YN")))){
									if ("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))) { // Cooling으로 굳는성질
										setResultProcList(procRstList, 0, "[API Plan] 굳는유체 : Y");

										// Plan 32 select
										setResultListPlan(planRstList, iPIdx, "32", null, fp);
										setResultProcList(procRstList, 0, "[API Plan] API Plan : 32");

										// Plan 02
										setResultListPlan(planRstList, iPIdx, "02", null, fp);
										setResultProcList(procRstList, 0, "[API Plan] API Plan : 02");

										// "Double Cooler 적용 검토 필요" Comment 함께 띄울 것.
										Map<String,Object> addNote = new HashMap<String,Object>();
										addNote.put("PLAN", "02");
										setResultNoteList(noteRstList, iPIdx, "[Plan 02] Double  Cooler 적용 검토 필요", "p", addNote);
									} else {

										setResultProcList(procRstList, 0, "[API Plan] 굳는유체 : N");

										// ISC2 Series ?
										// (ISC2-PP, ISC2-BB ISC2-682PP, ISC2-682BB)
										if (isISC2SeriesChk(sealRstList, iPIdx)) {
											// Plan 21
											setResultListPlan(planRstList, iPIdx, "21", null, fp);

											setResultProcList(procRstList, 0,
													"[API Plan] ISC2  Series = Y , API Plan : 21");
										} else {
											// Plan 23
											setResultListPlan(planRstList, iPIdx, "23", null, fp);

											setResultProcList(procRstList, 0,
													"[API Plan] ISC2  Series = N , API Plan : 23");
										}
									}
								}

							} else {

								// 온도하락시 굳는 유체 or Pour point 문제여부
								// 변경(21.04.05) : Or 74 Plan 적용여부
								if ("Y".equals(StringUtil.get(item.get("PC_COOL_TROUBLE_CHK")))
										|| isPlan("74", StringUtil.get(item.get("API_PLAN_DIR")))) {
									setResultProcList(procRstList, 0,
											"[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) or 74 Plan 요구 : Y");

									// Without Inboard Plan - 빈값으로 Plan정보 생성
									setResultListPlan(planRstList, iPIdx, "[BLANK]", null, fp);

									// setResultNoteList(noteRstList, 0, "Without Inboard Plan");
									setResultProcList(procRstList, 0, "Without Inboard Plan");

								} else {
									setResultProcList(procRstList, 0,
											"[API Plan] 굳는 성질 (Pour Point, 냉각, 상온 등) or 74 Plan 요구 : N");

									// Pump Type Horizontal - Plan 11
									// Vertical - Plan 13

									// Horizontal - OH1,2, BB
									// Vertical - OH3~6, VS (except VS4)
									if ("H".equals(sEquipmentType)) {
										setResultListPlan(planRstList, iPIdx, "11", null, fp);
										setResultProcList(procRstList, 0, "[API Plan] Horizontal, API Plan : 11");
									} else {
										setResultListPlan(planRstList, iPIdx, "13", null, fp);
										setResultProcList(procRstList, 0, "[API Plan] Vertical, API Plan : 13");
									}
								}

							}

						} else {
							setResultProcList(procRstList, 0, "[API Plan] 3CW-FB : N");

							// Without Inboard Plan - 빈값으로 Plan정보 생성
							setResultListPlan(planRstList, iPIdx, "[BLANK]", null, fp);

							// setResultNoteList(noteRstList, 0, "Without Inboard Plan");
							setResultProcList(procRstList, 0, "Without Inboard Plan");
						}

					}
				}
			}

		}

		// -----------------------------
		// OutBoard 처리
		// -----------------------------
		sealListTmp = new ArrayList<Integer>();
		for (Map<String, Object> sm : sealRstList) {

			// int iPIdx = 99999;

			// Plan을 추가할 IDX MO를 확인한다.
			if (sm.get("ADD_INFO") != null) {
				Map<String, Object> addInfo = (HashMap<String, Object>) sm.get("ADD_INFO");
				// End User, FTA 에서 등록된 Seal Type을 제외 한 idx 중 시작값
				if ("1".equals(StringUtil.get(addInfo.get("R_TYPE")))
						|| "Y".equals(StringUtil.get(addInfo.get("API_PROCESS_SET")))) {
					continue;
				} else {
					// iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
					sealListTmp.add(NumberUtil.toInt(sm.get("P_IDX")));
				}
			} else {
				// iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
				sealListTmp.add(NumberUtil.toInt(sm.get("P_IDX")));
			}

		}

		// for(Map<String,Object> sm : sealRstList) {
		for (Integer iPIdx : sealListTmp) {

			// ---------------------------------
			// Outboard Plan - Arrangement2
			// ---------------------------------
			if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

				setResultProcList(procRstList, 0, "[API Plan] Arrangement 2 Outboard ");

				// 2CW-CS Configuration
				// 사용자가 configuration으로 입력한 경우 Plan을 입력된값으로 configuration이 정해진 경우
				// Plan 75, 76에 대한 요구? <- ?
				if (("2CW-CS".equals(StringUtil.get(item.get("SEAL_CONFIG")))
						|| "2CW-CS".equals(StringUtil.get(item.get("API_PLAN_DIR_CONFIG"))))
						|| isPlan("75", StringUtil.get(item.get("API_PLAN_DIR")))
						|| isPlan("76", StringUtil.get(item.get("API_PLAN_DIR")))) {

					setResultProcList(procRstList, 0, "[API Plan] Plan 75, 76에 대한 요구 => Y");

					// 2CW-CS , 72 Plan 및 이후 적용Plan이 75,76을 사용하기 때문에 Gas 유체에 대한 처리를 위해 GSL Seal로 교체
					// 처리
					setOutboardSealType(sealRstList, iPIdx, "GSL");

					if (NumberUtil.toDouble(item.get("TEMP_NOR")) < 0 || NumberUtil.toDouble(item.get("TEMP_MIN")) < 0
							|| NumberUtil.toDouble(item.get("TEMP_MAX")) < 0
							|| isPlan("72", StringUtil.get(item.get("API_PLAN_DIR")))) {

						// Plan 72 add
						// setResultListPlan(planRstList, iPIdx, "72", null, fp);
						setResultProcList(procRstList, 0,
								"[API Plan] Temperature < 0 or Plan 72에 대한 요구 => Y, API Plan Add : 72");

						// Vapor pressure > ATM
						// ATM:1.013
						if (NumberUtil.toDouble(item.get("VAP_PRES_NOR")) > 1
								|| NumberUtil.toDouble(item.get("VAP_PRES_MIN")) > 1
								|| NumberUtil.toDouble(item.get("VAP_PRES_MAX")) > 1) {

							// Plan 76 select
							// setApiPlanOutboard(planRstList, iPIdx, "76", fp);
							setApiPlanOutboard(planRstList, iPIdx, "72/76", fp); // 앞단계에서 72가 더해지는 기준에 의해
							setResultProcList(procRstList, 0, "[API Plan] Vapor pressure > ATM => Y, API Plan : 76");
						} else {
							// Plan 75 select
							setApiPlanOutboard(planRstList, iPIdx, "75", fp);
							setApiPlanOutboard(planRstList, iPIdx, "72/75", fp); // 앞단계에서 72가 더해지는 기준에 의해
							setResultProcList(procRstList, 0, "[API Plan] Vapor pressure > ATM => N, API Plan : 75");
						}

					} else {

						setResultProcList(procRstList, 0, "[API Plan] Temperature < 0 or Plan 72에 대한 요구 => N");

						// Vapor pressure > ATM
						// ATM:1.013
						if (NumberUtil.toDouble(item.get("VAP_PRES_NOR")) > 1.013
								|| NumberUtil.toDouble(item.get("VAP_PRES_MIN")) > 1.013
								|| NumberUtil.toDouble(item.get("VAP_PRES_MAX")) > 1.013) {
							// Plan 76 select
							setApiPlanOutboard(planRstList, iPIdx, "76", fp);

							setResultProcList(procRstList, 0, "[API Plan] Vapor pressure > ATM => Y, API Plan : 76");
						} else {
							// Plan 75 select
							setApiPlanOutboard(planRstList, iPIdx, "75", fp);

							setResultProcList(procRstList, 0, "[API Plan] Vapor pressure > ATM => N, API Plan : 75");
						}

					}
				} else {
					// Plan 52 select
					setApiPlanOutboard(planRstList, iPIdx, "52", fp);

					setResultProcList(procRstList, 0, "[API Plan] Plan 75, 76에 대한 요구 => N, API Plan : 52");
				}
			}

			// ---------------------------------
			// Outboard Plan - Arrangement3
			// ---------------------------------
			if ("3".equals(StringUtil.get(item.get("ARRANGEMENT")))) {

				setResultProcList(procRstList, 0, "[API Plan] Arrangement 3 Outboard ");

				// Plan 74 적용 요구?
				if (isPlan("74", StringUtil.get(item.get("API_PLAN_DIR")))) {
					// GF-200 or GX-200
					// Bellows = Y : GX-200
					// 그 외 : GF-200 , GX-200 모두 추천 - 순서대로 추천

					if ("Y".equals(StringUtil.get(item.get("BELLOWS_YN")))) { // Bellows 요구일 경우

						// BELLOWS_YN : Y 일 경우는 GX-200 만 추천
						// 현재프로세스로 설정된 Seal 정보를 변경
						for (Map<String, Object> m : sealRstList) {
							if (iPIdx == NumberUtil.toInt(m.get("P_IDX"))) {
								m.put("P_VAL", "GX-200");
							}
						}

					} else {

						// 현재프로세스로 설정된 Seal 정보를 변경
						for (Map<String, Object> m : sealRstList) {
							if (iPIdx == NumberUtil.toInt(m.get("P_IDX"))) {
								m.put("P_VAL", "GF-200");
							}
						}

						// GX-200 추가
						// GX-200 Seal 추가를 위한 Idx
						iAdd_GX200_idx = iPIdx;

					}

					/*
					 * String sChgSeal = ""; if("Y".equals(StringUtil.get(item.get("BELLOWS_YN"))))
					 * { // Bellows 요구일 경우 sChgSeal="GX-200"; }else{ if
					 * ("A".equals(StringUtil.get(item.get("ABC_TYPE")))){ // Type 결과가 A일 경우
					 * sChgSeal="GF-200"; }else if ("B".equals(StringUtil.get(item.get("ABC_TYPE")))
					 * || "C".equals(StringUtil.get(item.get("ABC_TYPE")))){ sChgSeal="GX-200"; } }
					 * 
					 * if (!"".equals(sChgSeal)) { //현재프로세스로 설정된 Seal 정보를 변경 for(Map<String,Object>
					 * m : sealRstList) { if(iPIdx == NumberUtil.toInt(m.get("P_IDX"))) {
					 * m.put("P_VAL",sChgSeal); } } }else { //현재프로세스로 설정된 Seal 정보를 GF-200으로 변경
					 * for(Map<String,Object> m : sealRstList) { if(iPIdx ==
					 * NumberUtil.toInt(m.get("P_IDX"))) { m.put("P_VAL","GF-200"); } }
					 * 
					 * //GX-200 Seal 추가를 위한 Idx iAdd_GX200_idx = iPIdx; }
					 */

					// if(!"Y".equals(StringUtil.get(item.get("BELLOWS_YN")))) {
					// GX-200 Seal 추가를 위한 Idx
					// iAdd_GX200_idx = iPIdx;
					// }

				} else {
					if (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 5.6) {
						// Plan 53A
						setApiPlanOutboard(planRstList, iPIdx, "53A", fp);
						setResultProcList(procRstList, 0, "[API Plan] Psc < 5.6 => Y, API Plan : 53A");

					} else {
						// Plan 53B
						setApiPlanOutboard(planRstList, iPIdx, "53B", fp);
						setResultProcList(procRstList, 0, "[API Plan] Psc < 5.6 => N, API Plan : 53B");

						// ----------------------------
						// A3 , 53B Plan이 정해진 경우
						// ----------------------------
						// * Type A 인 경우
						// 압력에 15barg 압력추가 QB 1번 그래프 체크 = Y 이면
						// Psc ≤ 51.7 barg 체크
						// Y : HSH/HSH
						// N : DHTW/DHTW, 3CW-FF 로 Seal Type 변경 필요

						// * TYPE B, C 인경우
						// Psc + 15bar 가 c3 operation window 압력허용여부 체크
						// Y : 처리없음
						// N : 결과 삭제 후 Note 표시 : Type 변경필요

//						if("A".equals(StringUtil.get(item.get("ABC_TYPE")))){
//							setResultProcList(procRstList, 0, "[Seal API] A3,53B 재처리-Type : A");
//							
//							ScriptEngineManager mgr = new ScriptEngineManager();
//							ScriptEngine engine = mgr.getEngineByName("JavaScript");
//							
//							double dSealSizetmp = getSealSize(item, "IN", "QBW");
//							double dSealChamPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 15;
//							if (isQBWPressChk(item, engine, "1", dSealChamPress, dSealSizetmp )) { // size = In로 적용
//								setResultProcList(procRstList, 0, "[Seal] A3,53B 재처리-QB 한계 초과 : Y");
//								
//								//if (dSealChamPress <= 51.7) {
//								if (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) <= 51.7) {
//									setResultProcList(procRstList, 0, "[Seal] (Psc+15) ≤ 51.7 barg : Y");
//									//Y :	HSH/HSH	로 변경
//									for(Map<String,Object> s : sealRstList) {
//										if(NumberUtil.toInt(s.get("P_IDX")) == iPIdx) {
//											s.put("P_VAL", "HSH/HSH");
//											setResultProcList(procRstList, 0, "[Seal] A3,53B 재처리-Seal설정 : HSH/HSH");
//										}
//									}
//								}else {
//									setResultProcList(procRstList, 0, "[Seal API] A3,53B 재처리-(Psc+15) ≤ 51.7 barg : N");
//									//N :	DHTW/DHTW, 3CW-FF 
//									for(Map<String,Object> s : sealRstList) {
//										if(NumberUtil.toInt(s.get("P_IDX")) == iPIdx) {
//											s.put("P_VAL", "DHTW/DHTW");
//											item.put("SEAL_CONFIG", "3CW-FF");
//											setResultProcList(procRstList, 0, "[Seal] A3,53B 재처리-Seal설정 : DHTW/DHTW");
//										}
//									}
//								}
//							}
//								
//						}else { // B or C
//							setResultProcList(procRstList, 0, "[Seal API] A3,53B 재처리-Type:B");
//							
//							
//							//C3 체크
//							String[] sSeals = new String[]{} ; // Outboard Seal로 체크한다.
//							for(Map<String,Object> s : sealRstList) {
//								if(NumberUtil.toInt(s.get("P_IDX")) == iPIdx) {
//									sSeals = StringUtil.get(s.get("P_VAL")).split("/");
//									break;
//								}
//							}
//
//							boolean bIsOk = true;
//							Map<String,Object> param = null;
//							for(String sSeal : sSeals) {
//								param = new HashMap<String,Object>();
//								param.put("SEAL_CHAM_MAX", NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 15); // 15압력을 더해서 체크
//								if (!step_c3_permit_chk(param, sSeal) ) {
//									bIsOk = false;
//									break;
//								} 
//							}
//							
//							if(!bIsOk) {
//								//결과 삭제 후  Note 표시 : Type 변경필요
//								// 결과 삭제
//								removeResultAll(fp, iPIdx);
//								// Note
//								setResultNoteList(noteRstList, iPIdx, "A3, 53B 조건에서 압력초과 : Type 변경필요");
//							}	
//						}

					}
				}
			}

		}

		// GX-200 ADD 추가처리
		if (iAdd_GX200_idx != 0) {
			int iPidx_add = getNextIdx(fp);
			setResultList(sealRstList, iPidx_add, "GX-200", null, fp);

			// GX-200 Plan 정보를 체크하여 GF-200 Seal Plan에 추가
			List<String> addPlanList = new ArrayList<String>();
			for (Map<String, Object> planM : planRstList) {
				if (iAdd_GX200_idx == NumberUtil.toInt(planM.get("P_IDX"))) {
					String sVal = "" + planM.get("P_VAL");
					if ("".equals(sVal))
						sVal = "[BLANK]"; // Plan에서 빈값으로 등록되는 경우를 처리하기 위함
					addPlanList.add(sVal);
				}
			}
			for (String p : addPlanList) {
				setResultListPlan(planRstList, iPidx_add, p, null, fp);
			}
		}

		// API Process 완료 처리
		for (Map<String, Object> sm : sealRstList) {
			Map<String, Object> addInfo = sm.get("ADD_INFO") == null ? new HashMap<String, Object>()
					: (HashMap<String, Object>) sm.get("ADD_INFO");
			if (!"1".equals(StringUtil.get(addInfo.get("R_TYPE")))) {
				addInfo.put("API_PROCESS_SET", "Y");
			}
			sm.put("ADD_INFO", addInfo);
		}

		System.out.println("step_api_plan end - sealRstList : " + sealRstList);
		System.out.println("step api plan end");

		return "";
	}

	private void step_seal_type_set_after_api_plan(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		System.out.println("======>" + sealRstList);

		List<Map<String, String>> addSealList = new ArrayList<Map<String, String>>();

		for (Map<String, Object> s : sealRstList) {

			// __seal_model_type_A_api_1__
			// A1 : Plan 23 - DHTW / 그 외 - UHTW
			// A2 : Plan 23 - DHTW/DHTW / 그 외 - UHTW/DHTW
			// A3 : DHTW/DHTW, 3CW-FF
			if ("__seal_model_type_A_api_1__".equals(StringUtil.get(s.get("P_VAL")))) {
				int ichk = 0;

				for (Map<String, Object> p : planRstList) {
					if (NumberUtil.toInt(s.get("P_IDX")) == NumberUtil.toInt(p.get("P_IDX"))) {
						String plan = StringUtil.get(p.get("P_VAL")).split("/")[0];

						if (ichk == 0) {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] ARRANGEMENT 1 : Y");
								if ("23".equals(plan)) {
									s.put("P_VAL", "DHTW");
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : DHTW");
								} else {
									s.put("P_VAL", "UHTW");
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : UHTW");
								}
							} else if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] ARRANGEMENT 2 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : DHTW/DHTW");
									s.put("P_VAL", "DHTW/DHTW");
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : UHTW/DHTW");
									s.put("P_VAL", "UHTW/DHTW");
								}
							} else if ("3".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] ARRANGEMENT 3 : Y");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : DHTW/DHTW");
								s.put("P_VAL", "DHTW/DHTW");
								item.put("SEAL_CONFIG", "3CW-FF");
							}
						} else {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] ARRANGEMENT 1 : Y");
								if ("23".equals(plan)) {

									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : DHTW");
									// s.put("P_VAL","DHTW");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "DHTW");
									addSealList.add(addSeal);

								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : UHTW");

									// s.put("P_VAL","UHTW");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "UHTW");
									addSealList.add(addSeal);

								}
							} else if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] ARRANGEMENT 2 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : DHTW/DHTW");
									// s.put("P_VAL","DHTW/DHTW");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "DHTW/DHTW");
									addSealList.add(addSeal);

								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : UHTW/DHTW");
									// s.put("P_VAL","UHTW/DHTW");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "UHTW/DHTW");
									addSealList.add(addSeal);
								}
							} else if ("3".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] ARRANGEMENT 3 : Y");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : DHTW/DHTW");
								// s.put("P_VAL","DHTW/DHTW");
								item.put("SEAL_CONFIG", "3CW-FF");

								Map<String, String> addSeal = new HashMap<String, String>();
								addSeal.put("P_IDX", "" + s.get("P_IDX"));
								addSeal.put("P_VAL", "DHTW/DHTW");
								addSealList.add(addSeal);
							}
						}
						ichk++;
					}
				}

				// Arrangement2 에서 GSL로 Outboard 교체되는 로직이 해당단계에서 아직 Seal이 정해지지 않은 경우가 있어
				// 여기서 해당되는 경우 추가 적용
				if ("2".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
					// 2CW-CS Configuration
					// Plan 75, 76에 대한 요구? <- ?
					if (("2CW-CS".equals(StringUtil.get(item.get("SEAL_CONFIG")))
							|| "2CW-CS".equals(StringUtil.get(item.get("API_PLAN_DIR_CONFIG"))))
							|| isPlan("75", StringUtil.get(item.get("API_PLAN_DIR")))
							|| isPlan("76", StringUtil.get(item.get("API_PLAN_DIR")))) {
						setResultProcList(procRstList, 0,
								"[API Plan] 2CW-CS, Plan 75, 76에 대한 요구로 Outboard Seal을 GSL로 교체");
						// 2CW-CS , 72 Plan 및 이후 적용Plan이 75,76을 사용하기 때문에 Gas 유체에 대한 처리를 위해 GSL Seal로 교체
						// 처리
						setOutboardSealType(sealRstList, NumberUtil.toInt(s.get("P_IDX")), "GSL");
					}
				}

			}

			// __seal_model_type_A_api_2__
			// A1 : Plan 23 - ISC2-682XP / 그 외 - ISC2-682PX
			// A2 and A3 : ISC2-682PP
			if ("__seal_model_type_A_api_2__".equals(StringUtil.get(s.get("P_VAL")))) {
				int ichk = 0;
				for (Map<String, Object> p : planRstList) {
					if (NumberUtil.toInt(s.get("P_IDX")) == NumberUtil.toInt(p.get("P_IDX"))) {
						String plan = StringUtil.get(p.get("P_VAL")).split("/")[0];

						if (ichk == 0) {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682XP");
									s.put("P_VAL", "ISC2-682XP");
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682PX");
									s.put("P_VAL", "ISC2-682PX");
								}
							} else {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : N");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682PP");
								s.put("P_VAL", "ISC2-682PP");
							}
						} else {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682XP");
									// s.put("P_VAL","ISC2-682XP");

									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "ISC2-682XP");
									addSealList.add(addSeal);
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682PX");
									// s.put("P_VAL","ISC2-682PX");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "ISC2-682PX");
									addSealList.add(addSeal);
								}
							} else {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : N");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682PP");
								// s.put("P_VAL","ISC2-682PP");
								Map<String, String> addSeal = new HashMap<String, String>();
								addSeal.put("P_IDX", "" + s.get("P_IDX"));
								addSeal.put("P_VAL", "ISC2-682PP");
								addSealList.add(addSeal);
							}
						}

						ichk++;
					}
				}
			}

			// __seal_model_type_B_api_1__
			// A1 : Plan 23 - ISC2-682XB / 그 외 - ISC2-682BX
			// A2 and A3 : ISC2-682BB
			if ("__seal_model_type_B_api_1__".equals(StringUtil.get(s.get("P_VAL")))) {
				int ichk = 0;
				for (Map<String, Object> p : planRstList) {
					if (NumberUtil.toInt(s.get("P_IDX")) == NumberUtil.toInt(p.get("P_IDX"))) {
						String plan = StringUtil.get(p.get("P_VAL")).split("/")[0];

						if (ichk == 0) {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682XB");
									s.put("P_VAL", "ISC2-682XB");
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682BX");
									s.put("P_VAL", "ISC2-682BX");
								}
							} else {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : N");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682BB");
								s.put("P_VAL", "ISC2-682BB");
							}
						} else {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682XB");
									// s.put("P_VAL","ISC2-682XB");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "ISC2-682XB");
									addSealList.add(addSeal);
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682BX");
									// s.put("P_VAL","ISC2-682BX");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "ISC2-682BX");
									addSealList.add(addSeal);
								}
							} else {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : N");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-682BB");
								// s.put("P_VAL","ISC2-682BB");
								Map<String, String> addSeal = new HashMap<String, String>();
								addSeal.put("P_IDX", "" + s.get("P_IDX"));
								addSeal.put("P_VAL", "ISC2-682BB");
								addSealList.add(addSeal);
							}
						}

						ichk++;
					}
				}
			}

			// A1 : Plan 23 - ISC2-XB
			// 그 외 ISC2-BX
			// A2 and A3 : ISC2-BB
			if ("__seal_model_cartridge_api_1__".equals(StringUtil.get(s.get("P_VAL")))) {
				int ichk = 0;
				for (Map<String, Object> p : planRstList) {
					if (NumberUtil.toInt(s.get("P_IDX")) == NumberUtil.toInt(p.get("P_IDX"))) {
						String plan = StringUtil.get(p.get("P_VAL")).split("/")[0];

						if (ichk == 0) {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal Type] Arrangement 1 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-XB");
									s.put("P_VAL", "ISC2-XB");
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-BX");
									s.put("P_VAL", "ISC2-BX");
								}
							} else {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : N");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-BB");
								s.put("P_VAL", "ISC2-BB");
							}
						} else {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal Type] Arrangement 1 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-XB");
									// s.put("P_VAL","ISC2-XB");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "ISC2-XB");
									addSealList.add(addSeal);
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-BX");
									// s.put("P_VAL","ISC2-BX");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "ISC2-BX");
									addSealList.add(addSeal);
								}
							} else {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : N");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-BB");
								// s.put("P_VAL","ISC2-BB");
								Map<String, String> addSeal = new HashMap<String, String>();
								addSeal.put("P_IDX", "" + s.get("P_IDX"));
								addSeal.put("P_VAL", "ISC2-BB");
								addSealList.add(addSeal);
							}
						}

						ichk++;
					}
				}
			}

			// A1 : Plan 23 - ISC2-XP
			// 그 외 ISC2-PX
			// A2 and A3 : ISC2-PP
			if ("__seal_model_cartridge_api_2__".equals(StringUtil.get(s.get("P_VAL")))) {
				int ichk = 0;
				for (Map<String, Object> p : planRstList) {
					if (NumberUtil.toInt(s.get("P_IDX")) == NumberUtil.toInt(p.get("P_IDX"))) {
						String plan = StringUtil.get(p.get("P_VAL")).split("/")[0];

						if (ichk == 0) {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal Type] Arrangement 1 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-XP");
									s.put("P_VAL", "ISC2-XP");
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-PX");
									s.put("P_VAL", "ISC2-PX");
								}
							} else {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : N");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-PP");
								s.put("P_VAL", "ISC2-PP");
							}
						} else {
							if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
								setResultProcList(procRstList, 0, "[Seal Type] Arrangement 1 : Y");
								if ("23".equals(plan)) {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : Y");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-XP");
									// s.put("P_VAL","ISC2-XP");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "ISC2-XP");
									addSealList.add(addSeal);
								} else {
									setResultProcList(procRstList, 0, "[Seal] API Plan 23 : N");
									setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-PX");
									// s.put("P_VAL","ISC2-PX");
									Map<String, String> addSeal = new HashMap<String, String>();
									addSeal.put("P_IDX", "" + s.get("P_IDX"));
									addSeal.put("P_VAL", "ISC2-PX");
									addSealList.add(addSeal);
								}
							} else {
								setResultProcList(procRstList, 0, "[Seal] Arrangement 1 : N");
								setResultProcList(procRstList, 0, "[Seal] Seal 설정 : ISC2-PP");
								// s.put("P_VAL","ISC2-PP");
								Map<String, String> addSeal = new HashMap<String, String>();
								addSeal.put("P_IDX", "" + s.get("P_IDX"));
								addSeal.put("P_VAL", "ISC2-PP");
								addSealList.add(addSeal);
							}
						}
						ichk++;
					}
				}
			}

		}

		// 추가 필요 시 Seal 정보 추가
		// 동일 Index 내에서 추가됨.
		// - Api Plan에 따라 Seal 설정되는 케이스에서 Plan이 복수개가 나오는 경우
		// 동일한 Seal로 설정되는 문제해결을 위함
		for (Map<String, String> addSeal : addSealList) {
			setResultList(sealRstList, NumberUtil.toInt(addSeal.get("P_IDX")), addSeal.get("P_VAL"), null, fp);
		}

	}

	private void step_seal_type_etc(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		// String[] saProduct = (String[])fp.get("saProduct");

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		String bIsRoChk = null; // RO 허용조건 체크
		boolean bIsROOk = true; // RO 허용

		// Shaft Size (Inch)
		// double dShaftSizeIN = NumberUtil.toDouble(item.get("SHAFT_SIZE")) * 0.0393701
		// ;
		double dShaftSizeIN = NumberUtil.toDouble(item.get("SHAFT_SIZE")) / 25.4;
		// Seal Size (Inch)
		// double dSealSizeIN = getSealSize(item, "IN");
		// Seal Chamber Press (psig)
		double dSealChamPres_psig = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) / 0.069;
		double dSealChamPres = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"));
		// 그래프 압력
		double dPress = 0d;

		Map<String, Object> addInfo2 = null;
		Map<String, Object> addInfo4 = null;
		Map<String, Object> g_param = new HashMap<String, Object>();
		Map<String, Object> gf_param = new HashMap<String, Object>();

		// 등록된 Seal에서 별도 추가정보 처리 필요 데이터에 대한 작업
		for (Map<String, Object> seal : sealRstList) {

			int iIdx = NumberUtil.toInt(seal.get("P_IDX"));

			// RO Seal 압력 한계 이내?
			// __RO_CHK_1__
			// __RO_CHK_2__

			if ("__RO_CHK_1__".equals(StringUtil.get(seal.get("P_VAL")))
					|| "__RO_CHK_2__".equals(StringUtil.get(seal.get("P_VAL")))) {

				// RO 그래프 체크

				if (bIsRoChk == null) {

					double dSealSizeROtmp = getSealSize(item, "IN", "RO", "", "1", fp);
					setResultProcList(procRstList, 0, "[Seal] Seal Size[RO기준] :" + dSealSizeROtmp + " IN");

					String sM2 = "";
					String sM4 = "";

					List<String> productChkList = new ArrayList<String>();
					for (String product : saProductGroup) {
						productChkList.add(product);
					}

					// 2, 4번 재질 정보 임시 리스트
					List<Map<String, Object>> materialList2_tmp = null;
					List<Map<String, Object>> materialList4_tmp = null;
					Iterator iterM2 = null;
					Iterator iterM4 = null;

					for (int i = 0; i < 2; i++) {
						if (i == 0) {
							materialList2_tmp = material2RstList;
							materialList4_tmp = material4RstList;
						} else {
							materialList2_tmp = material2OutRstList;
							materialList4_tmp = material4OutRstList;
						}

						// 삭제 체크 리스트
						List<Map<String, String>> chkList = new ArrayList<Map<String, String>>();

						for (Map<String, Object> m2 : materialList2_tmp) { // Face2 재질
							sM2 = "";
							sM4 = "";
							if (iIdx == NumberUtil.toInt(m2.get("P_IDX"))) {
								addInfo2 = (Map<String, Object>) m2.get("ADD_INFO"); // 추가정보
								sM2 = StringUtil.get(addInfo2.get("MTRL_CD"));
							}

							for (Map<String, Object> m4 : materialList4_tmp) { // Face4 재질
								if (iIdx == NumberUtil.toInt(m4.get("P_IDX"))) {
									addInfo4 = (Map<String, Object>) m4.get("ADD_INFO"); // 추가정보
									sM4 = StringUtil.get(addInfo4.get("MTRL_CD"));
								}

								// Graph 대상 체크
								if (!"".equals(sM2) && !"".equals(sM4)) {
									g_param.clear();
									g_param.put("SEAL_TYPE", "RO");
									g_param.put("MTRL_CD_M2", sM2);
									g_param.put("MTRL_CD_M4", sM4);
									g_param.put("SPEED", item.get("RPM_MAX")); // 속도는 min, nor, max중 어떤걸 쓸지 확인 필요

									for (String productChkItem : productChkList) {

										g_param.put("BELLOWS_MTRL", null);
										g_param.put("PRODUCT_GRP", productChkItem);

										// graph 조회
										List<Map<String, Object>> graphList = rBMapper.selectRuleGraph(g_param);

										if (graphList.size() > 0) {
											// graph Func 조회
											for (Map<String, Object> g_data : graphList) {
												gf_param.clear();
												gf_param.put("GRAPH_NO", g_data.get("GRAPH_NO"));
												gf_param.put("CURV_NO", g_data.get("CURV_NO"));

												// Size 구분체크
												if ("SEAL".equals(g_data.get("SIZE_GB"))) {
													// gf_param.put("VAL",dSealSizeIN); // Seal SIze
													gf_param.put("VAL", dSealSizeROtmp); // Seal SIze

												} else if ("SHAFT".equals(g_data.get("SIZE_GB"))) {
													// gf_param.put("SIZE", item.get("SHAFT_SIZE")); // Seal SIze
													gf_param.put("VAL", dShaftSizeIN); // Seal SIze (IN로 변경한다.) 1 mm -
																						// 0.0393701 in
												}

												List<Map<String, Object>> grapFunchList = rBMapper
														.selectRuleGraphFunc(gf_param);
												String sFunc = "";
												if (grapFunchList.size() > 0) {
													sFunc = StringUtil.get(
															((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
												}

												// 압력 체크 (씰챔버압력과 비교)
												if (!"".equals(sFunc)) {

													if ("SEAL".equals(g_data.get("SIZE_GB"))) {
														// sFunc = sFunc.replace("x",""+dSealSizeIN); //
														sFunc = sFunc.replace("x", "" + dSealSizeROtmp); //
													} else if ("SHAFT".equals(g_data.get("SIZE_GB"))) {
														sFunc = sFunc.replace("x", "" + dShaftSizeIN); //
													}
													System.out.println("[RO Chk]sFunc :  " + sFunc);

													dPress = NumberUtil.toDouble(engine.eval(sFunc));

													System.out.println("[RO Chk]func Press :  " + dPress
															+ " / SealCham Press : " + dSealChamPres_psig);

													// 압력 제한범위를 초과하면 (씰챔버압력)
													// 한개라도 넘어가면 false 처리
													if ("A1-26".equals(StringUtil.get(g_data.get("GRAPH_NO")))
															|| "A1-27".equals(StringUtil.get(g_data.get("GRAPH_NO")))) { // bar로
																															// 비교

														if (dPress < dSealChamPres) {
															System.out.println("[RO Chk]press Over");
															bIsROOk = false;
															break;
														}

													} else { // pai로 비교
														if (dPress < dSealChamPres_psig) {
															System.out.println("[RO Chk]press Over");
															bIsROOk = false;
															break;
														}
													}

												}
											}
										} // end graph check

									}

								} // if (!"".equals(sM2) && !"".equals(sM4)) {

							}

						} // for (int i=0;i<2;i++) {

					} // for (int i=0;i<2;i++) {

				} // if(bIsRoChk == null) {

				// bIsROOk

				// RO Seal 압력 한계 이내?
				// __RO_CHK_1__
				// __RO_CHK_2__
				if ("__RO_CHK_1__".equals(StringUtil.get(seal.get("P_VAL")))) {
					if (bIsROOk) {

						setResultProcList(procRstList, 0, "[Seal] RO Seal 압력 한계 이내 : Y");
						setResultProcList(procRstList, 0, "[Seal] Seal 설정 : RO");

						seal.put("P_VAL", "RO"); // RO 실적용
					} else {

						setResultProcList(procRstList, 0, "[Seal] RO Seal 압력 한계 이내 : N");
						setResultProcList(procRstList, 0, "[Seal] Seal 설정 : PTO");

						seal.put("P_VAL", "PTO"); // PTO 실적용
					}
				} else if ("__RO_CHK_2__".equals(StringUtil.get(seal.get("P_VAL")))) {
					if (bIsROOk) {

						setResultProcList(procRstList, 0, "[Seal] RO Seal 압력 한계 이내 : Y");
						setResultProcList(procRstList, 0, "[Seal] Seal 설정 : RO/RO");

						seal.put("P_VAL", "RO/RO"); // RO 실적용
					} else {

						setResultProcList(procRstList, 0, "[Seal] RO Seal 압력 한계 이내 : N");
						setResultProcList(procRstList, 0, "[Seal] Seal 설정 : RO/PTO");

						seal.put("P_VAL", "RO/PTO"); // PTO 실적용
					}
				}
			}
		}
	}

	private void step_seal_type_after(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer

		if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {
			String sTmp = null;
			for (Map<String, Object> m : sealRstList) {

				Map<String, Object> addInfo = m.get("ADD_INFO") == null ? new HashMap<String, Object>()
						: (HashMap<String, Object>) m.get("ADD_INFO");
				// 표준 프로세스로 설정된 Seal Type인 경우 skip
				if ("".equals(StringUtil.get(addInfo.get("R_TYPE")))) {
					continue;
				}

				// 의미없는 코드....일수도
				sTmp = StringUtil.get(m.get("P_VAL"));
				// 이후에 다시 처리하기위한 Seal Type정보는 skip -> "__" 으로 시작
				if (sTmp.startsWith("__"))
					continue;

				if (!sTmp.contains("/")) {
					m.put("P_VAL", sTmp + "/" + sTmp);
				}
			}
		}

		// 우선적용된 재질에 대하여 dual Seal로 설정된 경우 재질정보도 설정한다.
		if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {

			for (Map<String, Object> m : sealRstList) {

				// System.out.println("dual Seal Material 체크용 - Seal Type : " + m.get("P_VAL"));

				Map<String, Object> addInfo = null;
				if (m.get("ADD_INFO") != null) {
					addInfo = (HashMap<String, Object>) m.get("ADD_INFO");
					// End User, FTA 에서 등록된 Seal Type을 제외 한 idx 중 시작값
					// if (!"1".equals(addInfo.get("R_TYPE"))) continue;
					if ("".equals(addInfo.get("R_TYPE"))) {
						continue;
					}
				}

//				Map<String,Object> addInfo = (HashMap<String,Object>)m.get("ADD_INFO"); 
//				// End User, FTA 에서 등록된 Seal Type을 제외 한 idx 중 시작값 
//				if (!"1".equals(addInfo.get("R_TYPE")))  continue;

				int iPidx = NumberUtil.toInt(m.get("P_IDX"));

				for (Map<String, Object> m1 : material1RstList) {
					if (iPidx == NumberUtil.toInt(m1.get("P_IDX"))) {
						Map<String, Object> m_tmp = new HashMap<String, Object>();

						Map<String, Object> m_add_tmp = null;
						if (m1.get("ADD_INFO") != null) {
							m_add_tmp = (HashMap<String, Object>) ((HashMap<String, Object>) m1.get("ADD_INFO"))
									.clone();
						}

						m_tmp.put("P_IDX", iPidx);
						m_tmp.put("P_SEQ", getMaxSeq(material1OutRstList, iPidx));
						m_tmp.put("P_VAL", m1.get("P_VAL"));
						m_tmp.put("ADD_INFO", m_add_tmp);
						material1OutRstList.add(m_tmp);
					}
				}

				for (Map<String, Object> m1 : material2RstList) {
					if (iPidx == NumberUtil.toInt(m1.get("P_IDX"))) {
						Map<String, Object> m_tmp = new HashMap<String, Object>();
						Map<String, Object> m_add_tmp = null;
						if (m1.get("ADD_INFO") != null) {
							m_add_tmp = (HashMap<String, Object>) ((HashMap<String, Object>) m1.get("ADD_INFO"))
									.clone();
						}
						m_tmp.put("P_IDX", iPidx);
						m_tmp.put("P_SEQ", getMaxSeq(material2OutRstList, iPidx));
						m_tmp.put("P_VAL", m1.get("P_VAL"));
						m_tmp.put("ADD_INFO", m_add_tmp);
						material2OutRstList.add(m_tmp);
					}
				}

				for (Map<String, Object> m1 : material3RstList) {
					if (iPidx == NumberUtil.toInt(m1.get("P_IDX"))) {
						Map<String, Object> m_tmp = new HashMap<String, Object>();
						Map<String, Object> m_add_tmp = null;
						if (m1.get("ADD_INFO") != null) {
							m_add_tmp = (HashMap<String, Object>) ((HashMap<String, Object>) m1.get("ADD_INFO"))
									.clone();
						}
						m_tmp.put("P_IDX", iPidx);
						m_tmp.put("P_SEQ", getMaxSeq(material3OutRstList, iPidx));
						m_tmp.put("P_VAL", m1.get("P_VAL"));
						m_tmp.put("ADD_INFO", m_add_tmp);
						material3OutRstList.add(m_tmp);
					}
				}

				for (Map<String, Object> m1 : material4RstList) {
					if (iPidx == NumberUtil.toInt(m1.get("P_IDX"))) {
						Map<String, Object> m_tmp = new HashMap<String, Object>();
						Map<String, Object> m_add_tmp = null;
						if (m1.get("ADD_INFO") != null) {
							m_add_tmp = (HashMap<String, Object>) ((HashMap<String, Object>) m1.get("ADD_INFO"))
									.clone();
						}
						m_tmp.put("P_IDX", iPidx);
						m_tmp.put("P_SEQ", getMaxSeq(material4OutRstList, iPidx));
						m_tmp.put("P_VAL", m1.get("P_VAL"));
						m_tmp.put("ADD_INFO", m_add_tmp);
						material4OutRstList.add(m_tmp);
					}
				}

			}

		}

	}

	private String step_api_plan_after_process(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer

		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		// 처리 Index 목록
		List<String> processIdx = new ArrayList<String>();

		for (Map<String, Object> m : sealRstList) {
			// Process로 생성된 경우
			if (m.get("ADD_INFO") == null) {
				processIdx.add(StringUtil.get(m.get("P_IDX")));
			} else {
				Map<String, Object> adm = (HashMap<String, Object>) m.get("ADD_INFO");
				// FTA나 End user로 설정된 Seal은 일단 제외
				if (!"1".equals(StringUtil.get(adm.get("R_TYPE")))) {
					processIdx.add(StringUtil.get(m.get("P_IDX")));
				}
			}
		}

		System.out.println("--------------------plan dir check----------------->>>");
		System.out.println(" planRstList : " + planRstList);
		System.out.println(" processIdx : " + processIdx);

		String sPlanDir = StringUtil.get(item.get("API_PLAN_DIR")); // 지정 Plan

		// Seal 목록 기준으로 Plan 정보를 재구성한다.
		for (String sIdx : processIdx) {

			for (Map<String, Object> m : planRstList) {

				if (sIdx.equals(StringUtil.get(m.get("P_IDX")))) {

					int iPIdx = NumberUtil.toInt(sIdx);
					String sResultPlan = StringUtil.get(m.get("P_VAL"));// plan 정보

					String sSealType = "";
					for (Map<String, Object> sm : sealRstList) {
						if (sIdx.equals(StringUtil.get(sm.get("P_IDX")))) {
							sSealType = StringUtil.get(sm.get("P_VAL"));
							break;
						}
					}

					String sSealTypeIn = sSealType.split("/")[0]; // Inboard Seal

					// -------------------------------------------
					// 유체별 추가 적용 처리
					// -------------------------------------------

					// [B1-13] Product에 NaOH 가 포함될 경우
					// NAOH = SODIUM HYDROXIDE
					if (isProduct("SODIUM HYDROXIDE", saProductGroup, saProduct)) {
						Map<String, Object> ptm = new HashMap<String, Object>();
						ptm.put("MCD", "B11301");
						String sgb_tmp = getProductGb_byGrouping(item, "SODIUM HYDROXIDE", saProductGroup, saProduct);
						ptm.put("NAOH_CONT", "".equals(sgb_tmp) ? "0" : sgb_tmp.replace("%", ""));
						ptm.put("TEMP_MAX", item.get("TEMP_MAX"));

						System.out.println("========> NAOH_CONT : " + sgb_tmp);

						List<Map<String, Object>> rComList = rBMapper.selectRuleComListB11301(ptm);

						if (!rComList.isEmpty()) {
							ptm.clear();
							ptm.put("MCD", "B11302");
							ptm.put("ATTR1", rComList.get(0).get("ATTR1"));
							List<Map<String, Object>> rComList2 = getRuleComListType1(ptm); // B11302 - Caustic services
																							// Selections

							if (!rComList2.isEmpty()) {

								Map<String, Object> comM = rComList2.get(0);

								// ---------------------------------------
								// 입력된 사용자 조건에 따른 Arrangement 및 Plan 반영
								// ---------------------------------------
								// Arrangement
								if (comM.get("ATTR2") != null && !"".equals(StringUtil.get(comM.get("ATTR2")))) {
									int iArrangementTmp = NumberUtil.toInt(comM.get("ATTR2"));

									// step_arrangement_pre 에서 사용자에 입력된 인자에 따른 arrangement가 설정된 경우 체크
									if (NumberUtil.toInt(item.get("ARRANGEMENT")) == 0
											|| NumberUtil.toInt(item.get("ARRANGEMENT")) <= iArrangementTmp) {

										String sNaohAddPlan = StringUtil.get(comM.get("ATTR3"));
										// plan정보
										if (comM.get("ATTR3") != null && !"".equals(sNaohAddPlan)) {

											setResultProcList(procRstList, 0,
													"유체 NaOH 기준 적용 : API Plan : " + sNaohAddPlan);

											// sPrioritySetPlan = setApiConfig(sPrioritySetPlan, sNaohAddPlan,
											// procRstList);
											// Naoh 기준 Plan을 우선시
											sResultPlan = setApiConfig(sNaohAddPlan, sResultPlan, iPIdx, fp, sPlanDir);
											m.put("P_VAL", sResultPlan);
										}
									}
								}
							}
						}
					}

					if (isProduct("SULFUR", saProductGroup, saProduct)) {
						if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 1.5
								&& NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) >= 1.5
								&& NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) >= 1.5) {

							// plan 02/62 추가
							if (NumberUtil.toInt(item.get("ARRANGEMENT")) == 1) {

								setResultProcList(procRstList, 0, "유체 : Sulfur 기준 적용 : API Plan : 02/62");
								sResultPlan = setApiConfig("02/62", sResultPlan, iPIdx, fp, sPlanDir);
								m.put("P_VAL", sResultPlan);
							}
						}
					}

					if (isProduct("[RESIDUE]", saProductGroup, saProduct)) {

						// Clean 여부에 따라 다음과 같이 적용
						// Clean 하지 않거나 또는 정확한 정보 없는 경우 : Plan 32 적용

						if ("N".equals(StringUtil.get(item.get("SOLID_GB")))
								|| "".equals(StringUtil.get(item.get("SOLID_GB")))) {
							// no logic
						} else { // clean하지 않을 경우

							// if (NumberUtil.toInt(item.get("ARRANGEMENT")) == 1) {
							sResultPlan = setApiConfig("32", sResultPlan, iPIdx, fp, sPlanDir);
							m.put("P_VAL", sResultPlan);
							setResultProcList(procRstList, 0, "유체 Residue Rule 기준적용 : API Plan : 32");
							// }

						}
					}

					// -----------------------------
					// 사용자 지정 입력 Plan 반영
					// -----------------------------
					if (!"".equals(sPlanDir)) {

						// sPerSetPlan = setApiConfig(sPerSetPlan, sPlanDir);

						// 조건
						// 1. 지정 Plan이 추천 Plan과 비교하였을 때 상위 Plan여부 체크
						// -지정 Plan이 추천 Plan과 같거나상위 Plan인 경우 지정Plan 기준
						// -지정 Plan이 추천 Plan보다 하위 Plan인 경우 추천Plan 기준
						// if (NumberUtil.toInt(item.get("API_PLAN_DIR_ARRANGEMENT")) >=
						// NumberUtil.toInt(item.get("ARRANGEMENT"))) {
						// 사용자입력 Plan을 기준으로 재설정한다.
						// sPrioritySetPlan = setApiConfig(sPlanDir, sPrioritySetPlan, procRstList);
						// }else {
						// 추천 Plan을 기준으로 재설정한다.
						// sPrioritySetPlan = setApiConfig(sPrioritySetPlan, sPlanDir, procRstList);
						// }

						// 지정 Plan이 추천 Plan과 비교하였을 때 지정plan이 추천Plan보다 같거나 상위 Plan인 경우 추천Plan에 결합
						if (NumberUtil.toInt(item.get("API_PLAN_DIR_ARRANGEMENT")) >= NumberUtil
								.toInt(item.get("ARRANGEMENT"))) {

							String sPlanDir2 = "";
							for (String sPlanTmp : sPlanDir.split("/")) {
								boolean bIs = true;

								// A3일때 23 Plan은 3CW-FB Inboard Seal만 가능
								if ("23".equals(sPlanTmp) && NumberUtil.toInt(item.get("ARRANGEMENT")) == 3) {
									bIs = false;
									// A3일때 23Plan은 3CW-FB인 Inboard Seal만 가능
									for (String sChk : getSealConfig(sSealTypeIn)) {
										if ("3CW-FB".equals(sChk)) {
											bIs = true;
											break;
										}
									}
									if (!bIs) {
										Map<String,Object> addNote = new HashMap<String,Object>();
										addNote.put("PLAN", "23");
										setResultNoteList(noteRstList, iPIdx, "A3일때 23 Plan은 3CW-FB Inboard Seal만 가능", "p", addNote);
									}
								}

								// 사용자 지정 61,62Plan은 제거한다
								// 이 후 61,62 Plan이 지정되는 단계가 있으므로...
								if ("61".equals(sPlanTmp) || "62".equals(sPlanTmp)) {
									bIs = false;
								}

								if (bIs) {
									if ("".equals(sPlanDir2)) {
										sPlanDir2 = sPlanDir2 + sPlanTmp;
									} else {
										sPlanDir2 = sPlanDir2 + "/" + sPlanTmp;
									}
								}
							}

							// 사용자 지정 Plan을 기준으로 추천 Plan을 결합
							// sResultPlan = setApiConfig(sPlanDir, sResultPlan, iPIdx, fp);
							sResultPlan = setApiConfig(sPlanDir2, sResultPlan, iPIdx, fp, sPlanDir);
						}

						m.put("P_VAL", sResultPlan);

						// 중간진행사항
						setResultProcList(procRstList, 0, "사용자 지정 Plan 반영 : " + sPlanDir);

					}

					// API 682 타입일 경우 61plan 추가
					// 0. API682 = 'Y' 무조건 61이 없으면 붙인다.
					// plan 61 적용 시 예외사항 : ISC2-682XP, ISC2-682XB 제외 --> 61,62 plan 미적용 Seal 별도 관리로
					// 로직 제외함.
					// 제외 - 1. 온도 가 0 도 이하일 경우는 61 대신 62로 적용
					// 제외 - 2. A1 이고 결정화 체크 시 62로 적용
					// 3. 온도 100이상 이고 Oil 포함인경우는 61로 그냥 설정하고 Note에 "62필요적용여부검토필요"
					// 변경(21.04.05) : -> 온도 80이상 Oil 포함
					// 제외 - 4. Sea water, SALT Product Group일 경우 Plan 62 추가

					// 61기준보다 우선 적용*****
					// 62 Plan 적용 로직 수정 - API682 기준 필요없음
					// - 1. 온도 가 0 도 이하일 경우는 62로 적용
					// - Sea water, SALT Product Group일 경우 Plan 62 적용
					// - A1 이고 결정화 체크 시 62로 적용

					String sAddPlan = "";
					String sNote = "";
					Map<String,Object> addNote = new HashMap<String,Object>();
					
					if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))
							&& "Y".equals(StringUtil.get(item.get("PC_LEAKAGE_CHK")))) { // 결정화 체크 시 62로 적용
						sAddPlan = "62";

					} else if (isProduct("WATER - SEA", saProductGroup, saProduct)
							|| isProduct("SALT", saProductGroup, saProduct)) {
						sAddPlan = "62";

					} else if (NumberUtil.toDouble(item.get("TEMP_NOR")) <= 0
							|| NumberUtil.toDouble(item.get("TEMP_MIN")) <= 0
							|| NumberUtil.toDouble(item.get("TEMP_MAX")) <= 0) { // 0도이하
						sAddPlan = "62";

					} else {
						if ("Y".equals(StringUtil.get(item.get("API682_YN")))) {

							if (NumberUtil.toDouble(item.get("TEMP_MAX")) >= 80
									&& isProduct("OIL", saProductGroup, saProduct)) {
								sAddPlan = "61";
								sNote = "온도 80이상,Oil 포함 : 62필요적용여부 검토필요";
								addNote.put("PLAN", "61");

							} else {
								sAddPlan = "61";
							}
						}
					}

					if (!"".equals(sAddPlan)) {
						setResultProcList(procRstList, 0, "API682 타입 Plan add : " + sAddPlan);

						sResultPlan = setApiConfig(sResultPlan, sAddPlan, iPIdx, fp, sPlanDir);
						m.put("P_VAL", sResultPlan);

						if (!"".equals(sNote)) {
							setResultNoteList(noteRstList, NumberUtil.toInt(sIdx), sNote, "p", addNote);
						}
					}

					// -----------------------------------------
					// 61, 62 plan 적용불가 Seal에 대한 처리
					// -----------------------------------------
					// E005
					Map<String, Object> param = new HashMap<String, Object>();
					param.put("MCD", "E005");
					param.put("ATTR1", sSealTypeIn);
					List<Map<String, Object>> rComList = getRuleComListType1(param);
					// 61,62 Plan 적용 불가 Seal일경우
					if (!rComList.isEmpty()) {
						String sResultPlan_new = "";
						for (String s : sResultPlan.split("/")) {
							if ("61".equals(s) || "62".equals(s)) {
								continue;
							} else {
								if ("".equals(sResultPlan_new)) {
									sResultPlan_new = s;
								} else {
									sResultPlan_new = sResultPlan_new + "/" + s;
								}
							}
						}
						sResultPlan = sResultPlan_new;
						m.put("P_VAL", sResultPlan);
						setResultProcList(procRstList, 0, "61,62 적용불가 Seal 처리 : " + sSealTypeIn);
					}

					// -----------------------------------------
					// Plan 결과 check
					// 11,13 Plan 동시 입력 체크
					// 11,13 plan이 동시 설정된 경우 Pump Type에 따라 한개의 Plan만 적용
					// 11 : Horizontal
					// 13 : Vertical
					// -----------------------------------------
					boolean bIsChk1 = false;
					String[] sPlanChk1 = sResultPlan.split("/");
					if (Arrays.asList(sPlanChk1).contains("11") && Arrays.asList(sPlanChk1).contains("13")) {
						String sPumpType_HV = StringUtil.get(item.get("PUMP_TYPE_HV"));
						String sRemovePlan = "";
						if ("H".equals(sPumpType_HV)) {
							sRemovePlan = "13";
						} else if ("V".equals(sPumpType_HV)) {
							sRemovePlan = "11";
						}

						String sResultPlan_new = "";
						for (String s : sResultPlan.split("/")) {
							if (sRemovePlan.equals(s)) {
								continue;
							} else {
								if ("".equals(sResultPlan_new)) {
									sResultPlan_new = s;
								} else {
									sResultPlan_new = sResultPlan_new + "/" + s;
								}
							}
						}
						sResultPlan = sResultPlan_new;
						m.put("P_VAL", sResultPlan);
						setResultProcList(procRstList, 0, "함께 사용 불가 Plan 처리로 제외 : " + sRemovePlan);
					}

					// -----------------------------------------
					// Plan 결과 check
					// Plan 72 적용 시에는 Plan 62 필요하지 않음.
					// 변경(21.04.05):로직제외
					// -----------------------------------------
					/*
					 * boolean bIsChk2 = false; if (Arrays.asList(sPlanChk1).contains("72") &&
					 * Arrays.asList(sPlanChk1).contains("62")) { String sResultPlan_new = "";
					 * for(String s : sResultPlan.split("/")) { if("62".equals(s)) { continue; }else
					 * { if("".equals(sResultPlan_new)) { sResultPlan_new = s; }else {
					 * sResultPlan_new = sResultPlan_new + "/"+ s; } } } sResultPlan =
					 * sResultPlan_new; m.put("P_VAL",sResultPlan); setResultProcList(procRstList,
					 * 0, "함께 사용 불가 Plan 처리로 제외 : " + "62"); }
					 */

					// -----------------------------------------------------------
					// Seal 결과별 Arrangement를 여기서 저장해둔다.
					// -----------------------------------------------------------
					for (Map<String, Object> sm : sealRstList) {
						if (sIdx.equals(StringUtil.get(sm.get("P_IDX")))) {
							Map<String, Object> sm_addInfo = sm.get("ADD_INFO") == null ? new HashMap<String, Object>()
									: (HashMap<String, Object>) sm.get("ADD_INFO");
							sm_addInfo.put("ARRANGEMENT", getArrangement(sResultPlan)); // Seal별 Arrangement를 저장한다.
							break;
						}
					}

				} // end idx equal check
			} // end planlist
		}

		return "";
	}

	private void step_material(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		String sArrangement = "";
		Map<String, Object> ptm = new HashMap<String, Object>(); // 쿼리 공통 파라미터

		// 불확실한 유체에 대하여 가스켓에 FFKM 재질을 우선 적용하기 위해
		// C1에 없는 product 유무를 확인

		// A3 Outboard 는 접액 체크를 하지 않으므로
		// 가스켓 FFKM 우선적용 기준 적용 불필요 -> 유체기준도 동일하게 적용 X

		// Seal,API Plan 선택 Process 를 통해 등록된 씰타입별 처리
		for (Map<String, Object> seal : sealRstList) {

			int iPIdx = NumberUtil.toInt(seal.get("P_IDX"));
			boolean bIs = true;
			Map<String, Object> seal_addInfo = seal.get("ADD_INFO") == null ? new HashMap<String, Object>()
					: (HashMap<String, Object>) seal.get("ADD_INFO");

			// End User 에서 등록된 Seal Type을 제외 한 idx 중 시작값
			if ("1".equals(StringUtil.get(seal_addInfo.get("R_TYPE")))) {
				bIs = false;
			}

			if (bIs) { // End User 설정으로 등록되지 않은 Seal 일 경우 처리

				// ----------------------------------------------------------------
				// 각 설정된 Seal별 Arrangement가 다를 수 있어 Seal추천정보에서 설정된
				// ----------------------------------------------------------------
				// Seal Type별 설정된 Arrangement정보를 사용
				sArrangement = StringUtil.get(seal_addInfo.get("ARRANGEMENT"));
				System.out.println("Material 설정 : idx : " + iPIdx + " , Arrangement : " + sArrangement);

				// Seal Type 정보 Set
				String sSealType = StringUtil.get(seal.get("P_VAL"));
				String[] sSealTypeArr = sSealType.split("/");
				String sSealTypeIn = sSealTypeArr[0]; // Inboard Seal
				String sSealTypeOut = ""; // Outboard Seal

				if ("2".equals(sArrangement) || "3".equals(sArrangement)) {
					sSealTypeOut = sSealTypeArr.length > 1 ? sSealTypeArr[1] : sSealTypeArr[0];
				}

				// Seal ABC Type
				String sSealIn_ABC_Type = "";
				String sSealOut_ABC_Type = "";
				Map<String, Object> abcTypeParam = new HashMap<String, Object>();
				abcTypeParam.put("SEAL_TYPE", sSealTypeIn);
				List<Map<String, Object>> abcType_c3List = rBMapper.selectRuleC3(abcTypeParam);

				// inboard seal ABC Type
				for (Map<String, Object> c3 : abcType_c3List) {
					String s = StringUtil.get(c3.get("SEAL_TYPE"));
					for (String ss : s.split(",")) {
						boolean bChk = false;
						if (ss.equals(sSealTypeIn)) {
							sSealIn_ABC_Type = StringUtil.get(c3.get("SEAL_GB_TYPE"));
							bChk = true;
							break;
						}
						if (bChk)
							break;
					}
				}

				abcTypeParam.clear();
				abcTypeParam.put("SEAL_TYPE", sSealTypeOut);
				abcType_c3List = rBMapper.selectRuleC3(abcTypeParam);

				// outboard seal ABC Type
				for (Map<String, Object> c3 : abcType_c3List) {
					String s = StringUtil.get(c3.get("SEAL_TYPE"));
					for (String ss : s.split(",")) {
						boolean bChk = false;
						if (ss.equals(sSealTypeOut)) {
							sSealOut_ABC_Type = StringUtil.get(c3.get("SEAL_GB_TYPE"));
							bChk = true;
							break;
						}
						if (bChk)
							break;
					}
				}

				// ---------------------
				// 재질 Seq 순서 설정기준
				// ---------------------

				// 최우선표시 재질 : -200 ~ (점도체크 시 처리)
				// 저온체크 대체재질 : -100 ~
				// 직접지정재질 : -1
				// 표준재질 : 1 ~

				// -------------------------------------
				// 지정 Material이 있을 경우
				// -------------------------------------
				String sDirMtrlDigit = "";
				String sDirMtrlGb = "";
				List<Map<String, Object>> mtrllList = new ArrayList<Map<String, Object>>();
				for (int i = 1; i <= 8; i++) {
					if (i == 1) {
						sDirMtrlDigit = StringUtil.get(item.get("M_IN_1")).toUpperCase();
						mtrllList = material1RstList;
						sDirMtrlGb = "1";
					} else if (i == 2) {
						sDirMtrlDigit = StringUtil.get(item.get("M_IN_2")).toUpperCase();
						mtrllList = material2RstList;
						sDirMtrlGb = "2";
					} else if (i == 3) {
						sDirMtrlDigit = StringUtil.get(item.get("M_IN_3")).toUpperCase();
						mtrllList = material3RstList;
						sDirMtrlGb = "3";
					} else if (i == 4) {
						sDirMtrlDigit = StringUtil.get(item.get("M_IN_4")).toUpperCase();
						mtrllList = material4RstList;
						sDirMtrlGb = "4";
					} else if (i == 5) {
						sDirMtrlDigit = StringUtil.get(item.get("M_OUT_1")).toUpperCase();
						mtrllList = material1OutRstList;
						sDirMtrlGb = "1";
					} else if (i == 6) {
						sDirMtrlDigit = StringUtil.get(item.get("M_OUT_2")).toUpperCase();
						mtrllList = material2OutRstList;
						sDirMtrlGb = "2";
					} else if (i == 7) {
						sDirMtrlDigit = StringUtil.get(item.get("M_OUT_3")).toUpperCase();
						mtrllList = material3OutRstList;
						sDirMtrlGb = "3";
					} else if (i == 8) {
						sDirMtrlDigit = StringUtil.get(item.get("M_OUT_4")).toUpperCase();
						mtrllList = material4OutRstList;
						sDirMtrlGb = "4";
					}

					if (!"".equals(sDirMtrlDigit)) {
						String sDirMtrlCd = getMaterialCd("" + i, sDirMtrlDigit);
						if (!"".equals(sDirMtrlCd)) {
							setMaterialResultListPrefer(mtrllList, sealRstList, sDirMtrlGb, iPIdx, -1, sDirMtrlCd, null,
									(i <= 4 ? "IN" : "OUT"));
							for (Map<String, Object> mMap : mtrllList) {

								if (item.containsKey("__IS_PRODUCT_WATER_GUIDE") && item.get("__IS_PRODUCT_WATER_GUIDE").equals("Y") && // Water Guide 이면서
										mMap.get("P_SEQ").equals(1) && // 최우선 재질이면서
										mMap.get("P_IDX").equals(iPIdx)) {

									if (!((Map) mMap.get("ADD_INFO")).get("MTRL_CD").equals(sDirMtrlCd)) { // 사용자 지정 재질과
																											// 안맞는 경우
										setResultNoteList(noteRstList, iPIdx,
												"Water Guide에 맞지 않는 재질입니다 : " + sDirMtrlCd);
									}
									break;
								}
							}
						} else {
							if ("Y".equals(sDirMtrlDigit)) {
								// Y 재질 코드 "-"를 부여하여 표시
								setMaterialResultListPrefer(mtrllList, sealRstList, sDirMtrlGb, iPIdx, -1, "-", null,
										(i <= 4 ? "IN" : "OUT"));
							} else {
								// 빈칸으로 표시하기 위한 TEMPORARY CODE : ---
								setMaterialResultListPrefer(mtrllList, sealRstList, sDirMtrlGb, iPIdx, -1, "---", null,
										(i <= 4 ? "IN" : "OUT"));
								setResultNoteList(noteRstList, iPIdx, "정확하지 않은 지정된 재질 : " + (i <= 4 ? "IN" : "OUT")
										+ " " + sDirMtrlGb + " : " + sDirMtrlDigit);
							}
						}
					}
				}

				System.out.println("지정 Material이 있을 경우 End");

				// =================================
				// 우선순위 재질 기준 적용
				// =================================

				// -------------------------------------
				// High Corrosive 체크 시
				// -------------------------------------
				// High corrosive가 체크되면 인보드 메탈재질을 비워두고
				// "High corrosive가 적용되어 메탈 재질검토 필요" 노트 표시
				if ("Y".equals(StringUtil.get(item.get("PC_HIGH_CORR_CHK")))) {
					Map<String, Object> addInfo_tmp = new HashMap<String, Object>();
					addInfo_tmp.put("MTRL_NM2", "High corrosive가 적용되어 메탈 재질검토 필요");
					setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, -200, "---", addInfo_tmp,
							"IN");

					setResultProcList(procRstList, iPIdx, "High corrosive가 적용되어 메탈재질 비움.");
					setResultNoteList(noteRstList, iPIdx, "High corrosive가 적용되어 메탈 재질검토 필요"); // note
				}

				// Chloride - Metal 적용
				// if(isProduct("CHLORIDE",saProductGroup, saProduct)) {
				if (isProduct("CHLORIDE", saProductGroup, saProduct)) {

					if (getProductCont(item, "CHLORIDE", "PPM") == 0) { // Chloride 농도가 없을 경우 Skip
						// 기준적용 Skip
					} else {
						String[] sMtrlcds = getChlorideMetal(item); // Chloride Metal 재질 조회

						if (sMtrlcds == null) {
							setResultProcList(procRstList, iPIdx, "Chloride 기준 적용 - Metal : 적용 재질 없음");
							setResultNoteList(noteRstList, iPIdx, "Chloride 기준 적용가능 Metal 재질 없음");
							setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, -200, "---", null,
									"IN");
							if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
								setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, -200, "---",
										null, "OUT");
							}
						} else {
							for (String sMtrlcd : sMtrlcds) {
								if (!"".equals(sMtrlcd)) {
									setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, sMtrlcd,
											null, "IN");
									setResultProcList(procRstList, iPIdx, "Chloride 기준 적용 - Metal : " + sMtrlcd);

									// A3인 경우 유체특성에 따른 재질 적용X
									if (NumberUtil.toInt(sArrangement) == 2) {
										setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0,
												sMtrlcd, null, "OUT");
									}
								}
							}
						}
					}
				}

				// ------------------------------
				// Water Guide Gasket 재질 적용
				// OH1 또는 Non-API Pump의 경우 Hot Water Application에서 Gasket 재질만 적용
				// ------------------------------
				String sPumpTypeG = getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")), _pumpTypeGroupInfo, null);
				if (saProductGroup.length == 1
						&& (saProductGroup[0].equals("WATER") || saProductGroup[0].equals("WATER - CONDENSATE")
								|| saProductGroup[0].equals("WATER - COOLING TOWER")
								|| saProductGroup[0].equals("WATER - DEIONIZED")
								|| saProductGroup[0].equals("WATER - DEMINERALIZED")
								|| saProductGroup[0].equals("WATER - DISTILLED")
								|| saProductGroup[0].equals("WHITE WATER"))
						&& ("OH1".equals(sPumpTypeG) || ("N".equals(StringUtil.get(item.get("API682_YN")))))) {

					// Parameter Map 설정
					ptm.clear();
					// Seal Size는 QBW에서만 유효하게 처리되므로 QBW기준으로 설정하고 조회한다.
					ptm.put("SEAL_SIZE", getSealSize(item, "MM", "QBW", "", "1", fp)); // Seal Size
					ptm.put("TEMP_MAX", item.get("TEMP_MAX")); // 온도 최대
					ptm.put("SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX")); // 압력 최대
					List<Map<String, Object>> rComList = rBMapper.selectRuleComListB401(ptm);

					if (!rComList.isEmpty()) {
						// 중간진행사항
						setResultProcList(procRstList, 0, "Water Application Guide 적용 : Gasket");

						for (Map<String, Object> m : rComList) {

							// Material 3 : Gasket 3nd
							if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
								for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
									setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, s, null,
											"IN");

									// A3인 경우 유체특성에 따른 재질 적용X
									if (NumberUtil.toInt(sArrangement) == 2) {
										setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, s,
												null, "OUT");
									}
								}
							}
						}
					}
				}

				// -----------------------------
				// 가스켓 우선적용 유체
				// -----------------------------

				// [C6-9] BUTADIENE 유체일 경우
				// Chemraz 505 우선적용 : G005 -> AD로 변경
				if (isProduct("BUTADIENE", saProductGroup, saProduct)) {

					// Material 3 : Gasket
					setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, "AD", null, "IN");

					if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
						setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, "AD", null, "OUT");
					}

					// 중간진행사항
					setResultProcList(procRstList, iPIdx, "[C6] Butadiene 기준 - 가스켓 : AD");
				}

				// [C7-6] Amine
				// O-ring : Chemraz 605, Chemraz 505, 나머지 FFKM의 순으로 적용
				if (isProduct("AMINE", saProductGroup, saProduct)) {
					for (String s : (new String[] { "AD", "G005", "X675" })) {
						setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, s, null, "IN");

						if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
							setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, s, null,
									"OUT");
						}
					}

					// 중간진행사항
					setResultProcList(procRstList, iPIdx, "[C7-6] Amine 기준 - 가스켓 : Chemraz 605, 505 ...");
				}

				// [C7-7] EO / PO ETHYLENE OXIDE / PROPYLENE OXIDE
				// O-ring : Chemraz 605 우선 적용
				if (isProduct("ETHYLENE OXIDE", saProductGroup, saProduct)
						|| isProduct("PROPYLENE OXIDE", saProductGroup, saProduct)) {
					for (String s : (new String[] { "AD" })) {
						setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, s, null, "IN");

						if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
							setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, s, null,
									"OUT");
						}
					}
					// 중간진행사항
					setResultProcList(procRstList, iPIdx, "EO / PO 기준 - 가스켓 : Chemraz 605");
				}

				// -----------------------------
				// 유체 중 C1에 없는 유체에 대하여 FFKM 적용
				// 유체가 C1에 없는 경우 : FFKM 우선 적용 , Note 표시
				// 유체는 있는경우 & 농도 또는 온도 가 범위에 없는 경우 : FFKM 적용, 해당 내용 NOTE 표시
				// 예외사항 체크 필요
				// - Water Guide 일때 Skip
				// -----------------------------
				if (!"Y".equals(StringUtil.get(item.get("__IS_PRODUCT_WATER_GUIDE"))) // Water Guide X
				) {

					if (!isC1GuideProduct(item, saProduct, saProductGroup, fp)) {
						setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, "[FFKM]", null, "IN");
						if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
							setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, "[FFKM]", null,
									"OUT");
						}
						setResultProcList(procRstList, iPIdx, "Material Guide에 없거나 해당하는 정보가 없음 : 가스켓 FFKM 우선적용");
					}

				}

				// -------------------------------------
				// FFKM 우선순위 그룹
				// -------------------------------------
				// AMINE Hierarchy
				// HYDROCARBON Hierarchy
				// METHANOL
				// OIL Hierarchy
				// SLURRY
				// SODIUM HYDROXIDE
				// SOLVENT
				// WASTE WATER

				if (isProduct("HYDROCARBON", saProductGroup, saProduct)
						|| isProduct("METHANOL", saProductGroup, saProduct)
						|| isProduct("OIL", saProductGroup, saProduct) || isProduct("SLURRY", saProductGroup, saProduct)
						|| isProduct("SODIUM HYDROXIDE", saProductGroup, saProduct)
						|| isProduct("SOLVENT", saProductGroup, saProduct)
						|| isProduct("WASTE WATER", saProductGroup, saProduct)) {

					// Gasket
					setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, "[FFKM]", null, "IN");

					if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
						setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, "[FFKM]", null,
								"OUT");
					}

					// 중간진행사항
					setResultProcList(procRstList, iPIdx, "FFKM 우선순위 그룹 - 가스켓 : FFKM ");
				}

				// [C7-11] Propane 유체일 경우
				if (isProduct("PROPANE", saProductGroup, saProduct)) {
					// Material 3 : Gasket
					setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, "[FFKM]", null, "IN");

					if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
						setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, "[FFKM]", null,
								"OUT");
					}

					// 중간진행사항
					setResultProcList(procRstList, iPIdx, "Propane 기준 - 가스켓 : FFKM ");
				}

				// [C7-13] Residue 유체일 경우
				if (isProduct("[RESIDUE]", saProductGroup, saProduct)) {

					// 공통 사항 : Inboard Seal Face SiC vs SiC 적용 (점도 및 온도에 관계 없이)
					// Inboard 에만 적용

					// setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, 0,
					// "SL", null, "IN");
					// setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, 0,
					// "SL", null, "IN");

					// Material 2 : face 2nd
					// face2 위치
					// int iM2Pos = getFaceSeq(sSealTypeIn, "[SIC]", 2, "IN");
					// setMaterialResultList_byStd(sSealTypeIn, "[SIC]", iM2Pos, "IN", iPIdx, fp);

					// int iM4Pos = iM2Pos==2?4:2;
					// setMaterialResultList_byStd(sSealTypeIn, "[SIC]", iM4Pos, "IN", iPIdx, fp);

					setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, 0, "[SIC]", null, "IN");
					setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, 0, "[SIC]", null, "IN");

					if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
						setMaterialResultListPrefer(material2OutRstList, sealRstList, "2", iPIdx, 0, "[SIC]", null,
								"OUT");
						setMaterialResultListPrefer(material4OutRstList, sealRstList, "4", iPIdx, 0, "[SIC]", null,
								"OUT");
					}

					setResultProcList(procRstList, iPIdx, "유체 Residue 기준 - Face : SiC vs SiC");

					// Clean하지 않을 경우, Gasket FFKM 적용
					if ("Y".equals(StringUtil.get(item.get("SOLID_YN")))
							|| "Y1".equals(StringUtil.get(item.get("SOLID_YN")))) {
						setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, "[FFKM]", null, "IN");
						if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
							setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, "[FFKM]", null,
									"OUT");
						}
					}

					setResultProcList(procRstList, iPIdx, "유체 Residue 기준 - 가스켓 : FFKM");
				}

				// [B1-13] Product에 NaOH 가 포함될 경우
				// Material, API Plan = [B1-13]
				// NAOH = SODIUM HYDROXIDE
				if (isProduct("SODIUM HYDROXIDE", saProductGroup, saProduct)) {
					ptm.clear();
					ptm.put("MCD", "B11301");
					String sgb_tmp = getProductGb_byGrouping(item, "SODIUM HYDROXIDE", saProductGroup, saProduct);
					ptm.put("NAOH_CONT", "".equals(sgb_tmp) ? "0" : sgb_tmp.replace("%", ""));
					ptm.put("TEMP_MAX", item.get("TEMP_MAX"));

					List<Map<String, Object>> rComList = rBMapper.selectRuleComListB11301(ptm);

					if (!rComList.isEmpty()) {
						ptm.clear();
						ptm.put("MCD", "B11302");
						ptm.put("ATTR1", rComList.get(0).get("ATTR1"));
						List<Map<String, Object>> rComList2 = getRuleComListType1(ptm); // B11302 - Caustic services
																						// Selections

						if (!rComList2.isEmpty()) {

							Map<String, Object> m = rComList2.get(0);

							setResultProcList(procRstList, iPIdx, "유체 NaOH 기준 적용 - 재질");

							// Material 1 : Metal 1st
							if (m.get("ATTR4") != null && !"".equals(String.valueOf(m.get("ATTR4")))) {
								for (String s : String.valueOf(m.get("ATTR4")).split(",")) {
									setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, s, null,
											"IN");

									if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
										setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0, s,
												null, "OUT");
									}
								}
							}

							// Material 3 : Gasket 3nd
							if (m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
								for (String s : String.valueOf(m.get("ATTR7")).split(",")) {
									setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, s, null,
											"IN");

									if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
										setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, s,
												null, "OUT");
									}
								}
							}

							// Seal Type이 지정되지 않은 재질기준이므로 현재 설정된 SealType을 C9표준재질에서 체크하여
							// 재질 2, 4 위치를 잡는다.

							// Material 2 : face 2nd
							int iM2Pos = 0; // face2 위치
							if (m.get("ATTR5") != null && !"".equals(String.valueOf(m.get("ATTR5")))) {
								for (String s : String.valueOf(m.get("ATTR5")).split(",")) {
									if (iM2Pos == 0)
										iM2Pos = getFaceSeq(sSealTypeIn, s, 2, "IN");
									System.out.println("2번재질 : " + s + "  iM2Pos: " + iM2Pos);
									// setMaterialResultList_byStd(sSealTypeIn, s, iM2Pos, "IN", iPIdx, fp);

									if ("[RESIN CARBON]".equals(s)) {
										// 표준재질에서 해당하는 재질을 설정
										setMaterialResultList_byStd(sSealTypeIn, s, iM2Pos, "IN", iPIdx, fp);
									} else {
										if (iM2Pos == 2) {
											setMaterialResultListPrefer(material2RstList, sealRstList, "" + 2, iPIdx, 0,
													s, null, "IN");
										} else {
											setMaterialResultListPrefer(material4RstList, sealRstList, "" + 4, iPIdx, 0,
													s, null, "IN");
										}
									}
								}
							}

							int iM4Pos = iM2Pos == 2 ? 4 : 2;
							if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
								for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
									System.out.println("4번재질 : " + s + "  iM4Pos: " + iM4Pos);
									// setMaterialResultList_byStd(sSealTypeIn, s, iM4Pos, "IN", iPIdx, fp);
									if ("[RESIN CARBON]".equals(s)) {
										// 표준재질에서 해당하는 재질을 설정
										setMaterialResultList_byStd(sSealTypeIn, s, iM2Pos, "IN", iPIdx, fp);
									} else {
										if (iM4Pos == 2) {
											setMaterialResultListPrefer(material2RstList, sealRstList, "" + 2, iPIdx, 0,
													s, null, "IN");
										} else {
											setMaterialResultListPrefer(material4RstList, sealRstList, "" + 4, iPIdx, 0,
													s, null, "IN");
										}
									}

								}
							}

							// Outboard 재질
							iM2Pos = 0;
							if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
								if (m.get("ATTR5") != null && !"".equals(String.valueOf(m.get("ATTR5")))) {
									for (String s : String.valueOf(m.get("ATTR5")).split(",")) {
										if (iM2Pos == 0)
											iM2Pos = getFaceSeq(sSealTypeOut, s, 2, "OUT");
										// setMaterialResultList_byStd(sSealTypeOut, s, iM2Pos, "OUT", iPIdx, fp);

										if ("[RESIN CARBON]".equals(s)) {
											// 표준재질에서 해당하는 재질을 설정
											setMaterialResultList_byStd(sSealTypeOut, s, iM2Pos, "OUT", iPIdx, fp);
										} else {
											if (iM2Pos == 2) {
												setMaterialResultListPrefer(material2OutRstList, sealRstList, "" + 2,
														iPIdx, 0, s, null, "OUT");
											} else {
												setMaterialResultListPrefer(material4OutRstList, sealRstList, "" + 4,
														iPIdx, 0, s, null, "OUT");
											}
										}

									}
								}

								iM4Pos = iM2Pos == 2 ? 4 : 2;
								if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
									for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
										// setMaterialResultList_byStd(sSealTypeOut, s, iM4Pos, "OUT", iPIdx, fp);

										if ("[RESIN CARBON]".equals(s)) {
											// 표준재질에서 해당하는 재질을 설정
											setMaterialResultList_byStd(sSealTypeOut, s, iM4Pos, "OUT", iPIdx, fp);
										} else {
											if (iM2Pos == 2) {
												setMaterialResultListPrefer(material2OutRstList, sealRstList, "" + 2,
														iPIdx, 0, s, null, "OUT");
											} else {
												setMaterialResultListPrefer(material4OutRstList, sealRstList, "" + 4,
														iPIdx, 0, s, null, "OUT");
											}
										}

									}
								}
							}
						}
					}
				}

				// [C7-8] H2SO4 농도에 따른
				// H2SO4 = SULFURIC ACID
				if (isProduct("SULFURIC ACID", saProductGroup, saProduct)) {

					item.put("H2SO4_CONT", getProductCont(item, "SULFURIC ACID", "%"));
					List<Map<String, Object>> rComC78List = null;
					List<Map<String, Object>> rComC781List = rBMapper.selectRuleComListC7801(item);

					if (rComC781List.size() > 0) {
						rComC78List = rComC781List;
					} else {
						rComC78List = rBMapper.selectRuleComListC7802(item);
					}

					if (rComC78List != null && rComC78List.size() > 0) {
						HashMap<String, Object> m = (HashMap<String, Object>) rComC781List.get(0);

						// 재질 설정
						// Material 1 : Metal 1st
						if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
							for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
								setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, s, null,
										"IN");

								// Outboard 재질
								if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
									setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0, s,
											null, "OUT");
								}
							}
						}

						// Material 2 : face 2nd
						if (m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
							for (String s : String.valueOf(m.get("ATTR7")).split(",")) {
								setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, 0, s, null,
										"IN");

								// Outboard 재질
								if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
									setMaterialResultListPrefer(material2OutRstList, sealRstList, "1", iPIdx, 0, s,
											null, "OUT");
								}
							}
						}

						// Material 3 : Gasket 3nd
						if (m.get("ATTR9") != null && !"".equals(String.valueOf(m.get("ATTR9")))) {
							for (String s : String.valueOf(m.get("ATTR9")).split(",")) {
								setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, s, null,
										"IN");

								// Outboard 재질
								if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
									setMaterialResultListPrefer(material3OutRstList, sealRstList, "1", iPIdx, 0, s,
											null, "OUT");
								}
							}
						}

						// Material 4 : face 4th
						if (m.get("ATTR8") != null && !"".equals(String.valueOf(m.get("ATTR8")))) {
							for (String s : String.valueOf(m.get("ATTR8")).split(",")) {
								setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, 0, s, null,
										"IN");

								// Outboard 재질
								if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
									setMaterialResultListPrefer(material4OutRstList, sealRstList, "1", iPIdx, 0, s,
											null, "OUT");
								}
							}
						}

						// 추가정보가 있을 경우
						if (!"".equals(StringUtil.get(m.get("ATTR10")))) {
							ptm.clear();
							ptm.put("MCD", "C7803");
							ptm.put("ATTR1", m.get("ATTR10"));
							List<Map<String, Object>> rComList = getRuleComListType1(ptm);
							if (rComList.size() > 0) {
								setResultNoteList(noteRstList, iPIdx, String.valueOf(rComList.get(0).get("ATTR2")));
							}
						}

						// 중간진행사항
						setResultProcList(procRstList, iPIdx, "[C7-8] 유체 SULFURIC ACID 기준 재질 적용 ");
					}
				}

				// H2S = HYDROGEN SULFIDE
//				if(isProduct("HYDROGEN SULFIDE",saProductGroup,saProduct)) {
//					//Seal Face 
//				}

				// [B1-2] Styrene Monomer Applications
				// Product Group = Styrene Monomer => SM
				// 적용 RULE 기준 : B1201
//				if(isProduct("SM",saProductGroup, saProduct)) {
//					ptm.clear();
//					ptm.put("MCD", "B1201");
//					ptm.put("SCD", "B1201010"); 
//					List<Map<String,Object>> rComList = getRuleComListType1(ptm); 
//					
//					if (!rComList.isEmpty()) {
//						
//						String sSealTypeTmp = StringUtil.get(rComList.get(0).get("ATTR1")); //Seal Type
//						
//						//Material
//						if(sSealTypeTmp.equals(sSealTypeIn)) {
//							for(String s : String.valueOf(rComList.get(0).get("ATTR2")).split(",")) {
//								for(String s_ : s.split("/")) {
//									String[] s__ = s_.split(" ");
//									if (s__.length> 0 ) setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, s__[0].trim(), null, "IN");
//									if (s__.length> 1 ) setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, 0, s__[1].trim(), null, "IN");
//									if (s__.length> 2 ) setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, s__[2].trim(), null, "IN");
//									if (s__.length> 3 ) setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, 0, s__[3].trim(), null, "IN");
//								}
//							}
//							// 중간진행사항
//							setResultProcList(procRstList, iPIdx, "[B1-2] Styrene Monomer Applications Rule 적용 : Material Inner");
//						}
//						
//						
//						if(sSealTypeTmp.equals(sSealTypeOut)) {
//							for(String s : String.valueOf(rComList.get(0).get("ATTR2")).split(",")) {
//								for(String s_ : s.split("/")) {
//									String[] s__ = s_.split(" ");
//									if("2".equals(sArrangement) || "3".equals(sArrangement)) {
//										if (s__.length> 0 ) setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0, s__[0].trim(), null, "OUT");
//										if (s__.length> 1 ) setMaterialResultListPrefer(material2OutRstList, sealRstList, "2", iPIdx, 0, s__[1].trim(), null, "OUT");
//										if (s__.length> 2 ) setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, s__[2].trim(), null, "OUT");
//										if (s__.length> 3 ) setMaterialResultListPrefer(material4OutRstList, sealRstList, "4", iPIdx, 0, s__[3].trim(), null, "OUT");
//									}
//								}
//							}
//							// 중간진행사항
//							setResultProcList(procRstList, iPIdx, "[B1-2] Styrene Monomer Applications Rule 적용 : Material Outer");
//						}
//					}
//				}
//				
//				System.out.println("----- [B1-2] Styrene Monomer Applications end ----");
//				System.out.println("----- material3RstList ---- : " + material3RstList);
//				System.out.println("----- material3OutRstList ---- : " + material3OutRstList);

				// [B1-14] Crude oil with water cut
				if (isProduct("CRUDE OIL", saProductGroup, saProduct)
						&& isProduct("[WATER-BASE]", saProductGroup, saProduct)) {
					ptm.clear();
					ptm.put("WATER_CONT", getProductCont(item, "WATER", "%"));
					List<Map<String, Object>> list = rBMapper.selectRuleComListB11401(ptm);
					if (list.size() > 0) {
						Map<String, Object> data = list.get(0);

						// Material 1 : Metal
						for (String s : StringUtil.get(data.get("ATTR6")).split(",")) {
							setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, s, null, "IN");

							if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
								setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0, s, null,
										"OUT");
							}

							// 중간진행사항
							setResultProcList(procRstList, iPIdx, "Crude oil w/ Water Cut - Metal 기준 적용");
						}

					}
				}

				// [B1-6] HF alkylation 유체 사용 시
				// Step1에서 refinery 서비스 선택 후 Equipment가 HF Alkylation Unit인 Case
//				if ( "Z060385".equals( String.valueOf(item.get("EQUIPMENT")) ) ) {
//					ptm.clear();
//					ptm.put("MCD", "B1601");
//					ptm.put("SCD", "B1601010"); 
//					List<Map<String,Object>> rComList = getRuleComListType1(ptm); 
//					
//					if (!rComList.isEmpty()) {
//						
//						String sSealTypeTmp = StringUtil.get(rComList.get(0).get("ATTR1")); //Seal Type
//						
//						if(sSealTypeTmp.equals(sSealTypeIn)) {
//							//Material
//							for(String s : String.valueOf(rComList.get(0).get("ATTR2")).split(",")) {
//								for(String s_ : s.split("/")) {
//									String[] s__ = s_.split(" ");
//									if (s__.length> 0 ) setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, s__[0].trim(), null);
//									if (s__.length> 1 ) setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, s__[1].trim(), null);
//									if (s__.length> 2 ) setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, s__[2].trim(), null);
//									if (s__.length> 3 ) setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, s__[3].trim(), null);
//								}
//							}
//							
//							// 중간진행사항
//							setResultProcList(procRstList, iPIdx, "[B1-6] HF alkylation Fluid 적용 : Material Inner");
//						}
//						
//						if(sSealTypeTmp.equals(sSealTypeOut)) {
//							//Material
//							for(String s : String.valueOf(rComList.get(0).get("ATTR2")).split(",")) {
//								for(String s_ : s.split("/")) {
//									String[] s__ = s_.split(" ");
//									if("2".equals(sArrangement) || "3".equals(sArrangement)) {
//										if (s__.length> 0 ) setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, s__[0].trim(), null);
//										if (s__.length> 1 ) setMaterialResultListPrefer(material2OutRstList, sealRstList, "2", iPIdx, s__[1].trim(), null);
//										if (s__.length> 2 ) setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, s__[2].trim(), null);
//										if (s__.length> 3 ) setMaterialResultListPrefer(material4OutRstList, sealRstList, "4", iPIdx, s__[3].trim(), null);
//									}
//								}
//							}
//							
//							// 중간진행사항
//							setResultProcList(procRstList, iPIdx, "[B1-6] HF alkylation Fluid 적용 : Material Outer");
//						}
//					}
//				}
//				
//				System.out.println("----- [B1-13] [B1-6] HF alkylation 유체 사용 시  end ----");
//				System.out.println("----- material3RstList ---- : " + material3RstList);
//				System.out.println("----- material3OutRstList ---- : " + material3OutRstList);

//				
//				System.out.println("----- 극저온 서비스 start ----");
//				System.out.println("----- material3RstList ---- : " + material3RstList);
//				System.out.println("----- material3OutRstList ---- : " + material3OutRstList);

//				// [B1-8] 극저온 서비스
//				ptm.clear();
//				ptm.put("ATTR6", sSealType);
//				ptm.put("TEMP_MIN", item.get("TEMP_MIN"));
//				ptm.put("TEMP_MAX", item.get("TEMP_MAX"));
//				ptm.put("SEAL_CHAM_MIN", item.get("SEAL_CHAM_MIN"));
//				ptm.put("SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX"));
//				
//				String sPumpTypeG = mLService.getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")),_pumpTypeGroupInfo,null);
//				String sEquipmentType = "";
//				if ("OH1".equals(sPumpTypeG) ||
//						"OH2".equals(sPumpTypeG) ||
//						"BB".equals(sPumpTypeG)) {
//					sEquipmentType = "Z120020"; //H
//				}else {
//					if (!"VS4".equals(sPumpTypeG)) {
//						sEquipmentType = "Z120010"; //V
//					}else {
//						sEquipmentType = "Z120020"; //H
//					}
//				}
//				ptm.put("EQUIPMENT_TYPE", sEquipmentType);
//						
//				List<Map<String,Object>> rComB1_8List = rBMapper.selectRuleComListB1801(ptm); 
//				
//				if (!rComB1_8List.isEmpty()) {
//					for(Map<String,Object> m : rComB1_8List) {
//						
//						// Material
//						if(m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
//							//Material
//							for(String s : String.valueOf(m.get("ATTR7")).split(",")) {
//								String[] s_ = s.split("/");
//								String[] s__ = s_[0].split(" ");
//								if (s__.length> 0 ) setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, s__[0].trim(), null);
//								if (s__.length> 1 ) setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, s__[1].trim(), null);
//								if (s__.length> 2 ) setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, s__[2].trim(), null);
//								if (s__.length> 3 ) setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, s__[3].trim(), null);
//								
//								if(s_.length > 1) {
//									s__ = s_[1].split(" ");
//									if (s__.length> 0 ) setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, s__[0].trim(), null);
//									if (s__.length> 1 ) setMaterialResultListPrefer(material2OutRstList, sealRstList, "2", iPIdx, s__[1].trim(), null);
//									if (s__.length> 2 ) setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, s__[2].trim(), null);
//									if (s__.length> 3 ) setMaterialResultListPrefer(material4OutRstList, sealRstList, "4", iPIdx, s__[3].trim(), null);
//								}
//							}
//						}
//							
//						//추가정보
//						if (m.get("ATTR9") != null ) {
//							ptm.clear();
//							ptm.put("MCD", "B1802"); // B4 추가정보
//							ptm.put("ATTR1",m.get("ATTR9")); // 추가정보 구분
//							List<Map<String,Object>> rComList2 = rBMapper.selectRuleComListType1(ptm);
//							for(Map<String,Object> m2 : rComList2) {
//								setResultNoteList(noteRstList, iPIdx, "[B1-8] Cryogenic Applications - " +String.valueOf(m2.get("ATTR2")));
//							}
//						}
//						
//						// 중간진행사항
//						setResultProcList(procRstList, iPIdx, "[B1-8] Cryogenic Applications Rule 적용");
//						
//					}
//				}
//				
//				System.out.println("----- 극저온 서비스 end ----");
//				System.out.println("----- material3RstList ---- : " + material3RstList);
//				System.out.println("----- material3OutRstList ---- : " + material3OutRstList);

				// [C7-9] Sulfur 일 경우
				if (isProduct("SULFUR", saProductGroup, saProduct)) {
					// Molten Sulfur 또는 Sulfur 함유 & Product 비중 1.5 이상일 경우
					// BXRH, HXCU (Gland, Sleeve : 316SS), Plan 02/62

					if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 1.5
							|| NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) >= 1.5
							|| NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) >= 1.5) {

						setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, "AT", null, "IN");
						setMaterialResultListPrefer(material2RstList, sealRstList, "2", iPIdx, 0, "SL", null, "IN");
						setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, "QF", null, "IN");
						setMaterialResultListPrefer(material4RstList, sealRstList, "4", iPIdx, 0, "RY", null, "IN");

						if (NumberUtil.toInt(sArrangement) >= 2) {
							setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0, "AT", null,
									"OUT");
							setMaterialResultListPrefer(material2OutRstList, sealRstList, "2", iPIdx, 0, "SL", null,
									"OUT");
							setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0, "QF", null,
									"OUT");
							setMaterialResultListPrefer(material4OutRstList, sealRstList, "4", iPIdx, 0, "RY", null,
									"OUT");
						}
						// Note
						setResultNoteList(noteRstList, iPIdx, "Gland, Sleeve : 316SS");
						// 중간진행사항
						setResultProcList(procRstList, iPIdx, "[C7-9] Sulfur 기준 적용 - 재질 : H X C U ");
					} else {
						// * Type A (Pusher) : Viton은 100℃ 까지, 그 이상의 온도에는 FFKM
						// * Type B or C (Bellows) : BX seal 재질코드 K---
						// BXHH seal 재질코드 G---(2 wt% 미만), H---(2 wt% 이상)
						if ("A".equals(StringUtil.get(item.get("ABC_TYPE")))) {
							if (NumberUtil.toDouble(item.get("TEMP_NOR")) <= 100
									&& NumberUtil.toDouble(item.get("TEMP_MIN")) <= 100
									&& NumberUtil.toDouble(item.get("TEMP_MAX")) <= 100) {

								setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, "[FKM]", null,
										"IN");
								if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
									setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0,
											"[FKM]", null, "OUT");
								}
							} else {
								setMaterialResultListPrefer(material3RstList, sealRstList, "3", iPIdx, 0, "[FFKM]",
										null, "IN");
								if (NumberUtil.toInt(sArrangement) == 2) { // A3인 경우 유체특성에 따른 재질 적용X
									setMaterialResultListPrefer(material3OutRstList, sealRstList, "3", iPIdx, 0,
											"[FFKM]", null, "OUT");
								}
							}

						} else { // Type B or C
							if (sSealTypeIn.startsWith("BXHH")) {
								if (getProductCont(item, "SULFUR", "%") < 2) {
									setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, "AM",
											null, "IN");
								} else {
									setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, "AT",
											null, "IN");
								}
							} else if (sSealTypeIn.startsWith("BX")) {
								setMaterialResultListPrefer(material1RstList, sealRstList, "1", iPIdx, 0, "CX", null,
										"IN");
							}

							if (sSealTypeOut.startsWith("BXHH")) {
								if (getProductCont(item, "SULFUR", "%") < 2) {
									setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0, "AM",
											null, "OUT");
								} else {
									setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0, "AT",
											null, "OUT");
								}
							} else if (sSealTypeIn.startsWith("BX")) {
								setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iPIdx, 0, "CX", null,
										"IN");
							}
						}
					}
				}

				// ----------------------------------
				// 점도 체크 로직
				// A1, A2 인 경우 , A3인경우 점도체크 X
				// 10cp 까지 레진카본 가능 - 로직적용 불필요
				// 20cp 까지는 RY 가능
				// 20cp 넘어가면 sic 가능
				// ----------------------------------
				// 이 단계까지 추가된 재질에 대항여 점도 체크

				// if(NumberUtil.toInt(item.get("ARRANGEMENT")) <= 2) {
				// System.out.println(seal_addInfo.get("ARRANGEMENT"));

				// 결과마다 Arrangement가 다를 수 있어 Seal 정보에서 Arrangement를 가져와서 체크한다.
				if (NumberUtil.toInt(sArrangement) <= 2) {
					if (NumberUtil.toDouble(item.get("VISC_MAX")) > 10) {
						List<Map<String, Object>> materialList_tmp = new ArrayList<Map<String, Object>>();
						for (int i = 1; i <= 8; i++) {
							if (i == 1 || 1 == 3 || 1 == 5 || 1 == 7) {
								continue;
							} else if (i == 2) {
								materialList_tmp = material2RstList;
							} else if (i == 4) {
								materialList_tmp = material4RstList;
							} else if (i == 6) {
								materialList_tmp = material2OutRstList;
							} else if (i == 8) {
								materialList_tmp = material4OutRstList;
							}

							for (Iterator<Map<String, Object>> iterator = materialList_tmp.iterator(); iterator
									.hasNext();) {
								Map<String, Object> m = iterator.next();
								Map<String, Object> addInfo = m.get("ADD_INFO") == null ? new HashMap<String, Object>()
										: (Map<String, Object>) m.get("ADD_INFO");

								if (iPIdx != NumberUtil.toInt(m.get("P_IDX")))
									continue;

								if (NumberUtil.toDouble(item.get("VISC_MAX")) <= 20) {
									if ((StringUtil.get(addInfo.get("MTRL_CD"))).equals("[CARBON]")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("KR3")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("GE")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("AE")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("KI")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("OH")) {
										setResultProcList(procRstList, iPIdx,
												"재질 face-점도체크로 제외 : " + addInfo.get("MTRL_CD"));
										iterator.remove();
									}
								} else if (NumberUtil.toDouble(item.get("VISC_MAX")) > 20) {
									if ((StringUtil.get(addInfo.get("MTRL_CD"))).equals("[CARBON]")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("KR3")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("GE")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("AE")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("KI")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("OH")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("AP")
											|| (StringUtil.get(addInfo.get("MTRL_CD"))).trim().equals("RY")) {
										setResultProcList(procRstList, iPIdx,
												"재질 face-점도체크로 제외 : " + addInfo.get("MTRL_CD"));
										iterator.remove();
									}
								}
							}

						}
					}
				}

				// ---------------------------------------------------------------------
				// Type C 인 Seal 일 경우 가스켓은 C (QF) 이외 재질은 모두 제외시킨다.
				// ---------------------------------------------------------------------
				if ("C".equals(sSealIn_ABC_Type)) {
					for (Iterator<Map<String, Object>> iterator = material3RstList.iterator(); iterator.hasNext();) {
						Map<String, Object> m = iterator.next();
						Map<String, Object> addInfo = getMapMapData(m, "ADD_INFO");
						if (!"C".equals(StringUtil.get(m.get("P_VAL")))) {
							iterator.remove();
						}
					}
					setResultProcList(procRstList, iPIdx, "Type C Seal에 따라 Inboard 가스켓은 C 만 유지함.");
				}

				if ("C".equals(sSealOut_ABC_Type) && NumberUtil.toInt(sArrangement) >= 2) {
					for (Iterator<Map<String, Object>> iterator = material3OutRstList.iterator(); iterator.hasNext();) {
						Map<String, Object> m = iterator.next();
						Map<String, Object> addInfo = getMapMapData(m, "ADD_INFO");
						if (!"C".equals(StringUtil.get(m.get("P_VAL")))) {
							iterator.remove();
						}
					}
					setResultProcList(procRstList, iPIdx, "Type C Seal에 따라 Outboard 가스켓은 C 만 유지함.");
				}

				// --------------
				// C1 Grade 체크
				// --------------

				step_c1_chk(item, fp);

				// ------------------
				// C9 재질 조회
				// ------------------

				System.out.println("----- C9 재질선정 Start ----");

				// 재질정보 유무에 따라 없을 경우 C9 표준재질 적용
//				boolean bIsMaterial1 = false,bIsMaterial2 = false, bIsMaterial3 = false,bIsMaterial4 = false;
//				boolean bIsMaterial1Out = false,bIsMaterial2Out = false,bIsMaterial3Out = false,bIsMaterial4Out = false;
//				if (material1RstList.size() ==0 ) bIsMaterial1 = true;
//				if (material2RstList.size() ==0 ) bIsMaterial2 = true;
//				if (material3RstList.size() ==0 ) bIsMaterial3 = true;
//				if (material4RstList.size() ==0 ) bIsMaterial4 = true;
//				if (material1OutRstList.size() ==0 ) bIsMaterial1Out = true;
//				if (material2OutRstList.size() ==0 ) bIsMaterial2Out = true;
//				if (material3OutRstList.size() ==0 ) bIsMaterial3Out = true;
//				if (material4OutRstList.size() ==0 ) bIsMaterial4Out = true;

				// 재질이 있어도 무조건 C9 표준재질을 설정한다.
				boolean bIsMaterial1 = true, bIsMaterial2 = true, bIsMaterial3 = true, bIsMaterial4 = true;
				boolean bIsMaterial1Out = true, bIsMaterial2Out = true, bIsMaterial3Out = true, bIsMaterial4Out = true;

				// C9 재질 조회
				Map<String, Object> param = new HashMap<String, Object>();

				if ("__RO_CHK_1__".equals(sSealTypeIn) || "__RO_CHK_2__".equals(sSealTypeIn)) {
					// param.put("SEAL_TYPE", "RO"); // RO 그래프 체크를 위해 RO 재질로 설정한다.
					sSealTypeIn = "RO";
				}

				param.put("SEAL_TYPE", sSealTypeIn);

				List<Map<String, Object>> ruleC9List = rBMapper.selectRuleC9_1(param); // C9 표준재질 조회
				if (!ruleC9List.isEmpty()) {
					for (Map<String, Object> c9_map : ruleC9List) {
						boolean bIsSealType = false;
						for (String sSealTypeDiv : StringUtil.get(c9_map.get("SEAL_TYPE")).split(",")) {

							// System.out.println("sSealTypeDiv : " + sSealTypeDiv + " : sSealTypeIn : " +
							// sSealTypeIn);

							if ((sSealTypeDiv.trim()).equals(sSealTypeIn)) {
								bIsSealType = true;
								break;
							}
						}

						// 해당 Seal 정보일 경우
						if (bIsSealType) {
							// 재질 정보 추가

							HashMap<String, Object> m_t = new HashMap<String, Object>();
							m_t.put("C9_YN", "Y"); // C9 Rule 표준재질 유무
							m_t.put("SEAL_GB", c9_map.get("SEAL_GB")); // Seal 구분 : Bellows, Pusher - Mutiple Springs
																		// ...
							m_t.put("S_MTRL", c9_map.get("S_MTRL")); // Small Part Material Code
							m_t.put("S_MTRL_YN", c9_map.get("S_MTRL_YN")); // Small Part 접액여부 : Y/N
							m_t.put("GS_MTRL", c9_map.get("GS_MTRL")); // Gland & Sleeve Material For Bellows Meterial

							// 재질 1~4
							for (int i = 1; i <= 4; i++) {
								String sMtrlCds = String.valueOf(c9_map.get("MTRL_CD_IN_M" + i));

								boolean bIsVIscChk = false;

								// if(NumberUtil.toInt(item.get("ARRANGEMENT")) <= 2) {
								// A2 이하에서 점도체크
								if (NumberUtil.toInt(sArrangement) <= 2) {
									if (i == 2 || i == 4) {
										String sMtrlCds_tmp = ""; // 새로정의할 재질정보
										// A1, A2 인 경우
										// 10cp 까지 레진카본 가능
										// 20cp 까지는 RY 가능
										// 20cp 넘어가면 sic 가능
										if (NumberUtil.toDouble(item.get("VISC_MAX")) <= 10) {
											sMtrlCds_tmp = sMtrlCds;
										} else if (NumberUtil.toDouble(item.get("VISC_MAX")) <= 20) {
											bIsVIscChk = true;
											// 레진카본이 있을 경우 제거
											for (String sMtrlTmp : sMtrlCds.split(",")) {
												if ((sMtrlTmp.trim().equals("[CARBON]") || sMtrlTmp.trim().equals("KR3")
														|| sMtrlTmp.trim().equals("GE") || sMtrlTmp.trim().equals("AE")
														|| sMtrlTmp.trim().equals("KI") || sMtrlTmp.trim().equals("OH")

												)) {
													setResultProcList(procRstList, 0,
															"표준재질-점도체크(10초과)로 제외 : " + sMtrlTmp);
												} else {
													if (sMtrlCds_tmp.length() > 0) {
														sMtrlCds_tmp = sMtrlCds_tmp + "," + sMtrlTmp;
													} else {
														sMtrlCds_tmp = sMtrlCds_tmp + sMtrlTmp;
													}
												}
											}
										} else if (NumberUtil.toDouble(item.get("VISC_MAX")) > 20) {
											bIsVIscChk = true;
											// 레진카본, antimony 카본을 제거
											for (String sMtrlTmp : sMtrlCds.split(",")) {
												if ((sMtrlTmp.trim().equals("[CARBON]") || sMtrlTmp.trim().equals("KR3")
														|| sMtrlTmp.trim().equals("GE") || sMtrlTmp.trim().equals("AE")
														|| sMtrlTmp.trim().equals("KI") || sMtrlTmp.trim().equals("OH")
														|| sMtrlTmp.trim().equals("RY")
														|| sMtrlTmp.trim().equals("AP"))) {
													setResultProcList(procRstList, 0,
															"표준재질-점도체크(20초과)로 제외 : " + sMtrlTmp);
												} else {
													if (sMtrlCds_tmp.length() > 0) {
														sMtrlCds_tmp = sMtrlCds_tmp + "," + sMtrlTmp;
													} else {
														sMtrlCds_tmp = sMtrlCds_tmp + sMtrlTmp;
													}
												}
											}
										}
										sMtrlCds = sMtrlCds_tmp;

										if (bIsVIscChk && "".equals(sMtrlCds)) {
											if (NumberUtil.toDouble(item.get("VISC_MAX")) <= 20) {
												sMtrlCds = "RY"; // Antimony Carbon 적용
												setResultProcList(procRstList, 0, "점도 20이하 표준재질이 없어 RY 적용");
											} else {
												sMtrlCds = "SL"; // SIC 적용
												setResultProcList(procRstList, 0, "점도 20이하 표준재질이 없어 SL 적용");
											}
										}
									}
								}

								for (String sVal : sMtrlCds.split(",")) {
									HashMap<String, Object> m_i = (HashMap<String, Object>) m_t.clone();
									if (i == 1 && bIsMaterial1) {
										setMaterialResultListPrefer(material1RstList, sealRstList, "" + i, iPIdx, 0,
												sVal, m_i, "IN");
									}
									if (i == 2 && bIsMaterial2) {
										setMaterialResultListPrefer(material2RstList, sealRstList, "" + i, iPIdx, 0,
												sVal, m_i, "IN");
									}
									if (i == 3 && bIsMaterial3) {
										setMaterialResultListPrefer(material3RstList, sealRstList, "" + i, iPIdx, 0,
												sVal, m_i, "IN");
									}
									if (i == 4 && bIsMaterial4) {
										setMaterialResultListPrefer(material4RstList, sealRstList, "" + i, iPIdx, 0,
												sVal, m_i, "IN");
									}
								}
							}
							break;
						}
					}
				}

				// Arrangement가 1이 아니고 RO check 상태일경우 => 필요없는 코드?
				if (("__RO_CHK_1__".equals(sSealTypeOut) || "__RO_CHK_2__".equals(sSealTypeOut))
						&& !"1".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
					// param.put("SEAL_TYPE", "RO"); // RO 그래프 체크를 위해 RO 재질로 설정한다.
					sSealTypeOut = "RO";
				}

				// Outboard Seal이 있을 경우
				if (!"".equals(sSealTypeOut)) {
					param = new HashMap<String, Object>();
					param.put("SEAL_TYPE", sSealTypeOut);

					ruleC9List = rBMapper.selectRuleC9_1(param); // C9 표준재질 조회
					if (!ruleC9List.isEmpty()) {
						for (Map<String, Object> c9_map : ruleC9List) {
							boolean bIsSealType = false;
							for (String sSealTypeDiv : StringUtil.get(c9_map.get("SEAL_TYPE")).split(",")) {

								// System.out.println("sSealTypeDiv : " + sSealTypeDiv + " : sSealTypeOut : " +
								// sSealTypeOut);

								if ((sSealTypeDiv.trim()).equals(sSealTypeOut.trim())) {
									bIsSealType = true;
									break;
								}
							}

							// 해당 Seal 정보일 경우
							if (bIsSealType) {
								// 재질 정보 추가

								HashMap<String, Object> m_t = new HashMap<String, Object>();
								m_t.put("C9_YN", "Y"); // C9 Rule 표준재질 유무
								m_t.put("SEAL_GB", c9_map.get("SEAL_GB")); // Seal 구분 : Bellows, Pusher - Mutiple
																			// Springs ...
								m_t.put("S_MTRL", c9_map.get("S_MTRL")); // Small Part Material Code
								m_t.put("S_MTRL_YN", c9_map.get("S_MTRL_YN")); // Small Part 접액여부 : Y/N
								m_t.put("GS_MTRL", c9_map.get("GS_MTRL")); // Gland & Sleeve Material For Bellows
																			// Meterial

								// 재질 1~4
								for (int i = 1; i <= 4; i++) {

									boolean bIsVIscChk = false;
									String sMtrlCds = String.valueOf(c9_map.get("MTRL_CD_OUT_M" + i));

									// if(NumberUtil.toInt(item.get("ARRANGEMENT")) <= 2) {\
									// A2 이하에서 점도체크
									if (NumberUtil.toInt(sArrangement) <= 2) {
										if (i == 2 || i == 4) {
											String sMtrlCds_tmp = ""; // 새로정의할 재질정보
											// 10cp 까지 레진카본 가능
											// 20cp 까지는 RY 가능
											// 20cp 넘어가면 sic 가능
											if (NumberUtil.toDouble(item.get("VISC_MAX")) <= 10) {
												sMtrlCds_tmp = sMtrlCds;
											} else if (NumberUtil.toDouble(item.get("VISC_MAX")) <= 20) {
												bIsVIscChk = true;
												// 레진카본이 있을 경우 제거
												for (String sMtrlTmp : sMtrlCds.split(",")) {
													if ((sMtrlTmp.trim().equals("[CARBON]")
															|| sMtrlTmp.trim().equals("KR3")
															|| sMtrlTmp.trim().equals("GE")
															|| sMtrlTmp.trim().equals("AE")
															|| sMtrlTmp.trim().equals("KI")
															|| sMtrlTmp.trim().equals("OH"))) {
														setResultProcList(procRstList, 0,
																"표준재질-점도체크(10초과)로 제외 : " + sMtrlTmp);
													} else {
														if (sMtrlCds_tmp.length() > 0) {
															sMtrlCds_tmp = sMtrlCds_tmp + "," + sMtrlTmp;
														} else {
															sMtrlCds_tmp = sMtrlCds_tmp + sMtrlTmp;
														}
													}
												}
											} else if (NumberUtil.toDouble(item.get("VISC_MAX")) > 20) {
												bIsVIscChk = true;
												// 레진카본, antimony 카본을 제거
												for (String sMtrlTmp : sMtrlCds.split(",")) {
													if ((sMtrlTmp.trim().equals("[CARBON]")
															|| sMtrlTmp.trim().equals("KR3")
															|| sMtrlTmp.trim().equals("GE")
															|| sMtrlTmp.trim().equals("AE")
															|| sMtrlTmp.trim().equals("KI")
															|| sMtrlTmp.trim().equals("OH")
															|| sMtrlTmp.trim().equals("RY")
															|| sMtrlTmp.trim().equals("AP"))) {
														setResultProcList(procRstList, 0,
																"표준재질-점도체크(20초과)로 제외 : " + sMtrlTmp);
													} else {
														if (sMtrlCds_tmp.length() > 0) {
															sMtrlCds_tmp = sMtrlCds_tmp + "," + sMtrlTmp;
														} else {
															sMtrlCds_tmp = sMtrlCds_tmp + sMtrlTmp;
														}
													}
												}
											}
											sMtrlCds = sMtrlCds_tmp;

											if (bIsVIscChk && "".equals(sMtrlCds)) {
												if (NumberUtil.toDouble(item.get("VISC_MAX")) <= 20) {
													sMtrlCds = "RY"; // Antimony Carbon 적용
													setResultProcList(procRstList, 0, "점도 20이하 표준재질이 없어 RY 적용");
												} else {
													sMtrlCds = "SL"; // SIC 적용
													setResultProcList(procRstList, 0, "점도 20이하 표준재질이 없어 SL 적용");
												}
											}
										}
									}

									for (String sVal : sMtrlCds.split(",")) {
										HashMap<String, Object> m_o = (HashMap<String, Object>) m_t.clone();
										if (i == 1 && bIsMaterial1Out) {
											setMaterialResultListPrefer(material1OutRstList, sealRstList, "" + i, iPIdx,
													0, sVal, m_o, "OUT");
										}
										if (i == 2 && bIsMaterial2Out) {
											setMaterialResultListPrefer(material2OutRstList, sealRstList, "" + i, iPIdx,
													0, sVal, m_o, "OUT");
										}
										if (i == 3 && bIsMaterial3Out) {
											setMaterialResultListPrefer(material3OutRstList, sealRstList, "" + i, iPIdx,
													0, sVal, m_o, "OUT");
										}
										if (i == 4 && bIsMaterial4Out) {
											setMaterialResultListPrefer(material4OutRstList, sealRstList, "" + i, iPIdx,
													0, sVal, m_o, "OUT");
										}
									}
								}
								break;
							}
						}
					}
				}

			}
		}

		System.out.println("----- C9재질선정 end ----");

	}

	private void step_c1_chk(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		for (int i = 0; i < 8; i++) {
			List<Map<String, Object>> mlist = null;
			String sMtrlType = null;
			if (i == 0) {
				mlist = material1RstList;
				sMtrlType = "1";
			} else if (i == 1) {
				mlist = material2RstList;
				sMtrlType = "2";
			} else if (i == 2) {
				mlist = material3RstList;
				sMtrlType = "3";
			} else if (i == 3) {
				mlist = material4RstList;
				sMtrlType = "2";
			} else if (i == 4) {
				mlist = material1OutRstList;
				sMtrlType = "1";
			} else if (i == 5) {
				mlist = material2OutRstList;
				sMtrlType = "2";
			} else if (i == 6) {
				mlist = material3OutRstList;
				sMtrlType = "3";
			} else if (i == 7) {
				mlist = material4OutRstList;
				sMtrlType = "2";
			}

			Map<String, Object> c1_map = new HashMap<String, Object>();
			Map<String, Object> mtrlAddInfo = null;

			// System.out.println("Grade search : " + sMtrlType);

			for (Map<String, Object> m : mlist) {
				if (m.get("ADD_INFO") == null)
					continue;
				mtrlAddInfo = (HashMap<String, Object>) m.get("ADD_INFO");

				// 체크가 되지 않은 항목에 관해 처리
				if (!"Y".equals(StringUtil.get(mtrlAddInfo.get("C1_CHK_YN")))) {

					for (String s : saProduct) { // product 별 체크

						String sProductMtrl_grade = "";

						// C1 Check 유체가 아닌경우 SKip
						if (!isC1CheckFluid(s))
							continue;

						// 재질코드별 복합정보를 가지고 있는 타입으로 인한 추가처리
						for (String sMtrlCdSub : getSepMtrlInfo(sMtrlType,
								StringUtil.get(mtrlAddInfo.get("MTRL_CD")))) {

							// Brine 일때 추가조건 적용
							String sApply = getBrineProduct(item, s, saProductGroup, saProduct);

							// Product별 Grade를 조회
							c1_map.clear();
							// c1_map.put("PRODUCT",s);
							c1_map.put("PRODUCT", sApply); // C1 Product에 적용 할 명을 일부로직 적용 후 반영
							c1_map.put("MTRL_TYPE", "M" + sMtrlType);
							// c1_map.put("MTRL_CD",getC1SearchMtrlCode(sMtrlType,""+mtrlAddInfo.get("MTRL_CD")));
							c1_map.put("MTRL_CD", getC1SearchMtrlCode(sMtrlType, sMtrlCdSub));
							c1_map.put("GB", getProductGb(item, s)); // Product 구분정보 (C1 조회용)
							c1_map.put("CONT", getProductCont(item, s, "%")); // Product 농도
							c1_map.put("TEMP_MIN", item.get("TEMP_MIN"));
							c1_map.put("TEMP_MAX", item.get("TEMP_MAX"));

							//System.out.println("c1_map : " + c1_map);

							String sGrade = getGrade(c1_map); // C1 Grade를 조회

							if ("".equals(sProductMtrl_grade)) {
								sProductMtrl_grade = sGrade;
							} else {
								if ("".equals(sGrade) || "X".equals(sGrade)) { // Grade가 없는 경우 최우선
									sProductMtrl_grade = "X";
									break;
								} else if ("B".equals(sGrade)) {
									if ("A".equals(sProductMtrl_grade)) {
										sProductMtrl_grade = "B";
									}
								}
							}
						}

						// mtrlAddInfo.put(s+"_GRADE", sGrade); // Grade Set
						mtrlAddInfo.put(s + "_GRADE", sProductMtrl_grade); // Grade Set
					}

				}

			}
		}

		// System.out.println("Grade 반영 후");
		// System.out.println("material1RstList : " + material1RstList);
		// System.out.println("material2RstList : " + material2RstList);
		// System.out.println("material3RstList : " + material3RstList);
		// System.out.println("material4RstList : " + material4RstList);
		// System.out.println("material3OutRstList : " + material3OutRstList);

		// 설정된 Grade 에 따라 처리

		// Arrangement 3 && Outboard인 경우 C1체크 Skip

		System.out.println("C1 Grade 에 따른 처리");
		Map<String, Object> mtrlAddInfo = null;
		Map<String, Object> sealAddInfo = null;
		Iterator iter = null;
		String sMaterialType = "";
		String sMaterialGb = "";
		String sIdxArrangement = ""; // arrangement
		
		for (int i = 0; i < 8; i++) {
			if (i == 0) {
				iter = material1RstList.iterator();
				sMaterialType = "Metal";
				sMaterialGb = "In 1";
			} else if (i == 1) {
				iter = material2RstList.iterator();
				sMaterialType = "R. Face";
				sMaterialGb = "In 2";
			} else if (i == 2) {
				iter = material3RstList.iterator();
				sMaterialType = "Gasket";
				sMaterialGb = "In 3";
			} else if (i == 3) {
				iter = material4RstList.iterator();
				sMaterialType = "S. Face";
				sMaterialGb = "In 4";
			} else if (i == 4) { // Outboard
				iter = material1OutRstList.iterator();
				sMaterialType = "Metal";
				sMaterialGb = "Out 1";
			} else if (i == 5) { // Outboard
				iter = material2OutRstList.iterator();
				sMaterialType = "R. Face";
				sMaterialGb = "Out 2";
			} else if (i == 6) { // Outboard
				iter = material3OutRstList.iterator();
				sMaterialType = "Gasket";
				sMaterialGb = "Out 3";
			} else if (i == 7) { // Outboard
				iter = material4OutRstList.iterator();
				sMaterialType = "S. Face";
				sMaterialGb = "Out 4";
			}

			// System.out.println("sMaterialType : " + sMaterialType);

			while (iter.hasNext()) {

				Map<String, Object> m = (HashMap<String, Object>) iter.next();

				// 재질 추가정보
				if (m.get("ADD_INFO") == null)
					continue;
				
				mtrlAddInfo = (HashMap<String, Object>) m.get("ADD_INFO");

				sIdxArrangement = "";
				// Seal Type 정보에서 Arrangement를 체크
				for (Map<String, Object> sm : sealRstList) {
					if (StringUtil.get(m.get("P_IDX")).equals(StringUtil.get(sm.get("P_IDX")))) {
						sealAddInfo = sm.get("ADD_INFO") == null ? new HashMap<String, Object>()
								: (HashMap<String, Object>) sm.get("ADD_INFO");
						sIdxArrangement = StringUtil.get(sealAddInfo.get("ARRANGEMENT"));
						break;
					}
				}

				// End User로 등록된 정보 Skip
				if (sealAddInfo!=null && "1".equals(StringUtil.get(sealAddInfo.get("R_TYPE")))) {
					continue;
				}

				// ------------------------------------------------
				// Arrangement 3 && Outboard인 경우 C1체크 Skip
				// ------------------------------------------------
				if ("3".equals(sIdxArrangement)) {
					if (i >= 4) { // Outboard Material 일 경우
						mtrlAddInfo.put("C1_CHK_YN", "Y"); // C1 체크 완료 유무
						continue;
					}
				}

				// System.out.println("Map Data : " + m);

				boolean bUse = true; // Grade에 따른 사용 유무
				boolean bNonGrade = true; // Grade 조회결과 유무 :
				String sProductDis1 = "";
				String sProductDis2 = "";
				// String sProductChkProduct = "";

				if (!"Y".equals(StringUtil.get(mtrlAddInfo.get("C1_CHK_YN")))) {

					// C1 Material에 없는 재질일 경우 우선선정가이드에서 선택된 재질은 Skip
					// - C9 표준재질에서 나온 재질은 없을 경우 Note 표시 후 Skip
					for (String s : saProduct) {

						// C1체크유체가 아닐경우 Skip
						if (!isC1CheckFluid(s)) {
							mtrlAddInfo.put("C1_CHK_YN", "Y"); // C1 체크 완료 유무
							continue;
						}

						// [Metal]
						// A,B 그레이드가 아닌 경우 제거
						// [R. Face]
						// A 그레이드가 아닌 경우 제거
						// [S. Face]
						// A 그레이드가 아닌 경우 제거
						// [Gaskets]
						// A,B 그레이드가 아닌 경우 제거

						if ("Metal".equals(sMaterialType)) {

							if ("X".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))
									|| "".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
								bUse = false;
								sProductDis1 += s + ",";
							}

							if ("NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
								sProductDis2 += s + ",";
							}

							if (!"NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
								bNonGrade = false;
							}
						} else if ("R. Face".equals(sMaterialType) || "S. Face".equals(sMaterialType)) {

							if ("B".equals(mtrlAddInfo.get(s + "_GRADE")) || "X".equals(mtrlAddInfo.get(s + "_GRADE"))
									|| "".equals(mtrlAddInfo.get(s + "_GRADE"))) {
								bUse = false;
								sProductDis1 += s + ",";
								// sProductChkProduct += s +",";
							}

							if ("NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
								sProductDis2 += s + ",";
							}
							if (!"NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
								bNonGrade = false;
							}
						} else if ("Gasket".equals(sMaterialType)) {

							if ("B".equals(mtrlAddInfo.get(s + "_GRADE")) || "X".equals(mtrlAddInfo.get(s + "_GRADE"))
									|| "".equals(mtrlAddInfo.get(s + "_GRADE"))) {
								bUse = false;
								sProductDis1 += s + ",";
								// sProductChkProduct += s +",";
							}

							if ("NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
								sProductDis2 += s + ",";
							}

							if (!"NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
								bNonGrade = false;
							}

						}

					}

					//System.out.println("MTRL_CD : " + mtrlAddInfo.get("MTRL_CD"));
					//System.out.println("bUse : " + bUse);
					//System.out.println("bNonGrade : " + bNonGrade);
					//System.out.println("StringUtil.get(mtrlAddInfo.get(\"C9_YN\")) : " + StringUtil.get(mtrlAddInfo.get("C9_YN")));

					if (!"".equals(sProductDis2)) {
						sProductDis2 = "정보없음 : " + sProductDis2.substring(0, sProductDis2.length() - 1);
					}

					// System.out.println(m.get("P_IDX") + " " + sMaterialType + " " +
					// m.get("P_VAL") + " " + mtrlAddInfo.get("MTRL_NM") + "bUse : " + bUse + "
					// bNonGrade : " + bNonGrade);

					if (!bUse && !bNonGrade) {

						if (sProductDis1.length() > 0) {
							sProductDis1 = sProductDis1.substring(0, sProductDis1.length() - 1);
						}

						setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")),
								"[Material Guide] Grade 체크로 삭제 - " + sMaterialGb + " : " + sProductDis1 + " : "
										+ m.get("P_VAL") + "  "
										+ (("Y".equals(StringUtil.get(m.get("P_VAL"))))
												? "" + mtrlAddInfo.get("MTRL_NM")
												: ""));

						iter.remove();
					} else {
						mtrlAddInfo.put("C1_CHK_YN", "Y"); // C1 체크 완료 유무
					}

					// C1 재질표에 없는 재질이고 C9 표준재질로 설정된 경우
					/*
					 * if (bNonGrade && "Y".equals(StringUtil.get(mtrlAddInfo.get("C9_YN")))) {
					 * setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")),
					 * "[C1] 정보없음-"+sMaterialGb+" : " + m.get("P_VAL") +
					 * "("+mtrlAddInfo.get("MTRL_CD") + ") : " + " "+ sProductDis2 ); }
					 */

				}

			}

		} // 설정된 Grade 에 따라 처리

		System.out.println("step_c1_chk end !");
	}

	private void step_c1_chk2(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		System.out.println("step_c1_chk2 start !");

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		// List<Map<String,Object>> material2RstList =
		// (List<Map<String,Object>>)fp.get("material2RstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		// List<Map<String,Object>> material4RstList =
		// (List<Map<String,Object>>)fp.get("material4RstList");
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");
		// List<Map<String,Object>> material2OutRstList =
		// (List<Map<String,Object>>)fp.get("material2OutRstList");
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");
		// List<Map<String,Object>> material4OutRstList =
		// (List<Map<String,Object>>)fp.get("material4OutRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		/*
		 * // Gasket 우선순위 후순위 체크? // 3rd digit, Gasket 재질 규칙 //1. 표준 재질 또는 우선 선정 재질이 A
		 * grade면 그대로 적용 //2. 표준 재질 또는 우선 선정 재질이 B grade고, 그 후순위 재질이 A grade이면 // Static
		 * Gasket에는 B grade인 표준 재질 또는 우선 선정 재질 적용, Dynamic Gasket에는 A grade인 후순위 재질 적용
		 * // -> 따라서 2종의 재질이 사용되므로 3rd digit에 ‘Y’로 표기 및 노트에 위 내용 표시 //3. 선택 가능한 재질이 B
		 * grade 뿐이고, A grade가 없다면, // Static Gasket에는 B grade 적용, Dynamic Gasket에는 별도의
		 * 재질로 수동 선정 필요 // -> 2종의 재질이 사용되므로 3rd digit에 ‘Y’로 표기 및 노트에 위 내용 표시
		 * 
		 * Iterator iter = null; Iterator iter_remove = null; Map<String,Object>
		 * mtrlAddInfo = null;
		 * 
		 * System.out.println("Gasket 후순위 체크 material3RstList : " + material3RstList);
		 * 
		 * List<String> idxList = new ArrayList<String>(); for(Map<String,Object> m3 :
		 * material3RstList) { if( !idxList.contains(StringUtil.get(m3.get("P_IDX")))){
		 * idxList.add(StringUtil.get(m3.get("P_IDX"))); } }
		 * 
		 * // Inner for(String sIdx : idxList) { iter = material3RstList.iterator();
		 * 
		 * String sS2 = ""; // 2번조건에 해당하는 하위 정보 String sS3 = ""; // 3번조건에 해당하는 하위 정보
		 * boolean bY = false;
		 * 
		 * for(String s : saProduct) { int istep=0; while(iter.hasNext()) {
		 * Map<String,Object> m = (HashMap<String,Object>)iter.next();
		 * 
		 * if (!sIdx.equals(StringUtil.get(m.get("P_IDX")))){ continue; }
		 * if(m.get("ADD_INFO")==null) continue;
		 * 
		 * mtrlAddInfo = (HashMap<String,Object>)m.get("ADD_INFO"); if (istep == 0) { if
		 * ("B".equals(StringUtil.get(mtrlAddInfo.get(s+"_GRADE")))){ bY = true; break;
		 * } } else { // 후순위 Grade 정보 if (
		 * "A".equals(StringUtil.get(mtrlAddInfo.get(s+"_GRADE")))){ sS2 +=
		 * StringUtil.get(m.get("P_VAL"))+","; }else { sS3 +=
		 * StringUtil.get(m.get("P_VAL"))+","; } } istep++; } }
		 * 
		 * //상위 재질이 A Grade가 아닌경우 //B Grade 가 우선순위 일 경우 if(bY) { iter_remove =
		 * material3RstList.iterator();
		 * 
		 * while(iter_remove.hasNext()) { Map<String,Object> m =
		 * (HashMap<String,Object>)iter_remove.next(); if
		 * (sIdx.equals(StringUtil.get(m.get("P_IDX")))){ iter_remove.remove(); } }
		 * 
		 * String sNote = ""; if("".equals(sS2)) { // 모두 B
		 * sNote="[Inboard] Static Gasket에는 B Grade 적용, Dynamic Gasket에는 별도의 재질로 수동 선정 필요"
		 * ; }else {
		 * sNote="[Inboard] Static Gasket에는 B Grade인 표준재질 또는 우선 선정 재질 적용, Dynamic Gasket에는 A Grade인 후순위 재질 적용"
		 * ; }
		 * 
		 * // Y 재질 추가 Map<String,Object> m = new HashMap<String,Object>();
		 * m.put("P_TYPE", "M3"); m.put("P_IDX", sIdx); m.put("P_SEQ",
		 * getMaxSeq(material3RstList,NumberUtil.toInt(sIdx))); m.put("P_VAL", "Y");
		 * //재질정보 표시 Digit
		 * 
		 * Map<String,Object> addInfo = new HashMap<String,Object>();
		 * addInfo.put("MTRL_NM",sNote); addInfo.put("MTRL_CD","-"); m.put("ADD_INFO",
		 * addInfo); // 추가정보 material3RstList.add(m);
		 * 
		 * setResultNoteList(noteRstList, NumberUtil.toInt(sIdx), sNote);
		 * 
		 * } }
		 * 
		 * 
		 * 
		 * 
		 * idxList = new ArrayList<String>(); for(Map<String,Object> m3 :
		 * material3OutRstList) { if(
		 * !idxList.contains(StringUtil.get(m3.get("P_IDX")))){
		 * idxList.add(StringUtil.get(m3.get("P_IDX"))); } }
		 * 
		 * // Outer for(String sIdx : idxList) { iter = material3OutRstList.iterator();
		 * 
		 * String sS2 = ""; // 2번조건에 해당하는 하위 정보 String sS3 = ""; // 3번조건에 해당하는 하위 정보
		 * boolean bY = false;
		 * 
		 * for(String s : saProduct) { int istep=0; while(iter.hasNext()) {
		 * Map<String,Object> m = (HashMap<String,Object>)iter.next();
		 * 
		 * if (!sIdx.equals(StringUtil.get(m.get("P_IDX")))){ continue; }
		 * if(m.get("ADD_INFO")==null) continue;
		 * 
		 * mtrlAddInfo = (HashMap<String,Object>)m.get("ADD_INFO"); if (istep ==0) { if
		 * ("B".equals(StringUtil.get(mtrlAddInfo.get(s+"_GRADE")))){ bY = true; break;
		 * } } else { // 후순위 Grade 정보 if (
		 * "A".equals(StringUtil.get(mtrlAddInfo.get(s+"_GRADE")))){ sS2 +=
		 * StringUtil.get(m.get("P_VAL"))+","; }else { sS3 +=
		 * StringUtil.get(m.get("P_VAL"))+","; } } istep++; } }
		 * 
		 * //상위 재질이 A Grade가 아닌경우 //B Grade 가 우선순위 일 경우 if(bY) { iter_remove =
		 * material3OutRstList.iterator();
		 * 
		 * while(iter_remove.hasNext()) { Map<String,Object> m =
		 * (HashMap<String,Object>)iter_remove.next(); if
		 * (sIdx.equals(StringUtil.get(m.get("P_IDX")))){ iter_remove.remove(); } }
		 * 
		 * String sNote = ""; if("".equals(sS2)) { // 모두 B
		 * sNote="[Outboard] Static Gasket에는 B Grade 적용, Dynamic Gasket에는 별도의 재질로 수동 선정 필요"
		 * ; }else {
		 * sNote="[Outboard] Static Gasket에는 B Grade인 표준재질 또는 우선 선정 재질 적용, Dynamic Gasket에는 A Grade인 후순위 재질 적용"
		 * ; }
		 * 
		 * // Y 재질 추가 Map<String,Object> m = new HashMap<String,Object>();
		 * m.put("P_TYPE", "M3"); m.put("P_IDX", sIdx); m.put("P_SEQ",
		 * getMaxSeq(material3OutRstList,NumberUtil.toInt(sIdx))); m.put("P_VAL", "Y");
		 * //재질정보 표시 Digit
		 * 
		 * Map<String,Object> addInfo = new HashMap<String,Object>();
		 * addInfo.put("MTRL_NM",sNote); addInfo.put("MTRL_CD","-"); m.put("ADD_INFO",
		 * addInfo); // 추가정보 material3OutRstList.add(m);
		 * 
		 * setResultNoteList(noteRstList, NumberUtil.toInt(sIdx), sNote);
		 * 
		 * } }
		 */
		// System.out.println("step_c1_chk2 - Gasket 후순위 체크 end !");

		// ---------------------------------------------
		// Small Part Maerial check
		// ---------------------------------------------
		/*
		 * - Small Part Material과 Small Part 접액 여부 Small Part Material은 4자리 재질코드의 1st
		 * digit의 재질과 같을 수도 있고, 다를 수도 있으며, 특정 재질로 지정되는 경우도 있습니다. 또한 접액 여부 즉 Product와
		 * 접촉하는 구조냐 아니냐에 따라서도 선정 방향이 달라질 수가 있어서 별도로 표시
		 * 
		 * a. Small Part Material 칸이 공란인 경우 : (Small Part 접액 여부와 무관) 별도로 고려할 사항 없음
		 * 
		 * b. Small Part Material이 지정되어 있으며, Small Part 접액 여부가 ‘N’인 경우 : 지정된 Small Part
		 * Material 표시 (Product와 접촉하지 않으므로 적합성 여부 판단 불필요함) (Metal 재질과는 별개이므로, 4자리 재질코드의
		 * 1st digit에는 반영 안됨. 별도 노트에 표시)
		 * 
		 * c. Small Part Material이 지정되어 있으며, Small Part 접액 여부가 ‘Y’인 경우 : 가. 지정된 재질이 A
		 * grade인 경우 그대로 적용하고, 해당 재질 노트 표시 나. 지정된 재질이 B, X grade인 경우 해당 재질 적용 불가 지정 재질
		 * 외에 나머지 재질에서 대체 선정은 불가능 (지정된 경우 지정 재질만 사용) 이 경우 “Material Guide (FTA101)에서는
		 * 적합한 재질이 없음” 이라고 노트 표시
		 * 
		 * 접액여부 체크라 Arrangement3일경우 Skip
		 */

		System.out.println("material1RstList : " + material1RstList);
		System.out.println("material1OutRstList : " + material1OutRstList);

		List<Map<String, Object>> material1Temp = null;

		for (Map<String, Object> sm : sealRstList) {
			Map<String, Object> sm_addInfo = sm.get("ADD_INFO") == null ? new HashMap<String, Object>()
					: (HashMap<String, Object>) sm.get("ADD_INFO");

			if (!"".equals(StringUtil.get(sm_addInfo.get("R_TYPE")))) {
				continue;
			}

			int iSealPidx = NumberUtil.toInt(sm.get("P_IDX")); // 현재 Idx
			String sArrangement = StringUtil.get(sm_addInfo.get("ARRANGEMENT"));

			String sSMTRL_msg1 = "", sSMTRL_msg2 = "";

			List<String> sSmallPartMtrl = new ArrayList<String>(); // 사용가능 Small Part Material List
			List<String> sSmallPartMtrl_n = new ArrayList<String>(); // 사용불가 Small Part Material List
			boolean bIsSmallPartMtrl = false;

			for (int i = 0; i < 2; i++) {
				boolean bIsCheckC = false; // C 조건 적용여부

				// 접액여부 체크라 Arrangement3일경우 Skip
				if ("3".equals(sArrangement) && i == 1)
					continue;

				if (i == 0)
					material1Temp = material1RstList;
				else
					material1Temp = material1OutRstList;

				Iterator mtrl1iter = material1Temp.iterator();

				while (mtrl1iter.hasNext()) {

					Map<String, Object> m = (Map<String, Object>) mtrl1iter.next();

					if (iSealPidx != NumberUtil.toInt(m.get("P_IDX")))
						continue;

					Map<String, Object> addInfo = (Map<String, Object>) m.get("ADD_INFO");

					for (String product : saProduct) {
						if ("".equals(StringUtil.get(addInfo.get("S_MTRL")))) {
							continue;
						} else if (!"".equals(addInfo.get("S_MTRL")) && "N".equals(addInfo.get("S_MTRL_YN"))) {

							bIsSmallPartMtrl = true;

							for (String ss : StringUtil.get(addInfo.get("S_MTRL")).split(",")) {
								if (!sSmallPartMtrl.contains(ss)) {
									sSmallPartMtrl.add(ss);
								}
							}

						} else if (!"".equals(addInfo.get("S_MTRL")) && "Y".equals(addInfo.get("S_MTRL_YN"))) {
							bIsSmallPartMtrl = true;
							bIsCheckC = true;

							// Small Part 재질 체크
							for (String ss : StringUtil.get(addInfo.get("S_MTRL")).split(",")) {

								// C1체크유체가 아닐경우 Skip
								if (!isC1CheckFluid(ss)) {
									if (!sSmallPartMtrl.contains(ss)) {
										sSmallPartMtrl.add(ss);
									}
									continue;
								}

								String sApply = getBrineProduct(item, product, saProductGroup, saProduct);

								// Product별 Grade를 조회
								Map<String, Object> c1_map = new HashMap<String, Object>();
								c1_map.put("PRODUCT", sApply); // C1 Product에 적용 할 명을 일부로직 적용 후 반영
								c1_map.put("MTRL_TYPE", "M1");
								c1_map.put("MTRL_CD", ss);
								c1_map.put("GB", getProductGb(item, product)); // Product 구분정보 (C1 조회용)
								c1_map.put("CONT", getProductCont(item, product, "%")); // Product 농도
								c1_map.put("TEMP_MIN", item.get("TEMP_MIN"));
								c1_map.put("TEMP_MAX", item.get("TEMP_MAX"));

								String sGrade = getGrade(c1_map); // C1 Grade를 조회

								if ("A".equals(sGrade) || "NONE".equals(sGrade)) {
									if (!sSmallPartMtrl.contains(ss)) {
										sSmallPartMtrl.add(ss);
									}
								} else {
									if (!sSmallPartMtrl_n.contains(ss)) {
										sSmallPartMtrl_n.add(ss);
									}
								}

								/*
								 * Map<String,Object> c1_map = new HashMap<String,Object>(); c1_map.clear();
								 * c1_map.put("PRODUCT",product); c1_map.put("MTRL_TYPE","M1");
								 * c1_map.put("MTRL_CD",ss); c1_map.put("GB",getProductGb(item, product)); //
								 * Product 구분정보 (C1 조회용)
								 * //c1_map.put("CONT",item.get(s+"_CONT")==null?"0":item.get(s+"_CONT")); // s
								 * 농도 c1_map.put("CONT",getProductCont(item, product, "%")); // s 농도
								 * c1_map.put("TEMP_MIN",item.get("TEMP_MIN"));
								 * c1_map.put("TEMP_MAX",item.get("TEMP_MAX")); List<Map<String,Object>> c1_list
								 * = rBMapper.selectRuleC1(c1_map); Map<String,Object> c1_m = null;
								 * if(c1_list.size()>0) { c1_m = c1_list.get(0); if (
								 * "A".equals(c1_m.get("GRADE"))){ // if(!sSmallPartMtrl.contains(ss)) {
								 * sSmallPartMtrl.add(ss); } }else { if(!sSmallPartMtrl_n.contains(ss)) {
								 * sSmallPartMtrl_n.add(ss); } } }else { //상위그룹으로 다시 검색
								 * c1_map.put("PRODUCT",getProductGrp(product)); c1_list =
								 * rBMapper.selectRuleC1(c1_map); if(c1_list.size()>0) { c1_m = c1_list.get(0);
								 * if ( "A".equals(c1_m.get("GRADE"))){ if(!sSmallPartMtrl.contains(ss)) {
								 * sSmallPartMtrl.add(ss); } }else{ if (!sSmallPartMtrl_n.contains(ss)) {
								 * sSmallPartMtrl_n.add(ss); } } } }
								 */
							}
						}
					}
				} // while (mtrl1iter.hasNext()) {

				if (bIsSmallPartMtrl) {

					Iterator smallMtrlList = sSmallPartMtrl.iterator();

					while (smallMtrlList.hasNext()) {
						String m = (String) smallMtrlList.next();
						for (String em : sSmallPartMtrl_n) {
							if (m.equals(em)) {
								smallMtrlList.remove();
							}
						}
					}

					if (sSmallPartMtrl.size() > 0) {
						String sMsg = "";
						for (String s : sSmallPartMtrl) {
							if ("".equals(sMsg)) {
								sMsg += s + "(" + getMaterialNm("1", s) + ")";
							} else {
								sMsg += ", " + s + "(" + getMaterialNm("1", s) + ")";
							}
						}
						setResultNoteList(noteRstList, iSealPidx, "Small Part Material : " + sMsg);
					}

					if (sSmallPartMtrl_n.size() > 0) {
						String sMsg = "";
						for (String s : sSmallPartMtrl_n) {
							if ("".equals(sMsg)) {
								sMsg += s + "(" + getMaterialNm("1", s) + ")";
							} else {
								sMsg += ", " + s + "(" + getMaterialNm("1", s) + ")";
							}
						}
						setResultNoteList(noteRstList, iSealPidx,
								"Small Part Material : " + sMsg + " : Material Guide상 부적합, 추가 검토 필요함"); // 노트
					}
				}

//				if(!"".equals(sSMTRL_msg2)) {
//					setResultNoteList(noteRstList, iSealPidx, sSMTRL_msg2); // 노트
//				}else if (!"".equals(sSMTRL_msg1)) {
//					setResultNoteList(noteRstList, iSealPidx, sSMTRL_msg1); // 노트
//				}

				// Gland/Sleeve Meterial
				/*
				 * 구분 칸에 Bellows라고 표시된 Seal Model들은 4자리 재질코드의 1st digit 재질과 Gland/Sleeve 재질이 다른
				 * 경우가 많음. Gland/Sleeve라는 부품이 Seal Assembly에서는 가장 덩치가 큰 부품들이라, Seal 전체 가격에 미치는
				 * 영향이 커서, 이의 재질도 구분할 필요가 있어서 별도로 표시. 따라서, Bellows로 구분된 경우 4자리 재질코드의 1st digit와
				 * 별개로 Gland/Sleeve 재질을 별도 노트 표시 필요
				 * 
				 * a. 지정된 Gland/Sleeve 재질이 A, B grade인 경우 그대로 적용하고, 재질 노트 표시 b. 지정된 Gland/Sleeve
				 * 재질이 X grade인 경우 나머지 재질에서 적합한 재질 및 “표준 재질이 부적합하여 대체 재질 선정함” 이라고 노트 표시
				 */
				for (Map<String, Object> m : material1Temp) {

					if (iSealPidx != NumberUtil.toInt(m.get("P_IDX")))
						continue;

					Map<String, Object> addInfo = (Map<String, Object>) m.get("ADD_INFO");
					for (String s : saProduct) {
						if ("BELLOWS".equals(StringUtil.get(m.get("SEAL_GB")).toUpperCase())) { // Bellows일경우
							// String sGsMtrl = StringUtils.get(addInfo.get("GS_MTRL"));

							// Small Part 재질 체크
							for (String ss : StringUtil.get(addInfo.get("GS_MTRL")).split(",")) {

								// C1체크유체가 아닐경우 Skip
								if (!isC1CheckFluid(ss)) {
									setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")),
											"Gland/Sleeve : " + ss); // 노트
									continue;
								}

								Map<String, Object> c1_map = new HashMap<String, Object>();
								c1_map.clear();
								c1_map.put("PRODUCT", s);
								c1_map.put("MTRL_TYPE", "M1");
								c1_map.put("MTRL_CD", ss);
								c1_map.put("GB", getProductGb(item, s)); // Product 구분정보 (C1 조회용)
								// c1_map.put("CONT",item.get(s+"_CONT")==null?"0":item.get(s+"_CONT")); // s 농도
								c1_map.put("CONT", getProductCont(item, s, "%")); // s 농도
								c1_map.put("TEMP_MIN", item.get("TEMP_MIN"));
								c1_map.put("TEMP_MAX", item.get("TEMP_MAX"));
								List<Map<String, Object>> c1_list = rBMapper.selectRuleC1(c1_map);
								Map<String, Object> c1_m = null;
								if (c1_list.size() > 0) {
									c1_m = c1_list.get(0);
									if ("A".equals(c1_m.get("GRADE")) || "B".equals(c1_m.get("GRADE"))) {
										setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")),
												"Gland/Sleeve : " + ss); // 노트
									} else {
										// 상위그룹으로 다시 검색
										c1_map.put("PRODUCT", getProductGrp(s));
										c1_list = rBMapper.selectRuleC1(c1_map);
										if (c1_list.size() > 0) {
											c1_m = c1_list.get(0);
											if ("A".equals(c1_m.get("GRADE")) || "B".equals(c1_m.get("GRADE"))) {
												setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")),
														"Gland/Sleeve : " + ss); // 노트
											} else {
												setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")),
														"Gland/Sleeve :  표준재질이 부적합하여 대체 재질 선정함");
											}
										}
									}
								}
							}
						}
					}
				}

				// C 체크로 Metal 재질이 모두 제외된 경우 Y 재질 추가하고 Note
				if (bIsCheckC) {

					// 현재 Idx의 메탈재질 수 Count
					int iMetalMtrlCnt = 0;
					for (Map<String, Object> mm : material1Temp) {
						if (NumberUtil.toInt(mm.get("P_IDX")) == iSealPidx) {
							iMetalMtrlCnt++;
						}
					}

					if (iMetalMtrlCnt == 0) {
						if (i == 0) {
							setMaterialResultListPrefer(material1RstList, sealRstList, "1", iSealPidx, 0, "-", null,
									"IN");
						} else {
							setMaterialResultListPrefer(material1OutRstList, sealRstList, "1", iSealPidx, 0, "-", null,
									"OUT");
						}
						setResultNoteList(noteRstList, iSealPidx, "적합한 Metal 재질 없음");
					}
				}

			}
		} // end for(Map<String,Object> sm : sealRstList) {

		//System.out.println("step_c1_chk2 - Gland/Sleeve Meterial check end !");
		//System.out.println("step_c1_chk2 end !");

	}

	@SuppressWarnings("unchecked")
	private void step_restriction_chk(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		Map<String, Object> ptm = new HashMap<String, Object>(); // 조회 임시 Param Map

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		System.out.println("step_restriction_chk Start ");

		// [C7]Amine product 일 경우 RY 재질 사용 불가
		if (isProduct("AMINE", saProductGroup, saProduct)) {

			for (int i = 1; i <= 4; i++) {

				@SuppressWarnings("rawtypes")
				Iterator iter = null;

				if (i == 1)
					iter = material2RstList.iterator();
				else if (i == 2)
					iter = material4RstList.iterator();
				else if (i == 3)
					iter = material2OutRstList.iterator();
				else if (i == 4)
					iter = material4OutRstList.iterator();

				while (iter.hasNext()) {
					Map<String, Object> m = (HashMap<String, Object>) iter.next();
					Map<String, Object> addInfo = (Map<String, Object>) m.get("ADD_INFO"); // 추가정보

					int iArrangement = getSealArrangement(item, sealRstList, NumberUtil.toInt(m.get("P_IDX")));

					if ((i == 1 || i == 2) && (StringUtil.get(addInfo.get("MTRL_CD")).equals("RY")
							|| StringUtil.get(addInfo.get("MTRL_CD")).equals("AP"))) {
						iter.remove();
						setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")),
								"[C7] Amine 기준 적용-사용불가재질 In : " + m.get("P_VAL"));
					} else if ((i == 3 || i == 4) && (StringUtil.get(addInfo.get("MTRL_CD")).equals("RY")
							|| StringUtil.get(addInfo.get("MTRL_CD")).equals("AP")) && iArrangement == 2) {
						iter.remove();
						setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")),
								"[C7] Amine 기준 적용-사용불가재질 Out : " + m.get("P_VAL"));
					}
				}
			}
		}

		// B1-11 체크
		// H2S 포함일경우
		// H2S = HYDROGEN SULFIDE
		if (isProduct("HYDROGEN SULFIDE", saProductGroup, saProduct)) {

			// double dH2SCont = NumberUtil.toDouble(item.get("H2S_CONT"));
			// dH2SCont = dH2SCont * 10000;//ppm으로 변환
			// H2S = HYDROGEN SULFIDE
			double dH2SCont = getProductCont(item, "HYDROGEN SULFIDE", "PPM");

			ptm.put("MCD", "B11101");
			ptm.put("H2S_CONT", dH2SCont);
			List<Map<String, Object>> rComB11101List = rBMapper.selectRuleComListB11101(ptm);

			if (!rComB11101List.isEmpty()) {

				for (int i = 0; i < 2; i++) {

					@SuppressWarnings("rawtypes")
					Iterator iter = null;

					if (i == 0)
						iter = material3RstList.iterator(); // In
					else
						iter = material3OutRstList.iterator(); // Out

					while (iter.hasNext()) {
						Map<String, Object> m3 = (HashMap<String, Object>) iter.next();
						Map<String, Object> addInfo = m3.get("ADD_INFO") == null ? new HashMap<String, Object>()
								: (HashMap<String, Object>) m3.get("ADD_INFO");

						int iArrangement = getSealArrangement(item, sealRstList, NumberUtil.toInt(m3.get("P_IDX")));
						if (i == 1 && iArrangement == 3)
							continue;

						boolean bchk = false;
						String sMtrlCd = StringUtil.get(addInfo.get("MTRL_CD"));
						String sMtrlGrp = getMaterialGrp(sMtrlCd);

						String sChkVal = "";
						for (Map<String, Object> m : rComB11101List) {

							String mtrlcds = StringUtil.get(m.get("ATTR1"));
							boolean bIsChk = false;
							for (String s : mtrlcds.split(",")) {
								if (sMtrlCd.equals(s) || sMtrlGrp.equals(s)) {
									sChkVal = StringUtil.get(m.get("ATTR2"));
									bIsChk = true;
									break;
								}
								if (bIsChk)
									break;
							}
						}

						System.out.println("H2S 체크 : " + sMtrlCd + " : 체크값 : " + sChkVal);

						if ("Y".equals(sChkVal)) {
							bchk = true;
						} else if ("[CHK]".equals(sChkVal)) { // HYDROCARBON 체크
							System.out.println("sMtrlCd : " + sMtrlCd);
							System.out.println("[CHK]");
							if (isProduct("HYDROCARBON", saProductGroup, saProduct)) { // 제품에 HYDROCARBON 이 있을 경우

								setResultProcList(procRstList, NumberUtil.toInt(m3.get("P_IDX")),
										"H2S - Hydrocarbon 포함 시 사용불가 :" + sMtrlCd);
								bchk = false;
								break;
							} else {
								bchk = true;
							}

							System.out.println("bchk : " + bchk);
						} else if ("[G-CHK]".equals(sChkVal)) {
							if (dH2SCont > 50) {
								setResultProcList(procRstList, NumberUtil.toInt(m3.get("P_IDX")),
										"유체 H2S - H2S 농도 50PPM 초과시 FKM 불가");
								bchk = false;
							} else {
								Map<String, Object> h2sMapChkParam = new HashMap<String, Object>();
								h2sMapChkParam.put("GRAPH_NO", "A2");
								h2sMapChkParam.put("CURV_NO", "1");
								h2sMapChkParam.put("VAL", item.get("TEMP_MAX")); // 온도
								List<Map<String, Object>> h2sMapChkList = rBMapper.selectRuleGraphFunc(h2sMapChkParam);

								String sFunc = "";
								double dCont = 0;
								if (h2sMapChkList.size() > 0) {
									sFunc = StringUtil.get(((Map<String, Object>) h2sMapChkList.get(0)).get("FUNC"));

									if (!"".equals(sFunc)) {
										sFunc = sFunc.replace("x", "" + item.get("TEMP_MAX")); //

										dCont = NumberUtil.toDouble(engine.eval(sFunc));

										if (dCont < dH2SCont) {
											setResultProcList(procRstList, NumberUtil.toInt(m3.get("P_IDX")),
													"유체[A2] H2S 농도 제한 초과 :" + dH2SCont);
											bchk = false;
										} else {
											bchk = true;
										}
									}
								}
							}
						}

						if (!bchk) { // 조건을 만족하지 않으면 삭제
							// material3RstList.remove(m3);
							iter.remove();
							setResultProcList(procRstList, NumberUtil.toInt(m3.get("P_IDX")),
									"유체 H2S 조건으로 제외-가스켓 : " + (i == 0 ? "In" : "Out") + " : " + m3.get("P_VAL"));
						}
					}

					// ????
//					if(i==0) iter = material2RstList.iterator();
//					else iter = material2OutRstList.iterator();
//					
//					while(iter.hasNext()) {
//						Map<String,Object> m2 = (HashMap<String,Object>)iter.next();
//						if(StringUtil.get(m2.get("MTRL_CD")).equals("RY")){
//							iter.remove();
//							setResultProcList(procRstList, NumberUtil.toInt(m2.get("P_IDX")), 
//									"[B1-11] Fluid with H2S 조건으로 삭제 :" + m2.get("P_VAL"));
//						}
//					}
//		
//					if(i==0) iter = material4RstList.iterator();
//					else iter = material4OutRstList.iterator();
//					while(iter.hasNext()) {
//						Map<String,Object> m4 = (HashMap<String,Object>)iter.next();
//						if(StringUtil.get(m4.get("MTRL_CD")).equals("RY") || StringUtil.get(m4.get("MTRL_CD")).equals("AP")){
//							iter.remove();
//							setResultProcList(procRstList, NumberUtil.toInt(m4.get("P_IDX")), 
//									"[B1-11] Fluid with H2S 조건으로 삭제 :" + m4.get("P_VAL"));
//						}
//					}

				} // for

			} // if(!rComB11101List.isEmpty()) {

			// H2S 유체일 경우 Antimony Carbon 사용불가
			for (int i = 0; i < 4; i++) {

				@SuppressWarnings("rawtypes")
				Iterator iter = null;
				String sMtrlGb = "";
				if (i == 0) {
					iter = material2RstList.iterator();
					sMtrlGb = "2";// In
				} else if (i == 1) {
					iter = material4RstList.iterator();
					sMtrlGb = "4";// In
				} else if (i == 2) {
					iter = material2OutRstList.iterator();
					sMtrlGb = "2";
					; // Out
				} else if (i == 3) {
					iter = material4OutRstList.iterator();
					sMtrlGb = "4";// Out
				}

				while (iter.hasNext()) {
					Map<String, Object> mm = (HashMap<String, Object>) iter.next();
					// Map<String,Object> addInfo = getMapMapData(mm,"ADD_INFO");

					int iArrangement = getSealArrangement(item, sealRstList, NumberUtil.toInt(mm.get("P_IDX")));
					if ((i == 2 || i == 3) && iArrangement == 3)
						continue; // 접액여부체크 불필요

					if ("RY".equals(getMaterialCd(sMtrlGb, StringUtil.get(mm.get("P_VAL"))))
							|| "AP".equals(getMaterialCd(sMtrlGb, StringUtil.get(mm.get("P_VAL"))))) {
						setResultProcList(procRstList, NumberUtil.toInt(mm.get("P_IDX")),
								"유체 H2S 조건으로 제외-Face : " + (i == 0 || i == 1 ? "In" : "Out") + " : " + mm.get("P_VAL"));
						iter.remove();
					}
				}
			}

		} // if(isProduct("H2S",saProductGroup)) {

		// Sulfur Product 가 포함된 경우
		if (isProduct("SULFUR", saProductGroup, saProduct)) {

			// Molten Sulfur 또는 Sulfur 함유 & Product 비중 1.5 미만일 경우
			if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) < 1.5
					|| NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")) < 1.5
					|| NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) < 1.5) {

				// * Type A (Pusher) : Viton은 100℃ 까지, 그 이상의 온도에는 FFKM
				if ("A".equals(StringUtil.get(item.get("ABC_TYPE")))) {
					if (NumberUtil.toDouble(item.get("TEMP_NOR")) > 100
							|| NumberUtil.toDouble(item.get("TEMP_MIN")) > 100
							|| NumberUtil.toDouble(item.get("TEMP_MAX")) > 100) {

						// FKM 항목 제외

						List<Map<String, Object>> materialList_tmp = null;
						// 각 재질별 체크
						for (int i = 1; i <= 2; i++) {
							String gb = "";
							if (i == 1) {
								materialList_tmp = material3RstList;
								gb = " In 3";
							} else if (i == 2) {
								materialList_tmp = material3OutRstList;
								gb = " Out 3";
							}

							for (Iterator<Map<String, Object>> iterator = materialList_tmp.iterator(); iterator
									.hasNext();) {
								Map<String, Object> mm = iterator.next();
								Map<String, Object> mm_addInfo = mm.get("ADD_INFO") == null
										? new HashMap<String, Object>()
										: (HashMap<String, Object>) mm.get("ADD_INFO");

								// 온도체크이지만 유체에 따른 온도조건이므로 가압조건에서 체크 제외한다.
								int iArrangement = getSealArrangement(item, sealRstList,
										NumberUtil.toInt(mm.get("P_IDX")));
								if (iArrangement == 3)
									continue;

								String sMtrlCd = StringUtil.get(mm_addInfo.get("MTRL_CD"));// 재질코드

								if ("[FKM]".equals(getMaterialGrp(sMtrlCd))) {
									setResultProcList(procRstList, 0, "Sulfur 온도 제한으로 FKM 재질 제외 : " + gb + " : "
											+ mm.get("P_VAL") + "(" + sMtrlCd + ")");
									iterator.remove();
								}
							}
						}
					}
				}
			}
		}

		// -----------------------------
		// C3 표준재질별 압력 체크
		// -----------------------------
		// C3 List
		List<Map<String, Object>> c3Lsit = rBMapper.selectRuleC3(new HashMap<String, Object>());
		String sArrangement = ""; // Seal별 Arrangement
		for (Map<String, Object> sm : sealRstList) {

			Map<String, Object> addInfo = sm.get("ADD_INFO") == null ? new HashMap<String, Object>()
					: (HashMap<String, Object>) sm.get("ADD_INFO");

			if ("".equals(StringUtil.get(addInfo.get("R_TYPE")))) {

				int iPidx = NumberUtil.toInt(sm.get("P_IDX"));

				// Seal 목록 구성
				String sSealFull = StringUtil.get(sm.get("P_VAL"));
				String[] sSeals = getSealType(item, sSealFull);

				sArrangement = StringUtil.get(addInfo.get("ARRANGEMENT"));

				// C3에서 체크할 압력값
				double dItemPressChkValue = 0.0d;
				double dItemPressChkValueIn = getC3CheckPress(item, iPidx, "IN", fp);
				double dItemPressChkValueOut = getC3CheckPress(item, iPidx, "OUT", fp);

				System.out.println("C3 재질 압력 체크 : Seal : " + sSealFull);
				System.out.println("C3 재질 압력 체크 : dItemPressChkValueIn : " + dItemPressChkValueIn);
				System.out.println("C3 재질 압력 체크 : dItemPressChkValueOut : " + dItemPressChkValueOut);

				int ichk = 0;
				for (String sSeal : sSeals) {
					List<Map<String, Object>> materialList_tmp = null;

					double dSealSize = getSealSize(item, "MM", sSeal, sSealFull, "1", fp); // MM
					// System.out.println("C3 재질체크 : " + sSeal + ", dSealSize: " + dSealSize);
					String gb = "";

					// 각 재질별 체크
					for (int i = 1; i <= 4; i++) {

						if (ichk == 0) { // inner Seal
							if (i == 1) {
								materialList_tmp = material1RstList;
								gb = " In 1";
							} else if (i == 2) {
								materialList_tmp = material2RstList;
								gb = " In 2";
							} else if (i == 3) {
								materialList_tmp = material3RstList;
								gb = " In 3";
							} else if (i == 4) {
								materialList_tmp = material4RstList;
								gb = " In 4";
							}

							dItemPressChkValue = dItemPressChkValueIn;

						} else { // outer Seal
							if (i == 1) {
								materialList_tmp = material1OutRstList;
								gb = " Out 1";
							} else if (i == 2) {
								materialList_tmp = material2OutRstList;
								gb = " Out 2";
							} else if (i == 3) {
								materialList_tmp = material3OutRstList;
								gb = " Out 3";
							} else if (i == 4) {
								materialList_tmp = material4OutRstList;
								gb = " Out 4";
							}

							dItemPressChkValue = dItemPressChkValueOut;
						}

						for (Iterator<Map<String, Object>> iterator = materialList_tmp.iterator(); iterator
								.hasNext();) {
							Map<String, Object> mm = iterator.next();

							if (iPidx == NumberUtil.toInt(mm.get("P_IDX"))) {
								Map<String, Object> mm_addInfo = mm.get("ADD_INFO") == null
										? new HashMap<String, Object>()
										: (HashMap<String, Object>) mm.get("ADD_INFO");
								String sMtrlCd = StringUtil.get(mm_addInfo.get("MTRL_CD"));// 재질코드

								System.out.println("C3 재질 압력 체크 : " + gb + " sMtrlCd : " + sMtrlCd);

								boolean bIsMtrl = false; // 해당하는 Seal의 재질이 있는가?
								boolean bIsMtrlPressOk = false; // 압력체크를 만족하는가?

								for (Map<String, Object> c3 : c3Lsit) {
									String[] c3_seals = StringUtil.get(c3.get("SEAL_TYPE")).trim().split(",");

									String[] c3_mtrl = null;
									if (i == 1)
										c3_mtrl = StringUtil.get(c3.get("MTRL_CD_M1")).trim().split(",");
									else if (i == 2)
										c3_mtrl = StringUtil.get(c3.get("MTRL_CD_M2")).trim().split(",");
									else if (i == 3)
										c3_mtrl = StringUtil.get(c3.get("MTRL_CD_M3")).trim().split(",");
									else if (i == 4)
										c3_mtrl = StringUtil.get(c3.get("MTRL_CD_M4")).trim().split(",");

									// 해당하는 Seal type과 재질코드가 있을 경우
									if (Arrays.asList(c3_seals).contains(sSeal)
											&& Arrays.asList(c3_mtrl).contains(sMtrlCd)) {

										String sConfiguration = StringUtil.get(item.get("SEAL_CONFIG"));

										// 씰사이즈, 압력을 체크한다.
										// A.SHAFT_SIZE_MIN, A.SHAFT_SIZE_MAX, A.SEAL_SIZE_MIN, A.SEAL_SIZE_MAX,
										// A.PRESS_OUT_D,

										boolean bdelChk1 = false;
										boolean bdelChk2 = false;
										// boolean bdelChk3 = false;

										// Cinfiguration이 있는 경우
										if ("".equals(sConfiguration)
												|| "".equals(StringUtil.get(c3.get("SEAL_CONFIG")))) {
											bdelChk1 = true;
										} else if ((!"".equals(sConfiguration)
												&& !"".equals(StringUtil.get(c3.get("SEAL_CONFIG"))))
												&& sConfiguration.equals(StringUtil.get(c3.get("SEAL_CONFIG")))) {
											bdelChk1 = true;
										}

										double dSealSizeMin = NumberUtil.toDouble(c3.get("SEAL_SIZE_MIN"));
										double dSealSizeMax = NumberUtil.toDouble(c3.get("SEAL_SIZE_MAX"));

										if (dSealSizeMin == 0)
											dSealSizeMin = -99999.0;
										if (dSealSizeMax == 0)
											dSealSizeMin = 99999.0;

										// SealSize 구간에 있는 경우
										if (dSealSize >= dSealSizeMin && dSealSize <= dSealSizeMax) {
											bdelChk2 = true;
										}

										if (bdelChk1 && bdelChk2) {
											bIsMtrl = true;
											if (NumberUtil.toDouble(c3.get("PRESS_OUT_D")) > 0) {
												if (NumberUtil.toDouble(c3.get("PRESS_OUT_D")) >= dItemPressChkValue) {
													bIsMtrlPressOk = true;
												}
											} else {
												bIsMtrlPressOk = true;
											}
										}

									}

									if (bIsMtrl && bIsMtrlPressOk) {
										break;
									}

								}

								if (bIsMtrl && !bIsMtrlPressOk) {
									setResultProcList(procRstList, iPidx, "Operating Window 압력체크로 제외 : " + gb + " : "
											+ mm.get("P_VAL") + "(" + sMtrlCd + ")");
									iterator.remove();
								}

							}
						}
					}
					ichk++;
				}

			}
		}

		List<String> chkGraphRemoveList = new ArrayList<String>(); // Seal 추천정보 전체 삭제대상 리스트 ( Seal, Material, Plan )

		// ------------------------------------------------------------------------
		// 체크 항목 처리
		// ------------------------------------------------------------------------
		Map<String, Object> addInfo = null;

		// ------------------------------------------------------------------------
		// DURA & BWIP Graph 체크
		// ------------------------------------------------------------------------
		System.out.println("DURA Graph check Start ");

		Map<String, Object> addInfo2 = null;
		Map<String, Object> addInfo4 = null;

		int iIdx = 0;
		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> g_param = new HashMap<String, Object>();
		Map<String, Object> gf_param = new HashMap<String, Object>();
		double dPress = 0.d;

		// Shaft Size (Inch)
		// double dShaftSizeIN = NumberUtil.toDouble(item.get("SHAFT_SIZE")) * 0.0393701
		// ;
		double dShaftSizeIN = NumberUtil.toDouble(item.get("SHAFT_SIZE")) / 25.4;

		// Seal Size (Inch)
		// double dSealSizeIN = getSealSize(item, "IN");

		// Seal Chamber Press (psig)
		double dSealChamPres_psig = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) / 0.069;

		// Choride 농도
		// double dChlorideCont = getProductCont(item, "CHLORIDE", "PPM");

		// PH
		// double dPH = NumberUtil.toDouble(item.get("PH")); // PH

		// BW Graph 체크 유무
		boolean isBW_graph_chk = false;
		// if(isProduct("CHLORINE",saProductGroup, saProduct)) {
		if (isProduct("CHLORIDE", saProductGroup, saProduct)) {
			if (NumberUtil.toInt(item.get("PH")) > 0) {
				isBW_graph_chk = true;
			}
		}

		// Seal List 정보 재설정 (C3)
		// List<Map<String,Object>> c3Lsit = rBMapper.selectRuleC3(new
		// HashMap<String,Object>());
		for (Map<String, Object> seal : sealRstList) {

			// End User, Fta 조건으로 생성된 seal type skip
			if ("1".equals(seal.get("R_TYPE")))
				continue;

			String sSeal = StringUtil.get(seal.get("P_VAL")); // 형태가 듀얼일 수 있음.
			String sSealIn = "", sSealOut = "";
			boolean bIsBreakIn = false, bIsBreakOut = false;

			if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))
					|| "".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
				sSealIn = sSeal;
			} else {
				if (sSeal.contains("/")) {
					sSealIn = sSeal.split("/")[0];
					sSealOut = sSeal.split("/")[1];
				} else {
					sSealIn = sSeal;
					sSealOut = sSeal;
				}
			}

			for (Map<String, Object> c3 : c3Lsit) {
				String sSealTypes_c3s = StringUtil.get(c3.get("SEAL_TYPE")); // ","로 구성된 복수정보

				for (String sSealTypes_c3 : sSealTypes_c3s.split(",")) {
					if (sSealIn.equals(sSealTypes_c3)) {
						Map<String, Object> sSeal_addInfo = seal.get("ADD_INFO") == null ? new HashMap<String, Object>()
								: (HashMap<String, Object>) seal.get("ADD_INFO");

						sSeal_addInfo.put("SEAL_STD_GB_IN", c3.get("SEAL_STD_GB"));
						sSeal_addInfo.put("SEAL_GB_TYPE_IN", c3.get("SEAL_GB_TYPE"));
						sSeal_addInfo.put("RS_GB_IN", c3.get("RS_GB"));
						sSeal_addInfo.put("PV_CURVE_IN", c3.get("PV_CURVE"));
						sSeal_addInfo.put("SEAL_GB_IN", c3.get("SEAL_GB"));
						// sSeal_addInfo.put("CONFIG", c3.get("CONFIG"));

						seal.put("ADD_INFO", sSeal_addInfo);
						bIsBreakIn = true;
						break;
					}
				}

				for (String sSealTypes_c3 : sSealTypes_c3s.split(",")) {
					if (sSealOut.equals(sSealTypes_c3)) {
						Map<String, Object> sSeal_addInfo = seal.get("ADD_INFO") == null ? new HashMap<String, Object>()
								: (HashMap<String, Object>) seal.get("ADD_INFO");

						sSeal_addInfo.put("SEAL_STD_GB_OUT", c3.get("SEAL_STD_GB"));
						sSeal_addInfo.put("SEAL_GB_TYPE_OUT", c3.get("SEAL_GB_TYPE"));
						sSeal_addInfo.put("RS_GB_OUT", c3.get("RS_GB"));
						sSeal_addInfo.put("PV_CURVE_OUT", c3.get("PV_CURVE"));
						sSeal_addInfo.put("SEAL_GB_OUT", c3.get("SEAL_GB"));
						// sSeal_addInfo.put("CONFIG", c3.get("CONFIG"));

						seal.put("ADD_INFO", sSeal_addInfo);
						bIsBreakOut = true;
						break;
					}
				}

				if (bIsBreakIn && bIsBreakOut)
					break;
			}
		} // end -- Seal List 정보 재설정 (C3)

		for (Map<String, Object> m : sealRstList) {
			addInfo = m.get("ADD_INFO") == null ? new HashMap<String, Object>()
					: (Map<String, Object>) m.get("ADD_INFO"); // 추가정보

			iIdx = NumberUtil.toInt(m.get("P_IDX")); // Index

			String sSealType_full = (String) m.get("P_VAL"); // Seal type

			// 듀얼 씰에 대한 처리
			String[] sSealTypes = null;

			if ("1".equals(StringUtil.get(item.get("ARRANGEMENT")))
					|| "".equals(StringUtil.get(item.get("ARRANGEMENT")))) {
				sSealTypes = new String[1];
				sSealTypes[0] = sSealType_full;
			} else {
				sSealTypes = new String[2];
				if (sSealType_full.contains("/")) {
					sSealTypes = sSealType_full.split("/");
				} else {
					sSealTypes[0] = sSealType_full;
					sSealTypes[1] = sSealType_full;
				}
			}

			int iSealInOutChk = 0;
			String SEAL_STD_GB = null;

			// 씰 수에 따라 처리
			for (String sSealType : sSealTypes) {

				if (iSealInOutChk == 0) {
					SEAL_STD_GB = StringUtil.get(addInfo.get("SEAL_STD_GB_IN"));
				} else {
					SEAL_STD_GB = StringUtil.get(addInfo.get("SEAL_STD_GB_OUT"));
				}

				double dSealSizeInTmp = getSealSize(item, "IN", sSealType, sSealType_full, "1", fp); // inch
				// setResultProcList(procRstList, 0, "[Seal API] Seal Size ["+sSealType+"] :" +
				// dSealSizeInTmp + " IN");

				if ("DURA".equals(StringUtil.get(SEAL_STD_GB))) { // Dura Seal 일 경우

					String sM2 = "";
					String sM4 = "";

					// Bellows Metal 조건 적용 Graph에 대한 변수처리
					List<String> bellowsMtrlList = new ArrayList<String>();
					if ("X-100".equals(sSealType) || "X-101".equals(sSealType) || "X-200".equals(sSealType)
							|| "X-201".equals(sSealType) || "CBR".equals(sSealType) || "CBS".equals(sSealType)) {

						// material 정보에서 S_MTRL 정보를 조회한다.
						// 같은 Idx에는 같은값을 가지므로 재질정보 중 한곳에서 값을 가져온다.
						String sS_MTRL = getS_MTRL(fp, iIdx);
						for (String smtrl : sS_MTRL.split(",")) {
							bellowsMtrlList.add(smtrl);
						}
					} else {
						bellowsMtrlList.add("");
					}

					// Product 조건 적용 Graph에 대한 변수처리
					List<String> productChkList = new ArrayList<String>();
					if ("X-100".equals(sSealType) || "X-101".equals(sSealType) || "X-200".equals(sSealType)
							|| "X-201".equals(sSealType) || "CBR".equals(sSealType) || "CBS".equals(sSealType)
							|| "PTO".equals(sSealType) || "RO".equals(sSealType) || "P-200".equals(sSealType)) {
						for (String product : saProductGroup) {
							productChkList.add(product);
						}
					} else {
						productChkList.add("");
					}

					// 2, 4번 재질 정보 임시 리스트
					List<Map<String, Object>> materialList2_tmp = null;
					List<Map<String, Object>> materialList4_tmp = null;
					Iterator iterM2 = null;
					Iterator iterM4 = null;

					// for (int i=0;i<2;i++) {

					if (iSealInOutChk == 0) {
						materialList2_tmp = material2RstList;
						materialList4_tmp = material4RstList;
					} else {
						materialList2_tmp = material2OutRstList;
						materialList4_tmp = material4OutRstList;
					}

					// 삭제 체크 리스트
					List<Map<String, String>> chkList = new ArrayList<Map<String, String>>();

					for (Map<String, Object> m2 : materialList2_tmp) { // Face2 재질
						sM2 = "";
						sM4 = "";
						if (iIdx == NumberUtil.toInt(m2.get("P_IDX"))) {
							addInfo2 = (Map<String, Object>) m2.get("ADD_INFO"); // 추가정보
							sM2 = StringUtil.get(addInfo2.get("MTRL_CD"));
						}

						for (Map<String, Object> m4 : materialList4_tmp) { // Face4 재질
							if (iIdx == NumberUtil.toInt(m4.get("P_IDX"))) {
								addInfo4 = (Map<String, Object>) m4.get("ADD_INFO"); // 추가정보
								sM4 = StringUtil.get(addInfo4.get("MTRL_CD"));
							}

							// Graph 대상 체크
							if (!"".equals(sM2) && !"".equals(sM4)) {
								g_param.clear();
								g_param.put("SEAL_TYPE", sSealType);
								g_param.put("MTRL_CD_M2", sM2);
								g_param.put("MTRL_CD_M4", sM4);
								g_param.put("SPEED", item.get("RPM_MAX")); // 속도는 min, nor, max중 어떤걸 쓸지 확인 필요

								// ------------------------------------//
								// Seal Type별 추가조건 설정 //
								// ------------------------------------//

								// Arrangement
								if ("P-200".equals(sSealType)) {
									g_param.put("ARRANGEMENT", StringUtil.get(item.get("ARRANGEMENT"))); // Arrangement
								}

								// Seal Size
								if ("P-50".equals(sSealType)) {
									g_param.put("SEAL_SIZE", NumberUtil.toDouble(item.get("SEAL_SIZE"))); // Seal Size
								}

								// Temp
								if ("PBR".equals(sSealType) && "PBS".equals(sSealType)) {
									g_param.put("TEMP", NumberUtil.toDouble(item.get("TEMP_MAX"))); // 온도
								}

								// Bellows Metal 목록과 Product 그룹 목록이 복수개로 발생할 수 있어 Looping 처리.
								for (String bellowsMtrlItem : bellowsMtrlList) {
									for (String productChkItem : productChkList) {

										g_param.put("BELLOWS_MTRL", bellowsMtrlItem);
										g_param.put("PRODUCT_GRP", productChkItem);

										// graph 조회
										List<Map<String, Object>> graphList = rBMapper.selectRuleGraph(g_param);

										if (graphList.size() > 0) {
											// graph Func 조회
											for (Map<String, Object> g_data : graphList) {
												gf_param.clear();
												gf_param.put("GRAPH_NO", g_data.get("GRAPH_NO"));
												gf_param.put("CURV_NO", g_data.get("CURV_NO"));

												// Size 구분체크
												if ("SEAL".equals(g_data.get("SIZE_GB"))) {
													gf_param.put("VAL", dSealSizeInTmp); // Seal SIze
												} else if ("SHAFT".equals(g_data.get("SIZE_GB"))) {
													// gf_param.put("SIZE", item.get("SHAFT_SIZE")); // Seal SIze
													gf_param.put("VAL", dShaftSizeIN); // Seal SIze (IN로 변경한다.) 1 mm -
																						// 0.0393701 in
												}

												List<Map<String, Object>> grapFunchList = rBMapper
														.selectRuleGraphFunc(gf_param);
												String sFunc = "";
												if (grapFunchList.size() > 0) {
													sFunc = StringUtil.get(
															((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
												}

												// 압력 체크 (씰챔버압력과 비교)
												if (!"".equals(sFunc)) {

													if ("SEAL".equals(g_data.get("SIZE_GB"))) {
														sFunc = sFunc.replace("x", "" + dSealSizeInTmp); //
													} else if ("SHAFT".equals(g_data.get("SIZE_GB"))) {
														sFunc = sFunc.replace("x", "" + dShaftSizeIN); //
													}
													System.out.println("sFunc :  " + sFunc);

													dPress = NumberUtil.toDouble(engine.eval(sFunc));
													// dPress = dPress * 0.069; // to BARG

													// 압력 제한범위를 초과하면 (씰챔버압력)
													if (dPress < dSealChamPres_psig) {
														Map<String, String> chk = new HashMap<String, String>();

														chk.put("P_IDX", String.valueOf(iIdx));
														chk.put("M2_SEQ", String.valueOf(m2.get("P_SEQ")));
														chk.put("M4_SEQ", String.valueOf(m4.get("P_SEQ")));
														chk.put("GRAPH_NO", String.valueOf(g_data.get("GRAPH_NO")));
														chkList.add(chk);

													}
												}
											}
										} // end graph check

									} // end for
								} // end for

							} // if (!"".equals(sM2) && !"".equals(sM4)) {
						} // end for
					} // end for

					// ----------------------------------
					// remove 대상 처리
					// ----------------------------------
					if (iSealInOutChk == 0) {
						iterM2 = material2RstList.iterator();
						iterM4 = material4RstList.iterator();
					} else {
						iterM2 = material2OutRstList.iterator();
						iterM4 = material4OutRstList.iterator();
					}

					// @SuppressWarnings("rawtypes")
					// Iterator iterM2 = material2RstList.iterator();
					while (iterM2.hasNext()) {
						Map<String, Object> m2 = (Map<String, Object>) iterM2.next();
						for (Map<String, String> chk : chkList) {

							if (chk.get("P_IDX").equals(String.valueOf(m2.get("P_IDX")))
									&& chk.get("M2_SEQ").equals(String.valueOf(m2.get("P_SEQ")))) {
								setResultProcList(procRstList, NumberUtil.toInt(m2.get("P_IDX")),
										"[A1] Graph 체크 조건으로 삭제 - Rotating Face : " + chk.get("GRAPH_NO") + " , "
												+ m2.get("P_VAL"));
								iterM2.remove();
								break;
							}
						}
					}

					// @SuppressWarnings("rawtypes")
					// Iterator iterM4 = material4RstList.iterator();
					while (iterM4.hasNext()) {
						Map<String, Object> m4 = (Map<String, Object>) iterM4.next();
						for (Map<String, String> chk : chkList) {
							if (chk.get("P_IDX").equals(String.valueOf(m4.get("P_IDX")))
									&& chk.get("M4_SEQ").equals(String.valueOf(m4.get("P_SEQ")))) {
								setResultProcList(procRstList, NumberUtil.toInt(m4.get("P_IDX")),
										"[A1] Graph 체크 조건으로 삭제 - Stationary Face : " + chk.get("GRAPH_NO") + " , "
												+ m4.get("P_VAL"));
								iterM4.remove();
								break;
							}
						}
					}

				}

//				// BW Seal 일 경우	
//				}else if ( isBW_graph_chk &&  "BWIP".equals(StringUtil.get(addInfo.get("SEAL_STD_GB"))) ) { 
//					// A8 그래프 범위에 해당하는지 체크
//					boolean bRemove = false;
//					// ph
//					// Chloride : % -> ppm
//					
//					// 대상 Metal 재질 : DB, ZB, M002
//					List<Map<String,Object>> graphFunclist = null;
//					String graphFunc1="",graphFunc2="",graphFunc3="";
//					double dGraphFunc1Val=0, dGraphFunc2Val=0, dGraphFunc3Val=0;
//					
//					Iterator iterM1 = null;
//					for (int i=0;i<2;i++) {
//						if (i==0) iterM1 = material1RstList.iterator();
//						else iterM1 = material1OutRstList.iterator();
//						//Iterator iterM1 = material1RstList.iterator();
//						
//						while(iterM1.hasNext()) {
//							Map<String,Object> dataM1 = (HashMap<String,Object>)iterM1.next();
//							
//							if (!"DB".equals(StringUtil.get(dataM1.get("P_VAL"))) && 
//									!"ZB".equals(StringUtil.get(dataM1.get("P_VAL"))) &&
//									!"M002".equals(StringUtil.get(dataM1.get("P_VAL")))
//									) { 
//								continue;
//							}
//							
//							if ("DB".equals(StringUtil.get(dataM1.get("P_VAL"))) || 
//									"ZB".equals(StringUtil.get(dataM1.get("P_VAL"))) 
//									) { 
//								gf_param.clear();
//								gf_param.put("GRAPH_NO","A8");
//								gf_param.put("CURV_NO","1");
//								gf_param.put("VAL",dChlorideCont); // Chloride 농도
//								graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);
//								if (graphFunclist.size() > 0) {
//									graphFunc1 =StringUtil.get(((Map<String,Object>)graphFunclist.get(0)).get("FUNC"));
//								}
//							}
//							
//							if ("ZB".equals(StringUtil.get(dataM1.get("P_VAL"))) || 
//									"M002".equals(StringUtil.get(dataM1.get("P_VAL"))) 
//									) { 
//								gf_param.clear();
//								gf_param.put("GRAPH_NO","A8");
//								gf_param.put("CURV_NO","2");
//								gf_param.put("VAL",dChlorideCont); // Chloride 농도
//								graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);
//								if (graphFunclist.size() > 0) {
//									graphFunc2 =StringUtil.get(((Map<String,Object>)graphFunclist.get(0)).get("FUNC"));
//								}
//							}
//							if ("M002".equals(StringUtil.get(dataM1.get("P_VAL"))) 
//									) { 
//								gf_param.clear();
//								gf_param.put("GRAPH_NO","A8");
//								gf_param.put("CURV_NO","3");
//								gf_param.put("VAL",dChlorideCont); // Chloride 농도
//								if (graphFunclist.size() > 0) {
//									graphFunc3 =StringUtil.get(((Map<String,Object>)graphFunclist.get(0)).get("FUNC"));
//								}
//							}
//							
//							if ("DB".equals(StringUtil.get(dataM1.get("P_VAL")))){  // 1번그래프보다 높아야 함.
//								graphFunc1 = graphFunc1.replace("x",""+dChlorideCont);
//								dGraphFunc1Val = NumberUtil.toDouble(engine.eval(graphFunc1));
//								
//								if(dGraphFunc1Val > dPH) {
//									bRemove = true;
//								}
//							}else if ("ZB".equals(StringUtil.get(dataM1.get("P_VAL")))){  // 2번그래프보다 높고 1번그래프보다 낮아야 함.
//								graphFunc1 = graphFunc1.replace("x",""+dChlorideCont);
//								dGraphFunc1Val = NumberUtil.toDouble(engine.eval(graphFunc1));
//								
//								graphFunc2 = graphFunc2.replace("x",""+dChlorideCont);
//								dGraphFunc1Val = NumberUtil.toDouble(engine.eval(graphFunc1));
//								
//								if(dGraphFunc1Val < dPH || dGraphFunc2Val > dPH) {
//									bRemove = true;
//								}
//							}else if ("M002".equals(StringUtil.get(dataM1.get("P_VAL")))){  // 2번그래프보다 낮고 3번그래프보다 높아야함.
//								graphFunc2 = graphFunc2.replace("x",""+dChlorideCont);
//								dGraphFunc2Val = NumberUtil.toDouble(engine.eval(graphFunc2));
//								
//								graphFunc3 = graphFunc3.replace("x",""+dChlorideCont);
//								dGraphFunc3Val = NumberUtil.toDouble(engine.eval(graphFunc3));
//								
//								if(dGraphFunc2Val < dPH || dGraphFunc3Val > dPH) {
//									bRemove = true;
//								}
//							}
//							
//							if(bRemove) {
//								setResultProcList(procRstList, NumberUtil.toInt(dataM1.get("P_IDX")), 
//										"[A8] Graph 체크 조건으로 삭제 - Metal : " + dataM1.get("P_VAL"));
//								iterM1.remove();
//							}
//						}
//					}
//					
//				}

				// [A2] 체크
//				if("QB".equals(sSealType) || "QBQ".equals(sSealType) || "QBS".equals(sSealType) || "QBQLZ".equals(sSealType) ) {
//					
//					System.out.println("A2 Start");
//					System.out.println("sSealType : " + sSealType);
//					System.out.println("dSealChamPres_psig : " + dSealChamPres_psig);
//					//Graph 조회
//					gf_param.clear();
//					gf_param.put("GRAPH_NO","A2");
//					if ("QBQLZ".equals(sSealType)) {
//						//Graph 조회
//						gf_param.put("VAL",dSealSize); // seal size
//						gf_param.put("CURV_NO","2");
//					}else {
//						gf_param.put("VAL",dSealSize); // seal size
//						gf_param.put("CURV_NO","1");
//					}
//					List<Map<String,Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);
//					
//					String sFunc = "";
//					if (grapFunchList.size() > 0) {
//						sFunc =StringUtil.get(((Map<String,Object>)grapFunchList.get(0)).get("FUNC"));
//					}
//					System.out.println("sFunc : " + sFunc);
//					sFunc = sFunc.replace("x",""+dSealSize); //
//					dPress = NumberUtil.toDouble(engine.eval(sFunc));
//					
//					// 압력 제한범위를 초과하면 (씰챔버압력)
//					if (dPress < dSealChamPres_psig) {
//						setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")), 
//								"[A2] Graph 체크 조건으로 삭제 - 제한압력(psig) : " + engine.eval(sFunc));
//						chkGraphRemoveList.add(""+m.get("P_IDX"));
//					}
//					
//					System.out.println("A2 End");
//				}

				iSealInOutChk++;

			} // end for(String sSealType : sSealTypes) {

		}
		System.out.println("Graph check End");

		// MD-200 Curve 체크
		// Differential Pressure(Paig) = Seal Cham Press(Psig) + 25 (Psig)
		Iterator iterS = sealRstList.iterator();
		while (iterS.hasNext()) {
			Map<String, Object> m = (HashMap<String, Object>) iterS.next();
			if ("MD-200".equals(StringUtil.get(m.get("P_VAL")))) {
				List<Map<String, Object>> list = rBMapper.selectRuleComListA401(item);
				if (list.size() > 0) {
					Map<String, Object> data = (Map<String, Object>) list.get(0);

					double dSealSizeInTmp = getSealSize(item, "IN", "MD-200", "MD-200", "1", fp); // inch
					// setResultProcList(procRstList, 0, "[Seal API] Seal Size [MD-200] :" +
					// dSealSizeInTmp + " IN");

					// Graph 조회
					gf_param.clear();
					gf_param.put("VAL", dSealSizeInTmp); // Seal SIze
					gf_param.put("GRAPH_NO", data.get("ATTR1"));
					gf_param.put("CURV_NO", data.get("ATTR2"));

					List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);
					String sFunc = "";
					if (grapFunchList.size() > 0) {
						sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
					}

					sFunc = sFunc.replace("x", "" + dSealSizeInTmp); //

					if (25 < NumberUtil.toDouble(engine.eval(sFunc))) { // 차압을 초과하면
						setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")),
								"[A4] Graph 체크 조건으로 삭제, 제한 차압(Differential Pressure) : " + engine.eval(sFunc));
						chkGraphRemoveList.add("" + m.get("P_IDX"));
					}
				}
			}
		}
		System.out.println("MD-200 Curve 체크 Start");

		// GF-200 Gas Consumption 정보 표시
		// Total Gas Consumption(SCFH) = CF * RPM / 1000
		for (Map<String, Object> m : sealRstList) {
			if ("GF-200".equals(m.get("SEAL_TYPE")) || "GX-200".equals(m.get("SEAL_TYPE"))) {
				List<Map<String, Object>> list = null;
				if ("GF-200".equals(m.get("SEAL_TYPE"))) {
					list = rBMapper.selectRuleComListA501(item);
				} else if ("GX-200".equals(m.get("SEAL_TYPE"))) {
					list = rBMapper.selectRuleComListA601(item);
				} else {
					list = new ArrayList();
				}

				if (list.size() > 0) {
					Map<String, Object> data = (Map<String, Object>) list.get(0);

					// Graph 조회
					gf_param.clear();
					gf_param.put("VAL", dSealChamPres_psig); // Seal Chamber Pressure (Psig)
					gf_param.put("GRAPH_NO", data.get("ATTR1"));
					gf_param.put("CURV_NO", data.get("ATTR2"));

					List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);
					String sFunc = "";
					if (grapFunchList.size() > 0) {
						sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
					}

					sFunc = sFunc.replace("x", "" + dSealChamPres_psig); //

					double dCF = NumberUtil.toDouble(engine.eval(sFunc)); // CF 값

					// SCFH 값 Note에 추가
					setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")),
							"Total Gas Consumption(SCFH) : " + dCF * NumberUtil.toDouble(item.get("RPM_MAX")) / 1000);
				}

			} else if ("ML-200".equals(m.get("SEAL_TYPE"))) {
				// ML-200 Curve 체크
				// Inboard Consumption + Outboard Consumption
				// Inboard 대상 차압 : Seal cham press + 50 psig - Seal cham press = 50psig
				// Outboard 대상 차압 : Seal cham press + 50 psig - 0 = Seal cham press + 50 psig
				List<Map<String, Object>> list = rBMapper.selectRuleComListA701(item);

				if (list.size() > 0) {
					Map<String, Object> data = (Map<String, Object>) list.get(0);

					double dDP_in_psig = 50;
					double dDP_out_psig = dSealChamPres_psig + 50;

					// Graph 조회
					gf_param.clear();
					gf_param.put("VAL", dSealChamPres_psig); // Seal Chamber Pressure (Psig)
					gf_param.put("GRAPH_NO", data.get("ATTR1"));
					gf_param.put("CURV_NO", data.get("ATTR2"));

					List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);
					String sFunc = "";
					String sFunc_in = "";
					String sFunc_out = "";
					if (grapFunchList.size() > 0) {
						sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
					}

					sFunc_in = sFunc.replace("x", "" + dDP_in_psig); // inboard 기준
					sFunc_out = sFunc.replace("x", "" + dDP_out_psig); // outboart 기준

					double dTotalCF = NumberUtil.toDouble(engine.eval(sFunc_in))
							+ NumberUtil.toDouble(engine.eval(sFunc_out));

					// SCFH 값 Note에 추가
					setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")),
							"Total Gas Consumption(SCFH) : " + dTotalCF);
				}

			}
		}
		System.out.println("Total Gas Consumption End");

		// 삭제 대상이 있을 경우 삭제
		if (chkGraphRemoveList.size() > 0) {
			removeResult(sealRstList, chkGraphRemoveList);
			removeResult(planRstList, chkGraphRemoveList);

			removeResult(material1RstList, chkGraphRemoveList);
			removeResult(material2RstList, chkGraphRemoveList);
			removeResult(material3RstList, chkGraphRemoveList);
			removeResult(material4RstList, chkGraphRemoveList);

			// removeResult(noteRstList,chkGraphRemoveList);
			// removeResult(procRstList,chkGraphRemoveList);
		}

		System.out.println("O-ring (Gaskets) 온도범위 체크 Start");

		// 가스켓 온도체크 진행 여부
		boolean bIsProcessGasketHighTempChk = true;

		// Water Guide X
		if ("Y".equals(StringUtil.get(item.get("__IS_PRODUCT_WATER_GUIDE")))) {
			bIsProcessGasketHighTempChk = false;
		}

		// 가스켓 고온 체크
		if (NumberUtil.toDouble(item.get("TEMP_MAX")) >= 0 && bIsProcessGasketHighTempChk) {
			Iterator iterM3 = null;
			String gb = "";
			for (int i = 0; i < 2; i++) {
				if (i == 0) {
					iterM3 = material3RstList.iterator();
					gb = " In 3 ";
				} else {
					iterM3 = material3OutRstList.iterator();
					gb = " Out 3 ";
				}

				while (iterM3.hasNext()) {
					Map<String, Object> m = (HashMap<String, Object>) iterM3.next();
					addInfo = m.get("ADD_INFO") == null ? new HashMap<String, Object>()
							: (HashMap<String, Object>) m.get("ADD_INFO");
					param.clear();
					param.put("ATTR1", addInfo.get("MTRL_CD"));
					List<Map<String, Object>> list = rBMapper.selectRuleComListC601(param);

					if (list.size() > 0) {
						boolean bIsDel = false; // 삭제유무
						for (Map<String, Object> c6data : list) {
							// if ( NumberUtil.toDouble(c6data.get("ATTR2")) >
							// NumberUtil.toDouble(item.get("TEMP_MIN")) ||
							// 온도값중 0도 이하가 있는 경우가 있어 조건 수정
							if (NumberUtil.toDouble(c6data.get("ATTR3")) < NumberUtil.toDouble(item.get("TEMP_MAX"))) {
								bIsDel = true;
								break;
							}
						}

						if (bIsDel) {
							setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")), "[C6] 가스켓 고온 체크로 삭제 : "
									+ gb + " : " + m.get("P_VAL") + "(" + addInfo.get("MTRL_NM") + ")");
							iterM3.remove();
						}
					}
				} // end while
			}

		}

		// 가스켓 저온 체크
		if (NumberUtil.toDouble(item.get("TEMP_MIN")) < 0) { // Gaskets 저온 체크

			Iterator iterM3 = null;
			String gb = "";
			List<Map<String, Object>> lowTemplist = rBMapper.selectRuleComListC602(null); // 저온 체크 리스트
			List<Map<String, Object>> lowTempDelMtrl3List = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> lowTempDelMtrl3OutList = new ArrayList<Map<String, Object>>();

			for (int i = 0; i < 2; i++) {
				if (i == 0) {
					iterM3 = material3RstList.iterator();
					gb = " In 3 ";
				} else {
					iterM3 = material3OutRstList.iterator();
					gb = " Out 3 ";
				}

				int j = 0;
				while (iterM3.hasNext()) {
					Map<String, Object> m = (HashMap<String, Object>) iterM3.next();
					addInfo = m.get("ADD_INFO") == null ? new HashMap<String, Object>()
							: (HashMap<String, Object>) m.get("ADD_INFO");

					boolean bIsDel = true; // 삭제유무
					for (Map<String, Object> c6data : lowTemplist) {
						if (StringUtil.get(addInfo.get("MTRL_CD")).equals(StringUtil.get(c6data.get("ATTR1")))) {
							// 저온에서 체크는 온도 범위에 재질이 있을 경우만 유효함.
//							if ( NumberUtil.toDouble(c6data.get("ATTR2")) <= NumberUtil.toDouble(item.get("TEMP_MIN")) &&
//									NumberUtil.toDouble(c6data.get("ATTR3")) >= NumberUtil.toDouble(item.get("TEMP_MAX")) 
//									) {
							if (NumberUtil.toDouble(c6data.get("ATTR2")) <= NumberUtil.toDouble(item.get("TEMP_MIN"))) {
								bIsDel = false;
								break;
							}
						}

					}

					// 최우선 추천재질에서 저온체크가 통과하면 전체 로직 Skip 함.
					if (j == 0) {
						if (!bIsDel) {
							break;
						}
					}

					if (bIsDel) {
						Map<String, Object> tmpMtrl = new HashMap<String, Object>();
						tmpMtrl.put("MTRL_CD", StringUtil.get(addInfo.get("MTRL_CD")));
						tmpMtrl.put("P_IDX", m.get("P_IDX"));

						if (i == 0) {
							lowTempDelMtrl3List.add(tmpMtrl);
						} else {
							lowTempDelMtrl3OutList.add(tmpMtrl);
						}
						setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")), "[C6] 가스켓 저온 체크로 삭제 : " + gb
								+ " : " + m.get("P_VAL") + "(" + addInfo.get("MTRL_CD") + ")");
						iterM3.remove();
					}
					j++;
				}
			}

			System.out.println("lowTempDelMtrl3List : " + lowTempDelMtrl3List);
			System.out.println("lowTempDelMtrl3OutList : " + lowTempDelMtrl3OutList);

			// 저온체크로 삭제되어 추가가 필요한 재질에 대하여
			List<Map<String, Object>> mtrlList = null;
			List<Map<String, Object>> lowTempDelMtrlList = null;
			for (int i = 0; i < 2; i++) {
				if (i == 0) {
					mtrlList = material3RstList;
					lowTempDelMtrlList = lowTempDelMtrl3List;
					gb = " In 3 ";
				} else {
					mtrlList = material3OutRstList;
					lowTempDelMtrlList = lowTempDelMtrl3OutList;
					gb = " Out 3 ";
				}

				int iSeqTmp = -100;
				for (Map<String, Object> m : lowTempDelMtrlList) {

					String sMtrlCd = StringUtil.get(m.get("MTRL_CD"));
					int iPIdx = NumberUtil.toInt(m.get("P_IDX"));
					boolean bIsAdd = false;

					// boolean bIsChgOk = false; // 저온재질 변경완료
					// String sChgMtrlCd = "";
					String sMaterialGrp = getMaterialGrp(sMtrlCd); // 재질의 그룹

					for (Map<String, Object> c6data : lowTemplist) {
						// 동일재질그룹 우선순위로 온도조건을 만족할경우
						if (sMaterialGrp.equals(StringUtil.get(c6data.get("ATTR5")))
								&& !sMtrlCd.equals(StringUtil.get(c6data.get("ATTR1")))) {
//							if ( NumberUtil.toDouble(c6data.get("ATTR2")) <= NumberUtil.toDouble(item.get("TEMP_MIN")) &&
//									NumberUtil.toDouble(c6data.get("ATTR3")) >= NumberUtil.toDouble(item.get("TEMP_MAX")) 
//									) {
							if (NumberUtil.toDouble(c6data.get("ATTR2")) <= NumberUtil.toDouble(item.get("TEMP_MIN"))) {

								if (i == 0) {
									setMaterialResultListPrefer(mtrlList, sealRstList, "3", iPIdx, iSeqTmp,
											c6data.get("ATTR1"), null, "IN");
								} else {
									setMaterialResultListPrefer(mtrlList, sealRstList, "3", iPIdx, iSeqTmp,
											c6data.get("ATTR1"), null, "OUT");
								}

								// setResultProcList(procRstList, NumberUtil.toInt(m.get("P_IDX")),
								// "[C6] O-Ring 저온 체크로 추가 : " + gb + " : " + c6data.get("ATTR1"));
								bIsAdd = true;
								iSeqTmp++;
							}
						}
					}

					// 해당하는그룹의 재질이 저온 리스트에 없으면 우선순위별로 허용온도에 해당하는 재질을 순서대로 붙인다.
					if (!bIsAdd) {
						for (Map<String, Object> c6data : lowTemplist) {
							// 동일재질그룹 우선순위로 온도조건을 만족할경우
							if (!sMtrlCd.equals(StringUtil.get(c6data.get("ATTR1")))) {
//								if ( NumberUtil.toDouble(c6data.get("ATTR2")) <= NumberUtil.toDouble(item.get("TEMP_MIN")) &&
//										NumberUtil.toDouble(c6data.get("ATTR3")) >= NumberUtil.toDouble(item.get("TEMP_MAX")) 
//										) {
								if (NumberUtil.toDouble(c6data.get("ATTR2")) <= NumberUtil
										.toDouble(item.get("TEMP_MIN"))) {
									if (i == 0) {
										setMaterialResultListPrefer(mtrlList, sealRstList, "3", iPIdx, iSeqTmp,
												c6data.get("ATTR1"), null, "IN");
									} else {
										setMaterialResultListPrefer(mtrlList, sealRstList, "3", iPIdx, iSeqTmp,
												c6data.get("ATTR1"), null, "OUT");
									}
									iSeqTmp++;
								}
							}
						}
					}
				}
			}
		}

		System.out.println("material3RstList : " + material3RstList);
		System.out.println("material3OutRstList : " + material3OutRstList);
		System.out.println("O-ring (Gaskets) 온도범위 체크 End");

		// C1 체크 재처리
		step_c1_chk(item, fp);

		// C3 체크
		// 선정된 Seal정보에 대하여 C3허용여부를 체크한다
		System.out.println(">>> 선정된 Seal정보에 대하여 C3허용여부 체크");
		for (Map<String, Object> sm : sealRstList) {

			int iPIdx = NumberUtil.toInt(sm.get("P_IDX"));
			String sSealType = StringUtil.get(sm.get("P_VAL"));
			int ichk = 0;
			for (String sSeal : sSealType.split("/")) {
				// 압력 재설정
				Map<String, Object> c3_param = (Map<String, Object>) ((HashMap<String, Object>) item).clone();

				// Press : Plan이 설정된 이 후라 Inboard , Outboard에 따라 압력 조정
				double dC3CheckPress = getC3CheckPress(item, iPIdx, (ichk == 0 ? "IN" : "OUT"), fp);
				c3_param.put("SEAL_CHAM_NOR", dC3CheckPress);
				c3_param.put("SEAL_CHAM_MIN", dC3CheckPress);
				c3_param.put("SEAL_CHAM_MAX", dC3CheckPress);

				// Line Speed
				double dC3ChekSealSize = getSealSize(item, "MM", sSeal, sSealType, "1", fp);
				c3_param.put("SEAL_SIZE", dC3ChekSealSize);

				// Line Speed
				// 주속계산 시 Mean Dia 로 계산되는 Seal Type 처리
				double dC3ChekSealSize2 = getSealSize(item, "MM", sSeal, sSealType, "2", fp);
				// 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60
				// Max기준으로만 체크함.
				c3_param.put("L_SPD_NOR", 0);
				c3_param.put("L_SPD_MIN", 0);
				c3_param.put("L_SPD_MAX",
						3.14 * dC3ChekSealSize2 * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속

				if (!isC3OperatingCheck(c3_param, sSeal)) {
					if(c3_param.containsKey("C3_EXCEPTION")) {
						setResultNoteListOper(noteRstList, iPIdx, "Operating Window 허용범위를 만족하지 않습니다 : " + sSeal + "\n["
								+ c3_param.get("C3_EXCEPTION").toString() + "]", sSeal);
					}else {
						setResultNoteListOper(noteRstList, iPIdx, "Operating Window 허용범위를 만족하지 않습니다 : " + sSeal, sSeal);
					}
				}

				ichk++;
			}
		}
	}

	private void step_result_filter(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer
		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");//
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");//

		// Sorting
		Collections.sort(material1RstList, new sortMap());
		Collections.sort(material2RstList, new sortMap());
		Collections.sort(material3RstList, new sortMap());
		Collections.sort(material4RstList, new sortMap());
		Collections.sort(material1OutRstList, new sortMap());
		Collections.sort(material2OutRstList, new sortMap());
		Collections.sort(material3OutRstList, new sortMap());
		Collections.sort(material4OutRstList, new sortMap());
		Collections.sort(planRstList, new sortMap());
		Collections.sort(sealRstList, new sortMap());

		// Seq Reorder
		setRstListSeqReOrd(material1RstList);
		setRstListSeqReOrd(material2RstList);
		setRstListSeqReOrd(material3RstList);
		setRstListSeqReOrd(material4RstList);
		setRstListSeqReOrd(material1OutRstList);
		setRstListSeqReOrd(material2OutRstList);
		setRstListSeqReOrd(material3OutRstList);
		setRstListSeqReOrd(material4OutRstList);
		setRstListSeqReOrd(planRstList);
		setRstListSeqReOrd(sealRstList);

		System.out.println("sealRstList : " + sealRstList);

		// 표준재질은 최상위 한건 만 표시
		Iterator restoreMtrl = null;
		for (int i_restore = 0; i_restore < 8; i_restore++) {
			if (i_restore == 0)
				restoreMtrl = material1RstList.iterator();
			else if (i_restore == 1)
				restoreMtrl = material2RstList.iterator();
			else if (i_restore == 2)
				restoreMtrl = material3RstList.iterator();
			else if (i_restore == 3)
				restoreMtrl = material4RstList.iterator();
			else if (i_restore == 4)
				restoreMtrl = material1OutRstList.iterator();
			else if (i_restore == 5)
				restoreMtrl = material2OutRstList.iterator();
			else if (i_restore == 6)
				restoreMtrl = material3OutRstList.iterator();
			else if (i_restore == 7)
				restoreMtrl = material4OutRstList.iterator();

			int i = 0; // 순번체크
			int iCurIdx = 0; // 현재 체크중인 idx
			while (restoreMtrl.hasNext()) {
				Map<String, Object> m = (HashMap<String, Object>) restoreMtrl.next();
				if (iCurIdx != NumberUtil.toInt(m.get("P_IDX"))) {
					iCurIdx = NumberUtil.toInt(m.get("P_IDX"));
					i = 0;
				} else {
					i++;
				}
				if (i > 0)
					restoreMtrl.remove();
			}
		}

		// 동일 Index에서 중복되는 Plan이 있을 경우 삭제
		Iterator restorePlan = planRstList.iterator();
		int i = 0; // 순번체크
		int iCurIdx = 0; // 현재 체크중인 idx
		String sCurVal = "";
		while (restorePlan.hasNext()) {
			Map<String, Object> m = (HashMap<String, Object>) restorePlan.next();

			if (iCurIdx != NumberUtil.toInt(m.get("P_IDX"))) {
				iCurIdx = NumberUtil.toInt(m.get("P_IDX"));
				sCurVal = StringUtil.get(m.get("P_VAL"));
				i = 0;
			} else {
				i++;
			}
			if (i > 0 && sCurVal.equals(StringUtil.get(m.get("P_VAL")))) {
				restorePlan.remove();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void step_additional_process(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");

		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer

		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		// double dSealSizeIN = getSealSize(item, "IN");// Inch
		double dSgMin = NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")); // 비중
		Map<String, Object> ptm = new HashMap<String, Object>();

		// ------------------------------------------------------------
		// 사용자 지정인자에 의한 Arrangement와 결과 Arrangement의 비교 1
		// ------------------------------------------------------------
		/* 최종목록에서 Note 추가하게 변경 : 21.10
		if ((!"".equals(StringUtil.get(item.get("API_PLAN_DIR_ARRANGEMENT")))
				&& !"".equals(StringUtil.get(item.get("ARRANGEMENT"))))
				&& (NumberUtil.toInt(item.get("API_PLAN_DIR_ARRANGEMENT")) != NumberUtil
						.toInt(item.get("ARRANGEMENT")))) {
			String sNote = "지정 Plan의 Arrangement : " + item.get("API_PLAN_DIR_ARRANGEMENT")
					+ " / 추천 Plan의 Arrangement : " + item.get("ARRANGEMENT");
			setResultNoteList(noteRstList, 0, sNote);
		}*/

		// --------------------------------
		// 지정 Plan과 추천결과 Plan 비교
		// --------------------------------
		/* 최종목록에서 Note 추가하게 변경 : 21.10
		if (!"".equals(StringUtil.get(item.get("API_PLAN_DIR")))) {

			for (Map<String, Object> p : planRstList) {
				boolean bIsPlanEqual = true; // 지정Plan과 추천Plan 일치여부
				
				// plan 비교
				for (String sDirPlan : StringUtil.get(item.get("API_PLAN_DIR")).trim().split("/")) { // 지정 Plan
					if ("61".equals(sDirPlan)) {
						continue;
					} else {
						boolean bIsPlanTmp = false;
						for (String sPlan : ("" + p.get("P_VAL")).split("/")) { // 추천 Plan
							if (sDirPlan.equals(sPlan)) {
								bIsPlanTmp = true;
								break;
							}
						}
						if (!bIsPlanTmp) {
							bIsPlanEqual = false;
						}
					}
				}
				
				if (!bIsPlanEqual) {
					setResultNoteList(noteRstList, Integer.parseInt(p.get("P_IDX").toString()), "지정 Plan과 추천결과 Plan이 상이합니다");
				}
			}

			//if (!bIsPlanEqual) {
			//	setResultNoteList(noteRstList, 0, "지정 Plan과 추천결과 Plan이 상이합니다");
			//}
		}*/

		// --------------------------------
		// 지정 Single/Dual 선택값과 추천결과 비교
		// --------------------------------
		/* 최종목록에서 Note 추가하게 변경 : 21.10
		if (!"".equals(StringUtil.get(item.get("S_D_GB")))) {
			String sSDgb = StringUtil.get(item.get("S_D_GB"));

			if (("D".equals(sSDgb) && NumberUtil.toInt(item.get("ARRANGEMENT")) < 2)
					|| ("S".equals(sSDgb) && NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2)) {
				setResultNoteList(noteRstList, 0, "지정 Single/Dual과 추천결과 Arrangement가 상이합니다");
			}
		}*/

		// --------------------------------
		// 지정 Seal Configuration 선택값과 추천결과 비교
		// --------------------------------
		/* 최종목록에서 Note 추가하게 변경 : 21.10
		if (!"".equals(StringUtil.get(item.get("SEAL_CONFIG")))) {
			String sSealConfigArrangement = StringUtil.get(item.get("SEAL_CONFIG")).substring(0, 1);
			if (NumberUtil.toInt(sSealConfigArrangement) != NumberUtil.toInt(item.get("ARRANGEMENT"))) {
				setResultNoteList(noteRstList, 0, "지정 Single Configuration과 추천결과 Arrangement가 상이합니다");
			}
		}*/

		// ----------------------------------------------
		// [Seal Model NON API Non-Cartridge Type]
		// ----------------------------------------------

		// RO / PTO GASKET 재질 재설정 및 씰타입 재설정 로직
		// 1.프로세스
		// 씰선정될 경우 온도 가스켓 가스켓변경
		// RO 온도 < 100 K 7
		// PTO 온도 < 100 K 7
		// (7재질이 C1 체크를 통과여부 체크 필요)

		// 2.프로세스 : 가스켓재질이 7로 변경되면
		// 씰선정될 경우 최조 GASKET이 7,8 이 나올 경우 7:GT, 8:HD
		// RO RO-TT 로변경
		// PTO PT 로변경

		if ("N".equals(StringUtil.get(item.get("API682_YN"))) && ("".equals(StringUtil.get(item.get("CARTRIDGE_TYPE")))
				|| "Z160020".equals(StringUtil.get(item.get("CARTRIDGE_TYPE"))))) {

			if (NumberUtil.toDouble(item.get("TEMP_MAX")) < 100) { // 100 이하 기준

				System.out.println("-----RO 체크-------->");

				// 1 process
				for (Map<String, Object> m : sealRstList) {
					Map<String, Object> addInfo = m.get("ADD_INFO") == null ? new HashMap<String, Object>()
							: (HashMap<String, Object>) m.get("ADD_INFO");
					if ("".equals(StringUtil.get(addInfo.get("R_TYPE")))) {

						String sIdx = StringUtil.get(m.get("P_IDX"));
						String[] sSeals = StringUtil.get(m.get("P_VAL")).split("/");
						String sSealIn = sSeals.length > 0 ? sSeals[0] : "";
						String sSealOut = sSeals.length > 1 ? sSeals[1] : "";

						if ("RO".equals(sSealIn) || "PTO".equals(sSealIn)) {

							System.out.println("------sSealIn------->" + sSealIn);

							// Gasket 재질 체크
							for (Map<String, Object> mm : material3RstList) {
								if (sIdx.equals(StringUtil.get(mm.get("P_IDX")))) {
									String sGasketMtrlDigit = StringUtil.get(mm.get("P_VAL"));

									System.out.println("--------sGasketMtrlDigit----->" + sGasketMtrlDigit);

									if ("K".equals(sGasketMtrlDigit)) { // Kalez6375일 경우
										Map<String, Object> gasket_AddInfo = mm.get("ADD_INFO") == null
												? new HashMap<String, Object>()
												: (HashMap<String, Object>) mm.get("ADD_INFO");
										gasket_AddInfo.put("MTRL_CD", "GT");
										gasket_AddInfo.put("MTRL_NM", "PTFE");
										mm.put("P_VAL", "7"); // PTFE로 변경

										System.out.println("mm : " + mm);

										setResultProcList(procRstList, NumberUtil.toInt(sIdx),
												"RO,PTO Seal Inboard Gaskets 변경 : K -> 7");
										break;
									}
								}
							}
						}

						System.out.println("------sSealOut------->" + sSealOut);

						if ("RO".equals(sSealOut) || "PTO".equals(sSealOut)) {
							// Gasket 재질 체크
							for (Map<String, Object> mm : material3OutRstList) {
								if (sIdx.equals(StringUtil.get(mm.get("P_IDX")))) {
									String sGasketMtrlDigit = StringUtil.get(mm.get("P_VAL"));
									if ("K".equals(sGasketMtrlDigit)) { // Kalez6375일 경우
										Map<String, Object> gasket_AddInfo = mm.get("ADD_INFO") == null
												? new HashMap<String, Object>()
												: (HashMap<String, Object>) mm.get("ADD_INFO");
										gasket_AddInfo.put("MTRL_CD", "GT");
										gasket_AddInfo.put("MTRL_NM", "PTFE");
										mm.put("P_VAL", "7"); // PTFE로 변경

										setResultProcList(procRstList, NumberUtil.toInt(sIdx),
												"RO,PTO Seal Outboard Gaskets 변경 : K -> 7");
										break;
									}
								}
							}
						}

					}
				}

				// 2 process
				for (Map<String, Object> m : sealRstList) {
					Map<String, Object> addInfo = m.get("ADD_INFO") == null ? new HashMap<String, Object>()
							: (HashMap<String, Object>) m.get("ADD_INFO");
					if ("".equals(StringUtil.get(addInfo.get("R_TYPE")))) {

						String sIdx = StringUtil.get(m.get("P_IDX"));
						String sSealsOrg = StringUtil.get(m.get("P_VAL"));
						String[] sSeals = sSealsOrg.split("/");
						String sSealIn = sSeals.length > 0 ? sSeals[0] : "";
						String sSealOut = sSeals.length > 1 ? sSeals[1] : "";
						String sSealInChg = sSealIn, sSealOutChg = sSealOut;
						String sSealChg = "";

						if ("RO".equals(sSealIn) || "PTO".equals(sSealIn)) {
							// Gasket 재질 체크
							for (Map<String, Object> mm : material3RstList) {
								if (sIdx.equals(StringUtil.get(mm.get("P_IDX")))) {
									String sGasketMtrlDigit = StringUtil.get(mm.get("P_VAL"));
									if ("7".equals(sGasketMtrlDigit) || "8".equals(sGasketMtrlDigit)) {
										if ("RO".equals(sSealIn)) {
											sSealInChg = "RO-TT";
										} else {
											sSealInChg = "PT";
										}
									}
								}
							}
						}

						System.out.println("------sSealInChg------->" + sSealInChg);

						if ("RO".equals(sSealOut) || "PTO".equals(sSealOut)) {
							// Gasket 재질 체크
							for (Map<String, Object> mm : material3OutRstList) {
								if (sIdx.equals(StringUtil.get(mm.get("P_IDX")))) {
									String sGasketMtrlDigit = StringUtil.get(mm.get("P_VAL"));
									if ("7".equals(sGasketMtrlDigit) || "8".equals(sGasketMtrlDigit)) {
										if ("RO".equals(sSealIn)) {
											sSealOutChg = "RO-TT";
										} else {
											sSealOutChg = "PT";
										}
									}
								}
							}
						}

						System.out.println("------sSealOutChg------->" + sSealOutChg);

						if (!"".equals(sSealOutChg)) {
							sSealChg = sSealInChg + "/" + sSealOutChg;
						} else {
							sSealChg = sSealInChg;
						}
						m.put("P_VAL", sSealChg);

						if (!sSealsOrg.equals(sSealChg)) {
							setResultProcList(procRstList, NumberUtil.toInt(sIdx),
									"RO,PTO Seal 변경 : " + sSealsOrg + " -> " + sSealChg);
						}

					}
				}
			}

		}

		/*
		 * //B-12 face Material String sFaceMaterialNoteHard = "",
		 * sFaceMaterialNoteSoft="";
		 * 
		 * //hard if(isProduct("HCL",saProductGroup, saProduct) ||
		 * isProduct("H2SO4",saProductGroup, saProduct) ||
		 * isProduct("HNO3",saProductGroup, saProduct) ) { sFaceMaterialNoteHard +=
		 * "On very strong acids (examples 30% HCl, 50% H2SO4, 50% HNO3)<br/>"; }
		 * 
		 * if (getProductCont(item, "NAOH", "%") >= 50) { sFaceMaterialNoteHard +=
		 * "On very strong caustics (50% NaOH)<br/>"; }
		 * 
		 * if (getProductCont(item, "SOLID", "PPM") >= 500) { sFaceMaterialNoteHard +=
		 * "On products containing more than 500 ppm solids(without filtering system)<br/>"
		 * ; }
		 * 
		 * if (NumberUtil.toDouble(item.get("VISC_MIN")) > 680) { sFaceMaterialNoteHard
		 * += "On viscous products(higer than 680 cP)<br/>"; }
		 * 
		 * if (isProduct("CRUDE OIL",saProductGroup, saProduct) &&
		 * isProduct("WATER",saProductGroup, saProduct) ) { if(getProductCont(item,
		 * "WATER", "%") <= 10) { sFaceMaterialNoteHard +=
		 * "On Crude oils with a water cut from 1 to 10%<br/>"; } }
		 * 
		 * if (!"".equals(sFaceMaterialNoteHard)) { sFaceMaterialNoteHard =
		 * "[B1-12] When hard faces should be used :  <br/>" + sFaceMaterialNoteHard; }
		 * 
		 * //soft if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) <= 0.65) {
		 * sFaceMaterialNoteSoft +=
		 * "On light hydrocarbons(specific gravity below 0.65)<br/>"; }
		 * 
		 * if (isProduct("WATER",saProductGroup, saProduct) &&
		 * NumberUtil.toDouble(item.get("TEMP_MIN")) > 70) { sFaceMaterialNoteSoft +=
		 * "On water above 70℃<br/>"; }
		 * 
		 * if (NumberUtil.toDouble(item.get("VAP_PRES_MAX")) * 1.1 >
		 * NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"))) { sFaceMaterialNoteSoft +=
		 * "On Products with a vapour pressure very close to the sealing pressure(vapour pressure X 1.1 > Sealing pressure)<br/>"
		 * ; }
		 * 
		 * if (isProduct("CRUDE OIL",saProductGroup, saProduct) &&
		 * isProduct("WATER",saProductGroup, saProduct) ) { if(getProductCont(item,
		 * "WATER", "%") > 50) { sFaceMaterialNoteSoft +=
		 * "On Crude oils containing more than 50% water <br/>"; } }
		 * 
		 * if (NumberUtil.toDouble(item.get("VISC_MAX")) < 0.4) { sFaceMaterialNoteSoft
		 * += "On products with a viscosity below 0.4cP<br/>"; }
		 * 
		 * if (NumberUtil.toDouble(item.get("VISC_MIN")) >=0.4 &&
		 * NumberUtil.toDouble(item.get("VISC_MAX")) <=2 &&
		 * NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) > 10 ) { sFaceMaterialNoteSoft
		 * +=
		 * "On products with a viscosity between 0.4 and 2 cP and pressures above 10 barG <br/>"
		 * ; }
		 * 
		 * if (!"".equals(sFaceMaterialNoteSoft)) { sFaceMaterialNoteSoft =
		 * "[B1-12] When carbon faces should be used : <br/>" + sFaceMaterialNoteSoft; }
		 * 
		 * //note 추가 if (!"".equals(sFaceMaterialNoteHard)) {
		 * setResultNoteList(noteRstList, 999,sFaceMaterialNoteHard); }
		 * 
		 * if (!"".equals(sFaceMaterialNoteSoft)) { setResultNoteList(noteRstList,
		 * 999,sFaceMaterialNoteSoft); }
		 */

		// B1-4 Seal Injection and Quench Guidelines
		for (Map<String, Object> m : planRstList) {

			// Seal Type
			String sSealTypeFull = "";
			String sSealTypeTmp = "";
			for (Map<String, Object> sm : sealRstList) {
				if (m.get("P_IDX").equals(sm.get("P_IDX"))) {
					sSealTypeFull = StringUtil.get(sm.get("P_VAL"));
					if (sSealTypeFull.contains("/")) {
						sSealTypeTmp = sSealTypeFull.split("/")[0];
					} else {
						sSealTypeTmp = sSealTypeFull;
					}
					break;
				}
			}
			double dSealSizeInTmp = getSealSize(item, "IN", sSealTypeTmp, sSealTypeFull, "1", fp);

			String sPlans = StringUtil.get(m.get("P_VAL")); // Plan 정보
			for (String sPlan : sPlans.split("/")) {
				if ("11".equals(sPlan) || "13".equals(sPlan) || "31".equals(sPlan)) {
					
					Map<String,Object> addNote = new HashMap<String,Object>();
					addNote.put("PLAN",sPlan);
					
					// 비중 0.65 초과 <---- 650kg/m3 이상 (비중 0.65)
					if (dSgMin > 0.65) {
						setResultNoteList(noteRstList, 
								NumberUtil.toInt(m.get("P_IDX")),
								"Plan " + sPlan + " : flow in liters per minute : "
										+ (dSealSizeInTmp == 0 ? "Seal Size in inch x 3"
												: NumberUtil.round(dSealSizeInTmp * 3, 1)), "p", addNote);
					} else {
						setResultNoteList(noteRstList, 
								NumberUtil.toInt(m.get("P_IDX")),
								"Plan " + sPlan + " : flow in liters per minute : "
										+ (dSealSizeInTmp == 0 ? "Seal Size in inch x 6"
												: NumberUtil.round(dSealSizeInTmp * 6, 1)), "p", addNote);
					}
				} else if ("32".equals(sPlan)) {
					
					Map<String,Object> addNote = new HashMap<String,Object>();
					addNote.put("PLAN",sPlan);

					ptm.clear();
					ptm.put("MCD", "B1401");
					List<Map<String, Object>> rComList = getRuleComListType1(ptm);

					String sNote = "";
					double dStdBushing = 0, dAPIClearanceBushing = 0, dLipSeal = 0, dRuleOfThumb = 0,
							dRequiredForCooling = 0;
					double dRate = 0.0d;
					for (Map<String, Object> mCom : rComList) {

						if (dSealSizeInTmp <= NumberUtil.toDouble(mCom.get("ATTR1"))) {
							dStdBushing = NumberUtil.toDouble(mCom.get("ATTR2"));
							dAPIClearanceBushing = NumberUtil.toDouble(mCom.get("ATTR3"));
							dLipSeal = NumberUtil.toDouble(mCom.get("ATTR4"));
							dRuleOfThumb = NumberUtil.toDouble(mCom.get("ATTR5"));
							dRequiredForCooling = NumberUtil.toDouble(mCom.get("ATTR6"));

							dRate = 1.0 * dSealSizeInTmp / NumberUtil.toDouble(mCom.get("ATTR1"));

							sNote = "Plan " + sPlan + " : [Seal injection and quench guidelines] <br/>";
							sNote += "&nbsp;&nbsp;Standard bushing (lpm) : " + NumberUtil.round(dStdBushing * dRate, 1)
									+ " <br/>";
							sNote += "&nbsp;&nbsp;API clearance bushing (lpm) : "
									+ NumberUtil.round(dAPIClearanceBushing * dRate, 1) + "<br/>";
							sNote += "&nbsp;&nbsp;Lip seal (lph) : " + NumberUtil.round(dLipSeal * dRate, 1) + "<br/>";
							sNote += "&nbsp;&nbsp;Rule of thumb (lpm) : " + NumberUtil.round(dRuleOfThumb * dRate, 1)
									+ "<br/>";
							sNote += "&nbsp;&nbsp;Required for cooling (to 250°C) (lpm) : "
									+ NumberUtil.round(dRequiredForCooling * dRate, 1);

							setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")), sNote, "p", addNote);
							break;
						}
					}

				} else if ("62".equals(sPlan)) {
					// Quench Type에 따라 처리
					// Z130010 Water
					// Z130020 Nitrogen
					// Z130030 Steam
					
					Map<String,Object> addNote = new HashMap<String,Object>();
					addNote.put("PLAN",sPlan);

					if ("Z130010".equals(StringUtil.get(item.get("QUENCH_TYPE")))) { // water
						ptm.clear();
						ptm.put("MCD", "B1403");
						List<Map<String, Object>> rComList = getRuleComListType1(ptm);

						String sNote = "";
						double dToAvoidCrystalBuildUp = 0, dToCarryAwaySolidsFromLeakage = 0, dFroCollingPurposes = 0;
						double dRate = 0;
						for (Map<String, Object> mCom : rComList) {
							if (dSealSizeInTmp <= NumberUtil.toDouble(mCom.get("ATTR1"))) {
								dToAvoidCrystalBuildUp = NumberUtil.toDouble(mCom.get("ATTR2"));
								dToCarryAwaySolidsFromLeakage = NumberUtil.toDouble(mCom.get("ATTR3"));
								dFroCollingPurposes = NumberUtil.toDouble(mCom.get("ATTR4"));

								dRate = dSealSizeInTmp / NumberUtil.toDouble(mCom.get("ATTR1"));

								sNote = "[Plan " + sPlan + "][Seal injection and quench guidelines] <br/>";
								sNote += "&nbsp;&nbsp;To avoid crystal build up (lph) : "
										+ NumberUtil.round(dToAvoidCrystalBuildUp * dRate, 1) + "<br/>";
								sNote += "&nbsp;&nbsp;To carry away solids from leakage (lph) : "
										+ NumberUtil.round(dToCarryAwaySolidsFromLeakage * dRate, 1) + "<br/>";
								sNote += "&nbsp;&nbsp;For cooling purposes above 80℃ (lph) : "
										+ NumberUtil.round(dFroCollingPurposes * dRate, 1);

								setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")), sNote, "p", addNote);
								break;
							}
						}
					} else if ("Z130020".equals(StringUtil.get("QUENCH_TYPE"))) { // Nitrogen
						setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")),
								"Plan " + sPlan + " : flow in liters per hour : "
										+ (dSealSizeInTmp == 0 ? "Seal Size in inch x 20" : (dSealSizeInTmp * 20)), "p", addNote);
					} else if ("Z130030".equals(StringUtil.get("QUENCH_TYPE"))) { // Steam
						setResultNoteList(noteRstList, NumberUtil.toInt(m.get("P_IDX")),
								"Plan " + sPlan + " : flow in liters per kg/hour : "
										+ (dSealSizeInTmp == 0 ? "Seal Size in inch x 0.25" : (dSealSizeInTmp * 0.25)), "p", addNote);
					}
				}
			}

		}

		// WASTE WATER 그룹일 경우 NOTE 추가
		// "PLAN 32 고려 검토"
		if (isProduct("WASTE WATER", saProductGroup, saProduct)) {
			setResultNoteList(noteRstList, 0, "WASTE WATER 유체에 따라 PLAN 32 고려 검토");
		}

		// -----------------------------------------------------
		// Index 별 Plan정보가 복수개로 설정될 경우 Seal 과 재질을 Plan수에 맞게 채워준다.
		// -----------------------------------------------------
		List<String> addList = new ArrayList<String>();
		int iPIdxChk = 0;
		for (Map<String, Object> pm : planRstList) {
			if (iPIdxChk != NumberUtil.toInt(pm.get("P_IDX")) && NumberUtil.toInt(pm.get("P_IDX")) != 0) {
				addList.add("" + pm.get("P_IDX"));
				iPIdxChk = NumberUtil.toInt(pm.get("P_IDX"));
			}
		}

		// System.out.println("addList : " + addList);

		for (String sIdx : addList) {
			int iSeqCnt = 0;
			for (Map<String, Object> pm : planRstList) {
				if (sIdx.equals(StringUtil.get(pm.get("P_IDX")))) {
					iSeqCnt++;
				}
			}

			// System.out.println("iSeqCnt : " + iSeqCnt);

			// iMaxSeq 수만큰 추가
			List<Map<String, Object>> restoreRstList = null;

			for (int i_restore = 0; i_restore < 9; i_restore++) {
				if (i_restore == 0) {
					restoreRstList = material1RstList;
				} else if (i_restore == 1) {
					restoreRstList = material2RstList;
				} else if (i_restore == 2) {
					restoreRstList = material3RstList;
				} else if (i_restore == 3) {
					restoreRstList = material4RstList;
				} else if (i_restore == 4) {
					restoreRstList = material1OutRstList;
				} else if (i_restore == 5) {
					restoreRstList = material2OutRstList;
				} else if (i_restore == 6) {
					restoreRstList = material3OutRstList;
				} else if (i_restore == 7) {
					restoreRstList = material4OutRstList;
				} else if (i_restore == 8) {
					restoreRstList = sealRstList;
				}

				// System.out.println("restoreRstList : " + restoreRstList);

				int ichk = 0;
				int iSeq = 0;
				Map<String, Object> orgM = null; // 추가할 정보
				for (Map<String, Object> m : restoreRstList) {
					if (sIdx.equals(StringUtil.get(m.get("P_IDX")))) {
						if (orgM == null)
							orgM = (Map<String, Object>) ((HashMap<String, Object>) m).clone();
						if (iSeq < NumberUtil.toInt(m.get("P_SEQ")))
							iSeq = NumberUtil.toInt(m.get("P_SEQ"));
						ichk++;
					}
				}

				// System.out.println("ichk : " + ichk);

				if (iSeqCnt != 0 && ichk != 0) {
					for (int addi = 0; addi < (iSeqCnt - ichk); addi++) {

						Map<String, Object> addM = (Map<String, Object>) ((HashMap<String, Object>) orgM).clone();
						addM.put("P_SEQ", ++iSeq);
						restoreRstList.add(addM);
					}
				}
			}
		}

//		System.out.println("sealRstList 1 : " + sealRstList);
//		System.out.println("planRstList 1 : " + planRstList);
//		System.out.println("material1RstList 1 : " + material1RstList);
//		System.out.println("material2RstList 1 : " + material2RstList);
//		System.out.println("material3RstList 1 : " + material3RstList);
//		System.out.println("material4RstList 1 : " + material4RstList);
//		System.out.println("material1OutRstList 1 : " + material1OutRstList);
//		System.out.println("material2OutRstList 1 : " + material2OutRstList);
//		System.out.println("material3OutRstList 1 : " + material3OutRstList);
//		System.out.println("material4OutRstList 1 : " + material4OutRstList);

		// -----------------------------------
		// 결과 정리
		// -----------------------------------
		// Sorting
		Collections.sort(sealRstList, new sortMap());
		Collections.sort(material1RstList, new sortMap());
		Collections.sort(material2RstList, new sortMap());
		Collections.sort(material3RstList, new sortMap());
		Collections.sort(material4RstList, new sortMap());
		Collections.sort(material1OutRstList, new sortMap());
		Collections.sort(material2OutRstList, new sortMap());
		Collections.sort(material3OutRstList, new sortMap());
		Collections.sort(material4OutRstList, new sortMap());
		Collections.sort(planRstList, new sortMap());
		Collections.sort(noteRstList, new sortMap());
		Collections.sort(procRstList, new sortMap());

		// Seq Reorder
		setRstListSeqReOrd(sealRstList);
		setRstListSeqReOrd(material1RstList);
		setRstListSeqReOrd(material2RstList);
		setRstListSeqReOrd(material3RstList);
		setRstListSeqReOrd(material4RstList);
		setRstListSeqReOrd(material1OutRstList);
		setRstListSeqReOrd(material2OutRstList);
		setRstListSeqReOrd(material3OutRstList);
		setRstListSeqReOrd(material4OutRstList);
		setRstListSeqReOrd(planRstList);

		// 하나의 리스트로 구성한다.
		List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();
		boolean isVal = false;

		int iMaxIdx = getNextIdx(fp) - 1;

		for (int i = 1; i <= iMaxIdx; i++) {
			// idx별 max seq를 구한다.
			int iMaxSeq = 0;
			for (Map<String, Object> m : sealRstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
			for (Map<String, Object> m : material1RstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
			for (Map<String, Object> m : material2RstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
			for (Map<String, Object> m : material3RstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
			for (Map<String, Object> m : material4RstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}

			for (Map<String, Object> m : material1OutRstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
			for (Map<String, Object> m : material2OutRstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
			for (Map<String, Object> m : material3OutRstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
			for (Map<String, Object> m : material4OutRstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
			for (Map<String, Object> m : planRstList) {
				if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}

			//System.out.println("iMaxSeq : " + iMaxSeq);

			// seq 만큼 loop
			for (int j = 1; j <= iMaxSeq; j++) {
				Map<String, Object> rst = new HashMap<String, Object>();
				// Idx를 처음만 표시하게...
//				if( j == 1) {
				rst.put("P_IDX", i);
//				}else {
//					rst.put("P_IDX","");
//				}

				rst.put("P_SEQ", j);

				// Seal Type
				isVal = false;
				for (Map<String, Object> m : sealRstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("SEAL", m.get("P_VAL"));
						rst.put("SEAL_CONFIG", m.get("ADD_INFO") == null ? ""
								: ((HashMap<String, Object>) m.get("ADD_INFO")).get("CONFIG"));
						rst.put("SEAL_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("SEAL", "");

				// Material 1Type
				isVal = false;
				for (Map<String, Object> m : material1RstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("MTRL1", m.get("P_VAL"));
						rst.put("MTRL1_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("MTRL1", "");

				// Material 2Type
				isVal = false;
				for (Map<String, Object> m : material2RstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("MTRL2", m.get("P_VAL"));
						rst.put("MTRL2_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("MTRL2", "");

				// Material 3Type
				isVal = false;
				for (Map<String, Object> m : material3RstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("MTRL3", m.get("P_VAL"));
						rst.put("MTRL3_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("MTRL3", "");

				// Material 4Type
				isVal = false;
				for (Map<String, Object> m : material4RstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("MTRL4", m.get("P_VAL"));
						rst.put("MTRL4_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("MTRL4", "");

				// Outer
				// Material 1Type
				isVal = false;
				for (Map<String, Object> m : material1OutRstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("MTRL_OUT1", m.get("P_VAL"));
						rst.put("MTRL_OUT1_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("MTRL_OUT1", "");

				// Material 2Type
				isVal = false;
				for (Map<String, Object> m : material2OutRstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("MTRL_OUT2", m.get("P_VAL"));
						rst.put("MTRL_OUT2_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("MTRL_OUT2", "");

				// Material 3Type
				isVal = false;
				for (Map<String, Object> m : material3OutRstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("MTRL_OUT3", m.get("P_VAL"));
						rst.put("MTRL_OUT3_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("MTRL_OUT3", "");

				// Material 4Type
				isVal = false;
				for (Map<String, Object> m : material4OutRstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("MTRL_OUT4", m.get("P_VAL"));
						rst.put("MTRL_OUT4_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("MTRL_OUT4", "");

				// Plan Type
				isVal = false;
				for (Map<String, Object> m : planRstList) {
					if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
						rst.put("PLAN", m.get("P_VAL"));
						rst.put("PLAN_ADD_INFO",
								m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
						isVal = true;
						break;
					}
				}
				if (!isVal)
					rst.put("PLAN", "");
				rstList.add(rst);
			}
		}

		fp.put("FINAL_LIST", rstList);

	}

	/**
	 * Arrangement 와 Configuration 에 따라 API 결합
	 * 
	 * @param sBasePlan
	 * @param sAddPlan
	 * @param procRstList
	 * @return
	 * @throws Exception
	 */
	private String setApiConfig(String sBasePlan, String sAddPlan, int iPIdx, Map<String, Object> fp, String sDirPlan)
			throws Exception {

		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		// Plan별 Arrangement, configuration 정보
		List<Map<String, Object>> planConfigList = rBMapper.selectRuleComListE002(null);

		String[] sBasePlans = sBasePlan.trim().split("/"); // Base Plan
		String[] sAddPlans = sAddPlan.trim().split("/"); // 추가할 Plan

		ArrayList<String> sBasePlanList = new ArrayList<String>(Arrays.asList(sBasePlans));
		ArrayList<String> sAddPlanList = new ArrayList<String>(Arrays.asList(sAddPlans));
		ArrayList<Map<String, String>> replaceList = new ArrayList<Map<String, String>>();

		System.out.println("setApiConfig : sBasePlanList : " + sBasePlanList);
		System.out.println("setApiConfig : sAddPlanList : " + sAddPlanList);

		// sAddPlanType
		// "" : 일반
		// 1 : 사용자 지정 Plan

		// sDirPlan : 사용자 지정 Plan
		// 우선시 적용 조건 적용 필요 : 1CW, 20미만의 지정 Plan에 대하여 동일 조건에서 우선적용처리

		// -------------------------
		// 비정상적인 Plan을 제거한다.
		// -------------------------
		// sBasePlan에 대하여 plan 오류 체크
		for (Iterator<String> iterator = sBasePlanList.iterator(); iterator.hasNext();) {
			String p1 = iterator.next().trim();

			// if("".equals(p1)) continue;

			boolean bIs = false;
			for (Map<String, Object> p2 : planConfigList) { // Plan 기준정보 리스트
				if (p1.equals(StringUtil.get(p2.get("ATTR4")))) { // plan코드 기준과 비교
					bIs = true;
					break;
				}
			}
			if (!bIs) { // 기준에 없는 plan이면 삭제
				if (!"".equals(p1))
					setResultProcList(procRstList, iPIdx, "Plan 기준에 없는 정보입니다 : " + p1);
				iterator.remove();
			}
		}

		// sAddPlan에 대하여 plan 오류 체크
		for (Iterator<String> iterator = sAddPlanList.iterator(); iterator.hasNext();) {
			String p1 = iterator.next().trim();
			boolean bIs = false;
			for (Map<String, Object> p2 : planConfigList) { // Plan 기준정보 리스트
				if (p1.equals(StringUtil.get(p2.get("ATTR4")))) { // plan코드 기준과 비교
					bIs = true;
					break;
				}
			}
			if (!bIs) { // 기준에 없는 plan이면 삭제
				if (!"".equals(p1))
					setResultProcList(procRstList, iPIdx, "Plan 기준에 없는 정보입니다 : " + p1);
				iterator.remove();
			}
		}

		// --------------------------------------------------
		// 기존 Plan에 붙이고자 하는 plan의 조건 확인 후 Add
		// --------------------------------------------------
		for (String addPlan : sAddPlanList) {
			if (!sBasePlanList.contains(addPlan)) { // 포함되지 않은 Plan이 들어올 경우

				// 결합 가능유무 확인
				boolean bIsConn = true;
				// 사용자 지정Plan 유무
				boolean bIsUserDirPlan = false;
				String sUserDirPlan = ""; // 사용자 지정Plan 적용 값

				// plan 변경정보
				Map<String, String> replaceMap = null;

				// 연결하고자 하는 Plan의 configuration
				String sAddConfiguration = "";
				String sAddConfigurationArrangement = "";
				for (Map<String, Object> pm : planConfigList) {
					if (addPlan.equals(StringUtil.get(pm.get("ATTR4")))) {
						sAddConfiguration = "" + pm.get("ATTR2") + pm.get("ATTR3");
						sAddConfigurationArrangement = "" + pm.get("ATTR1");
						break;
					}
				}

				// 현재 Plan 정보의 configuration과 비교
				for (String sBasePlan_tmp : sBasePlanList) {

					String sBaseConfiguration_tmp = "";
					String sBaseConfigurationArrangement_tmp = "";
					for (Map<String, Object> pm : planConfigList) {
						if (sBasePlan_tmp.equals(StringUtil.get(pm.get("ATTR4")))) {
							sBaseConfiguration_tmp = "" + pm.get("ATTR2") + pm.get("ATTR3");
							sBaseConfigurationArrangement_tmp = "" + pm.get("ATTR1");
							break;
						}
					}

					// Base Plan이 사용자 지정Plan에 속하는지 체크
					for (String sDirPlanTmp : sDirPlan.split("/")) {
						if (sBasePlan_tmp.equals(sDirPlanTmp)) {
							bIsUserDirPlan = true;
							sUserDirPlan = sDirPlanTmp;
							break;
						}
					}

					// 추가하는 Plan이 사용자 지정Plan에 속하는지 체크
					for (String sDirPlanTmp : sDirPlan.split("/")) {
						if (addPlan.equals(sDirPlanTmp)) {
							bIsUserDirPlan = true;
							sUserDirPlan = sDirPlanTmp;
							break;
						}
					}

					// configuration이 같은 경우 기본적으로 사용 불가
					// 예외사항 추가
					// 1 번 제한
					if (sAddConfiguration.equals(sBaseConfiguration_tmp)) {
						if ("13".equals(addPlan) && ("11".equals(sBasePlan_tmp) || "12".equals(sBasePlan_tmp)
								|| "21".equals(sBasePlan_tmp) || "31".equals(sBasePlan_tmp))) {
							// skip
						} else if ("13".equals(sBasePlan_tmp) && ("11".equals(addPlan) || "12".equals(addPlan)
								|| "21".equals(addPlan) || "31".equals(addPlan))) {
							// skip
						} else if ("1CW".equals(sAddConfiguration)) {

							if (!"".equals(sDirPlan)) { // 사용자지정 Plan이 있을 경우

								if (NumberUtil.toInt(addPlan) > NumberUtil.toInt(sBasePlan_tmp)) {

									// plan 숫자가 20 미만일 경우는 사용자 지정 Plan을 우선 적용
									// 사용자지정Plan : sBasePlan_tmp
									if (bIsUserDirPlan && NumberUtil.toInt(addPlan) < 20
											&& NumberUtil.toInt(sBasePlan_tmp) < 20) {
										replaceMap = new HashMap<String, String>();
										replaceMap.put("base_api", sBasePlan_tmp);
										replaceMap.put("add_api", sUserDirPlan); // 사용자지정 Plan으로 고정
										replaceList.add(replaceMap);
										bIsConn = false;

									} else {
										replaceMap = new HashMap<String, String>();
										replaceMap.put("base_api", sBasePlan_tmp);
										replaceMap.put("add_api", addPlan);
										replaceList.add(replaceMap);
										bIsConn = false;
									}

								} else {
									bIsConn = false;
								}

							} else {
								if (NumberUtil.toInt(addPlan) > NumberUtil.toInt(sBasePlan_tmp)) {
									replaceMap = new HashMap<String, String>();
									replaceMap.put("base_api", sBasePlan_tmp);
									replaceMap.put("add_api", addPlan);
									replaceList.add(replaceMap);
									bIsConn = false;
								} else {
									bIsConn = false;
								}
							}

						} else if ("3CW".equals(sAddConfiguration) && addPlan.compareTo(sBasePlan_tmp) > 0) {
							replaceMap = new HashMap<String, String>();
							replaceMap.put("base_api", sBasePlan_tmp);
							replaceMap.put("add_api", addPlan);
							replaceList.add(replaceMap);
							bIsConn = false;
						} else {
							bIsConn = false;
						}
					}

					// 2,3 번 제한
					if ((sAddConfiguration.startsWith("2") && sBaseConfiguration_tmp.startsWith("3"))
							|| (sAddConfiguration.startsWith("3") && sBaseConfiguration_tmp.startsWith("2"))) {
						bIsConn = false;
						// 4 번 제한
					} else if ((sAddConfiguration.startsWith("2CW") && sBaseConfiguration_tmp.startsWith("2NC"))
							|| (sAddConfiguration.startsWith("2NC") && sBaseConfiguration_tmp.startsWith("2CW"))) {
						bIsConn = false;
						// 5번 제한
					} else if ((sAddConfiguration.startsWith("3CW") && sBaseConfiguration_tmp.startsWith("3NC"))
							|| (sAddConfiguration.startsWith("3NC") && sBaseConfiguration_tmp.startsWith("3CW"))) {
						bIsConn = false;
					}

					// 6번제한 사항
					if ((("Q".equals(sAddConfigurationArrangement) && "--".equals(sAddConfiguration))
							&& "3NC".equals(sBaseConfiguration_tmp))
							|| (("Q".equals(sBaseConfigurationArrangement_tmp) && "--".equals(sBaseConfiguration_tmp))
									&& "3NC".equals(sAddConfiguration))) {
						bIsConn = false;
					}

					if ((("Q".equals(sAddConfigurationArrangement) && "1CW".equals(sAddConfiguration))
							&& !("1".equals(sBaseConfigurationArrangement_tmp) && "1CW".equals(sBaseConfiguration_tmp)))
							|| (("Q".equals(sBaseConfigurationArrangement_tmp) && "1CW".equals(sBaseConfiguration_tmp))
									&& !("1".equals(sAddConfigurationArrangement)
											&& "1CW".equals(sAddConfiguration)))) {
						bIsConn = false;
					}

				} // end for(String sBasePlan_tmp : sBasePlanList) {

				// 결합 가능하면 결합
				if (bIsConn) {
					sBasePlanList.add(addPlan);
				} else {
					if (replaceMap == null) {
						// 중간진행사항
						setResultProcList(procRstList, 0, "API Configuration 적용 불가 : " + addPlan);
					}
				}
			}
		}

		// 변경해야할 Plan을 여기서 변경

		// 변경정보가 있을 경우
		if (replaceList.size() > 0) {
			for (Iterator<String> iterator = sBasePlanList.iterator(); iterator.hasNext();) {
				String s = iterator.next();

				// 삭제해야할 api plan remove
				for (Map<String, String> rm : replaceList) {
					if (rm.get("base_api").equals(s)) {
						iterator.remove();
					}
				}
			}

			// 추가할 api plan add
			for (Map<String, String> rm : replaceList) {
				sBasePlanList.add(rm.get("add_api"));
			}
		}

		// sBasePlanList.sort(c);

		// sort and conn
		String[] array = sBasePlanList.toArray(new String[sBasePlanList.size()]);
		Arrays.sort(array);
		String sResultPlan = "";
		for (String s : array) {
			if (sResultPlan.length() > 0) {
				sResultPlan = sResultPlan + "/";
			}
			sResultPlan = sResultPlan + s;
		}

		System.out.println("------------------sResultPlan------------------->>> : " + sResultPlan);
		return sResultPlan;
	}

	private void setApiPlanOutboard(List<Map<String, Object>> planRstList, int iPIdx, String sOutPlan,
			Map<String, Object> fp) throws Exception {

		System.out.println("setApiPlanOutboard iPIdx : " + iPIdx);
		System.out.println("setApiPlanOutboard planRstList size : " + planRstList.size());
		System.out.println("setApiPlanOutboard planRstList : " + planRstList);

		boolean bIsSetPlan = false;

		for (Iterator<Map<String, Object>> iterator = planRstList.iterator(); iterator.hasNext();) {
			Map<String, Object> m = iterator.next();

			if (iPIdx == NumberUtil.toInt(m.get("P_IDX"))) {
				String sInnerPlan = StringUtil.get(m.get("P_VAL"));
				if (!"".equals(sInnerPlan)) {
					m.put("P_VAL", sInnerPlan + "/" + sOutPlan);
				} else {
					m.put("P_VAL", sOutPlan);
				}
				bIsSetPlan = true;
			}
		}

//		for(Map<String,Object> m : planRstList) {
//			
//			System.out.println("m 1 : " + m);
//			
//			
//			if(iPIdx == NumberUtil.toInt(m.get("P_IDX"))) {
//				String sInnerPlan = StringUtil.get(m.get("P_VAL"));
//				if (!"".equals(sInnerPlan)) {
//					m.put("P_VAL",sInnerPlan + "/"+sOutPlan);
//				}else {
//					m.put("P_VAL",sOutPlan);
//				}
//				bIsSetPlan=true;
//			}
//			
//			System.out.println("m 2 : " + m);
//		}
		if (!bIsSetPlan) {
			setResultListPlan(planRstList, iPIdx, sOutPlan, null, fp);
		}

		System.out.println("setApiPlanOutboard end");

	}

	private void setOutboardSealType(List<Map<String, Object>> sealRstList, int iPIdx, String sSealType) {
		for (Map<String, Object> sm : sealRstList) {
			if (NumberUtil.toInt(sm.get("P_IDX")) == iPIdx) {
				String sSealType_cur = StringUtil.get(sm.get("P_VAL"));

				if (sSealType_cur.contains("/")) {
					sm.put("P_VAL", sSealType_cur.substring(0, sSealType_cur.indexOf("/")) + "/" + sSealType);
				}
			}
		}
	}

	private String[] getSealType(Map<String, Object> item, String SealType) throws Exception {
		String[] sSeals = null;

		if (SealType.split("/").length == 1 && NumberUtil.toInt(item.get("ARRANGENT")) >= 2) {
			sSeals = new String[2];
			sSeals[0] = SealType;
			sSeals[1] = SealType;
		} else {
			sSeals = SealType.split("/");
		}

		return sSeals;
	}

	private String getS_MTRL(Map<String, Object> fp, int iPIdx) throws Exception {

		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");

		String sS_MTRL = "";
		List<Map<String, Object>> list = null;
		for (int i = 0; i < 8; i++) {
			if ("".equals(sS_MTRL)) {

				if (i == 0)
					list = material1RstList;
				else if (i == 1)
					list = material2RstList;
				else if (i == 2)
					list = material3RstList;
				else if (i == 3)
					list = material4RstList;
				else if (i == 4)
					list = material1OutRstList;
				else if (i == 5)
					list = material2OutRstList;
				else if (i == 6)
					list = material3OutRstList;
				else if (i == 7)
					list = material4OutRstList;

				for (Map<String, Object> m : list) {
					if (iPIdx == NumberUtil.toInt(m.get("P_IDX"))) {
						if (m.get("ADD_INFO") == null)
							continue;
						Map<String, Object> am = (HashMap<String, Object>) m.get("ADD_INFO");
						sS_MTRL = StringUtil.get(am.get("S_MTRL"));
					}
				}
			}
		}

		return sS_MTRL;
	}

	private double getSealSize(Map<String, Object> item, String sUnit) throws Exception {
		double dSealSize = 0;
		dSealSize = NumberUtil.toDouble(item.get("SEAL_SIZE")); // inch
		// [작업필요] - Seal Size가 없을 경우 Shaft Size를 가지고 기준표 참조하여 적용
		// Shaft size 단위는 MM로 변환되어 Item에 적재되면 기본값을 가지고 있음.

		// 입력된 Seal Size가 없을 경우
		if (dSealSize == 0) {
			if ("IN".equals(sUnit)) {
				// dSealSize = (Math.round(NumberUtil.toDouble(item.get("SHAFT_SIZE")) *
				// 0.0393701 * 1000.0))/1000.0;
				dSealSize = (Math.round(NumberUtil.toDouble(item.get("SHAFT_SIZE")) / 25.4 * 1000.0)) / 1000.0;
			} else {
				dSealSize = NumberUtil.toDouble(item.get("SHAFT_SIZE"));
			}
		}
		return dSealSize;
	}

	private double getSealSize(Map<String, Object> item, String sReturnUnit, String sSealType, String sSealTypeFull,
			String sSizeType, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		double dSealSize = 0;
		dSealSize = NumberUtil.toDouble(item.get("SEAL_SIZE")); // inch 입력인자에서 지정된 Seal Size
		// Seal Size가 없을 경우 Shaft Size를 가지고 기준표 참조하여 적용
		// Shaft size 단위는 MM로 변환되어 Item에 적재되 기본값을 가지고 있음.

		// 입력된 Seal Size가 없을 경우
		// 1. Seal Type 과 Shaft size 사이즈 관계에 따라 Seal Size를 설정한다.
		// - Face Mean Size가 있을 경우 Seal Szie보다 우선 적용한다.
		// 2. 1에서 값이 정해지지 않을 경우 Shaft Siz로 Seal Size를 설정

		if (dSealSize == 0) {

			// Cartridge 선택
			// API682=Y => Cartridge가 기본
			// API682=N => Mon-Cartridge가 기본
			String sCartridgeType = StringUtil.get(item.get("CARTRIDGE_TYPE"));
			if ("".equals(sCartridgeType)) {
				if ("Y".equals(StringUtil.get(item.get("API682_YN")))) {
					sCartridgeType = "Z160010"; // 값이 없을 경우 cartridge로 설정
				} else {
					sCartridgeType = "Z160020"; // 값이 없을 경우 Mon-cartridge로 설정
				}
			}

			// Seal Size Main 정보 조회
			// Seal Type 전체를 기준으로 1차 조회
			Map<String, Object> ptm = new HashMap<String, Object>();
			ptm.put("SEAL_TYPE", sSealTypeFull);
			ptm.put("CARTRIDGE_TYPE", sCartridgeType);
			List<Map<String, Object>> mList = rBMapper.getRbSealSizeMain(ptm);

			Map<String, Object> mSealSizeMap = null;
			if (!mList.isEmpty()) {
				// SIZE_MAPP_CD, SEAL_SIZE_UNIT, SHAFT_SIZE_UNIT, SHAFT_SIZE_SCH_GB,
				// SHAFT_SIZE_MIN, SHAFT_SIZE_MAX
				mSealSizeMap = mList.get(0);
			} else {
				ptm.put("SEAL_TYPE", sSealType);
				mList = rBMapper.getRbSealSizeMain(ptm);
				if (!mList.isEmpty()) {
					mSealSizeMap = mList.get(0);
				}
			}

			// Seal Size Sub 정보 조회
			if (mSealSizeMap != null) {

				ptm.clear();
				ptm.put("SIZE_MAPP_CD", mSealSizeMap.get("SIZE_MAPP_CD"));
				ptm.put("SHAFT_SIZE_SCH_GB", mSealSizeMap.get("SHAFT_SIZE_SCH_GB"));

				String sSealSizeUnit = StringUtil.get(mSealSizeMap.get("SEAL_SIZE_UNIT"));
				String sShaftSizeUnit = StringUtil.get(mSealSizeMap.get("SHAFT_SIZE_UNIT"));
				double dShaftSizeMin = NumberUtil.toDouble(mSealSizeMap.get("SHAFT_SIZE_MIN"));
				double dShaftSizeMax = NumberUtil.toDouble(mSealSizeMap.get("SHAFT_SIZE_MAX"));

				double dShaftSize = 0.0d;

				System.out.println("Seal Size : Shaft Size : " + StringUtil.get(item.get("SHAFT_SIZE_O")));
				System.out.println("Seal Size : dShaftSize Min/Max : " + dShaftSizeMin + "/" + dShaftSizeMax);

				// Shaft Size 단위가 입력인자와 같은 조건일 경우
				if (sShaftSizeUnit.equals(StringUtil.get(item.get("SHAFT_SIZE_UNIT")))) {
					dShaftSize = NumberUtil.toDouble(item.get("SHAFT_SIZE_O")); // 입력된 값 그대로 사용
				} else {

					if ("IN".equals(sShaftSizeUnit)) { // mm to in
						dShaftSize = Math.round((NumberUtil.toDouble(item.get("SHAFT_SIZE")) / 25.4) * 1000.0) / 1000.0;
					} else { // in to mm
						dShaftSize = Math.round((NumberUtil.toDouble(item.get("SHAFT_SIZE")) * 25.4) * 1000.0) / 1000.0;
					}

					System.out.println("Seal Size : ShaftSize 단위변환 : " + dShaftSize);
				}

				// Seal Szie 구하기
				if ((dShaftSizeMin != 0 && dShaftSize < dShaftSizeMin)
						|| (dShaftSizeMax != 0 && dShaftSize > dShaftSizeMax)) {
					dSealSize = 0;

					// Seal Size 범위 초과 Note 표시
					setResultNoteList(noteRstList, 0, "Seal Size Min/Max 값 초과: " + sSealType);

				} else {

					if ("3".equals(StringUtil.get(mSealSizeMap.get("SHAFT_SIZE_SCH_GB")))) { // Shaft Size와 동일
						dSealSize = NumberUtil.toDouble(item.get("SHAFT_SIZE"));
						dSealSize = getSealSizeCnv(dSealSize, "MM", sReturnUnit);
					} else {
						ptm.put("SHAFT_SIZE", dShaftSize);
						List<Map<String, Object>> sList = rBMapper.getRbSealSizeSub(ptm);

						Map<String, Object> sSealSizeMap = null;
						if (!sList.isEmpty()) {
							sSealSizeMap = sList.get(0);

							// SEAL_SIZE1 : inboard
							// SEAL_SIZE2 : outboard
							// SEAL_SIZE_DIS

							if (NumberUtil.toDouble(sSealSizeMap.get("SEAL_SIZE2")) == 0) {

								if ("2".equals(sSizeType)) {// Face Mean Size가 있는 경우 우선적용
									if (NumberUtil.toDouble(sSealSizeMap.get("FACE_MEAN_SIZE1")) != 0) {
										dSealSize = NumberUtil.toDouble(sSealSizeMap.get("FACE_MEAN_SIZE1"));
									} else {
										dSealSize = NumberUtil.toDouble(sSealSizeMap.get("SEAL_SIZE1"));
									}
								} else {
									dSealSize = NumberUtil.toDouble(sSealSizeMap.get("SEAL_SIZE1"));
								}

							} else {

								// 이부분 데이터 발생 시 Face Mean Size에 대한 로직 체크 필요

								// inboard, outboard 체크
								String[] sealTmp = sSealTypeFull.split("/");
								if (sealTmp.length > 1 && sSealType.equals(sealTmp[1])) { // Outboard에 해당될때
									dSealSize = NumberUtil.toDouble(sSealSizeMap.get("SEAL_SIZE2"));
								} else {
									dSealSize = NumberUtil.toDouble(sSealSizeMap.get("SEAL_SIZE1"));
								}
							}
							dSealSize = getSealSizeCnv(dSealSize, sSealSizeUnit, sReturnUnit);
							
						} else {
							// Seal Size 범위 초과 Note 표시
							Map<String,Object> addNote = new HashMap<String,Object>();
							addNote.put("SEAL", sSealType);
							setResultNoteList(noteRstList, 0, "Seal Size 기준 범위 초과 : " + sSealType, "s", addNote);
							//System.out.println("Seal Size : Seal Size 기준 범위 초과");
						}
					}
				}
			}

			// Seal Size가 없을 경우 Shaft Size로 대체
			if (dSealSize == 0) {
				dSealSize = NumberUtil.toDouble(item.get("SHAFT_SIZE"));
				dSealSize = getSealSizeCnv(dSealSize, "MM", sReturnUnit);
			}

			//System.out.println("Seal Size : 결정 Seal Size : " + dSealSize);
		}
		return dSealSize;
	}

	/**
	 * Seal Size 변환 MM, IN로만 단위가 들어온다고 가정함.
	 * 
	 * @param size
	 * @param sUnit
	 * @param sChgUnit
	 * @return
	 */
	private double getSealSizeCnv(double size, String sUnit, String sChgUnit) {
		double dSize = 0;
		if (sUnit.equals(sChgUnit)) {
			dSize = size;
		} else {
			if ("IN".equals(sChgUnit)) { // mm to in
				dSize = (Math.round(size / 25.4 * 1000.0)) / 1000.0;
			} else { // in to mm
				dSize = (Math.round(size * 25.4 * 1000.0)) / 1000.0;
			}
		}
		return dSize;
	}

	private int getNextIdx(Map<String, Object> fp) throws Exception {
		int iPIdx = 0;
//		List<Map<String,Object>> sealRstList = (List<Map<String,Object>>)fp.get("sealRstList");
//		for(Map<String,Object> m : sealRstList) {
//			if(iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
//				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
//			}
//		}

		List<Map<String, Object>> list = null;
		list = (List<Map<String, Object>>) fp.get("sealRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material1RstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material2RstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material3RstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material4RstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material1OutRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material2OutRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material3OutRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material4OutRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("planRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}

		return ++iPIdx;
	}

	private int getMaxIdx(Map<String, Object> fp) throws Exception {
		int iPIdx = 0;

		List<Map<String, Object>> list = null;
		list = (List<Map<String, Object>>) fp.get("sealRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material1RstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material2RstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material3RstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material4RstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material1OutRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material2OutRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material3OutRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("material4OutRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}
		list = (List<Map<String, Object>>) fp.get("planRstList");
		for (Map<String, Object> m : list) {
			if (iPIdx < NumberUtil.toInt(m.get("P_IDX"))) {
				iPIdx = NumberUtil.toInt(m.get("P_IDX"));
			}
		}

		return iPIdx;
	}

	/**
	 * 재질코드 to 재질Digit
	 * 
	 * @param materialList
	 * @param sMaterialPartType
	 * @param sMaterialCode
	 * @return
	 */
	private String getMaterialDigit(String sMaterialPartType, String sMaterialCode) {
		String sDigit = "";
		for (Map<String, Object> m : _materialList) {
			if ("FS".equals(String.valueOf(m.get("MTRL_TYPE")))
					&& sMaterialPartType.equals(String.valueOf(m.get("PART_TYPE")))
					&& sMaterialCode.equals(String.valueOf(m.get("MTRL_CD")))) {
				sDigit = String.valueOf(m.get("DIGIT"));
			}
		}
		return sDigit;
	}

	/**
	 * 재질Digit to 재질코드
	 * 
	 * @param sMaterialPartType
	 * @param sMaterialDigit
	 * @return
	 */
	private String getMaterialCd(String sMaterialPartType, String sMaterialDigit) {
		String sCd = "";
		if ("Y".equals(sMaterialDigit)) {
			return "";
		} else {
			for (Map<String, Object> m : _materialList) {
				if ("FS".equals(String.valueOf(m.get("MTRL_TYPE")))
						&& sMaterialPartType.equals(String.valueOf(m.get("PART_TYPE")))
						&& sMaterialDigit.equals(String.valueOf(m.get("DIGIT")))) {
					sCd = String.valueOf(m.get("MTRL_CD"));
				}
			}
			return sCd;
		}
	}

	/**
	 * 재질코드 to 재질명
	 * 
	 * @param sMaterialPartType
	 * @param sMaterialCode
	 * @return
	 */
	private String getMaterialNm(String sMaterialPartType, String sMaterialCode) {
		String sNM = "";
		for (Map<String, Object> m : _materialList) {
			if ("FS".equals(String.valueOf(m.get("MTRL_TYPE")))
					&& sMaterialPartType.equals(String.valueOf(m.get("PART_TYPE")))
					&& sMaterialCode.equals(String.valueOf(m.get("MTRL_CD")))) {
				sNM = String.valueOf(m.get("MTRL_NM"));
			}
		}
		return sNM;
	}

	/**
	 * 현재 조건에 해당하는 Chloride Metal 정보를 반환한다.
	 * 
	 * @param item
	 * @return
	 */
	private String[] getChlorideMetal(Map<String, Object> item) throws Exception {
		String[] sMtrlCd = null;

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		// A8번 그래프 체크

		// A9번 그래프 체크

		// 두 그래프의 결과가 상이하면 상위 재질을 적용
		// 재질 우선순위 : 5(DB) -> F(DX) -> 9(NL)

		double dChlorideCont = getProductCont(item, "CHLORIDE", "PPM"); // chloride 농도
		double dPH = NumberUtil.toDouble(item.get("PH")); // PH
		double dTemp = NumberUtil.toDouble(item.get("TEMP_MAX")); // 온도

		Map<String, Object> gf_param = new HashMap<String, Object>();
		List<Map<String, Object>> graphFunclist = null;
		String graphFunc = "";
		double dGraphFuncVal = 0.0d;

		String sMtrlCd1 = "", sMtrlCd2 = "";

		System.out.println("dChlorideCont : " + dChlorideCont);
		System.out.println("dPH : " + dPH);
		System.out.println("dTemp : " + dTemp);

		// A8 그래프에서 재질 설정
		if (dPH > 1) {

			// 316 SS가 허용되는지 먼저 체크 (5:DB)
			gf_param.clear();
			gf_param.put("GRAPH_NO", "A8");
			gf_param.put("CURV_NO", "1");
			gf_param.put("VAL", dChlorideCont); // Chloride 농도
			graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);

			if (graphFunclist.size() > 0) {
				graphFunc = StringUtil.get(((Map<String, Object>) graphFunclist.get(0)).get("FUNC"));
				graphFunc = graphFunc.replace("x", "" + dChlorideCont);
				dGraphFuncVal = NumberUtil.toDouble(engine.eval(graphFunc));

				System.out.println("sMtrlCd1 A8 1 dGraphFuncVal : " + dGraphFuncVal);

				if (dGraphFuncVal != 0 && dGraphFuncVal < dPH) {
					sMtrlCd1 = "DB";
				}
			}

			System.out.println("sMtrlCd1 A8 1번 그래프 기준적용 : " + sMtrlCd1);

			// DB가 허용되지 않을 경우 CD4MCu(F:DX)가 허용되는지 체크
			if ("".equals(sMtrlCd1)) {
				gf_param.clear();
				gf_param.put("GRAPH_NO", "A8");
				gf_param.put("CURV_NO", "2");
				gf_param.put("VAL", dChlorideCont); // Chloride 농도
				graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);

				if (graphFunclist.size() > 0) {
					graphFunc = StringUtil.get(((Map<String, Object>) graphFunclist.get(0)).get("FUNC"));
					graphFunc = graphFunc.replace("x", "" + dChlorideCont);
					dGraphFuncVal = NumberUtil.toDouble(engine.eval(graphFunc));

					System.out.println("sMtrlCd1 A8 2 dGraphFuncVal : " + dGraphFuncVal);

					if (dGraphFuncVal != 0 && dGraphFuncVal < dPH) {
						sMtrlCd1 = "DX";
					}
				}

				System.out.println("sMtrlCd1 A8 2번 그래프 기준적용 : " + sMtrlCd1);
			}

			// 위두조건에서 모두 재질이 정해지지 않으면 HASTELLOY(9:NL) 설정
			if ("".equals(sMtrlCd1)) {
				sMtrlCd1 = "NL";
			}

			System.out.println("sMtrlCd1 A8 3번 그래프 기준적용 : " + sMtrlCd1);

		}

		// -----------------------
		// A9 1번 그래프 체크
		// -----------------------

		// 316 SS가 허용되는지 먼저 체크 (5:DB)
		gf_param.clear();
		gf_param.put("GRAPH_NO", "A9");
		gf_param.put("CURV_NO", "1");
		gf_param.put("VAL", dChlorideCont); // Chloride 농도
		graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);

		if (graphFunclist.size() > 0) {
			graphFunc = StringUtil.get(((Map<String, Object>) graphFunclist.get(0)).get("FUNC"));
			graphFunc = graphFunc.replace("x", "" + dChlorideCont);
			dGraphFuncVal = NumberUtil.toDouble(engine.eval(graphFunc));

			System.out.println("sMtrlCd2 A9 1 graphFunc : " + graphFunc);
			System.out.println("sMtrlCd2 A9 1 dGraphFuncVal : " + dGraphFuncVal);

			if (dGraphFuncVal != 0 && dGraphFuncVal > dTemp) {
				sMtrlCd2 = "DB";
			}
		}

		System.out.println("sMtrlCd1 A9 1번 그래프 기준적용 : " + sMtrlCd2);

		// DB가 허용되지 않을 경우 CD4MCu(F:DX)가 허용되는지 체크
		if ("".equals(sMtrlCd2)) {
			gf_param.clear();
			gf_param.put("GRAPH_NO", "A9");
			gf_param.put("CURV_NO", "2");
			gf_param.put("VAL", dChlorideCont); // Chloride 농도
			graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);

			if (graphFunclist.size() > 0) {
				graphFunc = StringUtil.get(((Map<String, Object>) graphFunclist.get(0)).get("FUNC"));
				graphFunc = graphFunc.replace("x", "" + dChlorideCont);
				dGraphFuncVal = NumberUtil.toDouble(engine.eval(graphFunc));

				System.out.println("sMtrlCd2 A9 2 graphFunc : " + graphFunc);
				System.out.println("sMtrlCd2 A9 2 dGraphFuncVal : " + dGraphFuncVal);

				if (dGraphFuncVal != 0 && dGraphFuncVal > dTemp) {
					sMtrlCd2 = "DX";
				}
			}
		}

		System.out.println("sMtrlCd1 A9 2번 그래프 기준적용 : " + sMtrlCd2);

		// 위두조건에서 모두 재질이 정해지지 않으면 HASTELLOY(9:NL) 설정
		if ("".equals(sMtrlCd2)) {
			gf_param.clear();
			gf_param.put("GRAPH_NO", "A9");
			gf_param.put("CURV_NO", "3");
			gf_param.put("VAL", dChlorideCont); // Chloride 농도
			graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);

			if (graphFunclist.size() > 0) {
				graphFunc = StringUtil.get(((Map<String, Object>) graphFunclist.get(0)).get("FUNC"));
				graphFunc = graphFunc.replace("x", "" + dChlorideCont);
				dGraphFuncVal = NumberUtil.toDouble(engine.eval(graphFunc));

				System.out.println("sMtrlCd2 A9 3 graphFunc : " + graphFunc);
				System.out.println("sMtrlCd2 A9 3 dGraphFuncVal : " + dGraphFuncVal);

				if (dGraphFuncVal != 0 && dGraphFuncVal > dTemp) {
					sMtrlCd2 = "NL";
				}
			}
		}
		System.out.println("sMtrlCd1 A9 3번 그래프 기준적용 : " + sMtrlCd2);

		// A8,A9 결과에 따라 Metal 재질 선정
		String[] sMtrlSet = new String[] { "DB", "DX", "NL" };

		int iMtrl1 = 0, iMtrl2 = 0;
		for (int i = 0; i < sMtrlSet.length; i++) {
			if (sMtrlCd1.equals(sMtrlSet[i])) {
				iMtrl1 = i;
			}
			if (sMtrlCd2.equals(sMtrlSet[i])) {
				iMtrl2 = i;
			}
		}

		int iMtrl = Math.max(iMtrl1, iMtrl2);
		if (iMtrl == 0)
			sMtrlCd = new String[] { "DB", "DX", "NL" };
		else if (iMtrl == 1)
			sMtrlCd = new String[] { "DX", "NL" };
		else if (iMtrl == 2)
			sMtrlCd = new String[] { "NL" };

		return sMtrlCd;
	}

	private String getMaterialGrp(String s) {
		String sRtn = s;

		if (s.equals("GU") || s.equals("X537") || s.equals("VX") || s.equals("G016")) {
			sRtn = "[FKM]";
		} else if (s.equals("AD") || s.equals("TW") || s.equals("GG") || s.equals("X675") || s.equals("FP")
				|| s.equals("G002") || s.equals("G005") || s.equals("G006") || s.equals("G014")) {
			sRtn = "[FFKM]";
		} else if ("MG".equals(s)) {
			sRtn = "[EPDM]";
		} else if ("LV".equals(s) || "RI".equals(s)) {
			sRtn = "[TUC]";
		} else if ("SL".equals(s) || "YO".equals(s)) {
			sRtn = "[SIC]";
		} else if ("KR3".equals(s) || "GE".equals(s) || "AE".equals(s) || "KI".equals(s) || "OH".equals(s)
				|| "F007".equals(s) || "F008".equals(s) || "F015".equals(s) || "F017".equals(s)) {
			sRtn = "[RESIN CARBON]";
		} else if ("AP".equals(s) || "RY".equals(s)) {
			sRtn = "[ANTIMONY CARBON]";
		}
		return sRtn;
	}

	private String getMaterialDetail(String s) {
		String sRtn = s;
		if ("[FKM]".equals(s)) {
			sRtn = "GU";
		} else if ("[FFKM]".equals(s)) {
			sRtn = "X675,AD,G005,G002,G006";
		} else if ("[SIC]".equals(s)) {
			sRtn = "SL,YO";
		} else if ("[TUC]".equals(s)) {
			sRtn = "LV,RI";
		} else if ("[RESIN CARBON]".equals(s)) {
			sRtn = "KR3,GE";
		} else if ("[ANTIMONY CARBON]".equals(s)) {
			sRtn = "RY,AP";
		}
		return sRtn;
	}

	// C1 체크 시 공통적으로 사용되는 재질에 대한 변경처리
	private String getC1SearchMtrlCode(String sMtrlType, String sMtrlCd) {
		String sRtn = sMtrlCd;

		if ("3".equals(sMtrlType)) {
			if ("[FKM]".equals(getMaterialGrp(sMtrlCd))) {
				sRtn = "GU";
			} else if ("G015".equals(getMaterialGrp(sMtrlCd))) { // NBR(X614)
				sRtn = "GS"; // Nitrile(NBR)
			}
		} else if ("2".equals(sMtrlType) || "4".equals(sMtrlType)) {

			// Face 재질 Grade 체크는 4번째 재질도 2번째 재질과 같은 조건으로 체크
			// C1 Material Guide에는 M2 데이터로만 있음.

			if ("[RESIN CARBON]".equals(getMaterialGrp(sMtrlCd))) {
				sRtn = "GE";
			} else if ("AP".equals(sMtrlCd)) { // face 에서 W는 U로 변경하여 C1 체크 (Antimony Carbon)
				sRtn = "RY";
			}
		}

		return sRtn;
	}

	/**
	 * Plan으로 Arrangement를 체크하여 반환
	 * 
	 * @param item
	 * @param iPIdx
	 * @param fp
	 * @return
	 */
	private int getArrangement(String sPlan) throws Exception {
		// Plan별 Arrangement, configuration 정보
		List<Map<String, Object>> planConfigList = rBMapper.selectRuleComListE002(null);
		int ia = 0;
		for (String p1 : sPlan.split("/")) {
			for (Map<String, Object> p2 : planConfigList) { // Plan 기준정보 리스트
				if (p1.equals(StringUtil.get(p2.get("ATTR4")))) { // plan코드 기준과 비교
					if (ia < NumberUtil.toInt(p2.get("ATTR2"))) {
						ia = NumberUtil.toInt(p2.get("ATTR2"));
					}
				}
			}
		}
		return ia;
	}

	private int getSealArrangement(Map<String, Object> item, List<Map<String, Object>> sealRstList, int iPIdx)
			throws Exception {

		int iArrangement = 0;
		for (Map<String, Object> sm : sealRstList) {
			if (iPIdx == NumberUtil.toInt(sm.get("P_IDX"))) {
				Map<String, Object> sm_addInfo = getMapMapData(sm, "ADD_INFO");
				iArrangement = NumberUtil.toInt(sm_addInfo.get("ARRANGEMENT"));
				break;
			}
		}
		if (iArrangement == 0) {
			iArrangement = NumberUtil.toInt(item.get("ARRANGEMENT"));
		}
		return iArrangement;
	}

	private String[] getSealConfig(String sSeal) throws Exception {

		String[] sReturnValue = null;

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("SEAL_TYPE", sSeal);
		List<Map<String, Object>> c3List = rBMapper.selectRuleC3(param);

		for (Map<String, Object> c3 : c3List) {
			String[] c3_seals = StringUtil.get(c3.get("SEAL_TYPE")).trim().split(",");
			for (String c3_seal : c3_seals) {
				if (sSeal.equals(c3_seal)) {
					sReturnValue = (StringUtil.get(c3.get("SEAL_CONFIG"))).split("/");
				}
			}
		}
		return sReturnValue;
	}

	private String[] getSepMtrlInfo(String sType, String sMtrlCd) throws Exception {
		List<String> sRtnMtrlCd = new ArrayList<String>();
		if ("1".equals(sType)) {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("MCD", "E006");
			param.put("ATTR1", sMtrlCd);
			List<Map<String, Object>> list = rBMapper.selectRuleComListType1(param);
			if (!list.isEmpty()) {
				for (Map<String, Object> m : list) {
					if (!"".equals(StringUtil.get(m.get("ATTR2")))) {
						sRtnMtrlCd.add(StringUtil.get(m.get("ATTR2")));
					}
				}
			} else {
				sRtnMtrlCd.add(sMtrlCd);
			}
		} else {
			sRtnMtrlCd.add(sMtrlCd);
		}
		return sRtnMtrlCd.toArray(new String[sRtnMtrlCd.size()]);
	}

	/**
	 * C3에서 사용할 압력값을 반환
	 * 
	 * @param item
	 * @param iPIdx
	 * @param sInOut
	 * @param fp
	 * @return
	 * @throws Exception
	 */
	private double getC3CheckPress(Map<String, Object> item, int iPIdx, String sInOut, Map<String, Object> fp)
			throws Exception {
		double dPress = 0.0d;

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");

		int iArrangement = 0;
		for (Map<String, Object> sm : sealRstList) {

			if (iPIdx == NumberUtil.toInt(sm.get("P_IDX"))) {

				Map<String, Object> sm_addInfo = getMapMapData(sm, "ADD_INFO");

				// 첫번째 Seal로 체크할경우
				// 자체가 Dual Seal인 경우 Outboard 기준으로 체크(확인필요)
				if ("IN".equals(sInOut)) {
					// Dual Seal인 경우 Outboard 기준으로 체크
					Map<String, Object> param = new HashMap<String, Object>();
					param.put("MCD", "E004");
					param.put("ATTR1", (StringUtil.get(sm.get("P_VAL"))).split("/")[0]);
					List<Map<String, Object>> rComList = getRuleComListType1(param);
					String sArrChkNote = "";
					if (!rComList.isEmpty()) {
						Map<String, Object> rc = rComList.get(0);
						String sSD_gb = StringUtil.get(rc.get("ATTR2")); // Seal 자체가 싱글 또는 듀얼인 경우 체크
						if ("D".equals(sSD_gb)) { // 자체가 듀얼로만 쓰는 경우
							sInOut = "OUT";
						}
					}
				}

				iArrangement = NumberUtil.toInt(sm_addInfo.get("ARRANGEMENT"));
				if (iArrangement == 0) {
					iArrangement = NumberUtil.toInt(item.get("ARRANGEMENT"));
				}

				if (iArrangement == 3) {

					// A3 53B 53A 그 외
					// In 15 1.4 1.4
					// Out 씰챔버+15 씰챔버+1.4 씰챔버+1.4

					// 변경 : 21.03.23
					// A3 53B 53A 그 외 (53A 기준과 동일)
					// In Psc.Max + 15 - Psc.Nor Psc.Max + 1.4 - Psc.Nor
					// Out Psc.Max + 15 Psc.Max + 1.4

					boolean b53B = false;
					boolean b53A = false;

					for (Map<String, Object> am : planRstList) {
						if (iPIdx == NumberUtil.toInt(am.get("P_IDX"))) {
							for (String s : StringUtil.get(am.get("P_VAL")).split("/")) {
								if ("53B".equals(s)) {
									b53B = true;
									break;
								}
							}
							if (b53B)
								break;
						}
					}

					if (!b53B) {
						for (Map<String, Object> am : planRstList) {
							if (iPIdx == NumberUtil.toInt(am.get("P_IDX"))) {
								for (String s : StringUtil.get(am.get("P_VAL")).split("/")) {
									if ("53A".equals(s)) {
										b53A = true;
										break;
									}
								}
								if (b53A)
									break;
							}
						}
					}

					if (b53B) {
						if ("OUT".equals(sInOut)) {
							// dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 15;
							dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 15;
						} else {
							// dPress = 15;
							dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 15
									- NumberUtil.toDouble(item.get("SEAL_CHAM_NOR"));
						}
					} else if (b53A) {
						if ("OUT".equals(sInOut)) {
							// dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 1.4;
							dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 1.4;
						} else {
							// dPress = 1.4;
							dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 1.4
									- NumberUtil.toDouble(item.get("SEAL_CHAM_NOR"));
						}
					} else { // 53A와 동일하게 처리
						if ("OUT".equals(sInOut)) {
							// dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 1.4;
							dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 1.4;
						} else {
							// dPress = 1.4;
							dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 1.4
									- NumberUtil.toDouble(item.get("SEAL_CHAM_NOR"));
						}
					}

				} else if (iArrangement == 2) {
					if ("OUT".equals(sInOut)) {
						dPress = 0;
					} else { // IN
						dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"));
					}
				} else if (iArrangement == 1) {
					dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"));
				} else {
					dPress = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"));
				}

			}
		}

		return dPress;
	}

	private Map<String, Object> getMapMapData(Map<String, Object> m, String sKey) {
		Map<String, Object> returnMap = null;
		if (m.get(sKey) == null) {
			returnMap = new HashMap<String, Object>();
		} else {
			returnMap = (HashMap<String, Object>) m.get(sKey);
		}
		return returnMap;
	}

	private Map<String, Object> getC3CheckParam(Map<String, Object> item, String sSeal, String sSealFull,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		Map<String, Object> c3Param = (HashMap<String, Object>) ((HashMap<String, Object>) item).clone(); // item Param
																											// 복사

		c3Param.put("SEAL_TYPE", sSeal);

		double dSealSize_mm = getSealSize(item, "MM", sSeal, sSealFull, "1", fp); // Seal Size (mm)
		c3Param.put("SEAL_SIZE", dSealSize_mm);

		setResultProcList(procRstList, 0, "Seal Size [" + sSeal + "] : " + dSealSize_mm + " MM");

		c3Param.put("type", item.get("ABC_TYPE"));

		double dSealSize_mm2 = getSealSize(item, "MM", sSeal, sSealFull, "2", fp); // Seal Size (mm) - 주속 계산용
		// 속도(ft/s) : 3.14 * Seal Dia(mm) * 0.00328084 * RPM / 60
		c3Param.put("L_SPD_NOR", 0);
		c3Param.put("L_SPD_MIN", 0);
		c3Param.put("L_SPD_MAX", 3.14 * dSealSize_mm2 * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속

		return c3Param;
	}

	private boolean isVOC(String[] saProductGroup, String[] saProduct) throws Exception {
		boolean isVOC = false;

		// VOC 목록
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("MCD", "E001");
		List<Map<String, Object>> rComList = getRuleComListType1(param);

		// VOC 체크
		// 입력된 Product 중 Voc 리스트에 속하는 항목이 있는 지 체크
		for (String s : saProductGroup) {

			if ("".equals(s) || "-".equals(s)) {
				continue;
			}

			for (Map<String, Object> m : rComList) {
				if (s.equals(StringUtil.get(m.get("ATTR1")).toUpperCase())) { // Product Group명 체크
					isVOC = true;
					break;
				}
			}
		}

		return isVOC;
	}

	private boolean isPlan(String sPlan, String sPlanDir) throws Exception {
		boolean isPlan = false;
		for (String s : sPlanDir.split("/")) {
			if (s.equals(sPlan)) {
				isPlan = true;
				break;
			}
		}
		return isPlan;
	}

	static boolean c3Flag = false;

	/**
	 * C3 허용 범위를 체크한다. 모든범위를 체크
	 * 
	 * @param param
	 * @param sSealType
	 * @return
	 * @throws Exception
	 */
	private boolean isC3OperatingCheck(Map<String, Object> param, String sSealType) throws Exception {
		// C3 Operation 체크
		boolean bIsOk = false;
		c3Flag = false;
		param.put("SEAL_TYPE", sSealType);

		List<Map<String, Object>> c3List = rBMapper.selectRuleC3(param);

		if (!c3List.isEmpty()) {
			for (Map<String, Object> m : c3List) {
				String seals = StringUtil.get(m.get("SEAL_TYPE"));
				for (String seal : seals.split(",")) {
					if (sSealType.equals(seal)) {
						bIsOk = true;
						break;
					}
				}
			}

		} else {
			String[] arr = { "TEMP_NOR", "TEMP_MAX", "TEMP_MIN", "SEAL_CHAM_NOR", "SEAL_CHAM_MAX", "SEAL_CHAM_MIN",
					"VISC_NOR", "VISC_MIN", "VISC_MAX", "SPEC_GRAVITY_MIN", "SPEC_GRAVITY_MAX", "SPEC_GRAVITY_NOR",
					"RPM_MIN", "RPM_MAX", "RPM_NOR", "L_SPD_MIN", "L_SPD_MAX", "L_SPD_NOR", "SEAL_CHAM_NOR_O",
					"SEAL_CHAM_MIN_O", "SEAL_CHAM_MAX_O" };
			boolean[] visited = new boolean[arr.length];

			for (int r = 1; r <= 3; r++) {
				if (c3Flag) {
					break;
				}
				comb1(arr, visited, 0, r, param);
			}
		}

		return bIsOk;
	}

	private void comb1(String[] arr, boolean[] visited, int start, int r, Map<String, Object> param) throws Exception {
		if (r == 0) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("SEAL_TYPE", param.get("SEAL_TYPE"));
			if (param.containsKey("API682_TYPE")) {
				map.put("API682_TYPE", param.get("API682_TYPE"));
			}
			if (param.containsKey("API682_RS")) {
				map.put("API682_RS", param.get("API682_RS"));
			}
			if (param.containsKey("ARRANGEMENT")) {
				map.put("ARRANGEMENT", param.get("ARRANGEMENT"));
			}
			if (param.containsKey("END_USER_SEAL_TYPE")) {
				map.put("END_USER_SEAL_TYPE", param.get("END_USER_SEAL_TYPE"));
			}
			if (param.containsKey("END_USER_HDO_TYPE_C_COND")) {
				map.put("END_USER_HDO_TYPE_C_COND", param.get("END_USER_HDO_TYPE_C_COND"));
			}
			if (param.containsKey("SEAL_GB_TYPE")) {
				map.put("SEAL_GB_TYPE", param.get("SEAL_GB_TYPE"));
			}
			if (param.containsKey("SLURRY_SEAL_YN")) {
				map.put("SLURRY_SEAL_YN", param.get("SLURRY_SEAL_YN"));
			}
			if (param.containsKey("CONFIGURATION")) {
				map.put("CONFIGURATION", param.get("CONFIGURATION"));
			}
			map.put("SEAL_SIZE", param.get("SEAL_SIZE"));
			List<Map<String, Object>> c3List = rBMapper.selectRuleC3(map);
			if (c3List.size() == 0) {
				System.out.println("씰 싸이즈 문제");
				param.put("C3_EXCEPTION", "Seal Size");
				c3Flag = true;
				return;
			} else {
				if (param.containsKey("SHAFT_SIZE")) {
					map.put("SHAFT_SIZE", param.get("SHAFT_SIZE"));
				} else {
					map.put("SHAFT_SIZE", 0);
				}
				c3List = rBMapper.selectRuleC3(map);
				if (c3List.size() == 0) {
					System.out.println("쌰프트 싸이즈 문제");
					param.put("C3_EXCEPTION", "Shaft Size");
					c3Flag = true;
					return;
				} else {
					List<String> expList = new ArrayList<String>();
					int cnt = 0;
					for (int i = 0; i < arr.length; i++) {
						if (visited[i] == true) {
							expList.add(arr[i]);
							if (arr[i].equals("SEAL_CHAM_MIN_O")) {
								map.put("VAP_PRES_MIN_O", param.get("VAP_PRES_MIN_O"));
							} else if (arr[i].equals("SEAL_CHAM_MAX_O")) {
								map.put("VAP_PRES_MAX_O", param.get("VAP_PRES_MAX_O"));
							} else if (arr[i].equals("SEAL_CHAM_NOR_O")) {
								map.put("VAP_PRES_NOR_O", param.get("VAP_PRES_NOR_O"));
							}
							map.put(arr[i], param.get(arr[i]));
						}
					}
					c3List = rBMapper.selectRuleC3(map);
					if (map.containsKey("VAP_PRES_NOR_O")) {
						System.out.println(c3List);
					}
					if (c3List.size() == 0) {
						System.out.println("다른 문제");
						System.out.println(expList);
						param.put("C3_EXCEPTION", String.join(",", expList).replace("TEMP", "Temperature")
								.replace("SEAL_CHAM_NOR_0", "Vapor Margin").replace("SEAL_CHAM_MIN_0", "Vapor Margin")
								.replace("SEAL_CHAM_MAX_0", "Vapor Margin").replace("SEAL_CHAM_NOR_O", "Vapor Margin")
								.replace("SEAL_CHAM_MIN_O", "Vapor Margin").replace("SEAL_CHAM_MAX_O", "Vapor Margin")
								.replace("SEAL_CHAM", "Seal Chamber Pressure").replace("VISC", "Viscosity")
								.replace("SPEC_GRAVITY", "Specific Gravity").replace("RPM", "Shaft Speed")
								.replace("L_SPD", "Shaft Speed").replace("_NOR", "(Normal)").replace("_MAX", "(Max)")
								.replace("_MIN", "(Min)"));
						c3Flag = true;
					}
					return;
				}
			}
		} else {
			for (int i = start; i < arr.length; i++) {
				if (c3Flag) {
					break;
				} else {
					visited[i] = true;
					comb1(arr, visited, i + 1, r - 1, param);
					visited[i] = false;
				}

			}
		}
	}

	/**
	 * seal model 선정 process에서 C3 체크 시 Size에 해당하는 범위가 없을 경우 Note 후 True 반환
	 * 
	 * @param item
	 * @param sSealType
	 * @param sSealTypeFull
	 * @param iIdx
	 * @param fp
	 * @return
	 * @throws Exception
	 */
	private boolean isC3OperatingCheck2(Map<String, Object> item, String sSealType, String sSealTypeFull, int iIdx,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		boolean bIs = false;

		double dShaftSize = NumberUtil.toDouble(item.get("SHAFT_SIZE"));

		// c3 체크 시 seal type 및 seal size로 해당하는 정보가 있는지 먼저 체크
		double dSealSize = getSealSize(item, "MM", sSealType, sSealTypeFull, "1", fp);
		Map<String, Object> c3Param = new HashMap<String, Object>();
		c3Param.put("SEAL_TYPE", sSealType);
		c3Param.put("SEAL_SIZE", dSealSize); // mm 로 변경
		c3Param.put("SHAFT_SIZE", dShaftSize); // mm 로 변경
		c3Param.put("type", item.get("ABC_TYPE")); // type
		List<Map<String, Object>> c3List = rBMapper.selectRuleC3(c3Param);

		boolean bIsSize = false;
		for (Map<String, Object> c3 : c3List) {
			String s = StringUtil.get(c3.get("SEAL_TYPE"));
			for (String ss : s.split(",")) {
				if (ss.equals(sSealType)) {
					bIsSize = true;
					break;
				}
			}
		}

		// Size가 해당하는 정보가 있을 경우 Operation Window 체크
		if (bIsSize) {
			c3Param = (HashMap<String, Object>) ((HashMap<String, Object>) item).clone(); // item Parma 복사
			c3Param.put("SEAL_TYPE", sSealType);
			c3Param.put("SEAL_SIZE", dSealSize); // mm 로 변경
			c3Param.put("SHAFT_SIZE", dShaftSize); // mm 로 변경
			c3Param.put("type", item.get("ABC_TYPE")); // type

			// 속도(ft/s) : 3.14 * Seal Dia(mm) * 0.00328084 * RPM / 60
			c3Param.put("L_SPD_NOR", 0);
			c3Param.put("L_SPD_MIN", 0);
			c3Param.put("L_SPD_MAX", 3.14 * dSealSize * 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60); // 주속

			c3List = rBMapper.selectRuleC3(c3Param);

			boolean bIsOperOk = false;
			for (Map<String, Object> c3 : c3List) {
				String s = StringUtil.get(c3.get("SEAL_TYPE"));
				for (String ss : s.split(",")) {
					if (ss.equals(sSealType)) {
						bIsOperOk = true;
						break;
					}
				}
			}
			bIs = bIsOperOk;

		} else {
			// 해당하는 Size구간이 없습니다
			setResultNoteList(noteRstList, iIdx, "Seal/Shaft Size에 해당하는 Operating Window 구간이 없습니다 : " + sSealType);
			setResultProcList(procRstList, iIdx,
					"Seal/Shaft Size에 해당하는 Operating Window 구간이 없습니다 : " + sSealType + " : " + dSealSize + " MM");
			bIs = true;
		}

		return bIs;
	}

	private boolean isISC2SeriesChk(List<Map<String, Object>> sealRstList, int iPIdx) throws Exception {
		// (ISC2-PP, ISC2-BB, ISC2-682PP, ISC2-682BB)
		// __seal_model_type_A_api_2__ -> Plan 설정에 따라 이후 ISC2 시리즈로 설정될 예정
		// __seal_model_cartridge_api_1__ -> Plan 설정에 따라 이후 A2 이상일때 : ISC2-BB
		// __seal_model_cartridge_api_2__ -> Plan 설정에 따라 이후 A2 이상일때 : ISC2-PP
		boolean bIs = false;
		for (Map<String, Object> m : sealRstList) {
			if (iPIdx == NumberUtil.toInt(m.get("P_IDX"))) {
				System.out.println("isISC2SeriesChk : " + m.get("P_VAL"));
				if ("ISC2-PP".equals(StringUtil.get(m.get("P_VAL"))) || "ISC2-BB".equals(StringUtil.get(m.get("P_VAL")))
						|| "ISC2-682PP".equals(StringUtil.get(m.get("P_VAL")))
						|| "ISC2-682BB".equals(StringUtil.get(m.get("P_VAL")))
						|| "__seal_model_type_A_api_2__".equals(StringUtil.get(m.get("P_VAL")))
						|| "__seal_model_cartridge_api_1__".equals(StringUtil.get(m.get("P_VAL")))
						|| "__seal_model_cartridge_api_2__".equals(StringUtil.get(m.get("P_VAL")))) {
					bIs = true;
					break;
				}
			}
		}
		return bIs;
	}

	private boolean isQBWPressChk(Map<String, Object> item, ScriptEngine engine, String sGno, double dSealChamPress,
			double dSealSize) throws Exception {

		boolean bIs = false;

		// Seal Chamber Press (psig)
		// double dSealChamPres_psig = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) /
		// 0.069;
		double dSealChamPres_psig = dSealChamPress / 0.069;

		// Graph 조회
		Map<String, Object> gf_param = new HashMap<String, Object>();
		gf_param.put("GRAPH_NO", "A2"); // Graph Type
		gf_param.put("VAL", dSealSize); // seal size // Inch
		gf_param.put("CURV_NO", sGno); // Graph No.

		List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);

		String sFunc = "";
		if (grapFunchList.size() > 0) {
			sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
		}
		System.out.println("sFunc : " + sFunc);

		sFunc = sFunc.replace("x", "" + dSealSize); //
		double dLimitPress = NumberUtil.toDouble(engine.eval(sFunc));

		System.out.println("dSealChamPres_psig : " + dSealChamPres_psig + " , dLimitPress : " + dLimitPress);

		// 압력 제한범위를 초과하면 (씰챔버압력)
		if (dLimitPress < dSealChamPres_psig) {
			bIs = true;
		}

		return bIs;
	}

	/**
	 * 체크 //a. Psc - V.P ≥ 3.5 bar -> barg로 맞춤 (BARA로도 같은 결과)
	 * 
	 * @param item
	 * @return
	 */
	private boolean isVPMchk1(Map<String, Object> item) {

		boolean is = false;
		boolean is_nor = true, is_min = true, is_max = true;

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_NOR_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_NOR_O")))) {
			if ((NumberUtil.toDouble(item.get("SEAL_CHAM_NOR_O"))
					- (NumberUtil.toDouble(item.get("VAP_PRES_NOR_O")) - 1)) >= 3.5) {
				is_nor = true;
			} else {
				is_nor = false;
			}
		}

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_MIN_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_MIN_O")))) {
			if ((NumberUtil.toDouble(item.get("SEAL_CHAM_MIN_O"))
					- (NumberUtil.toDouble(item.get("VAP_PRES_MIN_O")) - 1)) >= 3.5) {
				is_min = true;
			} else {
				is_min = false;
			}
		}

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_MAX_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_MAX_O")))) {
			if ((NumberUtil.toDouble(item.get("SEAL_CHAM_MAX_O"))
					- (NumberUtil.toDouble(item.get("VAP_PRES_MAX_O")) - 1)) >= 3.5) {
				is_max = true;
			} else {
				is_max = false;
			}
		}

		if (is_nor && is_min && is_max)
			is = true;

		return is;
	}

	/**
	 * 체크 Psc ≥ V.P X 1.3 -> 절대압력 기준
	 * 
	 * @param item
	 * @return
	 */
	private boolean isVPMchk2(Map<String, Object> item) {

		boolean is = false;
		boolean is_nor = true, is_min = true, is_max = true;

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_NOR_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_NOR_O")))) {
			if ((NumberUtil.toDouble(item.get("SEAL_CHAM_NOR_O"))
					+ 1) >= (NumberUtil.toDouble(item.get("VAP_PRES_NOR_O")) * 1.3)) {
				is_nor = true;
			} else {
				is_nor = false;
			}
		}

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_MIN_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_MIN_O")))) {
			if ((NumberUtil.toDouble(item.get("SEAL_CHAM_MIN_O"))
					+ 1) >= (NumberUtil.toDouble(item.get("VAP_PRES_MIN_O")) * 1.3)) {
				is_min = true;
			} else {
				is_min = false;
			}
		}

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_MAX_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_MAX_O")))) {
			if ((NumberUtil.toDouble(item.get("SEAL_CHAM_MAX_O"))
					+ 1) >= (NumberUtil.toDouble(item.get("VAP_PRES_MAX_O")) * 1.3)) {
				is_max = true;
			} else {
				is_max = false;
			}
		}

		if (is_nor && is_min && is_max)
			is = true;

		return is;
	}

	/**
	 * c. VPM 0.34 ~ 3.4bar 이내 -> 단위맞춤 Barg
	 * 
	 * @param item
	 * @return
	 */
	private boolean isVPMchk3(Map<String, Object> item) {

		boolean is = false;
		boolean is_nor = true, is_min = true, is_max = true;

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_NOR_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_NOR_O")))) {
			if (NumberUtil.toDouble(item.get("SEAL_CHAM_NOR_O"))
					- (NumberUtil.toDouble(item.get("VAP_PRES_NOR_O")) - 1) >= 0.34
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_NOR_O"))
							- (NumberUtil.toDouble(item.get("VAP_PRES_NOR_O")) - 1) <= 3.4) {
				is_nor = true;
			} else {
				is_nor = false;
			}
		}

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_MIN_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_MIN_O")))) {
			if (NumberUtil.toDouble(item.get("SEAL_CHAM_MIN_O"))
					- (NumberUtil.toDouble(item.get("VAP_PRES_MIN_O")) - 1) >= 0.34
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN_O"))
							- (NumberUtil.toDouble(item.get("VAP_PRES_MIN_O")) - 1) <= 3.4) {
				is_min = true;
			} else {
				is_min = false;
			}
		}

		if (!"".equals(StringUtil.get(item.get("SEAL_CHAM_MAX_O")))
				&& !"".equals(StringUtil.get(item.get("VAP_PRES_MAX_O")))) {
			if (NumberUtil.toDouble(item.get("SEAL_CHAM_MAX_O"))
					- (NumberUtil.toDouble(item.get("VAP_PRES_MAX_O")) - 1) >= 0.34
					&& NumberUtil.toDouble(item.get("SEAL_CHAM_MAX_O"))
							- (NumberUtil.toDouble(item.get("VAP_PRES_MAX_O")) - 1) <= 3.4) {
				is_max = true;
			} else {
				is_max = false;
			}
		}

		if (is_nor && is_min && is_max)
			is = true;

		return is;
	}

	private boolean isSealConfig(Map<String, Object> item, String sSealType, String sConfig) throws Exception {
		boolean bIs = false;

		// C3에서 Config 체크
		Map<String, Object> param = new HashMap<String, Object>();

		String sSealType_tmp = "";
		if ("__seal_model_cartridge_api_2__".equals(sSealType)) {
			sSealType_tmp = "ISC2-PP"; // A2 이상 으로 가정함.
		} else if ("__seal_model_cartridge_api_1__".equals(sSealType)) {
			sSealType_tmp = "ISC2-BB"; // A2 이상 으로 가정함.
		} else {
			if (sSealType.contains("/")) {
				sSealType_tmp = sSealType.split("/")[0];
			} else {
				sSealType_tmp = sSealType;
			}
		}

		param.put("SEAL_TYPE", sSealType_tmp); // SealType
		param.put("CONFIGURATION", sConfig); // Configuration

		List<Map<String, Object>> ruleC3List = rBMapper.selectRuleC3(param);
		if (ruleC3List.size() > 0) {
			bIs = true;
		}

		return bIs;
	}

	private boolean isC1GuideProduct(Map<String, Object> item, String[] saProduct, String[] saProductGroup,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");

		boolean bIs = true;

		if (saProduct.length == 0 || (saProduct.length == 1 && "".equals(saProduct[0]))) {
			setResultNoteList(noteRstList, 0, "Product Group 정보 없는 유체 : " + item.get("PRODUCT"), 
					"c", null);
			return false;
		}

		Map<String, Object> param = new HashMap<String, Object>();
		List<Map<String, Object>> list = null;
		String sProductTmp = "";
		
		for (String sProduct : saProduct) {

			// C1체크 유체가 아닐경우 Skip
			if (!isC1CheckFluid(sProduct))
				continue;

			sProductTmp = sProduct;
			param.clear();
			param.put("PRODUCT", sProductTmp);
			list = rBMapper.selectRuleC1ChkIsData(param);

			if (list.size() == 0) {

				// 상위그룹으로 재조회
				sProductTmp = getProductGrp(sProduct);
				param.clear();
				param.put("PRODUCT", sProductTmp);
				list = rBMapper.selectRuleC1ChkIsData(param);

				// 최종적으로 데이터가 없는경우
				if (list.size() == 0) {
					bIs = false;
					setResultNoteList(noteRstList, 0,
							"Material Guide 정보에 없는 유체 : " + sProduct + "(" + sProductTmp + ")",
							"c", null);
				}
			}

			// C1체크 유체일 경우 
			// - 농도, 온도, 구분자에 따른 체크
			if (bIs) {
				
				param.clear();
				sProductTmp = sProduct;
				param.put("PRODUCT", sProductTmp);
				param.put("GB", getProductGb(item, sProduct));
				param.put("CONT", getProductCont(item, sProduct, "%"));
				// param.put("TEMP_MIN",item.get("TEMP_MIN")); //온도조건 제외.21.03.26
				// param.put("TEMP_MAX",item.get("TEMP_MAX")); //온도조건 제외.21.03.26
				list = rBMapper.selectRuleC1ChkIsData(param);

				if (list.size() == 0) {
					
					// 상위그룹으로 재조회
					sProductTmp = getProductGrp(sProduct);
					param.put("PRODUCT", sProductTmp);
					list = rBMapper.selectRuleC1ChkIsData(param);
					
					// 최종적으로 데이터가 없는경우 Note
					if (list.size() == 0) {
						bIs = false;
						setResultNoteList(noteRstList, 0, "Material Guide 정보를 조회하기 위한 구분값/농도 범위에 없는 유체 : " + sProduct + "(" + sProductTmp + ")", 
								"c", null);
					}
				}
			}
		}

		return bIs;
	}

	/**
	 * 제품(Product)그룹 정보에 정보 포함유무 체크
	 * 
	 * @param product
	 * @param saProductGroup
	 * @return
	 */
	private boolean isProduct(String product, String[] saProductGroup, String[] saProduct) {
		boolean isProduct = false;
		product = product.toUpperCase();// 대문자

		// water base 그룹여부 체크
		if ("[WATER-BASE]".equals(product)) {

			// 등록된 Product의 그룹 목록에 WATER 키워드를 가진 그룹이 있는지를 체크
			// ex) Product Grp : WASTE WATER , WATER - SALT
			for (String p : saProductGroup) {
				if (p.contains("WATER")) {
					isProduct = true;
					break;
				}
			}

		} else if ("[RESIDUE]".equals(product)) {

			for (String p : saProductGroup) {
				if (p.contains("RESIDUE")) {
					isProduct = true;
					break;
				}
			}

		} else {
			// Product 명으로 체크
			for (String p : saProduct) {
				if (product.equals(p)) {
					isProduct = true;
					break;
				}
			}

			// Product로 체크되지 않은 경우 Group명으로 체크
			if (!isProduct) {
				for (String p : saProductGroup) {

					// product 그룹이 없는 경우 SKip한다.
					// 아래 Lv3 체크 시 빈값이 있는 경우가 있어 조건에 걸릴수가 있음
					if ("".equals(p))
						continue;

					if (product.equals(p)) {
						isProduct = true;
						break;
					}

					// Product Group의 Hierarchy로도 체크
					for (Map<String, Object> m : _productGroupHierInfo) {
						if (p.equals(String.valueOf(m.get("LV3")))) { // 3레벨이 있는 경우
							if (product.equals(String.valueOf(m.get("LV1")))) { // 최상위 그룹명과 체크
								isProduct = true;
								break;
							}
						}

						if (p.equals(String.valueOf(m.get("LV2")))) { // 2레벨이 있는 경우
							if (product.equals(String.valueOf(m.get("LV1")))) { // 최상위 그룹명과 체크
								isProduct = true;
								break;
							}
						}
					}

					if (isProduct)
						break;
				}
			}
		}

		return isProduct;
	}

	private boolean isC1CheckFluid(String product) throws Exception {
		boolean bIs = true;

		if ("CHLORIDE".equals(product)) {
			bIs = false;
		} else if ("CHLORIDE".equals(getProductGrp(product))) {
			bIs = false;
		}

		return bIs;
	}

	/**
	 * Seal Model 추천 진행여부
	 * 
	 * @param item
	 * @return
	 */
	private boolean isSealProcess(Map<String, Object> item, Map<String, Object> fp) {
		if ("Y".equals(item.get("__IS_PRODUCT_WATER_GUIDE"))) { // Water Guide 적용시 Seal Model 추천X
			//사용자지정 Arrangement가 2,3이면 Water Guide의 표준인 1보다 상위요건이므로 Seal을 다시 설정할 필요가 있음.21.10.22
			int iDirArrangement = NumberUtil.toInt(item.get("API_PLAN_DIR_ARRANGEMENT"));
			if(iDirArrangement >=2) {
				
				List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList"); // SealList
				// 이 후 SealType 설정에서 
				// R_TYPE : 2 And   (2 : Prefered에서 설정된 Seal 추천정보) 
				// Seal 값이 빈값일 경우 추천된 Seal 정보로 빈칸을 채우게함. 21.10.22
				for(Map<String,Object> sm : sealRstList) {
					Map<String, Object> sm_addInfo = getMapMapData(sm, "ADD_INFO");
					if(sm_addInfo.get("PRE_TYPE").equals("WATER")) {  //water guide 설정 Seal 정보
						sm.put("P_VAL", "");
					}
				}
				
				return true;
			}else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * 추천결과 Array에 추가
	 */
	private int setResultList(List<Map<String, Object>> list, int iIdx, Object val, Map<String, Object> addInfo,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");// 추천결과 중간과정이력 List
		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList"); // SealList

		// 값이 없이 넘어올경우 Skip
		if (val == null || "".equals(String.valueOf(val).trim()))
			return iIdx;

		// -------------------------------------
		// idx -1, -2 는 Seal 일경우 예외처리임.
		// -------------------------------------
		
		// idx = -1
		// 현재 설정된 목록의 최대 idx를 확인 후 해당 idx까지 비어있는 항목에 대하여 seal 정보로 채운다.
		if (iIdx == -1) {

			// R_TYPE = 2인 Seal정보가 있을 경우 추가하지 않는다.
			boolean bIsAddSeal = true;
			for (Map<String, Object> sm : sealRstList) {
				Map<String, Object> sm_addInfo = sm.get("ADD_INFO") == null ? new HashMap<String, Object>()
						: (HashMap<String, Object>) sm.get("ADD_INFO");
				if ("2".equals(sm_addInfo.get("R_TYPE"))) {
					bIsAddSeal = false;
					
					/////
					if(StringUtil.get(sm.get("P_VAL")).equals("")) {
						sm.put("P_VAL", String.valueOf(val).trim());
					}
					
					break;
				}
			}

			if (bIsAddSeal) {
				int iNewIdx = getNextIdx(fp);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("P_IDX", iNewIdx);
				m.put("P_SEQ", getMaxSeq(list, iNewIdx));
				m.put("P_VAL", String.valueOf(val).trim());
				m.put("ADD_INFO", addInfo);
				// list.add(m);
				list.add(m);
				
				
				addList("S",iNewIdx,String.valueOf(val).trim());
				
			}

		} else if (iIdx == -2) {
			// 비교대상 Seal(직전 추가된 마지막 seal)을 확인 후 Seal 이외의 나머지 정보를 설정

			iIdx = getNextIdx(fp); // 추가할 자신의 idx

			// Seal 등록
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("P_VAL", String.valueOf(val).trim());
			m.put("ADD_INFO", addInfo);
			list.add(m);

			addList("S",iIdx,String.valueOf(val).trim());

		} else {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));

			if ("[BLANK]".equals(String.valueOf(val))) { // 빈값으로 등록 필요 시
				m.put("P_VAL", "");
			} else {
				m.put("P_VAL", String.valueOf(val).trim());
			}

			m.put("ADD_INFO", addInfo);
			list.add(m);

			addList("S",iIdx,String.valueOf(val).trim());
		}

		return iIdx;
	}

	/**
	 * 추천결과 Array에 추가
	 */
	private int setResultListPlan(List<Map<String, Object>> list, int iIdx, Object val, Map<String, Object> addInfo,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");// 추천결과 중간과정이력 List
		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList"); // SealList

		// 값이 없이 넘어올경우 Skip
		if (val == null || "".equals(String.valueOf(val).trim()))
			return iIdx;

		// idx -1, -2 는 Seal 일경우 예외처리임.

		// idx = -1
		// 현재 설정된 목록의 최대 idx를 확인 후 해당 idx까지 비어있는 항목에 대하여 seal 정보로 채운다.
		if (iIdx == -1) {

			// R_TYPE = 2인 Seal정보가 있을 경우 추가하지 않는다.
			boolean bIsAddSeal = true;
			for (Map<String, Object> sm : sealRstList) {
				Map<String, Object> sm_addInfo = sm.get("ADD_INFO") == null ? new HashMap<String, Object>()
						: (HashMap<String, Object>) sm.get("ADD_INFO");
				if ("2".equals(sm_addInfo.get("R_TYPE"))) {
					bIsAddSeal = false;
					break;
				}
			}

			if (bIsAddSeal) {
				int iNewIdx = getNextIdx(fp);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("P_IDX", iNewIdx);
				m.put("P_SEQ", getMaxSeq(list, iNewIdx));
				m.put("P_VAL", String.valueOf(val).trim());
				m.put("ADD_INFO", addInfo);
				// list.add(m);
				list.add(m);

				addList("P",iNewIdx,String.valueOf(val).trim());
			}

		} else if (iIdx == -2) {
			// 비교대상 Seal(직전 추가된 마지막 seal)을 확인 후 Seal 이외의 나머지 정보를 설정

			iIdx = getNextIdx(fp); // 추가할 자신의 idx

			// Seal 등록
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("P_VAL", String.valueOf(val).trim());
			m.put("ADD_INFO", addInfo);
			list.add(m);

			addList("P",iIdx,String.valueOf(val).trim());

		} else {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));

			if ("[BLANK]".equals(String.valueOf(val))) { // 빈값으로 등록 필요 시
				m.put("P_VAL", "");
			} else {
				m.put("P_VAL", String.valueOf(val).trim());
			}

			m.put("ADD_INFO", addInfo);
			list.add(m);
			
			addList("P",iIdx,String.valueOf(val).trim());
		}

		return iIdx;
	}

	/**
	 * 추천 Plan 결과 Array에 추가
	 */
	private int setPlanResultList(List<Map<String, Object>> list, int iIdx, Object val, Map<String, Object> addInfo,
			Map<String, Object> fp) throws Exception {

		// 1. 직접입력받은 Plan 이 있을수 있음. - 여기서 무시
		// 2. Inboard로 입력된 Plan
		// 3. Outboard로 입력된 Plan
		// 4. Plan Configuration 규칙에 따라 조합 필요

		// -2 : 비교대상 Seal(직전 추가된 마지막 seal)을 확인 후 Seal 이외의 나머지 정보를 설정
		// 그 외 : idx에 plan 추가
		if (iIdx == -2) {
			// 비교대상 Seal(직전 추가된 마지막 seal)을 확인 후 Seal 이외의 나머지 정보를 설정

			iIdx = getNextIdx(fp); // 추가할 자신의 idx

			int targetIdx = 0; // 정보를 가져올 idx

			for (Map<String, Object> sm : list) { // Seal List
				if (targetIdx < NumberUtil.toInt(sm.get("P_IDX"))) {
					targetIdx = NumberUtil.toInt(sm.get("P_IDX"));
				}
			}

			List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");// Seal
			List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
			List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
			List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
			List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
			List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
			List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
			List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
			List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer
			List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList"); // plan

			List<Map<String, Object>> listTmp = null;
			for (int i = 0; i < 9; i++) {
				if (i == 0)
					listTmp = sealRstList;
				else if (i == 1)
					listTmp = material1RstList;
				else if (i == 2)
					listTmp = material2RstList;
				else if (i == 3)
					listTmp = material3RstList;
				else if (i == 4)
					listTmp = material4RstList;
				else if (i == 5)
					listTmp = material1OutRstList;
				else if (i == 6)
					listTmp = material2OutRstList;
				else if (i == 7)
					listTmp = material3OutRstList;
				else if (i == 8)
					listTmp = material4OutRstList;

				List<Map<String, Object>> addListTemp = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> m1 : listTmp) {
					if (targetIdx == NumberUtil.toInt(m1.get("P_IDX"))) {
						Map<String, Object> m_tmp = new HashMap<String, Object>();
						Map<String, Object> m_add_tmp = null;
						if (m1.get("ADD_INFO") != null)
							m_add_tmp = (HashMap<String, Object>) ((HashMap<String, Object>) m1.get("ADD_INFO"))
									.clone();
						m_tmp.put("P_IDX", iIdx);
						m_tmp.put("P_SEQ", getMaxSeq(listTmp, iIdx));
						m_tmp.put("P_VAL", m1.get("P_VAL"));
						m_tmp.put("ADD_INFO", m_add_tmp);
						// material1RstList.add(m_tmp);
						addListTemp.add(m_tmp);
					}
				}

				for (Map<String, Object> addTemp : addListTemp) {
					listTmp.add(addTemp);
				}

			}

			// Plan 추가
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("P_VAL", String.valueOf(val).trim());
			m.put("ADD_INFO", addInfo);
			list.add(m);

			addList("P",iIdx,String.valueOf(val).trim());

		} else {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("P_VAL", String.valueOf(val).trim());
			m.put("ADD_INFO", addInfo);
			list.add(m);

			addList("P",iIdx,String.valueOf(val).trim());
		}

		return iIdx;
	}

	private List<Map<String, Object>> setMaterialResultList(List<Map<String, Object>> list, // 설정할 Material List
			String sType, int iIdx, int iSeq, Object val, Map<String, Object> addInfo) throws Exception {

		// val : material code

		if (val == null || "".equals(String.valueOf(val).trim()))
			return list;

		if (addInfo == null)
			addInfo = new HashMap<String, Object>();

		String sCd = StringUtil.get(val);
		addInfo.put("MTRL_CD", sCd);

		String sMtrlNM = getMaterialNm(sType, sCd);
		if ("".equals(sMtrlNM))
			sMtrlNM = sMtrlNM + StringUtil.get(addInfo.get("MTRL_NM2"));
		addInfo.put("MTRL_NM", sMtrlNM);

		// String sCd = getMaterialCd(sType,String.valueOf(val).trim());
		// addInfo.put("MTRL_CD",sCd);
		// addInfo.put("MTRL_NM",getMaterialNm( sType,sCd));
		// setMaterialResultList(list, sID, sNo, "M"+sType, iIdx, s, addInfo);

//		System.out.println("Mtrl Type : " + sType);
//		System.out.println("Mtrl Cd : " + sCd);
//		System.out.println("Mtrl Nm : " + sMtrlNM);
//		System.out.println("Mtrl Digit : " + getMaterialDigit(sType,sCd));

		Map<String, Object> m = new HashMap<String, Object>();
		// m.put("P_TYPE", sType);
		m.put("P_IDX", iIdx);
		m.put("P_SEQ", iSeq == 0 ? getMaxSeq(list, iIdx) : iSeq);
		// m.put("P_VAL", String.valueOf(val).trim()); //재질정보 표시 Digit
		m.put("P_VAL", getMaterialDigit(sType, sCd)); // 재질정보 표시 Digit
		m.put("ADD_INFO", addInfo); // 추가정보
		list.add(m);

		return list;
	}

	private List<Map<String, Object>> setMaterialResultList_byDigit(List<Map<String, Object>> list, // 설정할 Material List
			String sType, int iIdx, Object val, Map<String, Object> addInfo) throws Exception {

		// val : material digit

		if (val == null || "".equals(String.valueOf(val).trim()))
			return list;

		if (addInfo == null)
			addInfo = new HashMap<String, Object>();

		// String sCd = StringUtil.get(val);
		String sCd = "";
		String sDigit = String.valueOf(val).trim();
		if ("Y".equals(StringUtil.get(val))) {
			sCd = "";
			addInfo.put("MTRL_CD", "-");
			addInfo.put("MTRL_NM", "-");
		} else {
			sCd = getMaterialCd(sType, StringUtil.get(sDigit));
			addInfo.put("MTRL_CD", sCd);
			addInfo.put("MTRL_NM", getMaterialNm(sType, sCd));
		}

		// String sCd = getMaterialCd(sType,String.valueOf(val).trim());
		// addInfo.put("MTRL_CD",sCd);
		// addInfo.put("MTRL_NM",getMaterialNm( sType,sCd));
		// setMaterialResultList(list, sID, sNo, "M"+sType, iIdx, s, addInfo);

		System.out.println("sType : " + sType);
		System.out.println("sCd : " + sCd);
		System.out.println("getMaterialDigit(sType,sCd) : " + getMaterialDigit(sType, sCd));

		Map<String, Object> m = new HashMap<String, Object>();
		// m.put("P_TYPE", sType);
		m.put("P_IDX", iIdx);
		m.put("P_SEQ", getMaxSeq(list, iIdx));
		// m.put("P_VAL", String.valueOf(val).trim()); //재질정보 표시 Digit
		m.put("P_VAL", sDigit); // 재질정보 표시 Digit
		m.put("ADD_INFO", addInfo); // 추가정보
		list.add(m);

		return list;
	}

	/**
	 * 재질정보를 추천결과에 담기 전 선행 처리
	 * 
	 * @param list
	 * @param sealRstList
	 * @param sID
	 * @param sNo
	 * @param sType
	 * @param iIdx
	 * @param val
	 * @param pre_addInfo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> setMaterialResultListPrefer(List<Map<String, Object>> list, // 설정할 Material List
			List<Map<String, Object>> sealRstList, // 설정된 Seal List (추가정보 조회용)
			String sType, int iIdx, int iSeq, Object val, Map<String, Object> pre_addInfo, String sInOut)
			throws Exception {

		if (val == null || "".equals(String.valueOf(val).trim()))
			return list;

		// 재질그룹 체크
		/*
		 * FKM : GU FFKM : X675, AD, Chemraz 505, Chemraz 555, Chemraz 615 =>
		 * X675,AD,G005,G002,G006 RESIN CARBON : KR3, GE CARBON : Carbon 우선순위 SIC =>
		 * SL,YO TUC => LV,RI
		 */

		Map<String, Object> addInfo = null;

		if ("[FKM]".equals(String.valueOf(val))) { // FKM 재질
			for (String s : getMaterialDetail("[FKM]").split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM",getMaterialNm( "3",s));
				setMaterialResultList(list, "3", iIdx, iSeq, s, addInfo);
			}
		} else if ("[FFKM]".equals(String.valueOf(val))) { // FFKM 재질
			// Kalrez 6375 , Chemraz 605 , Chemraz 505 , Chemraz 555 , Chemraz 615
			for (String s : "X675,AD,G005,G002,G006".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM",getMaterialNm( "3",s));
				setMaterialResultList(list, "3", iIdx, iSeq, s, addInfo);
			}
		} else if ("[SIC]".equals(String.valueOf(val))) { // Silicon carbide 재질
			System.out.println("------------here-------------");
			for (String s : "SL,YO".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM",getMaterialNm( sType,s));
				setMaterialResultList(list, sType, iIdx, iSeq, s, addInfo);
			}
		} else if ("[TUC]".equals(String.valueOf(val))) { // tungsten carbide 재질
			for (String s : "LV,RI".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM",getMaterialNm( sType,s));
				// setMaterialResultList(list, sType, iIdx, getMaterialDigit( sType,s),
				// addInfo);
				setMaterialResultList(list, sType, iIdx, iSeq, s, addInfo);
			}
		} else if ("[RESIN CARBON]".equals(String.valueOf(val))) {
			for (String s : "KR3, GE".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM",getMaterialNm( sType,s));
				setMaterialResultList(list, sType, iIdx, iSeq, s, addInfo);
			}
		} else if ("[CARBON]".equals(String.valueOf(val))) {

			/*
			 * KR3,RY,GE,AP AP,RY = Antimony Carbon
			 * 
			 * Engineered Seal (HSH, DHTW, UHTW) 1 AP 우선 선정 2 GE 화학적 부식성 등의 이유로 필요시 <- 로직 X
			 * 그 외 Seal 1 - Seal Type별 표준 재질에서 지정된 것을 우선 선정 <- 로직 X 2 KR3 Seal Type별 표준 재질
			 * 미지정이면서, Dura PV Curve에 포함된 Seal Type인 경우 우선 선정 3 RY Seal Type별 표준 재질 미지정이면서,
			 * 상기 2에 해당하지 않는 경우 우선 선정 4 GE 화학적 부식성 등의 이유로 필요시 <- 로직 X
			 */
			String sCarbonMtrlCd = "";

			// Engineered Seal (
			for (Map<String, Object> m : sealRstList) {
				if (iIdx == NumberUtil.toInt(m.get("P_IDX"))) {
					String[] sSeals = StringUtil.get(m.get("SEAL_TYPE")).split("/");
					String sSeal = "";
					if ("IN".equals(sInOut)) {
						sSeal = sSeals[0];
					} else {
						if (sSeals.length > 1) {
							sSeal = sSeals[1];
						} else {
							sSeal = sSeals[0];
						}
					}
					if ("HSH".equals(sSeal) || "DHTW".equals(sSeal) || "UHTW".equals(sSeal)) {
						sCarbonMtrlCd = "AP,GE";
					}

				}
			}

			if ("".equals(sCarbonMtrlCd)) {
				// PV-Curve 적용여부 체크
				for (Map<String, Object> m : sealRstList) {

					String[] sSeals = StringUtil.get(m.get("SEAL_TYPE")).split("/");
					String sSeal = "";
					if ("IN".equals(sInOut)) {
						sSeal = sSeals[0];
					} else {
						if (sSeals.length > 1) {
							sSeal = sSeals[1];
						} else {
							sSeal = sSeals[0];
						}
					}

					// PV Curve 대상 Seal 체크 필요
					if (iIdx == NumberUtil.toInt(m.get("P_IDX"))) {

						Map<String, Object> param = new HashMap<String, Object>();
						param.put("SEAL_TYPE", sSeal);
						List<Map<String, Object>> c3List = rBMapper.selectRuleC3(param);
						if (!c3List.isEmpty()) {
							for (Map<String, Object> cm : c3List) {
								String c3Seals = StringUtil.get(cm.get("SEAL_TYPE"));
								for (String c3Seal : c3Seals.split(",")) {
									if (sSeal.equals(c3Seal)) {

										// PV_CURVE 타입 Seal일 경우
										if (!"".equals(StringUtil.get(cm.get("PV_CURVE")))) {
											sCarbonMtrlCd = "KR3";
											break;
										}
									}

								}
								if (!"".equals(sCarbonMtrlCd))
									break;
							}
						}
					}
				}
			}

			if ("".equals(sCarbonMtrlCd)) {
				sCarbonMtrlCd = "RY,GE";
			}

			// 부식성기준 추가
//			if(isProduct("SODIUM HYDROXIDE",saProductGroup, saProduct)) {
//				sCarbonMtrlCd = "GE";
//			}

			for (String s : sCarbonMtrlCd.split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM",getMaterialNm( sType,s));
				setMaterialResultList(list, sType, iIdx, iSeq, s, addInfo);
			}

		} else if ("[NBR]".equals(String.valueOf(val))) { // tungsten carbide 재질
			for (String s : "GS,GW,QM".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM",getMaterialNm( sType,s));
				setMaterialResultList(list, sType, iIdx, iSeq, s, addInfo);
			}

		} else if ("[ANTIMONY CARBON]".equals(String.valueOf(val))) { // tungsten carbide 재질
			for (String s : "RY".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM",getMaterialNm( sType,s));
				setMaterialResultList(list, sType, iIdx, iSeq, s, addInfo);
			}

		} else {
			// 일반 재질 등록 시
			for (String s : String.valueOf(val).split(",")) {
				if (pre_addInfo != null) {
					addInfo = (Map<String, Object>) ((HashMap<String, Object>) pre_addInfo).clone();
				} else {
					addInfo = new HashMap<String, Object>();
				}
				// addInfo.put("MTRL_CD",s);
				// addInfo.put("MTRL_NM","".equals(getMaterialNm(sType,s))?"정확하지
				// 않은정보":getMaterialNm(sType,s));
				setMaterialResultList(list, sType, iIdx, iSeq, s, addInfo);
			}
		}

		return list;
	}

	private void setMaterialResultList_byStd(String sSealType, String sAddMtrl, int iPos, String sInOut, int iPIdx,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		// List<Map<String,Object>> material1RstList =
		// (List<Map<String,Object>>)fp.get("material1RstList");//Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		// List<Map<String,Object>> material3RstList =
		// (List<Map<String,Object>>)fp.get("material3RstList");//Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		// List<Map<String,Object>> material1OutRstList =
		// (List<Map<String,Object>>)fp.get("material1OutRstList");//Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		// List<Map<String,Object>> material3OutRstList =
		// (List<Map<String,Object>>)fp.get("material3OutRstList");//Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("SEAL_TYPE", sSealType);
		List<Map<String, Object>> ruleC9List = rBMapper.selectRuleC9_1(param); // C9 표준재질 조회

		String sC9Mtrl = "";
		String sM4 = "";
		if (!ruleC9List.isEmpty()) {
			for (Map<String, Object> c9_map : ruleC9List) {
				boolean bIsSealType = false;
				for (String sSealTypeDiv : StringUtil.get(c9_map.get("SEAL_TYPE")).split(",")) {
					if ((sSealTypeDiv.trim()).equals(sSealType.trim())) {
						bIsSealType = true;
						break;
					}
				}
				if (bIsSealType) {
					if ("IN".equals(sInOut)) {
						if (iPos == 2) {
							sC9Mtrl = StringUtil.get(c9_map.get("MTRL_CD_IN_M2"));
						} else if (iPos == 4) {
							sC9Mtrl = StringUtil.get(c9_map.get("MTRL_CD_IN_M4"));
						}
					} else {
						if (iPos == 2) {
							sC9Mtrl = StringUtil.get(c9_map.get("MTRL_CD_OUT_M2"));
						} else if (iPos == 4) {
							sC9Mtrl = StringUtil.get(c9_map.get("MTRL_CD_OUT_M4"));
						}
					}
					break;

				}
			}
		}

		// 씰의 표준재질 목록에서 같은 그룹의 재질을 찾아서 적용한다.
		for (String sC9 : sC9Mtrl.split(",")) {
			for (String sMtrl : getMaterialDetail(sAddMtrl).split(",")) {
				if (sC9.trim().equals(sMtrl.trim())) {
					// 추가
					if ("IN".equals(sInOut)) {
						if (iPos == 2) {
							setMaterialResultListPrefer(material2RstList, sealRstList, "" + 2, iPIdx, 0, sC9, null,
									"IN");
						} else {
							setMaterialResultListPrefer(material4RstList, sealRstList, "" + 4, iPIdx, 0, sC9, null,
									"IN");
						}
					} else { // Out
						if (iPos == 2) {
							setMaterialResultListPrefer(material2OutRstList, sealRstList, "" + 2, iPIdx, 0, sC9, null,
									"OUT");
						} else {
							setMaterialResultListPrefer(material4OutRstList, sealRstList, "" + 4, iPIdx, 0, sC9, null,
									"OUT");
						}
					}

				}
			}
		}

	}

	/**
	 * 추천결과 Note 정보를 Array에 추가
	 */
	private List<Map<String, Object>> setResultNoteList(List<Map<String, Object>> list, int iIdx, Object val)
			throws Exception {
		return setResultNoteList(list, iIdx, val, null, null);
	}

	private List<Map<String, Object>> setResultNoteList(List<Map<String, Object>> list, int iIdx, Object val, String sType, Map<String,Object> addNote)
			throws Exception {

		// 중복체크
		boolean b = true;
		for (Map<String, Object> m : list) {
			if ((String.valueOf(m.get("P_IDX")) + String.valueOf(m.get("NOTE"))).equals(iIdx + String.valueOf(val))) {
				b = false;
				break;
			}
		}
		
		if(sType==null) sType="";
		if(addNote==null) addNote = new HashMap<String,Object>();
		
		if (b) {
			Map<String, Object> m = new HashMap<String, Object>();
			
			//m.put("P_IDX", iIdx);
			//공통타입일 경우 idx : 0
			//if("c".equals(sType)) {
			//	iIdx = 0;
			//}
			
			int iSeq = getMaxSeq2(list);
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", iSeq);
			m.put("NOTE", val);
			m.put("ADD_INFO", addNote);
			m.put("TYPE", sType);
			list.add(m);
						
			//note정보 추가
			Map<String, Object> map = new HashMap<String, Object>();
			/*
			if(_sealTypeList.size()==0 && _apiPlanList.size()==0) {
				map.put("P_IDX", 0);
				map.put("NOTE", val);
				map.put("SEAL", "");
				map.put("PLAN", "");
			}else if(_sealTypeList.size()==_apiPlanList.size()) {
				int pIdx = Integer.parseInt(_sealTypeList.get(_sealTypeList.size()-1).get("P_IDX").toString());
				String seal = _sealTypeList.get(_sealTypeList.size()-1).get("P_VAL").toString();
				String plan = _apiPlanList.get(_apiPlanList.size()-1).get("P_VAL").toString();
				map.put("P_IDX", pIdx);
				map.put("NOTE", val);
				map.put("SEAL", seal);
				if(val.toString().indexOf("Plan 11")>-1) {
					map.put("PLAN", "11");
				}else {
					map.put("PLAN", plan);
				}
			}else if(_sealTypeList.size()>_apiPlanList.size()) {
				int pIdx = Integer.parseInt(_sealTypeList.get(_sealTypeList.size()-1).get("P_IDX").toString());
				String seal = _sealTypeList.get(_sealTypeList.size()-1).get("P_VAL").toString();
				map.put("P_IDX", pIdx);
				map.put("NOTE", val);
				map.put("SEAL", seal);
				map.put("PLAN", "");
			}else if(_sealTypeList.size()<_apiPlanList.size()) {
				int pIdx = Integer.parseInt(_apiPlanList.get(_apiPlanList.size()-1).get("P_IDX").toString());
				String plan = _apiPlanList.get(_apiPlanList.size()-1).get("P_VAL").toString();
				map.put("P_IDX", pIdx);
				map.put("NOTE", val);
				map.put("SEAL", "");
				if(val.toString().indexOf("Plan 11")>-1) {
					map.put("PLAN", "11");
				}else {
					map.put("PLAN", plan);
				}
			}*/
			
			
			map.put("P_IDX", iIdx);
			map.put("NOTE", val);
			map.put("SEAL", "");
			map.put("PLAN", "");
						
			map.put("P_SEQ", iSeq);
			map.put("TYPE", sType);
			map.put("ADD_INFO", addNote);
			
			_noteList.add(map);
		}
		return list;
	}

	private List<Map<String, Object>> setResultNoteListOper(List<Map<String, Object>> list, int iIdx, Object val,
			String sealType) throws Exception {

		// 중복체크
		boolean b = true;
		for (Map<String, Object> m : list) {
			if ((String.valueOf(m.get("P_IDX")) + String.valueOf(m.get("NOTE"))).equals(iIdx + String.valueOf(val))) {
				b = false;
				break;
			}
		}
		if (b) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("NOTE", val);
			list.add(m);
			
			Map<String, Object> map = new HashMap<String, Object>();
			for(Map<String,Object> s : _sealTypeList) {
				if(s.get("P_VAL").equals(sealType)) {
					int pIdx = Integer.parseInt(s.get("P_IDX").toString());
					map.put("P_IDX", pIdx);
					map.put("NOTE", val);
					map.put("SEAL", sealType);
					for(Map<String,Object> p : _apiPlanList) {
						if(pIdx==Integer.parseInt(p.get("P_IDX").toString())) {
							map.put("PLAN", p.get("P_VAL"));
						}
					}
					if(!map.containsKey("PLAN")) {
						map.put("PLAN", "");
					}
					break;
				}
			}
			map.put("TYPE","");
			_noteList.add(map);
		}
		return list;
	}

	/**
	 * 추천결과 진행과정 정보를 Array에 추가
	 * 
	 * @param list
	 * @param sID
	 * @param sNo
	 * @param sType
	 * @param iIdx
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> setResultProcList(List<Map<String, Object>> list, int iIdx, Object val)
			throws Exception {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("P_IDX", iIdx);
		m.put("P_SEQ", getMaxSeq(list, iIdx));
		m.put("PROC_CONT", val);
		list.add(m);
		return list;
	}

	private boolean setProcessReset(Map<String, Object> item, String sType, Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer

		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		boolean bIsProcess = false;

		// Arrangement 3 으로 추천 추가기준이 적용된 경우 Skip
		if (!"".equals(StringUtil.get(item.get("__ADD_PROCESS_0")))) {
			return false;
		}

		// -----------------------------------------------------------------------------------
		// 53B Plan이 설정된 경우 ///////////////////////////////////////////////////////////
		// ***** Seal Chamber Press에 15Bar를 더한 후 Type 프로세스부터 다시 적용필요. *****
		// -----------------------------------------------------------------------------------
		// 이미처리된 로직이 아니라면
		// 지정Seal 입력된 경우가 아니라면
		if (!"Y".equals(StringUtil.get(item.get("__ADD_PROCESS_1")))
				&& !"Y".equals(StringUtil.get(item.get("IS_DIR_SEAL")))) {

			// 53B 체크
			List<String> smIdxList = new ArrayList<String>();
			for (Map<String, Object> sm : sealRstList) {
				Map<String, Object> sm_addInfo = sm.get("ADD_INFO") == null ? new HashMap<String, Object>()
						: (HashMap<String, Object>) sm.get("ADD_INFO");

				int isealIdx = 0;
				if (!"".equals(StringUtil.get(sm_addInfo.get("R_TYPE")))) { // end user, fta 처리된 로직이 아닌경우
					continue;
				} else {
					isealIdx = NumberUtil.toInt(sm.get("P_IDX"));
				}

				// 53B 플랜여부를 체크한다.
				for (Map<String, Object> pm : planRstList) {
					if (isealIdx == NumberUtil.toInt(pm.get("P_IDX"))) {
						if (StringUtil.get(pm.get("P_VAL")).contains("53B")) {
							bIsProcess = true;
							smIdxList.add("" + isealIdx); // 제거를 위해 대상 Index를 담아둔다
							break;
						}
					}
				}
			}

			if (bIsProcess) {
				setResultProcList(procRstList, 0, "[API Plan] 53B 결과에 따라 Barrier Pressure(PBF)기준으로 재 검토");
				setResultProcList(procRstList, 0, "----------------------------------------------------");

				// 압력값을 재조정
				item.put("SEAL_CHAM_NOR", NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) + 15);
				item.put("SEAL_CHAM_MIN", NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) + 15);
				item.put("SEAL_CHAM_MAX", NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) + 15);

				if ("".equals(StringUtil.get(item.get("SEAL_CHAM_NOR_O"))))
					item.put("SEAL_CHAM_NOR_O", NumberUtil.toDouble(item.get("SEAL_CHAM_NOR_O")) + 15);
				if ("".equals(StringUtil.get(item.get("SEAL_CHAM_MIN_O"))))
					item.put("SEAL_CHAM_MIN_O", NumberUtil.toDouble(item.get("SEAL_CHAM_MIN_O")) + 15);
				if ("".equals(StringUtil.get(item.get("SEAL_CHAM_MAX_O"))))
					item.put("SEAL_CHAM_MAX_O", NumberUtil.toDouble(item.get("SEAL_CHAM_MAX_O")) + 15);

				for (String sDelIdx : smIdxList) {

					// 결과제거
					List<Map<String, Object>> delList = null;
					for (int i = 0; i < 10; i++) {
						if (i == 0)
							delList = sealRstList;
						else if (i == 1)
							delList = planRstList;
						else if (i == 2)
							delList = material1RstList;
						else if (i == 3)
							delList = material2RstList;
						else if (i == 4)
							delList = material3RstList;
						else if (i == 5)
							delList = material4RstList;
						else if (i == 6)
							delList = material1OutRstList;
						else if (i == 7)
							delList = material2OutRstList;
						else if (i == 8)
							delList = material3OutRstList;
						else if (i == 9)
							delList = material4OutRstList;

						Iterator iterDel = delList.iterator();
						while (iterDel.hasNext()) {
							Map<String, Object> delM = (HashMap<String, Object>) iterDel.next();
							if (sDelIdx.equals(StringUtil.get(delM.get("P_IDX")))) {
								System.out.println("Plan 삭제 : " + sDelIdx);
								iterDel.remove();
							}
						} // end while
					}
				}
			}

			item.put("__ADD_PROCESS_1", "Y");
		}

		return bIsProcess;

	}

	private boolean setResetItem(Map<String, Object> item, String sType) {

		// 53B Bf 재처리 프로세스
		if ("__ADD_PROCESS_1".equals(sType)) {

			item.put("SEAL_CHAM_NOR", NumberUtil.toDouble(item.get("SEAL_CHAM_NOR")) - 15);
			item.put("SEAL_CHAM_MIN", NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) - 15);
			item.put("SEAL_CHAM_MAX", NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) - 15);

			if ("".equals(StringUtil.get(item.get("SEAL_CHAM_NOR_O"))))
				item.put("SEAL_CHAM_NOR_O", NumberUtil.toDouble(item.get("SEAL_CHAM_NOR_O")) - 15);
			if ("".equals(StringUtil.get(item.get("SEAL_CHAM_MIN_O"))))
				item.put("SEAL_CHAM_MIN_O", NumberUtil.toDouble(item.get("SEAL_CHAM_MIN_O")) - 15);
			if ("".equals(StringUtil.get(item.get("SEAL_CHAM_MAX_O"))))
				item.put("SEAL_CHAM_MAX_O", NumberUtil.toDouble(item.get("SEAL_CHAM_MAX_O")) - 15);
		}

		return true;
	}

	private void setRstListSeqReOrd(List<Map<String, Object>> list) {
		int iSeq = 1;
		int iIdx = 1;
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> m = list.get(i);

			if (iIdx != NumberUtil.toInt(m.get("P_IDX"))) {
				iIdx = NumberUtil.toInt(m.get("P_IDX"));
				iSeq = 1;
			}
			m.put("P_SEQ", iSeq++);
		}
	}

	/**
	 * 추천결과에서 Check List에 해당하는 인덱스를 삭제한다.
	 * 
	 * @param chkList
	 */
	private void removeResult(List<Map<String, Object>> list, List<String> chkList) {
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			Map<String, Object> data = (Map<String, Object>) iter.next();
			for (String chks : chkList) {
				if (StringUtil.get(data.get("P_IDX")).equals(chks)) {
					iter.remove();
					break;
				}
			}
		}
	}

	/**
	 * 추천결과에서 idx에 해당하는 모든 정보를 삭제한다.
	 * 
	 * @param chkList
	 */
	private void removeResultAll(Map<String, Object> fp, int iPidx) {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");// Inner
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");// Inner
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");// Inner
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");// Inner
		List<Map<String, Object>> material1OutRstList = (List<Map<String, Object>>) fp.get("material1OutRstList");// Outer
		List<Map<String, Object>> material2OutRstList = (List<Map<String, Object>>) fp.get("material2OutRstList");// Outer
		List<Map<String, Object>> material3OutRstList = (List<Map<String, Object>>) fp.get("material3OutRstList");// Outer
		List<Map<String, Object>> material4OutRstList = (List<Map<String, Object>>) fp.get("material4OutRstList");// Outer
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");// Outer

		List<Map<String, Object>> delList = new ArrayList<Map<String, Object>>();
		for (int i = 1; i <= 10; i++) {
			if (i == 1)
				delList = sealRstList;
			else if (i == 2)
				delList = material1RstList;
			else if (i == 3)
				delList = material2RstList;
			else if (i == 4)
				delList = material3RstList;
			else if (i == 5)
				delList = material4RstList;
			else if (i == 6)
				delList = material1OutRstList;
			else if (i == 7)
				delList = material2OutRstList;
			else if (i == 8)
				delList = material3OutRstList;
			else if (i == 9)
				delList = material4OutRstList;
			else if (i == 10)
				delList = planRstList;

			Iterator iter = delList.iterator();
			while (iter.hasNext()) {
				Map<String, Object> data = (Map<String, Object>) iter.next();
				if (NumberUtil.toInt(data.get("P_IDX")) == iPidx) {
					iter.remove();
					break;
				}
			}
		}

	}

	public List<Map<String, Object>> getPumbTypeList() throws Exception {
		List<Map<String, Object>> listGrpPumpTypeInfo = mLMapper.getGroupingInfo("pumpType");
		for (Map<String, Object> m : listGrpPumpTypeInfo) {
			m.put("PUMP_TYPE", m.get("GRP_SUB"));
			m.put("PUMP_TYPE_NM", m.get("GRP_SUB"));
		}

		// Non Api Pump 추가
		Map<String, Object> m_add = new HashMap<String, Object>();
		m_add.put("PUMP_TYPE", "NA");
		m_add.put("PUMP_TYPE_NM", "Non-API Pump");
		listGrpPumpTypeInfo.add(m_add);

		return listGrpPumpTypeInfo;
	}

	public List<Map<String, Object>> getRbGraphSelList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> listGrpPumpTypeInfo = null;

		if ("curve_no".equals(StringUtil.get(param.get("SEL_TYPE")))) {
			listGrpPumpTypeInfo = rBMapper.getRbGraphCurvNoSelList(param);
		} else {
			listGrpPumpTypeInfo = rBMapper.getRbGraphSelList(param);
		}

		return listGrpPumpTypeInfo;
	}

	public Map<String, Object> getRbGraphResult(Map<String, Object> param) throws Exception {

		// Get Curve no
		Map<String, Object> g_param = new HashMap<String, Object>();

		if (!"".equals(StringUtil.get(param.get("BELLOWS_MTRL")))) {
			g_param.put("BELLOWS_MTRL", param.get("BELLOWS_MTRL"));
		}

		if (!"".equals(StringUtil.get(param.get("PRODUCT_GRP")))) {
			g_param.put("PRODUCT_GRP", param.get("PRODUCT_GRP"));
		}

		if (!"".equals(StringUtil.get(param.get("ARRANGEMENT")))) {
			g_param.put("ARRANGEMENT", param.get("ARRANGEMENT"));
		}

		if (!"".equals(StringUtil.get(param.get("SPEED")))) {
			g_param.put("SPEED", param.get("SPEED"));
		}

		if (!"".equals(StringUtil.get(param.get("GRAPH_NO")))) {
			g_param.put("GRAPH_NO", param.get("GRAPH_NO"));
		}

		if (!"".equals(StringUtil.get(param.get("TEMP"))) && !",".equals(StringUtil.get(param.get("TEMP")))) {

			g_param.put("TEMP_FR", StringUtil.get(param.get("TEMP")).split(",")[0]);
			g_param.put("TEMP_TO", StringUtil.get(param.get("TEMP")).split(",")[1]);
		}

		if (!"".equals(StringUtil.get(param.get("MATERIALS"))) && !",".equals(StringUtil.get(param.get("MATERIALS")))) {
			g_param.put("MTRL_CD_M2", StringUtil.get(param.get("MATERIALS")).split(",")[0]);
			g_param.put("MTRL_CD_M4", StringUtil.get(param.get("MATERIALS")).split(",")[1]);
		}

		Map<String, Object> sResult = new HashMap<String, Object>();
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		String sSize = StringUtil.get(param.get("INPUT_SIZE"));

		if ("".equals(StringUtil.get(param.get("CURVE_NO")))) {
			// graph 조회
			List<Map<String, Object>> graphList = rBMapper.selectRuleGraphBySimul(g_param);

			// Get Graph Value
			if (graphList.size() > 0) {

				Map<String, Object> g_data = graphList.get(0);
				g_param.put("CURV_NO", g_data.get("CURV_NO"));
				g_param.put("VAL", sSize); // Seal or Shaft SIze

				sResult.put("curve_no", g_data.get("CURV_NO"));
			} else {
				sResult.put("curve_no", "해당하는 Curve가 없습니다.");
			}
		} else {
			g_param.put("CURV_NO", StringUtil.get(param.get("CURVE_NO")));
			sResult.put("curve_no", StringUtil.get(param.get("CURVE_NO")));
		}
		g_param.put("VAL", sSize); // Seal or Shaft SIze

		if ("".equals(StringUtil.get(g_param.get("CURV_NO")))) {
			sResult.put("result", "해당하는 그래프 조건의 결과가 없습니다.");
			return sResult;
		}

		List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(g_param);
		String sFunc = "";
		if (grapFunchList.size() > 0) {
			sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));

			System.out.println("sFunc : " + sFunc);

			sFunc = sFunc.replace("x", "" + sSize); //
			double d = NumberUtil.toDouble(engine.eval(sFunc));

			sResult.put("result", d);

		} else {
			sResult.put("result", "입력값 범위에 해당하는 값이 없습니다.");
		}

		return sResult;
	}

	/**
	 * Brine 구분 적용여부에 따라 적용할 Producct 정보 반환
	 * 
	 * @param item
	 * @param product
	 * @param saProductGroup
	 * @param saProduct
	 * @return
	 */
	private String getBrineProduct(Map<String, Object> item, String product, String[] saProductGroup,
			String[] saProduct) {
		String sProduct = "";

		// [C1] Material Guide (FTA101)에서 재질 선정시 Fluid 명을 다음과 같이 처리하여 진행
		// Brine 계열
		// *Glycol 계열 EG or PG로 확인되면 그에 맞게 진행, 구분 안되고 Glycol로만 확인되면 EG로 진행
		// *해수 또는 염수 계열 : Brine으로 진행

		// BRINE_GB
		// Z110010 Glycol 계열
		// Z110020 해수 염수계열

		// Glycol 계열 일경우 추가 조건
		// BRINE_SUB_GB
		// EG : ETHYLENE GLYCOL
		// PG : PROPYLENE GLYCOL
		// NONE : 불확실할경우 EG -> ETHYLENE GLYCOL

		// Brine 계열체크
		if (isProduct("BRINE", saProductGroup, saProduct)) {
			if ("Z110010".equals(StringUtil.get(item.get("BRINE_GB")))) { // Glycol 계열

				// Glycol 계열 Sub 조건
				if ("EG".equals(StringUtil.get(item.get("BRINE_SUB_GB")))) {
					sProduct = "ETHYLENE GLYCOL";
				} else if ("PG".equals(StringUtil.get(item.get("BRINE_SUB_GB")))) {
					sProduct = "PROPYLENE GLYCOL";
				} else if ("NONE".equals(StringUtil.get(item.get("BRINE_SUB_GB")))) {
					sProduct = "ETHYLENE GLYCOL";
				} else {
					sProduct = "ETHYLENE GLYCOL";
				}
//				if(isProduct("ETHYLENE GLYCOL",saProductGroup, saProduct)) {
//					sProduct = "ETHYLENE GLYCOL";
//				}else if(isProduct("PROPYLENE GLYCOL",saProductGroup, saProduct)) {
//					sProduct = "PROPYLENE GLYCOL";
//				}else {
//					sProduct = "ETHYLENE GLYCOL";
//				}
			} else if ("Z110020".equals(StringUtil.get(item.get("BRINE_GB")))) { // 해수 염수계열
				sProduct = "BRINE";
			} else {
				sProduct = "ETHYLENE GLYCOL";
			}
		} else {
			sProduct = product;
		}

		return sProduct;
	}

	/**
	 * face 재질의 순서를 반환한다.
	 * 
	 * @param sSealType
	 * @param sMtrlCd   : 재질코드
	 * @param iPrePos   : 초기 설정 위치
	 * @param sIO       : IN / OUT
	 * @return
	 * @throws Exception
	 */
	private int getFaceSeq(String sSealType, String sMtrlCd, int iPrePos, String sIO) throws Exception {

		int ipos = 0;

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("SEAL_TYPE", sSealType);
		List<Map<String, Object>> ruleC9List = rBMapper.selectRuleC9_1(param); // C9 표준재질 조회

		String sM2 = "";
		String sM4 = "";
		if (!ruleC9List.isEmpty()) {
			for (Map<String, Object> c9_map : ruleC9List) {
				boolean bIsSealType = false;
				for (String sSealTypeDiv : StringUtil.get(c9_map.get("SEAL_TYPE")).split(",")) {
					if ((sSealTypeDiv.trim()).equals(sSealType.trim())) {
						bIsSealType = true;
						break;
					}
				}
				if (bIsSealType) {
					if ("IN".equals(sIO)) {
						sM2 = StringUtil.get(c9_map.get("MTRL_CD_IN_M2"));
						sM4 = StringUtil.get(c9_map.get("MTRL_CD_IN_M4"));
					} else {
						sM2 = StringUtil.get(c9_map.get("MTRL_CD_OUT_M2"));
						sM4 = StringUtil.get(c9_map.get("MTRL_CD_OUT_M4"));
					}
					break;
				}
			}

			// sM2가 Soft인지 Hard인지 구분
			System.out.println("C9 sM2 : " + sM2);
			System.out.println("C9 sM4 : " + sM4);

			String sM2Type = "H", sM4Type = "H";
			for (String s : sM2.split(",")) {
				if ("[RESIN CARBON]".equals(getMaterialGrp(s)) || "[ANTIMONY CARBON]".equals(getMaterialGrp(s))) {
					sM2Type = "S";
					break;
				}
			}

			for (String s : sM4.split(",")) {
				if ("[RESIN CARBON]".equals(getMaterialGrp(s)) || "[ANTIMONY CARBON]".equals(getMaterialGrp(s))) {
					sM4Type = "S";
					break;
				}
			}

			System.out.println("C9 getMaterialGrp(sMtrlCdType) : " + getMaterialGrp(sMtrlCd));

			String sMtrlCdType = "";
			if ("[RESIN CARBON]".equals(getMaterialGrp(sMtrlCd))
					|| "[ANTIMONY CARBON]".equals(getMaterialGrp(sMtrlCd))) {
				sMtrlCdType = "S";
			} else {
				sMtrlCdType = "H";
			}

			System.out.println("C9 sMtrlCdType : " + sMtrlCdType);
			System.out.println("C9 sM2Type : " + sM2Type);
			System.out.println("C9 sM4Type : " + sM4Type);

			if ("S".equals(sMtrlCdType) && "S".equals(sM2Type)) {
				ipos = 2;
			} else if ("S".equals(sMtrlCdType) && "S".equals(sM4Type)) {
				ipos = 4;
			} else {
				ipos = iPrePos;
			}
		}

		return ipos;
	}

	public String getGroupingStr(String sOrgVal, List<Map<String, Object>> listGrpInfo,
			List<Map<String, Object>> listGroupHierInfo) {
		String sGroupResult = "";
		sOrgVal = sOrgVal.toUpperCase(); // 대문자 처리

		// System.out.println("---------------------------------------------");
		// System.out.println("getGroupingStr :::::::: sOrgVal : " + sOrgVal);
		for (Map<String, Object> m : listGrpInfo) {

			if (sOrgVal.contains(String.valueOf(m.get("GRP_SUB")))
					&& !sGroupResult.contains(String.valueOf(m.get("GRP")))) { // 기존에 포함되지 않은 경우

				System.out.println("getGroupingStr : GRP_SUB : " + String.valueOf(m.get("GRP_SUB")));

				// 좌우에 문자이외의 값이 있는 경우만 처리
				// [^a-zA-Z]*\b단어\b[^a-zA-Z]*
				Pattern p = Pattern.compile("[^a-zA-Z]*\\b" + String.valueOf(m.get("GRP_SUB")) + "\\b[^a-zA-Z]*");
				Matcher rm = p.matcher(sOrgVal);
				if (!rm.find()) {
					continue;
				}

				if (sGroupResult.length() > 0) {
					sGroupResult = sGroupResult + "+" + String.valueOf(m.get("GRP"));
				} else {
					sGroupResult = sGroupResult + String.valueOf(m.get("GRP"));
				}

				System.out.println("getGroupingStr : sGroupResult : " + sGroupResult);

				sOrgVal = sOrgVal.replace(String.valueOf(m.get("GRP_SUB")), "_____"); // 선택된 항목 Remove : 공백을 만듦으로서 다음 문자
																						// 체크때 의도치 않게 단어가 조합될 수 있으므로 별도
																						// 문자로 치환한다.

				System.out.println("getGroupingStr : sOrgVal : " + sOrgVal);

			}
		}
		// System.out.println("getGroupingStr :::::::: sGroupResult : " + sGroupResult);

		String[] tmp = sGroupResult.split("\\+");
		List<String> tmpList = new ArrayList<>(Arrays.asList(tmp)); // 리스트로 변환

		// 그룹 Hierarchy 처리
		if (listGroupHierInfo != null) {
			for (Map<String, Object> m : listGroupHierInfo) {
				if (tmpList.contains(String.valueOf(m.get("LV3")))) { // 3레벨이 있는 경우
					tmpList.remove(String.valueOf(m.get("LV2"))); // 2레벨 항목 제거
					tmpList.remove(String.valueOf(m.get("LV1"))); // 1레벨 항목 제거
				}
			}
			for (Map<String, Object> m : listGroupHierInfo) {
				if (tmpList.contains(String.valueOf(m.get("LV2")))) { // 2레벨이 있는 경우
					tmpList.remove(String.valueOf(m.get("LV1")));// 1레벨 항목 제거
				}
			}
		}

		tmpList.sort(null);

		// 결과 정렬
		// String[] tmp = sGroupResult.split("\\+");
		// Arrays.sort(tmp); // sort
		sGroupResult = String.join("+", tmpList);

		// System.out.println("getGroupingStr :::::::: sGroupResult 최종 : " +
		// sGroupResult);

		return sGroupResult;
	}

	/**
	 * Product Group 정보를 바탕으로 유효한 Product 정보를 추출한다. - Grouping 하위 Product 정보를 반환
	 */
	public String getProductStr(String sOrgVal, List<Map<String, Object>> listGrpInfo, int ord) {
		String sProductResult = "";
		List<String> productList = new ArrayList<String>();
		sOrgVal = sOrgVal.toUpperCase(); // 대문자 처리

		for (Map<String, Object> m : listGrpInfo) {
			// if (sOrgVal.contains(String.valueOf(m.get("GRP_SUB"))) &&
			// !sProductResult.contains( String.valueOf(m.get("GRP_SUB"))) ) { // 기존에 포함되지
			// 않은 경우
			if (sOrgVal.contains(String.valueOf(m.get("GRP_SUB")))
					&& !productList.contains(String.valueOf(m.get("GRP_SUB")))) {

				// 좌우에 문자(a~z)값이 있는 경우 처리
				// [^a-zA-Z]*\b단어\b[^a-zA-Z]*
				Pattern p = Pattern.compile("[^a-zA-Z]*\\b" + String.valueOf(m.get("GRP_SUB")) + "\\b[^a-zA-Z]*");
				Matcher rm = p.matcher(sOrgVal);
				if (!rm.find()) {
					continue;
				}

				productList.add(String.valueOf(m.get("GRP_SUB")));
				sOrgVal = sOrgVal.replace(String.valueOf(m.get("GRP_SUB")), "_____"); // 선택된 항목 Remove : 공백을 만듦으로서 다음 문자
																						// 체크때 의도치 않게 단어가 조합될 수 있으므로 별도
																						// 문자로 치환한다.
			}
		}
		// System.out.println("getGroupingStr :::::::: sGroupResult : " + sGroupResult);
		if (ord == 1) { // 정렬조건일때
			productList.sort(null);// 정렬
		}

		// 결과 정렬
		sProductResult = String.join("+", productList);
		return sProductResult;
	}

	/**
	 * Rule 기준정보를 반환 (공통) MCD : 필수 SCD, ATTR1~15 옵션
	 * 
	 * @param p
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> getRuleComListType1(Map<String, Object> p) throws Exception {
		return rBMapper.selectRuleComListType1(p);
	}

	/**
	 * UI에서 Product Grouping 정보 조회용
	 */
	public String getGroupingStr(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> listGrpInfo = mLMapper.getGroupingInfo("product"); // product Grouping 정보
		// List<Map<String,Object>> listGroupHierInfo = mLMapper.getGroupingHier(null);
		// // product Hierarchy 정보
		// return mLService.getGroupingStr(String.valueOf(param.get("PRODUCT")),
		// listGrpInfo, listGroupHierInfo);
		return mLService.getProductStr(String.valueOf(param.get("PRODUCT")), listGrpInfo, 0); // 옵션 : 정렬X
	}

	/**
	 * UI에서 Product Grouping 정보 조회용 - product별 추가 구분정보 포함 반환
	 */
	public List<Map<String, Object>> getGroupingInfo(Map<String, Object> param) throws Exception {

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		// String sProductGrpStr =
		// mLService.getProductStr(String.valueOf(param.get("PRODUCT")),
		// mLMapper.getGroupingInfo("product"), 0);
		String sProductGrpStr = getProductStr(String.valueOf(param.get("PRODUCT")), mLMapper.getGroupingInfo("product"),
				0);

		// result.put("product_grp_str", sProductGrpStr);

		// Product별 추가구분정보
		Map<String, Object> productM = null;
		Map<String, Object> ptm = null;
		for (String sProduct : sProductGrpStr.split("[+]")) {

			productM = new HashMap<String, Object>();
			productM.put("product", sProduct);

			ptm = new HashMap<String, Object>();
			// ptm.put("MCD", "C101");
			ptm.put("ATTR1", sProduct);
			List<Map<String, Object>> rComList = rBMapper.selectRuleComListC101(ptm);

			String sPGroup = getProductGrp(sProduct);

			System.out.println("rComList : " + rComList + ":" + rComList.size());
			System.out.println("sPGroup : " + sPGroup);

			// 조회건수가 없을 경우 상위그룹명으로 재조회
			if (rComList.size() == 1 || rComList.size() == 0) { // 빈칸하나는 조회되므로
				ptm.put("ATTR1", sPGroup);
				rComList = rBMapper.selectRuleComListC101(ptm);
			}

			if (rComList.size() > 1) {
				productM.put("product_gb", rComList);
			}

			// 농도 구분 적용 유무 (상위그룹과 동시 체크)
			ptm.clear();
			ptm.put("PRODUCT", sProduct); // Product
			ptm.put("PRODUCT_GRP", sPGroup); // Product 상위그룹
			List<Map<String, Object>> ruleC1List = rBMapper.selectRuleC1ChkContUse(ptm);
			if (ruleC1List.size() > 0) {
				productM.put("cont_yn", ((Map<String, Object>) ruleC1List.get(0)).get("cont_yn"));
			} else {
				productM.put("cont_yn", "N");
			}

			// 상위 그룹명
			productM.put("product_grp", sPGroup);

			result.add(productM);
		}
		return result;
	}

	/**
	 * 입력인자의 상위 Product Group명을 반환
	 * 
	 * @param product
	 * @return
	 */
	private String getProductGrp(String product) throws Exception {
		String sProductGrp = "";

		// Product Grouping Info.
		if (_productGroupInfo == null)
			_productGroupInfo = mLMapper.getGroupingInfo("product");
		
		for (Map<String, Object> m : _productGroupInfo) {
			if (Objects.equal(product, m.get("GRP_SUB"))) {
				sProductGrp = StringUtil.get(m.get("GRP"));
				break;
			}
		}
		return sProductGrp;
	}

	/**
	 * 입력파라메타에서 입력된 product의 농도를 반환한다.
	 * 
	 * @param item
	 * @param product
	 * @return
	 */
	private double getProductCont(Map<String, Object> item, String product, String unit) {
		double dCont = 0d;

		// product 명으로
		for (int i = 1; i <= 12; i++) {
			if (product.equals(StringUtil.get(item.get("PRODUCTNM_" + i)))) {
				// unit은 PPM과 % 두가지 값중에 하나가 들어온다는 가정임.
				if (unit.equals(StringUtil.get(item.get("PRODUCTUT_" + i)))) {
					dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i));
				} else {
					if (unit.equals("%")) { // %
						dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i)) / 10000.0;
					} else { // ppm
						dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i)) * 10000.0;
					}
				}
				break;
			}
		}

		// product => product group으로 넘어옴
		// sProducts = product 배열정보
		// sProducts에 있는 product중 넘어온 product(product group)에 속하는 정보를 확인하고 get
		if (dCont == 0) {
			for (Map<String, Object> m : _productGroupInfo) {
				if (Objects.equal(product, m.get("GRP"))) {

					for (int i = 1; i <= 12; i++) {
						if ((StringUtil.get(m.get("GRP_SUB")).equals(StringUtil.get(item.get("PRODUCTNM_" + i))))) {
							// unit은 PPM과 % 두가지 값중에 하나가 들어온다는 가정임.
							if (unit.equals(StringUtil.get(item.get("PRODUCTUT_" + i)))) {
								dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i));
							} else {
								if (unit.equals("%")) { // %
									dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i)) / 10000.0;
								} else { // ppm
									dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i)) * 10000.0;
								}
							}

							break;

						}
					}
				}
			}
		}

		return dCont;
	}

	/**
	 * 입력파라메타에서 Product의 구분값을 반환한다.
	 * 
	 * @param item
	 * @param product
	 * @return
	 */
	private String getProductGb(Map<String, Object> item, String product) {
		String sGb = "";
		for (int i = 1; i <= 12; i++) {
			if (product.equals(StringUtil.get(item.get("PRODUCTNM_" + i)))) {
				sGb = StringUtil.get(item.get("PRODUCTGB_" + i));
				break;
			}
		}

		// [옵션없음] 선택값으로 넘어오는경우 "-"로 처리
		// C1 유체구분에서 구분값으로 나누어지는 유체중 구분값이 없는 경우 '-' 으로 설정되어있음.
		if ("[옵션없음]".equals(sGb)) {
			sGb = "-";
		}
		return sGb;
	}

	/**
	 * C1에서 재질 Grade를 반환한다.
	 * 
	 * @param c1_map
	 * @return
	 * @throws Exception
	 */
	private String getGrade(Map<String, Object> c1_map) throws Exception {
		String sGrade = "";
		String sProductGrp = "";
		boolean bIsGet = false;

		// product 정보를 임시저장
		String sProduct = StringUtil.get(c1_map.get("PRODUCT"));

		if ("".equals(sProductGrp)) {
			for (Map<String, Object> m : _productGroupInfo) {
				if (Objects.equal(c1_map.get("PRODUCT"), m.get("GRP_SUB"))) {
					sProductGrp = StringUtil.get(m.get("GRP"));
					break;
				}
			}
		}

		List<Map<String, Object>> c1_list = rBMapper.selectRuleC1(c1_map);

		// Map<String,Object> c1_data = null;
		if (c1_list.size() > 0) {

			bIsGet = true;

			// 복수항목으로 나올수 있으므로 모두 체크
			// ""(Z), X, B, A 우선순으로 체크
			int iGrade = 0;
			for (Map<String, Object> c1 : c1_list) {
				int iGrade_tmp = ((int) StringUtil.get(c1.get("GRADE"), "Z").toCharArray()[0]);
				if (iGrade < iGrade_tmp)
					iGrade = iGrade_tmp;
			}

			sGrade = Character.toString((char) iGrade);
			if ("Z".equals(sGrade)) {
				sGrade = "";
			}

			// Grade가 빈값일 경우
			if ("".equals(sGrade)) {

				// 상위 Product Group으로 다시 조회
				c1_map.put("PRODUCT", sProductGrp); // 상위Product Group으로 변경

				c1_list = rBMapper.selectRuleC1(c1_map);
				if (c1_list.size() > 0) {
					bIsGet = true;

					for (Map<String, Object> c1 : c1_list) {
						int iGrade_tmp = ((int) StringUtil.get(c1.get("GRADE"), "Z").toCharArray()[0]);
						if (iGrade < iGrade_tmp)
							iGrade = iGrade_tmp;
					}

					sGrade = Character.toString((char) iGrade); // Ascii to Char
					if ("Z".equals(sGrade)) {
						sGrade = "";
					}

				}
			}

		} else {

			// 상위 Product Group으로 다시 조회
			c1_map.put("PRODUCT", sProductGrp); // 상위Product Group으로 변경
			c1_list = rBMapper.selectRuleC1(c1_map);
			if (c1_list.size() > 0) {
				bIsGet = true;
				// c1_data = c1_list.get(0); // 첫번째항목
				// sGrade = StringUtil.get(c1_data.get("GRADE"));

				// ""(Z), X, B, A 우선순으로 체크
				int iGrade = 0;
				for (Map<String, Object> c1 : c1_list) {
					int iGrade_tmp = ((int) StringUtil.get(c1.get("GRADE"), "Z").toCharArray()[0]);
					if (iGrade < iGrade_tmp)
						iGrade = iGrade_tmp;
				}
				sGrade = Character.toString((char) iGrade);
				if ("Z".equals(sGrade)) {
					sGrade = "";
				}

			}
		}

		// 데이터가 조회되지 않을경우 온도조건을 변경하여 조회
		if (!bIsGet || "".equals(sGrade)) {

			// Temp Max가 조회하는 정보의 온도 범위보다 높음 또는 남음 체크
			c1_map.put("PRODUCT", sProduct);
			c1_map.put("PRODUCT_GRP", sProductGrp);
			c1_list = rBMapper.selectRuleC1_temp_cond(c1_map);

			String sQryType = "";
			if (!c1_list.isEmpty()) {
				Map<String, Object> c1_temp_min_max = c1_list.get(0);
				double dMinTempFr = NumberUtil.toDouble(c1_temp_min_max.get("MIN_TEMP_FR"));
				double dMaxTempTo = NumberUtil.toDouble(c1_temp_min_max.get("MAX_TEMP_TO"));

				if (dMaxTempTo < NumberUtil.toDouble(c1_map.get("TEMP_MAX")) && dMaxTempTo != 999) {
					sQryType = "1"; // Max값으로 비교
				} else if (dMinTempFr > NumberUtil.toDouble(c1_map.get("TEMP_MIN")) && dMinTempFr != -999) {
					sQryType = "2"; // Min값으로 비교
				} else {
					sQryType = "1";
				}
			}

			if ("1".equals(sQryType)) {
				c1_list = rBMapper.selectRuleC1_temp_cond_max(c1_map);
			} else {
				c1_list = rBMapper.selectRuleC1_temp_cond_min(c1_map);
			}

			if (c1_list.size() > 0) {
				// 복수항목으로 나올수 있으므로 모두 체크
				// ""(Z), X, B, A 우선순으로 체크
				int iGrade = 0;
				for (Map<String, Object> c1 : c1_list) {
					int iGrade_tmp = ((int) StringUtil.get(c1.get("GRADE"), "Z").toCharArray()[0]);
					if (iGrade < iGrade_tmp)
						iGrade = iGrade_tmp;
				}

				sGrade = Character.toString((char) iGrade);
				if ("Z".equals(sGrade)) {
					sGrade = "";
				}
			} else {
				// 그래도 조회되는 항목이 없을 경우 NONE 처리
				sGrade = "NONE";
			}
		}

		return sGrade;
	}

	/**
	 * 현재 결과리스트에서 Idx 기준 Max Seq를 반환
	 * 
	 * @param list
	 * @param ildx
	 * @return
	 */
	private int getMaxSeq(List<Map<String, Object>> list, int ildx) {
		int iSeq = 0;
		for (Map<String, Object> m : list) {
			if (NumberUtil.toInt(m.get("P_IDX")) == ildx) {
				if (iSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
					iSeq = NumberUtil.toInt(m.get("P_SEQ"));
				}
			}
		}
		iSeq = iSeq + 1;
		return iSeq;
	}
	
	private int getMaxSeq2(List<Map<String, Object>> list) {
		int iSeq = 0;
		for (Map<String, Object> m : list) {
			if (iSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
				iSeq = NumberUtil.toInt(m.get("P_SEQ"));
			}
		}
		iSeq = iSeq + 1;
		return iSeq;
	}

	public Map<String, Object> getunknownApi(Map<String, Object> param) throws Exception {

		Map<String, Object> result = new HashMap<String, Object>();

		List<String> resultUnknownApi = new ArrayList<String>();

		// Plan 정보
		List<Map<String, Object>> planList = rBMapper.selectRuleComListE002(null);
		// 입력 plan 정보
		String sPlanDir = StringUtil.get(param.get("API_PLAN_DIR"));
		int ia = 0;
		// 직접입력 Plan이 있을 경우
		if (!"".equals(sPlanDir)) {
			for (String sPlan : sPlanDir.split("/")) {
				boolean bIs = false;
				for (Map<String, Object> pm : planList) {
					if (sPlan.equals(StringUtil.get(pm.get("ATTR4")))) {
						bIs = true;

						if (ia < NumberUtil.toInt(pm.get("ATTR2"))) {
							ia = NumberUtil.toInt(pm.get("ATTR2"));
						}
						break;
					}
				}
				if (!bIs) {
					resultUnknownApi.add(sPlan);
				}
			}
		}
		result.put("UNKNOWN_API", resultUnknownApi); // 알수없는 API 정보
		result.put("ARRANGEMENT", ia); // Arrangement 체크

		return result;
	}

	/**
	 * 표준단위로 수치 컨버전
	 * 
	 * @param item
	 * @return
	 * @throws Exception
	 */
	private void convToStdUnit(Map<String, Object> item, String sCnvType, ScriptEngine engine,
			List<Map<String, Object>> listUnitCode, List<Map<String, Object>> listUnitChg,
			List<Map<String, Object>> listTransTxtVal, List<Map<String, Object>> listSsuChg,
			List<Map<String, Object>> listShaftSizeEpc) throws Exception {

		// 일부 입력값에 대하여 기존 입력값 유지
		// Seal Chamber Press, Vaper Press, Shaft Size
		item.put("VAP_PRES_NOR_O", item.get("VAP_PRES_NOR"));
		item.put("VAP_PRES_MIN_O", item.get("VAP_PRES_MIN"));
		item.put("VAP_PRES_MAX_O", item.get("VAP_PRES_MAX"));

		item.put("SEAL_CHAM_NOR_O", item.get("SEAL_CHAM_NOR"));
		item.put("SEAL_CHAM_MIN_O", item.get("SEAL_CHAM_MIN"));
		item.put("SEAL_CHAM_MAX_O", item.get("SEAL_CHAM_MAX"));

		item.put("SHAFT_SIZE_O", item.get("SHAFT_SIZE"));

		// Shaft Size 빈값처리 (EPC 단계 가정값) 먼저 진행
		if ("".equals(StringUtil.get(item.get("SHAFT_SIZE")))) {
			for (Map<String, Object> m : listShaftSizeEpc) {
				if (StringUtil.get(m.get("ATTR2")).equals(StringUtil.get(item.get("PUMP_TYPE")))) {
					item.put("SHAFT_SIZE", m.get("ATTR1"));
					break;
				}
			}
			item.put("SHAFT_SIZE_UNIT", "MM"); // 단위 MM로 설정
		}

		// 빈값 처리
		item = mLService.setEmptyDataWithDefaultData(item);

		// 비중 : 이 후 계산 시 필요로 먼저 선언
		String sSpecGravityNor = String.valueOf(item.get("SPEC_GRAVITY_NOR"));
		String sSpecGravityMin = String.valueOf(item.get("SPEC_GRAVITY_MIN"));
		String sSpecGravityMax = String.valueOf(item.get("SPEC_GRAVITY_MAX"));

		String aBaseUnit = null; // 기준단위

		// 온도
		aBaseUnit = mLService.unitConvBase(listUnitCode, "TEMP_NOR");
		System.out.println("TEMP aBaseUnit : " + aBaseUnit);
		System.out.println("TEMP UNIT : " + item.get("TEMP_UNIT"));
		item.put("TEMP_NOR", mLService.convWithUnit(sCnvType, engine, "TEMP_NOR", item.get("TEMP_NOR"),
				item.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
		item.put("TEMP_MIN", mLService.convWithUnit(sCnvType, engine, "TEMP_MIN", item.get("TEMP_MIN"),
				item.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
		item.put("TEMP_MAX", mLService.convWithUnit(sCnvType, engine, "TEMP_MAX", item.get("TEMP_MAX"),
				item.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));

		// 점도 : Viscosity
		aBaseUnit = mLService.unitConvBase(listUnitCode, "VISC_NOR");
		System.out.println("VISC aBaseUnit : " + aBaseUnit);
		item.put("VISC_NOR", mLService.convWithUnit(sCnvType, engine, "VISC_NOR", item.get("VISC_NOR"),
				item.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityNor));
		item.put("VISC_MIN", mLService.convWithUnit(sCnvType, engine, "VISC_MIN", item.get("VISC_MIN"),
				item.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMin));
		item.put("VISC_MAX", mLService.convWithUnit(sCnvType, engine, "VISC_MAX", item.get("VISC_MAX"),
				item.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMax));

		// 증기압력 : Vapor Pressure
		aBaseUnit = mLService.unitConvBase(listUnitCode, "VAP_PRES_NOR");
		item.put("VAP_PRES_NOR", mLService.convWithUnit(sCnvType, engine, "VAP_PRES_NOR", item.get("VAP_PRES_NOR"),
				item.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
		item.put("VAP_PRES_MIN", mLService.convWithUnit(sCnvType, engine, "VAP_PRES_MIN", item.get("VAP_PRES_MIN"),
				item.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
		item.put("VAP_PRES_MAX", mLService.convWithUnit(sCnvType, engine, "VAP_PRES_MAX", item.get("VAP_PRES_MAX"),
				item.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
		// -> 증기압 입력값 별도 저장 정보 단위환산
		item.put("VAP_PRES_NOR_O", mLService.convWithUnit(sCnvType, engine, "VAP_PRES_NOR", item.get("VAP_PRES_NOR_O"),
				item.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
		item.put("VAP_PRES_MIN_O", mLService.convWithUnit(sCnvType, engine, "VAP_PRES_MIN", item.get("VAP_PRES_MIN_O"),
				item.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
		item.put("VAP_PRES_MAX_O", mLService.convWithUnit(sCnvType, engine, "VAP_PRES_MAX", item.get("VAP_PRES_MAX_O"),
				item.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));

		// 씰챔버압력 : Seal Chamber Pressure
		aBaseUnit = mLService.unitConvBase(listUnitCode, "SEAL_CHAM_NOR");
		item.put("SEAL_CHAM_NOR", mLService.convWithUnit(sCnvType, engine, "SEAL_CHAM_NOR", item.get("SEAL_CHAM_NOR"),
				item.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
		item.put("SEAL_CHAM_MIN", mLService.convWithUnit(sCnvType, engine, "SEAL_CHAM_MIN", item.get("SEAL_CHAM_MIN"),
				item.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
		item.put("SEAL_CHAM_MAX", mLService.convWithUnit(sCnvType, engine, "SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX"),
				item.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
		// -> 씰챔버압력 입력값 별도 저장 정보 단위환산
		item.put("SEAL_CHAM_NOR_O",
				mLService.convWithUnit(sCnvType, engine, "SEAL_CHAM_NOR", item.get("SEAL_CHAM_NOR_O"),
						item.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
		item.put("SEAL_CHAM_MIN_O",
				mLService.convWithUnit(sCnvType, engine, "SEAL_CHAM_MIN", item.get("SEAL_CHAM_MIN_O"),
						item.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
		item.put("SEAL_CHAM_MAX_O",
				mLService.convWithUnit(sCnvType, engine, "SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX_O"),
						item.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));

		// RPM - Shaft Speed
		aBaseUnit = mLService.unitConvBase(listUnitCode, "RPM_NOR");
		item.put("RPM_NOR", mLService.convWithUnit(sCnvType, engine, "RPM_NOR", item.get("RPM_NOR"),
				item.get("RPM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
		item.put("RPM_MIN", mLService.convWithUnit(sCnvType, engine, "RPM_MIN", item.get("RPM_MIN"),
				item.get("RPM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
		item.put("RPM_MAX", mLService.convWithUnit(sCnvType, engine, "RPM_MAX", item.get("RPM_MAX"),
				item.get("RPM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));

		// Shaft Size
		aBaseUnit = mLService.unitConvBase(listUnitCode, "SHAFT_SIZE");
		item.put("SHAFT_SIZE", mLService.convWithUnit(sCnvType, engine, "SHAFT_SIZE", item.get("SHAFT_SIZE"),
				item.get("SHAFT_SIZE_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, null));

		// Shaft Speed : 선속도
		// Shaft Size(Dia.)와 RPM속도 있을 경우 ft/s로 산출하여 사용 [C3]활용을 위함.
		// 속도(mm/m) : 3.14 * Shaft Dia(mm) * RPM
		// 속도(ft/s) : 3.14 * Shaft Dia(mm) * 0.00328084 * RPM / 60
		// item.put("L_SPD_MIN", 3.14 * NumberUtil.toDouble(item.get("SHAFT_SIZE")) *
		// 0.00328084 * NumberUtil.toDouble(item.get("RPM_MIN")) / 60 );
		// item.put("L_SPD_NOR", 3.14 * NumberUtil.toDouble(item.get("SHAFT_SIZE")) *
		// 0.00328084 * NumberUtil.toDouble(item.get("RPM_NOR")) / 60 );
		// item.put("L_SPD_MAX", 3.14 * NumberUtil.toDouble(item.get("SHAFT_SIZE")) *
		// 0.00328084 * NumberUtil.toDouble(item.get("RPM_MAX")) / 60 );

	}

	// 결과 정렬 처리 ( List 내 Map 결과 정렬처리)
	public class sortMap implements Comparator<Map<String, Object>> {
		@Override
		public int compare(Map<String, Object> a, Map<String, Object> b) {
			int aIdx = Integer.parseInt(a.get("P_IDX").toString());
			int bIdx = Integer.parseInt(b.get("P_IDX").toString());
			int aSeq = Integer.parseInt(a.get("P_SEQ").toString());
			int bSeq = Integer.parseInt(b.get("P_SEQ").toString());

			if (aIdx < bIdx)
				return -1;
			else if (aIdx > bIdx)
				return 1;
			else {
				if (aSeq < bSeq)
					return -1;
				else if (aSeq > bSeq)
					return 1;
				else
					return 0;
			}
			// return aIdx < bIdx ? -1 : aIdx> bIdx ? 1 : 0;
		}
	}
	
	// 결과 정렬 처리 ( List 내 Map 결과 정렬처리)
	public class sortMap2 implements Comparator<Map<String, Object>> {
		@Override
		public int compare(Map<String, Object> a, Map<String, Object> b) {
			//int aIdx = Integer.parseInt(a.get("P_IDX").toString());
			//int bIdx = Integer.parseInt(b.get("P_IDX").toString());
			int aIdx = Integer.parseInt(a.get("P_ID_SEQ").toString());
			int bIdx = Integer.parseInt(b.get("P_ID_SEQ").toString());
			int aSeq = Integer.parseInt(a.get("P_SEQ")==null?"999":a.get("P_SEQ").toString());
			int bSeq = Integer.parseInt(b.get("P_SEQ")==null?"999":b.get("P_SEQ").toString());

			if (aIdx < bIdx)
				return -1;
			else if (aIdx > bIdx)
				return 1;
			else {
				if (aSeq < bSeq)
					return -1;
				else if (aSeq > bSeq)
					return 1;
				else
					return 0;
			}
			// return aIdx < bIdx ? -1 : aIdx> bIdx ? 1 : 0;
		}
	}

	public void _____________Unused_functions_below______________() throws Exception {

	}

	// 이전버전
	public Map<String, Object> predictSealByRuleBased_____(Map<String, Object> param) throws Exception {

		// 최종 결과 Map
		Map<String, Object> result = new HashMap<String, Object>();

		if (!(boolean) param.get("target4_check")) { // 실행체크가 되지 않은 경우

			// Seal 정보 추천 아이템 목록
			List<Map<String, Object>> predict_itemList = param.get("predict_list") == null
					? new ArrayList<Map<String, Object>>()
					: (List<Map<String, Object>>) param.get("predict_list");

			List<Map<String, Object>> result_items = new ArrayList<Map<String, Object>>();
			Map<String, Object> result_item = null; // 결과

			for (Map<String, Object> item : predict_itemList) {
				Map<String, Object> itemRuleResult = new HashMap<String, Object>();
				itemRuleResult.put("RST", new ArrayList<Map<String, Object>>()); // 추천결과
				itemRuleResult.put("NOTE", new ArrayList<Map<String, Object>>());
				itemRuleResult.put("PROC", new ArrayList<Map<String, Object>>());

				result_item = new HashMap<String, Object>();
				result_item.put("predict_idx", String.valueOf(item.get("NO"))); // Item No.
				result_item.put("predict_msg", "complete");
				result_item.put("RESULT", itemRuleResult);
				result_item.put("param", item); // 조건

				result_items.add(result_item); // 결과리스트를 Add
			}
			result.put("RULE_RESULT", result_items);

			return result;
		}

		/*
		 * [입력파라미터] P_ID : 예측ID : 예측 시 마다 생성되는 UID ML 모델 추천사용 입력인자 Rule 추천 추가 입력인자
		 * 
		 * [출력] 결과 테이블에 저장 TB_RULE_RST TB_RULE_RST_NOTE TB_RULE_RES_PROC
		 * 
		 */

		// ----------------------------
		// 공용 변수
		// ----------------------------
		// Map<String,Object> ptm = new HashMap<String,Object>(); // 조회 임시 Param Map
		_productGroupInfo = mLMapper.getGroupingInfo("product"); // Product Grouping Info.
		List<Map<String, Object>> productGroupHierInfo = mLMapper.getGroupingHier(null); // Product Group Hierarchy
		_materialList = rBMapper.selectMaterialList(null); // Material List
		// boolean isProduct = false; //Product 체크 -> 추후삭제

		// 단위변환처리 정보
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		List<Map<String, Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();// 단위코드정보
		List<Map<String, Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();// 단위변환정보
		List<Map<String, Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();// Text 입력값 변환정보
		List<Map<String, Object>> listSsuChg = mLMapper.getSsuChglInfo();// SSU 변환 정보 (Visc 용)

		// List<Map<String,Object>> lTemp = null;
		// String sTmp = ""; // 문자 임시처리 변수

		// 추천결과 ID
		String _pID = param.get("P_ID") == null ? UUID.randomUUID() + "" : String.valueOf(param.get("P_ID"));

		// Seal 정보 추천 아이템 목록
		List<Map<String, Object>> predict_itemList = param.get("predict_list") == null
				? new ArrayList<Map<String, Object>>()
				: (List<Map<String, Object>>) param.get("predict_list");

		List<Map<String, Object>> result_items = new ArrayList<Map<String, Object>>();
		Map<String, Object> result_item = null; // 결과

		try {
			// -------------------------------------------------
			// 아이템별 Seal 추천정보 도출
			// -------------------------------------------------
			for (Map<String, Object> item : predict_itemList) {

				result_item = new HashMap<String, Object>();

				// ========================================================================================
				// ================= ITEM 별 Rule-Based 추천
				// =====================================================
				// ========================================================================================

				System.out.println("ITEM : " + item.toString());

				// ----------------------------
				// 결과 변수
				// ----------------------------
				int iPIdx = 0; // 추천 Index
				// Map<String,Object> rstList = new HashMap<String,Object>();// 추천결과 Map
				List<Map<String, Object>> sealRstList = new ArrayList<Map<String, Object>>(); // Seal Type 추천 결과 List
				// List<Map<String,Object>> materialRstList = new
				// ArrayList<Map<String,Object>>(); // Material Full 추천 결과 List
				List<Map<String, Object>> planRstList = new ArrayList<Map<String, Object>>(); // API Plan 추천 결과 List
				List<Map<String, Object>> material1RstList = new ArrayList<Map<String, Object>>(); // Material 1st 추천 결과
																									// List
				List<Map<String, Object>> material2RstList = new ArrayList<Map<String, Object>>(); // Material 2nd 추천 결과
																									// List
				List<Map<String, Object>> material3RstList = new ArrayList<Map<String, Object>>(); // Material 3rd 추천 결과
																									// List
				List<Map<String, Object>> material4RstList = new ArrayList<Map<String, Object>>(); // Material 4th 추천 결과
																									// List
				List<Map<String, Object>> noteRstList = new ArrayList<Map<String, Object>>(); // 추천결과 특이사항(Note) List
				List<Map<String, Object>> procRstList = new ArrayList<Map<String, Object>>(); // 추천결과 중간과정이력 List

				// Product Group Info (Array)
				String[] saProductGroup = (mLService.getGroupingStr(String.valueOf(item.get("PRODUCT")),
						_productGroupInfo, productGroupHierInfo)).split("[+]");
				// Product Info (Array)
				String[] saProduct = (mLService.getProductStr(String.valueOf(item.get("PRODUCT")), _productGroupInfo,
						0)).split("[+]");
				List<String> arrangement = new ArrayList<String>(); // arrangement 조건
				String sNo = String.valueOf(item.get("NO")); // Item No.

				// 입력조건 단위환산처리
				// convToStdUnit(item, "3", engine, listUnitCode, listUnitChg, listTransTxtVal,
				// listSsuChg);

				System.out.println("단위변환 ITEM : " + item.toString());

				// 추가변수
				item.put("SLURRY_SEAL_YN", "N"); // Slurry Seal 유무

				// 파라미터 Map
				Map<String, Object> fp = new HashMap<String, Object>();
				fp.put("sealRstList", sealRstList);
				// fp.put("materialRstList", materialRstList);
				fp.put("planRstList", planRstList);
				fp.put("noteRstList", noteRstList);
				fp.put("procRstList", procRstList);
				fp.put("material1RstList", material1RstList);
				fp.put("material2RstList", material2RstList);
				fp.put("material3RstList", material3RstList);
				fp.put("material4RstList", material4RstList);
				fp.put("arrangement", arrangement);
				fp.put("saProductGroup", saProductGroup);
				fp.put("saProduct", saProduct);

				// End User가 HDO 유무
				item.put("IS_HDO", "Z140020".equals(StringUtil.get(item.get("END_USER"))) ? "Y" : "N");

				if (!"".equals(StringUtil.get(item.get("GS_CASE")))) { // End User GS Caltex에 대한 선택값이 있을 경우
					iPIdx = step1_rule_priority_0(_pID, sNo, iPIdx, item, fp);
				} else {
					// -------------------------------------------------------------
					// Step 1
					// [B2] FTA 조건을 체크
					// 적용 RULE 기준 : B2010
					// -------------------------------------------------------------
					if (!"Z04900".equals(String.valueOf(item.get("APPLICATION")))) { // Application이 Others 인경우는 Skip
						// [B2] 기준 적용
						iPIdx = step1_rule_priority_10(_pID, sNo, iPIdx, item, fp);
					}
					System.out.println("Step 1 OK ");

					// -------------------------------------------------------------
					// Step 2
					// 우선적용 조건을 적용
					// -------------------------------------------------------------
					// 씰타입이 선정되지 않은 경우
					// if (sealRstList.size() <=0) {
					iPIdx = step2_rule_priority_20(_pID, sNo, iPIdx, item, fp);
					// } // end if (sealRstList.size() <=0) {
					System.out.println("step2_priority_rule_1 OK ");

					// -------------------------------------------------------------
					// Step 3
					// [C3]을 통해 Seal Type 선정
					// -------------------------------------------------------------
					iPIdx = step3_rule_C3(_pID, sNo, iPIdx, item, fp);
					System.out.println("setSealTypeListByC3Rule OK ");

					// -------------------------------------------------------------
					// 제약조건 체크
					// -------------------------------------------------------------
					step5_restrictions_chk(item, fp);

					// -------------------------------------------------------------
					// 추가정보
					// -------------------------------------------------------------
					step6_add_note(_pID, sNo, iPIdx, item, fp);
				}

				// ----------------------------------------
				// 추천 결과를 저장한다.
				// ----------------------------------------
				// setRuleRst(sealRstList);
				// setRuleRst(materialRstList);
				// setRuleRst(planRstList);
				// setRuleRstNote(noteRstList);
				// setRuleRstProc(procRstList);
				//
				// setRuleRst(material1RstList);
				// setRuleRst(material2RstList);
				// setRuleRst(material3RstList);
				// setRuleRst(material4RstList);

				// Sorting
				Collections.sort(sealRstList, new sortMap());
				Collections.sort(material1RstList, new sortMap());
				Collections.sort(material2RstList, new sortMap());
				Collections.sort(material3RstList, new sortMap());
				Collections.sort(material4RstList, new sortMap());
				Collections.sort(planRstList, new sortMap());
				Collections.sort(noteRstList, new sortMap());
				Collections.sort(procRstList, new sortMap());

				// Seq Reorder
				setRstListSeqReOrd(sealRstList);
				setRstListSeqReOrd(material1RstList);
				setRstListSeqReOrd(material2RstList);
				setRstListSeqReOrd(material3RstList);
				setRstListSeqReOrd(material4RstList);
				setRstListSeqReOrd(planRstList);

				// 하나의 리스트로 구성한다.
				List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();
				boolean isVal = false;

				for (int i = 1; i <= iPIdx; i++) {
					// idx별 max seq를 구한다.
					int iMaxSeq = 0;
					for (Map<String, Object> m : sealRstList) {
						if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
							iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
						}
					}
					for (Map<String, Object> m : material1RstList) {
						if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
							iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
						}
					}
					for (Map<String, Object> m : material2RstList) {
						if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
							iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
						}
					}
					for (Map<String, Object> m : material3RstList) {
						if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
							iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
						}
					}
					for (Map<String, Object> m : material4RstList) {
						if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
							iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
						}
					}
					for (Map<String, Object> m : planRstList) {
						if (NumberUtil.toInt(m.get("P_IDX")) == i && iMaxSeq < NumberUtil.toInt(m.get("P_SEQ"))) {
							iMaxSeq = NumberUtil.toInt(m.get("P_SEQ"));
						}
					}

					// seq 만큼 loop
					for (int j = 1; j <= iMaxSeq; j++) {
						Map<String, Object> rst = new HashMap<String, Object>();

						// Idx를 처음만 표시하게...
						if (j == 1) {
							rst.put("P_IDX", i);
						} else {
							rst.put("P_IDX", "");
						}

						rst.put("P_SEQ", j);

						// Seal Type
						isVal = false;
						for (Map<String, Object> m : sealRstList) {
							if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
								rst.put("SEAL", m.get("P_VAL"));
								rst.put("SEAL_CONFIG", m.get("ADD_INFO") == null ? ""
										: ((HashMap<String, Object>) m.get("ADD_INFO")).get("CONFIG"));
								rst.put("SEAL_ADD_INFO",
										m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
								isVal = true;
								break;
							}
						}
						if (!isVal)
							rst.put("SEAL", "");

						// Material 1Type
						isVal = false;
						for (Map<String, Object> m : material1RstList) {
							if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
								rst.put("MTRL1", m.get("P_VAL"));
								rst.put("MTRL1_ADD_INFO",
										m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
								isVal = true;
								break;
							}
						}
						if (!isVal)
							rst.put("MTRL1", "");

						// Material 2Type
						isVal = false;
						for (Map<String, Object> m : material2RstList) {
							if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
								rst.put("MTRL2", m.get("P_VAL"));
								rst.put("MTRL2_ADD_INFO",
										m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
								isVal = true;
								break;
							}
						}
						if (!isVal)
							rst.put("MTRL2", "");

						// Material 3Type
						isVal = false;
						for (Map<String, Object> m : material3RstList) {
							if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
								rst.put("MTRL3", m.get("P_VAL"));
								rst.put("MTRL3_ADD_INFO",
										m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
								isVal = true;
								break;
							}
						}
						if (!isVal)
							rst.put("MTRL3", "");

						// Material 4Type
						isVal = false;
						for (Map<String, Object> m : material4RstList) {
							if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
								rst.put("MTRL4", m.get("P_VAL"));
								rst.put("MTRL4_ADD_INFO",
										m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
								isVal = true;
								break;
							}
						}
						if (!isVal)
							rst.put("MTRL4", "");

						// Plan Type
						isVal = false;
						for (Map<String, Object> m : planRstList) {
							if (NumberUtil.toInt(m.get("P_IDX")) == i && NumberUtil.toInt(m.get("P_SEQ")) == j) {
								rst.put("PLAN", m.get("P_VAL"));
								rst.put("PLAN_ADD_INFO",
										m.get("ADD_INFO") == null ? new HashMap<String, Object>() : m.get("ADD_INFO"));
								isVal = true;
								break;
							}
						}
						if (!isVal)
							rst.put("PLAN", "");
						rstList.add(rst);
					}
				}
				// rstList.forEach(System.out::println);

				Map<String, Object> itemRuleResult = new HashMap<String, Object>();
//				itemRuleResult.put("SEAL",sealRstList);
//				itemRuleResult.put("MTRL1",material1RstList);
//				itemRuleResult.put("MTRL2",material2RstList);
//				itemRuleResult.put("MTRL3",material3RstList);
//				itemRuleResult.put("MTRL4",material4RstList);
//				itemRuleResult.put("MTRL",materialRstList); // 제거 예정
//				itemRuleResult.put("PLAN",planRstList);
				itemRuleResult.put("RST", rstList); // 추천결과
				itemRuleResult.put("NOTE", noteRstList);
				itemRuleResult.put("PROC", procRstList);

				// ========================================================================================
				// ================= ITEM 별 Rule-Based 추천
				// =====================================================
				// ========================================================================================

				result_item.put("predict_idx", sNo);
				result_item.put("predict_msg", "complete");
				result_item.put("RESULT", itemRuleResult);
				result_item.put("param", item); // 조건

				result_items.add(result_item); // 결과리스트를 Add

			} // end predict_itemList

			result.put("RULE_RESULT", result_items);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

		return result;
	}

	/*
	 * ==============================================================
	 * 
	 * 서브 Function
	 * 
	 * ==============================================================
	 */
	@SuppressWarnings("unchecked")
	private int step1_rule_priority_0(String _pID, String sNo, int iPIdx, Map<String, Object> item,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		// List<Map<String,Object>> materialRstList =
		// (List<Map<String,Object>>)fp.get("materialRstList");

		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");

		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		// End User Selection

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("ATTR1", item.get("GS_CASE")); // GS Caltex 최종선택 Case
		param.put("TEMP_MIN", item.get("TEMP_MIN"));
		param.put("TEMP_MAX", item.get("TEMP_MAX"));
		param.put("SPEC_GRAVITY_MIN", item.get("SPEC_GRAVITY_MIN"));
		param.put("SPEC_GRAVITY_MAX", item.get("SPEC_GRAVITY_MAX"));
		param.put("SEAL_CHAM_MIN", item.get("SEAL_CHAM_MIN"));
		param.put("SEAL_CHAM_MAX", item.get("SEAL_CHAM_MAX"));
		param.put("VAP_PRES_MIN", NumberUtil.toDouble(item.get("VAP_PRES_MIN")) / 0.069); // PSIA로 변경 PSIA = BARA /
																							// 0.069
		param.put("VAP_PRES_MAX", NumberUtil.toDouble(item.get("VAP_PRES_MAX")) / 0.069); // PSIA로 변경 PSIA = BARA /
																							// 0.069

		List<Map<String, Object>> list = rBMapper.selectRuleComListB801(param);

		if (list.size() > 0) {
			String sNote = null;
			for (Map<String, Object> m : list) {
				iPIdx++;
				sNote = "";
				// Seal Type
				if (!StringUtil.isBlank(m.get("ATTR2"))) {
					for (String s : String.valueOf(m.get("ATTR2")).split(",")) {
						setResultList(sealRstList, _pID, sNo, "S", iPIdx, s);
					}
				}

				// Material
				System.out.println("----->" + m.get("ATTR3"));

				if (!StringUtil.isBlank(m.get("ATTR3"))) {
					for (String s : String.valueOf(m.get("ATTR3")).split(",")) {
						for (String s_ : s.split("/")) {
							String[] s__ = s_.split(" ");
							if (s__.length > 0)
								setMaterialResultListPre2(material1RstList, sealRstList, _pID, sNo, "1", iPIdx,
										s__[0].trim(), null);
							if (s__.length > 1)
								setMaterialResultListPre2(material2RstList, sealRstList, _pID, sNo, "2", iPIdx,
										s__[1].trim(), null);
							if (s__.length > 2)
								setMaterialResultListPre2(material3RstList, sealRstList, _pID, sNo, "3", iPIdx,
										s__[2].trim(), null);
							if (s__.length > 3)
								setMaterialResultListPre2(material4RstList, sealRstList, _pID, sNo, "4", iPIdx,
										s__[3].trim(), null);
						}
					}
				}

				// API Plan
				if (!StringUtil.isBlank(m.get("ATTR4"))) {
					for (String s : String.valueOf(m.get("ATTR4")).split(",")) {
						setResultList(planRstList, _pID, sNo, "P", iPIdx, s);
					}
				}

				// 특이사항
				sNote += StringUtil.get(m.get("ATTR5")) + " , " + StringUtil.get(m.get("ATTR6"));
				if (!"".equals(sNote)) {
					setResultNoteList(noteRstList, _pID, sNo, iPIdx, sNote);
				}

				// 중간진행사항
				setResultProcList(procRstList, _pID, sNo, iPIdx, "[C8] End User 기준 적용");
			}
		} else {
			iPIdx++;

			setResultProcList(procRstList, _pID, sNo, iPIdx, "[C8] 범위에 해당하는 조건이 없음.");
			setResultNoteList(noteRstList, _pID, sNo, iPIdx, "[C8] GS Caltex - 고객지원팀과 협의");
		}

		return iPIdx;
	}

	@SuppressWarnings("unchecked")
	private int step1_rule_priority_10(String _pID, String sNo, int iPIdx, Map<String, Object> item,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		// List<Map<String,Object>> materialRstList =
		// (List<Map<String,Object>>)fp.get("materialRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");

		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		Map<String, Object> ptm = new HashMap<String, Object>();
		ptm.put("MCD", "B201");
		ptm.put("ATTR1", StringUtil.get(item.get("EQUIPMENT"), "-----")); // Equipment 선택값 - 빈값일 경우 임의의 갑을 설정 -> 데이터가
																			// Load되지 않게
		List<Map<String, Object>> rComList = getRuleComListType1(ptm);

		if (!rComList.isEmpty()) {
			// 씰정보별로 값이 있을 경우 저장한다.
			for (Map<String, Object> m : rComList) {
				iPIdx++;

				// Seal Type
				if (!StringUtil.isBlank(m.get("ATTR2"))) {
					for (String s : String.valueOf(m.get("ATTR2")).split(",")) {
						setResultList(sealRstList, _pID, sNo, "S", iPIdx, s);
					}
				}

				// Material
				if (!StringUtil.isBlank(m.get("ATTR3"))) {
					for (String s : String.valueOf(m.get("ATTR3")).split(",")) {
						for (String s_ : s.split("/")) {
							String[] s__ = s_.split(" ");
							if (s__.length > 0)
								setMaterialResultListPre2(material1RstList, sealRstList, _pID, sNo, "1", iPIdx,
										s__[0].trim(), null);
							if (s__.length > 1)
								setMaterialResultListPre2(material2RstList, sealRstList, _pID, sNo, "2", iPIdx,
										s__[1].trim(), null);
							if (s__.length > 2)
								setMaterialResultListPre2(material3RstList, sealRstList, _pID, sNo, "3", iPIdx,
										s__[2].trim(), null);
							if (s__.length > 3)
								setMaterialResultListPre2(material4RstList, sealRstList, _pID, sNo, "4", iPIdx,
										s__[3].trim(), null);
						}
					}
				}

				// API Plan
				if (!StringUtil.isBlank(m.get("ATTR4"))) {
					for (String s : String.valueOf(m.get("ATTR4")).split(",")) {
						setResultList(planRstList, _pID, sNo, "P", iPIdx, s);
					}
				}

				// 특이사항
				if (!StringUtil.isBlank(m.get("ATTR5"))) {
					setResultNoteList(noteRstList, _pID, sNo, iPIdx, String.valueOf(m.get("ATTR5")));
				}

				// 중간진행사항
				setResultProcList(procRstList, _pID, sNo, iPIdx, "[B2] FTA 기준 적용");
			}
		}
		return iPIdx;
	}

	@SuppressWarnings("unchecked")
	private int step2_rule_priority_20(String _pID, String sNo, int iPIdx, Map<String, Object> item,
			Map<String, Object> fp) throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		// List<Map<String,Object>> materialRstList =
		// (List<Map<String,Object>>)fp.get("materialRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");

		List<String> arrangementList = (List<String>) fp.get("arrangementList");
		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		// -------------------------------------------------------------
		// Step 2
		// 우선적용조건 체크
		// [B1] ACR (B1-2, B1-5, B1-6, B1-13)
		// [B4] (Hot) Water Guide
		// -------------------------------------------------------------

		// B1-7 점도체크
		if (NumberUtil.toDouble(item.get("VISC_NOR")) > 2125) {
			item.put("ARRANGEMENT", "3");
			setResultProcList(procRstList, _pID, sNo, 999, "[B1-7] Viscosity 조건으로 Arrangement 3 적용");
		}

		Map<String, Object> ptm = new HashMap<String, Object>(); // 쿼리 파라미터

		// [B1-2] Styrene Monomer Applications
		// Product Group = Styrene Monomer => SM
		// 적용 RULE 기준 : B1201

		if (isProduct("SM", saProductGroup, saProduct)) {
			ptm.clear();
			ptm.put("MCD", "B1201");
			ptm.put("SCD", "B1201010");
			List<Map<String, Object>> rComList = getRuleComListType1(ptm);

			if (!rComList.isEmpty()) {
				iPIdx++;
				// Seal Type
				setResultList(sealRstList, _pID, sNo, "S", iPIdx, rComList.get(0).get("ATTR1"));
				// Material
				for (String s : String.valueOf(rComList.get(0).get("ATTR2")).split(",")) {
					for (String s_ : s.split("/")) {
						String[] s__ = s_.split(" ");
						if (s__.length > 0)
							setMaterialResultListPre2(material1RstList, sealRstList, _pID, sNo, "1", iPIdx,
									s__[0].trim(), null);
						if (s__.length > 1)
							setMaterialResultListPre2(material2RstList, sealRstList, _pID, sNo, "2", iPIdx,
									s__[1].trim(), null);
						if (s__.length > 2)
							setMaterialResultListPre2(material3RstList, sealRstList, _pID, sNo, "3", iPIdx,
									s__[2].trim(), null);
						if (s__.length > 3)
							setMaterialResultListPre2(material4RstList, sealRstList, _pID, sNo, "4", iPIdx,
									s__[3].trim(), null);
					}
				}
				// API Plan
				setResultList(planRstList, _pID, sNo, "P", iPIdx, rComList.get(0).get("ATTR3"));

				// 중간진행사항
				setResultProcList(procRstList, _pID, sNo, iPIdx, "[B1-2] Styrene Monomer Applications Rule 적용");
			}
		}

		// [B1-5] refinery (정유) 서비스
		// Step1에서 refinery 서비스 선택 후 Equipment가 Others인 Case
		if ("Z060390".equals(String.valueOf(item.get("EQUIPMENT")))) {

			List<String> B191_cond = new ArrayList<String>();

			// [B1-9]H2S 포함일 경우 체크
			if (isProduct("H2S", saProductGroup, saProduct)) {
				// H2S 농도에 따른 Arrangement
				ptm.clear();
				ptm.put("MCD", "B1901");
				// ptm.put("H2S_CONT", item.get("H2S_CONT"));
				ptm.put("H2S_CONT", getProductCont(item, "H2S", "PPM"));
				List<Map<String, Object>> rComB1901List = rBMapper.selectRuleComListB1901(ptm);

				if (!rComB1901List.isEmpty()) {
					if ("3".equals(String.valueOf(rComB1901List.get(0).get("ATTR3")))) { // 가압조건일 경우
						B191_cond.add("P"); // 가압
					} else {
						B191_cond.add("P");
						B191_cond.add("UP"); // 비가압
					}

					if (!"".equals(String.valueOf(rComB1901List.get(0).get("ATTR4")))) { // 추가정보가 있을 경우
						setResultNoteList(noteRstList, _pID, sNo, -1,
								String.valueOf(rComB1901List.get(0).get("ATTR4")));
					}
				}
			} else {
				B191_cond.add("P");
				B191_cond.add("UP");
			}

			// B1-5 정보 조회
			ptm.clear();
			ptm.put("MCD", "B1501");
			ptm.put("P_TYPE", B191_cond);
			List<Map<String, Object>> rComB1501List = rBMapper.selectRuleComListB1501(ptm);

			if (!rComB1501List.isEmpty()) {
				for (Map<String, Object> m : rComB1501List) {
					iPIdx++;

					// Seal Type
					if (m.get("ATTR2") != null && !"".equals(String.valueOf(m.get("ATTR2")))) {
						for (String s : String.valueOf(m.get("ATTR2")).split(",")) {
							setResultList(sealRstList, _pID, sNo, "S", iPIdx, s);
						}
					}
					// API Plan
					if (m.get("ATTR3") != null && !"".equals(String.valueOf(m.get("ATTR3")))) {
						for (String s : String.valueOf(m.get("ATTR3")).split(",")) {
							setResultList(planRstList, _pID, sNo, "P", iPIdx, s);
						}
					}

					// 중간진행사항
					setResultProcList(procRstList, _pID, sNo, iPIdx, "[B1-5] General Refinery Services 적용");
				}

				// Product에 따른 추가정보
				for (String s : new String[] { "WATER", "HYDROCARBON" }) {
					if (isProduct(s, saProductGroup, saProduct)) {
						ptm.clear();
						ptm.put("MCD", "B1502");
						ptm.put("ATTR1", s);
						List<Map<String, Object>> rComList = getRuleComListType1(ptm);
						for (Map<String, Object> m : rComList) {
							setResultNoteList(noteRstList, _pID, sNo, -1, String.valueOf(m.get("ATTR2")));
						}
					}
				}
			}

		} // end [B1-5] refinery (정유) 서비스

		// [B1-6] HF alkylation 유체 사용 시
		// if(isProduct("ALKYLATE HF",saProductGroup)) {
		// Step1에서 refinery 서비스 선택 후 Equipment가 HF Alkylation Unit인 Case
		if ("Z060385".equals(String.valueOf(item.get("EQUIPMENT")))) {
			ptm.clear();
			ptm.put("MCD", "B1601");
			ptm.put("SCD", "B1601010");
			List<Map<String, Object>> rComList = getRuleComListType1(ptm);

			if (!rComList.isEmpty()) {
				iPIdx++;
				setResultList(sealRstList, _pID, sNo, "S", iPIdx, rComList.get(0).get("ATTR1"));// Seal Type

				// Material
				for (String s : String.valueOf(rComList.get(0).get("ATTR2")).split(",")) {
					for (String s_ : s.split("/")) {
						String[] s__ = s_.split(" ");
						if (s__.length > 0)
							setMaterialResultListPre2(material1RstList, sealRstList, _pID, sNo, "1", iPIdx,
									s__[0].trim(), null);
						if (s__.length > 1)
							setMaterialResultListPre2(material2RstList, sealRstList, _pID, sNo, "2", iPIdx,
									s__[1].trim(), null);
						if (s__.length > 2)
							setMaterialResultListPre2(material3RstList, sealRstList, _pID, sNo, "3", iPIdx,
									s__[2].trim(), null);
						if (s__.length > 3)
							setMaterialResultListPre2(material4RstList, sealRstList, _pID, sNo, "4", iPIdx,
									s__[3].trim(), null);
					}
				}

				setResultList(planRstList, _pID, sNo, "P", iPIdx, rComList.get(0).get("ATTR3"));// API Plan

				// 중간진행사항
				setResultProcList(procRstList, _pID, sNo, iPIdx, "[B1-6] HF alkylation Fluid 적용 시");
			}
		}

		// [B1-13] Product에 NaOH 가 포함될 경우
		// Material, API Plan = [B1-13]
		if (String.valueOf(item.get("PRODUCT")).toUpperCase().contains("NAOH")) {
			ptm.clear();
			ptm.put("MCD", "B11301");
			// ptm.put("NAOH_CONT", item.get("NAOH_CONT"));
			ptm.put("NAOH_CONT", getProductCont(item, "NAOH", "%"));
			ptm.put("TEMP_MAX", item.get("TEMP_MAX"));
			List<Map<String, Object>> rComList = rBMapper.selectRuleComListB11301(ptm);

			if (!rComList.isEmpty()) {
				ptm.clear();
				ptm.put("MCD", "B11302");
				ptm.put("ATTR1", rComList.get(0).get("ATTR1"));
				List<Map<String, Object>> rComList2 = getRuleComListType1(ptm); // B11302 - Caustic services Selections

				if (!rComList2.isEmpty()) {
					Map<String, Object> m = rComList2.get(0);
					iPIdx++;

					// API Plan
					if (m.get("ATTR3") != null && !"".equals(String.valueOf(m.get("ATTR3")))) {
						for (String s : String.valueOf(m.get("ATTR3")).split(",")) {
							setResultList(planRstList, _pID, sNo, "P", iPIdx, s);
						}
					}

					// Material 1 : Metal 1st
					if (m.get("ATTR4") != null && !"".equals(String.valueOf(m.get("ATTR4")))) {
						for (String s : String.valueOf(m.get("ATTR4")).split(",")) {
							setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, s, null);
						}
					}

					// Material 2 : face 2nd
					if (m.get("ATTR5") != null && !"".equals(String.valueOf(m.get("ATTR5")))) {
						for (String s : String.valueOf(m.get("ATTR5")).split(",")) {
							setMaterialResultListPre(material2RstList, sealRstList, _pID, sNo, "2", iPIdx, s, null);
						}
					}

					// Material 3 : Gasket 3nd
					if (m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
						for (String s : String.valueOf(m.get("ATTR7")).split(",")) {
							setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, s, null);
						}
					}

					// Material 4 : face 4th
					if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
						for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
							setMaterialResultListPre(material4RstList, sealRstList, _pID, sNo, "4", iPIdx, s, null);
						}
					}

					// Arrangement Add
					if (arrangementList.contains(m.get("ATTR2")))
						arrangementList.add(String.valueOf(m.get("ATTR2")));

					// 중간진행사항
					setResultProcList(procRstList, _pID, sNo, iPIdx, "[B1-13] Fluid with NaOH Rule 적용");
				}
			}
		}

		// [B4] (Hot) Water Guide
		if (isProduct("WATER", saProductGroup, saProduct)) { // Product Grpup = WATER

			List<Map<String, Object>> rComList = rBMapper.selectRuleComListB401(item);

			if (!rComList.isEmpty()) {
				for (Map<String, Object> m : rComList) {

					iPIdx++;

					// Press Limit 체크
					String bPressLimitChkFailMsg = "";
					ptm.clear();
					ptm.put("ATTR1", item.get("ATTR10")); // press limit 구분
					ptm.put("SHAFT_SIZE", item.get("SHAFT_SIZE"));
					List<Map<String, Object>> rComList3 = rBMapper.selectRuleComListB403(ptm);
					if (!rComList3.isEmpty()) {
						// press limit를 초과하는 경우
						if (Double.parseDouble(String.valueOf(item.get("SEAL_CHAM_MAX"))) < Double
								.parseDouble(String.valueOf(rComList3.get(0).get("ATTR4")))) {
							if (m.get("ATTR11") != null) { // Press Limit 체크 실패시 처리 메세지가 있을 경우
								bPressLimitChkFailMsg = String.valueOf(m.get("ATTR11"));
							} else { // Skip
								setResultNoteList(noteRstList, _pID, sNo, iPIdx, " [B4] 압력 허용치 초과 ");
								continue;
							}
						}
					}

					// Seal Type
					if (m.get("ATTR3") != null && !"".equals(String.valueOf(m.get("ATTR3")))) {
						for (String s : String.valueOf(m.get("ATTR3")).split(",")) {
							setResultList(sealRstList, _pID, sNo, "S", iPIdx, s);
						}
					}

					// Material 1 : Metal 1st
					if (m.get("ATTR4") != null && !"".equals(String.valueOf(m.get("ATTR4")))) {
						for (String s : String.valueOf(m.get("ATTR4")).split(",")) {
							setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, s, null);
						}
					}

					// Material 2 : face 2nd
					if (m.get("ATTR5") != null && !"".equals(String.valueOf(m.get("ATTR5")))) {
						for (String s : String.valueOf(m.get("ATTR5")).split(",")) {
							setMaterialResultListPre(material2RstList, sealRstList, _pID, sNo, "2", iPIdx, s, null);
						}
					}

					// Material 3 : Gasket 3nd
					if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
						for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
							setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, s, null);
						}
					}

					// Material 4 : face 4th
					if (m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
						for (String s : String.valueOf(m.get("ATTR7")).split(",")) {
							setMaterialResultListPre(material4RstList, sealRstList, _pID, sNo, "4", iPIdx, s, null);
						}
					}

					// API Plan
					if (m.get("ATTR8") != null && !"".equals(String.valueOf(m.get("ATTR8")))) {
						for (String s : String.valueOf(m.get("ATTR8")).split(",")) {
							setResultList(planRstList, _pID, sNo, "P", iPIdx, s);
						}
					}

					// 추가정보
					if (!"".equals(StringUtil.get(m.get("ATTR9")))) {
						ptm.clear();
						ptm.put("MCD", "B402"); // B4 추가정보
						ptm.put("ATTR1", m.get("ATTR9")); // 추가정보 구분
						List<Map<String, Object>> rComList2 = rBMapper.selectRuleComListType1(ptm);
						for (Map<String, Object> m2 : rComList2) {
							setResultNoteList(noteRstList, _pID, sNo, iPIdx, String.valueOf(m2.get("ATTR2")));
						}
					}

					// Press Limit Check 실패 시 메세지가 있는 경우
					if (!"".equals(bPressLimitChkFailMsg)) {
						ptm.clear();
						ptm.put("MCD", "B402"); // B4 추가정보
						ptm.put("ATTR1", bPressLimitChkFailMsg); // 추가정보 구분
						List<Map<String, Object>> rComList2 = rBMapper.selectRuleComListType1(ptm);
						for (Map<String, Object> m2 : rComList2) {
							setResultNoteList(noteRstList, _pID, sNo, iPIdx, String.valueOf(m2.get("ATTR2")));
						}
					}

					// 중간진행사항
					setResultProcList(procRstList, _pID, sNo, iPIdx, "[B4] Water Application Guide 적용");

				}
			}

		}

		// [B1-8] 극저온 서비스
		List<Map<String, Object>> rComB1_8List = rBMapper.selectRuleComListB1801(item);

		if (!rComB1_8List.isEmpty()) {
			for (Map<String, Object> m : rComB1_8List) {

				iPIdx++;

				// Seal Type
				if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
					for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
						setResultList(sealRstList, _pID, sNo, "S", iPIdx, s);
					}
				}

				// Material
				if (m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
					// Material
					for (String s : String.valueOf(m.get("ATTR7")).split(",")) {
						for (String s_ : s.split("/")) {
							String[] s__ = s_.split(" ");
							if (s__.length > 0)
								setMaterialResultListPre2(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, s__[0],
										null);
							if (s__.length > 1)
								setMaterialResultListPre2(material2RstList, sealRstList, _pID, sNo, "2", iPIdx, s__[1],
										null);
							if (s__.length > 2)
								setMaterialResultListPre2(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, s__[2],
										null);
							if (s__.length > 3)
								setMaterialResultListPre2(material4RstList, sealRstList, _pID, sNo, "4", iPIdx, s__[3],
										null);
						}
					}
				}

				// API Plan
				if (m.get("ATTR8") != null && !"".equals(String.valueOf(m.get("ATTR8")))) {
					for (String s : String.valueOf(m.get("ATTR8")).split(",")) {
						setResultList(planRstList, _pID, sNo, "P", iPIdx, s);
					}
				}

				// 추가정보
				if (m.get("ATTR9") != null) {
					ptm.clear();
					ptm.put("MCD", "B1802"); // B4 추가정보
					ptm.put("ATTR1", m.get("ATTR9")); // 추가정보 구분
					List<Map<String, Object>> rComList2 = rBMapper.selectRuleComListType1(ptm);
					for (Map<String, Object> m2 : rComList2) {
						setResultNoteList(noteRstList, _pID, sNo, iPIdx,
								"[B1-8] Cryogenic Applications - " + String.valueOf(m2.get("ATTR2")));
					}
				}

				// 중간진행사항
				setResultProcList(procRstList, _pID, sNo, iPIdx, "[B1-8] Cryogenic Applications Rule 적용");

			}
		}

		// [B1-1] Fluid with Solid
		if (!"".equals(StringUtil.get(item.get("SOLID_SIZE_NOR")))
				|| !"".equals(StringUtil.get(item.get("SOLID_SIZE_MIN")))
				|| !"".equals(StringUtil.get(item.get("SOLID_SIZE_MAX")))
				|| !"".equals(StringUtil.get(item.get("SOLID_SIZE_MAX_CHK")))
				|| (getProductCont(item, "SOLID", "PPM") > 0)) {

			if ("Y".equals(StringUtil.get(item.get("SOLID_SIZE_MAX_CHK")))) { // Max Check 된 경우
				iPIdx++;
				setResultList(planRstList, _pID, sNo, "P", iPIdx, "32"); // Plan 32 고정
			} else {
				double d = 0.d;
				double d1 = NumberUtil.toDouble(item.get("SOLID_SIZE_NOR"));
				double d2 = NumberUtil.toDouble(item.get("SOLID_SIZE_MIN"));
				double d3 = NumberUtil.toDouble(item.get("SOLID_SIZE_MAX"));
				if (d < d1)
					d = d1;
				if (d < d2)
					d = d2;
				if (d < d3)
					d = d3;

				item.put("SOLID_SIZE", d);

				List<Map<String, Object>> rComB1_1List = rBMapper.selectRuleComListB1101(item);
				if (!rComB1_1List.isEmpty()) {
					for (Map<String, Object> m : rComB1_1List) {
						if (!"".equals(StringUtil.get(m.get("ATTR8")))) {
							iPIdx++;
							setResultList(planRstList, _pID, sNo, "P", iPIdx, StringUtil.get(m.get("ATTR8")));
						}
						if (!"".equals(StringUtil.get(m.get("ATTR5")))) {
							item.put("SLURRY_SEAL_YN", "Y"); // slurry seal 적용 [C3]에서 활용
						}
					}
				}

			}

			// 중간진행사항
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[B1-1] Fluid with Solid Rule 적용");
		}

		// [C7-6] Amine
		// O-ring : Chemraz 605, Chemraz 505, 나머지 FFKM의 순으로 적용
		if (isProduct("AMINE", saProductGroup, saProduct)) {
			iPIdx++;
			for (String s : (new String[] { "AD", "G005", "X675" })) {
				setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, s, null);
			}

			// 중간진행사항
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[C7-6] Amine Rule 적용 : O-Ring");
		}

		// [C7-7] EO / PO ETHYLENE OXIDE / PROPYLENE OXIDE
		// O-ring : Chemraz 605 적용
		if (isProduct("ETHYLENE OXIDE", saProductGroup, saProduct)
				|| isProduct("PROPYLENE OXIDE", saProductGroup, saProduct)) {
			iPIdx++;
			for (String s : (new String[] { "AD" })) {
				setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, s, null);
			}
			// 중간진행사항
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[C7-7] EO / PO Rule 적용 : O-Ring");

			item.put("ARRANGEMENT", "3"); // Arrangement 설정

		}

		// [C7-8] H2SO4 농도에 따른
		if (isProduct("H2SO4", saProductGroup, saProduct)) {

			List<Map<String, Object>> rComC78List = null;
			List<Map<String, Object>> rComC781List = rBMapper.selectRuleComListC7801(item);

			if (rComC781List.size() > 0) {
				rComC78List = rComC781List;
			} else {
				rComC78List = rBMapper.selectRuleComListC7802(item);
			}

			if (rComC78List != null && rComC78List.size() > 0) {
				HashMap<String, Object> m = (HashMap<String, Object>) rComC781List.get(0);

				iPIdx++;

				// 재질 설정
				// Material 1 : Metal 1st
				if (m.get("ATTR6") != null && !"".equals(String.valueOf(m.get("ATTR6")))) {
					for (String s : String.valueOf(m.get("ATTR6")).split(",")) {
						setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, s, null);
					}
				}

				// Material 2 : face 2nd
				if (m.get("ATTR7") != null && !"".equals(String.valueOf(m.get("ATTR7")))) {
					for (String s : String.valueOf(m.get("ATTR7")).split(",")) {
						setMaterialResultListPre(material2RstList, sealRstList, _pID, sNo, "2", iPIdx, s, null);
					}
				}

				// Material 3 : Gasket 3nd
				if (m.get("ATTR9") != null && !"".equals(String.valueOf(m.get("ATTR9")))) {
					for (String s : String.valueOf(m.get("ATTR9")).split(",")) {
						setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, s, null);
					}
				}

				// Material 4 : face 4th
				if (m.get("ATTR8") != null && !"".equals(String.valueOf(m.get("ATTR8")))) {
					for (String s : String.valueOf(m.get("ATTR8")).split(",")) {
						setMaterialResultListPre(material4RstList, sealRstList, _pID, sNo, "4", iPIdx, s, null);
					}
				}

				// arrangement
				if (item.get("ARRANGEMENT") == null
						|| (NumberUtil.toDouble(item.get("ARRANGEMENT"))) < NumberUtil.toDouble(m.get("ATTR5"))) {
					item.put("ARRANGEMENT", m.get("ATTR5")); // Arrangement 설정
				}

				// 추가정보가 있을 경우
				if (!"".equals(StringUtil.get(m.get("ATTR10")))) {
					ptm.clear();
					ptm.put("MCD", "C7803");
					ptm.put("ATTR1", m.get("ATTR10"));
					List<Map<String, Object>> rComList = getRuleComListType1(ptm);
					if (rComList.size() > 0) {
						setResultNoteList(noteRstList, _pID, sNo, iPIdx, String.valueOf(rComList.get(0).get("ATTR2")));
					}
				}

				// 중간진행사항
				setResultProcList(procRstList, _pID, sNo, iPIdx, "[C7-8] H2SO4 Rule 적용 ");
			}

		}

		// [C7-11] Propane 유체일 경우
		if (isProduct("PROPANE", saProductGroup, saProduct)) {
			iPIdx++;
			// Material 3 : Gasket
			setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, "[FFKM]", null);
			// 중간진행사항
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[C7-11] Propane Rule 적용 ");
		}

		// [C6-5] Waste Water 유체일 경우
		if (isProduct("WASTE WATER", saProductGroup, saProduct)) {
			iPIdx++;
			// Material 3 : Gasket
			setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, "[FFKM]", null);
			// 중간진행사항
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[C6] Waste Water Rule 적용 ");
		}

		// [C6-9] BUTADIENE 유체일 경우
		if (isProduct("BUTADIENE", saProductGroup, saProduct)) {
			iPIdx++;
			// Material 3 : Gasket
			setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, "G005", null);
			// 중간진행사항
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[C6] Butadiene Rule 적용 ");
		}

		// [C7-12] EDC , 1,2-Dichloroethane 유체일 경우
		if (isProduct("ETHYLENE DICHLORIDE", saProductGroup, saProduct)) {
			// ISC2-PX, ISC2-682PX

			iPIdx++;
			// Seal Type
			setResultList(sealRstList, _pID, sNo, "S", iPIdx, "ISC2-PX");
			setResultList(sealRstList, _pID, sNo, "S", iPIdx, "ISC2-682PX");

			// 중간진행사항
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[C7-12] EDC Rule 적용 ");
		}

		// [C7-13] Residue 유체일 경우
		if (isProduct("RESIDUE", saProductGroup, saProduct)) {
			iPIdx++;

			String sProc = " ";

			// 공통 사항 : Inboard Seal Face SiC vs SiC 적용 (점도 및 온도에 관계 없이)

			// Material 2 : face
			setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "2", iPIdx, "SL", null);
			// Material 4 : face
			setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "4", iPIdx, "SL", null);

			sProc += "공통사항 : SiC vs SiC 적용,";

			// Clean 여부에 따라 다음과 같이 적용
			// Clean 하지 않거나 또는 정확한 정보 없는 경우 : Plan 32 적용
			// Clean 한 경우 : Pour point / 온도 하락 시 점도 변화 / 상온에서 굳는 성질이 있는지 확인 -> 3

			// Clean한 경우 유체 성질에 따른 Plan 선정
			// 굳는 성질이 있는 유체일 경우 : Arrangement 3 적용
			// 굳는 성질이 없는 유체일 경우 : Arrangement 2 적용
			if ("Y".equals(StringUtil.get(item.get("RESI_CLEAN_GB")))) {
				if ("Y".equals(StringUtil.get(item.get("RESI_HRDN_GB")))) { // 굳는성질
					// arrangement
					item.put("ARRANGEMENT", "3");

					sProc += "ARRANGEMENT 3 적용,";
				} else {
					// arrangement
					if (item.get("ARRANGEMENT") == null || (NumberUtil.toDouble(item.get("ARRANGEMENT"))) < 2) {
						item.put("ARRANGEMENT", "2");
						sProc += "ARRANGEMENT 2 적용,";
					}
				}
			} else {
				setResultList(planRstList, _pID, sNo, "P", iPIdx, "32");
				sProc += "Plan 32 적용,";
			}

			// 중간진행사항
			setResultProcList(procRstList, _pID, sNo, iPIdx,
					"[C7-13] Residue Rule 적용 :" + sProc.substring(0, sProc.length() - 1));

		}

		// [C7-14] Hot Oil 일 경우
		if (isProduct("OIL", saProductGroup, saProduct)) {

			// 유체 성질에 따른 Plan 선정
			// 굳는 성질이 있는 유체일 경우
			// Single Seal : Plan 02/62 or 32/62 적용
			// Dual Seal : Plan 53A/B/C or 54 적용
			// 굳는 성질이 없는 유체일 경우
			// Single Seal : Plan 23/62
			// Dual Seal : Plan 23/52

			if (NumberUtil.toDouble(item.get("TEMP_NOR")) >= 80) { // 0il & 80C 이상
				iPIdx++;
				if ("Y".equals(StringUtil.get(item.get("OIL_HRDN_YN")))) { // 굳는성질일 경우
					if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {
						setResultList(planRstList, _pID, sNo, "P", iPIdx, "53A/B/C");
						setResultList(planRstList, _pID, sNo, "P", iPIdx, "54");
					} else {
						setResultList(planRstList, _pID, sNo, "P", iPIdx, "02/62");
						setResultList(planRstList, _pID, sNo, "P", iPIdx, "32/62");
					}
				} else {
					if (NumberUtil.toInt(item.get("ARRANGEMENT")) >= 2) {
						setResultList(planRstList, _pID, sNo, "P", iPIdx, "23/52");
					} else {
						setResultList(planRstList, _pID, sNo, "P", iPIdx, "23/62");
					}
				}

				// 중간진행사항
				setResultProcList(procRstList, _pID, sNo, iPIdx,
						"[C7-14] Hot Oil Rule 적용 : Arrangement : " + StringUtil.get(item.get("ARRANGEMENT"), "-"));
			}

		}

		// [C7-9] Sulfur 일 경우
		if (isProduct("SULFUR", saProductGroup, saProduct)) {
			// Molten Sulfur 또는 Sulfur 함유 & Product 비중 1.5 이상일 경우
			// BXRH, HXCU (Gland, Sleeve : 316SS), Plan 02/62

			if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 1.5) {
				iPIdx++;

				// Seal
				setResultList(sealRstList, _pID, sNo, "S", iPIdx, "BXRH");// Seal Type

				// Material
				setMaterialResultListPre2(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, "H", null);
				setMaterialResultListPre2(material2RstList, sealRstList, _pID, sNo, "2", iPIdx, "X", null);
				setMaterialResultListPre2(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, "C", null);
				setMaterialResultListPre2(material4RstList, sealRstList, _pID, sNo, "4", iPIdx, "U", null);

				// API Plan
				setResultList(planRstList, _pID, sNo, "P", iPIdx, "02/62");

				// Note
				setResultNoteList(noteRstList, _pID, sNo, iPIdx, "Gland, Sleeve : 316SS");

				// 중간진행사항
				setResultProcList(procRstList, _pID, sNo, iPIdx, "[C7-9] Sulfur Rule 적용 :  S.G >=1.5  ");
			}
		}

		// [B1-14] Crude oil with water cut
		if (isProduct("CRUDE OIL", saProductGroup, saProduct) && isProduct("WATER", saProductGroup, saProduct)) {
			ptm.clear();
			ptm.put("WATER_CONT", getProductCont(item, "WATER", "%"));
			List<Map<String, Object>> list = rBMapper.selectRuleComListB11401(ptm);
			if (list.size() > 0) {
				Map<String, Object> data = list.get(0);
				if (!"".equals(StringUtil.get(data.get("ATTR5")))) { // arrangement
					if ((NumberUtil.toDouble(item.get("ARRANGEMENT"))) < NumberUtil.toDouble(data.get("ATTR5"))) {
						item.put("ARRANGEMENT", data.get("ATTR5")); // Arrangement 설정
						// 중간진행사항
						setResultProcList(procRstList, _pID, sNo, iPIdx,
								"[B1-14] Crude oil with water cut 기준 적용 - Arrangement :  " + data.get("ATTR5"));
					}
				}

				// metal material 설정

				// Material 1 : Metal
				for (String s : StringUtil.get(data.get("ATTR6")).split(",")) {
					setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "3", iPIdx, s, null);
					// 중간진행사항
					setResultProcList(procRstList, _pID, sNo, iPIdx,
							"[B1-14] Crude oil with water cut Rule 적용 - Metal Material : " + s);
				}

			}
		}

		return iPIdx;

	}

	/**
	 * 입력조건을 받아 C3 Rule에서 조건을 만족하는 Seal Type을 추출한다.
	 * 
	 * @param p
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int step3_rule_C3(String _pID, String sNo, int iPIdx, Map<String, Object> item, Map<String, Object> fp)
			throws Exception {

		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		// List<Map<String,Object>> materialRstList =
		// (List<Map<String,Object>>)fp.get("materialRstList");
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");
		// List<String> arrangementList = (List<String>)fp.get("arrangementList");
		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		// [B3] API682 기준에 따라
		List<Map<String, Object>> rComList = rBMapper.selectRuleComListB301(item); // API682 기준 조회

		List<String> api682TypeList = new ArrayList<String>();
		List<String> api682MaterialList = new ArrayList<String>();
		List<String> api682PlanList = new ArrayList<String>();
		List<String> api682RSList = new ArrayList<String>();
		List<String> api682ArrangementList = new ArrayList<String>();
		List<String> api682Note = new ArrayList<String>();
		String api682EngineeredYN = "";
		boolean isEndUserHDO = "Y".equals(StringUtil.get(item.get("IS_HDO"))) ? true : false; // HDO End User 유무
		boolean isEndUserHDOCon1Note = false;

		if (!rComList.isEmpty()) {
			for (Map<String, Object> m : rComList) {
				String sAPI682Products = StringUtil.get(m.get("ATTR1")); // Api 682 Product 기준
				for (String sAPI682Product : sAPI682Products.split(",")) {
					for (String s : saProductGroup) {
						// product그룹에 해당하는 경우 Type 추가
						if (s.equals(sAPI682Product)) {

							for (String sType : StringUtil.get(m.get("ATTR2")).split(",")) { // type이 콤마로 구분되어 등록
								if (!api682TypeList.contains(sType) && !StringUtil.isBlank(sType))
									api682TypeList.add(sType); // Type Add
							}

							for (String sGasketMtrl : StringUtil.get(m.get("ATTR3")).split(",")) { // 콤마로 구분되어 등록
								if (!api682MaterialList.contains(sGasketMtrl) && !StringUtil.isBlank(sGasketMtrl))
									api682MaterialList.add(sGasketMtrl); // Gasket 재질 (3번째 재질)
							}

							for (String sPlan : StringUtil.get(m.get("ATTR4")).split(",")) { // 콤마로 구분되어 등록
								if (!api682MaterialList.contains(sPlan) && !StringUtil.isBlank(sPlan))
									api682MaterialList.add(sPlan); // Plan
							}

							if (!api682RSList.contains(StringUtil.get(m.get("ATTR5")))
									&& !StringUtil.isBlank(m.get("ATTR5")))
								api682RSList.add(String.valueOf(m.get("ATTR5"))); // Rotation/Stationary
							if (!api682ArrangementList.contains(StringUtil.get(m.get("ATTR6")))
									&& !StringUtil.isBlank(m.get("ATTR6")))
								api682ArrangementList.add(String.valueOf(m.get("ATTR6"))); // Arrangement
							if (!api682Note.contains(StringUtil.get(m.get("ATTR17")))
									&& !StringUtil.isBlank(m.get("ATTR17")))
								api682Note.add(String.valueOf(m.get("ATTR17"))); // note

							if (!"".equals(StringUtil.get(m.get("ATTR18")))) {
								api682EngineeredYN = StringUtil.get(m.get("ATTR18"));
							}
						}
					}
				}
			}
		}

//		 System.out.println("api682List : " + api682List.toString());

		// API682 기준 재질 추가 : Gasket
		for (String s : api682MaterialList) {
			iPIdx++;
			// setRuleRstList(material3RstList, _pID, sNo, "M3", iPIdx,
			// getMaterialDigit(_materialList, "3",s), s);
			setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, s, null);
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[B3] API682 기준 Gasket 재질 추가 : " + s);
		}
		// API682 기준 Plan 추가
		for (String s : api682PlanList) {
			iPIdx++;
			setResultList(planRstList, _pID, sNo, "P", iPIdx, s);
			setResultProcList(procRstList, _pID, sNo, iPIdx, "[B3] API682 기준 Plan 추가 : " + s);
		}

		// arrangement
		if (api682ArrangementList.size() > 0) {
			for (String sArrangement : api682ArrangementList) {
				if (item.get("ARRANGEMENT") == null
						|| (NumberUtil.toDouble(item.get("ARRANGEMENT"))) < NumberUtil.toDouble(sArrangement)) {
					item.put("ARRANGEMENT", sArrangement); // Arrangement 설정
					setResultProcList(procRstList, _pID, sNo, iPIdx, "[B3] API682 기준 Arrangement 설정 : " + sArrangement);
				}
			}
		}

//		 System.out.println("api682 Type Set ok : " + api682TypeList.toString());
//		 System.out.println("api682 Material Set ok : " + api682MaterialList.toString());
//		 System.out.println("api682 Plan Set ok : " + api682PlanList.toString());
//		 System.out.println("api682 RS Set ok : " + api682RSList.toString());

		// 중간진행사항
		setResultProcList(procRstList, _pID, sNo, 999, "[B3] API682에 따른 적용 Type : "
				+ (api682TypeList == null || api682TypeList.size() == 0 ? " - " : String.join(",", api682TypeList)));

		// API682 Note 추가
//		if (api682Note.size() > 0 ) {
//			iPIdx++;
//		}
		for (String s : api682Note) {
			setResultNoteList(noteRstList, _pID, sNo, 999, "[B3] API682 기준 : " + s);
		}

		// API 682 에서 Engineereed Seal로 정의된 경우 이후 로직 Skip
		if ("Y".equals(api682EngineeredYN))
			return -1;

		// C3조회 조건으로 추가
		item.put("API682_TYPE", api682TypeList);
		item.put("API682_RS", api682RSList);

		// C3에서 설정된 재질정보
//		 List<String> c3Mtrl1List = new ArrayList<String>();
//		 List<String> c3Mtrl2List = new ArrayList<String>();
//		 List<String> c3Mtrl3List = new ArrayList<String>();
//		 List<String> c3Mtrl4List = new ArrayList<String>();

		// ------------------------------------------------------
		// 운전조건에 따른 C3 기준 조회
		// HDO End User 일경우 예외처리
		List<Map<String, Object>> ruleC3List = null;

		System.out.println("==> isEndUserHDO : " + isEndUserHDO);

		if (isEndUserHDO) { // HDO End User 일 경우
			// Pump Type이 OH1일 경우 : Non API Pump

			String sPumpTypeG = mLService.getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")), _pumpTypeGroupInfo,
					null);
			if ("OH1".equals(sPumpTypeG)) {
				// double dShaftSize =
				// NumberUtil.round(NumberUtil.toDouble(item.get("SHAFT_SIZE")) * 0.0393701, 3)
				// ; // Shaft Size (In)
				double dShaftSize = NumberUtil.round(NumberUtil.toDouble(item.get("SHAFT_SIZE")) / 25.4, 3); // Shaft
																												// Size
																												// (In)
				if (dShaftSize == 1.125 || dShaftSize == 1.375 || dShaftSize == 1.875 || dShaftSize == 2.625) {
					item.put("END_USER_SEAL_TYPE", "ISC2-682");
				} else {
					item.put("END_USER_SEAL_TYPE", "QBQW");
				}

				ruleC3List = rBMapper.selectRuleC3(item);
				// C3조건을 만족하지 않는다면 조건을 무시하고 조회 후 Note 에 내용 추가
				if (ruleC3List.size() == 0) {
					ruleC3List = rBMapper.selectRuleC3_bySealType(item);
					// Note 추가 유무
					isEndUserHDOCon1Note = true;
				}

			} else {
				// Type C Bellows Seal 적용 시 : BXHHSW/BXHHSW 적용 조건 추가
				// Type C가 아니거나 Type C and Seal Type BXHHSW를 포함하는
				item.put("END_USER_HDO_TYPE_C_COND", "Y");
				// item.put("END_USER_SEAL_TYPE", "BXHHSW");
				ruleC3List = rBMapper.selectRuleC3(item);
			}

		} else { // 일반
			ruleC3List = rBMapper.selectRuleC3(item);

		}

		List<Map<String, Object>> ruleC9ChkList = new ArrayList<Map<String, Object>>(); // C9 체크리스트

		boolean bIsSulfur = isProduct("SULFUR", saProductGroup, saProduct);
		boolean bIsArrangement2 = (item.get("ARRANGEMENT") == null || NumberUtil.toInt(item.get("ARRANGEMENT")) == 2)
				? true
				: false;
		boolean bIsArrangement3 = (item.get("ARRANGEMENT") == null || NumberUtil.toInt(item.get("ARRANGEMENT")) == 3)
				? true
				: false;
		// double dShaftSize =
		// NumberUtil.round(NumberUtil.toDouble(item.get("SHAFT_SIZE")) * 0.0393701, 3)
		// ; // Shaft Size (In)

		if (ruleC3List.size() > 0) {

			for (Map<String, Object> m : ruleC3List) {

				boolean bC3Mtrl1 = false;
				boolean bC3Mtrl2 = false;
				boolean bC3Mtrl3 = false;
				boolean bC3Mtrl4 = false;

				iPIdx++;
				String sProcMsg = "";

				// Seal Type
				if (m.get("SEAL_TYPE") != null && !"".equals(String.valueOf(m.get("SEAL_TYPE")))) {

					// Config 정보가 없을 경우
					if (StringUtil.get(m.get("CONFIG")).equals("")) {
						Map<String, Object> sealAddInfo = new HashMap<String, Object>(); // Seal 선택 추가정보
						sealAddInfo.put("SEAL_STD_GB", m.get("SEAL_STD_GB")); // Seal 구분 (DURA/BWIP/ISC)
						sealAddInfo.put("SEAL_GB_TYPE", m.get("SEAL_GB_TYPE")); // Seal 구분 Type (A,B,C)
						sealAddInfo.put("RS_GB", m.get("RS_GB")); // rotating stationary 구분
						sealAddInfo.put("PV_CURVE", m.get("PV_CURVE")); // PV Curve 구분

						String sPumpTypeG = mLService.getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")),
								_pumpTypeGroupInfo, null);
						for (String s : String.valueOf(m.get("SEAL_TYPE")).split(",")) {
							if (isEndUserHDO && "OH1".equals(sPumpTypeG) && !"QBQW".equals(s)) { // HDO 1조건
								continue;
							} else if (isEndUserHDO && "C".equals(StringUtil.get(m.get("SEAL_GB_TYPE")))
									&& !"BXHHSW".equals(s)) { // HDO 2조건
								continue;
							}

							setResultList(sealRstList, _pID, sNo, "S", iPIdx, s, sealAddInfo);
						}

						// Config 정보가 있을 경우
					} else {

						String sPumpTypeG = mLService.getGroupingStr(StringUtil.get(item.get("PUMP_TYPE")),
								_pumpTypeGroupInfo, null);

						for (String sConfig : StringUtil.get(m.get("CONFIG")).split(",")) {
							for (String s : String.valueOf(m.get("SEAL_TYPE")).split(",")) {

								Map<String, Object> sealAddInfo = new HashMap<String, Object>(); // Seal 선택 추가정보
								sealAddInfo.put("SEAL_STD_GB", m.get("SEAL_STD_GB")); // Seal 구분 (DURA/BWIP/ISC)
								sealAddInfo.put("SEAL_GB_TYPE", m.get("SEAL_GB_TYPE")); // Seal 구분 Type (A,B,C)
								sealAddInfo.put("RS_GB", m.get("RS_GB")); // rotating stationary 구분
								sealAddInfo.put("PV_CURVE", m.get("PV_CURVE")); // PV Curve 구분
								sealAddInfo.put("CONFIG", sConfig); // Config

								if (sConfig.equals("1")) { // single

									if (isEndUserHDO && "OH1".equals(sPumpTypeG) && !"QBQW".equals(s)) { // HDO 1조건
										continue;
									} else if (isEndUserHDO && "C".equals(StringUtil.get(m.get("SEAL_GB_TYPE")))) { // HDO
																													// 2조건
																													// BXHHSW/BXHHSW
																													// 강제
																													// 적용이라
																													// Single
																													// Type
																													// 제거
										continue;
									}

									if (NumberUtil.toInt(item.get("ARRANGEMENT")) != 0
											&& NumberUtil.toInt(item.get("ARRANGEMENT")) != 1) { // 설정된 Arrangement
																									// Rule이 1이 아니면 Skip
										continue;
									}

									// [C4] QB 조건
									if ("QBW".equals(s) && NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) < 0.65) {
										continue;
									} else if ("QBQW".equals(s)
											&& NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 0.65) {
										continue;
									} else {
										// HDO && Single QBW로 설정될 경우
										if (isEndUserHDO && "QBW".equals(s)) {
											setResultList(sealRstList, _pID, sNo, "S", iPIdx, "QBQW", sealAddInfo);
											setResultProcList(procRstList, _pID, sNo, iPIdx,
													"[HDO] Single QBW -> QBQW");
											// 일반
										} else {
											setResultList(sealRstList, _pID, sNo, "S", iPIdx, s, sealAddInfo);
										}
									}

								} else { // dual

									if (isEndUserHDO && "OH1".equals(sPumpTypeG) && !"QBQW".equals(s)) { // HDO 1조건
										continue;
									} else if (isEndUserHDO && "C".equals(StringUtil.get(m.get("SEAL_GB_TYPE")))
											&& !"BXHHSW".equals(s)) { // HDO 2조건
										continue;
									}

									if (sConfig.equals("2") && bIsArrangement2) { // tandem - Unpressurized

										if (NumberUtil.toInt(item.get("ARRANGEMENT")) != 0
												&& NumberUtil.toInt(item.get("ARRANGEMENT")) != 2) { // 설정된 Arrangement
																										// Rule이 2가 아니면
																										// Skip
											continue;
										}

										// [C4] QB 조건
										if ("QBW".equals(s)
												&& NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) < 0.65) {
											continue;
										} else if ("QBQW".equals(s)
												&& NumberUtil.toDouble(item.get("SPEC_GRAVITY_NOR")) >= 0.65) {
											continue;
										} else {
											setResultList(sealRstList, _pID, sNo, "S", iPIdx, s + "/" + s, sealAddInfo);
										}

									} else if (sConfig.equals("3") && bIsArrangement3) { // double : Pressurized

										if (NumberUtil.toInt(item.get("ARRANGEMENT")) != 0
												&& NumberUtil.toInt(item.get("ARRANGEMENT")) != 3) { // 설정된 Arrangement
																										// Rule이 3이 아니면
																										// Skip
											continue;
										}

										if ("QBB".equals(s) || "QB2B".equals(s)) {
											setResultList(sealRstList, _pID, sNo, "S", iPIdx, s + "/QBW", sealAddInfo);

											// 일반
										} else {
											setResultList(sealRstList, _pID, sNo, "S", iPIdx, s + "/" + s, sealAddInfo);
										}

									}
								}
							}
						}
					}
					sProcMsg += " Seal : " + m.get("SEAL_TYPE");
				}

				// if(bIsHDOCtypeBellowsAdd) continue;

				// Material - digit1 - metal
				if (m.get("MTRL_CD_M1") != null && !"".equals(String.valueOf(m.get("MTRL_CD_M1")))) {

					// [C7-9] Sulfur 일 경우
					// 1) Sulfur 함유 & Product 비중 1.5 미만일 경우
					// * Type B or C (Bellows) : BX seal 재질코드 K---
					// BXHH seal 재질코드 G---(2 wt% 미만), H---(2 wt% 이상)
					if (bIsSulfur && StringUtil.get(m.get("SEAL_GB")).startsWith("Bellows")
							&& ("BX".equals(String.valueOf(m.get("SEAL_TYPE"))))
							|| ("BXHH".equals(String.valueOf(m.get("SEAL_TYPE"))))) { // Bellows -> Type B,C 일 경우
						if ("BX".equals(String.valueOf(m.get("SEAL_TYPE")))) {
							setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, "CX", null);
							bC3Mtrl1 = true;
							sProcMsg += " Metal : K";
							setResultProcList(procRstList, _pID, sNo, iPIdx, "[C7-9] Sulfur 적용 - Seal Type : BX  K---");
						} else if ("BXHH".equals(String.valueOf(m.get("SEAL_TYPE")))) {
							if (getProductCont(item, "SULFUR", "%") >= 2) {
								setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, "AT",
										null);
								sProcMsg += " Metal : H";
								setResultProcList(procRstList, _pID, sNo, iPIdx,
										"[C7-9] Sulfur 적용 - Seal Type : BXHH H---");
							} else {
								setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, "AM",
										null);
								sProcMsg += " Metal : G";
								setResultProcList(procRstList, _pID, sNo, iPIdx,
										"[C7-9] Sulfur 적용 - Seal Type : BXHH G---");
							}
							bC3Mtrl1 = true;

						}

					} else {
						for (String s : String.valueOf(m.get("MTRL_CD_M1")).split(",")) {
							setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "1", iPIdx, s, null);
							bC3Mtrl1 = true;
						}
						sProcMsg += " Metal : " + m.get("MTRL_CD_M1");
					}
				}

				// Material - digit2 - face1
				if (m.get("MTRL_CD_M2") != null && !"".equals(String.valueOf(m.get("MTRL_CD_M2")))) {
					for (String s : String.valueOf(m.get("MTRL_CD_M2")).split(",")) {
						setMaterialResultListPre(material2RstList, sealRstList, _pID, sNo, "2", iPIdx, s, null);
						bC3Mtrl2 = true;
					}
					sProcMsg += " R. Face : " + m.get("MTRL_CD_M2");
				}

				// Material - digit3 - gasket
				if (m.get("MTRL_CD_M3") != null && !"".equals(String.valueOf(m.get("MTRL_CD_M3")))) {

					String sGasket = "";

					// [C7-9] Sulfur 일 경우
					// 1) Sulfur 함유 & Product 비중 1.5 미만일 경우
					// * Type A (Pusher) : Viton은 100℃ 까지, 그 이상의 온도에는 FFKM
					boolean bIsSulfurMtrlApply = false;
					// [C6-8] Pusher 타입일 경우
					// ~ 100C : FKM , 100C ~ : FFKM
					if (bIsSulfur && StringUtil.get(m.get("SEAL_GB")).startsWith("Pusher")) { // pusher -> Type A
						if (NumberUtil.toDouble(item.get("TEMP_MAX")) < 100) {
							setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, "[FKM]",
									null);
							sGasket += "FKM,";
						} else {
							setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, "[FFKM]",
									null);
							sGasket += "FFKM,";
						}
						bIsSulfurMtrlApply = true;

						setResultProcList(procRstList, _pID, sNo, iPIdx, "[C7-9] Sulfur 적용 - Gasket : Pusher Type ");
					}

					for (String s : String.valueOf(m.get("MTRL_CD_M3")).split(",")) {
						if (bIsSulfurMtrlApply
								&& ((bIsSulfur && "[FKM]".equals(s)) || (bIsSulfur && "[FFKM]".equals(s)))) {
							continue;
						} else {
							setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPIdx, s, null);
							sGasket += s + ",";
						}
						bC3Mtrl3 = true;
					}

					sProcMsg += " Gasket : "
							+ (sGasket.length() > 0 ? sGasket.substring(0, sGasket.length() - 1) : "-");
				}

				// Material - digit4 - face2
				if (m.get("MTRL_CD_M4") != null && !"".equals(String.valueOf(m.get("MTRL_CD_M4")))) {
					for (String s : String.valueOf(m.get("MTRL_CD_M4")).split(",")) {
						setMaterialResultListPre(material4RstList, sealRstList, _pID, sNo, "4", iPIdx, s, null);
						bC3Mtrl4 = true;
					}
					sProcMsg += "S. Face : " + m.get("MTRL_CD_M4");
				}

				// C3에서 재질이 설정되지 않은 경우 C9 표준 재질을 체크한다.
				// 정보 저장 리스트 : ruleC9ChkList
				if (!bC3Mtrl1) {
					Map<String, Object> c9MtrlChk = new HashMap<String, Object>();
					c9MtrlChk.put("P_IDX", iPIdx);
					c9MtrlChk.put("R_NO", m.get("R_NO"));
					c9MtrlChk.put("MTRL_TYPE", "M1");
					ruleC9ChkList.add(c9MtrlChk);
				}
				if (!bC3Mtrl2) {
					Map<String, Object> c9MtrlChk = new HashMap<String, Object>();
					c9MtrlChk.put("P_IDX", iPIdx);
					c9MtrlChk.put("R_NO", m.get("R_NO"));
					c9MtrlChk.put("MTRL_TYPE", "M2");
					ruleC9ChkList.add(c9MtrlChk);
				}
				if (!bC3Mtrl3) {
					Map<String, Object> c9MtrlChk = new HashMap<String, Object>();
					c9MtrlChk.put("P_IDX", iPIdx);
					c9MtrlChk.put("R_NO", m.get("R_NO"));
					c9MtrlChk.put("MTRL_TYPE", "M3");
					ruleC9ChkList.add(c9MtrlChk);
				}
				if (!bC3Mtrl4) {
					Map<String, Object> c9MtrlChk = new HashMap<String, Object>();
					c9MtrlChk.put("P_IDX", iPIdx);
					c9MtrlChk.put("R_NO", m.get("R_NO"));
					c9MtrlChk.put("MTRL_TYPE", "M4");
					ruleC9ChkList.add(c9MtrlChk);
				}

				setResultProcList(procRstList, _pID, sNo, iPIdx, "Seal Operating Window 적용 - " + sProcMsg);

				if (isEndUserHDOCon1Note) {
					setResultNoteList(noteRstList, _pID, sNo, iPIdx,
							"[HDO] Non API Pump 적용 시 Seal Type별 Operating Window 조건 만족하지 않음");
				}
			}

		} // end if (ruleC3List.size() > 0) {

		// System.out.println(" =====> ruleC9ChkList : " + ruleC9ChkList);

		// --------------------------------------------------
		// C9 체크대상이 있을 경우 재질정보를 추가
		// C3에서 재질 각 파트를 확인하여 값이 없을 경우 C9에서 재질각 파트별체크
		// --------------------------------------------------
		for (Map<String, Object> c3_param : ruleC9ChkList) {

			List<Map<String, Object>> ruleC9List = rBMapper.selectRuleC9(c3_param); // C9 표준재질 조회
			if (!ruleC9List.isEmpty()) {
				Map<String, Object> c9_map = ruleC9List.get(0);
				List<String> c9MtrlInfo = new ArrayList<String>();

				// Config 정보에 따른 In-Out board 재질 추가
				if (String.valueOf(c9_map.get("CONFIG")).contains("2")
						|| String.valueOf(c9_map.get("CONFIG")).contains("3")) { // config 구성이 듀얼일경우
					for (String s : String
							.valueOf(c9_map.get("MTRL_CD_IN_" + String.valueOf(c3_param.get("MTRL_TYPE"))))
							.split(",")) {
						if (!c9MtrlInfo.contains(s))
							c9MtrlInfo.add(s);
					}
					for (String s : String
							.valueOf(c9_map.get("MTRL_CD_OUT_" + String.valueOf(c3_param.get("MTRL_TYPE"))))
							.split(",")) {
						if (!c9MtrlInfo.contains(s))
							c9MtrlInfo.add(s);
					}
				} else { // single일 경우
					for (String s : String
							.valueOf(c9_map.get("MTRL_CD_IN_" + String.valueOf(c3_param.get("MTRL_TYPE"))))
							.split(",")) {
						if (!c9MtrlInfo.contains(s))
							c9MtrlInfo.add(s);
					}
				}

				// System.out.println(" =====> c3_param : " + c3_param);
				// System.out.println(" =====> c9MtrlInfo : " + c9MtrlInfo);

				// iPIdx++;
				int iPidxC3 = NumberUtil.toInt(c3_param.get("P_IDX")); // C3에서 선택된 SealType의 Index => 서로 연결하기 위함.

				setResultProcList(procRstList, _pID, sNo, iPidxC3,
						"[C9] Seal Type 표준재질 Config : " + (("".equals(StringUtil.get(c9_map.get("CONFIG")))) ? "-"
								: StringUtil.get(c9_map.get("CONFIG"))));

				String sMtrlType = null;
				// C9 Seal Type별 표준재질에서 조회된 재질정보 등록
				for (String s : c9MtrlInfo) {
					HashMap<String, Object> m_t = new HashMap<String, Object>();

					m_t.put("C9_YN", "Y"); // C9 Rule 표준재질 유무
					m_t.put("SEAL_GB", c9_map.get("SEAL_GB")); // Seal 구분 : Bellows, Pusher - Mutiple Springs ...
					m_t.put("S_MTRL", c9_map.get("S_MTRL")); // Small Part Material Code
					m_t.put("S_MTRL_YN", c9_map.get("S_MTRL_YN")); // Small Part 접액여부 : Y/N
					m_t.put("GS_MTRL", c9_map.get("GS_MTRL")); // Gland & Sleeve Material For Bellows Meterial

					sMtrlType = String.valueOf(c3_param.get("MTRL_TYPE"));

					if ("M1".equals(sMtrlType)) {
						setMaterialResultListPre(material1RstList, sealRstList, _pID, sNo, "1", iPidxC3, s, m_t);
					} else if ("M2".equals(sMtrlType)) {
						setMaterialResultListPre(material2RstList, sealRstList, _pID, sNo, "2", iPidxC3, s, m_t);
					} else if ("M3".equals(sMtrlType)) {
						setMaterialResultListPre(material3RstList, sealRstList, _pID, sNo, "3", iPidxC3, s, m_t);
					} else if ("M4".equals(sMtrlType)) {
						setMaterialResultListPre(material4RstList, sealRstList, _pID, sNo, "4", iPidxC3, s, m_t);
					}

					if (!"".equals(s)) {
						setResultProcList(procRstList, _pID, sNo, iPidxC3,
								"[C9] Seal Type 표준재질 추가 - " + getMaterialPartName(sMtrlType.substring(1)) + " : "
										+ getMaterialDigit(sMtrlType.substring(1), s) + "(" + s + ")");
					}

				}
			} // end C9 재질이 있을 경우
		} // for (Map<String,Object> c3_param : ruleC9ChkList ) {

		// System.out.println(" =====> material1RstList : " + material1RstList);
		// System.out.println(" =====> material2RstList : " + material2RstList);
		// System.out.println(" =====> material3RstList : " + material3RstList);
		// System.out.println(" =====> material4RstList : " + material4RstList);

		// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
		// C1 Check Start
		// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
		for (int i = 0; i < 4; i++) {
			List<Map<String, Object>> mlist = null;
			String sMtrlType = null;
			if (i == 0) {
				mlist = material1RstList;
				sMtrlType = "M1";
			} else if (i == 1) {
				mlist = material2RstList;
				sMtrlType = "M2";
			} else if (i == 2) {
				mlist = material3RstList;
				sMtrlType = "M3";
			} else if (i == 3) {
				sMtrlType = "M2"; // 2와 같음
				mlist = material4RstList;
			}

			Map<String, Object> c1_map = new HashMap<String, Object>();
			Map<String, Object> mtrlAddInfo = new HashMap<String, Object>();
			for (Map<String, Object> m : mlist) {
				if (m.get("ADD_INFO") == null)
					continue;
				mtrlAddInfo = (HashMap<String, Object>) m.get("ADD_INFO");
				// if("Y".equals(String.valueOf(mtrlAddInfo.get("C9_YN")))){ // C9 표준재질일 경우

				for (String s : saProduct) {

					// Brine 일때 추가조건 적용
					s = getBrineProduct(item, s, saProductGroup, saProduct);

					// Product별 Grade를 조회
					c1_map.clear();
					c1_map.put("PRODUCT", s);
					c1_map.put("MTRL_TYPE", sMtrlType);
					c1_map.put("MTRL_CD", mtrlAddInfo.get("MTRL_CD"));
					c1_map.put("GB", getProductGb(item, s)); // Product 구분정보 (C1 조회용)
					c1_map.put("CONT", getProductCont(item, s, "%")); // Product 농도
					c1_map.put("TEMP_NOR", item.get("TEMP_NOR"));
					c1_map.put("TEMP_NAX", item.get("TEMP_MAX"));

					String sGrade = getGrade(c1_map); // C1 Grade를 조회
					mtrlAddInfo.put(s + "_GRADE", sGrade); // Grade Set

				}
				// }
			}
		}

		// 설정된 Grade 에 따라 처리
		Map<String, Object> mtrlAddInfo = new HashMap<String, Object>();

		Iterator iter = material1RstList.iterator();
		while (iter.hasNext()) {
			Map<String, Object> m = (HashMap<String, Object>) iter.next();
			if (m.get("ADD_INFO") == null)
				continue;
			mtrlAddInfo = (HashMap<String, Object>) m.get("ADD_INFO");
			// if(!"Y".equals(String.valueOf(mtrlAddInfo.get("C9_YN")))) continue; // C9
			// 표준재질일 경우

			boolean bUse = false; // Grade에 따른 사용 유무

			boolean bNonGrade = false; // Grade 조회결과 유무 :
			// C1 Material에 없는 재질일 경우 우선선정가이드에서 선택된 재질은 Skip
			// - C9 표준재질에서 나온 재질은 없을 경우 Note 표시 후 Skip

			for (String s : saProduct) {
				if ("A".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))
						|| "B".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
					bUse = true;
				}
				if ("NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
					bNonGrade = true;
				}
			}

			if (!bUse && !bNonGrade) {
				setResultProcList(procRstList, StringUtil.get(m.get("P_IDX")), StringUtil.get(m.get("P_NO")),
						NumberUtil.toInt(m.get("P_IDX")),
						"Material Guide Grade 체크로 삭제 - Metal : " + m.get("P_VAL") + ", " + mtrlAddInfo.get("MTRL_NM"));
				iter.remove();
			}

			// C1 재질표에 없는 재질이고 C9 표준재질로 설정된 경우
			if (bNonGrade && "Y".equals(StringUtil.get(mtrlAddInfo.get("C9_YN")))) {
				setResultNoteList(noteRstList, StringUtil.get(m.get("P_IDX")), StringUtil.get(m.get("P_NO")),
						NumberUtil.toInt(m.get("P_IDX")), "Material Guide 표준에 없는 재질 Metal : " + m.get("P_VAL") + "("
								+ mtrlAddInfo.get("MTRL_CD") + ") : " + mtrlAddInfo.get("MTRL_NM"));
			}
		}

		iter = material2RstList.iterator();
		while (iter.hasNext()) {
			Map<String, Object> m = (HashMap<String, Object>) iter.next();
			if (m.get("ADD_INFO") == null)
				continue;
			mtrlAddInfo = (HashMap<String, Object>) m.get("ADD_INFO");

			boolean bUse = false;
			boolean bNonGrade = false;
			for (String s : saProduct) {
				if ("A".equals(mtrlAddInfo.get(s + "_GRADE"))) {
					bUse = true;
				}
				if ("NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
					bNonGrade = true;
				}
			}
			if (!bUse && !bNonGrade) {
				setResultProcList(procRstList, StringUtil.get(m.get("P_IDX")), StringUtil.get(m.get("P_NO")),
						NumberUtil.toInt(m.get("P_IDX")), "Material Guide Grade 체크로 삭제 - R. Face : " + m.get("P_VAL")
								+ ", " + mtrlAddInfo.get("MTRL_NM"));
				iter.remove();
			}
			// C1 재질표에 없는 재질이고 C9 표준재질로 설정된 경우
			if (bNonGrade && "Y".equals(StringUtil.get(mtrlAddInfo.get("C9_YN")))) {
				setResultNoteList(noteRstList, StringUtil.get(m.get("P_IDX")), StringUtil.get(m.get("P_NO")),
						NumberUtil.toInt(m.get("P_IDX")), "Material Guide 표준에 없는 재질 R. Face : " + m.get("P_VAL") + "("
								+ mtrlAddInfo.get("MTRL_CD") + ") : " + mtrlAddInfo.get("MTRL_NM"));
			}
		}

		iter = material4RstList.iterator();
		while (iter.hasNext()) {
			Map<String, Object> m = (HashMap<String, Object>) iter.next();
			if (m.get("ADD_INFO") == null)
				continue;
			mtrlAddInfo = (HashMap<String, Object>) m.get("ADD_INFO");
			// if(!"Y".equals(String.valueOf(mtrlAddInfo.get("C9_YN")))) continue; // C9
			// 표준재질일 경우

			boolean bUse = false;
			boolean bNonGrade = false;
			for (String s : saProduct) {
				if ("A".equals(mtrlAddInfo.get(s + "_GRADE"))) {
					bUse = true;
				}
				if ("NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
					bNonGrade = true;
				}
			}
			if (!bUse && !bNonGrade) {
				setResultProcList(procRstList, StringUtil.get(m.get("P_IDX")), StringUtil.get(m.get("P_NO")),
						NumberUtil.toInt(m.get("P_IDX")),
						"[C1] Grade 체크로 삭제 - S. Face : " + m.get("P_VAL") + ", " + mtrlAddInfo.get("MTRL_NM"));
				iter.remove();
			}
			// C1 재질표에 없는 재질이고 C9 표준재질로 설정된 경우
			if (bNonGrade && "Y".equals(StringUtil.get(mtrlAddInfo.get("C9_YN")))) {
				setResultNoteList(noteRstList, StringUtil.get(m.get("P_IDX")), StringUtil.get(m.get("P_NO")),
						NumberUtil.toInt(m.get("P_IDX")), " [C1] 표준에 없는 재질 S. Face : " + m.get("P_VAL") + "("
								+ mtrlAddInfo.get("MTRL_CD") + ") : " + mtrlAddInfo.get("MTRL_NM"));
			}
		}

		// Gaskets
		// A,B 그레이드가 아닌 경우 제거
		iter = material3RstList.iterator();
		while (iter.hasNext()) {
			Map<String, Object> m = (HashMap<String, Object>) iter.next();
			if (m.get("ADD_INFO") == null)
				continue;
			mtrlAddInfo = (HashMap<String, Object>) m.get("ADD_INFO");
			// if(!"Y".equals(String.valueOf(mtrlAddInfo.get("C9_YN")))) continue; // C9
			// 표준재질일 경우

			boolean bUse = false;
			boolean bNonGrade = false;
			for (String s : saProduct) {
				if ("A".equals(mtrlAddInfo.get(s + "_GRADE")) || "B".equals(mtrlAddInfo.get(s + "_GRADE"))) {
					bUse = true;
				}
				if ("NONE".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
					bNonGrade = true;
				}
			}
			if (!bUse && !bNonGrade) {
				setResultProcList(procRstList, StringUtil.get(m.get("P_IDX")), StringUtil.get(m.get("P_NO")),
						NumberUtil.toInt(m.get("P_IDX")),
						"[C1] Grade 체크로 삭제 - Gaskets : " + m.get("P_VAL") + ", " + mtrlAddInfo.get("MTRL_NM"));
				iter.remove();
			}
			// C1 재질표에 없는 재질이고 C9 표준재질로 설정된 경우
			if (bNonGrade && "Y".equals(StringUtil.get(mtrlAddInfo.get("C9_YN")))) {
				setResultNoteList(noteRstList, StringUtil.get(m.get("P_IDX")), StringUtil.get(m.get("P_NO")),
						NumberUtil.toInt(m.get("P_IDX")), " [C1] 표준에 없는 재질 Gaskets : " + m.get("P_VAL") + "("
								+ mtrlAddInfo.get("MTRL_CD") + ") : " + mtrlAddInfo.get("MTRL_NM"));
			}
		}

		// 우선순위 후순위 체크?
		for (String s : saProduct) {
			iter = material3RstList.iterator();
			int istep = 0;
			String sSA = "";
			String sDA = "";
			boolean bY = false;

			while (iter.hasNext()) {
				Map<String, Object> m = (HashMap<String, Object>) iter.next();
				if (m.get("ADD_INFO") == null)
					continue;
				mtrlAddInfo = (HashMap<String, Object>) m.get("ADD_INFO");
				if (istep == 0) {
					if ("A".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
						bY = true;
						break;
					}
				} else {

					if ("A".equals(StringUtil.get(mtrlAddInfo.get(s + "_GRADE")))) {
						sDA += StringUtil.get(m.get("P_VAL")) + ",";
					} else {
						sSA += StringUtil.get(m.get("P_VAL")) + ",";
					}

				}
			}

			if (!bY && !"".equals(sSA)) {

				iter = material3RstList.iterator();
				while (iter.hasNext()) {
					iter.remove();
				}

				// Y 재질 추가
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("P_ID", _pID);
				m.put("P_NO", sNo);
				m.put("P_TYPE", "M3");
				m.put("P_IDX", 999);
				m.put("P_SEQ", getMaxSeq(material3RstList, 999));
				m.put("P_VAL", "Y"); // 재질정보 표시 Digit
				m.put("ADD_INFO", null); // 추가정보
				material3RstList.add(m);

				if ("".equals(sSA)) { // 모두 B

					// 재질 다 삭제하고 Y 추가
					// Static Gasket에는 B Grade 적용, Dynamic Gasket에는 별도의 재질로 수동 선정 필요
					String sNote = "Static Gasket에는 B Grade 적용, Dynamic Gasket에는 별도의 재질로 수동 선정 필요";
					setResultNoteList(noteRstList, _pID, sNo, 999, sNote);
				} else {

					// Static Gasket에는 B Grade인 표준재질 또는 우선 선정 재질 적용, Dynamic Gasket에는 A Grade인 후순위
					// 재질 적용
					String sNote = "Static Gasket에는 B Grade인 표준재질 또는 우선 선정 재질 적용, Dynamic Gasket에는 A Grade인 후순위 재질 적용";
					setResultNoteList(noteRstList, _pID, sNo, 999, sNote);
				}

				break;
			}
		}

		// Seal Type이 있는경우 재질이 하나도 선정되지 않을 경우 C1에서 A,B에 해당하는 다른 재질을 찾는다.
		for (int i = 0; i < 4; i++) {
			List<Map<String, Object>> mlist = null;
			String sMtrlType = null;
			if (i == 0) {
				mlist = material1RstList;
				sMtrlType = "1";
			} else if (i == 1) {
				mlist = material2RstList;
				sMtrlType = "2";
			} else if (i == 2) {
				mlist = material3RstList;
				sMtrlType = "3";
			} else if (i == 3) {
				sMtrlType = "2"; // 2와 같음
				mlist = material4RstList;
			}

			Map<String, Object> c1_map = new HashMap<String, Object>();
			int icnt = 0;
			for (String s : saProduct) {
				if (mlist.size() == 0) {
					c1_map.clear();
					c1_map.put("PRODUCT", s);
					c1_map.put("MTRL_TYPE", "M" + sMtrlType);
					c1_map.put("CONT", getProductCont(item, s, "%")); // s 농도
					c1_map.put("TEMP", item.get("TEMP_NOR"));

					List<Map<String, Object>> c1_list = rBMapper.selectRuleC1(c1_map);

					// A Grade 적용
					if (c1_list.size() > 0) {
						for (Map<String, Object> m : c1_list) {
							if ("A".equals(m.get("GRADE"))) {
								setMaterialResultListPre(mlist, sealRstList, _pID, sNo, sMtrlType, iPIdx,
										m.get("MTRL_CD"), null);

								setResultProcList(procRstList, StringUtil.get(m.get("P_IDX")),
										StringUtil.get(m.get("P_NO")), NumberUtil.toInt(m.get("P_IDX")),
										"[C1] Grade 체크로 삭제 - Gaskets : " + m.get("P_VAL") + ", "
												+ mtrlAddInfo.get("MTRL_NM"));

								icnt++;
							} else if ("B".equals(m.get("GRADE")) && "1".equals(sMtrlType)) { // Metal일 경우 B그레이드도 추가
								setMaterialResultListPre(mlist, sealRstList, _pID, sNo, sMtrlType, iPIdx,
										m.get("MTRL_CD"), null);
								icnt++;
							}
						}
					}

					// 상위그룹명으로 다시 찾는다.
					if (icnt == 0) {
						c1_map.put("PRODUCT", getProductGrp(s));
						c1_list = rBMapper.selectRuleC1(c1_map);

						// A Grade 적용
						if (c1_list.size() > 0) {
							for (Map<String, Object> m : c1_list) {
								if ("A".equals(m.get("GRADE"))) {
									setMaterialResultListPre(mlist, sealRstList, _pID, sNo, sMtrlType, iPIdx,
											m.get("MTRL_CD"), null);
								} else if ("B".equals(m.get("GRADE")) && "1".equals(sMtrlType)) { // Metal일 경우 B그레이드도 추가
									setMaterialResultListPre(mlist, sealRstList, _pID, sNo, sMtrlType, iPIdx,
											m.get("MTRL_CD"), null);
								}
							}
						}
					}

				}
			}
		}

		// ---------------------------------------------
		// Small Part Maerial check
		// ---------------------------------------------
		if (material1RstList.size() > 0) {
			Iterator mtrl1iter = material1RstList.iterator();
			while (mtrl1iter.hasNext()) {
				Map<String, Object> m = (Map<String, Object>) mtrl1iter.next();
				Map<String, Object> addInfo = (Map<String, Object>) m.get("ADD_INFO");
				for (String s : saProduct) {
					if ("".equals(StringUtil.get(addInfo.get("S_MTRL")))) {
						continue;
					} else if (!"".equals(addInfo.get("S_MTRL")) && "N".equals(addInfo.get("S_MTRL_YN"))) {

						for (String ss : StringUtil.get(addInfo.get("S_MTRL")).split(",")) {
							setResultNoteList(noteRstList, _pID, sNo, NumberUtil.toInt(m.get("P_IDX")),
									"Small Part Material : " + ss + " (" + getMaterialNm("1", ss) + ")"); // 노트 추가
						}
						// setResultNoteList(noteRstList, _pID, sNo, NumberUtil.toInt(m.get("P_IDX")),
						// "Small Part Material : " + addInfo.get("S_MTRL")); // 노트 추가
					} else if (!"".equals(addInfo.get("S_MTRL")) && "Y".equals(addInfo.get("S_MTRL_YN"))) {

						// Small Part 재질 체크
						for (String ss : StringUtil.get(addInfo.get("S_MTRL")).split(",")) {
							Map<String, Object> c1_map = new HashMap<String, Object>();
							c1_map.clear();
							c1_map.put("PRODUCT", s);
							c1_map.put("MTRL_TYPE", "M1");
							c1_map.put("MTRL_CD", ss);
							// c1_map.put("CONT",item.get(s+"_CONT")==null?"0":item.get(s+"_CONT")); // s 농도
							c1_map.put("CONT", getProductCont(item, s, "%")); // s 농도
							c1_map.put("TEMP", item.get("TEMP_NOR"));
							List<Map<String, Object>> c1_list = rBMapper.selectRuleC1(c1_map);
							Map<String, Object> c1_m = null;
							if (c1_list.size() > 0) {
								c1_m = c1_list.get(0);
								if ("A".equals(c1_m.get("GRADE"))) {
									setResultNoteList(noteRstList, _pID, sNo, NumberUtil.toInt(m.get("P_IDX")),
											"Small Part Material :  " + ss + " (" + getMaterialNm("1", ss) + ")"); // 노트
								} else {
									// 상위그룹으로 다시 검색
									c1_map.put("PRODUCT", getProductGrp(s));
									c1_list = rBMapper.selectRuleC1(c1_map);
									if (c1_list.size() > 0) {
										c1_m = c1_list.get(0);
										if ("A".equals(c1_m.get("GRADE"))) {
											setResultNoteList(noteRstList, _pID, sNo, NumberUtil.toInt(m.get("P_IDX")),
													"Small Part Material :  " + ss + " (" + getMaterialNm("1", ss)
															+ ")"); // 노트
										} else {
											setResultProcList(procRstList, _pID, sNo, NumberUtil.toInt(m.get("P_IDX")),
													"[C1] Small Part 접액여부 Y & Grade ≠ A 로 삭제 - Product :" + s
															+ ", Metal : " + m.get("P_VAL") + "("
															+ addInfo.get("MTRL_CD") + ")");
											mtrl1iter.remove(); // 삭제
										}
									}
								}
							}
						}
					}
				}
			}
		}

		// Gland/Sleeve Meterial
		if (material1RstList.size() > 0) {
			for (Map<String, Object> m : material1RstList) {
				Map<String, Object> addInfo = (Map<String, Object>) m.get("ADD_INFO");
				for (String s : saProduct) {
					if ("BELLOWS".equals(StringUtil.get(m.get("SEAL_GB")).toUpperCase())) { // Bellows일경우
						// String sGsMtrl = StringUtils.get(addInfo.get("GS_MTRL"));

						// Small Part 재질 체크
						for (String ss : StringUtil.get(addInfo.get("GS_MTRL")).split(",")) {
							Map<String, Object> c1_map = new HashMap<String, Object>();
							c1_map.clear();
							c1_map.put("PRODUCT", s);
							c1_map.put("MTRL_TYPE", "M1");
							c1_map.put("MTRL_CD", ss);
							// c1_map.put("CONT",item.get(s+"_CONT")==null?"0":item.get(s+"_CONT")); // s 농도
							c1_map.put("CONT", getProductCont(item, s, "%")); // s 농도
							c1_map.put("TEMP", item.get("TEMP_NOR"));
							List<Map<String, Object>> c1_list = rBMapper.selectRuleC1(c1_map);
							Map<String, Object> c1_m = null;
							if (c1_list.size() > 0) {
								c1_m = c1_list.get(0);
								if ("A".equals(c1_m.get("GRADE")) || "B".equals(c1_m.get("GRADE"))) {
									setResultNoteList(noteRstList, _pID, sNo, NumberUtil.toInt(m.get("P_IDX")),
											"Gland/Sleeve : " + ss); // 노트
								} else {
									// 상위그룹으로 다시 검색
									c1_map.put("PRODUCT", getProductGrp(s));
									c1_list = rBMapper.selectRuleC1(c1_map);
									if (c1_list.size() > 0) {
										c1_m = c1_list.get(0);
										if ("A".equals(c1_m.get("GRADE")) || "B".equals(c1_m.get("GRADE"))) {
											setResultNoteList(noteRstList, _pID, sNo, NumberUtil.toInt(m.get("P_IDX")),
													"Gland/Sleeve : " + ss); // 노트
										} else {
											setResultProcList(procRstList, _pID, sNo, NumberUtil.toInt(m.get("P_IDX")),
													"Gland/Sleeve :  표준재질이 부적합하여 대체 재질 선정함");
										}
									}
								}
							}
						}

					}
				}

			}

		}

		// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
		// C1 Check end
		// ----------------------------------------------------------------------------------------------------------------------------------------------------------------

		// 그래도 재질이 없다면
		if (material1RstList.size() == 0) {
			String sNote = "Metal : Material Guide(FTA101)에서는 적합한 재질이 없음.";
			setResultNoteList(noteRstList, _pID, sNo, 999, sNote);
		}

		if (material2RstList.size() == 0) {
			String sNote = "Rotating Face : Material Guide(FTA101)에서는 적합한 재질이 없음.";
			setResultNoteList(noteRstList, _pID, sNo, 999, sNote);
		}

		if (material3RstList.size() == 0) {
			String sNote = "Gaskets : Material Guide(FTA101)에서는 적합한 재질이 없음.";
			setResultNoteList(noteRstList, _pID, sNo, 999, sNote);
		}

		if (material4RstList.size() == 0) {
			String sNote = "Staionary Face : Material Guide(FTA101)에서는 적합한 재질이 없음.";
			setResultNoteList(noteRstList, _pID, sNo, 999, sNote);
		}

		return iPIdx;
	}

	/**
	 * 후순위 Rule 적용
	 * 
	 * @param _pID
	 * @param sNo
	 * @param iPIdx
	 * @param item
	 * @param fp
	 * @return
	 * @throws Exception
	 */
	private int step3_rule_after(String _pID, String sNo, int iPIdx, Map<String, Object> item, Map<String, Object> fp)
			throws Exception {

		// HDO Rule 적용
		// 3. Single QBW로 선정이 되는 경우 QBW 대신 QBQW 적용
		return iPIdx;
	}

	/**
	 * 제약사항을 체크
	 * 
	 * @param item
	 * @param fp
	 * @throws Exception
	 */
	private void step5_restrictions_chk(Map<String, Object> item, Map<String, Object> fp) throws Exception {

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sealRstList = (List<Map<String, Object>>) fp.get("sealRstList");
		// @SuppressWarnings("unchecked")
		// List<Map<String,Object>> materialRstList =
		// (List<Map<String,Object>>)fp.get("materialRstList");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> material1RstList = (List<Map<String, Object>>) fp.get("material1RstList");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> material2RstList = (List<Map<String, Object>>) fp.get("material2RstList");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> material3RstList = (List<Map<String, Object>>) fp.get("material3RstList");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> material4RstList = (List<Map<String, Object>>) fp.get("material4RstList");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> procRstList = (List<Map<String, Object>>) fp.get("procRstList");

		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		Map<String, Object> ptm = new HashMap<String, Object>(); // 조회 임시 Param Map

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		System.out.println("step5_restrictions_chk Start ");

		// B1-11 체크
		// H2S 포함일경우
		if (isProduct("H2S", saProductGroup, saProduct)) {

			// double dH2SCont = NumberUtil.toDouble(item.get("H2S_CONT"));
			// dH2SCont = dH2SCont * 10000;//ppm으로 변환
			double dH2SCont = getProductCont(item, "H2S", "PPM");

			ptm.put("MCD", "B11101");
			ptm.put("H2S_CONT", dH2SCont);
			List<Map<String, Object>> rComB11101List = rBMapper.selectRuleComListB11101(ptm);

			if (!rComB11101List.isEmpty()) {

				@SuppressWarnings("rawtypes")
				Iterator iter = material3RstList.iterator();
				while (iter.hasNext()) {
					Map<String, Object> m3 = (HashMap<String, Object>) iter.next();
					Map<String, Object> addInfo = m3.get("ADD_INFO") == null ? new HashMap<String, Object>()
							: (HashMap<String, Object>) m3.get("ADD_INFO");
					// for(Map<String,Object> m3 : material3RstList ) {
					boolean bchk = false;

					for (Map<String, Object> m : rComB11101List) {

						String mtrlcds = StringUtil.get(m.get("ATTR1"));
						for (String s : mtrlcds.split(",")) {
							for (String s2 : getMaterialDetail(s).split(",")) {

								if (StringUtil.get(addInfo.get("MTRL_CD")).equals(s2)) {
									if ("Y".equals(StringUtil.get(m.get("ATTR2")))) {
										bchk = true;
										break;
									}
								}
							}
						}

						if ("[CHK]".equals(StringUtil.get(m.get("ATTR2")))) { // HYDROCARBON 체크
							if (isProduct("HYDROCARBON", saProductGroup, saProduct)) { // 제품에 HYDROCARBON 이 있을 경우
								bchk = false;
							}
						}

						if ("[G-CHK]".equals(StringUtil.get(m.get("ATTR2")))) { // H2S 농도 그래프에 따른

							Map<String, Object> h2sMapChkParam = new HashMap<String, Object>();
							h2sMapChkParam.put("GRAPH_NO", "A2");
							h2sMapChkParam.put("CURV_NO", "1");
							h2sMapChkParam.put("VAL", item.get("TEMP_MAX")); // 온도
							List h2sMapChkList = rBMapper.selectRuleGraphFunc(h2sMapChkParam);

							String sFunc = "";
							double dCont = 0;
							if (h2sMapChkList.size() > 0) {
								sFunc = StringUtil.get(((Map<String, Object>) h2sMapChkList.get(0)).get("FUNC"));

								if (!"".equals(sFunc)) {
									sFunc = sFunc.replace("x", "" + item.get("TEMP_MAX")); //

									dCont = NumberUtil.toDouble(engine.eval(sFunc));

									if (dCont < dH2SCont) {
										setResultProcList(procRstList, StringUtil.get(m3.get("P_ID")),
												StringUtil.get(m3.get("P_NO")), NumberUtil.toInt(m3.get("P_IDX")),
												"[A2] H2S 농도 제한 초과 :" + dH2SCont);
										bchk = false;
									} else {
										bchk = true;
									}
								}
							}

						}
					}

					if (!bchk) { // 조건을 만족하지 않으면 삭제
						// material3RstList.remove(m3);
						iter.remove();
						setResultProcList(procRstList, StringUtil.get(m3.get("P_ID")), StringUtil.get(m3.get("P_NO")),
								NumberUtil.toInt(m3.get("P_IDX")),
								"[B1-11] Fluid with H2S 조건으로 삭제 - Gaskets : " + m3.get("P_VAL"));
					}
				}

				// face 재질이 antimony carbon 사용 제한
				iter = material2RstList.iterator();
				while (iter.hasNext()) {
					Map<String, Object> m2 = (HashMap<String, Object>) iter.next();
					if (StringUtil.get(m2.get("MTRL_CD")).equals("RY")) {
						iter.remove();
						setResultProcList(procRstList, StringUtil.get(m2.get("P_ID")), StringUtil.get(m2.get("P_NO")),
								NumberUtil.toInt(m2.get("P_IDX")),
								"[B1-11] Fluid with H2S 조건으로 삭제 :" + m2.get("P_VAL"));
					}
				}

				iter = material4RstList.iterator();
				while (iter.hasNext()) {
					Map<String, Object> m4 = (HashMap<String, Object>) iter.next();
					if (StringUtil.get(m4.get("MTRL_CD")).equals("RY")
							|| StringUtil.get(m4.get("MTRL_CD")).equals("AP")) {
						iter.remove();
						setResultProcList(procRstList, StringUtil.get(m4.get("P_ID")), StringUtil.get(m4.get("P_NO")),
								NumberUtil.toInt(m4.get("P_IDX")),
								"[B1-11] Fluid with H2S 조건으로 삭제 :" + m4.get("P_VAL"));
					}
				}
			}

		} // if(isProduct("H2S",saProductGroup)) {

		// C7-6 Amine 고려사항
		if (isProduct("AMINE", saProductGroup, saProduct)) {
			// fase 재질이 antimony carbon 사용 제한
			Iterator iter = material2RstList.iterator();
			while (iter.hasNext()) {
				Map<String, Object> m2 = (HashMap<String, Object>) iter.next();
				if (StringUtil.get(m2.get("MTRL_CD")).equals("RY")) {
					iter.remove();
					setResultProcList(procRstList, StringUtil.get(m2.get("P_ID")), StringUtil.get(m2.get("P_NO")),
							NumberUtil.toInt(m2.get("P_IDX")), "[C7-6] Amine 조건으로 삭제 :" + m2.get("P_VAL"));
				}
			}

			iter = material4RstList.iterator();
			while (iter.hasNext()) {
				Map<String, Object> m4 = (HashMap<String, Object>) iter.next();
				if (StringUtil.get(m4.get("MTRL_CD")).equals("RY") || StringUtil.get(m4.get("MTRL_CD")).equals("AP")) {
					iter.remove();
					setResultProcList(procRstList, StringUtil.get(m4.get("P_ID")), StringUtil.get(m4.get("P_NO")),
							NumberUtil.toInt(m4.get("P_IDX")), "[C7-6] Amine 조건으로 삭제 :" + m4.get("P_VAL"));
				}
			}

		} // if(isProduct("AMINE",saProductGroup,saProduct)) {

		List<String> chkGraphRemoveList = new ArrayList<String>(); // Seal 추천정보 전체 삭제대상 리스트 ( Seal, Material, Plan )

		// ------------------------------------------------------------------------
		// 체크 항목 처리
		// ------------------------------------------------------------------------
		Map<String, Object> addInfo = null;

		// ------------------------------------------------------------------------
		// DURA & BWIP Graph 체크
		// ------------------------------------------------------------------------
		System.out.println("DURA Graph check Start ");

		Map<String, Object> addInfo2 = null;
		Map<String, Object> addInfo4 = null;
		String sSealType = "";
		int iIdx = 0;
		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> g_param = new HashMap<String, Object>();
		Map<String, Object> gf_param = new HashMap<String, Object>();
		double dPress = 0.d;

		double dSealSize = NumberUtil.toDouble(item.get("SEAL_SIZE")); // In
		// double dSealSize = 3;
		// double dShaftSize = NumberUtil.toDouble(item.get("SHAFT_SIZE")) * 0.0393701 ;
		double dShaftSize = NumberUtil.toDouble(item.get("SHAFT_SIZE")) / 25.4;
		// Seal Chamber Press (psig)
		double dSealChamPres_psig = NumberUtil.toDouble(item.get("SEAL_CHAM_MAX")) / 0.069;

		// Choride 농도
		double dChlorideCont = getProductCont(item, "CHLORIDE", "PPM");

		// PH
		double dPH = NumberUtil.toDouble(item.get("PH")); // PH

		// BW Graph 체크 유무
		boolean isBW_graph_chk = false;
		if (isProduct("CHLORIDE", saProductGroup, saProduct)) {
			if (NumberUtil.toInt(item.get("PH")) > 0) {
				isBW_graph_chk = true;
			}
		}

		System.out.println("[sealRstList] : " + sealRstList);

		for (Map<String, Object> m : sealRstList) {
			addInfo = m.get("ADD_INFO") == null ? new HashMap<String, Object>()
					: (Map<String, Object>) m.get("ADD_INFO"); // 추가정보

			sSealType = (String) m.get("P_VAL"); // Seal type
			iIdx = NumberUtil.toInt(m.get("P_IDX")); // Index

			if ("DURA".equals(StringUtil.get(addInfo.get("SEAL_STD_GB")))) { // Dura Seal 일 경우

				String sM2 = "";
				String sM4 = "";

//				System.out.println("[SealType] : " +sSealType);

				// Bellows Metal 조건 적용 Graph에 대한 변수처리
				List<String> bellowsMtrlList = new ArrayList<String>();
				if ("X-100".equals(sSealType) || "X-101".equals(sSealType) || "X-200".equals(sSealType)
						|| "X-201".equals(sSealType) || "CBR".equals(sSealType) || "CBS".equals(sSealType)) {
					for (String smtrl : StringUtil.get(addInfo.get("S_MTRL")).split(",")) {
						bellowsMtrlList.add(smtrl);
					}
				} else {
					bellowsMtrlList.add("");
				}

				// Product 조건 적용 Graph에 대한 변수처리
				List<String> productChkList = new ArrayList<String>();
				if ("X-100".equals(sSealType) || "X-101".equals(sSealType) || "X-200".equals(sSealType)
						|| "X-201".equals(sSealType) || "CBR".equals(sSealType) || "CBS".equals(sSealType)
						|| "PTO".equals(sSealType) || "RO".equals(sSealType) || "P-200".equals(sSealType)) {
					for (String product : saProductGroup) {
						productChkList.add(product);
					}
				} else {
					productChkList.add("");
				}

				List<Map<String, String>> chkList = new ArrayList<Map<String, String>>();

				for (Map<String, Object> m2 : material2RstList) { // Face2 재질
					sM2 = "";
					sM4 = "";
					if (iIdx == NumberUtil.toInt(m2.get("P_IDX"))) {
						addInfo2 = (Map<String, Object>) m2.get("ADD_INFO"); // 추가정보
						sM2 = StringUtil.get(addInfo2.get("MTRL_CD"));
					}

					for (Map<String, Object> m4 : material4RstList) { // Face4 재질
						if (iIdx == NumberUtil.toInt(m4.get("P_IDX"))) {
							addInfo4 = (Map<String, Object>) m4.get("ADD_INFO"); // 추가정보
							sM4 = StringUtil.get(addInfo4.get("MTRL_CD"));
						}

						// Graph 대상 체크
						if (!"".equals(sM2) && !"".equals(sM4)) {
							g_param.clear();
							g_param.put("SEAL_TYPE", sSealType);
							g_param.put("MTRL_CD_M2", sM2);
							g_param.put("MTRL_CD_M4", sM4);
							g_param.put("SPEED", item.get("RPM_MAX")); // 속도는 min, nor, max중 어떤걸 쓸지 확인 필요

							// ------------------------------------//
							// Seal Type별 추가조건 설정 //
							// ------------------------------------//

							// Arrangement
							if ("P-200".equals(sSealType)) {
								g_param.put("ARRANGEMENT", StringUtil.get(item.get("ARRANGEMENT"))); // Arrangement
							}

							// Seal Size
							if ("P-50".equals(sSealType)) {
								g_param.put("SEAL_SIZE", NumberUtil.toDouble(item.get("SEAL_SIZE"))); // Seal Size
							}

							// Temp
							if ("PBR".equals(sSealType) && "PBS".equals(sSealType)) {
								g_param.put("TEMP", NumberUtil.toDouble(item.get("TEMP_MAX"))); // 온도
							}

//							System.out.println("bellowsMtrlList : " + bellowsMtrlList.toString() +":"+ bellowsMtrlList.size());
//							System.out.println("productChkItem : " + productChkList.toString() +":"+ productChkList.size());

							// Bellows Metal 목록과 Product 그룹 목록이 복수개로 발생할 수 있어 Looping 처리.
							for (String bellowsMtrlItem : bellowsMtrlList) {
								for (String productChkItem : productChkList) {

									g_param.put("BELLOWS_MTRL", bellowsMtrlItem);
									g_param.put("PRODUCT_GRP", productChkItem);

									// graph 조회
									List<Map<String, Object>> graphList = rBMapper.selectRuleGraph(g_param);

									if (graphList.size() > 0) {
										// graph Func 조회
										for (Map<String, Object> g_data : graphList) {
											gf_param.clear();
											gf_param.put("GRAPH_NO", g_data.get("GRAPH_NO"));
											gf_param.put("CURV_NO", g_data.get("CURV_NO"));

											// Size 구분체크
											if ("SEAL".equals(g_data.get("SIZE_GB"))) {
												gf_param.put("VAL", dSealSize); // Seal SIze
											} else if ("SHAFT".equals(g_data.get("SIZE_GB"))) {
												// gf_param.put("SIZE", item.get("SHAFT_SIZE")); // Seal SIze
												gf_param.put("VAL", dShaftSize); // Seal SIze (IN로 변경한다.) 1 mm -
																					// 0.0393701 in
											}

											List<Map<String, Object>> grapFunchList = rBMapper
													.selectRuleGraphFunc(gf_param);
											String sFunc = "";
											if (grapFunchList.size() > 0) {
												sFunc = StringUtil
														.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
											}

											// 압력 체크 (씰챔버압력과 비교)
											if (!"".equals(sFunc)) {

												if ("SEAL".equals(g_data.get("SIZE_GB"))) {
													sFunc = sFunc.replace("x", "" + dSealSize); //
												} else if ("SHAFT".equals(g_data.get("SIZE_GB"))) {
													sFunc = sFunc.replace("x", "" + dShaftSize); //
												}
												System.out.println("sFunc :  " + sFunc);

												dPress = NumberUtil.toDouble(engine.eval(sFunc));
												// dPress = dPress * 0.069; // to BARG

												// 압력 제한범위를 초과하면 (씰챔버압력)
												if (dPress < dSealChamPres_psig) {
													Map<String, String> chk = new HashMap<String, String>();

													chk.put("P_IDX", String.valueOf(iIdx));
													chk.put("M2_SEQ", String.valueOf(m2.get("P_SEQ")));
													chk.put("M4_SEQ", String.valueOf(m4.get("P_SEQ")));
													chk.put("GRAPH_NO", String.valueOf(g_data.get("GRAPH_NO")));
													chkList.add(chk);

												}
											}
										}
									} // end graph check

								}
							}

						}
					}
				}

				// ----------------------------------
				// remove 대상 처리
				// ----------------------------------
//				System.out.println("chkList : " + chkList);

				@SuppressWarnings("rawtypes")
				Iterator iterM2 = material2RstList.iterator();
				while (iterM2.hasNext()) {
					Map<String, Object> m2 = (Map<String, Object>) iterM2.next();
					for (Map<String, String> chk : chkList) {

						if (chk.get("P_IDX").equals(String.valueOf(m2.get("P_IDX")))
								&& chk.get("M2_SEQ").equals(String.valueOf(m2.get("P_SEQ")))) {
							setResultProcList(procRstList, StringUtil.get(m2.get("P_ID")),
									StringUtil.get(m2.get("P_NO")), NumberUtil.toInt(m2.get("P_IDX")),
									"[A1] Graph 체크 조건으로 삭제 - Rotating Face : " + chk.get("GRAPH_NO") + " , "
											+ m2.get("P_VAL"));
							iterM2.remove();
							break;
						}
					}
				}

				@SuppressWarnings("rawtypes")
				Iterator iterM4 = material4RstList.iterator();
				while (iterM4.hasNext()) {
					Map<String, Object> m4 = (Map<String, Object>) iterM4.next();
					for (Map<String, String> chk : chkList) {
						if (chk.get("P_IDX").equals(String.valueOf(m4.get("P_IDX")))
								&& chk.get("M4_SEQ").equals(String.valueOf(m4.get("P_SEQ")))) {
							setResultProcList(procRstList, StringUtil.get(m4.get("P_ID")),
									StringUtil.get(m4.get("P_NO")), NumberUtil.toInt(m4.get("P_IDX")),
									"[A1] Graph 체크 조건으로 삭제 - Stationary Face : " + chk.get("GRAPH_NO") + " , "
											+ m4.get("P_VAL"));
							iterM4.remove();
							break;
						}
					}
				}

				// BW Seal 일 경우
			} else if (isBW_graph_chk && "BWIP".equals(StringUtil.get(addInfo.get("SEAL_STD_GB")))) {
				// A8 그래프 범위에 해당하는지 체크
				boolean bRemove = false;
				// ph
				// Chloride : % -> ppm

				// 대상 Metal 재질 : DB, ZB, M002
				List<Map<String, Object>> graphFunclist = null;
				String graphFunc1 = "", graphFunc2 = "", graphFunc3 = "";
				double dGraphFunc1Val = 0, dGraphFunc2Val = 0, dGraphFunc3Val = 0;

				Iterator iterM1 = material1RstList.iterator();
				while (iterM1.hasNext()) {
					Map<String, Object> dataM1 = (HashMap<String, Object>) iterM1.next();

					if (!"DB".equals(StringUtil.get(dataM1.get("P_VAL")))
							&& !"ZB".equals(StringUtil.get(dataM1.get("P_VAL")))
							&& !"M002".equals(StringUtil.get(dataM1.get("P_VAL")))) {
						continue;
					}

					if ("DB".equals(StringUtil.get(dataM1.get("P_VAL")))
							|| "ZB".equals(StringUtil.get(dataM1.get("P_VAL")))) {
						gf_param.clear();
						gf_param.put("GRAPH_NO", "A8");
						gf_param.put("CURV_NO", "1");
						gf_param.put("VAL", dChlorideCont); // Chloride 농도
						graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);
						if (graphFunclist.size() > 0) {
							graphFunc1 = StringUtil.get(((Map<String, Object>) graphFunclist.get(0)).get("FUNC"));
						}
					}

					if ("ZB".equals(StringUtil.get(dataM1.get("P_VAL")))
							|| "M002".equals(StringUtil.get(dataM1.get("P_VAL")))) {
						gf_param.clear();
						gf_param.put("GRAPH_NO", "A8");
						gf_param.put("CURV_NO", "2");
						gf_param.put("VAL", dChlorideCont); // Chloride 농도
						graphFunclist = rBMapper.selectRuleGraphFunc(gf_param);
						if (graphFunclist.size() > 0) {
							graphFunc2 = StringUtil.get(((Map<String, Object>) graphFunclist.get(0)).get("FUNC"));
						}
					}
					if ("M002".equals(StringUtil.get(dataM1.get("P_VAL")))) {
						gf_param.clear();
						gf_param.put("GRAPH_NO", "A8");
						gf_param.put("CURV_NO", "3");
						gf_param.put("VAL", dChlorideCont); // Chloride 농도
						if (graphFunclist.size() > 0) {
							graphFunc3 = StringUtil.get(((Map<String, Object>) graphFunclist.get(0)).get("FUNC"));
						}
					}

					if ("DB".equals(StringUtil.get(dataM1.get("P_VAL")))) { // 1번그래프보다 높아야 함.
						graphFunc1 = graphFunc1.replace("x", "" + dChlorideCont);
						dGraphFunc1Val = NumberUtil.toDouble(engine.eval(graphFunc1));

						if (dGraphFunc1Val > dPH) {
							bRemove = true;
						}
					} else if ("ZB".equals(StringUtil.get(dataM1.get("P_VAL")))) { // 2번그래프보다 높고 1번그래프보다 낮아야 함.
						graphFunc1 = graphFunc1.replace("x", "" + dChlorideCont);
						dGraphFunc1Val = NumberUtil.toDouble(engine.eval(graphFunc1));

						graphFunc2 = graphFunc2.replace("x", "" + dChlorideCont);
						dGraphFunc1Val = NumberUtil.toDouble(engine.eval(graphFunc1));

						if (dGraphFunc1Val < dPH || dGraphFunc2Val > dPH) {
							bRemove = true;
						}
					} else if ("M002".equals(StringUtil.get(dataM1.get("P_VAL")))) { // 2번그래프보다 낮고 3번그래프보다 높아야함.
						graphFunc2 = graphFunc2.replace("x", "" + dChlorideCont);
						dGraphFunc2Val = NumberUtil.toDouble(engine.eval(graphFunc2));

						graphFunc3 = graphFunc3.replace("x", "" + dChlorideCont);
						dGraphFunc3Val = NumberUtil.toDouble(engine.eval(graphFunc3));

						if (dGraphFunc2Val < dPH || dGraphFunc3Val > dPH) {
							bRemove = true;
						}
					}

					if (bRemove) {
						setResultProcList(procRstList, StringUtil.get(dataM1.get("P_ID")),
								StringUtil.get(dataM1.get("P_NO")), NumberUtil.toInt(dataM1.get("P_IDX")),
								"[A8] Graph 체크 조건으로 삭제 - Metal : A8 , " + dataM1.get("P_VAL"));
						iterM1.remove();
					}
				}
			}

			// [A2] 체크
			if ("QB".equals(sSealType) || "QBQ".equals(sSealType) || "QBS".equals(sSealType)
					|| "QBQLZ".equals(sSealType)) {

				System.out.println("A2 Start");
				System.out.println("sSealType : " + sSealType);
				System.out.println("dSealChamPres_psig : " + dSealChamPres_psig);
				// Graph 조회
				gf_param.clear();
				gf_param.put("GRAPH_NO", "A2");
				if ("QBQLZ".equals(sSealType)) {
					// Graph 조회
					gf_param.put("VAL", dSealSize); // seal size
					gf_param.put("CURV_NO", "2");
				} else {
					gf_param.put("VAL", dSealSize); // seal size
					gf_param.put("CURV_NO", "1");
				}
				List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);

				String sFunc = "";
				if (grapFunchList.size() > 0) {
					sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
				}
				System.out.println("sFunc : " + sFunc);
				sFunc = sFunc.replace("x", "" + dSealSize); //
				dPress = NumberUtil.toDouble(engine.eval(sFunc));

				// 압력 제한범위를 초과하면 (씰챔버압력)
				if (dPress < dSealChamPres_psig) {
					setResultProcList(procRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
							NumberUtil.toInt(m.get("P_IDX")),
							"[A2] Graph 체크 조건으로 삭제 - 제한압력(psig) : " + engine.eval(sFunc));
					chkGraphRemoveList.add("" + m.get("P_IDX"));
				}

				System.out.println("A2 End");
			}
		}

		System.out.println("A4 Start");

		// MD-200 Curve 체크
		// Differential Pressure(Paig) = Seal Cham Press(Psig) + 25 (Psig)
		Iterator iterS = sealRstList.iterator();
		while (iterS.hasNext()) {
			Map<String, Object> m = (HashMap<String, Object>) iterS.next();
			if ("MD-200".equals(StringUtil.get(m.get("P_VAL")))) {
				List<Map<String, Object>> list = rBMapper.selectRuleComListA401(item);
				if (list.size() > 0) {
					Map<String, Object> data = (Map<String, Object>) list.get(0);

					// Graph 조회
					gf_param.clear();
					gf_param.put("VAL", dSealSize); // Seal SIze
					gf_param.put("GRAPH_NO", data.get("ATTR1"));
					gf_param.put("CURV_NO", data.get("ATTR2"));

					List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);
					String sFunc = "";
					if (grapFunchList.size() > 0) {
						sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
					}

					sFunc = sFunc.replace("x", "" + dSealSize); //

					if (25 < NumberUtil.toDouble(engine.eval(sFunc))) { // 차압을 초과하면
						setResultProcList(procRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
								NumberUtil.toInt(m.get("P_IDX")),
								"[A4] Graph 체크 조건으로 삭제, 제한 차압(Differential Pressure) : " + engine.eval(sFunc));
						chkGraphRemoveList.add("" + m.get("P_IDX"));
					}
				}
			}
		}

		System.out.println("Total Gas Consumption Start");
		// GF-200 Gas Consumption 정보 표시
		// Total Gas Consumption(SCFH) = CF * RPM / 1000
		for (Map<String, Object> m : sealRstList) {
			if ("GF-200".equals(m.get("SEAL_TYPE")) || "GX-200".equals(m.get("SEAL_TYPE"))) {
				List<Map<String, Object>> list = null;
				if ("GF-200".equals(m.get("SEAL_TYPE"))) {
					list = rBMapper.selectRuleComListA501(item);
				} else if ("GX-200".equals(m.get("SEAL_TYPE"))) {
					list = rBMapper.selectRuleComListA601(item);
				} else {
					list = new ArrayList();
				}

				if (list.size() > 0) {
					Map<String, Object> data = (Map<String, Object>) list.get(0);

					// Graph 조회
					gf_param.clear();
					gf_param.put("VAL", dSealChamPres_psig); // Seal Chamber Pressure (Psig)
					gf_param.put("GRAPH_NO", data.get("ATTR1"));
					gf_param.put("CURV_NO", data.get("ATTR2"));

					List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);
					String sFunc = "";
					if (grapFunchList.size() > 0) {
						sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
					}

					sFunc = sFunc.replace("x", "" + dSealChamPres_psig); //

					double dCF = NumberUtil.toDouble(engine.eval(sFunc)); // CF 값

					// SCFH 값 Note에 추가
					setResultNoteList(noteRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
							NumberUtil.toInt(m.get("P_IDX")),
							"Total Gas Consumption(SCFH) : " + dCF * NumberUtil.toDouble(item.get("RPM_MAX")) / 1000);
				}

			} else if ("ML-200".equals(m.get("SEAL_TYPE"))) {
				// ML-200 Curve 체크
				// Inboard Consumption + Outboard Consumption
				// Inboard 대상 차압 : Seal cham press + 50 psig - Seal cham press = 50psig
				// Outboard 대상 차압 : Seal cham press + 50 psig - 0 = Seal cham press + 50 psig
				List<Map<String, Object>> list = rBMapper.selectRuleComListA701(item);

				if (list.size() > 0) {
					Map<String, Object> data = (Map<String, Object>) list.get(0);

					double dDP_in_psig = 50;
					double dDP_out_psig = dSealChamPres_psig + 50;

					// Graph 조회
					gf_param.clear();
					gf_param.put("VAL", dSealChamPres_psig); // Seal Chamber Pressure (Psig)
					gf_param.put("GRAPH_NO", data.get("ATTR1"));
					gf_param.put("CURV_NO", data.get("ATTR2"));

					List<Map<String, Object>> grapFunchList = rBMapper.selectRuleGraphFunc(gf_param);
					String sFunc = "";
					String sFunc_in = "";
					String sFunc_out = "";
					if (grapFunchList.size() > 0) {
						sFunc = StringUtil.get(((Map<String, Object>) grapFunchList.get(0)).get("FUNC"));
					}

					sFunc_in = sFunc.replace("x", "" + dDP_in_psig); // inboard 기준
					sFunc_out = sFunc.replace("x", "" + dDP_out_psig); // outboart 기준

					double dTotalCF = NumberUtil.toDouble(engine.eval(sFunc_in))
							+ NumberUtil.toDouble(engine.eval(sFunc_out));

					// SCFH 값 Note에 추가
					setResultNoteList(noteRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
							NumberUtil.toInt(m.get("P_IDX")), "Total Gas Consumption(SCFH) : " + dTotalCF);
				}

			}
		}

		// O-ring (Gaskets) 온도범위 체크
		Iterator iterM3 = material3RstList.iterator();
		while (iterM3.hasNext()) {
			Map<String, Object> m = (HashMap<String, Object>) iterM3.next();
			addInfo = (HashMap<String, Object>) m.get("ADD_INFO");
			param.clear();
			param.put("ATTR1", addInfo.get("MTRL_CD"));
			List<Map<String, Object>> list = rBMapper.selectRuleComListC601(param);

			if (list.size() > 0) {
				for (Map<String, Object> c6data : list) {
					if (NumberUtil.toDouble(c6data.get("ATTR2")) > NumberUtil.toDouble(item.get("TEMP_MIN"))
							|| NumberUtil.toDouble(c6data.get("ATTR3")) < NumberUtil.toDouble(item.get("TEMP_MAX"))) {

						setResultProcList(procRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
								NumberUtil.toInt(m.get("P_IDX")), "[C6] O-Ring 온도체크 조건으로 삭제 : " + m.get("P_VAL"));
						iterM3.remove();
						break;
					}
				}
			}
		} // end while

		// 삭제 대상이 있을 경우 삭제
		if (chkGraphRemoveList.size() > 0) {
			removeResult(sealRstList, chkGraphRemoveList);
			// removeResult(materialRstList,chkGraphRemoveList);
			removeResult(planRstList, chkGraphRemoveList);
			removeResult(noteRstList, chkGraphRemoveList);
			removeResult(procRstList, chkGraphRemoveList);
			removeResult(material1RstList, chkGraphRemoveList);
			removeResult(material2RstList, chkGraphRemoveList);
			removeResult(material3RstList, chkGraphRemoveList);
			removeResult(material4RstList, chkGraphRemoveList);
		}

	}

	/**
	 * 추가적인 정보 표시
	 * 
	 * @param item
	 * @param fp
	 * @throws Exception
	 */
	private void step6_add_note(String _pID, String sNo, int iPIdx, Map<String, Object> item, Map<String, Object> fp)
			throws Exception {

		List<Map<String, Object>> planRstList = (List<Map<String, Object>>) fp.get("planRstList");
		List<Map<String, Object>> noteRstList = (List<Map<String, Object>>) fp.get("noteRstList");
		String[] saProductGroup = (String[]) fp.get("saProductGroup");
		String[] saProduct = (String[]) fp.get("saProduct");

		double dSealSize = NumberUtil.toDouble(item.get("SEAL_SIZE")); // Inch
		double dSgMin = NumberUtil.toDouble(item.get("SPEC_GRAVITY_MIN")); // 비중

		Map<String, Object> ptm = new HashMap<String, Object>();

		// B-12 face Material
		String sFaceMaterialNoteHard = "", sFaceMaterialNoteSoft = "";

		// hard
		if (isProduct("HCL", saProductGroup, saProduct) || isProduct("H2SO4", saProductGroup, saProduct)
				|| isProduct("HNO3", saProductGroup, saProduct)) {
			sFaceMaterialNoteHard += "On very strong acids (examples 30% HCl, 50% H2SO4, 50% HNO3)\n";
		}

		if (getProductCont(item, "NAOH", "%") >= 50) {
			sFaceMaterialNoteHard += "On very strong caustics (50% NaOH)\n";
		}

		if (getProductCont(item, "SOLID", "PPM") >= 500) {
			sFaceMaterialNoteHard += "On products containing more than 500 ppm solids(without filtering system)\n";
		}

		if (NumberUtil.toDouble(item.get("VISC_MIN")) > 680) {
			sFaceMaterialNoteHard += "On viscous products(higer than 680 cP)\n";
		}

		if (isProduct("CRUDE OIL", saProductGroup, saProduct) && isProduct("WATER", saProductGroup, saProduct)) {
			if (getProductCont(item, "WATER", "%") <= 10) {
				sFaceMaterialNoteHard += "On Crude oils with a water cut from 1 to 10%\n";
			}
		}

		if (!"".equals(sFaceMaterialNoteHard)) {
			sFaceMaterialNoteHard = "[B1-12] When hard faces should be used :  \n" + sFaceMaterialNoteHard;
		}

		// soft
		if (NumberUtil.toDouble(item.get("SPEC_GRAVITY_MAX")) <= 0.65) {
			sFaceMaterialNoteSoft += "On light hydrocarbons(specific gravity below 0.65)\n";
		}

		if (isProduct("WATER", saProductGroup, saProduct) && NumberUtil.toDouble(item.get("TEMP_MIN")) > 70) {
			sFaceMaterialNoteSoft += "On water above 70℃\n";
		}

		if (NumberUtil.toDouble(item.get("VAP_PRES_MAX")) * 1.1 > NumberUtil.toDouble(item.get("SEAL_CHAM_MAX"))) {
			sFaceMaterialNoteSoft += "On Products with a vapour pressure very close to the sealing pressure(vapour pressure X 1.1 > Sealing pressure)\n";
		}

		if (isProduct("CRUDE OIL", saProductGroup, saProduct) && isProduct("WATER", saProductGroup, saProduct)) {
			if (getProductCont(item, "WATER", "%") > 50) {
				sFaceMaterialNoteSoft += "On Crude oils containing more than 50% water \n";
			}
		}

		if (NumberUtil.toDouble(item.get("VISC_MAX")) < 0.4) {
			sFaceMaterialNoteSoft += "On products with a viscosity below 0.4cP\n";
		}

		if (NumberUtil.toDouble(item.get("VISC_MIN")) >= 0.4 && NumberUtil.toDouble(item.get("VISC_MAX")) <= 2
				&& NumberUtil.toDouble(item.get("SEAL_CHAM_MIN")) > 10) {
			sFaceMaterialNoteSoft += "On products with a viscosity between 0.4 and 2 cP and pressures above 10 barG \n";
		}

		if (!"".equals(sFaceMaterialNoteSoft)) {
			sFaceMaterialNoteSoft = "[B1-12] When carbon faces should be used :  \n" + sFaceMaterialNoteSoft;
		}

		// note 추가
		if (!"".equals(sFaceMaterialNoteHard)) {
			setResultNoteList(noteRstList, _pID, sNo, 999, sFaceMaterialNoteHard);
		}

		if (!"".equals(sFaceMaterialNoteSoft)) {
			setResultNoteList(noteRstList, _pID, sNo, 999, sFaceMaterialNoteSoft);
		}

		// B1-4 Seal Injection and Quench Guidelines
		for (Map<String, Object> m : planRstList) {
			String sPlans = StringUtil.get(m.get("P_VAL")); // Plan 정보
			for (String sPlan : sPlans.split("/")) {
				if ("11".equals(sPlan) || "13".equals(sPlan) || "31".equals(sPlan)) {
					// 비중 0.65 초과 <---- 650kg/m3 이상 (비중 0.65)
					if (dSgMin > 0.65) {
						setResultNoteList(noteRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
								NumberUtil.toInt(m.get("P_IDX")), "Plan " + sPlan + " : flow in liters per minute : "
										+ (dSealSize == 0 ? "Seal Size in inch x 3" : (dSealSize * 3)));
					} else {
						setResultNoteList(noteRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
								NumberUtil.toInt(m.get("P_IDX")), "Plan " + sPlan + " : flow in liters per minute : "
										+ (dSealSize == 0 ? "Seal Size in inch x 6" : (dSealSize * 6)));
					}
				} else if ("32".equals(sPlan)) {

					ptm.clear();
					ptm.put("MCD", "B1401");
					List<Map<String, Object>> rComList = getRuleComListType1(ptm);

					String sNote = "";
					double dStdBushing = 0, dAPIClearanceBushing = 0, dLipSeal = 0, dRuleOfThumb = 0,
							dRequiredForCooling = 0;
					double dRate = 0;
					for (Map<String, Object> mCom : rComList) {

						if (dSealSize <= NumberUtil.toDouble(mCom.get("ATTR1"))) {
							dStdBushing = NumberUtil.toDouble(mCom.get("ATTR2"));
							dAPIClearanceBushing = NumberUtil.toDouble(mCom.get("ATTR3"));
							dLipSeal = NumberUtil.toDouble(mCom.get("ATTR4"));
							dRuleOfThumb = NumberUtil.toDouble(mCom.get("ATTR5"));
							dRequiredForCooling = NumberUtil.toDouble(mCom.get("ATTR6"));

							dRate = dSealSize / NumberUtil.toDouble(mCom.get("ATTR1"));

							sNote = "Plan " + sPlan + " : [Seal injection and quench guidelines]<br/> ";
							sNote += "&nbsp;Standard bushing (lpm) : " + (dStdBushing * dRate) + ",";
							sNote += "&nbsp;API clearance bushing (lpm) : " + (dAPIClearanceBushing * dRate) + ",";
							sNote += "&nbsp;Lip seal (lph) : " + (dLipSeal * dRate) + ",";
							sNote += "&nbsp;Rule of thumb (lpm) : " + (dRuleOfThumb * dRate) + ",";
							sNote += "&nbsp;Required for cooling (to 250°C) (lpm) : " + (dRequiredForCooling * dRate);

							setResultNoteList(noteRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
									NumberUtil.toInt(m.get("P_IDX")), sNote);
							break;
						}
					}

				} else if ("62".equals(sPlan)) {
					// Quench Type에 따라 처리
					// Z130010 Water
					// Z130020 Nitrogen
					// Z130030 Steam

					if ("Z130010".equals(StringUtil.get("QUENCH_TYPE"))) { // water
						ptm.clear();
						ptm.put("MCD", "B1403");
						List<Map<String, Object>> rComList = getRuleComListType1(ptm);

						String sNote = "";
						double dToAvoidCrystalBuildUp = 0, dToCarryAwaySolidsFromLeakage = 0, dFroCollingPurposes = 0;
						double dRate = 0;
						for (Map<String, Object> mCom : rComList) {
							if (dSealSize <= NumberUtil.toDouble(mCom.get("ATTR1"))) {
								dToAvoidCrystalBuildUp = NumberUtil.toDouble(mCom.get("ATTR2"));
								dToCarryAwaySolidsFromLeakage = NumberUtil.toDouble(mCom.get("ATTR3"));
								dFroCollingPurposes = NumberUtil.toDouble(mCom.get("ATTR4"));

								dRate = dSealSize / NumberUtil.toDouble(mCom.get("ATTR1"));

								sNote = "Plan " + sPlan + ": [Seal injection and quench guidelines]<br/> ";
								sNote += "&nbsp;To avoid crystal build up (lph) : " + (dToAvoidCrystalBuildUp * dRate)
										+ ",";
								sNote += "&nbsp;To carry away solids from leakage (lph) : "
										+ (dToCarryAwaySolidsFromLeakage * dRate) + ",";
								sNote += "&nbsp;For cooling purposes above 80℃ (lph) : "
										+ (dFroCollingPurposes * dRate);

								setResultNoteList(noteRstList, StringUtil.get(m.get("P_ID")),
										StringUtil.get(m.get("P_NO")), NumberUtil.toInt(m.get("P_IDX")), sNote);
								break;
							}
						}
					} else if ("Z130020".equals(StringUtil.get("QUENCH_TYPE"))) { // Nitrogen
						setResultNoteList(noteRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
								NumberUtil.toInt(m.get("P_IDX")), "[Plan " + sPlan + "] flow in liters per hour : "
										+ (dSealSize == 0 ? "Seal Size in inch x 20" : (dSealSize * 20)));
					} else if ("Z130030".equals(StringUtil.get("QUENCH_TYPE"))) { // Steam
						setResultNoteList(noteRstList, StringUtil.get(m.get("P_ID")), StringUtil.get(m.get("P_NO")),
								NumberUtil.toInt(m.get("P_IDX")), "[Plan " + sPlan + "] flow in liters per kg/hour : "
										+ (dSealSize == 0 ? "Seal Size in inch x 0.25" : (dSealSize * 0.25)));
					}
				}
			}

		}

	}

	/*
	 * ==============================================================
	 * 
	 * 내부 사용 Function
	 * 
	 * ==============================================================
	 */

	/**
	 * 추천결과 Array에 추가
	 * 
	 * @param list
	 * @param sID
	 * @param sNo
	 * @param sType
	 * @param iIdx
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> setResultList(List<Map<String, Object>> list, String sID, String sNo,
			String sType, int iIdx, Object val) throws Exception {
		return setResultList(list, sID, sNo, sType, iIdx, val, null);
	}

	private List<Map<String, Object>> setResultList(List<Map<String, Object>> list, String sID, String sNo,
			String sType, int iIdx, Object val, Map<String, Object> addInfo) throws Exception {

		// 값이 없이 넘어올경우 Skip
		if (val == null || "".equals(String.valueOf(val).trim()))
			return list;

		// 중복체크
		boolean b = true;
//		for(Map<String,Object> m : list) {
//			if(String.valueOf(m.get("P_VAL")).equals(String.valueOf(val))) {
//				b=false;
//				break;
//			}
//		}

		if (b) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_ID", sID);
			m.put("P_NO", sNo);
			m.put("P_TYPE", sType);
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("P_VAL", String.valueOf(val).trim()); // Seal, Material, Api Plan 정보
			m.put("ADD_INFO", addInfo);
			list.add(m);
			
			
			addList(sType,iIdx,String.valueOf(val).trim());
		}
		return list;
	}

	private List<Map<String, Object>> setMaterialResultList(List<Map<String, Object>> list, String sID, String sNo,
			String sType, int iIdx, Object val, Map<String, Object> addInfo) throws Exception {

		// 값이 없이 넘어올경우 Skip
		if (val == null || "".equals(String.valueOf(val).trim()))
			return list;

		// 중복체크
		boolean b = true;
//		for(Map<String,Object> m : list) {
//			if(String.valueOf(m.get("P_VAL")).equals(String.valueOf(val))) {
//				b=false;
//				break;
//			}
//		}

		if (b) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_ID", sID);
			m.put("P_NO", sNo);
			m.put("P_TYPE", sType);
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("P_VAL", String.valueOf(val).trim()); // 재질정보 표시 Digit
			m.put("ADD_INFO", addInfo); // 추가정보
			list.add(m);
		}
		return list;
	}

	/**
	 * 재질정보를 추천결과에 담기 전 선행 처리
	 * 
	 * @param list
	 * @param sealRstList
	 * @param sID
	 * @param sNo
	 * @param sType
	 * @param iIdx
	 * @param val
	 * @param pre_addInfo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> setMaterialResultListPre(List<Map<String, Object>> list, // 설정할 Material List
			List<Map<String, Object>> sealRstList, // 설정된 Seal List (추가정보 조회용)
			String sID, String sNo, String sType, int iIdx, Object val, HashMap<String, Object> pre_addInfo)
			throws Exception {

		if (val == null || "".equals(String.valueOf(val).trim()))
			return list;

		// val = material code

		// 재질그룹 체크
		/*
		 * FKM : GU FFKM : X675, AD, Chemraz 505, Chemraz 555, Chemraz 615 =>
		 * X675,AD,G005,G002,G006 RESIN CARBON : KR3, GE CARBON : Carbon 우선순위 SIC =>
		 * SL,YO TUC => LV,RI
		 */

		Map<String, Object> addInfo = null;

		if ("[FKM]".equals(String.valueOf(val))) { // FKM 재질
			for (String s : getMaterialDetail("[FKM]").split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) pre_addInfo.clone();
				addInfo.put("MTRL_CD", s);
				addInfo.put("MTRL_NM", getMaterialNm("3", s));
				setMaterialResultList(list, sID, sNo, "M3", iIdx, getMaterialDigit("3", s), addInfo);
			}
		} else if ("[FFKM]".equals(String.valueOf(val))) { // FFKM 재질
			for (String s : "X675,AD,G005,G002,G006".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) pre_addInfo.clone();
				addInfo.put("MTRL_CD", s);
				addInfo.put("MTRL_NM", getMaterialNm("3", s));
				setMaterialResultList(list, sID, sNo, "M3", iIdx, getMaterialDigit("3", s), addInfo);
			}
		} else if ("[SIC]".equals(String.valueOf(val))) { // Silicon carbide 재질
			for (String s : "SL,YO".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) pre_addInfo.clone();
				addInfo.put("MTRL_CD", s);
				addInfo.put("MTRL_NM", getMaterialNm(sType, s));
				setMaterialResultList(list, sID, sNo, "M" + sType, iIdx, getMaterialDigit(sType, s), addInfo);
			}
		} else if ("[TUC]".equals(String.valueOf(val))) { // tungsten carbide 재질
			for (String s : "LV,RI".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) pre_addInfo.clone();
				addInfo.put("MTRL_CD", s);
				addInfo.put("MTRL_NM", getMaterialNm(sType, s));
				setMaterialResultList(list, sID, sNo, "M" + sType, iIdx, getMaterialDigit(sType, s), addInfo);
			}
		} else if ("[RESIN CARBON]".equals(String.valueOf(val))) {
			for (String s : "KR3, GE".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) pre_addInfo.clone();
				addInfo.put("MTRL_CD", s);
				addInfo.put("MTRL_NM", getMaterialNm(sType, s));
				setMaterialResultList(list, sID, sNo, "M" + sType, iIdx, getMaterialDigit(sType, s), addInfo);
			}
		} else if ("[CARBON]".equals(String.valueOf(val))) {

			/*
			 * KR3,RY,GE,AP AP,RY = Antimony Carbon
			 * 
			 * Engineered Seal (HSH, DHTW, UHTW) 1 AP 우선 선정 2 GE 화학적 부식성 등의 이유로 필요시 그 외 Seal
			 * 1 - Seal Type별 표준 재질에서 지정된 것을 우선 선정 2 KR3 Seal Type별 표준 재질 미지정이면서, Dura PV
			 * Curve에 포함된 Seal Type인 경우 우선 선정 3 RY Seal Type별 표준 재질 미지정이면서, 상기 2에 해당하지 않는 경우
			 * 우선 선정 4 GE 화학적 부식성 등의 이유로 필요시
			 */

			String sCarbonMtrlCd = "";

			// Engineered Seal (
			for (Map<String, Object> m : sealRstList) {
				if (iIdx == NumberUtil.toInt(m.get("P_IDX")) && ("HSH".equals(StringUtil.get(m.get("SEAL_TYPE")))
						|| "DHTW".equals(StringUtil.get(m.get("SEAL_TYPE")))
						|| "UHTW".equals(StringUtil.get(m.get("SEAL_TYPE"))))) {
					sCarbonMtrlCd = "AP";
					break;
				}
			}

			if ("".equals(sCarbonMtrlCd)) {
				// PV-Curve 적용여부 체크
				for (Map<String, Object> m : sealRstList) {
					if (iIdx == NumberUtil.toInt(m.get("P_IDX")) && !"".equals(StringUtil.get(m.get("PV_CURVE")))) {
						sCarbonMtrlCd = "KR3";
						break;
					}
				}
			}

			if ("".equals(sCarbonMtrlCd)) {
				sCarbonMtrlCd = "RY";
			}

			addInfo = new HashMap<String, Object>();
			if (pre_addInfo != null)
				addInfo = (Map<String, Object>) pre_addInfo.clone();
			addInfo.put("MTRL_CD", sCarbonMtrlCd);
			addInfo.put("MTRL_NM", getMaterialNm(sType, sCarbonMtrlCd));
			setMaterialResultList(list, sID, sNo, "M" + sType, iIdx, getMaterialDigit(sType, sCarbonMtrlCd), addInfo);

		} else if ("[NBR]".equals(String.valueOf(val))) { // tungsten carbide 재질
			for (String s : "GS,GW,QM".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) pre_addInfo.clone();
				addInfo.put("MTRL_CD", s);
				addInfo.put("MTRL_NM", getMaterialNm(sType, s));
				setMaterialResultList(list, sID, sNo, "M" + sType, iIdx, getMaterialDigit(sType, s), addInfo);
			}

		} else if ("[ANTIMONY CARBON]".equals(String.valueOf(val))) { // ANTIMONY CARBON
			for (String s : "RY".split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) pre_addInfo.clone();
				addInfo.put("MTRL_CD", s);
				addInfo.put("MTRL_NM", getMaterialNm(sType, s));
				setMaterialResultList(list, sID, sNo, "M" + sType, iIdx, getMaterialDigit(sType, s), addInfo);
			}

		} else {
			// 일반 재질 등록 시
			for (String s : String.valueOf(val).split(",")) {
				addInfo = new HashMap<String, Object>();
				if (pre_addInfo != null)
					addInfo = (Map<String, Object>) pre_addInfo.clone();
				addInfo.put("MTRL_CD", s);
				addInfo.put("MTRL_NM", getMaterialNm(sType, s));
				setMaterialResultList(list, sID, sNo, "M" + sType, iIdx, getMaterialDigit(sType, s), addInfo);
			}
		}

		return list;
	}

	/**
	 * digit 코드를 직접 받는 경우
	 * 
	 * @param list
	 * @param sealRstList
	 * @param sID
	 * @param sNo
	 * @param sType
	 * @param iIdx
	 * @param val
	 * @param addInfo
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> setMaterialResultListPre2(List<Map<String, Object>> list, // 설정할 Material List
			List<Map<String, Object>> sealRstList, // 설정된 Seal List (추가정보 조회용)
			String sID, String sNo, String sType, int iIdx, Object val, Map<String, Object> addInfo) throws Exception {

		if (val == null || "".equals(String.valueOf(val).trim()))
			return list;

		// val = material digit

		for (String s : String.valueOf(val).split(",")) {
			addInfo = new HashMap<String, Object>();
			String sCd = getMaterialCd(sType, s);
			addInfo.put("MTRL_CD", sCd);
			addInfo.put("MTRL_NM", getMaterialNm(sType, sCd));
			setMaterialResultList(list, sID, sNo, "M" + sType, iIdx, s, addInfo);
		}
		return list;
	}

	/**
	 * 현재 미사용 ---- 입력파라메타에서 입력된 product의 농도를 반환한다. 넘어온 Product를 동일 그룹핑정보이므로 해당되는 항목을
	 * 입력값에서 찾아서 반환
	 * 
	 * @param item
	 * @param product
	 * @return
	 */
	private double getProductCont_byGrouping(Map<String, Object> item, String product, String unit,
			String[] saProductGroup, String[] saProduct) {
		double dCont = 0d;
		for (int i = 1; i <= 12; i++) {

			String sInsProductNmTmp = StringUtil.get(item.get("PRODUCTNM_" + i));
			if (isProduct(sInsProductNmTmp, saProductGroup, saProduct)) {
				// unit은 PPM과 % 두가지 값중에 하나가 들어온다는 가정임.
				if (unit.equals(StringUtil.get(item.get("PRODUCTUT_" + i)))) {
					dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i));
				} else {
					if (unit.equals("%")) { // %
						dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i)) / 10000.0;
					} else { // ppm
						dCont = NumberUtil.toDouble(item.get("PRODUCTND_" + i)) * 10000.0;
					}
				}
				break;
			}

		}
		return dCont;
	}

	private String getProductGb_byGrouping(Map<String, Object> item, String product, String[] saProductGroup,
			String[] saProduct) {
		String sGb = "";
		for (int i = 1; i <= 12; i++) {

			String sInsProductNmTmp = StringUtil.get(item.get("PRODUCTNM_" + i));
			// System.out.println("sInsProductNmTmp : " + sInsProductNmTmp);

			if (isProduct(sInsProductNmTmp, saProductGroup, saProduct)) {

				// System.out.println("isproduct Y : " + item.get("PRODUCTGB_"+i));
				sGb = StringUtil.get(item.get("PRODUCTGB_" + i));
				break;
			}

			if (product.equals(sInsProductNmTmp)) {
				sGb = StringUtil.get(item.get("PRODUCTGB_" + i));
				break;
			}
		}
		return sGb;
	}

	/**
	 * 추천결과 Note 정보를 Array에 추가
	 * 
	 * @param list
	 * @param sID
	 * @param sNo
	 * @param sType
	 * @param iIdx
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> setResultNoteList(List<Map<String, Object>> list, String sID, String sNo,
			int iIdx, Object val) throws Exception {
		return setResultNoteList(list, sID, sNo, iIdx, val, null, null);
	}
	
	/**
	 * 
	 * @param list
	 * @param sID
	 * @param sNo
	 * @param iIdx
	 * @param val
	 * @param sType : c(공통), s(seal), m(material), p(plan)
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> setResultNoteList(List<Map<String, Object>> list, String sID, String sNo,
			int iIdx, Object val, String sType, Map<String,Object> addNote) throws Exception {
		
		// 중복체크
		boolean b = true;
		for (Map<String, Object> m : list) {
			if (String.valueOf(m.get("NOTE")).equals(String.valueOf(val))) {
				b = false;
				break;
			}
		}
		
		if(sType == null) sType = "";
		
		if (b) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_ID", sID);
			m.put("P_NO", sNo);
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("NOTE", val);
			list.add(m);
			
			Map<String, Object> map = new HashMap<String, Object>();
			if(_sealTypeList.size()==0 && _apiPlanList.size()==0) {
				map.put("P_IDX", 0);
				map.put("NOTE", val);
				map.put("SEAL", "");
				map.put("PLAN", "");
			}else if(_sealTypeList.size()==_apiPlanList.size()) {
				int pIdx = Integer.parseInt(_sealTypeList.get(_sealTypeList.size()-1).get("P_IDX").toString());
				String seal = _sealTypeList.get(_sealTypeList.size()-1).get("P_VAL").toString();
				String plan = _apiPlanList.get(_apiPlanList.size()-1).get("P_VAL").toString();
				map.put("P_IDX", pIdx);
				map.put("NOTE", val);
				map.put("SEAL", seal);
				if(val.toString().indexOf("Plan 11")>-1) {
					map.put("PLAN", "11");
				}else {
					map.put("PLAN", plan);
				}
			}else if(_sealTypeList.size()>_apiPlanList.size()) {
				int pIdx = Integer.parseInt(_sealTypeList.get(_sealTypeList.size()-1).get("P_IDX").toString());
				String seal = _sealTypeList.get(_sealTypeList.size()-1).get("P_VAL").toString();
				map.put("P_IDX", pIdx);
				map.put("NOTE", val);
				map.put("SEAL", seal);
				map.put("PLAN", "");
			}else if(_sealTypeList.size()<_apiPlanList.size()) {
				int pIdx = Integer.parseInt(_apiPlanList.get(_apiPlanList.size()-1).get("P_IDX").toString());
				String plan = _apiPlanList.get(_apiPlanList.size()-1).get("P_VAL").toString();
				map.put("P_IDX", pIdx);
				map.put("NOTE", val);
				map.put("SEAL", "");
				if(val.toString().indexOf("Plan 11")>-1) {
					map.put("PLAN", "11");
				}else {
					map.put("PLAN", plan);
				}
			}
			map.put("TYPE", sType);
			_noteList.add(map);
		}
		return list;
	}

	/**
	 * 추천결과 진행과정 정보를 Array에 추가
	 * 
	 * @param list
	 * @param sID
	 * @param sNo
	 * @param sType
	 * @param iIdx
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> setResultProcList(List<Map<String, Object>> list, String sID, String sNo,
			int iIdx, Object val) throws Exception {
		// 중복체크
		boolean b = true;
		for (Map<String, Object> m : list) {
			if ((String.valueOf(m.get("P_IDX")) + String.valueOf(m.get("PROC_CONT")))
					.equals(iIdx + String.valueOf(val))) {
				b = false;
				break;
			}
		}
		if (b) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("P_ID", sID);
			m.put("P_NO", sNo);
			m.put("P_IDX", iIdx);
			m.put("P_SEQ", getMaxSeq(list, iIdx));
			m.put("PROC_CONT", val);
			list.add(m);
		}
		return list;
	}

	/**
	 * Rule 추천정보 결과를 저장
	 * 
	 * @param p - List
	 * @throws Exception
	 */
	/*
	 * private void setRuleRst(List<Map<String,Object>> l) throws Exception{
	 * for(Map<String,Object> p : l) { if(p.get("P_VAL")!=null &&
	 * !"".equals(p.get("P_VAL"))) // Value에 값이 없는 경우는 저장 Skip
	 * rBMapper.insertRuleRst(p); } }
	 */

	/**
	 * Rule 추천정보 결과를 저장
	 * 
	 * @param p - Map
	 * @throws Exception
	 */
	/*
	 * private void setRuleRst(Map<String,Object> p) throws Exception{
	 * rBMapper.insertRuleRst(p); }
	 */

	/**
	 * Rule 추천정보 결과 특이사항(Note)을 저장
	 * 
	 * @param p - List
	 * @throws Exception
	 */
	/*
	 * private void setRuleRstNote(List<Map<String,Object>> l) throws Exception{
	 * for(Map<String,Object> p : l) { rBMapper.insertRuleRstNote(p); } }
	 */

	/**
	 * Rule 추천정보 결과 주요 처리 정보를 저장
	 * 
	 * @param p - List
	 * @throws Exception
	 */
	/*
	 * private void setRuleRstProc(List<Map<String,Object>> l) throws Exception{
	 * for(Map<String,Object> p : l) { rBMapper.insertRuleRstProc(p); } }
	 */

	private String getMaterialPartName(Object o) {
		if ("1".equals(o.toString()))
			return "Metal";
		else if ("2".equals(o.toString()))
			return "R. Face";
		else if ("3".equals(o.toString()))
			return "Gaskets";
		else if ("4".equals(o.toString()))
			return "S. Face";
		else
			return o.toString();
	}

	public void _____________test_func_check______________() throws Exception {
	}

	// Transaction Test
	@Override
	public void test(Map<String, Object> param) throws Exception {

//			Map<String,Object> p = new HashMap<String,Object>();
//			p.put("MCD","'B301'");
//			//p.put("MCD","'B1910'");
//			//p.put("MCD","B1910");
//			p.put("TEMP_MIN","10");
//			p.put("TEMP_MAX","100");
//			p.put("SEAL_CHAM_MIN","1");
//			p.put("SEAL_CHAM_MAX","11");
//			p.put("VAP_PRES_MIN","0");
//			p.put("VAP_PRES_MAX","0");
//			List<Map<String,Object>> l = rBMapper.selectRuleComListB301(p);
//			System.out.println(l);
//			

//			Map<String,Object> m1 = new HashMap<String,Object>();
//			m1.put("MCD","A");
//			m1.put("SCD","AA");
//			m1.put("CD_NM","에이");
//			test1(m1);
//			
//			Map<String,Object> m2 = new HashMap<String,Object>();
//			m2.put("MCD","A");
//			m2.put("SCD","AA");
//			m2.put("CD_NM","에이");
//			test2(m2);

		Map<String, Object> m = new HashMap<String, Object>();
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		Map<String, Object> lm = new HashMap<String, Object>();
		lm.put("A", "AAAAAA");
		l.add(lm);
		m.put("LIST", l);

		testA(m);

		System.out.println(l);
	}

	private void testA(Map<String, Object> m) {
		List<Map<String, Object>> ll = (List<Map<String, Object>>) m.get("LIST");
		Map<String, Object> llm = new HashMap<String, Object>();
		llm.put("B", "BBBBB");
		ll.add(llm);
	}

	@Override
	public void test1(Map<String, Object> param) throws Exception {
		rBMapper.setTest1Save(param);
	}

	@Override
	public void test2(Map<String, Object> param) throws Exception {
		rBMapper.setTest1Save(param);
	}

	
	private void addList(String type, int idx, String val) {
		if(type.equals("S")) {
			boolean dpFlag = false; 
			for(Map<String,Object> s : _sealTypeList) {
				int pIdx = Integer.parseInt(s.get("P_IDX").toString());
				if(idx==pIdx) {
					s.put("P_VAL", val);
					dpFlag = true;
					break;
				}
			}
			if(!dpFlag) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("P_IDX", idx);
				map.put("P_VAL", val);
				_sealTypeList.add(map);
			}
		}else if(type.equals("P")) {
			boolean dpFlag = false; 
			for(Map<String,Object> p : _apiPlanList) {
				int pIdx = Integer.parseInt(p.get("P_IDX").toString());
				if(idx==pIdx) {
					p.put("P_VAL", val);
					dpFlag = true;
					break;
				}
			}
			if(!dpFlag) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("P_IDX", idx);
				map.put("P_VAL", val);
				_apiPlanList.add(map);
			}
		}
	}
	
	
	
	@Override
	public List<Map<String, Object>> getASList(Map<String, Object> params) throws Exception {
		List<Map<String,Object>> predictData = (List<Map<String, Object>>) params.get("RESULT");//CONN_COL
		List<Map<String, Object>> ruleBasedData = (List<Map<String, Object>>) params.get("RULE_RESULT");
		List<String> paramSet = new ArrayList<String>();
		
		for(int i=0;i < predictData.size();i++) {
			LOGGER.info("예측 값 추출:"+i);
			LOGGER.info(predictData.get(i).toString());
			Map<String,Object> predictMap = (Map<String, Object>) predictData.get(i).get("RESULT");
			List<Map<String,Object>> conn_col = (List<Map<String, Object>>) predictMap.get("CONN_COL");
			for(int j=0;j<conn_col.size();j++) {
				Map<String, Object> connMap = conn_col.get(j);
				// connColClass = SealType + " | " + material Inner 1,2,3,4 + " | " + material Inner 1,2,3,4 + " | "+ API PLAN
				String connColClass = (String) connMap.get("CLASS");
				paramSet.add(connColClass);
			};
		}
		
		
		for(int m=0;m<ruleBasedData.size();m++) {
			Map<String, Object> ruleMap = (Map<String, Object>) ruleBasedData.get(m).get("RESULT");
			List<Map<String,Object>> ruleResultMap = (List<Map<String, Object>>) ruleMap.get("RST"); 
			for(int n = 0; n< ruleResultMap.size();n++) {
				Map<String, Object> rstMap = ruleResultMap.get(n);
				String ruleSealType = (String) rstMap.get("SEAL");
				String ruleMTRLIn1 = (String) rstMap.get("MTRL1");
				String ruleMTRLIn2 = (String) rstMap.get("MTRL2");
				String ruleMTRLIn3 = (String) rstMap.get("MTRL3");
				String ruleMTRLIn4 = (String) rstMap.get("MTRL4");
				String ruleMTRLOut1 = (String) rstMap.get("MTRL_OUT1");
				String ruleMTRLOut2 = (String) rstMap.get("MTRL_OUT2");
				String ruleMTRLOut3 = (String) rstMap.get("MTRL_OUT3");
				String ruleMTRLOut4 = (String) rstMap.get("MTRL_OUT4");
				String ruleApiPlan = (String) rstMap.get("PLAN");
				String ruleSeperator = " | ";
				// SealType + " | " + material Inner 1,2,3,4 + " | " + material outer 1,2,3,4 + " | "+ API PLAN 형식으로
				String ruleMergeClass = ruleSealType;
				
				if(!"".equals(ruleMTRLIn1) || 
						!"".equals(ruleMTRLIn2) || 
						!"".equals(ruleMTRLIn3) ||
						!"".equals(ruleMTRLIn4)) {
					
					ruleMergeClass += ruleSeperator + ruleMTRLIn1 + " " + ruleMTRLIn2 +
					" " + ruleMTRLIn3 + " " + ruleMTRLIn4;
				}
				
				if(!"".equals(ruleMTRLOut1) || 
						!"".equals(ruleMTRLOut2) || 
						!"".equals(ruleMTRLOut3) ||
						!"".equals(ruleMTRLOut4)) {
					ruleMergeClass += " / ";
					ruleMergeClass += ruleMTRLOut1;
					ruleMergeClass += "  " + ruleMTRLOut2;
					ruleMergeClass += "  " + ruleMTRLOut3;
					ruleMergeClass += "  " + ruleMTRLOut4;
				}
				
				ruleMergeClass += ruleSeperator;
				ruleMergeClass += ruleApiPlan;
				
				Map<String, Object> sendParam2 = new HashMap<String, Object>();
				
				paramSet.add(ruleMergeClass);
			}
		}
		System.out.println(paramSet);
		Map <String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("filteringClass", paramSet);
		return rBMapper.getASHistoryList(paramMap);
	}
}
