<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
	<head>
		<title>ClusterControl View</title>
		<link rel="stylesheet" href="<c:url value="/resources/blueprint/screen.css" />" type="text/css" media="screen, projection">
		<link rel="stylesheet" href="<c:url value="/resources/blueprint/print.css" />" type="text/css" media="print">
		<link rel="stylesheet" href="<c:url value="/resources/cc.css" />" type="text/css" media="screen, projection">
		<!--[if lt IE 8]>
			<link rel="stylesheet" href="<c:url value="/resources/blueprint/ie.css" />" type="text/css" media="screen, projection">
		<![endif]-->
		<link rel="stylesheet" href="<c:url value="/resources/popup.css" />" type="text/css" media="screen, projection">
		<script type="text/javascript" src="<c:url value="/resources/jquery-1.7.1.min.js" /> "></script>
		<script type="text/javascript" src="<c:url value="/resources/json.min.js" /> "></script>
	</head>
	<body>
		<div class="container">
			<h1>ClusterControl View</h1>
			<div id="workerstable" class="span-12 last">
	
				<!-- header -->
				<div class="headers">
					<div id="col1" class="header" style="float: left;">worker</div>
<c:forEach items="${cluster.hostNames}" var="hostName">
					<div id="stat" class="header" style="float: left;"><c:out value="${hostName}"/></div>
</c:forEach>
				</div>
				<div style="clear: both;"></div>
				
				<!-- statuses -->
				<div class="workers">
<c:forEach items="${cluster.workers}" var="worker">
					<div id="col1" class="hostName"><c:out value="${worker.name}"/></div>
	<c:forEach items="${worker.statuses}" var="status">
					<div class="status ${status.hostName}-${worker.name}" id="stat"><c:out value="${status.status}"/></div>
	</c:forEach>
					<div class="status ${status.hostName}-${worker.name}" id="stat-btn">
						<input id="${worker.name}-disable" class="${worker.name}-disable" type="button" value="Disable" onclick="confirmAction('disable','${worker.name}')" disabled="disabled" />
					</div>
					<div class="status ${status.hostName}-${worker.name}" id="stat-btn">
						<input id="${worker.name}-enable" class="${worker.name}-enable" type="button" value="Enable" onclick="confirmAction('enable','${worker.name}')" disabled="disabled" />
					</div>
					<div style="clear: both;"></div>
</c:forEach>
				</div>
			</div>
			<div id="alternatives">
				<div>
					<span class="col1">AutoRefresh</span>
					On&nbsp;<input type="radio" id="ctrlautorefreshon" value="on" onmouseup="startTimer(this.value);" />
					Off&nbsp;<input type="radio" id="ctrlautorefreshoff" value="off" onmouseup="startTimer(this.value);" />
				</div>
				<div>
					<span class="col1">Manual Refresh</span><input value="Refresh" type="button" onclick="pollUpdate();" title="Refresh Status"/>
				</div>
				<div>
					<span class="col1">Status:</span>&nbsp;<span id="actionStatus"></span>
				</div>		
				<div>
					<span class="col1">Last Update:</span>&nbsp;<span id="lastPoll"></span>
				</div>
			</div>
			<div id="mask" style="display: none;"></div>
			<div id="popup" style="display: none;">
				<div class="span-10 last">
					<h3 id="actionHeader">Action:</h3>
					<div id="speed">
					<label>Activate: <input type="radio" name="enablerate" value="slow" title="Slow Activation" onmouseup="initiate(this.value);" />(S)low
					                 <input type="radio" name="enablerate" value="medium" title="Medium Activation" onmouseup="initiate(this.value);" />(M)edium
					                 <input type="radio" name="enablerate" value="aggressive" title="Fast Activation" onmouseup="initiate(this.value);" />(A)ggressive
					                 <input type="radio" name="enablerate" value="custom" title="Custom Activation" onmouseup="initiate(this.value);" />(C)ustom
									&nbsp;&nbsp;&nbsp;Disable: <input type="radio" name="enablerate" value="disable" title="Disable All" onclick="initiate(this.value);" />
					</label>
					</div>
					<a href="#" onclick="closePopup();">Close</a>
				</div>
			</div>
			<hr/>
			<ul>
				<li> <a href="?locale=en_us">us</a> |  <a href="?locale=en_gb">gb</a> | <a href="?locale=es_es">es</a> | <a href="?locale=de_de">de</a> </li>
			</ul>
		</div>
		<div>TODO</div>
		<div>
			<ul>
				<li>DONE! implement interfaces of ClusterManager and WorkerFactory</li>
				<li>implement error handling - no worker for url found</li>
				<li>implement error handling - error reading old worker setup</li>
				<li>layout - fix two column layout for "alternatives"</li>
				<li>layout - flowing layout for "alternatives"</li>
				<li>sign off</li>
			</ul>
		</div>
	</body>

	<script type="text/javascript">
	<!--
		var worker = '';
		var performAction = '';
		var triggerId = '';
		$(document).ready(function() {
	        //
	        // runs at body onload
	        triggerId = startTimer('on');
		});

		function pollUpdate() {
			// reset the actionStatus field
			$('#actionStatus').html("");
			$.getJSON("cluster/poll", { name: $('#name').val() }, function(cluster) {
				fieldUpdate(cluster);
			});
		}
		function fieldUpdate(cluster) {
			var retval = '';
			var workers = cluster.workers;
			
			for (i=0;i < workers.length;i++)
			{
				var statuses = workers[i].statuses;
				var workerName = workers[i].name;
				var lastStatus = workers[i].status;

				for (j=0;j < statuses.length;j++)
				{
					var status = statuses[j];
					var field = status.hostName+"-"+workerName;
					
					$("div.status."+field).html(status.status);
					
					if(status.status == "Ok") {
						$("div.status."+field).css("color", "green");
					} else if(status.status == "Dis") {
						$("div.status."+field).css("color", "red");
					} else if(status.status == "Dis Err") {
						$("div.status."+field).css("color", "red");
					} else {
						$("div.status."+field).css("color", "yellow");
					}
				}
				if(workers[i].status == "allDisabled") {
					// set button state - Disable #x
					$('#'+workerName+'-disable').attr('disabled', true);
					$('#'+workerName+'-enable').attr('disabled', false);
				} else if(workers[i].status == "allEnabled") {
					// set button state - Enable #x
					$('#'+workerName+'-disable').attr('disabled', false);
					$('#'+workerName+'-enable').attr('disabled', true);
				} else {
					// set button state - Enable all
					$('#'+workerName+'-disable').attr('disabled', false);
					$('#'+workerName+'-enable').attr('disabled', false);
				}
			}
			// add last poll time
			$('#lastPoll').html(cluster.lastPoll);
		}
    	function startTimer(toggle)
    	{
			if(toggle == 'on') {
    			$('#ctrlautorefreshoff').attr('checked', false);
	    		$('#actionStatus').html("Started autorefresh");
	    		triggerId = window.setInterval(pollUpdate, 10000);
	    		return triggerId;
			} else if(toggle == 'off') {
    			$('#ctrlautorefreshon').attr('checked', false);
	    		$('#actionStatus').html("Stopped autorefresh");
				window.clearInterval(triggerId);
			}
    	}
    	function confirmAction(action, workerName) 
    	{
			$('#actionHeader').html("Action: \'" + action + "\' worker \'" + workerName + "\'");
    		worker = workerName;
    		performAction = action;
    		
    		showPopup();
    	}
    	function initiate(speed) 
    	{
    		var text = "\'" + performAction + "\' worker \'" + worker + "\', "+speed + " speed";
    		if (confirm ("Do you want to "+text+"?")) {
    			$('#actionStatus').html("Performing " + text);

            	// perform action on this worker
     			$.getJSON("cluster/"+performAction+"/" + worker+"/"+speed, function(cluster) {
    				fieldUpdate(cluster);
    			});
    			closePopup();
    			return;
    		}
    		closePopup();
    		$('#actionStatus').html("Cancelled action " + text);
    	}
		function showPopup() {
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
			// reset all variables	
		}
    --></script>
	
</html>
