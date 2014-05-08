/*PickClass.java UPDATED VERSION*/
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

public class PickClass extends HttpServlet
{

    private static final long serialVersionUID = 1L;
    private String userID; //make these final later
    private String password;
   
    //private String vid; 
    private String logbid;

    protected void doRequest(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	res.setContentType("text/html");
	res.setHeader("pragma", "no-cache"); 
	HttpSession session= req.getSession();
	PrintWriter out = res.getWriter();

	String self = res.encodeURL(req.getRequestURI());

	Connection con = null; 
	/*	
	out.println("user"+req.getCookie("user"));
	out.println("pwd"+req.getCookie("pwd"));
	
	*/
	try {
	    pageHeader(out, "Pick Class");
	    con = TraceDB.connect("trace_db");

	    Cookie [] cookies= req.getCookies();

	    String userName = null;
	    String sessionID = null;
	


	    
	    //  out.println(cookies.length);
	    if (cookies.length<2){ //gonna have to change this.... 
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		out.println("<font color=red>Access Denied. Please log in.</font>");
	    
		rd.include(req, res);
	    }
	    else {
		for (Cookie cookie: cookies){
		    
		    if (cookie!=null){
			if(cookie.getName().equals("user")){
			    userName = cookie.getValue();
			    out.println("username"+userName);
			}
			if(cookie.getName().equals("JSESSIONID")){
			    sessionID = cookie.getValue();
			    out.println("sessionID"+sessionID);
			}
			if (cookie.getName().equals("bid")){
			    logbid= cookie.getValue();
			    out.println("logbid"+logbid);
			}
			//do this earlier? 
	       
			if (cookie.getName().equals("pwd")){
			    password=cookie.getValue();
			    out.println("password"+password);
			}
			
		    }
		}
		String name = getName(con,logbid);
		out.println("<h2>Welcome "+name+".</h2>");
		printSessionList(out, con, self);
	    }
	}
	catch (SQLException e) {
	    out.println("Error: " + e);
	}
	catch (Exception e) {
	    RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    out.println("<font color=red>Access Denied. Please log in.</font>");
	    
            rd.include(req, res);
	    e.printStackTrace(out);
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

    private void printSessionList(PrintWriter out, Connection con, String self) 
	throws SQLException
    {
	//grabbing the max vid and then incrementing to create the unique vid
	PreparedStatement visitQuery= con.prepareStatement("select max(vid) from sessions;");
	ResultSet vidrow= visitQuery.executeQuery();

	String vid= String.valueOf((Math.random() * (10000 - 10)) + 10);
	if (vidrow.next()){
	    vid= String.valueOf(Integer.parseInt(vidrow.getString("max(vid)"))+1);
	}//there, it is now unique lol


	PreparedStatement sessionsQuery= con.prepareStatement("select classes.crn, className from tutors, classes where classes.crn=tutors.crn and bid=?;");
	//	PreparedStatement sessionsQuery = con.prepareStatement("select classes.crn, vid, tid, className from sessions, classes where classes.crn=sessions.crn and tid=?");
	//	out.println(logbid);
	int logbidint= Integer.parseInt(logbid);
	//out.println(logbidint);
	sessionsQuery.setString(1, logbid); 
	//sessionsQuery.setInt(1, logbidint);   //get tid from login later 
	ResultSet results = sessionsQuery.executeQuery(); 
	out.println("<p>Choose your session.</p>");
	out.println("<form method='post' action='SessionVisits'>"); //wasn't working with post...don't know why though
	out.println("<p><select name='crn'>");



	
	while(results.next()){
	    String crn = results.getString("crn");
	    //  out.println(crn);
	    String cname = results.getString("className");
	    //out.println(cname);
	    // String vid= results.getString("vid");
	    //out.println("vid"+ vid);
	    //  out.println("tid"+tid);

	    //out.println("<option name='crn' value='"+crn+vid+"'>"+cname+"</option>");

	    out.println("<option name='crn' value='"+crn+vid+"'>"+cname+"</option>");


	    //	out.println("<input type='hidden' name='vid' value='"+vid+"'>");
	}

	out.println("</select>");
	//	out.println("<input type='hidden'name='crn" 
	
	//  	out.println("<input type='hidden' name='vid' value='"+vid+"'>");
	/*	out.println("<input type='hidden' name='user' value='"+userID+"'>"+
		    "<input type='hidden' name='pwd' value='"+password+"'>"+
	   
	*/
	out.println("<input type='submit' name='submit' value='Go'></form>");

    }

    private String getName(Connection con, String logbid) 
	throws SQLException
    {
	PreparedStatement query = con.prepareStatement("select studname from person where bid=?");
	query.setString(1, logbid); //get bid from login later 
	ResultSet results = query.executeQuery();
	String name = ""; 
	if(results.next())
	    name = results.getString("studname");
	return name; 
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


