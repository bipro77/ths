package com.bipro.ths.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import com.fireflies.mcqs.cache.CacheService;
//import com.bipro.ths.service.McqService;
import com.bipro.ths.service.UserService;
import com.bipro.ths.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

//import com.bracu.hrm.cache.CacheService;
//import com.bracu.hrm.model.User;
//import com.bracu.hrm.model.org.Company;
//import com.bracu.hrm.service.CompanyService;
//import com.bracu.hrm.service.UserService;

@Component
public class DefaultAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	protected Log logger = LogFactory.getLog(this.getClass());
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
//	@Autowired
//	private CacheService cacheService;
	@Autowired
	private UserService userService;

//	@Autowired
//	private McqService mcqService;

//	@Autowired
//	private CompanyService companyService;
	 @Override
	    public void onAuthenticationSuccess(HttpServletRequest request,
					HttpServletResponse response, Authentication authentication)   throws IOException {
		 User currentUser = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if ( !(auth instanceof AnonymousAuthenticationToken) ) {
			currentUser = userService.findByUsername(auth.getName());
			currentUser.setPassword(null);
		}

		String currentUserRole = "";
		 Iterator itr = auth.getAuthorities().iterator();
		 while(itr.hasNext()) {
//			 System.out.println( itr.next());
			 currentUserRole =  itr.next().toString();
		 }
//		 System.out.println("currentUser 88" + currentUser.getFullName());
		HttpSession session = request.getSession();

		session.setAttribute("currentUser", currentUser);
		session.setAttribute("currentUserRole", currentUserRole );
		session.setAttribute("currentUserId", currentUser.getId());
		session.setAttribute("currentUserEmail", currentUser.getEmail());
//		session.setAttribute("quesSetList", mcqService.findDistinctQuesSet());
//		 System.out.println("setList" + mcqService.findDistinctQuesSet());
//		 	cacheService.setUserCase(user);
		 	//Company company =companyService.findById(user.getCompany().getId());
//		 	cacheService.setCompanyCase(user.getCompany());
	        handle(request, response, authentication);
	        clearAuthenticationAttributes(request);
	    }

//	@Override
//	public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
//		//do some logic here if you want something to be done whenever
//		//the user successfully logs in.
//		User currentUser = null;
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		if (!(auth instanceof AnonymousAuthenticationToken)) {
//			currentUser = userService.findByUsername(auth.getName());
//			currentUser.setPassword(null);
//		}
//		HttpSession session = httpServletRequest.getSession();
//		session.setAttribute("currentUser", currentUser);
////		System.out.println("getUsername"+ user.getUsername());
//		//set our response to OK status
//		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//
//		//since we have created our custom success handler, its up to us to where
//		//we will redirect the user after successfully login
//		httpServletResponse.sendRedirect("/");
//	}

	 protected void handle(HttpServletRequest request,
		      HttpServletResponse response, Authentication authentication)
		      throws IOException {
		        String targetUrl = determineTargetUrl(authentication);

		        if (response.isCommitted()) {
		            logger.debug(
		              "Response has already been committed. Unable to redirect to "
		              + targetUrl);
		            return;
		        }

		        redirectStrategy.sendRedirect(request, response, targetUrl);
		    }
	 protected String determineTargetUrl(Authentication authentication) {
	        boolean isUser = false;
	        boolean isAdmin = false;
	        Collection<? extends GrantedAuthority> authorities
	         = authentication.getAuthorities();
	        for (GrantedAuthority grantedAuthority : authorities) {
	            if (grantedAuthority.getAuthority().equals("ROLE_USER")) {
	                isUser = true;
	                break;
	            } else if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
	                isAdmin = true;
	                break;
	            }
	        }

	      /*  if (isUser) {
	            return "/dashboard";
	        } else if (isAdmin) {
	            return "/home";
	        } else {
	            return "/";
	        }*/
	        return "/";
	    }


	 protected void clearAuthenticationAttributes(HttpServletRequest request) {
	        HttpSession session = request.getSession(false);
	        if (session == null) {
	            return;
	        }
	        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	    }

	    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
	        this.redirectStrategy = redirectStrategy;
	    }
	    protected RedirectStrategy getRedirectStrategy() {
	        return redirectStrategy;
	    }

/*	@Override
	public void onAuthenticationSuccess(HttpServletRequest arg0, HttpServletResponse arg1, Authentication authentication)
			throws IOException, ServletException {


		Collectionextends GrantedAuthority> authorities = authentication.getAuthorities();
		authorities.forEach(authority -> {
			if(authority.getAuthority().equals("ROLE_USER")) {
				try {
					redirectStrategy.sendRedirect(arg0, arg1, "/user");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(authority.getAuthority().equals("ROLE_ADMIN")) {
				try {
					redirectStrategy.sendRedirect(arg0, arg1, "/admin");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
	            throw new IllegalStateException();
	        }
		});

	}*/






}
