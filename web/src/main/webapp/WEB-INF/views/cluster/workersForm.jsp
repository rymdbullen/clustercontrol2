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
		<script type="text/javascript" src="<c:url value="/resources/jquery-1.4.min.js" /> "></script>
		<script type="text/javascript" src="<c:url value="/resources/json.min.js" /> "></script>
	</head>
	<body>
		<div class="container">
			<h1>ClusterControl View</h1>
			<div id="workerstable" class="span-12 last">
	
				<!-- header -->
				<div class="headers">
					<div id="col1" class="header" style="float: left;">hostname</div>
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
</c:forEach>
				</div>
			</div>
			<div id="alternatives">
				<span>AutoRefresh</span>
				On&nbsp;<input type="radio" id="ctrlautorefresh" value="on"  onclick="convertToGetAndRelocate(this.value);" />
				Off&nbsp;<input type="radio" id="ctrlautorefresh" value="off"  onclick="convertToGetAndRelocate(this.value);" />
				<span>Manual Refresh</span>
				<input value="Refresh" type="button" onclick="pollUpdate();" disabled="disabled" title="Update Status table"/>
			</div>
			<div id="actionStatus"></div>
			<div id="mask" style="display: none;"></div>
			<div id="popup" style="display: none;">
				<div class="span-10 last">
					<h3 id="actionHeader">Action:</h3>
					<div id="acceleration">
					<label>Activate: <input type="radio" name="enablerate" value="slow" title="Slow Activation" onclick="initiate(this.value)" />(S)low
					                 <input type="radio" name="enablerate" value="medium" title="Medium Activation" onclick="initiate(this.value)" />(M)edium
					                 <input type="radio" name="enablerate" value="aggressive" title="Fast Activation" onclick="initiate(this.value)" />(A)ggressive
					                 <input type="radio" name="enablerate" value="custom" title="Custom Activation" onclick="initiate(this.value)" />(C)ustom
									&nbsp;&nbsp;&nbsp;Disable: <input type="radio" name="enablerate" value="disable" title="Deactivate" onclick="initiate(this.value)" />
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
	</body>

	<script type="text/javascript">	
		$(document).ready(function() {
	        //
	        // runs at body onload
	        startTimer();
		});

		function pollUpdate() {
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
					
					if(status.status == "ok") {
						$("div.status."+field).css("color", "green");
					} else if(status.status == "nok") {
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
		}
    	function startTimer()
    	{
			var url = window.location.search;
        	var ctrlautorefresh = document.getElementById('ctrlautorefresh');
			if(url.indexOf('autorefresh') > -1) {
            	timerID = setTimeout("refreshPeriodic()", interval*1000);
			} else {
				window.setInterval(pollUpdate, 10000);
	    		$('#actionStatus').html("Started timer ");
			}
    	}
    	function confirmAction(action, workerName) 
    	{
			$('#actionHeader').html("Action: \'" + action + "\' worker \'" + workerName + "\'");
    		worker = workerName;
    		performAction = action;
    		showPopup();
    	}
    	function initiate(acceleration) 
    	{
    		var answer = confirm ("Do you want to \'" + performAction + "\' worker \'" + worker + "\'?")
    		if (answer) {
    			$('#actionStatus').html("Performing \'" + performAction + "\' worker \'" + worker + "\'");
            	// do you want to activate this worker?
            	if(!confirmAction(performAction, worker)) return;
                
    			$.getJSON("cluster/"+performAction+"/" + worker, function(cluster) {
    				fieldUpdate(cluster);
    			});
    			closePopup()
    			return true;
    		}
    		$('#actionStatus').html("Cancelled action \'" + performAction + "\' worker \'" + worker + "\'");
			$('#actionStatus').fadeIn('fast');

    		return false;
    	}
		function showPopup() {
			alert(worker);
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
		var worker = '';
		var performAction = '';
    --></script>
	
</html>
