package com.levware.common.interceptor;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.levware.common.session.SessionInfo;
import com.levware.common.session.SessionUtil;

public class DefaultInterceptor extends HandlerInterceptorAdapter {
	

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws UnsupportedEncodingException, ModelAndViewDefiningException {
		
		return true;
	}

	@Override
	public void postHandle(final HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		if (modelAndView != null) {
			final ModelMap model = modelAndView.getModelMap();
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			
			if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
				model.addAttribute("user",(User) authentication.getPrincipal());
				model.addAttribute("userName", ((User) authentication.getPrincipal()).getUsername());
				model.addAttribute("role",authentication.getAuthorities().toString());
				model.addAttribute("authState", "Logined");
				
				SessionInfo sessInfo = SessionUtil.getSessionInfo(request);
				if(sessInfo!=null) model.addAttribute("ref_data_role", sessInfo.getREF_DATA_ROLE());
			}
			//System.out.println("role : " + authentication.getAuthorities().toString());
		}
	}

	

}