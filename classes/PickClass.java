import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

public class PickClass extends HttpServlet
{

    private static final long serialVersionUID = 1L; //don't really know what this means
    private final String userID = "admin";
    private final String password = "password";
    private String crnpick =""; 

    protected void doRequest(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	res.setContentType("text/html");
	res.setHeader("pragma", "no-cache"); //what does this mean? LOL pragma?  
	PrintWriter out = res.getWriter();

	String self = res.encodeURL(req.getRequestURI());

	Connection con = null; 

	

	try {
	    pageHeader(out, "Pick Session");
	    con = TraceDB.connect("trace_db");

	    String name = getName(con);
	    out.println("<h2>Welcome"+"</h2>");	
	    Cookie [] cookies= req.getCookies();

	    String userName = null;
	    String sessionID = null;

	    
	    out.println(cookies.length); //maybe just simplify this to use cookie length (if 1, then don't let in)
	    if (cookies.length<2){
		out.println("Please log in");
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		  out.println("<font color=red>Either user name or password is wrong.</font>");
	    
            rd.include(req, res);
	    }else {
	    for (Cookie cookie: cookies){
		    
		if (cookie!=null){
		    out.println(cookie.getName());
		    if(cookie.getName().equals("user")){
			out.println("checking for userName");
			userName = cookie.getValue();
			out.println("username is:"+ userName);
		    }
		    if(cookie.getName().equals("JSESSIONID")){
			out.println("checking for ID");
			sessionID = cookie.getValue();
			out.println("session is"+ sessionID); }
		}
	    }

	    printSessionList(out, con, self);
	    }}
	catch (SQLException e) {
	    out.println("Error: " + e);
	}
	catch (Exception e) {
		out.println("Please log in");
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		  out.println("<font color=red>Either user name or password is wrong.</font>");
	    
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

    private void printSessionList(PrintWriter out, Connection con, String self) 
	throws SQLException
    {
	PreparedStatement sessionsQuery = con.prepareStatement("select classes.crn, className from sessions, classes where classes.crn=sessions.crn and tid=?");
	sessionsQuery.setInt(1, 22222222);   //get tid from login later 
	ResultSet results = sessionsQuery.executeQuery(); 
	out.println(self);
	out.println("<p>Choose your session.</p>");
	out.println("<form method='post' action='/trace/servlet/SessionVisits'>"); //wasn't working with post...don't know why though
	out.println("<p><select name='crn'>");
	while(results.next()){
	    String crn = results.getString("crn");
	    out.println(crn);
	    String cname = results.getString("className");
	    out.println("<option value='"+crn+"'>"+cname+"</option>");
	}
	out.println("</select>");

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
        String user = req.getParameter("user");
        String pwd = req.getParameter("pwd");
	PrintWriter out = res.getWriter();
         
        if(userID.equals(user) && password.equals(pwd)){
            HttpSession session = req.getSession();
            session.setAttribute("user", "Swag");
            //setting session to expire in 30 mins
            session.setMaxInactiveInterval(30*60); 
            Cookie userName = new Cookie("user", user);
            res.addCookie(userName);

	    out.println("cookie added");

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
        }else{
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    //  PrintWriter out= res.getWriter();
            out.println("<font color=red>Either user name or password is wrong.</font>");
            rd.include(req, res);
        }
 
    
	doRequest(req, res);
      
    }
}


