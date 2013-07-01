package com.ravello.plugins.common.impl;

import com.ravello.plugins.common.Application;
import com.ravello.plugins.common.BlueprintService;
import com.ravello.plugins.common.BlueprintsRestService;
import com.ravello.plugins.exceptions.ApplicationCreateException;
import com.ravello.plugins.exceptions.BlueprintNotFoundException;

public class BlueprintServiceImpl implements BlueprintService {

	private BlueprintsRestService restService;

	@Override
	public Application createApplication(String blueprintName, String appName)
			throws BlueprintNotFoundException, ApplicationCreateException {
		Application blueprint = restService.findBlueprint(blueprintName);
		return createApplication(blueprint.getId(), appName);
	}

	@Override
	public Application createApplication(long blueprintId, String appName)
			throws ApplicationCreateException {
		try {
			return restService.createApplication(blueprintId, appName);
		} catch (Exception e) {
			throw new ApplicationCreateException(e);
		} catch (Throwable t) {
			throw new ApplicationCreateException(t);
		}
	}

	@Override
	public void setRestClient(BlueprintsRestService restService) {
		this.restService = restService;
	}

}
