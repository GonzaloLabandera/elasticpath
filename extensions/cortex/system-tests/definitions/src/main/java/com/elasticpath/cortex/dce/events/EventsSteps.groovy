package com.elasticpath.cortex.dce.events

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.java.en.When

class EventsSteps {

	@When('^I POST an event (.+) to URI (.+)$')
	static void postEventToURI(String jsonInput, String eventsResource) {
		String uri = "/" + eventsResource
		client.POST(uri, jsonInput)
				.stopIfFailure()
	}

}
