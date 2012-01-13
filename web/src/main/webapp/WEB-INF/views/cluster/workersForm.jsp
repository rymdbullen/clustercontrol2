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
			<c:choose>
			<c:when test="${cluster.hostNames == 1}">
			<div id="workerstable" class="span-12 last">
			</c:when>
			<c:when test="${cluster.hostNames > 1}">
			<div id="workerstable" class="span-13 last">
			</c:when>
			<c:otherwise>
			<div id="workerstable" class="span-12 last">
			</c:otherwise>
			</c:choose>
				<div class="headers">
					<div id="col1" class="header" style="float: left;">worker</div><c:forEach items="${cluster.hostNames}" var="hostName">
					<div id="stat" class="header" style="float: left;"><c:out value="${hostName}"/></div></c:forEach>
				</div>
				<div style="clear: both;"></div>
				
				<!-- statuses -->
				<div class="workers"><c:forEach items="${cluster.workers}" var="worker">
					<div id="col1" class="hostName"><c:out value="${worker.name}"/></div><c:forEach items="${worker.statuses}" var="status">
					<div class="status status-${status.id}" id="stat" title="${status.hostName}, ${status.route}, ${status.loadFactor}, ${status.transferred}"><c:out value="${status.status}"/></div></c:forEach>
					<div class="status btn-${worker.id}" id="stat-btn">
						<input id="${worker.id}-disable" class="${worker.id}-disable" type="button" value="Disable" onclick="confirmAction('disable','${worker.id}','${worker.name}')" disabled="disabled" />
					</div>
					<div class="status btn-${worker.id}" id="stat-btn">
						<input id="${worker.id}-enable" class="${worker.id}-enable" type="button" value="Enable" onclick="confirmAction('enable','${worker.id}','${worker.name}')" disabled="disabled" />
					</div>
					<div style="clear: both;"></div></c:forEach>
				</div>
			</div>
			<div id="alternatives" class="span-12 last">
				<div><span class="col1">AutoRefresh</span>
				On&nbsp;<input type="radio" id="ctrlautorefreshon" name="ctrlautorefreshon" value="on" onmouseup="startTimer(this.value);" />
				Off&nbsp;<input type="radio" id="ctrlautorefreshoff" name="ctrlautorefreshoff" value="off" onmouseup="startTimer(this.value);" /></div>
				<div><span class="col1">Manual Refresh</span><input value="Refresh" type="button" onclick="pollUpdate();" title="Refresh Status"/></div>
				<div><span class="col1">Status:</span><span id="actionStatus">&nbsp;</span></div>
				<div><span class="col1">Last Update:</span><span id="lastPoll">&nbsp;</span></div>
				<div><span class="col1">Locale</span> <a href="?locale=en_us">us</a> |  <a href="?locale=en_gb">gb</a> | <a href="?locale=es_es">es</a> | <a href="?locale=de_de">de</a> </div>
			</div>
			<div id="mask" style="display: none;"></div>
			<div id="popup" style="display: none;">
				<div class="span-10 last">
					<h3 id="actionHeader">Action:</h3>
					<div id="speed">
						<form name="dummyForm" method="get" action="" onsubmit="return false;">
							<fieldset>
								<legend>Initiate Action:</legend>
							    <label onmouseup="initAction('slow');"><input type="radio" name="enablerate" value="slow" title="Slow Activation" onmouseup="initAction(this.value);" />(S)low</label><br/>
							    <label onmouseup="initAction('medium');"><input type="radio" name="enablerate" value="medium" title="Medium Activation" onmouseup="initAction(this.value);" />(M)edium</label><br/>
							    <label onmouseup="initAction('aggressive');"><input type="radio" name="enablerate" value="aggressive" title="Fast Activation" onmouseup="initAction(this.value);" />(A)ggressive</label><br/>
							    <label onmouseup="initAction('custom');"><input type="radio" name="enablerate" value="custom" title="Custom Activation" onmouseup="initAction(this.value);" />(C)ustom</label><br/>
							</fieldset>
						</form>
					</div>
					<span>
						<input id="initiate-nok" class="" type="button" value="Cancel" onmouseup="cancelAction();" />
					</span>
					<span id="speed-buttons" style="display: none;">
						<input id="initiate-ok" class="" type="button" value="Initiate" onmouseup="performAction();" />
					</span>
				</div>
			</div>
			<hr/>
			<div>TODO</div>
			<div>
				<ul>
					<li>Test: multiple hosts and more than two workers per host</li>
					<li>mod_jk is not implemented</li>
					<li>Bug! implement startup initialization of the view, e.g. enable/disable buttons</li>
					<li>Bug! layout - fix layout for body mask</li>
					<li>STARTED: implement error handling - error reading old worker setup</li>
					<li>sign off</li>
				</ul>
			</div>
			<div>DONE</div>
			<div>
				<ul>
					<li>DONE! implement error handling - no worker for url found</li>
					<li>DONE! Bug! httpclient does not handle 404</li>
					<li>DONE! layout - fix two column layout for "alternatives"</li>
					<li>DONE! layout - flowing layout for "alternatives"</li>
					<li>DONE! clear radio buttons on cancel</li>
					<li>DONE! layout - fix label for radio buttons on body mask</li>
					<li>DONE! implement interfaces of ClusterManager and WorkerFactory</li>
					<li>DONE! Bug: When only one host disable/enable buttons dont match</li>
				</ul>
			</div>
		</div>
	</body>

	<script type="text/javascript">
	<!--
		var worker = '';
		var choosenSpeed = '';
		var actionToPerform = '';
		var triggerId = '';
		$(document).ready(function() {
	        //
	        // runs at body onload
	        triggerId = startTimer('on');
		});

		function pollUpdate() {
			// reset the actionStatus field
			$('#actionStatus').html("-");
			$.getJSON("cluster/poll", { name: $('#name').val() }, function(cluster) {
				fieldUpdate(cluster);
			});
		}
		function fieldUpdate(cluster) {
			var workers = cluster.workers;
//alert('workers.length='+workers.length);
			for (i=0;i < workers.length;i++)
			{
				var statuses = workers[i].statuses;
				var workerName = workers[i].id;
				var lastStatus = workers[i].status;
//alert('statuses.length='+statuses.length);
				for (j=0;j < statuses.length;j++)
				{
					var status = statuses[j];
					var field = "status-"+status.id;
//if(j==1 && i==1) { alert(field); }
					$("div.status."+field).html(status.status);
//if(j==1 && i==1) { alert( $("div.status."+field).html()+" "+status.status); }
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
    			$('#ctrlautorefreshon').attr('checked', true);
    			$('#ctrlautorefreshoff').attr('checked', false);
	    		$('#actionStatus').html("Started autorefresh");
	    		triggerId = window.setInterval(pollUpdate, 60000);
	    		return triggerId;
			} else if(toggle == 'off') {
    			$('#ctrlautorefreshoff').attr('checked', true);
    			$('#ctrlautorefreshon').attr('checked', false);
	    		$('#actionStatus').html("Stopped autorefresh");
				window.clearInterval(triggerId);
			}
    	}
    	function confirmAction(action, workerName, workerDisplayName) 
    	{
			$('#actionHeader').html("Action: \'" + action + "\' worker \'" + workerDisplayName + "\'");
    		worker = workerName;
    		actionToPerform = action;
    		
    		showPopup();
    	}
    	function initAction(speed) 
    	{
			$('#speed-buttons').fadeIn('fast');
			choosenSpeed = speed
    		var radioObj = $("#enablerate");
    		setCheckedValue(radioObj, speed);
    	}
    	function performAction()
    	{
    		var text = "\'" + actionToPerform + "\' worker \'" + worker + "\', "+choosenSpeed + " speed";
   			$('#actionStatus').html("Performing " + text);

           	// perform action on this worker
   			$.getJSON("cluster/"+actionToPerform+"/" + worker+"/"+speed, function(cluster) {
   				fieldUpdate(cluster);
   			});
   			radioObj = document.forms['dummyForm'].elements['enablerate'];
   			closePopup();
    		setCheckedValue(radioObj, '');
   			return;
    	}
    	function cancelAction() 
    	{
    		closePopup();
    		radioObj = document.forms['dummyForm'].elements['enablerate'];
    		setCheckedValue(radioObj, '');
    		$('#speed-buttons').fadeOut('fast');
    		$('#actionStatus').html("Action Cancelled for " + worker);
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

		// set the radio button with the given value as being checked
		// do nothing if there are no radio buttons
		// if the given value does not exist, all the radio buttons
		// are reset to unchecked
		function setCheckedValue(radioObj, newValue) {
			if(!radioObj) {
				return;
			}
			var radioLength = radioObj.length;
			if(radioLength == undefined) {
				radioObj.checked = (radioObj.value == newValue.toString());
				return;
			}
			for(var i = 0; i < radioLength; i++) {
				radioObj[i].checked = false;
				if(radioObj[i].value == newValue.toString()) {
					radioObj[i].checked = true;
				}
			}
		}
    --></script>
	
</html>
