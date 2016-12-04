package acmdb;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import javax.servlet.http.*;
import java.util.Date;
import java.util.HashMap;

public class Order{
	public Order() {
	}
	public int islegalname(String name) throws Exception{
		if (name==null)
		{
			return 0;
		}
		int n=name.length();
		char[] s = name.toCharArray();
		for (int i=0;i<n;i++){
			if (!((s[i]<='9' && s[i]>='0') ||(s[i]<='z' && s[i]>='a')||(s[i]<='Z' && s[i]>='A') ))
			{
				return 0;
			}
		}
		return 1;
	}
	public int isnumber(String num) throws Exception{
		try {
			Integer.parseInt(num);
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}
	public int isfloat(String num) throws Exception{
		try {
			Float.parseFloat(num);
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}
	public String hashkey(String key){
		int n=key.length();
		char[] s = key.toCharArray();
		String res="";
		for (int i=0;i<n;i++)
		{
			char c=(char) (s[i]+3);
			res=res+c;
		}
		return res;
	}
	public String rehashkey(String key){
		int n=key.length();
		char[] s = key.toCharArray();
		String res="";
		for (int i=0;i<n;i++)
		{
			char c=(char) (s[i]-3);
			res=res+c;
		}
		return res;
	}
	public String normal(String S){
		int n=S.length();
		char[] s = S.toCharArray();
		String res="";
		for (int i=0;i<n;i++)
		{
			char c=s[i];
			if (c<='Z' && c>='A' ){
				c=(char)(c-'A'+'a');
			}
			res=res+c;
		}
		return res;
	}
	public int finduser(String login) throws Exception{
		Connector connector=new Connector();
		String query = String.format("select * from Users where login = \'%s\'",login);
		ResultSet results;
		try {
			results = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (results.next()) {
			return 1;
		} else {
			return 0;
		}
	}
	public int findPOI(String pid) throws Exception{
		Connector connector=new Connector();
		String query = String.format("select * from POI where pid =%s",pid);
		ResultSet results;
		try {
			results = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (results.next()) {
			return 1;
		} else {
			return 0;
		}
	}
	public String findfeedback(String fid) throws Exception{
		Connector connector=new Connector();
		String query = String.format("select * from feedback where pid =%s",fid);
		ResultSet results;
		try {
			results = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (results.next()) {
			return results.getString("login");
		} 
		else {
			return "0";
		}
	}
	public String try_to_login(String username, String password) throws Exception{
		if(islegalname(username)==0 ){
			return "Illegal username!";
		}
		if (password == null){
			return "Password can't be empty!";
		}
		Connector connector=new Connector();
		String pw=hashkey(password);
		String query = "select * from Users where login = '" + username + "' and password = '" + pw+"'";
		ResultSet results;
		try {
			results = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (results.next()) {
			if (results.getString("userType")=="admin") {  
				return "Welcome admin!";
			}
			else{
				return "Welcome user!";
			}
		} else {
			return "No such user!";
		}
	}

	public String try_to_reg(String login, String password, String name, String phone, String address) throws Exception{
		if(islegalname(login)==0 ){
			return "Illegal username!";
		}
		if (password == null){
			return "Password can't be empty!";
		}
		if(finduser(login)==1 ){
			return "This username already exists!";
		}

		String pw=hashkey(password); //encode password
		Connector connector=new Connector();
		String query = String.format("insert into Users (login, password, name, phone, address) values(\'%s\',\'%s\', \'%s\', \'%s\', \'%s\')",login, pw, name, phone, address);
		boolean results;
		try {
			results = connector.stmt.execute(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (results) {
			return "Successful registration!";
		} else {
			return "Unsuccessful registration!";
		}
	}
	
	public String try_visit(String login, String pid, String cost, String numberofheads, String year, String month, String day) throws Exception{
		if(islegalname(login)==0 || findPOI(pid)==0|| isfloat(cost)==0|| isnumber(numberofheads)==0 || isnumber(year)==0 || isnumber(month)==0 || isnumber(day)==0) {
			return "Illegal visit data!";
		}
		
		int n_pid=Integer.parseInt(pid);
		float n_cost=Float.parseFloat(cost);
		int n_head=Integer.parseInt(numberofheads);
		String visitdate=year+"-"+month+"-"+day;
		
		Connector connector=new Connector();
	
		String query= String.format("insert into VisEvent (cost, numberofheads) values(%lf, %d)",n_cost, n_head);
		
		boolean result;
		try {
			result = connector.stmt.execute(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (!result) {
			return "Error!";
		}
		int vid=-1;
		
		String query4 = "SELECT LAST_INSERT_ID()";

		ResultSet result4;
		result4 = connector.stmt.executeQuery(query4);
		if (result4.next()) {
			vid = Integer.parseInt(result4.getString(1));
		}
		
		String query3= String.format("insert into Visit (login, pid, vid, visitdate) values(\'%s\', %d, %d, \'%s\')",login, n_pid, vid ,visitdate);
		
		boolean result3;
		try {
			result3 = connector.stmt.execute(query3);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query3 + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (!result3) {
			return "Error!";
		}
		String recommendation=try_ask_for_suggestion(login,pid);
		return recommendation;
		
	}	
	
	public String try_favorite(String login, String pid) throws Exception{
		if(islegalname(login)==0 || findPOI(pid)==0) {
			return "Illegal favorite data!";
		}
		Connector connector=new Connector();
		Date dt=new Date();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //
        String fvdate= df.format(dt); //yyyy/MM/dd HH:mm:ss
		
		String query2= String.format("insert into Favorites (login, pid, fvdate) values(\'%s\', %d,\'%s\')",login, pid, fvdate);
		
		boolean result2;
		try {
			result2 = connector.stmt.execute(query2);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query2 + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (!result2) {
			return "Error!";
		}
		return "Successful!";
	}	
	
	public String try_feedback(String login, String pid, String score, String text) throws Exception{
		if(islegalname(login)==0 || findPOI(pid)==0|| isnumber(score)==0) {
			return "Illegal feedback data!";
		}
		Connector connector=new Connector();
		
		//one to one feedback
		String query1 = String.format("select * from feedback where login=\'%s\' and pid=%s",login,pid);
		ResultSet result1;
		try {
			result1 = connector.stmt.executeQuery(query1);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query1 + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (result1.next()) {
			return "The feedback already exists!";
		}
		
		Date dt=new Date();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); 
        String fbdate= df.format(dt); 
        
        
		String query2= String.format("insert into Feedback (login, pid, fddate, score, comment_text) values(\'%s\', %s, \'%s\', %s ,\'%s\')",login, pid, fbdate, score, text);
		
		boolean result2;
		try {
			result2 = connector.stmt.execute(query2);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query2 + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (!result2) {
			return "Error!";
		}
		return "Successful!";
	}	
	
	public String try_rate(String login, String fid, String rating) throws Exception{
		if(islegalname(login)==0 || findfeedback(fid)=="0"|| isnumber(rating)==0) {
			return "Illegal rating data!";
		}
		if (findfeedback(fid)==login){
			return "Can not rate your own feedback!";
		}
		Connector connector=new Connector();
		
		//where is already rate
		String query1 = String.format("select * from Rates where login=\'%s\' and fid=%s",login,fid);
		ResultSet result1;
		try {
			result1 = connector.stmt.executeQuery(query1);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query1 + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		
		if (result1.next()) {
			String query2= String.format("update Rates set rating=%d where login=\'%s\' and fid=%s",rating,login,fid);
			boolean result2;
			try {
				result2 = connector.stmt.execute(query2);
			} catch (Exception e) {
				System.err.println("Unable to execute query:" + query2 + "\n");
				System.err.println(e.getMessage());
				throw (e);
			}
			if (!result2) {
				return "Rating error!";
			}
		}
		else {
			String query2= String.format("insert into Rates (login, fid, rating) values(\'%s\', %s, %s)",login, fid, rating);
			
			boolean result2;
			try {
				result2 = connector.stmt.execute(query2);
			} catch (Exception e) {
				System.err.println("Unable to execute query:" + query2 + "\n");
				System.err.println(e.getMessage());
				throw (e);
			}
			if (!result2) {
				return "Rating error!";
			}
		}
		
		return "Successful!";
	}	
	
	public String try_trust(String login1, String login2, String istrusted) throws Exception{
		if(islegalname(login1)==0 || findfeedback(login2)=="0"|| isnumber(istrusted)==0) {
			return "Illegal trust data!";
		}
		Connector connector=new Connector();
		String query1 = String.format("select * from Trust where login1=\'%s\' and login2=\'%s\'",login1,login2);
		ResultSet result1;
		try {
			result1 = connector.stmt.executeQuery(query1);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query1 + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (result1.next()){
			String query2= String.format("update Trust set isTrusted=%s where login1=\'%s\' and login2=\'%s\'",istrusted,login1,login2);
			boolean result2;
			try {
				result2 = connector.stmt.execute(query2);
			} catch (Exception e) {
				System.err.println("Unable to execute query:" + query2 + "\n");
				System.err.println(e.getMessage());
				throw (e);
			}
			if (!result2) {
				return "Error!";
			}
		}
		else{
			String query2= String.format("insert into Trust (login1, login2, isTrusted) values(\'%s\', \'%s\', %s)",login1, login2, istrusted);
			
			boolean result2;
			try {
				result2 = connector.stmt.execute(query2);
			} catch (Exception e) {
				System.err.println("Unable to execute query:" + query2 + "\n");
				System.err.println(e.getMessage());
				throw (e);
			}
			if (!result2) {
				return "Error!";
			}
		}
		return "Successful!";
	}	
	
	public String try_query(String minprice, String maxprice, String city, String state, String category, String keyword, String Orderby, String direction) throws Exception{
		
		float Min=(float) -1e9;
		float Max=(float) 1e9;
		if(isfloat(minprice)==1) {
			Min=Float.parseFloat(minprice);
		}
		if(isfloat(maxprice)==1) {
			Max=Float.parseFloat(maxprice);
		}
		int dir=0;
		/*
		 * 0---descending order
		 * 1---ascending order
		 */
		if (direction!=null && isnumber(direction)==1 ){
			dir=Integer.parseInt(direction);
		}
		int orderby=0;
		/*
		 * 0----default  by price
		 * 1---- by the average numerical score of the feedbacks
		 * 2---- by the average numerical score of the trusted user feedbacks
		 */
		if (isnumber(Orderby)==1){
			orderby=Integer.parseInt(Orderby);
		}
		Connector connector=new Connector();
		String query=String.format("select * from POI poi where poi.price<=%s and poi.price >=%s",Max,Min);
		if (category!=null){
			query=query+String.format(" and poi.category = %s",normal(category));
		}
		if (city!=null){
			query=query+String.format(" and poi.city = %s",normal(city));
		}
		if (state!=null){
			query=query+String.format(" and poi.state = %s",normal(state));
		}
		if (keyword!=null){
			query=query+String.format(" and exist(select * from Keywords k1, HasKeywords h1 where k1.wid=h1.wid and h1.pid=poi.pid and k1.word=%s)",normal(keyword));
		}
		
		String part2=String.format("order by ");
		if (orderby==0){
			part2+="price";
		}
		else if (orderby==1){
			part2+="( select AVG(score) from feedback fb where fb.pid=poi.pid)";
		}
		else {
			part2+="( select AVG(score) from feedback fb where fb.pid=poi.pid and exist (select * from trust where login2=fb.login and isTrusted=1)  )";
		}
		
		if (dir==0){
			part2+="DESC";
		}
		
		query+=part2;
		ResultSet result;
		try {
			result = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		int limit=20;
		
		if (result.next()){
			String res=String.format("%-10s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-20s %-25s %-25s \n\n","pid","name","state","city","price","category","telephone","address","hours of operation","year of establishment","URL");
			for (int i=1;i<=limit;i++){
				String tmp=String.format("%-10s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-20s %-25s %-25s\n\n",
				result.getInt("pid"), result.getString("name"),result.getString("state"),result.getString("city"),
				result.getString("price"),result.getString("category"),result.getString("telephone"),
				result.getString("address"),result.getString("HOO"),result.getString("YOE"),result.getShort("URL"));
				
				res=res+tmp;
				if (!result.next()){
					break;
				}
			}
			res+="\n";
			return res;
		}
		else {
			return "No such POI!";
		}
		
		
	}	
	
	public String try_ask_for_feedback(String POI, String topn) throws Exception{
		int top=5;
		if (findPOI(POI)==0){
			return "No such feedback!";
		}
		if (topn!=null){
			top=Integer.parseInt(topn);
			if (top<0){
				top=5;
			}
		}
		
		Connector connector=new Connector();
		String query=String.format("select * from feedback fb where fb.pid=%s",POI);
		String part2="order by (select sum(rating) from Rates r where r.fid=fb.fid) DESC";
		query+=part2;
		ResultSet result;
		try {
			result = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		int limit=20;
		if (limit>top){
			limit=top;
		}
		
		if (result.next()){
			
			String res=String.format("%-10s %-10s %-20s %-5s %-20s %-50s\n\n","fid","pid","username","score","feedback date","comment");
			for (int i=1;i<=limit;i++){
				String tmp=String.format("%-10d %-10d %-20s %-5d %-20s %-50s\n\n",
				result.getInt("fid"), result.getInt("pid"),result.getString("login"),result.getInt("score"),
				result.getDate("fbdate"),result.getString("comment_text"));
				
				res=res+tmp;
				if (!result.next()){
					break;
				}
			}
			res+="\n";
			return res;
		}
		else {
			return "No such feedback!";
		}
		
}	

	public String try_ask_for_suggestion(String login, String pid) throws Exception{

		Connector connector=new Connector();
		String query=String.format("select * from POI poi where poi.pid!=%s ",pid);
		String part2=String.format("order by (select count * from Visit v where v.pid=poi.pid and exist(select * from Visit v2 where v2.login=v.login and v2.pid=%s))  DESC",pid);
		
		query+=part2;
		
		ResultSet result;
		try {
			result = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		int limit=5;
		
		if (result.next()){
			
			String res="Successful! \n Here are some recommendation POIs!:\n\n";
			res+=String.format("%-10s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-20s %-25s %-25s \n\n","pid","name","state","city","price","category","telephone","address","hours of operation","year of establishment","URL");
			for (int i=1;i<=limit;i++){
				String tmp=String.format("%-10s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-20s %-25s %-25s\n\n",
				result.getInt("pid"), result.getString("name"),result.getString("state"),result.getString("city"),
				result.getString("price"),result.getString("category"),result.getString("telephone"),
				result.getString("address"),result.getString("HOO"),result.getString("YOE"),result.getShort("URL"));
				
				res=res+tmp;
				if (!result.next()){
					break;
				}
			}
			res+="\n";
			return res;
		}
		else {
			return "Successful!";
		}
}	
	
	public ArrayList<String> find_one_degree(String login) throws Exception{
		Connector connector=new Connector();
		String query=String.format("select login from User u where u.login!=\'%s\' and exist(select * "
				+ "from Favorites f1, Favorites f2 where f1.login=u.login and f2.login =\'%s\' and f1.pid=f2.pid)",login,login);
		ResultSet result;
		try {
			result = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		ArrayList<String> res=new ArrayList<String>();
		while (result.next()){
			res.add(result.getString(login));
		}
		return res;
	}
	
	public String try_measure(String login1, String login2) throws Exception{
		int dis=10000000;
		if (login1==login2){
			dis=0;
		}
		else {
			HashMap<String,Integer> hash=new HashMap<String,Integer>();
			ArrayList<String> L=new ArrayList<String>();
			int cnt=0,head=0;
			L.add(login1);
			hash.put(login1,0);
			boolean not_find=true;
			while (head<=cnt && not_find)
			{
				String now=(String)L.get(head);
				int d=(int)hash.get(now);
				ArrayList<String> sons=find_one_degree(now);
				int m=sons.size();
				for (int i=0;i<m;i++){
					String next=(String)sons.get(i);
					if (next==login2){
						dis=d+1;
						not_find=false;
						break;
					}
					if (hash.get(next)==null){
						hash.put(next,d+1);
						L.add(next);
					}
				}
				head+=1;
			}
		}
		String res;
		if (dis<10000000){
			res=String.format("The distance between %s and %s is %d",login1,login2,dis); 
		}
		else {
			res=String.format("The distance between %s and %s is infinite",login1,login2); 
		}
		return res;
	}
	
	public String try_statistics(String Type, String m, String category) throws Exception{
		
		if (isnumber(Type)==0||isnumber(m)==0){
			return "Invalid statistics!";
		}
		Connector connector=new Connector();
		/*
		 * type: 0--popular
		 * 		 1--expensive
		 * 		 2--highly rated
		 */
		String query;
		int M=Integer.parseInt(m);
		int type=Integer.parseInt(Type);
		if (type==0)
		{
			query=String.format("select * from POI poi where poi.category=\'%s\' order by (select count * from Visit v where v.pid=poi.pid) DESC",category);
		}
		else if (type==1){
			query=String.format("select * from POI poi where poi.category=\'%s\' order by (select AVG(e.cost/e.numberofheads) from Visit v, VisEvent e where v.vid=e.vid and v.pid=poi.pid) DESC",category);
		}
		else {
			query=String.format("select * from POI poi where poi.category=\'%s\' order by (select AVG(fb.score) from feedback fb where fb.pid=poi.pid) DESC",category);
		}
		
		
		ResultSet result;
		try {
			result = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		
		if (result.next()){
			
			String res="Successful! \n Here are those POIs!:\n\n";
			res+=String.format("%-10s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-20s %-25s %-25s \n\n","pid","name","state","city","price","category","telephone","address","hours of operation","year of establishment","URL");
			for (int i=1;i<=M;i++){
				String tmp=String.format("%-10s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-20s %-25s %-25s\n\n",
				result.getInt("pid"), result.getString("name"),result.getString("state"),result.getString("city"),
				result.getString("price"),result.getString("category"),result.getString("telephone"),
				result.getString("address"),result.getString("HOO"),result.getString("YOE"),result.getShort("URL"));
				
				res=res+tmp;
				if (!result.next()){
					break;
				}
			}
			res+="\n";
			return res;
		}
		else {
			return "Successful!";
		}
}	
	
	public String try_new_poi(String name, String state, String city, String address, String URL, String tel, String YOE, String HOO, String price, String keywords, String category) throws Exception{
		if (isfloat(price)==0){
			return "Unsuccessful!";
		}
		float Price=Float.parseFloat(price);
		Connector connector=new Connector();
		String query=String.format("insert into POI (name, state, city, address, URL, telephone, YOE, HOO, price, category) "
				+ "values(\'%s\',\'%s\', \'%s\', \'%s\', \'%s\', \'%s\', \'%s\', \'%s\', %lf, \'%s\')",
				name, state, city, address, URL, tel, YOE, HOO, Price, category);
		boolean result;
		try {
			result = connector.stmt.execute(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (!result){
			return "Unsuccessful!";
		}
		
		int pid=-1;
		
		String query4 = "SELECT LAST_INSERT_ID()";

		ResultSet result4;
		result4 = connector.stmt.executeQuery(query4);
		if (result4.next()) {
			pid = Integer.parseInt(result4.getString(1));
		}
		else{
			return "Unsuccessful!";
		}
		String[] strarray=keywords.split("[, ]");
		for (String s:strarray)
		if (s!="," &&s!="" && s!=" ")
		{
			String query2=String.format("insert into Keywords(word, language) values (%d, English) ",normal(s),pid);
			boolean result2;
			try {
				result2 = connector.stmt.execute(query2);
			} catch (Exception e) {
				System.err.println("Unable to execute query:" + query2 + "\n");
				System.err.println(e.getMessage());
				throw (e);
			}
			if (!result2){
				return "Unsuccessful!";
			}
			
			int wid=-1;
			
			String query5 = "SELECT LAST_INSERT_ID()";

			ResultSet result5;
			result5 = connector.stmt.executeQuery(query5);
			if (result5.next()) {
				wid = Integer.parseInt(result5.getString(1));
			}
			else{
				return "Unsuccessful!";
			}
			
			query2=String.format("insert into HasKeywords(pid, wid) values (%d, %d) ",pid,wid);
			try {
				result2 = connector.stmt.execute(query2);
			} catch (Exception e) {
				System.err.println("Unable to execute query:" + query2 + "\n");
				System.err.println(e.getMessage());
				throw (e);
			}
			if (!result2){
				return "Unsuccessful!";
			}
		}
		
		return "successful!";
		
}	
	
	public String try_update_poi(String pid, String name, String state, String city, String address, String URL, String tel, String YOE, String HOO, String price, String keywords, String category) throws Exception{
		if (isfloat(price)==0 || findPOI(pid)==0){
			return "Unsuccessful!";
		}
		Connector connector=new Connector();
		String query=String.format("update POI set ");
		int change=0;
		if (name!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("name = \'%s\'",name);
			change=1;
		}
		
		if (state!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("state = \'%s\'",state);
			change=1;
		}
		
		if (city!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("city = \'%s\'",city);
			change=1;
		}
		
		if (address!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("address = \'%s\'",address);
			change=1;
		}
		
		if (URL!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("URL = \'%s\'",URL);
			change=1;
		}
		
		if (tel!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("telephone = \'%s\'",tel);
			change=1;
		}
		if (YOE!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("YOE = \'%s\'",YOE);
			change=1;
		}
		
		if (HOO!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("HOO = \'%s\'",HOO);
			change=1;
		}
		
		if (price!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("price = \'%s\'",price);
			change=1;
		}
		
		if (category!=null){
			if (change==1){
				query+=" , ";
			}
			query+=String.format("category = \'%s\'",category);
			change=1;
		}
		query+=String.format("where pid=%s",pid);
		
		boolean result;
		try {
			result = connector.stmt.execute(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (!result){
			return "Unsuccessful!";
		}
		
		String delete=String.format("delete from HasKeywords where pid=%s",pid);

		try {
			result = connector.stmt.execute(delete);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		if (!result){
			return "Unsuccessful Delete!";
		}
		
		String[] strarray=keywords.split("[, ]");
		for (String s:strarray)
		if (s!="," &&s!="" && s!=" ")
		{
			String query2=String.format("insert into Keywords(word, language) values (%s, English) ",normal(s),pid);
			boolean result2;
			try {
				result2 = connector.stmt.execute(query2);
			} catch (Exception e) {
				System.err.println("Unable to execute query:" + query2 + "\n");
				System.err.println(e.getMessage());
				throw (e);
			}
			if (!result2){
				return "Unsuccessful!";
			}
			
			int wid=-1;
			
			String query5 = "SELECT LAST_INSERT_ID()";

			ResultSet result5;
			result5 = connector.stmt.executeQuery(query5);
			if (result5.next()) {
				wid = Integer.parseInt(result5.getString(1));
			}
			else{
				return "Unsuccessful!";
			}
			
			query2=String.format("insert into HasKeywords(pid, wid) values (%s, %d) ",pid,wid);
			try {
				result2 = connector.stmt.execute(query2);
			} catch (Exception e) {
				System.err.println("Unable to execute query:" + query2 + "\n");
				System.err.println(e.getMessage());
				throw (e);
			}
			if (!result2){
				return "Unsuccessful!";
			}
		}
		
		return "successful!";
		
}	
	
	public String try_admin_statistics(String Type, String m) throws Exception{
		
		if (isnumber(Type)==0||isnumber(m)==0){
			return "Invalid statistics!";
		}
		Connector connector=new Connector();
		/*
		 * type: 0--trusted
		 * 		 1--useful
		 */
		String query;
		int M=Integer.parseInt(m);
		int type=Integer.parseInt(Type);
		if (type==0)
		{
			query=String.format("select * from Users u order by (select sum(isTrusted) from Trust t where t.login2=u.login) DESC");
		}
		else {
			query=String.format("select * from Users u order by (select AVG(select AVG(r.rating) from Rates r where r.fid=fb.fid) from feedback fb where fb.login=u.login) DESC");
		}
		
		
		ResultSet result;
		try {
			result = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		
		if (result.next()){
			
			String res="Successful! \n Here are those Users!:\n\n";
			res+=String.format("%-20s %-20s %-10s %-20s %-15s %-50s \n\n","username","name","userType","encoded password","phone","address");
			for (int i=1;i<=M;i++){
				String tmp=String.format("%-20s %-20s %-10s %-20s %-15s %-50s \n\n",
				result.getInt("login"), result.getString("name"),result.getString("userType"),result.getString("password"),
				result.getString("phone"),result.getString("address"));
				
				res=res+tmp;
				if (!result.next()){
					break;
				}
			}
			res+="\n";
			return res;
		}
		else {
			return "Successful!";
		}
}	
	
	public String show_Users() throws Exception{
		
		Connector connector=new Connector();
		String query;
		query=String.format("select * from Users u order by u.login");
		
		
		ResultSet result;
		try {
			result = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		int limit=20;
		if (result.next()){
			
			String res="Successful! \n Here are Users!:\n\n";
			res+=String.format("%-20s %-20s %-10s %-20s %-15s %-50s \n\n","username","name","userType","encoded password","phone","address");
			for (int i=1;i<=limit;i++){
				String tmp=String.format("%-20s %-20s %-10s %-20s %-15s %-50s \n\n",
				result.getInt("login"), result.getString("name"),result.getBoolean("userType"),result.getString("password"),
				result.getString("phone"),result.getString("address"));
				
				res=res+tmp;
				if (!result.next()){
					break;
				}
			}
			res+="\n";
			return res;
		}
		else {
			return "Successful!";
		}
}	
	
	public String show_feedbacks() throws Exception{
		
		Connector connector=new Connector();
		String query;
		query=String.format("select * from Feedback fb order by fb.fid");
		
		
		ResultSet result;
		try {
			result = connector.stmt.executeQuery(query);
		} catch (Exception e) {
			System.err.println("Unable to execute query:" + query + "\n");
			System.err.println(e.getMessage());
			throw (e);
		}
		int limit=20;
		if (result.next()){
			
			String res="Successful! \n Here are Feedbacks!:\n\n";
			res+=String.format("%-10s %-10s %-20s %-20s %-10s %-50s \n\n","fid","pid","username","fbdate","score","comment");
			for (int i=1;i<=limit;i++){
				String tmp=String.format("%-10d %-10d %-20s %-20s %-10s %-50s \n\n",
				result.getInt("fid"), result.getInt("pid"),result.getString("login"),result.getString("fbdate"),
				result.getInt("score"),result.getString("comment_text"));
				
				res=res+tmp;
				if (!result.next()){
					break;
				}
			}
			res+="\n";
			return res;
		}
		else {
			return "Successful!";
		}
}	
	
	public String getOrders(String attrName, String attrValue, Statement stmt) throws Exception{
		String query;
		String resultstr="";
		ResultSet results; 
		query="Select * from orders where "+attrName+"='"+attrValue+"'";
		try{
			results = stmt.executeQuery(query);
        } catch(Exception e) {
			System.err.println("Unable to execute query:"+query+"\n");
	                System.err.println(e.getMessage());
			throw(e);
		}
		System.out.println("Order:getOrders query="+query+"\n");
		while (results.next()){
			resultstr += "<b>"+results.getString("login")+"</b> purchased "+results.getInt("quantity") +
							" copies of &nbsp'<i>"+results.getString("title")+"'</i><BR>\n";	
		}
		return resultstr;
	}
	
	
}


