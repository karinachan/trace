import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
 
/**
 * Servlet implementation class LoginServlet
 */

public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L; //don't really know what this means
    // private final String userID = "admin";
    //   private final String password = "password";
    
    private String encrypted="";
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
 
        // get request parameters for userID and password
	//change this to grab studentBID entries from the database..
        String user = request.getParameter("user");
        String pwd = request.getParameter("pwd");
	String logbid= null;
        PrintWriter out=response.getWriter();
	Connection con=null;
	    try{ 


		con=TraceDB.connect("trace_db");
		boolean validate = false;
		PreparedStatement sessionUsers= con.prepareStatement("select user, cryp, password(?),bid from login_user where user=?");
		sessionUsers.setString(1, pwd);
		sessionUsers.setString(2, user);
		ResultSet results = sessionUsers.executeQuery();
		if(results.next()){
		    String pass1 = results.getString("cryp");
		    String pass2 = results.getString("password('"+ pwd + "')");
		    logbid= Integer.toString(results.getInt("bid"));
		    // out.println(logbid);
		    if(pass1.equals(pass2)){
			validate = true; 
			encrypted=pass2;
		}
		}
		
        if(validate){
            HttpSession session = request.getSession();
            session.setAttribute("user", "Swag");
            //setting session to expire in 30 mins
            session.setMaxInactiveInterval(10*60); 
            Cookie userName = new Cookie("user", user);
	    Cookie pwdCook= new Cookie("pwd", encrypted);
	    Cookie bidCook= new Cookie("bid", logbid);
            response.addCookie(userName);
	    response.addCookie(pwdCook);
	    response.addCookie(bidCook);

            //Get the encoded URL string
            String encodedURL = response.encodeRedirectURL("http://cs.wellesley.edu:8080/trace/servlet/PickClass"); //this part isn't working...we have a loginsuccess.jsp page but i don't know how to run it. also, i think the sessions got fucked up with the log in part. also as of now there is no way to verify if someone was logged in for each individual sesssion visits or class pick page.....UGH this is really frustrating. 
            response.sendRedirect(encodedURL);
        }else{
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    //     PrintWriter out= response.getWriter();
            out.println("<font color=red>Either user name or password is wrong.</font>");
            rd.include(request, response);
        }}
	catch (SQLException e) {
	    out.println("Error: " + e);
	}
	catch (Exception e) {
	    RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    out.println("<font color=red>Access Denied. Please log in.</font>");
	    
            rd.include(request, response);
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
    }
	
 
      
 
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doPost(req,res);
    }
}