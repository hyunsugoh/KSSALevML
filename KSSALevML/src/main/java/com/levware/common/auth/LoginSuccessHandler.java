package com.levware.common.auth;

/**
* OLAP Application의 로그인 성공 시 동작하는 method 를 제어하는 Handler
* <p><b>NOTE:</b> 
*  OLAP Application의 로그인 성공 시 동작하는 Handler
* @author 최진
* @since 2019.03.04
* @version 1.0
* @see
*
* <pre>
* == 개정이력(Modification Information) ==
*
* 수정일	수정자	수정내용
* -------	--------	---------------------------
* 2019.03.04	최 진	최초 생성
*
* </pre>
*/
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.levware.admin.service.AdminLoginService;
import com.levware.admin.service.AdminService;
import com.levware.common.session.SessionInfo;
import com.levware.common.session.SessionUtil;
import com.levware.user.service.UserDataAPIService;
import com.levware.user.service.UserInfoVO;

public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	public static Logger LOGGER = LogManager.getFormatterLogger(LoginSuccessHandler.class);

	@Resource(name = "UserDataAPIService")
	UserDataAPIService userDataApiService;
	
	@Resource(name = "AdminLoginService")
	AdminLoginService adminLoginService;
	
	@Resource(name = "AdminService")
	AdminService adminService;
	
	/**
	 * 유저 및 관리자 LoginSuccesshandler
	 * @since 2019.04.03
	 * @author 박수연
	 *
	 */
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException{
		User user = (User) authentication.getPrincipal();
		LOGGER.debug(user.getUsername()+ ": User  Logged in : " + user.getUsername());
			try {
				//로그인 권한 체크
				String role = authentication.getAuthorities().toString();
				LOGGER.debug("role :"+ role);
				//LOGGER.debug("ROLE_ADMIN :"+ role.equals("[ROLE_ADMIN]"));
				//LOGGER.debug("ROLE_USER :"+ role.equals("[ROLE_USER]"));
				
				//추가정보를 관리하는 세션 생성
				Map<String,Object> userInfo = null;
				SessionInfo sessInfo = null;
				if (role.equals("[ROLE_ADMIN]")) {
					userInfo = adminService.getManagerInfo(user.getUsername());
					sessInfo = new SessionInfo(String.valueOf(userInfo.get("USER_ID")));
					sessInfo.setREF_DATA_ROLE("");
				}else {
					userInfo = adminService.getUserInfo(user.getUsername());
					sessInfo = new SessionInfo(String.valueOf(userInfo.get("USER_ID")));
					sessInfo.setREF_DATA_ROLE(String.valueOf(userInfo.get("REF_DATA_ROLE")));
				}
				//HttpSession session = request.getSession();
				//session.invalidate();
				SessionUtil.setSessionInfo(request,sessInfo);
				
				
				if (role.equals("[ROLE_USER]") || role.equals("[ROLE_SUPER]")){
					userDataApiService.logHisInsert(user.getUsername()); //이력 저장
				}
				
				// 2019.10.14 메인 화면으로 이동- 권한 모두 통일
				setDefaultTargetUrl("/user/main.do");
				super.onAuthenticationSuccess(request, response, authentication);
				
				//관리자
//				if(role.equals("[ROLE_SUPER]")){
//					// 2019.10.14 메인 화면으로 이동- 권한 모두 통일
//					setDefaultTargetUrl("/user/main.do");
//					super.onAuthenticationSuccess(request, response, authentication);
//				}else if(role.equals("[ROLE_ADMIN]")){
//					//IP 체크
//					String managerId = user.getUsername(); //유저Id
//					String clientIp = IPTraceUtils.getRemoteAddr(request); //접속ip
//					
//					//db IP
//					List<AdminInfoVO> ObjectList =  adminLoginService.getManagerIp(managerId);
//					
//					LOGGER.debug("ObjectList", ObjectList.toString());
//					
//					boolean isMatchedIP = false;
//					
//					for(int i=0;i<ObjectList.size();i++){
//						String managerIp = ObjectList.get(i).getManagerIp();
//						if(clientIp.equals(managerIp)){
//							isMatchedIP = true;
//							LOGGER.debug(managerIp+ ": client IP Address Matched -> Allow ====================");
//							break;
//						}
//					}
//					
//					LOGGER.debug(clientIp+ ": clientIp   ====================");
//				
//					if(isMatchedIP){
//						setDefaultTargetUrl("/user/main.do");
//						super.onAuthenticationSuccess(request, response, authentication);
//						
//					}else{	
//						LOGGER.info(isMatchedIP+ ": client IP Address UnMatched -> Denied====================");
//						response.sendRedirect("/logon.do?fail2=true"); //"허용아이피가 아닙니다."
//					}
//				
//				//유저 로그인
//				}else if(role.equals("[ROLE_USER]")){
//					userDataApiService.logHisInsert(user.getUsername()); //이력 저장
//					setDefaultTargetUrl("/user/main.do");
//					super.onAuthenticationSuccess(request, response, authentication);
//				}else{
//					response.sendRedirect("/logon.do?fail3=true"); //"관리자에게 문의해주세요."
//				}
				
			} catch (Exception e) {
				LOGGER.error(e);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.sendRedirect("/logon.do?fail3=true");
			}
		
	}
}

