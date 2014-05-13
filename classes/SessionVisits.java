/*SessionVisits.java
 UPDATE VERSION*/
    import java.io.*;
  import javax.servlet.*;
  import javax.servlet.http.*;
  import java.sql.*;
  import java.util.*;
  /*
  Goal: Establish sessions tracking.
  1. Generate the list of people taking classes
  2. When you click on their names then they are logged in
  3. A cookie is stored and the length of their session is inserted
  
  */
public class SessionVisits extends HttpServlet
{
    
    private static final long serialVersionUID = 1L; //don't really know what this means

    private String userID;
    private String password;
    private final String SHOW_BUTTON = "show visits";
    private final String BN_INPUT = "bn"; //change this to B numbers
    private boolean loaded= false; //whether initial hashmap has been created(not loaded here)
    private String vid="";
    private String CRN ="";
    private HashMap<String,String> studentlist = new HashMap<String,String>();
    private String logbid;
    private boolean verified=false; //initially not verified
    private String userName = null;
    private String sessionID = null;


    protected void doRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
	HttpSession session = req.getSession(true);
	res.setContentType("text/html");
	res.setHeader("pragma", "no-cache");
	PrintWriter out = res.getWriter();
	//	out.println("in sessionvisits");
	int visits = updateVisits(session, out);
	String self = res.encodeURL(req.getRequestURI());
	
      
	HashMap<String,String> inSession = (HashMap<String,String>) session.getAttribute("students_loggedin");
	if( inSession == null ) {
	    inSession = new HashMap<String,String>();
	    session.setAttribute("students_loggedin",inSession);
	}
	String canceledparam = req.getParameter("canceled");
	//	out.println("canceled"+canceledparam);
	String vid1=vid;
	//	out.println("vid1"+vid);
	CRN = req.getParameter("crn");
	vid = CRN.substring(5);
	//	out.println("vid"+vid);
	CRN = CRN.substring(0,5);
	
	//	out.println("crn"+CRN);
	if(canceledparam!=null){
	    if(canceledparam.equals("true")){
		//	out.println("incancelparam");
		int v1 = Integer.parseInt(vid1);
		v1 = v1 - 1; 
		vid1 = Integer.toString(v1);
	    }
	}
	
       	if(!vid1.equals(vid)){
	    loaded=false;
	    clearList(inSession, out);
	}

	Connection con = null;
	

	try {
	    pageheader(out,"Session Visits");
	    //  out.println("in try");
	
	    con = TraceDB.connect("trace_db");

	    Cookie [] cookies= req.getCookies();
	    
      	    if (cookies.length<4){ //gonna have to change this.... 
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
			out.println("<font color=red>Access Denied. Please log in.</font>");
	    
		rd.include(req, res);
	    }
	    else {
		verified=checkCookies(cookies, session, out, con, req, res);	    
		
	    }

	    printWelcome(session,out, con);
          
          String bn = req.getParameter(BN_INPUT);
          String stuName = req.getParameter("title"); 
          String x = req.getParameter("x");
       	  String button = req.getParameter("update");
	  /*  out.println("sessionid"+sessionID);
	  out.println("username"+userName);
	  out.println("visits"+visits);
	  out.println("loaded"+loaded);
	  out.println("verified"+verified);
	  out.println("CRN"+ CRN);*/
          if(loaded==false && verified){ //if you've visited the page and you haven't loaded the tree, load it
	      //  out.println("in if visits>0 loaded=false");
	           visits = updateVisits(session, out);
	      loaded=true;
	      PreparedStatement query = con.prepareStatement("select studname, person.bid from taking, person where crn=? and person.bid=taking.bid;");
	      query.setString(1, CRN);
	      ResultSet result = query.executeQuery();
            
	      while(result.next()){
		  String student = result.getString("studname");
		  //  out.println("student:"+student);
		  String bid = result.getString("bid");
		  //	  out.println("bid:"+bid);
		  studentlist.put(bid, student);
	      }
	      // out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	      PreparedStatement visitINSERT= con.prepareStatement("insert into sessions(vid,tid,crn,entertime,howlong,status) values (?,?,?,now(),2,?);"); ///HERE IN INSERT STATEMENT
	      visitINSERT.setString(1,vid);
	      visitINSERT.setString(2,logbid);
	      visitINSERT.setString(3,CRN);
	      visitINSERT.setString(4,"in progress");
	      visitINSERT.executeUpdate();
	      //  out.println("Session generated");
	   
          }
          
          //if they press x to remove someone from the insession list
          if (x!=null){
	      inSession.remove(bn);
	      studentlist.put(bn, stuName);
          }
	  // out.println("button"+button);
	  // out.println("ABOUT TO UPDATE THE LIST THING"+ bn);
	   
	  if(button!=null){
	
	      if(button.equals("Submit")){
		  //	  	  out.println("update yes");
		 
		  updateList(con, inSession, out,bn,req,res);
		 
	      } 
	      
	  }
	  else{

	  
	      addtolist(con, inSession,out,bn,stuName, x, req);  
	
	      processShowinSession(req,out,self,inSession);
	      printforms(out,con,self);
	  }
       	  out.println("<form class='pure-form' method='post' action='"+self+"'>"+
		     
		      "<input type='hidden' name='crn' value='"+CRN+vid+"'>"+
		      
	  	      "<input class='pure-button' type='submit' name='update' value='Submit'></form>");
	  //	  cancelSessions(out, con);
	  /*	  out.println( "<form class='pure-form' method='post' action='PickClass'>"+
		  "<input type='submit' name='back' value='Back'></form>");*/

	  boolean canceled = true; 
	  	out.println("<form method='post' action='PickClass'>"+
			    "<input type='hidden' name='hiddenvid' value='"+vid+"'>"+
			    "<input type='hidden' name='canceled' value='"+canceled+"'>"+
		    "<input type='submit' name='cancel' value='Cancel Session'></form>");
		     
        }
        catch (SQLException e) {

	    out.println("Error: " + e);         
        }
        catch (Exception e) {
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
      
    private boolean checkCookies(Cookie [] cookies, HttpSession session, PrintWriter out, Connection con, HttpServletRequest req, HttpServletResponse res){
	
	boolean working= false;
	//		out.println("in checkCookie"); 
	for (Cookie cookie: cookies){
		    
	    if (cookie!=null){
		if(cookie.getName().equals("user")){
		    userName = cookie.getValue();
		    //	     out.println("username"+userName);
		}
		if(cookie.getName().equals("JSESSIONID")){
		    sessionID = cookie.getValue();
		    // out.println("sessionID"+sessionID);
		}
		if (cookie.getName().equals("bid")){
		    logbid= cookie.getValue();
		    // out.println("logbid"+logbid);
		}
		//do this earlier? 
	       
		if (cookie.getName().equals("pwd")){
		    password=cookie.getValue();
		    //  out.println("password"+password);
		}
		if (cookie.getName().equals("crn")){
		    CRN=cookie.getValue();
		    //  out.println("crnpick"+CRN);
		}
		working=true;	
	    }}



	    return working;
	
    }

  

    /*
  private void logout(PrintWriter out){
	out.println("<form method='post' action='LogoutServlet'>"+
		    "<input type='submit' name='logout' value='Logout'></form>");
	
		    } */
      private int updateVisits(HttpSession session, PrintWriter out) {
	  Integer visits = (Integer)session.getAttribute("visits");
	  if(visits == null){
	      visits = 0;
	  }
	  visits = (Integer) (visits.intValue()+1);
        
	  // Store back in the session
	  session.setAttribute("visits",(Integer) visits);
	  return visits;
      }
      
      private void printWelcome(HttpSession session, PrintWriter out, Connection con) throws SQLException {
	  PreparedStatement query = con.prepareStatement("SELECT tid, crn, howlong from sessions where crn=?");
	  query.setString(1, CRN);
	  ResultSet rs = query.executeQuery();
	  if(rs.next()){
	      int tid = rs.getInt("tid");
	      int crn = rs.getInt("crn");
	     
	      int length = rs.getInt("howlong");
	      PreparedStatement classquery = con.prepareStatement("SELECT className, vtype from classes where crn=?");
	      classquery.setInt(1,crn);
	      ResultSet classrs = classquery.executeQuery();
	      if(classrs.next()){
		  String className = classrs.getString("className");
		  String type = classrs.getString("vtype");
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

    private void clearList(HashMap<String, String> loggedin, PrintWriter out){
	loggedin.clear();
	studentlist.clear();
    }
          
      //adds students to "logged in" list
    private void addtolist(Connection con, HashMap<String,String> loggedin, PrintWriter out, String bn, String stuName, String x, HttpServletRequest req) throws SQLException{
        
	  if( bn!=null && x==null) { //if B number exists and x button hasn't been pressed
	      //is there an issue with this because it only does it once? 
 
	    String Curr = loggedin.get(bn);
	    
	    loggedin.put(bn,stuName);
	    try{
		studentlist.remove(bn);
	    } 
	    catch (Exception e){
		//	out.println("Carry on");
	    }
	    String button = req.getParameter("submit");
	    
	 
	  }
	 
      }

    private void updateList(Connection con, HashMap<String,String> loggedin, PrintWriter out, String bn, HttpServletRequest req, HttpServletResponse res) throws SQLException{

	  Set keys = loggedin.keySet();
	  Iterator it= keys.iterator();
	  while(it.hasNext()){
	      String key = (String) it.next();
	      // out.println("key"+key);
	      

	      //  out.println("inside updateList: the key:"+key);
	      // out.println("inside updateList: the vid: "+ vid);
	    
	      PreparedStatement query = con.prepareStatement("INSERT into visiting (bid, vid) VALUES(?, ?);");
	      
	      query.setString(1, key); //trying to pass through 
	      query.setString(2, vid); //how do we get the vid  of this? as the session id? 
	      query.executeUpdate();
	       out.println("Database updated!");

	      PreparedStatement closeshop= con.prepareStatement("UPDATE sessions SET status='closed' WHERE vid=?");
	      closeshop.setString(1, vid);
	      closeshop.executeUpdate();
	       out.println("Session closed");
	  }
	  try{
	      res.sendRedirect("http://cs.wellesley.edu:8080/trace/servlet/ConfirmSubmit");
	      RequestDispatcher rd = getServletContext().getRequestDispatcher("/servlet/ConfirmSubmit");
	      rd.include(req, res);
	      
	      

	      }
	      catch (Exception e){
		  out.println(e);	  
	      	}	//	} 
      }
      
      private void processShowinSession(HttpServletRequest req, PrintWriter out,String self,HashMap<String,String> loggedin) {
	  showlogged(out,loggedin, self); 
      }
      
      private void showlogged(PrintWriter out, HashMap<String,String> loggedin, String self) {
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
      
      //change this to show the list of kids in each class
      private void printforms(PrintWriter out, Connection con, String self)
      throws SQLException
      {    
	  
	  Set keys = studentlist.keySet();
	  Iterator it= keys.iterator();
	 
	  out.println("<br>");
	  out.println("<p>Class List:</p>");
	  out.println("<ul>");
	  while (it.hasNext()) {
	      //          out.println("in it");
	      String key = (String) it.next();
  
	      out.println("<form class='pure-form' method='post' action='"+self+"'>"+
			  "<input type='hidden' name='crn' value='"+CRN+vid+"'>"+
		      	  "<input type='hidden' name='"+BN_INPUT+"' value='"+key+"'>"+
			  "<input type='hidden' name='title' value='"+(studentlist.get(key))+"'>"+ 
			 
			  "<li><input class='pure-button' type='submit' value='Log in '> " +(studentlist.get(key))+"</form>");
	  }
        out.println("</ul>"); 

      }

      private void pageheader(PrintWriter out, String title) {
	  // out.println("in pageheader");
	  out.println("<!doctype html>\n"
		      + "<html lang='en'>\n"
		      + "<head>\n"
		      + "<title>"+ title + "</title>\n"
		      + "<meta charset='utf-8'>\n"
		      + "<link rel='stylesheet' type='text/css' href='../css/webdb-style.css'>\n"
		      +"<link rel='stylesheet' href='http://yui.yahooapis.com/pure/0.4.2/pure-min.css'>"
		      + "</head>\n");
      }

      protected void doGet(HttpServletRequest req, HttpServletResponse res)
	  throws ServletException, IOException
      {
	  try{ 
	  doRequest(req,res);
	  } catch (Exception e) { //we are hitting a null error here- i think we have to save values we collect
	      //in the doRequest with our initial visit into cookies so that they are saved... 
	      //handling now with a redirect back to the pick class page (this creates incomplete entries though
	      
	  res.sendRedirect("http://cs.wellesley.edu:8080/trace/servlet/PickClass");
	  }

      }

      protected void doPost(HttpServletRequest req, HttpServletResponse res)
	  throws ServletException, IOException
      {

	  doRequest(req, res); 
	
      }
      
  }
