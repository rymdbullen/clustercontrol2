package net.local.clustercontrol.web;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class SetupHost {
	@NotNull
	@NotBlank
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
