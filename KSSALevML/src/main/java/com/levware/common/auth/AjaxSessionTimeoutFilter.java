package com.levware.common.auth;
/**
* OLAP Application의 Timeout 발생 시 ajax 상태 값을 제어하는 filter
* <p><b>NOTE:</b> 
*  OLAP Application의 Timeout 발생 시 ajax 상태 값을 제어하는 filter
* @author 최진
* @since 2019.04.15
* @version 1.0
* @see
*
* <pre>
* == 개정이력(Modification Information) ==
*
* 수정일	수정자	수정내용
* -------	--------	---------------------------
* 2019.04.15	최 진	최초 생성
*
* </pre>
*/
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

public class AjaxSessionTimeoutFilter implements Filter {

    private String ajaxHeader;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (isAjaxRequest(req)) {
            try {
                chain.doFilter(req, res);
            } catch (AccessDeniedException e) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (AuthenticationException e) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        return req.getHeader(ajaxHeader) != null && req.getHeader(ajaxHeader).equals(Boolean.TRUE.toString());
    }

    public void init(FilterConfig filterConfig) {}
    public void destroy() {}
    
    /**
     * Set AJAX Request Header (Default is AJAX)
     *
     * @param ajaxHeader
     */
    public void setAjaxHeader(String ajaxHeader) {
        this.ajaxHeader = ajaxHeader;
    }

}
