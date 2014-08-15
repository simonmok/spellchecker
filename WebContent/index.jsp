<%@ page import="com.simonmok.spell.*" %>
<%
	final String passage = request.getParameter("text");
	final String jsonString = SpellCheckUtil.getJsonSuggestion(passage, SpellCheckUtil.SearchOption.FULL_DICTIONARY);
%>
<html>
<head>
	<title>Spell Checker</title>
	<script type="text/javascript" src="resources/js/jquery-1.11.1.min.js"></script>
	<script type="text/javascript">
		var suggestions = <%=jsonString%>;
		var cursor = 0;
		var total = 0;
    	$(document).ready(function () {
    		init();
    	});
	</script>
	<script type="text/javascript" src="resources/js/spell.js"></script>
	<link rel="stylesheet" type="text/css" href="resources/css/style.css"/>
</head>

<body>
	<form action="index.jsp" method="post">
		<h1>Spell Checker</h1>
		<textarea id="text" name="text" style="width: 100%" rows="10" required><%=passage == null ? "" : passage%></textarea>
		<div align="right">
			<input type="submit" value="Check Spelling" class="button"/>
		</div>
	</form>
	<table class="spellTable" style="display: none">
		<tr>
			<td style="width: 300px">
				<table>
					<tr>
						<td valign="top" class="column"><label for="original">Word:</label></td>
						<td><input type="text" id="original" size="15" style="width: 100%" readonly="readonly"/></td>
					</tr>
					<tr>
						<td valign="top"><label for="suggestion">Suggestions:</label></td>
						<td>
							<select size="<%=SpellCheckUtil.MAX_SUGGESTION%>" style="width: 100%" id="suggestion">
							</select>
						</td>
					</tr>
					<tr>
						<td valign="top">
							<input type="button" id="change" value="Change Word" onclick="change()" class="action"/>
						</td>
						<td>
							<input type="button" id="ignore" value="Ignore Suggestion" onclick="ignore()" class="action"/>
						</td>
					</tr>
				</table>
			</td>
			<td valign="top" class="preview">
				<label for="preview">Preview:</label><br/>
				<span id="preview"></span>
			</td>
		</tr>
		<tr>
			<td valign="top">
				Reviewing <span id="cursor"></span> of <span id="total"></span> word(s)
			</td>
			<td>
				<div align="right">
					<input type="button" value="Apply Changes" onclick="apply()" class="button"/>
				</div>
			</td>
		</tr>
	</table>
</body>
</html>