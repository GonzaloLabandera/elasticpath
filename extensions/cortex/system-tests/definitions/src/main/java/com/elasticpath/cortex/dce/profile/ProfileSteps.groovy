package com.elasticpath.cortex.dce.profile

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE
import static com.elasticpath.cortex.dce.SharedConstants.FAMILY_NAME
import static com.elasticpath.cortex.dce.SharedConstants.GIVEN_NAME
import static com.elasticpath.cortex.dce.SharedConstants.PASSWORD
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Profile
import com.elasticpath.cortexTestObjects.Registration

class ProfileSteps {

	def static newUserName

	@When('^I create a new shopper profile with family-name (.+), given-name (.+), password (.+), and unique user name$')
	static void createNewShopperProfile(String familyName, String givenName, String password) {
		newUserName = generateUniqueEmail()
		registerShopper(DEFAULT_SCOPE, familyName, givenName, password, newUserName)
	}

	@When('^I authenticate with newly created shopper$')
	static void authenticateWithNewShopper() {
		client.authRegisteredUserByName(DEFAULT_SCOPE, newUserName)
	}

	@When('^I create a new shopper profile with family-name (.*), given-name (.*), password (.*), and user name (.+) in scope (.+)$')
	static void createNewShopperProfileInScope(String familyName, String givenName, String password, String existingUserName, String scope) {
		registerShopper(scope, familyName, givenName, password, existingUserName)
	}

	@When('^I create a new shopper profile in scope (.+)$')
	static void createNewShopper(String scope){
		newUserName = generateUniqueEmail()
		registerShopper(scope, FAMILY_NAME, GIVEN_NAME, PASSWORD, newUserName)
				.stopIfFailure()
	}

	@When('^I authenticate with newly created shopper in scope (.+)$')
	static void authNewShopper(String scope){
		client.authRegisteredUserByName(scope, newUserName)
				.stopIfFailure()
	}

	@Then('^I should see my profile name as family-name (.+) and given-name (.+)$')
	static void verifyProfileFields(String familyName, String givenName) {
		client.GET("/")
				.defaultprofile()
				.stopIfFailure()
		assertThat(client["family-name"])
				.as("Family name is not as expected")
				.isEqualTo(familyName)
		assertThat(client["given-name"])
				.as("Given name is not as expected")
				.isEqualTo(givenName)
	}

	/**
	 * The OOTB Cortex behaviours is that in order to update email, user needs to use createemailaction
	 * which will replace the old email value.
	 */
	@When('^(?:I create my email id|I update my email id with new email) and I can see the new email id in my profile$')
	static void updateProfileEmail() {
		def userName = UUID.randomUUID().toString() + "@elasticpath.com"
		Profile.addEmail(userName)
		assertThat(client["email"])
				.as("The email is not as expected")
				.isEqualTo(userName)
	}

	@When('^I create invalid email (.*)$')
	static void createInvalidEmail(String email) {
		Profile.addEmailWithoutFollow(email)
	}

	@When('^I update my profile family-name (.*) and given-name (.*)$')
	static void updateProfileNames(String familyName, String givenName) {
		Profile.updateProfile(familyName, givenName)
	}

	@When('^I authenticate as another shopper and attempt to update the other shoppers profile family-name (.+) and given-name (.+)$')
	static void updateProfileWithAnotherShopperID(String familyName, String givenName) {
		Profile.getProfile()
		def profileURI = client.body.self.uri
		client.authAsRegisteredUser()
		Profile.updateProfile(profileURI, familyName, givenName)
	}

	@When('^I POST to registration with body (.+)$')
	static void postToRegistration(String jsonInput) {
		Registration.registerWithoutFollow(jsonInput)
	}

	@When('^I PUT to profile with json body (.+)$')
	static void putToProfile(String jsonInput) {
		Profile.updateProfile(jsonInput)
	}

	static def registerShopper(registrationScope, familyName, givenName, password, username) {
		client.authAsAPublicUser(registrationScope)

		client.GET("registrations/$registrationScope/newaccount/form")
				.registeraction("family-name": familyName, "given-name": givenName, "password": password, "username": username)
	}

	static def generateUniqueEmail() {
		return UUID.randomUUID().toString() + "@elasticpath.com"
	}
}
