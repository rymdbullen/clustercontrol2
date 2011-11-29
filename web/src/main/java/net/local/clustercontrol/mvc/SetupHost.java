package net.local.clustercontrol.mvc;

import javax.validation.constraints.NotNull;

public class SetupHost {
	@NotNull
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
