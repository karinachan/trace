/* PickClass.java
Vivienne Shaw and Karina Chan

Homepage upon log in. Admins are allowed to search through the database, create sessions, and delete sessions. 
Tutors can see their sessions and open a new one.  

*/


import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils; // To do the string escaping

public class PickClass extends HttpServlet
{
    //escapes strings for security 
    private static String escape(String raw) {
        return StringEscapeUtils.escapeHtml(raw);
    }

    protected synchronized void doRequest(HttpServletRequest req, HttpServletResponse res)
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
	   
	    //in case the tutor canceled the session in SessionVisits
	    String button = req.getParameter("cancel");
	    String canceled = req.getParameter("canceled");
	    String tempvid = req.getParameter("hiddenvid");
	   
	    String logbid= session.getAttribute("logbid").toString();
	    String sessionID = session.toString(); //should be null if it doesn't exist

	      if (sessionID!=null){ 
	    
		  //	checkCookies(cookies);
		  String name = getName(con,logbid);
		  boolean deleted = false;
		  out.println("<h2>Welcome "+name+".</h2>");
	
		  //Delete session from database if tutor canceled it 
		  if(button!=null){
		      if (button.equals("Cancel Session")){
			  cancelSessions(out,con,tempvid);
			  deleted = true; 
		      }
		  }

		  //Check if user is admin
		  PreparedStatement typequery = con.prepareStatement("select ptype from person where bid=?");
		  typequery.setString(1, logbid);
		  ResultSet rs = typequery.executeQuery();
		  String ptype = ""; 	       
		  if(rs.next())
		      ptype = rs.getString("ptype");
		  
		  //Display admin page, else display tutor page 
		  if(ptype!=null){
		      if(ptype.equals("admin")){
			  adminSearch(out, con, self, req);
		      }
		  }
		  else{
		      printSessionList(out, con, self, deleted, canceled,logbid);
		  }

		  //logout button
		  logout(out);
	      } 
	      else {
		  //redirect to login page 
		  RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		  out.println("<font color=red>Access Denied. Please log in.</font>");
		  
		  rd.include(req, res);
	      }
	}
	catch (SQLException e) {
	    out.println("Error: " + e);
	}
	catch (Exception e) {
	    //redirect to login page 
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
    
    //returns url 
    private String getUrl(HttpServletRequest req) {
	String reqUrl = req.getRequestURL().toString();
	String queryString = req.getQueryString();  
	if (queryString != null) {
	    reqUrl += "?"+queryString;
	}
	return reqUrl;
    }
        

    //Allows admins to create sessions, search records, and delete sessions
    private void adminSearch(PrintWriter out, Connection con, String self, HttpServletRequest req) throws SQLException {

	Statement query = con.createStatement();
	String searchurl = getUrl(req);
       
	//create session forms 
	out.println("<form method='post'><b>Create Session:</b> <br>"+
		    "Class CRN: <input type='text' name='addcrn' value=''></input><br>"+
		    "<input type='submit' name='addsession' value='New'></input></form><br>");

	String add = req.getParameter("addsession");
	String addcrn = escape(req.getParameter("addcrn")); 
	if(add!=null && addcrn!=null){
	    addSessionForm(con, req, out, self, addcrn); 
	}
	String update = req.getParameter("submit");
	if(update!=null){
	    if(update.equals("Add Session")){
		addSession(con, req, out); 
	    }
	}
		   
	//Session search. Can search by type, name, class, date or any combination of the 4. 
	out.println("<br><b>Search: (Click Go for all sessions) </b><br>");
	out.println("<form method='get' action='"+self+"'>");
	out.println("<select name='helptype'><option value='Select'>Select...</option><option value='SI'>SI</option><option value='helproom/peer tutoring'>Help Room</option><option value='writing tutor'>Writing Tutor</option><option value='none'>None</option>");
	out.println("<input type='text' name='person' value='Person'></input>");
	out.println("<input type='text' name='class' value='Class'></input>");
	out.println("<input type='text' name='date' value='Date (yyyy-mm-dd)'></input>");
	out.println("<input type='submit' name='submit' value='Go'></input></form><br>");
   
	//Deletes session if delete button is pushed. 
	if(update!=null){

	    if(update.equals("Delete Session")){
		String delete= req.getParameter("editvid"); 
		PreparedStatement deletequery1 = con.prepareStatement("Delete from visiting where vid=?");
		deletequery1.setString(1, delete); 
		deletequery1.executeUpdate();
		PreparedStatement deletequery2 = con.prepareStatement("Delete from sessions where vid=?");
		deletequery2.setString(1, delete);
		deletequery2.executeUpdate();
		out.println("<br>Session " + delete + " deleted.<br>");
	    }
	}

	//Runs search if search button is pushed. 
	if(update!=null){
	    
	    if(update.equals("Go")){
	    
		ResultSet rs = getQuery(con, req, out);
		
		//Export button form 
		out.println("<p><b>Sessions:</p></b>");		
		out.println("<form action='http://cs.wellesley.edu/~trace/cgi-bin/exporttable.cgi' method='get'>");
		out.println("<input type='hidden' name='searchurl' value='"+searchurl+"'>");
		out.println("Export Records: <input type='submit' name='exportfile' value='Export'>");
		out.println("</form><br>");

		//Records table 
		out.println("<table border='1'>");
		out.println("<tr><th>vid</th><th>Tutor</th><th>CRN</th><th>Class</th><th>Type</th><th>Students</th><th>Start</th><th>Status</th></tr>");
		while(rs.next()){
		    String vid = rs.getString("vid");
		    String tutor = rs.getString("tutor");
		    String name = rs.getString("studname");
		    String crn = rs.getString("crn");
		    String cname = rs.getString("className");
		    String vtype = rs.getString("vtype");
		    String start = rs.getString("entertime");
		    String status = rs.getString("status");
		    out.println("<tr><td>"+vid+"</td><td>"+tutor+"</td><td>"+crn+"</td><td>"+
				cname+"</td><td>"+vtype+"</td><td>"+name+"</td><td>"+start+"</td><td>"+status+"</td><td>");

		    //delete button 
		    out.println("<form method='post' action='"+self+"'><input type='hidden' name='editvid' value='"+vid+"'>"+		      
			    "<input type='submit' name='submit' value='Delete Session'></form></td></tr>");
		}

		out.println("</table>");
		out.println("<br>");
	    }
	}	
    }

    //Allows admin to enter students, type, and date for a new help room session based on crn 
    private void addSessionForm(Connection con, HttpServletRequest req, PrintWriter out, String self, String addcrn) throws SQLException {

	//Get Tutor information from crn 
	PreparedStatement tutorquery = con.prepareStatement("select studname from person, tutors where tutors.bid=person.bid and tutors.crn=?");
	tutorquery.setString(1, addcrn); 
	ResultSet trs = tutorquery.executeQuery(); 
	if(trs.next()){
	    out.println("Tutor: " + trs.getString("studname")+"<br>"); 
	}
	
	//Get students in class that can be logged in 
	PreparedStatement query = con.prepareStatement("select person.bid, studname from taking, person where taking.crn=? and person.bid=taking.bid");
	query.setString(1, addcrn); 
	ResultSet rs = query.executeQuery(); 

	//Form for creating a new session. 
	out.println("<form method='post' action='"+self+"'>");
	out.println("<input type='hidden' name='addcrn' value='"+addcrn+"'></input>");
	out.println("<br>Type: <select name='helptype'><option value='Select'>Select...</option><option value='SI'>SI</option><option value='helproom/peer tutoring'>Help Room</option><option value='writing tutor'>Writing Tutor</option><option value='none'>None</option></select><br>");
	out.println("Students: <br><br>");
	//Create checkboxes for students in class 
	while(rs.next()){
	    String tempname = rs.getString("studname"); 
	    String tempbid = rs.getString("bid"); 
	    out.println("<input type='checkbox' name='student' value='"+tempbid+"'>"+tempname+"</input><br>");
	}
	out.println("<br>Date: <input type='text' name='date' value='(yyyy-mm-dd)'></input><br>");
	out.println("<br><input type='submit' name='submit' value='Add Session'></input></form><br>");	
     
    }

    //Gets information from addSessionForm and updates database 
    private void addSession(Connection con, HttpServletRequest req, PrintWriter out) throws SQLException{
	String type = req.getParameter("helptype"); 
	String addcrn = req.getParameter("addcrn");
	String date = escape(req.getParameter("date")); 
	String[] names = req.getParameterValues("student");
       
	//Get tutor bid 
	PreparedStatement tidinfo = con.prepareStatement("select bid from tutors where crn=?"); 
	tidinfo.setString(1, addcrn); 
	ResultSet ts = tidinfo.executeQuery();
	String tid = ""; 
	if(ts.next())
	    tid = ts.getString("bid");

	//get max vid and add one, for the new session 
	PreparedStatement getvid = con.prepareStatement("select max(vid) from sessions"); 
	ResultSet rs = getvid.executeQuery(); 
        String vid = ""; 
	if(rs.next()){
	    int vidnum = Integer.parseInt(rs.getString("max(vid)"))+1;
	    vid = Integer.toString(vidnum); 
	}

	//insert data into table sessions 
	PreparedStatement addsess = con.prepareStatement("INSERT into sessions VALUES(?, ?, ?, ?, 2, 'closed')"); 
	addsess.setString(1, vid); 
	addsess.setString(2, tid); 
	addsess.setString(3, addcrn); 
	addsess.setString(4, date); 
	addsess.executeUpdate(); 
	
	//insert student data into table visiting
	PreparedStatement addstudent = con.prepareStatement("INSERT into visiting VALUES(?, ? , ?)"); 
	//loop through names array, since we used checkboxes 
	for(int i=0; i<names.length; i++){
	    String bid = names[i];  
	    addstudent.setString(1, bid); 
	    addstudent.setString(2, vid); 
	    addstudent.setString(3, date); 
	    addstudent.executeUpdate(); 
	}
 
	out.println("<br>Database updated with new session " + vid + ".<br>");    
    }

    //Write the admin search query based on entered search terms 
    private ResultSet getQuery(Connection con, HttpServletRequest req, PrintWriter out) throws SQLException {
	String type = req.getParameter("helptype");
	//escape to prevent XSS attacks
	String person = escape(req.getParameter("person"));
	String classname = escape(req.getParameter("class"));
	String date = escape(req.getParameter("date"));
	
	//to keep track of which search terms are used 
	boolean entertype = false; 
	boolean enterperson = false;
	boolean enterclass = false; 
	boolean enterdate = false; 

	//to help prepare statements with correct index
	int querycount = 0; 
	
	//initial query 
	String querystring = "select sessions.vid, A.studname as 'tutor' , sessions.crn, className, B.studname, vtype, entertime, status from sessions, person A, person B, classes, visiting where sessions.tid=A.bid and sessions.crn=classes.crn and sessions.vid=visiting.vid and B.bid=visiting.bid"; 

	//get data from form and modify query if necessary 
	if(type!=null){
	    if(!(type.equals("Select")||type.equals(""))){
		querycount++; 
		querystring = querystring + " and vtype = ?";
		entertype = true; 
	    }
	}
	if(person!=null){
	    if(!(person.equals("Person")||person.equals(""))){
		//Can search by tutor name or student name 
		querycount = querycount+2; 
		querystring = querystring + " and (A.studname like ? OR B.studname like ?)";
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
      
	PreparedStatement query = con.prepareStatement(querystring+";");

	//Set variables in prepared statement 
	if(enterdate){
	    query.setString(querycount, "%"+date+"%"); 
	    querycount--; 
	}
	if(enterclass){
	    query.setString(querycount, "%"+classname+"%"); 
	    querycount--; 
	}
	if(enterperson){
	    query.setString(querycount, "%"+person+"%"); 
	    querycount--; 
	    query.setString(querycount, "%"+person+"%");
	    querycount--;
	}
	if(entertype){
	    query.setString(querycount, type);
	    querycount--;
	} 
	    
	ResultSet rs = query.executeQuery(); 
	return rs; 
    }
    
    
    //If tutor cancels the session on the SessionVisits page, delete session from database. 
    private void cancelSessions(PrintWriter out, Connection con, String vid) throws SQLException{
	try{ 
	    PreparedStatement query = con.prepareStatement("delete from sessions where vid=?");
	    query.setString(1, vid);
	    query.executeUpdate();

	} catch (Exception e){
	   
	    
	}
	
    }

    //Prints out available sessions for tutor to enter 
    private void printSessionList(PrintWriter out, Connection con, String self, boolean deleted, String canceled, String logbid) 
	throws SQLException
    {
	//Grab the max vid and then increment to create the unique vid
	PreparedStatement visitQuery= con.prepareStatement("select max(vid) from sessions;");
	ResultSet vidrow= visitQuery.executeQuery();
	String vid = "";
	if (vidrow.next()){
	    vid= String.valueOf(Integer.parseInt(vidrow.getString("max(vid)"))+1);
	}

	//Get class crn and name to print out 
	PreparedStatement sessionsQuery= con.prepareStatement("select classes.crn, className from tutors, classes where classes.crn=tutors.crn and bid=?;");
	int logbidint= Integer.parseInt(logbid);
	sessionsQuery.setString(1, logbid);  
	ResultSet results = sessionsQuery.executeQuery(); 

	//print out list as dropdown 
	out.println("<p>Choose your session.</p>");
	out.println("<form method='post' action='SessionVisits'>"); 
	out.println("<p><select name='crn'>"); 	

	while(results.next()){

	    String crn = results.getString("crn");
	    String cname = results.getString("className");
	    out.println("<option name='crn' value='"+crn+vid+"'>"+cname+"</option>");
	}

	out.println("</select>");
       
	out.println("<input type='hidden' name='canceled' value='"+canceled+"'>"+
		    "<input type='submit' name='submit' value='Go'></form>");
    
    }

    //Logout button 
    private void logout(PrintWriter out){
	out.println("<form method='post' action='LogoutServlet'>"+
		    "<input type='submit' name='logout' value='Logout'></form>");
    }

    //returns name of student 
    private String getName(Connection con, String logbid) 
	throws SQLException
    {
	PreparedStatement query = con.prepareStatement("select studname from person where bid=?");
	query.setString(1, logbid); 
	ResultSet results = query.executeQuery();
	String name = ""; 
	if(results.next())
	    name = results.getString("studname");
	return name; 
    }
	
    //html page header 
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


