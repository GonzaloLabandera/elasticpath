package com.elasticpath.definitions.stateobjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Object to pass the state of test objects.
 */
public class ObjectsContext {

	private final List<Projection> projections = new ArrayList<>();

	public Projection getProjection() {
		return projections.get(projections.size() - 1);
	}

	/**
	 * Adds projection to the collection of created projections.
	 *
	 * @param newProjection projection object which should be added
	 */
	public void addProjection(final Projection newProjection) {
		Projection projection = new Projection();
		projection.setCode(newProjection.getCode());
		projection.setStore(newProjection.getStore());
		projection.setProjectionDateTime(newProjection.getProjectionDateTime());
		projection.setVersion(newProjection.getVersion());
		projection.setDeleted(newProjection.getDeleted());
		projection.setSchemaVersion(newProjection.getSchemaVersion());
		projection.setContentHash(newProjection.getContentHash());
		projection.setContent(newProjection.getContent());
		projections.add(projection);
	}

	/**
	 * Updates previously created  projection with new values.
	 *
	 * @param newProjection projection object which contains new values
	 */
	public void updateProjection(final Projection newProjection) {
		for (Projection projection : projections) {
			if (projection.getCode().equals(newProjection.getCode())) {
				projection.setProjectionDateTime(newProjection.getProjectionDateTime());
				projection.setVersion(newProjection.getVersion());
				projection.setDeleted(newProjection.getDeleted());
				projection.setSchemaVersion(newProjection.getSchemaVersion());
				projection.setContentHash(newProjection.getContentHash());
				projection.setContent(newProjection.getContent());
			}
		}
	}

	/**
	 * Returns a collection of added projection objects.
	 *
	 * @return a collection of added projection objects
	 */
	public List<Projection> getProjections() {
		return this.projections;
	}

	/**
	 * Returns firstly added projection object.
	 *
	 * @return firstly added added projection object
	 */
	public Projection getFirstAddedProjection() {
		return this.projections.get(0);
	}

	/**
	 * Returns previously added projection object.
	 *
	 * @return previously added projection object
	 */
	public Projection getPreviousProjection() {
		int previousElementOffset = 2;
		if (projections.size() < previousElementOffset) {
			return getFirstAddedProjection();
		}
		return this.projections.get(projections.size() - previousElementOffset);
	}

	/**
	 * Returns a collection of the codes of all added projections.
	 *
	 * @return a collection of the codes of all added projections
	 */
	public List<String> getProjectionsCodes() {
		List<String> codes = new ArrayList<>();
		for (Projection projection : this.projections) {
			codes.add(projection.getCode());
		}
		return codes;
	}

	/**
	 * @param code code which is used to find projection.
	 * @return projection object by provided code and null if there was no such a projection.
	 */
	public Projection getProjection(final String code) {
		for (Projection projection : projections) {
			if (code.equals(projection.getCode())) {
				return projection;
			}
		}
		return null;
	}
}
