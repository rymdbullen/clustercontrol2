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
			<h1>
				Setup ClusterControl
			</h1>
			<div class="span-12 last">	
				<form:form modelAttribute="setupHost" action="cluster" method="post">
				  	<fieldset>
						<legend>Cluster Setup</legend>
						<p>
							<form:label	id="urlLabel" for="url" path="url" cssErrorClass="error">Url</form:label><br/>
							<form:input path="url" /><form:errors path="url" />
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
			// check name availability on focus lost
			$('#name').blur(function() {
				if ($('#name').val()) {	
					checkAvailability();
				}
			});
/* 			$.getJSON("cluster/1", function(cluster) {
				var retval = '';
				var retval2 = '';
				for (i=0;i < cluster.length;i++)
				{
					var th = cluster[i];
					var workers = th.workers;
					retval += th.hostName+ ': ' + th.url + '\n';
					for (j=0;j < workers.length;j++) 
					{
						var worker = workers[j];
						retval += ' workerName:'+worker.workerName;
						retval += ' status    :'+worker.status;
						retval += ' lastStatus:'+worker.lastStatus + '\n';
					}
				}
				alert("retval="+retval);
				$("#url2").val(retval);
			});
			$('body').css('overflow','hidden');
			$('#popup').fadeIn('fast');
			$('#mask').fadeIn('fast');
 */			
			$("#account").submit(function() {
				var account = $(this).serializeObject();
				$.postJSON("account", account, function(data) {
					$("#assignedId").val(data.id);
					showPopup();
				});
				return false;				
			});

 			$("#setupHost").submit(function() {
				var cluster = $(this).serializeObject();
				$.postJSON("cluster", cluster, function(data) {
					//$("#url").val(data.initStatus);

					if (data.initStatus == 'ok') {
//						alert(data.initStatus);
						location.reload(true);
					//	$.getJSON("cluster", { url: $('#url').val() }, function() {
					//	});
					//	
					}
				});
				return false;
			});
 		});

		function checkAvailability() {
			$.getJSON("account/availability", { name: $('#name').val() }, function(availability) {
				if (availability.available) {
					fieldValidated("name", { valid : true });
				} else {
					fieldValidated("name", { valid : false, message : $('#name').val() + " is not available, try " + availability.suggestions });
				}
			});
		}

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

		function showPopup() {
			$.getJSON("account/" + $("#assignedId").val(), function(account) {
				$("#confirmedName").val(account.name);
				$("#confirmedBalance").val(account.balance);
				$("#confirmedEquityAllocation").val(account.equityAllocation);
				$("#confirmedRenewalDate").val(account.renewalDate);
			});			
			$('body').css('overflow','hidden');
			$('#popup').fadeIn('fast');
			$('#mask').fadeIn('fast');
		}
		
		function closePopup() {
			$('#popup').fadeOut('fast');
			$('#mask').fadeOut('fast');
			$('body').css('overflow','auto');
			resetForm();
		}

		function resetForm() {
			$('#account')[0].reset();
		}
		
	</script>
	
</html>
