import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

public class ConfirmSubmit extends HttpServlet
{

    private static final long serialVersionUID = 1L;
    private final String userID = "admin";
    private final String password = "password";
    private String crnpick =""; 
    private String vid; 

    protected void doRequest(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	res.setContentType("text/html");
	res.setHeader("pragma", "no-cache"); 
	PrintWriter out = res.getWriter();

	String self = res.encodeURL(req.getRequestURI());

	Connection con = null; 

	

	try {
	    pageHeader(out, "Database Updated");
	    con = TraceDB.connect("trace_db");

	   	
	    Cookie [] cookies= req.getCookies();

	    String userName = null;
	    String sessionID = null;

	    
	    //  out.println(cookies.length); //maybe just simplify this to use cookie length (if 1, then don't let in)
	    if (cookies.length<2){
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		out.println("<font color=red>Access Denied. Please log in.</font>");
	    
		rd.include(req, res);
	    }
	    else {
		for (Cookie cookie: cookies){
		    
		    if (cookie!=null){
			if(cookie.getName().equals("user")){
			    userName = cookie.getValue();
			}
			if(cookie.getName().equals("JSESSIONID")){
			    sessionID = cookie.getValue();
			}
		    }
		}
		//	String name = getName(con);
		out.println("Database updated. Thank you.");
		out.println("<form method='post' action='PickClass'>"+
			    "<input type='submit' name='back' value='Back to home'>"+
			    "<input type='hidden' name='pwd' value='"+password+"'>"+
			    "<input type='hidden' name='user' value='"+userID+"'></form>");
		//	printSessionList(out, con, self);
	    }
	}
	catch (SQLException e) {
	    out.println("Error: " + e);
	}
	catch (Exception e) {
	    RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    out.println("<font color=red>Access Denied. Please log in.</font>");
	    
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

    /*  private void printSessionList(PrintWriter out, Connection con, String self) 
	throws SQLException
    {
	PreparedStatement sessionsQuery = con.prepareStatement("select classes.crn, vid, tid, className from sessions, classes where classes.crn=sessions.crn and tid=?");
	sessionsQuery.setInt(1, 22222222);   //get tid from login later 
	ResultSet results = sessionsQuery.executeQuery(); 
	out.println("<p>Choose your session.</p>");
	out.println("<form method='post' action='/trace/servlet/SessionVisits'>"); //wasn't working with post...don't know why though
	out.println("<p><select name='crn'>");

	while(results.next()){
	    String crn = results.getString("crn");
	    out.println(crn);
	    String cname = results.getString("className");
	    vid= results.getString("vid");
	    //out.println("vid"+ vid);
	    //  out.println("tid"+tid);
	    out.println("<option value='"+crn+"'>"+cname+"</option>");
	}

	out.println("</select>");

	//	out.println("<input type='hidden' name='vid' value='"+vid+"'>");
	out.println("<input type='hidden' name='user' value='"+userID+"'>"+
		    "<input type='hidden' name='pwd' value='"+password+"'>"+
		    "<input type='hidden' name='vid' value='"+vid+"'>");
	out.println("<input type='submit' name='submit' value='Go'></form>");

    }

    private String getName(Connection con) 
	throws SQLException
    {
	PreparedStatement query = con.prepareStatement("select studname from students where bid=?");
	query.setInt(1, 22222222); //get bid from login later 
	ResultSet results = query.executeQuery();
	String name = ""; 
	if(results.next())
	    name = results.getString("studname");
	return name; 
    }
    */

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


