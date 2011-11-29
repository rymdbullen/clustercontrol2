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
			<h1>
				ClusterControl View
			</h1>
			<div id="workerstable" class="span-12 last">
	
				<!-- header -->
				<div class="headers">
					<div id="col1" class="header" style="float: left;">hostname</div>
<c:forEach items="${cluster.workerNames}" var="workerName">
					<div id="stat" class="header" style="float: left;"><c:out value="${workerName}"/></div>
</c:forEach>
				</div>
				<div style="clear: both;"></div>
				
				<!-- statuses -->
				<div class="workers">
<c:forEach items="${cluster.workerHosts}" var="workerHost">
					<div id="col1" class="hostName"><c:out value="${workerHost.hostName}"/></div>
	<c:forEach items="${workerHost.workers}" var="worker">
					<div class="status ${workerHost.hostName}-${worker.workerName}" id="stat"><c:out value="${worker.status}"/></div>
	</c:forEach>
					<div style="clear: both;"></div>
</c:forEach>
				</div>
				<!-- footer -->
				<div class="headers">
					<div id="col1" class="header">&nbsp;</div>
<c:forEach items="${cluster.workerNames}" var="workerName">
			        <input id="${workerName}-disable" class="${workerName}-disable" type="button" value="Disable" onclick="disable(this.id)" disasbled="disabled" />
			        <input id="${workerName}-enable" class="${workerName}-enable" type="button" value="Enable" onclick="enable(this.id)" disasbled="disabled" />
</c:forEach>
				</div>
			</div>
			<div id="alternatives" class="refresh" style="background: whitesmoke;">
				<input id="btnStatusComplex" value="Refresh" type="button" onclick="pollUpdate();" disabled="disabled" title="Update Status table"/>
				<div id="autorefreshtext" style="display: none;">
					<span>AutoRefresh</span>
					On&nbsp;<input type="radio" id="ctrlautorefresh" value="on"  onclick="convertToGetAndRelocate(this.value);" />
					Off&nbsp;<input type="radio" id="ctrlautorefresh" value="off"  onclick="convertToGetAndRelocate(this.value);" />
				</div>
				<div id="acceleration" style="display: none;">
				<label>Activate: <input type="radio" name="enablerate" value="slow" title="Slow Activation" onclick="initiate(this.value)" />(S)low
				                 <input type="radio" name="enablerate" value="medium" title="Medium Activation" onclick="initiate(this.value)" />(M)edium
				                 <input type="radio" name="enablerate" value="aggressive" title="Fast Activation" onclick="initiate(this.value)" />(A)ggressive
				                 <input type="radio" name="enablerate" value="custom" title="Custom Activation" onclick="initiate(this.value)" />(C)ustom
								&nbsp;&nbsp;&nbsp;Disable: <input type="radio" name="enablerate" value="disable" title="Deactivate" onclick="initiate(this.value)" />
				</label>
				</div>
			</div>
				<div id="actionStatus"></div>
			<hr>
			<ul>
				<li> <a href="?locale=en_us">us</a> |  <a href="?locale=en_gb">gb</a> | <a href="?locale=es_es">es</a> | <a href="?locale=de_de">de</a> </li>
			</ul>
		</div>
	</body>

	<script type="text/javascript">	
		$(document).ready(function() {
			// do nothing yet
			
		});

		function pollUpdate() {
			$.getJSON("cluster/poll", { name: $('#name').val() }, function(cluster) {
				fieldUpdate(cluster);
			});
		}
		function fieldUpdate(cluster) {
			var retval = '';
			var workerHosts = cluster.workerHosts;
//alert('test'+workerHosts+' '+workerHosts.length);
			for (i=0;i < workerHosts.length;i++)
			{
				var workerHost = workerHosts[i];
				var workers = workerHost.workers;
				
//alert(workers.length);
//alert(workerHost.hostName);
//alert(workerHost.url);
				retval += workerHost.hostName+ ': ' + workerHost.url + '\n';
				for (j=0;j < workers.length;j++)
				{
					var worker = workers[j];
					
					var field = workerHost.hostName+"-"+worker.workerName;
					
					$("div.status."+field).html(worker.status);
					
					if(worker.status == "ok") {
						$("div.status."+field).css("color", "green");
					} else if(worker.status == "nok") {
						$("div.status."+field).css("color", "red");
					}
				}
			}
			//alert("retval="+retval);
			//$("#url2").val(retval);
		}
    	function atload() 
    	{
			var url = window.location.search;
        	var ctrlautorefresh = document.getElementById('ctrlautorefresh');
//alert('hej: '+ctrlautorefresh.size+' '+ctrlautorefresh.checked);
			if(url.indexOf('autorefresh') > -1) {
            	timerID = setTimeout("refreshPeriodic()", interval*1000);
			} else {
				window.setInterval(pollUpdate, 60000);
			}
    	}
    	function confirmAction(action, workerName) 
    	{
    		var answer = confirm ("Do you want to \'" + action + "\' worker \'" + workerName + "\'?")
    		if (answer) {
    			$('#actionStatus').html("Performing \'" + action + "\' worker \'" + workerName + "\'");
    			return true;
    		}
    		$('#actionStatus').html("Cancelled action \'" + action + "\' worker \'" + workerName + "\'");
			$('#actionStatus').fadeIn('fast');

    		return false;
    	}
        function enable(workerName)
        {
        	//alert(eleid);
        	// we have an id of the form "edit{id}", eg "edit42". We lookup the "42"
        	//var workerName = eleid.substring(6);
        	// do you want to activate this worker?
        	if(!confirmAction("activate", workerName)) return;
            
			$.getJSON("cluster/enable/" + workerName, function(cluster) {
				fieldUpdate(cluster);
			});
      	}
        function disable(workerName)
        {
        	//alert(eleid);
        	// we have an id of the form "edit{id}", eg "edit42". We lookup the "42"
        	//var workerName = eleid.substring(6);
        	// do you want to activate this worker?
        	if(!confirmAction("disable", workerName)) return;
        	
			$.getJSON("cluster/disable/"+workerName, function(cluster) {
				fieldUpdate(cluster);
			});
      	}

        //
        // runs at body onload
        window.onload=atload;
    --></script>
	
</html>
