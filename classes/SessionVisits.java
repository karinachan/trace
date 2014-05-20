/*SessionVisits.java
Vivienne Shaw and Karina CHan 

SessionVisits allows tutors to log students into the sessions. They can also cancel existing sessions. 

*/

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
  

public class SessionVisits extends HttpServlet
{

    private final String SHOW_BUTTON = "show visits";
    private final String BN_INPUT = "bn";


    protected synchronized void doRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
		
	HttpSession session = req.getSession(true);
        String CRN= "";
	res.setContentType("text/html");
	res.setHeader("pragma", "no-cache");
	PrintWriter out = res.getWriter();
    
      
	String self = res.encodeURL(req.getRequestURI());

	//Hashmaps to record list of students that are not logged in and students that are. 
        HashMap<String,String> studentlist =(HashMap<String,String>) session.getAttribute("students_notlogged");
	HashMap<String,String> inSession = (HashMap<String,String>) session.getAttribute("students_loggedin");

	//Reset hashmaps 
	if( inSession == null ) {
	    inSession = new HashMap<String,String>();
	    session.setAttribute("students_loggedin",inSession);
	}
	if (studentlist == null) {
	    studentlist= new HashMap<String,String>();
	    session.setAttribute("students_notlogged",studentlist);
	}
	
	//create new vid if previous session was canceled based on previous vid. 
	//vid is parsed out of crn 
	String canceledparam = req.getParameter("canceled");
	Object vid1=session.getAttribute("vid");
	CRN = req.getParameter("crn");
	String vid = CRN.substring(5);
	session.setAttribute("vid", vid);

	CRN = CRN.substring(0,5);
    
	if(canceledparam!=null){
	    if(canceledparam.equals("true")){
		int v1 = Integer.parseInt(vid1.toString());
		v1 = v1 - 1; 
		vid1 = Integer.toString(v1);
	    }
	}

	Boolean loaded= (Boolean) session.getAttribute("loaded");
	if (loaded == null) {
	    loaded= false;
	    session.setAttribute("loaded",true);
	}
	try {
	    if(!vid1.toString().equals(vid) || vid1== null){
	    loaded=false;
	    session.setAttribute("loaded",false);
	    clearList(inSession,studentlist, out);
	}
	} catch (Exception e) {
	    
	}

	Connection con = null;
	boolean verified= false;
	

	try {
	    pageheader(out,"Session Visits");
	
	    con = TraceDB.connect("trace_db");

	    Cookie [] cookies= req.getCookies();
	    String logbid= session.getAttribute("logbid").toString();
	    String sessionID= session.toString();
	  
	    if (sessionID==null) {
	
		RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		out.println("<font color=red>Access Denied. Please log in.</font>");
		rd.include(req, res);
		}
	    else {
		//	checkCookies(cookies, session, out, con, req, res, CRN);
		verified= true;
		
	    }

	    printWelcome(session,out, con, CRN);
          
	    String bn = req.getParameter(BN_INPUT);
	    String stuName = req.getParameter("title"); 
	    //for canceling students out of logged in list 
	    String x = req.getParameter("x");
	    String button = req.getParameter("update");
	  
	  
          if(loaded==false && verified && !CRN.equals("")){ //if you've visited the page and you haven't loaded the tree, load it
	      session.setAttribute("loaded",true); //so loaded changes
	      loaded=true;

	      //Get student list
	      PreparedStatement query = con.prepareStatement("select studname, person.bid from taking, person where crn=? and person.bid=taking.bid;");
	      query.setString(1, CRN);
	      ResultSet result = query.executeQuery();
            
	      //Add students to hashmap studentlist 
	      while(result.next()){
		  String student = result.getString("studname");
		  String bid = result.getString("bid");
		  studentlist.put(bid, student);
	      }
	      PreparedStatement visitINSERT= con.prepareStatement("insert into sessions(vid,tid,crn,entertime,howlong,status) values (?,?,?,now(),2,?);"); 
	      visitINSERT.setString(1,vid);
	      visitINSERT.setString(2,logbid);
	      visitINSERT.setString(3,CRN);
	      visitINSERT.setString(4,"in progress");
	      visitINSERT.executeUpdate();
          }
          
          //if they press x to remove someone from the insession list
          if (x!=null){
	      inSession.remove(bn);
	      studentlist.put(bn, stuName);
          }
	   
	  //update database if tutor clicks "submit" 
	  if(button!=null){
	      if(button.equals("Submit")){
       		  updateList(con, vid, inSession, studentlist, out,bn,req,res);
	      } 
	  }
	  else{
	      //add students to logged in list 
	      addtolist(con, inSession, studentlist, out,bn,stuName, x, req);
	      //print list and form
	      processShowinSession(req,out,self,inSession, CRN, vid);
	      printforms(out,con,self, CRN, vid, studentlist);
	  }

	  //Submit form 
       	  out.println("<form class='pure-form' method='post' action='"+self+"'>"+
		      "<input type='hidden' name='crn' value='"+CRN+vid+"'>"+
	  	      "<input class='pure-button' type='submit' name='update' value='Submit'></form>");

	  boolean canceled = true; 
	 
	  //Cancel Session form. Redirects to pick class with values to notify a session was canceled 
	  out.println("<form method='post' action='PickClass'>"+
		      "<input type='hidden' name='hiddenvid' value='"+vid+"'>"+
		      "<input type='hidden' name='canceled' value='"+canceled+"'>"+
		      "<input type='submit' name='cancel' value='Cancel Session'></form>");
		     
        }
        catch (SQLException e) {
	    out.println("Error: " + e);         
        }
        catch (Exception e) {
	    //redirect to login page 
	    RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    out.println("<font color=red>Hi. Access Denied. Please login.</font>");
	    rd.include(req, res);
        }
        finally {
	    if( con != null ) {
		try {
		    con.close();
		}
		catch( Exception e ) {
		    //	    out.println("the last one");
		    e.printStackTrace(out);
		}
	    }
        }

	
        out.println("</body></html>");
    }
    /*      
    private void checkCookies(Cookie [] cookies, HttpSession session, PrintWriter out, Connection con, HttpServletRequest req, HttpServletResponse res, String CRN){
	

	//	out.println("in checkCookie"); 
	for (Cookie cookie: cookies){
		   
	    if (cookie.getName().equals("crn")){
		CRN=cookie.getValue();
		//  out.println("crnpick"+CRN);
	    }
	 
	}
    }
	
    */
      
    //Print session information at the top of the page 
    private void printWelcome(HttpSession session, PrintWriter out, Connection con, String CRN) throws SQLException {
	//get session information 
	PreparedStatement query = con.prepareStatement("SELECT tid, crn, howlong from sessions where crn=?");
	query.setString(1, CRN);
	ResultSet rs = query.executeQuery();
	if(rs.next()){
	    int tid = rs.getInt("tid");
	    int crn = rs.getInt("crn");	     
	    int length = rs.getInt("howlong");
	    //get class information 
	    PreparedStatement classquery = con.prepareStatement("SELECT className, vtype from classes where crn=?");
	    classquery.setInt(1,crn);
	    ResultSet classrs = classquery.executeQuery();
	    if(classrs.next()){
		String className = classrs.getString("className");
		String type = classrs.getString("vtype");
		//get tutor information 
		PreparedStatement studentquery = con.prepareStatement("SELECT studname from person where bid=?");
		studentquery.setInt(1, tid);
		ResultSet studrs = studentquery.executeQuery();
		if(studrs.next()){
		    String studname = studrs.getString("studname");
		    out.println("<h1>"+className+" "+type+"<h1>");
		    out.println("<h2>" + "Length: " + length + " hours" + "     " + "Tutor: " + studname+"</h2>");
		}
	    }
	}
	  
         
      }

    //Clear all student list hashmaps 
    private void clearList(HashMap<String, String> loggedin, HashMap<String, String> studentlist, PrintWriter out){
	loggedin.clear();
	studentlist.clear();
	}
          
    //adds students to "logged in" list and removes from student list 
    private void addtolist(Connection con, HashMap<String,String> loggedin, HashMap<String,String> studentlist, PrintWriter out, String bn, String stuName, String x, HttpServletRequest req) throws SQLException{
       
	  if( bn!=null && x==null) { //if B number exists and x button hasn't been pressed 
	    
	    loggedin.put(bn,stuName);

	    try{
		studentlist.remove(bn);
	    } 
	    catch (Exception e){
	       
	    }
	   
	  }
	 
    }

    //Updates database with new session informaiton 
    private void updateList(Connection con, String vid, HashMap<String,String> loggedin, HashMap<String,String> studentlist, PrintWriter out, String bn, HttpServletRequest req, HttpServletResponse res) throws SQLException{
	
	//iterate through hashmap of logged in students 
	Set keys = loggedin.keySet();
	Iterator it= keys.iterator();
	try{
	    while(it.hasNext()){
		String key = (String) it.next();
		PreparedStatement query = con.prepareStatement("INSERT into visiting (bid, vid) VALUES(?, ?);");  
		query.setString(1, key); //trying to pass through 
		query.setString(2, vid); 
		query.executeUpdate();
		out.println("Database updated!");
		
		/*	PreparedStatement closeshop= con.prepareStatement("UPDATE sessions SET status='closed' WHERE vid=?");
		closeshop.setString(1, vid);
		closeshop.executeUpdate();
		out.println("Session closed");*/
	    }
	    PreparedStatement closeshop= con.prepareStatement("UPDATE sessions SET status='closed' WHERE vid=?");
	    closeshop.setString(1, vid);
	    closeshop.executeUpdate();
	    out.println("Session closed");
	    
	    //redirect to confirmation page  
	    res.sendRedirect("http://cs.wellesley.edu:8080/trace/servlet/ConfirmSubmit");
	   
	}
	catch (Exception e){
	    out.println(e);	  
	}      
      }
      
    //show logged in students 
    private void processShowinSession(HttpServletRequest req, PrintWriter out,String self,HashMap<String,String> loggedin, String CRN, String vid) {
	showlogged(out,loggedin, self, CRN, vid); 
      }
      
    //show logged in students and add X button to cancel them  
    private void showlogged(PrintWriter out, HashMap<String,String> loggedin, String self, String CRN, String vid) {
	out.println("<p>Logged in students: ");
	Set keys = loggedin.keySet();
	Iterator it = keys.iterator();
	out.println("<ul>");
	while (it.hasNext()) {
	    String key = (String) it.next();
	    out.println("<form class='pure-form' method='post' action='"+self+"'>"+
			"<input type='hidden' name='crn' value='"+CRN+vid+"'>"+
			"<input type='hidden' name='"+BN_INPUT+"' value='"+key+"'>"+
			"<input type='hidden' name='title' value='"+(loggedin.get(key))+"'>"+
			
			"<li><input class='pure-button' type='submit' name='x' value='x'>" +(loggedin.get(key)) + "</form>");
	}
	out.println("</ul></p>");
    }
      
    //print list of students in class not yet logged in 
    private void printforms(PrintWriter out, Connection con, String self, String CRN, String vid, HashMap<String,String> studentlist)
      throws SQLException
      {    
	  //iterate through list and add name with log in button to log them in 
	  Set keys = studentlist.keySet();
	  Iterator it= keys.iterator();
	 
	  out.println("<br>");
	  out.println("<p>Class List:</p>");
	  out.println("<ul>");
	  while (it.hasNext()) {
       
	      String key = (String) it.next();
  
	      out.println("<form class='pure-form' method='post' action='"+self+"'>"+
			  "<input type='hidden' name='crn' value='"+CRN+vid+"'>"+
		      	  "<input type='hidden' name='"+BN_INPUT+"' value='"+key+"'>"+
			  "<input type='hidden' name='title' value='"+(studentlist.get(key))+"'>"+ 
			 
			  "<li><input class='pure-button' type='submit' value='Log in '> " +(studentlist.get(key))+"</form>");
	  }
	  out.println("</ul>"); 	
      }


    //HTML page header
    private void pageheader(PrintWriter out, String title) {
	out.println("<!doctype html>\n"
		    + "<html lang='en'>\n"
		    + "<head>\n"
		    + "<title>"+ title + "</title>\n"
		    + "<meta charset='utf-8'>\n"
		    + "<link rel='stylesheet' type='text/css' href='../css/webdb-style.css'>\n"
		    +"<link rel='stylesheet' href='http://yui.yahooapis.com/pure/0.4.2/pure-min.css'>"
		    +"<script type='text/javascript' src='http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.js'></script>"
		    + "</head>\n");
	/*
	    +"<script language='JavaScript'> window.onbeforeunload = confirmExit; function confirmExit() {" 
		    +"return 'You have attempted to leave this page. Are you sure?';} </script>"  
	*/
      }
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	try{ 
	    doRequest(req,res);
	} catch (Exception e) { 
	    
	    res.sendRedirect("http://cs.wellesley.edu:8080/trace/servlet/PickClass");
	}
	
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	
	doRequest(req, res); 
	
    }
    
}
