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
    //   private String crnpick =""; 
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
	


	    
	    //  out.println(cookies.length); //maybe just simplify this to use cookie length (if 1, then don't let in)
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
	PreparedStatement sessionsQuery = con.prepareStatement("select classes.crn, vid, tid, className from sessions, classes where classes.crn=sessions.crn and tid=?");
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
	    String vid= results.getString("vid");
	    //out.println("vid"+ vid);
	    //  out.println("tid"+tid);
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

		//change this to grab studentBID entries from the database..
	// String user = req.getParameter("user");
	// String pwd = req.getParameter("pwd");
	//	PrintWriter out = res.getWriter();
	/*    
        if(userID.equals(user) && password.equals(pwd)){
            HttpSession session = req.getSession();
            session.setAttribute("user", "Swag");
            //setting session to expire in 30 mins
            session.setMaxInactiveInterval(30*60); 
            Cookie userName = new Cookie("user", user);
	    Cookie pwdCook= new Cookie("pwd", password);
	    Cookie bidCook= new Cookie("bid", logbid);
	    Cookie crnCook= new Cookie("crn",crnpick);
            res.addCookie(userName);
	    res.addCookie(pwdCook);
	    res.addCookie(bidCook);
	    res.addCookie(crnCook);
	*/

	    // out.println("cookie added");

	    /*	    String user = (String) session.getAttribute("user");
	String userName = null;
	String sessionID = null;
	Cookie[] cookies = req.getCookies();
	if(cookies !=null){
	    for(Cookie cookie : cookies){
		if(cookie.getName().equals("user")) userName = cookie.getValue();
		if(cookie.getName().equals("JSESSIONID")) sessionID = cookie.getValue();
	    }
	}
            //Get the encoded URL string
	    /*      String encodedURL = response.encodeRedirectURL("http://cs.wellesley.edu:8080/trace/servlet/PickClass"); //this part isn't working...we have a loginsuccess.jsp page but i don't know how to run it. also, i think the sessions got fucked up with the log in part. also as of now there is no way to verify if someone was logged in for each individual sesssion visits or class pick page.....UGH this is really frustrating. 
		    response.sendRedirect(encodedURL);*/

	/*
        }else{
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    //  PrintWriter out= res.getWriter();
            out.println("<font color=red>Either user name or password is wrong.</font>");
            rd.include(req, res);
        }
 
	*/
	doRequest(req, res);
      
    }
}


