import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

public class ConfirmSubmit extends HttpServlet
{

    /*
    private final String userID = "admin";
    private final String password = "password";
    private String crnpick =""; 
    private String vid; */

    protected void doRequest(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	res.setContentType("text/html");
	res.setHeader("pragma", "no-cache"); 
	PrintWriter out = res.getWriter();
	HttpSession session= req.getSession(true);

	String self = res.encodeURL(req.getRequestURI());

	Connection con = null; 

	

	try {
	    String sessionID=session.toString();
	    pageHeader(out, "Database Updated");
	    con = TraceDB.connect("trace_db");

	   	
	    
	    //  out.println(cookies.length); //maybe just simplify this to use cookie length (if 1, then don't let in)
	    if (sessionID==null){
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	
	    
		rd.include(req, res);
	    }
	    else {
	
		out.println("Database has been updated. Thank you!");
		out.println("<form method='post' action='PickClass'>"+
			    "<input type='submit' name='back' value='Back to home'>");
			
	    }
	}
	catch (SQLException e) {
	    out.println("Error: " + e);
	}
	catch (Exception e) {
	    RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	  
	    
            rd.include(req, res);
	    // e.printStackTrace(out);
	}
	finally {
	    if(con!=null){
		try{
		    con.close();
		}
		catch(Exception e){
		    e.printStackTrace(out);
		}
	    }
	}
	out.println("</body></html>");
    }


    private void pageHeader(PrintWriter out, String title) {
        out.println(
		   "<!doctype html>\n"
                    + "<html lang='en'>\n"
                    + "<head>\n"
                    + "<title>"+ title + "</title>\n"
                    + "<meta charset='utf-8'>\n"
                    + "<link rel='stylesheet' type='text/css' href='../css/webdb-style.css'>\n"
                    + "</head>\n");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	doRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	doRequest(req, res);
      
    }
}


