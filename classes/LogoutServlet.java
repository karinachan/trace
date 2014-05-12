import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
 
/**
 * Servlet implementation class LogoutServlet
 */
//@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    String oldSession="";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	out.println("in logoutservlet");
        Cookie[] cookies = request.getCookies();
        if(cookies != null){ 
 
	    for (int i = 0; i < cookies.length; i++) {
		if (cookies[i].getName().equals("user")){
		    out.println("user");
		    out.println(cookies[i].getValue());
		    
		}
		out.println("within if statement");
		cookies[i].setValue("");
	
		cookies[i].setMaxAge(0);
		response.addCookie(cookies[i]);
	    	out.println("cookie"+i+": "+ cookies[i].getValue());
	    }
	  

	} else {
	    out.println("what cookies");
	}
    	    
    //invalidate the session if exists
        HttpSession session = request.getSession(false);
        out.println("User="+session.getAttribute("user"));
	response.setHeader("Cache-control","no-cache, no-store");
        if(session != null){
	    out.println(oldSession);
            session.invalidate();
	   
	    try{

	    out.println(session.getAttribute("user"));
	    } catch (Exception e){
		out.println("it worked");
		//	response.sendRedirect("http://cs.wellesley.edu:8080/trace/");
		//	out.println(session.getAttribute("user"));
	    }
        }
	 response.sendRedirect("http://cs.wellesley.edu:8080/trace/");
    }

 protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doPost(req,res);
    }
 
}

