<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
	<head>
		<title>Setup ClusterControl</title>
		<link rel="stylesheet" href="<c:url value="/resources/blueprint/screen.css" />" type="text/css" media="screen, projection">
		<link rel="stylesheet" href="<c:url value="/resources/blueprint/print.css" />" type="text/css" media="print">
		<!--[if lt IE 8]>
			<link rel="stylesheet" href="<c:url value="/resources/blueprint/ie.css" />" type="text/css" media="screen, projection">
		<![endif]-->
		<link rel="stylesheet" href="<c:url value="/resources/popup.css" />" type="text/css" media="screen, projection">
		<script type="text/javascript" src="<c:url value="/resources/jquery-1.4.min.js" /> "></script>
		<script type="text/javascript" src="<c:url value="/resources/json.min.js" /> "></script>
	</head>
	<body>
		<div class="container">
			<h1>ClusterControl Setup</h1>
			<div class="span-12 last">
				<form:form modelAttribute="setupHost" action="cluster" method="post">
				  	<fieldset>
						<legend>Cluster Setup</legend>
						<p>
							<form:label	id="urlLabel" for="url" path="url" cssErrorClass="error">Url</form:label><br/>
							<form:input path="url" value="http://localhost:8080/balancer-manager" /><form:errors path="url" /><div id="status"></div>
						</p>
						<p>	
							<input id="create" type="submit" value="Create" />
						</p>
					</fieldset>
				</form:form>
			</div>
			<hr>	
			<ul>
				<li> <a href="?locale=en_us">us</a> |  <a href="?locale=en_gb">gb</a> | <a href="?locale=es_es">es</a> | <a href="?locale=de_de">de</a> </li>
			</ul>	
		</div>
		<div id="mask" style="display: none;"></div>
		<div id="popup" style="display: none;">
			<div class="span-8 last">
				<h3>Account Created Successfully</h3>
				<form>
					<fieldset>
						<p>
							<label for="url2">url</label><br/>
							<input id="url2" type="text" readonly="readonly" />		
						</p>
					</fieldset>
				</form>
				<a href="#" onclick="closePopup();">Close</a>			
			</div>			
		</div>		
	</body>

	<script type="text/javascript">	
		$(document).ready(function() {
 			$("#setupHost").submit(function() {
				var cluster = $(this).serializeObject();
				$.postJSON("cluster", cluster, function(data) {
					if (data.initStatus == 'ok') {
						$('#status').html("ok");
						location.reload(true);
					} else if (data.initStatus == 'nok') {
						$('#status').html("Failed to initialize with url");
					}
				});
				return false;
			});
 		});

		function fieldValidated(field, result) {
			if (result.valid) {
				$("#" + field + "Label").removeClass("error");
				$("#" + field + "\\.errors").remove();
				$('#create').attr("disabled", false);
			} else {
				$("#" + field + "Label").addClass("error");
				if ($("#" + field + "\\.errors").length == 0) {
					$("#" + field).after("<span id='" + field + ".errors'>" + result.message + "</span>");		
				} else {
					$("#" + field + "\\.errors").html("<span id='" + field + ".errors'>" + result.message + "</span>");		
				}
				$('#create').attr("disabled", true);					
			}			
		}

		function resetForm() {
			$('#account')[0].reset();
		}
		
	</script>
	
</html>
