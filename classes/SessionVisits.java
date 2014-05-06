/*SessionVisits.java*/
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
    private final String userID = "admin";
    private final String password = "password";
    private final String SHOW_BUTTON = "show visits";
    private final String BN_INPUT = "bn"; //change this to B numbers
      private boolean loaded= false; //whether initial hashmap has been created(not loaded here)
      private String vid="";
    private String CRN ="";
    private HashMap<String,String> studentlist = new HashMap<String,String>();

      //vid not saving from req to req (logging in and out students) 
      //also, some mysql error
      //at some point we have to find another way to pass variables from page to page without hidden (you can literally 
      //just steal the password rn

    protected void doRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
	HttpSession session = req.getSession(true);
	res.setContentType("text/html");
	res.setHeader("pragma", "no-cache");
	PrintWriter out = res.getWriter();
	
	int visits = updateVisits(session, out);
	String self = res.encodeURL(req.getRequestURI());
      
	HashMap<String,String> inSession = (HashMap<String,String>) session.getAttribute("students_loggedin");
	if( inSession == null ) {
	    inSession = new HashMap<String,String>();
	    session.setAttribute("students_loggedin",inSession);
	}
      
	CRN = req.getParameter("crn"); //should be the class number
	out.println(CRN);
	vid= req.getParameter("vid"); 
	out.println(vid);

	Connection con = null;
	String userName = null;
	String sessionID = null;

	try {
	    pageheader(out,"Session Visits");
	    con = TraceDB.connect("trace_db"); //need to change this to another one
        
	    Cookie [] cookies= req.getCookies();
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
             
	    printWelcome(session,out, con); //don't know why this doesn't print when we reload the page with button
          
          String bn = req.getParameter(BN_INPUT); //will change to B numbers
          String stuName = req.getParameter("title"); //will change to B numbers
          String x = req.getParameter("x");
       	  String button = req.getParameter("update");
	  out.println("sessionid"+sessionID);
	  out.println("username"+userName);
	  out.println("visits"+visits);
	  out.println("CRN"+ CRN);
          if(visits>0 && loaded==false){ //if you've visited the page and you haven't loaded the tree, load it
	      visits = updateVisits(session, out);
	      loaded=true;
	      PreparedStatement query = con.prepareStatement("select studname, students.bid from taking, students where crn=? and students.bid=taking.bid;");
	      query.setString(1, CRN);
	      ResultSet result = query.executeQuery();
            
	      while(result.next()){
		  String student = result.getString("studname");
		  out.println("student:"+student);
		  String bid = result.getString("bid");
		  out.println("bid:"+bid);
		  studentlist.put(bid, student);
	      }
          }
          
          //if they press x to remove someone from the insession list
          if (x!=null){
	      inSession.remove(bn);
	      studentlist.put(bn, stuName);
          }

	  if(button!=null){
	      if(button.equals("Submit")){
		  out.println("update yes");
		  updateList(con, inSession, out,bn);
	      }
	  }

	  //from the form 
	  	  out.println("the form bn:" + bn);
	  	  out.println("the form stuName: "+ stuName);
     
		  addtolist(con, inSession,out,bn,stuName, x, req); //why why why wrong wrong worng no work 
	  // updateList(con, inSession,out,bn);
          processShowinSession(req,out,self,inSession);
          printforms(out,con,self);
       	  out.println("<form class='pure-form' method='post' action='"+self+"'>"+
		      "<input type='hidden' name='pwd' value='"+password+"'>"+
		      "<input type='hidden' name='user' value='"+userID+"'>"+
		      "<input type='hidden' name='crn' value='"+CRN+"'>"+
		      "<input type='hidden' name='vid' value='"+vid+"'>"+
	  	      "<input class='pure-button' type='submit' name='update' value='Submit'></form>");
	  out.println( "<form class='pure-form' method='post' action='PickClass'>"+
	 	      "<input type='submit' name='back' value='Back'>"+
		      "<input type='hidden' name='pwd' value='"+password+"'>"+
	  	      "<input type='hidden' name='user' value='"+userID+"'></form>");
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
		    out.println("the last one");
		    e.printStackTrace(out);
		}
	    }
        }
        out.println("</body></html>");
    }
      
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
	  PreparedStatement query = con.prepareStatement("SELECT tid, crn, roomnum, length from sessions where crn=?");
	  query.setString(1, CRN);
	  ResultSet rs = query.executeQuery();
	  if(rs.next()){
	      int tid = rs.getInt("tid");
	      int crn = rs.getInt("crn");
	      String room = rs.getString("roomnum");
	      int length = rs.getInt("length");
	      PreparedStatement classquery = con.prepareStatement("SELECT className, vtype from classes where crn=?");
	      classquery.setInt(1,crn);
	      ResultSet classrs = classquery.executeQuery();
	      if(classrs.next()){
		  String className = classrs.getString("className");
		  String type = classrs.getString("vtype");
		  PreparedStatement studentquery = con.prepareStatement("SELECT studname from students where bid=?");
		  studentquery.setInt(1, tid);
		  ResultSet studrs = studentquery.executeQuery();
		  if(studrs.next()){
		      String studname = studrs.getString("studname");
		      out.println("<h1>"+className+" "+type+"<h1>");
		      out.println("<h2>Room: " + room + "     " + "Length: " + length + " hours" + "     " + "Tutor: " + studname+"</h2>");
		  }
	      }
	  }
         
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
		out.println("Carry on");
	    }
	    String button = req.getParameter("submit");
	    
	    /*
	    if(button.equals("update")){
		PreparedStatement visitingquery = con.prepareStatement("Select vid from sessions where crn=?");
		// out.println("crn"+CRN);
		visitingquery.setString(1, CRN);
		ResultSet results = visitingquery.executeQuery();
		String vid = "";
		//	out.println("in");
		if(results.next())
		    vid = results.getString("vid");
		// out.println("vid");
		PreparedStatement query = con.prepareStatement("INSERT into visiting (bid, vid) VALUES(?, ?)");
		query.setString(1, bn);
		query.setString(2, vid);
		query.executeUpdate();
	      	out.println("Database updated.");
               
		} 
	    */
	  }
	  else{
	      out.println("else");
	  }
      }

      private void updateList(Connection con, HashMap<String,String> loggedin, PrintWriter out, String bn) {//throws SQLException{

	  Set keys = loggedin.keySet();
	  Iterator it= keys.iterator();
	  while(it.hasNext()){
	      String key = (String) it.next();
	      out.println("key"+key);
	  

	      /*  PreparedStatement visitingquery = con.prepareStatement("Select vid from sessions where crn=?");
	      visitingquery.setString(1, CRN);
	      ResultSet results = visitingquery.executeQuery();
	      String vid = "";
	      if(results.next())
		  vid = results.getString("vid");
	      */
	      out.println("inside updateList: the key:"+key);
	      out.println("inside updateList: the vid: "+ vid);
	      try{
	      PreparedStatement query = con.prepareStatement("INSERT into visiting (bid, vid) VALUES(?, ?);");
	      
	      query.setString(1, key); //trying to pass through 
	      query.setString(2, vid); //how do we get the vid  of this? as the session id? 
	      query.executeUpdate();
	      out.println("Database updated!");
	       }
	  	  catch (SQLException e){
	     out.println(e);	  
		  }	}	//	} 
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
			  "<input type='hidden' name='"+BN_INPUT+"' value='"+key+"'>"+
			  "<input type='hidden' name='title' value='"+(loggedin.get(key))+"'>"+
			  "<input type='hidden' name='crn' value='"+CRN+"'>"+
			  "<input type='hidden' name='user' value='"+userID+"'>"+
			  "<input type='hidden' name='pwd' value='"+password+"'>"+
			  "<input type='hidden' name='vid' value='"+vid+"'>"+
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
	      String key = (String) it.next();
  
	      out.println("<form class='pure-form' method='post' action='"+self+"'>"+
			  "<input type='hidden' name='"+BN_INPUT+"' value='"+key+"'>"+
			  "<input type='hidden' name='title' value='"+(studentlist.get(key))+"'>"+ "<input type='hidden' name='vid' value='"+vid+"'>"+
			  "<input type='hidden' name='crn' value='"+CRN+"'>"+
			  "<input type='hidden' name='user' value='"+userID+"'>"+
			  "<input type='hidden' name='pwd' value='"+password+"'>"+
			  "<li><input class='pure-button' type='submit' value='Log in '> " +(studentlist.get(key))+"</form>");
	  }
        out.println("</ul>"); 
	//	out.println("<br><br><input type='submit' name='update' value='Submit'>"+
	//	    "<input type='submit' name='back' value='Back'></form>");
      }

      private void pageheader(PrintWriter out, String title) {
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
	  doRequest(req,res);
      }

      protected void doPost(HttpServletRequest req, HttpServletResponse res)
	  throws ServletException, IOException
      {
	  
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
	      
	      doRequest(req, res);
    
	  }
	  else{
	      // out.println("crunch");
	      //  out.println(visits);
	      RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	      //  PrintWriter out= res.getWriter();
	      out.println("<font color=red>Access Denied. Please login.</font>");
	      rd.include(req, res);
	  }
      }
      
  }
