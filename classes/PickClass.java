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
	
	try {
	    pageHeader(out, "Pick Class");
	    con = TraceDB.connect("trace_db");

	    Cookie [] cookies= req.getCookies();

	    String userName = null;
	    String sessionID = null;
	    String button = req.getParameter("cancel");
	    String canceled = req.getParameter("canceled");
	    String tempvid = req.getParameter("hiddenvid");
	    //	    out.println("button"+button);
	    //  out.println("tempvid"+tempvid);
	   
	    
	    // out.println("length"+cookies.length);

	     
	      if (cookies.length<4){ //gonna have to change this.... 
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		out.println("<font color=red>Access Denied. Please log in.</font>");
	    
		rd.include(req, res);
	      }
	      else {
		  //	  out.println("sessid"+sessionID);
		  /*	  if (sessionID==null) {
		      res.sendRedirect("http://cs.wellesley.edu:8080/trace/");
		     

		  }
		  */

		  for (Cookie cookie: cookies){
		      //   out.println("cookie"+cookie.getName());
		      if (cookie==null){ //if the cookie is nulled 
			 res.sendRedirect("http://cs.wellesley.edu:8080/trace/");

		      }
			if(cookie.getName().equals("JSESSIONID")){
			    sessionID = cookie.getValue();
			    //     out.println("sessionID"+sessionID);
			    
			} 
			if(cookie.getName().equals("user")){
			    userName = cookie.getValue();
			    //  out.println("username"+userName);
			}
		
			if (cookie.getName().equals("bid")){
			    logbid= cookie.getValue();
			    //  out.println("logbid"+logbid);
			}
			//do this earlier? 
	       
			if (cookie.getName().equals("pwd")){
			    password=cookie.getValue();
			    // out.println("password"+password);
			}
			
			//  }
		}
		String name = getName(con,logbid);
		boolean deleted = false;
		out.println("<h2>Welcome "+name+".</h2>");
		if(button!=null){
		    if (button.equals("Cancel Session")){
			//	out.println("cancelling");
			cancelSessions(out,con,tempvid);
			//	out.println("yay success!");
		        deleted = true; 


		    }
		}
		PreparedStatement typequery = con.prepareStatement("select ptype from person where bid=?");
		typequery.setString(1, logbid);
		ResultSet rs = typequery.executeQuery();
		String ptype = ""; 
		//out.println("here");
		if(rs.next())
		    ptype = rs.getString("ptype");
		//	out.println("ptype"+ptype);
		//out.println("here again");
		if(ptype!=null){
		    if(ptype.equals("admin")){
			adminSearch(out, con, self, req);
		    }
		}
		else{
		    //   out.println("here3");
		    printSessionList(out, con, self, deleted, canceled);
		}
	    
		logout(out);
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

    private void adminSearch(PrintWriter out, Connection con, String self, HttpServletRequest req) throws SQLException {
	Statement query = con.createStatement();
	//	ResultSet rs = query.executeQuery("select vid, sessions.tid, studname, sessions.crn, className, vtype, entertime, howlong, status from sessions, person, classes where sessions.tid=person.bid and sessions.crn=classes.crn;");


	out.println("<br><b>Search:</b><br>");
	out.println("<form method='post' action='"+self+"'>");
	out.println("<select name='helptype'><option value='Select'>Select...</option><option value='SI'>SI</option><option value='helproom/peer tutoring'>Help Room</option><option value='writing tutor'>Writing Tutor</option><option value='none'>None</option>");
	out.println("<input type='text' name='person' value='Person'></input>");
	out.println("<input type='text' name='class' value='Class'></input>");
	out.println("<input type='text' name='date' value='Date (yyyy-mm-dd)'></input>");
	out.println("<input type='submit' name='submit' value='Go'></input></form><br>");

	ResultSet rs = getQuery(con, req, out);
	out.println("<p><b>All Sessions:</p></b>");

	out.println("<table border='1'>");
	out.println("<tr><th>vid</th><th>Tutor ID</th><th>Name</th><th>CRN</th><th>Class</th><th>Type</th><th>Start</th><th>Length</th><th>Status</th></tr>");
	while(rs.next()){
	    String vid = rs.getString("vid");
	    String tid = rs.getString("tid");
	    String name = rs.getString("studname");
	    String crn = rs.getString("crn");
	    String cname = rs.getString("className");
	    String vtype = rs.getString("vtype");
	    String start = rs.getString("entertime");
	    String length = rs.getString("howlong");
	    String status = rs.getString("status");
	    out.println("<tr><td>"+vid+"</td><td>"+tid+"</td><td>"+name+"</td><td>"+crn+"</td><td>"+cname+"</td><td>"+vtype+"</td><td>"+start+"</td><td>"+length+"</td><td>"+status+"</td></tr>");
	}
	out.println("</table>");
	out.println("<br>");
	
                   
    }

    private ResultSet  getQuery(Connection con, HttpServletRequest req, PrintWriter out) throws SQLException {
	String type = req.getParameter("helptype");
	String person = req.getParameter("person");
	String classname = req.getParameter("class");
	String date = req.getParameter("date");
	/*	out.println(type);
	out.println(person);
	out.println(classname);
	out.println(date);*/
	boolean entertype = false; 
	boolean enterperson = false;
	boolean enterclass = false; 
	boolean enterdate = false; 
	int querycount = 0; 
	String querystring = "select vid, sessions.tid, studname, sessions.crn, className, vtype, entertime, howlong, status from sessions, person, classes where sessions.tid=person.bid and sessions.crn=classes.crn"; 
	if(type!=null){
	    if(!(type.equals("Select")||type.equals(""))){
		querycount++; 
		querystring = querystring + " and vtype = ?";
		entertype = true; 
	    }
	}
	if(person!=null){
	    if(!(person.equals("Person")||person.equals(""))){
		querycount++;
		querystring = querystring + " and studname like ?";
		enterperson = true; 
	    }
	}
	if(classname!=null){
	    if(!(classname.equals("Class")||classname.equals(""))){
		querycount++;
		querystring = querystring + " and className like ?";
		enterclass = true; 
	    }
	}
	if(date!=null){
	    if(!(date.equals("Date (yyyy-mm-dd)")||date.equals(""))){
		querycount++; 
		querystring = querystring + " and entertime like ?";
		enterdate = true; 
	    }
	}
	//	out.println(querystring);
	PreparedStatement query = con.prepareStatement(querystring+";");
	
	if(enterdate){
	    query.setString(querycount, "%"+date+"%"); 
	    querycount--; 
	}
	if(enterclass){
	    query.setString(querycount, "%"+classname+"%"); 
	    querycount--; 
	}
	if(enterperson){
	    // out.println("count"+querycount);
	    //out.println("person"+person); 
	    query.setString(querycount, "%"+person+"%"); 
	    querycount--; 
	}
	if(entertype){
	    //    out.println("count"+querycount);
	    //out.println("type"+type); 
	    query.setString(querycount, type);
	    querycount--;
	}
	//	out.println(querystring); 
	    
	ResultSet rs = query.executeQuery(); 
	return rs; 
//	PreparedStatement query = con.prepareStatement();
    }
    
    private void cancelSessions(PrintWriter out, Connection con, String vid) throws SQLException{
	try{ 
	    //	out.println("in cancel Sessions");
	PreparedStatement query = con.prepareStatement("delete from sessions where vid=?");
	query.setString(1, vid);
	query.executeUpdate();

	//	out.println("vid"+vid);
	//	out.println("session cancelled");
	} catch (Exception e){
	    //  out.println("vid: "+ vid);
	   

	}
	
    }

    private void printSessionList(PrintWriter out, Connection con, String self, boolean deleted, String canceled) 
	throws SQLException
    {
	//grabbing the max vid and then incrementing to create the unique vid
	PreparedStatement visitQuery= con.prepareStatement("select max(vid) from sessions;");
	ResultSet vidrow= visitQuery.executeQuery();

	String vid= String.valueOf((Math.random() * (10000 - 10)) + 10);
	if (vidrow.next()){
	    //	    if(deleted)
	    //	vid = String.valueOf(Integer.parseInt(vidrow.getString("max(vid)"))+2);
	    // else
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
	//	String canceled = cancel; 
	out.println("<input type='hidden' name='canceled' value='"+canceled+"'>"+
		    "<input type='submit' name='submit' value='Go'></form>");
    
    }

    private void logout(PrintWriter out){
	out.println("<form method='post' action='LogoutServlet'>"+
		    "<input type='submit' name='logout' value='Logout'></form>");
	
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


