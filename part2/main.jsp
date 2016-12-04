<%@page language = "java" import = "acmdb.*" %>
<html>
<head>
<script LANGUAGE = "javascript">
function check_login(form_obj) {
	if(form_obj.username == "") {
		alert("username should be specified.");
		return false;
	}else if(form_obj.password == "") {
		alert("password should be specified.");
		return false;
	}else {
		return true;
	}
}
</script>
</head>
<body>
<%
boolean flag = false;
String newuser = request.getParameter("newuser");
String password = request.getParameter("newpass");
String username;
String password;
if(newuser != null) {
	flag = true;
	boolean regSuccess = try_to_reg(newuser, newpass);
	if(regSuccess) {
	%><BR><BR>register success<BR><BR><%
	}else {
	%><BR><BR>register unsuccess<BR><BR><%
	}
}else {
	username = request.getParameter("username");
	password = request.getParameter("password");
	if(username == null) {
		flag = true;
	}
	Order order = new Order();
	
	int loginSuccess = order.try_to_login(username, password);//0: unsuccessful, 1: user, 2: admin
	out.println(username + password + loginSuccess);
	if(loginSuccess == 0) {
		flag = true;
	}
}
if(flag) {
%>
<BR><BR>
Please input your username and password:
<form name = "username" method = post onsubmit = "return check_login(this)" action = "main.jsp">
	<input type = text name = "username" length = 16>
	<input type = password name = "password" length = 16>
	<input type = submit>
</form>
Or register:
<form name = "reg" method = post onsubmit = "return check_login(this)" action = "main.jsp">
	<input type = text name = "newuser" length = 16>
	<input type = password name = "newpass" length = 16>
	<input type = submit>
</form>
<%
} else {
	%>
	<BR><BR> login as <% out.println(username); %> <BR><BR>
	<%
	if(request.getParameter("visit") != null) {
		boolean success = try_visit(username, request.getParameter("visit_poi"));
		if(success) {
			%> <BR><BR> Update(visit) successful! <BR><BR> <%
		}else {
			%> <BR><BR> Update(visit) unsuccessful(check poi name) <BR><BR> <%
		}
	}else if(request.getParameter("favorite") != null) {
		boolean success = try_favorite(username, request.getPatameter("favorite_poi"));
		if(success) {
			%> <BR><BR> Update(set favorite poi) successful! <BR><BR> <%
		}else {
			%> <BR><BR> Update(set favorite poi) unsuccessful(check poi name) <BR><BR> <%
		}
	}else if(request.getParameter("feedback") != null) {
		boolean success = try_feedback(username, request.getParameter("feedback_poi"), request.getParameter("feedback_date"), request.getParameter("feedback_score"), request.getParameter("feedback_text"));
		if(success) {
			%> <BR><BR> feedback submitted successfully! <BR><BR> <%
		}else {
			%> <BR><BR> feedback invalid(check poi name, score must be in 0 and 10 and be an integer, you can only give a poi one feedback and are not allowed to update it) <BR><BR> <%
		}
	}else if(request.getParameter("rate") != null) {
		boolean success = try_rate(username, request.getParameter("rate_feedbackid"), request.getParameter("rate_rating"));
		if(success) {
			%> <BR><BR> rate successful! <BR><BR> <%
		}else {
			%> <BR><BR> rate unsuccessful(check feedback id, rating must be one of 0, 1, 2(meaning useless, useful, very useful, respectively) <BR><BR> <%
		}
	}else if(request.getParameter("trust") != null) {
		boolean success = try_trust(username, request.getParameter("trust_user"));
		if(success) {
			%> <BR><BR> trust successful! <BR><BR> <%
		}else {
			%> <BR><BR> trust unsuccessful(check username, you can't trust yourself) <BR><BR> <%
		}
	}else if(request.getParameter("query") != null) {
		String result = try_query(request.getParameter("query_type"), request.getParameter("query_key"), request.getParameter("query_sort_by"));
		if(result != null) {
			out.println(result);
		}else {
			%> <BR><BR> invalid query(check arguments) <BR><BR> <%
		}
	}else if(request.getParameter("ask_for_feedback") != null) {
		String result = try_ask_for_feedback(request.getParameter("ask_for_feedback_poi"));
		if(result != null) {
			out.println(result);
		}else {
			%> <BR><BR> invalid asking for feedback(check poi name) <BR><BR><%
		}
	}else if(request.getParameter("ask_for_suggestion") != null) {
		String result = try_ask_for_suggestion(username);
		if(result != null) {
			out.println(result);
		}else {
			%> <BR><BR> invalid asking for suggestion(i don't know why!) <BR><BR> <%
		}
	}else if(request.getParameter("measure") != null) {
		String result = try_measure(request.getParameter("measure_user1"), request.getParameter("measure_user2"));
		if(result != null) {
			out.println(result);
		}else {
			%> <BR><BR> invalid measure request(check usernames) <BR><BR> <%
		}
	}else if(request.getParameter("statistics") != null) {
		String result = try_statistics();
		if(result != null) {
			out.println(result);
		}else {
			%> <BR><BR> statistics ????? (why?!) <BR><BR> <%
		}
	}else if(request.getParameter("admin_new_poi") != null) {
		boolean success = try_new_poi(request.getParameter("admin_new_poi_name"), request.getParameter("admin_new_poi_address"), request.getParameter("admin_new_poi_url"), request.getParameter("admin_new_poi_tel"), request.getParameter("admin_new_poi_year"), request.getParameter("admin_new_poi_hour"), request.getParameter("admin_new_poi_price"), request.getParameter("admin_new_poi_keywords"), request.getParameter("admin_new_poi_category"));
		if(success) {
			%> <BR><BR> POI updated! <BR><BR> <%
		}else {
			%> <BR><BR> POI update unsuccessful(check arguments) <BR><BR> <%
		}
	}else if(request.getParameter("admin_statistics") != null) {
		String result = try_admin_statistics();
		if(result != null) {
			out.println(result);
		}else {
			%> <BR><BR> unknown error <BR><BR> <%
		}
	}else if(request.getParameter("fun") != null) {
		String result = try_fun();
		if(result != null) {
			out.println(result);
		}else {
			%><BR><BR> not fun <BR><BR> <%
		}
	}
	%>
	<BR><BR> logout <BR><BR>
	<form name = "logout" method = post onsubmit = "return true" action = "main.jsp">
		<input type = submit>
	</form>
	
	<BR><BR> fun <BR><BR>
	<form name = "funForm" method = post onsubmit = "return true" action = "main.jsp">
		<input type = hidden name = "fun" value = "0">
		<input type = submit>
	</form>
	<BR><BR> Enter a poi you have recently visited: <BR><BR>
	<form name = "visitForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "visit_poi" length = 16>
	<input type = hidden name = "visit" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
</form>
	
	<BR><BR> Enter your favorite poi: <BR><BR>
	<form name = "favoriteForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "favorite_poi" length = 16>
	<input type = hidden name = "favorite" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >

	<input type = submit>
</form>

	<BR><BR> Enter a feedback on poi you have recently visited: <BR><BR>
	<form name = "feedbackForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "feedback_poi" length = 16>
	<input type = text name = "feedback_date" length = 16>
	<input type = text name = "feedback_score" length = 16>
	<input type = text name = "feedback_text" length = 16>
	<input type = hidden name = "feedback" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>


	<BR><BR> Enter a rating on an other's feedback: <BR><BR>
	<form name = "rateForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "rate_feedback_id" length = 16>
	<input type = text name = "rate_rating" length = 16>
	<input type = hidden name = "rate" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>
	
	<BR><BR> Enter user to trust him: <BR><BR>
	<form name = "trustForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "trust_user" length = 16>
	<input type = hidden name = "trust" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>

	<BR><BR> Find POI((1)type: 0 for price range, 1 for address matching, 2 for name matching, 3 for category; (2)content: write a, b for price range and plain text for the other type of queries; (3)sort result: 1 by price, 2 by average feedback score, 3 by average feedback score of your trusted users): <BR><BR>
	<form name = "queryForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "query_type" length = 16>
	<input type = text name = "query_key" length = 16>
	<input type = text name = "query_sort_by" length = 16>
	<input type = hidden name = "query" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>
	
	<BR><BR> Ask for useful feedbacks for a POI: <BR><BR>
	<form name = "ask_for_feedbackForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "ask_for_feedback_poi" length = 16>
	<input type = hidden name = "ask_for_feedback" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>

	<BR><BR> Ask for visiting suggestions: <BR><BR>
	<form name = "ask_for_suggestionForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = hidden name = "ask_for_suggestion" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>

	<BR><BR> Degree of seperation for two users: <BR><BR>
	<form name = "measureForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "measure_user1" length = 16>
	<input type=  text name = "measure_user2" length = 16>
	<input type = hidden name = "measure" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>

	<BR><BR> Statistics: <BR><BR>
	<form name = "statisticsForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = hidden name = "statistics" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>

	<% if(successful == 2) { %>
	<BR><BR> Admin statistics: <BR><BR>
	<form name = "admin_statisticsForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "measure_user1" length = 16>
	<input type=  text name = "measure_user2" length = 16>
	<input type = hidden name = "admin_statistics" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>

	<BR><BR> new or update poi information(name, address, url, tel, year, hour, price, keywords, category): <BR><BR>
	<form name = "admin_new_poiForm" method = post onsubmit = "return true" action = "main.jsp">
	<input type = text name = "admin_new_poi_name" length = 16>
	<input type = text name = "admin_new_poi_address" length = 16>
	<input type = text name = "admin_new_poi_url" length = 16>
	<input type = text name = "admin_new_poi_tel" length = 16>
	<input type = text name = "admin_new_poi_year" length = 16>
	<input type = text name = "admin_new_poi_hour" length = 16>
	<input type = text name = "admin_new_poi_price" length = 16>
	<input type = text name = "admin_new_poi_keywords" length = 16>
	<input type = text name = "admin_new_poi_category" length = 16>
	<input type = hidden name = "admin_new_poi" value = "0">
	<input type = hidden name = "username" value = <% out.println(username); %> >
	<input type = hidden name = "password" value = <% out.println(password); %> >
	<input type = submit>
	</form>
	<%}%>
	//user functions here, must post username and password implicitly.
	//following by admin functions!
	<%
}
%>
</body>
	
